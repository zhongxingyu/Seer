 package br.com.caelum.vraptor.vaas;
 
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import br.com.caelum.vraptor.vaas.authentication.Authenticator;
 import br.com.caelum.vraptor.vaas.authentication.VaasPrincipalSession;
 import br.com.caelum.vraptor.vaas.authorization.PermissionVerifier;
 import br.com.caelum.vraptor.vaas.event.AuthorizationFailedEvent;
 import br.com.caelum.vraptor.vaas.event.RefreshUserEvent;
 
 /**
  * This flow is the basis of VAAS. Any framework which wants to use VAAS, should 
  * use this object and just implement its way of continue the request flow.
  * @author Alberto Souza
  *
  */
 @ApplicationScoped
 public class AuthenticationAuthorizationFlow {
 
 	protected static final String LOGOUT_URL_PARAMETER = "logoutUrl";
 	protected static final String LOGIN_URL_PARAMETER = "loginUrl";
 	private PermissionVerifier permissions;
 	private Authenticator auth;
 	private ServletContext context;
 	private HttpServletRequest httpRequest;
 	private Event<RefreshUserEvent> refreshUserEvent;
 	private VaasPrincipalSession principalSession;
 	private Event<AuthorizationFailedEvent> authorizationFailedEvent;
 
 	private String loginUrl;
 	private String logoutUrl;
 	
	/** @deprecated CDI eyes only*/
	protected AuthenticationAuthorizationFlow() {
	}
	
 	@Inject
 	public AuthenticationAuthorizationFlow(PermissionVerifier permissions,
 			Authenticator auth, ServletContext context,
 			HttpServletRequest httpRequest,
 			Event<RefreshUserEvent> refreshUserEvent,
 			VaasPrincipalSession principalSession,
			Event<AuthorizationFailedEvent> authorizationFailedEvent) {
 		this.permissions = permissions;
 		this.auth = auth;
 		this.context = context;
 		this.httpRequest = httpRequest;
 		this.refreshUserEvent = refreshUserEvent;
 		this.principalSession = principalSession;
 		this.authorizationFailedEvent = authorizationFailedEvent;
 	}
 
 	@PostConstruct
 	public void config() {
 		this.loginUrl = context.getInitParameter(LOGIN_URL_PARAMETER);
 		this.logoutUrl = context.getInitParameter(LOGOUT_URL_PARAMETER);
 	}
 	
 	public void intercept(Runnable frameworkFlow) {
 		String context = this.context.getContextPath();
 		String uri = httpRequest.getRequestURI().substring(context.length());
 
 		if (uri.equals(loginUrl)) {
 			auth.tryToLogin();
 			return;
 		}
 
 		if (uri.equals(logoutUrl)) {
 			auth.tryToLogout();
 			return;
 		}
 
 		List<Rule> rulesNotAllowed = permissions.verifyAccessFor(uri);
 		
 		maybeRefreshUser();
 		if (!rulesNotAllowed.isEmpty()) {
 			authorizationFailedEvent.fire(new AuthorizationFailedEvent(
 					rulesNotAllowed));
 		} else {
 			frameworkFlow.run();
 		}
 
 	}
 
 	private void maybeRefreshUser() {
 		if (principalSession.isLogged()) {
 			refreshUserEvent.fire(new RefreshUserEvent());
 		}
 	}
 }
