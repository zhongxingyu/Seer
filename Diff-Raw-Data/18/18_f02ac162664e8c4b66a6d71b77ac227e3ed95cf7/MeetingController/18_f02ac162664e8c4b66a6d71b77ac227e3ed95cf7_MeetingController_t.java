 package friendzone.elec3609.controller;
 
 import java.util.ArrayList;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import friendzone.elec3609.service.DatabaseHandler;
 import friendzone.elec3609.service.MeetingsMaker;
 import friendzone.elec3609.model.*;
 
 @Controller
 @SessionAttributes({"student", "dbHandler"})
 public class MeetingController {
 
 	final static DatabaseHandler dbHandler = DatabaseHandler.getInstance();
 	
 	//dummy data
 	//Student student = dbHandler.getStudent("123456789");
 //	UnitOfStudy uos =  new UnitOfStudy("ELEC3609", "Internet Software Platforms", 500);
 //	Project project = new Project(uos.getUnitCode(),  "aProject",new java.sql.Date(8099,10,15));
 //	Team team = new Team(1234, project.getID(), "FriendZone");
	Team team = dbHandler.getTeam(1);
 	//Meeting meeting = dbHandler.getMeetings("371830314").get(0);
 	//Student student = dbHandler.getStudent("325516396");
 	Student student = dbHandler.getStudent(team.getMembers().get(0).getSID());
 	
 	@RequestMapping("/meeting")
 	public String getEnums(Map<String, Object> map){
 
 
 		//dbHandler.addMeeting(1, );
 		//dbHandler.addMeeting(1, new java.sql.Timestamp(2013, 12, 10, 3, 50, 20, 1), new java.sql.Timestamp(2013, 12, 10, 4, 20, 20, 1));
 //		INSERT INTO meeting VALUES (1,1, timestamp '2013-09-27 23:00:00', timestamp '2013-09-27 23:30:00', 'School OF IT')
 //		map.put("studentTeams",student.getTeams());
 //		map.put("meetingAttendees", meeting.getAttendees());
 		//map.put("teamMembers", team.getMembers());
 //		map.put("meetingLocation", meeting.getLocation());
 		//map.put("team", dbHandler.getStudent(team.getMembers().get(0).getSID()).getTeams());
 		map.put("teamID", team.getID());
 		map.put("teamName", team.getName());
 		//map.put("meetingLocation", dbHandler.getMeetings(team.getMembers().get(0).getSID()));
 		//map.put("meetingLocation", dbHandler.getMeetings("371830314").get(0).getLocation());
 		//map.put("team", dbHandler.getMeetings("371830314").get(0).getAttendees());
 		map.put("meetings", student.getMeetings());
 		
 		return "meeting";
 	}
 	
 	@RequestMapping("/meeting/new")
 	public String newMeeting(Map<String, Object> map){
 		
 		//Get an arrayList<String> Of team names
 		List<Team> studentTeams = student.getTeams();
 		ArrayList<String> teamNames = new ArrayList<String>();
 		for(Team t: studentTeams)
 		{
 			teamNames.add(t.getName());
 		}
 		
 		System.out.println("StudentAvailability:");
 		System.out.println(student.getAvailability());
 		
 		boolean[][] avail = student.getAvailability();
 		List<String> availStrings = new ArrayList<String>();
 		for(int i=0; i<7; i++){
 			for(int j=0; j<12; j++){
 				availStrings.add(String.valueOf(avail[i][j]));
 			}
 		}
 		System.out.println("StudentAvailability:");
 		System.out.println(availStrings);
 		
 		
 		
 		MeetingsMaker mm = new MeetingsMaker();
 		int[][] ca = mm.countAvailability(team);
 //		System.out.println("LOOK HERE");
 //		System.out.println("LOOK HERE");
 //		System.out.println("LOOK HERE");
 		System.out.println("LOOK HERE ca:");
 		System.out.println(ca);
 		//If this works ^ YAY IT DOES
 		
 		//Converts the given 2d Boolean array into strings
 		//Corresponding to their respective days
 		ArrayList<String> availMondayStrings = new ArrayList<String>();
 		ArrayList<String> availTuesdayStrings = new ArrayList<String>();
 		ArrayList<String> availWednesdayStrings = new ArrayList<String>();
 		ArrayList<String> availThursdayStrings = new ArrayList<String>();
 		ArrayList<String> availFridayStrings = new ArrayList<String>();
 		ArrayList<String> availSaturdayStrings = new ArrayList<String>();
 		ArrayList<String> availSundayStrings = new ArrayList<String>();
 		
 		for(int j=0;j<12; j++)
 		{
 			//availMondayStrings.add(String.)
 			availMondayStrings.add(String.valueOf(ca[0][j]));
 			availTuesdayStrings.add(String.valueOf(ca[1][j]));
 			availWednesdayStrings.add(String.valueOf(ca[2][j]));
 			availThursdayStrings.add(String.valueOf(ca[3][j]));
 			availFridayStrings.add(String.valueOf(ca[4][j]));
 			availSaturdayStrings.add(String.valueOf(ca[5][j]));
 			availSundayStrings.add(String.valueOf(ca[6][j]));
 		}
 		ArrayList<String>teamNames2 =teamNames;
 		//ArrayList<String>[][] members= mm.getMembers();
 		ArrayList<String> availMembers = mm.getAvailableMembers(0, 0, team);
 		map.put("availabilityMonday", availMondayStrings);
 		map.put("availabilityTuesday", availTuesdayStrings);
 		map.put("availabilityWednesday", availWednesdayStrings);
 		map.put("availabilityThursday", availThursdayStrings);
 		map.put("availabilityFriday", availFridayStrings);
 		map.put("availabilitySaturday", availSaturdayStrings);
 		map.put("availabilitySunday", availSundayStrings);
 		map.put("teamNames", teamNames);
 		map.put("teamNames2", teamNames);
 		map.put("maxCount", mm.getMaxCount());
 		map.put("teamSize", team.getMembers().size());
 		//map.put("availMembers", );
 		return "newMeeting";
 	}
 	
 	@RequestMapping("/meeting/create")
 	public String makeMeeting(Map<String, Object> map){
 		
 		
 		return "redirect:/meeting/"; //redirect to main meeting page
 	}
 
 }
