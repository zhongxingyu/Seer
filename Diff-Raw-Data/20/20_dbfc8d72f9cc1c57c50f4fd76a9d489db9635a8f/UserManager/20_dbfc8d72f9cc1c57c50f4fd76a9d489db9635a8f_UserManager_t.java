 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
 import ejb.Administrator;
 import ejb.Student;
 import ejb.Teacher;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 /**
  *
  * @author Rowan
  */
 @ManagedBean
 @SessionScoped
 public class UserManager {
 
     private Student student;
     private Teacher teacher;
     private Administrator administrator;
 
     public UserManager() {
     }
 
     public Student getStudent() {
         return student;
     }
 
     public void setStudent(Student student) {
         this.student = student;
     }
 
     public Teacher getTeacher() {
         return teacher;
     }
 
     public void setTeacher(Teacher teacher) {
         this.teacher = teacher;
     }
 
     public Administrator getAdministrator() {
         return administrator;
     }
 
     public void setAdministrator(Administrator administrator) {
         this.administrator = administrator;
     }
 
     public boolean loggedIn() {
         if (getUser() != null) {
             return true;
         }
         return false;
     }
 
     public String logout() {
         student = null;
         teacher = null;
         administrator = null;
         return "index";
     }
     
     public String getUsername() {
         if (isStudent())
             return student.getUsername();
         else if (isTeacher())
             return teacher.getUsername();
         else if (isAdministrator())
             return administrator.getUsername();
         return null;
     }
     
     public int getId() {
         if (isStudent())
             return student.getStudentId();
         if (isTeacher())
             return teacher.getTeacherId();
         return 0;
     }
     
    // Return "Student", "Teacher", "Administrator" or null for not logged in
     public String getType() {
        Object user = getUser();
        
        if (user == null) {
            return null;
        } else {
            return user.getClass().getSimpleName();
        }
     }
     
     public Object getUser() {
         if (student != null)
             return student;
         if (teacher != null)
             return teacher;
         if (administrator != null)
             return administrator;
         return null;
     }
     
     public boolean isStudent() {
         if (getUser() instanceof Student)
             return true;
         return false;
     }
     
     public boolean isTeacher() {
         if (getUser() instanceof Teacher)
             return true;
         return false;
     }
     
     public boolean isAdministrator() {
         if (getUser() instanceof Administrator)
             return true;
         return false;
     }
 }
