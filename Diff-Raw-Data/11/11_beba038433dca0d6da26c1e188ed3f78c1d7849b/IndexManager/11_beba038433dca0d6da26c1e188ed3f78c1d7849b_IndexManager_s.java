 package com.charitybuzz.web.manager.controller;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.charitybuzz.dto.Operator;
 import com.charitybuzz.service.OperatorService;
 import com.charitybuzz.web.manager.form.LoginForm;
 
 @Controller
 @RequestMapping(value = "/manager")
 public class IndexManager {
 
 	@Resource
 	private OperatorService operatorService;
 	
 	/**
 	 * index 頁面
 	 * @return
 	 */
 	@RequestMapping(value = "/index",method = RequestMethod.GET)
 	public ModelAndView index() {
 		ModelAndView mav = new ModelAndView("manager/index");
 		return mav;
 
 	}
 	/**
	 * 登入成功頁面
	 * @return
	 */
	@RequestMapping(value = "/login_success")
	public String loginSuccess() {
		return "manager/index";
		
	}
	/**
 	 * 是否登入成功
 	 * 成功 add sessionObject
 	 * @param form
 	 * @param session
 	 * @return
 	 */
 	@RequestMapping(value = "/index",method = RequestMethod.POST)
 	public ModelAndView loginForm(LoginForm form,HttpSession session) {
 		ModelAndView mav = new ModelAndView("manager/index");
 		// 是否manager Login
 		Operator operator = operatorService.findByName(form.getName());
 		if (operator != null) {
 			if ((operator.getPassWord()).equals(form.getPassWord())) {
 				session.setAttribute("operator", operator);
				return new ModelAndView("redirect:/manager/index.do");
 			}
 		}
 		return mav;
 		
 	}
 
 }
