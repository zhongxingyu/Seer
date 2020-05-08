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
 
 import java.lang.*;
 import java.io.*;
 import java.util.*;
 import org.webmacro.util.*;
 import org.webmacro.*;
 import org.webmacro.resource.*;
 import org.webmacro.engine.StringTemplate;
 
 /**
   * This handler gets called if a normal handler could not 
   * be constructed--it writes out an error message 
   * explaining what went wrong.
   */
 final class ErrorHandler implements Handler
 {
   private static final String DEFAULT_ERROR_TEXT = 
    "<HTML><HEAD><TITLE>Error</TITLE></HEAD>" 
     + "<BODY><H1>Error</H1>"
     + "<HR>$error</BODY></HTML>";
   
   private Template _errorTemplate = null;
    /**
      * The default error handler simply returns its template
      * @see TemplateStore
      * @exception HandlerException if you don't want to handle the connect
      * @return A Template which can be used to interpret the connection
      */
    public Template accept(WebContext c)
       throws HandlerException 
    {
      Broker broker = c.getBroker();
      String templateName;
 
      try {
        templateName = (String) broker.get("config", WMServlet.ERROR_TEMPLATE);
      } 
      catch (ResourceException e) {
        templateName = WMServlet.ERROR_TEMPLATE_DEFAULT;
      }
 
      try {
        _errorTemplate = (Template) broker.get("template", templateName);
      }
      catch (ResourceException e) {
        _errorTemplate = new StringTemplate(broker, DEFAULT_ERROR_TEXT,
                                            "WebMacro default error template");
      }
      
      return _errorTemplate;
    }
 
    /**
      * Does nothing
      */
    public void destroy() { }
 
    /**
      * Does nothing
      */
    public void init() { }
 
 
    /**
      * Return the name of this handler
      */
    final public String toString()
    {
       return "WebMacro ErrorHandler";
    }
 }
 
 
