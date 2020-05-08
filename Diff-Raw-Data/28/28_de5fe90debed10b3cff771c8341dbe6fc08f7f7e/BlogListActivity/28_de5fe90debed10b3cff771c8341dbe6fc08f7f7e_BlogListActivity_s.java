 package com.tw.thoughtblogs;
 
 import android.app.ListActivity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.tw.thoughtblogs.model.Blog;
 import com.tw.thoughtblogs.model.BlogData;
 import com.tw.thoughtblogs.services.ThoughtBlogService;
 import com.tw.thoughtblogs.util.Constants;
 
 import java.util.List;
 
 public class BlogListActivity extends ListActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         startFeedContentService();
         setListContent();
         handleIntent(getIntent());
         IntentFilter filter = new IntentFilter();
         filter.addAction(Constants.REFRESH_INTENT);
         BroadcastReceiver receiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 if (Constants.REFRESH_INTENT.equals(intent.getAction())) {
                     setListContent();
                 }
             }
         };
 
         this.registerReceiver(receiver, filter);
     }
 
     private void handleIntent(Intent intent) {
         if (Constants.REFRESH_INTENT.equals(intent.getAction())) {
             setListContent();
         }
     }
 
     private void setListContent() {
         BlogData blogData = new BlogData(context());
         List<Blog> blogs = blogData.list();
         blogData.close();
         this.setListAdapter(new BlogAdapter(context(), R.layout.list_item, blogs));
     }
 
     private void startFeedContentService() {
         Intent intent = new Intent(context(), ThoughtBlogService.class);
         startService(intent);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         TextView blogIdTextView = (TextView) v.findViewById(R.id.blog_id);
         String blogId = blogIdTextView.getText().toString();
        Log.v("BlogListActivity", "Marking Read - ID=" + blogId);
         BlogData blogData = new BlogData(context());
         blogData.markRead(blogId);
         blogData.close();
         Intent showContent = new Intent(context(), BlogDetailActivity.class);
         showContent.setData(Uri.parse(blogId));
         startActivity(showContent);
     }
 
     private Context context() {
         return this.getApplicationContext();
     }
 }
