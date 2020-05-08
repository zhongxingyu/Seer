 package com.youthministry.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
@Controller
 public class AdminController {
 
 	@RequestMapping(value={"/admin"})
 	public ModelAndView admin(HttpServletRequest request, Model model) {
 		System.out.println(request.getServletPath());
 	    Map<String, Object> myModel = new HashMap<String, Object>();
 		return new ModelAndView("admin", "adminContent", myModel);
 	}
 }
