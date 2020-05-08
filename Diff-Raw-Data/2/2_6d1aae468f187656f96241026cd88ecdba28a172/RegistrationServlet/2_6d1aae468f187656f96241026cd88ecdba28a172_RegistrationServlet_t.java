 package dashboard.servlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import dashboard.error.EmailInUseException;
 import dashboard.error.InvalidEmailException;
 import dashboard.error.InvalidPasswordException;
 import dashboard.error.InvalidUserNameException;
 import dashboard.error.NoSuchCourseException;
 import dashboard.error.UserNameInUseException;
 import dashboard.model.CourseContract;
 import dashboard.model.Student;
 import dashboard.registry.CourseRegistry;
 import dashboard.registry.StudentRegistry;
 import dashboard.util.RegistryInitializer;
 
 public class RegistrationServlet extends HttpServlet {
 	
 	private static final long serialVersionUID = -5081331444892620046L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp){
 		HttpSession session = req.getSession();
 		if(!RegistryInitializer.initialized()){
 			RegistryInitializer.initialize(session.getServletContext());
 		}
 	}
 	
 	/**
 	 * Called when a user is trying to register
 	 * @param req
 	 * @param resp
 	 * @throws IOException
 	 */
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		String action = req.getParameter("submit");
 		if(action.equals("Volgende"))
 			addStudent(req, resp);
 		else if(action.equals("Opslaan"))
 			addCourses(req, resp);
 		else if(action.equals("register2")){
 			HttpSession session = req.getSession();
 			Student student = (Student)session.getAttribute("student");
 			session.setAttribute("student_temp",student);
 			resp.sendRedirect("/jsp/register/add_courses.jsp");
 		}
 	}
 
 	private void addStudent(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		String username = req.getParameter("username");
 		String email = req.getParameter("mail");
 		String firstName = req.getParameter("firstname");
 		String lastName = req.getParameter("lastname");
		String password = req.getParameter("password1");
 		HttpSession session = req.getSession();
 		try{
 			StudentRegistry.addUser(firstName,lastName,username,email,password);//add the user to the list of existing users
 			session.setAttribute("student_temp",StudentRegistry.getUserByUserName(username));
 			resp.sendRedirect("/jsp/register/add_courses.jsp");
 		} catch (UserNameInUseException e){
 			resp.sendRedirect("/jsp/register/register.jsp?msg=De gebruikersnaam " + username + " is reeds in gebruik!");
 		} catch (InvalidUserNameException e){
 			resp.sendRedirect("/jsp/register/register.jsp?msg=De gebruikersnaam " + username + " is ongeldig!");
 		} catch (EmailInUseException e){
 			resp.sendRedirect("/jsp/register/register.jsp?msg=Email: " + email + " is reeds in gebruik!");
 		} catch (InvalidEmailException e){
 			resp.sendRedirect("/jsp/register/register.jsp?msg=Email: " + email + " is ongeldig!");
 		} catch (InvalidPasswordException e){
 			resp.sendRedirect("/jsp/register/register.jsp?msg=Het opgegeven wachtwoord is ongeldig!");
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
