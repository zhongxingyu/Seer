 package se.diversify.webroti.data;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * A Repository of meetings
  */
 public class Repository {
 
     private Repository(){
         // Singleton - don't instantiate me please
     }
 
    private static Map<String, Meeting> meetings = new HashMap<String, Meeting>();
 
     /**
      * Create a new meeting
      * @return a new meeting
      */
     public static Meeting createMeeting() {
         Meeting meeting = new Meeting();
         meetings.put(meeting.getId(), meeting);
         return meeting;
     }
 
     /**
      * Get a meeting given an id, or throws a IllegalArgumentException if the meeting was not found
      * @param id the id of the searched meeting
      * @return the meeting with the given id
      */
     public static Meeting getMeeting(String id) {
         if (!meetings.containsKey(id)) {
             throw new IllegalArgumentException("Meeting with id " + id + " can not be found");
         }
         return meetings.get(id);
     }
 }
