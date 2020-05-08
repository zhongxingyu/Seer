 package controllers;
 
 import java.util.Date;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.formdata.SurferFormData;
 import views.formdata.SurferTypes;
 import views.html.Index;
 import views.html.ShowSurfer;
 import views.html.ManageSurfer;
 import views.html.Updates;
 import models.SurferDB;
 
 /**
  * Implements the controllers for this application.
  */
 public class Application extends Controller {
 
   /**
    * Returns the home page. 
    * @return The resulting home page. 
    */
   public static Result index() { 
     return ok(Index.render("Welcome to the home page."));
   }
   
   public static Result newSurfer(){
     SurferFormData surferFD = new SurferFormData();
     Form<SurferFormData> formData = Form.form(SurferFormData.class).fill(surferFD);
     return ok(ManageSurfer.render(formData, SurferTypes.getTypes()));
   }
   
   public static Result postSurfer(){
     Form<SurferFormData> formData = Form.form(SurferFormData.class).bindFromRequest();
     if (formData.hasErrors()) {
       flash("error", "Please correct the form below.");
       return badRequest(ManageSurfer.render(formData, SurferTypes.getTypes()));
     }
     else {
       SurferFormData data = formData.get();
       flash("success", String.format("Successfully added %s", data.name));
       SurferDB.add(data.slug, data);
       return ok(ShowSurfer.render(SurferDB.getSurfer(data.slug)));
     }
   }
   
   public static Result deleteSurfer(String slug){
     SurferDB.deleteSurfer(slug);
     SurferDB.getDeleteSurfer(slug).setAction("Delete");
     SurferDB.getDeleteSurfer(slug).setDate(new Date());;
     return ok(Index.render(""));
   }
   
   public static Result manageSurfer(String slug){
     SurferFormData surferFD = new SurferFormData(SurferDB.getSurfer(slug));
     surferFD.action = "Edit";
     surferFD.date = new Date();
     Form<SurferFormData> formData = Form.form(SurferFormData.class).fill(surferFD);
     return ok(ManageSurfer.render(formData, SurferTypes.getTypes(SurferDB.getSurfer(slug).getType())));
   }
   
   public static Result getSurfer(String slug){
     if (SurferDB.getSurfer(slug) != null){
       return ok(ShowSurfer.render(SurferDB.getSurfer(slug)));
     }
     else {
       return ok(Index.render(""));
     }
   }
   
   public static Result getUpdates(){
       return ok(Updates.render(""));
   }
   
 }
