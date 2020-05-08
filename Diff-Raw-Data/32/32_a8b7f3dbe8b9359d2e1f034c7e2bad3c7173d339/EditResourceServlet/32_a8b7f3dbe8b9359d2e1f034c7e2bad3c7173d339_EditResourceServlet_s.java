 package com.admin.servlet;
 
 import java.io.IOException;
import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Enumeration;
 
import javax.servlet.RequestDispatcher;
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
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
 		
 		if (action.equals("delete_resource")) {
 			OntologyManager om = new OntologyManager();
 			boolean result = om.deleteResource(resource);
 			if (result) {
				writer.write("OK");
 			} else {
				writer.write("error");
 			}
 		} else if (action.equals("delete_annotations")) {
			writer.write("Borraremos las anotaciones: <br />");
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
				writer.write("OK");
 			} else {
				writer.write("error");
 			}
 		} else if (action.equals("add_annotations")) {
 			response.sendRedirect("PreAddResource.jsp?resource=" + resource);
 		}
		
		writer.close();
 	}
 }
