 package assemblers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import models.Attendee;
 import models.Meeting;
 import models.User;
 import results.RenderCustomJson;
 import utils.GsonFactory;
 import DTO.MeetingDTO;
 
 import com.google.gson.*;
 
 /**
  * Assembler for the MeetingDTO and Meeting classes.
  * 
  * @see MeetingDTO
  * @see Meeting
  * @author Alex Jarvis axj7@aber.ac.uk
  */
 public class MeetingAssembler {
 	
 	/**
 	 * Writes a MeetingDTO from a Meeting object.
 	 * @param meeting
 	 * @return
 	 */
 	public static MeetingDTO writeDTO(Meeting meeting) {
 		MeetingDTO meetingDTO = new MeetingDTO();
 		meetingDTO.id = meeting.id;
 		meetingDTO.time = new DateTime(meeting.time);
 		meetingDTO.place = CoordinateAssembler.writeDTO(meeting.place);
 		meetingDTO.attendees = AttendeeAssembler.writeDTOs(meeting.attendees);
 		meetingDTO.owner = UserSummaryAssembler.writeDTO(meeting.owner);
 		meetingDTO.title = meeting.title;
 		meetingDTO.description = meeting.description;
 		
 		return meetingDTO;
 	}
 	
 	/**
 	 * Writes a List of MeetingDTOs from the meetings that a User is related to 
 	 * (where they have an Attendee object).
 	 * @param user
 	 * @return
 	 */
 	public static List<MeetingDTO> writeDTOs(User user) {
 		ArrayList<MeetingDTO> meetings = new ArrayList<MeetingDTO>();
 		for (Attendee attendee : user.meetingsRelated) {
 			meetings.add(writeDTO(attendee.meeting));
 		}
 		return meetings;
 	}
 	
 	/**
 	 * Creates a Meeting for the specified User and returns a MeetingDTO that
 	 * represents this newly created Meeting.
 	 * 
 	 * Also creates the attendees for the meeting based on the data inside the
 	 * MeetingDTO.
 	 * @param meetingDTO
 	 * @param user
 	 * @return
 	 */
 	public static MeetingDTO createMeeting(MeetingDTO meetingDTO, User user) {
 		Meeting meeting = new Meeting();
 		meeting.time = meetingDTO.time.toDate();
 		if (meetingDTO.place != null) {
 			meeting.place = CoordinateAssembler.createCoordinate(meetingDTO.place);
 		}
 		meeting.owner = user;
 		meeting.title = meetingDTO.title;
 		meeting.description = meetingDTO.description;
 		
 		// The Attendee is the owning side of the relationship, so the meeting must be saved first.
 		meeting.create();
 		if (meetingDTO.attendees != null) {
 			AttendeeAssembler.createAttendees(meetingDTO.attendees, meeting);
 		}
 		
 		return writeDTO(meeting);
 	}
 	
 	/**
 	 * Updates a Meeting, using the data from a MeetingDTO
 	 * @param meetingDTO
 	 * @return
 	 */
 	public static MeetingDTO updateMeeting(MeetingDTO meetingDTO) {
 		Meeting meeting = Meeting.findById(meetingDTO.id);
 		if (meeting != null) {
 			meeting.time = meetingDTO.time.toDate();
 			
 			
			CoordinateAssembler.updateCoordinate(meetingDTO.place);
 			meeting.title = meetingDTO.title;
 			meeting.description = meetingDTO.description;
 			AttendeeAssembler.updateAttendees(meeting, meetingDTO.attendees);
 			
 			return writeDTO(meeting);
 		}
 		return null;
 	}
 	
 	/**
 	 * Updates the meeting with a JsonObject
 	 * @param jsonObject
 	 * @return
 	 */
 	public static MeetingDTO updateMeetingWithJsonObject(JsonObject jsonObject) {
 		MeetingDTO meetingDTO = meetingDTOWithJsonObject(jsonObject);
 		meetingDTO = updateMeeting(meetingDTO);
 		return meetingDTO;
 	}
 	
 	/**
 	 * Returns a MeetingDTO object from a JsonObject
 	 * @param jsonObject
 	 * @return
 	 */
 	public static MeetingDTO meetingDTOWithJsonObject(JsonObject jsonObject) {
 		GsonBuilder gsonBuilder = GsonFactory.gsonBuilder();
 		MeetingDTO meetingDTO = gsonBuilder.create().fromJson(jsonObject, MeetingDTO.class);
 		return meetingDTO;
 	}
 	
 	/**
 	 * Returns a MeetingDTO object from a Json string
 	 * @param jsonString
 	 * @return
 	 */
 	public static MeetingDTO meetingDTOWithJsonString(String jsonString) {
 		GsonBuilder gsonBuilder = GsonFactory.gsonBuilder();
 		MeetingDTO meetingDTO = gsonBuilder.create().fromJson(jsonString, MeetingDTO.class);
 		return meetingDTO;
 	}
 
 }
