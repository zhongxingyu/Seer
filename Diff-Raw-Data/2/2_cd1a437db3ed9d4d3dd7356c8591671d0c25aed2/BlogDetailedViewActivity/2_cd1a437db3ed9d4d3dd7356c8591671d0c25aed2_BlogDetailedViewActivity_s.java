 package com.janrain.android.quickshare;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Config;
 import android.util.Log;
 import android.view.View;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * Created by IntelliJ IDEA.
  * User: lillialexis
  * Date: 4/22/11
  * Time: 3:16 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BlogDetailedViewActivity extends Activity implements View.OnClickListener {
 
     private static final String TAG = BlogSummaryListActivity.class.getSimpleName();
 
     private BlogData mBlogData;
     private BlogArticle mBlogArticle;
 
     private Button mShareBlog;
 //    private TextView mTitle;
 //    private TextView mDate;
 //    private TextView mText;
 
     private WebView mWebview;
 
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.blog_detailed_view_layout);
 
         mBlogData = BlogData.getInstance(this);
 
         mShareBlog = (Button)findViewById(R.id.share_button);
         mShareBlog.setOnClickListener(this);
 
         mWebview = (WebView)findViewById(R.id.webview_1);
         mWebview.setWebViewClient(mWebviewClient);
 
 //        mTitle = (TextView)findViewById(R.id.title);
 //        mDate = (TextView)findViewById(R.id.date);
 //        mText = (TextView)findViewById(R.id.text);
 
         loadCurrentBlog();
     }
 
     public void loadCurrentBlog() {
         mBlogArticle = mBlogData.getCurrentBlogArticle();
 
         mWebview.loadDataWithBaseURL("http://www.janrain.com/blogs/",
                                             "<html><body>" +
                                             "<h1>" + mBlogArticle.getTitle() + "</h1><br />" +
                                             "<h2>" + mBlogArticle.getDate() + "</h2><br />" +
                                             "<div class='body'>" + mBlogArticle.getDescription() + "</div>" +
                                             "</body></html>",
                                             "text/html", "UTF-8", "");
 
 //        mTitle.setText(mBlogArticle.getTitle());
 //        mDate.setText(mBlogArticle.getDate());
 //        mText.setText(mBlogArticle.getDescription());
     }
 
     public void onClick(View view) {
         mBlogData.shareCurrentBlogArticle();
     }
 
     private void openNewWebViewToUrl(String url) {
         mBlogData.setUrlToBeLoaded(url);
         this.startActivity(new Intent(this, BlogWebViewActivity.class));
     }
 
     private WebViewClient mWebviewClient = new WebViewClient(){
         private final String TAG = this.getClass().getSimpleName();
 
         @Override
         public boolean shouldOverrideUrlLoading(WebView webView, String url) {
             if (Config.LOGD) {
                 Log.d(TAG, "[shouldOverrideUrlLoading] url: " + url);
             }
 
             openNewWebViewToUrl(url);
            return false;
         }
 
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
             if (Config.LOGD) {
                 Log.d(TAG, "[onPageStarted] url: " + url);
             }
 
             super.onPageStarted(view, url, favicon);
         }
 
         @Override
         public void onPageFinished(WebView view, String url) {
             if (Config.LOGD) {
                 Log.d(TAG, "[onPageFinished] url: " + url);
             }
 
             super.onPageFinished(view, url);
         }
 
         @Override
         public void onReceivedError(WebView view, int errorCode, String description, String url) {
             super.onReceivedError(view, errorCode, description, url);
             Log.e(TAG, "[onReceivedError] code: " + errorCode + " | description: " + description
                 + " | url: " + url);
         }
     };
 
 }
