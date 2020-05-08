 /*
  * Copyright (C) 2011 by Alexandre Jasmin <alexandre.jasmin@gmail.com>
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.github.ajasmin.telususagewidget;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.appwidget.AppWidgetManager;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class ReportAccountErrorActivity extends Activity {
 	private static final int POST_IN_PROGRESS = 0;
 	private static final int POST_COMPLETE = 1;
 	private static final int POST_ERROR = 2;
 	
 	static private class PostDataThread extends Thread {
 		public InputStream dataStream;
 		
 		public AtomicReference<Handler> postCompletedHandler = new AtomicReference<Handler>();
 		public AtomicInteger result = new AtomicInteger(POST_IN_PROGRESS);
 		@Override
 		public void run() {
 			final DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("https://telus-usage-widget.appspot.com/error_report");
 			httpPost.setEntity(new InputStreamEntity(dataStream, -1));
 			
 			int r = POST_COMPLETE;
 			try {
 				httpclient.execute(httpPost);
 			} catch (Exception e) {
 			    r = POST_ERROR;
 			}
 			
 			result.set(r);
 			Handler handler = postCompletedHandler.get();
 			if (handler != null)
 			    handler.sendEmptyMessage(0);
 		}
 	}
 	
 	
 	private static Map<Integer, PostDataThread> postDataThreadsMap
 	        = new HashMap<Integer, PostDataThread>();
 	
 	private int appWidgetId;
 	private PostDataThread postDataThread;
 	
 	private ProgressDialog progressDialog;
 	private AlertDialog errorDialog;
 	private AlertDialog completeDialog;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		retriveAppWidgetId();
 		
         setContentView(R.layout.report_account_error);        
         
 		configureEventHandlers();
 		setupWebView();
 	}
 	
     private void retriveAppWidgetId() {
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
         appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
         if (extras != null) {
             appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
         }
         
         // Finish the activity if we don't receive an appWidgetId
         if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
             finish();
     }
 	
 	private void configureEventHandlers() {
 		final Button sendButton = (Button) findViewById(R.id.button_send);
 		
 		CheckBox agree = (CheckBox) findViewById(R.id.agree);
 		agree.setOnCheckedChangeListener(new OnCheckedChangeListener() { public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 			sendButton.setEnabled(isChecked);
 		}});
 		
 		sendButton.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) {
 			initiateDataPost();
 		}});
 		
 		Button cancelButton = (Button) findViewById(R.id.button_cancel);
 		cancelButton.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) {
 			finish();
 		}});
 	}
 	
 	   private void setupWebView() {
 	        // Load cached usage report in the WebView
 	        WebView webview = (WebView) findViewById(R.id.web_view);
 	        try {
 	            URL url = getFileStreamPath(""+appWidgetId).toURL();
 	            webview.loadUrl(url.toString());
 	        } catch (MalformedURLException e) {
 	            throw new Error(e);
 	        }
 	        
 	        // Prevent links in the WebView from being used
 	        webview.setWebViewClient(new WebViewClient() {
 	            public boolean shouldOverrideUrlLoading(WebView view, String url) {
 	                return true;
 	            }
 	        });
 	    }
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		postDataThread = postDataThreadsMap.get(appWidgetId);
         if (postDataThread != null) {
         	registerPostCompletedHandler();
         	showPostState();
         }
 	}
 
 	@Override
 	protected void onPause() {
 	    dismissDialogs();
 	    unregisterPostCompletedHandler();
 	    super.onPause();
 	}
    
 	private void initiateDataPost() throws Error {
 		postDataThread = new PostDataThread();
 		postDataThreadsMap.put(appWidgetId, postDataThread);
 		try {
 			postDataThread.dataStream = this.openFileInput(""+appWidgetId);
 		} catch (FileNotFoundException e) {
 			throw new Error(e);
 		}
 		registerPostCompletedHandler();
 		postDataThread.start();
 		showPostState();
 	}
 	
 	private void registerPostCompletedHandler() {
 		Handler handler = new Handler() {
         	@Override
         	public void handleMessage(Message msg) {
         		showPostState();
         	}
 		};
 		postDataThread.postCompletedHandler.set(handler);
 	}
 
 	private void unregisterPostCompletedHandler() {
 	    if (postDataThread != null)
 	        postDataThread.postCompletedHandler.set(null);
 	}
 	
 	private void showPostState() {
 		switch (postDataThread.result.get()) {
 			case POST_IN_PROGRESS:
 				if (progressDialog == null) {
 					progressDialog = ProgressDialog.show(this, null, "Sending data...");
 				}
 				break;
 			case POST_COMPLETE:
 				dismissDialogs();
 				if (completeDialog == null) {
 					completeDialog = new AlertDialog.Builder(this)
 						.setMessage("Report sent")
 						.setCancelable(false)
 						.setPositiveButton("Okay", new AlertDialog.OnClickListener() { public void onClick(DialogInterface dialog, int which) {
 						    postDataThreadsMap.remove(appWidgetId);
 							finish();
 						}})
 						.show();
 				}
 				break;
 			case POST_ERROR:
 				dismissDialogs();
 				if (errorDialog == null) {
 					errorDialog = new AlertDialog.Builder(this)
 						.setMessage("Unable to send data")
 						.setCancelable(false)
 						.setPositiveButton("Okay", new AlertDialog.OnClickListener() { public void onClick(DialogInterface dialog, int which) {
 						    postDataThreadsMap.remove(appWidgetId);
 							dismissDialogs();
 						}})
 						.show();
 				}
 				break;
 		}
 	}
 
 	private void dismissDialogs() {
 		if (progressDialog != null) {
 			progressDialog.dismiss();
 			progressDialog = null;
 		}
 		if (completeDialog != null) {
 			completeDialog.dismiss();
 			completeDialog = null;
 		}
 		if (errorDialog != null) {
 			errorDialog.dismiss();
 			errorDialog = null;
 		}
 	}
 }
