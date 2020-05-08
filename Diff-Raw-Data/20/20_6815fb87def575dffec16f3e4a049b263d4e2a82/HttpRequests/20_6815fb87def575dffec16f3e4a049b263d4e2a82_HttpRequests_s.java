 package com.example.friendzyapp;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 
 import org.osmdroid.util.GeoPoint;
 
 import android.util.Log;
 
 /*
  * A non-static container for all the request objects.
  * 
  * Refactored out here so initServer can access them in a non-android environment
  * for Robotium testing.
  */
 public class HttpRequests {
     private static final String TAG = "HttpRequests";
 
     public static class LoginRequest {
         public LoginRequest(String _userID, List<String> _facebookFriends, String ri, String pn) {
             userID = _userID;
             facebookFriends = _facebookFriends;
             this.regId = ri;
             this.phone_number = pn;
         }
 
         public String userID;
         public List<String> facebookFriends;
         public String regId;
         public String phone_number;
     }
 
     public static class PostStatusRequest {
         public String userID;
         public String status;
         public String is_public;
 
         public PostStatusRequest(String userID, String status) {
             this.userID = userID;
             this.status = status;
             this.is_public = "true";
         }
     }
 
     public static class AcceptMatchRequest {
         public String userID;
         public String friendID;
         public Map<String, String> userLocation;
 
         public AcceptMatchRequest(String userID, String friendID, Map<String, String> userLocation) {
             this.userID = userID;
             this.friendID = friendID;
             this.userLocation = userLocation;
         }
     }
 
     public static class PostMsgRequest {
         public String userID;
         public String friendID;
         public String msg;
         public ServerMeetupLocation meetup_location; // make an object for this!!
 
         public PostMsgRequest(String userID, String friendID, String msg, ServerMeetupLocation meetup_location) {
             Log.d(TAG, "I'm user :" + userID);
             this.userID = userID;
             this.friendID = friendID;
             this.msg = msg;
             this.meetup_location = meetup_location;
         }
     }
     
     
     /*
      * To be jsoned and sent to server only
      */
     public static class ServerMeetupLocation {
        public String meeting_name;
    	public newLocation meeting_location;
     	
    	public ServerMeetupLocation(String meeting_name, newLocation meeting_location) {
    		this.meeting_name = meeting_name;
    		this.meeting_location = meeting_location;
     	}
     	
     	public ServerMeetupLocation() {
     		//emtpy one; don't update the sever
     	}
 
     }
     
     public static class newLocation {
     	public String latitude;
     	public String longitude;
     	
     	public newLocation(String latitude, String longitude) {
     		this.latitude = latitude;
     		this.longitude = longitude;
     	}
     	
     }
     
     /*
      * To be used in receiving only
      */
     public static class MeetupLocation implements Serializable {
         private static final long serialVersionUID = 1L;
         public String meetingName;
     	public GeoPoint meetingLocation;
     	
     	public MeetupLocation(String meetingName, GeoPoint meetingLocation) {
     	    Log.d(TAG, "Someone is trying to instantiate a MeetupLocation!");
     		this.meetingName = meetingName;
     		this.meetingLocation = meetingLocation;
     	}
     	
     	public MeetupLocation() {
     		// empty one used for if location is not set.
     	}
     	
     }
     
     public static class SubUpdateRequest {
         public String userID;
         public String type;  // available types: "add", "delete"
         public String subscribe_topic;
         public List<String> subscribe_to;
 
         public SubUpdateRequest(String userID, String type, String subscribe_topic, List<String> subscribe_to) {
         	 Log.wtf(TAG, "I'm user :" + userID);
             this.userID = userID;
             this.type = type;
             this.subscribe_topic = subscribe_topic;
             this.subscribe_to = subscribe_to;
         }
     }
     
     public static class SetSMSRequest {
         public String userID;
         public Boolean sms;
 
         public SetSMSRequest(String userID, Boolean sms) {
             this.userID = userID;
             this.sms = sms;
         }
     }
     
     public static class GetEventsRequest {
         public String userID;
         public Map<String, String> userLocation;
 
         public GetEventsRequest(String userID, Map<String, String> userLocation) {
             this.userID = userID;
             this.userLocation = userLocation;
         }
     }
 
 }
