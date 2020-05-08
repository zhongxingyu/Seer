 package com.focusings.focusingsworld.TwitterParser;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.List;
 
 import com.focusings.focusingsworld.ImageAndTextList.AsyncImageLoader;
 import com.focusings.focusingsworld.ImageAndTextList.AsyncImageLoader.ImageCallback;
 import com.focusings.focusingsworld.R;
 
 import android.app.Activity;
 import android.graphics.drawable.Drawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class TweetsListAdapter extends ArrayAdapter<TweetInfo>{
 
 	private AsyncImageLoader asyncImageLoader;
 	
 	
     public TweetsListAdapter(Activity activity, List<TweetInfo> imageAndTexts) {
         super(activity, 0, imageAndTexts);
         asyncImageLoader = new AsyncImageLoader();
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         Activity activity = (Activity) getContext();
         LayoutInflater inflater = activity.getLayoutInflater();
 
         // Inflate the views from XML
         View rowView = inflater.inflate(R.layout.tweet_row, null);
         TweetInfo tweet = getItem(position);
 
         // Load the image and set it on the ImageView
         final ImageView userImageView = (ImageView) rowView.findViewById(R.id.userImage);
         Drawable cachedImage = asyncImageLoader.loadDrawable(tweet.getUserImage(), new ImageCallback() {
             public void imageLoaded(Drawable imageDrawable, String imageUrl) {
             	userImageView.setImageDrawable(imageDrawable);
             }
         });
         userImageView.setImageDrawable(cachedImage);
         
         TextView userNameView = (TextView) rowView.findViewById(R.id.userName);
         userNameView.setText(tweet.getUserName());
         
         TextView userTwitterAccountView = (TextView) rowView.findViewById(R.id.userTwitterAccount);
         userTwitterAccountView.setText(tweet.getUserTwitterAccount());
         
         TextView tweetContentView = (TextView) rowView.findViewById(R.id.tweetContent);
         tweetContentView.setText(tweet.getTweetContent());
         
         return rowView;
     }
 
     public static Drawable loadImageFromUrl(String url) {
         InputStream inputStream;
         try {
             inputStream = new URL(url).openStream();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return Drawable.createFromStream(inputStream, "src");
     }
   
 }
