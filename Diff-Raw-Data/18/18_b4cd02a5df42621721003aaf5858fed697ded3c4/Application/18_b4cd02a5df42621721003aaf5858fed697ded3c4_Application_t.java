 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
         render();
     }
 
     public static void add() {
         render();
     }
 
    public static void show() {
     	
     	if(Ask.isLoggedInBool()){
     		
         	List<Picture> rawPics = Picture.listByStylist(session.get("id"));
         	List<Picture> pics = Picture.ratioify(rawPics, 240);
        
        	render(pics);    		
     	}
     	else{
     		Application.index();
     	}
     	
     }
 }
