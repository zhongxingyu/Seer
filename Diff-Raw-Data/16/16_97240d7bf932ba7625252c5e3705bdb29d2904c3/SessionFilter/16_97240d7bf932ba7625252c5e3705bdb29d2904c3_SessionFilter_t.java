 /* **********************************************************************
     Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 package edu.rpi.sss.util.servlets;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpSessionContext;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 /** This filter wraps the request and returns a wrapper to the HttpSession
  * object on calls to getSession().
  *
  * <p>This wrapper prefixes the attribute names on get/setAttribute if the name
  * does not start with the prefix. The purpose is to ensure name uniqueness when
  * applications share the same session. This is a temporary fix to what appears
  * to be a jetspeed2 or portal-bridge bug.
  *
  * <p>We may still end up getting access to the real session via session
  * listeners so we need to take care. This may not be a complete solution.
  *
  *   @author Mike Douglass   douglm @ rpi.edu
  */
@SuppressWarnings("deprecation")
 public class SessionFilter implements Filter {
   private ServletContext ctx;
   private boolean debug = false;
 
   private static class HttpSessionWrapper implements HttpSession {
     private HttpSession sess;
     private String attributePrefix;
 
     HttpSessionWrapper(String attributePrefix) {
       this.attributePrefix = attributePrefix;
     }
 
     void setSession(HttpSession sess) {
       this.sess = sess;
     }
 
     public long getCreationTime() throws IllegalStateException {
       return sess.getCreationTime();
     }
 
     public String getId() throws IllegalStateException {
       return sess.getId();
     }
 
     public long getLastAccessedTime() throws IllegalStateException {
       return sess.getLastAccessedTime();
     }
 
     public ServletContext getServletContext() {
       return sess.getServletContext();
     }
 
     public void setMaxInactiveInterval(int interval) {
       sess.setMaxInactiveInterval(interval);
     }
 
     public int getMaxInactiveInterval() {
       return sess.getMaxInactiveInterval();
     }
 
     public HttpSessionContext getSessionContext() {
       return sess.getSessionContext();
     }
 
     public Object getAttribute(String name) throws IllegalStateException {
       return sess.getAttribute(makeName(name));
     }
 
     public Object getValue(String name) throws IllegalStateException {
       return sess.getAttribute(makeName(name));
     }
 
     public Enumeration getAttributeNames() throws IllegalStateException {
       return sess.getAttributeNames();
     }
 
     public String[] getValueNames() throws IllegalStateException {
       return sess.getValueNames();
     }
 
     public void setAttribute(String name,
                              Object value) throws IllegalStateException {
       sess.setAttribute(makeName(name), value);
     }
 
     public void putValue(String name,
                          Object value) throws IllegalStateException {
       sess.setAttribute(makeName(name), value);
     }
 
     public void removeAttribute(String name) throws IllegalStateException {
       sess.removeAttribute(makeName(name));
     }
 
     public void removeValue(String name) throws IllegalStateException {
       sess.removeAttribute(makeName(name));
     }
 
     public void invalidate() throws IllegalStateException {
       sess.invalidate();
     }
 
     public boolean isNew() throws IllegalStateException {
       return sess.isNew();
     }
 
     private String makeName(String name) {
       if (name == null) {
         return name;
       }
 
       if (name.startsWith(attributePrefix)) {
         return name;
       }
 
       return attributePrefix + name;
     }
   }
 
   private static class WrappedRequest extends HttpServletRequestWrapper {
     HttpSessionWrapper sess;
     HttpServletRequest req;
 
     WrappedRequest(HttpServletRequest req,
                    HttpSessionWrapper sess) {
       super(req);
       sess.setSession(req.getSession());
       this.sess = sess;
       this.req = req;
     }
 
     public HttpSession getSession() {
       return sess;
     }
 
     public HttpSession getSession(boolean create) {
       boolean exists = req.getSession() != null;
 
       if (!exists) {
         if (!create) {
           return null;
         }
         sess.setSession(req.getSession(true));
       }
 
       return sess;
     }
   }
 
   private String attributePrefix;
 
   /* (non-Javadoc)
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig filterConfig) throws ServletException {
     ctx = filterConfig.getServletContext();
     String temp = filterConfig.getInitParameter("debug");
     debug = (String.valueOf(temp).equals("true"));
 
     attributePrefix = filterConfig.getInitParameter("attributePrefix") + "?";
   }
 
   public void doFilter(ServletRequest req,
                        ServletResponse response,
                        FilterChain filterChain)
          throws IOException, ServletException {
     HttpServletRequest hreq = (HttpServletRequest)req;
     HttpSessionWrapper sessWrapper = new HttpSessionWrapper(attributePrefix);
 
     /* See if we're already in the chain. Only need to wrap once.
      */
     ServletRequest r = hreq;
     boolean wrapped = false;
 
     while (r instanceof HttpServletRequestWrapper) {
       if (r instanceof WrappedRequest) {
         wrapped = true;
         break;
       }
 
       r = ((HttpServletRequestWrapper)r).getRequest();
     }
 
     if (!wrapped) {
       hreq = new WrappedRequest(hreq, sessWrapper);
     }
 
     filterChain.doFilter(hreq, response);
   }
 
   public void destroy() {
     if (debug) {
       ctx.log("Destroying filter...");
     }
   }
 
   /**
    * @param val
    */
   public void setDebug(boolean val) {
     debug = val;
   }
 
   /**
    * @return debug flag
    */
   public boolean getDebug() {
     return debug;
   }
 }
 
