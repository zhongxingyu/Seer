 package se.starbox.controllers;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import se.starbox.models.SettingsModel;
 
 /**
  * Servlet implementation class SettingsController
  */
 @WebServlet(
 		description = "Handles local user settings",
 		urlPatterns = {
 				"/settings/",
 				"/settings"
 		}
 )
 public class SettingsController extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private SettingsModel sm;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public SettingsController() {
         super();
         // TODO Auto-generated constructor stub
         
         sm = new SettingsModel();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		request.setAttribute("displayName", sm.getDisplayName());
 		request.setAttribute("email", sm.getEmail());
 		request.setAttribute("starboxFolder", sm.getStarboxFolder());
 		request.setAttribute("indexUpdateInterval", sm.getIndexUpdateInterval());
 		request.setAttribute("ip", SettingsModel.getIP());
 		
 		RequestDispatcher view = request.getRequestDispatcher("/settings.jsp");
 		view.forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Map<String, String[]> params = request.getParameterMap();
 
 		
		if (params.containsKey("path")) {
			if (!sm.setStarboxFolder(params.get("path")[0]))
				response.sendError(404, "Invalid path");
		}
 		
 		if (params.containsKey("interval")) {
 			try {
 				int interval = Integer.parseInt(params.get("interval")[0]); //in minutes
 				if (interval > 0)
 					sm.setIndexUpdateInterval(interval);
 				
 				
 			} catch (NumberFormatException e) {
 				System.err.println("SettignsController - Incorrect interval given");
 			}
 		}
 		
 		if (params.containsKey("displayname"))
 			sm.setDisplayName(params.get("displayname")[0]);
 		
 		if (params.containsKey("email"))
 			sm.setEmail(params.get("email")[0]);
 
 		if (params.containsKey("updateindex"))
 			SettingsModel.updateIndex();
 
 		if (params.containsKey("shutdown"))
 			SettingsModel.shutDown();
 
 		
 		request.setAttribute("displayName", sm.getDisplayName());
 		request.setAttribute("email", sm.getEmail());
 		request.setAttribute("starboxFolder", sm.getStarboxFolder());
 		request.setAttribute("indexUpdateInterval", sm.getIndexUpdateInterval());
 		request.setAttribute("ip", SettingsModel.getIP());
 		
 		//RequestDispatcher view = request.getRequestDispatcher("/settings.jsp");
 		//view.forward(request, response);
 	}
 }
