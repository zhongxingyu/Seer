 package com.android.task.main;
 
 import com.android.task.R;
 import com.android.task.main.function.CheckAppClientExit;
 import com.android.task.main.function.MainPage;
 import com.android.task.main.function.SysSetting;
 import com.android.task.main.function.UrlConfigure;
 import com.android.task.tools.CustomExceptionHandler;
 import com.android.task.tools.InsertFileToMediaStore;
 import com.android.task.tools.ScaleBitmap;
 import com.android.task.tools.UploadMessage;
 import com.android.task.tools.EquipmentId;
 import com.android.task.web.MyWebChromeClient;
 import com.android.task.web.MyWebClient;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.webkit.WebView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /* Change Log */
 /* 0.11 
  * 	   Bug Fix:
  * 	   1. ȡbitmapڴƵbug
  * 	   2. ֧ú˼ȡϴݻҳ
 * 0.12
 * 	   Feature:
 * 	   1. web¼
  */
 public class WebMainActivity extends Activity {
 	
	public  static final String         VERSION		  = "0.12";
 	public  ProgressDialog ProgressDialog = null;
 
 	private final boolean	   APP_DEBUG    = false;
 	private final String TAG = WebMainActivity.class.getName();
 	
 	private WebView mWebView;
 	private UrlConfigure mUrlConf;
 	private CheckAppClientExit mExit;
 	private MainPage		   mMpage;
 	private SysSetting		   mSysSetting;
 	private EquipmentId		   mEquipmentId;
 	
 	
     @SuppressWarnings("static-access")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
     	Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
         super.onCreate(savedInstanceState);
         this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
         
         this.getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
 
         this.setProgressBarVisibility(true);
         ProgressDialog = new ProgressDialog(this);
     	
         ProgressDialog.setCancelable(true);
         ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         ProgressDialog.setTitle("ݴ...");
         mEquipmentId = new EquipmentId(this);
 
         setContentView(R.layout.web_main);
         
         mWebView = (WebView) findViewById(R.id.main_webview);
         mWebView.setFocusable(true);
         mWebView.getSettings().setJavaScriptEnabled(true);
         mWebView.getSettings().setUserAgentString(mEquipmentId.getId());
        mWebView.getSettings().setSavePassword(false);
         
         mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
         mWebView.setWebChromeClient(new MyWebChromeClient(this));
         mWebView.setWebViewClient(new MyWebClient(this));
         
         
         mUrlConf 		= new UrlConfigure(this,this.mWebView);
         mSysSetting		= new SysSetting(this,this.mWebView);
         
         mWebView.loadUrl(mUrlConf.getUrl());
         
         mExit	= new CheckAppClientExit(this);
         mMpage  = new MainPage(this);
 
         TextView menu_text = (TextView)findViewById(R.id.main_menu_text);
         menu_text.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View arg0) {
 				if (WebMainActivity.this.APP_DEBUG){
 					Log.d(TAG,"DEBUGģʽ");
 					mMpage.getMainPageDialog().show();
 				}else{
 			        mWebView.loadUrl(mUrlConf.getUrl());
 			        Toast.makeText(WebMainActivity.this, "...", Toast.LENGTH_LONG).show();
 //			        Toast.makeText(WebMainActivity.this, String.valueOf(getRequestedOrientation()) , Toast.LENGTH_LONG).show();
 				}
 			}
 		});
         
         TextView config_text = (TextView)findViewById(R.id.main_config_text);
         config_text.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View arg0) {
 				mSysSetting.getSettingDialog().show();
 //				mUrlConf.getUrlDialog().show();
 			}
 		});
         
         TextView exit_text = (TextView)findViewById(R.id.main_exit_text);
         exit_text.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View arg0) {
 				mExit.getCheckAppExitDialog().show();
 			}
 		});
         
     }
     protected void onActivityResult(int requestCode, int resultCode,  Intent intent) 
     {
 
     	if(requestCode== MyWebChromeClient.FILECHOOSER_IMAG_RESULTCODE)  
     	{  
 
     		Uri result = intent == null || resultCode != RESULT_OK ? null  : intent.getData();  
     		ScaleBitmap scale_bitmap = new ScaleBitmap(this,result);
     		Bitmap      bit_map      = scale_bitmap.scale();
     		InsertFileToMediaStore insert_file = new InsertFileToMediaStore(this,bit_map,"image/jpeg");
 			Uri uri = insert_file.insert();
     		UploadMessage.set_upload_message(uri);
     		bit_map = null;
     		scale_bitmap.release();
     	}else if (requestCode == MyWebChromeClient.FILECHOOSER_VIDEO_RESULTCODE){
     		Uri result = intent == null || resultCode != RESULT_OK ? null  : intent.getData();  
     		UploadMessage.set_upload_message(result);
     	}else if (requestCode == MyWebChromeClient.CAPTURE_PICTURE_INTENT){
     		 if (resultCode != RESULT_OK  ){
     			 UploadMessage.set_upload_message(null);
     		 }else{
     			 if (UploadMessage.get_file_uri() != null){
     				 	try {
     				 		ScaleBitmap scale_bitmap = new ScaleBitmap(this,UploadMessage.get_file_uri());
 
     				 		Bitmap      bit_map      = scale_bitmap.scale();
 
     				 		if (bit_map == null){
     			    			 UploadMessage.set_upload_message(null);
 
     				 		}else{
     				 			InsertFileToMediaStore insert_file = new InsertFileToMediaStore(this,bit_map,"image/jpeg");
     				 			Uri uri = insert_file.insert();
     				 			UploadMessage.set_upload_message(uri);
 
     				 		}
     				 		bit_map = null;
     			    		scale_bitmap.release();
     				 	}catch (Exception e)
     				 	{
     				        Toast.makeText(WebMainActivity.this, "ļ", Toast.LENGTH_LONG).show();
     		    			 UploadMessage.set_upload_message(null);
     				 	}
     			 }else{
     				 UploadMessage.set_upload_message();
     			 }
     		 }
     	}else if (requestCode == MyWebChromeClient.CAPTURE_VIDEO_INTENT){
     		if (resultCode != RESULT_OK  ){
    			 	UploadMessage.set_upload_message(null);
    		 	}else{
    		 		UploadMessage.set_upload_message(intent.getData());
    		 	}
     	}
 
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {   
     	if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {  
     		UploadMessage.set_upload_message(null);
 			Log.d(TAG,"go back");
     		mWebView.goBack(); 
     		return true;
     	}
 		Log.d(TAG,"can't go back");
     	UploadMessage.set_upload_message(null);
     	return super.onKeyDown(keyCode, event);
     }
 }
