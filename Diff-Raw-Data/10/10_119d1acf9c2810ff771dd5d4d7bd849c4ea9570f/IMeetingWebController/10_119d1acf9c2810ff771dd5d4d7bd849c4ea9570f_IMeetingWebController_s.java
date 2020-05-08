 package com.imeeting.mvc.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.imeeting.constants.WebConstants;
 import com.imeeting.web.user.UserBean;
 
 
 @Controller
 public class IMeetingWebController {
 	
 	@RequestMapping("/")
 	public ModelAndView index(HttpSession session, HttpServletRequest request) {
 		ModelAndView view = new ModelAndView();
 		view.setViewName("index");
 		view.addObject(WebConstants.page_name.name(), "home");
 		return view;
 	}
 
 	@RequestMapping(value = "/home", method = RequestMethod.GET)
 	public ModelAndView home(HttpSession session, HttpServletRequest request) {
 		ModelAndView view = new ModelAndView();
 		view.setViewName("index");
 		view.addObject(WebConstants.page_name.name(), "home");
 		return view;
 	}
 
 	@RequestMapping(value = "/mobile", method = RequestMethod.GET)
 	public String mobile(HttpSession session, HttpServletRequest request) {
 		return "index_mobile";
 	}	
 
 	@RequestMapping(value = "/signin", method = RequestMethod.GET)
 	public ModelAndView signin() {
 		ModelAndView view = new ModelAndView();
 		view.setViewName("signin");
 		view.addObject(WebConstants.page_name.name(), "signin");
 		return view;
 	}
 
 	@RequestMapping(value = "/features", method = RequestMethod.GET)
 	public ModelAndView features() {
 		ModelAndView view = new ModelAndView();
 		view.setViewName("features");
 		view.addObject(WebConstants.page_name.name(), "features");
 		return view;
 	}
 
 	@RequestMapping(value = "/forgetpwd", method = RequestMethod.GET)
 	public ModelAndView forgetpwd() {
 		ModelAndView view = new ModelAndView();
 		view.setViewName("forgetpwd");
 		view.addObject(WebConstants.page_name.name(), "forgetpwd");
 		return view;
 	}
 	
 	@RequestMapping(value = "/signout", method = RequestMethod.GET)
 	public String signout(HttpSession session) {
 		session.removeAttribute(UserBean.SESSION_BEAN);
 		return "redirect:/";
 	}
 	
 }
