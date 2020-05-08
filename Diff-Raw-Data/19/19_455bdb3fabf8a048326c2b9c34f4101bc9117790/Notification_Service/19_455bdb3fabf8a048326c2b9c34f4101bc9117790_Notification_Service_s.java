 package com.bun.notificationshistory;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TimeZone;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Parcelable; 
 import android.provider.CallLog.Calls;
 import android.accessibilityservice.AccessibilityService;
 import android.accessibilityservice.AccessibilityServiceInfo;
 import android.app.Activity;
 import android.app.Notification;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.ViewGroup;
 import android.view.accessibility.AccessibilityEvent;
 import android.view.accessibility.AccessibilityRecord;
 import android.widget.Toast;
 import android.widget.TextView;
 
 
 public class Notification_Service extends AccessibilityService {
 	
 	HashMap<String, String> mInstalledApplications = new HashMap<String, String>();
     HashMap<String, String> mLastMessage = new HashMap<String, String>();
     HashMap<String, Long> mLastTimeStamp = new HashMap<String, Long>();
     DBController controller;
     BroadcastReceiver CallBlocker;
     private Context ctx;
 
 	@Override
 	public void onAccessibilityEvent(AccessibilityEvent event) {
 		try{
 			HashMap<String,String> dbMap = new HashMap<String,String>();
 			String message = "";
 			String appName = getApplicationName(event.getPackageName().toString());
             Log.d("NotificationHistory", "[PackageName]         " + event.getPackageName());
             Log.d("NotificationHistory", "[Application]         " + appName);
             Log.d("NotificationHistory", "[EventTime]           " + event.getEventTime());
             Log.d("NotificationHistory", "[EventText]           " + String.valueOf(event.getText()));
             String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
             
             dbMap.put("appName", appName);
             dbMap.put("notTime", date);
             dbMap.put("packageName", event.getPackageName().toString());
             
             try{
 	            Notification notification = (Notification) event.getParcelableData();
 	            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	            ViewGroup localView = (ViewGroup) inflater.inflate(notification.contentView.getLayoutId(), null);
 	            notification.contentView.reapply(getApplicationContext(), localView);
 	            
 	            ArrayList<TextView> views = new ArrayList<TextView>();
 	            getAllTextView(views, localView);
 	            for (TextView v: views) {
 	                String text = v.getText().toString();
 	                if (!text.isEmpty()) {
 	                    Log.d("Notification_History", "[Text]                " + text);
 	                    message += text + "\n";
 	                }
 	            }
             }catch(Exception e){
             	message = String.valueOf(event.getText());
             }
            
             DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
             Calendar calendar = Calendar.getInstance();
             calendar.setTimeInMillis(System.currentTimeMillis());
             String formattedDate = formatter.format(calendar.getTime());
            
             
             dbMap.put("notDate", formattedDate);
             dbMap.put("message", String.valueOf(event.getText()));
             dbMap.put("additionalInfo", message);
             Log.d("Notification_History", "Total Records in DB till now :" + controller.getAllNotifications().size());
             if((message != null && !message.equals("[]")) && !("[]".equals(event.getText())))
             	controller.insertNotification(dbMap, this);
 		}catch(Exception e){
 			 Log.e("NotificationHistory", "Exception in Handlingthe Event : " + e);
 		}
 	   }
 	@Override
 	public void onInterrupt() {
	    // TODO Auto-generated method stub.
	
 	}
 	
 	@Override
 	protected void onServiceConnected() {
 		try{
 			refreshApplicationList();
 	        mLastMessage.clear();
 	        mLastTimeStamp.clear();
 	        controller = new DBController(this);
 	        Log.d("NotificationHistory", "notification service started.");
 	        ctx = this;
 	        
 			//AccessibilityServiceInfo info = new AccessibilityServiceInfo();
 			//info.feedbackType = 1;
 			//info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
 			//info.notificationTimeout = 100; 
 			//info.feedbackType = AccessibilityEvent.TYPES_ALL_MASK;
 			//setServiceInfo(info);
 	        registerForLockCodeToUnhideAppIcon(this);
 		}catch(Exception e){
 			Log.e("NotificationHistory", "Failed to configure accessibility service", e);
 		}
       
      }
 	
 	private void registerForLockCodeToUnhideAppIcon(final Context ctx){
 		CallBlocker =new BroadcastReceiver()
 		{
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				
 				String passcode = controller.getAllPreferences().get("Passcode");
 				
 				if(passcode == null){
 					passcode = "0000";
 					HashMap<String,String> prefMap = new HashMap<String,String>();
 					prefMap.put("Passcode", "0000");
 					controller.insertPreference(prefMap, ctx);
 				}
 				
 				String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
 				
 				insertCallLog(number, "Outgoing Call - ");
 				
 				if(passcode.trim().equals(number)){
 					ComponentName componentToDisable =
 							  new ComponentName("com.bun.notificationshistory",
 							  "com.bun.notificationshistory.Notification_Activity");
 
 							  getPackageManager().setComponentEnabledSetting(
 							  componentToDisable,
 							  PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
 							  PackageManager.DONT_KILL_APP);
 							  
 					  Toast.makeText(ctx, 
 							  getString(R.string.app_name) + " is unhidden now.", 
 					           Toast.LENGTH_LONG).show();
 				}
 		         
 		         
 		         
 		         
 			}
 		};//BroadcastReceiver
 		IntentFilter filter= new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
 		registerReceiver(CallBlocker, filter);
 		
 		PhoneCallListener phoneListener = new PhoneCallListener();
 	    TelephonyManager telephonyManager = (TelephonyManager) this
 	            .getSystemService(Context.TELEPHONY_SERVICE);
 	    telephonyManager.listen(phoneListener,
 	            PhoneStateListener.LISTEN_CALL_STATE);
 		
 	}
 	
 	private void insertCallLog(String number, String text){
 		HashMap<String,String> values = new HashMap<String,String>();	
 		
 		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(System.currentTimeMillis());
         String formattedDate = formatter.format(calendar.getTime());
 		
 		values.put("notDate", formattedDate.split(" ")[1]);
 	    values.put("notTime", formattedDate.split(" ")[0]);
 	    values.put("message", text + number);
 	    values.put("appName", "Phone");
 	    values.put("packageName", "com.android.phone");
 	    values.put("additionalInfo", text + number);
 	    
 	    controller.insertNotification(values, ctx);
 	}
 	
 	private class PhoneCallListener extends PhoneStateListener {
 
 	    private boolean isPhoneCalling = false;
 	    private boolean isIncomingCallStarted = false;
 
 	    @Override
 	    public void onCallStateChanged(int state, String incomingNumber) {
 	    	
 	    	
 
 	        if (TelephonyManager.CALL_STATE_RINGING == state) {
 	        	isIncomingCallStarted = true;
 	        }
 
	        if (TelephonyManager.CALL_STATE_OFFHOOK == state && isIncomingCallStarted) {
 	        	
 
 	            isPhoneCalling = true;
 	        }
 
 	        if (TelephonyManager.CALL_STATE_IDLE == state) {
 	            // run when class initial and phone call ended, need detect flag
 	            // from CALL_STATE_OFFHOOK
 	            //Log.i(LOG_TAG, "IDLE number");
 
 	            if (isPhoneCalling) {
 
 	                Handler handler = new Handler();
 
 	                //Put in delay because call log is not updated immediately when state changed
 	                // The dialler takes a little bit of time to write to it 500ms seems to be enough
 	                handler.postDelayed(new Runnable() {
 
 	                    @Override
 	                    public void run() {
 	                        // get start of cursor
 	                          Log.i("CallLogDetailsActivity", "Getting Log activity...");
 	                            String[] projection = new String[]{Calls.NUMBER};
 	                            Cursor cur = getContentResolver().query(Calls.CONTENT_URI, projection, null, null, Calls.DATE +" desc");
 	                            cur.moveToFirst();
 	                            String lastCallnumber = cur.getString(0);
 	                            insertCallLog(lastCallnumber, "Received Call - ");
 	                    }
 	                },500);
 
 	                isPhoneCalling = false;
 	            }
 
 	        }
 	    }
 	}
 	
 	private String getApplicationName(String packageName) {
         String name = packageName;
         
         if (mInstalledApplications.containsKey(packageName)) {
             name = mInstalledApplications.get(packageName);
         } else {
             refreshApplicationList();
             if (mInstalledApplications.containsKey(packageName)) {
                 name = mInstalledApplications.get(packageName);
             }
         }
         
         return name;
     }
 	
 	private void refreshApplicationList() {
         final PackageManager pm = getBaseContext().getPackageManager();
         List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
         
         for (ApplicationInfo packageInfo : packages) {
             mInstalledApplications.put(packageInfo.packageName, packageInfo.loadLabel(pm).toString());
         }
     } 
 	
 	   private void getAllTextView(ArrayList<TextView> views, ViewGroup v)
 	    {
 	        if (null == views) {
 	            return;
 	        }
 	        for (int i = 0; i < v.getChildCount(); i++)
 	        {
 	            Object child = v.getChildAt(i); 
 	            if (child instanceof TextView)
 	            {
 	                views.add((TextView)child);
 	            }
 	            else if(child instanceof ViewGroup)
 	            {
 	                getAllTextView(views, (ViewGroup)child);  // Recursive call.
 	            }
 	        }
 	    }
 	
 }
