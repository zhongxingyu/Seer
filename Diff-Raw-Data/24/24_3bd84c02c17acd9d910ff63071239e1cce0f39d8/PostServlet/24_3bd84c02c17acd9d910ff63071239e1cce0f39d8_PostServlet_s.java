 /**
  * 
  */
 package edu.usc.cs587.examples.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.usc.cs587.examples.Constants;
 import edu.usc.cs587.examples.dbhandlers.DatabaseHandler;
 
 /**
  * @author Ling
  *
  */
 public class PostServlet extends HttpServlet{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1484329906951015084L;
 
 	
 	
 	@Override
 	public void init() throws ServletException {
 		// TODO Auto-generated method stub
 		super.init();
 		
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		// TODO Auto-generated method stub
 //		super.doGet(req, resp);
 		doRequest(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		// TODO Auto-generated method stub
 //		super.doPost();
 		doRequest(req, resp);
 	}
 
 	protected void doRequest(HttpServletRequest request, HttpServletResponse response) 
 		throws IOException {
 		response.setContentType("text/html");
 		PrintWriter out = response.getWriter();
 		String username = request.getParameter("username");
 		String randomString = request.getParameter("random");
 		long pubDate = Long.parseLong(request.getParameter("pubdate"));
 		System.out.println("post parameters username = "+username + " random string = "+randomString
 				+ " pubDate = "+Constants.getTimeAsString(pubDate));
 		//call method to write to the DB
 		DatabaseHandler handler = new DatabaseHandler ();
		handler.insertRecordToDB(username, randomString, pubDate);
 		handler.closeConnection();
 	}
 }
