 package duyhung.news;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import duyhung.news.model.Variables;
 
 public class NewsReadActivity extends Activity {
 
 	private WebView newsWebView;
 	private ProgressDialog progressDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_news_read);
 
 		String newsLink = getIntent().getExtras().getString(Variables.LINK);
 		
 		newsWebView = (WebView) findViewById(R.id.newsWebView);
 //		newsWebView.getSettings().setSupportZoom(true);
 //		newsWebView.setInitialScale(1);
 //		newsWebView.getSettings().setLoadWithOverviewMode(true);
 //		newsWebView.getSettings().setUseWideViewPort(true);
 //		newsWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
 //		newsWebView.setScrollbarFadingEnabled(false);
 		
 		progressDialog = ProgressDialog.show(this, "", "Loading news ... ");
 		
 		newsWebView.setWebViewClient(new NewsWebViewClient());
 		newsWebView.loadUrl(newsLink);
//		new RetrieveNewsDetails().execute(newsLink);
 	}
 
 	private class NewsWebViewClient extends WebViewClient{
 		@Override
 		public void onPageFinished(WebView view, String url) {
 			if(progressDialog != null){
 				progressDialog.dismiss();
 			}
 			super.onPageFinished(view, url);
 		}
 	}
 	
 }
