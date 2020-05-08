 package kap.placity_beta;
 
 
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.widget.ProgressBar;
 
 @SuppressLint("SetJavaScriptEnabled")
 public class MainActivity extends Activity {
 	
 	public static WebView myWebView; //TODO: static ergnzt 
 	private ProgressBar myLoadingBar;
 	private receiver start;
 	
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_main);
         
         final Context myApp = this;
         
         Bundle b = getIntent().getExtras();
         String id = b.getString("ID");
         String sender = b.getString("sender");
         //setLoading(b.getBoolean("loading"));
         Log.v("sender ", sender);
         
         myWebView = (WebView) findViewById(R.id.webview);    
         myWebView.setWebChromeClient(new WebChromeClient(){  
             @Override  
             public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)   
             {  
                 new AlertDialog.Builder(myApp)  
                     .setTitle("")  
                     .setMessage(message)  
                     .setPositiveButton(android.R.string.ok,  
                             new AlertDialog.OnClickListener()   
                             {  
                                 public void onClick(DialogInterface dialog, int which)   
                                 {  
                                     result.confirm();  
                                 }
                             })  
                     .setCancelable(false)  
                     .create()  
                     .show();  
                   
                 return true;  
             }});
         WebSettings webSettings = myWebView.getSettings();
         webSettings.setJavaScriptEnabled(true);
         myWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); 
         myWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
         
        myWebView.loadUrl("file:///data/data/kap.placity_beta/app_games/"+id+"/0.html");
         
         myLoadingBar = (ProgressBar) findViewById(R.id.progressBar1);
         
         myLoadingBar.setVisibility(View.INVISIBLE);
         
 //        if (sender.equals("scanCode")) {
 //        	myWebView.setVisibility(View.INVISIBLE);
 //        	myLoadingBar.setVisibility(View.VISIBLE);
 //        }
 //        else {
 //            myWebView.setVisibility(View.VISIBLE);
 //            myLoadingBar.setVisibility(View.INVISIBLE);
 //        };
     }
 
     @Override
     public void onResume() {
     	super.onResume();
     	
     	start = new receiver();
     	registerReceiver(start, new IntentFilter("DownloadStep.ONE"));
     }
     
     @Override
     public void onStop() {
     	super.onStop();
     	
     	unregisterReceiver(start);
     }
     
     public class receiver extends BroadcastReceiver{
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			
 			//setLoading(false);
 			
 			myWebView.reload(); 
 		}
 	}
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     } 
     
     public void setLoading(boolean mode){
     	if (mode) {
     		myWebView.setVisibility(View.INVISIBLE);
     		myLoadingBar.setVisibility(View.VISIBLE);
     	}
     	else {
     		myWebView.setVisibility(View.VISIBLE);
             myLoadingBar.setVisibility(View.INVISIBLE);
     	}
     }
 }
