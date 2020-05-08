 package controllers;
 
 import models.Picture;
 import models.User;
 import play.*;
 import play.mvc.*;
 import play.i18n.Messages;
 
 /**
  * For CRUD-interface
  * 
  * @author Peksa
  */
 public class Users extends CRUD
 {
 	
 	public static void register()
 	{
 		render();
 	}
 	
 	public static void add(String email, String username, String password, String fullname, Picture picture, int idiotTest) throws Throwable
 	{
 		// Quick fix
 		if(fullname == null || fullname.length() == 0) fullname = username;
 		if(email == null) email = "";
 		
 		if(idiotTest != 4711)
 		{
 			flash.error(Messages.get("errors.idiot"));
             params.flash();
             register();
 		}
 		if(validation.hasErrors())
 		{
             flash.error(Messages.get("errors.Parameters"));
             params.flash();
             register();
 		}
 		if (username.length() < 5)
 		{
             flash.error(Messages.get("errors.shortUsername"));
             params.flash();
             register();
 		}
 		if (password.length() < 8)
 		{
             flash.error(Messages.get("errors.shortPass"));
             params.flash();
             register();
 		}
		if (User.count("byUsernameIlike", username) > 0)
 		{
             flash.error(Messages.get("errors.TakenUsername"));
             params.flash();
             register();
 		}
 		else if (User.count("email = ?", email) > 0)
 		{
             flash.error(Messages.get("errors.TakenEmail"));
             params.flash();
             register();
 		}
 		else
 		{
 			String hash = Security.sha512Hash(username, password);
 			User user = new User(email, username, hash);
 			picture.save();
 			user.fullname = fullname;
 			user.picture = picture;
 			user.save();
 			
 			flash.keep("url"); // TODO(peksa): wat?
 			flash.success(Messages.get("controllers.Users.success"));
 			Secure.login();
 		}
 		
 	}
 }
