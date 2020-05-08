 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 import models.*;
 import views.html.*;
 
 import play.data.Form;
 
 @Security.Authenticated(Secured.class)
 public class Students extends Controller {
   static Form<Course> courseForm = form(Course.class);
 
   public static Result index() {
     return redirect(routes.Students.studyplan());
   }
 
   public static Result addToStudyPlan(Long idCourse, Long idStudent) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isStudent(uc))
     {
       Student s = uc.getStudent();
       s.addToStudyPlan(idCourse);
       return redirect(
         routes.Students.index());
     }
     else if (Secured.isAdmin(uc))
     {
       Student s = Student.find.byId(idStudent);
       s.addToStudyPlan(idCourse);
       return redirect(
         routes.Admins.studentDetails(idStudent));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   public static Result rmFromStudyPlan(Long idCourse, Long idStudent) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isStudent(uc))
     {
       Student s = uc.getStudent();
       s.rmFromStudyPlan(idCourse);
       return redirect(
         routes.Students.index());
     }
     else if (Secured.isAdmin(uc))
     {
       Student s = Student.find.byId(idStudent);
       s.rmFromStudyPlan(idCourse);
       return redirect(
         routes.Admins.studentDetails(idStudent));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   public static Result studyplan() {
     return Students.studyplan(courseForm,false,"");
   }
 
   public static Result studyplan(Form<Course> form, boolean badRequest, String appreqMsg) {
     String username = request().username();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isStudent(uc))
     {
       Student student = uc.getStudent();
       List<Course> studyPlan = student.getStudyPlan();
       List<Course> coursesNotInSp = new ArrayList();
       for (Course c: Course.currentCourses())
         if (!studyPlan.contains(c))
           coursesNotInSp.add(c);
       if (badRequest)
         return badRequest(students_studyplans.render(uc,student,studyPlan, coursesNotInSp, form, appreqMsg));
       else
         return ok(students_studyplans.render(uc,student,studyPlan, coursesNotInSp, form, appreqMsg));
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 
   public static Result career() {
     String username = request().username();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isStudent(uc))
     {
       Student student = uc.getStudent();
       List<CourseEnrollment> career = student.getEnrollmentsCareer();
       return ok(students_careers.render(uc, career));
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 
   public static Result appreq() {
     String username = request().username();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isStudent(uc))
     {
       Student student = uc.getStudent();
       
       if (student.isStudyPlanOk())
       {
         student.approvalRequest();
         
         //send email
        String body = "";
         String subject = "Request for study plan APPROVAL";
         String msg = SecuredApplication.emailMeNow(student.currentAdvisor.email,body,subject,student.email);        
         
         return Students.studyplan(courseForm,false,msg);
       }
       else
       {
         String msg = student.checkStudyPlan();
         return Students.studyplan(courseForm,false,msg);
       }
       
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 
   public static Result newExternCourse() {
     Form<Course> filledForm = courseForm.bindFromRequest();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isStudent(uc))
     {
       if (filledForm.hasErrors())
       {
         return Students.studyplan(filledForm,true,"");
       }
       else
       {
         Course newcourse = filledForm.get();
         newcourse.academicYear = Course.AcademicYear();
         newcourse.credits = 3;
         newcourse.isInManifesto = false;
         newcourse.notes = "external course";
         newcourse.isbyUNITN = false;
         newcourse.deleted = false;
         Course.create(newcourse);
 
         Students.addToStudyPlan(newcourse.courseID.longValue(),uc.getStudent().userID.longValue());
         return redirect(routes.Students.studyplan());
       }
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
   
   public static Result modifyStudyplan() {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isStudent(uc))
     {
       uc.getStudent().rejectSP();
       return  studyplan() ;
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 }
