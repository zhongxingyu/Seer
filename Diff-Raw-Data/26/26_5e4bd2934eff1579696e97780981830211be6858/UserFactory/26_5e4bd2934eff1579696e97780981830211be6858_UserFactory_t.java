 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.chalmers.fannysangles.friendzone.model.managers;
 
 import it.chalmers.fannysangles.friendzone.model.FriendzoneUser;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 /**
  *
  * @author marcusisaksson
  */
 public class UserFactory {
     UserManager userManager = lookupUserManagerBean();
        
     private static UserFactory instance;
    @EJB
    UserManager um;
     
     public static UserFactory getInstance() {
         if(instance == null) {
             instance = new UserFactory();
         }
         return instance;
     }
     
     public FriendzoneUser getUniversalUser(){
//        UserManager um = lookupUserManagerBean();
         if(um.getRange(0, 1).size() < 1){
             um.add(new FriendzoneUser("Friend", "friend", null, null, 0, null, null));
         }
         return (FriendzoneUser) um.getRange(0, 1).get(0);
     }
 
     private UserManager lookupUserManagerBean() {
         try {
             Context c = new InitialContext();
             return (UserManager) c.lookup("java:global/it.chalmers.fannysangles_friendzone-ear_ear_1.0-SNAPSHOT/it.chalmers.fannysangles_friendzone-model_ejb_1.0-SNAPSHOT/UserManager!it.chalmers.fannysangles.friendzone.model.managers.UserManager");
         } catch (NamingException ne) {
             Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
             throw new RuntimeException(ne);
         }
     }
     
 }
