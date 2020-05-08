 package com.mjeanroy.springhub.security;
 
 import com.mjeanroy.springhub.commons.web.utils.Session;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.math.NumberUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.method.HandlerMethod;
 import org.springframework.web.servlet.HandlerInterceptor;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class SecurityInterceptor implements HandlerInterceptor {
 
 	/** Class logger */
 	private static final Logger LOG = LoggerFactory.getLogger(SecurityInterceptor.class);
 
 	/** Secret key used by cookie */
 	private String secret;
 
 	/** Salt used by cookie */
 	private String salt;
 
 	/** Cookie name used for session security check */
 	private String cookieName;
 
 	@Override
 	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
 		HandlerMethod method = (HandlerMethod) handler;
 		Security methodAnnotation = method.getMethodAnnotation(Security.class);
 		if (methodAnnotation != null) {
 			LOG.debug("Request is securized, check for session");
 
 			Session session = new Session(request, response, salt, secret);
 			String value = session.get(cookieName);
 
 			if (!NumberUtils.isNumber(value)) {
 				LOG.info("No session available or session is not valid");
 				if (StringUtils.isNotEmpty(methodAnnotation.redirectTo())) {
 					LOG.info("Redirect to page '{}'", methodAnnotation.redirectTo());
 					response.sendRedirect(methodAnnotation.redirectTo());
 				}
 				else {
 					LOG.info("Return unauthorized status");
					response.setStatus(401);
 				}
 				return false;
 			}
 
 		}
 		return true;
 	}
 
 	@Override
 	public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
 	}
 
 	@Override
 	public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
 	}
 
 	public void setSecret(String secret) {
 		this.secret = secret;
 	}
 
 	public void setSalt(String salt) {
 		this.salt = salt;
 	}
 
 	public void setCookieName(String cookieName) {
 		this.cookieName = cookieName;
 	}
 }
