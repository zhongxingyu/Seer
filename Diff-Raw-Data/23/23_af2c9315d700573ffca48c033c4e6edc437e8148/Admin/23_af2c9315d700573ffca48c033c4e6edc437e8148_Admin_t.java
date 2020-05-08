 package controllers;
 
 import play.mvc.*;
 import views.html.*;
 
 import utils.DataUtil;
 
 import models.*;
 
 public class Admin extends MasterController {
 
     public static Result index() {
         return ok(admin.render(DataUtil.dump("users",User.class)));
     }
 
 }
