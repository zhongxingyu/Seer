 package com.comp1008.happygui;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.webkit.WebView;
 
 public class MainActivity extends Activity {
 	private WebView webView;
 	public JSObject androidJS;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.splash);
 		webView = new WebView(this);
 		webView.setWebViewClient(new MyWebViewClient(this));
 		//setContentView(webView);
 		try {
 			loadEditor();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void loadEditor() throws IOException { // loads the editor into the webview
 		webView.getSettings().setJavaScriptEnabled(true);
 		webView.loadUrl("file:///android_asset/html5/editor.html");
 		androidJS = new JSObject(this, webView);
 		webView.addJavascriptInterface(androidJS, "jsObject");
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 
 		if (resultCode == RESULT_OK) {
 			if (requestCode == androidJS.CAMERA_PICTURE_INTENT) {
 				Log.d("onActivityResult", "Got Camera Picture!");
 				androidJS.addImage(androidJS.cameraFile);
 			} else if(requestCode == androidJS.GALLERY_IMAGE_INTENT) {
 				Log.d("onActivityResult", "Got Gallery Image - " + intent.getDataString());
 		        String[] filePathColumn = { MediaStore.Images.Media.DATA };
 		 
 		        Cursor cursor = getContentResolver().query(intent.getData(), filePathColumn, null, null, null);
 		        cursor.moveToFirst();
 		        String picturePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
 		        cursor.close();
 		         
 		        Log.d("onActivityResult", " = " + picturePath);
 		        androidJS.addImage(new File(picturePath));
 			}
 		}
 	}
 
 }
