 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
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
 
     public boolean loggedIn() {
         if (student != null || teacher != null) {
             return true;
         }
         return false;
     }
 
     public void logout() {
         student = null;
         teacher = null;
     }
     
     public String getUsername() {
         if (isStudent())
            return getClass().getUsername();
         else if (isTeacher())
             return teacher.getUsername();
         return null;
     }
     
     // Return "Student", "Teacher" or "Administrator"
     public String getType() {
         return getUser().getClass().getSimpleName();
     }
     
     public Object getUser() {
         if (student != null)
             return student;
         if (teacher != null)
             return teacher;
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
 }
