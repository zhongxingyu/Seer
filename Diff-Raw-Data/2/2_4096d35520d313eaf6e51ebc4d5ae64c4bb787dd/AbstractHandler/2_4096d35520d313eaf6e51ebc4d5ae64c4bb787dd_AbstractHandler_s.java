 /**
  * 
  */
 package net.mysocio.ui;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import net.mysocio.ui.management.CommandExecutionException;
 
 import org.slf4j.Logger;
 
 /**
  * @author Aladdin
  *
  */
 public abstract class AbstractHandler extends HttpServlet {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6643496918578749898L;
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// Set a cookie for the user, so that the counter does not increate
 		// everytime the user press refresh
 		HttpSession session = request.getSession(true);
 		// Set the session valid for 15 minutes
 		session.setMaxInactiveInterval(15*60);
 		response.setCharacterEncoding("UTF-8");
 		String responseString = "";
 		PrintWriter out = response.getWriter();
 		try {
 			responseString = handleRequest(request, response);
 		} catch (Exception e) {
 			responseString = handleError(request, response, e, getLogger());
 		}
 		out.print(responseString);
 	}
 	
 	protected abstract String handleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws CommandExecutionException;
 	protected abstract Logger getLogger();
 	
 	/**
 	 * @param request
 	 * @param response
 	 * @param e
 	 * @return
 	 * @throws IOException
 	 */
 	protected String handleError(HttpServletRequest request,
 			HttpServletResponse response, Exception e, Logger logger)
 			throws IOException {
 		String responseString;
 		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		responseString = e.getMessage();
		logger.error("Request failed.");
 		return responseString;
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 }
