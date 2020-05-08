 package edu.rit.taskers.command;
 
 import edu.rit.taskers.data.NewUser;
 import edu.rit.taskers.model.Contact;
 import edu.rit.taskers.model.User;
 import edu.rit.taskers.persistence.ContactDao;
 import edu.rit.taskers.persistence.UserDao;
 
 import java.util.Calendar;
 
 /**
  * @author ian hunt
  * @date 11/4/11
  */
 public class NewUserCommand {
 
     private NewUser newUser;
     private UserDao userDao;
     private ContactDao contactDao;
 
     public NewUserCommand(NewUser newUser, UserDao userDao, ContactDao contactDao) {
         this.newUser = newUser;
         this.userDao = userDao;
         this.contactDao = contactDao;
     }
 
     public void execute() {
 
         if(newUser == null) {
             System.err.println("NewUserCommand with null NewUser");
             
         } else {
 	        Contact c = new Contact();
 	        c.setName( newUser.getName() );
 	        c.setEmail( newUser.getEmail() );
 	        c.setPhone( newUser.getPhone() );
 	
 	        //Note these may be optional/configurable later
 	        c.setBio( newUser.getBio() );
 	        c.setPictureURL( newUser.getPictureURL() );
 	
 	        c.setRole( newUser.getRole() );
 	
 	        User u = new User();
 	        u.setLogin( newUser.getLogin() );
 	        u.setPassword( newUser.getPassword() );
 	        u.setCreated( Calendar.getInstance().getTime() );
 	        userDao.save(u);
	        
	        User savedUser = userDao.findByUsername(newUser.getLogin());
	        
	        c.setUserId(savedUser.getId());
	        contactDao.save(c);	        
         }
     }
 
 }
