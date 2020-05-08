 package com.digt.auth;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.shindig.auth.SecurityToken;
 import org.springframework.security.web.WebAttributes;
 import org.springframework.security.web.savedrequest.DefaultSavedRequest;
 
 import com.digt.common.utils.LogUtils;
 import com.digt.jpa.spi.ProfileService;
 import com.digt.model.PersonProfile;
 import com.digt.store.AuthStore;
 import com.google.inject.Inject;
 
 public class LoginFormAuthProvider implements AuthProvider {
 
 	public static final String AUTH_TYPE = "loginform";
 	private static final Logger LOG = Logger.getLogger(
 			LoginFormAuthProvider.class.getName());
 
 	@Inject
 	protected AuthStore store;
 	@Inject
 	protected ProfileService profileSvc;
 	
 	@Override
 	public void authorize(HttpServletRequest request, HttpServletResponse resp)
 			throws IOException, AuthorizationException {
 		
 		String login = request.getParameter("login");
 		String password = request.getParameter("password");
 		
 		try {
 			PersonProfile prof = profileSvc.getPersonProfileLogin(login).get();
 			if (prof != null)
 			{
 				if (prof.getPassword() != null && prof.getPassword().length() > 0 &&
 						prof.getPassword().equals(password))
 				{
 					SecurityToken token = new DigtSecurityToken(prof.getUserId(), null, null);
 					store.store(token);
 					Cookie c = new Cookie(DigtAuthorizationHandler.TOKEN, ""+token.hashCode());
 					if ("1".equals(request.getParameter("remember")))
 						c.setMaxAge(3600 * 24); // 1 day
 					c.setSecure(true);
 					resp.addCookie(c);
 					// Mark login date/time
 					prof.setLastLogon(new Date());
 					profileSvc.update(prof);
 					String redirectUrl = "web/";
 					HttpSession sess = request.getSession();
 					if (sess != null) {
 						DefaultSavedRequest req = (DefaultSavedRequest) sess.getAttribute(WebAttributes.SAVED_REQUEST);
						if (req != null) {
 							redirectUrl += "?" + req.getQueryString();
 						}
 					}
 					resp.sendRedirect(redirectUrl);
 					return;
 				}
 				else
 					request.getSession().setAttribute("err_msg", "Неправильный пароль.");
 			}
 			else
 				request.getSession().setAttribute("err_msg", "Неправильный логин.");
 			
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			request.getSession().setAttribute("err_msg", e.getMessage());
 		}
 		
 		resp.sendRedirect("login.jsp?login_error=1");
 
 	}
 
 }
