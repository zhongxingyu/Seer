 package com.bun.notificationshistory;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import com.android.internal.telephony.ITelephony;
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 
 
 import android.accessibilityservice.AccessibilityServiceInfo;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 
 import android.graphics.drawable.Drawable;
 
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 
 
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.ViewGroup;
 
 import android.view.accessibility.AccessibilityEvent;
 import android.view.accessibility.AccessibilityManager;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class Notification_Activity extends Activity{
 	
 	Notification_Adapter adapter;
 	DBController controller;
 	ListView layout;
 	private String accServiceId = "com.bun.notificationshistory/com.bun.notificationshistory.Notification_Service";
 	private Context ctx;
 	
 	BroadcastReceiver CallBlocker;
 	TelephonyManager telephonyManager;
 	ITelephony telephonyService; 
 	
 		
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.notification_main);
 		adapter = new Notification_Adapter();
 		layout = (ListView) findViewById(R.id.notificationsListViewId);		
 		controller = new DBController(this);	
 		populateNotificationAdapter(layout);
 		Boolean serviceStatus = isAccessibilityEnabled(this, accServiceId);
 		Boolean firstTimeRun = controller.getAllPreferences().get("FirstTimeTTSWarning").equals("No");
 				
 		if(firstTimeRun){
 			Intent intentG=new Intent(getApplicationContext(), Tutorial_1.class);	    		    	
 	    	startActivity(intentG);
 		}
 		
 		if(serviceStatus == false){
 			showServiceAlert();
 		}
 		
 		ctx = this;
 		
 		if(isSamsungPhoneWithTTS(ctx) && controller.getAllPreferences().get("FirstTimeTTSWarning").equals("No")){
 			
 			alertForSamsungTTS();
 			
 		}
 		
 		if(firstTimeRun){
 			
 			HashMap<String,String> prefMap = new HashMap<String,String>();
 			prefMap.put("FirstTimeTTSWarning", "Yes");
 			controller.updatePreferences(prefMap);
 			
 		}
 		registerForContextMenu(layout);
 		
 				  
 		//registerForLockCodeToUnhideAppIcon();
         	
 	}
 	
 	
 	
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		if (CallBlocker != null)
 		{
 			unregisterReceiver(CallBlocker);
 			CallBlocker = null;
 		}
 	 }
 		
 	
 	 
 	private void alertForSamsungTTS(){
 		AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
 		        Notification_Activity.this);
 
 		// Setting Dialog Title
 		alertDialog2.setTitle("Warning");
 
 		// Setting Dialog Message
 		alertDialog2.setMessage(R.string.access_serv_warning);
 
 		// Setting Icon to Dialog
 		//alertDialog2.setIcon(R.drawable.delete);
 
 		// Setting Positive "Yes" Btn
 		alertDialog2.setPositiveButton("YES",
 		        new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int which) {
 		            	Intent intent = new Intent();
 		    			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
 		    			Uri uri = Uri.fromParts("package", "com.google.android.tts",
 		    			        null);
 		    			intent.setData(uri);
 		    			startActivity(intent);
 		    			
 		    			Intent intent1 = new Intent();
 		    			intent1.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
 		    			Uri uri1 = Uri.fromParts("package", "com.samsung.SMT",
 		    			        null);
 		    			intent1.setData(uri1);
 		    			startActivity(intent1);
 			        	
 		            }
 		        });
 		// Setting Negative "NO" Btn
 		alertDialog2.setNegativeButton("NO",
 		        new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int which) {
 		               
 		                dialog.cancel();
 		            }
 		        });
 
 		// Showing Alert Dialog
 		alertDialog2.show();
 
 	}
 	
 	public boolean isSamsungPhoneWithTTS(Context context) {
 
 	    boolean retour = false;
 
 	    try {
 	        @SuppressWarnings("unused")
 	        ApplicationInfo info = context.getPackageManager()
 	                .getApplicationInfo("com.samsung.SMT", 0);
 	        retour = true;
 	    } catch (PackageManager.NameNotFoundException e) {
 	        retour = false;
 	    }
 	    
 	    
 
 	    return retour;
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		// TODO Auto-generated method stub
 		super.onCreateContextMenu(menu, v, menuInfo);
 		AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
 		Notification n = (Notification)adapter.getItem(contextMenuInfo.position);
 		if(n.getAppName() != null){
 			menu.setHeaderTitle("Select the Action");  
 	        menu.add(0, v.getId(), 0, "Ignore this App");
 	        menu.add(0, v.getId(), 0, "Uninstall this App");
 		}
         //menu.add(0, v.getId(), 0, "Delete the Contact"); 
 	}
 	
 	@Override  
     public boolean onContextItemSelected(MenuItem item)
     {  
 
 
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();		 
 		
         //  info.position will give the index of selected item
 		int intIndexSelected = info.position; 
 		
 		
         if(item.getTitle()=="Ignore this App")
         {
         	Notification n = (Notification)adapter.getItem(intIndexSelected);
         	if(layout == null){
         		layout = (ListView) findViewById(R.id.notificationsListViewId);
         	}
         	HashSet<String> ignoreAppSet = new HashSet<String>();
         	ignoreAppSet.add(n.getAppName());
         	controller.insertIgnoredApps(ignoreAppSet, ctx); 
         	
         	adapter.clearNotifications();
         	populateNotificationAdapter(layout);
         	adapter.notifyDataSetChanged();
         	layout.setAdapter(adapter);
                    
         }else if(item.getTitle()=="Uninstall this App"){
         	Notification n = (Notification)adapter.getItem(intIndexSelected);
         	Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package",
 			n.getPackageName(),null));
 			startActivity(intent);  
         }
                  
         return true;  
                 
                            
       } 
 		
 
 	private DatabaseChangedReceiver mReceiver = new DatabaseChangedReceiver() {
 	   public void onReceive(Context context, Intent intent) {
 		   layout = (ListView) findViewById(R.id.notificationsListViewId);
 		   adapter.clearNotifications();
 		   populateNotificationAdapter(layout);
 		   adapter.notifyDataSetChanged();
 	   }
 
 	};
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		
 		unregisterReceiver(mReceiver);
 		
 	}
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		
 		
 		IntentFilter intentFilter = new IntentFilter(DatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
         registerReceiver(mReceiver, intentFilter);
         adapter.clearNotifications();
         populateNotificationAdapter(layout);
         adapter.notifyDataSetChanged();
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.notification_main_menu, menu);
 		return true;
 	}
 	
 	public boolean onPrepareOptionsMenu (Menu menu) {    
 	    
 		for(int i=0; i< menu.size(); i++){
 			MenuItem mi = menu.getItem(i);
 			if(i == 2){
 				
 				HashMap<String,String> preferences = controller.getAllPreferences();
 				
 				if("GroupByDay".equals(preferences.get("NotificationGroupBy"))){
 					
 					mi.setTitle("Group by Apps");
 					
 				}else if("GroupByApp".equals(preferences.get("NotificationGroupBy"))){
 					
 					mi.setTitle("Group by Days");
 					
 				}
 				
 			}
 		}
 			        
 	    return super.onPrepareOptionsMenu(menu);
 	}
 	
 	 @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
 	 	 
         switch (item.getItemId())
         {
         case R.id.menu_clear:	            
         	showClearWarning();
         	break;
  
         case R.id.meni_Exit:
         	finish();
         		        
         	break;
         case R.id.group_notifications:
         	updateGroupNotificationPreference();
         	
         	break;
         case R.id.view_graphs:
         	Intent intentG=new Intent(getApplicationContext(), First_Graph_Activity.class);	    		    	
 	    	startActivity(intentG);	    	
         	break;
         	
         case R.id.view_ignored_Apps:
         	        	
 	    	Intent intent=new Intent(getApplicationContext(), Ignored_Apps_Activity.class);	    		    	
 	    	startActivity(intent);	    	
         	break;
         	
         case R.id.air_push_id:
         	Intent intentA=new Intent(getApplicationContext(), DetectorActivity.class);	    		    	
 	    	startActivity(intentA);	    	
         	break;
         	
         case R.id.hide_this_app:
         	showHideAppMessage(); 
         	//finish();
         	break;	
         	
         default:
             return super.onOptionsItemSelected(item);
             
         }
         
         return true;
     } 
 	 
 	 private void showHideAppMessage(){
 			final AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
 			        Notification_Activity.this);
 
 			// Setting Dialog Title
 			alertDialog2.setTitle("Warning");
 
 			// Setting Dialog Message
 			alertDialog2.setMessage("Hiding app will remove the app icon from app drawer and not from the home screen. Remove the home screen icon manually.It may take few seconds to hide the app icon.\nDial the Passcode to unhide the app.");
 
 			// Setting Icon to Dialog
 			//alertDialog2.setIcon(R.drawable.delete);
 
 			// Setting Positive "Yes" Btn
 			alertDialog2.setPositiveButton("YES",
 			        new DialogInterface.OnClickListener() {
 			            public void onClick(DialogInterface dialog, int which) {
 			            	ComponentName componentToDisable =
 			      				  new ComponentName("com.bun.notificationshistory",
 			      				  "com.bun.notificationshistory.Notification_Activity");
 
 			      				  getPackageManager().setComponentEnabledSetting(
 			      				  componentToDisable,
 			      				  PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
 			      				  PackageManager.DONT_KILL_APP);
 			      				Intent startMain = new Intent(Intent.ACTION_MAIN);
 			      				startMain.addCategory(Intent.CATEGORY_HOME);
 			      				startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			      				startActivity(startMain);
 				        	
 			            }
 			        });
 			// Setting Negative "NO" Btn
 			alertDialog2.setNegativeButton("Change Passcode",
 			        new DialogInterface.OnClickListener() {
 			            public void onClick(DialogInterface dialog, int which) {
 			               
 			                //dialog.cancel();
 			            	changePasscode();
 			            	
 			            }
 			        });
 
 	
 			alertDialog2.show();
 		} 
 	
 	 
 	 Boolean hasErrors =false;
 	 private void changePasscode(){
 		 LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
 		 final View layout = inflater.inflate(R.layout.change_password, (ViewGroup) findViewById(R.id.cpRoot));
 		 
 			AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
 			        Notification_Activity.this);
 
 			// Setting Dialog Title
 			alertDialog2.setTitle("Change Passcode");
 
 			// Setting Dialog Message
 			alertDialog2.setView(layout);
 
 			// Setting Icon to Dialog
 			//alertDialog2.setIcon(R.drawable.delete);
 
 			// Setting Positive "Yes" Btn
 			alertDialog2.setPositiveButton("Save",
 			        new DialogInterface.OnClickListener() {
 			            public void onClick(DialogInterface dialog, int which) {
 			            	hasErrors = false;
 			            	String oldPasscode = controller.getAllPreferences().get("Passcode");
 			            	String errMsg = "";
 			            	
 			            	final EditText oldPass    = (EditText) layout.findViewById(R.id.EditText_OldPwd);
 			            	final EditText password1    = (EditText) layout.findViewById(R.id.EditText_Pwd1);
 			                final EditText password2    = (EditText) layout.findViewById(R.id.EditText_Pwd2);
 			                final TextView error        = (TextView) layout.findViewById(R.id.TextView_PwdProblem);   
 			                
 			                if(oldPass.getText().toString().trim().equals("") ){
 			                	errMsg = "Old Passcode cannot be blank.\n";
 			                	hasErrors = true;
 			                }else if(!oldPass.getText().toString().equals(oldPasscode)){
 			                	errMsg += "Old Passcode is not correct.\n";
 			                	hasErrors = true;
 			                }
 			                
 			                if(password1.getText().toString().equals("") || password2.getText().toString().equals("")){
 			                	errMsg += "New Passcodes cannot be blank.\n";
 			                	hasErrors = true;
 			                }else if(!password1.getText().toString().equals(password2.getText().toString())){
 			                	errMsg += "New Passcodes do not match.\n";
 			                	hasErrors = true;
 			                }
 			                
 			                if(hasErrors == false){
 			                	HashMap<String,String> prefMap = new HashMap<String,String>();
 			                	prefMap.put("Passcode", password1.getText().toString());
 			                	controller.updatePreferences(prefMap);
 			                	Toast.makeText(getApplicationContext(),
                                         "Password Changed successfully",
                                         Toast.LENGTH_SHORT).show();
 			                }else{
 			                	error.setText(errMsg);
 			                }
 			            	
 				        	
 			            }
 			        });
 			// Setting Negative "NO" Btn
 			alertDialog2.setNegativeButton("Cancel",
 			        new DialogInterface.OnClickListener() {
 			            public void onClick(DialogInterface dialog, int which) {
			            	hasErrors = false;
 			                dialog.cancel();
 			            }
 			        });
 			
 			final AlertDialog alertClient = alertDialog2.create();
 	        alertClient.show();
 
 	        alertClient
 	                .setOnDismissListener(new DialogInterface.OnDismissListener() {
 
 	                    @Override
 	                    public void onDismiss(DialogInterface dialog) {
 	                        //If the error flag was set to true then show the dialog again
 	                        if (hasErrors == true) {
 	                            alertClient.show();
 	                        } else {
 	                        	
 	                        }
 
 	                    }
 	                });
 			
 		} 
 	 
 	 
 	 
 	 
 	private void showClearWarning(){
 		AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
 		        Notification_Activity.this);
 
 		// Setting Dialog Title
 		alertDialog2.setTitle("Warning");
 
 		// Setting Dialog Message
 		alertDialog2.setMessage(R.string.clear_warning);
 
 		// Setting Icon to Dialog
 		//alertDialog2.setIcon(R.drawable.delete);
 
 		// Setting Positive "Yes" Btn
 		alertDialog2.setPositiveButton("YES",
 		        new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int which) {
 		            	if(layout == null){
 		            		layout = (ListView) findViewById(R.id.notificationsListViewId);
 		            	}
 		            	controller.deleteAllNotifications();
 		            	adapter.clearNotifications();
 		            	populateDefaultMessage(layout,false);
 		            	adapter.notifyDataSetChanged();
 			        	layout.setAdapter(adapter);
 			        	
 		            }
 		        });
 		// Setting Negative "NO" Btn
 		alertDialog2.setNegativeButton("NO",
 		        new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int which) {
 		               
 		                dialog.cancel();
 		            }
 		        });
 
 		// Showing Alert Dialog
 		alertDialog2.show();
 	}
 	
 	public void deleteAppNotifications(final View view){
 		
 		String appName = view.getTag().toString().split("##")[0];
 		final String packageName = view.getTag().toString().split("##")[1];
 			
 		AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
 		        Notification_Activity.this);
 
 		// Setting Dialog Title
 		alertDialog2.setTitle("Warning");
 
 		// Setting Dialog Message
 		alertDialog2.setMessage("Are you sure you want to delete all Notifications for " + appName + " ?");
 
 		// Setting Icon to Dialog
 		//alertDialog2.setIcon(R.drawable.delete);
 
 		// Setting Positive "Yes" Btn
 		alertDialog2.setPositiveButton("YES",
 		        new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int which) {
 		            	if(layout == null){
 		            		layout = (ListView) findViewById(R.id.notificationsListViewId);
 		            	}
 		            	controller.deleteAppNotifications(packageName);
 		            	adapter.clearNotifications();
 		            	populateNotificationAdapter(layout);
 		            	adapter.notifyDataSetChanged();
 			        	layout.setAdapter(adapter);
 			        	
 		            }
 		        });
 		// Setting Negative "NO" Btn
 		alertDialog2.setNegativeButton("NO",
 		        new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int which) {
 		               
 		                dialog.cancel();
 		            }
 		        });
 
 		// Showing Alert Dialog
 		alertDialog2.show();
 		
 	}
 	
 	private void showServiceAlert(){
 		AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
 		        Notification_Activity.this);
 
 		// Setting Dialog Title
 		alertDialog2.setTitle("Notifications History service");
 
 		// Setting Dialog Message
 		alertDialog2.setMessage(R.string.service_warning);
 
 		// Setting Icon to Dialog
 		//alertDialog2.setIcon(R.drawable.delete);
 
 		// Setting Positive "Yes" Btn
 		alertDialog2.setPositiveButton("ACTIVATE",
 		        new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int which) {
 		            	Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS); 
 		            	startActivityForResult(intent, 0);
 		            }
 		        });
 		// Setting Negative "NO" Btn
 		alertDialog2.setNegativeButton("LATER",
 		        new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int which) {
 		               
 		                dialog.cancel();
 		            }
 		        });
 
 		// Showing Alert Dialog
 		alertDialog2.show();
 	}
 	
 	private void populateNotificationAdapter(ListView layout){
 
 		HashMap<String,String> preferences = controller.getAllPreferences();
 		
 		if("GroupByDay".equals(preferences.get("NotificationGroupBy"))){
 			
 			populateAdapterGroupedByDay();
 			
 		}else if("GroupByApp".equals(preferences.get("NotificationGroupBy"))){
 			
 			populateAdapterGroupedByApp();
 			
 		}
 		
 		
 		layout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			
 		    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 		    	Notification n = (Notification)adapter.getItem(position);
 		    	Intent intent=new Intent(getApplicationContext(), Notification_Details.class);
 		    	intent.putExtra("date", n.getNotTime());
 		    	intent.putExtra("app", n.getAppName());
 		    	if(n.getIsSectionHeader() == null)
 		    		startActivity(intent);
 		    }
 		});
 		layout.setAdapter(adapter);
 		
 	}
 	
 	private void populateAdapterGroupedByDay(){
 		Notification n ;
 		ArrayList<HashMap<String, String>> data = controller.getAllNotifications();
 		HashMap<String,String> appPackageMap = new HashMap<String,String>();
 		HashMap<String,String> appLastTimeMap = new HashMap<String,String>();
 		HashSet<String> ignoredApps = controller.getAllIgnoredApps();
 		Boolean hasNotification = false;
 		
 		if(data != null && data.size() > 0){
 			Boolean isSectionHeader = true;
 			String initialDate = data.get(0).get("notTime");
 			LinkedHashMap<String,Integer> appMap = new LinkedHashMap<String,Integer>();
 			
 			Integer counter = 0;
 			
 			for(HashMap<String,String> hm : data){
 				
 				appPackageMap.put(hm.get("appName"), hm.get("packageName"));
 			
 				counter ++;
 				
 				if(isSectionHeader){
 					n = new Notification();
 					n.setIsSectionHeader(true);
 					n.setSectionHeaderValue(hm.get("notTime"));
 					isSectionHeader = false;
 					adapter.addNotification(n);
 				}
 				
 				if(initialDate.equals(hm.get("notTime"))){
 					if(appMap.get(hm.get("appName")) != null){
 						appMap.put(hm.get("appName"), appMap.get(hm.get("appName")) + 1);						
 					}else{
 						appMap.put(hm.get("appName") , 1);
 						appLastTimeMap.put(hm.get("appName"), hm.get("notTime") + "  " +hm.get("notDate"));
 					}
 					
 					if(counter == data.size()){
 						if(appMap.size() > 0){
 							appMap = Utils.sortHashMapByValuesD(appMap);
 							for(String app : appMap.keySet()){
 								Notification nn = new Notification();
 								nn.setAppName(app);
 								nn.setNotificationCount(appMap.get(app));
 								nn.setNotTime(initialDate);
 								nn.setLastActivityDate(appLastTimeMap.get(app));
 								try{
 									
 									Drawable icon;
 									if(app.equals("Google Talk")){
 										icon = getResources().getDrawable( R.drawable.googletalk );
 									}else{
 										icon = this.getPackageManager().getApplicationIcon(appPackageMap.get(app));
 									}
 									nn.setAppIcon(icon);
 									nn.setPackageName(appPackageMap.get(app));
 								}
 								catch (PackageManager.NameNotFoundException ne)
 								 {
 
 								 }
 								if(!ignoredApps.contains(app)){
 									adapter.addNotification(nn);
 									hasNotification = true;
 								}
 							}
 						}
 					}
 				}else{
 					isSectionHeader = true;
 					if(appMap.size() > 0){
 						appMap = Utils.sortHashMapByValuesD(appMap);
 						for(String app : appMap.keySet()){
 							Notification nn = new Notification();
 							nn.setAppName(app);							
 							nn.setNotificationCount(appMap.get(app));
 							nn.setNotTime(initialDate);
 							nn.setLastActivityDate(appLastTimeMap.get(app));
 							try{
 								Drawable icon;
 								if(app.equals("Google Talk")){
 									
 									icon = getResources().getDrawable( R.drawable.googletalk );
 								}else{
 									icon = this.getPackageManager().getApplicationIcon(appPackageMap.get(app));
 								}
 								nn.setAppIcon(icon);
 								nn.setPackageName(appPackageMap.get(app));
 							}
 							catch (PackageManager.NameNotFoundException ne)
 							 {
 
 							 }
 							if(!ignoredApps.contains(app)){
 								adapter.addNotification(nn);
 								hasNotification = true;
 							}
 						}
 					}
 					appMap.clear();
 					appLastTimeMap.clear();
 					initialDate = hm.get("notTime");
 				}
 				
 			}
 			if(hasNotification == false){
 				adapter.clearNotifications();
 				populateDefaultMessage(layout,true);
 			}
 		}else{
 			populateDefaultMessage(layout,true);
 		}
 	}
 	
 	
 	
 	private void populateAdapterGroupedByApp(){
 		
 		ArrayList<HashMap<String, String>> data = controller.getAllNotifications();
 		HashMap<String,String> appPackageMap = new HashMap<String,String>();
 		HashMap<String,String> appLastTimeMap = new HashMap<String,String>();
 		HashSet<String> ignoredApps = controller.getAllIgnoredApps();
 		Boolean hasNotifications = false;
 		
 		if(data != null && data.size() > 0){
 			
 			LinkedHashMap<String,Integer> appMap = new LinkedHashMap<String,Integer>();			
 			
 			for(HashMap<String,String> hm : data){
 				
 				appPackageMap.put(hm.get("appName"), hm.get("packageName"));
 				
 				if(appMap.get(hm.get("appName")) != null){
 					appMap.put(hm.get("appName"), appMap.get(hm.get("appName")) + 1);					
 				}else{
 					appMap.put(hm.get("appName") , 1);
 					appLastTimeMap.put(hm.get("appName"), hm.get("notTime") + "  " +hm.get("notDate"));
 				}
 				
 			}
 			
 			if(appMap.size() > 0){
 				appMap = Utils.sortHashMapByValuesD(appMap);
 				for(String app : appMap.keySet()){
 					Notification nn = new Notification();
 					nn.setAppName(app);
 					nn.setNotificationCount(appMap.get(app));					
 					nn.setLastActivityDate(appLastTimeMap.get(app));
 					nn.setPackageName(appPackageMap.get(app));
 					try{
 						
 						Drawable icon;
 						if(app.equals("Google Talk")){
 							icon = getResources().getDrawable( R.drawable.googletalk );
 						}else{
 							icon = this.getPackageManager().getApplicationIcon(appPackageMap.get(app));
 						}
 						nn.setAppIcon(icon);
 					}
 					catch (PackageManager.NameNotFoundException ne)
 					 {
 
 					 }
 					if(!ignoredApps.contains(app)){
 						adapter.addNotification(nn);
 						hasNotifications = true;
 					}
 				}
 			}
 			if(hasNotifications == false){
 				adapter.clearNotifications();
 				populateDefaultMessage(layout,false);
 			}
 		}else{
 			populateDefaultMessage(layout,false);
 		}
 		
 		
 	}
 
 	
 	
 	
 	private void populateDefaultMessage(ListView layout, Boolean isGroupedByDay){
 		Notification n1 = new Notification();
 		Notification n2 = new Notification();
 		n1.setIsSectionHeader(true);
 		n1.setSectionHeaderValue("Today");
 		n2.setAppName("No Notifications till now. ");
 		n2.setNotificationCount(-1);
 		if(isGroupedByDay){
 			adapter.addNotification(n1);
 		}
 		adapter.addNotification(n2);
 		layout.setAdapter(adapter);
 	}
 	
 	
 	
 	public boolean isAccessibilityEnabled(Context context, String id) {
 
 	    /*AccessibilityManager am = (AccessibilityManager) context
 	            .getSystemService(Context.ACCESSIBILITY_SERVICE);
 
 	    List<AccessibilityServiceInfo> runningServices = am
 	            .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
 	    for (AccessibilityServiceInfo service : runningServices) {
 	    	Log.d("isAccessibilityEnabled", "Accessibility Service=========== : " + service.getSettingsActivityName());
 	        if (id.equals(service.getId())) {
 	            return true;
 	        }
 	    }
 
 	    return false;*/
 		
 		int accessibilityEnabled = 0;
 	    final String LIGHTFLOW_ACCESSIBILITY_SERVICE = "com.example.test/com.example.text.ccessibilityService";
 	    boolean accessibilityFound = false;
 	    try {
 	        accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
 	        Log.d("test", "ACCESSIBILITY: " + accessibilityEnabled);
 	    } catch (SettingNotFoundException e) {
 	        Log.d("test", "Error finding setting, default accessibility to not found: " + e.getMessage());
 	    }
 
 	    TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
 
 	    if (accessibilityEnabled==1){
 	        Log.d("test", "***ACCESSIBILIY IS ENABLED***: ");
 
 
 	         String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
 	         Log.d("test", "Setting: " + settingValue);
 	         if (settingValue != null) {
 	             TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
 	             splitter.setString(settingValue);
 	             while (splitter.hasNext()) {
 	                 String accessabilityService = splitter.next();
 	                 Log.d("test", "Setting: " + accessabilityService);
 	                 if (accessabilityService.equalsIgnoreCase(accServiceId)){
 	                     Log.d("test", "We've found the correct setting - accessibility is switched on!");
 	                     return true;
 	                 }
 	             }
 	         }
 
 	        Log.d("test", "***END***");
 	    }
 	    else{
 	    	
 	        Log.d("test", "***ACCESSIBILIY IS DISABLED***");
 	        return false;
 	    }
 	    return false;
 		    
 	}
 	
 	private void updateGroupNotificationPreference(){
 		
 		
 		
 		HashMap<String,String> map = new HashMap<String,String>();
 		HashMap<String,String> preferences = controller.getAllPreferences();
 		
 		if("GroupByDay".equals(preferences.get("NotificationGroupBy"))){
 			
 			map.put("NotificationGroupBy", "GroupByApp");
 			
 		}else if("GroupByApp".equals(preferences.get("NotificationGroupBy"))){
 			
 			map.put("NotificationGroupBy", "GroupByDay");
 			
 		}
 		
 		controller.updatePreferences(map);
 		
 		adapter.clearNotifications();
     	populateNotificationAdapter(layout);
     	adapter.notifyDataSetChanged();
     	layout.setAdapter(adapter);
 		
 	}
 		
 
 }
