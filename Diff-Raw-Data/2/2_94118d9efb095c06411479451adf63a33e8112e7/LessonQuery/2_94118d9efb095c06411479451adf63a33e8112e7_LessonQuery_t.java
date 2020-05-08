 package com.huskysoft.eduki.data;
 
 import static com.huskysoft.eduki.data.UrlConstants.COURSES;
 
 import java.lang.reflect.Type;
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.huskysoft.eduki.TaskComplete;
 
 public class LessonQuery {
     public static void getAllLessons(TaskComplete callback, String course_id) {
     	String lessonsInCourseUrl = COURSES + "/" + course_id;
         new ConnectionTask(callback).execute(lessonsInCourseUrl);
     }
     
     public static void getSpecificLesson(TaskComplete callback, String course_id, String lesson_id) {
    	String specificLessonUrl = COURSES + "/" + course_id + "/lessons/" + lesson_id;
     	new ConnectionTask(callback).execute(specificLessonUrl);
     }
     
     public static List<Lesson> parseLessonsList(String data) {
         Gson gson = new Gson();
         Type collectionType = new TypeToken<List<Lesson>>(){}.getType();
         List<Lesson> lessons = gson.fromJson(data, collectionType);
         return lessons;
     }
 
 	public static String parseLessonContent(String data) {
 		return data;
 	}
 }
