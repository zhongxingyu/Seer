 package $package;
 
 import agave.AbstractHandler;
import agave.Destination;
 import agave.HandlesRequestsTo;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class WelcomeHandler extends AbstractHandler {
 
     @HandlesRequestsTo("/")
    public Destination welcome() throws Exception {
        request.setAttribute("context", request.getContextPath().substring(1));
        return new Destination("/WEB-INF/jsp/index.jsp");
     }
     
 }
