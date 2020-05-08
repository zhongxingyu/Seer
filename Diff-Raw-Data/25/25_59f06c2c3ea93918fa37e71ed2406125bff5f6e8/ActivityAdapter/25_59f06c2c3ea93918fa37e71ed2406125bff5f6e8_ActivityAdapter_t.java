 package org.macno.puma.adapter;
 
 import static org.macno.puma.PumaApplication.APP_NAME;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
import org.macno.puma.R;
 import org.macno.puma.activity.ViewActivity;
 import org.macno.puma.core.Account;
 import org.macno.puma.provider.Pumpio;
 import org.macno.puma.util.ActivityUtil;
 
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
import android.widget.Toast;
 
 public class ActivityAdapter extends ArrayAdapter<JSONObject> {
 
 	private String mFeed;
 	private Context mContext;
 	private Account mAccount;
 	private StreamHandler mHandler = new StreamHandler(this);
 
 	private JSONArray mItems;
 	
 	private Pumpio mPumpio;
 
 	private boolean mLoading = false;
 	
 	public ActivityAdapter(Context context, Account account, String feed) {
 		super(context,0,new ArrayList<JSONObject>());
 		mContext = context;
 		mAccount = account;
 		mFeed = feed;
 		loadStreams();
 	}
 	
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent) {
 		
 		JSONObject act  = getItem(position);
 		LinearLayout view = ActivityUtil.getViewActivity(getContext(), act);
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
 	
 	private void loadStreams() {
 		if(mLoading) {
 			return;
 		}
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				mLoading=true;
 				if(mPumpio == null) {
 					mPumpio = new Pumpio(mContext);
 					mPumpio.setAccount(mAccount);
 				}
 				JSONObject stream = mPumpio.fetchStream(mFeed, null, null, 20);
				if(stream == null) {
					mHandler.sendLoadStramFailed();
					return;
				}
 				JSONArray items = stream.optJSONArray("items");
 				mItems = items;
 
 				mHandler.sendLoadComplete();
 			}
 		};
 		new Thread(runnable).start();
 	}
 	
 	private void loadStreamsForNewer() {
 		if(mLoading) {
 			return;
 		}
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				mLoading=true;
 				if(mPumpio == null) {
 					mPumpio = new Pumpio(mContext);
 					mPumpio.setAccount(mAccount);
 				}
 				JSONObject last = getItem(0);
 				Log.d(APP_NAME,"Asking for newer than "  + last.optString("id"));
 				JSONObject stream = mPumpio.fetchStream(mFeed, last.optString("id"), null, 20);
 				JSONArray items = stream.optJSONArray("items");
 				mItems = items;
 
 				mHandler.sendReloadedNewer();
 			}
 		};
 		new Thread(runnable).start();
 	}
 	
 	private void reloadAdapter(boolean newer) {
 		mLoading=false;
 		if(mItems != null) {
 			for(int i=0;i<mItems.length();i++) {
 				// Check if activity id is already here.. 
 				
 				try {
 					JSONObject act = mItems.getJSONObject(i);
 					if(act.has("object")) {
 						String activityId = act.getString("id");
 						if(newer) {
 							checkAndDeleteIfExists(activityId);
 							add(act);
 						} else {
 							if(!checkIfExists(activityId)) { // I'm moving backward. If act exists, it's newer.. 
 								add(act);
 							}
 						}
 					}
 				} catch(JSONException e) {
 					Log.e(APP_NAME, e.getMessage(),e);
 				}
 			}
 			mItems = null;
 		}
 		notifyDataSetChanged();
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
		Toast.makeText(mContext, R.string.load_stream_failed, Toast.LENGTH_LONG).show();
	}
	
 	private static class StreamHandler extends Handler {
     	
     	private final WeakReference<ActivityAdapter> mTarget;
     	
     	private static final int MSG_POST_OK = 0;
     	private static final int MSG_RELOADED_NEWER = 1;
    	private static final int MSG_LOAD_STREAM_FAILED = 2;
     	
     	StreamHandler(ActivityAdapter target) {
 			mTarget = new WeakReference<ActivityAdapter>(target);
 		}
     	
     	public void handleMessage(Message msg) {
     		ActivityAdapter target = mTarget.get();
 			
 			switch (msg.what) {
 			
 			case MSG_RELOADED_NEWER:
 				target.reloadAdapter(true);
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
    	
    	void sendLoadStramFailed() {
    		sendEmptyMessage(MSG_LOAD_STREAM_FAILED);
    	}
     }
 	
 }
