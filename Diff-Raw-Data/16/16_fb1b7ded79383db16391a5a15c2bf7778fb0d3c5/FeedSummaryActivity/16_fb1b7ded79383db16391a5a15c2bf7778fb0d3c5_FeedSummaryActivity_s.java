 package com.janrain.android.quickshare;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.util.Config;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 
 import java.util.ArrayList;
 
 /**
  * Created by IntelliJ IDEA.
  * User: lillialexis
  * Date: 4/22/11
  * Time: 12:55 PM
  * To change this template use File | Settings | File Templates.
  */
 public class FeedSummaryActivity extends ListActivity implements View.OnClickListener, FeedReaderListener {
     private static final String TAG = FeedSummaryActivity.class.getSimpleName();
 
     private ArrayList<Story> mStories;
     private StoryAdapter mAdapter;
     private FeedData mFeedData;
 
     private Button mRefreshBlog;
 
     public FeedSummaryActivity() {
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.feed_summary_listview);
 
         mRefreshBlog = (Button)findViewById(R.id.refresh_blog);
         mRefreshBlog.setOnClickListener(this);
 
         mFeedData = FeedData.getInstance(this);
         mStories = mFeedData.getFeed();
 
         if (mStories == null) {
             mStories = new ArrayList<Story>();
         }
 
         mAdapter = new StoryAdapter(this, R.layout.feed_summary_listview_row, mStories);
         setListAdapter(mAdapter);
     }
 
     public void onResume () {
         super.onResume();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         Log.d(TAG, "onStart");
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int pos, long id) {
         Story story = mAdapter.getItem(pos);
         mFeedData.setCurrentStory(story);
         this.startActivity(new Intent(this, StoryDetailActivity.class));
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return super.onOptionsItemSelected(item);
     }
 
     public void onClick(View view) {
         mRefreshBlog.setText("Loading new articles...");
         mFeedData.asyncLoadJanrainBlog(this);
     }
 
     public void AsyncFeedReadSucceeded() {
         mRefreshBlog.setText("Refresh");
         mAdapter.notifyDataSetChanged();
     }
 
     public void AsyncFeedReadFailed() {
         mRefreshBlog.setText("Refresh");
         mAdapter.notifyDataSetChanged();
     }
 
     private class StoryAdapter extends ArrayAdapter<Story> {
         private int mResourceId;
 
         public StoryAdapter(Context context, int resId, ArrayList<Story> items) {
             super(context, -1, items);
 
             mResourceId = resId;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View v = convertView;
             if (v == null) {
                 LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = li.inflate(mResourceId, null);
                 Log.i(TAG, "[getView] with null converView");
             } else Log.i(TAG, "[getView] with non null convertView");
 
            ImageView icon = (ImageView)v.findViewById(R.id.row_story_icon);
             TextView title = (TextView)v.findViewById(R.id.row_story_title);
             TextView date = (TextView)v.findViewById(R.id.row_story_date);
 
             Story story = getItem(position);
 
             Log.d(TAG, "[getView] for row " + ((Integer) position).toString() + ": " + story.getTitle());
 
 //            Bitmap bd = story.getImage();
 //            int width = bd.getWidth();// .getIntrinsicWidth();
 //            int height = bd.getHeight();// .getIntrinsicHeight();
 //
 //            if (Config.LOGD)
 //                Log.d(TAG, "[getView] image size: " +
 //                        ((Integer)width).toString() + ", " + ((Integer)height).toString());
 //
 //            if (width > 120 && height > 90)
 //                bd = Bitmap.createScaledBitmap(bd, width/3, height/3, true);
 //                mImage = new ScaleDrawable(bd, Gravity.CLIP_HORIZONTAL | Gravity.CLIP_VERTICAL, width/3, height/3);
 //            else
 //                mImage = new ScaleDrawable(bd, Gravity.CLIP_HORIZONTAL | Gravity.CLIP_VERTICAL, width, height);
 
 //            icon.setImageDrawable(story.getImage());
            icon.setImageBitmap(story.getImage());
             title.setText(story.getTitle());
             date.setText(story.getDate());
 
             return v;
         }
     }
 }
