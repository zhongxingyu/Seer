 package com.f2prateek.samples;
 
 import android.app.ListActivity;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 /**
  * An example to observe how ListView recycling works.
  */
 public class RecyclingViewListActivity extends ListActivity {
 
     private static final int SAMPLE_SIZE = 50;
    private static String TAG = "RecyclingViewListActivity";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setListAdapter(new SimpleTextAdapter(SAMPLE_SIZE));
     }
 
     private class SimpleTextAdapter extends BaseAdapter {
         private final int count;
 
         private SimpleTextAdapter(int count) {
             this.count = count;
         }
 
         public int getCount() {
             return count;
         }
 
         public Object getItem(int position) {
             return position;
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(int position, View convertView, ViewGroup parent) {
             TextView textView;
             String text;
             Drawable drawable;
             // First check if the convertView is null
             if (convertView == null) {
                 // If it is null, inflate a new view.
                 textView = new TextView(RecyclingViewListActivity.this);
                 // For the sample, we're also setting some resources that are displayed based on the recycled state.
                 text = getResources().getString(R.string.unrecycled_text, position);
                 drawable = getResources().getDrawable(R.drawable.ic_action_unrecycled);
             } else {
                 // If it is not null, we have a view that is recycled.
                 // Get your widgets by
                 // a. Casting (if it is the root view)
                 // b. findViewById
                 // c. ViewHolder pattern
                 textView = (TextView) convertView;
                 text = getResources().getString(R.string.recycled_text, position);
                 drawable = getResources().getDrawable(R.drawable.ic_action_recycled);
             }
             textView.setText(text);
             textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
             return textView;
         }
     }
 
 }
 
