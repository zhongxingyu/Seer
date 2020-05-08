 package fr.mabreizh.droid;
 
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bostonandroid.datepreference.DatePreference;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import fr.mabreizh.droid.tools.TimeTools;
 
 public class MainScreen extends RoboActivity implements OnItemClickListener{
 	
 	private static final String TAG = MainScreen.class.getSimpleName();
 	
 	private static final String KEY_PCT = "keyPct";
 	private static final String KEY_DATE = "keyDate";
 	private static final String KEY_ETA = "keyEta";
 	
 	private static final String[] PREFS_UNITS_KEY = 
 	{
 		"millisecond_enable",
 		"second_enable",
 		"minute_enable",
 		"hour_enable",
 		"day_enable",
 		"week_enable",
 		"month_enable",
 		"year_enable"
 	};
 	
 	private static final String[] froms = new String[]{KEY_PCT, KEY_DATE, KEY_ETA};
 	private static final int[]    tos   = new int[]{R.id.item_pct, R.id.item_date, R.id.item_eta};
 	
 	private static final DecimalFormat df = new DecimalFormat("@@##%");
 	private static final DateFormat longFormat = DateFormat.getDateTimeInstance();
 	
 	private static boolean shortUnits = TimeTools.SMALL_DEF;
 	private static boolean[] selectedUnits = TimeTools.ALL;
 	
 	private static final double[] intermeds = new double[]{.05,.1,.15,.2,.25,.3,.3333,.35,.4,.45,.5,.55,.6,.6666,.7,.75,.8,.85,.9,.95,.97,.98,.99};
 	private static final int[] intermeds2 = new int[]{1,2,5,7,10};
 	private static final int[] intermeds3 = new int[]{14, 19};
 	
 	private static final int MSG_REFRESH = 1;
 	
 	private Handler mHandler = new Handler()
 	{
 		public void handleMessage(Message msg) 
 		{
 			switch (msg.what) {
 			case MSG_REFRESH:
 				@SuppressWarnings("unchecked")
 				List<Map<String, String>> list = (List<Map<String, String>>) msg.obj;
 				values.clear();
 				values.addAll(list);
 				adapter.notifyDataSetChanged();
 				break;
 
 			default:
 				break;
 			}
 		}
 	};
 	
 	@InjectView(R.id.intermediate) private ListView intermList;
 	@InjectView(R.id.fromTo) private TextView fromTo;
 	private AsyncTask<Long, PctWrapper, Void> task;
 	private BaseAdapter adapter;
 	private List<Map<String, ?>> values;
 	private long start = -1;
 	private long end = -1;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         values = new ArrayList<Map<String,?>>();
         adapter = new SimpleAdapter(getApplicationContext(), values, R.layout.list_item, froms, tos);
         intermList.setAdapter(adapter);
         intermList.setOnItemClickListener(this);        
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	setDisplayFormat();
     	setStartStop();
     	fromTo.setText(longFormat.format(new Date(start)) + " - " + longFormat.format(new Date(end)));
 		task = new TimeAsyncTask(mHandler, getApplicationContext()).execute(new Long[]{start, end});
     }
     
     @Override
     protected void onPause() {
     	if(task != null) task.cancel(true);
     	super.onPause();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	menu.add(Menu.NONE, R.string.menu_settings, Menu.NONE, R.string.menu_settings);
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	if(item.getItemId() == R.string.menu_settings)
     	{
     		startActivityForResult(new Intent(this, SettingsActivity.class), R.string.menu_settings);
     		return true;
     	}
     	return false;
     }
     
     private void setStartStop()
     {
     	start = -1;
     	end = -1;
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
     	Intent intent = getIntent();
         if(intent != null && intent.getExtras() != null)
         {
         	start = intent.getExtras().getLong("start", -1);
         	end = intent.getExtras().getLong("end", -1);
         }
         SimpleDateFormat dpFormatter = DatePreference.formatter();
         long now = System.currentTimeMillis();
         long aWeek = now + (5 * 24 * 60 * 60 * 1000); 
         if(start == -1)
         {
         	String sNow = dpFormatter.format(new Date(now));
         	String sStart = prefs.getString(SettingsActivity.START, sNow);
         	String sStartHour = prefs.getString(SettingsActivity.START_HOUR, "09:00");
         	try {
 				start = dpFormatter.parse(sStart).getTime() + parseHour(sStartHour);
 			} catch (ParseException e) {
 				Log.e(TAG, e.getMessage(), e);
 			}
         }
         if(end == -1)
         {
         	String sWeek = dpFormatter.format(new Date(aWeek));
         	String sEnd = prefs.getString(SettingsActivity.END, sWeek);
         	String sEndHour = prefs.getString(SettingsActivity.END_HOUR, "19:00");
         	try {
 				end = dpFormatter.parse(sEnd).getTime() + parseHour(sEndHour);
 			} catch (ParseException e) {
 				Log.e(TAG, e.getMessage(), e);
 			}
         }
     }
     
     private long parseHour(String temps) throws ParseException
     {
     	long tps = 0;
     	int sep = temps.indexOf(':');
     	if(sep < 0) throw new ParseException("':' is missing", 0);
     	String hour = temps.substring(0, sep);
     	if(TextUtils.isEmpty(hour)) throw new ParseException("no hour specified", 0);
     	if(!TextUtils.isDigitsOnly(hour)) throw new ParseException("hour is not a number", 0);
     	String minute = temps.substring(sep+1);
     	if(TextUtils.isEmpty(minute)) throw new ParseException("no minute specified", sep);
     	if(!TextUtils.isDigitsOnly(minute)) throw new ParseException("minute is not a number", sep);
     	int h = Integer.parseInt(hour);
     	int m = Integer.parseInt(minute);
     	tps = h * 3600000 + m * 60000;
     	return tps;
     }
     
     private void setDisplayFormat()
     {
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
     	shortUnits = prefs.getBoolean("short", false);
     	for(int i = 0; i < PREFS_UNITS_KEY.length; i++)
     	{
     		selectedUnits[i] = prefs.getBoolean(PREFS_UNITS_KEY[i], false);
     	}
     }
     
     private static class TimeAsyncTask extends AsyncTask<Long, PctWrapper, Void>
     {
     	
     	private Handler mHandler;
     	private Context mContext;
     	private long diff;
     	
     	public TimeAsyncTask(Handler handler, Context context) {
 			mHandler = handler;
 			mContext = context;
 		}
 
 		@Override
 		protected Void doInBackground(Long... dates) {
 			if(dates.length != 2)
 				return null;
 			Comparator<PctWrapper> comp = new Comparator<PctWrapper>() {
 				
 				public int compare(PctWrapper object1, PctWrapper object2) {
 					if(object1 == null) return -1;
 					if(object2 == null) return 1;
					String pct1 = object1.get(KEY_PCT);
					String pct2 = object2.get(KEY_PCT);
					Double d1 = 0d;
					Double d2 = 0d;
					try {
						d1 = df.parse(pct1).doubleValue();
						d2 = df.parse(pct2).doubleValue();
					} catch (ParseException e) {}
					return -d1.compareTo(d2);
 				}
 			};
 			long start = dates[0].longValue();
 			long end = dates[1].longValue();
 			long now = System.currentTimeMillis();
 			diff = end - now;
 			while(!isCancelled() && diff > 0)
 			{
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					Log.e(TAG, e.getMessage(), e);
 				}
 				PctWrapper[] pws = new PctWrapper[intermeds.length + intermeds2.length + intermeds3.length + 1];
 				now = System.currentTimeMillis();
 				diff = end - now;
 				long longdiff = end - start;
 				double pct = (1d * diff) / longdiff;
 				pws[0] = new PctWrapper();
 				pws[0].put(KEY_PCT, df.format(pct));
 				pws[0].put(KEY_DATE, longFormat.format(new Date(now)));
 				pws[0].put(KEY_ETA, TimeTools.formatRemaining(mContext, diff, selectedUnits, shortUnits));
 				int k = 0;
 				for(int i = 0; i < intermeds.length; i++, k++)
 				{
 					double d = 1d - intermeds[i];
 					if(d < pct)
 					{
 						PctWrapper pw = new PctWrapper();
 						pw.put(KEY_PCT, df.format(d));
 						long date = (long) (end - (d * longdiff));
 						pw.put(KEY_DATE, longFormat.format(new Date(date)));
 						long eta = date - now; 
 						pw.put(KEY_ETA, TimeTools.formatRemaining(mContext, eta, selectedUnits, shortUnits));
 						pws[k + 1] = pw;
 					}
 				}
 				for(int j = 0; j < intermeds2.length; j++, k++)
 				{
 					int d = intermeds2[j];
 					long toMs = d * 24 * 3600000;
 					if(toMs < diff)
 					{
 						PctWrapper pw = new PctWrapper();
 						long date = now + toMs;
 						double pct2 = (1d * (end - date)) / longdiff;
 						pw.put(KEY_PCT, df.format(pct2));
 						pw.put(KEY_DATE, longFormat.format(new Date(date)));
 						pw.put(KEY_ETA, TimeTools.formatRemaining(mContext, toMs, selectedUnits, shortUnits));
 						pws[k + 1] = pw;
 					}
 				}
 				Calendar cal = Calendar.getInstance();
 				for(int l = 0; l < intermeds3.length; l++, k++)
 				{
 					cal.setTimeInMillis(now);
 					int h = intermeds3[l];
 					cal.set(Calendar.HOUR_OF_DAY, h);
 					cal.set(Calendar.MINUTE, 0);
 					cal.set(Calendar.SECOND, 0);
 					cal.set(Calendar.MILLISECOND, 0);
 					long date = cal.getTimeInMillis();
 					if(date > now)
 					{
 						long diff = date - now;
 						PctWrapper pw = new PctWrapper();
 						double pct2 = (1d * (end - date)) / longdiff;
 						pw.put(KEY_PCT, df.format(pct2));
 						pw.put(KEY_DATE, longFormat.format(new Date(date)));
 						pw.put(KEY_ETA, TimeTools.formatRemaining(mContext, diff, selectedUnits, shortUnits));
 						pws[k + 1] = pw;
 					}
 				}
 				Arrays.sort(pws, comp);
 				publishProgress(pws);
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onProgressUpdate(PctWrapper... values) {
 			List<Map<String, String>> list = new ArrayList<Map<String,String>>();
 			for(PctWrapper pw : values)
 			{
 				if(pw != null)
 				{
 					list.add(pw);
 				}
 			}
 			if(list.size() > 0)
 			{
 				mHandler.obtainMessage(MSG_REFRESH, list).sendToTarget();
 			}
 		}
     	
     }
     
     private static class PctWrapper extends HashMap<String, String>
     {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1442576754531829062L;
 
     }
 
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		PctWrapper pw = (PctWrapper) parent.getItemAtPosition(position);
 		String date = pw.get(KEY_DATE);
 		long end = -1;
 		try {
 			end = longFormat.parse(date).getTime();
 		} catch (ParseException e) {
 			Log.e(TAG, e.getMessage(), e);
 		}
 		Intent i = new Intent(this, getClass());
 		i.putExtra("start", start);
 		i.putExtra("end", end);
 		startActivity(i);
 	}
 }
