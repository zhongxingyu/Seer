 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.core.context.portlet;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.Principal;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.FacesException;
 import javax.faces.application.ViewHandler;
 import javax.faces.context.ExternalContext;
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletContext;
 import javax.portlet.PortletException;
 import javax.portlet.PortletRequest;
 import javax.portlet.PortletRequestDispatcher;
 import javax.portlet.PortletResponse;
 import javax.portlet.PortletSession;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 
 import org.seasar.framework.container.external.portlet.PortletApplicationMap;
 import org.seasar.framework.container.external.portlet.PortletInitParameterMap;
 import org.seasar.framework.container.external.portlet.PortletRequestHeaderMap;
 import org.seasar.framework.container.external.portlet.PortletRequestHeaderValuesMap;
 import org.seasar.framework.container.external.portlet.PortletRequestMap;
 import org.seasar.framework.container.external.portlet.PortletRequestParameterMap;
 import org.seasar.framework.container.external.portlet.PortletRequestParameterValuesMap;
 import org.seasar.framework.container.external.portlet.PortletSessionMap;
 import org.seasar.framework.log.Logger;
 import org.seasar.framework.util.AssertionUtil;
 import org.seasar.framework.util.EnumerationIterator;
 
 /**
  * PortletExternalContextImpl is ExternalContext implementation for Portlet environment.
  * 
  * @author shot
  * @author shinsuke
  * 
  */
 public class PortletExternalContextImpl extends ExternalContext {
 
     private static Logger logger = Logger
             .getLogger(PortletExternalContextImpl.class);
 
     private static final String INIT_PARAMETER_MAP_ATTRIBUTE = PortletInitParameterMap.class
             .getName();
 
     private static final String SESSION_NAMESPACE = PortletExternalContextImpl.class
             .getName()
             + ".Namespace";
 
     private static final Map EMPTY_UNMODIFIABLE_MAP = Collections
             .unmodifiableMap(new HashMap(0));
 
     private PortletContext portletContext;
 
     private PortletRequest portletRequest;
 
     private PortletResponse portletResponse;
 
     private Map applicationMap;
 
     private Map sessionMap;
 
     private Map requestMap;
 
     private Map requestParameterMap;
 
     private Map requestParameterValuesMap;
 
     private Map requestHeaderMap;
 
     private Map requestHeaderValuesMap;
 
     private Map initParameterMap;
 
     private boolean isActionRequest;
 
     public PortletExternalContextImpl(PortletContext context,
             PortletRequest request, PortletResponse response) {
         this.portletContext = context;
         this.portletRequest = request;
         this.portletResponse = response;
         this.isActionRequest = (portletRequest != null && portletRequest instanceof ActionRequest);
 
         if (isActionRequest) {
             // ActionRequest/ActionResponse
 
             ActionRequest actionRequest = (ActionRequest) portletRequest;
 
             // Section 2.5.2.2 in JSF 1.1 spec
             String contentType = portletRequest.getProperty("Content-Type");
 
             String characterEncoding = null;
             if (contentType != null) {
                 int charIndex = contentType.indexOf("charset=");
                 if (charIndex != -1) {
                     characterEncoding = contentType.substring(charIndex + 8);
                 }
             }
 
             if (characterEncoding == null) {
                 PortletSession session = portletRequest
                         .getPortletSession(false);
 
                 if (session != null) {
                     characterEncoding = (String) session.getAttribute(
                             ViewHandler.CHARACTER_ENCODING_KEY,
                             PortletSession.PORTLET_SCOPE);
                 }
             }
 
             // Set the encoding to the request if the request is ActionRequest.
             if (characterEncoding != null) {
                 try {
                     actionRequest.setCharacterEncoding(characterEncoding);
                 } catch (UnsupportedEncodingException e) {
                     logger.warn("The specified encoding is wrong: "
                             + characterEncoding, e);
                 } catch (IllegalStateException e) {
                     logger
                             .warn(
                                     "setCharacterEncoding(String) must not be called "
                                             + "after reading request parameters or reading input using getReader()",
                                     e);
                 }
             }
 
         } else {
             // RenderRequest/RenderResponse
 
             Map sessionMap = getSessionMap();
             sessionMap.put(SESSION_NAMESPACE,
                     ((RenderResponse) portletResponse).getNamespace());
         }
     }
 
     public void dispatch(String path) throws IOException {
         if (isActionRequest) {
             throw new IllegalStateException(
                     "Cannot call dispatch(String) if the reqeust is ActionRequest.");
         }
 
         PortletRequestDispatcher requestDispatcher = portletContext
                 .getRequestDispatcher(path);
 
         try {
             requestDispatcher.include((RenderRequest) portletRequest,
                     (RenderResponse) portletResponse);
         } catch (PortletException e) {
             throw new FacesException(
                     "Failed to include the content of a resource in the response.",
                     e);
         }
     }
 
     public String encodeActionURL(String url) {
         AssertionUtil.assertNotNull("url is null.", url);
         return portletResponse.encodeURL(url);
     }
 
     public String encodeNamespace(String name) {
         return name + getNamespace();
     }
 
     public String encodeResourceURL(String url) {
         AssertionUtil.assertNotNull("url is null.", url);
        if (url.indexOf("://") == -1 && !url.startsWith("/")) {
            return url;
        } 
         return portletResponse.encodeURL(url);
     }
 
     public Map getApplicationMap() {
         if (applicationMap == null) {
             applicationMap = new PortletApplicationMap(portletContext);
         }
         return applicationMap;
     }
 
     public String getAuthType() {
         return portletRequest.getAuthType();
     }
 
     public Object getContext() {
         return portletContext;
     }
 
     public String getInitParameter(String name) {
         return portletContext.getInitParameter(name);
     }
 
     public Map getInitParameterMap() {
         if (initParameterMap == null) {
             if ((initParameterMap = (Map) portletContext
                     .getAttribute(INIT_PARAMETER_MAP_ATTRIBUTE)) == null) {
                 initParameterMap = new PortletInitParameterMap(portletContext);
                 portletContext.setAttribute(INIT_PARAMETER_MAP_ATTRIBUTE,
                         initParameterMap);
             }
         }
         return initParameterMap;
     }
 
     public String getRemoteUser() {
         return portletRequest.getRemoteUser();
     }
 
     public Object getRequest() {
         return portletRequest;
     }
 
     public String getRequestContextPath() {
         return portletRequest.getContextPath();
     }
 
     public Map getRequestCookieMap() {
         return EMPTY_UNMODIFIABLE_MAP;
     }
 
     public Map getRequestHeaderMap() {
         if (requestHeaderMap == null) {
             requestHeaderMap = new PortletRequestHeaderMap(portletRequest);
         }
         return requestHeaderMap;
     }
 
     public Map getRequestHeaderValuesMap() {
         if (requestHeaderValuesMap == null) {
             requestHeaderValuesMap = new PortletRequestHeaderValuesMap(
                     portletRequest);
         }
         return requestHeaderValuesMap;
     }
 
     public Locale getRequestLocale() {
         return portletRequest.getLocale();
     }
 
     public Iterator getRequestLocales() {
         return new EnumerationIterator(portletRequest.getLocales());
     }
 
     public Map getRequestMap() {
         if (requestMap == null) {
             requestMap = new PortletRequestMap(portletRequest);
         }
         return requestMap;
     }
 
     public Map getRequestParameterMap() {
         if (requestParameterMap == null) {
             requestParameterMap = new PortletRequestParameterMap(portletRequest);
         }
         return requestParameterMap;
     }
 
     public Iterator getRequestParameterNames() {
         return new EnumerationIterator(portletRequest.getParameterNames());
     }
 
     public Map getRequestParameterValuesMap() {
         if (requestParameterValuesMap == null) {
             requestParameterValuesMap = new PortletRequestParameterValuesMap(
                     portletRequest);
         }
         return requestParameterValuesMap;
     }
 
     public String getRequestPathInfo() {
         // must return null
         return null;
     }
 
     public String getRequestServletPath() {
         // must return null
         return null;
     }
 
     public URL getResource(String path) throws MalformedURLException {
         AssertionUtil.assertNotNull("path is null.", path);
         return portletContext.getResource(path);
     }
 
     public InputStream getResourceAsStream(String path) {
         AssertionUtil.assertNotNull("path is null.", path);
         return portletContext.getResourceAsStream(path);
     }
 
     public Set getResourcePaths(String path) {
         AssertionUtil.assertNotNull("path is null.", path);
         return portletContext.getResourcePaths(path);
     }
 
     public Object getResponse() {
         return portletResponse;
     }
 
     public Object getSession(boolean create) {
         return portletRequest.getPortletSession(create);
     }
 
     public Map getSessionMap() {
         if (sessionMap == null) {
             sessionMap = new PortletSessionMap(portletRequest);
         }
         return sessionMap;
     }
 
     public Principal getUserPrincipal() {
         return portletRequest.getUserPrincipal();
     }
 
     public boolean isUserInRole(String role) {
         AssertionUtil.assertNotNull("role is null.", role);
         return portletRequest.isUserInRole(role);
     }
 
     public void log(String message) {
         AssertionUtil.assertNotNull("message is null.", message);
         portletContext.log(message);
     }
 
     public void log(String message, Throwable exception) {
         AssertionUtil.assertNotNull("message", message);
         AssertionUtil.assertNotNull("exception", exception);
         portletContext.log(message, exception);
     }
 
     public void redirect(String url) throws IOException {
         if (portletResponse instanceof ActionResponse) {
             ((ActionResponse) portletResponse).sendRedirect(url);
         } else {
             throw new IllegalArgumentException(
                     "RenderResponse does not support redirect(String).");
         }
     }
 
     protected String getNamespace() {
         if (isActionRequest) {
             Map sessionMap = getSessionMap();
             String namespace = (String) sessionMap.get(SESSION_NAMESPACE);
             if (namespace != null) {
                 return namespace;
             }
             throw new IllegalStateException(
                     "Cannot call encodeNamespace(String) if the request is ActionRequest.");
         }
         return ((RenderResponse) portletResponse).getNamespace();
     }
 }
