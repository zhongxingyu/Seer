 package controllers;
 
 import java.io.File;
 import java.util.Date;
 import java.util.List;
 
 import models.Room;
 import models.Roomy;
 import play.*;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class Profile extends Controller{
 	
     public static void index(){
    	String username = Security.connected();
    	Roomy r = Roomy.find("byEmail", username).first();
     	long roomId = r.owner.getId();
        render(roomId,r,username);
     }
     
     public static void updateRoomy(String username, String password, String fname, String lname, String squestion, String sanswer, String bday, File photo){
     	String email = Security.connected();
     	Roomy r = Roomy.find("byEmail", email).first();
     	//if (r.password==password){
 	    	r.firstName = fname;
 	    	r.lastName =lname;
 	    	r.password = password;
 	    	r.secretQuestion = squestion;
 	    	r.sqAnswer = sanswer;
 	    	r.username = username;
 	    	r.save();
     	//}
     	index();
     }
     
     
     public static void updatePhoto(File photo){
     	String email = Security.connected();
     	Roomy r = Roomy.find("byEmail", email).first();
     	r.pathToPicture = photo;
     	index();
     }
     
     public static void updateCurrentRoom( String newRoom){
     	String email = Security.connected();
     	Roomy r = Roomy.find("byEmail", email).first();
     	Room room = Room.find("byName", newRoom).first();
     	r.owner = room;
     	r.save();
     	room.save();
     	Deposit.index(room.getId());
     }
 }
 
