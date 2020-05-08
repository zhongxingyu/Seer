 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 import models.*;
 import views.html.*;
 
 import play.data.Form;
 import scala.util.parsing.combinator.testing.Str;
 
 /**
  * Contains the controllers for the role admin
  */
 @Security.Authenticated(Secured.class)
 public class Admins extends Controller {
   static Form<Course> courseForm = form(Course.class);
   static Form<Student> studentFormEditing = form(Student.class);
   static Form<Student> newStudentForm = form(Student.class);
   static Form<UserCredentials> ucForm = form(UserCredentials.class);
   static Form<UserRole> urForm = form(UserRole.class);
   static Form<Supervisor> supervisorForm = form(Supervisor.class);
   static Form<Course> internalCourseForm = form(Course.class);
   static Form<Course> externalCourseForm = form(Course.class);
   static Form<Course> courseEditingForm = form(Course.class);
   static Form<Supervisor> editSupervisorForm = form(Supervisor.class);
 
   public static Result index() {
     return redirect(routes.Admins.courses());
   }
 
   /**
    * Overloading of method courses
    */
   public static Result courses() {
     return Admins.courses(internalCourseForm,externalCourseForm,false);
   }
 
   /**
    * Renders the page for managing the course
    */
   public static Result courses(Form<Course> intForm, Form<Course> extForm, boolean badRequest) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       List<Course> courses = Course.currentCourses();
       Collections.sort(courses,new Comparators.CourseCompareByDate());
       if (badRequest)
         return badRequest(admin_courses.render(uc,courses,intForm,extForm));
       else
         return ok(admin_courses.render(uc,courses,intForm,extForm));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   /**
    * Overloading for methond courseDetails
    */
   public static Result courseDetails(Long id) {
     Course c = Course.find.byId(id);
     courseEditingForm = courseEditingForm.fill(c);
     return Admins.courseDetails(id,courseEditingForm,false);
   }
 
   /**
    * Renders the page for editing the details of a course
    */
   public static Result courseDetails(Long id, Form<Course> form, boolean badRequest) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       if (badRequest)
         return badRequest(admin_course_details.render(uc,id,form));
       else
         return ok(admin_course_details.render(uc,id,form));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   /**
    * Read the data from POST requests for editing course data
    */
   public static Result editCourse(Long id) {
     Form<Course> filledForm = courseEditingForm.bindFromRequest();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       System.out.println(filledForm);
       if (filledForm.hasErrors())
       {
         System.out.println("Errore");
         return Admins.courseDetails(id,filledForm,true);
       }
       else
       {
         filledForm.get().update();
         return redirect(routes.Admins.courses());
       }
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 
   /**
    * Read the data from POST requests for creating new internal courses
    */
   public static Result newInternalCourse() {
     Form<Course> filledForm = internalCourseForm.bindFromRequest();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       System.out.println(filledForm);
       if (filledForm.hasErrors())
       {
         System.out.println("Errore");
         return Admins.courses(filledForm,externalCourseForm,true);
       }
       else
       {
         Course.create(filledForm.get());
         return redirect(routes.Admins.courses());
       }
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 
   /**
    * Read the data from POST requests for creating new external courses
    */
   public static Result newExternalCourse() {
     Form<Course> filledForm = externalCourseForm.bindFromRequest();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       System.out.println(filledForm);
       if (filledForm.hasErrors())
       {
         System.out.println("Errore");
         return Admins.courses(internalCourseForm,filledForm,true);
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
         return redirect(routes.Admins.courses());
       }
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 
   /**
    * Renders page form managing old courses
    */
   public static Result oldcourses() {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       List<Course> courses = Course.oldCourses();
       Collections.sort(courses,new Comparators.CourseCompareByDate());
       return ok(admin_oldcourses.render(uc,courses));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   /**
    * Overloading for method studentDetails
    */
   public static Result studentDetails(Long studentId) {
     Student student = Student.find.byId(studentId);
     studentFormEditing = studentFormEditing.fill(student);
     return Admins.studentDetails(studentId, studentFormEditing, false);
   }
 
   /**
    * Renders page for view and modify student data
    */
   public static Result studentDetails(Long studentId, Form<Student> form, boolean badRequest) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       Student student = Student.find.byId(studentId);
       List<Course> studyPlan = student.getStudyPlan();
       List<Course> coursesNotInSp = new ArrayList();
       for (Course c: Course.currentCourses())
         if (!studyPlan.contains(c))
           coursesNotInSp.add(c);
       List<CourseEnrollment> career = student.getEnrollmentsCareer();
       if (badRequest)
         return badRequest(admin_students_details.render(uc,student,studyPlan,coursesNotInSp,courseForm,career,studentFormEditing));
       else
         return ok(admin_students_details.render(uc,student,studyPlan,coursesNotInSp,courseForm,career,studentFormEditing));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   /**
    * Read data from POST request for editing student data
    */
   public static Result editStudentData(Long studentId) {
     Form<Student> filledForm = studentFormEditing.bindFromRequest();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       Student student = Student.find.byId(studentId);
       if (filledForm.hasErrors())
       {
         return Admins.studentDetails(studentId,filledForm,true);
       }
       else
       {
         filledForm.get().update();
         return redirect(routes.Admins.studentDetails(studentId));
       }
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 
   /**
    * Overloading for method students
    */
   public static Result students() {
    return Admins.students(newStudentForm,false,"");
   }
 
   /**
    * Renders page for managing students
    */
  public static Result students(Form<Student> form, boolean badRequest, String popup) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       List<Student> students = Student.getNotSuspended();
       Collections.sort(students,new Comparators.StudentCompareByName());
       Collections.sort(students,new Comparators.StudentCompareByStudyPlan());
       if (badRequest)
        return badRequest(admin_students.render(uc,students,form,popup));
       else
        return ok(admin_students.render(uc,students,form,popup));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   /**
    * Read data from POST request for creating new students
    */
   public static Result newStudent() {
     Form<Student> filledForm = newStudentForm.bindFromRequest();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       System.out.println(filledForm);
       if (filledForm.hasErrors())
       {
         System.out.println("Errore");
        return Admins.students(filledForm,true,"");
       }
       else
       {
         Student s = filledForm.get();
         s.courseYear = 0;
         s.admittedConditionally = false;
         s.isPlanApproved = 0;
         Student.create(s);
         return redirect(routes.Admins.students());
       }
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 
   /**
    * Renders page form managing suspended students
    */
   public static Result suspendedStudents() {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       List<Student> students = Student.getSuspended();
       Collections.sort(students,new Comparators.StudentCompareByName());
       return ok(admin_suspended_students.render(uc,students));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   /**
    * Suspend the student with specified id
    */
    public static Result suspendStudent(Long id) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       Student student = Student.find.byId(id);
       student.setSuspended(true);
       return Admins.studentDetails(id);
     }
     else
       return unauthorized(forbidden.render());
   }
 
   /**
    * Unsuspend the student with specified id
    */ 
   public static Result unsuspendStudent(Long id) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       Student student = Student.find.byId(id);
       student.setSuspended(false);
       return Admins.suspendedStudents();
     }
     else
       return unauthorized(forbidden.render());
   }
 
 
   /**
    * Overloading for method supervisors
    */
   public static Result supervisors(Long id) {
     Supervisor s = Supervisor.find.byId(id);
     if (s!=null)
       editSupervisorForm = editSupervisorForm.fill(s);
     return Admins.supervisors(id,supervisorForm,editSupervisorForm,false);
   }
 
   /**
    * Renders page for managing supervisors
    */
   public static Result supervisors(Long id, Form<Supervisor> newForm, Form<Supervisor> editForm, boolean badRequest) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
 
     if (Secured.isAdmin(uc))
     {
       List<Supervisor> supervisors = Supervisor.all();
       Collections.sort(supervisors,new Comparators.SupervisorCompareByName());
       if (badRequest)
         return badRequest(admin_supervisors.render(uc,supervisors,id,newForm,editForm));
       else
         return ok(admin_supervisors.render(uc,supervisors,id,newForm,editForm));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   /**
    * Read the data from POST requests for editing supervisor data
    */
   public static Result editSupervisor(Long id) {
     Form<Supervisor> filledForm = editSupervisorForm.bindFromRequest();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       System.out.println(filledForm);
       if (filledForm.hasErrors())
       {
         System.out.println("Errore");
         return Admins.supervisors(id,supervisorForm,filledForm,true);
       }
       else
       {
         filledForm.get().update();
         return redirect(routes.Admins.supervisors(id));
       }
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 
   /**
    * Read the data from POST requests for creating new supervisor
    */
   public static Result newSupervisor() {
     Form<Supervisor> filledForm = supervisorForm.bindFromRequest();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       System.out.println(filledForm);
       if (filledForm.hasErrors())
       {
         System.out.println("Errore");
         return Admins.supervisors(-1l,filledForm,editSupervisorForm,true);
       }
       else
       {
         Supervisor.create(filledForm.get());
         return redirect(routes.Admins.supervisors(-1l));
       }
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 
   /**
    * Overloading of method credentials
    */
   public static Result credentials() {
     return Admins.credentials(ucForm,urForm,false);
   }
 
   /**
    * Renders the page for managing usercredentials
    */
   public static Result credentials(Form<UserCredentials> userForm, Form<UserRole> roleForm, boolean badRequest) {
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       List<UserCredentials> ucs = UserCredentials.all();
       Collections.sort(ucs,new Comparators.UserCredentialsCompareByUserName());
       Collections.sort(ucs,new Comparators.UserCredentialsCompareByRole());
       if (badRequest)
         return badRequest(admin_credentials.render(uc,ucs,UserRole.all(),userForm, roleForm));
       else
         return ok(admin_credentials.render(uc,ucs,UserRole.all(),userForm, roleForm));
     }
     else
       return unauthorized(forbidden.render());
   }
 
   /**
    * Read the data from POST requests for creating a new user credential
    */
   public static Result newUserCredential() {
     Form<UserCredentials> filledForm = ucForm.bindFromRequest();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       System.out.println(filledForm);
       if (filledForm.hasErrors())
       {
         System.out.println("Errore");
         return Admins.credentials(filledForm,urForm,true);
       }
       else
       {
         UserCredentials.create(filledForm.get());
         return redirect(routes.Admins.credentials());
       }
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
 
 
   /**
    * Read the data from POST requests for creating a new user role
    */
   public static Result newUserRole() {
     Form<UserRole> filledForm = urForm.bindFromRequest();
     UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
     if (Secured.isAdmin(uc))
     {
       System.out.println(filledForm);
       if (filledForm.hasErrors())
       {
         System.out.println("Errore");
         return Admins.credentials(ucForm,filledForm,true);
       }
       else
       {
         UserRole ur = filledForm.get();
         ur.deleted = false;
         UserRole.create(ur);
         return redirect(routes.Admins.credentials());
       }
     }
     else
     {
       return unauthorized(forbidden.render());
     }
   }
   
   public static Result warnStudents(){
 	  UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
 	  System.out.println("\n\n\n\n\n______________________________________");
 	  
 	  if (Secured.isAdmin(uc))
 	    {
 		  
 		  
 		  List<Student> students = Student.getNotSuspended();
 		  int countOK=0;
 		  int countFail=0;
 		  String result="";
 		  String cannotreach="";
 		  for(Student student:students){
 			  if(student.isPlanApproved==0){
 				  result = SecuredApplication.emailMe(student.email, "", "NOTIFICATION","");
 				  if(result.contains("ERROR")){
 					  countFail++;
 					  cannotreach += student.fullName + " - " + student.email + result+"\n";
 				  }
 				  else {
 					  countOK++;
 				  }
 			  }
 		  }
 		  result = "" + countOK + " emails correctly sent\n" + countFail + " emails can't be send\n";
 		  if(!cannotreach.equals("")){
 			  result = result + "the following names are unreachable:\n" + cannotreach;
 		  }
	      return Admins.students(newStudentForm,false,result);
 	    }
 	    else
 	    {
 	      return unauthorized(forbidden.render());
 	    }	  
   }
   
 }
