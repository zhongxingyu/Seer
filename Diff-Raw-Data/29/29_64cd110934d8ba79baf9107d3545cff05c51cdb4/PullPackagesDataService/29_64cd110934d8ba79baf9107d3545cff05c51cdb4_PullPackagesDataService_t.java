 package com.cs411.packman;
 
 import org.json.JSONArray;
 
 import com.example.packman.R;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.widget.Toast;
 
 public class PullPackagesDataService extends Service {
 	
     Thread background;
 	JSONArray currentItems;
     
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
     }
 
     @Override
     public void onDestroy() {
    	if (background != null) {
    		background.interrupt();
    	}
         Toast.makeText(this, R.string.polling_disabled, Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onStart(Intent intent, int startid) {
         Toast.makeText(this, R.string.polling_enabled, Toast.LENGTH_SHORT).show();
 
         final Messenger messenger = (Messenger) intent.getExtras().get("messenger");
 
 		// Create a background thread that we'll start onResume and stop onPause
 		background = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				JSONArray newItems;
 				try {
 					newItems = new RequestTask().getPackages();
 					
					// Serialize the items and then compare if the items have changed.
 					boolean haveItemsChanged = currentItems == null ? true
 							: !newItems.toString().equals(currentItems.toString());
 
 					if (haveItemsChanged) {
 						Message msg = Message.obtain();
 						msg.arg1 = 1; 
 						currentItems = newItems;
 						messenger.send(msg);
 					}
 					
 					// Re run the thread every 10 seconds
 					Thread.sleep(10000);
 					run();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		background.start();
     }
 }
