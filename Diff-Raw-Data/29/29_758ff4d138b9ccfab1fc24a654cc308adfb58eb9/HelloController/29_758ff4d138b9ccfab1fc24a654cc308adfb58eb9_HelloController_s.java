 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 
 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package ${package}.${moduleName}.view;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
 /** Spring MVC controller to show a hello message.
  */
 public class HelloController extends AbstractController {
 
   /** Forwards the request to the hello view.
    *
    * {@inheritDoc}
    */
 	@Override
   protected ModelAndView handleRequestInternal(final HttpServletRequest
       request, final HttpServletResponse response) throws Exception {
 		ModelAndView mav = new ModelAndView("hello");
 		return mav;
 	}
 }
 
