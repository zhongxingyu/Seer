 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.exercise.submitting;
 
 /**
  *
  * @author lccstudent
  */
 public class PostLogger extends Poster {
 
     private static final String URL = "http://intel.gatech.edu/toolkit/auto_postLogger.php";
 
     public PostLogger() {
        super(URL, "problem_id", "user_id", "session_id", "java_class", "java_method", "level", "message", "timestamp");
     }
 }
