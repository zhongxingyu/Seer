 package com.maveric;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Spinner;
 
 public class ExceriseImageShowActivity extends MavericBaseActiity {
 
 	@Override
 	protected void setContentToLayout() {
 		setContentView(R.layout.imagelist);
 
 	}
 
 	Context ctx;
 	Spinner exceriseSpinner;
 	WebView exceriseImage;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		exceriseSpinner = (Spinner) findViewById(R.id.excerisespinner);
 		exceriseImage = (WebView) findViewById(R.id.exceriseimag);
 		// exceriseImage.getSettings().setJavaScriptEnabled(true);
 		exceriseSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> list, View arg1, int pos,
 					long arg3) {
 				try {
 					String data = list.getItemAtPosition(pos).toString();
					
					if(!data.equalsIgnoreCase("-- Please select --")){
 					exceriseImage.loadUrl("file:///android_asset/excerisegif/" + data
 							+ ".gif");
					}
 				} catch (Exception e) {
 					Log.i("kumar", "error in webview" + e.getMessage());
 				}
 
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 		});
 	}
 }
