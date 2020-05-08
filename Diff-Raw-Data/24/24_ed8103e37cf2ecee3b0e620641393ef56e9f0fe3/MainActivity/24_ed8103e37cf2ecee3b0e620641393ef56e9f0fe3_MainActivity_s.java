 package com.mili.xiaominglui.app.vello.ui;
 
 import java.lang.ref.WeakReference;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import wei.mark.standout.StandOutWindow;
 
 import android.accounts.Account;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.LayerDrawable;
 import android.graphics.drawable.TransitionDrawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.widget.FrameLayout;
 import android.widget.FrameLayout.LayoutParams;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.avos.avoscloud.AVAnalytics;
 import com.mili.xiaominglui.app.vello.R;
 import com.mili.xiaominglui.app.vello.authenticator.Constants;
 import com.mili.xiaominglui.app.vello.config.VelloConfig;
 import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
 import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
 import com.mili.xiaominglui.app.vello.service.VelloService;
 import com.mili.xiaominglui.app.vello.syncadapter.SyncHelper;
 import com.mili.xiaominglui.app.vello.util.AccountUtils;
 import com.mili.xiaominglui.app.vello.util.HelpUtils;
 import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
 import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
 
 public class MainActivity extends BaseActivity implements ReviewViewFragment.onStatusChangedListener, ConnectionTimeOutFragment.ConnectionTimeOutFragmentEventListener {
 	private static final String TAG = MainActivity.class.getSimpleName();
 	
 	private static final int CONTENT_VIEW_ID = 666;
 	private Drawable oldBackground = null;
 	private int currentColor = 0xFF666666;
 	private boolean isInFront;
 	private final Handler handler = new Handler();
 	private NotificationManager mNM;
 	
 	private MainActivityUIHandler mUICallback = new MainActivityUIHandler(this);
 
 	static class MainActivityUIHandler extends Handler {
 		WeakReference<MainActivity> mActivity;
 
 		MainActivityUIHandler(MainActivity activity) {
 			mActivity = new WeakReference<MainActivity>(activity);
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			MainActivity theActivity = mActivity.get();
 			switch (msg.what) {
 			case VelloService.MSG_DIALOG_BAD_DATA_ERROR_SHOW:
 				theActivity.showBadDataErrorDialog();
 				break;
 			case VelloService.MSG_SHOW_RESULT_WORDCARD:
 			    TrelloCard result = (TrelloCard) msg.obj;
 			    break;
 			case VelloService.MSG_STATUS_INIT_ACCOUNT_BEGIN:
				theActivity.preInitAccount();
 				break;
 			case VelloService.MSG_STATUS_INIT_ACCOUNT_END:
 				if (VelloConfig.DEBUG_SWITCH) {
 					Log.d(TAG, "status_init_account_end");
 				}
 				theActivity.postInitAccount();
 			case VelloService.MSG_STATUS_SYNC_BEGIN:
 				theActivity.preSync();
 				break;
 			case VelloService.MSG_STATUS_SYNC_END:
 				theActivity.postSync();
 				break;
 			case VelloService.MSG_STATUS_SYNC_BLANK:
 				theActivity.showSyncBlank();
 				break;
 			case VelloService.MSG_STATUS_REVOKE_BEGIN:
 				theActivity.preAuthTokenRevoke();
 				break;
 			case VelloService.MSG_STATUS_CONNECTION_TIMEOUT:
 				if (theActivity.isInFront) {
 					theActivity.showConnectionTimeoutView();
 				}
 				break;
 			case VelloService.MSG_STATUS_WEBHOOK_DELETED:
 				theActivity.localSignOut();
 				break;
 			}
 		}
 	}
 	
 	Messenger mService = null;
 	private boolean mIsBound;
 
 	final Messenger mMessenger = new Messenger(mUICallback);
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the service object we can use to
 			// interact with the service. Because we have bound to a explicit
 			// service that we know is running in our own process, we can
 			// cast its IBinder to a concrete class and directly access it.
 			mService = new Messenger(service);
 
 			try {
 				Message msg = Message.obtain(null, VelloService.MSG_REGISTER_CLIENT);
 				msg.replyTo = mMessenger;
 				mService.send(msg);
 			} catch (RemoteException e) {
 				// In this case the service has crashed before we could even do
 				// anything with it
 			}
 
 			if (AccountUtils.hasVocabularyBoard(getApplicationContext()) && AccountUtils.isVocabularyBoardWellFormed(getApplicationContext())) {
 				// all initialized
 				// if has no open card, blank page after init
 				// if has open card, review page
 			} else {
 				// begin to check vocabulary board
 				sendMessageToService(VelloService.MSG_CHECK_VOCABULARY_BOARD);
 			}
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			// This is called when the connection with the service has been
 			// unexpectedly disconnected - process crashed.
 			mService = null;
 		}
 	};
 
 	private void sendMessageToService(int type) {
 		if (mIsBound) {
 			if (mService != null) {
 				try {
 					Message msg = Message.obtain(null, type);
 					msg.replyTo = mMessenger;
 					mService.send(msg);
 				} catch (RemoteException e) {
 				}
 			}
 		}
 	}
 
 	void doBindService() {
 		// Establish a connection with the service. We use an explicit
 		// class name because we want a specific service implementation that
 		// we know will be running in our own process (and thus won't be
 		// supporting component replacement by other applications).
 		bindService(new Intent(this, VelloService.class), mConnection, Context.BIND_AUTO_CREATE);
 		mIsBound = true;
 	}
 
 	void doUnbindService() {
 		if (mIsBound) {
 			// Detach our existing connection.
 			unbindService(mConnection);
 			mIsBound = false;
 		}
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
         if (isFinishing()) {
             return;
         }
         mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
         FrameLayout frame = new FrameLayout(this);
         frame.setId(CONTENT_VIEW_ID);
         setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
         
         FragmentManager fm = getSupportFragmentManager();
 		fm.beginTransaction().add(CONTENT_VIEW_ID, ReviewViewFragment.newInstance()).commit();
 
 		handleIntent(getIntent());
 		doBindService();
 		
 		AVAnalytics.trackAppOpened(getIntent());
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 	}
 
 	@Override
 	protected void onResume() {
 		isInFront = true;
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		isInFront = false;
 		super.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		doUnbindService();
 	}
 	
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		onNewIntent(getIntent());
 	}
 	
 	@Override
 	protected void onNewIntent(Intent intent) {
 		setIntent(intent);
 		handleIntent(intent);
 	}
 	
 	private void handleIntent(Intent intent) {
 
 	}
 	
 	private void doWordSearch(String query) {
 		// TODO
 		// 1. check if the word is in remote but closed, show if yes and re-open, go on if no
 		// 2. query dictionary service
 		// 3. show and insert local cache item
 		// 4. sync for ensuring adding to remote successfully
 		if (mIsBound) {
 			if (mService != null) {
 				try {
 					Message msg = Message.obtain(null, VelloService.MSG_TRIGGER_QUERY_WORD);
 					msg.obj = query;
 					msg.replyTo = mMessenger;
 					mService.send(msg);
 				} catch (RemoteException e) {
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getSupportMenuInflater().inflate(R.menu.home, menu);
 
 //		setupSearchMenuItem(menu);
 
 		return true;
 	}
 	/* hide search function
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void setupSearchMenuItem(Menu menu) {
         MenuItem searchItem = menu.findItem(R.id.menu_search);
         if (searchItem != null && UIUtils.hasHoneycomb()) {
             SearchView searchView = (SearchView) searchItem.getActionView();
             searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
 				
 				@Override
 				public boolean onQueryTextSubmit(String query) {
 				    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
 					inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
 					return true;
 				}
 				
 				@Override
 				public boolean onQueryTextChange(String newText) {
 					if (mHomeViewFragment.isAdded()) {
 						((HomeViewFragment) mHomeViewFragment).onQueryTextChange(newText);
 					}
 					
 					return true;
 				}
 			});
             if (searchView != null) {
                 SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
                 searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
             }
         }
     }
     */
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		/** disable search at present
 		case R.id.menu_search:
 			if (!UIUtils.hasHoneycomb()) {
                 startSearch(null, false, Bundle.EMPTY, false);
                 return true;
             }
 			break; 
 		case R.id.menu_sync:
 			triggerRefresh();
 			return true;**/
 
 		case R.id.menu_about:
 			HelpUtils.showAbout(this);
 			return true;
 
 		case R.id.menu_settings:
 			startActivity(new Intent(this, SettingsActivity.class));
 			return true;
 
 		case R.id.menu_sign_out:
 			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 			int syncFreqValue = Integer.valueOf(settings.getString(SettingsActivity.KEY_PREF_SYNC_FREQ, "24"));
 			if (syncFreqValue == 0) {
 				// deactive PUSH
 				if (!AccountUtils.getVocabularyBoardWebHookId(getApplicationContext()).equals("")) {
 					sendMessageToService(VelloService.MSG_DELETE_WEBHOOK);
 					return true;
 				}
 			}
 			localSignOut();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void localSignOut() {
 		AccountUtils.signOut(this);
 		// restart
 		Intent intent = getIntent();
 		finish();
 		startActivity(intent);
 	}
 
 	private void triggerRefresh() {
 		SyncHelper.requestManualSync(new Account(AccountUtils.getChosenAccountName(this), Constants.ACCOUNT_TYPE));
 	}
 	
 	private void changeColor(int newColor) {
 		// change ActionBar color just if an ActionBar is available
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 
 			Drawable colorDrawable = new ColorDrawable(newColor);
 			Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
 			LayerDrawable ld = new LayerDrawable(new Drawable[] { colorDrawable, bottomDrawable });
 
 			if (oldBackground == null) {
 
 				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
 					ld.setCallback(drawableCallback);
 				} else {
 					getActionBar().setBackgroundDrawable(ld);
 				}
 
 			} else {
 
 				TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, ld });
 
 				// workaround for broken ActionBarContainer drawable handling on
 				// pre-API 17 builds
 				// https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
 				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
 					td.setCallback(drawableCallback);
 				} else {
 					getActionBar().setBackgroundDrawable(td);
 				}
 
 				td.startTransition(200);
 
 			}
 
 			oldBackground = ld;
 
 			// http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
 			getActionBar().setDisplayShowTitleEnabled(false);
 			getActionBar().setDisplayShowTitleEnabled(true);
 
 		}
 
 		currentColor = newColor;
 
 	}
 	private void preInitAccount() {
 		FragmentManager fm = getSupportFragmentManager();
 		fm.beginTransaction().replace(CONTENT_VIEW_ID, new ProgressFragment()).commit();
 	}
 	
 	private void postInitAccount() {
 		Account account = new Account(AccountUtils.getChosenAccountName(getApplicationContext()), Constants.ACCOUNT_TYPE);
 		ContentResolver.setIsSyncable(account, VelloProvider.AUTHORITY, 1);
 		ContentResolver.setSyncAutomatically(account, VelloProvider.AUTHORITY, true);
 		ContentResolver.setMasterSyncAutomatically(true);
 		
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		int syncFreqValue = Integer.valueOf(settings.getString(SettingsActivity.KEY_PREF_SYNC_FREQ, "24"));
 		if (syncFreqValue > 0) {
 			Bundle extras = new Bundle();
 			ContentResolver.addPeriodicSync(account, VelloProvider.AUTHORITY, extras, syncFreqValue * 60 * 60);
 		}
 		
 		triggerRefresh();
 	}
 	
 	private void preSync() {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "preSync");
 		}
 		if (isInFront) {
 			FragmentManager fm = getSupportFragmentManager();
 			fm.beginTransaction().replace(CONTENT_VIEW_ID, new ProgressFragment()).commit();
 		}
 	}
 	
 	private void postSync() {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "postSync");
 		}
 		// show notification
 		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
 		PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
 
 		ProviderCriteria cri = new ProviderCriteria();
 		cri.addSortOrder(DbWordCard.Columns.DUE, true);
 		Calendar rightNow = Calendar.getInstance();
 
 		SimpleDateFormat fo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 		long rightNowUnixTime = rightNow.getTimeInMillis();
 		long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
 		String now = fo.format(new Date(rightNowUnixTimeGMT));
 		cri.addEq(DbWordCard.Columns.MARKDELETED, "false");
 		cri.addLt(DbWordCard.Columns.DUE, now, true);
 		Cursor cur = getContentResolver().query(
 				DbWordCard.CONTENT_URI,
 				DbWordCard.PROJECTION,
 				cri.getWhereClause(),
 				cri.getWhereParams(),
 				cri.getOrderClause());
 		if (cur != null) {
 			int num = cur.getCount();
 			if (num > 0) {
 				Resources res = getApplicationContext().getResources();
 				String stringContentTitle = res.getQuantityString(R.plurals.notif_content_title, num, num);
 				String stringContentText = res.getString(R.string.notif_content_text);
 				Notification noti = new NotificationCompat.Builder(getApplicationContext())
 						.setContentTitle(stringContentTitle)
 						.setContentText(stringContentText)
 						.setSmallIcon(R.drawable.ic_launcher)
 						.setContentIntent(pIntent)
 						.build();
 
 
 				// Hide the notification after its selected
 				noti.flags |= Notification.FLAG_AUTO_CANCEL;
 
 				mNM.notify(0, noti);
 				if (isInFront) {
 					FragmentManager fm = getSupportFragmentManager();
 					fm.beginTransaction().replace(CONTENT_VIEW_ID, ReviewViewFragment.newInstance()).commit();
 				}
 			} else {
 				// no word need recalling
 				showSyncBlank();
 			}
 			cur.close();
 		}
 	}
 	
 	private void showSyncBlank() {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "showSyncBlank called, with isInFront=" + isInFront);
 		}
 		if (isInFront) {
 			FragmentManager fm = getSupportFragmentManager();
 			fm.beginTransaction().replace(CONTENT_VIEW_ID, SyncBlankViewFragment.newInstance()).commit();
 		}
 	}
 	
 	private void preAuthTokenRevoke() {
 		FragmentManager fm = getSupportFragmentManager();
 		fm.beginTransaction().replace(CONTENT_VIEW_ID, new ProgressFragment()).commit();
 	}
 	
 	private void showConnectionTimeoutView() {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "showConnectionTimeoutView called, with isInFront=" + isInFront);
 		}
 		if (isInFront) {
 			FragmentManager fm = getSupportFragmentManager();
 			fm.beginTransaction().replace(CONTENT_VIEW_ID, ConnectionTimeOutFragment.newInstance()).commit();
 		}
 	}
 	
 	private Drawable.Callback drawableCallback = new Drawable.Callback() {
 		@Override
 		public void invalidateDrawable(Drawable who) {
 			getActionBar().setBackgroundDrawable(who);
 		}
 
 		@Override
 		public void scheduleDrawable(Drawable who, Runnable what, long when) {
 			handler.postAtTime(what, when);
 		}
 
 		@Override
 		public void unscheduleDrawable(Drawable who, Runnable what) {
 			handler.removeCallbacks(what);
 		}
 	};
 
 	@Override
 	public void onModeChanged(int modeColor) {
 		changeColor(modeColor);
 	}
 
 	@Override
 	public void syncOnAllRecalled() {
 		triggerRefresh();
 	}
 
 	@Override
 	public void onWordRecalled() {
 	}
 
 	@Override
 	public void onReload() {
 		triggerRefresh();
 	}
 }
