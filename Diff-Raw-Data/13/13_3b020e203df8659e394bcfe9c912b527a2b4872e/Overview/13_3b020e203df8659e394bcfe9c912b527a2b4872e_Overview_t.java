 package controllers;
 
 import java.util.List;
 import java.util.Set;
 
 import models.Availability;
 import models.Lesson;
 import models.Student;
 import models.User;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 public class Overview extends Controller {
 
 	@Before
 	static void setConnectedUser() {
 		if (Security.isConnected()) {
 			User user = User.find("byEmail", Security.connected()).first();
 			renderArgs.put("user", user);
 		}
 	}
 
 	public static void lessons(Student student) {
 		List<Lesson> lessons = Lesson.findAll();
 		renderArgs.put("lessons", lessons);
 		render(lessons);
 	}
 }
