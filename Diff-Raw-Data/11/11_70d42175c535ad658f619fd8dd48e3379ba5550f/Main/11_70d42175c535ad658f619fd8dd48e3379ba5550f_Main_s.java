 package org.shokai.firmata.sample;
 
 import org.shokai.firmata.ArduinoFirmata;
 import org.shokai.firmata.ArduinoFirmataEventHandler;
 
 import java.io.*;
 import java.lang.*;
 import android.hardware.usb.*;
 
 import android.app.*;
 import android.content.*;
 import android.os.*;
 import android.util.*;
 import android.widget.*;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class Main extends Activity{
     private String TAG = "ArduinoFirmataSample";
     private Handler handler;
     private ArduinoFirmata arduino;
     private ToggleButton btnDigitalWrite;
     private TextView textAnalogRead;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         this.handler = new Handler();
         this.btnDigitalWrite = (ToggleButton)findViewById(R.id.btn_digital_write);
         this.textAnalogRead = (TextView)findViewById(R.id.text_analog_read);
         Log.v(TAG, "start activity");
         Log.v(TAG, ArduinoFirmata.VERSION);
 
         
         this.arduino = new ArduinoFirmata(this);
         final Activity self = this;
         arduino.addEventHandler(new ArduinoFirmataEventHandler(){
                 public void onError(String errorMessage){
                     Log.e(TAG, errorMessage);
                 }
                 public void onClose(){
                     Log.v(TAG, "arduino closed");
                     self.finish();
                 }
             });
         btnDigitalWrite.setOnCheckedChangeListener(new OnCheckedChangeListener(){
                 public void onCheckedChanged(CompoundButton btn, boolean isChecked){
                     Log.v(TAG, isChecked ? "on" : "off");
                     arduino.digitalWrite(13, isChecked);
                 }
             });
         try{
             arduino.start();
             arduino.pinMode(7, arduino.INPUT);
             new Thread(new Runnable(){
                     public void run(){
                         while(arduino.isOpen()){
                             try{
                                Thread.sleep(1500);
                                arduino.analogWrite(11, 20);
                                Thread.sleep(1500);
                                arduino.analogWrite(11, 255);
                                 handler.post(new Runnable(){
                                         public void run(){
                                             int ad = arduino.analogRead(0);
                                             textAnalogRead.setText("analogRead(0) = "+String.valueOf(ad));
                                         }
                                     });
                                Log.v(TAG+" digital read(7)", String.valueOf(arduino.digitalRead(7)));
                             }
                             catch(InterruptedException e){
                                 e.printStackTrace();
                             }
                         }
                     }
                 }).start();
         }
         catch(IOException e){
             e.printStackTrace();
             finish();
         }
         catch(InterruptedException e){
             e.printStackTrace();
             finish();
         }
     }
 }
