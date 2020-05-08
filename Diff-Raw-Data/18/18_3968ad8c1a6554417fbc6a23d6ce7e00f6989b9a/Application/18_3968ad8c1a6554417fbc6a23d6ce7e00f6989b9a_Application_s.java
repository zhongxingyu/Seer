 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import models.*;
 import views.html.*;
 
 import play.data.Form;
 import models.FormData;
 
@Security.Authenticated(Secured.class)
 public class Application extends Controller {
   static Form<Course> courseForm = form(Course.class);
   
   public static Result index() {
    
    System.out.println("***REQ***\n" + request().username());
    System.out.println("username:" + request().username());
    System.out.println("uri:" + request().uri());
    
    return ok(index.render(UserCredentials.find.where().eq("userName",request().username()).findUnique()));
    
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
 
