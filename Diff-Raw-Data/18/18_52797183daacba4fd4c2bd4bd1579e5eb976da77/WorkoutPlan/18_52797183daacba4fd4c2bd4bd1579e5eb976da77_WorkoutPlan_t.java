 package controllers;
 
 import javax.persistence.Entity;
 
 import models.User;
 import play.mvc.Controller;
 
 public class WorkoutPlan extends Controller{
 	
 	public static void workoutplan()
 	{
 		//String email = Security.connected();
 		User signedUser=User.convertToUser(Security.session.get("user"));
 		String email=null;
 		if(signedUser!=null)
 			email=signedUser.email;
 			
 		System.out.println("Looking for email... " + email);
 		User user = (User) User.find("byEmail", email).first();
 		if (user==null)
 		{
 			Application.index(null);
 		}
 		else
 		{
 			renderTemplate("Application/workout_plan.html");
 		}
 		
 	}
	
	public static void addToWorkoutPlan()
	{
		
		
	}
 }
