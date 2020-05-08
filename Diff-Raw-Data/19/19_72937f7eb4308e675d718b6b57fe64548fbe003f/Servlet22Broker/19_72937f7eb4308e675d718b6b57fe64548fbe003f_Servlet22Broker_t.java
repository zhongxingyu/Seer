 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.  
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or 
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)  
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.  
  */
 
 
 package org.webmacro.servlet;
 
 import org.webmacro.*;
 import org.webmacro.util.*;
 
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import javax.servlet.*;
 
 /**
  * An implementation of Broker tailored for Servlet 2.2
  * environments.  Loads templates and other resources from the servlet
  * context (WAR file), writes log messages to the servlet log, and loads
  * properties from the WAR file context parameters.
  * @author Brian Goetz
  * @since 0.96
  */
 
 public class Servlet22Broker extends ServletBroker {
    protected final ClassLoader _servletClassLoader;
 
    protected Servlet22Broker(ServletContext sc, 
                              ClassLoader cl) throws InitException {
       super(sc);
       _servletClassLoader = cl;
       String propertySource = WEBMACRO_DEFAULTS + ", " + WEBMACRO_PROPERTIES
         + ", (WAR file)" +  ", " + "(System Properties)";
       loadDefaultSettings();
       loadSettings(WEBMACRO_PROPERTIES, true);
       loadServletSettings(Broker.SETTINGS_PREFIX);
       loadSystemSettings();
       initLog(_config);
 
       _log.notice("Loaded settings from " + propertySource);
       init();
    }
 
    protected void loadServletSettings(String prefix) 
       throws InitException {
       Properties p = new Properties();
       Enumeration e = _servletContext.getInitParameterNames();
      if (e != null) {
         String dotPrefix = (prefix == null) ? "" : prefix + ".";
         while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (prefix == null)
               p.setProperty(key, _servletContext.getInitParameter(key));
            else if (key.startsWith(dotPrefix)) 
               p.setProperty(key, _servletContext.getInitParameter(key)
                                       .substring(dotPrefix.length()));
         }
       }
       _config.load(p, prefix);
    }
 
    public static Broker getBroker(Servlet s) throws InitException {
       ServletContext sc = s.getServletConfig().getServletContext();
       ClassLoader cl = s.getClass().getClassLoader();
       try {
          Broker b = findBroker(sc);
          if (b == null) {
             b = new Servlet22Broker(sc, cl); 
             register(sc, b);
          }
          else 
            b.getLog("broker").notice("Servlet " 
                                      + s.getServletConfig().getServletName()
                                      + " joining Broker " + b.getName());
          return b;
       }
       catch (InitException e) {
          Log log = LogSystem.getSystemLog("wm");
          log.error("Failed to initialized WebMacro from servlet context" 
                    + sc.toString());
          throw e;
       }
    }
 
    /** 
     * Get a resource (file) from the the Broker's class loader
     */
    public URL getResource(String name) {
       try {
          URL u = _servletContext.getResource(name);
          if (u != null && u.getProtocol().equals("file")) {
            File f = new File(u.getFile());
            if (!f.exists())
               u = null;
          }
          if (u == null)
             u = _servletClassLoader.getResource(name);
          if (u == null) 
             u = super.getResource(name);
          return u;
       }
       catch (MalformedURLException e) {
          _log.warning("MalformedURLException caught in " + 
                       "ServletBroker.getResource for " + name);
          return null;
       }
    }
 
    /**
     * Get a resource (file) from the Broker's class loader 
     */
    public InputStream getResourceAsStream(String name) {
       InputStream is = _servletContext.getResourceAsStream(name);
       if (is == null)
          is = _servletClassLoader.getResourceAsStream(name);
       if (is == null) 
          is = super.getResourceAsStream(name);
       return is;
    }
 
    /**
     * Loads a class by name. Uses the servlet classloader to load the
     * class. If the class is not found uses the Broker classForName
     * implementation.  */
    
    public Class classForName(String name) throws ClassNotFoundException {
       Class cls = null;
       try { 
          cls = _servletClassLoader.loadClass(name);
       }
       catch (ClassNotFoundException e) { }
 
       if (cls==null) 
          cls = super.classForName(name);
 
       return cls;
    }
    
 }
