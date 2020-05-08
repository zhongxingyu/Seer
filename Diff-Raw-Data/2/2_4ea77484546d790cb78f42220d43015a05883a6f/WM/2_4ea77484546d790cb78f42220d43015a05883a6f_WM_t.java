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
 
 
 package org.webmacro;
 
 import org.webmacro.engine.*;
 import org.webmacro.resource.*;
 import org.webmacro.util.*;
 import org.webmacro.profile.*;
 import org.webmacro.servlet.*;
 
 import java.util.*;
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 
 /**
   * This class implements the WebMacro Manager interface. You can instantiate 
   * this yourself if you want to use WebMacro in standalone mode, rather than
   * subclassing from org.webmacro.servlet.WMServlet. This is actually the 
   * same class used by the servlet framework to manage access to the broker 
   * there, so you really don't lose much of anything by choosing to go 
   * standalone by using this object. All you have to do is come up with
   * your own context objects.
   */
 public class WM implements WebMacro
 {
 
    final private static Map _brokers = new HashMap();
    final private Context _context;
    private WebContext _webContext = null;
 
    // INIT METHODS--MANAGE ACCESS TO THE BROKER
 
    final private Broker _broker;      // cache for rapid access
 
    private boolean _alive = false;   // so we don't unload twice
 
    final private Provider _tmplProvider;
    final private Provider _urlProvider;
    final private Log _log;
 
    final private ThreadLocal _contextCache;
    final private ThreadLocal _webContextCache;
 
 
    /**
     * Constructs a WM which gets its properties (optionally) from the
     * file WebMacro.properties, as found on the class path.  No servlet
     * integration.  Templates will be loaded from the class path or from
     * TemplatePath.   Most users will want to use the WM(Servlet) constructor.
     */
    public WM() throws InitException
    {
       this(Broker.getBroker());
    }
 
    /**
     * Constructs a WM which gets its properties from the file specified,
     * which must exist on the class path or be an absolute path.  No servlet
     * integration.  Templates will be loaded from the class path or from
     * TemplatePath.  Most users will want to use the WM(Servlet) constructor.
     */
    public WM(String config) throws InitException {
       this(Broker.getBroker(config));
    }
 
    /**
     * Constructs a WM is tied to a Servlet broker.  Depending on the 
     * servlet containers level of servlet support, property fetching,
     * logging, and template fetching will be managed by the servlet broker.
     */
    public WM(Servlet s) throws InitException {
       this(ServletBroker.getBroker(s));
    }
 
    /**
     * Constructs a WM from an arbitrary Broker.  Don't use this unless
     * you have very specific needs and know what you are doing; constructing
     * a properly functioning broker is not obvious. 
     */
    public WM(Broker broker) throws InitException {
       if (broker == null) 
          throw new InitException("No Broker passed to WM()");
 
       _broker = broker;
       _alive = true;
       _log = _broker.getLog("wm", "WebMacro instance lifecycle");
       _log.info("new " + this
              + "; version=" + WebMacro.VERSION + ", " + WebMacro.BUILD_DATE);
       _context = new Context(_broker);
       _contextCache = new ThreadLocal() {
             public Object initialValue() { return new ScalablePool(); }
          };
       _webContext = new WebContext (_broker);
       _webContextCache = new ThreadLocal() {
             public Object initialValue() { return new ScalablePool(); }
          };
       
       try {
          _tmplProvider = _broker.getProvider("template");
          _urlProvider = _broker.getProvider("url");
       } 
       catch (NotFoundException nfe) {
          _log.error("Could not load configuration", nfe);
          throw new InitException("Could not locate provider; "
             + "This implies that WebMacro is badly misconfigured, you\n"
             + "should double check that all configuration files and\n"
             + "options are set up correctly. In a default install of\n"
             + "WebMacro this likely means your WebMacro.properties file\n"
             + "was not found on your CLASSPATH.", nfe);
       }
    }
 
    /**
      * Call this method when you are finished with WebMacro. If you
      * don't call this method, the Broker and all of WebMacro's caches
      * may not be properly shut down, potentially resulting in loss of
      * data, and wasted memory. This method is called in the
      * finalizer, but it is best to call it as soon as you know you
      * are done with WebMacro.
      * <p>
      * After a call to destroy() attempts to use this object may yield
      * unpredicatble results.  
      */
    final public void destroy() {
       if (_alive) {
          _alive = false;
          _webContext = null;
          _log.info("shutdown " + this);
       }
    }
 
    public String toString() {
       return "WebMacro(" + _broker.getName() + ")";
    }
 
    /**
      * This message returns false until you destroy() this object,
      * subsequently it returns true. Do not attempt to use this object
      * after it has been destroyed.  */
    final public boolean isDestroyed() {
       return !_alive;
    }
 
 
    /**
      * You should never call this method, on any object. Leave it up
      * to the garbage collector. If you want to shut this object down,
      * call destroy() instead. If you subclass this message, be sure
      * to call super.finalize() since this is one of the cases where
      * it matters.  */
    protected void finalize() throws Throwable {
       try {
          destroy();
       }
       finally {
          super.finalize();
       }
    }
 
 
    /**
      * This object is used to access components that have been plugged
      * into WebMacro; it is shared between all instances of this class and
      * its subclasses. It is created when the first instance is initialized,
      * and deleted when the last instance is shut down. If you attempt to 
      * access it after the last servlet has been shutdown, it will either 
      * be in a shutdown state or else null.
      */
    final public Broker getBroker() {
       // this method can be unsynch. because the broker manages its own
       // state, plus the only time the _broker will be shutdown or null 
       // is after the last servlet has shutdown--so why would anyone be 
       // accessing us then? if they do the _broker will throw exceptions
       // complaining that it has been shut down, or they'll get a null here.
       return _broker;
    }
 
    /**
     * Retrieve a FastWriter from WebMacro's internal pool of FastWriters.
     * A FastWriter is used when writing templates to an output stream
     *
     * @param out The output stream the FastWriter should write to.  Typically
     *           this will be your ServletOutputStream.  It can be null if
     *           only want the fast writer to buffer the output.
     * @param enctype the Encoding type to use
     */
    final public FastWriter getFastWriter (OutputStream out, String enctype)
                                           throws UnsupportedEncodingException {
        return FastWriter.getInstance (_broker, out, enctype);
    }
    
    
    
    /**
      * Instantiate a new context from a pool. This method is more 
      * efficient, in terms of object creation, than creating a 
      * Context directly. The Context will return to the pool 
      * when Context.recycle() is called.
      */
    final public Context getContext() {
       Pool cpool = (Pool) _contextCache.get();
       Context c = (Context) cpool.get();
       if (c == null) {
         c = (Context) _context.clone();
         c.setPool(cpool);
       }
       else
         c.clear();
       return c;
    }
 
    /**
      * Instantiate a new webcontext from a pool. This method is more
      * efficient, in terms of object creation, than creating a 
      * WebContext object directly. The WebContext will return to
      * the pool when WebContext.recycle() is called.
      */
    final public WebContext getWebContext(HttpServletRequest req, 
             HttpServletResponse resp) 
    {
       Pool cpool = (Pool) _webContextCache.get();
       WebContext c = (WebContext) cpool.get();
       if (c == null) {
         c = _webContext.newInstance(req,resp);
         c.setPool(cpool);
       }
       else
         c.reinitialize(req, resp);
       return c;   
    }
 
 
    /**
      * Retrieve a template from the "template" provider.
      * @exception NotFoundException  if the template could not be found
      * @exception ResourceException  if the template could not be loaded
      */
    final public Template getTemplate(String key) 
       throws ResourceException
    {
       return (Template) _tmplProvider.get(key); 
    }
 
    /**
      * Retrieve a URL from the "url" provider. Equivalent to 
      * getBroker().getValue("url",url)
      * @exception NotFoundException  if the template could not be found
      * @exception ResourceException  if the template could not be loaded
      */
    final public String getURL(String url) 
       throws ResourceException
    {
       return (String) _urlProvider.get(url);
    }
 
    /**
      * Retrieve configuration information from the "config" provider.
      * Equivalent to getBroker().get("config",key)
      * @exception NotFoundException could not locate requested information
      */
    final public String getConfig(String key) 
       throws NotFoundException
    {
       try {
          return (String) _broker.get("config", key);
       }
       catch (NotFoundException e) { throw e; }
       catch (ResourceException e) { 
         throw new NotFoundException(e.toString(), e); 
       }
    }
 
    /**
      * Get a log to write information to. Log type names should be lower
      * case and short. They may be printed on every line of the log 
      * file. The description is a longer explanation of the type of
      * log messages you intend to produce with this Log object.
      */
    final public Log getLog(String type, String description) {
       return _broker.getLog(type, description);
    }
 
    /**
      * Get a log using the type as the description
      */
    final public Log getLog(String type) {
       return _broker.getLog(type,type);
    }
 
    /**
     * Convenience method for writing a template to an OutputStream.
     * This method takes care of all the typical work involved
     * in writing a template.<p>
     *
     * This method uses the default <code>TemplateOutputEncoding</code> specified in
     * WebMacro.defaults or your custom WebMacro.properties.
     *
     * @param templateName name of Template to write.  Must be accessible
     *                     via TemplatePath
     * @param out          where the output of the template should go
     * @param context      The Context (can be a WebContext too) used
     *                     during the template evaluation phase
     * @throws java.io.IOException if the template cannot be written to the
     *                             specified output stream
     * @throws ResourceException if the template name specified cannot be found
     * @throws PropertyException if a fatal error occured during the Template
     *                           evaluation phase
     */
    final public void writeTemplate (String templateName, java.io.OutputStream out,
                               Context context)
             throws java.io.IOException, ResourceException, PropertyException {
 
       writeTemplate (templateName, out, 
                      getConfig (WMConstants.TEMPLATE_OUTPUT_ENCODING), context); 
    }
 
    /**
     * Convienence method for writing a template to an OutputStream.
     * This method takes care of all the typical work involved
     * in writing a template.
     *
     * @param templateName name of Template to write.  Must be accessible
     *                     via TemplatePath
     * @param out          where the output of the template should go
     * @param encoding     character encoding to use when writing the template
     *                     if the encoding is <code>null</code>, the default
     *                     <code>TemplateOutputEncoding</code> is used
     * @param context      The Context (can be a WebContext too) used
     *                     during the template evaluation phase
     * @throws java.io.IOException if the template cannot be written to the
     *                             specified output stream
     * @throws ResourceException if the template name specified cannot be found
     * @throws PropertyException if a fatal error occured during the Template
     *                           evaluation phase
     */
    final public void writeTemplate (String templateName, java.io.OutputStream out,
                               String encoding, Context context)
               throws java.io.IOException, ResourceException, PropertyException {
      
       if (encoding == null)
          encoding = getConfig (WMConstants.TEMPLATE_OUTPUT_ENCODING);
 
       Template tmpl = getTemplate (templateName);
       FastWriter fw = getFastWriter (out, encoding);
       tmpl.write (fw, context);
       fw.close ();
    }
 }
