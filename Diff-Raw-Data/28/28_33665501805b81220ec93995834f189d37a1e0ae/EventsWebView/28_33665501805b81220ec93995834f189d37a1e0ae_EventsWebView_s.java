 package watsons.wine;
  
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.os.Bundle;
 import android.webkit.WebView;
  
 @SuppressLint("SetJavaScriptEnabled")
 public class EventsWebView extends Activity {
  
 	private static String event_url = "http://watsonwine.bull-b.com/CodeIgniter_2.1.3/index.php/api/event_by_date/";
 	private WebView webView;
  
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.events_web);
 		
 		// Receive Parameter
         Bundle bundle = this.getIntent().getExtras();
         String date = bundle.getString("date");
         String url = event_url + date;
  
 		webView = (WebView) findViewById(R.id.events_webview);
		webView.getSettings().setJavaScriptEnabled(true);
 		webView.loadUrl(url);
 	}
 }
