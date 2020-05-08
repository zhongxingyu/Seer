 package dashboard.servlet;
 
 
 import java.io.IOException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import dashboard.error.CourseAlreadyTakenException;
 import dashboard.error.NoSuchCourseException;
 import dashboard.model.CourseContract;
 import dashboard.model.Student;
 import dashboard.registry.CourseRegistry;
 
 
 public class CourseAddServlet extends HttpServlet {
 	
 
 	private static final long serialVersionUID = 696129415243839733L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		HttpSession session = req.getSession();
 		Student student = (Student)session.getAttribute("student");
 		if(student == null)
 			resp.sendRedirect("/login");
 		else
 			resp.sendRedirect("/add_course.jsp");
 	}
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		HttpSession session = req.getSession();
 		Student student = (Student)session.getAttribute("student");
 		String action = req.getParameter("submit");
 		CourseContract course;
 		try {
 			course = new CourseContract(CourseRegistry.getCourse(action));
 			student.addCourse(course);
 			resp.sendRedirect("/settings_vak.jsp");
 		} catch (NoSuchCourseException e) {
 			resp.sendRedirect("/error.jsp?msg=trying to add unexisting course");
 		} catch (CourseAlreadyTakenException e) {
 			resp.sendRedirect("/error.jsp?msg=can't add courses twice");
 		}
 	}
 			
 
 }
