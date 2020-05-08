 package co.shoutbreak.ui;
 
 import co.shoutbreak.R;
 import co.shoutbreak.components.SBNotificationManager;
 import co.shoutbreak.components.SBStateManager;
 import co.shoutbreak.misc.C;
 import co.shoutbreak.misc.SBLog;
 import co.shoutbreak.service.SBService;
 import co.shoutbreak.service.SBServiceBridgeInterface;
 import co.shoutbreak.ui.views.ComposeView;
 import co.shoutbreak.ui.views.InboxView;
 import co.shoutbreak.ui.views.ProfileView;
 import co.shoutbreak.ui.views.SBView;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 
 public class SBContext extends Activity {
 
 	public static final int COMPOSE_VIEW = 0;
 	public static final int INBOX_VIEW = 1;
 	public static final int PROFILE_VIEW = 2;
 	
 	private static final String TAG = "SBContext.java";
 
 	private SBStateManager _StateManager;
 	private SBNotificationManager _NotificationManager;
 	private SBServiceBridgeInterface _ServiceBinder;
 	private SBView _ViewArray[];
 	private SBView _CurrentView;
 
 	/* LIFECYCLE METHODS */
 
 	@Override
 	public void onCreate(Bundle bundle) {
 		Intent serviceIntent;
 		ImageButton composeTab;
 		ImageButton inboxTab;
 		ImageButton profileTab;
 		
 		SBLog.i(TAG, "onCreate()");
 		super.onCreate(bundle);
 
 		// initialize components
 		_NotificationManager = new SBNotificationManager(this);
 
 		// connect to service
 		serviceIntent = new Intent(SBContext.this, SBService.class);
 		serviceIntent.putExtra(C.START_FROM_UI, true);
 		startService(serviceIntent); // must be called, BIND_AUTO_CREATE doesn't start service
 		bindService(serviceIntent, _ServiceConnection, Context.BIND_AUTO_CREATE);
 		
 		// register tab listeners
		setContentView(R.layout.main);
 		composeTab = (ImageButton) findViewById(R.id.composeTab);
 		composeTab.setOnClickListener(_composeTabListener);
 		inboxTab = (ImageButton) findViewById(R.id.inboxTab);
 		inboxTab.setOnClickListener(_inboxTabListener);
 		profileTab = (ImageButton) findViewById(R.id.profileTab);
 		profileTab.setOnClickListener(_profileTabListener);
 	}
 
 	private ServiceConnection _ServiceConnection = new ServiceConnection() {
 
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			SBLog.i(TAG, "onServiceConnected()");
 			_ServiceBinder = (SBServiceBridgeInterface) service;
 			_StateManager = _ServiceBinder.getStateManager();
			
			// initialize views
			_ViewArray = new SBView[3];
			_ViewArray[COMPOSE_VIEW] = new ComposeView(SBContext.this, "Send Shout", R.id.compose_view, 0);
			_ViewArray[INBOX_VIEW] = new InboxView(SBContext.this, "Inbox", R.id.inbox_view, 1);
			_ViewArray[PROFILE_VIEW] = new ProfileView(SBContext.this, "Profile", R.id.profile_view, 2);
			switchView(_ViewArray[COMPOSE_VIEW]);
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			// should never be called
 			SBLog.i(TAG, "onServiceDisconnected()");
 		}
 
 	};
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		SBLog.i(TAG, "onNewIntent()");
 		_NotificationManager.handleNotificationExtras(intent.getExtras());
 	}
 
 	@Override
 	protected void onStart() {
 		SBLog.i(TAG, "onStart()");
 		super.onStart();
 	}
 
 	@Override
 	protected void onResume() {
 		SBLog.i(TAG, "onResume()");
 		super.onResume();
 		/* TODO: not sure if this is needed */
 		_NotificationManager.handleNotificationExtras(getIntent().getExtras());
 	}
 
 	@Override
 	protected void onPause() {
 		SBLog.i(TAG, "onResume()");
 		super.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		SBLog.i(TAG, "onStop()");
 		super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		SBLog.i(TAG, "onDestroy()");
 		
 		// unregister Views from StateManager
 		for (SBView view: _ViewArray) {
 			view.destroy();
 		}
 		// unbind and null bound objects
 		unbindService(_ServiceConnection);
 		_StateManager = null;
 		_ServiceBinder = null;
 		
 		super.onDestroy();
 	}
 
 	/* VIEW METHODS */
 
 	public void switchView(SBView view) {
 		SBLog.i(TAG, "switchView(" + view.getName() + ")");
 		if (_CurrentView != null) {
 			_CurrentView.hide();
 		}
 		_CurrentView = view;
 		_CurrentView.show();
 	}
 	
 	public SBView getView(int viewId) {
 		return _ViewArray[viewId];
 	}
 	
 	private OnClickListener _composeTabListener = new OnClickListener() {
 		public void onClick(View v) {
 			switchView(_ViewArray[COMPOSE_VIEW]);
 		}
 	};
 	
 	private OnClickListener _inboxTabListener = new OnClickListener() {
 		public void onClick(View v) {
 			switchView(_ViewArray[INBOX_VIEW]);
 		}
 	};
 	
 	private OnClickListener _profileTabListener = new OnClickListener() {
 		public void onClick(View v) {
 			switchView(_ViewArray[PROFILE_VIEW]);
 		}
 	};
 	
 	public SBStateManager getStateManager() {
 		return _StateManager;
 	}
 }
