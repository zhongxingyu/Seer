 package com.example.grantmobile;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DetailViewActivity extends FragmentActivity {
 	// these two named parameter are for the Intent interface to this activity (both reference Strings)
 	public static final String TAG_REQUEST_ID = "RequestId"; // required, no default!!!!
 	public static final String TAG_DAY_OF_MONTH = "DayOfMonth"; // optional, default is first day of month
 	public static final String requestURL = "http://mid-state.net/mobileclass2/android";
 	
 	public static final String TAG_SUCCESS = "success";  // "true" is good
 	public static final String TAG_MESSAGE = "message";
 	
 	public static final String TAG_HOURS = "hours"; 
 	public static final String TAG_GRANT = "grant";
 	public static final String TAG_NON_GRANT = "non-grant";
 	public static final String TAG_LEAVE = "leave";
 	
 	public static final String TAG_MONTH = "month";
 	public static final String TAG_YEAR = "year";
 	
 	public static final String TAG_SUPERVISOR = "supervisor";
 	public static final String TAG_EMPLOYEE = "employee";
 	public static final String TAG_FIRST_NAME = "firstname";
 	public static final String TAG_LAST_NAME = "lastname";
 	public static final String TAG_EMPLOYEE_ID = "id";
 	
 	public static final String TAG_GRANT_ID = "ID";
 	public static final String TAG_STATE_CATALOG_NUM = "stateCatalogNum";
 	public static final String TAG_GRANT_NUMBER = "grantNumber";
 	public static final String TAG_GRANT_TITLE = "grantTitle";
 	
 	ArrayList<String> grantHours; // grant hour array 
 	ArrayList<String> nonGrantHours; // non-grant hour array
 	ArrayList<String> leaveHours;  // leave hour array
 	HashMap<String,String> map;
 
 	JSONObject json = null; // entire json object
 
 	// day of week string array
 	static final String[] DOW = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
 							"Friday", "Saturday" };
 	static final String[] MOY = { "January", "February", "March", "April", "May", "June",
 									"July", "August", "September", "October", "November", "December" };
 		
 	Integer dowStart; // month starts on Wednesday for test, use WED - 1 
 	Integer domCurrent; // current day of month
 	Integer moy;
 	public static String requestId;
 	
 	TextView grantNameView, grantIdView, employeeNameView, catalogView;		
 	TextView dateView, dayView, grantHoursView, nonGrantHoursView, leaveHoursView;
 	TextView dayTotalHoursView, monthTotalHoursView;
 	
 	Context context;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		context = this;
 		setContentView(R.layout.activity_detail_view);
 		
 		grantHours = new ArrayList<String>(); // grant hour array 
 		nonGrantHours = new ArrayList<String>(); // non-grant hour array
 		leaveHours = new ArrayList<String>();  // leave hour array
 		map = new HashMap<String,String>();
 		
 		// intent interface
 		Intent intent = getIntent();
 		String str = intent.getStringExtra(TAG_DAY_OF_MONTH);
 		if (str==null) {
 			domCurrent = 1; // default is first day of month
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
 		grantNameView = (TextView)findViewById(R.id.grantNameTv);
 		grantIdView = (TextView)findViewById(R.id.grantIdTv);
 		employeeNameView = (TextView)findViewById(R.id.employeeNameTv);
 		catalogView = (TextView)findViewById(R.id.catalogTv);
 
 		// setup view for displaying specific info for a given date
 		dateView = (TextView)findViewById(R.id.dateTv);
 		dayView = (TextView)findViewById(R.id.dayOfWeekTv);
 		grantHoursView = (TextView)findViewById(R.id.grantHoursTv);
 		nonGrantHoursView = (TextView)findViewById(R.id.nonGrantHoursTv);
 		leaveHoursView = (TextView)findViewById(R.id.leaveHoursTv);
 
		dayTotalHoursView = (TextView)findViewById(R.id.dayTotalHoursTv);
		monthTotalHoursView = (TextView)findViewById(R.id.monthTotalHoursTv);
 		
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
                 setResult(RESULT_CANCELED);
                 finish();
             }
         });
         
 		// start access of grant data, updateView() below will access the data when ready
 		new JSONParser.RequestBuilder(requestURL)
 		.setUrl(requestURL)
 		.addParam("q", "email")
 		.addParam("id", String.valueOf(requestId))
 		.makeRequest(new JSONResultHandler());
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
     	
 		moy = Integer.parseInt(map.get(TAG_MONTH));
     	dateView.setText(MOY[moy]+" "+domString+", "+map.get(TAG_YEAR));
     	dayView.setText(DOW[(domCurrent+dowStart-1)%7]);
     	grantHoursView.setText(Double.valueOf(grantHours.get(domCurrent-1)).toString());
     	nonGrantHoursView.setText(Double.valueOf(nonGrantHours.get(domCurrent-1)).toString());
     	leaveHoursView.setText(Double.valueOf(leaveHours.get(domCurrent-1)).toString());
     		
     	Double dayTotal = Double.valueOf(grantHours.get(domCurrent-1))
     			+Double.valueOf(nonGrantHours.get(domCurrent-1))
     			+Double.valueOf(leaveHours.get(domCurrent-1));
     	dayTotalHoursView.setText(dayTotal.toString());
     	Double tgh = 0.0;
     	for (int i = 0; i < grantHours.size(); i++)
     		tgh = tgh + Double.valueOf(grantHours.get(i));
     	monthTotalHoursView.setText(tgh.toString());
 	}
 	
     class JSONResultHandler extends JSONParser.SimpleResultHandler {
 		@Override
 		public void onSuccess(Object oResult) {
 			try {
//				Toast.makeText(context, "invoked JSON parser", Toast.LENGTH_LONG).show();
 				JSONObject json = (JSONObject) oResult;
 				JSONArray jsa = null; // json array
 				JSONObject jso = null; // misc., message & hours object
 //				Toast.makeText(context, json.toString(), Toast.LENGTH_LONG).show();
 				
 				// get time arrays
     			jso = json.getJSONObject(TAG_HOURS);
     			jsa = jso.getJSONArray(TAG_GRANT);
     			for (int i = 0; i < jsa.length(); i++) {
     				grantHours.add((Integer.valueOf(jsa.getInt(i))).toString());  // these are the grant hours for each day of month
     			}
     			jsa = jso.getJSONArray(TAG_NON_GRANT);
     			for (int i = 0; i < jsa.length(); i++){
     				nonGrantHours.add((Integer.valueOf(jsa.getInt(i))).toString());  // these are the non-grant hours for each day of month
     			}
     			jsa = jso.getJSONArray(TAG_LEAVE);
     			for (int i = 0; i < jsa.length(); i++){
     				leaveHours.add((Integer.valueOf(jsa.getInt(i))).toString());  // these are the leave hours for each day of month
     			}
     			
 				// get date info
 				map.put(TAG_MONTH,json.getString(TAG_MONTH));
 				moy = Integer.parseInt(json.getString(TAG_MONTH));
 				map.put(TAG_YEAR,json.getString(TAG_YEAR));
 				
     			// get employee info
 				jso = json.getJSONObject(TAG_EMPLOYEE);
 				map.put(TAG_FIRST_NAME,jso.getString(TAG_FIRST_NAME));
 				map.put(TAG_LAST_NAME,jso.getString(TAG_LAST_NAME));
 				map.put(TAG_EMPLOYEE_ID,jso.getString(TAG_EMPLOYEE_ID));
 				
 				// get grant info
     			jso = json.getJSONObject(TAG_GRANT);	
 				map.put(TAG_GRANT_TITLE,jso.getString(TAG_GRANT_TITLE));
 				map.put(TAG_GRANT_ID,jso.getString(TAG_GRANT_ID));
 				map.put(TAG_STATE_CATALOG_NUM,jso.getString(TAG_STATE_CATALOG_NUM));
 				
 				updateView();
 			}
 			catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void onFailure(String errorMessage) {
 			Toast.makeText(DetailViewActivity.this, errorMessage, Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	/**
 	 * This procedure initializes the options menu.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
     /**
      * This procedure handles all of the options menu selection events.
      */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	
 		// Variables
 		
 		// The id of the menu item chosen
 		int itemId = 0;	
 		
 		// Determine id of item chosen, and respond accordingly
 		itemId = item.getItemId();
 		
 		switch (itemId) {
 		case (R.id.mnuDetail) :
 			Intent intent = new Intent(this, DetailViewActivity.class);
 			startActivity(intent);
 			break;
 		case (R.id.mnuDialog) :
 			//show dialog box
 			SubmitDialog dialog = new SubmitDialog();
 			FragmentManager manager = getSupportFragmentManager();
 			dialog.setUserID(732);
 			dialog.show(manager, "");
 			break;
 		}// end switch
 		
 		return true;
 		
 	}
 }
