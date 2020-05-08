 package com.micmiu.framework.web.v1.system.action;
 
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
 import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * LoginAction负责打开登录页面(GET请求)和登录出错页面(POST请求)， 真正登录的POST请求由Filter完成,
  * 
  * @author <a href="http://www.micmiu.com">Michael Sun</a>
  */
 @Controller
 public class LoginAction {
 
 	@RequestMapping(value = "/login.do", method = RequestMethod.GET)
 	public String loginShow() {
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated()) {
			return "redirect:/index.do";
		}
 		return "login";
 	}
 
 	@RequestMapping(value = "/login.do", method = RequestMethod.POST)
 	public String fail(
 			@RequestParam(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM) String userName,
 			Model model) {
 		model.addAttribute(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM,
 				userName);
 		return "login";
 	}
 
 }
