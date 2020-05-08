 package com.codepath.apps.restclienttemplate;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.codepath.apps.restclienttemplate.models.Tweet;
 import com.loopj.android.image.SmartImageView;
 
 public class TweetsAdapter extends ArrayAdapter<Tweet> {
 
 	public TweetsAdapter(Context context, List<Tweet> objects) {
 		super(context, 0, objects);
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View tweetView = convertView;
 		
 		if(tweetView == null) {
 			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			tweetView = inflater.inflate(R.layout.tweet_item, null);
 		}
 		
 		Tweet tweet_item = getItem(position);
 		
 		SmartImageView profileImage = (SmartImageView) tweetView.findViewById(R.id.profile_picture);
 		TextView userName = (TextView) tweetView.findViewById(R.id.user_name);
 		TextView tweetBody = (TextView) tweetView.findViewById(R.id.body_tweet);
 		
 		//Populate the view with the info in tweet
 		profileImage.setImageUrl(tweet_item.getUser().getProfileImageURL());
 		userName.setText(tweet_item.getUser().getSreenName());
 		tweetBody.setText(tweet_item.getBody());
 		
 		return tweetView;
 	}
 	
 }
