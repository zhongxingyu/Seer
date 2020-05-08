 /* 
 This file is part of OpenSatNav.
 
     OpenSatNav is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     OpenSatNav is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with OpenSatNav.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.opensatnav.android;
 
 import java.util.ArrayList;
 
 import org.andnav.osm.util.GeoPoint;
 import org.opensatnav.android.services.GeoCoder;
 import org.opensatnav.android.services.NominatimGeoCoder;
 import org.opensatnav.android.services.PlanoturGeoCoder;
 import org.opensatnav.android.util.UKPostCodeValidator;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 /**
  * 
  * @author Kieran Fleming
  * 
  */
 
 public class GetDirectionsActivity extends Activity {
 	protected static final int CHOOSE_LOCATION = 0;
 	protected Intent data;
 	protected Bundle locations;
 	protected ArrayList<String> route;
 	protected GeoPoint from;
 	protected String toText;
 	protected EditText toField;
 	protected Spinner vehicleSpinner;
 	protected String vehicle;
 	protected Boolean backgroundThreadComplete = true;
 	private RadioButton radio_text;
 	private RadioButton radio_poi;
 	private RadioButton radio_trans;
 	protected SharedPreferences prefs;
 	protected ProgressDialog progress;
 	
 
 	OnClickListener radio_listener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			// Perform action on clicks
 			RadioButton rb = (RadioButton) v;
 			RadioButton radio_text = (RadioButton) findViewById(R.id.radio_text_search);
 			RadioButton radio_poi = (RadioButton) findViewById(R.id.radio_poi_search);
 			RadioButton radio_trans = (RadioButton) findViewById(R.id.radio_trans_search);
 			if (rb.getId() == R.id.radio_poi_search) {
 				radio_text.setChecked(false);
 				radio_trans.setChecked(false);
 			} else if (rb.getId() == R.id.radio_text_search) {
 				radio_poi.setChecked(false);
 				radio_trans.setChecked(false);
 			}else if (rb.getId() == R.id.radio_trans_search) {
 				radio_poi.setChecked(false);
 				radio_text.setChecked(false);
 			}
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		data = new Intent();
 		super.onCreate(savedInstanceState);
 		Object state = getLastNonConfigurationInstance();
 		if(state instanceof Boolean)
 			backgroundThreadComplete = (Boolean) state;
 		if(backgroundThreadComplete==false){
 			progress = ProgressDialog.show(
 					GetDirectionsActivity.this, this.getResources().getText(
 							R.string.please_wait), this.getResources().getText(
 							R.string.searching), true, true);
 		}
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		Resources r = getResources();
 		from = GeoPoint.fromDoubleString(getIntent().getDataString(), ',');
 		setTitle(R.string.get_directions);
 		setContentView(R.layout.getdirections);
 
 		radio_text = (RadioButton) findViewById(R.id.radio_text_search);
 		radio_poi = (RadioButton) findViewById(R.id.radio_poi_search);
 		radio_text.setOnClickListener(radio_listener);
 		radio_poi.setOnClickListener(radio_listener);
 		radio_trans = (RadioButton) findViewById(R.id.radio_trans_search);
 		radio_trans.setOnClickListener(radio_listener);
 
 		final Spinner s_poi = (Spinner) findViewById(R.id.list_of_pois);
 		ArrayAdapter<?> adapter_poi = ArrayAdapter.createFromResource(this,
 				R.array.poi_types, android.R.layout.simple_spinner_item);
 		adapter_poi
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		s_poi.setAdapter(adapter_poi);
 		
 		String[] rawPois = r.getStringArray(R.array.poi_types_osmvalue);
 		for (int i = 0;i<rawPois.length;i++) {
 			if(rawPois[i].equals(prefs.getString("pref_poi", rawPois[0]))){
 				s_poi.setSelection(i);
 			}
 		}
 		String search_mode = prefs.getString("pref_search_mode", "text");
 		if(search_mode.equals("text")){
 			radio_text.setChecked(true);
 			radio_poi.setChecked(false);
 		}
 		else if(search_mode.equals("poi")){
 			radio_text.setChecked(false);
 			radio_poi.setChecked(true);
 		}
 		s_poi.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				s_poi.requestFocusFromTouch();
 				radio_text.setChecked(false);
 				radio_poi.setChecked(true);
				radio_trans.setChecked(false);
 				return false;
 			}
 		});
 		s_poi.setOnItemSelectedListener(new OnItemSelectedListener() {
 			boolean s_poi_selected_on_creation = false;
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				if (s_poi_selected_on_creation == false) {
 					s_poi_selected_on_creation = true;
 				} else {
 					radio_text.setChecked(false);
 					radio_poi.setChecked(true);
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		vehicleSpinner = (Spinner) findViewById(R.id.modeoftransport);
 		ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this,
 				R.array.mode_of_transport_types,
 				android.R.layout.simple_spinner_item);
 		adapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		vehicleSpinner.setAdapter(adapter);
 		String[] transportTypes = r.getStringArray(R.array.mode_of_transport_types_osmvalue);
 		for (int i = 0;i<transportTypes.length;i++) {
 			if(transportTypes[i].equals(prefs.getString("pref_vehicle", transportTypes[0]))){
 				vehicleSpinner.setSelection(i);
 			}
 		}
 		vehicleSpinner
 				.setPrompt((CharSequence) findViewById(R.string.transport_type));
 
 		toField = (EditText) findViewById(R.id.to_text_field);
 
 		toField.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				radio_text.setChecked(true);
 				radio_poi.setChecked(false);
				radio_trans.setChecked(false);
 				return false;
 			}
 		});
 		toField.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				radio_text.setChecked(true);
 				radio_poi.setChecked(false);
 			}
 		});
 		toField.setOnKeyListener(new OnKeyListener() {
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (keyCode != KeyEvent.KEYCODE_DPAD_DOWN
 						&& keyCode != KeyEvent.KEYCODE_DPAD_LEFT
 						&& keyCode != KeyEvent.KEYCODE_DPAD_UP
 						&& keyCode != KeyEvent.KEYCODE_DPAD_RIGHT) {
 					radio_text.setChecked(true);
 					radio_poi.setChecked(false);
					radio_trans.setChecked(false);
 				}
 				if ((event.getAction() == KeyEvent.ACTION_DOWN)
 						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
 
 					// Perform action on enter key press
 					getLocations(toField.getText().toString(), -1);
 					return true;
 				}
 				return false;
 			}
 
 		});
 		Button goButton = (Button) findViewById(R.id.go_button);
 		goButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				Editor prefEditor = prefs.edit();
 				if (radio_text.isChecked()) { // text search
 					getLocations(toField.getText().toString(), -1);
 					prefEditor.putString("pref_search_mode", "text");
 				} else if (radio_poi.isChecked()) { // poi search
 					int selectedPoi = (int) s_poi.getSelectedItemId();
 					String osmvalue = getResources().getStringArray(
 							R.array.poi_types_osmvalue)[selectedPoi];
 					from = GeoPoint.fromDoubleString(getIntent()
 							.getDataString(), ',');
 					getLocations(osmvalue,
 							selectedPoi);
 					prefEditor.putString("pref_poi", osmvalue);
 					prefEditor.putString("pref_search_mode", "poi");
 				} else if (radio_trans.isChecked())
 					getLocations(" ", -2);
 					
 				int selectedVehicle = (int) vehicleSpinner.getSelectedItemId();
 				vehicle = getResources().getStringArray(
 						R.array.mode_of_transport_types_osmvalue)[selectedVehicle];
 				prefEditor.putString("pref_vehicle", vehicle);
 				prefEditor.commit();
 			}
 		});
 	
 	}
 
 	public void getLocations(final String toText, final int selectedPoi) {
 		if (toText.length() != 0) {
 			backgroundThreadComplete = false;
 			progress = ProgressDialog.show(
 					GetDirectionsActivity.this, this.getResources().getText(
 							R.string.please_wait), this.getResources().getText(
 							R.string.searching), true, true);
 			final Handler handler = new Handler() {
 				// threading stuff - this actually handles the stuff after the
 				// thread has completed (code below)
 				@Override
 				public void handleMessage(Message msg) {
 					if (progress.isShowing())
 						try {
 							progress.dismiss();
 							backgroundThreadComplete = true;
 						} catch (IllegalArgumentException e) {
 							// if orientation change, thread continue but the dialog cannot be dismissed without exception
 						}
 						
 					if (locations != null) {
 						Intent intent;
 						if (selectedPoi == -1){
 							intent = new Intent(GetDirectionsActivity.this,
 								org.opensatnav.android.ChooseLocationActivity.class);
 //								org.opensatnav.android.TransChooseLocationServiceActivity.class);
 //								org.opensatnav.android.ChooseServiceActivity.class);
 						}else{ 
 							intent = new Intent(GetDirectionsActivity.this,
 //									org.opensatnav.android.ChooseLocationActivity.class);
 									org.opensatnav.android.TransChooseLocationServiceActivity.class);
 //									org.opensatnav.android.ChooseServiceActivity.class);
 						}
 						intent.putExtra("fromLocation", from.toDoubleString());
 						intent.putExtra("locations", locations);
 						startActivityForResult(intent, CHOOSE_LOCATION);
 					} else {
 						if (selectedPoi == -1) { // text search
 							// specific case for UK postcodes
 							String currentLocale = GetDirectionsActivity.this.getResources().getConfiguration().locale.getCountry();
 							if ((currentLocale.compareTo("GB") == 0) && (UKPostCodeValidator.isPostCode(toText.trim()))) {
 								UKPostCodeValidator.showFreeThePostCodeDialog(GetDirectionsActivity.this);
 							} else {
 								String text = String
 										.format(
 												GetDirectionsActivity.this
 														.getResources()
 														.getText(
 																R.string.place_not_found)
 														.toString(), toText);
 								Toast.makeText(GetDirectionsActivity.this,
 										text, Toast.LENGTH_LONG).show();
 							}
 						} else { // poi search
 							String stringValue = getResources().getStringArray(
 									R.array.poi_types)[selectedPoi];
 							Toast
 									.makeText(
 											GetDirectionsActivity.this,
 											GetDirectionsActivity.this
 													.getResources()
 													.getText(
 															R.string.could_not_find_poi)
 													+ " " + stringValue,
 											Toast.LENGTH_LONG).show();
 						}
 					}
 				}
 			};
 			new Thread(new Runnable() {
 				public void run() {
 					// put long running operations here
 					GeoCoder geoCoder = null;
 
 					
 //					geoCoder = new PlanoturGeoCoder();
 					geoCoder = new NominatimGeoCoder();
 						
 					
 					if (selectedPoi == -1) { // text search, rank results within an area
 						locations = geoCoder.query(toText, from, GeoCoder.IN_AREA, 25,
 								GetDirectionsActivity.this);
 					}
 					else if (selectedPoi == -2)
 						locations = (new PlanoturGeoCoder()).query(toText, from, GeoCoder.IN_AREA, 25,
 								GetDirectionsActivity.this);
 					else {  //POI search, just find the nearest matching POI
 					locations = geoCoder.query(toText, from, GeoCoder.FROM_POINT, 25,
 							GetDirectionsActivity.this);
 					}
 					// ok, we are done
 					handler.sendEmptyMessage(0);
 				}
 			}).start();
 
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == CHOOSE_LOCATION) {
 			if (resultCode == RESULT_OK) {
 				int selectedVehicle = (int) vehicleSpinner.getSelectedItemId();
 				vehicle = getResources().getStringArray(
 						R.array.mode_of_transport_types_osmvalue)[selectedVehicle];
 				Bundle bundle = new Bundle();
 				bundle.putString("vehicle", vehicle);
 				bundle.putString("to", data.getStringExtra("location"));
 				data.putExtras(bundle);
 				setResult(RESULT_OK, data);
 				finish();
 			}
 		}
 
 	}
 	
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 	    return backgroundThreadComplete;
 	}
 
 }
