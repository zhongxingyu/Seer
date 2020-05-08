 package net.pdp7.tvguide.spring.web;
 
 import org.springframework.ui.ModelMap;
 import org.springframework.web.context.request.WebRequest;
 
 import net.pdp7.commons.spring.web.context.request.WebRequestInterceptorAdapter;
 
 public class SessionInterceptor extends WebRequestInterceptorAdapter {
 	
 	protected final SessionService sessionService;
 
 	public SessionInterceptor(SessionService sessionService) {
 		this.sessionService = sessionService;
 	}
 	
	@Override
 	public void postHandle(WebRequest request, ModelMap model) throws Exception {
 		model.addAttribute("userId", sessionService.getUserId(request));
 	}
 
 }
