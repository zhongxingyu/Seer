 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lab1;
 
 /**
  *
  * @author Owner
  */
 public class StartUp {
     
     public static void main(String[] args) {
         
        Course course1 = new IntroToProgrammingCourse("Intro to Programming", "152-107", 2.0);
         
         System.out.println(course1);
        
        Course course2 = new IntroJavaCourse("Intro to Java", "152-134", 4.0, "152-107 Intro to Programming");
        
        System.out.println(course2);
    
     }
     
     
     
    
 }
