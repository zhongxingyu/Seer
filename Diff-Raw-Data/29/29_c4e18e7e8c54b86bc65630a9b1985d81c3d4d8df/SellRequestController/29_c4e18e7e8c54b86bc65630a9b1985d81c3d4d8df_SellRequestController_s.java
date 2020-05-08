 package edu.unsw.triangle.web;
 
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.unsw.triangle.controller.Controller;
 import edu.unsw.triangle.controller.ModelView;
 import edu.unsw.triangle.model.WebSession;
 import edu.unsw.triangle.util.Errors;
 
 public class SellRequestController implements Controller {
 
 	private final Logger logger = Logger.getLogger(SellRequestController.class.getName());
 	
 	@Override
 	public ModelView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception 
 	{
 		// Guard against admin
 		WebSession websession = (WebSession) request.getSession().getAttribute("websession");
 		if (websession.getProfile().isAdmin())
 		{
 			logger.warning("admin user: " + websession.getUsername() + " cannot access sell page");
 			Errors errors = new Errors().rejectValue("error", "admin cannot access sell page");
 			ModelView modelView = new ModelView("error.view").forward().addModel("errors", errors);
 			return modelView;
 		}
 		ModelView modelView = new ModelView(getFormView()).forward();
 		return modelView;
 	}
 
 	@Override
 	public String getFormView() 
 	{
 		return "sell.view";
 	}
 
 	@Override
	public void handleSession(HttpServletRequest request) {
		// TODO Auto-generated method stub
		
 	}
 
 }
