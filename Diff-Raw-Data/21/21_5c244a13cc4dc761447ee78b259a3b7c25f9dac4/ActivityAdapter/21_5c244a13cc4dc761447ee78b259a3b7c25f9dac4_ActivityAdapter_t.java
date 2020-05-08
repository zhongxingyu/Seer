 package org.macno.puma.adapter;
 
 import static org.macno.puma.PumaApplication.APP_NAME;
 import static org.macno.puma.PumaApplication.K_MAX_CACHED_ITEMS;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.net.ssl.SSLException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.macno.puma.R;
 import org.macno.puma.activity.ViewActivity;
 import org.macno.puma.core.Account;
 import org.macno.puma.manager.ActivityManager;
 import org.macno.puma.provider.Pumpio;
 import org.macno.puma.util.ActivityUtil;
 
import android.content.SharedPreferences;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class ActivityAdapter extends ArrayAdapter<JSONObject> implements ListView.OnScrollListener {
 
	private static final String K_FEED_SETTINGS = "feedsSettings";
	private static final String K_FEED_LAST_CHECK = "feedsLastcheck";
	
 	private String mFeed;
 	private String mFeedHash;
 	private FragmentActivity mContext;
 	private Account mAccount;
 	private StreamHandler mHandler = new StreamHandler(this);
 
 	private JSONArray mItems;
 	private boolean mHasPrevious;
 	
 	private Pumpio mPumpio;
 	ActivityManager mActivityManager;
 	private boolean mLoading = false;
	private SharedPreferences mSettings;
 	
 	public ActivityAdapter(FragmentActivity context, Account account, String feed) {
 		super(context,0,new ArrayList<JSONObject>());
 		mContext = context;
 		mAccount = account;
 		mPumpio = new Pumpio(mContext);
 		mPumpio.setAccount(mAccount);
 		mFeed = feed;
 		mFeedHash = mPumpio.getStreamHash(mFeed);
 		mActivityManager = new ActivityManager(mContext);
 		loadCache();
		mSettings = context.getSharedPreferences(K_FEED_SETTINGS, 0);
 		if(getCount()==0) {
 			loadStreams();
 		} else {
			try {				
				Date published = new Date(mSettings.getLong(K_FEED_LAST_CHECK, 0));
 				Date now = new Date();
 				if ( (now.getTime() - published.getTime()) >  ( 60 * 60 *1000) ) {
 					clearCache();
 				} else {
 					loadStreamsForNewer();
 				}
 			} catch (Exception e) {
 				Log.e(APP_NAME,e.getMessage());
 				loadStreamsForNewer();
 			}
 			
 		}
 	}
 	
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent) {
 		
 		JSONObject act  = getItem(position);
 		LinearLayout view = ActivityUtil.getViewActivity(mPumpio, act);
 		view.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				JSONObject act = getItem(position);
 				openViewActivity(act);
 			}
 		});
 
 		return view;
 	}
 		
 	private void openViewActivity(JSONObject act) {
 		ViewActivity.startActivity(mContext, mAccount, act);
 	}
 	
 	public void checkNewActivities() {
 		loadStreamsForNewer();
 	}
 	
 	public void clearCache() {
 		mActivityManager.deleteStream(mFeedHash);
 		clear();
 		notifyDataSetChanged();
 		loadStreams();
 	}
 	
 	private void loadCache() {
 		JSONObject o = mActivityManager.getStream(mFeedHash);
 		if(o == null) {
 			return;
 		}
 		JSONArray items = o.optJSONArray("items");
 		for(int i=0;i<items.length();i++) {
 			
 			try {
 				JSONObject act = items.getJSONObject(i);
 				if(act.has("object")) {
 					add(act);
 				}
 			} catch(JSONException e) {
 				Log.e(APP_NAME, e.getMessage(),e);
 			}
 		}
 		Log.d(APP_NAME,"Loaded cache " + mFeedHash + " : " + items.length() + " items ");
 	}
 	
 	private void loadStreams() {
 		if(mLoading) {
 			return;
 		}
 		loadCache();
 		mContext.setProgressBarIndeterminateVisibility(true);
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				mLoading=true;
 				try {
 					JSONObject stream = mPumpio.fetchStream(mFeed, null, null, 20);
 					if(stream == null) {
 						mHandler.sendLoadStramFailed();
 						return;
 					}
 					JSONArray items = stream.optJSONArray("items");
 					mItems = items;
 
 					mHandler.sendLoadComplete();
 				} catch(SSLException e) {
 					//
 				}
 			}
 		};
 		new Thread(runnable).start();
 	}
 	
 	private void loadStreamsForNewer() {
 		if(mLoading) {
 			return;
 		}
 		mContext.setProgressBarIndeterminateVisibility(true);
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				mLoading=true;
 				
 				JSONObject last = getItem(0);
 				Log.d(APP_NAME,"Asking for newer than "  + last.optString("id"));
 				try {
 					JSONObject stream = mPumpio.fetchStream(mFeed, last.optString("id"), null, 20);
 					mItems = stream.optJSONArray("items");
 
 					if (stream.optString("prev",null) != null) {
 						mHasPrevious = true;
 					}
 					mHandler.sendReloadedNewer();
 				} catch(SSLException e) {
 					//
 				}
 			}
 		};
 		new Thread(runnable).start();
 	}
 	
 	private void loadStreamsForOlder() {
 		if(mLoading) {
 			return;
 		}
 		Log.d(APP_NAME,"loadStreamsForOlder");
 		mContext.setProgressBarIndeterminateVisibility(true);
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				mLoading=true;
 				
 				JSONObject last = getItem(getCount()-1);
 				Log.d(APP_NAME,"Asking for older than "  + last.optString("id"));
 				try {
 					JSONObject stream = mPumpio.fetchStream(mFeed, null, last.optString("id"), 20);
 					if(stream == null) {
 						mHandler.sendLoadStramFailed();
 						return;
 					}
 					mItems = stream.optJSONArray("items");
 
 					mHandler.sendReloadedOlder();
 				} catch(SSLException e) {
 					//
 				}
 			}
 		};
 		new Thread(runnable).start();
 	}
 	
 	private void reloadAdapter(boolean newer) {
 		
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putLong( K_FEED_LAST_CHECK, (new Date()).getTime()).commit();
 		mLoading=false;
 		mContext.setProgressBarIndeterminateVisibility(false);
 		if(mItems != null) {
 			for(int i=0;i<mItems.length();i++) {
 				// Check if activity id is already here.. 
 				
 				try {
 					JSONObject act = mItems.getJSONObject(i);
 					if(act.has("object")) {
 						String activityId = act.getString("id");
 						if(newer) {
 							checkAndDeleteIfExists(activityId);
 							insert(act, i);
 //							Log.d(APP_NAME,"Inserting " + activityId + " in position " + 0);
 						} else {
 							if(!checkIfExists(activityId)) { // I'm moving backward. If act exists, it's newer.. 
 								add(act);
 //								Log.d(APP_NAME,"Appengin " + activityId + " at the end of the list");
 							}
 						}
 					}
 				} catch(JSONException e) {
 					Log.e(APP_NAME, e.getMessage(),e);
 				}
 			}
 			if(mHasPrevious) {
 				loadStreamsForNewer();
 				mHasPrevious = false;
 			}
 			mItems = null;
 			saveCache();
 		}
 		notifyDataSetChanged();
 	}
 	
 	private void saveCache() {
 		
 		JSONObject obj = new JSONObject();
 		JSONArray items = new JSONArray();
 		for (int i=0;i<getCount();i++) {
 			if(i > K_MAX_CACHED_ITEMS) {
 				break;
 			}
 			items.put(getItem(i));
 		}
 		try {
 			Log.d(APP_NAME,"Saving cache " + mFeedHash + " : " + items.length() + " items ");
 			obj.put("items", items);
 			mActivityManager.saveStream(mFeedHash, obj);
 		} catch(JSONException e) {
 			Log.e(APP_NAME,e.getMessage(),e);
 		}
 	}
 
 	private boolean checkIfExists(String activityId) {
 		for(int i=getCount()-1;i>=0;i--) {
 			JSONObject act = getItem(i);
 			String actId = act.optString("id");
 			if(actId.equals(activityId)) {
 				Log.d(APP_NAME, "["+activityId+"] Found the same activity . Reporting it");
 				return true; 
 			}
 		}
 		return false;
 	}
 	
 	private boolean checkAndDeleteIfExists(String activityId) {
 		for(int i=getCount()-1;i>=0;i--) {
 			JSONObject act = getItem(i);
 			String actId = act.optString("id");
 			if(actId.equals(activityId)) {
 				Log.d(APP_NAME, "["+activityId+"] Found the same activity. Deleting it");
 				remove(act);
 				return true; 
 			}
 		}
 		return false;
 	}
 	
 	private void notifyLoadStramFailed() {
 		mContext.setProgressBarIndeterminateVisibility(false);
 		Toast.makeText(mContext, R.string.load_stream_failed, Toast.LENGTH_LONG).show();
 	}
 	
 	private static class StreamHandler extends Handler {
     	
     	private final WeakReference<ActivityAdapter> mTarget;
     	
     	private static final int MSG_POST_OK = 0;
     	private static final int MSG_RELOADED_NEWER = 1;
     	private static final int MSG_RELOADED_OLDER = 2;
     	private static final int MSG_LOAD_STREAM_FAILED = 3;
     	
     	StreamHandler(ActivityAdapter target) {
 			mTarget = new WeakReference<ActivityAdapter>(target);
 		}
     	
     	public void handleMessage(Message msg) {
     		ActivityAdapter target = mTarget.get();
 			
 			switch (msg.what) {
 			
 			case MSG_RELOADED_NEWER:
 				target.reloadAdapter(true);
 				break;
 				
 			case MSG_RELOADED_OLDER:
 				target.reloadAdapter(false);
 				break;
 				
 			case MSG_POST_OK:
 				target.reloadAdapter(false);
 				break;
 				
 			case MSG_LOAD_STREAM_FAILED:
 				target.notifyLoadStramFailed();
 				break;
 			}
 		}
     	
     	void sendLoadComplete() {
     		sendEmptyMessage(MSG_POST_OK);
     	}
     	
     	void sendReloadedNewer() {
     		sendEmptyMessage(MSG_RELOADED_NEWER);
     	}
     	
     	void sendReloadedOlder() {
     		sendEmptyMessage(MSG_RELOADED_OLDER);
     	}
     	
     	void sendLoadStramFailed() {
     		sendEmptyMessage(MSG_LOAD_STREAM_FAILED);
     	}
     }
 
 	private int mFirstVisibleItem;
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		if (firstVisibleItem != mFirstVisibleItem) {
 			if (firstVisibleItem + visibleItemCount >= totalItemCount) {
 				loadStreamsForOlder();
 			}
 		} else {
 			mFirstVisibleItem = firstVisibleItem;
 		}
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		
 		
 	}
 	
 }
