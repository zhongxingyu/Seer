 package edu.unsw.triangle.web;
 
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.unsw.triangle.controller.AbstractFormController;
 import edu.unsw.triangle.controller.ModelView;
 import edu.unsw.triangle.data.ConnectionManager;
 import edu.unsw.triangle.data.DerbyDaoManager;
 import edu.unsw.triangle.model.Profile;
 import edu.unsw.triangle.model.WebSession;
 import edu.unsw.triangle.util.Errors;
 import edu.unsw.triangle.util.Messages;
 import edu.unsw.triangle.util.ProfileValidator;
 import edu.unsw.triangle.util.Validator;
 import edu.unsw.triangle.view.ProfileBinder;
 import edu.unsw.triangle.view.RequestBinder;
 
 public class ProfileFormController extends AbstractFormController 
 {
 	private final Logger logger = Logger.getLogger(ProfileFormController.class.getName());
 	
 	@Override
 	public String getFormView() 
 	{
 		return "profile.view";
 	}
 
 	@Override
 	protected Object createBackingObject(HttpServletRequest request) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	
 	
 	@Override
 	public ModelView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		ModelView modelView =  super.handleRequest(request, response);
		// If modelView is successful, then update websession with new profile
 		Errors errors = (Errors)modelView.getModel("errors");
 		if (errors == null)
 		{
 			logger.info("Updating websession with profile");
			WebSession websession = (WebSession)request.getSession().getAttribute("websession");
 			websession.setProfile((Profile)modelView.getModel("profile"));
 		}
 		return modelView;
 	}
 
 	@Override
 	protected ModelView handleFormSubmit(Object command) 
 	{
 		logger.info("handling profile form submit request");
 		Profile profile = (Profile) command;
 		ModelView modelView = null;
 		try 
 		{
 			DerbyDaoManager derbyManager = new DerbyDaoManager(ConnectionManager.getInstance());
 			
 			// Update profile in repository
 			derbyManager.getProfileDao().update(profile);
 			logger.info("username " + profile.getUsername() + " is successfully updated");
 			Messages messages = new Messages().add("profile.success", "Profile Updated");
 			modelView = new ModelView(getSuccessView()).forward().addModel("profile", command).
 					addModel("messages", messages);
 			derbyManager.close();
 		} 
 		catch (Exception e)
 		{
 			logger.severe("failed to update profile for " + profile.getUsername() + " reason " + e.getMessage());
 			e.printStackTrace();
 			Errors errors = new Errors().rejectValue("profile.error", "failed to update profile reason " + e.getMessage());
 			modelView = handleFormError(profile, errors);
 		}
 		
 		return modelView;
 	}
 
 	@Override
 	protected Validator getValidator() 
 	{
 		return new ProfileValidator();
 	}
 
 	@Override
 	protected RequestBinder getBinder() 
 	{
 		return new ProfileBinder();
 	}
 
 	@Override
 	protected String getSuccessView() 
 	{
 		return "profile.view";
 	}
 
 	@Override
 	protected ModelView handleFormError(Object command, Errors errors) 
 	{
 		ModelView modelView = new ModelView(getFormView()).forward().
 				addModel("profile.error", command).addModel("errors", errors);
 		return modelView;
 	}
 
 }
