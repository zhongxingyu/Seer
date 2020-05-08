 package com.uiproject.meetingplanner;
 
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.uiproject.meetingplanner.database.MeetingPlannerDatabaseHelper;
 import com.uiproject.meetingplanner.database.MeetingPlannerDatabaseManager;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CreateMeetingConfirm extends Activity {
 
 	public static final String PREFERENCE_FILENAME = "MeetAppPrefs";
 	TextView title, desc, date, time, tracktime, attendees, location;
 	private MeetingPlannerDatabaseManager db;
 	private String mtitle, mdesc, maddr, mdate, mstarttime, mendtime, mattendeeids, mnames;
 	private int mtracktime, mlon, mlat, uid;
 	private ArrayList<Integer> attendessIdsArray;
 	private SharedPreferences.Editor editor;
 	public static final String createMeetingConfirmTag = "CreateMeetingConfirm";
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.createmeetingconfirm);
         title = (TextView) findViewById(R.id.title);
         desc = (TextView) findViewById(R.id.desc);
         date = (TextView) findViewById(R.id.date);
         time = (TextView) findViewById(R.id.time);
         tracktime = (TextView) findViewById(R.id.tracktime);
         attendees = (TextView) findViewById(R.id.attendees);
         location = (TextView) findViewById(R.id.location);
 
         // Get var values from sharedpreferences
     	SharedPreferences settings = getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE); 
     	editor = settings.edit();
     	uid = settings.getInt("uid", 0);
     	int month = settings.getInt("mdatem", 0) + 1;
     	int day = settings.getInt("mdated", 0);
     	int year = settings.getInt("mdatey", 0);
     	int sh = settings.getInt("mstarth", 0);
     	int sm = settings.getInt("mstartm", 0);
     	int eh = settings.getInt("mendh", 0);
     	int em = settings.getInt("mendm", 0);
     	mtitle = settings.getString("mtitle", "Untitled");
     	mdesc = settings.getString("mdesc", "No description");
     	maddr = settings.getString("maddr", "default addr");
     	mdate = month + "/" + day + "/" + year;
     	mstarttime = pad(sh) + ":" + pad(sm);
     	mendtime = pad(eh) + ":" + pad(em);
     	mtracktime = (int) ((double) settings.getFloat("mtracktime", (float).5) * 60D);
     	mlon = settings.getInt("mlon", 0);
     	mlat = settings.getInt("mlat", 0);
     	
     	mattendeeids = "2,5,6";
     	mnames = settings.getString("mnames", "");
    	mattendeeids = settings.getString("mattendeeids", "");
     	
     	// Set the view
     	title.setText(mtitle);
     	desc.setText(mdesc);
     	date.setText(mdate);
     	time.setText(mstarttime + "-" + mendtime);
     	tracktime.setText( (double) settings.getFloat("mtracktime", (float).5) + " hours");
     	location.setText(maddr);
     	attendees.setText(mnames);
     	
     	// Convert attendees ids string back to an array
     	String n;
         String p;
         int commaIndex;
         String tempids = mattendeeids;
     	attendessIdsArray = new ArrayList<Integer>();
     	if (tempids.length() > 0){
 	    	while (tempids.length() > 0){
 	    		commaIndex = tempids.indexOf(',');
 	    		if (commaIndex == -1){
 	    			int meetingId = Integer.parseInt(tempids);
 	    			attendessIdsArray.add(meetingId);
 	    			break;
 	    		}else{
 		    		n = tempids.substring(0, commaIndex);
 		    		int meetingId = Integer.parseInt(n);
 		    		attendessIdsArray.add(meetingId);
 		    		tempids = tempids.substring(commaIndex + 1);
 	    		}
     		}
     	}
     	
     	
     	// Hook up with database
 	    db = new MeetingPlannerDatabaseManager(this, MeetingPlannerDatabaseHelper.DATABASE_VERSION);
 	    
 	    
     }
 
 	public void back(View Button){
 		onBackPressed();
 	}
 	
     public void cancel(View Button){
 
     	clearData();
     	CreateMeetingConfirm.this.setResult(R.string.cancel_create);
     	CreateMeetingConfirm.this.finish();
     	
     }
 	
     @Override
     public void onBackPressed(){
     	finish();
     	
     }
     
     public void confirm(View button) throws ParseException{
     	
     	//save meeting data into the db, send to server
 
     	int mid = Communicator.createMeeting(uid, mtitle, mdesc, mlat, mlon, maddr, mdate, mstarttime, mendtime, mtracktime, mattendeeids);
     	Communicator.acceptMeeting(uid, mid); // accept meeting
 
     	int initiatorID = uid;
     	
     	// create meeting start time unix timestamp //TODO
     	// Get current unix timestamp
 		long currentUnixTime = System.currentTimeMillis() / 1000L;// TODO
     	
     	String mstartdatetime = mdate + " " + mstarttime;
     	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
 		Date date = (Date)formatter.parse(mstartdatetime);
 		int timestampInt = (int) (date.getTime() / 1000L);
 		Log.d(createMeetingConfirmTag, " create meeting: meetingID = " + mid + ", date.gettime = " + date.getTime() + ", timestamp = " + timestampInt + ", current timestamp = " + currentUnixTime);
     	
     	// Add meeting & meeting users to internal db
 		db.open();
     	db.createMeeting(mid, mtitle, mlat, mlon, mdesc, maddr, mdate, mstarttime, mendtime, mtracktime, initiatorID, timestampInt); 
     	db.createMeetingUser(mid, uid, MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_ATTENDING, "0");	// initiator
     	Log.d(createMeetingConfirmTag, "create meeting: mtracktime = " + mtracktime);
     	Log.d(createMeetingConfirmTag, "attendee array size = " + attendessIdsArray.size());
     	for(int i=0; i<attendessIdsArray.size(); i++){
     		db.createMeetingUser(mid, attendessIdsArray.get(i), MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_PENDING, "0"); 	// other attendees other than initiator
     	}
     	
     	MeetingInstance m = db.getNextUpcomingMeeting(uid);
 	    Log.d(createMeetingConfirmTag, "getNextUpcomingMeeting: " + "meetingID = " + m.getMeetingID());
 	    
 	    // Check next upcoming meeting
 	    int nextmid = m.getMeetingID();
 	    
 	    if(nextmid < 0){
 	    	editor.putInt("currentTrackingMid", -1);
 	    }else{
 	    	editor.putInt("currentTrackingMid", mid);
 	    }
 	    
 	    editor.commit(); 
 	    
     	db.close();
 
     	clearData();
     	CreateMeetingConfirm.this.setResult(R.string.meeting_created);
     	CreateMeetingConfirm.this.finish();
     }
     
     public void clearData(){
 
     	// delete data from shared pref
     	SharedPreferences settings = getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE); 
     	SharedPreferences.Editor editor = settings.edit();
     	editor.remove("mtitle");
     	editor.remove("mdesc");
     	editor.remove("mdatem");
     	editor.remove("mdated");
     	editor.remove("mdatey");
     	editor.remove("mstarth");
     	editor.remove("mstartm");
     	editor.remove("mendh");
     	editor.remove("mendm");
     	editor.remove("mtracktime");
     	editor.remove("maddr");
     	editor.remove("mlat");
     	editor.remove("mlon");
     	editor.remove("mnames");
     	editor.remove("mphones");
     	editor.commit();
     }
     private static String pad(int c) {
         if (c >= 10)
             return String.valueOf(c);
         else
             return "0" + String.valueOf(c);
     }
     
 
 
 	 // menu 
 	    @Override
 	    public boolean onCreateOptionsMenu(Menu menu) {
 	        MenuInflater inflater = getMenuInflater();
 	        inflater.inflate(R.menu.logoutonly, menu);
 	        return true;
 	    }
 	    
 	    @Override
 	    public boolean onOptionsItemSelected(MenuItem item) {
 	        switch (item.getItemId()) {
 	            case R.id.logout:{
 	            	logout();
 	            	break;
 	            }
 	        }
 	        return true;
 	    }
 	    
 	    private void logout(){
           this.setResult(R.string.logout);
           this.finish();
 	    }
 }
