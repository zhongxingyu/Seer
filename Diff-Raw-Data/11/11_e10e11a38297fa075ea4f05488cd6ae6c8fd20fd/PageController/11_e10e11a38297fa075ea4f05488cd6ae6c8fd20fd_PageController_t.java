 package se.softhouse.garden.web.publisher;
 
 import java.util.Locale;
 
 import javax.annotation.Resource;
 import javax.servlet.ServletException;
 
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
 
import se.softhouse.garden.orchid.commons.text.OrchidMessage;

 @RequestMapping("/{context:page|project|faq|people}")
 @Controller
 public class PageController {
 
 	@Resource
 	MessageSource messageSource;
 
 	@RequestMapping("/{index}")
 	public String page(@PathVariable String context, @PathVariable String index, Model model, Locale locale) throws NoSuchRequestHandlingMethodException {
 		String view = this.messageSource.getMessage(context + "." + index + ".view", null, "page/" + context, locale);
		String check = this.messageSource.getMessage("page." + context + ".check", new Object[] { OrchidMessage.arg("conext", context).arg("index", index) },
		        null, locale);
		if (check != null) {
			String prop = this.messageSource.getMessage(check, null, null, locale);
			if (prop == null) {
				throw new NoSuchRequestHandlingMethodException("page", PageController.class);
			}
		}
 		model.addAttribute("context", context);
 		model.addAttribute("index", index);
 		return view;
 	}
 
 	@ExceptionHandler(ServletException.class)
 	public String handleException(ServletException ex) {
 		return "resourceNotFound";
 	}
 
 }
