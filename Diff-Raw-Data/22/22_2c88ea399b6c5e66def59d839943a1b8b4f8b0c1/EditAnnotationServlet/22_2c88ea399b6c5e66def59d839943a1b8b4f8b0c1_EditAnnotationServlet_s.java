 package com.admin.servlet;
 
 import java.io.IOException;
import java.io.PrintWriter;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.admin.owlmanager.OntologyManager;
 
 /**
  * Servlet implementation class EditAnnotationServlet
  */
 @WebServlet("/EditAnnotationServlet")
 public class EditAnnotationServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final String THESIS_PREFIX = "http://localhost/ontologies/ThesisOntology.owl#";
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public EditAnnotationServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		this.doPost(request, response);
 	}
 
 	
 	private ArrayList<String[]> unpackProperties(HttpServletRequest request) {
 		ArrayList<String[]> result = new ArrayList<String[]>();
 		Iterator<String> iterator = request.getParameterMap().keySet().iterator();
 		while (iterator.hasNext()) {
 			String current = iterator.next();
 			if (current.startsWith(THESIS_PREFIX)) {
 				String value = request.getParameter(current);
 				String[] addToResult = {current, value};
 				result.add(addToResult);
 			}
 		}
 		return result;
 	}
 	
 	private boolean deleteAnnotationProperties(String annotation, ArrayList<String[]> properties) {
 		boolean result = true;
 		OntologyManager manager = new OntologyManager();
 		
 		result = manager.removeAnnotationProperties(annotation, properties);
 		for (int i = 0; i < properties.size(); i++) {
 			String[] current = properties.get(i);
 			System.out.println(current[0] + "   " + current[1]);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String action = request.getParameter("action");
 		String annotationName = request.getParameter("annotation");
 		boolean result = true;
		response.setContentType("text/html");
		
		PrintWriter writer = response.getWriter();
 		
 		if (action.equals("delete_annotation")) {
 			OntologyManager manager = new OntologyManager();
 			result = result && manager.deleteResource(annotationName);
 		} else if (action.equals("delete_properties")) {
 			ArrayList<String[]> properties = this.unpackProperties(request);
 			this.deleteAnnotationProperties(annotationName, properties);
			writer.println("A Borrar propiedades");
 		} else if (action.equals("add_properties")) {
 			response.sendRedirect("AddAnnotation.jsp?edit=" + URLEncoder.encode(annotationName, "UTF-8"));
 		}
 		
 		if (result) {
			writer.println("SUCCESS");
 		} else {
			writer.write("ERROR");
 		}
		
		writer.close();
 	}
 }
