 package controllers;
 
 import java.util.Date;
 
 import models.Article;
 import models.ArticleComment;
 import models.Tutorial;
 import models.TutorialComment;
 import models.User;
 import play.mvc.Controller;
 import play.mvc.With;
 
 public class TutorialComments extends Controller{
 	
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
 		Tutorial tutorial = Tutorial.findById(id);
 		System.out.println("Article: "+ tutorial.content);
 		TutorialComment comment = new TutorialComment(tutorial, user, null, null, "", txtComment, new Date());
 		comment.save();
 		
 		LogMaker.log("TutorialCommentActivity", signedUser, "has added a comment");
 		
 		Tutorials.loadTutorial(tutorial.getId());
 		
 	}
 
 }
