 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 import models.*;
 import views.html.*;
 
 import play.data.Form;
 import models.FormData;
 
 @Security.Authenticated(Secured.class)
 public class Professors extends Controller {
   static Form<Course> courseForm = form(Course.class);
 
   public static Result index() {
     return redirect(routes.Professors.courses());
   }
 
   public static Result courses() {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isProfessor(uc))
     {
       Supervisor s = uc.getSupervisor();
       List<Course> courses = s.getCoursesSet();
       return ok(professor_courses.render(uc, courses));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   public static Result results(Long id) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isProfessor(uc))
     {
       Course course = Course.find.byId(id);
      return ok(professor_examResults.render(course));
     }
     else
       return unauthorized(forbidden.render());
   }
 }
