package com.example.neham.myapplication;

import android.app.Activity;
//import android.graphics.Camera;
import android.hardware.Camera;
//import android.hardware.camera2;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    SensorManager sm;
    Sensor accelerometer, lightSensor;
    TextView acceleration;
    Boolean init = false;
    int count;
    Camera camera;
    Camera.Parameters param;
    ImageButton flashLight;
    private float x1, y1, z1;
    private static final float deviation = (float) 7.0;
    boolean accelerometerFound = false, lightFound = false, shake = false, lightOn = false;

    /* The SensorEventListener lets us wire up to the real hardware events */
    private final SensorEventListener mySensorEventListenerAcc = new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {
            float x, y, z;
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            if (!init){
                x1 = x;
                y1 = y;
                z1 = z;
                init = true;
            }else{

                float differenceinX = Math.abs(x1 - x);
                float differenceinY = Math.abs(y1 - y);
                float differenceinZ = Math.abs(z1 - z);

                if(differenceinX < deviation){
                    differenceinX = (float) 0.0;
                }
                if(differenceinY < deviation){
                    differenceinY = (float) 0.0;
                }
                if(differenceinZ < deviation){
                    differenceinZ = (float) 0.0;
                }

                x1 = x;
                y1 = y;
                z1 = z;

                if (differenceinX > differenceinY) {

                    //acceleration.setText("Shake Count : "+ count);
                    count = count+1;
                    shake = true;
                    //Toast.makeText(MainActivity.this, "Shake Detected!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    /* The SensorEventListener lets us wire up to the real hardware events */
    private final SensorEventListener mySensorEventListenerLight = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            if(se.values[0] == 0){
                if (shake == true){
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

                    camera.setParameters(param);
                    camera.startPreview();
                    lightOn = true;
                    toggleImage();
                }
            }else{
                param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(param);
                camera.stopPreview();
                lightOn = false;
                shake = false;
                toggleImage();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // acceleration = (TextView) findViewById(R.id.acceleration);
        flashLight = (ImageButton) findViewById(R.id.flashlight);

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        getCameraInstance();
    }


    private void getCameraInstance(){
        camera = null;
        try{
            camera = Camera.open();
            param = camera.getParameters();
        }
        catch (Exception e){
            Toast.makeText(this,"Camera is not available or is being used",Toast.LENGTH_SHORT).show();
        }
    }

    private void releaseCamera(){
        if(camera !=null) {
            camera.release();
            camera = null;
        }
    }

    private void registerSensors(){

        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null){
            sm.registerListener(mySensorEventListenerAcc, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            accelerometerFound = true;
        } else{
            Toast.makeText(this, "Accelerometer is not available on your device", Toast.LENGTH_SHORT).show();
        }

        lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(lightSensor != null){
            sm.registerListener(mySensorEventListenerLight, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            lightFound = true;
        }else{
            Toast.makeText(this, "Light Sensor is not available on your device", Toast.LENGTH_SHORT).show();
        }

    }

    private void unregisterSensors(){
        sm.unregisterListener(mySensorEventListenerAcc);
        sm.unregisterListener(mySensorEventListenerLight);
    }

    protected void onResume() {
        super.onResume();
        registerSensors();
        if(camera == null){
            getCameraInstance();
        }
    }

    private void toggleImage(){
        if(lightOn == true){
            flashLight.setImageResource(R.drawable.light1);
        }else{
            flashLight.setImageResource(R.drawable.light2);
        }

    }



    //Unregister the Listener when the Activity is paused
    protected void onPause() {
        super.onPause();
        unregisterSensors();
        releaseCamera();
    }

    //Kill the app when it is stopped
    protected void onStop() {
        super.onStop();
        MainActivity.this.finish();
    }
}
