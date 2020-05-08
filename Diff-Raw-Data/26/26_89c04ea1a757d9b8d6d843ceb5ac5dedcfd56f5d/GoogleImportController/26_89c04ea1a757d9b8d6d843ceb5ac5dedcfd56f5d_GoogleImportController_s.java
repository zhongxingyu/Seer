 package com.aavu.server.web.controllers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.aavu.server.service.TheGoogleService;
 import com.aavu.server.service.UserService;
 import com.google.gdata.client.http.AuthSubUtil;
 import com.google.gdata.util.AuthenticationException;
 
 public class GoogleImportController extends BasicController {
 	private static final Logger log = Logger.getLogger(GoogleImportController.class);
 
 
 	private TheGoogleService googleService;
 
 	private String formView;
 
 
 	private Map<String, Object> getModelForMessage(String message) {
 		Map<String, Object> rtn = new HashMap<String, Object>();
 		rtn.put("message", message);
 		return rtn;
 	}
 
 	@Override
 	protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse arg1)
 			throws Exception {
 
 		String onetimeUseToken = AuthSubUtil.getTokenFromReply(req.getQueryString());
 
		googleService.importDocsForToken(onetimeUseToken);
 
 		try {
 
			int found = googleService.importDocsForToken(onetimeUseToken);
 
 			String successStr = "Found "
 					+ found
 					+ " documents. Importing in the background, it may take a bit before they are all available.";
 
 			return new ModelAndView(getView(), "message", successStr);
 		} catch (AuthenticationException e) {
 
 			return new ModelAndView(formView, getModelForMessage("Problem Logging In "
 					+ e.getMessage()));
 		}
 
 
 	}
 
 	@Required
 	public void setFormView(String formView) {
 		this.formView = formView;
 	}
 
 	@Required
 	public void setUserService(UserService userService) {
 		this.userService = userService;
 	}
 
 	@Required
 	public void setGoogleService(TheGoogleService googleService) {
 		this.googleService = googleService;
 	}
 
 
 
 }
