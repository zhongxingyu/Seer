 package sample.exceptions;
 
 import org.springframework.web.servlet.HandlerExceptionResolver;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Hashtable;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dushyant
  * Date: 16/8/12
  * Time: 11:17 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class ApiExceptionResolver implements HandlerExceptionResolver {
 
     public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, Exception e) {
         MappingJacksonJsonView jv = new MappingJacksonJsonView();
         Hashtable<String, String> hs = new Hashtable<String, String>();
         hs.put("status", "failed");
         if (e instanceof NotAuthorisedException) {
             httpServletResponse.setStatus(401);
             hs.put("message", "User is not authorized");
         }
         else if (e instanceof ResourceNotFoundException) {
             httpServletResponse.setStatus(404);
         }
         else {
            httpServletResponse.setStatus(412);
             hs.put("error-message", e.getMessage());
         }
 
         return new ModelAndView(jv, hs);
     }
 }
