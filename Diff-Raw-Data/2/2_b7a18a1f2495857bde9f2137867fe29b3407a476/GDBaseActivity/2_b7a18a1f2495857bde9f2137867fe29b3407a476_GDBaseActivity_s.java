 package com.dbstar.app;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.dbstar.R;
 import com.dbstar.app.alert.GDAlertDialog;
 import com.dbstar.app.alert.NotificationFragment;
 import com.dbstar.model.EventData;
 import com.dbstar.model.GDCommon;
 import com.dbstar.service.ClientObserver;
 import com.dbstar.service.GDAudioController;
 import com.dbstar.service.GDDataProviderService;
 import com.dbstar.service.GDDataProviderService.DataProviderBinder;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GDBaseActivity extends Activity implements ClientObserver {
 	private static final String TAG = "GDBaseActivity";
 
 	protected static final int DLG_ID_ALERT = 0;
 	protected static final int DLG_ID_SMARTCARD = 1;
 
 	protected static final int DLG_TYPE_FILE_NOTEXIST = 0;
 	protected static final int DLG_TYPE_SMARTCARD_INFO = 1;
 	protected static final int DLG_TYPE_NEWMAIL_INFO = 2;
 	protected static final int DLG_TYPE_NOTIFICATION = 3;
 
 	protected static final String INTENT_KEY_MENUPATH = "menu_path";
 	protected static final int MENU_LEVEL_1 = 0;
 	protected static final int MENU_LEVEL_2 = 1;
 	protected static final int MENU_LEVEL_3 = 2;
 	protected static final int MENU_LEVEL_COUNT = 3;
 	protected static final String MENU_STRING_DELIMITER = ">";
 	protected String mMenuPath;
 	protected MenuPathItem[] mMenuPathItems = new MenuPathItem[MENU_LEVEL_COUNT];
 	// Menu path container view
 	protected ViewGroup mMenuPathContainer;
 
 	protected boolean mIsStarted = false; // true when this activity is not
 											// visible
 	protected boolean mBlockSmartcardPopup = false; // false to allow activity
 													// show alert
 	protected int mSmartcardState = GDCommon.SMARTCARD_STATE_NONE;
 
 	protected boolean isSmartcardReady() {
 		if (mService != null) {
 			return mService.isSmartcardReady();
 		}
 
 		return false;
 	}
 
 	protected boolean isSmartcardPlugIn() {
 		if (mService != null) {
 			return mService.isSmartcardPlugIn();
 		}
 
 		return false;
 	}
 
 	// launcher will call this to check whether smartcard is plugged in.
 	protected void checkSmartcardStatus() {
 		if (mService == null)
 			return;
 
 		boolean isIn = mService.isSmartcardPlugIn();
 		if (!isIn) {
 			notifySmartcardStatusChanged();
 		}
 	}
 
 	protected class MenuPathItem {
 		TextView sTextView;
 		ImageView sDelimiter;
 	}
 
 	protected boolean mBound = false;
 	protected GDDataProviderService mService;
 
 	private ProgressDialog mLoadingDialog = null;
 	private String mLoadingText = null;
 
 	GDAlertDialog mAlertDlg = null, mSmartcardDlg = null;
 	int mAlertType = -1;
 
 	protected Handler mHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_SMARTCARD_STATUSCHANGED: {
 				alertSmartcardInfo();
 				break;
 			}
 			case MSG_NEW_MAIL: {
 				alertNewMail();
 				break;
 			}
 			case MSG_DISP_NOTIFICATION: {
 				displayNotification((String) msg.obj);
 				break;
 			}
 			case MSG_HIDE_NOTIFICATION: {
 				hideNotification();
 				break;
 			}
 			}
 		}
 	};
 
 	protected GDResourceAccessor mResource;
 
 	protected void initializeMenuPath() {
 
 		mMenuPathContainer = (ViewGroup) findViewById(R.id.menupath_view);
 
 		for (int i = 0; i < MENU_LEVEL_COUNT; i++) {
 			mMenuPathItems[i] = new MenuPathItem();
 		}
 
 		TextView textView = (TextView) findViewById(R.id.menupath_level1);
 		mMenuPathItems[0].sTextView = textView;
 		textView = (TextView) findViewById(R.id.menupath_level2);
 		mMenuPathItems[1].sTextView = textView;
 		textView = (TextView) findViewById(R.id.menupath_level3);
 		mMenuPathItems[2].sTextView = textView;
 
 		ImageView delimiterView = (ImageView) findViewById(R.id.menupath_level1_delimiter);
 		mMenuPathItems[0].sDelimiter = delimiterView;
 
 		delimiterView = (ImageView) findViewById(R.id.menupath_level2_delimiter);
 		mMenuPathItems[1].sDelimiter = delimiterView;
 
 		delimiterView = (ImageView) findViewById(R.id.menupath_level3_delimiter);
 		mMenuPathItems[2].sDelimiter = delimiterView;
 	}
 
 	protected void initializeView() {
 		initializeMenuPath();
 	}
 
 	protected void showMenuPath(String[] menuPath) {
 
 		for (int i = 0; i < mMenuPathItems.length; i++) {
 			if (i < menuPath.length) {
 				mMenuPathItems[i].sTextView.setVisibility(View.VISIBLE);
 				mMenuPathItems[i].sTextView.setText(menuPath[i]);
 
 				if (mMenuPathItems[i].sDelimiter != null) {
 					mMenuPathItems[i].sDelimiter.setVisibility(View.VISIBLE);
 				}
 			} else {
 				mMenuPathItems[i].sTextView.setVisibility(View.INVISIBLE);
 
 				if (mMenuPathItems[i].sDelimiter != null) {
 					mMenuPathItems[i].sDelimiter.setVisibility(View.INVISIBLE);
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mResource = new GDResourceAccessor(this);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		mIsStarted = true;
 
 		if (!mBound) {
 			Intent intent = new Intent(this, GDDataProviderService.class);
 			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 
 		mIsStarted = false;
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		if (mBound) {
 			mService.unRegisterPageObserver(this);
 		}
 
 		if (mBound) {
 			unbindService(mConnection);
 			mBound = false;
 		}
 
 		if (mDlgTimer != null) {
 			mDlgTimer.cancel();
 		}
 	}
 
 	public void setMute(boolean mute) {
 		Intent intent = new Intent(GDAudioController.ActionMute);
 		intent.putExtra("key_mute", mute);
 		sendBroadcast(intent);
 	}
 
 	public boolean isMute() {
 		AudioManager audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
 
 		boolean mute = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
 		return mute;
 	}
 
 	@Override
 	public void startActivity(Intent intent) {
 		super.startActivity(intent);
 
 		// overridePendingTransition(R.anim.slide_in_right, 0);
 		overridePendingTransition(R.anim.fade_in_short, R.anim.fade_out_short);
 	}
 
 	public void startActivity(Intent intent, boolean animate) {
 		super.startActivity(intent);
 
 		int enterAnimateId = 0;
 		int exitAnimateId = 0;
 		if (animate) {
 			// animateId = R.anim.slide_in_right;
 			enterAnimateId = R.anim.fade_in_short;
 			exitAnimateId = R.anim.fade_out_short;
 		}
 
 		overridePendingTransition(enterAnimateId, exitAnimateId);
 	}
 
 	@Override
 	public void finish() {
 		super.finish();
 
 		// eliminate the animation between activities
 		// enterAnim, exitAnim
 		// overridePendingTransition(0, R.anim.slide_out_left);
 		overridePendingTransition(R.anim.fade_in_short, R.anim.fade_out_short);
 	}
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			DataProviderBinder binder = (DataProviderBinder) service;
 			mService = binder.getService();
 			mBound = true;
 
 			onServiceStart();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName className) {
 			mBound = false;
 
 			onServiceStop();
 		}
 	};
 
 	protected void onServiceStart() {
 		Log.d(TAG, "onServiceStart");
 
 		mService.registerPageObserver(this);
 		// get the init state of smart card.
 		mSmartcardState = mService.getSmartcardState();
 	}
 
 	protected void onServiceStop() {
 		Log.d(TAG, "onServiceStop");
 
 		mService.unRegisterPageObserver(this);
 	}
 
 	public void updateData(int type, int param1, int param2, Object data) {
 
 	}
 
 	public void updateData(int type, Object key, Object data) {
 
 	}
 
 	public void notifyEvent(int type, Object event) {
 
 		Log.d(TAG, "======= notifyEvent ==== type " + type + " event " + event);
 
 		if (type == EventData.EVENT_SMARTCARD_STATUS) {
 			EventData.SmartcardStatus status = (EventData.SmartcardStatus) event;
 			mSmartcardState = status.State;
 
 			Log.d(TAG, " === mIsStarted == " + mIsStarted
 					+ " mBlockSmartcardPopup =" + mBlockSmartcardPopup);
 
 			if (mIsStarted) {
 				if (!mBlockSmartcardPopup) {
 					notifySmartcardStatusChanged();
 				}
 			} else {
 				// settings or guodian app is on top, so send message
 				// and let them to show smard card state info.
 				Intent intent = new Intent(GDCommon.ActionSDStateChange);
 				intent.putExtra(GDCommon.KeySDState, mSmartcardState);
 				sendBroadcast(intent);
 			}
 
 		} else if (type == EventData.EVENT_NEWMAIL) {
 			notifyNewMail();
 		} else if (type == EventData.EVENT_NOTIFICATION) {
 			Message msg = mHandler.obtainMessage(MSG_DISP_NOTIFICATION);
 			msg.obj = event;
 			msg.sendToTarget();
 		} else if (type == EventData.EVENT_HIDE_NOTIFICATION) {
 			mHandler.sendEmptyMessage(MSG_HIDE_NOTIFICATION);
 		}
 	}
 
 	protected boolean checkLoadingIsFinished() {
 		return true;
 	}
 
 	protected void showLoadingDialog() {
 
 		if (mLoadingText == null) {
 			mLoadingText = getResources().getString(R.string.loading_text);
 		}
 
 		if (mLoadingDialog == null || !mLoadingDialog.isShowing()) {
 			Log.d(TAG, "show loading dialog");
 			mLoadingDialog = ProgressDialog.show(this, "", mLoadingText, true);
 			mLoadingDialog.setCancelable(true);
 			mLoadingDialog.setCanceledOnTouchOutside(true);
 			mLoadingDialog.setOnCancelListener(new LoadingCancelListener());
 		}
 	}
 
 	protected void hideLoadingDialog() {
 		if (mLoadingDialog != null && mLoadingDialog.isShowing()
 				&& checkLoadingIsFinished()) {
 			Log.d(TAG, "hide loading dialog");
 			mLoadingDialog.dismiss();
 		}
 	}
 
 	private class LoadingCancelListener implements OnCancelListener {
 		public void onCancel(DialogInterface dialog) {
 			onLoadingCancelled();
 		}
 	}
 
 	protected void onLoadingCancelled() {
 		Log.d(TAG, "onLoadingCancelled");
 
 		cancelRequests(this);
 	}
 
 	protected void cancelRequests(ClientObserver observer) {
 		Log.d(TAG, "cancelRequests");
 
 		mService.cancelRequests(observer);
 	}
 
 	protected String formPageText(int pageNumber) {
 		String str = mResource.HanZi_Di;
 		str += (pageNumber + 1) + mResource.HanZi_Ye;
 
 		return str;
 	}
 
 	protected String formPageText(int pageNumber, int pageCount) {
 		String str = pageNumber + "/" + pageCount;
 		return str;
 	}
 
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 		switch (id) {
 		case DLG_ID_ALERT: {
 			mAlertDlg = new GDAlertDialog(this, id);
 			mAlertDlg.setOnShowListener(mOnShowListener);
 			dialog = mAlertDlg;
 			break;
 		}
 		case DLG_ID_SMARTCARD: {
 			mSmartcardDlg = new GDAlertDialog(this, id);
 			mSmartcardDlg.setOnShowListener(mOnShowListener);
 			dialog = mSmartcardDlg;
 			break;
 		}
 		}
 
 		return dialog;
 	}
 
 	DialogInterface.OnShowListener mOnShowListener = new DialogInterface.OnShowListener() {
 
 		@Override
 		public void onShow(DialogInterface dialog) {
 			if (dialog instanceof GDAlertDialog) {
 				displayAlertDlg((GDAlertDialog) dialog, mAlertType);
 			}
 
 		}
 	};
 
 	void displayAlertDlg(GDAlertDialog dialog, int type) {
 
 		Log.d(TAG, " ====  displayAlertDlg == " + type);
 
 		switch (type) {
 		case DLG_TYPE_FILE_NOTEXIST: {
 			dialog.setTitle(R.string.error_title);
 			dialog.setMessage(R.string.file_notexist);
 			dialog.showSingleButton();
 			break;
 		}
 		case DLG_TYPE_SMARTCARD_INFO: {
 			dialog.setTitle(R.string.smartcard_status_title);
 			dialog.showSingleButton();
 
 			if (mSmartcardState == GDCommon.SMARTCARD_STATE_INSERTED
 					|| mSmartcardState == GDCommon.SMARTCARD_STATE_INERTING) {
 				dialog.setMessage(R.string.smartcard_status_in);
			} else if (mSmartcardState == GDCommon.SMARTCARD_STATE_REMOVING
 					|| mSmartcardState == GDCommon.SMARTCARD_STATE_REMOVING) {
 				dialog.setMessage(R.string.smartcard_status_out);
 			} else {
 				dialog.setMessage(R.string.smartcard_status_invlid);
 			}
 			break;
 		}
 		case DLG_TYPE_NEWMAIL_INFO: {
 			dialog.setTitle(R.string.alert_title);
 			dialog.setMessage(R.string.email_newmail);
 			dialog.showSingleButton();
 			break;
 		}
 		}
 
 		if (dialog != null) {
 			dialog.mOkButton.requestFocus();
 		}
 	}
 
 	private static final int DLG_TIMEOUT = 3000;
 	protected static final int MSG_SMARTCARD_STATUSCHANGED = 0x80001;
 
 	protected static final int MSG_NEW_MAIL = 2;
 	protected static final int MSG_DISP_NOTIFICATION = 3;
 	protected static final int MSG_HIDE_NOTIFICATION = 4;
 
 	Timer mDlgTimer = null;
 	TimerTask mTimeoutTask = null;
 
 	protected void notifySmartcardStatusChanged() {
 		mHandler.sendEmptyMessage(MSG_SMARTCARD_STATUSCHANGED);
 	}
 
 	protected void notifyNewMail() {
 		mHandler.sendEmptyMessage(MSG_NEW_MAIL);
 	}
 
 	protected void alertFileNotExist() {
 		mAlertType = DLG_TYPE_FILE_NOTEXIST;
 
 		if (mAlertDlg == null || !mAlertDlg.isShowing()) {
 			showDialog(DLG_ID_ALERT);
 		} else {
 			displayAlertDlg(mAlertDlg, mAlertType);
 		}
 	}
 
 	protected void alertNewMail() {
 		mAlertType = DLG_TYPE_NEWMAIL_INFO;
 
 		if (mAlertDlg == null || !mAlertDlg.isShowing()) {
 			showDialog(DLG_ID_ALERT);
 		} else {
 			displayAlertDlg(mAlertDlg, mAlertType);
 		}
 	}
 
 	protected void alertSmartcardInfo() {
 		mAlertType = DLG_TYPE_SMARTCARD_INFO;
 
 		if (mService != null) {
 			mSmartcardState = mService.getSmartcardState();
 		}
 
 		Log.d(TAG, " ======== display smartcard state ==== " + mSmartcardState);
 
 		if (mSmartcardDlg == null || !mSmartcardDlg.isShowing()) {
 			if (mSmartcardDlg == null
 					&& mSmartcardState == GDCommon.SMARTCARD_STATE_INSERTED) {
 				// not display insert ok dialog.
 				return;
 			}
 
 			showDialog(DLG_ID_SMARTCARD);
 		} else {
 			displayAlertDlg(mSmartcardDlg, mAlertType);
 		}
 
 		if (mSmartcardState == GDCommon.SMARTCARD_STATE_INSERTED) {
 			hideDlgDelay();
 		} else {
 			if (mTimeoutTask != null) {
 				mTimeoutTask.cancel();
 			}
 
 			if (mDlgTimer != null) {
 				mDlgTimer.cancel();
 			}
 		}
 	}
 
 	protected void displayNotification(String message) {
 		Log.d(TAG, " ======= displayNotification ============ " + message);
 
 		if (message != null && !message.isEmpty()) {
 
 			FragmentTransaction ft = getFragmentManager().beginTransaction();
 			Fragment prev = getFragmentManager().findFragmentByTag(
 					"osd_notification");
 			if (prev != null) {
 				ft.remove(prev);
 			}
 			ft.addToBackStack(null);
 
 			String[] data = message.split("\t");
 			if (data.length > 1) {
 				int type = Integer.valueOf(data[0]);
 				int duration = GDCommon.OSDDISP_TIMEOUT;
 				// Create and show the dialog.
 				NotificationFragment newFragment = NotificationFragment
 						.newInstance(type, data[1], duration);
 
 				newFragment.show(ft, "osd_notification");
 			}
 		}
 	}
 
 	protected void hideNotification() {
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		Fragment prev = getFragmentManager().findFragmentByTag(
 				"osd_notification");
 		if (prev != null) {
 			ft.remove(prev);
 		}
 		ft.addToBackStack(null);
 	}
 
 	void hideDlgDelay() {
 		final Handler handler = new Handler() {
 
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 				case 0x4ef:
 					mTimeoutTask.cancel();
 					mTimeoutTask = null;
 					mDlgTimer.cancel();
 					mDlgTimer = null;
 
 					if (mSmartcardDlg != null && mSmartcardDlg.isShowing()) {
 						mSmartcardDlg.dismiss();
 					}
 					break;
 				}
 				super.handleMessage(msg);
 			}
 
 		};
 
 		if (mTimeoutTask != null) {
 			mTimeoutTask.cancel();
 		}
 
 		mTimeoutTask = new TimerTask() {
 
 			public void run() {
 				Message message = Message.obtain();
 				message.what = 0x4ef;
 				handler.sendMessage(message);
 			}
 		};
 
 		if (mDlgTimer != null) {
 			mDlgTimer.cancel();
 		}
 
 		mDlgTimer = new Timer();
 		mDlgTimer.schedule(mTimeoutTask, DLG_TIMEOUT);
 	}
 }
