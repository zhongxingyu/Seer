 package edu.sjsu.cinequest;
 
 import edu.sjsu.cinequest.comm.Platform;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
import android.widget.Toast;
 
 
 /**
  * The WebView for registration page  
  * 
  * @author Prabhjeet Ghuman
  *
  */
 public class RegistrationActivity extends Activity {
 	private WebView webview;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         //Get the progress bar
         getWindow().requestFeature(Window.FEATURE_PROGRESS);
         //requestWindowFeature(Window.FEATURE_PROGRESS);	//use this or above commented line to get progress bar
        
         this.setProgressBarVisibility(true);		//set progress bar visiblity to true
         
         setContentView(R.layout.registrationpage_layout);
  
         //get the webview
 	   webview = (WebView) this.findViewById(R.id.webview);
 	   //set JavaScript
 	   webview.getSettings().setJavaScriptEnabled(true);
 	   
 	   /*Zoom Control on web (You don't need this if ROM supports Multi-Touch */
 	   webview.getSettings().setSupportZoom(true);
 	   
 	   //Enable Multitouch if supported by ROM
 	   webview.getSettings().setBuiltInZoomControls(true); 
 
 	   //workaround so that the default browser doesn't take over on link clicks
 	   webview.setWebViewClient(new MyWebViewClient());
 	   
 	   //Get the registration page URL from the bundle contained within Intent
 	   Intent getIntent = getIntent();
        Bundle b = getIntent.getExtras();       
        String url = b.getString("url");
        
        
        final Activity activity = this;
        
        //Display the progress bar of webview using WebChromeClient
        webview.setWebChromeClient(new WebChromeClient(){
     	   		@Override
                 public void onProgressChanged(WebView view, int progress) {
                         activity.setTitle("Loading...");
                         activity.setProgress(progress * 100); 
                         //Reset the app title after page loaded
                         if(progress == 100)
                              activity.setTitle(R.string.app_name);
                 }
        });
 		if (!Platform.getInstance().isNetworkAvailable()) {
    		Toast.makeText(this, this.getResources().getString(R.string.no_network_msg), 
    				Toast.LENGTH_LONG).show();			
     	   webview.setNetworkAvailable(false);
 		}else{
 			webview.setNetworkAvailable(true);
 	       //Load the URL
 	       webview.loadUrl(url);
 		}
     }
     
     /**
      * Override onConfigurationChanged, so that webview does not reload the page
      * once the screen orientation changes. 
      * Also added android:configChanges="orientation" in manifest file's activity tag.
      * This is cause android to call onConfigurationChanged on orientation change, instead of
      * restarting the activity
      */
     @Override
     public void onConfigurationChanged(Configuration newConfig){        
         super.onConfigurationChanged(newConfig);
     }
 	   
     /**
      * Private WebViewClient class for webview
      */
 	private class MyWebViewClient extends WebViewClient {
 	        @Override
 	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
 	            view.loadUrl(url);
 	            return true;
 	        }
 	}
 	
     /**
      * Create a menu to be displayed when user hits Menu key on device
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.registrationactivity_menu, menu);
         
         return true;
     }
     
     /** Menu Item Click Listener*/
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         	case R.id.menu_option_goback:
 	            webview.goBack();
 	            return true;
         	case R.id.menu_option_goforward:
 	            webview.goForward();
 	            return true;
         	case R.id.menu_option_reload:
 	            webview.reload();
 	            return true;
 	        
 	        default:
 	            return super.onOptionsItemSelected(item);
         }
         
     }
     
     /** This method is called before showing the menu to user after user clicks menu button*/
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     	if( !webview.canGoBack() )
     		menu.findItem(R.id.menu_option_goback).setEnabled(false);
     	else
     		menu.findItem(R.id.menu_option_goback).setEnabled(true);
     	
     	if( !webview.canGoForward() )
     		menu.findItem(R.id.menu_option_goforward).setEnabled(false);
     	else
     		menu.findItem(R.id.menu_option_goforward).setEnabled(true);
     	return super.onPrepareOptionsMenu(menu);
     }
 }
