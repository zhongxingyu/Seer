 package com.megamip.voice;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.webkit.WebView;
 import android.widget.Toast;
 
 public class MipReceiver {
 	
 	protected static final int RESULT_SPEECH = 1;
 	private static final String JAVASCRIPT = "javascript:";
 	private static final String BRC = "()";
 	private static final String BRC_OPEN = "('";
 	private static final String BRC_CLOSE = "')";
 	private static final String Q = "?";
 	private static final String HTML_ROOT = "file:///mnt/sdcard/DCIM/gui/";	
 	private static final String TAG = "A3";
 	
 	// actors 
 	
 	private Handler handler; 
 	private WebView webView;
 	private Activity activity;
 // constructor 
 	public MipReceiver() {}
 	
 	public MipReceiver(Handler handler, WebView webView, Activity activity) {
 		this.handler = handler;
 		this.webView = webView;
 		this.activity = activity;
 	}
 
 
 // public methods 
 	
  public void pictureSearch(String keywords){
 	 
 	 String seq = "clearScreen();hideEyes();showCenterPanel();pictureSearch";
 	 callJsFunction(seq, keywords);
 
 	 
  }
  
  public void videoSearch(String keywords){
 	 
 	 String seq = "clearScreen();hideEyes();showCenterPanel();videoSearch";
 	 callJsFunction(seq, keywords);
  }
  
  public void webSearch(){}
  
  public void guiNext() {
 		callJsFunction("next", "");
 	}
  
  public void guiBack() {
 		
	// callJsFunction("back", "");
	 webView.performClick();
 		
  }
  
  public void guiHome() {
 	 String seq = "clearScreen();hideCenterPanel();showEyes";
 	 callJsFunction(seq, "");
 		
 	}
   
  public void guiShow() {
 	
 	 callJsFunction("show", "");
 	}
  
  
  public void speak(){
 	 
 		// TODO Auto-generated method stub
 		Intent intent = new Intent(
 				RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
 
 		try {
 			
 			activity.startActivityForResult(intent, RESULT_SPEECH);
 		
 		} catch (ActivityNotFoundException a) {
 			Toast t = Toast.makeText(activity.getApplicationContext(),
 					"Ops! Your device doesn't support Speech to Text",
 				Toast.LENGTH_SHORT);
 			
 		}
  }
  
  
  // methodes utilitaires
  
  public void callJsFunction(String functionName,String args){
 		String json = "";
 //		final String callbackFunction = JAVASCRIPT + "f1" + BRC_OPEN + json 
 //	  		+ BRC_CLOSE;
 		final String callbackFunction = JAVASCRIPT + functionName+BRC_OPEN+args+BRC_CLOSE ;
 		Log.d(TAG ,callbackFunction);
 		loadURL(callbackFunction); 	  	
 	}
 
  
  private void loadURL(final String in){
  	handler.post(new Runnable() {
          public void run() {
          	webView.loadUrl(in);
          }
      });
  }
 
 
 
 
 
 
 
 
 
 
 }
