 package com.uiproject.meetingplanner.database;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.json.JSONException;
 
 import android.util.Log;
 
 import com.uiproject.meetingplanner.Communicator;
 import com.uiproject.meetingplanner.MeetingInstance;
 import com.uiproject.meetingplanner.UserInstance;
 
 public class MeetingPlannerDatabaseUtility {
 
 	public static final String dbUtilityTag = "MeetingPlannerDatabaseUtility";
 	public MeetingPlannerDatabaseUtility(){
 		
 	}
 	
 	public static void updateDatabase(MeetingPlannerDatabaseManager db) throws JSONException, ParseException{
 		// Get meetings info & user infos from server
     	HashMap<Integer, UserInstance> usersMap = (HashMap<Integer, UserInstance>) Communicator.getAllUsers();
     	HashMap<Integer, MeetingInstance> meetingsMap = (HashMap<Integer, MeetingInstance>) Communicator.getAllMeetings();
     	
     	// 1. Delete all data in db
     	db.deleteAllUsers();
     	db.deleteAllMeetings();
     	db.deleteAllMeetingUsers();
     	
     	// 2-1. Update Users
     	for (UserInstance userObj : usersMap.values()) {
     		db.createUser(userObj.getUserID(), userObj.getUserFirstName(), userObj.getUserLastName(),
     				userObj.getUserEmail(), userObj.getUserPhone(), userObj.getUserLocationLon(), userObj.getUserLocationLat());
     	}
 
     	Log.d(dbUtilityTag, " meeting size = " + meetingsMap.size());
     	// 2-2. Update Meetings
     	for (MeetingInstance meetingObj : meetingsMap.values()) {
     		
     		// convert start time into unix timestamp
     		String mstartdatetime = meetingObj.getMeetingDate() + " " + meetingObj.getMeetingStartTime();
     		Log.d(dbUtilityTag, " mstartdatetime = " + mstartdatetime);
     		
    		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
     		Date date = (Date)formatter.parse(mstartdatetime); 
     		int timestampInt = (int) (date.getTime() / 1000L);
     		Log.d(dbUtilityTag, " create meeting: meetingID = " + meetingObj.getMeetingID() + ", timestamp = " + timestampInt);
     		
     		db.createMeeting(meetingObj.getMeetingID(), meetingObj.getMeetingTitle(), meetingObj.getMeetingLat(), meetingObj.getMeetingLon(),
     						meetingObj.getMeetingDescription(), meetingObj.getMeetingAddress(), meetingObj.getMeetingDate(), 
     						meetingObj.getMeetingStartTime(), meetingObj.getMeetingEndTime(), meetingObj.getMeetingTrackTime(), 
     						meetingObj.getMeetingInitiatorID(), timestampInt);
     	
     		// 2-3. Update Meeting Users
     		HashMap<Integer, UserInstance> meetingUsers = meetingObj.getMeetingAttendees();
     		
     		for(UserInstance meetingUserObj : meetingUsers.values()){
     			
     			int attendingStatusID = MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_PENDING;
     			
     			if(meetingUserObj.getUserAttendingStatus().compareTo(MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_ATTENDINGSTRING) == 0){
     				attendingStatusID = MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_ATTENDING;
     			}else if(meetingUserObj.getUserAttendingStatus().compareTo(MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_DECLININGSTRING) == 0){
     				attendingStatusID = MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_DECLINING;
     			}
     			
     			db.createMeetingUser(meetingObj.getMeetingID(), meetingUserObj.getUserID(), attendingStatusID, "0");
     		}
     	}
 	}
 }
