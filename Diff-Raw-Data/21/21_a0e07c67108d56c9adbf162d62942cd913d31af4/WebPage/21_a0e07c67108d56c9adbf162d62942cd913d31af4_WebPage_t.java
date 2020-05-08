 package edu.bonn.mobilegaming.geoquest.mission;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Xml.Encoding;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import edu.bonn.mobilegaming.geoquest.GeoQuestApp;
 import edu.bonn.mobilegaming.geoquest.Globals;
 import edu.bonn.mobilegaming.geoquest.R;
 import edu.bonn.mobilegaming.geoquest.ui.abstrakt.MissionOrToolUI;
 
 /**
  * Should display a webpage as a Mission.
  * 
  * @author Holger Muegge
  */
 
 public class WebPage extends MissionActivity implements OnClickListener {
 
     private Button okButton;
 
     WebView webview;
 
     /**
      * Called by the android framework when the mission is created. Setups the
      * View and calls the readXML method to get the dialogItems. The dialog
      * starts with the first dialogItem.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 
 	setContentView(R.layout.webpage);
 
 	this.webview = (WebView) findViewById(R.id.webview);
 	WebSettings webSettings = webview.getSettings();
 	webSettings.setJavaScriptEnabled(true);
 	webSettings.setBuiltInZoomControls(true);
	webSettings.setUseWideViewPort(true);
	webSettings.setLoadWithOverviewMode(true);
 	webSettings.setDefaultTextEncodingName(Encoding.UTF_8.toString());
	webSettings
		.setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20120427 Firefox/15.0a1");
	webview.setInitialScale(20);
 	webview.setWebViewClient(new HelloWebViewClient());
 
 	okButton = (Button) findViewById(R.id.webviewbutton);
 	String buttonText = mission.xmlMissionNode.attributeValue("buttontext");
 	if (buttonText == null)
 	    okButton.setText(R.string.button_text_proceed);
 	else
 	    okButton.setText((CharSequence) buttonText);
 
 	okButton.setOnClickListener(this);
 
 	// Load Webpage:
 	String uri = mission.xmlMissionNode.attributeValue("url");
 	String filepath = mission.xmlMissionNode.attributeValue("file");
 
 	if (uri != null) {
 	    // shuld load remote uri:
 	    if (GeoQuestApp.getInstance().isOnline()) {
 		// we are online => load uri:
 		this.webview.loadUrl(uri);
 	    } else {
 		// we can not load remote uri
 		if (filepath != null) {
 		    // load local file as specified for this case:
 		    loadLocalFile(filepath);
 		}
 	    }
 	} else {
 	    // no uri specified.
 	    if (filepath != null) {
 		loadLocalFile(filepath);
 	    }
 	}
     }
 
     private void loadLocalFile(String relativeFilePath) {
	String pathToLocalFile = "file://"
		+ GeoQuestApp.getRunningGameDir()
		+ "/"
		+ relativeFilePath;
 	this.webview.loadUrl(pathToLocalFile);
     }
 
     @Override
     public void onDestroy() {
 	super.onDestroy();
     }
 
     /**
      * On Click handler. If there is no dialogItem left the mission is over,
      * else the next dialogItem is shown.
      */
     public void onClick(View v) {
 	finish(Globals.STATUS_SUCCEEDED);
 	return;
     }
 
     @Override
     public boolean onKeyDown(int keyCode,
 			     KeyEvent event) {
	if ((keyCode == KeyEvent.KEYCODE_BACK)
		&& webview.canGoBack()) {
 	    webview.goBack();
 	    return true;
 	}
 	return super.onKeyDown(keyCode,
 			       event);
     }
 
     private class HelloWebViewClient extends WebViewClient {
 	@Override
 	public boolean shouldOverrideUrlLoading(WebView view,
 						String urlString) {
 	    Uri uri = Uri.parse(urlString);
	    if (urlString.toLowerCase().endsWith(".html")
		    || urlString.toLowerCase().endsWith(".htm")) {
 		view.loadUrl(urlString);
 	    } else if (urlString.toLowerCase().endsWith(".mp4")) {
 		Intent intent = new Intent(Intent.ACTION_VIEW);
 		intent.setDataAndType(uri,
 				      "video/mp4"); // TODO allgemein auf
 						    // MIME type
 						    // baseirend machen
 						    // mit
 						    // ContentResolver
 						    // und Mime.
 		startActivity(intent);
 	    } else if (urlString.toLowerCase().endsWith("-imgdir/")) {
 		Intent intent = new Intent(Intent.ACTION_VIEW);
 		intent.setDataAndType(uri,
 				      "image/*");
 		intent.putExtra(MediaStore.EXTRA_MEDIA_ALBUM,
 				uri.getPath());
 		startActivity(intent);
 	    } else {
 		return false;
 	    }
 	    return true;
 	}
     }
 
     public void onBlockingStateUpdated(boolean blocking) {
 	// TODO Auto-generated method stub
 
     }
 
     public MissionOrToolUI getUI() {
 	// TODO Auto-generated method stub
 	return null;
     }
 }
