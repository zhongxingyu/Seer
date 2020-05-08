 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.gravical.bwell.db;
 
 import com.gravical.bwell.models.Role;
 import com.gravical.bwell.models.User;
 import java.util.ArrayList;
 
 /**
  *
  * @author User
  */
 public class PopulateSampleData {
     private static ArrayList users = new ArrayList();
     
     private static ArrayList roles = new ArrayList();
 
     /**
      * @return the users
      */
     public static ArrayList getUsers() {
         return users;
     }
 
     /**
      * @return the roles
      */
     public static ArrayList getRoles() {
         return roles;
     }
     
     public PopulateSampleData () {
         
         Role admin      = new Role(1,"Admin");
         Role supervisor = new Role(2,"Supervisor");
         Role intern     = new Role(3,"Intern");
         Role client     = new Role(4,"Client");
         roles.add(admin);
         roles.add(supervisor);
         roles.add(intern);
         roles.add(client);
 
        User user1 = new User(1, "admin1","password",  "Ad", "Min", admin);
        User user2 = new User(2, "super1","password",  "Super", "Viser", supervisor);
        User user3 = new User(3, "intern1","password",  "In", "tern", intern);
        User user4 = new User(4, "client1","password",  "Cli", "Ent", client);
         users.add(user1);
         users.add(user2);
         users.add(user3);
         users.add(user4);        
     }
 }
