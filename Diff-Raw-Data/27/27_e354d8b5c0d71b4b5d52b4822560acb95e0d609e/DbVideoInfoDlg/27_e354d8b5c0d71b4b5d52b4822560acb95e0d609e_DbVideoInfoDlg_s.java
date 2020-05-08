 package com.dbstar.DbstarDVB.VideoPlayer.alert;
 
 import java.util.HashMap;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 
 import com.dbstar.DbstarDVB.R;
 
 public class DbVideoInfoDlg extends Dialog implements ViewStateManager {
 
 	private static final String TAG = "DbVideoInfoDlg";
 
 	Intent mIntent = null;
 	ViewState mState;
 	MediaData mMediaData;
 
 	HashMap<String, ViewState> mStates = new HashMap<String, ViewState>();
 
 	public DbVideoInfoDlg(Context context, Intent intent) {
 		super(context, R.style.DbDialog);
 
 		mMediaData = getMediaData(intent);
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setOnDismissListener(mDismissListener);
 
 		mState = new NormalState(this, this);
 		addState(NormalState.ID, mState);
 		// mState.enter(mMediaData);
 	}
 
 	DialogInterface.OnDismissListener mDismissListener = new DialogInterface.OnDismissListener() {
 		@Override
 		public void onDismiss(DialogInterface dialog) {
 
 			Log.d(TAG, "===== onDismiss ==== ");
 
 			if (mState != null) {
 				mState.stop();
 				mState.exit();
 				mState = null;
 			}
 		}
 	};
 
 	DialogInterface.OnShowListener mOnShowListener = new DialogInterface.OnShowListener() {
 
 		@Override
 		public void onShow(DialogInterface dialog) {
 
 			Log.d(TAG, "===== onShow ==== ");
 
 			// mState = getState(NormalState.ID);
 			//
 			// if (mState != null) {
 			// mState.enter(mMediaData);
 			// mState.start();
 			// }
 		}
 	};
 
 	public void onStart() {
 		super.onStart();
 
 		Log.d(TAG, "===== onStart ==== ");
 		mState = getState(NormalState.ID);
 
 		if (mState != null) {
 			mState.enter(mMediaData);
 			mState.start();
 		}
 	}
 
 	public void onStop() {
 		super.onStop();
 
 		Log.d(TAG, "===== onStop ==== ");
 
 		if (mState != null) {
 			mState.stop();
 		}
 	}
 
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event) {
 
 		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			Log.d("DbVideoInfoDlg",
					"==== onKeyDown keyCode ====" + event.getKeyCode());
 
			if (mState != null) {
 				mState.keyEvent(event.getKeyCode(), event);
 			}
 		}
 
 		return super.dispatchKeyEvent(event);
 	}
 
 	public ViewState getState(String id) {
 		ViewState state = mStates.get(id);
 
 		Log.d(TAG, "===== getState ==== " + id + " " + state);
 
 		return state;
 	}
 
 	public void addState(String id, ViewState state) {
 		Log.d(TAG, "===== addState ==== " + id + " " + state);
 
 		if (mStates.containsKey(id)) {
 			Log.d(TAG, "===== addState ==== has");
 			return;
 		}
 
 		mStates.put(id, state);
 	}
 
 	@Override
 	public void changeToState(ViewState state, Object args) {
 		String oldId = "";
 		if (mState != null) {
 			oldId = mState.getId();
 			mState.stop();
 			mState.exit();
 		}
 
 		Log.d(TAG, "===== addState ==== new " + state.getId() + " old = "
 				+ oldId);
 
 		addState(state.getId(), state);
 		state.enter(args);
 		state.start();
 		mState = state;
 	}
 
 	MediaData getMediaData(Intent intent) {
 		MediaData mediaData = new MediaData();
 
 		mediaData.PublicationSetID = intent.getStringExtra("publicationset_id");
 		mediaData.PublicationId = intent.getStringExtra("publication_id");
 		mediaData.Title = intent.getStringExtra("title");
 		mediaData.Description = intent.getStringExtra("description");
 		mediaData.Director = intent.getStringExtra("director");
 		mediaData.Scenarist = intent.getStringExtra("scenarist");
 		mediaData.Actors = intent.getStringExtra("actors");
 		mediaData.Type = intent.getStringExtra("type");
 		mediaData.CodeFormat = intent.getStringExtra("codeformat");
 		mediaData.Region = intent.getStringExtra("area");
 		mediaData.Bitrate = intent.getStringExtra("bitrate");
 		mediaData.Resolution = intent.getStringExtra("resolution");
 
 		Log.d("DbVideoInfoDlg", "PublicationId = " + mediaData.PublicationId);
 		Log.d("DbVideoInfoDlg", "Description = " + mediaData.Description);
 		return mediaData;
 	}
 }
