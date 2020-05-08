 package com.madp.meetme;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.madp.meetme.common.entities.Meeting;
 import com.madp.meetme.webapi.WebService;
 import com.madp.utils.Logger;
 import com.madp.utils.ParticipantsAdapter;
 import com.madp.utils.SerializerHelper;
 
 /**
  * Activity for displaying Meeting information
  * 
  * @author Niklas and Peter initial version
  * @author Saulius 2012-02-19 integrated WebService, added TODO elements, remove obsolete stuff
  * 
  */
 public class MeetingInfoActivity extends ListActivity {
 	private final String TAG = "MeetingInfoActivity";
 	private ParticipantsAdapter p_adapter;
 	private Context c;
 	private Meeting meeting;
 	private WebService ws;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.meetinginfo);
 
 		/* Create important stuff */
 		ws = new WebService(new Logger());
 		TextView infolabel = (TextView) findViewById(R.id.infolabel);
 		TextView timelabel = (TextView) findViewById(R.id.meetingtime);
 		TextView datelabel = (TextView) findViewById(R.id.meetingdate);
 		TextView sessionstartlabel = (TextView) findViewById(R.id.alarmtime);
 		TextView locationlabel = (TextView) findViewById(R.id.locationview);
 		ImageButton cancelMeeting = (ImageButton) findViewById(R.id.cancelmeeting);
 		ImageButton updateAndExit = (ImageButton) findViewById(R.id.update);
 		ImageButton mapbutton	= (ImageButton) findViewById(R.id.iblocation);
 		byte [] a=null;
 		
 		Bundle extras = getIntent().getExtras();
 		if(extras != null)
 			 a= extras.getByteArray("meeting");
 		else
 			Log.e("extras","no bundle in the intent");
 		if(a == null)
 			Log.e("getByteArray", "no extras tagged as meeting");
 		else{
 			meeting= (Meeting) SerializerHelper.deserializeObject(a);
 					
 			this.p_adapter = new ParticipantsAdapter(this, R.layout.meetingrow, meeting.getParticipants());
 			getListView().setAdapter(p_adapter);
 			Log.i("MeetingInfoActivity, on Create", "lat = "+meeting.getLatitude()+"lot = "+meeting.getLongitude());
 			
 			/* Set meeting info */
 			infolabel.setText(meeting.getTitle());
 			timelabel.setText(meeting.gettStarting());
 			datelabel.setText(meeting.gettStarting()); // TODO: deal with this, date and time is one thing...
 			locationlabel.setText(meeting.getAddress());
 			sessionstartlabel.setText(meeting.gettStarting()); // TODO: fix this
 			
 		}
 		mapbutton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
				Intent myintent = new Intent(v.getContext(),com.madp.meetme.GPSFindLocationOnMap.class);
 				
 				Bundle b = new Bundle();
 				b.putByteArray("meeting", SerializerHelper.serializeObject(meeting));
 				myintent.putExtras(b);
 				startActivity(myintent);
 			}
 		});
 		
 		
 		
 		cancelMeeting.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 				builder.setMessage("Are you sure you want to delete this meeting?").setCancelable(false)
 						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								/* Update email list on server to exclude this user */
 								// /TODO: to be implemented in WebService
 								finish();
 							}
 						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}
 		});
 
 		updateAndExit.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				int i = 0;
 				// TODO: to be implemented in WebService
 				finish();
 			}
 		});
 
 	}
 
 	// /TODO: should be fixed to support two arguments: starting date time (e.g '2012-02-02 11:50') and number of
 	// minutes to start alarm before the meeting.
 	public static String calculateAlarmTime(String meetingTime) {
 
 		char[] time = meetingTime.toCharArray();
 		String hour = time[0] + "" + time[1];
 		String min = time[3] + "" + time[4];
 
 		int timeHour = Integer.valueOf(hour);
 		int timeMin = Integer.valueOf(min);
 		if (timeMin >= 15) {
 			if (timeHour == 0) {
 				if ((timeMin - 15) == 0) {
 					return "00" + ":" + (timeMin - 15) + "0";
 				}
 				return "00" + ":" + (timeMin - 15);
 			}
 
 			if ((timeMin - 15) == 0) {
 				return timeHour + ":" + (timeMin - 15) + "0";
 			}
 			return timeHour + ":" + (timeMin - 15);
 		} else {
 			if ((timeHour - 1) == 0) {
 				return (timeHour - 1) + "0" + ":" + (timeMin + 45);
 			} else if (timeHour == 0) {
 				return "23:" + (timeMin + 45);
 			}
 			return (timeHour - 1) + ":" + (timeMin + 45);
 		}
 	}
 
 	private void refreshMeetingInfo(int id) {
 		Meeting meetingTmp = ws.getMeeting(id);
 		if (meetingTmp != null) {
 			meeting = meetingTmp;
 		}
 	}
 
 	/* Implement listers for buttons and a Item in the list */
 
 	/* Click on a listitem */
 
 }
