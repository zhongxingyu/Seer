 package com.admin.servlet;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.admin.owlmanager.OntologyManager;
 
 /**
  * Servlet implementation class AddAnnotationServlet
  */
 @WebServlet("/AddAnnotationServlet")
 public class AddAnnotationServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private static final String THESIS_PREFIX = "http://localhost/ontologies/ThesisOntology.owl#";
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AddAnnotationServlet() {
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
 		String annotationName = THESIS_PREFIX + request.getParameter("annotation");
 		String className = request.getParameter("class");
 		
 		OntologyManager manager = new OntologyManager();
 		boolean annotationAdd = true;
 		
 		if (!className.equals("noselect")) {
 			annotationAdd = annotationAdd && manager.addAnnotationURI(annotationName, className);
 		} else {
 			annotationAdd = false;
 		}
 		if (annotationAdd) {
 			if (className.endsWith("Concept")) {
 				String topic = request.getParameter("in-topic");
 				String relationType = request.getParameter("conceptrelation");
 				String relatedConcept = null;
 				if (!relationType.equals("noselect")) {
 					relatedConcept = request.getParameter("related");
 					annotationAdd = annotationAdd && manager.addTriplet(annotationName, THESIS_PREFIX + relationType, relatedConcept);
 				}
 				if ( (!topic.equals("noselect")) && (annotationAdd) ) {
 					annotationAdd = annotationAdd && manager.addTriplet(annotationName, THESIS_PREFIX + "in-topic", topic);
 				} else {
 					String message = URLEncoder.encode("There were errors adding the annotation", "UTF-8");
 					String link = "ErrorPage.jsp?message=" + message + "?link=javascript:window.history.back();";
 					manager.deleteResource(annotationName);
 					response.sendRedirect(link);
 				}
 			} else {
 				Map<String, String[]> values = request.getParameterMap();
 				Set<String> keySet = values.keySet();
 				Iterator<String> iter = keySet.iterator();
 				while (iter.hasNext()) {
 					String currentKey = iter.next();
 					if ( (!currentKey.equals("class")) && (!currentKey.equals("annotation")) ) {
 						String relation = THESIS_PREFIX + currentKey;
 						String[] objects = values.get(currentKey);
 						for (int i = 0; i < objects.length; i++) {
 							if (!objects[i].equals("noselect")) {
 								if (annotationAdd) {
 									annotationAdd = annotationAdd && manager.addTriplet(annotationName, relation, objects[i]);
 								} else {
 									String message = URLEncoder.encode("There were errors adding the annotation", "UTF-8");
 									String link = "ErrorPage.jsp?message=" + message + "&link=javascript:window.history.back();";
 									manager.deleteResource(annotationName);
 									response.sendRedirect(link);
 								}
 							}
 						}
 					}
 				}
 			}
 		} else {
 			String message = URLEncoder.encode("There were errors adding the annotation", "UTF-8");
 			String link = "ErrorPage.jsp?message=" + message + "&link=javascript:window.history.back();";
 			manager.deleteResource(annotationName);
 			response.sendRedirect(link);
 		}
 		String message = URLEncoder.encode("Annotation added", "UTF-8");
		String link = "ConfirmPage.jsp?message=" + message + "&link=index.html";
 		response.sendRedirect(link);
 	}
 }
