 
 package axirassa.webapp.pages.user;
 
 import java.io.IOException;
 
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.authz.annotation.RequiresGuest;
 import org.apache.shiro.subject.Subject;
 import org.apache.shiro.web.util.SavedRequest;
 import org.apache.shiro.web.util.WebUtils;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.Log;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.Secure;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.RequestGlobals;
 import org.apache.tapestry5.services.Response;
 import org.tynamo.security.services.PageService;
 
 import axirassa.model.exception.NoSaltException;
 import axirassa.webapp.components.AxForm;
 import axirassa.webapp.pages.MonitorConsole;
 import axirassa.webapp.services.AxirassaSecurityService;
 
 @Secure
 @RequiresGuest
 public class LoginUser {
 	@Inject
 	@Property
 	private AxirassaSecurityService security;
 
 	@Inject
 	private RequestGlobals requestGlobals;
 
 	@Inject
 	private PageService pageService;
 
 	@Inject
 	private Response response;
 
 	@Property
 	private String email;
 
 	@Property
 	private String password;
 
 	@Property
 	private boolean rememberme;
 
 	@Property
 	private boolean isLoggedIn;
 
 	@Component
 	private AxForm form;
 
 
	public void onActivate () {
 		if (security.isUser()) {
 			email = security.getEmail();
 			isLoggedIn = true;
 		}
 	}
 
 
 	@Log
	public void onValidateFromForm () throws NoSaltException {
 		if (email == null || password == null)
 			return;
 
 		Subject subject = security.getSubject();
 		try {
 			UsernamePasswordToken token = new UsernamePasswordToken(email, password);
 			token.setRememberMe(rememberme);
 			subject.login(token);
 		} catch (AuthenticationException e) {
 			showInvalidLoginMessage();
 		}
 	}
 
 
	private void showInvalidLoginMessage () {
		form.recordError("E-mail, password combination was not found in records");
 	}
 
 
 	@Log
	public Object onSuccessFromForm () {
 		SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(requestGlobals.getHTTPServletRequest());
 
 		if (savedRequest == null)
 			return pageService.getSuccessPage();
 
 		if (savedRequest.getMethod().equalsIgnoreCase("get"))
 			try {
 				response.sendRedirect(savedRequest.getRequestUrl());
 				return null;
 			} catch (IOException e) {
 				return pageService.getSuccessPage();
 			}
 
 		return MonitorConsole.class;
 	}
 }
