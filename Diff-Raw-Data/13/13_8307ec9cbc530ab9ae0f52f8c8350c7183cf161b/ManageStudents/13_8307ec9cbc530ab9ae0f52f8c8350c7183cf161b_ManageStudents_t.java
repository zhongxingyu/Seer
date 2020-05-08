 /**
  *
  */
 package cs236369.hw5;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import cs236369.hw5.dal.Student;
 
 /**
  * @author Dennis
  *
  */
 public class ManageStudents extends HttpServlet
 {
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 1L;
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("aaa");
 		String action = (String)request.getParameter("action");
 		if (action.equals("remove")){ 
 			String studentId = (String) request.getParameter("student_id");
 			Student s = new Student(Integer.parseInt(studentId));
 			if (!s.delete()){
 				System.out.println("can't delete " + Integer.parseInt(studentId));
 			}
 			response.sendRedirect("studentManagemant.jsp");
 		}
 	}
 }
