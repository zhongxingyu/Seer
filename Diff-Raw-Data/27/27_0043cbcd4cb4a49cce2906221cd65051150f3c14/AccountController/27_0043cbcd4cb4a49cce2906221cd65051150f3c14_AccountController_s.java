 package example.springmvc.controller;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.CookieValue;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import example.springmvc.data.RegistrationError;
 import example.springmvc.data.Session;
 import example.springmvc.data.SessionStorage;
 import example.springmvc.data.User;
 import example.springmvc.data.UserRegistrationData;
 import example.springmvc.data.UserStorage;
 import example.springmvc.utils.Result;
 
 /**
  * The AccountController handles all requests for signing up, logging in
  * and logging out.
  * 
  * @author Daniel
  *
  */
 @Controller
 public class AccountController {
 	
 	@Autowired
 	private UserStorage userStorage;
 
 	@Autowired
 	private SessionStorage sessionStorage;
 
 	/**
 	 * Displays the signup form.
 	 * @return
 	 */
 	@RequestMapping(value = "/signup", method = RequestMethod.GET)
 	public String getSignupPage() {
 		return "signup";
 	}
 
 	/**
 	 * Handles requests to create a new user and runs some very basic
 	 * validation checks on the uploaded data.
 	 * @param userRegistrationData
 	 * 		uploaded account data as defined by the user 
 	 * @return
 	 */
 	@RequestMapping(value = "/signup", method = RequestMethod.POST)
 	public ModelAndView doSignup(UserRegistrationData userRegistrationData) {
 		ModelAndView mav = new ModelAndView();
 		Result<User, RegistrationError> result = this.userStorage
 				.createNewUser(userRegistrationData);
 		if (result.isSuccess()) {
 			mav.setViewName("redirect:/");
 		} else {
 			RegistrationError error = result.getError();
 			switch (error.getErrorType()) {
 			case USER_ALREADY_EXISTS:
 				mav.addObject("userId_error", error.getErrorMsg());
 				break;
 			case PASSWORDS_DONT_MATCH:
 				mav.addObject("password_error", "not_equal");
 				break;
 			case UNKNOWN_ERROR:
 				mav.addObject("error", error.getErrorMsg());
 				break;
 
 			default:
 				mav.addObject("error", error.getErrorMsg());
 				break;
 			}
 
 			mav.setViewName("signup");
 		}
 		return mav;
 	}
 
 	/**
 	 * Displays the login form.
 	 * @return
 	 */
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public ModelAndView getLoginPage() {
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("login");
 		return mav;
 	}
 
 	/**
 	 * Handles the login. If successful a session cookie will be set in
 	 * the response and the user is now officially "logged in".
 	 * @param userId
 	 * 		user name
 	 * @param password
 	 * 		password of the user
 	 * @param response
 	 * 		response object, is needed to set the cookie
 	 * @return
 	 */
 	@RequestMapping(value = "/login", method = RequestMethod.POST)
 	public ModelAndView doLogin(String userId, String password,
 			HttpServletResponse response) {
 
 		User user = this.userStorage.byId(userId);
 		if (user == null || !user.getPassword().equals(password)) {
 			ModelAndView mav = new ModelAndView();
 			mav.setViewName("login");
 			mav.addObject("login_error", true);
 			return mav;
 		} else {
 			Session session = this.sessionStorage.create(userId);
 
 			response.addCookie(new Cookie("sessionId", session.getId()));
 
 			ModelAndView mav = new ModelAndView();
 			mav.setViewName("redirect:/");
 			mav.addObject("userId", userId);
 			return mav;
 		}
 	}
 	
 	/**
 	 * Log out an user and delete the session cookie.
 	 * @param sessionId
 	 * 		session id which needs to be finished, this is stored in a cookie
 	 * @param response
 	 * 		response object, used to delete the cookie
 	 * @return
 	 */
 	@RequestMapping(value = "/logout", method = RequestMethod.POST)
 	public String doLogout(@CookieValue("sessionId") String sessionId, HttpServletResponse response) {
 		if (sessionId != null) {
 			this.sessionStorage.deleteById(sessionId);
 			Cookie cookie = new Cookie("sessionId", null);
 			cookie.setMaxAge(0);
 			response.addCookie(cookie);
 		}
 		return "redirect:/";
 	}
 
 	// getters and setters
 	
 	public UserStorage getUserStorage() {
 		return userStorage;
 	}
 
 	public void setUserStorage(UserStorage userStorage) {
 		this.userStorage = userStorage;
 	}
 
 	public SessionStorage getSessionStorage() {
 		return sessionStorage;
 	}
 
 	public void setSessionStorage(SessionStorage sessionStorage) {
 		this.sessionStorage = sessionStorage;
 	}
 }
