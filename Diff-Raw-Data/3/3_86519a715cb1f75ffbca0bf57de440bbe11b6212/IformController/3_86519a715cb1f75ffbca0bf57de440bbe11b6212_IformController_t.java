 package com.fauxwerd.web.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @RequestMapping(value="/iform")
 public class IformController {
 
     final Logger log = LoggerFactory.getLogger(getClass());
 
 	@RequestMapping(method=RequestMethod.GET)
 	public ModelAndView getIform(HttpServletRequest req, HttpServletResponse res, Model model) {
 		String url = req.getParameter("url");
 		String u = req.getParameter("u");
 		String t = req.getParameter("t");
		
		if (log.isDebugEnabled()) log.debug(String.format("t = %s", t));
		
 		model.addAttribute("url", url);
 		model.addAttribute("u", u);
 		model.addAttribute("t", t);
 		ModelAndView modelAndView = new ModelAndView("iform");
 		return modelAndView;
 	}
 }
