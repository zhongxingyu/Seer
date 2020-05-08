 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import models.*;
 import views.html.*;
 
 import play.data.Form;
 import models.FormData;
 
 public class Application extends Controller {
   static Form<Course> courseForm = form(Course.class);
   
   public static Result index() {
    return ok(nonAuthIndex.render());
   }
 
   public static Result provadb() {
     return ok(
       provadb.render(Country.all(),Student.all())
       );
   }
 
   public static Result blankCourseForm() {
     return ok(courseform.render(courseForm));
   }
 
   public static Result newCourse() {
     Form<Course> filledForm = courseForm.bindFromRequest();
     if(filledForm.hasErrors()) {
         return badRequest(courseform.render(filledForm));
     } else {
         Course.create(filledForm.get());
         return ok("creato");
     }
   }
 }
 
