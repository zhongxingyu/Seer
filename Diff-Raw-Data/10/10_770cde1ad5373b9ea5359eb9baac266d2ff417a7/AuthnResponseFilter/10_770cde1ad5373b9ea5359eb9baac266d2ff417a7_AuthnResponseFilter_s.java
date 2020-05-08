 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.sdk.auth.filter;
 
 import com.google.common.base.Function;
 import com.lyndir.lhunath.opal.system.logging.Logger;
 import java.io.IOException;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import net.link.safeonline.sdk.auth.protocol.AuthnProtocolResponseContext;
 import net.link.safeonline.sdk.auth.protocol.ProtocolManager;
 import net.link.safeonline.sdk.configuration.AuthenticationContext;
 import net.link.util.exception.ValidationFailedException;
 import net.link.util.servlet.ErrorMessage;
 import net.link.util.servlet.ServletUtils;
 
 
 /**
  * This filter performs the actual login using the identity as received from the SafeOnline authentication web application.
  *
  * @author fcorneli
  */
 public class AuthnResponseFilter implements Filter {
 
     private static final Logger logger = Logger.get( AuthnResponseFilter.class );
 
     public static final String ERROR_PAGE = "ErrorPage";
 
     private String errorPage;
 
     @Override
     public void destroy() {
 
     }
 
     @Override
     public void init(final FilterConfig filterConfig)
             throws ServletException {
 
         this.errorPage = filterConfig.getInitParameter( ERROR_PAGE );
         if (null == this.errorPage)
             this.errorPage = filterConfig.getServletContext().getInitParameter( ERROR_PAGE );
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
             throws IOException, ServletException {
 
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response;
 
         try {
             AuthnProtocolResponseContext authnResponse = ProtocolManager.findAndValidateAuthnResponse( httpRequest, getContextFunction() );
             if (null == authnResponse)
                 authnResponse = ProtocolManager.findAndValidateAuthnAssertion( httpRequest, getContextFunction() );
             if (null != authnResponse)
                 onLogin( httpRequest.getSession(), authnResponse );
         }
         catch (ValidationFailedException e) {
             logger.err( e, "Validation failed: %s", e.getMessage() );
             ServletUtils.redirectToErrorPage( httpRequest, httpResponse, errorPage, null, new ErrorMessage( e ) );
         }
         catch (RuntimeException e) {
            logger.err( e, e.getMessage() );
         }
 
         chain.doFilter( request, response );
     }
 
     /**
      * Override this method if you want to create a custom context for detached authentication responses.
      * <p/>
      * The standard implementation uses {@link AuthenticationContext#AuthenticationContext()}.
      *
      * @return A function that provides the context for validating detached authentication responses (assertions).
      */
     protected Function<AuthnProtocolResponseContext, AuthenticationContext> getContextFunction() {
 
         return new Function<AuthnProtocolResponseContext, AuthenticationContext>() {
             @Override
             public AuthenticationContext apply(final AuthnProtocolResponseContext from) {
 
                 return new AuthenticationContext();
             }
         };
     }
 
     /**
      * Invoked when an authentication response is received.  The default implementation sets the user's credentials on the session if the
      * response was successful and does nothing if it wasn't.
      *
      * @param session       The HTTP session within which the response was received.
      * @param authnResponse The response that was received.
      */
     protected void onLogin(HttpSession session, AuthnProtocolResponseContext authnResponse) {
 
         if (authnResponse.isSuccess()) {
             logger.dbg( "userId: %s", authnResponse.getUserId() );
 
             LoginManager.set( session, authnResponse );
         }
     }
 }
