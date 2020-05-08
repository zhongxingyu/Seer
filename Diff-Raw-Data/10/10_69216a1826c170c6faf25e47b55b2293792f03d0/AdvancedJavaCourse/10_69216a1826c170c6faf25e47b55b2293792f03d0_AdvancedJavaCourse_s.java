 package lab1;
 
 import javax.swing.JOptionPane;
 
 /**
  * Describe responsibilities here.
  * AdvancedJavaCourse must provide setters and getters as determined by Course.
  * That means courseName, courseNumber, credits, and prerequisites.
  * I am also including the member properties here, all private.
  * @author      Mark Urbanski
  * @version     1.00
  */
 public class AdvancedJavaCourse extends Course{
     private String courseName; //this should be private
     private String courseNumber;
     private double credits;
     private String prerequisites;
 
     public AdvancedJavaCourse(String courseName, String courseNumber) {
         this.setCourseName(courseName);
         this.setCourseNumber(courseNumber);
     }
 
     public String getCapitalizedCourseName() {
         return this.getCourseName().toUpperCase();
     }
 
 
     @Override
     public String getPrerequisites() {
         return prerequisites;
     }
 
     @Override
     public void setPrerequisites(String prerequisites) {
         if(prerequisites == null || prerequisites.length() == 0) {
             JOptionPane.showMessageDialog(null,
                     "Error: prerequisites cannot be null of empty string");
             System.exit(0);
         }
         this.prerequisites = prerequisites;
     }
 
     @Override
     public void setCredits(double credits) {
         if(credits < 0.5 || credits > 4.0) {
             JOptionPane.showMessageDialog(null,
                     "Error: credits must be in the range 0.5 to 4.0");
             System.exit(0);
         }
        this.setCredits(credits);
     }
 
     @Override
     public String getCourseName() {
         return courseName;
     }
 
     @Override
     public void setCourseName(String courseName) {
         this.courseName = courseName;
     }
 
     @Override
     public String getCourseNumber() {
         return courseNumber;
     }
 
     @Override
     public void setCourseNumber(String courseNumber) {
         this.courseNumber = courseNumber;
     }
 
     @Override
     public double getCredits() {
         return credits;
     }
     
 }
