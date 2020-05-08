 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends LoggedApplication {
 
     public static void index() {
         List<DrawingRoom> availableRooms = DrawingRoom.find("order by name ASC").fetch();
         List<User> registeredUsers = User.find("order by login ASC").fetch();
         
        User connectedUser = getConnectedUser();
        
    	render(availableRooms, registeredUsers, connectedUser);
     }
 
     
     public static void showRoom(Long id) {
         DrawingRoom selectedRoom = DrawingRoom.findById(id);
         render(selectedRoom);
     }
 }
