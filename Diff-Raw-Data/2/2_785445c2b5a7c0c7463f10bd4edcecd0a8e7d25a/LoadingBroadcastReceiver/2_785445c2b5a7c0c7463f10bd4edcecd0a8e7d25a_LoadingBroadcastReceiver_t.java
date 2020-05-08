 package org.geekosphere.zeitgeist.broadcastreceiver;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.support.v4.content.LocalBroadcastManager;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public final class LoadingBroadcastReceiver extends BroadcastReceiver {
 	public static final String INTENT_ACTION_LOADING = "org.geekosphere.zeitgeist.activity.ZGActivity.INTENT_EXTRA_LOADING";
 	public static final String INTENT_ACTION_LOADING_DONE = "org.geekosphere.zeitgeist.activity.ZGActivity.INTENT_EXTRA_LOADING_DONE";
 
 	public static LoadingBroadcastReceiver INSTANCE;
 
 	private SherlockFragmentActivity activity;
 	private AtomicInteger counter = new AtomicInteger(0);
 
	public static final synchronized LoadingBroadcastReceiver getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new LoadingBroadcastReceiver();
 		}
 		return INSTANCE;
 	}
 
 	private LoadingBroadcastReceiver() {
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if (activity == null) {
 			return;
 		}
 		String action = intent.getAction();
 		if (action.equals(INTENT_ACTION_LOADING)) {
 			if (counter.get() == 0) {
 				activity.setSupportProgressBarIndeterminateVisibility(true);
 			}
 			counter.incrementAndGet();
 		} else {
 			if (counter.get() == 0) {
 				activity.setSupportProgressBarIndeterminateVisibility(false);
 			}
 			counter.decrementAndGet();
 		}
 	}
 
 	public void setActivity(SherlockFragmentActivity activity) {
 		this.activity = activity;
 	}
 
 	public final void registerReceiver(Context c) {
 		LocalBroadcastManager.getInstance(c).registerReceiver(this, new IntentFilter(LoadingBroadcastReceiver.INTENT_ACTION_LOADING));
 		LocalBroadcastManager.getInstance(c).registerReceiver(this, new IntentFilter(LoadingBroadcastReceiver.INTENT_ACTION_LOADING_DONE));
 	}
 
 	public final void unregisterReceiver(Context c) {
 		LocalBroadcastManager.getInstance(c).unregisterReceiver(this);
 	}
 }
