 package com.admin.servlet;
 
 import java.io.IOException;
import java.io.PrintWriter;
 import java.net.URLEncoder;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.admin.owlmanager.OntologyManager;
 
 /**
  * Servlet implementation class AddResourceServlet
  */
 @WebServlet("/AddResourceServlet")
 public class AddResourceServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private static final String CONCEPT = "concept";
 	private static final String TOPIC = "topic";
 	private static final String UNIT = "unit";
 	private static final String KNOWLEDGE_AREA = "knowledgearea";
 	private static final String DISCIPLINE = "discipline";
 	private static final String ENTITY = "entity";
 	private static final String TOOL = "tool";
 	private static final String ENTERPRISE = "enterprise";
 	private static final String TEAM = "team";
 	private static final String PERSON = "person";
 	private static final String URL = "uri";
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AddResourceServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	/**
 	 * Unpacks the values to be added as annotation
 	 * to the target resource
 	 * @param map the request's parameter map
 	 * @return the String[] of values to be added
 	 */
 	private String[] unpackValues(Map<String, String[]> map) {
 		
 		Set<String> keys = map.keySet();
 		String[] result = null;
 		
 		if ((keys.contains(CONCEPT)) && !(map.get(CONCEPT)[0].equalsIgnoreCase("invalid") || (map.get(CONCEPT)[0].equalsIgnoreCase("noselect")))) {
 			result = map.get(CONCEPT);
 		} else if ((keys.contains(TOPIC)) && !(map.get(TOPIC)[0].equalsIgnoreCase("invalid") || (map.get(TOPIC)[0].equalsIgnoreCase("noselect")))) {
 			result = map.get(TOPIC);
 		} else if ((keys.contains(UNIT)) && !(map.get(UNIT)[0].equalsIgnoreCase("invalid") || (map.get(UNIT)[0].equalsIgnoreCase("noselect"))))  {
 			result = map.get(UNIT);
 		} else if ((keys.contains(KNOWLEDGE_AREA)) && !(map.get(KNOWLEDGE_AREA)[0].equalsIgnoreCase("invalid") || (map.get(KNOWLEDGE_AREA)[0].equalsIgnoreCase("noselect")))) {
 			result = map.get(KNOWLEDGE_AREA);
 		} else if ((keys.contains(DISCIPLINE)) && !(map.get(DISCIPLINE)[0].equalsIgnoreCase("invalid") || (map.get(DISCIPLINE)[0].equalsIgnoreCase("noselect")))) {
 			result = map.get(DISCIPLINE);
 		} else {
 			if ((keys.contains(ENTITY)) && !(map.get(ENTITY)[0].equalsIgnoreCase("invalid") || (map.get(ENTITY)[0].equalsIgnoreCase("noselect")))) {
 				result = map.get(ENTITY);
 			} else if ((keys.contains(TOOL)) && !(map.get(TOOL)[0].equalsIgnoreCase("invalid") || (map.get(TOOL)[0].equalsIgnoreCase("noselect")))) {
 				result = map.get(TOOL);
 			} else if ((keys.contains(TEAM)) && !(map.get(TEAM)[0].equalsIgnoreCase("invalid") || (map.get(TEAM)[0].equalsIgnoreCase("noselect")))) {
 				result = map.get(TEAM);
 			} else if ((keys.contains(PERSON)) && !(map.get(PERSON)[0].equalsIgnoreCase("invalid") || (map.get(PERSON)[0].equalsIgnoreCase("noselect")))) {
 				result = map.get(PERSON);
 			} else if ((keys.contains(ENTERPRISE)) && !(map.get(ENTERPRISE)[0].equalsIgnoreCase("invalid") || (map.get(ENTERPRISE)[0].equalsIgnoreCase("noselect")))) {
 				result = map.get(ENTERPRISE);
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Validates if the resource URL is a valid URL
 	 * @param map the request parameters map
 	 * @return the valid URL or null us URL is invalid
 	 */
 	private String validateResourceURI(Map<String, String[]> map) {
 		String result = null;
 		Set<String> keys = map.keySet();
 		if ((keys.contains(URL)) && (!map.get(URL)[0].isEmpty())) {
 			String possibleURI = map.get(URL)[0];
 			if (possibleURI.startsWith("http://")) {
 				result = possibleURI;
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Map<String, String[]> map = request.getParameterMap();
 		response.setContentType("text/html");
 		String uri = this.validateResourceURI(map);
 		
 		if (uri != null) {
 			String[] selectedAnotations = this.unpackValues(map);
 			if (selectedAnotations == null) {
 				String message = URLEncoder.encode("You must select at least one annotation for the resource", "UTF-8");
 				String link = "ErrorPage.jsp?message=" + message + "&link=javascript:window.history.back();";
 				response.sendRedirect(link);
 			} else {
 				OntologyManager om = new OntologyManager();
 				boolean result = om.addResource(uri, selectedAnotations);
 				if (result) {
 					String message = URLEncoder.encode("Resource added successfully", "UTF-8");
 					String link = "ConfirmPage.jsp?message=" + message + "&link=PreAddResource.jsp";
 					response.sendRedirect(link);
 					
 				} else {
 					String message = URLEncoder.encode("There were errors while adding the resource", "UTF-8");
 					String link = "ErrorPage.jsp?message=" + message + "&link=PreAddResource.jsp";
 					response.sendRedirect(link);
 				}
 			}
 		} else {
 			String message = URLEncoder.encode("Invalid URL: must start with http://", "UTF-8");
 			String link = "ErrorPage.jsp?message=" + message + "&link=javascript:window.history.back();";
 			response.sendRedirect(link);
 		}
 	}
 }
