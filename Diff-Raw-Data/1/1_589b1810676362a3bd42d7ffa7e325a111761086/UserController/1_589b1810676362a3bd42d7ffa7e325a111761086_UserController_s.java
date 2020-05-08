 package cz.cvut.fel.bupro.controller;
 
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import cz.cvut.fel.bupro.model.User;
 import cz.cvut.fel.bupro.service.UserService;
 
 @Controller
 public class UserController {
 	private final Log log = LogFactory.getLog(getClass());
 	
 	@Autowired
 	private UserService userService;
 	
 	@ModelAttribute("userList")
 	public List<User> getUserList() {
 		return userService.getAllUsers();
 	}
 	
 	@RequestMapping({ "/user/list" })
 	public String showUserList() {
 		return "user-list";
 	}
 	
 	@RequestMapping({ "/user/view/{id}" })
 	public String showUserDetail(Model model, Locale locale, @PathVariable Long id) {
 		log.trace("UserController.showUserDetail()");
 		User user = userService.getUser(id);
 		model.addAttribute("user", user);
 		return "user-view";
 	}
 }
