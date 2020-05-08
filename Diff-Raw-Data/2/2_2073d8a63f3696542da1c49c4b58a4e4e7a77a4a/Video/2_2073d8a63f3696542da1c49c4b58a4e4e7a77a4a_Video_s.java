 package controllers;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import models.Lesson;
 import play.mvc.Controller;
 
 public class Video extends Controller {
 	
	private static final Pattern MOVIE_ID_EXTRACTOR = Pattern.compile("v=(\\w+)"); 
 
     public static void show(Long id) {
     	Lesson lesson = Lesson.findById(id);
     	String videoId = extractMovieId(lesson);
         renderTemplate("Application/video.html", lesson, videoId);
     }
 
 	private static String extractMovieId(Lesson lesson) {
 		Matcher matcher = MOVIE_ID_EXTRACTOR.matcher(lesson.url);
 		if (matcher.find()) {
 			return matcher.group(1);
 		}
 		return null;
 	}
     
 }
