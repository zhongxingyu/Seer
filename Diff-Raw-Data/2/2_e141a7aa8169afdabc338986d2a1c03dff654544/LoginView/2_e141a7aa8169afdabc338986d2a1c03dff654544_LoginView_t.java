 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.gui.impl.servlets;
 
 import java.util.Dictionary;
 
 import javax.servlet.Servlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.apache.velocity.tools.view.CookieTool;
 import org.osgi.service.component.ComponentContext;
 import org.osgi.service.http.HttpContext;
 import org.osgi.service.useradmin.Group;
 import org.osgi.service.useradmin.Role;
 import org.osgi.service.useradmin.User;
 import org.osgi.service.useradmin.UserAdmin;
 import org.paxle.gui.ALayoutServlet;
 import org.paxle.gui.impl.HttpAuthManager;
 import org.paxle.gui.impl.IHttpAuthManager;
 import org.paxle.gui.impl.ServiceManager;
 
 @Component(metatype=false, immediate=true)
 @Service(Servlet.class)
 @Properties({
 	@Property(name="org.paxle.servlet.path", value="/login"),
 	@Property(name="org.paxle.servlet.doUserAuth", boolValue=false)
 })
 public class LoginView extends ALayoutServlet {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Reference
 	protected UserAdmin userAdmin;
 	
 	@Reference
 	private IHttpAuthManager guiAuthManager;
 	
 	/**
 	 * This function is called the OSGI DS during component activation 
 	 * @param context
 	 */	
 	protected void activate(ComponentContext context) {
 		// check if an Administrator group is already available
		Group admins = (Group) userAdmin.getRole("Administrators")
 		if (admins == null) {
 			admins = (Group) userAdmin.createRole("Administrators",Role.GROUP);
 		}
 
 		User admin = (User) userAdmin.getRole("Administrator");
 		if (admin == null) {
 			// create a default admin user
 			admin = (User) userAdmin.createRole("Administrator", Role.USER);
 			admins.addMember(admin);
 
 			// configure http-login data
 			@SuppressWarnings("unchecked")
 			Dictionary<String, Object> props = admin.getProperties();
 			props.put(HttpAuthManager.USER_HTTP_LOGIN, "admin");
 
 			@SuppressWarnings("unchecked")
 			Dictionary<String, Object> credentials = admin.getCredentials();
 			credentials.put(HttpAuthManager.USER_HTTP_PASSWORD, "");
 		}
 	}
 		
 	@Override
 	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {
         Template template = null;
 
         try {
     		// Get the session
     		HttpSession session = request.getSession(true);
         	boolean doRedirect = false;
     		
         	if (request.getParameter("doLogin") != null) {
         		// getting user-name + password
         		String userName = request.getParameter("login.username");
         		String password = request.getParameter("login.password");
         		if (password == null) password = "";
         		
         		// getting the userAdmin service
         		ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
         		UserAdmin uAdmin = (UserAdmin) manager.getService(UserAdmin.class.getName());
         		
         		// auth user
         		final User user = this.guiAuthManager.authenticatedAs(request, userName, password);        		
         		if (user != null){
         			// remember login state
         			session.setAttribute("logon.isDone", Boolean.TRUE);
         			
     				// set user-data into the session
     				session.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.FORM_AUTH);
     				session.setAttribute(HttpContext.AUTHORIZATION, uAdmin.getAuthorization(user));
     				session.setAttribute(HttpContext.REMOTE_USER, user);
     				
     				// keep user-settings in sync with user-language settings
     				CookieTool cookieTool = (CookieTool) context.get("cookieTool");
     				if (cookieTool.get("l10n") != null) {
     					@SuppressWarnings("unchecked")
     					Dictionary<String,Object> userProps = user.getProperties();    					
     					userProps.put("user.language", cookieTool.get("l10n").getValue());
     				}    				
     				
     				doRedirect = true;
         		} else {
         			context.put("errorMsg","Unable to login. Username or password is invalid");
         		}
         	} else if (request.getParameter("doLogout") != null) {
         		session.removeAttribute("logon.isDone");
         		session.removeAttribute(HttpContext.AUTHENTICATION_TYPE);
         		session.removeAttribute(HttpContext.AUTHORIZATION);
         		session.removeAttribute(HttpContext.REMOTE_USER);
         		doRedirect = true;
         	}
         	
         	if (doRedirect) {
 				// redirect to target
     			if (session.getAttribute("login.target") != null) {
     				response.sendRedirect((String) session.getAttribute("login.target"));
     				session.removeAttribute("login.target");
     			} else if (request.getParameter("login.target") != null) {
     				response.sendRedirect((String) request.getParameter("login.target"));
     				request.removeAttribute("login.target");
     			} else {
     				response.sendRedirect("/");
     			}        		
         	} else {            
         		template = this.getTemplate("/resources/templates/LoginView.vm");
         	}
         } catch (Exception e ) {
         	this.logger.error(String.format(
         			"Unexpected '%s': %s",
         			e.getClass().getName(),
         			e.getMessage()
         	), e);
         	throw e;
         }
 
         return template;
 	}
 
 
 }
