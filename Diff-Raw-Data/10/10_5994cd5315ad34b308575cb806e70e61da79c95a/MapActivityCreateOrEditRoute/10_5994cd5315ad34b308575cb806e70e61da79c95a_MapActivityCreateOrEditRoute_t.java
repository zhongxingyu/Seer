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
 package no.ntnu.idi.socialhitchhiking.map;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.freerider.model.Location;
 import no.ntnu.idi.freerider.model.MapLocation;
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.model.Visibility;
 import no.ntnu.idi.freerider.protocol.JourneyRequest;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.RequestType;
 import no.ntnu.idi.freerider.protocol.Response;
 import no.ntnu.idi.freerider.protocol.ResponseStatus;
 import no.ntnu.idi.freerider.protocol.RouteRequest;
 import no.ntnu.idi.freerider.protocol.RouteResponse;
 import no.ntnu.idi.socialhitchhiking.R;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import no.ntnu.idi.socialhitchhiking.utility.DateChooser;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 
 /**
  * The activity where a user creates or edits a {@link Route}. 
  */
 public class MapActivityCreateOrEditRoute extends MapActivityAbstract{
 	
 	//adda
 	private FrameLayout AddDestFrameLayout;
 	private LinearLayout sclLayout;
 	private ArrayList<InitDestFrame> acList;
 	private String[] acStringList;
 	private int id = 0;
 
 	/**
 	 * This {@link CheckBox} determines whether a route should be saved or 
 	 * only be used as a "one time route".
 	 */
 	private CheckBox chk_saveRoute;
 	
 	/**
 	 * The field where the users writes where the route should end.
 	 */
 	private AutoCompleteTextView acTo;
 	
 	/**
 	 * The field where the users writes where the route should start.
 	 */
 	private AutoCompleteTextView acFrom;
 	
 	/**
 	 * The one time {@link Route} that a {@link Journey} should be created 
 	 * from (When {@link #chk_saveRoute} is not checked).
 	 */
 	private Route oneTimeRoute;
 	
 	/**
 	 * The {@link Route} that is saved, when {@link #chk_saveRoute} is checked.
 	 */
 	private Route commonRouteSelected;
 
 	
 	@Override
 	protected void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		
 		acList = new ArrayList<InitDestFrame>();
 
 		initAutocomplete();
 		initAddDestButton();
 		
 		Bundle extras = getIntent().getExtras();
 		if(extras != null){
 			inEditMode = extras.getBoolean("editMode");
 			positionOfRoute = extras.getInt("routePosition");
 		}
 		
 		chk_saveRoute = (CheckBox)findViewById(R.id.checkBoxSave);
 		Button button = ((Button)findViewById(R.id.btnChooseRoute));
 		
 		if(inEditMode){
 			chk_saveRoute.setVisibility(View.GONE);
 			button.setText("Update the route");
 			button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
 		}
 		
 		
 		button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if(chk_saveRoute.isChecked() || inEditMode){
 					createInputDialog("Route", "Insert name of Route", false);
 				}
 				else
 					createOneTimeJourney();
 			}
 		});
 		
 	}
 	
 	protected void setLayoutParams(){
 		sclLayout.removeView(AddDestFrameLayout);
 		initAddDestButton();
 	}
 	
 	private void addToAcList(InitDestFrame dest){
 		acList.add(dest);
 	}
 	
 	private void removeFromAcList(int number){
 		for(int i=0; i<acList.size();i++){
 			if(acList.get(i).getId()==number){
 				sclLayout.removeView(acList.get(i).getFrame());
 				acList.remove(acList.get(i));
 				break;
 			}
 		}
 	}
 	
 	private String[] getStringList(){
 		
 		
 		AutoCompleteTextView acV1 = (AutoCompleteTextView) findViewById(R.id.etGoingFrom);
 		AutoCompleteTextView acV2 = (AutoCompleteTextView) findViewById(R.id.etGoingTo);
 		
 		
 		ArrayList<InitDestFrame> mid = new ArrayList<InitDestFrame>();
 		mid = getAcList();
 		
 		acStringList = new String[mid.size()+2];
 		
 		//Adds the Going from location to the list
 		
 		acStringList[0] = acV1.getText().toString();
 		
 				
 		//Adds all the locations between start/stop to the list
 		for(int i=1; i<mid.size()+1; i++){
 			
 			InitDestFrame etD1 = mid.get(i-1);
 			
 			acStringList[i] = etD1.getAcField().getText().toString();
 		}
 		
 		//Adds going To location to the list
 		acStringList[mid.size()+1] = acV2.getText().toString();
 		
 		//check the inputs
 		for(int j=0; j<acStringList.length; j++){
 			Log.e("StringList" + j, "nr:" + acStringList[j]);
 		}
 		
 		return acStringList;
 	}
 	
 	//Get/return the acArray
 	public ArrayList<InitDestFrame> getAcList(){
 		return acList;
 	}
 	
 	
 	protected void initAddDestButton(){
 		
 		//Adds/enables the FrameLayout
 		AddDestFrameLayout = new FrameLayout(this);
 		AddDestFrameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, 80));
 		AddDestFrameLayout.setEnabled(true);
 		
 		//Fills the Image Icon
 		ImageView destAddIcon = new ImageView(this);
 		destAddIcon.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
		destAddIcon.setPadding(15, 5, 0, 12);
		//destAddIcon.setPadding(left, top, right, bottom);
		destAddIcon.setImageResource(R.drawable.google_marker_thumb_mini_through);
		
 		//Adds the imageicon to the framelayout/enables it 
 		AddDestFrameLayout.addView(destAddIcon);
 		
 		//Fills/sets the text
 		TextView destAddText = new TextView(this);
 		destAddText.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
		destAddText.setPadding(60, 12, 0, 0);
 		destAddText.setTextSize(15);
 		destAddText.setText(R.string.mapViewBtnAdd);
 		
 		//Adds the text to the framelayout
 		AddDestFrameLayout.addView(destAddText);
 		
 		//Adds the framelayout to the linearlayout (in the scrollview)
 		sclLayout = (LinearLayout) findViewById(R.id.sclLayout);
 		sclLayout.addView(AddDestFrameLayout, sclLayout.getChildCount());
 		
 		//Adds a clicklistener to the frameLayout
 		AddDestFrameLayout.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				
 				//Adds a new destination field
 				initDestFrameLayout();
 				
 				//Moves the botton to the buttom
 				setLayoutParams();
 			}
 			
 		});
 	}
 	
 	//Adds a new destination field
 	protected void initDestFrameLayout(){
 		addToAcList(new InitDestFrame(id));
 		id++;
 	}
 	
 	/*
 	public void deleteFrame(InitDestFrame view){
 		
 	}
 	*/
 	
 public class InitDestFrame{
 		
 		private FrameLayout destFrameLayout;
 		private AutoCompleteTextView acAdd;
 		private ImageView destIcon;
 		private final int id;
 		private ImageView extIcon;
 
 		public InitDestFrame(final int id){
 			this.destFrameLayout = new FrameLayout(MapActivityCreateOrEditRoute.this);
 			this.acAdd = new AutoCompleteTextView(MapActivityCreateOrEditRoute.this);
 			this.destIcon = new ImageView(MapActivityCreateOrEditRoute.this);
 			this.extIcon = new ImageView(MapActivityCreateOrEditRoute.this);
 			this.id = id;
 			
 			//Adds/enables a new frameLayout
 			destFrameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
 			
 			//The acTextField, adds the autoCompleteTextView/sets it/enables it
 			FrameLayout.LayoutParams lli = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
 			lli.setMargins(0, 10, 0, 0);
 			acAdd.setLayoutParams(lli);
 			acAdd.setEms(10);
 			acAdd.setHint(R.string.mapViewBtnAdd);
 			acAdd.setImeOptions(6);
 			acAdd.setPadding(60, 0, 0, 0);
 			acAdd.setSingleLine();
 			acAdd.setTextSize(15);
 			acAdd.setId(id);
 			acAdd.requestFocus();
 			
 			//Adds the AcTextField to the frameLayout
 			destFrameLayout.addView(acAdd);
 			
 			//The Image Icon/sets it/enables it
 			FrameLayout.LayoutParams lli2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, 16);
 			lli2.setMargins(10, 3, 0, 2);
 			destIcon.setLayoutParams(lli2);
 			destIcon.setPadding(5, 0, 0, 0);
 			destIcon.setImageResource(R.drawable.google_marker_thumb_mini_through);
 			
 			//adds the imageicon to the frameLayout
 			destFrameLayout.addView(destIcon);
 			
 			//The exit icon for closing the entire frame
 			extIcon.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, 5));
 			extIcon.setPadding(0,12,17,0);
 			extIcon.setImageResource(R.drawable.speech_bubble_overlay_close);
 			extIcon.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					removeFromAcList(id);
 				}
 			});
 			
 			//adds the exit imageicon to the framelayout
 			destFrameLayout.addView(extIcon);
 			
 			//adds the frameLayout to the linearLayout
 			sclLayout.addView(destFrameLayout);
 			
 			//adds the adapter for the textChangedListener
 			acAdd.setAdapter(adapter);
 			acAdd.addTextChangedListener(new AutoCompleteTextWatcher(MapActivityCreateOrEditRoute.this, adapter, acAdd));
 			
 			//sets the done button on the keyboard
 			acAdd.setOnEditorActionListener(new EditText.OnEditorActionListener(){
 				@Override
 				public boolean onEditorAction(TextView v, int actionId,
 						KeyEvent event) {
 					if(actionId == EditorInfo.IME_ACTION_DONE){
 						createMap();
 						return true;
 					}
 					else{
 						return false;
 					}
 				}
 			});
 		}
 		
 		public AutoCompleteTextView getAcField(){
 			return acAdd;
 		}
 		
 		public int getId(){
 			return id;
 		}
 		
 		public FrameLayout getFrame(){
 			return destFrameLayout;
 		}
 	}
 	
 	protected void createMap(){
 		//calls the MapActivityAbstract method to generate the map points/lines
 		drawPathOnMap(GeoHelper.getLocationList(getStringList()));
 	}
 	
 
 	@Override
 	protected void initContentView() {
 		setContentView(R.layout.mapactivity_create_route);
 	}
 
 	@Override
 	protected void initMapView(){
 		mapView = (MapView)findViewById(R.id.map_view);
 	}
 	
 	@Override
 	protected void initProgressBar() {
 		setProgressBar((ProgressBar)findViewById(R.id.progressBar));
 	}
 	@Override
 	public void onBackPressed() {
 		//getApp().getRoutes().set(positionOfRoute, getApp().getOldEditRoute());
 		super.onBackPressed();
 	}
 	private void createOneTimeJourney(){
 		final Response res = chooseRoute();
 		DateChooser dc = new DateChooser(this, new PropertyChangeListener() {
 
 			@Override
 			public void propertyChange(PropertyChangeEvent event) {
 				if(event.getPropertyName() == DateChooser.DATE_CHANGED){
 					if(res.getStatus() == ResponseStatus.OK){
 						sendJourneyRequest((Calendar) event.getNewValue());
 					}
 					else createConfirmDialog(false,"Journey","created","");
 				}
 			}
 		});
 		dc.show();
 	}
 	
 	/**
 	 * Initialize the {@link AutoCompleteTextView}'s with an {@link ArrayAdapter} 
 	 * and a listener ({@link AutoCompleteTextWatcher}). The listener gets autocomplete 
 	 * data from the Google Places API and updates the ArrayAdapter with these.
 	 */
 	private void initAutocomplete() {
 		adapter = new ArrayAdapter<String>(this,R.layout.item_list);
 		adapter.setNotifyOnChange(true); 
 		acFrom = (AutoCompleteTextView) findViewById(R.id.etGoingFrom);
 		acFrom.setAdapter(adapter);
 		acFrom.addTextChangedListener(new AutoCompleteTextWatcher(this, adapter, acFrom));
 		acFrom.setThreshold(1);	
 		acTo = (AutoCompleteTextView) findViewById(R.id.etGoingTo);
 		acTo.setAdapter(adapter);
 		acTo.addTextChangedListener(new AutoCompleteTextWatcher(this, adapter, acTo));
 		
 		
 		
 		acTo.setOnEditorActionListener(new EditText.OnEditorActionListener(){
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				if(actionId == EditorInfo.IME_ACTION_DONE){
 					
 					findAndDrawPath(v);
 					return true;
 				}
 				else{
 					return false;
 				}
 			}
 		});
 		
 
 	}
 	
 	/**
 	 * Sets the name of the {@link Route}, and calls {@link #chooseRoute()}.
 	 */
 	private void setInputDialogResult(String name){
 		selectedRoute.setRouteName(name);
 		chooseRoute();
 	}
 
 	/**
 	 * Takes the selected/drawn {@link Route} and chooses the correct 
 	 * {@link RouteRequest} to be sent (create route, update route or create ad hoc route).
 	 */
 	private Response chooseRoute() { 
 		String action = "created";
 		boolean saveRoute = chk_saveRoute.isChecked();
 		commonRouteSelected = getApp().getSelectedRoute();
 		if(commonRouteSelected != null){
 			commonRouteSelected.setMapPoints(selectedRoute.getMapPoints());
 			commonRouteSelected.setRouteData(selectedRoute.getRouteData());
 			commonRouteSelected.setName(selectedRoute.getName());
 		}else{
 			translateRoute();
 		}
 		
 		Request req; 
 		if(inEditMode) {
 			req = new RouteRequest(RequestType.UPDATE_ROUTE, getUser(), commonRouteSelected);
 			action = "updated";
 		}
 		else if(saveRoute)req = new RouteRequest(RequestType.CREATE_ROUTE, getUser(), commonRouteSelected);
 		else req = new RouteRequest(RequestType.CREATE_AD_HOC_ROUTE, getUser(), commonRouteSelected);
 		
 		
 		try {
 			
 			Response res = RequestTask.sendRequest(req,getApp());
 			if(res.getStatus() != ResponseStatus.OK){
 				if(inEditMode){
 					String msg = res.getErrorMessage();
 					String error = "";
 					if(msg != null && msg.contains("alter") && msg.contains("active")){
 						error = "\nCan't edit a route that's connected to an active journey";
 					}
 					createConfirmDialog(false, "Route", action,error);
 				}
 				else if(saveRoute)createConfirmDialog(false,"Route",action,"");
 				commonRouteSelected = getApp().getOldEditRoute();
 				getApp().getRoutes().set(positionOfRoute, getApp().getOldEditRoute());
 				return null;
 			}
 			else{
 				if(saveRoute || inEditMode)createConfirmDialog(true,"Route",action,"");
 
 				RouteResponse r = (RouteResponse) res;
 				if(!inEditMode){
 					oneTimeRoute = r.getRoutes().get(0);
 					if(saveRoute)
 						getApp().getRoutes().add(oneTimeRoute);
 				}else{
 					oneTimeRoute = r.getRoutes().get(0);
 					getApp().getRoutes().set(positionOfRoute, oneTimeRoute);
 				}
 
 				return res;
 				
 			}
 		} catch (ClientProtocolException e) {
 			if(saveRoute)createConfirmDialog(false,"Route",action,"");
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			if(saveRoute)createConfirmDialog(false,"Route",action,"");
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private void translateRoute() {
 		List<Location> list = selectedRoute.getRouteData();
 
 		String name = "";
 		int serial = -1;
 
 		if(chk_saveRoute.isChecked() || inEditMode){
 			name = selectedRoute.getRouteName();
 			serial = selectedRoute.getSerial();
 		}
 
 		commonRouteSelected = new Route(getUser(), name, list, serial);
 		commonRouteSelected.setMapPoints(selectedRoute.getMapPoints());
 	}
 	
 	private void sendJourneyRequest(Calendar cal){
 		Journey jour = new Journey(-1);
 		jour.setRoute(oneTimeRoute);
 		jour.setStart(cal);
 		jour.setVisibility(Visibility.PUBLIC);
 		JourneyRequest req = new JourneyRequest(RequestType.CREATE_JOURNEY, getApp().getUser(), jour);
 
 		Response res = null;
 		try {
 			res = RequestTask.sendRequest(req,getApp());
 			if(res.getStatus() != ResponseStatus.OK){
 			}
 			else{
 				if(getApp().getJourneys() != null)
 					getApp().getJourneys().add(jour);
 				createConfirmDialog(true, "Journey", "created","");
 			}
 		} catch (ClientProtocolException e) {
 			createConfirmDialog(false, "Journey","created","");
 			e.printStackTrace();
 		} catch (IOException e) {
 			createConfirmDialog(false, "Journey","created","");
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void createConfirmDialog(boolean flag,String type,String action,String error){ 
 		if(flag){
 			new AlertDialog.Builder(this).
 			setTitle("Confirmed").setMessage(type+" "+action+"!").setNegativeButton("Close", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					finish();
 				}
 			}).show();
 		}
 		else{
 			new AlertDialog.Builder(this).
 			setTitle("ERROR").setMessage(type+ " not "+action+"!"+error).setNegativeButton("Close", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface arg0, int arg1) {
 				}
 
 			}).show();
 		}
 	}
 	@Override
 	/**
 	 * Creates a menu from the xml_menu.xml file.
 	 * 
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.xml_mapmenu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	/**
 	 * Defines what happens when you click a {@link MenuItem}
 	 */
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		if(item.getItemId() == R.id.mapmenu_add){
 			addPointDialog();
 			return true;
 		}
 		else if(item.getItemId() == R.id.mapmenu_clear){
 			clearMap();
 			return true;
 		}
 		else if(item.getItemId() == R.id.mapmenu_order){
 			changeOrder();
 			return true;
 		}
 		else {
 			return super.onOptionsItemSelected(item);
 		}
 		
 	}
 
 	/**
 	 * Starts an activity where the user can change the order of (or delete) the map points.
 	 */
 	private void changeOrder() {
 		if(getSelectedRoute() == null || getSelectedRoute().getMapPoints() == null || getSelectedRoute().getMapPoints().size() == 0){
 			Toast toast = Toast.makeText(this, "You must add some points first", Toast.LENGTH_LONG);
 			toast.setGravity(Gravity.BOTTOM, toast.getXOffset() / 2, toast.getYOffset() / 2);
 			toast.show();
 			return;
 		}
 		Intent dragAndDropIntent = new Intent(this, no.ntnu.idi.socialhitchhiking.map.draganddrop.DragAndDropListActivity.class);
 		getApp().setSelectedMapRoute(selectedRoute);
 		dragAndDropIntent.putExtra("type", "changeOrder");
 		dragAndDropIntent.putExtra("editMode", inEditMode);
 		dragAndDropIntent.putExtra("routePosition", positionOfRoute);
 		startActivity(dragAndDropIntent);
 		finish();
 	}
 	
 	private void clearMap() {
 		Intent newClearedMap = new Intent(this, no.ntnu.idi.socialhitchhiking.map.MapActivityCreateOrEditRoute.class);
 		newClearedMap.putExtra("latitudeE6", mapView.getMapCenter().getLatitudeE6());
 		newClearedMap.putExtra("longitudeE6", mapView.getMapCenter().getLongitudeE6());
 		newClearedMap.putExtra("zoomLevel", mapView.getZoomLevel());
 		newClearedMap.putExtra("clear", true);
 		startActivity(newClearedMap);
 		finish();
 	}
 	
 	private void addPointDialog(){
 		createInputDialog("Add point","Add a point by writing the address",true);
 	}	
 	
 	private void createInputDialog(String title,String msg, boolean autoComplete){
 		SocialHitchhikingDialog alert = new SocialHitchhikingDialog(title, msg, autoComplete);
 		alert.show();
 	}
 
 	private class SocialHitchhikingDialog extends AlertDialog {
 		private EditText input;
 		private MapActivityCreateOrEditRoute activity;
 
 		public SocialHitchhikingDialog(String title,String msg, boolean autoComplete) {
 			super(MapActivityCreateOrEditRoute.this);
 			activity = MapActivityCreateOrEditRoute.this;
 			setTitle(title);
 			setMessage(msg);
 
 			if(autoComplete){
 				input = new AutoCompleteTextView(getContext());
 				adapter = new ArrayAdapter<String>(getContext(), R.layout.item_list);
 				adapter.setNotifyOnChange(true); 
 				input.addTextChangedListener(new AutoCompleteTextWatcher(activity, adapter, acTo));
 				android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if(which == DialogInterface.BUTTON_POSITIVE){
 							String value = input.getText().toString();
 							if(value == "" || value.length() == 0){
 								makeToast("You have to write an address");
 							}
 							else{
 								InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
 								imm.hideSoftInputFromWindow(input.getWindowToken(),0);
 								MapLocation mapLocation = GeoHelper.getLocation(value);
 								activity.addPoint(mapLocation);
 							}	
 						}
 						else if(which == DialogInterface.BUTTON_NEGATIVE){
 							
 						}
 					}
 				};
 				setButton(DialogInterface.BUTTON_POSITIVE, "OK", listener);
 				setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", listener);
 			}
 			else{
 				input = new EditText(activity);
 				setButton(DialogInterface.BUTTON_POSITIVE, "OK", new NameInputClickListener(input));
 				setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new NameInputClickListener(input));
 				if(inEditMode){
 					input.setText(selectedRoute.getRouteName());
 				}
 			}
 			
 			setView(input);
 		}
 	}
 
 	private class NameInputClickListener implements android.content.DialogInterface.OnClickListener{
 		private MapActivityCreateOrEditRoute activity;
 		private EditText input;
 
 		public NameInputClickListener(EditText input){ 
 			this.activity = MapActivityCreateOrEditRoute.this;
 			this.input = input;
 		}
 
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			if(which == DialogInterface.BUTTON_POSITIVE){
 				String value = input.getText().toString();
 				if(value == "" || value.length() == 0)
 					activity.createInputDialog("ERROR","Name can't be empty", false);
 				else{
 					InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(input.getWindowToken(),0);
 					activity.setInputDialogResult(value);
 				}	
 			}
 			else if(which == DialogInterface.BUTTON_NEGATIVE){
 				activity.makeToast("The route was not saved");
 			}
 		}
 	}
 
 	/**
 	 * When the user long presses on the screen, a dialog should pop up
 	 * where he/she is asked to add the point/address to the route.
 	 */
 	@Override
 	public void onLongPress(MotionEvent e) {
 		GeoPoint gp = mapView.getProjection().fromPixels(
 				(int) e.getX(),
 				(int) e.getY());
 		MapLocation mapLocation = (MapLocation) GeoHelper.getLocation(gp);
 
 		addPoint(mapLocation);
 	} 
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 
 }
