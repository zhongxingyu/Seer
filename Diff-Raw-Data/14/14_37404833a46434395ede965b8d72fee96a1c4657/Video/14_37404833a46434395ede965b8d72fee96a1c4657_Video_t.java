 package controllers;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import models.Lesson;
 import play.mvc.Controller;
 
 public class Video extends Controller {
 	
	private static final Pattern[] patterns = new Pattern[] {Pattern.compile("v=(.+)"), Pattern.compile("youtu.be/(.+)")};
 
     public static void show(Long id) {
     	Lesson lesson = Lesson.findById(id);
     	String videoId = extractMovieId(lesson);
         renderTemplate("Application/video.html", lesson, videoId);
     }
 
 	private static String extractMovieId(Lesson lesson) {
		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(lesson.url);
			if (matcher.find()) {
				return matcher.group(1);
			}
 		}
 		return null;
 	}
	
 }
