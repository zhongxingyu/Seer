 package com.youthministry.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
@Controller
 public class UserController {
 	
 	@RequestMapping(value={"/group","/home"})
 	public ModelAndView groupView(HttpServletRequest request) {
 		System.out.println(request.getServletPath());
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("groupName", "no group specified");
 		return new ModelAndView("viewGroup", "model", model);
 	}
 	
 	@RequestMapping(value={"/group/{groupName}"})
 	public ModelAndView groupView(HttpServletRequest request, @PathVariable String groupName) {
 		System.out.println(request.getServletPath());
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("groupName", groupName);
 		return new ModelAndView("viewGroup", "model", model);
 	}
 	
}
