 package ltg.heliotablet_android;
 
 import java.util.List;
 
 import ltg.commons.LTGEvent;
 import ltg.heliotablet_android.data.Reason;
 import ltg.heliotablet_android.data.ReasonDBOpenHelper;
 import ltg.heliotablet_android.view.LoginDialog;
 import ltg.heliotablet_android.view.controller.NonSwipeableViewPager;
 import ltg.heliotablet_android.view.controller.ObservationReasonController;
 import ltg.heliotablet_android.view.controller.TheoryReasonController;
 import ltg.heliotablet_android.view.observation.ObservationFragment;
 import ltg.heliotablet_android.view.theory.TheoryFragmentWithSQLiteLoaderNestFragments;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.google.common.collect.Lists;
 
 public class MainActivity extends FragmentActivity implements TabListener {
 
 	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 	private ActionBar actionBar;
 
 	private static final String TAG = "MainActivity";
 	private Messenger activityMessenger;
 	private SharedPreferences settings = null;
 
 	private MenuItem connectMenu;
 	private MenuItem disconnectMenu;
 	private SectionsPagerAdapter mSectionsPagerAdapter;
 	private NonSwipeableViewPager mViewPager;
 	
 	private Handler handler = new Handler() {
 		public void handleMessage(Message message) {
 			Intent intent = (Intent) message.obj;
 			if (intent != null) {
 				if (intent.getAction().equals(
 						XmppService.CHAT_ACTION_RECEIVED_MESSAGE)) {
 					receiveIntent(intent);
 				} else if (intent.getAction().equals(XmppService.SHOW_LOGIN)) {
 					prepDialog().show();
 				} else if (intent.getAction().equals(XmppService.ERROR)) {
 					makeToast(intent);
 				} else if (intent.getAction().equals(
 						XmppService.LTG_EVENT_RECEIVED)) {
 					receiveIntent(intent);
 				}
 			}
 
 		};
 	};
 
 	public void receiveIntent(Intent intent) {
 		if (intent != null) {
 			LTGEvent ltgEvent = (LTGEvent) intent
 					.getSerializableExtra(XmppService.LTG_EVENT);
 
 			if (ltgEvent != null) {
 				if (ltgEvent.getPayload() != null) {
 					JsonNode payload = ltgEvent.getPayload();
 					String color = payload.get("color").textValue();
 					String anchor = payload.get("anchor").textValue();
 					String reasonText = payload.get("reason").textValue();
 					String origin = ltgEvent.getOrigin();
 					if (ltgEvent.getType().equals("new_theory")) {
 						TheoryReasonController tc = TheoryReasonController.getInstance(this);
 
 						Reason reason = new Reason(anchor, color,
 								Reason.TYPE_THEORY, origin, true);
 						reason.setReasonText(reasonText);
 						try {
 							tc.insertReason(reason);
 						} catch(NullPointerException e) {
 							Log.e(TAG,"Problem inserting new theory");
 							insertReasonManually(reason,"INSERT");
 						}
 					} else if (ltgEvent.getType().equals("new_observation")) {
 						ObservationReasonController oc = ObservationReasonController.getInstance(this);
 						Reason reason = new Reason(anchor, color,
 								Reason.TYPE_OBSERVATION, origin, true);
 						reason.setReasonText(reasonText);
 						
 						try {
 							oc.insertReason(reason);
 						} catch(NullPointerException e) {
 							Log.e(TAG,"Problem inserting new observation");
 							insertReasonManually(reason,"INSERT");
 						}
 						
 					} else if(ltgEvent.getType().equals("update_theory")) {
 						
 					} else if(ltgEvent.getType().equals("update_observation")) { 
 						ObservationReasonController oc = ObservationReasonController.getInstance(this);
 
 					}  else if(ltgEvent.getType().equals("delete_theory")) {
 						TheoryReasonController tc = TheoryReasonController.getInstance(this);
 						Reason reason = new Reason(anchor, color,
 								Reason.TYPE_THEORY, origin, true);
 						tc.deleteReasonByOriginAndType(reason);
 						
 					} else if(ltgEvent.getType().equals("delete_obervation")) {
 						TheoryReasonController tc = TheoryReasonController.getInstance(this);
 						Reason reason = new Reason(anchor, color,
 								Reason.TYPE_OBSERVATION, origin, true);
 						tc.deleteReasonByOriginAndType(reason);
 					}
 
 				}
 
 			}
 			//makeToast("LTG EVENT Received: " + ltgEvent.toString());
 
 		}
 	}
 
 	private void insertReasonManually(final Reason reason, final String op) {
 		Thread insertThread = new Thread(new Runnable() {
 
 		    public void run() {
 
 		    	ReasonDBOpenHelper rbHelper = ReasonDBOpenHelper.getInstance(MainActivity.this);
 		    	
 		    	if( op.equals("INSERT"))
 		    		rbHelper.createReason(reason);
 		    	
 
 		    }
 
 		});
 		//insertThread.run();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		hardcodedUserNameXMPP();
 		initXmppService();
 
 		initTabs();
 	}
 	
 	
 
 	
 	public void initTabs() {
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
		mViewPager = (NonSwipeableViewPager) findViewById(R.id.main_pager);
 		mViewPager.setOffscreenPageLimit(3);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 		// When swiping between different sections, select the corresponding
 		// tab. We can also use ActionBar.Tab#select() to do this if we have
 		// a reference to the Tab.
 //		mViewPager
 //				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 //					@Override
 //					public void onPageSelected(int position) {
 //						actionBar.setSelectedNavigationItem(position);
 //					}
 //				});
 
 		// For each of the sections in the app, add a tab to the action bar.
 		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
 			// Create a tab with text corresponding to the page title defined by
 			// the adapter. Also specify this Activity object, which implements
 			// the TabListener interface, as the callback (listener) for when
 			// this tab is selected.
 			actionBar.addTab(actionBar.newTab()
 					.setText(mSectionsPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 	}
 
 	public void initXmppService() {
 		// XMPP bind
 		activityMessenger = new Messenger(handler);
 		Intent intent = new Intent(MainActivity.this, XmppService.class);
 		intent.setAction(XmppService.STARTUP);
 		intent.putExtra(XmppService.ACTIVITY_MESSAGER, activityMessenger);
 		intent.putExtra(XmppService.CHAT_TYPE, XmppService.GROUP_CHAT);
 		intent.putExtra(XmppService.GROUP_CHAT_NAME,
 				getString(R.string.XMPP_CHAT_ROOM));
 		startService(intent);
 	}
 
 	public AlertDialog prepDialog() {
 		OnClickListener negative = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 			}
 		};
 
 		OnClickListener positive = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 
 				AlertDialog ad = (AlertDialog) dialog;
 
 				EditText usernameTextView = (EditText) ad
 						.findViewById(R.id.usernameTextView);
 				EditText passwordTextView = (EditText) ad
 						.findViewById(R.id.passwordTextView);
 
 				String username = org.apache.commons.lang3.StringUtils
 						.stripToNull(usernameTextView.getText().toString());
 				String password = org.apache.commons.lang3.StringUtils
 						.stripToNull(passwordTextView.getText().toString());
 
 				settings = getSharedPreferences(getString(R.string.xmpp_prefs),
 						MODE_PRIVATE);
 				SharedPreferences.Editor prefEditor = settings.edit();
 				prefEditor.putString(getString(R.string.user_name), username);
 				prefEditor.putString(getString(R.string.password), password);
 				prefEditor.commit();
 
 				Intent intent = new Intent();
 				intent.setAction(XmppService.CONNECT);
 				Message newMessage = Message.obtain();
 				newMessage.obj = intent;
 				XmppService.sendToServiceHandler(intent);
 			}
 		};
 
 		return LoginDialog.createLoginDialog(this, positive, negative, null);
 	}
 
 	public void sendXmppMessage(String text) {
 		Intent intent = new Intent();
 		intent.setAction(XmppService.SEND_MESSAGE_CHAT);
 		intent.putExtra(XmppService.MESSAGE_TEXT_CHAT, text);
 		Message newMessage = Message.obtain();
 		newMessage.obj = intent;
 		XmppService.sendToServiceHandler(intent);
 	}
 
 	private void hardcodedUserNameXMPP() {
 		settings = getSharedPreferences(getString(R.string.xmpp_prefs),
 				MODE_PRIVATE);
 		SharedPreferences.Editor prefEditor = settings.edit();
 		// prefEditor.clear();
 		// prefEditor.commit();
 		prefEditor.putString(getString(R.string.user_name), "obama");
 		prefEditor.putString(getString(R.string.password), "obama");
 		prefEditor.putString(getString(R.string.XMPP_HOST_KEY),
 				getString(R.string.xmpp_host));
 		prefEditor.putInt(getString(R.string.XMPP_PORT), 5222);
 		prefEditor.commit();
 	}
 
 	public boolean shouldShowDialog() {
 
 		settings = getSharedPreferences(getString(R.string.xmpp_prefs),
 				MODE_PRIVATE);
 		String storedUserName = settings.getString(
 				getString(R.string.user_name), "");
 		String storedPassword = settings.getString(
 				getString(R.string.password), "");
 
 		if (storedPassword != null && storedUserName != null) {
 			return false;
 		}
 
 		return true;
 
 	}
 
 	public void makeToast(Intent intent) {
 		if (intent != null) {
 			String stringExtra = intent
 					.getStringExtra(XmppService.XMPP_MESSAGE);
 			makeToast(stringExtra);
 		}
 	}
 
 	public void makeToast(String toastText) {
 		Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
 		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 50);
 		toast.show();
 	}
 
 
 
 
 	private void setupTabs() {
 		// TODO Auto-generated method stub
 
 		// Theories tab
 
 		Tab tab = actionBar
 				.newTab()
 				.setText(R.string.tab_title_theories)
 				.setTabListener(this);
 		actionBar.addTab(tab);
 
 		tab = actionBar
 				.newTab()
 				.setText(R.string.tab_title_observations)
 				.setTabListener(this);
 
 		actionBar.addTab(tab);
 		tab = actionBar
 				.newTab()
 				.setText(R.string.tab_title_scratch_pad)
 				.setTabListener(this);
 
 		actionBar.addTab(tab);
 
 	}
 	
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		mViewPager.setCurrentItem(tab.getPosition());
 		
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		private List<Fragment> fragments = null;
 		
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 			fragments =  Lists.newArrayList(new TheoryFragmentWithSQLiteLoaderNestFragments(), new ObservationFragment(), new ScratchPadFragment());
 		}
 
 		@Override
 		public android.support.v4.app.Fragment getItem(int position) {
 			return fragments.get(position);
 		}
 		
 
 		@Override
 		public int getCount() {
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.tab_title_theories);
 			case 1:
 				return getString(R.string.tab_title_observations);
 			case 2:
 				return getString(R.string.tab_title_scratch_pad);
 			}
 			return null;
 		}
 	}
 	
 
 
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		// Restore the previously serialized current tab position.
 		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
 			getActionBar().setSelectedNavigationItem(
 					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// Serialize the current tab position.
 		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
 				.getSelectedNavigationIndex());
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 
 		connectMenu = menu.getItem(0);
 		disconnectMenu = menu.getItem(1);
 		disconnectMenu.setEnabled(false);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_connect:
 			prepDialog().show();
 			connectMenu.setEnabled(false);
 			disconnectMenu.setEnabled(true);
 			return true;
 		case R.id.menu_disconnect:
 			connectMenu.setEnabled(true);
 			disconnectMenu.setEnabled(false);
 			Intent intent = new Intent();
 			intent.setAction(XmppService.DISCONNECT);
 			Message newMessage = Message.obtain();
 			newMessage.obj = intent;
 			XmppService.sendToServiceHandler(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 
 }
