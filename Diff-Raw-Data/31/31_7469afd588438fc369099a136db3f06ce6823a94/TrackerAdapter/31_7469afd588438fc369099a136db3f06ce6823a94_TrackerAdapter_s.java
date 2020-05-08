 package com.uiproject.meetingplanner;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class TrackerAdapter extends BaseAdapter {
 	private Context context;
     private List<UserInstance> attendees;
     private List<UserInstance> meetingUsers;
     private MeetingInstance meetingInfo;
    private int etaHour;
    private int etaMinute;
     private Calendar cal;
     
 	public TrackerAdapter(Context context, List<UserInstance> attendees, List<UserInstance> meetingUsers, MeetingInstance meetingInfo) {
 		super();
 		this.context = context;
 		this.attendees = attendees;
 		this.meetingUsers = meetingUsers;
 		this.meetingInfo = meetingInfo;
		etaHour = 0;
		etaMinute = 0;
 		cal = new GregorianCalendar();
 	}
 
 	
 	public int getCount() {
 		return attendees.size();
 	}
 
 	public Object getItem(int position) {
 		return attendees.get(position);
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		UserInstance entry = attendees.get(position);
         if (convertView == null) {
             LayoutInflater inflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = inflater.inflate(R.layout.tracker_list_item, null);
         }
         
 
         
         TextView tvTrackerName = (TextView) convertView.findViewById(R.id.trackername);
         for (int i = 0; i < meetingUsers.size(); i++) {
         	if (entry.getUserID() == meetingUsers.get(i).getUserID()) {
         		tvTrackerName.setText(meetingUsers.get(i).getUserFirstName() + " " + meetingUsers.get(i).getUserLastName());
         	}
         }
         
         // ETA parser
         boolean minutesSet = false;
         String[] eta = entry.getUserEta().split("\\s+");
         for (int i = 0; i < eta.length; i++) {
         	if (isInteger(eta[i]) && !minutesSet) {
         		etaMinute = Integer.parseInt(eta[i]);
         		minutesSet = true;
         	}
         	else if (isInteger(eta[i]) && minutesSet) {
         		etaHour = etaMinute;
         		etaMinute = Integer.parseInt(eta[i]);
         	}
         }
        
         // Meeting time
         int meetingHour = Integer.parseInt(meetingInfo.getMeetingStartTime().substring(0, meetingInfo.getMeetingStartTime().indexOf(':')));
         int meetingMinute = Integer.parseInt(meetingInfo.getMeetingStartTime().substring(meetingInfo.getMeetingStartTime().indexOf(':') + 1));
         
         // Current time
         int currentHour = cal.get(Calendar.HOUR_OF_DAY);
         int currentMinute = cal.get(Calendar.MINUTE);
         
         // Time the user arrives
         int arrivalHour = currentHour + etaHour + (int)((currentMinute + etaMinute)/60);
         int arrivalMinute = (currentMinute + etaMinute) % 60;
           
         TextView tvEta = (TextView) convertView.findViewById(R.id.eta);
         tvEta.setText(entry.getUserEta());
         
         int difference = ((meetingHour - arrivalHour) * 60) + (meetingMinute - arrivalMinute);
         
         if (difference >= 0) {
        	tvEta.setTextColor(Color.GREEN);
         }
         else if (difference >= -20) {
        	tvEta.setTextColor(R.color.orange);
         }
         else {
         	tvEta.setTextColor(Color.RED);
         }
         
         return convertView;
 	}
 	
 	public boolean isInteger(String input) {  
 	   try {  
 	      Integer.parseInt(input);  
 	      return true;  
 	   }  
 	   catch( Exception e) {  
 	      return false;  
 	   }  
 	}
 }
