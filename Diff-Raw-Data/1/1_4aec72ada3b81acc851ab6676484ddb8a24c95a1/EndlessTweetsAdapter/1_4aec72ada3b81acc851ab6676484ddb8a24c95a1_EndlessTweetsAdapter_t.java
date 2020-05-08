 package com.eldridge.twitsync.adapter;
 
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 import com.commonsware.cwac.endless.EndlessAdapter;
 import com.eldridge.twitsync.R;
 import com.eldridge.twitsync.controller.TwitterApiController;
 
 import java.util.List;
 
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.TwitterException;
 
 /**
  * Created by ryaneldridge on 8/5/13.
  */
 public class EndlessTweetsAdapter extends EndlessAdapter {
 
     private Context context;
     private static LayoutInflater layoutInflater;
     private ResponseList<Status> items;
 
     public EndlessTweetsAdapter(ArrayAdapter<Status> wrapped, Context context) {
         super(wrapped);
         this.context = context;
         layoutInflater = LayoutInflater.from(context);
     }
 
     @Override
     protected View getPendingView(ViewGroup parent) {
         View pendingView = layoutInflater.inflate(R.layout.tweet_loading_item, null, false);
         return pendingView;
     }
 
     @Override
     protected boolean cacheInBackground() throws Exception {
         ArrayAdapter<Status> adapter = (ArrayAdapter<Status>) getWrappedAdapter();
         Status status = adapter.getItem(adapter.getCount() - 1);
         try {
             items = TwitterApiController.getInstance(context).syncGetUserTimeLineHistory(status.getId());
             if (items != null && !items.isEmpty()) {
                 return true;
             } else {
                 return false;
             }
         } catch (TwitterException te) {
             Log.e(EndlessTweetsAdapter.class.getSimpleName(), "", te);
             return false;
         }
     }
 
     @Override
     protected void appendCachedData() {
         ArrayAdapter<Status> tweets = (ArrayAdapter<Status>) getWrappedAdapter();
         if (items != null && !items.isEmpty()) {
             tweets.addAll(items);
         }
         ((ArrayAdapter<Status>)getWrappedAdapter()).notifyDataSetChanged();
     }
 
 }
