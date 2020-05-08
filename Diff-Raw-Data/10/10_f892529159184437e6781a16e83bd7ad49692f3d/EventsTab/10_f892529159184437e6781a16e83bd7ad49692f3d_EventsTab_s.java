 package watsons.wine;
 
 import java.text.DateFormatSymbols;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import watsons.wine.notification.NotificationMainActivity;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class EventsTab extends Activity implements
 		CalendarView.OnCellTouchListener, CalendarView.OnDrawableTouchListener {
 
 	// url to make request
 	private static String url = "http://watsonwine.bull-b.com/CodeIgniter_2.1.3/index.php/api/list_events/";
 
 	// JSON Node names
 	private static final String TAG_EVENT = "events";
 	private static final String TAG_ID = "id";
 	private static final String TAG_NAME = "name";
 	private static final String TAG_DATE = "event_date";
 	private static final String TAG_LOCATION = "location";
 	private static final String TAG_TIME = "event_time";
 	private static final String TAG_URL = "url";
 	private static final String TAG_NOTE = "note";
 	private static final String TAG_ENQUIRY = "enquiry";
 
 	Context mContext = EventsTab.this;
 	JSONArray events = null;
 	List<Map<String, String>> eventList = new ArrayList<Map<String, String>>();
 	// List<String> dateList = new ArrayList<String>();
 	List<Integer> dateList = new ArrayList<Integer>();
 
 	public static final String MIME_TYPE = "vnd.android.cursor.dir/vnd.exina.android.calendar.date";
 	CalendarView mView = null;
 	TextView mTextView = null;
 	TextView mHit;
 	Handler mHandler = new Handler();
 
 	private ImageView iv;
 	private RelativeLayout rl;
 	private JSONObject json;
 	private SharedPreferences sharedPreferences;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.events_tab);
 
 		// Refresh Button
 		rl = (RelativeLayout) findViewById(R.id.refresh_img);
 		iv = (ImageView) findViewById(R.id.refresh_btn);
 		iv.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				performRefresh();
 			}
 
 		});
 
 		mView = (CalendarView) findViewById(R.id.calendar_view);
 		mView.setOnDrawableTouchListener(this);
 		mView.setOnCellTouchListener(this);
 		mTextView = (TextView) findViewById(R.id.calendar_text);
 
 		mHandler.post(new Runnable() {
 			public void run() {
 				String yearString = String.valueOf(mView.getYear());
 				String monthString = DateFormatSymbols.getInstance(
 						new Locale("en", "EN")).getMonths()[mView.getMonth()];
 				mTextView.setText(monthString + " " + yearString);
 				new JsonTask().execute(url);
 			}
 		});
 		
 		/*mHandler.postDelayed(new Runnable() {
 			public void run() {
 				mView.drawDate(dateList);
 			}
 		}, 500);*/
 
 		ImageButton home_button = (ImageButton)findViewById(R.id.cellar_home_button);
         home_button.setOnClickListener(new OnClickListener(){
  			@Override
  			public void onClick(View v) {
  				finish();
  			}
         });
         
         ImageButton mail_button = (ImageButton)findViewById(R.id.cellar_mail_button);
         mail_button.setOnClickListener(new OnClickListener(){
  			@Override
  			public void onClick(View v) {
  				Intent intent = new Intent(getParent(), NotificationMainActivity.class);
 				TabGroupBase parentActivity = (TabGroupBase)getParent();
 	        	parentActivity.startChildActivity("MyCellarsMainCallByMail", intent);
  			}
         });
 	}
 
 	public void onTouch(Cell cell) {
 
 		int year = mView.getYear();
 		int month = mView.getMonth() + 1;
 		int day = cell.getDayOfMonth();
 		boolean tmp = cell.IsEvent();
 		if (tmp) {
 			String month_str = null;
 			if (month<10) 
 			{
 				month_str = "0"+String.valueOf(month);
 			}
 			else 
 			{
 				month_str = String.valueOf(month);
 			}
 			
 			String day_str = null;
 			if(day<10) {
 				day_str = "0"+String.valueOf(day);
 			}else {
 				day_str = String.valueOf(day);
 			}
 			
 			String str = String.valueOf(year)+month_str+day_str;
 			System.out.println(str);
 			Bundle bundle = new Bundle();
         	bundle.putString("date", str);
 			Intent intent = new Intent(getParent(), EventsWebView.class);
         	TabGroupBase parentActivity = (TabGroupBase)getParent();
         	intent.putExtras(bundle);
         	parentActivity.startChildActivity("EventsWebView", intent);
 		}
 	}
 
 	public void onTouch(final boolean left, final boolean right) {
 		String yearString = "", monthString = "";
 		if (left) {
 			if (mView.getMonth() == 0) {
 				yearString = String.valueOf(mView.getYear() - 1);
 				monthString = DateFormatSymbols.getInstance(
 						new Locale("en", "EN")).getMonths()[11];
 			} else {
 				yearString = String.valueOf(mView.getYear());
 				monthString = DateFormatSymbols.getInstance(
 						new Locale("en", "EN")).getMonths()[mView.getMonth() - 1];
 			}
 		} else if (right) {
 			if (mView.getMonth() == 11) {
 				yearString = String.valueOf(mView.getYear() + 1);
 				monthString = DateFormatSymbols.getInstance(
 						new Locale("en", "EN")).getMonths()[0];
 			} else {
 				yearString = String.valueOf(mView.getYear());
 				monthString = DateFormatSymbols.getInstance(
 						new Locale("en", "EN")).getMonths()[mView.getMonth() + 1];
 			}
 		}
 		mTextView.setText(monthString + " " + yearString);
 		// mView.invalidate();
 
 		mHandler.postDelayed(new Runnable() {
 			public void run() {
 				if (left) {
 					mView.previousMonth();
 				}
 
 				else if (right) {
 					mView.nextMonth();
 				} else
 					return;
 			}
 		}, 0);
 	}
 
 	protected void performRefresh() {
 		rl.setVisibility(View.VISIBLE);
 		iv.setVisibility(View.INVISIBLE);
 		// SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
 		SharedPreferences.Editor editor = sharedPreferences.edit();
 		editor.remove("json_events");
 		editor.commit();
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			public void run() {
 				Intent intent = new Intent(getParent(), EventsTab.class);
 				TabGroupBase parentActivity = (TabGroupBase) getParent();
 				parentActivity.startChildActivityNotAddId("EventsTab", intent);
 			}
 		}, 500);
 	}
 
 	/*@Override
 	protected void onRestart() {
		super.onRestart();
<<<<<<< HEAD
 		Intent intent = new Intent(getParent(), EventsTab.class);
 		TabGroupBase parentActivity = (TabGroupBase) getParent();
 		parentActivity.startChildActivityNotAddId("EventsTab", intent);
 	}
=======
		
 
	}*/
>>>>>>> stark
 	
 	private class JsonTask extends AsyncTask<String, Void, String> {
 		
 		ProgressDialog pdia;
 		Boolean quitTask;
 		
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			pdia = new ProgressDialog(getParent());
 			pdia.setMessage("Loading...");
 			pdia.show();
 			quitTask = false;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			super.onPostExecute(result);
 			
 			pdia.dismiss();
 			
 			if (quitTask) {
 				
 				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 						EventsTab.this.getParent());
 				alertDialogBuilder.setTitle("Warnings!");
 				alertDialogBuilder
 						.setMessage("Cannot connect. Please check your network and try again later.")
 						.setCancelable(true)
 						.setPositiveButton("OK",new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,int id) {
 								dialog.cancel();
 							}
 						});
 				AlertDialog alertDialog = alertDialogBuilder.create();
 				alertDialog.show();
 				
 				return;
 			}
 			
 			mView = (CalendarView) findViewById(R.id.calendar_view);
 			for (int i = 0; i < eventList.size(); i++) 
 			{
 				if (!eventList.get(i).get(TAG_DATE).equals("")) 
 				{
 					String date, tmp;
 					int index;
 					date = eventList.get(i).get(TAG_DATE);
 					tmp = date.substring(0, 4);
 					dateList.add(Integer.parseInt(tmp));
 					index = date.indexOf("-");
 					date = date.substring(index + 1);
 					index = date.indexOf("-");
 					tmp = date.substring(0, index);
 					dateList.add(Integer.parseInt(tmp));
 					date = date.substring(index + 1);
 					dateList.add(Integer.parseInt(date));
 				}
 			}
 			mHandler.postDelayed(new Runnable(){
 				@Override
 				public void run() {
 					mView.drawDate(dateList);
 				}
 			}, 100);
 			
 		}
 
 		@Override
 		protected String doInBackground(String... params) {
 			// Share Preference for Json
 			sharedPreferences = getPreferences(MODE_PRIVATE);
 			String strJson = sharedPreferences.getString("json_events", null);
 			if (strJson != null) {
 				try {
 					json = new JSONObject(strJson);
 				} catch (JSONException e1) {
 					e1.printStackTrace();
 				}
 			} else {
 				if (!isOnline())
 		        {
 		        	quitTask = true;
 		        	return null;
 		        }
 				// Creating JSON Parser instance
 				JSONParser jParser = new JSONParser();
 				// getting JSON string from URL
 				json = jParser.getJSONFromUrl(url);
 				
 				if(json == null)
 		        {
 		        	quitTask = true;
 		        	return null;
 		        }
 				SharedPreferences.Editor editor = sharedPreferences.edit();
 				editor.putString("json_events", json.toString());
 				editor.commit();
 			}
 
 			try {
 				
 				
 				// Getting Array of Contacts
 				events = json.getJSONArray(TAG_EVENT);
 
 				
 				
 				// looping through All Contacts
 				for (int i = 0; i < events.length(); i++) {
 					JSONObject c = events.getJSONObject(i);
 
 					// Storing each json item in variable
 					String id = c.getString(TAG_ID);
 					String name = c.getString(TAG_NAME);
 					String date = c.getString(TAG_DATE);
 
 					HashMap<String, String> map = new HashMap<String, String>();
 					// adding each child node to HashMap key => value
 					map.put(TAG_ID, id);
 					map.put(TAG_NAME, name);
 					map.put(TAG_DATE, date);
 
 					// adding HashList to ArrayList
 					eventList.add(map);
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			return "";
 		}
 		
 		public boolean isOnline() {
 		    ConnectivityManager cm =
 		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 		        return true;
 		    }
 		    return false;
 		}
 	}
 }
