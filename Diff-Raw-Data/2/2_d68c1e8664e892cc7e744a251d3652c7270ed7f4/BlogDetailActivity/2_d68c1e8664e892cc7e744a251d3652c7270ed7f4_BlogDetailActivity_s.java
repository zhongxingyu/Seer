 package com.tw.thoughtblogs;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.WebView;
 import com.tw.thoughtblogs.model.BlogData;
 
 public class BlogDetailActivity extends Activity {
 
     private BlogData blogData;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.blog_detail);
 
         Intent launchingIntent = getIntent();
         String blogID = launchingIntent.getData().toString();
 
         String details = getBlogData().loadDescription(blogID);
         details = details.replaceAll("&lt;", "<");
         details = details.replaceAll("&gt;", ">");
         details = details.replaceAll("#", "%23");
         details = details.replaceAll("%", "%25");
         details = details.replaceAll("\\?", "%27");
        details = "<html><body>" + details + "</body></html>";
         WebView viewer = (WebView) findViewById(R.id.blogDetailView);
         viewer.loadData(details, "text/html", "utf-8");
     }
 
     private BlogData getBlogData() {
         if (blogData == null)
             blogData = new BlogData(this);
         return blogData;
     }
 }
