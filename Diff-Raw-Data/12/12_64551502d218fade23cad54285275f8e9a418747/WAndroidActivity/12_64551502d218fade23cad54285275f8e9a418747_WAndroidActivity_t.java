 package com.willd.wandroid;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WAndroidActivity extends Activity {
 	int counter = 0;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         
         final Button button = (Button) findViewById(R.id.button);
         final Button start = (Button) findViewById(R.id.start);
         final Button save = (Button) findViewById(R.id.save);
         final EditText et = (EditText) findViewById(R.id.edittext);
         final TextView tv = (TextView) findViewById(R.id.textview);
         String ns = Context.NOTIFICATION_SERVICE;
         
         final NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
         
         
         int icon = R.drawable.ic_launcher;
         CharSequence tickerText = "Hello";
         long when = System.currentTimeMillis();
 
         final Notification notification = new Notification(icon, tickerText, when);
         
         Context context = getApplicationContext();
         CharSequence contentTitle = "My notification";
         CharSequence contentText = "Hello World!";
         Intent notificationintent = new Intent(this, WAndroidNotification.class);
        final Intent serviceintent = new Intent(this, WAndroidService.class);
         PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationintent, PendingIntent.FLAG_CANCEL_CURRENT);
         
 
         notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
         notification.defaults = Notification.DEFAULT_ALL;
         notification.flags=Notification.FLAG_AUTO_CANCEL;
         notificationintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
 
         
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v ) {
             	Toast.makeText(WAndroidActivity.this, R.string.testing, Toast.LENGTH_SHORT).show();
             	et.setText(Integer.toString(counter));
             	tv.setText(R.string.testing);
             	counter += 1;
 
 
                 mNotificationManager.notify(0, notification);
             
             }
         });
         start.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v ) {
            	startService(serviceintent);
             
             }
         });
         save.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
 				try
 		        {
 		            File root = new File(Environment.getExternalStorageDirectory(), "WAndroid");
 
 		            if (!root.exists())
 		                root.mkdir();
 
 		            File gpxfile = new File(root, "WAndroid.txt");
 
 		            BufferedWriter bW;
 		            
 		            bW = new BufferedWriter(new FileWriter(gpxfile, true));
 		            //bW.write(counter);
 		            bW.append(et.getText());
 		            bW.newLine();
 		            bW.flush();
 		            bW.close();
 		            
 		        }
 		        catch(IOException e)
 		        {
 		             e.printStackTrace();
 		        }
 		        
 				
 			}
         });
         
 	
     }
 
 } 
