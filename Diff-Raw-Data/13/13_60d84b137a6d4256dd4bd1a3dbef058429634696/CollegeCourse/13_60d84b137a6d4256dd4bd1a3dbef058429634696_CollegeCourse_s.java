 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lab1;
 
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author Stuart - HP AMD 10
  */
 public abstract class CollegeCourse {
     
     private String courseName;
     private String courseNumber;
     
     public CollegeCourse(String courseName, String courseNumber) {
         this.setCourseName(courseName);
         this.setCourseNumber(courseNumber);
     }
 
     public final String getCourseName() {
         return courseName;
     }
 
     public final String getCourseNumber() {
         return courseNumber;
     }
     
     public final void setCourseName(String courseName) {
         if(courseName == null || courseName.length() == 0) {
             JOptionPane.showMessageDialog(null,
                     "Error: course name cannot be null or empty string");
             System.exit(0);
         }
         this.courseName = courseName;
     }
     
     public final void setCourseNumber(String courseNumber) {
         if(courseNumber == null || courseNumber.length() == 0) {
             JOptionPane.showMessageDialog(null,
                     "Error: course number cannot be null or empty string");
             System.exit(0);
         }
         this.courseNumber = courseNumber;
     }
 }
