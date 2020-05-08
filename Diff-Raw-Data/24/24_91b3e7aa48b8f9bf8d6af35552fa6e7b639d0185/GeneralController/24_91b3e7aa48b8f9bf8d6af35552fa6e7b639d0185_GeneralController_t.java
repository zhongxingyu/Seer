 package il.co.brandis.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class GeneralController {
 
	@RequestMapping(value="/?*/?*")
	public String innerErrorPage() {
		return "redirect:/error";
	}
	@RequestMapping(value="/?*")
	public String errorPage() {
		return "generalerror";
 	}
 }
