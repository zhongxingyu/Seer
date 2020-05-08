 package de.seco.serp;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import de.seco.serp.controller.*;
 
 public class MainServlet extends HttpServlet {
 
 	// route request to controller
 	protected void route(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		String requestUri = request.getRequestURI();
 		if (requestUri.startsWith("/")) {
 			requestUri = requestUri.substring(1);
 		}
 		String[] uriParts = requestUri.split("/");
 		
 		BaseController controller = null;
 		
 		try {
 			String actionName = "";
 			
 			if (uriParts.length < 2) {
 				System.out.println ("non-api: " + requestUri);
				request.getRequestDispatcher("login.jsp").forward(request, response);
 //				PrintWriter out = response.getWriter(); 
 //				out.write("test");
 //				out.close();
 				return;
 			}
 			else {
 				actionName = uriParts[1];
 				
 				// api controller
 				if (uriParts[0].equals("api")) {
 					System.out.println("Api controller...");
 					controller = new ApiController();
 				}
 			}
 			
 			controller.invoke(actionName, request, response);
 		}
 		
 		catch (Exception e) {
 			System.out.println("cannot route: " + e.getMessage());
 			e.printStackTrace();
 		}
 		
 	}
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	    route(request, response);
 	}
 	 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	    route(request, response);
 	}
 	    
 }
