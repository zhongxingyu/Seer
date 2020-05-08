 package br.com.caelum.vraptor.vaas;
 
import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import br.com.caelum.vraptor.vaas.authentication.Authenticator;
 import br.com.caelum.vraptor.vaas.authorization.PermissionVerifier;
 import br.com.caelum.vraptor4.InterceptionException;
 import br.com.caelum.vraptor4.Intercepts;
 import br.com.caelum.vraptor4.controller.ControllerMethod;
 import br.com.caelum.vraptor4.core.InterceptorStack;
 import br.com.caelum.vraptor4.interceptor.Interceptor;
 
 @Intercepts
 @ApplicationScoped
 public class VaasInterceptor implements Interceptor {
 	@Inject private PermissionVerifier permissions;
 	@Inject private Authenticator auth;
 	@Inject	private ServletContext context;
 	@Inject private HttpServletRequest httpRequest;
 	
 	private String loginUrl;
 	private String logoutUrl;
 
	@PostConstruct
 	public void config(){
 		this.loginUrl = context.getInitParameter("loginUrl");
 		this.logoutUrl = context.getInitParameter("logoutUrl");
 	}
 	
 	@Override
 	public void intercept(InterceptorStack stack, ControllerMethod method,
 			Object controllerInstance) throws InterceptionException {
		String context = this.context.getContextPath();
 		String uri = httpRequest.getRequestURI().substring(context.length());
 		
 		if(uri.equals(loginUrl)){
 			auth.tryToLogin();
 			return;
 		}
 		
 		if(uri.equals(logoutUrl)){
 			auth.tryToLogout();
 			return;
 		}
 		
 		if(permissions.verifyAccessFor(uri)) stack.next(method, controllerInstance);
 	}
 
 	@Override
 	public boolean accepts(ControllerMethod method) {
 		return true;
 	}
 	
 
 
 }
