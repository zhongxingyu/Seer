 
 package com.huskysoft.eduki.data;
 
 /**
  * @author Cody Thomas, Rafael Vertido Class UrlConstants maintains a collection
  *         of variables representing appropriate api urls
  */
 public final class UrlConstants {
    public static final String BASE = "http://eduki-beta.herokuapp.com/api/";
     public static final String COURSES = BASE + "courses";
    public static final String LESSONS = BASE + "lessons";
 
     public static String getAllLessonsURL(int course_id) {
         return COURSES + "/" + course_id + "/lessons";
     }
 
    public static String getSingleLessonURL(int lesson_id) {
        return LESSONS + "/" + lesson_id;
     }
 }
