 package servlets;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import classes.DayCommentManager;
 import classes.GroupManager;
 import classes.Mark;
 import classes.MarkManager;
 
 /**
  * Servlet implementation class updateMarks
  */
 @WebServlet("/updateMarks")
 public class updateMarks extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public updateMarks() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		int groupID = (Integer) request.getSession().getAttribute("groupID");
 		int subjectID = (Integer) request.getSession()
 				.getAttribute("subjectID");
 		GroupManager grManager = (GroupManager) request.getServletContext()
 				.getAttribute("groupmanager");
 		MarkManager markManager = (MarkManager) request.getServletContext()
 				.getAttribute("markmanager");
 		int mark;
 		String comment;
 		String dayNote;
 		int markID;
 		String dayOfWeek = request.getParameter("date");
 		String date = "";
 		Calendar c = Calendar.getInstance();
 		switch (dayOfWeek) {
 		case "monday":
 			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
 			date = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
 			break;
 		case "tuesday":
 			c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
 			date = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
 			break;
 		case "wednesday":
 			c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
 			date = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
 			break;
 		case "thursday":
 			c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
 			date = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
 			break;
 		case "friday":
 			c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
 			date = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
 			break;
 		}
		
 		dayNote = request.getParameter("dayNote" + c.getTime().getDay());
		if(DayCommentManager.dayCommentAlreadyExists(subjectID, date)){
 			DayCommentManager.editDayComment(date, dayNote, subjectID);
		}
		else{
 			DayCommentManager.createDayComment(date, dayNote, subjectID);
 		}
 		for (int i : GroupManager.getStundetsOfGroup(groupID)) {
 			try {
 				mark = Integer.parseInt(request.getParameter(
 						Integer.toString(i)).trim());
 				comment = request.getParameter(Integer.toString(i) + "C");
 				if (MarkManager.markAlreadyExists(subjectID, i, date)) {
 					markID = MarkManager.getMarkByDate(subjectID, i, date).mark_id;
 					markManager.editMark(markID, subjectID, i, date, mark,
 							comment);
 				} else {
 					markID = MarkManager.getMarkID();
 					markManager.createMark(markID, subjectID, i, date, mark,
 							comment);
 				}
 			} catch (NumberFormatException e) {
				if(request.getParameter(Integer.toString(i) + "C")!=null && request.getParameter(Integer.toString(i) + "C").length() > 2){
 					comment = request.getParameter(Integer.toString(i) + "C");
 					if (markManager.markAlreadyExists(subjectID, i, date)) {
 						markID = MarkManager.getMarkByDate(subjectID, i, date).mark_id;
 						markManager.deleteMark(markID);
						markManager.createOnlyComment(markID, subjectID, i, date, comment);
					}
					else{
 						markID = MarkManager.getMarkID();
						markManager.createOnlyComment(markID, subjectID, i, date,comment);
 					}
				}
				else if (MarkManager.getMarkByDate(subjectID, i, date) != null) {
 					markID = MarkManager.getMarkByDate(subjectID, i, date).mark_id;
 					markManager.deleteMark(markID);
 				}
 			}
 
 		}
 		RequestDispatcher dispatch = request
 				.getRequestDispatcher("/showGroup.jsp?groupID="
 						+ Integer.toString(groupID) + "&subjectID=" + subjectID);
 		dispatch.forward(request, response);
 	}
 
 }
