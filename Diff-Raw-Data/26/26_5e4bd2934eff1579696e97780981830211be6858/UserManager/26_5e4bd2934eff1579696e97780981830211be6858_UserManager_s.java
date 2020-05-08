 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.chalmers.fannysangles.friendzone.model.managers;
 
 import it.chalmers.fannysangles.friendzone.model.FriendzoneUser;
 import it.chalmers.fannysangles.friendzone.utils.AbstractManager;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.Stateless;
 
 /**
  * Just a template for a manager at the moment...
  * @author Pontus
  */
 @Stateless
public class UserManager extends AbstractManager {
     
     public UserManager(){
         super(FriendzoneUser.class);
     }
    
 }
