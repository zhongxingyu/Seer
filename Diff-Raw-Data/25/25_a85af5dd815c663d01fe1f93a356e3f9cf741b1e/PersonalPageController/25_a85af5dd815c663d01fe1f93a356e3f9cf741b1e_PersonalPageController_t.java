 package controllers.user;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.avaje.ebean.Ebean;
 
 import models.data.Link;
 import models.dbentities.UserModel;
 import models.user.AuthenticationManager;
 import models.user.Gender;
 import play.mvc.Content;
 import play.mvc.Results;
 import play.data.DynamicForm;
 import play.data.Form;
 import play.data.format.Formats;
 import play.data.validation.Constraints.Required;
 import play.mvc.Result;
 import views.html.commons.error;
 import views.html.user.showinfo;
 import views.html.user.editinfo;
 import views.html.user.errorinfo;
 import views.html.user.editpass;
 import controllers.EController;
 import controllers.routes;
 import controllers.UserController.Register;
 import models.EMessages;
 
 
 /**
  * Code for Bebras Application by AVANK
  * User: thomas
  */
 public class PersonalPageController extends EController {
 
       public static Result show() {
     	  	  ArrayList<Link> breadcrumbs = new ArrayList<Link>();
               Content showpage;
               breadcrumbs.add(new Link(EMessages.get("app.home"),"/"));
               breadcrumbs.add(new Link(EMessages.get("user.pip.personalinfo"),"/personal"));
               if(AuthenticationManager.getInstance().isLoggedIn()){
                   showpage = showinfo.render(EMessages.get("user.pip.personalinfo"), breadcrumbs);
               }else{
                   showpage = errorinfo.render(EMessages.get("error.no_info"),breadcrumbs);
               }
             return ok(showpage);
       }
 
       public static Result edit() {
     	  ArrayList<Link> breadcrumbs = new ArrayList<Link>();
           Content showpage;
           breadcrumbs.add(new Link(EMessages.get("app.home"),"/"));
           breadcrumbs.add(new Link(EMessages.get("user.pie.editinfo"),"/editpersonal"));
           if(AuthenticationManager.getInstance().isLoggedIn()){
               showpage = editinfo.render(EMessages.get("user.pie.editinfo"),breadcrumbs);
           }else{
               showpage = errorinfo.render(EMessages.get("error.no_info"),breadcrumbs);
           }
           return ok(showpage);
       }
 
       public static Result changeInformation(){          
           Date bd = new Date();
           UserModel userModel = Ebean.find(UserModel.class).where().eq(
                   "id",AuthenticationManager.getInstance().getUser().getID()).findUnique();
 
           // get new given information about user and save in data
           DynamicForm editInfo = form().bindFromRequest();
           
           // name
           if(!editInfo.get("fname").equals("")){
               userModel.name = editInfo.get("fname");
               AuthenticationManager.getInstance().getUser().data.name = editInfo.get("fname");
           }else{     	  
         	  flash("error", EMessages.get(EMessages.get("error.no_input_fname")));
         	  return Results.redirect(controllers.user.routes.PersonalPageController.edit());         	  
           } 
           
           // email
           if(!editInfo.get("email").equals("")){
               userModel.email = editInfo.get("email");
               AuthenticationManager.getInstance().getUser().data.email = editInfo.get("email");
           }else{
         	  flash("error", EMessages.get(EMessages.get("error.no_input_email")));
         	  return Results.redirect(controllers.user.routes.PersonalPageController.edit());   
           }
           
           // bday
           if(!editInfo.get("bday").equals("")){
               try {
                  bd = new SimpleDateFormat("yyyy/MM/dd").parse(editInfo.get("bday"));
                   Date currentDate = new Date();
                   if(bd.after(currentDate)){
                       flash("error", EMessages.get(EMessages.get("error.wrong_date_time")));
                       return Results.redirect(controllers.user.routes.PersonalPageController.edit()); 
                   }
               } catch (ParseException e) {
             	  flash("error", EMessages.get(EMessages.get("error.date")));
             	  return Results.redirect(controllers.user.routes.PersonalPageController.edit()); 
               }
               userModel.birthdate = bd;
               AuthenticationManager.getInstance().getUser().data.birthdate = bd;
           }else{
         	  flash("error", EMessages.get(EMessages.get("error.date")));
         	  return Results.redirect(controllers.user.routes.PersonalPageController.edit());       	  
           }
           
           // gender
           AuthenticationManager.getInstance().getUser().data.preflanguage = editInfo.get("prefLanguage");
           Gender gen = Gender.Female;
           if(editInfo.get("gender").equals("Male")){
               gen = Gender.Male;
          }else if(editInfo.get("gender").equals("Other")){
        	  gen = Gender.Other;
           }
           AuthenticationManager.getInstance().getUser().data.gender = gen;
           
           // language
           userModel.preflanguage = editInfo.get("prefLanguage");
           EMessages.setLang(editInfo.get("prefLanguage"));
           userModel.gender = gen;
           
           // save new information in db
           Ebean.save(userModel);
 
           // redirect information page
           return Results.redirect(controllers.user.routes.PersonalPageController.show());
       }
 
       public static Result checkValid(){
           return ok();
       }
 
       public static Result changePassword(){
               ArrayList<Link> breadcrumbs = new ArrayList<Link>();
               Content showpage;
               breadcrumbs.add(new Link("Home","/"));
               breadcrumbs.add(new Link("Edit password","/passwedit"));
               if(AuthenticationManager.getInstance().isLoggedIn()){
                   showpage = editpass.render("Edit password", breadcrumbs);
               }else{
                   showpage = errorinfo.render("Information is not available",breadcrumbs);
               }
             return ok(showpage);
       }
       
       public static class Edit{
             @Required
             public String fname;
             @Required
             public String email;
             @Required
            @Formats.DateTime(pattern = "yyyy/MM/dd")
             public String bday;
             @Required
             public String password;
             @Required
             public String controle_passwd;
             @Required
             public String gender;
             @Required
             public String prefLanguage;
       }
 }
