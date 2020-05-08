 package controllers;
 
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import com.sun.xml.internal.ws.developer.UsesJAXBContext;
 
 import models.*;
 
 
 public class Upload extends Controller {
 	
 	
 	public static String pictureId = "null";
 
 	public static void index() {
 
         if (Login.loggedIn() == false) {
             flash.success("Please sign-in first.");
             Login.index();
         }
 	}
 	
 	
 	public static void uploadAvatar(Picture picture) {
         picture.save();
 
         List<User> users = User.findAll();
         for (int i = 0; i < users.size(); i++) {
             if (users.get(i).mail.equals(session.get("user"))) {
                 users.get(i).avatar = "http://localhost:9000/upload/getpicture?id=" + picture.id.toString();
                 picture.user = session.get("user");
                 picture.use = "avatar";
                 picture.save();
                 users.get(i).save();
             }
         }
 
         Account.index();
     }
 	
 	public static void uploadItem(Picture picture, double articleNr) {
        
		picture.save();
         
        
         List<Item> items = Item.findAll();
          
         for (int i = 0; i < items.size(); i++) {
             if (items.get(i).articleNr == articleNr) {
                 items.get(i).shop = "http://localhost:9000/upload/getpicture?id=" + picture.id.toString();
                 picture.user = session.get("user");
                 picture.use = "item";
                 picture.save();
                 items.get(i).save();
             }
         }
 
         Account.index();
     }
 	
 	
 	
 
     public static void getPicture(long id) {
         Picture picture = Picture.findById(id);
         response.setContentTypeIfNotSet(picture.image.type());
         renderBinary(picture.image.get());
         System.out.println("get Picture");
     }
 	
 }
