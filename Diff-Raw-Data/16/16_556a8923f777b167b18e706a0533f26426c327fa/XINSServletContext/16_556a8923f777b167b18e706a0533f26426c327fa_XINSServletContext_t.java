 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.servlet.container;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Set;
 import java.util.jar.JarFile;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.Servlet;
 import javax.servlet.ServletContext;
 
 import org.xins.common.Log;
import org.xins.common.Utils;
 
 /**
  * This class is an implementation of the ServletContext that can be
  * called locally.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class XINSServletContext implements ServletContext {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /** 
     * Creates a new instance of <code>XINSServletContext</code>
     */
    public XINSServletContext() {
    }
 
    /** 
     * Creates a new instance of <code>XINSServletContext</code>
     *
     * @param config
     *    the config of the servlet.
     */
    XINSServletContext(LocalServletConfig config) {
       _config = config;
      if (Utils.getJavaVersion() < 1.4) {
         try {
            _rootURL = "jar:" + config.getWarFile().toURL().toString() + "!";
         } catch (MalformedURLException muex) {
            
            // TODO Log
            muex.printStackTrace();
         }
      } else {
         _rootURL = "jar:" + config.getWarFile().toURI().toString() + "!";
      }
    }
    
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The configuration of the servlet.
     */
    private LocalServletConfig _config;
 
    /**
     * The root URL for the servlet
     */
    private String _rootURL;
    
    
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void removeAttribute(String str) {
       throw new UnsupportedOperationException();
    }
 
    public Servlet getServlet(String str) {
       throw new UnsupportedOperationException();
    }
 
    public Set getResourcePaths(String str) {
       throw new UnsupportedOperationException();
    }
 
    public Object getAttribute(String str) {
       throw new UnsupportedOperationException();
    }
 
    public ServletContext getContext(String str) {
       throw new UnsupportedOperationException();
    }
 
    public String getInitParameter(String str) {
       throw new UnsupportedOperationException();
    }
 
    public String getMimeType(String str) {
       throw new UnsupportedOperationException();
    }
 
    public RequestDispatcher getNamedDispatcher(String str) {
       throw new UnsupportedOperationException();
    }
 
    public String getRealPath(String str) {
       throw new UnsupportedOperationException();
    }
 
    public RequestDispatcher getRequestDispatcher(String str) {
       throw new UnsupportedOperationException();
    }
 
    public URL getResource(String str) {
       if (!str.startsWith("/")) {
          str = "/" + str;
       }
       try {
          return new URL(_rootURL + str);
       } catch (IOException ioe) {
          ioe.printStackTrace();
          return null;
       }
    }
 
    public InputStream getResourceAsStream(String str) {
       try {
          JarFile warFile = new JarFile(_config.getWarFile());
          return warFile.getInputStream(warFile.getJarEntry(str));
       } catch (IOException ioe) {
          ioe.printStackTrace();
          return null;
       }
    }
 
    public void log(Exception exception, String msg) {
       log(msg, exception);
    }
 
    public void log(String msg) {
       Log.log_1510(msg);
    }
 
    public void log(String msg, Throwable throwable) {
       Log.log_1511(throwable, msg);
    }
 
    public void setAttribute(String str, Object obj) {
       throw new UnsupportedOperationException();
    }
 
    public Enumeration getServlets() {
       throw new UnsupportedOperationException();
    }
 
    public Enumeration getServletNames() {
       throw new UnsupportedOperationException();
    }
 
    public String getServletContextName() {
       throw new UnsupportedOperationException();
    }
 
    public String getServerInfo() {
       throw new UnsupportedOperationException();
    }
 
    public Enumeration getAttributeNames() {
       throw new UnsupportedOperationException();
    }
 
    public Enumeration getInitParameterNames() {
       throw new UnsupportedOperationException();
    }
 
    public int getMajorVersion() {
       return 2;
    }
 
    public int getMinorVersion() {
       return 3;
    }
 }
