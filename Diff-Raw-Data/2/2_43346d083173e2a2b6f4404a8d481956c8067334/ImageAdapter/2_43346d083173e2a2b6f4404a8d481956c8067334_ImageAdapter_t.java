 package com.example.browsing;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.maple_android.MapleHttpClient;
 import com.example.maple_android.R;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
 
 /*
  * Extend BaseAdapter to allow grid to show pictures
  */
 public class ImageAdapter extends BaseAdapter {
 	private String TAG = "ImageAdapter";
 	private Context mContext;
 	private String mToken;
     private ArrayList<DisplayAd> mAds;
     private int MAX_TO_SHOW = 10;
     
     public ImageAdapter(Context c, JSONArray ads, String token) throws JSONException {
     	mAds = new ArrayList<DisplayAd>();
     	// Build up ArrayList of DisplayAds from JSON array
     	for (int i = 0; i < ads.length(); i++) {
     		DisplayAd dAd = new DisplayAd(ads.getJSONObject(i));
     		mAds.add(dAd);
     	}
     	mContext = c;
         mToken = token;
     }
 
     public int getCount() {
     	return Math.min(MAX_TO_SHOW, mAds.size());
     }
 
     public Object getItem(int position) {
 		return mAds.get(position);
     }
 
     public long getItemId(int position) {
         return position;
     }
 
     // create a new ImageView for each item referenced by the Adapter
     public View getView(int position, View convertView, ViewGroup parent) {
     	View adView = convertView;
         // Should check convertView == null here, but somehow that screws things up
 //        if (convertView == null) {
     	final DisplayAd dAd = mAds.get(position);
     	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     	adView = inflater.inflate(R.layout.ad_view, null);
     	final ImageView imageView = (ImageView) adView.findViewById(R.id.ad);
         imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
         imageView.setPadding(8, 8, 8, 8);
 
         ImageLoader imageLoader = ImageLoader.getInstance();
         imageLoader.loadImage(dAd.getUrl(), new SimpleImageLoadingListener() {
         	@Override
         	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
         		imageView.setImageBitmap(loadedImage);
         	}
         });
     	TextView titleView = (TextView) adView.findViewById(R.id.adTitle);
     	titleView.setText(dAd.getTitle());
     	titleView.setTextColor(Color.BLACK); 
     	TextView creatorText = (TextView) adView.findViewById(R.id.creatorName);
     	creatorText.setText(dAd.getCreator());
     	final TextView numVotesText = (TextView) adView.findViewById(R.id.numVotes);
     	numVotesText.setText("Votes: " + dAd.getNumVotes());
     	TextView createdText = (TextView) adView.findViewById(R.id.dateCreated);
     	createdText.setText(dAd.getRelativeTime() + " ago");
     	
         final Button voteButton = (Button) adView.findViewById(R.id.voteBtn);
     	addButtonActions(dAd, voteButton, numVotesText);
 
         return adView;
     }
     
     private void addButtonActions(final DisplayAd ad, final Button voteButton, final TextView numVotesText) {
 		final RequestParams params = new RequestParams();
 		// Another way to get the token
 		// Session session = Session.getActiveSession();
 		// params.put("token", session.getAccessToken());
 		params.put("post_id", ad.getImageId());
 		params.put("token", mToken);
 		
         if (ad.getVotedOn()) {
 			disableButton(voteButton);
         } else {
 	        voteButton.setOnClickListener(new OnClickListener() {
 	        	@Override
 	        	public void onClick(View v) {
 	        		MapleHttpClient.post("posts/vote_up", params, new AsyncHttpResponseHandler(){
 	            		@Override
 	        			public void onSuccess(int statusCode, String response) {
 	        				Log.d(TAG, response);
 	        				disableButton(voteButton);
 	        				// Add one more to the numVotes textview
 	        				int numVotesAdded = ad.getNumVotes() + 1;
 	        				numVotesText.setText("Votes: " + numVotesAdded);
 	        				Toast.makeText(mContext, "You voted!", Toast.LENGTH_SHORT).show();
 	        			}
 	        			@Override
 	        		    public void onFailure(Throwable error, String response) {
 	        				Toast.makeText(mContext, "Voting failed", Toast.LENGTH_SHORT).show();
 	        		    }
 	            	});
 	        	}
 	        });
         }
     }
     
     private void disableButton(final Button voteButton) {
		voteButton.setBackgroundResource(R.drawable.thumbs_up_pressed);
 		voteButton.setClickable(false);
     	voteButton.setEnabled(false);
     }
 }
