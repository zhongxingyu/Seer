 package com.pace.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 public class PafInitServlet extends HttpServlet {
 
 	Logger logger = Logger.getLogger(PafInitServlet.class);
 	/**
 	 * Constructor of the object.
 	 */
 	public PafInitServlet() {
 		super();
 	}
 
 	/**
 	 * Destruction of the servlet. <br>
 	 */
 	public void destroy() {
 		super.destroy(); // Just puts "destroy" string in log
 		// Put your code here
 	}
 
 	/**
 	 * The doGet method of the servlet. <br>
 	 *
 	 * This method is called when a form has its tag value method equals to get.
 	 * 
 	 * @param request the request send by the client to the server
 	 * @param response the response send by the server to the client
 	 * @throws ServletException if an error occurred
 	 * @throws IOException if an error occurred
 	 */
 	@SuppressWarnings("unused")
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 
 		response.setContentType("text/html");
 		PrintWriter out = response.getWriter();
 		out
 				.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
 		out.println("<HTML>");
 		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
 		out.println("  <BODY>");
 		out.print("    This is ");
 		out.print(this.getClass());
 		out.println(", using the GET method");
 		out.println("  </BODY>");
 		out.println("</HTML>");
 		out.flush();
 		out.close();
 	}
 
 	/**
 	 * The doPost method of the servlet. <br>
 	 *
 	 * This method is called when a form has its tag value method equals to post.
 	 * 
 	 * @param request the request send by the client to the server
 	 * @param response the response send by the server to the client
 	 * @throws ServletException if an error occurred
 	 * @throws IOException if an error occurred
 	 */
 	@SuppressWarnings("unused")
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 
 		response.setContentType("text/html");
 		PrintWriter out = response.getWriter();
 		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
 		out.println("<HTML>");
 		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
 		out.println("  <BODY>");
 		out.print("    This is ");
 		out.print(this.getClass());
 		out.println(", using the POST method");
 		out.println("  </BODY>");
 		out.println("</HTML>");
 		out.flush();
 		out.close();
 	}
 
 	/**
 	 * Initialization of the servlet. <br>
 	 *
 	 * @throws ServletException if an error occure
 	 */
 	@SuppressWarnings("unused")
 	public void init() throws ServletException {
 		logger.debug("Initialization Servlet Called");
         logger.info("--------------------------------------------------");       
         logger.info("Starting Pace Application Framework Server.");
         logger.info("Server Version: " + PafServerConstants.SERVER_VERSION);
         logger.info("--------------------------------------------------");
 		new PafMetaData();
 		PafDataService.getInstance();
 		PafViewService.getInstance();
 
 	}
 
 }
