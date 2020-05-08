 package watsons.wine;
  
 import android.app.Activity;
import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
  
 public class WineProductWeb extends Activity implements OnTouchListener, Handler.Callback {
  
 	private static String product_url = "http://watsonwine.bull-b.com/CodeIgniter_2.1.3/index.php/api/product_info/";
 	private WebView webView;
  
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.product_webview);
 		
 		// Receive Parameter
         Bundle bundle = this.getIntent().getExtras();
         String id = bundle.getString("id");
         //Toast.makeText(ProductWebView.this, id, Toast.LENGTH_SHORT).show();
         String url = product_url + id;
  
 		webView = (WebView) findViewById(R.id.product_webview);
 		webView.loadUrl(url);
 		webView.setOnTouchListener(this);
 		WebSettings websetting = webView.getSettings();
 		websetting.setJavaScriptEnabled(true);
 	}
 
 	@Override
 	public boolean handleMessage(Message msg) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
