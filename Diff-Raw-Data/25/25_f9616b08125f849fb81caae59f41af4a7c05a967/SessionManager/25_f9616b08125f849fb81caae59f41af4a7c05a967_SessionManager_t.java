 /*
  * $Id$
  *
  * Copyright 2003-2005 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server.frontend;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.manageable.BootstrapException;
 import org.xins.common.manageable.InitializationException;
 import org.xins.common.manageable.Manageable;
 import org.xins.common.service.Descriptor;
 import org.xins.common.spec.FunctionSpec;
 
 import org.xins.server.API;
 
 /**
  * Manager for the sessions and session properties for the XINS front-end framework.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ft.com">Anthony Goubard</a>
  */
 public class SessionManager extends Manageable {
 
    /**
     * The API, never <code>null</code>.
     */
    private API _api;
 
    /**
     * The session ID of the current running Thread, never <code>null</code>.
     */
    private ThreadLocal _currentSession = new ThreadLocal();
 
    /**
     * The list of pages that doesn't need to be logged in, cannot be <code>null</code>.
     */
    private ArrayList _unrestrictedPages = new ArrayList();
 
    /**
     * Creates the session manager.
     *
     * @param api
     *    the API.
     */
    public SessionManager(API api) {
       _api = api;
    }
 
    /**
     * Bootstrap the <code>GPFCallingConvention</code> object.
     */
    protected void bootstrapImpl(PropertyReader bootstrapProperties)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           BootstrapException {
       String loginPage = bootstrapProperties.get("xinsff.login.page");
       if (loginPage != null) {
          _unrestrictedPages.add(loginPage);
          _unrestrictedPages.add("Control");
          _unrestrictedPages.add("Logout");
          String unrestrictedPages = bootstrapProperties.get("xinsff.unrestricted.pages");
          if (unrestrictedPages != null && !unrestrictedPages.equals("")) {
             StringTokenizer stUnrestricted = new StringTokenizer(unrestrictedPages, ",", false);
             while (stUnrestricted.hasMoreTokens()) {
                String nextPage = stUnrestricted.nextToken();
                _unrestrictedPages.add(nextPage);
             }
          }
       } else {
          _unrestrictedPages.add("*");
       }
    }
 
    /**
     * Method called when the request is received.
     *
     * This method will take care of creating a sessionId if needed and
     * putting the input parameters in the session.
     *
     * @param request
     *    the HTTP request, cannot be <code>null</code>.
     */
    final void request(HttpServletRequest request) {
 
       // Find the session ID in the cookies
       String sessionId = null;
       Cookie[] cookies = request.getCookies();
       int cookieCount = (cookies == null) ? 0 : cookies.length;
       for (int i = 0; i < cookieCount && sessionId == null; i++) {
          Cookie cookie = cookies[i];
          String name = cookie.getName();
          if ("SessionId".equals(name)) {
             sessionId = cookie.getValue();
          }
       }
 
       HttpSession session = request.getSession(true);
       _currentSession.set(session);
 
       // If the session ID is not found in the cookies, create a new one
       if (sessionId == null) {
 
          sessionId = session.getId();
          setProperty(sessionId, Boolean.FALSE);
       }
 
       // Fill the input parameters
       HashMap inputParameters = new HashMap();
       Enumeration params = request.getParameterNames();
       while (params.hasMoreElements()) {
          String name = (String) params.nextElement();
          String value = request.getParameter(name);
          if ("".equals(value) || name.equals(getSessionId())) {
             value = null;
          }
          inputParameters.put(name, value);
       }
       setProperty("_inputs", inputParameters);
    }
 
    /**
     * Sets the input parameters in the session is the execution of the function is successful.
     *
     * @param successful
     *    <code>true</code> if the function is successful, <code>false</code> otherwise.
     */
    final void result(boolean successful) {
       if (successful) {
          HashMap inputParameters = (HashMap) getProperty("_inputs");
 
          // Only valid inputs of an existing function will be added.
          String command = (String) inputParameters.get("command");
          String action = (String) inputParameters.get("action");
          String functionName = command;
          // TODO put this in TextUtils
          if (action != null && !action.equals("") && !action.equalsIgnoreCase("show")) {
             functionName += action.substring(0, 1).toUpperCase() + action.substring(1);
          }
          try {
             Map specInputParameters = _api.getAPISpecification().getFunction(functionName).getInputParameters();
             Iterator itInputParameters = inputParameters.entrySet().iterator();
             while (itInputParameters.hasNext()) {
                Map.Entry nextInput = (Map.Entry) itInputParameters.next();
                String parameterName = (String) nextInput.getKey();
                if (specInputParameters.containsKey(parameterName)) {
                   String value = (String) nextInput.getValue();
                   if ("".equals(value) || parameterName.equals(getSessionId())) {
                      value = null;
                   }
                   setProperty(parameterName.toLowerCase(), value);
                }
             }
          } catch (Exception ex) {
             // Ignore
          }
       }
    }
 
    /**
     * Returns <code>true</code> if the user needs to log in to access the page.
     *
     * @return
     *    whether the user should log in.
     **/
    public boolean shouldLogIn() {
       // Check if the page requires a login
       HttpSession session = (HttpSession) _currentSession.get();
       if (session == null) {
          return true;
       }
      HashMap inputParameters = (HashMap) getProperty("_inputs");
       String command = (String) inputParameters.get("command");
       if (_unrestrictedPages.contains("*") ||
             _unrestrictedPages.contains(command) ||
             (command != null && command.startsWith("_"))) {
          return false;
       }
 
       // Check if the user is logged in
       return !getBoolProperty(getSessionId());
    }
 
    /**
     * Gets the session id.
     *
     * @return
     *    the session ID, can be <code>null</code>.
     */
    public String getSessionId() {
       HttpSession session = (HttpSession) _currentSession.get();
       if (session == null) {
          return null;
       }
       return session.getId();
    }
 
    /**
     * Gets the session properties.
     *
     * @return
     *    a map where the key is the property name and the value is the session
     *    property value.
     */
    public Map getProperties() {
       HttpSession session = (HttpSession) _currentSession.get();
       if (session == null) {
          return new HashMap();
       }
       HashMap properties = new HashMap();
       Enumeration enuAttributes = session.getAttributeNames();
       while (enuAttributes.hasMoreElements()) {
          String nextAttribute = (String) enuAttributes.nextElement();
          Object value = session.getAttribute(nextAttribute);
          properties.put(nextAttribute, value);
       }
       return properties;
    }
 
    /**
     * Adds a new session property. Any previous property is replaced.
     * If the value is <code>null</code>, the property is removed.
     *
     * @param name
     *    the name of the session property, cannot be <code>null</code>.
     *
     * @param value
     *    the value of the session property, can be <code>null</code>.
     */
    public void setProperty(String name, Object value) {
       HttpSession session = (HttpSession) _currentSession.get();
       if (session != null) {
          if (value == null) {
             removeProperty(name);
          } else {
             try {
             session.setAttribute(name, value);
             } catch (Throwable t) {
                t.printStackTrace();
             }
          }
       }
    }
 
    /**
     * Gets the value of a session property.
     *
     * @param name
     *    the name of the session property, cannot be <code>null</code>.
     *
     * @return
     *    the property value or <code>null</code> if the property does not exist.
     */
    public Object getProperty(String name) {
       HttpSession session = (HttpSession) _currentSession.get();
       if (session == null) {
          return null;
       }
       return session.getAttribute(name);
    }
 
    /**
     * Gets the value of a boolean session property.
     *
     * @param name
     *    the name of the session property, cannot be <code>null</code>.
     *
     * @return
     *    <code>true</code> if the value of the property is "true" or Boolean.TRUE,
     *    <code>false</code> otherwise.
     */
    public boolean getBoolProperty(String name) {
       HttpSession session = (HttpSession) _currentSession.get();
       if (session == null) {
          return false;
       }
       Object value = session.getAttribute(name);
       return "true".equals(value) || Boolean.TRUE.equals(value);
    }
 
    /**
     * Removes a session property.
     *
     * @param name
     *    the name of the session property, cannot be <code>null</code>.
     */
    public void removeProperty(String name) {
       HttpSession session = (HttpSession) _currentSession.get();
       if (session != null) {
          session.removeAttribute(name);
 
          // Also remove it from the input parameter list.
          Map inputParameters = (Map) session.getAttribute("_inputs");
          if (inputParameters != null) {
             inputParameters.remove(name);
          }
       }
    }
 
    /**
     * Removes all session properties for the customer.
     */
    public void removeProperties() {
       HttpSession session = (HttpSession) _currentSession.get();
       if (session != null) {
 
          // Removing the attributes directly throws a ConcurentModificationException in Tomcat
          ArrayList attributeNames = new ArrayList();
          Enumeration enuAttributes = session.getAttributeNames();
          while (enuAttributes.hasMoreElements()) {
             String nextAttribute = (String) enuAttributes.nextElement();
             attributeNames.add(nextAttribute);
          }
          Iterator itAttributes = attributeNames.iterator();
          while (itAttributes.hasNext()) {
             String nextAttribute = (String) itAttributes.next();
             session.removeAttribute(nextAttribute);
          }
       }
   }
 }
