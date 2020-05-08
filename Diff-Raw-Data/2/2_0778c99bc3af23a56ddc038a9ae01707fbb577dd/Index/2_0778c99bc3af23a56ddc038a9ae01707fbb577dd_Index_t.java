 package com.preppa.web.pages;
 
 import com.preppa.web.data.AnnouncementDAO;
 import com.preppa.web.data.UserObDAO;
 import com.preppa.web.data.VocabDAO;
 import com.preppa.web.entities.Announcement;
 import com.preppa.web.entities.User;
 import com.preppa.web.entities.Vocab;
 import java.util.List;
 import java.util.Random;
 import java.net.URL;
 import java.io.*;
 import org.apache.tapestry5.annotations.ApplicationState;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.springframework.security.Authentication;
 import org.springframework.security.context.SecurityContextHolder;
 
 
 public class Index {
 
     private final Random random = new Random();
     private String message = "mymessage";
     @ApplicationState
     private User user;
     @Inject
     private UserObDAO userDAO;
     private boolean userExists;
 
     @Inject
     private AnnouncementDAO announcementDAO;
     @Property
     private Announcement announcement;
     void onActivate() {
         //Attempt to get the authentication token if the user is already logged in but not in
         //the ASO object.
        if(!userExists)
        {
             Authentication token  = SecurityContextHolder.getContext().getAuthentication();
             if(token != null) {
                 System.out.println(token.getPrincipal());
                 if((token.getPrincipal() instanceof String)) {
                     String username = (String) token.getPrincipal();
                     if(username != null && !username.equals("anonymous"))
                             user = userDAO.findByUsername(username);
 
                 }
                 else
                 {
                     user = (User)token.getPrincipal();
                 }
             }
 
        }
 
     }
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         System.out.println("Setting the message: " + message);
         this.message = message;
     }
 
     /**
      * @return the user
      */
     public User getUser() {
         return user;
     }
 
 	public static BufferedReader read(String url) throws Exception {
 		return new BufferedReader(
 			new InputStreamReader(
 				new URL(url).openStream()));
     }
 
     public String getWebPage() throws Exception {
         BufferedReader reader = read("http://www.preppa.com");
 		String line = reader.readLine();
         String returnVal = "";
         boolean copy = false;
         String beginning = "<div class=" + '"' + "topPost" + '"' + ">";
         String end = "</div> <!-- Closes topPost --><br/>";
 
 		while (line != null) {
 			System.out.println(line);
 			line = reader.readLine();
             if (line.contains(beginning) == true) {
                 copy = true;
             }
             else if (line.contains(end) == true) {
                 copy = false;
                 returnVal = returnVal + line;
                 break;
             }
             if (copy) {
                 returnVal = returnVal + line;
             }
         }
         if(returnVal == null) {
             returnVal = "No Featured Article";
         }
         return returnVal;
     }
 
     public boolean getUserExists() {
         return userExists;
     }
     //@Inject
     //private Session session;
     @Inject
     private VocabDAO vocabDAO;
     /*@Inject
     private DictionaryDAO dictionaryDAO;
 
     public List<DictionaryWord> getDictionary()
     {
         //return session.createCriteria(UserOb.class).list();
         return dictionaryDAO.findByPartialName("Ab");
     }*/
 
     public List<User> getUsers()
     {
         //return session.createCriteria(UserOb.class).list();
         return userDAO.findAll();
     }
     public List<Vocab> getVocab()
     {
         //return session.createCriteria(UserOb.class).list();
         return vocabDAO.findAll();
     }
 
     public List<Announcement> getAnnouncements() {
         List<Announcement> returnVal;
         returnVal = announcementDAO.findAllOrderedByDate();
        if (returnVal != null && returnVal.size() > 0) {
             returnVal = returnVal.subList(0, 1);
         }
 
         return returnVal;
     }
 }
