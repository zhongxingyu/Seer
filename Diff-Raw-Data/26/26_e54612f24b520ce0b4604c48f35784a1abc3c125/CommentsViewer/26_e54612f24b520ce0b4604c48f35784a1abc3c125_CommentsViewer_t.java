 package dk.whooper.mobilsiden.screens;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.Scanner;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.nodes.Comment;
 
 import dk.whooper.mobilsiden.R;
 import dk.whooper.mobilsiden.R.layout;
 import dk.whooper.mobilsiden.R.menu;
 import dk.whooper.mobilsiden.service.ArticleScraper;
 import dk.whooper.mobilsiden.service.CommentsScraper;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebSettings.LayoutAlgorithm;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.support.v4.app.NavUtils;
 
 public class CommentsViewer extends Activity{
 
 	private WebView webView;
 	private Activity activity = this;
 	private static final String TAG = "WebViewer";
 	private String link;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		getWindow().requestFeature(Window.FEATURE_PROGRESS);
 		setContentView(R.layout.activity_comments_viewer);
 
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_comments_viewer);
 
 
 		link = getIntent().getExtras().getString("link");
 		webView = (WebView) findViewById(R.id.webView1);
 
 		webView.getSettings().setJavaScriptEnabled(true);
 
 		webView.setWebChromeClient(new WebChromeClient() {
 			public void onProgressChanged(WebView view, int progress) {
 				activity.setProgress(progress * 100);
 			}
 		});
 
 		webView.setWebViewClient(new Callback());
 		CommentsScraper commentsScraper = new CommentsScraper();
 		try {
 			webView.setHorizontalScrollBarEnabled(false);
 			webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
			String start = "<html><head><meta http-equiv='Content-Type' content='text/html' charset='UTF-8' /></head><body>";
			String end = "</body></html>";
			webView.loadData(start+ commentsScraper.execute(link).get() + end,"text/html; charset=UTF-8", null);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_web_viewer, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		case R.id.menu_settings:
 			Intent i = new Intent(this, SettingsActivity.class);
 			startActivity(i);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private class Callback extends WebViewClient{ 
 
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView  view, String  url){
 			Log.d(TAG, "URL IS: "+url);
 			if( url.contains("mobilsiden.dk") ){
 				Log.d(TAG,"Mobilsiden link found");
 				Intent i = new Intent(activity, ArticleViewer.class);
 				i.putExtra("link", url);
 				activity.startActivity(i);
 				return true;
 			}else{
 				Uri uriUrl = Uri.parse(url);
 				Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);  
 				startActivity(launchBrowser);  
 				return true;
 			}
 		}
 	}
 }
