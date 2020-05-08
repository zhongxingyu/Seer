 package com.darwinsys.springmvc;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.darwinsys.jwf.model.Person;
 import com.darwinsys.jwf.model.PersonDao;
 
 @Controller
@RequestMapping(value="/person")
 public class PersonHandler {
 	
 	@RequestMapping(method=RequestMethod.GET)
 	public String setupForm(Model m) {
			m.addAttribute("person", new Person());
		return "form";
 	}
 	
 	@Resource
 	PersonDao dao;
 	
 	@RequestMapping(method=RequestMethod.POST)
 	public String savePerson(@ModelAttribute Person person, Model m) {
 		System.out.println("PersonHandler.savePerson()");
 		
		m.addAttribute("message", "Successfully saved person: " + person);
 		dao.insert(person);
 		
		return "saved";
 	}
 }
