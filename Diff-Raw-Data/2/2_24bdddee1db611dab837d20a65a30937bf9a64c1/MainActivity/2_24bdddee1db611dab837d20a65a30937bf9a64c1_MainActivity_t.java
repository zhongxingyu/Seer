 package com.example.sendsms;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
     
     TextView tv;
     PowerManager.WakeLock wl;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                                         | PowerManager.ON_AFTER_RELEASE, "My Tag");
         wl.acquire();
         
         Button b = (Button) findViewById(R.id.button1);
         
         b.setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 new Cenas().execute();
                 
             }
         });
     }
     
     private class Cenas extends AsyncTask<Void, Integer, Void>{
         
         @Override
         protected Void doInBackground(Void... params) {
             
             for(int i=0; i<240; i++){
                 
                 publishProgress(i);
                 
                sendSMS("phonenumber", "sms " + i);
                 try {
                     Thread.sleep(10000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
             
             return null;
         }
         
         @Override
         public void onProgressUpdate( Integer ... params){
             
             tv = (TextView) findViewById(R.id.textView1);
             tv.setText("Sending "+params[0]+"...");
         }
         
         @Override
         protected void onPostExecute( Void result ) {
             
             super.onPostExecute(result);
             wl.release();
         }
         
         
     }
     
     private void sendSMS(String phoneNumber, String message)
     {
         Log.v("phoneNumber",phoneNumber);
         Log.v("MEssage",message);
         PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, X.class), 0);
         SmsManager sms = SmsManager.getDefault();
         sms.sendTextMessage(phoneNumber, null, message, pi, null);
     }
     
     class X {
         
     }
 }
