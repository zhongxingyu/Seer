 package com.bgg.farmstoryback.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class MainController {
 	
 	private Logger logger = LoggerFactory.getLogger(MainController.class);
 	
 	
 	@RequestMapping(value = "dashboard.do", method = RequestMethod.GET)
 	public String dashboard(Model model) {
 		logger.info("into dashboard.do");
		return "view/dashboard";
 	}
 	
 	@RequestMapping(value = "main.do", method = RequestMethod.GET)
 	public String main(Model model) {
 		
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("view/main");
 		logger.info("into main.do");
 		
 		return "main/main";
 	}
 	
 //	@RequestMapping(value = "sub.do", method = RequestMethod.GET)
 //	public String sub(Model model) {
 //		logger.info("into sub.do");
 //		
 //		//db sample
 //		List<HashMap<String, String>> data = viewService.memberList();
 //		
 //		for(int i=0;i<data.size();i++) {
 //			System.out.println(data.get(i));
 //		}
 //		return "view/sub";
 //	}
 	
 	// Contents
 	@RequestMapping(value = "contents/manage.do", method = RequestMethod.GET)
 	public String contentsManage(Model model) {
 		
 //		List<Map> cateList = cateService.listByLevel(1);
 //		model.addAttribute("cateList", cateList);
 		return "view/contents";
 	}
 }
