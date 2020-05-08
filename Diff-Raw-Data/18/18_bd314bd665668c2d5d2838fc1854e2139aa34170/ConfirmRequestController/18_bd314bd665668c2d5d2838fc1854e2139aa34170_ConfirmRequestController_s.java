 package edu.unsw.triangle.web;
 
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.unsw.triangle.controller.Controller;
 import edu.unsw.triangle.controller.ModelView;
 import edu.unsw.triangle.service.RegisterService;
 import edu.unsw.triangle.util.Errors;
 
 public class ConfirmRequestController implements Controller
 {
 	private final Logger logger = Logger.getLogger(ConfirmRequestController.class.getName());
 	
 	@Override
 	public ModelView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception 
 	{
 		// Request parameters
 		String username = request.getParameter("username");
 		String code = request.getParameter("code");
 		Errors errors = new Errors();
 		ModelView modelView = new ModelView(getFormView()).addModel("errors", errors).forward();
 		
 		if (username == null || code == null)
 		{
 			// No valid parameters
 			errors.rejectValue("confirm.error", "invalid confirmation url");
 			logger.warning("invalid confirmation url");
 			return modelView;
 		}
 		
 		try
 		{	
 			if (RegisterService.activate(username, code))
 			{
 				logger.info("user: " + username + " has been activated");
 			}
 			else
 			{
 				errors.rejectValue("confirm.error", "failed to activate user with url");
 			}
 		}
 		catch (Exception e)
 		{
 			errors.rejectValue("confirm.error", "failed to activate user reason: " + e.getMessage());
 			logger.warning("confirm.error failed to activate user reason: " + e.getMessage());
 			e.printStackTrace();
 		}
 		
 		return modelView;
 	}
 
 	@Override
 	public String getFormView() 
 	{
 		return "confirm.view";
 	}
 }
