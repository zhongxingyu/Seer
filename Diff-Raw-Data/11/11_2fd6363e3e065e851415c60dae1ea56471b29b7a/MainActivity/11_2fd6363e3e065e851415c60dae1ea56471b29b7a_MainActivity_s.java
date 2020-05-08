 package com.example.sendsms;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
     
     private TextView tv;
     private PowerManager.WakeLock wl;
     
     private String phoneNumber;
     private long timeout;
     private int smsQuantity;
     private int lastSMSNumber = 0;
     
     private SharedPreferences preferenceManager;
     private ProgressBar progressBar;
     
     private SendSMSTask task;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
         phoneNumber = preferenceManager.getString("phonenumber", null);
         
         String tmpTimeout = preferenceManager.getString("timeout", "20");
         timeout = Long.parseLong(tmpTimeout) * 1000;
         
         String tmpSMSQuantity = preferenceManager.getString("smsnumber", "245");
         smsQuantity = Integer.parseInt(tmpSMSQuantity);
         
         if (phoneNumber == null) {
         	Log.e("MainActivity", "phone number not defined.");
         	Intent settings = new Intent(this, OptionsActivity.class);
 			startActivityForResult(settings, 0);
         }
         
         tv = (TextView) findViewById(R.id.textView1);
         progressBar = (ProgressBar) findViewById(R.id.progressBar);
         progressBar.setMax(smsQuantity);
         
         PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "SendSMS");
         
         final Button startButton = (Button) findViewById(R.id.startSendButton);
         final Button stopButton = (Button) findViewById(R.id.stopSendButton);
         
         startButton.setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
             	if (task == null)
             		task = new SendSMSTask();
             	
             	task.setActivity(MainActivity.this);
                 task.execute(lastSMSNumber, smsQuantity);
                 startButton.setEnabled(false);
                 stopButton.setEnabled(true);
             }
         });
         
         stopButton.setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 task.cancel(true);
                 cleanTask();
             }
         });
     }
     
     private void cleanTask() {
     	task.setActivity(null);
         task = null;
         
         Button startButton = (Button) findViewById(R.id.startSendButton);
         Button stopButton = (Button) findViewById(R.id.stopSendButton);
         
         stopButton.setEnabled(false);
         startButton.setEnabled(true);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	super.onActivityResult(requestCode, resultCode, data);
     	
     	
     	if (resultCode == RESULT_OK) {
     		phoneNumber = preferenceManager.getString("phonenumber", null);
             //timeout = preferenceManager.getLong("timeout", 20000);
     		
     		String tmpTimeout = preferenceManager.getString("timeout", "20");
             timeout = Long.parseLong(tmpTimeout) * 1000;
             
             String tmpSMSQuantity = preferenceManager.getString("smsnumber", "245");
             smsQuantity = Integer.parseInt(tmpSMSQuantity);
             
             progressBar.setMax(smsQuantity);
     	}
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     	
     		case R.id.menu_settings: {
     			Intent settings = new Intent(this, OptionsActivity.class);
     			startActivityForResult(settings, 1);
     			return true;
     		}
     	
     		default: return super.onOptionsItemSelected(item);
     	}
     }
     
     private class SendSMSTask extends AsyncTask<Integer, Integer, Void>{
     	
     	private MainActivity main;
     	
         @Override
         protected Void doInBackground(Integer... params) {
         	
         	wl.acquire();
         	
             for(int i = params[0] + 1; i <= params[1]; i++) {
             	
             	if (!isCancelled()) {
                 
 	                publishProgress(i, params[1]);
 	                
 	                sendSMS(phoneNumber, "sms " + i);
 	                
 	                if (!isCancelled()) {
 		                try {
 	                		Thread.sleep(timeout);
 		                } catch (InterruptedException e) {
 		                    e.printStackTrace();
 		                    break;
 		                }
 	                }
 	            }
             }
             
             return null;
         }
         
         @Override
         public void onProgressUpdate( Integer ... params) {
         	
         	main.lastSMSNumber = params[0];
         	
             main.tv.setText("Sending SMS: " + params[0] + " of " + params[1]);
             main.progressBar.setProgress(params[0]);
         }
         
         @Override
         protected void onCancelled(Void result) {
         	super.onCancelled(result);
        	main.wl.release();
        	Log.d("onCancelled", "Called cancel!");
         }
         
         @Override
         protected void onPostExecute( Void result ) {
         	Log.d("onPostExecute", "Called on post execute!");
             super.onPostExecute(result);
            main.wl.release();
             main.lastSMSNumber = 0;
             main.tv.setText("Finished...");
             cleanTask();
         }
         
         public void setActivity(MainActivity activ) {
         	this.main = activ;
         }
     }
     
     private void sendSMS(String phoneNumber, String message)
     {
         Log.v("phoneNumber",phoneNumber);
         Log.v("Message",message);
         PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, X.class), 0);
         SmsManager sms = SmsManager.getDefault();
         sms.sendTextMessage(phoneNumber, null, message, pi, null);
     }
     
     class X {
         
     }
 }
