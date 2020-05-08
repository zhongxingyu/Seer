 package com.uiproject.meetingplanner;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.ExpandableListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.LinearLayout;
 import android.widget.SimpleExpandableListAdapter;
 import android.widget.TextView;
 
 import com.uiproject.meetingplanner.database.MeetingPlannerDatabaseHelper;
 import com.uiproject.meetingplanner.database.MeetingPlannerDatabaseManager;
 
 public class MeetingListAccepted extends ExpandableListActivity
 {
     private static final String LOG_TAG = "MeetingListAccepted";
     private MeetingPlannerDatabaseManager db;
     public ArrayList<MeetingInstance> allMeet;
     private String[] groups;
 	private String[][] children;
     public static final String PREFERENCE_FILENAME = "MeetAppPrefs";
     private int uid;
 
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle icicle)
     {
         super.onCreate(icicle);
         setContentView(R.layout.expandable_meeting_list);
         
         //Hook up the database
         db = new MeetingPlannerDatabaseManager(this, MeetingPlannerDatabaseHelper.DATABASE_VERSION);
 	    db.open();
 
 	    //PLEASE LEAVE THIS PART UNCOMMENTED TILL THE DATABASE HAS ENTRIES
 	    /*db.createUser(1, "Laura", "Rodriguez", "lau.rodriguez@gmail", "3128573352", 37, -34);
 	    db.createUser(2, "Dummy", "Joe", "tt@gmail.com", "1234567778", 32, 34);
 	    db.createMeeting(2,"Drinking party", 32, -35, "Happy Hour Drinks", "RTCC 202", "10/31/2011", "6:30pm", "9:00pm", 5, 5, 5);//TODO
 	    db.createMeeting(5,"Drinking party", 32, -35, "Happy Hour Drinks", "RTCC 202", "10/31/2011", "6:30pm", "9:00pm", 5, 5, 5);//TODO
 	    db.createMeeting(6,"Drinking party", 32, -35, "Happy Hour Drinks", "RTCC 202", "10/31/2011", "6:30pm", "9:00pm", 5, 5, 5);//TODO
 	    db.createMeeting(7,"Drinking party", 32, -35, "Happy Hour Drinks", "RTCC 202", "10/31/2011", "6:30pm", "9:00pm", 5, 5, 5);//TODO
 	    db.createMeetingUser(2, 1, 2, "Hello");
 	    db.createMeetingUser(2, 2, 1, "Hello2");*/
 	    
 	    SharedPreferences settings = getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE); 
     	uid = settings.getInt("uid", -1);
     	Log.v(LOG_TAG, "uid = " + uid);
     	allMeet = db.getAcceptedMeetings(uid);
         // Set up our adapter
     	//allMeet = db.getAllMeetings();
     	db.close();
     	
     	groups = new String[allMeet.size()];
     	children = new String[allMeet.size()][2];
     	
     	//Log.v(TAG, "Groups is " + groups.length);
     	//Log.v(TAG, "Children is " + children.length);
     	
     	for (int i = 0; i < allMeet.size(); i++)
     	{
     		//Log.v(TAG, "Element number " + i + " is " + allMeetings.get(i).getMeetingSubject());
     		groups[i] = allMeet.get(i).getMeetingTitle() + "\n";
     		//groups[i] = allMeet.get(i).getMeetingTitle() + "\n\n" + "Fixme" + "\t\t" + allMeet.get(i).getMeetingDate() + "\t" + allMeet.get(i).getMeetingStartTime();
     		children[i][0] = "Meeting ID: " + allMeet.get(i).getMeetingID() + "\n" + allMeet.get(i).getMeetingDescription() + "\n" + allMeet.get(i).getMeetingAddress() + "\n";
     		children[i][1] =  allMeet.get(i).getMeetingDate() + "\n" + allMeet.get(i).getMeetingStartTime() + " to " + allMeet.get(i).getMeetingEndTime();
      	}
     	
 		SimpleExpandableListAdapter expListAdapter =
 			new SimpleExpandableListAdapter(
 				this,
 				createGroupList(),	// groupData describes the first-level entries
 				R.layout.group_row,	// Layout for the first-level entries
 				new String[] { "colorName" },	// Key in the groupData maps to display
 				new int[] { R.id.childname },		// Data under "colorName" key goes into this TextView
 				createChildList(),	// childData describes second-level entries
 				R.layout.child_row,	// Layout for second-level entries
 				new String[] { "shadeName", "rgb" },	// Keys in childData maps to display
 				new int[] { R.id.childname, R.id.rgb }	// Data under the keys above go into these TextViews
 			){@Override
             public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                 final View v = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
 
                 MeetingInstance m = allMeet.get(childPosition);
                Button edit = (Button) findViewById(R.id.editBtn);
         		int creator = m.getMeetingInitiatorID();
         		if (creator != uid){
         			edit.setVisibility(View.GONE);
         		}
         		
        		Button track = (Button) findViewById(R.id.trackBtn);
         		
         		int currenth = Calendar.HOUR_OF_DAY;
         		int currentm = Calendar.MINUTE;
         		String start = m.getMeetingStartTime();
        		int starth = Integer.parseInt(start.substring(0, start.indexOf(',')));
        		int startm = Integer.parseInt(start.substring(start.indexOf(',') + 1));
         		int tracktime = m.getMeetingTrackTime();
         		int minutes_before = ((currenth - starth) * 60) + (currentm - startm);
         		if (minutes_before > tracktime){
         			track.setVisibility(View.GONE);
         		}
         		
                 return v;
             }
 		};
 		setListAdapter( expListAdapter );
     }
 
     public void  onContentChanged  () {
         super.onContentChanged();
         Log.d( LOG_TAG, "onContentChanged" );
     }
 
     public boolean onChildClick(
             ExpandableListView parent, 
             View v, 
             int groupPosition,
             int childPosition,
             long id) {
         Log.d( LOG_TAG, "onChildClick: "+childPosition );
 		
         //if( btn != null )
             
         return false;
     }
 
     public void  onGroupExpand  (int groupPosition) {
         Log.d( LOG_TAG,"onGroupExpand: "+groupPosition );
     }
 
     public void editButtonClicked(View view)
     {
         //get the row the clicked button is in
         LinearLayout vwParentRow = (LinearLayout)view.getParent();
          
         TextView child = (TextView)vwParentRow.getChildAt(0);
         Button btnChild = (Button)vwParentRow.getChildAt(2);
         String meeting_info = child.getText().toString().split(": ")[1];
         int meetingID = Integer.parseInt(meeting_info.split("\\n")[0]);
         
         //Toast.makeText(getApplicationContext(), "You clicked me! " + meetingID, Toast.LENGTH_SHORT).show();
       
         vwParentRow.refreshDrawableState();  
 
 		Intent intent = new Intent(MeetingListAccepted.this, EditMeeting.class);
 		intent.putExtra("mid", meetingID);
 		startActivity(intent);
     }
 /**
   * Creates the group list out of the colors[] array according to
   * the structure required by SimpleExpandableListAdapter. The resulting
   * List contains Maps. Each Map contains one entry with key "colorName" and
   * value of an entry in the colors[] array.
   */
     private List createGroupList() {
   	  ArrayList result = new ArrayList();
   	  for( int i = 0 ; i < groups.length ; ++i ) {
   		HashMap m = new HashMap();
   	    m.put( "colorName",groups[i] );
   		result.add( m );
   	  }
   	  return (List)result;
       }
 
 /**
   * Creates the child list out of the shades[] array according to the
   * structure required by SimpleExpandableListAdapter. The resulting List
   * contains one list for each group. Each such second-level group contains
   * Maps. Each such Map contains two keys: "shadeName" is the name of the
   * shade and "rgb" is the RGB value for the shade.
   */
 	private List createChildList() {
 		ArrayList result = new ArrayList();
 		for( int i = 0 ; i < children.length ; ++i ) {
 			// Second-level lists
 			ArrayList secList = new ArrayList();
 			for( int n = 0 ; n < children[i].length ; n += 2 ) {
 				HashMap child = new HashMap();
 				child.put( "shadeName", children[i][n] );
 				child.put( "rgb", children[i][n+1] );
 				secList.add( child );
 			}
 			result.add( secList );
 		}
 		return result;
 	}
 
 }
