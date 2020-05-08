 package dashboard.servlet;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import dashboard.error.AlreadyEndedException;
 import dashboard.error.InvalidAmountException;
 import dashboard.error.InvalidEndDateException;
 import dashboard.error.NotStudyingException;
 import dashboard.model.*;
 import dashboard.registry.CourseRegistry;
 import dashboard.registry.StudentRegistry;
 
 public class TrackingServlet extends HttpServlet{
 
 	private static final long serialVersionUID = -786837324508180891L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		HttpSession session = req.getSession();
 		Student student = (Student)session.getAttribute("student");
 		
		if(student.getCurrentStudyMoment() == null){
			resp.sendRedirect("/track.jsp");
		} else {
			resp.sendRedirect("/track.jsp?mode=stop");
 		}
 	}
 	/**
 	 * Called when a user starts or stops tracking a study moment
 	 * @param req
 	 * @param resp
 	 * @throws IOException
 	 */
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		HttpSession session = req.getSession();
 		Student student = (Student)session.getAttribute("student");//get the current user
 		
 		if(req.getParameter("submit").equals("start") && student.getCurrentStudyMoment() == null){//if the student is not studying yet
 			Course course = CourseRegistry.getCourse((String) req.getParameter("courseinput"));
 			Date start = new Date();
 			student.setCurrentStudyMoment(new StudyMoment(start,course));//create a new study moment
 			session.setAttribute("startTracking", start);
 			session.setAttribute("course", course);
 			resp.sendRedirect("/track.jsp?mode=stop");
 		} else {//if the student was already studying
 			if(req.getParameter("submit").equals("stop")){
 				try {
 					student.endStudying(new Date(), Integer.parseInt(req.getParameter("amount")),req.getParameter("kind"));//end the current studymoment
 				} catch (NumberFormatException e) {
 					e.printStackTrace();
 				} catch (AlreadyEndedException e) {
 					e.printStackTrace();
 				} catch (InvalidEndDateException e) {
 					e.printStackTrace();
 				} catch (InvalidAmountException e) {
 					e.printStackTrace();
 				}
 				resp.sendRedirect("/track.jsp");
 			} else if(req.getParameter("submit").equals("cancel")){//cancel the study moment
 				try {
 					student.cancelCurrentStudyMoment();
 				} catch (NotStudyingException e) {
 					e.printStackTrace();
 				}
 				resp.sendRedirect("/track.jsp");
 			} else {
 				resp.sendRedirect("/error.jsp");
 			}
 		}
 	} 
 	
 }
