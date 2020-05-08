 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 import models.*;
 import views.html.*;
 
 import play.data.Form;
 
 /**
  * Contains the controllers for the role professor
  */
 @Security.Authenticated(Secured.class)
 public class Professors extends Controller {
   static Map<Long,List<Form<CourseEnrollment>>> enrollForms = Collections.synchronizedMap(new HashMap());
   static Form<CourseEnrollment> enrollForm = form(CourseEnrollment.class);
 
   public static Result index() {
     return redirect(routes.Professors.courses());
   }
 
   /**
    * Render page with information about the professor courses
    */
   public static Result courses() {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isProfessor(uc))
     {
       Supervisor s = uc.getSupervisor();
       List<Course> courses = s.getCurrentCourses();
       return ok(professor_courses.render(uc, courses));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   /**
    * Overloading for method results (also prepare the forms needed)
    */
   public static Result results(Long id) {
     Course course = Course.find.byId(id);
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     List<Form<CourseEnrollment>> forms = new ArrayList<Form<CourseEnrollment>>();
     if (course.isInManifesto) {
       for (CourseEnrollment enrollment : course.getCoursesEnrollment())
       {
         Form<CourseEnrollment> f = form(CourseEnrollment.class);
         f = f.fill(enrollment);
         forms.add(f);
       }
     }
     else if (Secured.isSupervisor(uc))
     {
       Supervisor s = uc.getSupervisor();
       for (CourseEnrollment enrollment : course.getCoursesEnrollment())
       {
        if (s.getStudentsAdvisored().contains(enrollment.fetchStudent())) {
           Form<CourseEnrollment> f = form(CourseEnrollment.class);
           f = f.fill(enrollment);
           forms.add(f);
         }
       }
     }
     enrollForms.put(id,forms);
     return Professors.results(id, false);
   }
 
   /**
    * Render page where the professor shold give qualifications to students
    */
   public static Result results(Long id, boolean badRequest) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     Supervisor s = uc.getSupervisor();
     Course course = Course.find.byId(id);
     
     if (course.isInManifesto)
     {
       if (Secured.isProfessor(uc) && course.getProfessor().supervisorID.equals(s.supervisorID))
       {
           if (badRequest)
             return badRequest(professor_examResults.render(uc,course,enrollForms.get(id)));
           else
             return ok(professor_examResults.render(uc,course,enrollForms.get(id)));
       }
       else
         return unauthorized(forbidden.render());
     }
     else
     {
       if (Secured.isSupervisor(uc))
       {
         List<Student> students = new ArrayList(uc.getSupervisor().getStudentsAdvisored());
         List<Form<CourseEnrollment>> enrolls = enrollForms.get(id);
         if (badRequest)
           return badRequest(professor_examResults.render(uc,course,enrolls));
         else
           return ok(professor_examResults.render(uc,course,enrolls));
       }
       else
         return unauthorized(forbidden.render());
     }
   }
 
   /**
    * Read data from post request for add a qualification for
    * the course specified
    */
   public static Result addResults(Long courseId) {
     Form<CourseEnrollment> filledForm = enrollForm.bindFromRequest();
     System.out.println(filledForm);
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     Supervisor s = uc.getSupervisor();
     Course course = Course.find.byId(courseId);
     
     if (course.isInManifesto)
     {
       if (Secured.isProfessor(uc) && course.getProfessor().supervisorID.equals(s.supervisorID))
       {
         if (filledForm.hasErrors())
         {
           ListIterator<Form<CourseEnrollment>> i = enrollForms.get(courseId).listIterator();
           while (i.hasNext())
           {
             Form<CourseEnrollment> f = i.next();
             if (f.value().get().enrollmentID == Integer.parseInt(filledForm.data().get("enrollmentID")))
             {
               i.set(filledForm);
               break;
             }
           }
           return Professors.results(courseId, true);
         }
         else
         {
           CourseEnrollment ce = filledForm.get();
           if (filledForm.data().get("passed") != null)
           {
             ce.credits = ce.fetchCourse().credits;
           }
           else
             ce.credits = 0;
           ce.update();
           return redirect(routes.Professors.results(courseId));
         }
       }
       else
         return unauthorized(forbidden.render());
     }
     else
     {
       if (Secured.isSupervisor(uc))
       {
         if (filledForm.hasErrors())
         {
           ListIterator<Form<CourseEnrollment>> i = enrollForms.get(courseId).listIterator();
           while (i.hasNext())
           {
             Form<CourseEnrollment> f = i.next();
             if (f.value().get().enrollmentID == Integer.parseInt(filledForm.data().get("enrollmentID")))
             {
               i.set(filledForm);
               break;
             }
           }
           return Professors.results(courseId, true);
         }
         else
         {
           CourseEnrollment ce = filledForm.get();
           if (filledForm.data().get("passed") != null)
           {
             ce.credits = ce.fetchCourse().credits;
           }
           else
             ce.credits = 0;
           ce.update();
           return redirect(routes.Professors.results(courseId));
         }
       }
       else
         return unauthorized(forbidden.render());
     }
   }
 }
