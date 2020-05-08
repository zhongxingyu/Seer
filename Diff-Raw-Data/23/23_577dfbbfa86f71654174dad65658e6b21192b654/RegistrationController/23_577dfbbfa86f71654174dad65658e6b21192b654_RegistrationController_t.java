 package net.iteach.web.security;
 
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import net.iteach.api.SecurityService;
 import net.iteach.core.model.Ack;
 import net.iteach.core.model.AuthenticationMode;
 import net.iteach.core.model.UserMessage;
 import net.iteach.utils.InputException;
 import net.iteach.web.support.AbstractGUIController;
 import net.iteach.web.support.ErrorHandler;
 import net.iteach.web.support.ErrorMessage;
import net.iteach.web.support.WebUtils;
 import net.sf.jstring.Strings;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class RegistrationController extends AbstractGUIController {
 
 	private final SecurityService securityService;
 	private final Strings strings;
 
 	@Autowired
 	public RegistrationController(SecurityService securityService, ErrorHandler errorHandler, Strings strings) {
 		super(errorHandler);
 		this.securityService = securityService;
 		this.strings = strings;
 	}
 	
 	@ExceptionHandler(InputException.class)
 	public ModelAndView onInputException (HttpServletRequest request, HttpSession session, Locale locale, Exception ex) {
 		// Model
 		ModelAndView mav = new ModelAndView("registrationForm");
 		// Error message
 		ErrorMessage error = errorHandler.handleError (request, locale, ex);
 		// Error
 		mav.addObject("error", error);
 		// Mode
 		mav.addObject("mode", session.getAttribute(SessionKeys.USER_SECURITY_MODE));
 		// Identifier
 		String identifier = (String) session.getAttribute(SessionKeys.USER_OPENID_CREDENTIAL);
 		if (identifier == null) {
 			identifier = "";
 		}
 		mav.addObject("identifier", identifier);
        // Prepare the view
        WebUtils.prepareModelAndView(mav, request);
        // OK
 		return mav;
 	}
 
 	/**
 	 * @see OpenIDAuthenticationFailureHandler
 	 */
 	@RequestMapping(value = "/registration_openid", method = RequestMethod.GET)
 	public String registrationOpenID(Model model, HttpSession session) {
 		String identifier = (String) session.getAttribute(SessionKeys.USER_OPENID_CREDENTIAL);
 		model.addAttribute("identifier", identifier);
 		model.addAttribute("mode", AuthenticationMode.openid);
 		return registrationForm(model);
 	}
 
 	/**
 	 * Used to register as a simple user
 	 */
 	@RequestMapping(value = "/register", method = RequestMethod.GET)
 	public String register(Model model) {
 		model.addAttribute("mode", AuthenticationMode.password);
 		return registrationForm(model);
 	}
 
 	protected String registrationForm(Model model) {
 		model.addAttribute("admin", !securityService.isAdminInitialized());
 		return "registrationForm";
 	}
 
 	@RequestMapping(value = "/registration", method = RequestMethod.POST)
 	public String registration(Locale locale, Model model, HttpSession session, AuthenticationMode mode, String identifier, String firstName, String lastName, String email, String password) {
 		// Session
 		session.setAttribute(SessionKeys.USER_SECURITY_MODE, mode);
 		// Registration
 		Ack ack = securityService.register (locale, mode, identifier, firstName, lastName, email, password);
 		// OK
 		if (ack.isSuccess()) {
 			return loginOkNow(locale, model);
 		} else {
 			return loginValidationNeeded(locale, model);
 		}
 	}
 
 	/**
 	 * Redirect to the login with a message saying that the registration has been successful, but
 	 * needs to be confirmed.
 	 */
 	protected String loginValidationNeeded(Locale locale, Model model) {
 		model.addAttribute("message", UserMessage.warning(strings.get(locale, "login.registrationConfirmationNeeded")));
 		return "login";
 	}
 
 	/**
 	 * Redirect to the login with a message saying that the registration has been successful
 	 */
 	protected String loginOkNow(Locale locale, Model model) {
 		model.addAttribute("message", UserMessage.success(strings.get(locale, "login.registrationOK")));
 		return "login";
 	}
 
 }
