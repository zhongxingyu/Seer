 package wei.mark.tabletennis;
 
 import wei.mark.tabletennis.TableTennisRatings.Navigation;
 import wei.mark.tabletennis.util.Debuggable;
import wei.mark.tabletennis.util.ProviderSearchTask;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ScrollView;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 public class ActivityTabbedPlayerList extends TabActivity implements Debuggable {
 	TableTennisRatings app;
 	boolean mDebuggable;
 
 	TextView debugTextView;
 	ScrollView debugScrollView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		app = (TableTennisRatings) getApplication();
 
 		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			finish();
 			return;
 		}
 
 		setContentView(R.layout.activity_tabbed_player_list);
 
 		TabHost tabHost = getTabHost();
 		TabHost.TabSpec spec;
 		Intent intent;
 
 		intent = new Intent().setClass(this, ActivityPlayerList.class)
 				.putExtras(getIntent().getExtras())
 				.putExtra("provider", "usatt");
 		spec = tabHost
 				.newTabSpec("usatt")
 				.setIndicator(null,
 						getResources().getDrawable(R.drawable.usatt_selector))
 				.setContent(intent);
 		tabHost.addTab(spec);
 
 		intent = new Intent().setClass(this, ActivityPlayerList.class)
 				.putExtras(getIntent().getExtras()).putExtra("provider", "rc");
 		spec = tabHost
 				.newTabSpec("rc")
 				.setIndicator(null,
 						getResources().getDrawable(R.drawable.rc_selector))
 				.setContent(intent);
 		tabHost.addTab(spec);
 
 		tabHost.setCurrentTab(0); // TODO set to saved tab
 
 		mDebuggable = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
 		mDebuggable = false;
 		if (mDebuggable) {
 			findViewById(R.id.debug_stub).setVisibility(View.VISIBLE);
 			debugTextView = (TextView) findViewById(R.id.debug);
 			debugScrollView = (ScrollView) findViewById(R.id.debug_scroll);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		if (mDebuggable) {
 			debugTextView.setText(app.CurrentDebugMessage);
 			debugScrollView.post(new Runnable() {
 
 				@Override
 				public void run() {
 					debugScrollView.fullScroll(ScrollView.FOCUS_DOWN);
 				}
 			});
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		app.CurrentNavigation = Navigation.IDLE;
 		debug("Current navigation is now " + app.CurrentNavigation.toString());
 	}
 
 	@Override
 	public void debug(String msg) {
 		if (mDebuggable) {
 			app.CurrentDebugMessage = app.CurrentDebugMessage + "\n" + msg;
 
 			debugTextView.post(new Runnable() {
 
 				@Override
 				public void run() {
 					debugTextView.setText(app.CurrentDebugMessage);
 
 					debugScrollView.post(new Runnable() {
 
 						@Override
 						public void run() {
 							debugScrollView.fullScroll(ScrollView.FOCUS_DOWN);
 						}
 					});
 				}
 			});
 		}
 	}
 }
