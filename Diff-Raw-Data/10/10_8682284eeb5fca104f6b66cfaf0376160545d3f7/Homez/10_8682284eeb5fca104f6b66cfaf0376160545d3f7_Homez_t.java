 package controllers;
 
 import java.util.List;
 
 import org.apache.commons.lang.math.RandomUtils;
 
 
 import models.Photo;
 import models.Post;
 import models.Responses;
 import models.User;
 
 /**
  * User: Administrator Date: 2009-12-6 Time: 22:17:39
  */
 public class Homez extends Basez {
 	public static void index() {
 		User currentUser = getLoginUser();
         List<Photos> lastPhotos ;
 		if (currentUser != null) {
 			List<Photos>  yourlastPhotos = Photo.find("author = ? order by uploadAt desc", currentUser).fetch(5);
             renderArgs.put("yourlastPhotos",yourlastPhotos);
             lastPhotos = Photo.find("author != ? order by uploadAt desc",currentUser).fetch(5);    
             renderArgs.put("lastPhotos",lastPhotos );
 		} else {
		   long count = Photo.count();
		   if(count>0) {
		   int randomId = (int) (RandomUtils.nextInt()%count);
           Photo photo = Photo.all().from(randomId).first();
             renderArgs.put("photo",photo );
       }
		}
 		List<Post> lastPosts = Post.find("order by id desc").from(0).fetch(5);
 		renderArgs.put("lastPosts",lastPosts );		
 		List<Responses> lastResponses = Responses.find("order by postedAt desc").from(0).fetch(3);
 		renderArgs.put("lastResponses",lastResponses );
 		
 		render();
 	}
 }
