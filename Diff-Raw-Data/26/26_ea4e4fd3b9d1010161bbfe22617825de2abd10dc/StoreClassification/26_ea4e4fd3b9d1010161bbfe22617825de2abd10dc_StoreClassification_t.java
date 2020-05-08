 package uk.ac.kmi.server;
 
 import java.io.*;
 import java.net.URL;
 import java.util.*;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import uk.ac.kmi.server.store.URI;
 import uk.ac.kmi.server.store.URIImpl;
 import uk.ac.kmi.server.store.InitializeStore;
 
 /**
  * Servlet implementation class StoreClassification
  */
 public class StoreClassification extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private InitializeStore storeInit = null;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public StoreClassification() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		//processRequestOld(request, response);
 		processRequest(request, response);
 	}
 
 	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/html");
 		HttpSession session = request.getSession(true);
 	    //PrintWriter out = response.getWriter();
 	    //showVariablesInHTML(request, out);
 	    
 	    String processId = "http://www.kmi.open.ac.uk/survey/sessionLost";
 		String documentUri = "http://www.kmi.open.ac.uk/apiUri/sessionLost";
 		URI sessionId = new uk.ac.kmi.server.store.URIImpl("http://www.kmi.open.ac.uk/session/sessionLost");
 		long currentTime = System.currentTimeMillis();
 		
 		
 		//addFormData(URI apiUri, String surveyId, URI userId, HashMap<String, Vector<String>> parameters)
 		
 		if (session != null ){
 			processId = (String) session.getAttribute("processId");
 	    	documentUri = (String) session.getAttribute("apiURL");
 	    	if( processId == null || processId.trim() == "" ){
 				 processId = "http://www.kmi.open.ac.uk/survey/sessionEmpty" +currentTime;
 			 }
 			 if( documentUri == null || documentUri.trim() == "" ){
 				 documentUri= "http://www.kmi.open.ac.uk/apiUri/sessionEmpty" + currentTime;
 			 }
 	    	sessionId = new URIImpl("http://www.kmi.open.ac.uk/session/"+ session.getId());
	    	
	    	//request.setAttribute("nextURI",session.getAttribute("nextURI"));
 		}
 	    
 		initializeStore();
 		
 		HashMap<String, String[]> parameters = new HashMap<String, String[]>();
 		
 		Enumeration paramNames = request.getParameterNames();
 	    while(paramNames.hasMoreElements()) {
 	      String paramName = (String)paramNames.nextElement();
 	      String[] paramValues = request.getParameterValues(paramName);
 	      
 	      if (paramValues.length > 0) {
 		      parameters.put(paramName, paramValues);
 	      } else {
 		      parameters.put(paramName, new String[]{request.getParameter(paramName)});
 	      }
 	    } // while
 	    
	    /*if(parameters.containsKey("user")){
	    	session.setAttribute("user", parameters.get("user")[0]);
	    }*/
	    
 	    URIImpl URI = new URIImpl(documentUri);
	    //String newSurveyEntryId = 
 	    storeInit.addFormData(URI, processId, sessionId, parameters);
 	    
	    //request.setAttribute("newSurveyEntryId",newSurveyEntryId);
	    
 	    //request.setAttribute("URI", URI.toString());
	    //request.getRequestDispatcher("/success.jsp").forward(request, response);
 	    
 	   //Only for classify
	    request.getRequestDispatcher("redirect.jsp").forward(request, response);
 	  }
 
 	
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void processRequestOld(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/html");
 		HttpSession session = request.getSession(true);
 	    PrintWriter out = response.getWriter();
 	    
 	    showVariablesInHTML(request, out);
 	    
 	    String processId = "http://www.kmi.open.ac.uk/survey/sessionLost";
 		String documentUri = "http://www.kmi.open.ac.uk/apiUri/sessionLost";
 		URI sessionId = new uk.ac.kmi.server.store.URIImpl("http://www.kmi.open.ac.uk/session/sessionLost");
 		long currentTime = System.currentTimeMillis();
 		
 		if (session != null ){
 			processId = (String) session.getAttribute("processId");
 	    	documentUri = (String) session.getAttribute("apiURL");
 	    	if( processId == null || processId.trim() == "" ){
 				 processId = "http://www.kmi.open.ac.uk/survey/sessionEmpty" +currentTime;
 			 }
 			 if( documentUri == null || documentUri.trim() == "" ){
 				 documentUri= "http://www.kmi.open.ac.uk/apiUri/sessionEmpty" + currentTime;
 			 }
 	    	sessionId = new URIImpl("http://www.kmi.open.ac.uk/session/"+ session.getId());
 		}
 	    
 		initializeStore();
 		
 		Enumeration paramNames = request.getParameterNames();
 	    while(paramNames.hasMoreElements()) {
 	      String paramName = (String)paramNames.nextElement();
 	      out.println("<TR><TD>" + paramName + "\n<TD>");
 	      String[] paramValues = request.getParameterValues(paramName);
 	      
 	      for(int i=0; i<paramValues.length; i++) {
 	    	  storeInit.addClassifiedItem(new URIImpl(documentUri), processId, sessionId, paramValues[i]);
 	      }
 	      
 	    }
 
 	  }
 
 	@SuppressWarnings("unchecked")
 	private void showVariablesInHTML(HttpServletRequest request, PrintWriter out) {
 		String title = "Selected Classification";
 	    out.println(
 	                "<BODY BGCOLOR=\"#FDF5E6\">\n" +
 	                "<H1 ALIGN=CENTER>" + title + "</H1>\n" +
 	                "<TABLE BORDER=1 ALIGN=CENTER>\n" +
 	                "<TR BGCOLOR=\"#FFAD00\">\n" +
 	                "<TH>Parameter Name<TH>Parameter Value(s)");
 	    Enumeration paramNames = request.getParameterNames();
 	    while(paramNames.hasMoreElements()) {
 	      String paramName = (String)paramNames.nextElement();
 	      out.println("<TR><TD>" + paramName + "\n<TD>");
 	      String[] paramValues = request.getParameterValues(paramName);
 	      if (paramValues.length == 1) {
 	        String paramValue = paramValues[0];
 	        if (paramValue.length() == 0)
 	          out.print("<I>No Value</I>");
 	        else
 	          out.print(paramValue);
 	      } else {
 	        out.println("<UL>");
 	        for(int i=0; i<paramValues.length; i++) {
 	          out.println("<LI>" + paramValues[i]);
 	        }
 	        out.println("</UL>");
 	      }
 	    }
 	    out.println("</TABLE>\n</BODY></HTML>");
 	}
 
 	
 	private void initializeStore() {
 		if(storeInit ==null)
 			storeInit = new InitializeStore();
 	}
 
 }
