 package jie.android.el;
 
 import java.io.File;
 import java.io.IOException;
 
 import jie.android.el.fragment.BaseFragment;
 import jie.android.el.service.ServiceAccess;
 import jie.android.el.service.ServiceNotification;
 import jie.android.el.utils.Speaker;
 import jie.android.el.utils.Utils;
 import jie.android.el.utils.XmlTranslator;
 import jie.android.el.CommonConsts.AppArgument;
 import jie.android.el.CommonConsts.AudioAction;
 import jie.android.el.CommonConsts.BroadcastAction;
 import jie.android.el.CommonConsts.FragmentArgument;
 import jie.android.el.CommonConsts.ServiceState;
 import jie.android.el.CommonConsts.Setting;
 import jie.android.el.CommonConsts.UIMsg;
 import jie.android.el.CommonConsts.UpdateUIType;
 import jie.android.el.R;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.SearchView;
 import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
 
 public class ELActivity extends SherlockFragmentActivity implements FragmentSwitcher.OnSwitchListener {
 
 	private static final String Tag = ELActivity.class.getSimpleName();
 
 	private ServiceAccess serviceAccess = null;
 	private boolean isServiceNotificationRegisted = false;
 
 	private FragmentSwitcher fragmentSwitcher = null;
 
 	private ProgressDialog progressDialog = null;
 
 	private SearchView searchView = null;
 	private Menu actionMenu = null;
 	private SharedPreferences sharedPreferences = null;
 
 	private boolean watchSearchChanged = false;
 
 	private Handler handler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case UIMsg.UI_CREATED:
 				showProgressDialog(true, "Connecting to service...");
 				break;
 			case UIMsg.SERVICE_NOTIFICATION:
 				onServiceNotification(msg.arg1);
 				break;
 			case UIMsg.SERVICE_AUDIO_ACTION:
 				onServiceAudioAction((Intent) msg.obj);
 				break;
 			case UIMsg.SERVICE_UPDATE_AUDIO:
 				onServiceUpdateAutio((Intent) msg.obj);
 				break;
 			case UIMsg.UI_HIDE_TITLE:
 				onUIHideTile();
 				break;
 			default:
 				;
 			}
 		}
 	};
 
 	private ServiceNotification serviceNotification = new ServiceNotification.Stub() {
 
 		@Override
 		public void onServiceState(int state) throws RemoteException {
 			Message msg = Message.obtain(handler, UIMsg.SERVICE_NOTIFICATION, state, -1);
 			msg.sendToTarget();
 		}
 
 		@Override
 		public void onAudioAction(Intent intent) throws RemoteException {
 			final String action = intent.getAction();
 			if (action.equals(AudioAction.ACTION_AUDIO_SET)) {
 				showFragment(FragmentSwitcher.Type.SHOW, null);
 
 				if (getSharedPreferences().getBoolean(Setting.CONTENT_HIDE_TITLE, false)) {
 					Message msg = Message.obtain(handler, UIMsg.UI_HIDE_TITLE);
 					handler.sendMessageDelayed(msg, 1500);
 				}
 
 			}
 
 			Message msg = Message.obtain(handler, UIMsg.SERVICE_AUDIO_ACTION, intent);
 			msg.sendToTarget();
 		}
 
 		@Override
 		public void onUpdateAudio(Intent intent) throws RemoteException {
 			Message msg = Message.obtain(handler, UIMsg.SERVICE_UPDATE_AUDIO, intent);
 			msg.sendToTarget();
 		}
 	};
 
 	ServiceConnection serviceConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder binder) {
 			serviceAccess = ServiceAccess.Stub.asInterface(binder);
 			registServiceNotification(true);
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			registServiceNotification(false);
 			serviceAccess = null;
 		}
 
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		checkPath();
 
 		sharedPreferences = Utils.getSharedPreferences(this);// getSharedPreferences(AppArgument.NAME,
 																// Context.MODE_MULTI_PROCESS);
 
 		fragmentSwitcher = new FragmentSwitcher(this);
 		fragmentSwitcher.setOnSwitchListener(this);
 
 		this.setContentView(R.layout.activity_el);
 
 		initService();
 		initSpeaker();
 		initTranslator();
 
 		handler.sendEmptyMessage(UIMsg.UI_CREATED);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		registServiceNotification(true);
 	}
 
 	private boolean checkPath() {
 		String sdcard = Utils.getExtenalSDCardDirectory();
 		File f = new File(sdcard + AppArgument.PATH_ROOT);
 		if (!f.exists()) {
 			if (!f.mkdirs()) {
 				Log.e(Tag, "make directory '" + f.getAbsolutePath() + "' failed.");
 				return false;
 			}
 		}
 
 		f = new File(sdcard + AppArgument.PATH_EL);
 		if (!f.exists()) {
 			if (!f.mkdirs()) {
 				Log.e(Tag, "make directory '" + f.getAbsolutePath() + "' failed.");
 				return false;
 			}
 		}
 
 		f = new File(sdcard + AppArgument.PATH_CACHE);
 		if (!f.exists()) {
 			if (!f.mkdirs()) {
 				Log.e(Tag, "make directory '" + f.getAbsolutePath() + "' failed.");
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	protected void onPause() {
 		fragmentSwitcher.restore();
 
 		super.onPause();
 
 		registServiceNotification(false);
 	}
 
 	@Override
 	protected void onDestroy() {
 		Log.d(Tag, "onDestroy");
 
 		releaseSpeasker();
 		releaseService(false);
 
 		super.onDestroy();
 	}
 
 	private void initSpeaker() {
 		Speaker.init(this);
 	}
 
 	private void releaseSpeasker() {
 		Speaker.release();
 	}
 
 	private void initTranslator() {
 		try {
 			XmlTranslator.init(this.getResources().getAssets().open("ld2.xsl"));
 		} catch (IOException e) {
 			Log.e(Tag, "init transformer failed.");
 		}
 	}
 
 	private void initService() {
 		Intent intent = new Intent("jie.android.el.elservice");
 		this.startService(intent);
 
 		intent = new Intent("jie.android.el.elservice");
 		this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	private void releaseService(boolean stop) {
 		if (serviceAccess != null) {
 			this.unbindService(serviceConnection);
 			if (stop) {
 				this.stopService(new Intent("jie.android.el.elservice"));
 			}
 			serviceAccess = null;
 		}
 	}
 
 	public ServiceAccess getServiceAccess() {
 		return serviceAccess;
 	}
 
 	public Handler getHandler() {
 		return handler;
 	}
 
 	public SharedPreferences getSharedPreferences() {
 		return sharedPreferences;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		BaseFragment fragment = fragmentSwitcher.getFragment();
 		if (fragment != null && fragment.getMenuRes() != -1) {
 			fragment.onCreateOptionsMenu(menu, getSupportMenuInflater());
 		} else {
 			getSupportMenuInflater().inflate(R.menu.activity_el, menu);
 			initSearchView(menu.findItem(R.id.el_menu_search));
 		}
 
 		actionMenu = menu;
 
 		return true;
 	}
 
 	private void initSearchView(MenuItem menu) {
 
 		searchView = (SearchView) menu.getActionView();
 		// searchView.setSubmitButtonEnabled(true);
 
 		searchView.setOnSearchClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View view) {
 				if (fragmentSwitcher.getCurrentType() != FragmentSwitcher.Type.SHOW) {
 					showFragment(FragmentSwitcher.Type.DICTIONARY, null);
 				}
 			}
 
 		});
 
 		searchView.setOnQueryTextListener(new OnQueryTextListener() {
 
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				Bundle data = new Bundle();
 				data.putInt(FragmentArgument.ACTION, FragmentArgument.Action.QUERY.getId());
 				data.putString(FragmentArgument.TEXT, query);
 
 				showFragment(FragmentSwitcher.Type.DICTIONARY, data);
 
 				return true;
 			}
 
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				if (watchSearchChanged && fragmentSwitcher.getCurrentType() == FragmentSwitcher.Type.DICTIONARY) {
 
 					Bundle data = new Bundle();
 					data.putInt(FragmentArgument.ACTION, FragmentArgument.Action.QUERY.getId());
 					data.putString(FragmentArgument.TEXT, newText);
 
 					fragmentSwitcher.getFragment().onArguments(data);
 					return true;
 				} else {
 					return false;
 				}
 			}
 		});
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		BaseFragment fragment = fragmentSwitcher.getFragment();
 		if (fragment != null) {
 			if (fragment.OnOptionsItemSelected(item)) {
 				return true;
 			}
 		}
 
 		switch (item.getItemId()) {
 		case R.id.el_menu_download:
 			showFragment(FragmentSwitcher.Type.DOWNLOAD, null);
 			break;
 		case R.id.el_menu_setting:
 			showFragment(FragmentSwitcher.Type.SETTING, null);
 			break;
 		case R.id.el_menu_about:
 			showFragment(FragmentSwitcher.Type.ABOUT, null);
 			break;
 		case R.id.el_menu_search:
 			showFragment(FragmentSwitcher.Type.DICTIONARY, null);
 			break;
 		case R.id.el_menu_vocab:
 			pausePlaying();
 			showFragment(FragmentSwitcher.Type.MEMORY, null);
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void showFragment(FragmentSwitcher.Type type, Bundle args) {
		fragmentSwitcher.show(type, args);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		BaseFragment fragment = fragmentSwitcher.getFragment();
 		if (fragment != null) {
 			if (fragment.onKeyDown(keyCode, event)) {
 				return true;
 			}
 		}
 
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (!fragmentSwitcher.showPrevFragment()) {
 				// releaseService(true);
 				finish();
 				return true;
 			}
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_MENU) {
 			actionMenu.performIdentifierAction(R.id.el_menu_overflow, 0);
 			return true;
 		}
 		return super.onKeyUp(keyCode, event);
 	}
 
 	protected void onServiceNotification(int state) {
 		if (state == ServiceState.READY.getId()) {
 			if (fragmentSwitcher.getCurrentType() == null) {
 				showFragment(FragmentSwitcher.Type.LIST, null);
 			}
 			showProgressDialog(false, null);
 		} else if (state == ServiceState.PLAYING.getId()) {
 			showFragment(FragmentSwitcher.Type.SHOW, null);
 		}
 	}
 
 	private void showProgressDialog(boolean show, final String text) {
 		if (show) {
 			if (progressDialog == null) {
 				progressDialog = new ProgressDialog(this);
 			}
 
 			progressDialog.setMessage(text);
 			if (!progressDialog.isShowing()) {
 				progressDialog.show();
 			}
 		} else {
 			if (progressDialog != null) {
 				progressDialog.dismiss();
 			}
 		}
 	}
 
 	@Override
 	public void onSwitch(FragmentSwitcher.Type oldType, FragmentSwitcher.Type newType) {
 		if (newType != FragmentSwitcher.Type.DICTIONARY) {
 			watchSearchChanged = false;
 			if (searchView != null) {
 				searchView.setQuery("", false);
 				searchView.clearFocus();
 				searchView.setIconified(true);
 			}
 		} else {
 			watchSearchChanged = true;
 		}
 
 		// if (oldType == FragmentSwitcher.Type.SHOW) {
 		// BaseFragment fragment = fragmentSwitcher.getFragment();
 		// if (fragment != null) {
 		// fragment.on
 		// }
 		// }
 	}
 
 	protected void pausePlaying() {
 		if (fragmentSwitcher.getCurrentType() == FragmentSwitcher.Type.SHOW) {
 			Intent intent = new Intent(AudioAction.ACTION_AUDIO_FORCE_PAUSE);
 			sendBroadcast(intent);
 		}
 	}
 
 	private void registServiceNotification(boolean reg) {
 		if (serviceAccess == null || isServiceNotificationRegisted == reg) {
 			return;
 		}
 
 		try {
 			if (reg) {
 				serviceAccess.regServiceNotification(0, serviceNotification);
 			} else {
 				serviceAccess.unregServiceNotification(0);
 			}
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		isServiceNotificationRegisted = reg;
 	}
 
 	protected void onServiceAudioAction(Intent intent) {
 		if (fragmentSwitcher.getCurrentType() == FragmentSwitcher.Type.SHOW) {
 			fragmentSwitcher.getFragment().onIntent(intent);
 		}
 	}
 
 	protected void onServiceUpdateAutio(Intent intent) {
 		if (fragmentSwitcher.getCurrentType() == FragmentSwitcher.Type.SHOW) {
 			fragmentSwitcher.getFragment().onIntent(intent);
 		}
 	}
 
 	protected void onUIHideTile() {
 		if (fragmentSwitcher.getCurrentType() == FragmentSwitcher.Type.SHOW) {
 			Intent intent = new Intent(BroadcastAction.ACTION_UPDATE_UI);
 			intent.putExtra(BroadcastAction.DATA_TYPE, UpdateUIType.HIDE_TITLE.getId());
 			fragmentSwitcher.getFragment().onIntent(intent);
 		}
 	}
 
 }
