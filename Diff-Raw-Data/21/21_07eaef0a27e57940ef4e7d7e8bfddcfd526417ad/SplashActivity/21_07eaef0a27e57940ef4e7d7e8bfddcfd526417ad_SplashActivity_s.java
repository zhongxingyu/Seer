 package com.ibc;
 
 import java.util.Date;
 import java.util.Map;
 
 import com.ibc.library.CalendarProvider;
 import com.ibc.model.service.response.InstIDResponse;
 import com.ibc.service.ResultCode;
 import com.ibc.service.Service;
 import com.ibc.service.ServiceAction;
 import com.ibc.service.ServiceListener;
 import com.ibc.service.ServiceRespone;
 import com.ibc.util.Config;
 import com.ibc.util.Util;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class SplashActivity extends Activity {
     //for logging
     private static final String TAG = SplashActivity.class.getSimpleName();
 
     //use fullscreen mode for the splash branding?
     private final boolean fullscreen = true;
     //hiding the notification bar sometimes takes longer than 500ms
     //.. so keeping fullscreen=false is preferable for now?
     
     //force portrait mode?
     private final boolean forcePortraitMode = true;
 
     //store our asynchronous background task
     private ApplicationDataLoadingTask backgroundTask;
 
     //a simple selection routine
     private String targetActivity;
 
    @Override
     public void onCreate(Bundle savedInstanceState) {
         Log.d(TAG, "onCreate starting");
         super.onCreate(savedInstanceState);
 
         //remove title bar
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         //set fullscreen
         if (fullscreen) {
             getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         }
 
         //force portrait mode
         if (forcePortraitMode) {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         }
 
         //set the layout to res/layouts/splash.xml
         setContentView(R.layout.splash);
 
         //default target for where the splash screen goes
         targetActivity = "MainActivity";
 
         //check if a target activity was specified
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             if (extras.containsKey("targetActivity")) {
                 targetActivity = extras.getString("targetActivity");
             }
         }
         //get inst id
         iBCApplication app = iBCApplication.sharedInstance();
         String id = app.getSharedPreferencesManager().loadInstID();
         if (null == id) {
         	Service service = new Service(_listener);
         	service.getInstID();
         }
         //start loading the application data
         backgroundTask = new ApplicationDataLoadingTask();
         backgroundTask.execute();
 
         Log.d(TAG, "onCreate ending");
       //processing
        CalendarProvider provider = CalendarProvider.sharedInstance();
        provider.hasCalendar();
        CalendarProvider.addToCalendar(app, "demon hunter", Date.UTC(2012, 2, 16, 15, 0, 0), Date.UTC(2012, 2, 16, 16, 0, 0));
     }
    
    ServiceListener _listener = new ServiceListener() {
 
 		@Override
 		public void onComplete(Service service, ServiceRespone result) {
 			if (result.getAction() == ServiceAction.ActionGetInstID) {
 				if (result.getResultCode() == ResultCode.Success) {
 					InstIDResponse response = (InstIDResponse) result.getData();
 					String d = response.instID;
 					iBCApplication app = iBCApplication.sharedInstance();
 					app.getSharedPreferencesManager().saveInstID(d);
 					Map<String, String> map = app.getDiffServiceParams();
 					map.put("d", d);
 					String h = Util.hashMac(d + Config.KinectiaAppId);
 					map.put("h", h);
 					app.setDiffParams(map);
 				}
 			}
 		}
 		
 	};
 	
     @Override
     public void onResume() {
         Log.d(TAG, "onResume called");
         super.onResume();
     }
 
     @Override
     public void onStop() {
         Log.d(TAG, "onStop called");
         super.onStop();
         //finish();
     }
 
     @Override
     public void finish() {
         //cancel launch
         backgroundTask.cancel(true);
         super.finish();
     }
 
     public void startNextActivity() {
         Log.d(TAG, "startNextActivity called");
 
         //launch the next activity (or send back)
         Intent intent = null;
         if (targetActivity == "MainActivity") {
             intent = new Intent(this, MainActivity.class);
         }
         startActivity(intent);
 
         //we don't need this activity
         finish();
     }
 
     public class ApplicationDataLoadingTask extends AsyncTask<Void, Void, Void> {
     	
         protected void onPreExecute() {
             Log.d(TAG, "ApplicationDataLoadingTask::onPreExecute");
         }
     
         protected Void doInBackground(Void... params) {
             Log.d(TAG, "ApplicationDataLoadingTask::doInBackground");
             
             try {
                 Thread.sleep(3000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             
             return null;
         }
     
         protected void onPostExecute(Void v) {
             Log.d(TAG, "ApplicationDataLoadingTask::onPostExecute");
             startNextActivity();
         }
     }
 }
