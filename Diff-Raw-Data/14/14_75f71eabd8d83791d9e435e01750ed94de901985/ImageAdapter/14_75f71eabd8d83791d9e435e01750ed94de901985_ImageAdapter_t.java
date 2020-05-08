 package com.additt.browsing;
 
import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.support.v4.util.LruCache;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.additt.maple_android.MapleHttpClient;
 import com.additt.maple_android.R;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
 
 /**
  * Extend BaseAdapter to allow grid to show pictures and other attributes of ad
  */
 public class ImageAdapter extends BaseAdapter {
 	private LruCache<String, Bitmap> mMemoryCache;
 	private Context mContext;
 	private String mToken;
     private ArrayList<DisplayAd> mAds;
     private int MAX_TO_SHOW = 20;
     private ImageLoader mImageLoader;
     
     public ImageAdapter(Context c, JSONArray ads, String token) throws JSONException {
     	mAds = new ArrayList<DisplayAd>();
     	// Build up ArrayList of DisplayAds from JSON array
     	for (int i = 0; i < ads.length(); i++) {
     		DisplayAd dAd = new DisplayAd(ads.getJSONObject(i));
     		mAds.add(dAd);
     	}
     	mContext = c;
         mToken = token;
         mImageLoader = ImageLoader.getInstance();
         // Get max available VM memory, exceeding this amount will throw an
         // OutOfMemory exception. Stored in kilobytes as LruCache takes an
         // int in its constructor.
         final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
 
         // Use 1/8th of the available memory for this memory cache.
         final int cacheSize = maxMemory / 8;
 
         mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
             @Override
             protected int sizeOf(String key, Bitmap bitmap) {
                 // The cache size will be measured in kilobytes rather than
                 // number of items.
    			
    			int nBytes = 0;
    			try {
    				nBytes = bitmap.getByteCount() / 1024;
    			} catch (NoSuchMethodError e) {
    				ByteArrayOutputStream stream = new ByteArrayOutputStream();
    			    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    			    byte[] imageInByte = stream.toByteArray();
        			nBytes = imageInByte.length;
    			}
                return nBytes;
             }
         };
     }
     
     /**
      * Add bitmap with key (in our case, the url) to the cache
      */
     public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
         if (getBitmapFromMemCache(key) == null) {
             mMemoryCache.put(key, bitmap);
         }
     }
 
     /**
      * Get bitmap from cache based on key
      */
     public Bitmap getBitmapFromMemCache(String key) {
         return mMemoryCache.get(key);
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
     
     /**
      * Cache set up as descibed in:
      * As described in http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
      */
     public void loadBitmap(final String url, final ImageView imageView) {
         final Bitmap bitmap = getBitmapFromMemCache(url);
         if (bitmap != null) {
             imageView.setImageBitmap(bitmap);
         } else {
         	mImageLoader.loadImage(url, new SimpleImageLoadingListener() {
             	@Override
             	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
             		addBitmapToMemoryCache(url, loadedImage);
             		imageView.setImageBitmap(loadedImage);
             	}
             });
         }
     }
     
     // 
 
     /**
      * Set up fields in view based on data received for each item 
      * referenced by the Adapter
      */
     public View getView(int position, View convertView, ViewGroup parent) {
     	View adView = convertView;
     	final DisplayAd dAd = mAds.get(position);
     	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     	adView = inflater.inflate(R.layout.ad_view, null);
     	final ImageView imageView = (ImageView) adView.findViewById(R.id.ad);
         imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
         imageView.setPadding(8, 8, 8, 8);
         loadBitmap(dAd.getUrl(), imageView);
         
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
 		params.put("auth_token", mToken);
 		
         if (ad.getVotedOn()) {
 			disableButton(voteButton);
         } else {
 	        voteButton.setOnClickListener(new OnClickListener() {
 	        	@Override
 	        	public void onClick(View v) {
 	        		MapleHttpClient.post("posts/vote_up", params, new AsyncHttpResponseHandler(){
 	            		@Override
 	        			public void onSuccess(int statusCode, String response) {
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
