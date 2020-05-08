 
 package com.huskysoft.eduki.data;
 
 /**
  * @author Cody Thomas, Rafael Vertido Class UrlConstants maintains a collection
  *         of variables representing appropriate api urls
  */
 public final class UrlConstants {
    public static final String BASE = "http://eduki.herokuapp.com/api/";
     public static final String COURSES = BASE + "courses";
 
     public static String getAllLessonsURL(int course_id) {
         return COURSES + "/" + course_id + "/lessons";
     }
 
    public static String getSingleLessonURL(int course_id, int lesson_id) {
        return getAllLessonsURL(course_id) + "/" + lesson_id;
     }
 }
