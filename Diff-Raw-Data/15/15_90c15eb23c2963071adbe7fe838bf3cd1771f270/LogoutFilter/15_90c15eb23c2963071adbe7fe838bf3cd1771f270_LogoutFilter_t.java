 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.login.local.view;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.acegisecurity.ui.logout.LogoutHandler;
 
/** This logout filter handles the logout by calling the acegi implementation.
 *
 * It behaves as the original LogoutHandler, but if the logout succeed the
 * current request parameters are appended to the success url.
  *
  * @author mariano.nardi
  */
 public class LogoutFilter extends org.acegisecurity.ui.logout.LogoutFilter {
 
   /**
    * Creates a logout filters with the url to which the user will be forwarded
    * to and the set of handlers that will take care of the logout.
    *
    * @param logoutSuccessUrl the url where the user will be forwarded to once
    * the logout succeeds. It cannot be null or empty.
    *
    * @param handlers the collection of handlers that will take care of the
    * logout process. it cannot be empty.
    */
   public LogoutFilter(final String logoutSuccessUrl,
       final LogoutHandler[] handlers) {
     super(logoutSuccessUrl, handlers);
   }
 
   /**
    * Redirects the request to the given URL.
    *
    * It also appends the map of parameters to the url if they are available
    * in the request and the given url has no parameters yet.
    *
    * @param request the request associated with this forward.
    * @param response the server response in this forward.
    * @param url the url where the user will be redirected to.
    *
    * @throws IOException in case of an io error.
    */
   protected void sendRedirect(final HttpServletRequest request,
       final HttpServletResponse response, final String url)
       throws IOException {
     String redirectTo = url;
     if (!url.startsWith("http://") && !url.startsWith("https://")) {
       redirectTo = request.getContextPath() + redirectTo;
     }
     if (url.indexOf('?') == -1 && request.getQueryString() != null) {
       redirectTo += "?" + request.getQueryString();
     }
 
     response.sendRedirect(response.encodeRedirectURL(redirectTo));
   }
 }
 
