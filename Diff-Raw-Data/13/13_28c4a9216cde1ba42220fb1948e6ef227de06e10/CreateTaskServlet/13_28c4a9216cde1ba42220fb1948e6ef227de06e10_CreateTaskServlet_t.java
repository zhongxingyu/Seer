 package servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import db.TaskDb;
 
 	public class CreateTaskServlet extends HttpServlet {
 	
 		private static final long serialVersionUID = -2917901106721177733L;
 		private static final String TASKS_DIRECTORY = "/vol/project/2012/362/g1236218/TaskFiles/";
 
 		protected void doPost(HttpServletRequest request,
 	              HttpServletResponse response) throws IOException {
 			
 			response.setContentType("text/html;charset=UTF-8");
 	        PrintWriter out = response.getWriter();
 	        
 	        //validate user credentials
 	        HttpSession session = request.getSession();
 	        if (session == null) {
 	        	response.sendRedirect("/");
 	        	return;
 	        }
 	        int accountID = (Integer) session.getAttribute("account_id");
 	        
 	        //validate user, add task to db, maked task directory
 	        String name = request.getParameter("name");
 	        float accuracy;
 	        int max_labels;
 	        int media_type;
 	        int annotation_type;
 	        int input_type;
 			try {
 				accuracy = Float.parseFloat(request.getParameter("accuracy"));
 				max_labels = Integer.parseInt(request.getParameter("max_labels"));
 				media_type = Integer.parseInt(request.getParameter("media_type"));
 				annotation_type = Integer.parseInt(request.getParameter("annotation_type"));
 				input_type = Integer.parseInt(request.getParameter("input_type"));
 			} catch (NumberFormatException e) {
 				out.println("accuracy or type not an integer");
 				e.printStackTrace();
 				return;
 			}
 			long expiry;
 			try {
 				expiry = getLongDate(request);
 			} catch (ParseException e) {					
 		    	out.println("<meta http-equiv=\"Refresh\" content=\"5\"; url=\"addtask.jsp\">");
 		        out.println("<html>");
 		        out.println("<body>");
 		        out.println("bad date given, returning to add task page");
 		        out.println("</body>");
 		        out.println("</html>");
 		        return;
 			}
 			List<String> answers = new LinkedList<String>();
 			String answer = request.getParameter("answer1");
			answers.add(answer);
 			for(int i = 2 ; i < 5 && answer != null ; i++) {
				answer = request.getParameter("answer" + i);
				answers.add(answer);
 			}
 			int tid = TaskDb.addTask(accountID, name, request.getParameter("question"), 
 					accuracy, media_type, annotation_type, input_type, max_labels, expiry, answers);
 	        if( tid > 0) {
 	            File taskFolder = new File(TASKS_DIRECTORY + "/" + tid);
 	        	taskFolder.mkdirs();
 	        	response.sendRedirect("/client/upload.jsp");	        
 	        }
 	}
 		
 
 	private long getLongDate(HttpServletRequest request) throws ParseException{
 		String expiryDateStr = request.getParameter("day") + request.getParameter("month") + request.getParameter("year");
 		Date expiryDate = new SimpleDateFormat("ddMMyyyy").parse(expiryDateStr);
 		return expiryDate.getTime();
 	}
 }
