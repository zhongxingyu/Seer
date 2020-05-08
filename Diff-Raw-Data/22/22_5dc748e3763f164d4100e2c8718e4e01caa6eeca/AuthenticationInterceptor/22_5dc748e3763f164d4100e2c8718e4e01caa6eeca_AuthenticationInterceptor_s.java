 package com.postcenter.interfaces.rest.interceptors;
 
 import java.lang.reflect.Method;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Cookie;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.ext.Provider;
 
 import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
 import org.jboss.resteasy.annotations.interception.ServerInterceptor;
 import org.jboss.resteasy.core.Headers;
 import org.jboss.resteasy.core.ResourceMethod;
 import org.jboss.resteasy.core.ServerResponse;
 import org.jboss.resteasy.spi.Failure;
 import org.jboss.resteasy.spi.HttpRequest;
 import org.jboss.resteasy.spi.interception.AcceptedByMethod;
 import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
 
 import com.postcenter.domain.model.authentication.Authentication;
 import com.postcenter.domain.model.authentication.IAuthenticationRepository;
 
 @Provider
 @ServerInterceptor
 @SecurityPrecedence
 public class AuthenticationInterceptor implements PreProcessInterceptor, AcceptedByMethod {
 
 	@Inject
 	private IAuthenticationRepository authRepository;
 
 	@Override
 	public boolean accept(Class clazz, Method method) {
 		return method.isAnnotationPresent(Authenticate.class);
 	}
 
 	@Override
 	public ServerResponse preProcess(HttpRequest httpRequest, ResourceMethod resourceMethod) throws Failure, WebApplicationException {
 
 		Map<String, Cookie> cookies = httpRequest.getHttpHeaders().getCookies();
 		
 
 		if (cookies.containsKey(Authentication.COOKIE_EMAIL) && cookies.containsKey(Authentication.COOKIE_TOKEN)) {
 
 			String email = cookies.get(Authentication.COOKIE_EMAIL).getValue();
 			String token = cookies.get(Authentication.COOKIE_TOKEN).getValue();
 			
 			Authentication authentication = authRepository.findAuthenticationByToken(email, token);
 			
			if (authentication != null && authentication.isAuthtenticated(token)) {
 				
				token = authentication.authenticate();
				authRepository.store(authentication);
				
				cookies.put(Authentication.COOKIE_TOKEN, new Cookie(Authentication.COOKIE_TOKEN, token));
 				return null;
 			}
 			else {
 				cookies.remove(Authentication.COOKIE_EMAIL);
 				cookies.remove(Authentication.COOKIE_TOKEN);
 			}
 		}
 
 		return new ServerResponse("User unauthenticated", Status.FORBIDDEN.getStatusCode(), new Headers<Object>());
 	}
 }
