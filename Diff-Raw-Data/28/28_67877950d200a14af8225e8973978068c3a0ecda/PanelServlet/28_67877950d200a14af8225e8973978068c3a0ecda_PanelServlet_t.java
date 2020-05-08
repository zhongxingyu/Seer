 package solar;
 
 import components.Panel;
 import components.PanelController;
 import exceptions.PanelException;
 
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import com.google.appengine.labs.repackaged.org.json.*;
 
 public class PanelServlet extends HttpServlet {
 	
 	private static final long serialVersionUID = 1L;
 	private static final Logger log = Logger.getLogger(PanelServlet.class.getName());
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		String reqOption = request.getParameter("option");
 		JSONObject returnJsonObj = new JSONObject();
 		
 		if (reqOption.equals("addPanel")) {		
 			String manufacturer = "";
 			String model = "";
			float power = 0;
 			
 			boolean validInput = false;
 	
 			try {
 				manufacturer = request.getParameter("manufacturer");
 				model = request.getParameter("model");
				power = Float.parseFloat(request.getParameter("power"));			
 				
 				validInput = true;
 			}
 			catch (NumberFormatException e1) {
 				try {
 					returnJsonObj.put("Success", false);
 				}
 				catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			if (validInput) {				
 				// create new Panel
 				try {					
 					PanelController ctrl = new PanelController();
 					Panel p = new Panel(manufacturer, model, power);
 					returnJsonObj = ctrl.addPanel(p);
 					
 				}
 				catch (PanelException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}			
 			else {				
 				try {
 					returnJsonObj.put("Success", false);
 				}
 				catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}				
 			}		
 		}
 		else if (reqOption.equals("getPanels")) {
 			PanelController ctrl = new PanelController();
 			returnJsonObj = ctrl.getPanels();
 		}
 		else if (reqOption.equals("getPanelManufacturers")) {
 			PanelController ctrl = new PanelController();
 			returnJsonObj = ctrl.getPanelManufacturers();
 		}
 		else if (reqOption.equals("getPanelModels")) {
 			String manufacturer = request.getParameter("manufacturer");
 			PanelController ctrl = new PanelController();
 			returnJsonObj = ctrl.getPanelModels(manufacturer);
 		}
 		else if (reqOption.equals("getPanelPower")) {
 			String model = request.getParameter("model");
 			PanelController ctrl = new PanelController();
 			returnJsonObj = ctrl.getPanelPower(model);
 		}
 		else if (reqOption.equals("deletePanel")) {
 			String model = request.getParameter("model");
 			PanelController ctrl = new PanelController();
 			returnJsonObj = ctrl.deletePanel(model);
 		}
 		
 		response.setContentType("application/json");
 		response.getWriter().write(returnJsonObj.toString());
 		
 		log.log(Level.WARNING, returnJsonObj.toString());
 	}
 }
