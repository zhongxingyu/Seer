 package org.huamuzhen.oa.server.interceptor;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.huamuzhen.oa.domain.entity.User;
import org.huamuzhen.oa.domain.enumeration.Privilege;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 public class AuthorizeInterceptor extends HandlerInterceptorAdapter {
 
 	@Override
 	public boolean preHandle(HttpServletRequest request,
 			HttpServletResponse response, Object handler) throws Exception {
 
 		User user = (User)request.getSession().getAttribute("currentUser");
 		if(user == null){
			response.setCharacterEncoding("utf-8");
			response.getWriter().write("还未登陆，请登录");
 			return false;
 		}
		if(user.getPrivilege()== Privilege.ADMIN){
			if(request.getRequestURI().startsWith(request.getContextPath()+"/reportForm") || request.getRequestURI().startsWith(request.getContextPath()+"/feedback")){
				response.setCharacterEncoding("utf-8");
				response.getWriter().write("权限不够");
				return false;
			}
		}else{
			if(request.getRequestURI().startsWith(request.getContextPath()+ "/user") || request.getRequestURI().startsWith(request.getContextPath()+"/orgUnit") || request.getRequestURI().startsWith(request.getContextPath()+"/reportFormType")){
				response.setCharacterEncoding("utf-8");
				response.getWriter().write("权限不够");
				return false;
			}
		}
		
		return true;
 	}
 	
 }
