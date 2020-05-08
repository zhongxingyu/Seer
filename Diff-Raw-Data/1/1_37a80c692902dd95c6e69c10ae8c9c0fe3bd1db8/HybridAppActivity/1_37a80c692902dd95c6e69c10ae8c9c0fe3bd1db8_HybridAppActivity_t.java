 package com.HybridApp;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ViewGroup.LayoutParams;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.widget.LinearLayout;
 import com.HybridApp.R;
 
 public class HybridAppActivity extends Activity {
     /** Called when the activity is first created. */
     // I add this line
     // I add this line in 'klimt_branch' by klimt1004
     // I add this line in 'klimt_branch' by klimt1004 again
    // I add this line in 'new-branch'
 	private WebView mWebView = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
       //mWebView = (WebView) findViewById(R.id.webView);
 		mWebView = new WebView(this);
 
 		mWebView.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 		setContentView(mWebView);
 		mWebView.loadUrl("file:///android_asset/www/index2.html");
 		mWebView.getSettings().setJavaScriptEnabled(true);
 		mWebView.addJavascriptInterface(new MyHybrid(), "MyHybrid");
 		mWebView.setWebChromeClient(new WebChromeClient() {
 			@Override
 			public void onConsoleMessage(String message, int lineNumber,
 					String sourceID) {
 				super.onConsoleMessage(message, lineNumber, sourceID);
 			}
 		});
     }
     
     class MyHybrid {
     	public String hello() {
     		return "Hello hybrid App.";
     	}
     	
     	public String setInt(int data) {
     		return "setInt:" + Integer.toString(data);
     	}
     	
     	public String setFloat(float data) {
     		return "setFloat:" + Float.toString(data);
     	}
     	
     	public String setFloat(String data) {
     		return "setFloat:" + data + "<= string";
     	}
     	
     	public String setBoolean(boolean data) {
     		return "setBoolean:" + Boolean.toString(data);
     	}
     	
     	public String setString(String data) {
     		return "setString:" + data;
     	}
     	
     	public String setObject(String obj) {
     		return "setObject:" + obj;
     	}
     	
     	public String setArray(String obj) {
     		return "setArray:" + obj;
     	}
     	
     	public String setFunction(String obj) {
     		return "setFunction:" + obj;
     	}
     	
     	public void hang(int time) {
     		try {
 				Thread.sleep(time*1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
     	}
 
     	public void asyncJob(final int time) {
     		Thread thread = new Thread(new Runnable() {
     			@Override
     			public void run() {
     				try {
     					Thread.sleep(time*1000);
     					mWebView.loadUrl("javascript:document.getElementById('tTest').innerHTML += \"finish asyncJob()<br/>\"");
     				} catch (InterruptedException e) {
     					e.printStackTrace();
     				}
     			}
     		});
     		thread.start();
     	}
     	
     	public void aFuncJob(final String strFunc, final int time) {
     		Log.e("", strFunc);
     		Thread thread = new Thread(new Runnable() {
     			@Override
     			public void run() {
     				try {
     					Thread.sleep(time*1000);
     					mWebView.loadUrl("javascript:" + strFunc + "(\"finish aFuncJob()<br/>\")");
     				} catch (InterruptedException e) {
     					e.printStackTrace();
     				}
     			}
     		});
     		thread.start();
     	}
     	
     	public void aFuncJobWithId(final String strId, final int time) {
     		Log.e("", strId);
     		Thread thread = new Thread(new Runnable() {
     			@Override
     			public void run() {
     				try {
     					Thread.sleep(time*1000);
     					mWebView.loadUrl("javascript:CbMgr.fireCbFunc(\"" + strId + "\", \"finish aFuncJobWithId()<br/>\")");
     				} catch (InterruptedException e) {
     					e.printStackTrace();
     				}
     			}
     		});
     		thread.start();
     	}
     	
     	/*
     	 * dot
     	 */
     	public int dotNum = 0;
     	
     	public void addDot(final String cbId, final String arg) {
     		dotNum++;
     	}
     	
     	public void startDot(final String cbId, final String arg) {
     		dotNum = 0;
     		
     		final Thread thread = new Thread(new Runnable() {
     			@Override
     			public void run() {
     				while(true) {
     					if(dotNum == 10) {
     						Log.e("aa", "detect");
     						mWebView.loadUrl("javascript:CbMgr.fireCbFunc(\"" + cbId + "\", \"finish1\")");
     						break;
     					} else {
     						try {
     	    					Thread.sleep(500);
     	    				} catch (InterruptedException e) {
     	    					e.printStackTrace();
     	    				}
     					}
     				}
     			}
     		});
     		thread.start();
     	}
     	
     	public void HybridFunc(final String strFunc, final String cbId, final String arg) {
     		Log.e("aa", "HybridFunc");
     		if("addDot".equals(strFunc)) {
     			addDot(cbId, arg);
     		} else if("startDot".equals(strFunc)) {
     			startDot(cbId, arg);
     		}
     	}
     }
 }
