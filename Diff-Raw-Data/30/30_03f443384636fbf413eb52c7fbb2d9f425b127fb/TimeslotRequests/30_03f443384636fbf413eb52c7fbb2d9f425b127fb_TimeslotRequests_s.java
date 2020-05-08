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
      
       for( Team team : teams )
       {
         returnString += teamSerializer.serialize( team ) + ", ";
       }
      
      returnString = returnString.substring(0,returnString.length()-2);
      
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
       
      //timeslotSerializer.serialize( requests );
      
       String returnString = "[";
      
       for( TimeslotRequest request : requests )
       {
         returnString += timeslotSerializer.serialize( request ) + ", ";
       }
      
      returnString = returnString.substring(0,returnString.length()-2);
      
       returnString += "]";
       
       
       returnString = "{\"teams\":" + listTeams() + ", \"requests\":" + returnString + "}";
       
       renderJSON( returnString );
    }
   
//   public static void details()
//   {
//      TimeslotRequest.findById( id )
//   }
 }
