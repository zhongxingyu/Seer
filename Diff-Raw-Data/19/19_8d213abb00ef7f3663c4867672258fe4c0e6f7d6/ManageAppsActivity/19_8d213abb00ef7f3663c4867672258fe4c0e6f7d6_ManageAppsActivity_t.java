 package com.example.proauth;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PermissionInfo;
 import android.content.pm.ResolveInfo;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class ManageAppsActivity extends Activity {
 
 	// CheckBox prefCheckBox;
 	ListView appList;
 	public static String TAG = "ManageAppsActivity";
 	Context mContext;
 
 	String[] values;
 	String[] id_array;
 	Drawable[] app_icon_array;
 	SharedPreferences sp;
 	Editor e;
 
 	int selectedItem;
 	String app_to_edit;
 	
 	FileOutputStream log_file;
 	public static String LOG_FILE = "proauth_log.txt";
 
 	final String[] securityLevels = { SecurityLevel.PRIVATE.toString(),
 			SecurityLevel.HIGH.toString(), SecurityLevel.MEDIUM.toString(),
 			SecurityLevel.LOW.toString(), SecurityLevel.PUBLIC.toString(), };
 
 	static final HashSet<String> privatePerms = new HashSet<String>(Arrays.asList("android.permission.BROADCAST_SMS",
 			"android.permission.CALL_PHONE",
 			"android.permission.DELETE_PACKAGES",
 			"android.permission.READ_LOGS",
 			"android.permission.GET_TASKS",
 			"android.permission.SEND_SMS"));
 	
 	static final HashSet<String> mediumPerms = new HashSet<String>(Arrays.asList("android.permission.ACCESS_FINE_LOCATION",
 			 "android.permission.BLUETOOTH",
 			"android.permission.CAMERA",
 			"android.permission.MODIFY_PHONE_STATE",
 			 "android.permission.READ_CALENDAR",
 			"android.permission.READ_CONTACTS"));
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		//Log.d(TAG, "OnCreate");
 		super.onCreate(savedInstanceState);
 		mContext = this.getApplicationContext();
 		
 		File log_file_object = new File(mContext.getFilesDir(), LOG_FILE);
 		try {
 			log_file = new FileOutputStream(log_file_object);
 		} catch (FileNotFoundException e2) {
 			e2.printStackTrace();
 		}
 
 		// Disappear the icon/title
 		setContentView(R.layout.manage_apps);
 
 		// For the ListView
 		final PackageManager pm = getPackageManager();
 
 		ArrayList<String> apps = new ArrayList<String>();
 		ArrayList<String> ids = new ArrayList<String>();
 		ArrayList<Drawable> app_icons = new ArrayList<Drawable>();
 		// get a list of installed apps.
 		Intent intent = new Intent(Intent.ACTION_MAIN, null);
 		intent.addCategory(Intent.CATEGORY_LAUNCHER);
 
 
 		ArrayList<AppSecurity> ass = new ArrayList<AppSecurity>();
 		
 
 		// Grab and Set Preferences
 		sp = this.getSharedPreferences(TAG, MODE_PRIVATE);
 		e = sp.edit();
 
 		List<ResolveInfo> list = pm.queryIntentActivities(intent,
 				PackageManager.PERMISSION_GRANTED);
 		for (ResolveInfo rInfo : list) {
 			String pkg_name = rInfo.activityInfo.applicationInfo.packageName;
 			String app_name = rInfo.activityInfo.applicationInfo.loadLabel(pm).toString();
 			Drawable app_icon = pm.getApplicationIcon(rInfo.activityInfo.applicationInfo);
 			
 			if (ids.contains(pkg_name)){
 				continue;
 			}
 
 			// Get and Set Security
 			String security_level = sp.getString(pkg_name,
 					SecurityLevel.PUBLIC.toString());
 			
 			Log.d(TAG, "Getting permissions for " + pkg_name);
 			try {
 			    PackageInfo pkgInfo = getPackageManager().getPackageInfo(
 						    pkg_name, 
 						    PackageManager.GET_PERMISSIONS
 						  );
 			    String[] requestedPermissions = pkgInfo.requestedPermissions;
 			    if (requestedPermissions == null) {
 			    	Log.d(TAG, pkg_name + "... No requested permissions");
 			    } else {
 					for (int i = 0; i < requestedPermissions.length; i++) {
 					    if (mediumPerms.contains(requestedPermissions[i])){
 					    	Log.d(TAG, "Default to Medium because " + requestedPermissions[i]);
 					    	security_level = sp.getString(pkg_name, SecurityLevel.MEDIUM.toString());
 					    } else if (privatePerms.contains(requestedPermissions[i])){
 					    	Log.d(TAG, "Default to Private because " + requestedPermissions[i]);
 					    	security_level = sp.getString(pkg_name, SecurityLevel.PRIVATE.toString());
 					    	break;
 					    }
 						//Log.d(TAG, requestedPermissions[i]);
 					}
 			    }
 			}
 			catch (PackageManager.NameNotFoundException e) {
 				Log.d(TAG, "Failed to get request perms");
 			}
 			
 			
 			// Add to the Manage Apps Display
 			if (pkg_name.equals("com.android.phone") || pkg_name.equals("com.example.proauth")){	
 				// these apps aren't locked, and cannot be.
 				security_level = SecurityLevel.PUBLIC.toString();
 			} else {
 				apps.add(app_name);
 				ids.add(pkg_name);
 				//Log.d(TAG,"Image being saved:" + app_icon.toString());
 				app_icons.add(app_icon);
 				//Log.w(TAG, "Showing:" + rInfo.activityInfo.applicationInfo.packageName);
 				ass.add(new AppSecurity(app_name, pkg_name, security_level, app_icon));
 
 			}
 			e.putString(pkg_name, security_level);
 		}
 		appList = (ListView) findViewById(R.id.appList);
 		values = apps.toArray(new String[apps.size()]);
 		id_array = ids.toArray(new String[apps.size()]);
 		app_icon_array = app_icons.toArray(new Drawable[apps.size()]);
 		
 		e.commit();
 
 		AppSecurityArrayAdapter adapter = new AppSecurityArrayAdapter(this,
 				R.layout.app_security_listitem, ass);
 		appList.setAdapter(adapter);
 
 		// set listener for changing security level
 		OnItemClickListener listener = new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> myAdapter, View myView,
 					int myItemInt, long mylng) {
 				AppSecurity selectedFromList = (AppSecurity) (appList
 						.getItemAtPosition(myItemInt));
 				Log.d(TAG, "Selected the app: " + selectedFromList.toString());
 				app_to_edit = selectedFromList.app_id;
 				popDialog();
 			}
 		};
 		appList.setOnItemClickListener(listener);
 	}
 	
 	private void saveSecurityLevel(){
 		Log.d(TAG, "Saving..." + app_to_edit + selectedItem);
 		String securityLevel = securityLevels[selectedItem];
 		e.putString(app_to_edit, securityLevel);
 		e.commit();
 		refreshApps();
 	}
 
 	private void popDialog() {
 		// make dialog
 		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
 		alt_bld.setIcon(R.drawable.ic_launcher);
 		alt_bld.setTitle("Choose a Security Level");
 		selectedItem = -1;
 		alt_bld.setSingleChoiceItems(securityLevels, -1,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 						selectedItem = item;
 					}
 				});
 		alt_bld.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				Toast.makeText(getApplicationContext(),
 						"Saving: " + securityLevels[selectedItem],
 						Toast.LENGTH_SHORT).show();
 				saveSecurityLevel();
 			}
 		});
 		
 		alt_bld.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// Do Nothing.
 					}
 				});
 
 		AlertDialog alert = alt_bld.create();
 		alert.show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		// getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		Intent intent = new Intent("com.example.proauth.FINISH_ACTIVITY");
     	sendBroadcast(intent);
 		finish();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		/*
 		 * Because it's only ONE option in the menu. In order to make it simple,
 		 * We always start SetPreferenceActivity without checking.
 		 */
 		/*
 		 * Intent intent = new Intent();
 		 * intent.setClass(ManageAppsActivity.this,
 		 * SetPreferencesActivity.class); startActivityForResult(intent, 0);
 		 */
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		// super.onActivityResult(requestCode, resultCode, data);
 
 		/*
 		 * To make it simple, always re-load Preference setting.
 		 */
 
 		refreshApps();
 	}
 
 	private void refreshApps() {
 		Log.d(TAG, "Refresh");
 		ArrayList<AppSecurity> ass = new ArrayList<AppSecurity>();
 		for (int i = 0; i < values.length; i++) {
 			String security_level = sp.getString(id_array[i],
 					SecurityLevel.PRIVATE.toString());
 			e.putString(values[i], security_level);
 			ass.add(new AppSecurity(values[i], id_array[i], security_level, app_icon_array[i]));
 		}
 
 		AppSecurityArrayAdapter adapter = new AppSecurityArrayAdapter(this,
 				R.layout.app_security_listitem, ass);
 		appList.setAdapter(adapter);
 	}
 
 	public class AppSecurity {
 		public String app_name;
 		public String app_id;
 		public String app_security_level;
 		public Drawable app_icon;
 
 		public AppSecurity(String app_name, String id, String sl, Drawable app_icon) {
 			this.app_name = app_name;
 			this.app_id = id;
 			this.app_security_level = sl;
 			this.app_icon = app_icon;
 		}
 
 		@Override
 		public String toString() {
 			return (this.app_name + " " + this.app_security_level);
 		}
 	}
 	
 
 	/*
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 	}
 	*/
 
 	public class AppSecurityArrayAdapter extends ArrayAdapter<AppSecurity> {
 		private ArrayList<AppSecurity> apps;
 
 		public AppSecurityArrayAdapter(Context context, int textViewResourceId,
 				ArrayList<AppSecurity> app_security_settings) {
 			super(context, textViewResourceId, app_security_settings);
 			this.apps = app_security_settings;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.app_security_listitem, null);
 			}
 
 			AppSecurity app_security = apps.get(position);
 			if (app_security != null) {
 				TextView app_name = (TextView) v.findViewById(R.id.app_name);
 				TextView security_level = (TextView) v
 						.findViewById(R.id.app_security_level);
 				ImageView app_icon = (ImageView) v.findViewById(R.id.app_icon);
 
 				if (app_name != null) {
 					app_name.setText(app_security.app_name);
 				}
 
 				if (security_level != null) {
 					security_level.setText("Security Level: "
 							+ app_security.app_security_level);
 				}
 				
 
 				if (app_icon != null) {
 					app_icon.setImageDrawable(app_security.app_icon);
 				}
 			}
 			return v;
 		}
 	}
 }
