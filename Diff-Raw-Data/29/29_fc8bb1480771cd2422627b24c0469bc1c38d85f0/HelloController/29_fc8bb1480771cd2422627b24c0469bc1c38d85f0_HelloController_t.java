 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 
 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package ${package}.${moduleName}.view;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.Authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 /** Spring MVC controller to show a hello message.
  */
 public class HelloController extends AbstractController {
 
  /** The class logger.
   */
  private static Logger log = LoggerFactory.getLogger(HelloController.class);

   /** Forwards the request to the hello view.
    *
   * The model includes the userName, or an empty string if the user name could
   * not be determined.
   *
    * {@inheritDoc}
    */
 	@Override
   protected ModelAndView handleRequestInternal(final HttpServletRequest
       request, final HttpServletResponse response) throws Exception {
    log.trace("Entering handleRequestInternal");
 		ModelAndView mav = new ModelAndView("hello");

    Authentication authentication;
    authentication = SecurityContextHolder.getContext().getAuthentication();
    String userName = "";
    if (authentication != null) {
      userName = authentication.getName();
    }

    mav.addObject("userName", userName);

    log.trace("Leaving handleRequestInternal");
 		return mav;
 	}
 }
 
