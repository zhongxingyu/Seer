 package com.example.usermanagement;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.LoaderManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.Loader;
 import android.content.ServiceConnection;
 import android.content.pm.UserInfo;
 import android.os.Bundle;
import android.os.Handler;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.os.UserManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private static final String TAG = MainActivity.class.getSimpleName();
 
 	List<UserInfo> users = null;
 	TextView messageText = null;
 	Spinner userSpinner = null;
 	Button switchButton = null;
 	Button removeButton = null;
 	int selected = 0;
 	
	Handler handler = new Handler();
	UserSwitchReceiver receiver = null;
			
 	LoaderManager.LoaderCallbacks<List<UserInfo>> usersCallbacks =
 			new LoaderManager.LoaderCallbacks<List<UserInfo>>() {
 
 				@Override
 				public Loader<List<UserInfo>> onCreateLoader(int id, Bundle args) {
 					Loader<List<UserInfo>> loader = new UserLoader(MainActivity.this);
 					loader.forceLoad();
 					return loader;
 				}
 
 				@Override
 				public void onLoadFinished(Loader<List<UserInfo>> loader,
 						List<UserInfo> data) {
 					if (data != null && data.size() > 0) {
 						users = data;
 						List<String> names = new ArrayList<String>();
 						for (UserInfo u: users) {
 							names.add(u.name);
 						}
 						ArrayAdapter adapter = 
 								new ArrayAdapter<String>(
 										MainActivity.this,
 										android.R.layout.simple_list_item_1,
 										names);
 						userSpinner.setAdapter(adapter);
 					} 
 				}
 
 				@Override
 				public void onLoaderReset(Loader<List<UserInfo>> arg0) {
 					// TODO Auto-generated method stub
 					
 				}};
 				
 	IUpdateListener updateListener = new IUpdateListener.Stub() {
		final String TAG = IUpdateListener.class.getSimpleName();
 		
 		@Override
 		public void update() throws RemoteException {
			Log.d(TAG, "update:");
			handler.post(new Runnable(){
				@Override
				public void run() {
					init();
				}
			});
 		}
 	};
 	
 	IUserSwitchService userSwitchService = null;
 	
 	ServiceConnection userSwitchConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			userSwitchService = IUserSwitchService.Stub.asInterface(service);
 			try {
 				userSwitchService.addListener(updateListener);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			userSwitchService = null;
 			try {
 				userSwitchService.removeListener(updateListener);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}};
 		
 	AdapterView.OnItemSelectedListener selectedListener = new AdapterView.OnItemSelectedListener() {
 
 		@Override
 		public void onItemSelected(AdapterView<?> adapter, View view, int pos,
 				long id) {
 			selected = pos;
 			setSwitchButtonLabel(users.get(pos).name);
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> adapter) {
 			//
 		}
 	};
 	
 	void setSwitchButtonLabel(String name) {
 		String label = MainActivity.this.getString(R.string.switch_user_button_label) + " "+ name;
 		switchButton.setText(label);
 	}
 		
 	View.OnClickListener switchUserListener = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
         	try {
 				Object activityManagerNative = UserSwitchReceiver.getActivityManagerNative();
 				Method switchUser = null;
 				Method[] methods = activityManagerNative.getClass().getDeclaredMethods();
 				for (Method m: methods) if (m.getName().equals("switchUser")) switchUser = m;
 				Log.d(TAG, "onClick: switchUser="+switchUser+", selected="+selected);
 				switchUser.invoke(activityManagerNative, new Object[]{users.get(selected).id});
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
  		}
 	};
 	
 	View.OnClickListener removeUsersListener = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			try {
 				UserManager um = (UserManager)MainActivity.this.getSystemService(Context.USER_SERVICE);
 				Method getUsers = um.getClass().getMethod("getUsers", null);
 				Method removeUser = um.getClass().getMethod("removeUser", new Class[]{int.class});
 				List<UserInfo> users = (List<UserInfo>)getUsers.invoke(um, null);
 				for (int i=1; i<users.size(); i++) {
 					removeUser.invoke(um, new Object[]{users.get(i).id});
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	};
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         messageText = (TextView)findViewById(R.id.message_text);
         userSpinner = (Spinner)findViewById(R.id.user_spinner);
         switchButton = (Button)findViewById(R.id.switch_user_button);
         removeButton = (Button)findViewById(R.id.remove_users_button);
         
         userSpinner.setOnItemSelectedListener(selectedListener);
         
         switchButton.setOnClickListener(switchUserListener);
         removeButton.setOnClickListener(removeUsersListener);
 
    	receiver = new UserSwitchReceiver(this);
         init();
         
         bindService(new Intent(IUserSwitchService.class.getName()),
         		userSwitchConnection,
         		BIND_AUTO_CREATE);
     }
     
     void init() {
     	UserInfo currentUser = receiver.getCurrentUser();
     	if (currentUser != null) {
     		messageText.setText(currentUser.name);
     	}
     	
    	getLoaderManager().destroyLoader(0);
         getLoaderManager().initLoader(0, null, usersCallbacks);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 	@Override
 	protected void onDestroy() {
 		unbindService(userSwitchConnection);
		receiver.ungerister();
 
 		super.onDestroy();
 	}
     
 }
