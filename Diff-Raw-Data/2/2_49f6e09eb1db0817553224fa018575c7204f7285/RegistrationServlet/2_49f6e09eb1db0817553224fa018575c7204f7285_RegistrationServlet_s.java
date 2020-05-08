 package dashboard.servlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import dashboard.error.*;
 import dashboard.registry.CourseRegistry;
 import dashboard.registry.StudentRegistry;
 import dashboard.model.CourseContract;
 import dashboard.model.Student;
 
 public class RegistrationServlet extends HttpServlet {
 	
 	private static final long serialVersionUID = -5081331444892620046L;
 
 	/**
 	 * Called when a user is trying to register
 	 * @param req
 	 * @param resp
 	 * @throws IOException
 	 */
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		String action = req.getParameter("submit");
 		if(action.equals("volgende"))
 			addStudent(req, resp);
		else if(action.equals("registreren"))
 			addCourses(req, resp);
 		else if(action.equals("register2")){
 			HttpSession session = req.getSession();
 			Student student = (Student)session.getAttribute("student");
 			session.setAttribute("student_temp",student);
 			resp.sendRedirect("/register_settings.jsp");
 		}
 	}
 
 	private void addStudent(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		String username = req.getParameter("username");
 		String email = req.getParameter("mail");
 		String firstName = req.getParameter("firstname");
 		String lastName = req.getParameter("lastname");
 		String password = req.getParameter("password");
 		HttpSession session = req.getSession();
 		try{
 			StudentRegistry.addUser(firstName,lastName,username,email,password);//add the user to the list of existing users
 			session.setAttribute("student_temp",StudentRegistry.getUserByUserName(username));
 			resp.sendRedirect("/register_settings.jsp");
 		} catch (UserNameInUseException e){
 			resp.sendRedirect("/register.jsp?msg=This username is already in use!");
 		} catch (InvalidUserNameException e){
 			resp.sendRedirect("/register.jsp?msg=This username is not valid!");
 		} catch (EmailInUseException e){
 			resp.sendRedirect("/register.jsp?msg=Email: " + email + " is already in use!");
 		} catch (InvalidEmailException e){
 			resp.sendRedirect("/register.jsp?msg=This email is not valid!");
 		} catch (InvalidPasswordException e){
 			resp.sendRedirect("/register.jsp?msg=This password is not valid!");
 		}
 	}
 	
 	private void addCourses(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		String[] courses = req.getParameter("courses").split(";");
 		HttpSession session = req.getSession();
 		Student student = (Student)session.getAttribute("student_temp");
 		ArrayList<CourseContract> courseList = new ArrayList<CourseContract>();
 		for(int i = 0; i < courses.length; i++)
 			try {
 				courseList.add(new CourseContract(CourseRegistry.getCourse(courses[i])));
 			} catch (NoSuchCourseException e) {
 				e.printStackTrace();
 			}
 		if(courseList != null)
 			student.setCourses(courseList);
 		session.removeAttribute("student_temp");
 		session.setAttribute("student",student);
 		resp.sendRedirect("/track");
 	}
 }
