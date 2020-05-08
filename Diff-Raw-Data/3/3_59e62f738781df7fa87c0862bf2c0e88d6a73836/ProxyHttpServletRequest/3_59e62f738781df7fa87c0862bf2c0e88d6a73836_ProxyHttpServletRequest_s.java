 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.impl.push.servlet;
 
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.Part;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class ProxyHttpServletRequest implements HttpServletRequest {
     private static Logger log = Logger.getLogger(ProxyHttpServletRequest.class.getName());
     private FacesContext facesContext;
     private ExternalContext externalContext;
 
     public ProxyHttpServletRequest(FacesContext facesContext) {
         this.facesContext = facesContext;
         externalContext = facesContext.getExternalContext();
     }
 
     public javax.servlet.DispatcherType getDispatcherType()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  void setAsyncTimeout(long timeout)  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return ;
     }
 
     public  long getAsyncTimeout()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return -1;
     }
 
     public  void addAsyncListener(javax.servlet.AsyncListener listener)  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return;
     }
 
     public  void addAsyncListener(javax.servlet.AsyncListener listener, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response)  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return ;
     }
 
     public  boolean isAsyncStarted(){
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return false;
     }
 
     public  boolean isAsyncSupported(){
         log.fine("Asynchronous servlet API not currently supported in portlets");
         return false;
     }
 
     public  javax.servlet.AsyncContext getAsyncContext()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public javax.servlet.AsyncContext startAsync()       throws java.lang.IllegalStateException  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
     
     public javax.servlet.AsyncContext startAsync(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response)       throws java.lang.IllegalStateException  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
     
     public  javax.servlet.ServletContext getServletContext() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.Object getAttribute(java.lang.String name) {
         return facesContext.getExternalContext().getRequestMap().get(name);
     }
 
     public  java.util.Enumeration getAttributeNames()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getCharacterEncoding()  {
         Object result = getMethodAndInvoke(externalContext.getRequest(),"getCharacterEncoding");
         return (String)result;
     }
 
     public  void setCharacterEncoding(java.lang.String encoding)       throws java.io.UnsupportedEncodingException  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return ;
     }
 
     public  int getContentLength()  {
         return externalContext.getRequestContentLength();
     }
 
     public  java.lang.String getContentType()  {
         //Normally we'd rely on the ExternalContext but the portlet bridge returns
         //an incorrect content-type for the Ajax request used for fileUpload so we
         //rely on the underlying request value if possible.
         Object result = getMethodAndInvoke(externalContext.getRequest(),"getContentType");
         if( result != null ){
             return (String)result;
         }
         return externalContext.getRequestContentType();
     }
 
     public  javax.servlet.ServletInputStream getInputStream() throws java.io.IOException  {
         //Since servlet requests and portlet requests have different return values, we
         //check if there is a servlet version first.  If not, get the portlet version
         //and wrap it to make it look like a ServletInputStream.
         Object result = getMethodAndInvoke(externalContext.getRequest(),"getInputStream");
         if( result != null ){
             return (javax.servlet.ServletInputStream)result;
         }
 
         result = getMethodAndInvoke(externalContext.getRequest(),"getPortletInputStream");
         if( result != null){
             ProxyServletInputStream proxy = new ProxyServletInputStream((InputStream)result);
             return (javax.servlet.ServletInputStream)proxy;
         }
         return null;
     }
 
     public  java.lang.String getParameter(java.lang.String name)  {
         Map requestParameterMap = facesContext.getExternalContext().getRequestParameterMap();
         return (String) requestParameterMap.get(name);
     }
 
     public  java.util.Enumeration getParameterNames()  {
         Set paramNames = facesContext.getExternalContext().getRequestParameterMap().keySet();
         return Collections.enumeration(paramNames);
     }
 
     public  java.lang.String[] getParameterValues(java.lang.String name)  {
         return facesContext.getExternalContext().getRequestParameterValuesMap().get(name);
     }
 
     public  java.util.Map getParameterMap()  {
         return facesContext.getExternalContext().getRequestParameterMap();
     }
 
     public  java.lang.String getProtocol()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getScheme()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getServerName()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  int getServerPort()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return -1;
     }
 
     public  java.io.BufferedReader getReader()       throws java.io.IOException  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getRemoteAddr()  {
        log.severe("ProxyHttpServletRequest unsupported operation");
        if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getRemoteHost()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  void setAttribute(java.lang.String name, java.lang.Object value)  {
         facesContext.getExternalContext().getRequestMap().put(name, value);
     }
 
     public  void removeAttribute(java.lang.String name)  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return ;
     }
 
     public  java.util.Locale getLocale()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.util.Enumeration getLocales()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public boolean isSecure() {
         boolean isSecure = false;
         Object result = getMethodAndInvoke(externalContext.getRequest(), "isSecure");
         if (result != null) {
             isSecure = ((Boolean) result).booleanValue();
         }
         return isSecure;
     }
 
     public  javax.servlet.RequestDispatcher getRequestDispatcher(java.lang.String dispatcher)  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getRealPath(java.lang.String path)  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  int getRemotePort()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return -1;
     }
 
     public  java.lang.String getLocalName()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getLocalAddr()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  int getLocalPort()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return -1;
     }
 
 
     public  java.lang.String getAuthType()  {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  javax.servlet.http.Cookie[] getCookies() {
         Cookie[] cookies = new Cookie[0];
         Map cookieMap = facesContext.getExternalContext().getRequestCookieMap();
         if( cookieMap == null  || cookieMap.values() == null ){
             return cookies;
         }
         return (Cookie[]) cookieMap.values().toArray(cookies);
     }
 
     public  long getDateHeader(String name) {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return -1;
     }
 
     public  java.lang.String getHeader(String name) {
         return facesContext.getExternalContext().getRequestHeaderMap().get(name);
     }
 
     public  java.util.Enumeration<java.lang.String> getHeaders(String name) {
         String[] headers = facesContext.getExternalContext().getRequestHeaderValuesMap().get(name);
         return Collections.enumeration(Arrays.asList(headers));
     }
 
     public  java.util.Enumeration<java.lang.String> getHeaderNames() {
         Set headerNames = facesContext.getExternalContext().getRequestHeaderMap().keySet();
         return Collections.enumeration(headerNames);
     }
 
     public  int getIntHeader(String name) {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return -1;
     }
 
     public  java.lang.String getMethod() {
         // ICE-6371: The getMethod call is not available on the ExternalContext.  While it's
         // supported on HttpServletRequest, it's not available on all types of Portlet 2.0
         // requests so we do it reflectively. If it's there, we use it.  If not
         // we just return null rather than fail with an exception.
         Object result = getMethodAndInvoke(externalContext.getRequest(),"getMethod");
         return (String)result;
     }
 
     public  java.lang.String getPathInfo() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getPathTranslated() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getContextPath() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getQueryString() {
         //get from requestParameterMap
         return "";
     }
 
     public  java.lang.String getRemoteUser() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  boolean isUserInRole(String role) {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return false;
     }
 
     public  java.security.Principal getUserPrincipal() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getRequestedSessionId() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  java.lang.String getRequestURI() {
         String resourceName = facesContext.getExternalContext()
             .getRequestParameterMap().get("javax.faces.resource");
         if (null != resourceName)  {
             return resourceName;
         }
         return null;
     }
 
     public  java.lang.StringBuffer getRequestURL() {
         String resourceName = facesContext.getExternalContext()
             .getRequestParameterMap().get("javax.faces.resource");
         if (null != resourceName)  {
             return new StringBuffer(resourceName);
         }
         return null;
     }
 
     public  java.lang.String getServletPath() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  javax.servlet.http.HttpSession getSession(boolean create) {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  javax.servlet.http.HttpSession getSession() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  boolean isRequestedSessionIdValid() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return false;
     }
 
     public  boolean isRequestedSessionIdFromCookie() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return false;
     }
 
     public  boolean isRequestedSessionIdFromURL() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return false;
     }
 
     public  boolean isRequestedSessionIdFromUrl() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return false;
     }
 
     public  boolean authenticate(javax.servlet.http.HttpServletResponse response)       throws java.io.IOException, javax.servlet.ServletException {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return false;
     }
 
     public  void login(String s1, String s2)       throws javax.servlet.ServletException {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return;
     }
 
     public  void logout()       throws javax.servlet.ServletException {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return ;
     }
 
     public Collection<Part> getParts() {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     public  javax.servlet.http.Part getPart(String name)       throws java.lang.IllegalArgumentException {
         log.severe("ProxyHttpServletRequest unsupported operation");
         if (true) throw new UnsupportedOperationException();
         return null;
     }
 
     /**
      * Convenience method for getting and invoking a method with no parameters via reflection.
      *
      * @param obj  The instance to get the method from and invoke it on
      * @param method  The name of the method to look for and invoke on the object instance.
      * @return The result, if any, of the invoked method.
      */
     private static Object getMethodAndInvoke(Object obj, String method){
         try {
             Method meth = obj.getClass().getMethod(method, new Class[0]);
             Object result = meth.invoke(obj);
             return result;
         } catch (Exception e) {
             log.log(Level.FINE, "problem getting " + method + " from " + obj, e);
         }
         return null;
     }
 
     public String getUserAgentFromPortletFacesBridge(){
         //The PortletFaces Bridge does not include the user-agent header in the map so we need to try
         //a more roundabout way of retrieving it if possible.  This means reflectively getting the
         //original HttpServletRequest and querying it for the user-agent header.
         Object origRequest = getMethodAndInvoke(externalContext.getRequest(),"getHttpServletRequest");
 
         if(origRequest == null ){
             return null;
         }
 
         String userAgent = null;
         try {
             Method headerMeth = origRequest.getClass().getMethod("getHeader", String.class);
             Object result = headerMeth.invoke(origRequest,"user-agent");
             userAgent = (String)result;
         } catch (Exception e) {
             log.log(Level.FINE, "problem calling getHeader('user-agent') using " + origRequest, e);
         }
 
         return userAgent;
     }
 
 }
 
 /**
  * A class that makes an InputStream look like a ServletInputStream
  */
 class ProxyServletInputStream extends javax.servlet.ServletInputStream {
 
     private InputStream portletInputStream;
 
     ProxyServletInputStream( InputStream portletInputStream) {
         this.portletInputStream = portletInputStream;
     }
 
     @Override
     public int read() throws IOException {
         return portletInputStream.read();
     }
 }
