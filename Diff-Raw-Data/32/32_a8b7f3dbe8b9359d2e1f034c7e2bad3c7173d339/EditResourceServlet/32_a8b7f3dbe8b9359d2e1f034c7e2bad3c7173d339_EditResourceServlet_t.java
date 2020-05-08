 package com.admin.servlet;
 
 import java.io.IOException;
import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.admin.owlmanager.OntologyManager;
 
 /**
  * Servlet implementation class EditResourceServlet
  */
 @WebServlet("/EditResourceServlet")
 public class EditResourceServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public EditResourceServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		this.doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String action = request.getParameter("action");
 		String resource = request.getParameter("resource");
 		
 		if (action.equals("delete_resource")) {
 			OntologyManager om = new OntologyManager();
 			boolean result = om.deleteResource(resource);
 			if (result) {
				String message = URLEncoder.encode("Resource deleted", "UTF-8");
				String link = "ConfirmPage.jsp?message=" + message + "&link=ViewResources.jsp";
				response.sendRedirect(link);
 			} else {
				String message = URLEncoder.encode("Couldn't delete the requested resource", "UTF-8");
				String link = "ErrorPage.jsp?message=" + message + "?link=javascript:window.history.back();";
				response.sendRedirect(link);
 			}
 		} else if (action.equals("delete_annotations")) {
 			Enumeration<String> paramNames = request.getParameterNames();
 			OntologyManager om = new OntologyManager();
 			ArrayList<String> annotations = new ArrayList<String>();
 			
 			while (paramNames.hasMoreElements()) {
 				String current = paramNames.nextElement();
 				if (current.startsWith("http://")) {
 					annotations.add(current);
 				}
 			}
 			
 			boolean result = om.deleteResourceAnnotations(resource, annotations);
 			if (result) {
				String message = URLEncoder.encode("Annotations deleted", "UTF-8");
				String link = "ConfirmPage.jsp?message=" + message + "&link=ViewResources.jsp";
				response.sendRedirect(link);
 			} else {
				String message = URLEncoder.encode("Couldn't delete the requested annotations", "UTF-8");
				String link = "ErrorPage.jsp?message=" + message + "&link=javascript:window.history.back();";
				response.sendRedirect(link);
 			}
 		} else if (action.equals("add_annotations")) {
 			response.sendRedirect("PreAddResource.jsp?resource=" + resource);
 		}
 	}
 }
