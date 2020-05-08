 package de.anycook.api.filter;
 
 import de.anycook.session.Session;
 import org.apache.log4j.Logger;
 
 import javax.ws.rs.container.ContainerRequestContext;
 import javax.ws.rs.container.ContainerRequestFilter;
 import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
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
    private HttpHeaders hh;
    private Session session;
     private final Logger logger;
 
    public BackendAuthFilter(@Context HttpHeaders hh, @Context Session session){
         logger = Logger.getLogger(getClass());
        this.hh = hh;
        this.session = session;
     }
 
     @Override
     public void filter(ContainerRequestContext containerRequest){
         String path = containerRequest.getUriInfo().getPath();
 
         if(path.startsWith("/backend")){
             logger.debug(String.format("Authfilter: %s", path));
            session.checkAdminLogin();
         }
     }
 }
