 package de.anycook.api.filter;
 
 import de.anycook.session.Session;
 import org.apache.log4j.Logger;
 
import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.container.ContainerRequestContext;
 import javax.ws.rs.container.ContainerRequestFilter;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.ext.Provider;
 
 /**
  * Created with IntelliJ IDEA.
  * User: moji8208
  * Date: 3/15/13
  * Time: 10:51 AM
  * To change this template use File | Settings | File Templates.
  */
 @Provider
 public class BackendAuthFilter implements ContainerRequestFilter {
    private HttpServletRequest request;
     private final Logger logger;
 
    public BackendAuthFilter(@Context HttpServletRequest request){
         logger = Logger.getLogger(getClass());
        this.request = request;
     }
 
     @Override
     public void filter(ContainerRequestContext containerRequest){
         String path = containerRequest.getUriInfo().getPath();
 
         if(path.startsWith("/backend")){
             logger.debug(String.format("Authfilter: %s", path));
            Session.init(request).checkAdminLogin();
         }
     }
 }
