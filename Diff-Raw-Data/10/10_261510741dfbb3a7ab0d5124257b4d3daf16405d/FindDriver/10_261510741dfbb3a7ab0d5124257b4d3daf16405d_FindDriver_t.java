 /**
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @version: 		1.0
  *
  * Copyright (C) 2012 Freerider Team.
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  */
 package no.ntnu.idi.socialhitchhiking.findDriver;
 
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.ExecutionException;
 
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.freerider.model.Location;
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.protocol.JourneyResponse;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.ResponseStatus;
 import no.ntnu.idi.freerider.protocol.SearchRequest;
 import no.ntnu.idi.socialhitchhiking.R;
 import no.ntnu.idi.socialhitchhiking.SocialHitchhikingApplication;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import no.ntnu.idi.socialhitchhiking.map.AutoCompleteTextWatcher;
 import no.ntnu.idi.socialhitchhiking.map.GeoHelper;
 import no.ntnu.idi.socialhitchhiking.map.MapRoute;
 import no.ntnu.idi.socialhitchhiking.utility.DateChooser;
 import no.ntnu.idi.socialhitchhiking.utility.GpsHandler;
 import no.ntnu.idi.socialhitchhiking.utility.JourneyAdapter;
 import no.ntnu.idi.socialhitchhiking.utility.SocialHitchhikingActivity;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Looper;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * This is the activity where the hitchhiker search for available drivers. The hitchhiker enters where he/she
  * is going from, where he/she is going to and what date he/she wants to depart. By pressing the search button a search is started and
  * the result is shown in a list. By clicking on a driver in the list the hitchhiker will get extra information
  * about the driver and the drivers journey. The hitchhiker can then define pickup and dropoff points, and send a request 
  * to join. If desirable a comment can also be sent with the request.
  * 
  * @author Pl
  * @author Christian Thurmann-Nielsen
  * @author Thomas Gjerde
  * @author Kristoffer Aulie
  *
  */
 public class FindDriver extends SocialHitchhikingActivity implements PropertyChangeListener{
 
 	protected AutoCompleteTextView searchTo;
 	protected AutoCompleteTextView searchFrom;
 	protected Cursor cursor;
 	protected ListAdapter adapter;
 	private Button search;
 	ArrayAdapter<String> adapter2;
 	protected ListView driverList;
 	private Journey selectedJourney;
 	List<Journey> journeys;
 	private DateChooser dc;
 	private Calendar searchDate;
 	private Location goingFrom;
 	private Location goingTo;
 	private Button btnUpcoming;
 	private boolean upcoming;
 	private Button btnSpecifyDate;
 	private OnDateSetListener odsl;
 	private ProgressDialog loadingDialog;
 	private ProgressDialog searchingDialog;
 	private ProgressDialog searchingDialog2;
 	private ArrayList<PreviousSearch> previousSearch;
 	private ArrayAdapter<PreviousSearch> previousAdapter;
 	int numDays = 1; //Number of days in the future to search for, 1 being today
 	/**
 	 * The color that pickup and dropoff button should have when they have been selected. 
 	 * Only one of the buttons will have this at the same time.
 	 */
 	private int selected = Color.rgb(24, 215, 229);
 	
 	/**
 	 * The color that pickup and dropoff button should have when they are <i>not</i> selected.
 	 */
 	private int notSelected = Color.argb(200, 200, 200, 200);
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		journeys = new ArrayList<Journey>();
 		
 		
 		getApp().addPropertyListener(this);
 		setContentView(R.layout.find_driver);
 		previousSearch = getPreviousSearch();
 		previousAdapter = new ArrayAdapter<FindDriver.PreviousSearch>(this, R.layout.simple_spinner_with_newline);
 		previousAdapter.add(new PreviousSearch("[Select previous search]", ""));
 		for(int i = 0; i < previousSearch.size(); i++) {
 			if(previousSearch.get(i).getFrom() != null) {
 				previousAdapter.add(previousSearch.get(i));
 			}
 		}
 		Spinner previousSpinner = (Spinner)findViewById(R.id.previous);
 		previousSpinner.setAdapter(previousAdapter);
 		previousSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
 		{
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3)
 			{
 				if(arg2 != 0){
 					PreviousSearch ps = (PreviousSearch)arg0.getItemAtPosition(arg2);
 					searchFrom.setText(ps.getFrom());
 					searchTo.setText(ps.getTo());
 				}
 				
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0)
 			{
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 		searchTo = (AutoCompleteTextView) findViewById (R.id.search2);
 		searchFrom = (AutoCompleteTextView) findViewById (R.id.searchText);
 
 		search = (Button) findViewById(R.id.searchButton);
 		driverList = (ListView) findViewById (R.id.list);
 		dc = new DateChooser(this, this);
 		dc.setTitle("Set Search Date", "Set Search Time");
 		searchDate = Calendar.getInstance();
 		driverList.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position,
 					long id) {
 				selectedJourney = (Journey) driverList.getAdapter().getItem(position);
 				Route sr = selectedJourney.getRoute();
 				Intent intent = new Intent(FindDriver.this, no.ntnu.idi.socialhitchhiking.map.MapActivityAddPickupAndDropoff.class);
 				intent.putExtra("journey", true);
 				intent.putExtra("searchFrom", (Serializable)goingFrom);
 				intent.putExtra("searchTo", (Serializable)goingTo);
 				intent.putExtra("pickupString", searchFrom.getText().toString());
 				intent.putExtra("dropoffString", searchTo.getText().toString());
 				MapRoute mr = new MapRoute(sr.getOwner(), sr.getName(), sr.getSerial(), sr.getMapPoints());
 				getApp().setSelectedMapRoute(mr);
 				getApp().setSelectedJourney(selectedJourney);
 				startActivity(intent);
 				showLoad();
 			}
 
 		});
 		initAutocomplete();
 		search.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//Log.e("Clicked","Search");
 				if(searchFrom.getText().toString().equals("") && searchTo.getText().toString().equals("")){
 					Toast.makeText(FindDriver.this, "Please enter origin and destination", Toast.LENGTH_SHORT).show();
 				}
 				else
 				{
 
 					checkAccess();
 					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(searchFrom.getWindowToken(), 0);
 					imm.hideSoftInputFromWindow(searchTo.getWindowToken(), 0);
 				}
				previousSearch = getPreviousSearch();
				previousAdapter.clear();
				
				previousAdapter.add(new PreviousSearch("[Select previous search]", ""));
				for(int i = 0; i < previousSearch.size(); i++) {
					if(previousSearch.get(i).getFrom() != null) {
						previousAdapter.add(previousSearch.get(i));
					}
				}
 			}
 		});
 
 		odsl = new OnDateSetListener() {
 
 			@Override
 			public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
 				searchDate.set(arg0.getYear(), arg0.getMonth(), arg0.getDayOfMonth());
 				btnSpecifyDate.setText(arg0.getDayOfMonth()+"/"+(arg0.getMonth()+1)+"/"+arg0.getYear());
 			}
 			
 		};
 		
 		btnUpcoming = (Button)findViewById(R.id.btnUpcoming);
 		btnSpecifyDate = (Button)findViewById(R.id.btnPickDate);
 		btnUpcoming.setBackgroundColor(selected);
 		btnSpecifyDate.setBackgroundColor(notSelected);
 		upcoming = true;
 	}
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		try{
 			searchingDialog2.dismiss();
 		}catch (Exception e){
 			
 		}
 		
 	}
 
 	public void showLoad(){
 		searchingDialog2 = ProgressDialog.show(this, "Displaying route", "Please wait...");
 	}
 	
 	/**
 	 * Setting the Upcoming button as selected, and deselects the Specify Date button.
 	 * @param view
 	 */
 	public void btnUpcomingClicked(View view){
 		btnUpcoming.setBackgroundColor(selected);
 		btnSpecifyDate.setBackgroundColor(notSelected);
 		upcoming = true;
 		btnSpecifyDate.setText(R.string.specific_date);
 		
 	}
 	/**
 	 * Setting the Spesify Date button as selected, and deselects the Upcoming button.
 	 * @param view
 	 */
 	public void btnPickDateClicked(View view){
 		btnSpecifyDate.setBackgroundColor(selected);
 		btnUpcoming.setBackgroundColor(notSelected);
 		upcoming = false;
 		
 		Calendar cal=Calendar.getInstance();
 	    DatePickerDialog datePickDiag=new DatePickerDialog(FindDriver.this,odsl,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
 	    datePickDiag.show();
 	}
 	
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		//Returns from mapmode:
 		if(resultCode == 7331){
 			Serializable ser1 = data.getSerializableExtra("goingFrom");
 			Serializable ser2 = data.getSerializableExtra("goingTo");
 			if(ser1 != null && ser1 instanceof Location){
 				goingFrom = (Location) ser1;
 				if(goingFrom.getAddress() != null && goingFrom.getAddress().length() > 0){
 					searchFrom.setText(goingFrom.getAddress());
 				}
 			}
 			if(ser2 != null && ser2 instanceof Location){
 				goingTo = (Location) ser2;
 				if(goingTo.getAddress() != null && goingTo.getAddress().length() > 0){
 					searchTo.setText(goingTo.getAddress());
 				}
 			}
 			search.performClick();
 		}
 	}
 	/**
 	 * Method called by the PropertyChangeListener when the application gets a
 	 * new access token from facebook.
 	 * @deprecated
 	 */
 	private void relogin(){
 		if(sendLoginRequest()){
 			//chooseDate();
 		}
 	}
 	/**
 	 * Search for journeys on the server and updates the list of journeys accordingly.
 	 */
 	private void doSearch(){
 
 		searchingDialog = ProgressDialog.show(this, "Searching", "Searching");
 		PreviousSearch tempPrev = new PreviousSearch(searchFrom.getText().toString(), searchTo.getText().toString());
 		// Checking if the previous search already exists.
 		boolean isEqual = false;
 		for (int i = 1; i < previousSearch.size(); i++) {
 			if(previousSearch.get(i).getFrom() != null){
 				if(previousSearch.get(i).getFrom().equals(tempPrev.getFrom()) || previousSearch.get(i).getTo().equals(tempPrev.getTo())){
 					isEqual = true;
 				}
 			}
 		}
 		if(!isEqual){
 			previousSearch.add(tempPrev);
 			setPreviousSearch();
 		}
 		new RideSearch().execute();
 	}
 
 	/**
 	 * Checks whether the current access token from Facebook is still valid.
 	 * Tries to acquire a new access token if it's not valid, else, a search is made.
 	 */
 	private void checkAccess(){
 
 		if(!getApp().getMain().isSession()){
 			getApp().getMain().getAccess();
 		}
 		else {
 			doSearch();
 		}
 		getApp().getMain();
 	}
 
 	/**
 	 * Sends a search request to the server, with start and stop locations.
 	 * Creates a dialog if the request is not sent.
 	 * 
 	 * @return List of Journeys
 	 */
 	private List<Journey> search() {
 		JourneyResponse res = null;
 		try{
 
 				goingFrom = GeoHelper.getLocation(searchFrom.getText().toString());
 			
 				goingTo = GeoHelper.getLocation(searchTo.getText().toString());
 
 		} 
 		catch(IndexOutOfBoundsException e){
 
 		}
 		Calendar cal = Calendar.getInstance();
 		if(searchDate != null)
 			cal = searchDate;
 		Request req = new SearchRequest(getApp().getUser() , goingFrom, goingTo,cal, numDays);
 
 		try {
 			res = (JourneyResponse) RequestTask.sendRequest(req,getApp());
 
 			if(res.getStatus() != ResponseStatus.OK){
 				
 			}
 
 		} catch (MalformedURLException e) {
 			createAlertDialog(this, false, "Search request","sent", "MalformedURLException");
 			e.printStackTrace();
 		} catch (IOException e) {
 			createAlertDialog(this, false, "Search request","sent", "IOException");
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			Log.e("feilmelding: ", e.getCause().toString());
 			e.printStackTrace();
 		}   
 		return res.getJourneys();
 	}
 
 	/**
 	 * Initialize the {@link AutoCompleteTextView}'s with an {@link ArrayAdapter} 
 	 * and a listener ({@link AutoCompleteTextWatcher}). The listener gets autocomplete 
 	 * data from the Google Places API and updates the ArrayAdapter with these.
 	 */
 	public void initAutocomplete() {
 		adapter2 = new ArrayAdapter<String>(this,R.layout.item_list);
 		adapter2.setNotifyOnChange(true); 
 
 		searchFrom = (AutoCompleteTextView) findViewById(R.id.searchText);
 		searchFrom.setAdapter(adapter2);
 		searchFrom.addTextChangedListener(new AutoCompleteTextWatcher(this, adapter2, searchFrom));
 		searchFrom.setThreshold(1);
 
 		searchTo = (AutoCompleteTextView) findViewById(R.id.search2);  
 		searchTo.setAdapter(adapter2);
 		searchTo.addTextChangedListener(new AutoCompleteTextWatcher(this, adapter2, searchTo));
 
 	}
 	/**
 	 * Called when GPS has determined the user's current location
 	 * Converts coordinates to address and puts the result in the Origin field
 	 * @param location The user's current location
 	 */
 	public void gotLocation(android.location.Location location) {
 		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
 		try {
 			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
 			searchFrom.setText(addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAddressLine(1));
 		} catch (IOException e) {
 			//Log.e("IOError",e.getMessage());
 		}
 		loadingDialog.dismiss();
 	}
 	/**
 	 * Handles the click event when the current location button is clicked
 	 * Starts {@link GpsHandler}, shows a loading screen while waiting for result
 	 * and stops {@link GpsHandler} if no result is returned withing 60 seconds
 	 * @param view
 	 */
 	public void onGpsClicked(View view) {
 		 final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
 
 		    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
 		        Toast.makeText(this, "GPS is not activated", Toast.LENGTH_LONG);
 		    }
 		final GpsHandler gps = new GpsHandler(this);
 		gps.findLocation();
 		loadingDialog = ProgressDialog.show(this, "Locating", "Finding your location");
 		new Thread() {
 			public void run() {
 				try {
 					sleep(60000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				loadingDialog.dismiss();
 				gps.abortGPS();
 			}
 		}.start();
 
 	}
 	/**
 	 * @deprecated
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent ev) {
 		if(ev.getPropertyName() == SocialHitchhikingApplication.ACCESS_TOKEN){
 			relogin();
 		}
 		if(ev.getPropertyName() == DateChooser.DATE_CHANGED){
 			if(ev.getNewValue() != null)searchDate = (Calendar) ev.getNewValue();
 			doSearch();
 		}
 	}
 	
 	public void setPreviousSearch() {
 		SharedPreferences settings = getSharedPreferences("PreviousSearch", 0);
 		SharedPreferences.Editor editor = settings.edit();
 		for(int i = 0; i < 5; i++) {
 			editor.putString("from_" + i, previousSearch.get((previousSearch.size() -1) - i).getFrom());
 			editor.putString("to_" + i, previousSearch.get((previousSearch.size() -1) - i).getTo());
 		}
 		editor.commit();
 	}
 	public ArrayList<PreviousSearch> getPreviousSearch(){
 		SharedPreferences settings = getSharedPreferences("PreviousSearch", 0);
 		ArrayList<PreviousSearch> ret = new ArrayList<FindDriver.PreviousSearch>();
 		for(int i = 4; i >= 0; i--) {
 			PreviousSearch tempPrev = new PreviousSearch();
 			tempPrev.setFrom(settings.getString("from_" + i, null));
 			tempPrev.setTo(settings.getString("to_" + i, null));
 			ret.add(tempPrev);
 		}
 		return ret;
 	}
 	private class PreviousSearch {
 		private String from;
 		private String to;
 		public PreviousSearch(){
 			
 		}
 		public PreviousSearch(String from, String to){
 			this.from = from;
 			this.to = to;
 		}
 		public String getFrom(){
 			return from;
 		}
 		public void setFrom(String from){
 			this.from = from;
 		}
 		public String getTo(){
 			return to;
 		}
 		public void setTo(String to){
 			this.to = to;
 		}
 		@Override
 		public String toString()
 		{
 			if(to.equals("")){
 				return from;
 			}
 			else{
 				return "From: " + from + "\nTo: " + to;
 			}
 			
 		}
 		
 	}
 	private class RideSearch extends AsyncTask<Void, Integer, String> {
 
 		@Override
 		protected String doInBackground(Void... params) {
 			//Looper.prepare();
 			//journeys = null;
 			try {
 				if(upcoming) {
 					numDays = 7;
 					Calendar c = Calendar.getInstance();
 					searchDate.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
 					journeys = search();
 					//Log.e("Upcoming","Yes");
 					/*
 					Calendar c = Calendar.getInstance();
 					journeys = new ArrayList<Journey>();
 					for(int i = 0; i < 7; i++) {
 						searchDate.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
 						List<Journey> tempList = search();
 						for(int j = 0; j < tempList.size(); j++) {
 							journeys.add(tempList.get(j));
 						}
 						c.add(Calendar.DAY_OF_MONTH, 2); //2?
 					}
 					*/
 				} else {
 					numDays = 1;
 					journeys = search();
 				}
 				if(journeys.size() == 0) {
 					//Toast.makeText(FindDriver.this, "No rides matched your search", Toast.LENGTH_LONG);
 				}
 				
 			} catch (NullPointerException e) {
 				//Toast toast = Toast.makeText(FindDriver.this, "ERROR in receiving journeys", Toast.LENGTH_LONG);
 				//toast.show();
 				//Log.e("Error",e.getMessage());
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			if(journeys.size() == 0){
 				Toast.makeText(FindDriver.this, "No rides found", Toast.LENGTH_SHORT).show();
 			}
 			driverList.setAdapter(new JourneyAdapter(FindDriver.this, 0, journeys){
 
 				public View getView(int position, View convertView, ViewGroup parent) {
 					View row;
 					try
 					{
 						Journey current = this.getItem(position);
 						LayoutInflater inflater = getLayoutInflater();
 						//sends the strings with ride info to your_journey_list_item.xml 
 						row = inflater.inflate(R.layout.your_journey_list_item, parent, false);
 						TextView owner = (TextView)row.findViewById(R.id.your_journey_owner);
 						TextView visibility = (TextView)row.findViewById(R.id.your_journey_item_visibility);
 						TextView startTime = (TextView)row.findViewById(R.id.your_journey_item_starttime);
 						TextView start = (TextView)row.findViewById(R.id.your_journey_item_start);
 						TextView stop = (TextView)row.findViewById(R.id.your_journey_item_stop);
 						
 						int c=0;
 						switch (current.getVisibility()) {
 						case FRIENDS:
 							c = Color.GREEN;
 							break;
 						case FRIENDS_OF_FRIENDS:
 							c = Color.YELLOW;
 							break;
 						case PUBLIC:
 							c = Color.rgb(255, 128, 0);
 							break;
 						default:
 							break;
 						}
 						
 						String date = current.getStart().getTime().toString();
 						if(date.length()>0){
 							date = date.replaceAll("CET", "");
 							date = date.replaceAll("CEST", "");
 						}
 						else{
 							date = "no date";
 						}
 						
 						
 						owner.setText(current.getRoute().getOwner().getFullName());
 						visibility.setText(current.getVisibility().getDisplayName());
 						visibility.setTextColor(c);
 						startTime.setText(Html.fromHtml("<b>" + "Date: " +"</b>\t" + date));
 						start.setText(Html.fromHtml("<b>" + "From: "+"</b> " + current.getRoute().getStartAddress()));
 						stop.setText(Html.fromHtml("<b>" + "To: "+"</b>\t\t" + current.getRoute().getEndAddress()));
 
 					}
 					catch(NullPointerException e)
 					{
 					 row = convertView;
 					}
 					return row;
 				}
 
 				
 			});
 			//for(int i = 0; i < journeys.size(); i++) {
 				//Log.e("Stuff",journeys.get(i).getDriver().getFirstName());
 			//}
 			//Log.e("Freq",Integer.toString(journeys.get(0).getRoute().getFrequency()));
 			searchingDialog.dismiss();
 		}
 		
 	}
 }
