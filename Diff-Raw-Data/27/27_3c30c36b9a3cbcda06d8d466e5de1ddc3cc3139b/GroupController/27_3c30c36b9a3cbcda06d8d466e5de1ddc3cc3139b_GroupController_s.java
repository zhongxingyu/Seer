 package friendzone.elec3609.controller;
 
 import java.util.Map;
 
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import friendzone.elec3609.model.Student;
 import friendzone.elec3609.service.DatabaseHandler;
 
 @Controller
@SessionAttributes("student")
 public class GroupController {
 
 	final static DatabaseHandler dbHandler = DatabaseHandler.getInstance();
 	
 	@RequestMapping(value = "/group", method = RequestMethod.GET)
 	public String groupIndex(@ModelAttribute("student") Student student ){
 		
 		//if (model.get("studentAttr") != null) {
 			System.out.println(student.getFirstName().toString());
 		//}
 		return "group";
 	}
 	
 	@RequestMapping("/group/invite")
 	public String inviteFriends() {
 		
 		return "group_invite";
 	}
 	
 
 }
