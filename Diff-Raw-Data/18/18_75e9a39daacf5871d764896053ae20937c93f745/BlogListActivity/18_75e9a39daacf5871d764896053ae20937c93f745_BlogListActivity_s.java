 package com.tw.thoughtblogs;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.tw.thoughtblogs.model.Blog;
 import com.tw.thoughtblogs.model.BlogData;
 import com.tw.thoughtblogs.services.ThoughtBlogService;
 
 import java.util.Date;
 import java.util.List;
 
 import static com.tw.thoughtblogs.util.Constants.FEED_URL;
 
 public class BlogListActivity extends ListActivity {
     private ProgressDialog progressDialog;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         loadBlogs();
         startFeedContentService();
     }
 
     @Override
     protected void onRestart() {
         super.onRestart();
        List<Blog> blogs = fetch();
         this.setListAdapter(new BlogAdapter(context(), R.layout.list_item, blogs));
     }
 
     @Override
     public void onBackPressed() {
         super.onBackPressed();
         this.finish();
     }
 
     private void loadBlogs() {
         initProgressDialog();
         new BlogDownloadTask().execute("");
     }
 
     private void setListContent(List<Blog> blogs) {
         dismissProgressDialog();
         this.setListAdapter(new BlogAdapter(context(), R.layout.list_item, blogs));
     }
 
     private void initProgressDialog() {
         progressDialog = ProgressDialog.show(BlogListActivity.this, "Downloading ... ", "Fetching Latest Entries", true, true);
     }
 
     private void dismissProgressDialog() {
         if (progressDialog != null && progressDialog.isShowing())
             progressDialog.dismiss();
     }
 
     private void startFeedContentService() {
         Intent intent = new Intent(context(), ThoughtBlogService.class);
         startService(intent);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         TextView blogIdTextView = (TextView) v.findViewById(R.id.blog_id);
         String blogId = blogIdTextView.getText().toString();
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
 
    private List<Blog> fetch() {
         BlogData blogData = new BlogData(context());
         List<Blog> blogs = blogData.list();
         blogData.close();
         return blogs;
     }
 
     private class BlogDownloadTask extends AsyncTask<String, Void, List<Blog>> {
 
         protected List<Blog> doInBackground(String... args) {
 
             BlogData blogData = new BlogData(context());
             Date lastParsedDate = blogData.lastParsedDate();
             blogData.close();
 
             List<Blog> blogs = new RSSReader(FEED_URL).fetchLatestEntries(lastParsedDate);
             blogData = new BlogData(context());
             blogData.store(blogs);
             blogData.close();
 
             blogData = new BlogData(context());
             blogs = blogData.list();
             blogData.close();
 
             return blogs;
         }
 
         protected void onPostExecute(List<Blog> blogs) {
             BlogListActivity.this.setListContent(blogs);
         }
     }
 
 }
