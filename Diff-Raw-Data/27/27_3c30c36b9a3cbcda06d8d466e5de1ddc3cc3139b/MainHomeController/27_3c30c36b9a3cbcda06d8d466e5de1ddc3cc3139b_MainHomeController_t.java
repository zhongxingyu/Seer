 package friendzone.elec3609.controller;
 
import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
 
import friendzone.elec3609.model.*;
 import friendzone.elec3609.service.DatabaseHandler;
 
 @Controller
@SessionAttributes({"student", "dbHandler"})
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
 		
		// test for now
 		Student student = new Student("310278570", "jcho2360", "password", "Joon", "Choi", 
 									"jcho2360@uni.sydney.edu.au", "0420799956",
 									StudyLevel.findMatch("Undergraduate") , 
 									false, new ProgrammingLanguage[] {ProgrammingLanguage.findMatch("C++")});
 		
		// checks if the student is currently in team (should show in views the selection of current groups)
		List<Team> teamList = student.getTeams();
		
		// checks if the student is currently enrolled in subjects. this is for the list for students to choose
		// if they have not yet entered a group or with to join a group.
		List<UnitOfStudy> subjectList = student.getSubjects();
		
 		model.addAttribute("student", student);
 		return "mainHome";
 	}
 
 }
