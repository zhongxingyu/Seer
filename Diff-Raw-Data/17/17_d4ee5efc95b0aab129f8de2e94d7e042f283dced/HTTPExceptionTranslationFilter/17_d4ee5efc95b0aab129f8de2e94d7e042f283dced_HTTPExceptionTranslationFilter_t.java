 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.openplans.security;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 
 import org.acegisecurity.AuthenticationException;
 import org.acegisecurity.AccessDeniedException;
 import org.acegisecurity.context.SecurityContextHolder;
 import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
 
 /**
  * Filter to do custom error handling for OpenPlans.org/NYMap application. 
  * The basic idea is to return HTTP error codes rather than just redirecting, with some custom
  * text in the body to make it easy for the NYMap webapp to figure out what happened.
  *
  * @author David Winslow <dwinslow@openplans.org>
  */
 public class HTTPExceptionTranslationFilter implements Filter{
 
     static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.community");
 
     public HTTPExceptionTranslationFilter(){
         LOGGER.info("Initializing HTTPExceptionTranslationFilter");
     }
 
     public void destroy(){}
 
     public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) 
         throws IOException, ServletException{
         LOGGER.info("Applying HTTPExceptionTranslationFilter...");
         try{
             chain.doFilter(req, resp);
         } catch (AuthenticationException ae){
             sendUnauthenticatedMessage(resp);
             
             LOGGER.info("Handled AuthenticationException.");
         } catch (AccessDeniedException ade){
             if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken){
                 sendUnauthenticatedMessage(resp);
             } else {
                 sendDeniedMessage(resp);
             }
             
            LOGGER.info("Handled " + ade); 
            LOGGER.info("Logged in as " + SecurityContextHolder.getContext().getAuthentication());
//            ade.printStackTrace();
         }
     }
 
     public void init(FilterConfig conf){
     }
 
     private void sendUnauthenticatedMessage(ServletResponse resp){
         if (resp instanceof HttpServletResponse){
             HttpServletResponse hres = (HttpServletResponse) resp;
             hres.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             hres.setContentType("text/plain");
             try{
                 hres.getWriter().write("Authentication needed.");
             } catch (IOException ioe){
                 // it's okay, we already set the status code
             }
         }
 
     }
 
     private void sendDeniedMessage(ServletResponse resp){
         if (resp instanceof HttpServletResponse){
             HttpServletResponse hres = (HttpServletResponse) resp;
             hres.setStatus(HttpServletResponse.SC_FORBIDDEN);
             hres.setContentType("text/plain");
             try{
                 hres.getWriter().write("Permission denied.");
             } catch (IOException ioe){
                 // it's okay, we already set the status code
             }
         }
     }
 
 }
