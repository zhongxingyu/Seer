 package epam.ph.sg.controllers;
 
 /**
  * @author Talash Pavlo
  */
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class ErrorController {
 	
 	@RequestMapping("/404.html")
 	public String error404(HttpSession session) {
		if(session.getAttribute("user")!=null)
		{
			session.setAttribute("currentPos", "error404.html");
			return "index";
		}
		else
		{
			return "404";	
		}
 	}
 
 	@RequestMapping("/error404.html")
 	public String e404(HttpSession session) {
 	return "404";
 	}
 }
