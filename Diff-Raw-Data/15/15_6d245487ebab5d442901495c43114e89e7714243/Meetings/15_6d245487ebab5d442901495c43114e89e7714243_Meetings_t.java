 package controllers;
 
 import java.util.List;
 
 import models.Attendee;
 import models.Meeting;
 import models.User;
 import models.helpers.MeetingHelper;
 import play.mvc.With;
 import DTO.MeetingDTO;
 import assemblers.MeetingAssembler;
 
 import com.google.gson.JsonObject;
 
 import controllers.oauth2.AccessTokenFilter;
 
 /**
  * The Meetings controller is used to control all aspects of a meeting.
  * 
  * @author Alex Jarvis axj7@aber.ac.uk
  */
 @With({JSONRequestTypeFilter.class, NoCookieFilter.class, LoggingFilter.class, SSLCheckFilter.class})
 public class Meetings extends AccessTokenFilter {
 	
 	/**
 	 * 
 	 */
     public static void index() {
     	User authUser = userAuth.getAuthorisedUser();
     	List<MeetingDTO> meetings = MeetingAssembler.writeDTOs(authUser);
         renderJSON(meetings);
     }
     
     /**
      * 
      * @param body
      */
     public static void create(JsonObject body) {
     	
     	if (body != null && body.isJsonObject()) {
     		
 	    	MeetingDTO meetingDTO = MeetingAssembler.meetingDTOWithJsonObject(body);
 	    	
 	    	if (meetingDTO != null) {
 		    	MeetingDTO newMeetingDTO = MeetingAssembler.createMeeting(meetingDTO, userAuth.getAuthorisedUser());
 		    	response.status = 201;
 				renderJSON(newMeetingDTO);
 	    	}
 	    }
     	badRequest();
     }
     
     /**
      * 
      * @param id
      */
     public static void show(Long id) {
     	
    	Meeting meeting = Meeting.findById(id);
    	if (meeting != null) {
	    	// Check the authorised user is able to view the meeting
	    	User authUser = userAuth.getAuthorisedUser();
	    	for (Attendee attendee : meeting.attendees) {
	    		if (attendee.user.id.equals(authUser.id)) {
	    			renderJSON(MeetingAssembler.writeDTO(meeting));
	    		}
	    	}
     	}
     	
     	notFound();
     }
     
     /**
      * 
      * @param body
      * @param id
      */
     public static void update(Long id, JsonObject body) {
     	
     	if (body != null && body.isJsonObject()) {
     		Meeting meeting = Meeting.findById(id);
     		
     		// Check meeting exists and that the id in the JSON matches the id in the URL
     		if (meeting != null && body.get("id") != null && new Long(body.get("id").getAsLong()).equals(id)) {
     			
     			// Check the authorised user is the owner of the meeting
     			if (meeting.owner.equals(userAuth.getAuthorisedUser())) {
     				MeetingDTO newMeetingDTO = MeetingAssembler.updateMeetingWithJsonObject(body);
     	    		renderJSON(newMeetingDTO);
     			} else {
     				badRequest();
     			}
     		   
     		} else {
     			notFound();
     		}
 	    }
     	badRequest();
     }
     
     /**
      * 
      * @param id
      */
     public static void delete(Long id) {
     	
     	Meeting meeting = Meeting.findById(id);
 		
 		if (meeting != null) {
 			// Check meeting exists and that the Authorised user is the owner of the meeting.
 			if (meeting.owner.equals(userAuth.getAuthorisedUser())) {
 				meeting.delete();
 				ok();
 			} else {
 				badRequest();
 			}
 		} else {
 			notFound();
 		}
     }
     
     /**
      * 
      * @param id
      */
     public static void acceptMeetingRequest(Long id) {
     	Meeting meeting = Meeting.findById(id);
     	if (meeting != null) {
     		User authUser = userAuth.getAuthorisedUser();
     		if (MeetingHelper.acceptMeetingRequest(meeting, authUser)) {
     			ok();
     		} else {
     			badRequest();
     		}
     	}
     	notFound();
     }
     
     /**
      * 
      * @param id
      */
     public static void declineMeetingRequest(Long id) {
     	Meeting meeting = Meeting.findById(id);
     	if (meeting != null) {
     		User authUser = userAuth.getAuthorisedUser();
     		if (MeetingHelper.declineMeetingRequest(meeting, authUser)) {
     			ok();
     		} else {
     			badRequest();
     		}
     	}
     	notFound();
     }
 
 }
