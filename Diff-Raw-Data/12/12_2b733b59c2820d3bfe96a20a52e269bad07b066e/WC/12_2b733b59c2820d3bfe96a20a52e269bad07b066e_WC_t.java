 
 /*
  * Copyright (c) 1998, 1999 Semiotek Inc. All Rights Reserved.
  *
  * This software is the confidential intellectual property of
  * of Semiotek Inc.; it is copyrighted and licensed, not sold.
  * You may use it under the terms of the GNU General Public License,
  * version 2, as published by the Free Software Foundation. If you 
  * do not want to use the GPL, you may still use the software after
  * purchasing a proprietary developers license from Semiotek Inc.
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See the attached License.html file for details, or contact us
  * by e-mail at info@semiotek.com to get a copy.
  */
 
 
 package org.webmacro.servlet;
 
 import org.webmacro.util.java2.*;
 import org.webmacro.*;
 import java.util.*;
 import java.lang.reflect.*;
 import java.io.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.webmacro.util.*;
 import org.webmacro.engine.*;
 import org.webmacro.resource.*;
 
 /**
   * This is an implementation of the WebContext interface. It has the 
   * ability to hook in commonly re-usable utilities via the configuraiton
   * file for the Broker. These commonly re-usable utilities are then made
   * available for use in the context. They include: Cookie, Form, FormList, 
   * and several others--see the WebMacro.properties file for the actual 
   * list, and a description of what's been loaded.
   * <p>
   * @see org.webmacro.servlet.Reactor
   * @see org.webmacro.util.Property
   * @see org.webmacro.util.Map
   */
 final public class WC extends HashMap implements WebContext 
 {
 
    // raw data fields that are always set, final, and available to the package
 
    /**
      * Log configuration errors, context errors, etc.
      */
    private final static Log _log = new Log("webcon","WebContext Messages");
 
    /**
      * Broker from our reactor
      */
    final Broker _broker;              
 
    /**
      * The request for this http connect
      */
    HttpServletRequest _request = null;
 
    /**
      * The response for this http connect 
      */
    HttpServletResponse _response = null;
 
    // property interface fields that are lazily set, non-final, and private
 
    /**
      * Find the name of a tool given the name of a class
      */
    private String getToolName(String cname)
    {
       int start = cname.lastIndexOf('.') + 1;
       int end = (cname.endsWith("Tool")) ? 
          (cname.length() - 4) : cname.length();
       String ret = cname.substring(start,end);
       return ret;
    }
                
    /**
      * Construct a new WebContext with all the appropriate information.
      * WebContext is constructed by Reactor, so this is package level.
      * <p>
      * @param req the servlet request object with info about the request
      * @param resp the servlet response object we need to fill in
      * @param requestType a constant, as defined in Reactor, naming req type
      */
    WC(final Broker broker) {
       _broker = broker;
       try {
          String tools = (String) _broker.getValue("config","TemplateTools");
          Enumeration tenum = new StringTokenizer(tools);
          while (tenum.hasMoreElements()) {
             String toolName = (String) tenum.nextElement();
             try {
                Class toolType = Class.forName(toolName);
                String varName = getToolName(toolName);
                Object tool = toolType.newInstance(); 
               put(varName,tool);
             } catch (ClassNotFoundException ce) {
                _log.exception(ce);
                _log.error("Tool class " + toolName + " not found: " + ce);
             } catch (IllegalAccessException ia) {
                _log.exception(ia);
                _log.error("Tool class and methods must be public for "
                      + toolName + ": " + ia);
             } catch (InstantiationException ie) {
                _log.exception(ie);
                _log.error("Tool class " + toolName + " must have a public zero "
                      + "argument or default constructor: " + ie);
             }
          }
       } catch (NotFoundException e) {
          _log.exception(e);
          _log.error("Could not locate TemplateTools in config: " + e);
       } catch (InvalidTypeException e) {
          _log.exception(e);
          _log.error("Could not access config: " + e);
       }
    }
 
 
    /**
      * Create a new WebContext like this one, only with new values
      * for request and response
      */
    final public WebContext clone(
          final HttpServletRequest req, 
          final HttpServletResponse resp) 
    {
       try {
          WC wc = (WC) clone();
          wc._request = req;
          wc._response = resp;
          return wc;
       } catch (Exception e) {
          _log.exception(e);
          _log.error("Clone not supported on WebContext!");
          return null;
       }
    }
 
    /**
      * The HttpServletRequest object which contains information 
      * provided by the HttpServlet superclass about the Request.
      * Much of this data is provided in other forms later on;
      * those interfaces get their data from this object. 
      * In particular the form data has already been parsed.
      * <p>
      * @see HttpServletRequest
      * @see org.webmacro.util.Property
      */
    public final HttpServletRequest getRequest() { 
       return _request; 
    }
 
    /**
      * The HttpServletResponse object which contains information
      * about the response we are going to send back. Many of these
      * services are provided through other interfaces here as well;
      * they are built on top of this object.
      * <p>
      * @see HttpServletResponse
      * @see org.webmacro.util.Property
      */
    public final HttpServletResponse getResponse() { 
       return _response; 
    }
 
 
    /**
      * This object contains configuration data about the WebMacro
      * system, based on a file loaded at startup time. 
      * <p>
      * @see org.webmacro.util.Property
      * @see Config
      */
    public final String getConfig(String key) { 
       try {
          return (String) _broker.getValue(Broker.CONFIG_TYPE,key);
       } catch (Exception e) {
          _log.exception(e);
          _log.error("Could not load config");
          return null;
       }
    }
 
 
    /**
      * This object provides access to extended services and other 
      * resources, which live in the org.webmacro.resource package,
      * or that you define, or obtain from a third party.
      */
    public final Broker getBroker() { 
       return _broker; 
    } 
 
    // WebContext API
    public final Object getMacro(String key) 
       throws InvalidContextException
    {
       Object t = get(key);
       return (t instanceof Macro) ? ((Macro) t).evaluate(this) : t;
    }
 
    // WebContext API
    final public ContextTool getTool(String name) 
       throws InvalidContextException
    {
      Object ret = null;
       try {
         ret = getMacro(name);
          return (ContextTool) getMacro(name);
       } catch (ClassCastException ce) {
         Class c = (ret == null) ? null : ret.getClass();
         String v = (ret == null) ? "NULL" : ret.toString();
          throw new InvalidContextException("Not a tool, " + name +
               " is a " + c + ": " + v);
       } 
    }
 
 
    // LEGACY METHODS
 
 
    public String getForm(String field) {
       try {
          ContextTool ct = getTool("Form");
          return (String) ct.get(field);
       } catch (Exception e) {
          _log.exception(e);
          _log.error("Could not load Form tool");
          return null;
       }
    }
 
    public String[] getFormList(String field) {
       try {
          ContextTool ct = getTool("FormList");
          return (String[]) ct.get(field);
       } catch (Exception e) {
          _log.exception(e);
          _log.error("Could not load FormList tool");
          return null;
       }
    }
 
    public CGI_Impersonator getCGI() {
       try {
          return (CGI_Impersonator) getMacro("CGI");
       } catch (Exception e) {
          _log.exception(e);
          _log.error("Could not load CGI tool");
          return null;
       }
    }
 
    public Cookie getCookie(String name) {
       try {
          ContextTool ct = getTool("Cookie");
          return (Cookie) ct.get(name);
       } catch (Exception e) {
          _log.exception(e);
          _log.error("Could not load Cookie tool");
          return null;
       }
    }
 
    public void setCookie(String name, String value) {
       try {
          ContextTool ct = getTool("Cookie");
          ct.set(name, value);
       } catch (Exception e) {
          _log.exception(e);
          _log.error("Could not load Cookie tool");
       }
    }
 
    public HttpSession getSession() {
       try {
          return (HttpSession) getMacro("Session");
       } catch (Exception e) {
          _log.exception(e);
          _log.error("Could not load Session tool");
          return null;
       }
    }
 
 }
