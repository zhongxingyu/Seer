 package exercisesmanager.web;
 
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.LocaleResolver;
 
 import pif.system.session.DataCart;
 
 @Controller
 @RequestMapping("init.htm")
 public class InitController {
 
 	protected final Log logger = LogFactory.getLog(getClass());
 	
 	@Autowired
 	private LocaleResolver localeResolver;
 
	@Autowired
	private MessageSource messageSource;
	
 	@RequestMapping(method = RequestMethod.GET)
 	public String get(final ModelMap model,  HttpServletRequest request, HttpServletResponse response,
 			HttpSession session) {
 		logger.info("Init: 'get'");
 		Locale locale = getLocale(request);
 		localeResolver.setLocale(request, response, locale);
 		if (!DataCart.isInitialized(session)) {
 			DataCart.initialize(session);
 			DataCart.setLanguage(session, locale.getLanguage());
 			DataCart.setUserAuthorizationLevel(session, new Integer(0));
 			logger.info(("init: container initialized"));
 		}
 		model.addAttribute(DataCart.DATACART, DataCart.getDataCart(session));
 		return "redirect:main.htm";
 	}
 	
 	private Locale getLocale(HttpServletRequest request) {
 		Locale locale = request.getLocale();
 		if ((locale.getLanguage().startsWith(Locale.ENGLISH.toString()))
 				|| (locale.getLanguage().startsWith(Locale.ITALIAN.toString()))
 				|| (locale.getLanguage().startsWith("hu"))) {
 			if (locale.getLanguage().startsWith("hu")) {
 				locale = new Locale.Builder().setLanguage("hu").build();
 			} else {
 				String language = locale.getLanguage();
 				locale = new Locale.Builder().setLanguage(language).build();
 			}
 		} else {
 			locale = Locale.ENGLISH;
 		}
 		return locale;
 	}
 
 }
