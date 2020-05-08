 package controllers;
 
import java.util.List;
 
import models.DrawingRoom;
import models.User;
 
 public class Application extends LoggedApplication {
 
     public static void index() {
    	User connectedUser = getConnectedUser();
    	
    	connectedUser.join(null);
    	refreshConnectedUser(connectedUser);
    	
         List<DrawingRoom> availableRooms = DrawingRoom.find("order by name ASC").fetch();
         List<User> registeredUsers = User.find("order by login ASC").fetch();
         
     	render(availableRooms, registeredUsers, connectedUser);
     }
 }
