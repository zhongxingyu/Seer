 package com.codepath.apps.restclienttemplate;
 
 import android.content.Context;
 import android.text.Html;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.codepath.apps.restclienttemplate.models.Tweet;
 import com.codepath.apps.restclienttemplate.models.User;
 import com.nostra13.universalimageloader.core.ImageLoader;
 
 import java.util.List;
 
 /**
  * Created by rhu on 10/19/13.
  */
 public class TweetAdapter extends ArrayAdapter<Tweet> {
 
     public TweetAdapter(Context context, List<Tweet> tweets) {
         super(context, R.layout.item_tweet, tweets);
     }
 
     public View getView(int position, View convertView, ViewGroup parent) {
         if (convertView == null) {
             convertView = View.inflate(getContext(), R.layout.item_tweet, null);
         }
 
         Tweet tweet = getItem(position);
 
         ImageView imageView = (ImageView) convertView.findViewById(R.id.userAvatar);
 
         User user = tweet.getUser();
 
         ImageLoader.getInstance().displayImage(user.getProfileImageUrl(), imageView);
 
         TextView fullName = (TextView) convertView.findViewById(R.id.userName);
         String formattedName = "<b>" + user.getFullName() + "</b>" + " <small><font color='#7777777'>@" +
                    user.getTwitterHandle() + "</font></small>";
 
         fullName.setText(Html.fromHtml(formattedName));
 
         TextView bodyView = (TextView) convertView.findViewById(R.id.tweetText);
        bodyView.setText(tweet.getTweet());
 
         return convertView;
     }
 }
