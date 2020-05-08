 package controllers;
 
 import java.util.Date;
 
 import models.Article;
 import models.ArticleComment;
 import models.Exercise;
 import models.ExerciseComment;
 import models.Tutorial;
 import models.TutorialComment;
 import models.User;
 import play.mvc.Controller;
 import play.mvc.With;
 
 public class ExerciseComments extends Controller{
 	
 	public static void addComment(long id, String txtComment)
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
 		System.out.println("User:" + user.firstName);
 		Exercise exercise = Exercise.findById(id);
 		System.out.println("Article: "+ exercise.content);
 		ExerciseComment comment = new ExerciseComment(exercise, user, null, null, "", txtComment, new Date());
 		comment.save();
 		
 		LogMaker.log("ExerciseCommentActivity", signedUser, "has added a comment");
 		
 		Exercises.loadExercise(exercise.getId());
 		
 	}
 
 }
