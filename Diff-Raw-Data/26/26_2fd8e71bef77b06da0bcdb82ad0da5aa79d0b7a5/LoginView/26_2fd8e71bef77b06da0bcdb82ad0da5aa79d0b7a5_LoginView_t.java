 package org.paxle.gui.impl.servlets;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.osgi.service.http.HttpContext;
 import org.osgi.service.useradmin.User;
 import org.osgi.service.useradmin.UserAdmin;
 import org.paxle.gui.ALayoutServlet;
 import org.paxle.gui.impl.HttpContextAuth;
 import org.paxle.gui.impl.ServiceManager;
 
 public class LoginView extends ALayoutServlet {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Override
 	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {
         Template template = null;
 
         try {
     		// Get the session
     		HttpSession session = request.getSession(true);
         	
         	if (request.getParameter("doLogin") != null) {
         		// getting user-name + password
         		String userName = request.getParameter("login.username");
         		String password = request.getParameter("login.password");
         		if (password == null) password = "";
         		
         		// getting the userAdmin service
         		ServiceManager manager = (ServiceManager) context.get("manager");
         		UserAdmin uAdmin = (UserAdmin) manager.getService(UserAdmin.class.getName());
         		
         		// auth user
        		User user = HttpContextAuth.authenticatedAs(uAdmin, request, userName, password);        		
         		if (user != null){
         			// remember login state
         			session.setAttribute("logon.isDone", Boolean.TRUE);
         			
     				// set user-data into the session
     				session.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.FORM_AUTH);
     				session.setAttribute(HttpContext.AUTHORIZATION, uAdmin.getAuthorization(user));
     				session.setAttribute(HttpContext.REMOTE_USER, user);	        			
         			
     				// redirect to target
         			if (session.getAttribute("login.target") != null) {
         				response.sendRedirect((String) session.getAttribute("login.target"));
         			} else {
                 response.sendRedirect("/");
               }
         		} else {
         			context.put("errorMsg","Unable to login. Username or password is invalid");
         		}
         	} else if (request.getParameter("doLogout") != null) {
         		session.removeAttribute("logon.isDone");
         		session.removeAttribute(HttpContext.AUTHENTICATION_TYPE);
         		session.removeAttribute(HttpContext.AUTHORIZATION);
         		session.removeAttribute(HttpContext.REMOTE_USER);
         	}
             
             template = this.getTemplate("/resources/templates/LoginView.vm");
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
