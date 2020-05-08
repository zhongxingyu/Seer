 package sk.xeito.android.cpan;
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.os.Bundle;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 
 public class FeedItemActivity extends BaseActivity {
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.feed_item);
 
 		Intent intent = getIntent();
 		Bundle bundle = intent.getExtras();
 		if (bundle == null) {
 			bundle = new Bundle();
 		}
 
 		String url = bundle.getString(B.args.url);
 		if (url == null) {
 			finish();
 			return;
 		}
 
 		WebView webView = (WebView) findViewById(R.id.web_view);
 		WebSettings settings = webView.getSettings();
 		settings.setJavaScriptEnabled(true);
 		Utils.printf("Load URL %s", url);
 		webView.loadUrl(url);
 	}
 }
