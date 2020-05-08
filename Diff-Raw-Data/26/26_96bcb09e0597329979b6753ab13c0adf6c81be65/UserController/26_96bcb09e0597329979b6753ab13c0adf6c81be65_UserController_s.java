 package ncu.cc.moonshine.controllers;
 
 import ncu.cc.moonshine.domain.User;
 import ncu.cc.moonshine.services.IUserService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/user")
 public class UserController {
 		@Qualifier("database")
 		private @Autowired IUserService userService;
 		
 		@RequestMapping(method=RequestMethod.GET)
 		public String home(ModelMap model) {
 			model.addAttribute("userList", userService.findAll());
			return "user/home";
 		}
 		
 		@RequestMapping(value = "/add", method=RequestMethod.GET)
 		public String addUser(Model model) {
 			model.addAttribute("userBean", new User());
 			return "user/add";
 		}
 
 		@RequestMapping(value = "/add", method=RequestMethod.POST)
 		public String addUser(@ModelAttribute("userBean") User userBean, Model model) {
 			userService.addUser(userBean);
 			return "redirect:/user";
 		}
 		
 		@RequestMapping(value = "/modify/{userid}", method=RequestMethod.GET)
 		public String modifyUser(@PathVariable Integer userid, Model model) {
 			model.addAttribute("userBean", userService.getUserById(userid));
 			return "user/modify";
 		}
 		
 		@RequestMapping(value = "/modify/{userid}", method=RequestMethod.POST)
 		public String modifyUser(@ModelAttribute("userBean") User userBean, Model model) {
 			userService.modifyUser(userBean);
 			return "redirect:/user";
 		}
 		
 		@RequestMapping(value = "/delete", method=RequestMethod.GET)
 		public String deleteUser(@RequestParam("userId") Integer userid) {
 			userService.deleteUser(userService.getUserById(userid));
 			return "redirect:/user";
 		}
 		
 		@ExceptionHandler(RuntimeException.class)
 		public String handleRuntimeException(RuntimeException e) {
 			return "redirect:/user";
 		}
 		
 		@ExceptionHandler(Exception.class)
 		@ResponseBody
 		public String handleException(Exception e) {
 			return e.getMessage();
 		}
 	}
