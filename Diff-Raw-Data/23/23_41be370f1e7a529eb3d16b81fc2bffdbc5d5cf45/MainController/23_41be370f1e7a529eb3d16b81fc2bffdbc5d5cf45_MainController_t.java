 package local.acme.library;
 
 import javax.servlet.http.*;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.CookieValue;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class MainController
 {
 	private static final Logger logger = LoggerFactory.getLogger(MainController.class);
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public ModelAndView processHome(Model model,
 			@CookieValue(value="sessionId", required=false) String sessionId,
 			@CookieValue(value="isAdmin", required=false) String isAdmin,
 			@RequestParam(value="filter", required=false) String filter, HttpServletResponse response)
 	{
 		logger.info("Processing home page...");
 
 		if (Common.isEmpty(sessionId))
 		{
 			sessionId = DatabaseOperations.createSession();
 			response.addCookie(new Cookie("sessionId", sessionId));
 		}
 
 		else
 		{
 			Session session = DatabaseOperations.getSession(sessionId);
 
			if (session == null) {
				sessionId = DatabaseOperations.createSession();
				response.addCookie(new Cookie("sessionId", sessionId));
			} else {
				int userId = session.getUserId();

				if (userId != 0)
				{
					User user = DatabaseOperations.getUserById(userId);

					model.addAttribute("userId", userId);
					model.addAttribute("userName", user.getUserName());
					model.addAttribute("isAdmin", isAdmin);
				}
 			}
 		}
 
 		model.addAttribute("sessionId", sessionId);
 		model.addAttribute("filter", filter);
 		model.addAttribute("books", DatabaseOperations.getBooks(filter));
 
 		return new ModelAndView("home");
 	}
 
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public ModelAndView processLogin(Model model,
 			@CookieValue(value="sessionId", required=false) String sessionId,
 			@RequestParam(value="username", required=false) String userName,
 			@RequestParam(value="password", required=false) String password, HttpServletResponse response)
 	{
 		logger.info("Processing login page...");
 
 		if (Common.isEmpty(userName) || Common.isEmpty(password))
 		{
 			model.addAttribute("error", "Please enter your username and password.");
 		}
 
 		else
 		{
 			User user = DatabaseOperations.getUserByName(userName);
 
 			if (user == null)
 			{
 				model.addAttribute("error", "The specified user could not be found.");
 			}
 
 			else if (!user.getPassword().equalsIgnoreCase(password))
 			{
 				model.addAttribute("error", "The username and/or password are incorrect.");
 			}
 
 			else
 			{
 				if (user.isAdmin()) response.addCookie(new Cookie("isAdmin", "yes"));
 				else response.addCookie(new Cookie("isAdmin", "no"));
 				DatabaseOperations.updateSession(sessionId, user.getId());
 				return new ModelAndView("redirect:/");
 			}
 		}
 
 		return new ModelAndView("login");
 	}
 
 	@RequestMapping(value = "/logout", method = RequestMethod.GET)
 	public ModelAndView processLogout(Model model, HttpServletResponse response)
 	{
 		logger.info("Processing logout page...");
 
         response.addCookie(new Cookie("sessionId", null));
         response.addCookie(new Cookie("isAdmin", "no"));
 
 		return new ModelAndView("redirect:/");
 	}
 
 	@RequestMapping(value = "/createaccount", method = RequestMethod.GET)
 	public ModelAndView processCreateAccount(Model model,
 			@RequestParam(value="username", required=false) String userName,
 			@RequestParam(value="password1", required=false) String password1,
 			@RequestParam(value="password2", required=false) String password2,
 			HttpServletResponse response)
 	{
 		logger.info("Processing logout page...");
 
 		if (Common.isEmpty(userName) || Common.isEmpty(password1) || Common.isEmpty(password2))
 		{
 			model.addAttribute("error", "Please enter an username and the same password twice.");
 		}
 
 		else if (!password1.equals(password2))
 		{
 			model.addAttribute("error", "Please make sure to enter the same password twice.");
 		}
 
 		else
 		{
 			User user = new User();
 			user.setUserName(userName);
 			user.setPassword(password1);
 
 			if (DatabaseOperations.createUser(user))
 			{
 				return new ModelAndView("redirect:/");
 			}
 
 			else
 			{
 				model.addAttribute("error", "Please make sure to enter the same password twice.");
 			}
 		}
 
 		return new ModelAndView("createaccount");
 	}
 
 	@RequestMapping(value = "/block", method = RequestMethod.GET)
 	public ModelAndView processBlockBook(Model model,
 			@CookieValue(value="isAdmin", required=false) String isAdmin,
 			@RequestParam(value="id") String bookId,
 			@RequestParam(value="flag") String flag,
 			HttpServletResponse response)
 	{
 		logger.info("Processing block page...");
 
 		if ("yes".equalsIgnoreCase(isAdmin))
 		{
 			if ("yes".equalsIgnoreCase(flag))
 			{
 				DatabaseOperations.blockBook(Integer.parseInt(bookId), true);
 			}
 			else if ("no".equalsIgnoreCase(flag))
 			{
 				DatabaseOperations.blockBook(Integer.parseInt(bookId), false);
 			}
 		}
 
 		return new ModelAndView("redirect:/");
 	}
 
 	@RequestMapping(value = "/reserve", method = RequestMethod.GET)
 	public ModelAndView processReserveBook(Model model,
 			@CookieValue(value="sessionId", required=false) String sessionId,
 			@CookieValue(value="userId", required=false) String userId,
 			@RequestParam(value="id") String bookId,
 			@RequestParam(value="flag") String flag,
 			HttpServletResponse response)
 	{
 		logger.info("Processing reserve page...");
 
 		Session session = DatabaseOperations.getSession(sessionId);
 
 		if ("yes".equalsIgnoreCase(flag))
 		{
 			DatabaseOperations.reserveBook(bookId, session.getUserId());
 		}
 
 		else if ("no".equalsIgnoreCase(flag))
 		{
 			DatabaseOperations.unreserveBook(bookId, session.getUserId());
 		}
 
 		return new ModelAndView("redirect:/");
 	}
 }
