 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Model;
 
 import Model.User;
 import java.util.ArrayList;
 
 /**
  *
  * @author shawnkrecker
  */
 public class AllUsers {
     
 
     public static ArrayList<User> allUsers = new ArrayList<User>();
     
     User user1;
     User user2; 
     User user3;
 
     public AllUsers(){
         allUsers = new ArrayList<User>();
         
         user1 = new User();
         user2 = new User();
         user3 = new User();
         
         user1.setFirstName("Shawn");
         user1.setLastName("Krecker");
         user1.setEmail("krecker@oswego.edu");
         user1.setCurrentLocation("Oswego,NY");
         user1.setOccupation("Student");
         user1.setJobTitle("SUNY Oswego");
         user1.setEducation("SUNY Oswego");
         user1.setSkills("Java, JSP, HTML, CSS, Android Development, PHP, Photoshop, Illustrator");
         user1.setHobbies("Music Producation, Graphic Design, Programming, Web Design");
         user1.setUsername("skrecker");
         user1.setPassword("lala");
         allUsers.add(user1);
         
         user2.setFirstName("Ethan");
         user2.setLastName("Neil");
         user2.setEmail("eneil@oswego.edu");
         user2.setCurrentLocation("Oswego,NY");
         user2.setOccupation("Student");
         user2.setJobTitle("SUNY Oswego");
         user2.setEducation("SUNY Oswego");
         user2.setSkills(null);
         user2.setHobbies(null);
         user2.setUsername("Ethan");
         user2.setPassword("lala");
         allUsers.add(user2);
         
         user3.setFirstName("Ben");
         user3.setLastName("Gordon");
        user3.setEmail("bgordon1@oswego.edu");
         user3.setCurrentLocation("Oswego,NY");
         user3.setOccupation("Student");
         user3.setJobTitle("SUNY Oswego");
         user3.setEducation("SUNY Oswego");
         user3.setSkills(null);
         user3.setHobbies(null);
         user3.setUsername("Ben");
         user3.setPassword("lala");
         allUsers.add(user3);
               
     }
     
     public User getUser(String username){
         User user = null;
         for(User temp: allUsers){            
                 if(temp.getUsername().equals(username)){
                     user = temp;
                     return user;
                 }
             
         }
         return user;
     }
     
     
 }
