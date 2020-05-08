 package net.iteach.web.support;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Component;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 @Component("webInterceptor")
 public class WebInterceptor extends HandlerInterceptorAdapter {
 
 	private static final ThreadLocal<HttpServletRequest> CURRENT_REQUEST = new ThreadLocal<HttpServletRequest>();
 
 	public static HttpServletRequest getCurrentRequest() {
 		HttpServletRequest request = CURRENT_REQUEST.get();
 		if (request == null) {
 			throw new IllegalStateException("No current request");
 		} else {
 			return request;
 		}
 	}
 
 	@Override
 	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
 		CURRENT_REQUEST.set(request);
 		// OK
 		return true;
 	}
 	
 	@Override
 	public void postHandle(HttpServletRequest request, HttpServletResponse response,
 			Object handler,
 			ModelAndView modelAndView) throws Exception {
 		// Adds the base URL to the model
 		if (modelAndView != null) {
			modelAndView.addObject("baseURL", WebUtils.getBaseURL(request));
 		}
 		// OK
 		super.postHandle(request, response, handler, modelAndView);
 	}
 
 	@Override
 	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
 		CURRENT_REQUEST.set(null);
 	}
 
 }
