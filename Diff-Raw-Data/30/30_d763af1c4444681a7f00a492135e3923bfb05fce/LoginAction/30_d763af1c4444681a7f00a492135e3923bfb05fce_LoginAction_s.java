 package com.mcafee.bapp.action.login;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 public class LoginAction implements Controller{
 
 	@Override
 	public ModelAndView handleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		String username = request.getParameter("username");
 		String password = request.getParameter("password");
 		if("admin".equals(username) && "solidcore".equals(password)){
 			return new ModelAndView("redirect:done.html");
 		}
		return new ModelAndView("redirect:notdone.html");
 	}
 
 }
