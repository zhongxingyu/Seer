 /*
  *Chris Card
  *Nathan Harvey
  *10/27/12
  *This class contains the code for retrieving and saving users input to a new task or modification of an old task 
  */
 package csci422.CandN.to_dolist;
 
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.widget.ArrayAdapter;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class DetailForm extends Activity {
 
 	//------------------------------------------------------
 	//Beginning static fields
 	//------------------------------------------------------
 	
 	public static final String DETAIL_EXTRA = "csci422.CandN.to_dolist.curItem";
 
 	public static final String tag = "todoDetail";
 	
 	//------------------------------------------------------
 	//End static fields
 	//------------------------------------------------------
 
 	//------------------------------------------------------
 	//Beginning widget objects
 	//------------------------------------------------------
 	
 	private ImageButton[] priors = new ImageButton[4];
 	private EditText datetext;
 	private EditText address;
 	private EditText street;
 	private ToDoHelper helper;
 	private Cursor cur = null;
 	private Spinner pickList;//for future multi-list functionality
 	private EditText taskName;
 	private EditText notes;
 	private String[] Listnames = {"Main","Homework","Shopping"};
 	private SeekBar completion;
 
 	/** -1 is ?, 0 is a dot, 1 is one !, 2 is two !!  */
 	private int priority = -1;
 	private Date dueDate;
 	private DateFormat dateFormat;
 	
 	//------------------------------------------------------
 	//End widget objects
 	//------------------------------------------------------
 
 	//------------------------------------------------------
 	//Beginning gps Location vars and support objects
 	//------------------------------------------------------
 	
 	private LocationManager locmgr = null;
 
 	private ProgressDialog pd;//show spinning wheel while it is finding the location
 
 	private AtomicBoolean cancelLocation = new AtomicBoolean(false);//thread safe boolean will be using with a listener for location
 
 	private AtomicBoolean continueSearch = new AtomicBoolean(true);
 
 	private AtomicBoolean hasDialogShown = new AtomicBoolean(false);
 
 	private WaitForLocation gpsWait;
 
 	//strings for telling user about latitude and longitude
 	private static final String Lat = "LATITUDE:";
 	private static final String Lon = "LONGITUDE:";
 
 	private static final long gpsWaitDuration = 120000;//wait time for async task
 
 	private Builder alertBuild;
 	//private Builder dateAlert;  //TODO removed because
 	private AlertDialog promptContin;//builder for alert dialog
 
 	//end code get gps location
 
 	//------------------------------------------------------
 	//delete fields
 	//------------------------------------------------------
 
 	private boolean delete = false;
 
 	//------------------------------------------------------
 	//end delete code
 	//------------------------------------------------------
 
 	private String id = null;//id needs to be accessible to whole class so it can be used with todo helper
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		helper = new ToDoHelper(this);
 		
 		setContentView(R.layout.detail_form);
 
 		initWidgets();
 
 		//initDateDialog();
 
 		initFindLocation();
 
 		ArrayAdapter<CharSequence> adpt = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_single_choice, Listnames);
 		//pickList.setAdapter(adpt); //for future functionality
 	}
 	
 	//------------------------------------------------------
 	//Begining of Life Cycle Events
 	//------------------------------------------------------
 	
 	/**
 	 * only gets called if (by os) if there is a saved instatance state
 	 * @param state
 	 */
 	@Override
 	public void onRestoreInstanceState(Bundle state)
 	{
 		super.onRestoreInstanceState(state);
 		id = state.getString("IDofTask");
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		loadCurrent();
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString("IDofTask", id);
 	}
 	
 	@Override
 	protected void onDestroy() 
 	{
 		super.onDestroy();
 
 		if(gpsWait.getStatus() == AsyncTask.Status.RUNNING)
 		{//if the asyntask is still running for finding the location
 			gpsWait.cancel(true);
 		}
 
 		if(!delete)
 		{//if the user decided to delete the task make sure that
 			//it doesn't save again
 			saveStuff();
 		}
 		helper.close();
 	}
 	
 	//------------------------------------------------------
 	//End life cycle events
 	//------------------------------------------------------
 	
 	//------------------------------------------------------
 	//Beginning of initializing 
 	//------------------------------------------------------
 	
 	/**
 	 *This method initializes the widgets in the detail form layout
 	 */
 	private void initWidgets()
 	{
 		completion = (SeekBar) findViewById(R.id.completion);
 
 		//initializes priortity buttons
 		priors[0] = (ImageButton) findViewById(R.id.Priorityq);
 		priors[1] = (ImageButton) findViewById(R.id.Priority0);
 		priors[2] = (ImageButton) findViewById(R.id.Priority1);
 		priors[3] = (ImageButton) findViewById(R.id.Priority2);
 		
 		//date pickers
 		datetext = (EditText)findViewById(R.id.dueDatePicker);
 		datetext.setOnFocusChangeListener(new OnFocusChangeListener(){
 			public void onFocusChange(View arg0, boolean arg1) {
 				if(arg1) openCal(datetext);
 			}
 		});
 
 		dueDate = new Date();//current time
 		dateFormat = new SimpleDateFormat();
 		dateFormat.setLenient(true);
 		//pickList = ((Spinner) findViewById(R.id.pickList)); //for future functionality
 		taskName = ((EditText) findViewById(R.id.taskName));
 		notes = ((EditText) findViewById(R.id.notes));
 		address = (EditText)findViewById(R.id.address);
 		street = (EditText)findViewById(R.id.street);
 	}
 
 	/**
 	 * This initalizes datedialog confirmation.
 	 * TODO We removed this because 
 	 */
 	/*
 	private void initDateDialog()
 	{
 		dateAlert = new AlertDialog.Builder(this);
 		dateAlert.setPositiveButton("Yes", new OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				openCal(null);
 			}
 		});
 		dateAlert.setNegativeButton("No", null);
 		dateAlert.setTitle("Warning");
 		dateAlert.setMessage("Editing the date manually tends to lead to Date Parse Errors.\n  Would you like to select a date instead?");
 	}
 	*/
 
 	/**
 	 *This method sets up everything needed for the task of finding users location
 	 */
 	private void initFindLocation()
 	{
 		locmgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 
 		gpsWait = new WaitForLocation();//creates new async task
 
 		//inits the progress dialog with title message
 		pd = new ProgressDialog(this);
 		pd.setTitle("Finding location");
 		pd.setMessage("This could take a few minutes depending on your location.");
 		pd.setCancelable(true);//allows the dialog to be cancelable
 		pd.setIndeterminate(true);//this sets the spinning animation instead of progress
 		pd.setOnCancelListener(cancel);
 		pd.setButton(pd.BUTTON_NEUTRAL, "Cancel", new OnClickListener(){
 			public void onClick(DialogInterface dialog, int which) 
 			{
 				cancelLocation.set(true);
 				gpsWait.cancel(true);
 			}
 		});
 
 		//inits alertdialog with appropriate listenere and message
 		alertBuild = new AlertDialog.Builder(this);
 		alertBuild.setPositiveButton("Yes", new OnClickListener(){
 			public void onClick(DialogInterface arg0, int arg1) {
 				//nothing needed to be done
 				arg0.dismiss();
 				continueSearch.set(true);
 			}
 		});
 
 		alertBuild.setNegativeButton("No", new OnClickListener(){
 			public void onClick(DialogInterface arg0, int arg1) {
 				continueSearch.set(false);
 				gpsWait.cancel(true);
 				arg0.dismiss();
 			}
 		});
 
 		alertBuild.setMessage("Do you wish to continue to find your location?");
 
 		promptContin = alertBuild.create();
 	}
 
 	/**
 	 *This method loads the current task and populates widgets if there was an id passed in
 	 *by the intent
 	 */
 	private void loadCurrent() 
 	{
 		if(id==null)id = getIntent().getStringExtra(DETAIL_EXTRA);
 		if(id.length() == 0)
 		{
 			helper.insert("", "", "", "", "", 0, -1);
 			Handler loadDelay = new Handler();
 			loadDelay.postDelayed(new Runnable(){
 				public void run() {
 					Cursor v = helper.getAll("_id DESC");
 					v.moveToFirst();
 					id = v.getString(0);
 					v.close();
 					cur = helper.getById(id);
 				}
 			}, 700);
 			return;
 		}
 
 		cur = helper.getById(id);
 		cur.moveToFirst();//need to set cursor to the beginning
 
 		taskName.setText(helper.getTitle(cur));
 		loadAddressFields(helper.getAddress(cur));
 		notes.setText(helper.getNotes(cur));
 		if(!helper.getDate(cur).isEmpty()){
 			try {
 				dueDate = dateFormat.parse(helper.getDate(cur));
 			} catch (ParseException e) {
 				Log.e(tag, "Can't parse the date.");
 			}
 		}
 		datetext.setText(helper.getDate(cur));
 
 		completion.setProgress(helper.getState(cur));
 		priority = helper.getPriority(cur);
 		priors[priority+1].setBackgroundResource(R.drawable.highlight);
 	}
 	
 	/**
 	 * This method sets the two address fields if to the passed in string
 	 * or nothing if empty passed in string 
 	 * @param addre the street and address fields seperated by + or empty
 	 */
 	private void loadAddressFields(String addre)
 	{
 		if(!addre.isEmpty())
 		{
 			String words[] = addre.split("\\+");
 
 			street.setText(words[0]);
 			if(words.length >= 2) address.setText(words[1]);
 		}
 	}
 	
 	//------------------------------------------------------
 	//End initializing
 	//------------------------------------------------------
 
 	//------------------------------------------------------
 	//Beginning save code
 	//------------------------------------------------------
 	
 	/**
 	 *This method is called when need to
 	 */
 	public void saveStuff(){
 
 		int state = completion.getProgress();
 		
 		String dateString;
 
 		if(!datetext.getText().toString().isEmpty()){
 			try {
 				dueDate = dateFormat.parse(datetext.getText().toString());
 			} catch (ParseException e) {
 				Log.e(tag, "Can't parse the date. User probably edited manually:");
 				Log.e(tag, e.getMessage());
 				dueDate = new Date();//default to current time if user goofed up.
 			}
 			dateString = dateFormat.format(dueDate);
 		}else dateString = "";
 
 		if(cur==null){//make a new one
 			helper.insert(taskName.getText().toString(), parseAddressSave(), "Main", notes.getText().toString(), dateString, state, priority);
 		}else {//edit current
 			helper.update(id, taskName.getText().toString(), parseAddressSave(), "Main", notes.getText().toString(), dateString, state, priority);
 			helper.notified(id, false);
 		}
 		
 		Handler delay = new Handler();
 		delay.postDelayed(new Runnable(){
 			public void run() 
 			{
 				if(cur != null)
 				{
 					Cursor l = helper.getById(id);
 					l.moveToFirst();
 					OnBootReceiver.cancelAlarm(DetailForm.this, helper, l);
 					OnBootReceiver.setAlarm(DetailForm.this, helper, l);//sets alarm as well just incase it has been edited from the widget which wont update the alar
 					l.close();
 				}
 				else
 				{
 					Cursor l = helper.getAll("_id DESC");
 					l.moveToFirst();
 					OnBootReceiver.setAlarm(DetailForm.this, helper, l);//sets alarm as well just incase it has been edited from the widget which wont update the alar
 					l.close();
 				}
 			}
 		}, 650);
 	}
 
 	/**
 	 * This method takes the text from street and address text fields and parses it for
 	 * the data base
 	 * @return the string with the two fields seperated by a + or empty string
 	 */
 	private String parseAddressSave()
 	{
 		String ret;
 		if(!address.getText().toString().isEmpty() || !street.getText().toString().isEmpty())
 		{//if at least one of them has text
 			ret = street.getText().toString()+"+"+address.getText().toString();
 			return ret;
 		}
 		else
 		{
 			ret = "";
 			return ret;
 		}
 	}
 	
 	//------------------------------------------------------
 	//End save code
 	//------------------------------------------------------
 
 
 	public void priq(View v){priority=-1;clr(v);}
 	public void prin(View v){priority=0;clr(v);}
 	public void prio(View v){priority=1;clr(v);}
 	public void prit(View v){priority=2;clr(v);}
 
 	//------------------------------------------------------
 	//Beginning of Calls from buttons in detail_form layout file
 	//------------------------------------------------------
 	
 	/**
 	 * Clears the backgrounds for all priority buttons except one.
 	 * @param v the view to give an active background.
 	 */
 	public void clr(View v){
 		Log.d(tag, "Priority: "+priority);
 		for(ImageButton b : priors){
 			b.setBackgroundResource(R.drawable.priorityblank);
 		}
 		v.setBackgroundResource(R.drawable.highlight);
 	}
 	
 	/**
 	 *This method is called by the Done button in the detail_form layout
 	 */
 	public void onDone(View v)
 	{
 		finish();
 	}
 	
 	/**
 	 * This method opens our date_dialog so user can select a date
 	 * and we can keep it in a certain formate
 	 * @param v
 	 */
 	public void openCal(View v){ 
 		Builder calDialog = new Builder(this);
 		LayoutInflater inf = getLayoutInflater();
		calDialog.setView(inf.inflate(R.layout.date_dialog, null));
 		calDialog.setCancelable(true);
 		calDialog.setPositiveButton("Done", new OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int which) {
 				((TextView)((Dialog)dialog).findViewById(R.id.pleaseWaitMessage)).setText("Saving...");
 				DatePicker dp = ((DatePicker)((Dialog)dialog).findViewById(R.id.datePicker1));
 				TimePicker tp = ((TimePicker)((Dialog)dialog).findViewById(R.id.timePicker1));
 				GregorianCalendar gc;
 				gc = new GregorianCalendar(dp.getYear(),dp.getMonth(),dp.getDayOfMonth(),
 						tp.getCurrentHour(),tp.getCurrentMinute());//Is this the current, or selected?
 				DetailForm.this.setDueDate(gc.getTime());
 			}
 		});

 		calDialog.show();
 	}
 	
 	/**
 	 * The method is called by the here button add a locationlistenere to start gps to look for the users location
 	 * shows spinner dialog and starts the async task to wait for the location change
 	 * @param v
 	 */
 	public void getLoc(View v)
 	{
 		if(gpsWait.getStatus() == AsyncTask.Status.RUNNING)
 		{//if the spwwait is still running
 			gpsWait.cancel(true);
 		}
 
 		gpsWait = new WaitForLocation();
 
 		if(gpsWait.getStatus() == AsyncTask.Status.PENDING)
 		{//if gpsWait is waiting to run
 			locmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, onLocChange);//starts gps and listening for location change
 			pd.show();
 			gpsWait.execute("");
 		}
 	}
 
 	/**
 	 * This method opens the google maps app with a search based on what the user stores in the
 	 * address and street fields
 	 * @param v
 	 */
 	public void openMaps(View v)
 	{
 		if(!address.getText().toString().isEmpty() || !street.getText().toString().isEmpty())
 		{//add code here for lat and lon if applicable
 			if(street.getText().toString().contains(Lat) && address.getText().toString().contains(Lon))
 			{//if lat and lon keywords are in the text fields
 				String lat[] = street.getText().toString().split(":");
 				String lon[] = address.getText().toString().split(":");
 
 				String uri = "geo:"+lat[1]+","+lon[1];
 				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
 			}
 			else
 			{//send query to google maps
 				String uri = "geo:0,0?q="+street.getText().toString()+"+"+address.getText().toString();
 				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
 			}
 		}
 		else
 		{//ask user to fill the fields out before clicking the button
 			Toast.makeText(this, "Please fill out the two address fields or click the here button", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	//------------------------------------------------------
 	//End of Calls from buttons in detail_form layout file
 	//------------------------------------------------------
 	
 	//------------------------------------------------------
 	//Beginning menu code from detail_form menu
 	//------------------------------------------------------
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.detail_form_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		if(item.getItemId() == R.id.done)
 		{
 			finish();
 		}
 		else if(item.getItemId() == R.id.delete)
 		{
 			new Builder(this).setTitle("Do you want to delete the task?")
 			.setMessage("This action cannot be undone!")
 			.setPositiveButton("Yes", new OnClickListener(){
 				public void onClick(DialogInterface dialog,int which) {
 					deleteTask();
 				}
 			})
 			.setNegativeButton("No", null)
 			.create()
 			.show();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 
 	/**
 	 * This code sets up to delete the task or cancel adding the task
 	 */
 	public void deleteTask()
 	{
 		if(!id.isEmpty())
 		{//if there is an id 
 			Cursor c = helper.getById(id);
 			c.moveToFirst();
 			OnBootReceiver.cancelAlarm(this, helper, c);
 			helper.delete(id);
 			delete = true;
 			c.close();
 			finish();//kill activity
 		}
 		else
 		{//if no id just don't save
 			delete = true;
 			finish();
 		}
 	}
 	
 	//------------------------------------------------------	
 	//End menu code from detail_form menu
 	//------------------------------------------------------
 	
 	//------------------------------------------------------
 	//Beginning listeners
 	//------------------------------------------------------
 	
 	//gets location from gps
 	private LocationListener onLocChange = new LocationListener(){
 
 		public void onLocationChanged(Location location) 
 		{	
 			if(null != location)
 			{
 				//brings the latitude and longitude into something google maps can intemperate
 				//correctly
 				street.setText(Lat+(int)(location.getLatitude()*1E6));
 				address.setText(Lon+(int)(location.getLongitude()*1E6));
 
 				cancelLocation.set(true);
 
 				gpsWait.cancel(true);//cancels the async task if the location is gotten
 			}	
 		}
 
 		public void onProviderDisabled(String provider) {}
 		public void onProviderEnabled(String provider) {}
 		public void onStatusChanged(String provider, int status, Bundle extras) {}
 	};
 
 	//this is a listener for the spinner dialog so when the back button is pressed it performs that tasks below
 	private OnCancelListener cancel = new OnCancelListener(){
 		public void onCancel(DialogInterface arg0) {
 			cancelLocation.set(true);
 			gpsWait.cancel(true);
 		}
 	};
 
 	
 	
 	public Date getDueDate() {
 		return dueDate;
 	}
 
 	public void setDueDate(Date dueDate) {
 		this.dueDate = dueDate;
 		datetext.setText(dateFormat.format(dueDate));
 	}
 	
 	//------------------------------------------------------
 	//End listeners
 	//------------------------------------------------------	
 
 
 	/**
 	 * This async task will wait until the user cancels or the location is found
 	 * @author Chris Card
 	 *
 	 */
 	private class WaitForLocation extends AsyncTask<String, String, String>
 	{
 		@Override
 		protected String doInBackground(String... params)
 		{
 			//while the atomic booleans both are false
 			while(!cancelLocation.get() && continueSearch.get())
 			{
 
 				try{
 					Thread.sleep(gpsWaitDuration);//sleeps the async task thread
 
 					if(!cancelLocation.get() && continueSearch.get())//checks both attomic booleans
 					{//not sure if this part works haven't got here yer
 						pd.dismiss();
 						publishProgress(params);
 
 						Thread.sleep(4000);
 						publishProgress(params);
 					}
 				}catch (InterruptedException e)
 				{
 					Log.e("DetailForm", e.getMessage());
 				}
 			}
 			return "Finished";
 		}
 
 		@Override
 		protected void onProgressUpdate(String... prog)
 		{
 			if(continueSearch.get() && hasDialogShown.get())
 			{
 				pd.show();
 				hasDialogShown.set(false);
 			}
 			else if(continueSearch.get())
 			{
 				promptContin.show();
 				hasDialogShown.set(true);
 			}
 
 		}
 
 		@Override
 		protected void onPostExecute(String result)
 		{
 			pd.dismiss();//dismiss the progress dialog
 			//remove location listener
 			locmgr.removeUpdates(onLocChange);
 			//reset atomic booleans
 			cancelLocation.set(false);
 			continueSearch.set(true);
 			hasDialogShown.set(false);
 		}
 
 		@Override
 		protected void onCancelled()
 		{
 			//removes listener
 			pd.dismiss();
 			locmgr.removeUpdates(onLocChange);
 			cancelLocation.set(false);
 			continueSearch.set(true);
 			hasDialogShown.set(false);
 		}
 
 	}
 
 
 
 }
