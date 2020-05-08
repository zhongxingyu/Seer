 package com.dbstar.app.settings;
 
 import com.dbstar.R;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 
 public class GDAdvancedToolsActivity extends Activity {
 	private static final String TAG = "GDAdvancedToolsActivity";
 
 	private class Item {
 		public String component;
 		public String activity;
 	};
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
		setContentView(R.layout.advanced_tools_view);
 
 		initializeView();
 	}
 	
 	private void initializeView() {
 		Button btn = (Button) findViewById(R.id.btn_settings);
 		btn.setOnClickListener(mListener);
 		Item item = new Item();
 		btn.setTag(item);
 		item.component = "com.android.settings";
 		item.activity = "Settings";
 		
 		btn = (Button) findViewById(R.id.btn_webbrowser);
 		btn.setOnClickListener(mWBBtnListener);
 		
 		btn = (Button) findViewById(R.id.btn_filebrowser);
 		btn.setOnClickListener(mListener);
 		item = new Item();
 		btn.setTag(item);
 		item.component = "com.fb.FileBrower";
 		item.activity = "FileBrower";
 		
 	}
 	
 	View.OnClickListener mListener = new View.OnClickListener() {
 		
 		public void onClick(View v) {
 			if (v instanceof Button) {
 				Button btn = (Button) v;
 				
 				Item item = (Item) btn.getTag();
 				startComponent(item.component, item.activity);
 			}
 		}
 	};
 	
 	View.OnClickListener mWBBtnListener = new View.OnClickListener() {
 		
 		public void onClick(View v) {
 			startWebBrowser();
 		}
 	};
 	
 	
 	private void startComponent(String packageName, String activityName) {
 		Intent intent = new Intent();
 		String componentName = packageName + "." + activityName;
 		intent.setComponent(new ComponentName(packageName, componentName));
 		intent.setAction("android.intent.action.VIEW");
 
 		Log.d(TAG, "start " + componentName);
 		startActivity(intent);
 	}
 	
 	private void startWebBrowser() {
 		Intent webIntent = new Intent(Intent.ACTION_VIEW);
 		webIntent.setData(Uri.parse("http://www.baidu.com"));
 		startActivity(webIntent);
 	}
 }
