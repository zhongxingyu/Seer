 package friendzone.elec3609.controller;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
import friendzone.elec3609.model.ProgrammingLanguage;
import friendzone.elec3609.model.Student;
import friendzone.elec3609.model.StudyLevel;
 import friendzone.elec3609.service.DatabaseHandler;
 
 @Controller
 public class MainHomeController {
 
 	final static DatabaseHandler dbHandler = DatabaseHandler.getInstance();
 	
 	@RequestMapping(value ="/mainHome", method = RequestMethod.POST)
 	public String loginValidation(HttpServletRequest request, 
 			Model model){
 		if(request.getParameter("password") == null || request.getParameter("username") == null)
 			return "redirect:/";
 		if(!(request.getParameter("password").matches("poopoo")) || !(request.getParameter("username").matches("joseph")))
 			return "redirect:/";
 		
 		//if(!checkAuthentication(map.get("password").toString(), map.get("username").toString()))
 			//return "redirect:/";
 		
 		Student student = new Student("310278570", "jcho2360", "password", "Joon", "Choi", 
 									"jcho2360@uni.sydney.edu.au", "0420799956",
 									StudyLevel.findMatch("Undergraduate") , 
 									false, new ProgrammingLanguage[] {ProgrammingLanguage.findMatch("C++")});
 		
 		model.addAttribute("student", student);
 		return "mainHome";
 	}
 
 }
