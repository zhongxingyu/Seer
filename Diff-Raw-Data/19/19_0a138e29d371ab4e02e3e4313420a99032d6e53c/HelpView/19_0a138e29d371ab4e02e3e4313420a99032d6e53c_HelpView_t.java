 package gov.cdc;
 
 import gov.cdc.R;
 import android.app.Activity;
 import android.os.Bundle;
import android.view.View;
import android.view.Window;
 import android.webkit.WebView;
import android.widget.Button;
 
 /**
  * A simple activity to display the help.html page.  All this does is set the webView's content to assets/help.html.
  * @author Will
  *
  */
 public class HelpView extends Activity {
 	
	
 	private WebView webView;
 	
 	// set the the layout's helpView to webView and set its content to assets/help.html.
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_help_view);
 		
 		webView = (WebView) findViewById(R.id.helpView);
 
 		webView.loadUrl("file:///android_asset/html/help.html");
 	}
 }
