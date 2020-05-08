 package com.seo.webapp;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
  
 @Controller
 public class BaseController {
 	
 	@RequestMapping(value="/search", method = RequestMethod.GET)
 	public ModelAndView search() {
 		//model.addAttribute("message", "Maven Web Project + Spring 3 MVC - welcome()");
 		//return "index";
 		return new ModelAndView("index", "command", new Query());
  
 	}
  
 
 	@RequestMapping(value="/result", method = RequestMethod.POST)
 	public String result(@ModelAttribute("SpringWeb")Query q, ModelMap model) {
		// Query's m_query always seems to have a ',' (comma) that is appended--strip this. 
		q.setQuery(q.getQuery().substring(0, q.getQuery().length() - 1));
 		model.addAttribute("query", q.getQuery());
 		model.addAttribute("siteToCompare", q.getSiteToCompare());
 		try {
 			model.addAttribute("json", q.HTTP_Request());
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println(Query.LOG_ERR_MSG);
 			model.addAttribute("errMsg", Query.PAGE_ERR_MSG);
 			return "error";
 		}
 		return "result";
 	}
 
 	@RequestMapping(value="/student", method = RequestMethod.GET)
 	public ModelAndView student() {
 		return new ModelAndView("student", "command", new Student()); 
 	}
 
 	@RequestMapping(value = "/addStudent", method = RequestMethod.POST)
 	public String addStudent(@ModelAttribute("SpringWeb")Student student, ModelMap model) {
 		model.addAttribute("name", student.getName());
 		model.addAttribute("age", student.getAge());
 		model.addAttribute("id", student.getId());
 
 		return "studentResult";
 	}
  
  
 	@RequestMapping(value="/welcome/{name}", method = RequestMethod.GET)
 	public String welcomeName(@PathVariable String name, ModelMap model) {
  
 		model.addAttribute("message", "Maven Web Project + Spring 3 MVC - " + name);
 		return "sample";
  
 	}
  
 }
