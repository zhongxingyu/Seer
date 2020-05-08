 package com.changeit.mtbrowser;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.WindowManager;
 import android.webkit.GeolocationPermissions;
 import android.webkit.JsResult;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import com.changeit.wmpolyfill.WebClient;
 import com.changeit.wmpolyfill.helper.Alert;
 
 public class MultitouchBrowser extends Activity
 {
 
     WebView webview;
     Boolean webviewVisible;
     String[] urls = {
 	"http://openlayers.org/dev/examples/mobile.html",
 	"http://maps.google.de",
 	"http://ows.terrestris.de/webgis-client/index.html",
 	"http://eightmedia.github.com/hammer.js/#touchme",
 	"http://scripty2.com/demos/touch/pinchariffic/",
	"http://scripty2.com/demos/touch/testbed/",
	"http://www.dhteumeuleu.com/never-force",
//	"http://www.pluginmedia.net/dev/infector/"
 //	"http://leaflet.cloudmade.com/examples/mobile-example.html",
 //	"http://www.mapsmarker.com/wp-content/plugins/leaflet-maps-marker/leaflet-fullscreen.php?marker=1",
 //	"http://mapbox.com/easey/",
 //	"http://jacobtoye.github.com/Leaflet.draw/"
     };
 
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
	super.onRestoreInstanceState(savedInstanceState); 
	//To change body of generated methods, choose Tools | Templates.
    }

     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
 	super.onCreate(savedInstanceState);
 
 	// Hide the status bar at the top
 	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 	// Adds Progress bar Support
 	getWindow().requestFeature(Window.FEATURE_PROGRESS);
 	// Makes Progress bar Visible
 	getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
 
 	final Activity MyActivity = this;
 	WebChromeClient wcc = new WebChromeClient()
 	{
 	    @Override
 	    public void onProgressChanged(WebView view, int progress)
 	    {
 		//Make the bar disappear after URL is loaded, and changes string to Loading...
 		MyActivity.setTitle("Loading " + view.getUrl() + " ... ");
 		MyActivity.setProgress(progress * 100); //Make the bar disappear after URL is loaded
 
 		// Return the app name after finish loading
 		if (progress == 100) {
 		    MyActivity.setTitle(R.string.app_name);
 		}
 	    }
 
 	    @Override
 	    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback)
 	    {
 		showConfirmGeolocation(origin, callback);
 	    }
 
 	    @Override	// enable alert javascript, will generate native Android alert
 	    public boolean onJsAlert(WebView view, String url, String message, JsResult result)
 	    {
 		Alert alert = new Alert(view);
 		alert.show(message + ", Javascript Result [" + result.toString() + "];");
 		return true;
 	    }
 	};
 
 	webview = new WebView(this);
 
 	// remove white invisible scrollbar which otherwise generated white bar on the right side
 	webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
 
 	WebClient wmp = new WebClient(webview);
 //	wmp.setPolyfillAllTouches(true);
 	webview.setWebChromeClient(wcc);
 	displayStartupText();
     }
 
     protected void displayStartupText()
     {
 	setContentView(R.layout.main);
     }
 
     /**
      * Getting the back button to work
      *
      * @param keyCode
      * @param event
      * @return
      */
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
 	if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 	    if (webview.canGoBack() && webview.isShown()) {
 		webview.goBack();
 		return true;
 	    } else if (webview.isShown()) {
 		showExitDialog();
 		return false;
 	    }
 	}
 	return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
 	menu.add(2, 100, Menu.FIRST + 0, "Open Streetmap");
 	menu.add(1, 3, Menu.FIRST + 1, "Manage Bookmarks").setEnabled(false);
 	menu.add(1, 2, Menu.FIRST + 2, "Bookmarks");
 	menu.add(1, 1, Menu.FIRST + 3, "Add").setEnabled(false);
 	menu.add(1, 0, Menu.FIRST + 4, "Go to URL...");
 	menu.add(1, 4, Menu.FIRST + 5, "Preferences");
 
 	return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
 	Alert alert = new Alert(webview);
 	if (item.getGroupId() == 2) {
 	    this.loadUrl(urls[item.getItemId() - 100]);
 	} else {
 	    if (item.getItemId() == 2) {
 		showLinkList();
 	    } else {
 		alert.show("you clicked on item " + item.getTitle());
 	    }
 	}
 
 	return super.onOptionsItemSelected(item);
     }
 
     protected void loadUrl(String url)
     {
 	if (!webview.isShown()) {
 	    setContentView(webview);
 	}
 
 	webview.loadUrl(url);
     }
 
     protected void showExitDialog()
     {
 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	builder.setMessage("You want to proceed and close the Multitouch Browser ?")
 		.setTitle("Close");
 	final Activity MyActivity = this;
 	builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
 	{
 	    public void onClick(DialogInterface dialog, int id)
 	    {
 		dialog.dismiss();
 	    }
 	});
 	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
 	{
 	    public void onClick(DialogInterface dialog, int id)
 	    {
 		dialog.dismiss();
 	    }
 	});
 	AlertDialog dialog = builder.create();
 	dialog.show();
     }
 
     protected void showConfirmGeolocation(final String origin, final GeolocationPermissions.Callback callback)
     {
 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	builder.setMessage("Allow " + webview.getUrl() + " to access you position ?")
 		.setTitle("Geolocation");
 	builder.setPositiveButton("Locate Me", new DialogInterface.OnClickListener()
 	{
 	    public void onClick(DialogInterface dialog, int id)
 	    {
 		callback.invoke(origin, true, false);
 	    }
 	});
 	builder.setNegativeButton("Deny", new DialogInterface.OnClickListener()
 	{
 	    public void onClick(DialogInterface dialog, int id)
 	    {
 		dialog.dismiss();
 	    }
 	});
 	AlertDialog dialog = builder.create();
 	dialog.show();
     }
 
     public void showLinkList()
     {
 	final String[] urlNames = {
 	    "Open Streetmap",
 	    "Google Maps",
 	    "Terrestris",
 	    "Hammer JS Demo",
 	    "Pinchariffic",
	    "Scripty2 Testbed",
	    "Game"
		
 //	    "Leaflet Mobile Demo",
 //	    "Modest Maps",
 //	    "VisualMobility.tk (Leaflet)"
 	};
 
 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	builder.setTitle("Go to bookmark")
 		.setItems(urlNames, new DialogInterface.OnClickListener()
 	{
 	    public void onClick(DialogInterface dialog, int which)
 	    {
 		// The 'which' argument contains the index position
 		// of the selected item
 		loadUrl(urls[which]);
 	    }
 	});
 	AlertDialog dialog = builder.create();
 	dialog.show();
     }
 }
