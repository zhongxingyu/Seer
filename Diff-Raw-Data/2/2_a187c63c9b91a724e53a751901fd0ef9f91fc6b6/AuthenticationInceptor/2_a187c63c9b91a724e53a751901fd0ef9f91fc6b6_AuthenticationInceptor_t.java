 package net.kokkeli.resources.authentication;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 
 import net.kokkeli.ISettings;
 import net.kokkeli.data.ILogger;
 import net.kokkeli.data.LogSeverity;
 import net.kokkeli.data.Logging;
 import net.kokkeli.data.Role;
 import net.kokkeli.data.db.NotFoundInDatabase;
 import net.kokkeli.data.services.ISessionService;
 import net.kokkeli.resources.Access;
 import net.kokkeli.data.*;
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 
 import com.google.inject.Inject;
 
 /**
  * Authentication checker class
  * @author Hekku2
  */
 public class AuthenticationInceptor implements MethodInterceptor{
     
     /**
      * ILogger. This is protected for injecting.
      */
     @Inject
     protected ILogger logger;
     
     /**
      * ISessionService. This is proteced for injecting.
      */
     @Inject
     protected ISessionService sessions;
     
     /**
      * ISettings. This is protected for injecting
      */
     @Inject
     protected ISettings settings;
     
     /**
      * Creates authencation inceptor for catching Access-annotations
      */
     public AuthenticationInceptor(){
     }
     
     /**
      * This is invoked before method with Access-annotation is invoked.
      */
     public Object invoke(MethodInvocation invocation) throws Throwable {
         try {
             ILogger logger = new Logging();
             
             logger.log("Checking if all can access...", LogSeverity.TRACE);
             Access access = AuthenticationUtils.extractRoleAnnotation(invocation.getMethod().getAnnotations());
             
             //If no role is needed, continue proceeded without checking authentication
             if (access.value() == Role.NONE){
                 return invocation.proceed();
             }
 
             logger.log("Checking authentication...", LogSeverity.TRACE);
             HttpServletRequest request = AuthenticationUtils.extractRequest(invocation.getArguments());
             Cookie authCookie = AuthenticationUtils.extractLoginCookie(request.getCookies());
             Session session = sessions.get(authCookie.getValue());
            logger.log("User authenticated: " + session.getUser().getUserName(), LogSeverity.TRACE);
             
             return invocation.proceed();
         } catch (NotFoundInDatabase e) {
             logger.log("Old or invalid authentication." + e.getMessage(), LogSeverity.DEBUG);
             return Response.seeOther(UriBuilder.fromUri(settings.getBaseURI()).path("/authentication").build()).build();
         } catch (AuthenticationException e) {
             logger.log("There were no authenticaiton data: " + e.getMessage(), LogSeverity.DEBUG);
             return Response.seeOther(UriBuilder.fromUri(settings.getBaseURI()).path("/authentication").build()).build();
         }
 
         
     }
 }
