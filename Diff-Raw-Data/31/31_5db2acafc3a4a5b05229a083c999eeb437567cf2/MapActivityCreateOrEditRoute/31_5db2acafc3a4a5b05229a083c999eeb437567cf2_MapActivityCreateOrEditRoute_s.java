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
 	
 	//adda
 	private FrameLayout AddDestFrameLayout;
 	private LinearLayout sclLayout;
 	private ArrayList<InitDestFrame> acList;
 	//private String[] acStringList;
 	private int id = 0;
 	private Resources r;
 	
 	//Fra thomas
 	private ProgressDialog loadingDialog;
 	
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
 	
 	//final Button button = ((Button)findViewById(R.id.btnChooseRoute));
 	
 	private boolean hasDrawn;
 
 	
 	@Override
 	protected void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		
 		acList = new ArrayList<InitDestFrame>();
 		r = getResources();
 		initAutocomplete();
 		initAddDestButton();
 		
 		hasDrawn = false;
 		
 		Bundle extras = getIntent().getExtras();
 		if(extras != null){
 			inEditMode = extras.getBoolean("editMode");
 			positionOfRoute = extras.getInt("routePosition");
 		}
 		
 		
 		chk_saveRoute = (CheckBox)findViewById(R.id.checkBoxSave);
 		chk_saveRoute.setVisibility(8);
 		final Button button = ((Button)findViewById(R.id.btnChooseRoute));
 		
 		
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
 		
 		final AutoCompleteTextView acFrom = (AutoCompleteTextView) findViewById(R.id.etGoingFrom);
 		final AutoCompleteTextView acTo = (AutoCompleteTextView) findViewById(R.id.etGoingTo);
 		ImageView bClearFrom = ((ImageView)findViewById(R.id.etGoingFromClearIcon));
 		ImageView bClearTo = ((ImageView)findViewById(R.id.etGoingToClearIcon));
 		
 		bClearFrom.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				acFrom.setText("");
 				button.setEnabled(false);
 				button.setText("Show on Map");
 				
 			}
 			
 		});
 		
 		bClearTo.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				acTo.setText("");
 				button.setEnabled(false);
 				button.setText("Show on Map");
 				
 			}
 			
 		});
 		
 		acFrom.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				hasDrawn = false;
 				if(checkFields() && selectedRoute.getMapPoints().size()>1 && hasDrawn == true){
 					button.setEnabled(true);
 					button.setText("Next");
 					Log.e("IF1","vi kom hit");
 				}else if(checkFields() && selectedRoute.getMapPoints().size()>1 && hasDrawn == false){
 					button.setEnabled(true);
 					button.setText("Show on Map");
 					
 				}
 				else if(checkFields() && selectedRoute.getMapPoints().size() == 0){
 					button.setEnabled(true);
 					button.setText("Show on map");
 					Log.e("IF2","vi kom hit");
 					
 				}
 				else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 					button.setText("Show on map");
 					button.setEnabled(false);
 					Log.e("IF3","vi kom hit");
 				}
 				else if(inEditMode){
 					Log.e("IF4","vi kom hit");
 					
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
 		
 		acTo.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				hasDrawn = false;
 				if(checkFields() && selectedRoute.getMapPoints().size()>2 && hasDrawn == true){
 					button.setEnabled(true);
 					button.setText("Next");
 					Log.e("IF1","vi kom hit");
 				}else if(checkFields() && selectedRoute.getMapPoints().size()>2 && hasDrawn == false){
 					button.setEnabled(true);
 					button.setText("Show on Map");
 					
 				}
 				else if(checkFields() && selectedRoute.getMapPoints().size() == 0){
 					button.setEnabled(true);
 					button.setText("Show on map");
 					Log.e("IF2","vi kom hit");
 					
 				}
 				else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 					button.setText("Show on map");
 					button.setEnabled(false);
 					Log.e("IF3","vi kom hit");
 				}
 				else if(inEditMode){
 					Log.e("IF4","vi kom hit");
 					
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
 		
 	
 	protected boolean checkFields(){
 		AutoCompleteTextView acFrom = (AutoCompleteTextView) findViewById(R.id.etGoingFrom);
 		AutoCompleteTextView acTo = (AutoCompleteTextView) findViewById(R.id.etGoingTo);
 		
 		if((acFrom.getText().toString().equals("") || acFrom.getText().toString().equals("")) && (acTo.getText().toString().equals("") || acTo.getText().toString().equals("") && checkAddFields() == false)){
 			Log.e("checkFields_1", "false");
 			return false;
 		}else if(acTo.getText().toString().equals("") || acTo.getText().toString().equals("") || checkAddFields() == false){
 			Log.e("checkFields_2", "false");
 			//makeToast("You have to fill in the Driving from field");
 			return false;
 		}else if(acFrom.getText().toString().equals("") || acFrom.getText().toString().equals("") || checkAddFields() == false){
 			Log.e("checkFields_3", "false");
 			//makeToast("You have to fill in the Driving to field");
 			return false;
 		}else{
 			Log.e("checkFields_4", "true");
 			return true;
 		}
 	}
 	
 	protected boolean checkAddFields(){
 		boolean check = true;
 		for(int i=0; i<acList.size(); i++){
 			if(acList.get(i).getAcField().getText().toString().equals("") || acList.get(i).getAcField().getText().toString().length() == 0){
 				Log.e("CHECKADDFIELDS", "nr: er tom" + i);
 				check = false;
 			}
 		}
 		return check;
 	}
 	
 	protected void fillFieldsInEdit(){
 		AutoCompleteTextView acFrom = (AutoCompleteTextView) findViewById(R.id.etGoingFrom);
 		AutoCompleteTextView acTo = (AutoCompleteTextView) findViewById(R.id.etGoingTo);
 		
 		acFrom.setText(selectedRoute.getMapPoints().get(0).getShortAddress());
 		acTo.setText(selectedRoute.getMapPoints().get(selectedRoute.getMapPoints().size()-1).getShortAddress());
 		Log.e("SR-SIZE", selectedRoute.getMapPoints().size() + "");
 		
 		for(int i=1; i<selectedRoute.getMapPoints().size()-1; i++){
 			initDestFrameLayout();
 			acList.get(i-1).getAcField().setText(selectedRoute.getMapPoints().get(i).getShortAddress());
 			Log.e("AcList", acList.size() + "");
 			setLayoutParams();
 		}
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
 	
 	public int dipToPx(int dip){
 		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
 		return (int)px;
 	}
 	
 	
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
 				
 				//Moves the botton to the buttom
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
 		private boolean checks;
 
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
 			acAdd.setPadding(dipToPx(40), 0, 0, 0);
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
 			//destIcon.setImageResource(R.drawable.google_marker_thumb_mini_through);
 			
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
 						for(int s=0; s<getAcList().size(); s++){
 							Log.e("acList", getAcList().get(s).getAcField().getText() + "");
 							/*
 							if(getAcList().get(s).getAcField().getText().toString().equals("") || getAcList().get(s).getAcField().getText().toString().length() == 0){
 								checks = false;
 							}
 							*/
 						}
 						if(checkFields()){
 							/*
 							mapView.getOverlays().clear();
 							createMap();
 							*/
 							button.setEnabled(true);
 							button.setText("Next");
 						}else{
 							button.setEnabled(false);
 							button.setText("Show on map");
 							/*
 							checks = false;
 							*/
 						}
 					}else{
 						//At det her funker er p hyde med tyngdekraft, universett og alt annet fantastisk!
 						extIcon.setImageResource(R.drawable.cross_dropoff);
 						acAdd.setText("");
 						extIcon.setImageResource(R.drawable.cross_dropoff);
 						//acList.get(id).getAcField().setText(acAdd.getText().toString());
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
 							//mapView.getOverlays().clear();
 							//createMap();
 							createOneTimeJourney();
 							button.setEnabled(true);
 							button.setText("Next");
 							Log.e("IF1","vi kom hit");
 						}
 						else if(checkFields() && selectedRoute.getMapPoints().size() == 0){
 							mapView.getOverlays().clear();
 							createMap();
 							button.setEnabled(true);
 							button.setText("Show on map");
 							Log.e("IF2","vi kom hit");
 							
 						}
 						else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 							button.setText("Show on map");
 							button.setEnabled(false);
 							Log.e("IF3","vi kom hit");
 						}
 						else if(inEditMode){
 							Log.e("IF4","vi kom hit");
 							
 						}
 						else{
 							Log.e("IF5","vi kom hit");
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
 						Log.e("IF1","vi kom hit");
 					}else if(checkFields() && selectedRoute.getMapPoints().size()>1 && hasDrawn == false){
 						button.setEnabled(true);
 						button.setText("Show on Map");
 						
 					}
 					else if(checkFields() && selectedRoute.getMapPoints().size() == 0){
 						button.setEnabled(true);
 						button.setText("Show on map");
 						Log.e("IF2","vi kom hit");
 						
 					}
 					else if(checkFields() == false && selectedRoute.getMapPoints().size() == 0){
 						button.setText("Show on map");
 						button.setEnabled(false);
 						Log.e("IF3","vi kom hit");
 					}
 					else if(inEditMode){
 						Log.e("IF4","vi kom hit");
 						
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
 		Log.e("Length","Test");
 		hasDrawn = true;
 		drawPathOnMap(GeoHelper.getLocationList(getStringList()));
 		generateName();
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
 		
 		
 		//Log.e("Length",Integer.toString(commonRouteSelected.getRouteData().size()));
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
 		//else req = new RouteRequest(RequestType.CREATE_AD_HOC_ROUTE, getUser(), commonRouteSelected);
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
 	
 	private String generateName(){
 		
 		String name = "";
 		String midLong = "";
 		String midShort = "";
 		
 		for(int i=0; i<selectedRoute.getMapPoints().size(); i++){
 			midLong = selectedRoute.getMapPoints().get(i).getAddress();
 			Log.e("midLong", midLong + "");
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
 		
 		Log.e("NAME", name + "");
 		return name;
 	}
 	
 	//Fra thomas
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
 	
 	public void gotLocation(android.location.Location location) {
 		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
 		try {
 			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
 			acFrom.setText(addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAddressLine(1));
 		} catch (IOException e) {
 			//Log.e("IOError",e.getMessage());
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
 		
 		for(int i=0; i<selectedRoute.getMapPoints().size(); i++){
 			Log.e("locARRAY", selectedRoute.getMapPoints().get(i).getAddress() + "");
 		}
 	}
 	
 	private void sendJourneyRequest(Calendar cal){
 		Journey jour = new Journey(-1);
 		jour.setRoute(oneTimeRoute);
 		jour.setStart(cal);
 		jour.setVisibility(Visibility.PUBLIC);
 		TripPreferences pref = new TripPreferences(7,true,true,true,true,true);
 		pref.setPrefId(1);
 		jour.setTripPreferences(pref);
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
 			Log.e("Error",e.getMessage());
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
