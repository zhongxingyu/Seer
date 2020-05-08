 package fi.iki.joker.sevedroid;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.PopupWindow;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * The main activity for the application
  * TODO: Split this into multiple source files, too long now
  * Logcat: logcat Sevedroid:V *:S
  * @author juha
  *
  */
 
 public class SevedroidProjectActivity extends Activity implements OnItemSelectedListener, 
 																	OnClickListener {
     
 	/**
 	 * Key under which the project list is saved to spawned Activies extras 
 	 */
 	
 	public static final String PROJECTLIST_BUNDLE_KEY = "pListInMyStack";
 	
 	private static final String TAG = "Sevedroid";
 	private static final int optionsMenuId = 1;
 	private SeveraCommsUtils mScu = null;
 	private static final int requestCode = 1;
 	private static final int DATE_PICKER_DIALOG_ID = 0;
 	private static final int NOT_CONNECTED_DIALOG_ID = 1;
 	private boolean stateRestored = false;
 	
 	private Calendar claimDate = null;
 	/**
 	 * The list containing the Cases this user id has access to and the parcel identifier
 	 */
 	protected ArrayList<S3CaseItem> projectList = null;
 	protected static final String CASEITEMLIST_PARCEL_ID = "caseItemParcelID";
 	
 	/**
 	 * The list containing the phases under some particular case this user has access to and the parcel identifier
 	 */
 	
 	protected ArrayList<S3PhaseItem> phaseList = null;
 	protected static final String PHASEITEMLIST_PARCEL_ID = "phaseItemParcelID";
 	
 	/**
 	 * The list containing the work type items under some particular phase and the parcel identifier
 	 */
 	
 	protected ArrayList<S3WorkTypeItem> workTypeList = null;
 	protected static final String WORKTYPEITEMLIST_PARCEL_ID = "workTypeParcelID";
 	
 	protected String currentWorkTypeGUID = null;
 	protected String currentPhaseGUID = null;
 	
 	ProgressBar projectsProgress = null;
 	ProgressBar phasesProgress = null;
 	ProgressBar workTypeProgress = null;
 	ArrayAdapter<CharSequence> phaseAdapter = null;
 	Spinner projectPhaseSpinner = null;
 	ArrayAdapter<CharSequence> projectAdapter = null;
 	Spinner projectNameSpinner = null;
 	ArrayAdapter<CharSequence> workTypeAdapter = null;
 	Spinner workTypeSpinner = null;
 	boolean hourEntryStatus = false;
 	
 	/**
 	 * This callback is called when user has selected a date from the change_data_button
 	 */
 	
 	private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener() {
 
                 public void onDateSet(DatePicker view, int year, 
                                       int monthOfYear, int dayOfMonth) {
                     claimDate.set(year, monthOfYear, dayOfMonth);
                 }
             };
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG,"OnCreate called!");
         if(savedInstanceState == null) {
         	Log.d(TAG, "Saved instance state is null, recreating Activity state.");
        } else {
         	Log.d(TAG, "Saved instance state is not null, restoring Activity state...");
         	projectList = savedInstanceState.getParcelableArrayList(CASEITEMLIST_PARCEL_ID);
         	phaseList = savedInstanceState.getParcelableArrayList(PHASEITEMLIST_PARCEL_ID);
         	workTypeList = savedInstanceState.getParcelableArrayList(WORKTYPEITEMLIST_PARCEL_ID);
         	stateRestored = true;
         }
         if(SeveraCommsUtils.checkIfConnected(this) == false) {
 			showDialog(NOT_CONNECTED_DIALOG_ID);
 			return;
 		}
         setContentView(R.layout.main);
         // test if the user has set the api key, if yes, then follow on to load up the projects
         // if not, then auto start the config activity
         SevedroidContentStore contentStore = new SevedroidContentStore(this);
         if(contentStore.fetchApiKey() == null) {
         	Toast.makeText(this,"Please input your API key to use this app!",Toast.LENGTH_LONG).show();
 			Intent intent = new Intent();
 			intent.setClass(this, SevedroidConfig.class);
 			this.startActivityForResult(intent, requestCode);
 			return;
         } else {
         	runOnCreate();
         }
     }
     
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
		// TODO Save List instances here
 		Log.d(TAG,"onSaveInstanceState called!");
 		outState.putParcelableArrayList(CASEITEMLIST_PARCEL_ID, projectList);
 		outState.putParcelableArrayList(PHASEITEMLIST_PARCEL_ID, phaseList);
 		outState.putParcelableArrayList(WORKTYPEITEMLIST_PARCEL_ID, workTypeList);
     	super.onSaveInstanceState(outState);
 	}
 
 	private void runOnCreate() {
     	// make progress indicators global, hide before use
         projectsProgress = (ProgressBar)findViewById(R.id.projectsProgressBar);
         phasesProgress = (ProgressBar)findViewById(R.id.phasesProgressBar);
         workTypeProgress = (ProgressBar)findViewById(R.id.workTypeProgressBar);
         projectsProgress.setVisibility(View.GONE);
         phasesProgress.setVisibility(View.GONE);
         workTypeProgress.setVisibility(View.GONE);
         // the buttons are all handled by the OnClickListener implementation of this class)
         (findViewById(R.id.button1hour)).setOnClickListener(this);
         (findViewById(R.id.button2hour)).setOnClickListener(this);
         (findViewById(R.id.button3hour)).setOnClickListener(this);
         (findViewById(R.id.button4hour)).setOnClickListener(this);
         (findViewById(R.id.button5hour)).setOnClickListener(this);
         (findViewById(R.id.button6hour)).setOnClickListener(this);
         (findViewById(R.id.button7hour)).setOnClickListener(this);
         (findViewById(R.id.button8hour)).setOnClickListener(this);
         (findViewById(R.id.button9hour)).setOnClickListener(this);
         (findViewById(R.id.button10hour)).setOnClickListener(this);
         (findViewById(R.id.buttonplus30min)).setOnClickListener(this);
         (findViewById(R.id.button_claim)).setOnClickListener(this);
         (findViewById(R.id.button_claim_overtime)).setOnClickListener(this);
         (findViewById(R.id.change_date_button)).setOnClickListener(this);
         projectNameSpinner = (Spinner)findViewById(R.id.projectnamespinner);
         projectPhaseSpinner = (Spinner)findViewById(R.id.phasenamespinner);
         workTypeSpinner = (Spinner)findViewById(R.id.worktypespinner);
         
         if(projectList == null) {
         	projectList = new ArrayList<S3CaseItem>();
         	projectsProgress.setVisibility(View.VISIBLE);
         	new LoadCasesXMLTask(this).execute();
         	Toast.makeText(this, "Started to load projects... they will be available once loaded...", Toast.LENGTH_SHORT).show();
         }
         if(phaseList == null) {
         	phaseList = new ArrayList<S3PhaseItem>();
         }
         if(workTypeList == null) {
         	workTypeList = new ArrayList<S3WorkTypeItem>();
         }
 
         
         //as the spinner gets enabled and disabled from the asynctask, make sure it's not null
       
         projectAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, projectList);
         projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         projectNameSpinner.setAdapter(projectAdapter);
         projectNameSpinner.setOnItemSelectedListener(this);
         
         phaseAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,phaseList);
         phaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         projectPhaseSpinner.setOnItemSelectedListener(this);
         projectPhaseSpinner.setAdapter(phaseAdapter);
         
         workTypeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,workTypeList);
         workTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         workTypeSpinner.setOnItemSelectedListener(this);
         workTypeSpinner.setAdapter(workTypeAdapter);
         
         claimDate = Calendar.getInstance();
        
         Log.d(TAG,"OnCreate for main activity done.");
 
     }
 
     
     
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		if(requestCode == SevedroidProjectActivity.requestCode && resultCode == Activity.RESULT_OK) {
 			Log.d(TAG,"API KEY input finished with ok result... re-create!");
 			//TODO: Some happy day, change this to Activity.reCreate(), api level 11
			runOnCreate();
 		} else {
 			Log.d(TAG,"Received activity result:"+resultCode+
 					" with owner requestCode of "+requestCode+", but don't know what to do with it");
 		}
 	}
 
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 	
 	public List<S3CaseItem> getProjectList() {
 		return this.projectList;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		Log.d(TAG,"OnOptionsItemSelected...");
 		switch (item.getItemId()) {
 		case R.id.input_api_key_option:
 			Intent intent = new Intent();
 			intent.setClass(this, SevedroidConfig.class);
 			Log.d(TAG,"SevedroidConfig Activity starting...");
 			this.startActivity(intent);
 			break;
 		case R.id.query_claimed_hours:
 			Intent queryIntent = new Intent();
 			queryIntent.setClass(this, QueryHourEntries.class);
 			Bundle projectsListBundle = new Bundle();
 			projectsListBundle.putParcelableArrayList(PROJECTLIST_BUNDLE_KEY, projectList);
 			queryIntent.putExtras(projectsListBundle);
 			Log.d(TAG,"QueryHourEntries Activity starting...");
 			this.startActivity(queryIntent);
 			break;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	/**
 	 *  For some reason, I need to recreate the whole spinner, adapter, listener deal in order to
 	 *  get the Spinner to refresh. Please debug & optimize.
 	 *  DO NOT CALL FROM ASyncTask threads, will fail!
 	 *  These also do have the side-effect that the onItemSelected will get called. Thus, the
 	 *  binary switch there on that method.
 	 */
 	
 	private void projectSpinnerRefreshHack() {
 		this.projectAdapter.notifyDataSetChanged();
 		projectNameSpinner = (Spinner)findViewById(R.id.projectnamespinner);
         projectAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,projectList);
         projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         projectNameSpinner.setOnItemSelectedListener(this);
         projectNameSpinner.setAdapter(projectAdapter);
 	}
 
 	private void phaseSpinnerRefreshHack() {
 		this.phaseAdapter.notifyDataSetChanged();
 		projectPhaseSpinner = (Spinner)findViewById(R.id.phasenamespinner);
         phaseAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,phaseList);
         phaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         projectPhaseSpinner.setOnItemSelectedListener(this);
         projectPhaseSpinner.setAdapter(phaseAdapter);
 	}
 
 	private void workTypeSpinnerRefreshHack() {
 		this.workTypeAdapter.notifyDataSetChanged();
 		workTypeSpinner = (Spinner)findViewById(R.id.worktypespinner);
         workTypeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,workTypeList);
         workTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         workTypeSpinner.setOnItemSelectedListener(this);
         workTypeSpinner.setAdapter(workTypeAdapter);
 	}
 	
 	@Override
 	public void onItemSelected(AdapterView<?> adapterView, View view, int position,
 			long id) {
 		Log.d(TAG,"OnItemSelected called: adapterViewID:"+adapterView.getId()+"view ID:"+view.getId()+", pos:"+position+" id:"+id);
 		if(adapterView.getId() == R.id.projectnamespinner) {
 			/* user selected new case, look up case phases */
 			if(stateRestored) {
 				Log.d(TAG,"Because state is just restored, ignoring this onItemSelected event as a side effect of that...");
 				return;
 			} else {
 				String caseGuid = projectList.get(position).getCaseGuid();
 				Log.d(TAG,"User selected case pos: "+position+" with GUID: "+caseGuid);
 				phasesProgress.setVisibility(View.VISIBLE);
 				new LoadPhasesXMLTask(this).execute(caseGuid);
 			}
 		} else if(adapterView.getId() == R.id.phasenamespinner) {
 			if(stateRestored) {
 				Log.d(TAG,"Because state is just restored, ignoring this onItemSelected event as a side effect of that...");
 				return;
 			} else {
 				Log.d(TAG,"User selected phase pos: "+position);
 				Log.d(TAG,"Phase GUID for claiming:"+phaseList.get(position).getPhaseGUID());
 				Log.d(TAG,"Phase Name for claiming:"+phaseList.get(position).getPhaseName());
 				String phaseGuid = phaseList.get(position).getPhaseGUID();
 				this.currentPhaseGUID = phaseGuid;
 				new LoadWorkTypesXMLTask(this).execute(phaseGuid);
 			}
 		} else if(adapterView.getId() == R.id.worktypespinner) {
 			if(stateRestored) {
 				Log.d(TAG,"Because state is just restored, ignoring this onItemSelected event as a side effect of that...");
 				stateRestored = false;
 				return;
 			} else {
 				String workTypeGuid = workTypeList.get(position).getWorkTypeGUID();
 				Log.d(TAG,"User selected work type pos: "+position);
 				Log.d(TAG,"WorkType Guid for claiming:"+workTypeList.get(position).getWorkTypeGUID());
 				Log.d(TAG,"WorkType Name for claiming:"+workTypeList.get(position).getWorkTypeName());
 				this.currentWorkTypeGUID = workTypeGuid;
 			}
 		}
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		Log.d(TAG,"OnNothingSelected called.");
 	}
 	
 	@Override
 	public void onClick(View v) {
 		Log.d(TAG,"OnClick Called for view ID: "+v.getId());
 		int id = v.getId();
 		EditText hoursDisplay = (EditText)this.findViewById(R.id.hours_amount);
 		EditText minutesDisplay = (EditText)this.findViewById(R.id.minutes_amount);
 		//check if hours display and minutes display are numeric. If not, reset to zero
 		try {
 			Integer.parseInt(hoursDisplay.getText().toString());
 		} catch (NumberFormatException e) {
 			hoursDisplay.setText("0");
 		}
 		try {
 			Integer.parseInt(minutesDisplay.getText().toString());
 		} catch (NumberFormatException e) {
 			minutesDisplay.setText("0");
 		}
 		switch(id) {
 			case R.id.button1hour:
 				hoursDisplay.setText("1");
 				minutesDisplay.setText("0");
 				Log.d(TAG, "Adding 1 to hours...");
 				break;
 			case R.id.button2hour:
 				hoursDisplay.setText("2");
 				minutesDisplay.setText("0");
 				Log.d(TAG, "Adding 2 to hours...");
 				break;
 			case R.id.button3hour:
 				hoursDisplay.setText("3");
 				minutesDisplay.setText("0");
 				break;
 			case R.id.button4hour:
 				hoursDisplay.setText("4");
 				minutesDisplay.setText("0");
 				break;
 			case R.id.button5hour:
 				hoursDisplay.setText("5");
 				minutesDisplay.setText("0");
 				break;
 			case R.id.button6hour:
 				hoursDisplay.setText("6");
 				minutesDisplay.setText("0");
 				break;
 			case R.id.button7hour:
 				hoursDisplay.setText("7");
 				minutesDisplay.setText("0");
 				break;
 			case R.id.button8hour:
 				hoursDisplay.setText("8");
 				minutesDisplay.setText("0");
 				break;
 			case R.id.button9hour:
 				hoursDisplay.setText("9");
 				minutesDisplay.setText("0");
 				break;
 			case R.id.button10hour:
 				hoursDisplay.setText("10");
 				minutesDisplay.setText("0");
 				break;
 			case R.id.buttonplus30min:
 				// add 30 minutes to minutes display and try to be smart about it
 				Log.d(TAG,"Minutesdisplay is: ["+minutesDisplay.getText()+"]");
 				if(minutesDisplay.getText() == null || 
 					minutesDisplay.getText().toString().equals("") || 
 					minutesDisplay.getText().toString().equals("0"))  {
 					Log.d(TAG,"Since it seems null, setting minutes display to 30.");
 					minutesDisplay.setText("30");
 				} else {
 					Log.d(TAG,"Since it is not null, incrementing minutes display by 30.");
 					Integer minutesAmount = new Integer(minutesDisplay.getText().toString());
 					int realAmount = minutesAmount.intValue();
 					realAmount += 30;
 					if (realAmount > 59) {
 						Integer currentHoursAmount = new Integer(hoursDisplay.getText().toString());
 						int realHours = currentHoursAmount.intValue();
 						hoursDisplay.setText(String.valueOf(realHours+1));
 						minutesDisplay.setText(String.valueOf(realAmount-60));
 					} else {
 						minutesDisplay.setText(String.valueOf(realAmount));
 					}
 				}
 				break;
 			case R.id.button_claim_overtime:
 			case R.id.button_claim:
 				Log.d(TAG,"Started to claim...");
 				String description = ((EditText)findViewById(R.id.explanation_text)).getText().toString();
 				if(claimDate == null) {
 						claimDate = Calendar.getInstance();
 				}
 				String eventDate = SevedroidConstants.S3_DATE_FORMATTER.format(claimDate.getTime());
 				String phaseGuid = this.currentPhaseGUID;
 				String hours = ((EditText)findViewById(R.id.hours_amount)).getText().toString();
 				String minutes = ((EditText)findViewById(R.id.minutes_amount)).getText().toString();
 				String quantity = hours+"."+Math.round((Integer.parseInt(minutes))/0.6);
 				SevedroidContentStore scs = new SevedroidContentStore(this);
 				String userGuid = scs.fetchUserGUID();
 				String workTypeGuid = this.currentWorkTypeGUID;
 				String [] params = {description, eventDate, phaseGuid, quantity, userGuid, workTypeGuid};
 				new PublishHourEntryTask(this).execute(params);
 				break;
 			case R.id.change_date_button:
 				Log.d(TAG, "Change claim date...");
 				showDialog(DATE_PICKER_DIALOG_ID);
 				break;
 			default:
 				Log.d(TAG, "Click event detected but no action taken...");
 				break;
 		}
 		return;
 	}
 
 	protected void receiveProjectLoadingReadyEvent() {
 		Log.d(TAG,"Received project loading ready event on UI thread...");
 		Log.d(TAG,"Here, length of loaded projects list: "+projectList.size());
 		projectSpinnerRefreshHack();
 		Toast.makeText(this, "Projects are now loaded, you can now make your selection.", Toast.LENGTH_SHORT).show();
 		projectsProgress.setVisibility(View.GONE);
 	}
 	
 	protected void receivePhasesLoadingReadyEvent() {
 		Log.d(TAG,"Received phases loading ready event on UI thread...");
 		Log.d(TAG,"Here, length of loaded phases list: "+phaseList.size());
 		phaseSpinnerRefreshHack();
 		Toast.makeText(this, "Phases are now loaded, you can now make your selection.", Toast.LENGTH_SHORT).show();
 		phasesProgress.setVisibility(View.GONE);
 	}
 	
 	protected void receiveWorkTypesLoadingReadyEvent() {
 		Log.d(TAG,"Received work types loading ready event on UI thread...");
 		Log.d(TAG,"Here, length of loaded worktypes list: "+workTypeList.size());
 		workTypeSpinnerRefreshHack();
 		Toast.makeText(this, "Work types are now loaded, you can now make your selection.", Toast.LENGTH_SHORT).show();
 		phasesProgress.setVisibility(View.GONE);
 	}
 	
 	protected void receivePublishHourEntryReadyEvent() {
 		Log.d(TAG,"Received hour entry ready event on UI thread...");
 		Log.d(TAG,"Result was: "+hourEntryStatus);
 		if(hourEntryStatus) {
 			Toast.makeText(this, "Your work hours have been saved!", Toast.LENGTH_SHORT).show();	
 		} else {
 			Toast.makeText(this, "Failed to save your work hours! Please try again later!", Toast.LENGTH_LONG).show();
 		}
 		
 	}
 	
 	
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		// TODO Auto-generated method stub
 		if(id == DATE_PICKER_DIALOG_ID) {
 			return new DatePickerDialog(this, mDateSetListener, 
 					claimDate.get(Calendar.YEAR),
 					claimDate.get(Calendar.MONTH),
 					claimDate.get(Calendar.DAY_OF_MONTH));
 		}
 		if(id == NOT_CONNECTED_DIALOG_ID) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("This application requires a network connection.")
 			       .setCancelable(false)
 			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                SevedroidProjectActivity.this.finish();
 			           }
 			       });
 			AlertDialog alert = builder.create();
 			return alert;	
 		}
 		return null;
 	}
 
 	//TODO: Make these asynctasks go away from this already bloated class
 
 
 	/**
 	 * AsyncTask for loading the cases XML from S3 SOAP service 
 	 */
 	
 	private class LoadCasesXMLTask extends AsyncTask<Void, Integer, Boolean> {
 		
 		private SevedroidProjectActivity mParent;
 		public static final int STATUS_INIT = 1;
 		public static final int STATUS_TRANSFERRING = 2;
 		public static final int STATUS_PARSING = 3;
 		public static final int STATUS_RETURNING = 4;
 		
 		public LoadCasesXMLTask(SevedroidProjectActivity parent) {
 			mParent = parent;
 		}
 		
 		@Override
 		protected Boolean doInBackground(Void... nullArgs) {
 			Log.d(TAG,"Started doInBackground for LoadCasesXMLTask!");
 			mParent.projectNameSpinner.setClickable(false);
 			publishProgress(STATUS_INIT);
 			SeveraCommsUtils scu = new SeveraCommsUtils();
 			S3CaseContainer S3Cases = S3CaseContainer.getInstance();
 			//The next invocation is the one that takes time
 	        publishProgress(STATUS_TRANSFERRING);
 			S3Cases.setCasesXML(scu.getAllCasesXml(mParent));
 	        publishProgress(STATUS_PARSING);
 			mParent.projectList = S3Cases.getCases();
 			publishProgress(STATUS_RETURNING);
 			if(mParent.projectList.isEmpty()) {
 				Log.e(TAG,"Project list is empty! Returning with nothing to tell.");
 				return new Boolean(false);
 			} else {
 				Log.d(TAG,"Project list is not empty, enabling projectspinner...");
 				mParent.projectNameSpinner.setClickable(true);
 				return new Boolean(true);
 			}
 		}
 		
 		protected void onProgressUpdate(Integer... progress) {
 			Log.d(TAG,"Setting progress to: "+progress[0].toString());
 	         setProgress(progress[0]);
 	     }
 		
 		@Override
 		protected void onPostExecute(Boolean result) {
 			Log.d(TAG,"onPostExecute on LoadCasesXMLTask firing.");
 			mParent.receiveProjectLoadingReadyEvent();
 		}
 	}
 	
 	/**
 	 * AsyncTask for loading the phases XML from S3 SOAP service
 	 */
 	
 	private class LoadPhasesXMLTask extends AsyncTask<String, Integer, Boolean> {
 		
 		private SevedroidProjectActivity mParent;
 		public static final int STATUS_INIT = 1;
 		public static final int STATUS_TRANSFERRING = 2;
 		public static final int STATUS_PARSING = 3;
 		public static final int STATUS_RETURNING = 4;
 		
 		public LoadPhasesXMLTask(SevedroidProjectActivity parent) {
 			mParent = parent;
 		}
 		
 		@Override
 		protected Boolean doInBackground(String... caseGuid) {
 			Log.d(TAG,"Started doInBackground for LoadPhasesXMLTask!");
 			mParent.projectPhaseSpinner.setClickable(false);
 			publishProgress(STATUS_INIT);
 			SeveraCommsUtils scu = new SeveraCommsUtils();
 			S3PhaseContainer S3Phases = S3PhaseContainer.getInstance();
 			//The next invocation is the one that takes time
 	        publishProgress(STATUS_TRANSFERRING);
 			S3Phases.setPhasesXML(scu.getPhasesXMLByCaseGUID(mParent, caseGuid[0]));
 	        publishProgress(STATUS_PARSING);
 			mParent.phaseList = S3Phases.getPhases();
 			publishProgress(STATUS_RETURNING);
 			if(mParent.phaseList.isEmpty()) {
 				Log.e(TAG,"Phase list is empty! Returning with nothing to tell.");
 				return new Boolean(false);
 			} else {
 				Log.d(TAG,"Project list is not empty, enabling projectspinner...");
 				mParent.projectPhaseSpinner.setClickable(true);
 				return new Boolean(true);
 			}
 		}
 		
 		protected void onProgressUpdate(Integer... progress) {
 			Log.d(TAG,"Setting progress to: "+progress[0].toString());
 	         setProgress(progress[0]);
 	     }
 		
 		@Override
 		protected void onPostExecute(Boolean result) {
 			Log.d(TAG,"onPostExecute on LoadCasesXMLTask firing.");
 			mParent.receivePhasesLoadingReadyEvent();
 		}
 	}
 	
 	/**
 	 * AsyncTask for loading the worktypes XML from S3 SOAP service
 	 */
 	
 	private class LoadWorkTypesXMLTask extends AsyncTask<String, Integer, Boolean> {
 		
 		private SevedroidProjectActivity mParent;
 		public static final int STATUS_INIT = 1;
 		public static final int STATUS_TRANSFERRING = 2;
 		public static final int STATUS_PARSING = 3;
 		public static final int STATUS_RETURNING = 4;
 		
 		public LoadWorkTypesXMLTask(SevedroidProjectActivity parent) {
 			mParent = parent;
 		}
 		
 		@Override
 		protected Boolean doInBackground(String... phaseGuid) {
 			Log.d(TAG,"Started doInBackground for LoadPhasesXMLTask!");
 			mParent.workTypeSpinner.setClickable(false);
 			publishProgress(STATUS_INIT);
 			SeveraCommsUtils scu = new SeveraCommsUtils();
 			S3WorkTypeContainer S3WorkTypes = S3WorkTypeContainer.getInstance();
 			//The next invocation is the one that takes time
 	        publishProgress(STATUS_TRANSFERRING);
 			S3WorkTypes.setWorkTypesXML(scu.getWorkTypesXMLByPhaseGUID(mParent, phaseGuid[0]));
 	        publishProgress(STATUS_PARSING);
 			mParent.workTypeList = S3WorkTypes.getWorkTypes();
 			publishProgress(STATUS_RETURNING);
 			if(mParent.workTypeList.isEmpty()) {
 				Log.e(TAG,"Worktype list is empty! Returning with nothing to tell.");
 				return new Boolean(false);
 			} else {
 				Log.d(TAG,"Work type list is not empty, enabling worktypespinner...");
 				mParent.workTypeSpinner.setClickable(true);
 				return new Boolean(true);
 			}
 		}
 		
 		protected void onProgressUpdate(Integer... progress) {
 			Log.d(TAG,"Setting progress to: "+progress[0].toString());
 	         setProgress(progress[0]);
 	     }
 		
 		@Override
 		protected void onPostExecute(Boolean result) {
 			Log.d(TAG,"onPostExecute on LoadWorkTypesXMLTask firing.");
 			mParent.receiveWorkTypesLoadingReadyEvent();
 		}
 	}
 	
 	/**
 	 * AsyncTask for calling the IHourEntry
 	 */
 	
 	private class PublishHourEntryTask extends AsyncTask<String, Integer, Boolean> {
 		
 		SevedroidProjectActivity mParent = null;
 		
 		PublishHourEntryTask(SevedroidProjectActivity activity) {
 			mParent = activity;
 		}
 
 		/**
 		 * Publish this hour entry
 		 * @params String Description
 		 * @params String EventDate - the date formatted YYYY-MM-DD
 		 * @params String Phase's guid
 		 * @params String quantity amount of hours formatted as "1.5" (that's DOT, not comma)
 		 * @params String user guid as obtained from app config
 		 * @params String work type GUID
 		 */
 		
 		@Override
 		protected Boolean doInBackground(String... params) {
 			//gather necessary parameters
 			String description = params[0];
 			String eventDate = params[1];
 			String phaseGuid = params[2];
 			String quantity = params[3];
 			String userGuid = params[4];
 			String workTypeGuid = params[5];
 			SeveraCommsUtils scu = new SeveraCommsUtils();
 			boolean res = scu.publishHourEntry(mParent, description, eventDate, phaseGuid, quantity, userGuid, workTypeGuid);
 			return new Boolean(res);
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			Log.d(TAG,"onPostExecute on PublishHourEntryTask firing.");
 			if(result) {
 				mParent.hourEntryStatus = true;
 			} else {
 				mParent.hourEntryStatus = false;
 			}
 			mParent.receivePublishHourEntryReadyEvent();
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			// TODO Auto-generated method stub
 			super.onProgressUpdate(values);
 		}
 		
 		
 	}
 	
 }
