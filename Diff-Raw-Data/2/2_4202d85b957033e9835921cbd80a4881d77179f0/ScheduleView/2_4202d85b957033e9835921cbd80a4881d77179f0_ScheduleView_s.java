 package ca.communitech.appsfactory.waldo;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class ScheduleView extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_schedule_view);
 
 		//Utils.errormessage("Loading...", getBaseContext());
         //new ChangeHeaderColor().execute();
         //new PopulateScheduleTask().execute();
 
         //Add Content to Schedule
        
         //Add the deselection feature to each column
         //DOES THIS EVEN WORK???? -> todo = make this work in xml????!?!?!?!??!?!?!11?1//?1?!?!
         LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayout3);
         for (int i = 0; i < lin.getChildCount(); i++){
         	lin.getChildAt(i).setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v){
 					onBlankClick(v);
 				}
         	});
         }
     }
 
     //called when the add button is clickified
     public void addSchedule(View view) {
 		Intent intent = new Intent(this, CreateScheduleView.class);
 		startActivity(intent);
    	}
     
     @Override
     public void onResume(){
     	super.onResume();
     	View view = findViewById(R.id.refreshbutton);
     	refreshSchedule(view);
     	
     }
     @Override
  	public void onWindowFocusChanged(boolean hasFocus) {
  		super.onWindowFocusChanged(hasFocus);
  		View view = findViewById(R.id.refreshbutton);
     	onBlankClick(view);
  	}
     
     /**refresh the schedule*/
     public void refreshSchedule(View view) {
     	
     	//Remove all views from the columns
     	RelativeLayout column = (RelativeLayout) findViewById(R.id.moncolumn);
     	column.removeAllViews();
     	column = (RelativeLayout) findViewById(R.id.tuecolumn);
     	column.removeAllViews();
     	column = (RelativeLayout) findViewById(R.id.wedcolumn);
     	column.removeAllViews();
     	column = (RelativeLayout) findViewById(R.id.thucolumn);
     	column.removeAllViews();
     	column = (RelativeLayout) findViewById(R.id.fricolumn);
     	column.removeAllViews();
     	
     	
     	//Change header colors back
     	TextView header = (TextView) findViewById(R.id.mon_header);
 		header.setTextColor(Color.GRAY);
 		header.setBackgroundColor(Color.BLACK);
 		header = (TextView) findViewById(R.id.tue_header);
 		header.setTextColor(Color.GRAY);
 		header.setBackgroundColor(Color.BLACK);
 		header = (TextView) findViewById(R.id.wed_header);
 		header.setTextColor(Color.GRAY);
 		header.setBackgroundColor(Color.BLACK);
 		header = (TextView) findViewById(R.id.thu_header);
 		header.setTextColor(Color.GRAY);
 		header.setBackgroundColor(Color.BLACK);
 		header = (TextView) findViewById(R.id.fri_header);
 		header.setTextColor(Color.GRAY);
 		header.setBackgroundColor(Color.BLACK);
 		
 		
 		//repopulate
 		new ChangeHeaderColor().execute();
 		new PopulateScheduleTask().execute();
 		return;
     }
 
     /** get the current date from the server - must be called in a separate thread*/
 	private HttpResponse getDate() {
 		HttpClient client = new DefaultHttpClient();
         HttpPost post = new HttpPost(Constants.POST_URL);
     	JSONObject daterequest = new JSONObject();
     	
     	try {
         	daterequest.put("action", "currentDate");
         	StringEntity data_string = new StringEntity(daterequest.toString());
     		post.setEntity(data_string);
     		post.setHeader("dataType", "json");
     		
     		HttpResponse response = client.execute(post);
     		if (response.getStatusLine().getStatusCode() == 200) {
     			return response;
     		}
     		else {
     			//databaseConnectionErrorMessage();
     			return null;
     		}
     	} catch (Exception e){
     		//databaseConnectionErrorMessage();
     		return null;
     	}
 	}
 	
 	/**Toasts a generic database error message */
 	private void databaseConnectionErrorMessage() {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				Utils.errormessage("Error connecting to database. Please try again in a few moments", getBaseContext());
 			}
 		}).start();
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_schedule_view, menu);
         return true;
     }
     
     /**Extends AsyncTask to change the header color based on current day (serverside) */
     private class ChangeHeaderColor extends AsyncTask<Void, Integer, HttpResponse> {
 
 		@Override
 		/**Gets and returns the date in a separate thread*/
 		protected HttpResponse doInBackground(Void... params) {
 			return getDate();
 		}
 		
 		@Override
 		/**Disseminate the HttpResponse from getdate and change the header accordingly*/ 
 		protected void onPostExecute(HttpResponse response) {
 			try {
 				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
 				String json = reader.readLine();
 				JSONTokener tokener = new JSONTokener(json);
 				JSONObject jsonobject = new JSONObject(tokener);
 				TextView header;
 				//Change header color on switch of current day
 				switch (Constants.DAYS.valueOf(jsonobject.getString("weekday"))) {
 				case Monday:
 					header = (TextView) findViewById(R.id.mon_header);
 					header.setTextColor(Color.WHITE);
 					header.setBackgroundColor(Color.GRAY);
 					break;
 				case Tuesday:
 					header = (TextView) findViewById(R.id.tue_header);
 					header.setTextColor(Color.WHITE);
 					header.setBackgroundColor(Color.GRAY);
 					break;
 				case Wednesday:
 					header = (TextView) findViewById(R.id.wed_header);
 					header.setTextColor(Color.WHITE);
 					header.setBackgroundColor(Color.GRAY);
 					break;
 				case Thursday:
 					header = (TextView) findViewById(R.id.thu_header);
 					header.setTextColor(Color.WHITE);
 					header.setBackgroundColor(Color.GRAY);
 					break;
 				case Friday:
 					header = (TextView) findViewById(R.id.fri_header);
 					header.setTextColor(Color.WHITE);
 					header.setBackgroundColor(Color.GRAY);
 					break;
 
 				default:
 					//well shit
 					break;
 				}				
 			} catch (IllegalStateException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
     
     }
     
     /**The onclick of each column, used to select and deselect events from the update/delete activity*/
     private void onColumnClick(final View view, final String start, final String end) {
     	Button addbutton = (Button) findViewById(R.id.addbutton);
     	Button refreshbutton = (Button) findViewById(R.id.refreshbutton);   	
     	LinearLayout rl = (LinearLayout) findViewById(R.id.linearLayout3);
     	if (view.getId() == 665){
     		onBlankClick(view);
     		return;
     	} else {
     		//set the id of each schedule to the most metal of numbers and sets the background/border color to the default
 	    	for (int i=0; i < rl.getChildCount(); i++){
 	    		RelativeLayout col = (RelativeLayout) rl.getChildAt(i);
 	    		for (int a=0; a < col.getChildCount(); a++){
 	    			col.getChildAt(a).setId(666);
 	    			col.getChildAt(a).setBackgroundColor(Color.argb(255, 119, 119, 119));
 	    		}
 	    	}
 	    	//set the background/border and id of the actual clicked view
 	    	view.setBackgroundColor(Color.WHITE);
 	    	view.setId(665);
 	    	addbutton.setText("Update");
 	    	refreshbutton.setText("Delete");
 	    	addbutton.setOnClickListener(new View.OnClickListener() {
 	    		@Override
 	    		/** launch the update activity with requisite information*/
 	    		public void onClick(View v){
 	    			Intent intent = new Intent(getBaseContext(), CreateScheduleView.class);
 	    			String[] value;
 	    			value = new String[10];
 	    			value[0] = start;
 	    			value[1] = end;
 	    			String[] tagdata = (String[]) view.getTag();
 	    			value[2] = (String) tagdata[0];
 	    			value[3] = (String) tagdata[1];
 	    			intent.putExtra("ca.communitech.appsfactory.waldo.startEnd", value);
 	    			startActivity(intent);
 	    		}
 	    		
 	    	});
 	    	refreshbutton.setOnClickListener(new View.OnClickListener() {
 	    		@Override
 	    		/**Set the functionality of the "delete button"*/
 	    		public void onClick(final View v){
 	    			new AlertDialog.Builder(v.getContext())
 					.setTitle("Delete Event")
 					.setMessage("Do you really want to delete this event?")
 					.setPositiveButton("You Bet!", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 	    	    			RelativeLayout relv = (RelativeLayout) view;
 	    	    			deleteView(relv);
 				    	    refreshSchedule(relv);
 				    	    onBlankClick(relv);
 							return;
 						}
 					})
 					.setNegativeButton("Nope!", null)
 					.show();
 	    		}
 	    	});
     	}
     };
     
     /**Deselects events and resets the refresh/add buttons
      * 
      * @param v
      */
     private void onBlankClick(View v){
     	Button addbutton = (Button) findViewById(R.id.addbutton);
     	Button refreshbutton = (Button) findViewById(R.id.refreshbutton);
     	LinearLayout rl = (LinearLayout) findViewById(R.id.linearLayout3);
     	//Reset all schedule events
     	for (int i=0; i < rl.getChildCount(); i++){
     		RelativeLayout col = (RelativeLayout) rl.getChildAt(i);
     		for (int a=0; a < col.getChildCount(); a++){
     			col.getChildAt(a).setId(666);
     			col.getChildAt(a).setBackgroundColor(Color.argb(255, 119, 119, 119));
     		}
     	}
     	//reset button text
     	addbutton.setText("Add");
     	refreshbutton.setText("Refresh");
     	addbutton.setOnClickListener(new View.OnClickListener() {
     		@Override
     		public void onClick(View v){
     			Intent intent = new Intent(getBaseContext(), CreateScheduleView.class);
     			startActivity(intent);
     		}
     		
     	});
     	refreshbutton.setOnClickListener(new View.OnClickListener() {
     		@Override
     		public void onClick(View v){
     			refreshSchedule(v);
     		}
     	});
     }
     
     
     /*Delete an event layout from the database and remove it from the screen
      *
      * @param relv to be deleted
      *
      */
     private void deleteView (RelativeLayout relv) {
     	final TextView starttime = (TextView) relv.getChildAt(1);
 		final TextView endtime = (TextView)relv.getChildAt(2);
 		View parent = (View) relv.getParent();
 		final String date;
 		int id = parent.getId();
 		switch (id) {
 		case R.id.moncolumn:
 			date = "Monday";
 			break;
 		case R.id.tuecolumn:
 			date = "Tuesday";
 			break;
 		case R.id.wedcolumn:
 			date = "Wednesday";
 			break;
 		case R.id.thucolumn:
 			date = "Thursday";
 			break;
 		case R.id.fricolumn:
 			date = "Friday";
 			break;
 
 		default:
 			date = null;
 			break;
 		}
 	    new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				HttpClient client = new DefaultHttpClient();
 				HttpPost post = new HttpPost (Constants.POST_URL);        	
 				try {
 					JSONObject data_json = new JSONObject();
 					data_json.put("userName", getIntent().getExtras().getString(Log_In.USERNAME));
 					data_json.put("organizationId", Constants.ORGANIZATION_ID);
 					data_json.put("locationCode", Constants.LOCATION);
 					data_json.put("branchId", Constants.BRANCH_ID);
 					data_json.put("action", "deleteSchedule");
 					data_json.put("startingTime", starttime.getText().toString());
 					data_json.put("finishingTime", endtime.getText().toString());
 					data_json.put("selectedDate", date);
 					StringEntity data_string = new StringEntity(data_json.toString());
 					post.setEntity(data_string);
 					post.setHeader("dataType", "json");
 					HttpResponse response = client.execute(post);
 				} catch(Exception e) {
 					//TODO: handle error
 				}
 			}
 		}).start();
     }
 		
 	/**Populates the schedule with clickable events
 	 * 
 	 * @author nicholasmostowich
 	 *
 	 */
     private class PopulateScheduleTask extends AsyncTask<Void, Boolean, String> {
     	
     	private ProgressDialog dialog; //used for the "Loading..." message
 		
     	@Override
 		protected String doInBackground(Void... params) {
 			publishProgress(true);
 			HttpClient client = new DefaultHttpClient();
 	        HttpPost post = new HttpPost(Constants.POST_URL);
 	    	JSONObject request = new JSONObject();
 	    	SharedPreferences auth_stuff = getSharedPreferences(Constants.SHARED_PREFS_FILE, MODE_PRIVATE);
 	        String string = auth_stuff.getString("authstring", " " + Constants.AUTH_SPLITTER + " ");
 	        string = string.split(Constants.AUTH_SPLITTER)[0];
 	    	try {
 	        	request.put("action", "showSchedule");
 	        	request.put("branchId", Constants.BRANCH_ID);
 	        	request.put("organizationId", Constants.ORGANIZATION_ID);
 	        	request.put("locationCode", Constants.LOCATION);
 	        	request.put("userName", string);
 	        	StringEntity data_string = new StringEntity(request.toString());
 	    		post.setEntity(data_string);
 	    		post.setHeader("dataType", "json");
 	    		
 	    		HttpResponse response = client.execute(post);
 	    		if (response.getStatusLine().getStatusCode() == 200) {
 	    			publishProgress(false);
 					BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
 					String json = reader.readLine();
 	    			return json;
 	    		}
 	    		else {
 
 	    			databaseConnectionErrorMessage();
 	    			publishProgress(false);
 	    			return null;
 	    		}
 	    	} catch (Exception e){
 	    		databaseConnectionErrorMessage();
 	    		publishProgress(false);
 	    		return null;
 	    	}
 		}
 
 		@Override
 		protected void onProgressUpdate(Boolean... bool) {
 			super.onProgressUpdate(bool);
 	         if(bool[0]) {
 
 	        	 dialog = ProgressDialog.show(ScheduleView.this, "", 
 	                        "Loading Schedule. Please wait...", true);
 	        	 dialog.show();
 	        	 return;
 	         }else {
         	  	dialog.dismiss();
 	         }
 	     }
 
 		/*
 		@Override
 		protected void onProgressUpdate(Boolean...values){
 			super.onProgressUpdate(values);
 			Utils.errormessage("Loading...", getBaseContext());
 		}*/
 
 	      
 		
 		@Override
 		protected void onPostExecute(String json) {
 				try {
 					JSONTokener tokener = new JSONTokener(json);
 					JSONArray jsonarray = new JSONArray(tokener);
 					if (jsonarray.length() == 0) {
 						Utils.errormessage("There don't seem to be any scheduled events in our database.", getApplicationContext());
 					}
 					else{
 						//Iterate through the array of JSONObjects, populate for each
 						//Utils.errormessage("Load Complete!", getBaseContext());
 						for(int i=0; i < jsonarray.length(); i++){
 							String startTime_s = jsonarray.getJSONObject(i).getString("startTime");
 							String endTime_s = jsonarray.getJSONObject(i).getString("endTime");
 							String referenceId = jsonarray.getJSONObject(i).getString("referenceId");
 							String day = jsonarray.getJSONObject(i).getString("weekday");
 							switch (Constants.DAYS.valueOf(jsonarray.getJSONObject(i).getString("weekday"))) {
 							case Monday:
 								addScheduleEvent(startTime_s, endTime_s,
 										R.id.moncolumn, referenceId, day);
 								break;
 							case Tuesday:
 								addScheduleEvent(startTime_s, endTime_s,
 										R.id.tuecolumn, referenceId, day);
 								break;
 							case Wednesday:
 								addScheduleEvent(startTime_s, endTime_s,
 										R.id.wedcolumn, referenceId, day);
 								break;
 							case Thursday:
 								addScheduleEvent(startTime_s, endTime_s,
 										R.id.thucolumn, referenceId, day);
 								break;
 							case Friday:
 								addScheduleEvent(startTime_s, endTime_s,
 										R.id.fricolumn, referenceId, day);
 								break;
 							default:
 								//well shit
 								databaseConnectionErrorMessage();
 								break;
 							}
 						}							
 					}					
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					Log.i("CATCH HIT: ", e.getMessage());
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					Log.i("CATCH HIT: ", e.getMessage());
 				} catch (IllegalStateException e) {
 					// TODO Auto-generated catch block
 					Log.i("CATCH HIT: ", e.getMessage());
 				}
 		}
 		/** Helper to remove leading and trailing characters from a time string, ie converts '09:00:00' to '9:00'
 		 * @param string to be cleaned
 		 * @return cleaned string
 		 */
 		private String cleanTimeString(String string) {
 			if (string.charAt(0) == '0')
 				string = string.substring(1, 5);
 			else
 				string = string.substring(0,5);
 			return string;
 		}
 		
 		/** Adds an event to the screen 
 		 * 
 		 * @param startTime_s
 		 * @param endTime_s
 		 * @param columnId
 		 * @param referenceId
 		 * @param day
 		 * @throws ParseException
 		 */
 		private void addScheduleEvent(final String startTime_s, final String endTime_s,
 				int columnId, final String referenceId, final String day) throws ParseException {
 			ViewGroup col = (ViewGroup) findViewById(columnId);        
 			
 			//Create a layout to hold the event
 			LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
 			RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.timeboxborder, null, false);
 			//Add the TextView to the layout, but the function returns layout so...
 			View toptime = getLayoutInflater().inflate(R.layout.timeboxtexttop, layout);
 			//I have to use this ugly hack to get a reference to the actual TextView
 			TextView toptimetextview = (TextView) layout.getChildAt(layout.getChildCount()-1);
 			//Which lets me set the text
 			toptimetextview.setText(cleanTimeString(startTime_s));
 			
 			//Now to do it for the bottom:
 			View bottomtime = getLayoutInflater().inflate(R.layout.timeboxtextbottom, layout);
 			TextView bottomtimetextview = (TextView) layout.getChildAt(layout.getChildCount()-1);
 			bottomtimetextview.setText(cleanTimeString(endTime_s));
 			
 			
 			//Set margins and height
 				//First strings to actual time objects
 			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
 			Date startTime = df.parse(startTime_s);
 			Date endTime = df.parse(endTime_s);
 			
 				//do math and set params!
			double topmargin = startTime.getTime() - 46800000.0; //8 AM in MS
 			topmargin *= Constants.MS_TO_DP;
 			topmargin += 3;
 			topmargin *= (getResources().getDisplayMetrics().density);
 			topmargin += 0.5f;
 			
 			double height = endTime.getTime() - startTime.getTime() + 0.0;
 			height *= Constants.MS_TO_DP;
 			height -= 5.0;
 			height *= (getResources().getDisplayMetrics().density);
 			height += 0.5f;
 			
 			layout.setClickable(true);
 			layout.setOnClickListener (new View.OnClickListener() {
 				
 				@Override
 				public void onClick(final View v) {
 					onColumnClick(v, startTime_s, endTime_s);
 					
 				}
 			});
 			//set some constants for event
 			LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)height);
 			String[] layoutdata = new String[2];
 			layoutdata[0] = referenceId;
 			layoutdata[1] = day;
 			layout.setTag(layoutdata); //set data tag
 			col.addView(layout, lparams);
 			//h4x to fix margins
 			RelativeLayout.LayoutParams legitparams = (android.widget.RelativeLayout.LayoutParams) layout.getLayoutParams();
 			legitparams.topMargin = (int)topmargin;
 			legitparams.leftMargin = (int)(1*getResources().getDisplayMetrics().density + 0.5f);
 			legitparams.rightMargin = (int)(1*getResources().getDisplayMetrics().density + 0.5f);
 			return;
 		}
     }
    
 }
