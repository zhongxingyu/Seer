 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.sdk.auth.servlet;
 
 import com.google.common.base.Function;
 import com.lyndir.lhunath.opal.system.logging.Logger;
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import net.link.safeonline.sdk.auth.filter.LoginManager;
 import net.link.safeonline.sdk.auth.protocol.*;
 import net.link.safeonline.sdk.configuration.LogoutContext;
 import net.link.safeonline.sdk.servlet.AbstractConfidentialLinkIDInjectionServlet;
 import net.link.util.error.ValidationFailedException;
 import net.link.util.servlet.ErrorMessage;
 import net.link.util.servlet.ServletUtils;
 import net.link.util.servlet.annotation.Init;
 
 
 /**
  * Abstract Logout Servlet. This servlet contains the landing page to finalize the logout process initiated by the web application. This
  * servlet also removes the {@code userId} attribute and redirects to the specified target when the logout request was made.
  * <p/>
  * This servlet also handles a logout request sent by the link ID authentication web application due to a single logout request sent by an
  * linkID application. After handling the request, it will redirect to {@code LogoutPath}. To finalize this, the web application should
  * redirect back to this page using Http GET, which will trigger this landing page to send back a logout response to the link ID
  * authentication web application.
  *
  * @author wvdhaute
  */
 public abstract class AbstractLogoutServlet extends AbstractConfidentialLinkIDInjectionServlet {
 
     private static final Logger logger = Logger.get( AbstractLogoutServlet.class );
 
     public static final String ERROR_PAGE_PARAM = "ErrorPage";
 
     @Init(name = ERROR_PAGE_PARAM, optional = true)
     private String errorPage;
 
     @Override
     protected void invokePost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         logger.dbg( "POST: %s", request.getParameterMap().keySet() );
 
         try {
             // Check for a linkID logout response (response to the application's logout request).
             LogoutProtocolResponseContext logoutResponse = ProtocolManager.findAndValidateLogoutResponse( request );
             if (logoutResponse != null) {
                 // There is a logout response, handle it: Log out and redirect back to the application.
                 if (logout( request.getSession(), logoutResponse ))
                     // Logout allowed by application, clean up credentials.
                     LoginManager.cleanup( request.getSession() );
 
                 String target = logoutResponse.getRequest().getTarget();
                 response.sendRedirect( target == null? "/": target );
                 return;
             }
 
             // Check for a linkID logout request (request as a result of another pool application's logout request to linkID).
             LogoutProtocolRequestContext logoutRequest = ProtocolManager.findAndValidateLogoutRequest( request, getContextFunction() );
             if (logoutRequest != null) {
                 boolean logoutAllowed = logout( request.getSession(), logoutRequest );
                 if (logoutAllowed && logoutRequest.getUserId().equals( LoginManager.findUserId( request.getSession() ) ))
                     // Logout allowed by application and requests logout for current session user: clean up credentials.
                     LoginManager.cleanup( request.getSession() );
 
                 logoutRequest.getProtocolHandler().sendLogoutResponse( response, logoutRequest, !logoutAllowed );
                 return;
             }
         }
         catch (ValidationFailedException e) {
             logger.err( e, ServletUtils.redirectToErrorPage( request, response, errorPage, null, new ErrorMessage( e ) ) );
             return;
         }
 
        ServletUtils.redirectToErrorPage( request, response, errorPage, null, new ErrorMessage( "Invalid logout request" ) );
     }
 
     /**
      * Override this method if you want to create a custom context for logout responses depending on the logout request.
      * <p/>
      * The standard implementation uses {@link LogoutContext#LogoutContext()}.
      *
      * @return A function that provides the context for validating and creating a logout response to a given logout request.
      */
     protected Function<LogoutProtocolRequestContext, LogoutContext> getContextFunction() {
 
         return new Function<LogoutProtocolRequestContext, LogoutContext>() {
             @Override
             public LogoutContext apply(LogoutProtocolRequestContext from) {
 
                 return new LogoutContext();
             }
         };
     }
 
     /**
      * Invoked when a logout request is received from linkID as a result of another application in the pool requesting a single logout.
      * <p/>
      * Implement this to log the application user out of the session and perform any other possible user session cleanup.
      * <p/>
      * A successful logout will also cause the SDK to remove its credentials from the HTTP session.  Return false here if the application
      * state requires the user is not logged out.  The SDK will leave its credentials on the HTTP session and the single logout process
      * initiated by the other pool application will be marked as 'partial'.
      * <p/>
      * You are allowed to invalidate the HTTP session from here for a quick and thorough logout; if that makes sense in your application
      * logic.
      *
      * @param logoutRequest linkID's logout request.
      *
      * @return true if the application allowed the user to log out.
      */
     protected abstract boolean logout(HttpSession session, LogoutProtocolRequestContext logoutRequest);
 
     /**
      * Invoked after a logout request from this application has been handled by linkID and a logout response from linkID is received.
      * <p/>
      * Implement this to log the application user out of the session and perform any other possible user session cleanup.
      * <p/>
      * A successful logout will also cause the SDK to remove its credentials from the HTTP session.  Return false here if the application
      * state requires the user is not logged out.  The SDK will leave its credentials on the HTTP session.
      * <p/>
      * You are allowed to invalidate the HTTP session from here for a quick and thorough logout; if that makes sense in your application
      * logic.
      *
      * @param logoutResponse linkID's response to our logout request.
      *
      * @return true if the application allowed the user to log out.
      */
     protected abstract boolean logout(HttpSession session, LogoutProtocolResponseContext logoutResponse);
 }
