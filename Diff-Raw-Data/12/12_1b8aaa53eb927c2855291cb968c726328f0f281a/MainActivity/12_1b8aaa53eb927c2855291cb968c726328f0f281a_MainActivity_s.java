 package uk.co.mpkdashx.CBTT;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class MainActivity.
  * 
  * @author Martin Kemp <martin.kemp@capgemini.com>
  * @version 1.1
  * @since 10/05/2012
  */
 
 public class MainActivity extends SherlockFragmentActivity {
 	public static final String PREFS_NAME = "CBTTPrefsFile";
 	ViewPager mViewPager;
 	TabsAdapter mTabsAdapter;
 	static ProgressDialog mProgressDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
 		super.onCreate(savedInstanceState);
 
 		mProgressDialog = new ProgressDialog(MainActivity.this);
 		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		mProgressDialog.setMessage("Downloading...");
 		mProgressDialog.setIndeterminate(false);
 		mProgressDialog.setMax(100);
 		mProgressDialog.setCancelable(false);
 
 		mViewPager = new ViewPager(this);
 		mViewPager.setId(R.id.pager);
 		setContentView(mViewPager);
 		ActionBar bar = getSupportActionBar();
 		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		bar.setDisplayShowTitleEnabled(true);
 
 		mTabsAdapter = new TabsAdapter(this, mViewPager);
 		// mTabsAdapter.addTab(bar.newTab().setText("home"),
 		// HomeFragment.class, null);
 		mTabsAdapter.addTab(bar.newTab().setText("morning"),
 				MorningFragment.class, null);
 		mTabsAdapter.addTab(bar.newTab().setText("midday"),
 				MiddayFragment.class, null);
 		mTabsAdapter.addTab(bar.newTab().setText("afternoon"),
 				AfternoonFragment.class, null);
 
 		new checkForUpdateTask()
 				.execute("https://dl.dropbox.com/u/6138999/CBTTver.txt");
 	}
 
 	public class checkForUpdateTask extends AsyncTask<String, Void, Void> {
 
 		protected Void doInBackground(String... urls) {
 			String serverVersion = null;
 			String versionName = null;
 			InputStream is = null;
 			try {
 				is = new URL(urls[0]).openStream();
 				BufferedReader in = new BufferedReader(
 						new InputStreamReader(is));
 				serverVersion = in.readLine();
 				in.close();
 				// We need an Editor object to make preference changes.
 				// All objects are from android.context.Context
 				SharedPreferences settings = getSharedPreferences(PREFS_NAME,
 						MODE_PRIVATE);
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putString("serverVersion", serverVersion.toString());
 				editor.commit();
 			} catch (Exception e) {
 				CharSequence text = "Couldn't get latest version info, please try again later";
 				int duration = Toast.LENGTH_LONG;
 				Toast updateErrorToast = Toast.makeText(
 						getApplicationContext(), text, duration);
 				updateErrorToast.show();
 				Log.e("Error Saving Server Version Name: ", e.getMessage(), e);
 			}
 			try {
 				versionName = getPackageManager().getPackageInfo(
 						getPackageName(), 0).versionName;
 			} catch (Exception e) {
 				Log.e("Error Getting Current Version Name: ", e.getMessage(), e);
 			}
 
 			if (serverVersion.equals(versionName)) {
 			} else {
 				DialogFragment newFragment = UpdateDialog.newInstance(0);
 				newFragment.show(getSupportFragmentManager(), "title");
 			}
 			return null;
 		}
 	}
 
 	public static class UpdateDialog extends DialogFragment {
 		static UpdateDialog newInstance(int title) {
 			UpdateDialog frag = new UpdateDialog();
 			Bundle args = new Bundle();
 			args.putInt("Update Available", title);
 			frag.setArguments(args);
 			return frag;
 
 		}
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			return new AlertDialog.Builder(getActivity())
 					.setMessage(
 							"A new update is available, do you want to update?")
 					.setPositiveButton("Yes",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 
 									SharedPreferences settings = getActivity()
 											.getSharedPreferences(PREFS_NAME, 0);
 									String theServerVersion = settings
 											.getString("serverVersion", null);
 									try {
 										StartDownloadUpdate sDownloadUpdate = new StartDownloadUpdate();
 
 										Uri.Builder b = Uri.parse(
 												"http://dl.dropbox.com/u/6138999/CapgeminiBusTimetable_"
 														+ theServerVersion
 														+ ".apk").buildUpon();
 
 										String url = b.build().toString();
 
 										sDownloadUpdate.applicationContext = (MyApplication) getActivity()
 												.getApplicationContext();
 
 										sDownloadUpdate.execute(url,
 												theServerVersion);
 
 									} catch (Exception e) {
 										CharSequence text = "Couldn't download latest version , please try again later";
 										int duration = Toast.LENGTH_LONG;
 										Toast updateErrorToast = Toast
 												.makeText(
 														getActivity()
 																.getApplicationContext(),
 														text, duration);
 										updateErrorToast.show();
 										dialog.cancel();
 										Log.e("Error Downloading Latest Version: ",
 												e.getMessage(), e);
 									}
 
 								}
 							})
 					.setNegativeButton("No",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.cancel();
 								}
 							}).create();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.cancel_alarm:
 			
 			Context ctx = getApplicationContext();
 
 			Intent intent = new Intent(ctx, AlarmReceiver.class);
 
 			PendingIntent pIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
 			
 			AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
 			
 			alarmManager.cancel(pIntent);
 			
 			int duration = Toast.LENGTH_LONG;
 			Toast cancelAlarmToast = Toast
 					.makeText(ctx,
 							"Cancelled Alarm", duration);
 			cancelAlarmToast.show();
 			
 			return true;
 			
 		case R.id.exit:
 			finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 	}
 
 	public static class TabsAdapter extends FragmentPagerAdapter implements
 			ActionBar.TabListener, ViewPager.OnPageChangeListener {
 		private final Context mContext;
 		private final ActionBar mActionBar;
 		private final ViewPager mViewPager;
 		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 
 		static final class TabInfo {
 			private final Class<?> clss;
 			private final Bundle args;
 
 			TabInfo(Class<?> _class, Bundle _args) {
 				clss = _class;
 				args = _args;
 			}
 		}
 
 		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
 			super(activity.getSupportFragmentManager());
 			mContext = activity;
 			mActionBar = activity.getSupportActionBar();
 			mViewPager = pager;
 			mViewPager.setAdapter(this);
 			mViewPager.setOnPageChangeListener(this);
 		}
 
 		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
 			TabInfo info = new TabInfo(clss, args);
 			tab.setTag(info);
 			tab.setTabListener(this);
 			mTabs.add(info);
 			mActionBar.addTab(tab);
 			notifyDataSetChanged();
 		}
 
 		public int getCount() {
 			return mTabs.size();
 		}
 
 		public SherlockFragment getItem(int position) {
 			TabInfo info = mTabs.get(position);
 			return (SherlockFragment) Fragment.instantiate(mContext,
 					info.clss.getName(), info.args);
 		}
 
 		public void onPageScrolled(int position, float positionOffset,
 				int positionOffsetPixels) {
 		}
 
 		public void onPageSelected(int position) {
 			mActionBar.setSelectedNavigationItem(position);
 		}
 
 		public void onPageScrollStateChanged(int state) {
 		}
 
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			mViewPager.setCurrentItem(tab.getPosition());
 			// Log.v(TAG, "clicked");
 			Object tag = tab.getTag();
 			for (int i = 0; i < mTabs.size(); i++) {
 				if (mTabs.get(i) == tag) {
 					mViewPager.setCurrentItem(i);
 				}
 			}
 		}
 
 		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		}
 
 		public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		}
 
 		public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
 		}
 
 		public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
 		}
 	}
 
 	public static class TimePickerFragment extends SherlockDialogFragment
 			implements TimePickerDialog.OnTimeSetListener {
 		int mH;
 		int mM;
 
 		static TimePickerFragment newInstance(int H, int M) {
 			TimePickerFragment f = new TimePickerFragment();
 
 			Bundle args = new Bundle();
 			args.putInt("H", H);
 			args.putInt("M", M);
 			f.setArguments(args);
 
 			return f;
 		}
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			mH = getArguments().getInt("H");
 			mM = getArguments().getInt("M");
 
 			return new TimePickerDialog(getActivity(), this, mH, mM,
 					DateFormat.is24HourFormat(getActivity()));
 		}
 
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 
 			Context ctx = getSherlockActivity().getApplicationContext();
 
 			Intent intent = new Intent(ctx, AlarmReceiver.class);
 
 			PendingIntent pIntent = PendingIntent.getBroadcast(ctx, 0, intent,
 					0);
 
 			Calendar calendar = Calendar.getInstance();
 			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			calendar.set(Calendar.MINUTE, minute);
 			calendar.set(Calendar.SECOND, 00);
 			AlarmManager alarmManager = (AlarmManager) getSherlockActivity()
 					.getSystemService(ALARM_SERVICE);
 			alarmManager.set(AlarmManager.RTC_WAKEUP,
 					calendar.getTimeInMillis(), pIntent);
 
 		}
 	}
 }
