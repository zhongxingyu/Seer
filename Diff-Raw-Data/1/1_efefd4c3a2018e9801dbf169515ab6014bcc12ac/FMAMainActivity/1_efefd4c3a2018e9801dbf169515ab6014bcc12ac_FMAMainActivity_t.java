 package com.unidevel.findmyapp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 import android.app.ProgressDialog;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabSpec;
 import android.widget.Toast;
 
 
 public class FMAMainActivity extends TabActivity implements OnTabChangeListener {
 	ProgressDialog progressDialog;
 	ListView listSystem;
 	ListView listUser;
 	ListView listSaved;
 	ListView appView;
 	boolean loadedInstall;
 	Handler handler;
 	File appFile;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTitle(getString(R.string.app_name));
 		TabHost host = getTabHost();
 		LayoutInflater.from(this).inflate(R.layout.fma_main, host.getTabContentView(), true);
 		TabSpec spec;
 		spec = host.newTabSpec("system").setIndicator(getString(R.string.tab_system), getResources().getDrawable(R.drawable.system));
 		spec.setContent(R.id.tab1);
 		host.addTab(spec);
 		spec = host.newTabSpec("user").setIndicator(getString(R.string.tab_user), getResources().getDrawable(R.drawable.user));
 		spec.setContent(R.id.tab2);
 		host.addTab(spec);
 		spec = host.newTabSpec("saved").setIndicator(getString(R.string.tab_saved), getResources().getDrawable(R.drawable.newapp));
 		spec.setContent(R.id.tab3);
 		host.addTab(spec);
 		handler = new Handler();
 		listSystem = (ListView)findViewById(R.id.listSystem);
 		listUser = (ListView)findViewById(R.id.listUser);
 		listSaved = (ListView)findViewById(R.id.listSaved);
 		host.setOnTabChangedListener(this);
 		appView = listSystem;
 		File dir = new File(Environment.getExternalStorageDirectory(), "FindMyApp");
 		if (!dir.exists())	dir.mkdirs();
 		appFile = new File(dir, "apps.txt");
 		loadedInstall = false;
 		onTabChanged("system");
 		
 		registerForContextMenu(listSystem);
 		registerForContextMenu(listUser);
 		registerForContextMenu(listSaved);
 	}
 
 	@Override
 	public void onTabChanged(String tag) {
 		Log.i("onTabChanged", tag);
 //		Util.getAllApps(this);
 		if ( "system".equals(tag) ) {
 			appView = listSystem;
 		}
 		else if ( "user".equals(tag) ){
 			appView= listUser;
 		}
 		else {
 			appView = listSaved;
 		}
 		if ( !loadedInstall ) {
 			loadInstalled();
 			loadedInstall = true;
 		}
 	}
 	
 	public void loadInstalled() {
 		progressDialog = ProgressDialog.show(this, getString(R.string.title_loading), getString(R.string.msg_loading));
 		progressDialog.setCancelable(true);
 		new LoadAppsThread().start();
 	}
 	
 	public class LoadAppsThread extends Thread {
 
 		public LoadAppsThread() {
 		}
 
 		@Override
 		public void run() {
 			final List<AppInfo>[] apps = Util.getAllApps(FMAMainActivity.this);
 			HashMap<String,AppInfo> installedApps = new HashMap<String,AppInfo>();
 			for ( AppInfo app: apps[0] ) {
 				installedApps.put(app.getPackageName(), app);
 				Collections.sort(apps[0]);
 			}
 			for ( AppInfo app: apps[1] ) {
 				installedApps.put(app.getPackageName(), app);
 				Collections.sort(apps[1]);
 			}
 			
 			final List<AppInfo> savedApps = new ArrayList<AppInfo>();
 			FileInputStream input;
 			Properties props = new Properties();
 			try {
 				input = new FileInputStream(appFile);
 				props.load(input);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			File dir = appFile.getParentFile();
 			for (Object key : props.keySet()) {
 				String packageName = String.valueOf(key);
 				
 				AppInfo info = installedApps.get(packageName);
 				if ( info == null ) {
 					info = new AppInfo();
 					info.packageName = packageName;
 					info.name = String.valueOf(props.get(key));
 					File file = new File(dir, info.packageName+".png");
 					if ( file.exists() ) {
 						Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
 						info.icon = new BitmapDrawable(bitmap);
 					}
 					savedApps.add(info);
 				}
 				else {
 					info.hasHistory = true;
 				}
 				Collections.sort(savedApps);
 			}
 			
 			handler.post(new Runnable(){
 				@Override
 				public void run() {
 					AppItemAdapter adapter = new AppItemAdapter(FMAMainActivity.this, apps[0]);
 					listSystem.setAdapter(adapter);
 					adapter = new AppItemAdapter(FMAMainActivity.this, apps[1]);
 					listUser.setAdapter(adapter);
 					adapter = new AppItemAdapter(FMAMainActivity.this, savedApps);
 					listSaved.setAdapter(adapter);
 					
 					progressDialog.dismiss();
 				}
 			});
 		}
 	}
 	
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		if ( item.getItemId() == R.id.menuFind ) {
 			AdapterView.AdapterContextMenuInfo info = 
 				(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 			AppInfo app = (AppInfo) appView.getAdapter().getItem(info.position);
 			String APP_MARKET_URL = "market://details?id="+ app.packageName;
 			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_MARKET_URL));
			intent.setPackage("com.android.vending");
 			startActivity(intent);
 		}
 		else if ( item.getItemId() == R.id.menuRun ) {
 			AdapterView.AdapterContextMenuInfo info = 
 				(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 			AppInfo app = (AppInfo) appView.getAdapter().getItem(info.position);
 			Intent intent = getPackageManager().getLaunchIntentForPackage(app.packageName);
 			if( intent != null )startActivity(intent);
 			else {
 				
 			}
 		}
 		else if ( item.getItemId() == R.id.menuDelete ) {
 			AdapterView.AdapterContextMenuInfo info = 
 				(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 			AppItemAdapter apps = (AppItemAdapter) appView.getAdapter();
 			apps.remove(info.position);
 		}
 		else if ( item.getItemId() == R.id.menuUninstall ) {
 			AdapterView.AdapterContextMenuInfo info = 
 				(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 			AppInfo app = (AppInfo) appView.getAdapter().getItem(info.position);
 			Uri packageURI = Uri.parse("package:"+app.packageName);
 			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
 			startActivity(uninstallIntent);			
 		}
 		else if ( item.getItemId() == R.id.menuBackup ) {
 			AdapterView.AdapterContextMenuInfo info = 
 				(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 			AppInfo app = (AppInfo) appView.getAdapter().getItem(info.position);
 			File file = new File(app.path);
 			try {
 				File dir = new File(Environment.getExternalStorageDirectory(),"FindMyApp");
 				if ( !dir.exists() )dir.mkdirs();
 				File newFile = new File(dir, file.getName());
 				FileInputStream input = new FileInputStream(file);
 				FileOutputStream output = new FileOutputStream(newFile);
 				byte[] buf = new byte[8192];
 				int len;
 				while ( (len = input.read(buf)) > 0 ){
 					output.write(buf,0, len);
 				}
 				output.flush();
 				output.close();
 				input.close();
 				Toast.makeText(this, "Backup "+app.name+" to "+newFile.getPath(), 3).show();
 			}
 			catch(Throwable ex){
 				Log.e("Backup failed", ex.getMessage(), ex);
 				Toast.makeText(this, "Backup "+app.name+ " failed", 3).show();
 			}
 		}
 		return true;
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		if (v == appView) {
 			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 			AppInfo app = (AppInfo) appView.getAdapter().getItem(info.position);
 			menu.setHeaderTitle(app.name);
 			menu.add(Menu.NONE, R.id.menuFind, 1, R.string.menu_find);
 			if (!app.hasHistory) {
 				menu.add(Menu.NONE, R.id.menuDelete, 2, R.string.menu_remove);
 			}
 			else {
 				menu.add(Menu.NONE, R.id.menuRun, 0, R.string.menu_run);
 				menu.add(Menu.NONE, R.id.menuUninstall, 4, R.string.menu_uninstall);
 			}
 			if ( app.path != null ) {
 				menu.add(Menu.NONE, R.id.menuBackup, 2, R.string.menu_backup);
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		this.getMenuInflater().inflate(R.menu.myapp, menu);
 		return true;
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (R.id.menuRefresh == item.getItemId()) {
 			loadInstalled();
 		} else if (R.id.menuExport == item.getItemId()) {
 			AppItemAdapter adapter = (AppItemAdapter) listSystem.getAdapter();
 			List<AppInfo> apps = new ArrayList<AppInfo>(); 
 			apps.addAll(adapter.getApps());
 			adapter = (AppItemAdapter) listUser.getAdapter();
 			apps.addAll(adapter.getApps());
 			adapter = (AppItemAdapter) listSaved.getAdapter();
 			apps.addAll(adapter.getApps());
 
 			Properties props = new Properties();
 			File dir = appFile.getParentFile();
 			for (AppInfo app : apps) {
 				props.setProperty(app.packageName, app.name);
 				File file = new File(dir, app.packageName+".png");
 				if ( !file.exists() && app.icon != null ){
 					try {
 						saveDrawable((BitmapDrawable) app.icon, file);
 					}
 					catch(IOException ex){
 						Log.e("Export", ex.getMessage(), ex);
 					}
 				}
 			}
 			FileOutputStream out;
 			try {
 				out = new FileOutputStream(appFile);
 				props.save(out, "");
 				Toast.makeText(this, "Succeeded export to file " + appFile, 3)
 						.show();
 			} catch (FileNotFoundException e) {
 				Toast.makeText(this, "Error:" + e.getMessage(), 3).show();
 			}
 		}
 		// else if ( R.id.menuImport == item.getItemId() ) {
 		// loadImport(file, appView);
 		// }
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void saveDrawable(BitmapDrawable image, File file) throws IOException{
 		FileOutputStream out = new FileOutputStream(file);
 		Bitmap bitmap = image.getBitmap();
 		bitmap.compress(CompressFormat.PNG, 80, out);
 		out.flush();
 		out.close();
 	}
 }
