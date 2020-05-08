 package controllers;
 
 import java.util.List;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 import play.Logger;
 import play.db.jpa.*;
 
 import models.*;
 
 import play.libs.MimeTypes;
 import play.mvc.Controller;
 
 public class Contents extends Controller {
 
     public static void index() {
         render();
     }
 
     public static void create(String title, File photo)
 	throws FileNotFoundException {
 
     }
 
     public static void show(Integer id) {
         List<Content> contents = null;
         String controller = request.get().controller;
 		Logger.debug("Contents#show -> id: " + id);
 		Logger.debug("controller: " + controller);
         if (controller.equals("Photos")) {
             contents = Photo.find("byId", new Long(id)).fetch();
         } else if (controller.equals("Events")) {
             contents = Event.find("byId", new Long(id)).fetch();
         } else if (controller.equals("Coupons")) {
             contents = Coupon.find("byId", new Long(id)).fetch();
         } else if (controller.equals("Flyers")) {
             contents = Flyer.find("byId", new Long(id)).fetch();
         }
		Logger.debug("return content: " + contents.get(0));
 
         response.setContentTypeIfNotSet(contents.get(0).image.type());
         renderBinary(contents.get(0).image.get());
     }
     
     public static void list(String userId) {
 
     }    
     
 }
