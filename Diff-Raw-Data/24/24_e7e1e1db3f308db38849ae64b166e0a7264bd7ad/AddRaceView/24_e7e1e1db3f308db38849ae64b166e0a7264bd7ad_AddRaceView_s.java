 package com.gvccracing.android.tttimer.Dialogs;
 
 import java.util.Date;
 
 import com.gvccracing.android.tttimer.R;
 import com.gvccracing.android.tttimer.DataAccess.AppSettingsCP.AppSettings;
 import com.gvccracing.android.tttimer.DataAccess.RaceCP.Race;
 import com.gvccracing.android.tttimer.DataAccess.RaceLocationCP.RaceLocation;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.LinearLayout;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter;
 
 
 public class AddRaceView extends BaseDialog implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
 	public static final String LOG_TAG = "AddRaceView";
 	
 	protected Button btnAddNewRace;
 	private SimpleCursorAdapter locationsCursorAdapter = null;
 	protected Spinner raceLocation = null;
 	private EditText txtLaps = null;
 	/**
      * This is a special intent action that means "load your tab data".
      */
     public static final String RACE_ADDED_ACTION = "com.gvccracing.android.tttimer.RACE_ADDED";
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.dialog_add_race, container, false);
 
 		btnAddNewRace = (Button) v.findViewById(R.id.btnAddNewRace);
 		btnAddNewRace.setOnClickListener(this);		
 		
 		raceLocation = (Spinner) v.findViewById(R.id.spinnerRaceLocation);
 		
 		// TODO I tried to put this in the loader, but it won't show a dialog from inside the onLoadFinished, so figure out how to do this the right way		
 		String[] fieldsToRetrieve = new String[]{RaceLocation._ID, RaceLocation.CourseName};
 		String selection = null;
 		String[] selectionArgs = null;
 		String sortOrder = RaceLocation.CourseName;
 		Cursor locations = RaceLocation.Read(getActivity(), fieldsToRetrieve, selection, selectionArgs, sortOrder);
 		if(locations != null && locations.getCount() <= 0)
         {
         	// No locations...show another dialog to add a location
         	AddLocationView addLocationDialog = new AddLocationView();
 			FragmentManager fm = getActivity().getSupportFragmentManager();
 			addLocationDialog.show(fm, AddLocationView.LOG_TAG);
         }
 		locations.close();
 		locations = null;
 		
 		txtLaps = (EditText) v.findViewById(R.id.txtNumLaps);
         
         Spinner raceType = (Spinner) v.findViewById(R.id.spinnerRaceType);
         raceType.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
         		LinearLayout laps = (LinearLayout) getView().findViewById(R.id.llLaps);
             	if(id == 1){
             		if(Long.parseLong(txtLaps.getText().toString()) <= 1){
             			txtLaps.setText("2");
             		}
             		laps.setVisibility(View.VISIBLE);
             	}else{
             		txtLaps.setText("1");
             		laps.setVisibility(View.GONE);
             	}
             }
 
             public void onNothingSelected(AdapterView<?> parentView) {
                 // your code here
             }
         });
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
         		getActivity(), R.array.race_type_array, R.layout.control_simple_spinner );
 		adapter.setDropDownViewResource( R.layout.control_simple_spinner_dropdown );
 		raceType.setAdapter(adapter);
 		
         // Intervals probably won't ever change, so I'm ok with this for now but...
         // TODO change this to use an underlying tag for how long the selected interval is
 		Spinner startInterval = (Spinner) v.findViewById(R.id.spinnerStartInterval);
         ArrayAdapter<CharSequence> startIntervalAdapter = ArrayAdapter.createFromResource(
         		getActivity(), R.array.start_interval_array, R.layout.control_simple_spinner );
         startIntervalAdapter.setDropDownViewResource( R.layout.control_simple_spinner_dropdown );
         startInterval.setAdapter(startIntervalAdapter);
 		
 		return v;
 	}
 	
 	@Override 
 	protected int GetTitleResourceID() {
 		return R.string.AddRace;
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		// Initialize the cursor loader for the races list
 		this.getLoaderManager().initLoader(RACE_LOCATIONS_LOADER, null, this);
 	}
 	
 	protected long GetRaceStartInterval(){
 			Spinner startIntervalSpinner = (Spinner) getView().findViewById(R.id.spinnerStartInterval);
 			int raceStartIntervalID = (int)startIntervalSpinner.getSelectedItemId();
			long raceStartInterval = 60l;
 			switch(raceStartIntervalID){
				case 1:
 					raceStartInterval = 30l;
 					break;
				case 2:
 					raceStartInterval = 60l;
 					break;
 			}
 			
 			return raceStartInterval;
 	}
 	
 	protected long GetRaceTypeID(){
 		Spinner raceTypeSpinner = (Spinner) getView().findViewById(R.id.spinnerRaceType);
 		
 		return raceTypeSpinner.getSelectedItemId();
 	}
 	
 	protected long GetRaceLocationID(){
 		Spinner raceLocationSpinner = (Spinner) getView().findViewById(R.id.spinnerRaceLocation);
 		
 		return raceLocationSpinner.getSelectedItemId();
 	}
 	
 	protected Date GetRaceDate(){
 		DatePicker datePicker = (DatePicker) getView().findViewById(R.id.dateRaceDate);
 		return new Date(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
 	}
 	
 	public void onClick(View v) { 
 		try{
 			if (v == btnAddNewRace){
 				EditText txtLaps = (EditText) getView().findViewById(R.id.txtNumLaps);
 				Uri resultUri = Race.Create(getActivity(), GetRaceLocationID(), GetRaceDate(), null, GetRaceTypeID(), GetRaceStartInterval(), Long.parseLong(txtLaps.getText().toString()));
 	 			long race_ID = Long.parseLong(resultUri.getLastPathSegment());
 	 			
 	 			// Broadcast that a race was added
     			Intent raceAdded = new Intent();
     			raceAdded.setAction(RACE_ADDED_ACTION);
     			raceAdded.putExtra(AppSettings.AppSetting_RaceID_Name, race_ID);
     			raceAdded.putExtra(AppSettings.AppSetting_StartInterval_Name, GetRaceStartInterval());
     			getActivity().sendBroadcast(raceAdded);
 
     			// Hide the dialog
      			dismiss();
 			} else {
 				super.onClick(v);
 			}
 		}
 		catch(Exception ex){
 			Log.e(LOG_TAG, "onClick failed",ex);
 		}
 	}
 
 	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
 		Log.i(LOG_TAG, "onCreateLoader start: id=" + Integer.toString(id));
 		CursorLoader loader = null;
 		String[] projection;
 		String selection;
 		String[] selectionArgs;
 		String sortOrder;
 		switch(id){
 			case RACE_LOCATIONS_LOADER:
 				projection = new String[]{RaceLocation._ID, RaceLocation.CourseName};
 				selection = null;
 				selectionArgs = null;
 				sortOrder = RaceLocation.CourseName;
 				loader = new CursorLoader(getActivity(), RaceLocation.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 				break;
 		}
 		Log.i(LOG_TAG, "onCreateLoader complete: id=" + Integer.toString(id));
 		return loader;
 	}
 
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		try{
 			Log.i(LOG_TAG, "onLoadFinished start: id=" + Integer.toString(loader.getId()));			
 			switch(loader.getId()){
 				case RACE_LOCATIONS_LOADER:
 	            	locationsCursorAdapter = null;
 					if(locationsCursorAdapter == null){
 						String[] columns = new String[] { RaceLocation.CourseName };
 			            int[] to = new int[] {android.R.id.text1 };
 			            
 						// Create the cursor adapter for the list of races
 			            locationsCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.control_simple_spinner, cursor, columns, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
 			            locationsCursorAdapter.setDropDownViewResource( R.layout.control_simple_spinner_dropdown );
 			        	raceLocation.setAdapter(locationsCursorAdapter);
 					}
 					locationsCursorAdapter.swapCursor(cursor);
 					break;
 			}
 			Log.i(LOG_TAG, "onLoadFinished complete: id=" + Integer.toString(loader.getId()));
 		}catch(Exception ex){
 			Log.e(LOG_TAG, "onLoadFinished error", ex); 
 		}
 	}
 
 	public void onLoaderReset(Loader<Cursor> loader) {
 		try{
 			Log.i(LOG_TAG, "onLoadFinished start: id=" + Integer.toString(loader.getId()));			
 			switch(loader.getId()){
 				case RACE_LOCATIONS_LOADER:
 					locationsCursorAdapter.swapCursor(null);
 					break;
 			}
 			Log.i(LOG_TAG, "onLoaderReset complete: id=" + Integer.toString(loader.getId()));
 		}catch(Exception ex){
 			Log.e(LOG_TAG, "onLoaderReset error", ex); 
 		}
 	}
 
 	@Override
 	protected String LOG_TAG() {
 		return LOG_TAG;
 	}
 }
