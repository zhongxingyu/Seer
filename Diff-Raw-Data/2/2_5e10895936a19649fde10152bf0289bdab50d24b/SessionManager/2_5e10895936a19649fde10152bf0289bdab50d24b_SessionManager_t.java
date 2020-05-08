 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server.frontend;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.ChainedMap;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.manageable.BootstrapException;
 import org.xins.common.manageable.Manageable;
 import org.xins.common.spec.FunctionSpec;
 import org.xins.common.text.TextUtils;
 import org.xins.server.API;
 import org.xins.server.Log;
 
 /**
  * Manager for the sessions and session properties for the XINS front-end framework.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  *
  * @since XINS 1.5.0.
  */
 public class SessionManager extends Manageable {
 
    /**
     * The API, never <code>null</code>.
     */
    private API _api;
 
    /**
    * The HTTP session of the current running Thread, never <code>null</code>.
     */
    private ThreadLocal _currentSession = new ThreadLocal();
 
    /**
     * The list of pages that doesn't need to be logged in, cannot be <code>null</code>.
     */
    private ArrayList _unrestrictedPages = new ArrayList();
 
    /**
     * The default page, cannot be <code>null</code>.
     */
    private String _defaultCommand;
 
    /**
     * Creates the session manager.
     *
     * @param api
     *    the API.
     */
    public SessionManager(API api) {
       _api = api;
    }
 
    protected void bootstrapImpl(PropertyReader bootstrapProperties)
    throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          BootstrapException {
       _defaultCommand = bootstrapProperties.get("xinsff.default.command");
       if (_defaultCommand == null) {
          _defaultCommand = "DefaultCommand";
       }
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
    protected void request(HttpServletRequest request) {
 
       // Find the session ID in the cookies
       String sessionId = null;
       Cookie[] cookies = request.getCookies();
       int cookieCount = (cookies == null) ? 0 : cookies.length;
       for (int i = 0; i < cookieCount && sessionId == null; i++) {
          Cookie cookie = cookies[i];
          String name = cookie.getName();
          if ("SessionID".equals(name)) {
             sessionId = cookie.getValue();
          }
       }
 
       HttpSession session = request.getSession(true);
       _currentSession.set(session);
 
       // If the session ID is not found in the cookies, create a new one
       if (sessionId == null || sessionId.equals("") || sessionId.equals("null")) {
 
          sessionId = session.getId();
          setProperty(sessionId, Boolean.FALSE);
       }
 
       // Fill the input parameters
       Map inputParameters = new ChainedMap();
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
       setProperty("_remoteIP", request.getRemoteAddr());
       setProperty("_propertiesSet", new HashSet());
       setProperty("_userAgent", request.getHeader("User-Agent"));
    }
 
    /**
     * Sets the input parameters in the session is the execution of the function is successful.
     *
     * @param successful
     *    <code>true</code> if the function is successful, <code>false</code> otherwise.
     */
    protected void result(boolean successful) {
       if (successful) {
          Map inputParameters = (Map) getProperty("_inputs");
          Set propertiesSet =  (Set) getProperty("_propertiesSet");
          if (propertiesSet.contains("*")) {
             return;
          }
 
          // Only valid inputs of an existing function will be added.
          String command = (String) inputParameters.get("command");
          String action = (String) inputParameters.get("action");
          String functionName = command;
          // TODO put this in TextUtils
          if (action != null && !action.equals("") && !action.equalsIgnoreCase("show")) {
             functionName += TextUtils.firstCharUpper(action);
          }
          try {
             Map specInputParameters = _api.getAPISpecification().getFunction(functionName).getInputParameters();
             Map clonedInputParameters = new ChainedMap();
             clonedInputParameters.putAll(inputParameters);
             Iterator itInputParameters = clonedInputParameters.entrySet().iterator();
             while (itInputParameters.hasNext()) {
                Map.Entry nextInput = (Map.Entry) itInputParameters.next();
                String parameterName = (String) nextInput.getKey();
                parameterName = getRealParameter(parameterName, functionName);
                if (specInputParameters.containsKey(parameterName) && !propertiesSet.contains(parameterName)
                      && !propertiesSet.contains(parameterName.toLowerCase())) {
                   String value = (String) nextInput.getValue();
                   if ("".equals(value) || parameterName.equals(getSessionId())) {
                      value = null;
                   }
                   setProperty(parameterName.toLowerCase(), value);
                }
             }
          } catch (Exception ex) {
             // Ignore
             Utils.logIgnoredException(ex);
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
       Map inputParameters = (Map) getProperty("_inputs");
       String command = (String) inputParameters.get("command");
       if (command == null || command.equals("")) {
          command = _defaultCommand;
       }
       if (_unrestrictedPages.contains("*") ||
             _unrestrictedPages.contains(command) ||
             (command != null && command.startsWith("_"))) {
          return false;
       }
 
       // Check if the user is logged in
       boolean shouldLogIn = !getBoolProperty(getSessionId());
       return shouldLogIn;
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
       String sessionId = session.getId();
       return sessionId;
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
          return new ChainedMap();
       }
       Map properties = new ChainedMap();
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
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public void setProperty(String name, Object value) throws IllegalArgumentException {
       MandatoryArgumentChecker.check("name", name);
       HttpSession session = (HttpSession) _currentSession.get();
       if (session != null) {
          if (value == null) {
             removeProperty(name);
          } else {
             try {
                session.setAttribute(name, value);
             } catch (Throwable t) {
 
                // Should never happen as the session is valid
                Utils.logProgrammingError(t);
             }
          }
       }
       if (!name.startsWith("_")) {
          registerProperty(name);
       }
    }
 
    /**
     * Adds or sets a new session property.
     *
     * @param name
     *    the name of the session property, cannot be <code>null</code>.
     *
     * @param value
     *    the value of the session property.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public void setProperty(String name, boolean value) throws IllegalArgumentException {
       MandatoryArgumentChecker.check("name", name);
       setProperty(name, value ? Boolean.TRUE : Boolean.FALSE);
    }
 
    /**
     * Gets the value of a session property.
     *
     * @param name
     *    the name of the session property, cannot be <code>null</code>.
     *
     * @return
     *    the property value or <code>null</code> if the property does not exist.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public Object getProperty(String name) throws IllegalArgumentException {
       MandatoryArgumentChecker.check("name", name);
       HttpSession session = (HttpSession) _currentSession.get();
       if (session == null) {
          return null;
       }
       Object propertyValue = session.getAttribute(name);
       return propertyValue;
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
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public boolean getBoolProperty(String name) throws IllegalArgumentException {
       MandatoryArgumentChecker.check("name", name);
       HttpSession session = (HttpSession) _currentSession.get();
       if (session == null) {
          return false;
       }
       Object value = session.getAttribute(name);
       boolean isTrue = "true".equals(value) || Boolean.TRUE.equals(value);
       return isTrue;
    }
 
    /**
     * Removes a session property.
     *
     * @param name
     *    the name of the session property, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public void removeProperty(String name) throws IllegalArgumentException {
       MandatoryArgumentChecker.check("name", name);
       HttpSession session = (HttpSession) _currentSession.get();
       if (session != null) {
          session.removeAttribute(name);
 
          // Also remove it from the input parameter list.
          Map inputParameters = (Map) session.getAttribute("_inputs");
          if (inputParameters != null) {
             inputParameters.remove(name);
          }
          registerProperty(name);
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
             if (!nextAttribute.startsWith("_")) {
                attributeNames.add(nextAttribute);
             }
          }
          Iterator itAttributes = attributeNames.iterator();
          while (itAttributes.hasNext()) {
             String nextAttribute = (String) itAttributes.next();
             session.removeAttribute(nextAttribute);
          }
          registerProperty("*");
       }
    }
 
    /**
     * Registers a property as manually set by the user. The property will then
     * not be overwritten by the input parameter.
     *
     * @param name
     *    the name of the property set or remove in the function implementation, cannot be <code>null</code>.
     */
    private void registerProperty(String name) {
       Set propertiesSet = (Set) getProperty("_propertiesSet");
       if (propertiesSet != null) {
          propertiesSet.add(name);
       } else {
          propertiesSet = new HashSet();
          propertiesSet.add(name);
          setProperty("_propertiesSet", propertiesSet);
       }
    }
 
    /**
     * Gets the real parameter name.
     *
     * @param receivedParameter
     *    the name of the parameter as received.
     *
     * @param functionName
     *    the name of the function.
     *
     * @return
     *    the name of the parameter as specified in the function.
     *
     * @deprecated
     *    no mapping should be needed and the forms should send directly the correct parameters.
     */
    private String getRealParameter(String receivedParameter, String functionName) {
       String flatParameter = receivedParameter;
       if (receivedParameter.indexOf("_") != -1) {
          flatParameter = TextUtils.removeCharacter('_', receivedParameter);
       }
       try {
          FunctionSpec function = _api.getAPISpecification().getFunction(functionName);
          Set parametersSet = function.getInputParameters().keySet();
          if (parametersSet.contains(receivedParameter)) {
             return receivedParameter;
          }
          Iterator itParameters = parametersSet.iterator();
          while (itParameters.hasNext()) {
             String nextParameterName = (String) itParameters.next();
             if (nextParameterName.equalsIgnoreCase(flatParameter)) {
                return nextParameterName;
             }
          }
       } catch (Exception ex) {
          Log.log_3705(ex.getMessage());
       }
       return receivedParameter;
    }
 }
