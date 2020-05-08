 package servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import db.TaskDb;
 
 	public class CreateTaskServlet extends HttpServlet {
 	
 		private static final long serialVersionUID = -2917901106721177733L;
 
 		protected void doPost(HttpServletRequest request,
 	              HttpServletResponse response) throws IOException {
 			
 			response.setContentType("text/html;charset=UTF-8");
 	        PrintWriter out = response.getWriter();
 	        
 	        //validate user credentials
 	        HttpSession session = request.getSession();
 	        if (session == null) {
 	        	response.sendRedirect("/index.jsp");
 	        	return;
 	        }
 	        int accountID = (Integer) session.getAttribute("account_id");
 	        
 	        //validate user, add task to db, maked task directory
 	        String task = request.getParameter("name");
 	        double accuracy;
 	        int type;
 			try {
 				accuracy = Double.parseDouble(request.getParameter("accuracy"));
 				type = Integer.parseInt(request.getParameter("type"));
 			} catch (NumberFormatException e) {
 				out.println("accuracy or type not an integer");
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
 	        if( !TaskDb.addTask(accountID, task, request.getParameter("question"), accuracy, type, expiry))
 	        	return;
        	response.sendRedirect("/client/upload.jsp");	        
 	}
 		
 
 	private long getLongDate(HttpServletRequest request) throws ParseException{
 		String expiryDateStr = request.getParameter("day") + request.getParameter("month") + request.getParameter("year");
 		Date expiryDate = new SimpleDateFormat("ddMMyyyy").parse(expiryDateStr);
 		return expiryDate.getTime();
 	}
 }
