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

	public static void getStudentBalance(Student student) {
		int lessonCount = 0;
		int paidLessons = 0;
		lessonCount = student.lessons.size();
		paidLessons = student.paidLessons;
		renderArgs.put("balance", paidLessons - lessonCount);
	}

	public static void addPaidLessons(Student student, int count) {
		student.paidLessons += count;
		student.save();
	}
 }
