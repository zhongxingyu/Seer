 package edu.upc.dsbw.spring.web;
 
 import java.util.HashMap;
 import java.util.List;
 import java.lang.*;
 
 import javax.servlet.http.HttpSession;
 
 //import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.upc.dsbw.spring.business.dao.intefaces.UserDao;
 import edu.upc.dsbw.spring.business.model.Book;
 import edu.upc.dsbw.spring.business.model.User;
 import edu.upc.dsbw.spring.business.model.UserBook;
 
 import edu.upc.dsbw.spring.business.service.UserService;
 
 //import edu.upc.dsbw.spring.web.DTO.UserBook;
 
 @Controller
 @RequestMapping("/users/*")
 public class UsersController {
 
 	// @Autowired edu.upc.dsbw.spring.business.BooksController domainController;
 	// edu.upc.dsbw.spring.business.UsersController domainController;
 	@Autowired
 	UserService userService;
 
 	// TODO prova que vaig fer jo sense ordenar
 	@RequestMapping(value = "/readingbooks/user/{id}", method = RequestMethod.GET)
 	public ModelAndView listBooks(@PathVariable("id") Integer id) {
 		List<Book> books = userService.getReadingBooks(id);
 		return new ModelAndView("books/list", "books", books);
 	}
 
 	@RequestMapping(value = "/readbooks/user/{id}", method = RequestMethod.GET)
 	public ModelAndView listReadBooks(@PathVariable("id") Integer id) {
 		List<Book> books = userService.getReadBooks(id);
 		return new ModelAndView("books/list", "books", books);
 	}
 
 	@RequestMapping(value = "/list", method = RequestMethod.GET)
 	public ModelAndView listUsers() {
 
 		List<User> users = userService.listUsers();
 		return new ModelAndView("users/list", "users", users);
 	}
 
 	@RequestMapping(value = "/signup", method = RequestMethod.GET)
 	public ModelAndView viewSingnup() {
 		return new ModelAndView("users/singup");
 	}
 
 	@RequestMapping(value = "/newUser", method = RequestMethod.POST)
 	public ModelAndView newUser(HttpSession session,
 			@RequestParam("username") String user,
 			@RequestParam("password") String pass) {
 
 		if (user.length() < 1) {
 			String message = "Did you forget the username? Let's try again.";
 			return new ModelAndView("users/singup", "message", message)
 					.addObject("username", user);
 		}
 		if (pass.length() < 1) {
 			String message = "Did you forget the password? Let's try again.";
 			return new ModelAndView("users/singup", "message", message)
 					.addObject("username", user);
 		}
 
 		/*
 		 * if(pass.length() < 6){ String message =
 		 * "The password needs more than "+ pass.length()
 		 * +", at least 6 characters. Let's try again."; return new
 		 * ModelAndView("users/singup", "message", message); }
 		 */
 
 		// boolean res = domainController.addUser(user, user, pass);
 		
 		boolean res = userService.addUser(user, user, user, pass);
 		if (res) {
 			// String message = "Would you like to login right now?.";
 			// return "redirect:session/login.jsp"
 			userService.doLogin(user, pass, session);
 
 			// return new
 			// ModelAndView("redirect:/users/"+session.getAttribute("userId")+".html");
 			// //"session/welcome","message","Benvingut "+username).addObject("userid",
 			// session.getAttribute("userId"));
 			return new ModelAndView("redirect:../users/mybooks.html");
 		} else {
 			String message = user + " already exist, is not valid or is reserveds.";
 			return new ModelAndView("users/singup", "message", message)
 					.addObject("username", user);
 		}
 
 	}
 
 	@RequestMapping(value = "/setAdmin", method = RequestMethod.POST)
 	public ModelAndView setAdminStatus(@RequestParam("userId") Integer id,
 			@RequestParam("status") boolean status, HttpSession session) {
 		try {
 			Integer userId = (Integer) session.getAttribute("userId");
 			Boolean isAdmin = (session.getAttribute("isAdmin") != null && (Boolean)session.getAttribute("isAdmin") == true);
 			if (userId == null || !isAdmin || userId == id) throw new IllegalArgumentException("The user is not logged in, is not admin or is trying to waive his/her own admin rights");
 			
 			if (status) {
 				userService.doAdmin(id, userId);
 			} else {
 				userService.undoAdmin(id, userId);
 			}
 			return new ModelAndView("ajax/ok");
 		} catch (IllegalArgumentException ex) {
 			return new ModelAndView("ajax/error", "extraData", ex.getMessage());
 		}
 	}
 
 	// AJAX
 	@RequestMapping(value = "/follow", method = RequestMethod.POST)
 	public ModelAndView setFollowStatus(@RequestParam("userId") Integer id,
 			@RequestParam("status") boolean status, HttpSession session) {
 		try {
 			Integer userId = (Integer) session.getAttribute("userId");
 			if (userId == null || userId == id) throw new IllegalArgumentException("The user is not logged in or is trying to follow him/herself");
 			
 			if (status) {
 				userService.newFollower(userId, id);
 			} else {
 				userService.removeFollower(userId, id);
 			}
 
 			return new ModelAndView("ajax/ok");
 		} catch (IllegalArgumentException ex) {
 			return new ModelAndView("ajax/error", "extraData", ex.getMessage());
 		}
 	}
 
 	/*@RequestMapping(value = "/followeds/{id}", method = RequestMethod.GET)
 	public ModelAndView getFollowedUsers(HttpSession session) {
 		Integer id = (Integer) session.getAttribute("userId");
 		List<User> users = userService.getFollowedUsers(id);
 		return new ModelAndView("users/list", "users", users);
 	}*/
 
 	// AJAX
 	@RequestMapping(value = "/getFollowers", method = RequestMethod.POST)
 	public ModelAndView getFollowers(@RequestParam("userId") Integer id) {
 		try {
 			List<User> users = userService.getFollowerUsers(id);
 			return new ModelAndView("users/ajaxlist", "users", users);
 		} catch (IllegalArgumentException ex) {
 			return new ModelAndView("ajax/error");
 		}
 	}
 
 	// AJAX
 	@RequestMapping(value = "/getFollowing", method = RequestMethod.POST)
 	public ModelAndView getFollowing(@RequestParam("userId") Integer id) {
 		try {
 			List<User> users = userService.getFollowedUsers(id);
 			return new ModelAndView("users/ajaxlist", "users", users);
 		} catch (IllegalArgumentException ex) {
 			return new ModelAndView("ajax/error");
 		}
 	}
 
 	@RequestMapping(value = "/{username}", method = RequestMethod.GET)
 	public ModelAndView viewUser(@PathVariable("username") String username,
 			HttpSession session) { 
 		
 		String loggedUsername = (String)session.getAttribute("username");
 		if (loggedUsername == null || loggedUsername.equals("")) loggedUsername = "undefined";
 		
 		if (username.equals("mybooks")) {
			if (loggedUsername.equals("undefined")) {
 				username = loggedUsername;
 			} else {
 				return new ModelAndView("session/notLoggedIn", "message",
 						"Error: Has d'estar loguejat per tal de veure aquesta pagina");
 			}
 		}
 	
 		User user = userService.getUser(username);
 		List<User> followerUsers = userService.getFollowerUsers(user.getId());
 		List<User> followingUsers = userService.getFollowedUsers(user.getId());
 		List<Book> readingBooks = userService.getReadingBooks(user.getId());
 		List<Book> readBooks = userService.getReadBooks(user.getId());
 		
 		Integer loggedUserid = (Integer)session.getAttribute("userId");
 		boolean loggedIn = (loggedUserid != null);
 		
 		boolean isFollowing = false;
 		if (loggedIn) {
 			for (User follower: followerUsers) {
 				if (follower.getId().compareTo(loggedUserid) == 0) {
 					isFollowing = true;
 					break;
 				}
 			}
 		}
 		
 		int followers = followerUsers.size();
 		int following = followingUsers.size();
 		
 		boolean selfProfile = (loggedUserid != null && loggedUserid.compareTo(user.getId()) == 0);
 		boolean loggedIsAdmin = (session.getAttribute("isAdmin") != null && (Boolean)session.getAttribute("isAdmin") == true);
 		
 		return new ModelAndView("users/detail", "user", user)
 				.addObject("isAdmin", userService.isAdmin(user))
 				.addObject("canAdmin", (!selfProfile && loggedIsAdmin))
 				.addObject("isFollowing", isFollowing)
 				.addObject("followers", followers)
 				.addObject("following", following)
 				.addObject("readBooks", readBooks)
 				.addObject("readingBooks", readingBooks)
 				.addObject("loggedIn", loggedIn)
 				.addObject("loggedUsername", loggedUsername)
 				.addObject("selfprofile", selfProfile);
 	}
 
 	//
 	// @RequestMapping(value="/{id}",method=RequestMethod.GET)
 	// public ModelAndView viewUser(@PathVariable("id") Long id){
 	// User user = domainController.getUser(id);
 	// return new ModelAndView("users/detail", "user", user);
 	// }
 	//
 
 	// @RequestMapping(value="/{id}",method=RequestMethod.GET)
 	// public ModelAndView viewUser(@PathVariable("id") Long id, HttpSession
 	// session){
 	// /*if (session.getAttribute("username")!=null &&
 	// session.getAttribute("username")!=""){
 	// User user =
 	// domainController.getUser((Long.valueOf(session.getAttribute("userId").toString())));
 	// return new ModelAndView("users/mybooks", "user", user);
 	// }*/
 	// User user = domainController.getUser(id);
 	// return new ModelAndView("users/detail", "user", user);
 	// }
 
 	// @RequestMapping(value="/mybooks",method=RequestMethod.GET)
 	// public ModelAndView viewMyBooks(HttpSession session){
 	// if (session.getAttribute("username")!=null &&
 	// session.getAttribute("username")!=""){
 	// User user =
 	// domainController.getUser(Long.valueOf(session.getAttribute("userId").toString()));
 	// return new ModelAndView("users/mybooks", "user", user);
 	// }
 	// else{
 	// return new
 	// ModelAndView("session/notLoggedIn","message","Error: Has d'estar loguejat per tal de veure aquesta pàgina");
 	// }
 	// }
 
 	// @RequestMapping(value="/signup",method=RequestMethod.GET)
 	// public ModelAndView viewSingnup(HttpSession session){
 	// return new ModelAndView("users/singup");
 	// }
 
 	// @RequestMapping(value="/newUser",method=RequestMethod.POST)
 	// public ModelAndView newUser(HttpSession session,
 	// @RequestParam("username") String user, @RequestParam("password") String
 	// pass ){
 	//
 	// if(user.length() < 1){
 	// String message = "Did you forget the username? Let's try again.";
 	// return new ModelAndView("users/singup", "message", message);
 	// }
 	// if(pass.length() < 1){
 	// String message = "Did you forget the password? Let's try again.";
 	// return new ModelAndView("users/singup", "message", message);
 	// }
 	//
 	// /*
 	// if(pass.length() < 6){
 	// String message = "The password needs more than "+ pass.length()
 	// +", at least 6 characters. Let's try again.";
 	// return new ModelAndView("users/singup", "message", message);
 	// }*/
 	//
 	// boolean res = domainController.addUser(user, user, pass);
 	// if (res) {
 	// //String message = "Would you like to login right now?.";
 	// //return "redirect:session/login.jsp"
 	// loginController.doLogin(user, pass, session, domainController);
 	// return new
 	// ModelAndView("redirect:/users/"+session.getAttribute("userId")+".html");//,
 	// "message", "Benvingut " + user);//new
 	// ModelAndView("redirect:../session/login.html", "message", message);
 	// }
 	// else{
 	// String message = user + " already exists.";
 	// return new ModelAndView("users/singup", "message",
 	// message).addObject("username", user);
 	// }
 	//
 	// }
 
 }
