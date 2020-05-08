 /**
  * 
  */
 package com.gvccracing.android.tttimer.Tabs;
 
 import java.util.Calendar;
 
 import com.gvccracing.android.tttimer.R;
 import com.gvccracing.android.tttimer.CursorAdapters.CheckInCursorAdapter;
 import com.gvccracing.android.tttimer.CursorAdapters.StartOrderCursorAdapter;
 import com.gvccracing.android.tttimer.CursorAdapters.TeamCheckInCursorAdapter;
 import com.gvccracing.android.tttimer.CursorAdapters.TeamStartOrderCursorAdapter;
 import com.gvccracing.android.tttimer.DataAccess.AppSettingsCP.AppSettings;
 import com.gvccracing.android.tttimer.DataAccess.CheckInViewCP.CheckInViewExclusive;
 import com.gvccracing.android.tttimer.DataAccess.CheckInViewCP.CheckInViewInclusive;
 import com.gvccracing.android.tttimer.DataAccess.RaceCP.Race;
 import com.gvccracing.android.tttimer.DataAccess.RaceResultsCP.RaceResults;
 import com.gvccracing.android.tttimer.DataAccess.RacerCP.Racer;
 import com.gvccracing.android.tttimer.DataAccess.RacerClubInfoCP.RacerClubInfo;
 import com.gvccracing.android.tttimer.DataAccess.TeamCheckInViewCP.TeamCheckInViewExclusive;
 import com.gvccracing.android.tttimer.DataAccess.TeamCheckInViewCP.TeamCheckInViewInclusive;
 import com.gvccracing.android.tttimer.DataAccess.TeamInfoCP.TeamInfo;
 import com.gvccracing.android.tttimer.Dialogs.AddGhostRacerView;
 import com.gvccracing.android.tttimer.Dialogs.AddRacerView;
 import com.gvccracing.android.tttimer.Dialogs.AddTeamView;
 import com.gvccracing.android.tttimer.Dialogs.EditRacerView;
 import com.gvccracing.android.tttimer.Dialogs.EditTeamView;
 import com.gvccracing.android.tttimer.Dialogs.StartOrderActions;
 import com.gvccracing.android.tttimer.Utilities.RestartLoaderTextWatcher;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 /**
  * @author Perry, Mark
  *
  */
 public class CheckInTab extends BaseTab implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
 
 	public static final String CheckInTabSpecName =  "CheckInActivity";
 
 	private static final int TEAM_CHECKIN_LOADER = 0x61;
 
 	private static final int TEAM_START_ORDER_LOADER = 0x95;
 
 	private static final int RACE_INFO_LOADER_CHECKIN = 0x44;
 
 	private static final int APP_SETTINGS_LOADER_CHECKIN = 0x109;
 
 	private static final int CHECKIN_LOADER_CHECKIN = 0x110;
 
 	private static final int START_ORDER_LOADER_CHECKIN = 0x111;
 
 	private CursorAdapter checkInsCA;
 	private CursorAdapter startOrderCA;
 	
 	private Button btnAddRacer = null;
 	private Button btnAddGhostRacer = null;
 	private Button btnAddNewTeam = null;	
 	private EditText racerNameSearchText;
 	
 	private Long raceTypeID = 0l;
 	
 	private Loader<Cursor> checkInLoader = null;
 	private Loader<Cursor> teamsCheckInLoader = null;
 	private Loader<Cursor> startOrderLoader = null;
 	private Loader<Cursor> teamStartOrderLoader = null;
 	
 	private ListView startOrderList;
 	private ListView checkInList;
 	
 	private boolean autoCheckIn = false;
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.tab_checkin, container, false);
         
         btnAddRacer = (Button) view.findViewById(R.id.btnAddNewRacer);
 		btnAddRacer.setOnClickListener(this);
 		
 		btnAddNewTeam = (Button) view.findViewById(R.id.btnAddNewTeam);
 		btnAddNewTeam.setOnClickListener(this);
 		
 		btnAddGhostRacer = (Button) view.findViewById(R.id.btnAddGhostRacer);
 		btnAddGhostRacer.setOnClickListener(this);
 		
         view.setKeepScreenOn(true);
         
         return view;
     }
 	
 	@Override
 	public void onResume() { 
 		super.onResume(); 
 		
         RestartLoaderTextWatcher tw = new RestartLoaderTextWatcher(getActivity().getSupportLoaderManager(), CHECKIN_LOADER_CHECKIN, this);
         
         racerNameSearchText = (EditText) getView().findViewById (R.id.txtRacerNameFilter);
 		racerNameSearchText.addTextChangedListener(tw);
 		
 		startOrderList = (ListView) getView().findViewById(R.id.tblCheckedInRacers);
 		checkInList = (ListView) getView().findViewById(R.id.tblFilteredRacers);
 		
 	    getActivity().getSupportLoaderManager().restartLoader(APP_SETTINGS_LOADER_CHECKIN, null, this);
 	    getActivity().getSupportLoaderManager().restartLoader(RACE_INFO_LOADER_CHECKIN, null, this);
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		getActivity().getSupportLoaderManager().destroyLoader(APP_SETTINGS_LOADER_CHECKIN);
 		getActivity().getSupportLoaderManager().destroyLoader(CHECKIN_LOADER_CHECKIN);
 		getActivity().getSupportLoaderManager().destroyLoader(TEAM_CHECKIN_LOADER);
 		getActivity().getSupportLoaderManager().destroyLoader(START_ORDER_LOADER_CHECKIN);
 		getActivity().getSupportLoaderManager().destroyLoader(TEAM_START_ORDER_LOADER);
 		getActivity().getSupportLoaderManager().destroyLoader(RACE_INFO_LOADER_CHECKIN);	
 	}
 	
 	private void SetupList(ListView list, CursorAdapter ca, OnItemLongClickListener listener) {	
 		if(getView() != null){
 	        if( list != null){
 	        	list.setAdapter(ca);
 	        	
 	        	list.setFocusable(true);
 	        	list.setClickable(true);
 	        	list.setItemsCanFocus(true);
 				
 	        	list.setOnItemLongClickListener( listener );
 	        }
 		}
 	}	
 
 	@Override
 	public void onStart() { super.onStart(); };
 	
 	@Override
 	public void onStop() { super.onStop(); };
 
 	@Override
 	public String TabSpecName() {
 		return CheckInTabSpecName;
 	}
 
 	@Override
 	protected String LOG_TAG() {
 		return CheckInTabSpecName;
 	}
 	
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {	
 		Log.i(LOG_TAG(), "onCreateLoader start: id=" + Integer.toString(id));
 		CursorLoader loader = null;
 		String[] projection;
 		String selection;
 		String[] selectionArgs = null;
 		String sortOrder;
 		
 		switch(id){
 			case CHECKIN_LOADER_CHECKIN:
 				// Create the cursor adapter for the list of checkins
 		        checkInsCA = new CheckInCursorAdapter(getActivity(), null);
 		        
 				SetupList(checkInList, checkInsCA, new OnItemLongClickListener(){
 					public boolean onItemLongClick(AdapterView<?> arg0, View v,	int pos, long id) {
 						EditRacerView editRacerDialog = new EditRacerView(id);
 						FragmentManager fm = getActivity().getSupportFragmentManager();
 						editRacerDialog.show(fm, EditRacerView.LOG_TAG);
 						return false;
 					}
 	    		});	
 				
 				projection = new String[]{RacerClubInfo.getTableName() + "." + RacerClubInfo._ID + " as _id", Racer.LastName, Racer.FirstName};
 				selection = RacerClubInfo.Year + "=? AND " + RacerClubInfo.Upgraded + "=? AND " + RacerClubInfo.Category + "!=?";
 				selectionArgs = new String[]{ Integer.toString(Calendar.getInstance().get(Calendar.YEAR)), Long.toString(0l), "G"};
 				sortOrder = Racer.LastName;
 				String racerNameText = racerNameSearchText.getText().toString();
 				if(!racerNameText.equals("")){
 					selection += " AND UPPER(" + Racer.LastName + ") GLOB ?";
					selectionArgs = new String[]{ Integer.toString(Calendar.getInstance().get(Calendar.YEAR)), Long.toString(0l), racerNameSearchText.getText().toString().toUpperCase() + "*"};
 				}
 				loader = new CursorLoader(getActivity(), CheckInViewInclusive.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 				break;
 			case TEAM_CHECKIN_LOADER:
 				// Create the cursor adapter for the list of team checkins
 		        checkInsCA = new TeamCheckInCursorAdapter(getActivity(), null);
 
 				SetupList(checkInList, checkInsCA, new OnItemLongClickListener(){
 					public boolean onItemLongClick(AdapterView<?> arg0, View v,	int pos, long id) {
 						EditTeamView editRacerDialog = new EditTeamView(id);
 						FragmentManager fm = getActivity().getSupportFragmentManager();
 						editRacerDialog.show(fm, EditTeamView.LOG_TAG);
 						return false;
 					}
 	    		});
 				
 				projection = new String[]{TeamInfo.getTableName() + "." + TeamInfo._ID + " as _id", TeamInfo.TeamName, "group_concat(" + Racer.FirstName + "||' '||" + Racer.LastName + ", ',\n') as RacerNames"};
 				selection = TeamInfo.getTableName() + "." + TeamInfo.Year + "=? AND " + RacerClubInfo.Upgraded + "=? AND " + TeamInfo.TeamCategory + "!=?";
 				selectionArgs = new String[]{ Integer.toString(Calendar.getInstance().get(Calendar.YEAR)), Long.toString(0l), "G"};
 				sortOrder = TeamInfo.getTableName() + "." + TeamInfo.TeamName;
 				loader = new CursorLoader(getActivity(), Uri.withAppendedPath(TeamCheckInViewInclusive.CONTENT_URI, "group by " + TeamInfo.getTableName() + "." + TeamInfo._ID + "," + TeamInfo.TeamName), projection, selection, selectionArgs, sortOrder);
 				break;
 			case START_ORDER_LOADER_CHECKIN:
 				// Create the cursor adapter for the start order list
 				startOrderCA = new StartOrderCursorAdapter(getActivity(), null);
 
 				SetupList(startOrderList, startOrderCA, new OnItemLongClickListener(){
 					public boolean onItemLongClick(AdapterView<?> arg0, View v,
 							int pos, long id) {
 						StartOrderActions startOrderActionsDialog = new StartOrderActions(id);
 						FragmentManager fm = getActivity().getSupportFragmentManager();
 						startOrderActionsDialog.show(fm, StartOrderActions.LOG_TAG);
 						return false;
 					}
 	    		});
 				projection = new String[]{RaceResults.getTableName() + "." + RaceResults._ID + " as _id", Racer.LastName, Racer.FirstName, RaceResults.StartOrder, RaceResults.StartTimeOffset};
 				selection = RaceResults.Race_ID + "=" + AppSettings.getParameterSql(AppSettings.AppSetting_RaceID_Name);
 				selectionArgs = null;
 				sortOrder = RaceResults.StartOrder;
 				loader = new CursorLoader(getActivity(), CheckInViewExclusive.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 				break;
 			case TEAM_START_ORDER_LOADER:
 				// Create the cursor adapter for the start order list
 				startOrderCA = new TeamStartOrderCursorAdapter(getActivity(), null);
 
 				SetupList(startOrderList, startOrderCA, new OnItemLongClickListener(){
 					public boolean onItemLongClick(AdapterView<?> arg0, View v,
 							int pos, long id) {
 						StartOrderActions startOrderActionsDialog = new StartOrderActions(id);
 						FragmentManager fm = getActivity().getSupportFragmentManager();
 						startOrderActionsDialog.show(fm, StartOrderActions.LOG_TAG);
 						return false;
 					}
 	    		});
 				
 				projection = new String[]{RaceResults.getTableName() + "." + RaceResults._ID + " as _id", TeamInfo.TeamName, RaceResults.StartOrder, RaceResults.StartTimeOffset, "group_concat(" + Racer.FirstName + "||' '||" + Racer.LastName + ", ',\n') as RacerNames"};
 				selection = RaceResults.Race_ID + "=" + AppSettings.getParameterSql(AppSettings.AppSetting_RaceID_Name);
 				selectionArgs = null;
 				sortOrder = RaceResults.StartOrder;
 				loader = new CursorLoader(getActivity(), Uri.withAppendedPath(TeamCheckInViewExclusive.CONTENT_URI, "group by " + TeamInfo.getTableName() + "." + TeamInfo._ID + "," + TeamInfo.TeamName + "," + RaceResults.StartOrder + "," + RaceResults.StartTimeOffset), projection, selection, selectionArgs, sortOrder);
 				break;
 			case RACE_INFO_LOADER_CHECKIN:
 				projection = new String[]{Race.getTableName() + "." + Race._ID + " as _id", Race.RaceType, Race.NumLaps};
 				selection = Race.getTableName() + "." + Race._ID + "=" + AppSettings.getParameterSql(AppSettings.AppSetting_RaceID_Name);
 				selectionArgs = null;
 				sortOrder = Race.getTableName() + "." + Race._ID;
 				loader = new CursorLoader(getActivity(), Race.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 				break;
 			case APP_SETTINGS_LOADER_CHECKIN:
 				projection = new String[]{AppSettings.AppSettingName, AppSettings.AppSettingValue};
 				selection = null;
 				sortOrder = null;
 				selectionArgs = null;
 				loader = new CursorLoader(getActivity(), AppSettings.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 				break;
 		}
 		Log.i(LOG_TAG(), "onCreateLoader complete: id=" + Integer.toString(id));
 		return loader;
 	}
 
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		try{
 			Log.i(LOG_TAG(), "onLoadFinished start: id=" + Integer.toString(loader.getId()));			
 			switch(loader.getId()){
 				case CHECKIN_LOADER_CHECKIN:	
 				case TEAM_CHECKIN_LOADER:
 					checkInsCA.swapCursor(cursor);
 					break;
 				case START_ORDER_LOADER_CHECKIN:													
 					startOrderCA.swapCursor(cursor);
 					if(getView() != null){
 			    		startOrderList.setSelection(startOrderList.getCount());
 					}
 					break;
 				case TEAM_START_ORDER_LOADER:
 					startOrderCA.swapCursor(cursor);
 					if(getView() != null){
 			    		startOrderList.setSelection(startOrderList.getCount());
 					}
 					break;
 				case RACE_INFO_LOADER_CHECKIN:	
 					if(cursor!= null && cursor.getCount() > 0){
 						cursor.moveToFirst();
 						// Set up the tab based on the race information
 						raceTypeID = cursor.getLong(cursor.getColumnIndex(Race.RaceType));
 						if(getView() != null){
 							LinearLayout llFilters = (LinearLayout) getView().findViewById(R.id.llFilters);
 							Button btnAddNewTeam = (Button) getView().findViewById(R.id.btnAddNewTeam);
 
 							if(raceTypeID == 1){						        
 								btnAddNewTeam.setVisibility(View.VISIBLE);
 								btnAddRacer.setVisibility(View.GONE);
 								llFilters.setVisibility(View.GONE);
 
 								teamsCheckInLoader = getActivity().getSupportLoaderManager().restartLoader(TEAM_CHECKIN_LOADER, null, this);
 								teamStartOrderLoader = getActivity().getSupportLoaderManager().restartLoader(TEAM_START_ORDER_LOADER, null, this);
 							}else {								
 								btnAddNewTeam.setVisibility(View.GONE);
 								btnAddRacer.setVisibility(View.VISIBLE);
 								llFilters.setVisibility(View.VISIBLE);
 
 								checkInLoader = getActivity().getSupportLoaderManager().restartLoader(CHECKIN_LOADER_CHECKIN, null, this);
 								startOrderLoader = getActivity().getSupportLoaderManager().restartLoader(START_ORDER_LOADER_CHECKIN, null, this);
 							}
 						}
 					}
 					break;
 				case APP_SETTINGS_LOADER_CHECKIN:	
 					if(checkInLoader != null){
 						getActivity().getSupportLoaderManager().restartLoader(CHECKIN_LOADER_CHECKIN, null, this);
 					}
 					if(teamsCheckInLoader != null){
 						getActivity().getSupportLoaderManager().restartLoader(TEAM_CHECKIN_LOADER, null, this);
 					}
 					if(startOrderLoader != null){
 						getActivity().getSupportLoaderManager().restartLoader(START_ORDER_LOADER_CHECKIN, null, this);
 					}
 					if( teamStartOrderLoader != null){
 						getActivity().getSupportLoaderManager().restartLoader(TEAM_START_ORDER_LOADER, null, this);
 					}
 					autoCheckIn = Boolean.parseBoolean(AppSettings.ReadValue(getActivity(), AppSettings.AppSettings_AutoCheckIn_Name, "true"));
 					break;
 			}
 			Log.i(LOG_TAG(), "onLoadFinished complete: id=" + Integer.toString(loader.getId()));
 		}catch(Exception ex){
 			Log.e(LOG_TAG(), "onLoadFinished error", ex); 
 		}
 	}
 
 	public void onLoaderReset(Loader<Cursor> loader) {
 		try{
 			Log.i(LOG_TAG(), "onLoaderReset start: id=" + Integer.toString(loader.getId()));
 			switch(loader.getId()){
 				case CHECKIN_LOADER_CHECKIN:
 				case TEAM_CHECKIN_LOADER:
 					checkInsCA.swapCursor(null);
 					break;
 				case START_ORDER_LOADER_CHECKIN:
 				case TEAM_START_ORDER_LOADER:
 					startOrderCA.swapCursor(null);
 					break;
 				case RACE_INFO_LOADER_CHECKIN:
 					// Do nothing...this is only here for consistency					
 					break;
 				case APP_SETTINGS_LOADER_CHECKIN:
 					// Do nothing...this is only here for consistency
 					break;
 			}
 			Log.i(LOG_TAG(), "onLoaderReset complete: id=" + Integer.toString(loader.getId()));
 		}catch(Exception ex){
 			Log.e(LOG_TAG(), "onLoaderReset error", ex); 
 		}
 	}
 
 
 	public void onClick(View v) {
 		try
      	{
  			Log.v(LOG_TAG(), "btnNewRacerClick");
  			if (v == btnAddRacer)
 			{
 	            AddRacerView chooseModeDialog = new AddRacerView(autoCheckIn);
 				FragmentManager fm = getActivity().getSupportFragmentManager();
 				chooseModeDialog.show(fm, AddRacerView.LOG_TAG);
 			} else if (v == btnAddNewTeam){
 				AddTeamView addTeamDialog = new AddTeamView();
 				FragmentManager fm = getActivity().getSupportFragmentManager();
 				addTeamDialog.show(fm, AddTeamView.LOG_TAG);
 			} else if(v == btnAddGhostRacer){
 				AddGhostRacerView chooseModeDialog = new AddGhostRacerView();
 				FragmentManager fm = getActivity().getSupportFragmentManager();
 				chooseModeDialog.show(fm, AddGhostRacerView.LOG_TAG);
 			}
      	}
      	catch(Exception ex)
      	{
      		Log.e(LOG_TAG(), "btnNewRacerClick failed", ex);
      	}
 	}
 }
