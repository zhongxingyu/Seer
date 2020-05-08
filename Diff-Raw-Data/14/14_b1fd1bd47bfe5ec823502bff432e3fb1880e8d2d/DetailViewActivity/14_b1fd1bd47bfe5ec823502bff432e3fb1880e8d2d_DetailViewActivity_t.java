 package com.example.grantmobile;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DetailViewActivity extends Activity {
 	// these two named parameter are for the Intent interface to this activity (both reference Strings)
	public static final String TAG_REQUEST_ID = "RequestId"; // required, no default!!!!
	public static final String TAG_DAY_OF_MONTH = "DayOfMonth"; // optional, default is first day of month
 
 	private static final String TAG = "detailview";
 	private static String requestURL = "http://mid-state.net/mobileclass2/android";
 	
 	JSONParser jParser = new JSONParser();
 	ArrayList<String> grantHours; // grant hour array 
 	ArrayList<String> nonGrantHours; // non-grant hour array
 	ArrayList<String> leaveHours;  // leave hour array
 	HashMap<String,String> map;
 	
 	private static final String TAG_SUCCESS = "success";  // "true" is good
 	private static final String TAG_MESSAGE = "message";
 
 	private static final String TAG_HOURS = "hours"; 
 	private static final String TAG_GRANT = "grant";
 	private static final String TAG_NON_GRANT = "non-grant";
 	private static final String TAG_LEAVE = "leave";
 	
 	private static final String TAG_MONTH = "month";
 	private static final String TAG_YEAR = "year";
 	
 	private static final String TAG_EMPLOYEE = "employee";
 	private static final String TAG_FIRST_NAME = "firstname";
 	private static final String TAG_LAST_NAME = "lastname";
 	private static final String TAG_EMPLOYEE_ID = "id";
 	
 	private static final String TAG_GRANT_ID = "ID";
 	private static final String TAG_STATE_CATALOG_NUM = "stateCatalogNum";
 	private static final String TAG_GRANT_NUMBER = "grantNumber";
 	private static final String TAG_GRANT_TITLE = "grantTitle";
 
 	JSONObject json = null; // entire json object
 
 	// day of week string array
 	static final String[] DOW = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
 							"Friday", "Saturday" };
	static final String[] MOY = { "January", "February", "March", "April", "May", "June",
									"July", "August", "September", "October", "November", "December" };
 		
 	Integer dowStart; // month starts on Wednesday for test, use WED - 1 
 	Integer domCurrent; // current day of month
 	public static String requestId;
 	
 	TextView grantNameView;
 	TextView grantIdView;
 	TextView employeeNameView;
 	TextView catalogView;
 		
 	TextView dateView;
 	TextView dayView;
 	TextView grantHoursView;
 	TextView nonGrantHoursView;
 	TextView leaveHoursView;
 	
 	TextView dayTotalHoursView;
 	TextView monthTotalHoursView;
 	
 	Context context;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		context = this;
 		setContentView(R.layout.activity_detail_view);
 		
 		// instantiate parser and storage objects
 		jParser = new JSONParser();
 		grantHours = new ArrayList<String>(); // grant hour array 
 		nonGrantHours = new ArrayList<String>(); // non-grant hour array
 		leaveHours = new ArrayList<String>();  // leave hour array
 		map = new HashMap<String,String>();
 		
 		// intent interface
 		Intent intent = getIntent();
 		String str = intent.getStringExtra(TAG_DAY_OF_MONTH);
 		if (str==null) {
 			domCurrent = 1;
 		} else {
 			domCurrent = Integer.valueOf(str);
 		}
 		str = intent.getStringExtra(TAG_REQUEST_ID);
 		if (str==null) {
 			requestId = "23"; // for test
 		} else {
 			requestId = str;
 		}
 
 		// setup view for displaying grant info
 		grantNameView = (TextView)findViewById(R.id.grantNameView);
 		grantIdView = (TextView)findViewById(R.id.grantIdView);
 		employeeNameView = (TextView)findViewById(R.id.employeeNameView);
 		catalogView = (TextView)findViewById(R.id.catalogView);
 
 		// setup view for displaying specific info for a given date
 		dateView = (TextView)findViewById(R.id.dateView);
 		dayView = (TextView)findViewById(R.id.dayOfWeekView);
 		grantHoursView = (TextView)findViewById(R.id.grantHoursView);
 		nonGrantHoursView = (TextView)findViewById(R.id.nonGrantHoursView);
 		leaveHoursView = (TextView)findViewById(R.id.leaveHoursView);
 
 		dayTotalHoursView = (TextView)findViewById(R.id.dayTotalHoursView);
 		monthTotalHoursView = (TextView)findViewById(R.id.monthTotalHoursView);
 		
         final Button backwardButton = (Button) findViewById(R.id.backBtn);
         backwardButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Perform action on click
             	if (--domCurrent == 0) domCurrent = grantHours.size();  // underflow wrap
             	updateView();
             }
         });
         final Button forwardButton = (Button) findViewById(R.id.nextBtn);
         forwardButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	if (++domCurrent >  grantHours.size()) domCurrent = 1; // overflow wrap
             	updateView();
             }
         });
         
         final Button returnButton = (Button) findViewById(R.id.returnBtn);
         returnButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Toast.makeText(context, "Return to Calendar View", Toast.LENGTH_LONG).show();
                 finish();
             }
         });
         
 		// start access of grant data, updateView() below will access the data when ready
 		new GetGrantData().execute();
 	}
         
     private void updateView() {
 		String domString = String.valueOf(domCurrent);
     	
 		// figure out what first day-of-week is for this month (dowStart)
     	Calendar cal = Calendar.getInstance();
     	cal.set(Calendar.DATE,1);
    	cal.set(Calendar.MONTH, Integer.valueOf(map.get(TAG_MONTH)));
     	cal.set(Calendar.YEAR, Integer.valueOf(map.get(TAG_YEAR)));
     	cal.set(Calendar.DAY_OF_MONTH, 1); 	
         dowStart = (cal.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY)%7;
         
 		grantNameView.setText(map.get(TAG_GRANT_TITLE));
 		grantIdView.setText(map.get(TAG_GRANT_ID));
 		employeeNameView.setText(map.get(TAG_FIRST_NAME)+ " " + map.get(TAG_LAST_NAME));
 		catalogView.setText(map.get(TAG_STATE_CATALOG_NUM));
     	
		Integer moy = Integer.parseInt(map.get(TAG_MONTH));
    	dateView.setText(MOY[moy]+" "+domString+", "+map.get(TAG_YEAR));
     	dayView.setText(DOW[(domCurrent+dowStart-1)%7]);
     	grantHoursView.setText(grantHours.get(domCurrent-1));
     	nonGrantHoursView.setText(nonGrantHours.get(domCurrent-1));
     	leaveHoursView.setText(leaveHours.get(domCurrent-1));
     		
     	Integer dayTotal = Integer.valueOf(grantHours.get(domCurrent-1))+Integer.valueOf(nonGrantHours.get(domCurrent-1))+Integer.valueOf(leaveHours.get(domCurrent-1));
     	dayTotalHoursView.setText(dayTotal.toString());
     	Integer tgh = 0;
     	for (int i = 0; i < grantHours.size(); i++)
     		tgh = tgh + Integer.valueOf(grantHours.get(i));
     	monthTotalHoursView.setText(tgh.toString());
 	}
 
 	class GetGrantData extends AsyncTask<String,String,String> {
 
 		@Override
 		protected String doInBackground(String... args) {
 			List<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("q","email"));
 			params.add(new BasicNameValuePair("id",requestId)); // this will come from intent
 			Log.d(TAG+"_GET_REQ", requestURL);
 			json = JSONParser.makeHttpRequest(requestURL, "GET", params);
 			Log.d(TAG+"_GET_RET", json.toString());
 			return "OK";
 		}
 		
 		protected void onPostExecute(String file_url) {
 
 			runOnUiThread(new Runnable() {
 				public void run() {
 					JSONArray jsa = null; // json array
 					JSONObject jso = null; // json object
 					JSONObject jsom = null; // message json
 					JSONObject jsoh = null; // hours json
 					try {
 						String s = json.getString(TAG_SUCCESS);
 						if (s.equals("true")) {
 							jsom = json.getJSONObject(TAG_MESSAGE);
 							jsoh = jsom.getJSONObject(TAG_HOURS);
 							jsa = jsoh.getJSONArray(TAG_GRANT);
 					 	    for (int i = 0; i < jsa.length(); i++) {
 					 	    	grantHours.add((Integer.valueOf(jsa.getInt(i))).toString());  // these are the grant hours for each day of month
 					 	    }
 							jsa = jsoh.getJSONArray(TAG_NON_GRANT);
 					 	    for (int i = 0; i < jsa.length(); i++){
 					 	    	nonGrantHours.add((Integer.valueOf(jsa.getInt(i))).toString());  // these are the non-grant hours for each day of month
 					 	    }
 							jsa = jsoh.getJSONArray(TAG_LEAVE);
 					 	    for (int i = 0; i < jsa.length(); i++){
 					 	    	leaveHours.add((Integer.valueOf(jsa.getInt(i))).toString());  // these are the leave hours for each day of month
 					 	    }
 					 		map.put(TAG_MONTH,(Integer.valueOf(jsom.getInt(TAG_MONTH))).toString());
 					 		map.put(TAG_YEAR,(Integer.valueOf(jsom.getInt(TAG_YEAR))).toString());
 					 		jso = jsom.getJSONObject(TAG_EMPLOYEE);
 					 		map.put(TAG_FIRST_NAME, jso.getString(TAG_FIRST_NAME));
 					 		map.put(TAG_LAST_NAME, jso.getString(TAG_LAST_NAME));
 							Log.d(TAG+"_GET_REQ", "last name = " + jso.getString(TAG_LAST_NAME));
 					 		map.put(TAG_EMPLOYEE_ID, jso.getString(TAG_EMPLOYEE_ID));
 					 		jso = jsom.getJSONObject(TAG_GRANT);	
 					 		map.put(TAG_GRANT_ID, jso.getString(TAG_GRANT_ID));
 					 		map.put(TAG_GRANT_NUMBER, jso.getString(TAG_GRANT_NUMBER));
 					 		map.put(TAG_GRANT_TITLE, jso.getString(TAG_GRANT_TITLE));
 					 		map.put(TAG_STATE_CATALOG_NUM, jso.getString(TAG_STATE_CATALOG_NUM));	
 					 		updateView();
 					    }
 					} catch (JSONException e) {
 					    e.printStackTrace();
 					}
 				}
 			});
 		}
 	}
 
 
 }
