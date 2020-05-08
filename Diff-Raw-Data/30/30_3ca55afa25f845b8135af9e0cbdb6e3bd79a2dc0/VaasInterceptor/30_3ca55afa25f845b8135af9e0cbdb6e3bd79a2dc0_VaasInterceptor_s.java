 package br.com.caelum.vraptor.vaas;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.inject.Instance;
 import javax.enterprise.inject.spi.CDI;
 import javax.inject.Inject;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import br.com.caelum.vraptor.vaas.event.AuthenticateFailedEvent;
 import br.com.caelum.vraptor.vaas.event.AuthenticatedEvent;
 import br.com.caelum.vraptor.vaas.event.AuthorizationFailedEvent;
 import br.com.caelum.vraptor.vaas.event.LogoutEvent;
 import br.com.caelum.vraptor.vaas.event.NotLoggedEvent;
 import br.com.caelum.vraptor.vaas.event.RefreshUserEvent;
 import br.com.caelum.vraptor4.InterceptionException;
 import br.com.caelum.vraptor4.Intercepts;
 import br.com.caelum.vraptor4.controller.ControllerMethod;
 import br.com.caelum.vraptor4.core.InterceptorStack;
 import br.com.caelum.vraptor4.interceptor.Interceptor;
 
 @Intercepts
 @ApplicationScoped
 public class VaasInterceptor implements Interceptor {
 
 	@Inject
 	private Event<AuthenticatedEvent> authenticatedEvent;
 
 	@Inject
 	private Event<AuthenticateFailedEvent> authenticationFailedEvent;
 
 	@Inject
 	private Event<AuthorizationFailedEvent> authorizationFailedEvent;
 
 	@Inject
 	private Event<NotLoggedEvent> notLoggedEvent;
 
 	@Inject
 	private Event<RefreshUserEvent> refreshUserEvent;
 
 	@Inject
 	private Event<LogoutEvent> logoutEvent;
 
 	private String loginUrl;
 
 	private String logoutUrl;
 
 	private Object accessConfiguration;
 
 	private RolesConfigMethod rolesConfigMethod;
 	
 	@Inject
 	private ServletContext context;
 	
 	@Inject
 	private HttpServletRequest httpRequest;
 
	@SuppressWarnings("rawtypes")
 	@PostConstruct
 	public void config() {
 		this.loginUrl = context.getInitParameter("loginUrl");
 		this.logoutUrl = context.getInitParameter("logoutUrl");
		Instance possibleConfigurations = CDI.current().select(
				AccessConfiguration.class);
 		if (possibleConfigurations.isAmbiguous()) {
 			throw new RuntimeException(
 					"You should use just one AccessConfiguration class");
 		}
 		this.accessConfiguration = possibleConfigurations.get();
 		this.rolesConfigMethod = new RolesConfigMethod(this.accessConfiguration);
 	}
 
 
 	@Override
 	public void intercept(InterceptorStack stack, ControllerMethod method,
 			Object controllerInstance) throws InterceptionException {
 		if (httpRequest.getRequestURI().equals(logoutUrl)) {
 			logoutEvent.fire(new LogoutEvent());
 			return;
 		}
 		if (httpRequest.getRequestURI().equals(loginUrl)) {
 			try {
 				httpRequest.login(httpRequest.getParameter("login"),
 						httpRequest.getParameter("password"));
 				authenticatedEvent.fire(new AuthenticatedEvent(httpRequest
 						.getUserPrincipal()));
 			} catch (ServletException e) {
 				authenticationFailedEvent.fire(new AuthenticateFailedEvent(e));
 			}
 			return;
 		}
 
 		if (httpRequest.getUserPrincipal() == null) {
 			notLoggedEvent.fire(new NotLoggedEvent());
 			return;
 		}
 		List<Rule> roles = rolesConfigMethod.rolesFor(httpRequest.getRequestURI());
 		if (!roles.isEmpty()) {
 			ArrayList<Rule> rulesNotAllowed = new ArrayList<Rule>();
 			boolean authorized = true;
 			for (Rule rule : roles) {
 				authorized = authorized && rule.isAuthorized();
 				if(!authorized){
 					rulesNotAllowed.add(rule);
 				}
 			}
 			if (authorized) {
 				refreshUserEvent.fire(new RefreshUserEvent());
 				stack.next(method, controllerInstance);	
 			}
 			authorizationFailedEvent.fire(new AuthorizationFailedEvent(
 					rulesNotAllowed));
 		}		
 	}
 
 	@Override
 	public boolean accepts(ControllerMethod method) {
 		return true;
 	}
 
 }
