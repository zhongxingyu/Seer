 package org.huamuzhen.oa.server.interceptor;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.huamuzhen.oa.domain.entity.User;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 public class AuthorizeInterceptor extends HandlerInterceptorAdapter {
 
 	@Override
 	public boolean preHandle(HttpServletRequest request,
 			HttpServletResponse response, Object handler) throws Exception {
 
		if(request.getRequestURI().equals(request.getContextPath()) || request.getRequestURI().equals(request.getContextPath()+"/") || 
				request.getRequestURI().equals(request.getContextPath()+"/login")){
			return true;
		}
		
 		User user = (User)request.getSession().getAttribute("currentUser");
 		if(user == null){
			request.getRequestDispatcher("/").forward(request, response);
 			return false;
 		}
		return super.preHandle(request, response, handler);
 	}
 	
 }
