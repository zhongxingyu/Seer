 
 package com.huskysoft.eduki.data;
 
 import java.lang.reflect.Type;
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.huskysoft.eduki.TaskComplete;
 
 /**
  * @author Rafael Vertido LessonQuery will make queries related to lessons to
  *         the api
  */
 public class LessonQuery {
     /**
      * Will get a list of all lessons, returned to the callback as a string
      * 
      * @param callback The callback for when the request is complete
      */
     public static void getAllLessons(TaskComplete callback, int course_id) {
         String getLessonsURL = UrlConstants.getAllLessonsURL(course_id);
         new ConnectionTask(callback).execute(getLessonsURL);
     }
 
     /**
      * Will get a specific lesson, returned to the callback as a string
      * 
      * @param callback The callback for when the request is complete
      */
    public static void getSpecificLesson(TaskComplete callback, int lesson_id) {
        String specificLessonUrl = UrlConstants.getSingleLessonURL(lesson_id);
         new ConnectionTask(callback).execute(specificLessonUrl);
     }
 
     /**
      * Will parse the string and return a list of lessons represented in the
      * string.
      * 
      * @param data The json string to be parsed
      * @return A list of lessons represented in the string
      */
     public static List<Lesson> parseLessonsList(String data) {
         Gson gson = new Gson();
         Type collectionType = new TypeToken<List<Lesson>>() {
         }.getType();
         List<Lesson> lessons = gson.fromJson(data, collectionType);
         return lessons;
     }
 }
