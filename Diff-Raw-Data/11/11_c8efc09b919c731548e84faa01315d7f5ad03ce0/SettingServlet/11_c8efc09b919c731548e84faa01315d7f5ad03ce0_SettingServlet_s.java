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
 
 public class SettingServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 383365373572564568L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		HttpSession session = req.getSession();
 		Student student = (Student)session.getAttribute("student");
 		if(student == null)
 			resp.sendRedirect("/login");
 		else
 			resp.sendRedirect("/settings_info.jsp");
 	}
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		HttpSession session = req.getSession();
 		Student student = (Student)session.getAttribute("student");
 		String action = req.getParameter("submit");
 		if(action.contains("remove_")){
 			String vak = action.replace("remove_","");
 			remove(vak,student,req,resp);
 			resp.sendRedirect("/settings_vak.jsp");
 		} else if(action.equals("voeg")){
 			resp.sendRedirect("/add_course");
 		} else if(action.equals("namechange")){
 			String firstName = req.getParameter("firstname");
 			String lastName = req.getParameter("lastname");
 			student.setFirstName(firstName);
 			student.setLastName(lastName);
 			resp.sendRedirect("/settings");
 		} else if(action.equals("passchange")){
 			String pass1 = req.getParameter("pass1");
 			String pass2 = req.getParameter("pass2");
 			String pass3 = req.getParameter("pass3");
 			if(student.isCorrectPassword(pass1) && (pass2.equals(pass3))){
 				student.setPassword(pass2);
 				resp.sendRedirect("/settings_pass.jsp");
 			} else
 				resp.sendRedirect("/error.jsp/msg=wrong passwords");
 		}
 	}
 	
 	private void remove(String vak,Student student,HttpServletRequest req, HttpServletResponse resp) 
 			throws IOException{
 		try {
 			student.removeCourse(vak);
 		} catch (NoSuchCourseException e) {
 			resp.sendRedirect("/error.jsp?msg= tried to remove an unexisting course");
 		}
 	}
 }
