 /**
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @version: 		1.0
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
 package no.ntnu.idi.socialhitchhiking.map;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import no.ntnu.idi.freerider.model.Car;
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.freerider.model.Location;
 import no.ntnu.idi.freerider.model.MapLocation;
 import no.ntnu.idi.freerider.model.Notification;
 import no.ntnu.idi.freerider.model.NotificationType;
 import no.ntnu.idi.freerider.model.TripPreferences;
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.freerider.protocol.CarRequest;
 import no.ntnu.idi.freerider.protocol.CarResponse;
 import no.ntnu.idi.freerider.protocol.NotificationRequest;
 import no.ntnu.idi.freerider.protocol.PreferenceRequest;
 import no.ntnu.idi.freerider.protocol.PreferenceResponse;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.RequestType;
 import no.ntnu.idi.freerider.protocol.Response;
 import no.ntnu.idi.freerider.protocol.ResponseStatus;
 import no.ntnu.idi.freerider.protocol.UserResponse;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.content.Context;
 import android.content.res.Resources.NotFoundException;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.RatingBar;
 import android.widget.TabHost;
 import android.widget.TextView;
 import no.ntnu.idi.socialhitchhiking.R;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 /**
  * The activity used when a user should select where he/she wants to be picked up (pickup point), 
  * and where he/she wants to be dropped off (dropoff point). 
  *
  */
 public class MapActivityAddPickupAndDropoff extends MapActivityAbstract{
 
 	/**
 	 * When this field is true, the user should select a pickup point, by touching the map.
 	 */
 	private boolean isSelectingPickupPoint = false;
 	
 	/**
 	 * When this field is true, the user should select a dropoff point, by touching the map.
 	 */
 	private boolean isSelectingDropoffPoint = false;
 	
 	/**
 	 * The button to be pressed when the user should select a pickup point. 
 	 */
 	private Button btnSelectPickupPoint;
 	
 	/**
 	 * The button to be pressed when the user should select a dropoff point.
 	 */
 	private Button btnSelectDropoffPoint;
 	
 	/**
 	 * The button to press for sending a request.
 	 */
 	private Button btnSendRequest;
 	
 	/**
 	 * The selected pickup point.
 	 */
 	private Location pickupPoint;
 	
 	/**
 	 * The selected dropoff point.
 	 */
 	private Location dropoffPoint;
 	
 	/**
 	 * The color that pickup and dropoff button should have when they have been selected. 
 	 * Only one of the buttons will have this at the same time.
 	 */
 	private int selected = Color.argb(200, 170, 170, 250);
 	
 	/**
 	 * The color that pickup and dropoff button should have when they are <i>not</i> selected.
 	 */
 	private int notSelected = Color.argb(200, 200, 200, 200);
 	
 	/**
 	 * The {@link Overlay} that is used for drawing the pickup point.
 	 * Is null when no pickup point is selected.
 	 */
 	private Overlay overlayPickupThumb = null;
 	
 	/**
 	 * The {@link Overlay} that is used for drawing the dropoff point.
 	 * Is null when no dropoff point is selected.
 	 */
 	private Overlay overlayDropoffThumb = null;
 	
 	/**
 	 * The {@link ImageView} that is used to contain the profile picture of the driver.
 	 */
 	private ImageView picture;
 	
 	/**
 	 * The {@link AutoCompleteTextView} where the user can enter a pickup address
 	 */
 	private AutoCompleteTextView acPickup;
 	/**
 	 * The {@link AutoCompleteTextView} where the user can enter a dropoff address
 	 */
 	private AutoCompleteTextView acDropoff;
 	
 	
 	@Override
 	protected void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		
 		// Getting the selected journey
 		Journey journey = getApp().getSelectedJourney();
 		
 		// Setting up tabs
 		TabHost tabs = (TabHost)findViewById(R.id.tabhost);
 		tabs.setup();
 		
 		// Adding Ride tab
 		TabHost.TabSpec specRide = tabs.newTabSpec("tag1");
 		specRide.setContent(R.id.ride_tab);
 		specRide.setIndicator("Ride");
 		tabs.addTab(specRide);
 		
 		// Adding Driver tab
 		TabHost.TabSpec specDriver = tabs.newTabSpec("tag2");
 		specDriver.setContent(R.id.driver_tab);
 		specDriver.setIndicator("Driver");
 		tabs.addTab(specDriver);
 		
 		// Adding the pickup location address text
 		((AutoCompleteTextView)findViewById(R.id.pickupText)).setText(getIntent().getExtras().getString("pickupString"));
 				
 		// Adding the dropoff location address text
 		((AutoCompleteTextView)findViewById(R.id.dropoffText)).setText(getIntent().getExtras().getString("dropoffString"));
 		
 		
 		// Drawing the pickup and dropoff locations on the map
 		setPickupLocation();
 		setDropOffLocation();
 		
 		// Adding image of the driver
 		User driver = journey.getRoute().getOwner();
 		picture = (ImageView) findViewById(R.id.mapViewPickupImage);
 		
 		// Create an object for subclass of AsyncTask
         GetImage task = new GetImage(picture);
         // Execute the task: Get image from url and add it to the ImageView
         task.execute(driver.getPictureURL());
 		
 		// Adding the name of the driver
 		((TextView)findViewById(R.id.mapViewPickupTextViewName)).setText(driver.getFullName());
 		
 		// Getting the drivers preferences
 		TripPreferences pref = new TripPreferences(777, true, true, true, true, true);
 		pref.setPrefId(1); //Dummy data
 		Request req = new PreferenceRequest(RequestType.GET_PREFERENCE, driver, pref);
 		PreferenceResponse res = null;
 		try {
 			res = (PreferenceResponse) RequestTask.sendRequest(req,getApp());
 		} catch (ClientProtocolException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (ExecutionException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		// Setting the smoking preference
 		if(res.getPreferences().getSmoking()){
 			((ImageView)findViewById(R.id.mapViewPickupImageViewSmokingIcon)).setImageResource(R.drawable.green_check);
 		}else{
 			((ImageView)findViewById(R.id.mapViewPickupImageViewSmokingIcon)).setImageResource(R.drawable.red_cross);
 		}
 		// Setting the animals preference
 		if(res.getPreferences().getAnimals()){
 			((ImageView)findViewById(R.id.mapViewPickupImageViewAnimalsIcon)).setImageResource(R.drawable.green_check);
 		}else{
 			((ImageView)findViewById(R.id.mapViewPickupImageViewAnimalsIcon)).setImageResource(R.drawable.red_cross);
 		}
 		// Setting the breaks preference
 		if(res.getPreferences().getBreaks()){
 			((ImageView)findViewById(R.id.mapViewPickupImageViewBreaksIcon)).setImageResource(R.drawable.green_check);
 		}else{
 			((ImageView)findViewById(R.id.mapViewPickupImageViewBreaksIcon)).setImageResource(R.drawable.red_cross);
 		}
 		// Setting the music preference
 		if(res.getPreferences().getMusic()){
 			((ImageView)findViewById(R.id.mapViewPickupImageViewMusicIcon)).setImageResource(R.drawable.green_check);
 		}else{
 			((ImageView)findViewById(R.id.mapViewPickupImageViewMusicIcon)).setImageResource(R.drawable.red_cross);
 		}
 		// Setting the talking preference
 		if(res.getPreferences().getTalking()){
 			((ImageView)findViewById(R.id.mapViewPickupImageViewTalkingIcon)).setImageResource(R.drawable.green_check);
 		}else{
 			((ImageView)findViewById(R.id.mapViewPickupImageViewTalkingIcon)).setImageResource(R.drawable.red_cross);
 		}
 		
 		// Setting the number of available seats
 		((TextView)findViewById(R.id.mapViewPickupTextViewSeats)).setText(res.getPreferences().getSeatsAvailable() + " available seats");
 		
 		// Setting the age of the driver
 		((TextView)findViewById(R.id.mapViewPickupTextViewAge)).setText("Age: " + driver.getAge());
 		
 		// Adding the gender of the driver
 		if(driver.getGender() != null){
 			if(driver.getGender().equals("m")){
 				((ImageView)findViewById(R.id.mapViewPickupImageViewGender)).setImageResource(R.drawable.male);
 			}else if(driver.getGender().equals("f")){
 				((ImageView)findViewById(R.id.mapViewPickupImageViewGender)).setImageResource(R.drawable.female);
 			}
 		}
 		
 		// Setting the drivers mobile number
 		((TextView)findViewById(R.id.mapViewPickupTextViewPhone)).setText("Mobile: " + driver.getPhone());
 		
 		
 		// Getting the car image
 		Car car = new Car(driver.getCarId(),"Dummy",0.0); //"Dummy" and 0.0 are dummy vars. getApp() etc sends the current user's carid
 		Request carReq = new CarRequest(RequestType.GET_CAR, getApp().getUser(), car);
 		try {
 			CarResponse carRes = (CarResponse) RequestTask.sendRequest(carReq,getApp());
 			Bitmap carImage = BitmapFactory.decodeByteArray(carRes.getCar().getPhoto(), 0, carRes.getCar().getPhoto().length);
 			// Setting the car image
 			((ImageView)findViewById(R.id.mapViewPickupImageViewCar)).setImageBitmap(carImage);
 		} catch (ClientProtocolException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (ExecutionException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		// Setting the car name
 		((TextView)findViewById(R.id.mapViewPickupTextViewCarName)).setText("Car type: " + car.getCarName());
 
 		// Setting the comfort
 		((RatingBar)findViewById(R.id.mapViewPickupAndDropoffComfortStars)).setRating((float) car.getComfort());
 		
 		// Adding the date of ride
 		Date d = journey.getStart().getTime();
 		SimpleDateFormat sdfDate = new SimpleDateFormat("MMM dd yyyy");
 		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm zzz");
 		String dateText = "Date: " + sdfDate.format(d);
 		String timeText = "Time: " + sdfTime.format(d);
 		((TextView)findViewById(R.id.mapViewPickupTextViewDate)).setText(dateText + "\n" + timeText);
 		
 		//Adding Gender to the driver
 		ImageView iv_image;
 	    iv_image = (ImageView) findViewById(R.id.gender);
 	    
 	    try {
 			if (driver.getGender().equals("Male")){
 				Drawable male = getResources().getDrawable(R.drawable.male);
 				iv_image.setImageDrawable(male);
 			}
 			else if (driver.getGender().equals("Female")){
 				Drawable female = getResources().getDrawable(R.drawable.female);
 				iv_image.setImageDrawable(female);
 			}
 		} catch (NullPointerException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	    
 		 // Initializing the two autocomplete textviews pickup and dropoff
 	    initAutocomplete();
 	    
 		// Adding onClickListener for the button "Ask for a ride"
 		btnSendRequest = (Button)findViewById(no.ntnu.idi.socialhitchhiking.R.id.mapViewPickupBtnSendRequest);
 		btnSendRequest.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Response res;
 				NotificationRequest req;
 				
 				if(pickupPoint == null || dropoffPoint == null){
 					makeToast("You have to choose pickup point and dropoff point.");
 					return;
 				}
 				
 				inPickupMode = true;
 				
 				String senderID = getApp().getUser().getID();
 				String recipientID = getApp().getSelectedJourney().getRoute().getOwner().getID();
 				String senderName = getApp().getUser().getFullName();
 				String comment = ((EditText)findViewById(R.id.mapViewPickupEtComment)).getText().toString();
 				int journeyID = getApp().getSelectedJourney().getSerial();
 				
 				// Creating a new notification to be sendt to the driver
 				Notification n = new Notification(senderID, recipientID, senderName, comment, journeyID, NotificationType.HITCHHIKER_REQUEST, pickupPoint, dropoffPoint, Calendar.getInstance());
 				req = new NotificationRequest(RequestType.SEND_NOTIFICATION, getApp().getUser(), n);
 				
 				// Sending notification
 				try {
 					res = RequestTask.sendRequest(req,getApp());
 					if(res instanceof UserResponse){
 						if(res.getStatus() == ResponseStatus.OK){
 							makeToast("Notification sent");
 							finish();
 						}
 						if(res.getStatus() == ResponseStatus.FAILED){
 							if(res.getErrorMessage().contains("no_duplicate_notifications")){
 								makeToast("You have already sent a request on this journey");
 							}
 							else{
 								makeToast("Could not send request");
 							}
 						}
 					}
 				} catch (ClientProtocolException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		});
 		
 		// Adding buttons where you choose between pickup point and dropoff point
 		btnSelectPickupPoint = (Button)findViewById(R.id.mapViewPickupBtnPickup);
 		btnSelectDropoffPoint = (Button)findViewById(R.id.mapViewPickupBtnDropoff);
 		
 		// Setting the selected pickup point
		//setSelectingPickupPoint();
 		btnSelectPickupPoint.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				isSelectingDropoffPoint = false;
 				isSelectingPickupPoint = true;
 				btnSelectPickupPoint.setBackgroundColor(selected); 
 				btnSelectDropoffPoint.setBackgroundColor(notSelected);
 				makeToast("Press the map to add pickup location");
 				
 			}
 		});
 		
 		// Setting the selected dropoff point
 		btnSelectDropoffPoint.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				isSelectingDropoffPoint = true;
 				isSelectingPickupPoint = false;
 				btnSelectPickupPoint.setBackgroundColor(notSelected); 
 				btnSelectDropoffPoint.setBackgroundColor(selected);
 				makeToast("Press the map to add dropoff location");
 			}
 		});
 		
 		// Adding message to the user
 		makeToast("Please set a pickup and dropoff location.");
 		
 		/*
 		Bundle extras = getIntent().getExtras();
 		if(extras != null){
 			Serializable ser1 = extras.getSerializable("searchFrom");
 			Serializable ser2 = extras.getSerializable("searchTo");
 			if(ser1 != null && ser1 instanceof Location){
 				drawThumb((Location)ser1, true);
 			}
 			if(ser2 != null && ser2 instanceof Location){
 				drawThumb((Location)ser2, false);
 			}
 		}*/
 	}
 	
 	/**
 	 * Initialize the {@link AutoCompleteTextView}'s with an {@link ArrayAdapter} 
 	 * and a listener ({@link AutoCompleteTextWatcher}). The listener gets autocomplete 
 	 * data from the Google Places API and updates the ArrayAdapter with these.
 	 */
 	private void initAutocomplete() {
 		adapter = new ArrayAdapter<String>(this,R.layout.item_list);
 		adapter.setNotifyOnChange(true); 
 		
 		acPickup = (AutoCompleteTextView) findViewById(R.id.pickupText);
 		acPickup.setAdapter(adapter);
 		acPickup.addTextChangedListener(new AutoCompleteTextWatcher(this, adapter, acPickup));
 		acPickup.setThreshold(1);	
 		acPickup.selectAll();
 		
 		acDropoff = (AutoCompleteTextView) findViewById(R.id.dropoffText);
 		acDropoff.setAdapter(adapter);
 		//acTo.addTextChangedListener(new AutoCompleteTextWatcher(this, adapter, acTo));
 		
 		//adds the adapter for the textChangedListener
 		//acAdd.setAdapter(adapter);
 		acDropoff.addTextChangedListener(new AutoCompleteTextWatcher(this, adapter, acDropoff));
 		
 		//sets the next button on the keyboard
 		acPickup.setOnEditorActionListener(new EditText.OnEditorActionListener(){
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				if(actionId == EditorInfo.IME_ACTION_NEXT){
 					// Removes old pickup point
 					//setSelectingPickupPoint();
 					// Sets the pickup location
 					setPickupLocation();
 					// Sets focus to dropoff
 					acDropoff.requestFocus();
 					return true;
 				}
 				else{
 					return false;
 				}
 			}
 		});
 		
 		//sets the done button on the keyboard
 		acDropoff.setOnEditorActionListener(new EditText.OnEditorActionListener(){
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				if(actionId == EditorInfo.IME_ACTION_DONE){
 					// Removes old pickup point
 					//setSelectingDropoffPoint();
 					// Sets the dropoff location
 					setDropOffLocation();
 					// Sets focus to "Comment to driver"
 					((EditText)findViewById(R.id.mapViewPickupEtComment)).requestFocus();
 					return true;
 				}
 				else{
 					return false;
 				}
 			}
 		});
 	}
 	/**
 	 * Clearing the text in the pickup text field.
 	 */
 	public void clearPickupText(View v){
 		((EditText)findViewById(R.id.pickupText)).setText("");
 	}
 	/**
 	 * Clearing the text in the dropoff text field.
 	 */
 	public void clearDropoffText(View v){
 		((EditText)findViewById(R.id.dropoffText)).setText("");
 	}
 	
 	/**
 	 * Should be called when both a pickup point and a dropoff point has been selected.
 	 */
 	/*private void setDoneSelecting(){
 		isSelectingPickupPoint = false;
 		isSelectingDropoffPoint = false;
 		btnSelectPickupPoint.setBackgroundColor(notSelected); 
 		btnSelectDropoffPoint.setBackgroundColor(notSelected);
 	}*/
 
 	@Override
 	protected void initContentView() {
 		setContentView(R.layout.mapactivity_pickup_and_dropoff);
 	}
 	@Override
 	protected void initMapView() {
 		mapView = (MapView)findViewById(R.id.mapViewPickupAndDropoffMapView); 
 	}
 	@Override
 	protected void initProgressBar() {
 		setProgressBar((ProgressBar)findViewById(R.id.mapViewPickupProgressBar));
 	}
 
 	/**
 	 * This method loops trough the route path, to find out which {@link Location} is 
 	 * closest to the given {@link MapLocation}, and returns this.
 	 */
 	private Location findClosestLocationOnRoute(MapLocation ml){
 		List<Location> l = getApp().getSelectedJourney().getRoute().getRouteData();
 		
 		Location lClosest = l.get(0);
 		for (int i = 0; i < l.size(); i++) {
 			lClosest = closest(ml, lClosest, l.get(i));
 		}
 		return lClosest;
 	}
 
 	/**
 	 * Takes three parameters. Returns the one {@link Location}-parameter of the two last, that is closest 
 	 * to the first given {@link Location}.
 	 */
 	private static Location closest(Location loc, Location a, Location b){
 		android.location.Location location = new android.location.Location("");
 		location.setLatitude(loc.getLatitude());
 		location.setLongitude(loc.getLongitude());
 		android.location.Location lA = new android.location.Location("");
 		lA.setLatitude(a.getLatitude());
 		lA.setLongitude(a.getLongitude());
 		android.location.Location lB = new android.location.Location("");
 		lB.setLatitude(b.getLatitude());
 		lB.setLongitude(b.getLongitude());
 				
 		float distA = location.distanceTo(lA);
 		float distB = location.distanceTo(lB);
 		
 		if(distA < distB) return a;
 		return b;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
 	 */
 	@Override
 	public void onLongPress(MotionEvent e) {
 		// Does nothing
 	} 
 	
 	/**
 	 * Adds the pickup location to the map route.
 	 */
 	private void setPickupLocation(){
 		// Defines the AutoCompleteTextView pickupText
 		acPickup = (AutoCompleteTextView)findViewById(R.id.pickupText);
 		// Controls if the user has entered an address
 		if(!acPickup.getText().toString().equals("")){
 			// Gets the GeoPoint of the written address
 			GeoPoint pickupGeo = GeoHelper.getGeoPoint(acPickup.getText().toString());
 			// Gets the MapLocation of the GeoPoint
 			MapLocation mapLocation = (MapLocation) GeoHelper.getLocation(pickupGeo);
 			// Finds the pickup location on the route closest to the given address
 			Location temp = findClosestLocationOnRoute(mapLocation);
 		
 			//Controls if the user has entered a NEW address
 			if(pickupPoint != temp){
 				// Removes old pickup point (thumb)
 				if(overlayPickupThumb != null){
 					mapView.getOverlays().remove(overlayPickupThumb);
 					overlayPickupThumb = null;
 				}
 				mapView.invalidate();
 				
 				// If no dropoff point is specified, we add the pickup point to the map.
 				if(dropoffPoint == null){
 					pickupPoint = temp;
 					overlayPickupThumb = drawThumb(pickupPoint, true);
 					//setSelectingDropoffPoint();
 				}else{ // If a dropoff point is specified:
 					List<Location> l = getApp().getSelectedJourney().getRoute().getRouteData();
 					// Checks to make sure the pickup point is before the dropoff point.
 					if(l.indexOf(temp) < l.indexOf(dropoffPoint)){
 						makeToast("The pickup point has to be before the dropoff point");
 					}else{
 						// Adds the pickup point to the map by drawing a cross
 						pickupPoint = temp;
 						overlayPickupThumb = drawThumb(pickupPoint, true);
 						//setDoneSelecting();
 					}
 				}
 			}
 		}else{
 			makeToast("Please add a pickup address");
 		}
 	}
 	
 	/**
 	 * Adds the dropoff location to the map route.
 	 */
 	private void setDropOffLocation(){
 		// Defines the AutoCompleteTextView with the dropoff address
 		acDropoff = (AutoCompleteTextView)findViewById(R.id.dropoffText);
 		//Controls if the user has entered an address
 		if(!acDropoff.getText().toString().equals("")){
 			// Gets the GeoPoint of the given address
 			GeoPoint dropoffGeo = GeoHelper.getGeoPoint(acDropoff.getText().toString());
 			// Gets the MapLocation from the given GeoPoint
 			MapLocation mapLocation = (MapLocation) GeoHelper.getLocation(dropoffGeo);
 			// Finds the dropoff location on the route closest to the given address
 			Location temp = findClosestLocationOnRoute(mapLocation);
 			
 			// Controls if the user has entered a NEW address
 			if(dropoffPoint != temp){
 				// Removes old pickup point (thumb)
 				if(overlayDropoffThumb != null){
 					mapView.getOverlays().remove(overlayDropoffThumb);
 					overlayDropoffThumb = null;
 					//pickupPoint = null;
 				}
 				mapView.invalidate();
 				
 				// If no pickup point is specified, we add the dropoff point to the map.
 				if(pickupPoint == null){
 					dropoffPoint = temp;
 					overlayDropoffThumb = drawThumb(dropoffPoint, false);
 					//setSelectingPickupPoint();
 				}else{ // If a pickup point is specified:
 					List<Location> l = getApp().getSelectedJourney().getRoute().getRouteData();
 					// Checks to make sure the dropoff point is after the pickup point.
 					if(l.indexOf(temp) > l.indexOf(pickupPoint)){
 						makeToast("The droppoff point has to be after the pickup point");
 					}else{
 						// Adds the dropoff point to the map by drawing a cross
 						dropoffPoint = temp;
 						overlayDropoffThumb = drawThumb(dropoffPoint, false);
 						//setDoneSelecting();
 					}
 				}
 			}
 		}else{
 			makeToast("Please add a dropoff address");
 		}
 	}
 	
 	/**
 	 * When someone presses the map, this method is called and draws the pickup 
 	 * or dropoff point on the map depending on which of the {@link Button}s, 
 	 * {@link #btnSelectPickupPoint} or {@link #btnSelectDropoffPoint} that is pressed.
 	 */
 	@Override
 	public synchronized boolean onSingleTapUp(MotionEvent e) {
 		if(!isSelectingDropoffPoint && !isSelectingPickupPoint){
 			return false;
 		}
 		GeoPoint gp = mapView.getProjection().fromPixels(
 				(int) e.getX(),
 				(int) e.getY());
 		MapLocation mapLocation = (MapLocation) GeoHelper.getLocation(gp);
 		// If the user is selecting a pickup point
 		if(isSelectingPickupPoint){
 			Location temp = findClosestLocationOnRoute(mapLocation);
 			//Controls if the user has entered a NEW address
 			if(pickupPoint != temp){
 				// Removes old pickup point (thumb)
 				if(overlayPickupThumb != null){
 					mapView.getOverlays().remove(overlayPickupThumb);
 					overlayPickupThumb = null;
 				}
 				mapView.invalidate();
 				
 				// If no dropoff point is specified, we add the pickup point to the map.
 				if(dropoffPoint == null){
 					pickupPoint = temp;
 					overlayPickupThumb = drawThumb(pickupPoint, true);
 					//setSelectingDropoffPoint();
 				}else{ // If a dropoff point is specified:
 					List<Location> l = getApp().getSelectedJourney().getRoute().getRouteData();
 					// Checks to make sure the pickup point is before the dropoff point.
 					if(l.indexOf(temp) < l.indexOf(dropoffPoint)){
 						makeToast("The pickup point has to be before the dropoff point");
 					}else{
 						// Adds the pickup point to the map by drawing a thumb
 						pickupPoint = temp;
 						overlayPickupThumb = drawThumb(pickupPoint, true);
 						//setDoneSelecting();
 					}
 				}
 			}
 			// If the user is selecting a dropoff point
 		}else if(isSelectingDropoffPoint){
 			Location temp = findClosestLocationOnRoute(mapLocation);
 			// Controls if the user has entered a NEW address
 			if(dropoffPoint != temp){
 				// Removes old dropoff point (thumb)
 				if(overlayDropoffThumb != null){
 					mapView.getOverlays().remove(overlayDropoffThumb);
 					overlayDropoffThumb = null;
 					//pickupPoint = null;
 				}
 				mapView.invalidate();
 				
 				// If no pickup point is specified, we add the dropoff point to the map.
 				if(pickupPoint == null){
 					dropoffPoint = temp;
 					overlayDropoffThumb = drawThumb(dropoffPoint, false);
 					//setSelectingPickupPoint();
 				}else{ // If a pickup point is specified:
 					List<Location> l = getApp().getSelectedJourney().getRoute().getRouteData();
 					// Checks to make sure the dropoff point is after the pickup point.
 					if(l.indexOf(temp) > l.indexOf(pickupPoint)){
 						makeToast("The droppoff point has to be after the pickup point");
 					}else{
 						// Adds the dropoff point to the map by drawing a thumb
 						dropoffPoint = temp;
 						overlayDropoffThumb = drawThumb(dropoffPoint, false);
 						//setDoneSelecting();
 					}
 				}
 			}
 		}
 		return true;
 	}
 }
