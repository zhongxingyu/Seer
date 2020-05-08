 package exist.practice.controller.forms;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import exist.practice.Role;
 import exist.practice.User;
 import exist.practice.service.RoleService;
 import exist.practice.service.UserService;
 import exist.practice.validator.UsernameValidator;
 
 @Controller
 @RequestMapping("/saveUser.htm")
 public class SaveUserMVC {
 	
 	UsernameValidator unameValidator;
 	
 	public SaveUserMVC(){
 		unameValidator = new UsernameValidator();
 	}
 	
 	@Autowired
 	private RoleService roleService;
 	
 	private UserService userService;
 	
 	@Autowired
 	public void setUserServiceImpl(UserService userService) {
 		this.userService = userService;
 	}
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public String showForm(ModelMap model, HttpServletRequest req) {
 		System.out.println("Invoked: showForm");
 		model.put("user", new User());
 		return "saveUser";
 	}
 	
 	//(Model model, @ModelAttribute("mappedModel") T mappedModel, BindingResult bindingResult, @RequestParam(value="submit",required=true) String submitType
 	@RequestMapping(method = RequestMethod.POST)
 	public String onSubmit(HttpServletRequest req, ModelMap model, User user) {
 	    System.out.println("Invoked: onSubmit");
 	    String error = "Registration Failed! ";
 	    if(!user.getUserName().equals("") && !user.getPassword().equals("") 
 	    	&& !user.getFirstName().equals("") && !user.getLastName().equals("") 
 	    	&& !user.getMi().equals("") && !user.getEmailAddress().equals(""))
 	    {
 	    	if(unameValidator.validate(user.getUserName())){
 			    if(RegisterUserMVC.isAvailable(user.getUserName())){
 			    	if(user.getPassword().length() > 3){
 				    	if(user.getPassword().equals(req.getParameter("confirmPassword"))){
 				    		Role role = this.roleService.findRole("roleId", "2").get(0);
 							Set<Role> roleList = user.getRoleList();
 							if (roleList == null) {
 								roleList = new HashSet<Role>();
 							}
 							roleList.add(role);
 							user.setRoleList(roleList);
				    		userService.addUser(user);	
 				    		HttpSession session = req.getSession();
 				    		session.setAttribute("uname", user.getUserName());
 				    		model.put("error", "Registration Successful!");
 				    		return "redirect: home.htm";
 				    	} else error += "Passwords don't match.";
 			    	} else error += "Password too short.";
 			    } else error += "Username already in use.";
 	    	} else error += "Username too short/long or has invalid character(s).";
 	    } else error += "Make sure to fill-up all required fields.";
     	model.put("message", error);
     	return "saveUser";
 	}	
 	
 }
