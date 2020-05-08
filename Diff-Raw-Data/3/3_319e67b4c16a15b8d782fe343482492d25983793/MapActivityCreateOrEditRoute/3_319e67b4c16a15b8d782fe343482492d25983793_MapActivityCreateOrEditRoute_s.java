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
 import java.util.Locale;
 import java.util.concurrent.ExecutionException;
 
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.freerider.model.Location;
 import no.ntnu.idi.freerider.model.MapLocation;
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.model.TripPreferences;
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
 import no.ntnu.idi.socialhitchhiking.journey.ScheduleDrive;
 import no.ntnu.idi.socialhitchhiking.journey.TripOptions;
 import no.ntnu.idi.socialhitchhiking.utility.DateChooser;
 import no.ntnu.idi.socialhitchhiking.utility.GpsHandler;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.util.TypedValue;
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
 	
 	/**
 	 * The {@link FrameLayout} that contains the add destinationfield, clear/delete buttons
 	 */
 	private FrameLayout AddDestFrameLayout;
 	
 	/**
 	 * The {@link LinearLayout} containing everything in the scrollview
 	 */
 	private LinearLayout sclLayout;
 	
 	/**
 	 * ArrayList {@link ArrayList} of all the add destination fields
 	 */
 	private ArrayList<InitDestFrame> acList;
 	
 	/**
 	 * Id for the add destination objects
 	 */
 	private int id = 0;
 	
 	/**
 	 * Variable to contain the resources {@link Resources}
 	 */
 	private Resources r;
 	
 	/**
 	 * The loading dialog {@link ProgressDialog} for the gps
 	 */
 	private ProgressDialog loadingDialog;
 	
 	/**
 	 * This {@link CheckBox} determines whether a route should be saved or 
 	 * only be used as a "one time route".
 	 */
 	private CheckBox chk_saveRoute;
 	
 	/**
 	 * The {@link AutoCompleteTextView} where the users writes where the route should end.
 	 */
 	private AutoCompleteTextView acTo;
 	
 	/**
 	 * The {@link AutoCompleteTextView} where the users writes where the route should start.
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
 	
 	/**
 	 * Switch for checking if the map is up to date
 	 */
 	private boolean hasDrawn;
 
 	/**
 	 * Constructor
 	 */
 	@Override
 	protected void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		
 		acList = new ArrayList<InitDestFrame>();
 		r = getResources();
 		
 		//Calls the initAutoComplete method, adding the autocomplete listeners to acTo and acFrom
 		initAutocomplete();
 		
 		//Calls the initAddDestButton method, adding the driving through button
 		initAddDestButton();
 		
 		//Sets hasDrawn to false
 		hasDrawn = false;
 		
 		Bundle extras = getIntent().getExtras();
 		
 		if(extras != null){
 			inEditMode = extras.getBoolean("editMode");
 			positionOfRoute = extras.getInt("routePosition");
 		}
 		
 		//Hides the checkbox
 		chk_saveRoute = (CheckBox)findViewById(R.id.checkBoxSave);
 		chk_saveRoute.setVisibility(8);
 		
 		//Initialises the draw/next button
 		final Button button = ((Button)findViewById(R.id.btnChooseRoute));
 		
 		//Adjustments to the gui if in editmode
 		if(inEditMode){
 			chk_saveRoute.setVisibility(View.GONE);
 			button.setText("Update the route");
 			button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
 			fillFieldsInEdit();
 			button.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					createInputDialog("Route", "Insert name of Route", false);
 				}
 			});
 		} else{
 			button.setText("Show on map");
 			button.setEnabled(false);
 		}
 		
 		//Initialises the textviews and the clear buttons
 		final AutoCompleteTextView acFrom = (AutoCompleteTextView) findViewById(R.id.etGoingFrom);
 		final AutoCompleteTextView acTo = (AutoCompleteTextView) findViewById(R.id.etGoingTo);
 		ImageView bClearFrom = ((ImageView)findViewById(R.id.etGoingFromClearIcon));
 		ImageView bClearTo = ((ImageView)findViewById(R.id.etGoingToClearIcon));
 		
 		//If map is drawn fill the textviews
 		if(selectedRoute.getMapPoints().size() != 0){
 			fillFieldsOnClick();
 		}
 		
 		/**
 		 * onClickListener on the clearButton on the acFrom field {@link OnClickListener()}
 		 */
 		//Adds onClickListener to the clearbutton on the acFrom field
 		bClearFrom.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				acFrom.setText("");
 				button.setEnabled(false);
 				button.setText("Show on Map");
 				
 			}
 			
 		});
 		
 		/**
 		 * onClickListener on the clearButton on the acTo field {@link OnClickListener}
 		 */
 		//Adds onClickListener to the clearbutton on the acTo field
 		bClearTo.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				acTo.setText("");
 				button.setEnabled(false);
 				button.setText("Show on Map");
 				
 			}
 			
 		});
 		
 		/**
 		 * TextWatcher to the acFrom {@link autoCompleteTextView} autoCompleteTextView {@link TextWatcher()}
 		 */
 		//Adds a TextWatcher to the acFrom field, to update the draw/nextbutton, and its functionality
 		acFrom.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				hasDrawn = false;
 				if(checkFields() && selectedRoute.getMapPoints().size()>1 && hasDrawn == true){
 					button.setEnabled(true);
 					button.setText("Next");
 				}else if(checkFields() && selectedRoute.getMapPoints().size()>1 && hasDrawn == false){
 					button.setEnabled(true);
 					button.setText("Show on Map");
 					
 				}
 				else if(checkFields() && selectedRoute.getMapPoints().size() == 0){
 					button.setEnabled(true);
 					button.setText("Show on map");
 					
 				}
 				else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 					button.setText("Show on map");
 					button.setEnabled(false);
 				}
 				else if(inEditMode){
 					
 				}
 				else{
 					Log.e("IF5","vi kom hit");
 					button.setText("Show on map");
 					button.setEnabled(false);
 				}
 				
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				// TODO Auto-generated method stub
 				
 			}
 
 		});
 		
 		/**
 		 * TextWatcher to the acTo {@link autoCompleteTextView} autoCompleteTextView {@link TextWatcher()}
 		 */
 		//Adds a TextWatcher to the acFrom field, to update the draw/nextbutton, and its functionality
 		acTo.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				hasDrawn = false;
 				if(checkFields() && selectedRoute.getMapPoints().size()>2 && hasDrawn == true){
 					button.setEnabled(true);
 					button.setText("Next");
 				}else if(checkFields() && selectedRoute.getMapPoints().size()>2 && hasDrawn == false){
 					button.setEnabled(true);
 					button.setText("Show on Map");
 					
 				}
 				else if(checkFields() && selectedRoute.getMapPoints().size() == 0){
 					button.setEnabled(true);
 					button.setText("Show on map");
 					
 				}
 				else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 					button.setText("Show on map");
 					button.setEnabled(false);
 				}
 				else if(inEditMode){
 					
 				}
 				else{
 					button.setText("Show on map");
 					button.setEnabled(false);
 				}
 				
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				// TODO Auto-generated method stub
 				
 			}
 
 		});
 		
 		/**
 		 * onClickListener on the button(draw/next) {@link OnClickListener}
 		 */
 		//adds the onclickListener to the draw/next button
 		button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if(checkFields() && selectedRoute.getMapPoints().size()>1 && hasDrawn == true){
 					button.setText("Next");
 					createOneTimeJourney();
 					
 					
 				}else if(checkFields() && selectedRoute.getMapPoints().size()>1 && hasDrawn == false){
 					mapView.getOverlays().clear();
 					createMap();
 					button.setText("Next");
 					//createOneTimeJourney();
 				}
 				else if(checkFields() && selectedRoute.getMapPoints().size() == 0){
 					mapView.getOverlays().clear();
 					createMap();
 					button.setText("Next");
 					
 				}
 				else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 				}
 				else if(inEditMode){
 					createInputDialog("Route", "Insert name of Route", false);
 					button.setText("Next");
 					
 				}
 				else{
 					
 				}
 			}
 		});
 		
 		
 	}
 		
 	/** checkFields()
 	 * checks if all the fields are filled, and if the content of the fields correspond with the points on the map
 	 * @return Boolean
 	 */
 	protected boolean checkFields(){
 		AutoCompleteTextView acFrom = (AutoCompleteTextView) findViewById(R.id.etGoingFrom);
 		AutoCompleteTextView acTo = (AutoCompleteTextView) findViewById(R.id.etGoingTo);
 		
 		if((acFrom.getText().toString().equals("") || acFrom.getText().toString().equals("")) && (acTo.getText().toString().equals("") || acTo.getText().toString().equals("") && checkAddFields() == false)){
 			return false;
 		}else if(acTo.getText().toString().equals("") || acTo.getText().toString().equals("") || checkAddFields() == false){
 			//makeToast("You have to fill in the Driving from field");
 			return false;
 		}else if(acFrom.getText().toString().equals("") || acFrom.getText().toString().equals("") || checkAddFields() == false){
 			//makeToast("You have to fill in the Driving to field");
 			return false;
 		}else{
 			return true;
 		}
 	}
 	
 	/** checkAddFields()
 	 * helping method for the checkFields() method
 	 * checks the Driving Through fields
 	 * @return Boolean
 	 */
 	protected boolean checkAddFields(){
 		boolean check = true;
 		for(int i=0; i<acList.size(); i++){
 			if(acList.get(i).getAcField().getText().toString().equals("") || acList.get(i).getAcField().getText().toString().length() == 0){
 				check = false;
 			}
 		}
 		return check;
 	}
 	
 	/** fillFieldsInEdit()
 	 * fills the fields if inEditmode
 	 */
 	protected void fillFieldsInEdit(){
 		AutoCompleteTextView acFrom = (AutoCompleteTextView) findViewById(R.id.etGoingFrom);
 		AutoCompleteTextView acTo = (AutoCompleteTextView) findViewById(R.id.etGoingTo);
 		
 		acFrom.setText(selectedRoute.getMapPoints().get(0).getAddress());
 		acTo.setText(selectedRoute.getMapPoints().get(selectedRoute.getMapPoints().size()-1).getAddress());
 		
 		for(int i=1; i<selectedRoute.getMapPoints().size()-1; i++){
 			initDestFrameLayout();
 			acList.get(i-1).getAcField().setText(selectedRoute.getMapPoints().get(i).getAddress());
 			setLayoutParams();
 		}
 	}
 	
 	/** setLayoutParams()
 	 *  when a Driving Through field is removed/added, repaint the Driving Through button
 	 */
 	protected void setLayoutParams(){
 		sclLayout.removeView(AddDestFrameLayout);
 		initAddDestButton();
 	}
 	
 	/** addToAcList
 	 * when Driving Through is clicked, adds the generated field {@link dest} to acList arrayList {@link ArrayList}
 	 * @param dest
 	 */
 	private void addToAcList(InitDestFrame dest){
 		acList.add(dest);
 	}
 	
 	/** removeFromAcList(int number)
 	 * deletes a Driving Through field, both from layout {@link autoCompleteTextView} and acList {@link ArrayList}
 	 * @param number		the id of the field beeing deleted
 	 */
 	private void removeFromAcList(int number){
 		for(int i=0; i<acList.size();i++){
 			if(acList.get(i).getId()==number){
 				sclLayout.removeView(acList.get(i).getFrame());
 				acList.remove(acList.get(i));
 				break;
 			}
 		}
 	}
 	
 	/** getStringList()
 	 * gets all the points from the fields and adds them to a String[] {@link String[]}
 	 * @return String[]
 	 */
 	private String[] getStringList(){
 		String[] acStringList;
 		
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
 		return acStringList;
 	}
 	
 	/** getAcList()
 	 * getter for the acList {@link ArrayList}
 	 * @return
 	 */
 	//Get/return the acArray
 	public ArrayList<InitDestFrame> getAcList(){
 		return acList;
 	}
 	
 	/** dipToPx(int dip)
 	 * translates dip to px
 	 * @param dip
 	 * @return px
 	 */
 	public int dipToPx(int dip){
 		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
 		return (int)px;
 	}
 	
 	/** initAddDestButton()
 	 * adds the Driving Through button
 	 */
 	protected void initAddDestButton(){
 		
 		//Adds/enables the FrameLayout
 		AddDestFrameLayout = new FrameLayout(this);
 		AddDestFrameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, 80));
 		AddDestFrameLayout.setEnabled(true);
 		
 		//Fills the Image Icon
 		ImageView destAddIcon = new ImageView(this);
 		FrameLayout.LayoutParams lliDestIcon = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
 		lliDestIcon.setMargins(dipToPx(10), 0, 0, dipToPx(2));
 		destAddIcon.setLayoutParams(lliDestIcon);
 		destAddIcon.setPadding(0, dipToPx(5), 0, 0);
 		destAddIcon.setImageResource(R.drawable.google_marker_thumb_mini_through);
 		
 		//Adds the imageicon to the framelayout/enables it 
 		AddDestFrameLayout.addView(destAddIcon);
 		
 		//Fills/sets the text
 		TextView destAddText = new TextView(this);
 		FrameLayout.LayoutParams lliDest = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
 		lliDest.setMargins(0, dipToPx(5), 0, 0);
 		destAddText.setLayoutParams(lliDest);
 		destAddText.setPadding(dipToPx(40), dipToPx(6), 0, 0);
 		destAddText.setTextSize(15);
 		destAddText.setText(R.string.mapViewAcField);
 		
 		//Adds the text to the framelayout
 		AddDestFrameLayout.addView(destAddText);
 		
 		//Adds the framelayout to the linearlayout (in the scrollview)
 		sclLayout = (LinearLayout) findViewById(R.id.sclLayout);
 		sclLayout.addView(AddDestFrameLayout, sclLayout.getChildCount());
 		
 		final Button button = ((Button)findViewById(R.id.btnChooseRoute));
 		
 		//Adds a clicklistener to the frameLayout
 		AddDestFrameLayout.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				
 				//Adds a new destination field
 				initDestFrameLayout();
 				
 				//Moves the button to the bottom
 				setLayoutParams();
 				
 				if(checkFields() == false){
 					button.setEnabled(false);
 					button.setText("Show on map");
 				}else{
 					mapView.getOverlays().clear();
 					createMap();
 				}
 			}
 			
 		});
 	}
 	
 	/** initDestFrameLayout()
 	 * adds a Driving Through field when the driving through button is clicked
 	 */
 	//Adds a new destination field
 	protected void initDestFrameLayout(){
 		addToAcList(new InitDestFrame(id));
 		id++;
 	}
 	
 	/** InitDestFrame
 	 * object of the driving through field/buttons
 	 */
 	public class InitDestFrame{
 		
 		private FrameLayout destFrameLayout;
 		private AutoCompleteTextView acAdd;
 		private ImageView destIcon;
 		private final int id;
 		private ImageView extIcon;
 		private boolean checks;
 		
 		//Constructor
 		public InitDestFrame(final int id){
 			this.destFrameLayout = new FrameLayout(MapActivityCreateOrEditRoute.this);
 			this.acAdd = new AutoCompleteTextView(MapActivityCreateOrEditRoute.this);
 			this.destIcon = new ImageView(MapActivityCreateOrEditRoute.this);
 			this.extIcon = new ImageView(MapActivityCreateOrEditRoute.this);
 			this.id = id;
 			this.checks = true;
 			
 			//Adds/enables a new frameLayout
 			destFrameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
 			
 			//The acTextField, adds the autoCompleteTextView/sets it/enables it
 			FrameLayout.LayoutParams lli = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
 			lli.setMargins(0, dipToPx(8), 0, 0);
 			acAdd.setLayoutParams(lli);
 			acAdd.setEms(10);
 			acAdd.setHint(R.string.mapViewAcField);
 			acAdd.setImeOptions(6);
 			acAdd.setPadding(dipToPx(40), 0, dipToPx(55), 0);
 			acAdd.setSingleLine();
 			acAdd.setTextSize(15);
 			acAdd.setId(id);
 			acAdd.requestFocus();
 			
 			//Adds the AcTextField to the frameLayout
 			destFrameLayout.addView(acAdd);
 			
 			//The Image Icon/sets it/enables it
 			FrameLayout.LayoutParams lli2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, 16);
 			lli2.setMargins(dipToPx(10), 0, 0, dipToPx(2));
 			destIcon.setLayoutParams(lli2);
 			destIcon.setPadding(0, dipToPx(5), 0, 0);
 			destIcon.setImageResource(R.drawable.google_marker_thumb_mini_through);
 			
 			//adds the imageicon to the frameLayout
 			destFrameLayout.addView(destIcon);
 			
 			final Button button = ((Button)findViewById(R.id.btnChooseRoute));
 			
 			//The exit icon for closing the entire frame
 			extIcon.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, 5));
 			extIcon.setPadding(0,12,17,0);
 			extIcon.setImageResource(R.drawable.cross_dropoff);
 			extIcon.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					if(acAdd.getText().toString().equals("")){
 						//extIcon.setImageResource(R.drawable.cross_dropoff);
 						removeFromAcList(id);
 						if(checkFields()){
 							button.setEnabled(true);
 							button.setText("Next");
 						}else{
 							button.setEnabled(false);
 							button.setText("Show on map");
 						}
 					}else{
 						//At det her funker er p hyde med tyngdekraft, universett og alt annet fantastisk!
 						extIcon.setImageResource(R.drawable.cross_dropoff);
 						acAdd.setText("");
 						extIcon.setImageResource(R.drawable.cross_dropoff);
 					}
 				}
 			});
 			
 			//adds the exit imageicon to the framelayout
 			destFrameLayout.addView(extIcon);
 			
 			//adds the frameLayout to the linearLayout
 			sclLayout.addView(destFrameLayout);
 			
 			//final Button button = ((Button)findViewById(R.id.btnChooseRoute));
 			
 			//adds the adapter for the textChangedListener
 			acAdd.setAdapter(adapter);
 			acAdd.addTextChangedListener(new AutoCompleteTextWatcher(MapActivityCreateOrEditRoute.this, adapter, acAdd));
 			
 			//sets the done button on the keyboard
 			acAdd.setOnEditorActionListener(new EditText.OnEditorActionListener(){
 				@Override
 				public boolean onEditorAction(TextView v, int actionId,
 						KeyEvent event) {
 					if(actionId == EditorInfo.IME_ACTION_DONE){
 						
 						hasDrawn = true;
 						if(checkFields() && selectedRoute.getMapPoints().size()>2 && hasDrawn == true){
 							createOneTimeJourney();
 							button.setEnabled(true);
 							button.setText("Next");
 						}
 						else if(checkFields() && selectedRoute.getMapPoints().size() == 0){
 							mapView.getOverlays().clear();
 							createMap();
 							button.setEnabled(true);
 							button.setText("Show on map");
 							
 						}
 						else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 							button.setText("Show on map");
 							button.setEnabled(false);
 						}
 						else if(inEditMode){
 							
 						}
 						else{
 							button.setText("Show on map");
 							button.setEnabled(false);
 						}
 						mapView.getOverlays().clear();
 						createMap();
 						return true;
 					}
 					else{
 						return false;
 					}
 				}
 			});
 			
 			//Adds a TextWatcher to the textView, to update the draw/next button
 			acAdd.addTextChangedListener(new TextWatcher() {
 				
 				@Override
 				public void onTextChanged(CharSequence s, int start, int before, int count) {
 					if(acAdd.getText().toString() != ""){
 						extIcon.setImageResource(R.drawable.speech_bubble_overlay_close);
 					}else{
 						extIcon.setImageResource(R.drawable.cross_dropoff);
 					}
 					
 					hasDrawn = false;
 					if(checkFields() && selectedRoute.getMapPoints().size()>1 && hasDrawn == true){
 						button.setEnabled(true);
 						button.setText("Next");
 					}else if(checkFields() && selectedRoute.getMapPoints().size()>1 && hasDrawn == false){
 						button.setEnabled(true);
 						button.setText("Show on Map");
 						
 					}
 					else if(checkFields() && selectedRoute.getMapPoints().size() == 0){
 						button.setEnabled(true);
 						button.setText("Show on map");
 						
 					}
 					else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 						button.setText("Show on map");
 						button.setEnabled(false);
 					}
 					else if(inEditMode){
 						
 					}
 					else{
 						button.setText("Show on map");
 						button.setEnabled(false);
 					}
 					
 				}
 				
 				@Override
 				public void beforeTextChanged(CharSequence s, int start, int count,
 						int after) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				public void afterTextChanged(Editable s) {
 					// TODO Auto-generated method stub
 					
 				}
 
 			});
 		}
 		
 		/*
 		 * Getters 
 		 */
 		
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
 	
 	/** createMap()
 	 * draws the map
 	 */
 	protected void createMap(){
 		hasDrawn = true;
 		drawPathOnMap(GeoHelper.getLocationList(getStringList()));
 		generateName();
 	}
 	
 	/** initContentView()
 	 * sets the contentView when the nextbutton is clicked
 	 */
 	@Override
 	protected void initContentView() {
 		setContentView(R.layout.mapactivity_create_route);
 	}
 	
 	/** initMapView()
 	 * initializes the map
 	 */
 	@Override
 	protected void initMapView(){
 		mapView = (MapView)findViewById(R.id.map_view);
 	}
 	
 	/** initProgressBar()
 	 *  initializes the progress bar
 	 */
 	@Override
 	protected void initProgressBar() {
 		setProgressBar((ProgressBar)findViewById(R.id.progressBar));
 	}
 	
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 	}
 	
 	/** createOneTimeJourney()
 	 * when the nextbutton is clicked, and all the requirements are fullfilled, creates a oneTimeJourney
 	 */
 	private void createOneTimeJourney(){
 		final Response res = chooseRoute();
 		if(res.getStatus() == ResponseStatus.OK){
 			getApp().setSelectedRoute(oneTimeRoute);
 			setTripOptions();
 		}
 		else 
 			createConfirmDialog(false,"Journey","created","");
 		
 //		DateChooser dc = new DateChooser(this, new PropertyChangeListener() {
 //			@Override
 //			public void propertyChange(PropertyChangeEvent event) {
 //				if(event.getPropertyName() == DateChooser.DATE_CHANGED){
 //					if(res.getStatus() == ResponseStatus.OK){
 //						sendJourneyRequest((Calendar) event.getNewValue());
 //					}
 //					else createConfirmDialog(false,"Journey","created","");
 //				}
 //			}
 //		});
 //		dc.show();
 		
 	}
 	
 	/** setTripOptions()
 	 * sets the TripOptions
 	 */
 	private void setTripOptions(){
 		Intent intent = new Intent(MapActivityCreateOrEditRoute.this, no.ntnu.idi.socialhitchhiking.journey.TripOptions.class);
 		startActivity(intent);
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
 					
 					//findAndDrawPath(v);
 					mapView.getOverlays().clear();
 					createMap();
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
 		MapRoute tempRoute = new MapRoute(selectedRoute,GeoHelper.getLocationList(getStringList()),false);
 		selectedRoute.setRouteData(tempRoute.getRouteData());
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
 		else {
 			commonRouteSelected.setName(generateName());
 			req = new RouteRequest(RequestType.CREATE_ROUTE, getUser(), commonRouteSelected);
 		}
 		
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
 		} catch (NullPointerException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/** generateName()
 	 * generates a name for the route {@link Route} based on the points on the map
 	 * @return name
 	 */
 	private String generateName(){
 		
 		String name = "";
 		String midLong = "";
 		String midShort = "";
 		
 		for(int i=0; i<selectedRoute.getMapPoints().size(); i++){
 			midLong = selectedRoute.getMapPoints().get(i).getAddress();
 			for(int j=0; j<midLong.length(); j++){
 				if(midLong.charAt(j) == ','){
 					break;
 				}else{
 					midShort += midLong.charAt(j);
 				}
 			}
 			name += midShort;
 			name += ' ';
 			name += '-';
 			name += ' ';
 			midShort = "";
 		}
 		
 		return name;
 	}
 	
 	/** onGpsClicked
 	 * activates the gps when gps button is clicked, and fills the acFrom field with the gps info
 	 * @param view
 	 */
 	public void onGpsClicked(View view) {
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
 	
 	/** gotLocation(andoid.location.Location location)
 	 * helping method for the gpsClicked method
 	 * fills the acFrom {@link AutoCompleteTextView} field with the gps information
 	 * @param location
 	 */
 	public void gotLocation(android.location.Location location) {
 		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
 		try {
 			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
 			acFrom.setText(addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAddressLine(1));
 		} catch (IOException e) {
 		}
 		loadingDialog.dismiss();
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
 	 * Creates a menu from the xml_mapmenu.xml file.
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
 		/*
 		else if(item.getItemId() == R.id.mapmenu_clear){
 			clearMap();
 			return true;
 		}
 		*/
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
 	
 	/** fillFieldsOnClick()
 	 * fills the fields if the map is already drawn, or an error occures with the corresponding fields
 	 */
 	protected void fillFieldsOnClick(){
 		
 		final Button button = ((Button)findViewById(R.id.btnChooseRoute));
 		AutoCompleteTextView acFrom = (AutoCompleteTextView) findViewById(R.id.etGoingFrom);
 		AutoCompleteTextView acTo = (AutoCompleteTextView) findViewById(R.id.etGoingTo);
 		acFrom.setText("");
 		acTo.setText("");
 		int aSize = selectedRoute.getMapPoints().size();
 		
 		//Adds the first point to the going from field
 		if(aSize == 1){
 			acFrom.setText(selectedRoute.getMapPoints().get(0).getAddress().toString());
 			
 		}else if(aSize == 2){
 			acFrom.setText(selectedRoute.getMapPoints().get(0).getAddress().toString());
 			
 			acTo.setText(selectedRoute.getMapPoints().get(selectedRoute.getMapPoints().size()-1).getAddress().toString());
 			
 		}else if(aSize >= 3){
 			acFrom.setText(selectedRoute.getMapPoints().get(0).getAddress().toString());
 			acTo.setText(selectedRoute.getMapPoints().get(selectedRoute.getMapPoints().size()-1).getAddress().toString());
 			
 			//int counter = 0;
 			
 			while(acList.size()>=1){
 				int id = acList.get(0).getId();
 				removeFromAcList(id);
 				setLayoutParams();
 				//counter++;
 			}
 			
 			for(int i=1; i<selectedRoute.getMapPoints().size()-1; i++){
 				initDestFrameLayout();
 				acList.get(i-1).getAcField().setText(selectedRoute.getMapPoints().get(i).getAddress().toString());
 				
 				setLayoutParams();
 				
 			}
 		}
 		if(checkFields() && selectedRoute.getMapPoints().size()>1){
 			button.setEnabled(true);
 			button.setText("Next");
 			hasDrawn = true;
 		}else if(checkFields() && selectedRoute.getMapPoints().size() == 0){
 			button.setEnabled(true);
 			button.setText("Show on map");
 		}else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 			//fillFieldsOnClick();
 		}else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 			button.setText("Show on map");
 			button.setEnabled(false);
 		}
 		mapView.invalidate();
 	}
 	
 	/** clearMapOnClick(View view)
 	 * removes all the points on the map, and updates the draw/next button
 	 * @param view
 	 */
 	public void clearMapOnClick(View view){
 		mapView.getOverlays().clear();
 		final Button button = ((Button)findViewById(R.id.btnChooseRoute));
 		mapView.invalidate();
 		MapRoute midRoute = new MapRoute();
 		selectedRoute = midRoute;
 		
 		
 		if(checkFields() && selectedRoute.getMapPoints().size()>1){
 			button.setEnabled(true);
 			button.setText("Next");
 			//hasDrawn = true;
 		}else if(checkFields() && selectedRoute.getMapPoints().size() == 0){
 			button.setEnabled(true);
 			button.setText("Show on map");
 		}else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 			//fillFieldsOnClick();
 		}else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 			button.setText("Show on map");
 			button.setEnabled(false);
 		}
 		
 	}
 
 	/**
 	 * When the user long presses on the screen, a dialog should pop up
 	 * where he/she is asked to add the point/address to the route.
 	 */
 	
 	@Override
 	public void onLongPress(MotionEvent e) {
 		/*
 		GeoPoint gp = mapView.getProjection().fromPixels(
 				(int) e.getX(),
 				(int) e.getY());
 		MapLocation mapLocation = (MapLocation) GeoHelper.getLocation(gp);
 
 		addPoint(mapLocation);
 		fillFieldsOnClick();
 		*/
 	}
 	
 	
 	
 	/** onSingleTapUp(MotionEvent e)
 	 * updates the {@link AutoCompleteTextView} fields on map movement
 	 */
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		GeoPoint gp = mapView.getProjection().fromPixels(
 				(int) e.getX(),
 				(int) e.getY());
 		MapLocation mapLocation = (MapLocation) GeoHelper.getLocation(gp);
 
 		addPoint(mapLocation);
 		fillFieldsOnClick();
 		return false;
 	}
 	
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		fillFieldsOnClick();
 		return false;
 	}
 	
 
 }
