 package controllers;
 
 import java.util.Collection;
 
 import flexjson.JSONSerializer;
 
 import models.Team;
 import models.TimeslotRequest;
 import play.mvc.Controller;
 
 public class TimeslotRequests extends Controller
 {
    public static String listTeams()
    {
       final Collection<Team> teams = Team.findAll();
       
       JSONSerializer teamSerializer =
                new JSONSerializer().include(
                   "id",
                   "name",
                   "parent.id"
                ).exclude("*");
       
       String returnString = "[";
      int i = 0;
       for( Team team : teams )
       {
     	 if( i++ > 0 ) { returnString += ", "; }
         returnString += teamSerializer.serialize( team );
       }
       returnString += "]";
 
       return returnString;
    }
    
    
    public static void listEnhanced() {
       final Collection<TimeslotRequest> requests = TimeslotRequest.findAll();
       
       JSONSerializer timeslotSerializer =
          new JSONSerializer().include(
             "id",
             "startDate",
             "endDate",
             "assignedTimeslots.id",
             "assignedTimeslots.startDate",
             "assignedTimeslots.endDate",
             "assignedTimeslots.student.firstName",
             "assignedTimeslots.student.lastName",
             "requester.firstName",
             "requester.lastName",
             "requester.team.id",
             "student.firstName",
             "student.lastName"
          ).exclude("*");
       
       String returnString = "[";
      int i = 0;
       for( TimeslotRequest request : requests )
       {
    	 if( i++ > 0 ) { returnString += ", "; }
         returnString += timeslotSerializer.serialize( request );
       }
       returnString += "]";
       
       
       returnString = "{\"teams\":" + listTeams() + ", \"requests\":" + returnString + "}";
       
       renderJSON( returnString );
    }
 }
