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
 public class CreditCourse extends CollegeCourse {
     
     private double credits;
     private String prerequisites;
     
     public CreditCourse(String courseName, String courseNumber) {
        super(courseName, courseNumber);
     }
     
     public void setCredits(double credits) {
         if(credits < 0.5 || credits > 4.0) {
             System.out.println(
                     "Error: credits must be in the range 0.5 to 4.0");
             System.exit(0);
         }
         this.setCredits(credits);
     }
     
     public void setPrerequisites(String prerequisites) {
         if(prerequisites == null || prerequisites.length() == 0) {
             JOptionPane.showMessageDialog(null,
                     "Error: prerequisites cannot be null of empty string \n"
                   + "       Pass \"None\" when no prerequisites" );
             System.exit(0);
         }
         this.prerequisites = prerequisites;
     }
     
 }
