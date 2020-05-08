 package com.salquestfl.hnewsreader;
 
 import java.util.List;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.Intent;
 import android.content.Context;
 import android.app.Activity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.LayoutInflater;
 import android.widget.TextView;
 import android.widget.ImageView;
 import android.view.View.OnClickListener;
 import android.widget.BaseAdapter;
 import android.util.Log;
 import android.net.Uri;
 
 
 // Custom list adapter - it adds an click event handler for each 'arrow' image to open the comments URL
 public class ClickableButtonListAdapter extends BaseAdapter { 
 
     private static class ViewHolder {
         TextView titleText;
         TextView urlText;
         ImageView commentsImage;
     }
     private final Context context;
     private final ArrayList<HashMap<String, String>> articles;
     private final LayoutInflater inflater;
     private final OnClickListener listener;
 
     public ClickableButtonListAdapter(final Context context, ArrayList<HashMap<String, String>> articles) {
         this.context = context;
         this.articles = articles;
         inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
         listener = new OnClickListener() {
             @Override
             public void onClick(View view) {
                 String url = (String)view.getTag();
                 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                 context.startActivity(intent);
             }
         };
     }
 
     @Override
     public int getCount() {
         return articles.size();
     }
 
     @Override
     public HashMap<String, String> getItem(int position) {
         return articles.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public View getView(final int position, View convertView, ViewGroup parent) {
         ViewHolder holder = null;
         if (convertView == null) {
             convertView = inflater.inflate(R.layout.article, null);
             holder = new ViewHolder();
             holder.titleText = (TextView) convertView.findViewById(R.id.title);
             holder.urlText = (TextView) convertView.findViewById(R.id.url);
             holder.commentsImage = (ImageView) convertView.findViewById(R.id.arrow);
             holder.commentsImage.setOnClickListener(listener);
             convertView.setTag(holder);
         } else {
             holder = (ViewHolder) convertView.getTag();
         }
         final HashMap<String, String> article = (HashMap<String, String>) getItem(position);
         String title = article.get("title");
         holder.titleText.setText(title);
        String url = article.get("link");
         holder.urlText.setText(url);
         String commentsUrl = article.get("comments");
         holder.commentsImage.setTag(commentsUrl);
         return convertView;
     }
 }
 
