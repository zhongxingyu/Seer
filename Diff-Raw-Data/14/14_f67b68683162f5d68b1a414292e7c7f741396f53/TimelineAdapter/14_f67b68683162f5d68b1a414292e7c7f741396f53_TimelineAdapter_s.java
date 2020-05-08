 package com.company.tweeter;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 
 import twitter4j.util.TimeSpanConverter;
 import android.app.Activity;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 import com.company.tweeter.ImageDownloader.OnDownloadCompletedListener;
 
 public class TimelineAdapter extends SimpleCursorAdapter implements OnDownloadCompletedListener {
 
 	private static final String DATE_FORMAT_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";
 	private Activity activity;
 	private Cursor data;
 	
 	private CacheManager cacheManager;
 	
 	private ArrayList<String> nowDownloading;
 	
 	public TimelineAdapter(Activity activity, int layout, Cursor c,
 			String[] from, int[] to) {
 		super(activity, layout, c, from, to);
 		// TODO Auto-generated constructor stub
 		this.activity = activity;
 		this.data = c;
 		
 		cacheManager = CacheManager.getInstance();
 		
 		nowDownloading = new ArrayList<String>();
 	}
 	
 	static class ViewHolder {
 		TextView username;
 		TextView time;
 		TextView tweetMessage;
 		TextView retweetedBy;
 		
 		ImageView retweetImageView;
 		
 		ImageView userProfileImageView;
 	}
 	
 	private boolean isDownloading(String username) {
 		synchronized (nowDownloading) {
 			for (String object : nowDownloading) {
 				if(object.compareTo(username) == 0) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 		
		Hashtable<String, String> imageUrlHastable = new Hashtable<String, String>();
		
 		if(convertView == null) {
 			LayoutInflater inflater = activity.getLayoutInflater();
 			convertView = inflater.inflate(R.layout.tweet_row, null);
 			
 			holder = new ViewHolder();
 			
 			holder.username = (TextView) convertView.findViewById(R.id.username);
 			holder.time = (TextView) convertView.findViewById(R.id.time);
 			holder.tweetMessage = (TextView) convertView.findViewById(R.id.tweetMessage);
 			holder.retweetedBy = (TextView) convertView.findViewById(R.id.retweetedBy);
 			holder.userProfileImageView = (ImageView) convertView.findViewById(R.id.userImageView);
 			holder.retweetImageView = (ImageView) convertView.findViewById(R.id.retweetImageView);
 			
 			convertView.setTag(holder);
 		}
 		
 		else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 		
 		if(data.moveToPosition(position)) {
 			String usernameString = data.getString(data.getColumnIndex(Constants.USERNAME));
 			String imageUrl = data.getString(data.getColumnIndex(Constants.PROFILE_IMAGE));
 			
 			if(!isDownloading(usernameString) && cacheManager.getImageForKey(usernameString) == null) {
 				imageUrlHastable.put(usernameString, imageUrl);
 				Log.i(Constants.TAG, "Inside if condition");
 			}
 			
 			holder.username.setText(usernameString);
 			
 			String dateString = data.getString(data.getColumnIndex(Constants.CREATED_TIME));
 			Log.i(Constants.TAG, dateString);
 			
 			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
 			Date dateObject = null;
 			try {
 				dateObject = format.parse(dateString);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			
 			TimeSpanConverter convertor = new TimeSpanConverter();
 			
 			holder.time.setText(convertor.toTimeSpanString(dateObject));
 			
 			holder.tweetMessage.setText(data.getString(data.getColumnIndex(Constants.TWEET)));
 			holder.retweetedBy.setText(data.getString(data.getColumnIndex(Constants.RETWEETED_BY)));
 			
 			if(holder.retweetedBy.getText().length() == 0) {
 //				Log.d(Constants.TAG, "retweetedby is empty");
 				holder.retweetImageView.setVisibility(View.GONE);
 				holder.retweetedBy.setVisibility(View.GONE);
 			}
 			else {
 //				Log.d(Constants.TAG, "retweetedby is NOT empty");
 				holder.retweetImageView.setVisibility(View.VISIBLE);
 				holder.retweetedBy.setVisibility(View.VISIBLE);
 			}
 			
 			String imagePath = cacheManager.getImageForKey(usernameString);
 			
 			if(imagePath == null) {
 				holder.userProfileImageView.setImageResource(R.drawable.ic_launcher);
 				if(ImageDownloader.isNetworkConnected(activity)) {
 					ImageDownloader downloader = new ImageDownloader();
 					downloader.setContext(activity);
 					downloader.setOnDownloadCompletedListener(this);
 					synchronized (nowDownloading) {
 						nowDownloading.add(usernameString);
 					}
 					downloader.execute(imageUrlHastable);
 				}
 				else {
 					Log.d(Constants.TAG, "Offline");
 				}
 			}
 			else {
 				Bitmap bm = getImageBitmapFromPath(imagePath);
 //				Animation fade = AnimationUtils.loadAnimation(activity, R.anim.fade);
 				
 				if(bm != null) {
 					holder.userProfileImageView.setImageBitmap(ImageHelper
 							.getRoundedCornerBitmap(bm));
 				}
 				
 				else {
 					holder.userProfileImageView.setImageResource(R.drawable.ic_launcher);
 				}
 
 //				userProfileImageView.setAnimation(fade);
 			}
 			
 		}
 		
 		return convertView;
 	}
 	
 	@Override
 	public Object getItem(int position) {
 		String statusID = null;
 		
 		if(data.moveToPosition(position)) {
 			statusID = data.getString(data.getColumnIndex(Constants.STATUS_ID));
 		}
 		
 		return statusID;
 	}
 	
 	private Bitmap getImageBitmapFromPath(String imagePath) {
 		return BitmapFactory.decodeFile(imagePath);
 	}
 
 	public void onDownloadCompleted(String username) {
 		Log.d(Constants.TAG, "Download of image for " + username + " completed");
 		Log.d(Constants.TAG, "###########");
 		if(activity instanceof TimelineActivity) {
 			if(!((TimelineActivity) activity).isScrolling()) {
 				notifyDataSetChanged();
 			}
 		}
 		synchronized (nowDownloading) {
 			nowDownloading.remove(username);
 		}
 	}
 }
