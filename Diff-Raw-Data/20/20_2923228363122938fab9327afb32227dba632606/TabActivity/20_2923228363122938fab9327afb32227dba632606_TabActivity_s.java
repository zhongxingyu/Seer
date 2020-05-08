 package com.phunware.alerts.sample;
 
 import java.util.HashMap;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.Toast;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabContentFactory;
 
 import com.phunware.alerts.PwAlertsRegister;
 import com.phunware.alerts.sample.ConsoleOutput.ConsoleLogger;
 import com.phunware.core.PwCoreSession;
 
 public class TabActivity extends FragmentActivity implements OnTabChangeListener, ConsoleLogger{
 
 	private static final String TAG = "TabActivity";
 	/**
 	 * Placeholder for console output
 	 */
 	private ConsoleOutput mConsoleOutput;
 	
 	private TabHost mTabHost;
     private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, TabInfo>();
     private TabInfo mLastTab = null;
 
     @SuppressWarnings("rawtypes")
     private class TabInfo {
          private String tag;
 		private Class clss;
          private Bundle args;
          private Fragment fragment;
          TabInfo(String tag, Class clazz, Bundle args) {
              this.tag = tag;
              this.clss = clazz;
              this.args = args;
          }
  
     }
  
     class TabFactory implements TabContentFactory {
  
         private final Context mContext;
  
         /**
          * @param context
          */
         public TabFactory(Context context) {
             mContext = context;
         }
  
         /** (non-Javadoc)
          * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
          */
         public View createTabContent(String tag) {
             View v = new View(mContext);
             v.setMinimumWidth(0);
             v.setMinimumHeight(0);
             return v;
         }
  
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_tabs);
         
         mConsoleOutput = new ConsoleOutput(savedInstanceState);
         
         //Start the session here
     	PwCoreSession.getInstance().activityStartSession(this);
     	mConsoleOutput.appendToConsole("Is the device registered for alerts? "+( PwAlertsRegister.hasRegistered(this) ? "Yes!" : "No, register with the menu button"));
         
         IntentFilter filter = new IntentFilter();
         filter.addAction(Utils.ACTION_ON_REGISTERED);
         filter.addAction(Utils.ACTION_ON_UNREGISTERED);
         LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, filter);
         
         Bundle args = new Bundle();
         initialiseTabHost(args);
         if (savedInstanceState != null) {
             mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
         }
         
        // Display Push Message
         //This will also flush to the console
         handlePushResult(getIntent());
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
     	mConsoleOutput.saveInstanceState(outState);
     	outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
     	super.onSaveInstanceState(outState);
     }
     
     @Override
     protected void onStop() {
     	super.onStop();
     	PwCoreSession.getInstance().activityStopSession(this);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	getMenuInflater().inflate(R.menu.register, menu);
     	return super.onCreateOptionsMenu(menu);
     }
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int id = item.getItemId();
 		
 		if(id == R.id.menu_register){
 			PwAlertsRegister.register(this);
 			return true;
 		}
 		
 		else if(id == R.id.menu_unregister){
 			PwAlertsRegister.unregister(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
     private void initialiseTabHost(Bundle args) {
         mTabHost = (TabHost)findViewById(android.R.id.tabhost);
         mTabHost.setup();
         TabInfo tabInfo = null;
         Resources r = getResources();
         TabActivity.addTab(
         		this, this.mTabHost, 
         		this.mTabHost.newTabSpec(InfoFragment.TAG)
         			.setIndicator(r.getString(R.string.title_info), r.getDrawable(R.drawable.ic_tab_info)),
         		( tabInfo = new TabInfo(InfoFragment.TAG, InfoFragment.class, args)));
         this.mapTabInfo.put(tabInfo.tag, tabInfo);
         TabActivity.addTab(
         		this, this.mTabHost, 
         		this.mTabHost.newTabSpec(SubscriptionsFragment.TAG)
         			.setIndicator(r.getString(R.string.title_subscriptions), r.getDrawable(R.drawable.ic_tab_subscriptions)),
         		( tabInfo = new TabInfo(SubscriptionsFragment.TAG, SubscriptionsFragment.class, args)));
         this.mapTabInfo.put(tabInfo.tag, tabInfo);
         TabActivity.addTab(
         		this, this.mTabHost, 
         		this.mTabHost.newTabSpec(ConsoleFragment.TAG)
         			.setIndicator(r.getString(R.string.title_console), r.getDrawable(R.drawable.ic_tab_console)),
         		( tabInfo = new TabInfo(ConsoleFragment.TAG, ConsoleFragment.class, args)));
         this.mapTabInfo.put(tabInfo.tag, tabInfo);
         // Default to first tab
         this.onTabChanged(InfoFragment.TAG);
         //
         mTabHost.setOnTabChangedListener(this);
     }
  
     /**
      * @param activity
      * @param tabHost
      * @param tabSpec
      * @param clss
      * @param args
      */
     private static void addTab(TabActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
         // Attach a Tab view factory to the spec
         tabSpec.setContent(activity.new TabFactory(activity));
         String tag = tabSpec.getTag();
  
         // Check to see if we already have a fragment for this tab, probably
         // from a previously saved state.  If so, deactivate it, because our
         // initial state is that a tab isn't shown.
         tabInfo.fragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
         if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) {
             FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
             ft.detach(tabInfo.fragment);
             ft.commit();
             activity.getSupportFragmentManager().executePendingTransactions();
         }
  
         tabHost.addTab(tabSpec);
     }
  
     /** (non-Javadoc)
      * @see android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
      */
     public void onTabChanged(String tag) {
         TabInfo newTab = this.mapTabInfo.get(tag);
         if (mLastTab != newTab) {
             FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
             if (mLastTab != null) {
                 if (mLastTab.fragment != null) {
                     ft.detach(mLastTab.fragment);
                 }
             }
             if (newTab != null) {
                 if (newTab.fragment == null) {
                     newTab.fragment = Fragment.instantiate(this,
                             newTab.clss.getName(), newTab.args);
                     ft.add(R.id.realtabcontent, newTab.fragment, newTab.tag);
                 } else {
                     ft.attach(newTab.fragment);
                 }
             }
      
             mLastTab = newTab;
             ft.commit();
             this.getSupportFragmentManager().executePendingTransactions();
         }
         flushToConsole();
     }
 
 	@Override
 	public void clearConsole() {
 		mConsoleOutput.clearConsole();
 	}
 
 	/**
 	 * Push the cached text to the console if it is available
 	 */
 	public void flushToConsole()
 	{
 		ConsoleFragment frag = (ConsoleFragment)getSupportFragmentManager().findFragmentByTag(ConsoleFragment.TAG);
 		if(frag != null)
 		{
 			frag.setConsoleText(mConsoleOutput.getConsole());
 		}
 	}
 	
 	/*
 	 * Listen to messages being broadcasted by the alert intent service. This is
 	 * optional, mostly for demonstration.
 	 */
 	private BroadcastReceiver localReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if(action.equals(Utils.ACTION_ON_REGISTERED) || action.equals(Utils.ACTION_ON_UNREGISTERED))
 			{
 				String errMessage = intent.getStringExtra(Utils.BROADCAST_MESSAGE_KEY);
 				boolean isSuccessful = intent.getBooleanExtra(Utils.BROADCAST_SUCCESSFUL_KEY, false);
 				String msg = action+"\n"+ (isSuccessful ? "" : "NOT") + " Successful!";
 				if(!isSuccessful)
 					msg += "\n"+ errMessage;
 				mConsoleOutput.appendToConsole(msg);
 				flushToConsole();
 				
 				//refresh info fragment
 				InfoFragment frag = (InfoFragment) getSupportFragmentManager().findFragmentByTag(InfoFragment.TAG);
 				if (isSuccessful && action.equals(Utils.ACTION_ON_REGISTERED) && frag != null && frag.isVisible()) {
 					Toast.makeText(TabActivity.this, R.string.refreshing_after_register, Toast.LENGTH_SHORT).show();
 					frag.refreshInfo();
 				} else {
 					Toast.makeText(TabActivity.this, msg, Toast.LENGTH_SHORT).show();
 				}
 			}
 			
 		}
 	};
 
 	/*
 	 * 
 	 * The following code is for handling a push notification
 	 * 
 	 * 
 	 */
 	// Update the UI with new data string.
 	/**
 	 * Update the console with data obtained from a push notification.
 	 * This does nothing if there are no {@link Intent} extras.
 	 * AKA this was activity was not started by a push.
 	 * @param intent
 	 */
 	public void handlePushResult(Intent intent) {
 		Bundle bundle = intent.getExtras();
 		if(bundle == null)
 		{
 			Log.i(TAG, "updateResult returning, null intent extras");
 			return;
 		}
 		
 		String extras = bundle.getString(Utils.INTENT_ALERT_EXTRA);
 		String pid = bundle.getString(Utils.INTENT_ALERT_EXTRA_PID);
 		// String extrasStr = printAlertExtras(extras);
 		String extrasStr = extras;
 		String data = bundle.getString(Utils.INTENT_ALERT_DATA);
 		
 		mTabHost.setCurrentTabByTag(ConsoleFragment.TAG);
 		//this.onTabChanged(ConsoleFragment.TAG);
 
 		// Show what's information is included in the alert message.
 		
 		String text = "Alert Extras:\n" + extrasStr
 				+ "\nData:\n" + data;
 		mConsoleOutput.appendToConsole(text);
 		flushToConsole();
 
 		// You will need to initialize PRAISEAlertsDataReceiver. What this
 		// does is start a intent service to fetch the alert's payload and send
 		// a positive click to the PRAISE server for analytics.
 		PwAlertsRegister.sendPositiveClick(getApplicationContext(), pid);
 	}
 
 	/*
 	 * Added this to your code. This will allow the app to retrieve new alerts
 	 * while the activity is onPause.
 	 */
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		Log.d(TAG, " ON NEW INTENT");
 		handlePushResult(intent);
 	}
 
 	@Override
 	public void printToConsole(String text) {
 		mConsoleOutput.appendToConsole(text);
 		flushToConsole();
 	}
 }
