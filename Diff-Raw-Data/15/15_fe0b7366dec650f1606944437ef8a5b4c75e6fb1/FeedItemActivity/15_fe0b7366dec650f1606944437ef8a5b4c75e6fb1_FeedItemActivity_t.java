 package de.feedo.android;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.support.v7.app.ActionBarActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.webkit.WebView;
 
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 import org.json.JSONObject;
 
 import butterknife.InjectView;
 import butterknife.Views;
 import de.feedo.android.model.FeedItem;
 import de.feedo.android.net.FeedoApiHelper;
 
 public class FeedItemActivity extends ActionBarActivity {
     public static final String EXTRAS_KEY_FEED_ITEM_ID = "feed_item_id";
 
     @InjectView(R.id.feed_item_detail_webview)
     WebView mWebView;
 
     private FeedItem mFeedItem;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_feed_item);
 
         Views.inject(this);
 
         mFeedItem = FeedItem.load(FeedItem.class, getIntent().getLongExtra(EXTRAS_KEY_FEED_ITEM_ID, 0));
 
         // Show the Up button in the action bar.
         setupActionBar();
         setupUI();
     }
 
     private void setupUI() {
         String content = mFeedItem.content;
         if(content.isEmpty())
             content = mFeedItem.summary;
        String html = "<html>" +
                "<head>" +
                "<style>img{\n" +
                "  width: auto;\n" +
                "  max-width: 100%;\n" +
                "  height: auto;\n" +
                "  max-height: 100%;\n" +
                "}</style>" +
                "</head>" +
                "<body>"
                + content + "</body>" +
                "</html>";
         String mime = "text/html";
         String encoding = "utf-8";
 
         mWebView.getSettings().setJavaScriptEnabled(true);
         mWebView.loadDataWithBaseURL(mFeedItem.link, html, mime, encoding, null);
 
         mFeedItem.read = true;
         mFeedItem.save();
         FeedoApiHelper.setFeedItemRead(mFeedItem, new JsonHttpResponseHandler(){
             @Override
             public void onFailure(Throwable e, JSONObject errorResponse) {
                 Log.i("feedo", errorResponse.toString());
 
             }
         });
 
         this.setTitle(mFeedItem.title);
     }
 
     private void setupActionBar() {
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.feed_item, menu);
         return true;
     }
     
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
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
         }
         return super.onOptionsItemSelected(item);
     }
 
 }
