 package niclas.hedam.cyberhus;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ImageButton;
 
 
 public class Chat extends Activity {
     WebView mWebView;
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.browser);
 	    mWebView = (WebView) findViewById(R.id.webview);
 	    mWebView.getSettings().setJavaScriptEnabled(true);
 	    mWebView.setWebViewClient(new WebViewClient());
	    mWebView.loadUrl("http://chat.cybhus.dk/client.php");
 	    ImageButton closeButton = (ImageButton)this.findViewById(R.id.imageButton1);
 	    closeButton.setOnClickListener(new OnClickListener() {
 	      public void onClick(View v) {
 	        finish();
	        System.exit(0);
 	      }
 	    });
 	}
 }
