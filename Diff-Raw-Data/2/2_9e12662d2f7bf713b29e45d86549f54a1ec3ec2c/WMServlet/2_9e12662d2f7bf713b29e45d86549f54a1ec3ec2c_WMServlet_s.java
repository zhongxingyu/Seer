 
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
 
 import java.util.*;
 import java.io.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.webmacro.*;
 import org.webmacro.util.*;
 
 
 /**
   * This is the abstract base class used by all WebMacro servlets. You
   * can either subclass from it directly, or make use of one of the 
   * generic subclasses provided. 
   * <p>
   * It's primary function is to create a WebContext and manage a 
   * Broker. It also provides a couple of convenience functions 
   * that access the Broker and/or WebContext to make some commonly
   * accessed services more readily available. 
   * <p>
   * @see org.webmacro.Handler
   * @see org.webmacro.Broker
   */
 abstract public class WMServlet extends HttpServlet implements WebMacro
 {
 
    private WebMacro _wm = null;
    private Broker _broker = null;
    private WebContext _wcPrototype;
    private boolean _started = false;
 
    /**
      * The name of the config entry we look for to find out what to 
      * call the variable used in the ERROR_TEMPLATE
      */
    final static String ERROR_VARIABLE = "ErrorVariable";
 
    /**
      * The name of the error template we will use if something
      * goes wrong
      */
    final static String ERROR_TEMPLATE = "ErrorTemplate";
 
    /**
      * We put error messages into this variable for the ErrorTemplate
      */
    final static String ERROR_TEMPLATE_DEFAULT = "error.wm";
 
    /**
      * Log object used to write out messages
      */
    private Log _log;
 
    /**
      * null means all OK
      */
    private String _problem = "Not yet initialized: Your servlet API tried to access WebMacro without first calling init()!!!";
 
    /**
      * This is the old-style init method, it just calls init(), after
      * handing the ServletConfig object to the superclass
      * @exception ServletException if it failed to initialize
      */
    final public synchronized void init(ServletConfig sc) 
       throws ServletException 
    {
       super.init(sc);
       init();
    }
 
 
    /**
      * This method is called by the servlet runner--do not call it. It 
      * must not be overidden because it manages a shared instance
      * of the broker--you can overide the start() method instead, which
      * is called just after the broker is initialized.
      */
    final public synchronized void init()
    {
 
       if (_started) {
          return;
       }
 
       // locate a Broker
 
       if (_wm == null) {
          try {
             _wm = initWebMacro();
             _broker = _wm.getBroker();
          } catch (InitException e) {
             _problem = "Could not initialize the broker!\n\n"
                   + "*** Check that WebMacro.properties was in your servlet\n"
                   + "*** classpath, in a similar place to webmacro.jar \n"
                   + "*** and that all values were set correctly.\n\n"
                   + e.getMessage();
             Log sysLog = LogSystem.getSystemLog("servlet");
             sysLog.error(_problem, e);
             return;
          }
       }
       _log = _broker.getLog("servlet", "WMServlet lifecycle information");
 
       // set up WebContext
       try {
          _wcPrototype = initWebContext();
       } catch (InitException e) {
          _log.error("Failed to initialize a WebContext, the initWebContext\n"
                + "method returned an exception", e);
          _problem = e.getMessage();
          return;
       }
 
       try {
          start();
          _problem = null; 
       } catch (ServletException e) {
          _problem = "WebMacro application code failed to initialize: \n"
             + e + "\n" + "This error is the result of a failure in the\n"
                 + "code supplied by the application programmer.\n";
          _log.error(_problem,e);
       }
       _log.notice("started: " + this);
       _started = true;
 
    }
 
    /**
      * This method is called by the servlet runner--do not call it. It 
      * must not be overidden because it manages a shared instance of 
      * the broker--you can overide the stop() method instead, which 
      * will be called just before the broker is shut down.
      */
    final public synchronized void destroy() {
       stop();
       _log.notice("stopped: " + this);
       _wm.destroy();
       _wm = null;
       _started = false;
       super.destroy();
    }
 
    /**
      * Check whether or not the broker we are using has been shut down
      */
    final public boolean isDestroyed() {
       return _wm.isDestroyed();
    }
 
 
    // SERVLET API METHODS
 
    /**
      * Process an incoming GET request: Builds a WebContext up and then 
      * passes it to the handle() method. You can overide this if you want,
      * though for most purposes you are expected to overide handle() 
      * instead.
      * <p>
      * @param req the request we got
      * @param resp the response we are generating
      * @exception ServletException if we can't get our configuration
      * @exception IOException if we can't write to the output stream 
      */
    final protected void doGet(HttpServletRequest req, HttpServletResponse resp)
       throws ServletException, IOException
    { 
       doRequest(req,resp);
    }
 
    /**
      * Behaves exactly like doGet() except that it reads data from POST
      * before doing exactly the same thing. This means that you can use 
      * GET and POST interchangeably with WebMacro. You can overide this if
      * you want, though for most purposes you are expected to overide 
      * handle() instead.
      * <p>
      * @param req the request we got
      * @param resp the response we are generating
      * @exception ServletException if we can't get our configuration
      * @exception IOException if we can't read/write to the streams we got
      */
    final protected void doPost(HttpServletRequest req, HttpServletResponse resp)
       throws ServletException, IOException
    {
        doRequest(req,resp);
    }
 
    final private void doRequest(
          HttpServletRequest req, HttpServletResponse resp)
       throws ServletException, IOException
    {
 
       WebContext context = null;
 
       if (_problem != null) {
          init();
          if (_problem != null) {
             try { 
                resp.setContentType("text/html");
                FastWriter out = new FastWriter(resp.getOutputStream(),
                                                resp.getCharacterEncoding());
                out.write("<html><head><title>WebMacro Error</title></head>");
                out.write("<body><h1><font color=\"red\">WebMacro Error: ");
                out.write("</font></h1><pre>");
                out.write(_problem); 
                out.write("</pre>");
               out.write("Please contat the server administrator");
                out.flush();
                out.close();
             } catch (Exception e) {
                _log.error(_problem,e); 
             }
             return;
          }
       }
       
       boolean timing = false;
       try {
         context = newContext(req,resp);
         timing = Flags.PROFILE && context.isTiming();
         if (timing) context.startTiming("WMServlet",req.getRequestURI());
 
         Template t;
         try {
            if (timing) context.startTiming("handle");
            t = handle(context);
         } finally {
            if (timing) context.stopTiming();
         }
 
         if (t != null) {
           execute(t,context); 
         }
         if (timing) context.startTiming("WMServlet.destroyContext()");
         try { destroyContext(context); }
         finally { 
            if (timing) context.stopTiming(); 
         }
       } catch (HandlerException e) {
          if (context == null) {
             context = _wcPrototype.newInstance(req,resp);
          }
          _log.error("Your handler failed to handle the request:" + this, e);
          Template tmpl = error(context,
             "Your handler was unable to process the request successfully " +
             "for some reason. Here are the details:<p>" + e);
          execute(tmpl,context);  
       } catch (Exception e) {
          if (context == null) {
             context = _wcPrototype.newInstance(req,resp);
          }
          _log.error("Your handler failed to handle the request:" + this, e);
          Template tmpl = error(context,
             "The handler WebMacro used to handle this request failed for " +
             "some reason. This is likely a bug in the handler written " +
             "for this application. Here are the details:<p>" + e);
          execute(tmpl,_wcPrototype.newInstance(req,resp));  
       } finally {
          if (timing) context.stopTiming();
          context.recycle();
       }
    }
 
    
    // CONVENIENCE METHODS & ACCESS TO THE BROKER
 
    /**
      * Create an error template using the built in error handler.
      * This is useful for returning error messages on failure;
      * it is used by WMServlet to display errors resulting from
      * any exception that you may throw from the handle() method.
      * @param context will add error variable to context (see Config)
      * @param error a string explaining what went wrong
      */
    final protected Template error(WebContext context, String error)
    {
       Template tmpl = null;
       _log.warning(error);
       Handler hand = new ErrorHandler();
       try {
          context.put(getConfig(ERROR_VARIABLE), error);
          tmpl = hand.accept(context);
       } catch(NotFoundException e2) {
          _log.error("Could not find error variable in Config", e2);
       } catch(Exception e2) {
          _log.error("Unable to use ErrorHandler", e2);
       }
       return tmpl;
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
      * Get a Log object which can be used to write to the log file. 
      * Messages to the logfile will be associated with the supplied
      * type. The type name should be short as it may be printed on 
      * every log line. The description is a longer explanation of 
      * the type of messages you intend to write to this Log.
      */
    final public Log getLog(String type, String description) {
       return _broker.getLog(type, description);
    }
 
    /**
      * Get a Log object which can be used to write to the log file. 
      * Messages to the logfile will be associated with the supplied
      * type. The type will be used as the description.
      */
    final public Log getLog(String type) {
       return _broker.getLog(type, type);
    }
 
    /**
      * Retrieve a template from the "template" provider. Equivalent to 
      * getBroker().get(TemplateProvider.TYPE,key)
      * @exception NotFoundException if the template was not found
      */
    final public Template getTemplate(String key) 
       throws NotFoundException
    {
       return _wm.getTemplate(key);
    }
 
    /**
      * Retrieve a URL. This is largely equivalent to creating a URL 
      * object and requesting its content, though it will sit in 
      * WebMacro's cache rather than re-requesting each time. 
      * The content will be returned as an Object.
      */
    final public String getURL(String url)
       throws NotFoundException
    {
       return _wm.getURL(url);
    }
 
 
    /**
      * Retrieve configuration information from the "config" provider.
      * Equivalent to getBroker().get(Config.TYPE,key)
      * @exception NotFoundException could not locate requested information
      */
    final public String getConfig(String key) 
       throws NotFoundException
    {
       return _wm.getConfig(key);
    }
 
    /**
      * Create a new Context object
      */
    final public Context getContext()
    {
       return _wm.getContext();
    }
 
    /**
      * Create a new WebContext object
      */
    final public WebContext getWebContext(HttpServletRequest req, HttpServletResponse res)
    {
       return _wm.getWebContext(req,res);
    }
 
 
    // DELEGATE-TO METHODS -- COMMON THINGS MADE EASIER
 
    /**
      * This method takes a populated context and a template and 
      * writes out the interpreted template to the context's output
      * stream. 
      */
    final protected void execute(Template tmpl, WebContext c)
    {
       Writer out = null;
       boolean timing = Flags.PROFILE && c.isTiming();
       try {
          if (timing) c.startTiming("Template.write", tmpl);
          FastWriter fw;
          try {
             HttpServletResponse resp= c.getResponse();
             fw = FastWriter.getInstance(
                   resp.getOutputStream(), resp.getCharacterEncoding());
             tmpl.write(fw, c);
          } finally {
             if (timing) c.stopTiming();
          }
          if (timing) c.startTiming("FastWriter.close()");
          try { fw.close(); }
          finally { if (timing) c.stopTiming(); }
       } catch (IOException e) {
          // ignore disconnect
       } catch (Exception e) {
          String error =
             "WebMacro encountered an error while executing a  template:\n"
             + ((tmpl != null) ?  (tmpl  + ": " + e + "\n") :
                 ("The template failed to load; double check the "
                  + "TemplatePath in your webmacro.properties file."));
          _log.warning(error,e);
          try { out.write(error); } catch (Exception ignore) { }
       } finally {
          try {
             if (out != null) {
                out.flush();
                out.close();
             }
          } catch (Exception e3) {
             // ignore disconnect
          }
       }
    }
 
    // FRAMEWORK TEMPLATE METHODS--PLUG YOUR CODE IN HERE
 
 
    /**
      * This method is called at the beginning of a request and is 
      * responsible for providing a Context for the request. The 
      * default implementation calls WebContext.newInstance(req,resp) 
      * on the WebContext prototype returned by the initWebContext() method.
      * This is probably suitable for most servlets, though you can override
      * it and do something different if you like. You can throw a 
      * HandlerException if something goes wrong.
      */
    public WebContext newContext(
          HttpServletRequest req, HttpServletResponse resp) 
       throws HandlerException
    {
       return _wcPrototype.newInstance(req,resp);
    }
 
    /**
      * This method is called to handle the processing of a request. It 
      * should analyze the data in the request, put whatever values are 
      * required into the context, and return the appropriate view.
      * @see getTemplate
      * @return the template to be rendered by the WebMacro engine
      * @exception HandlerException throw this to produce vanilla error messages
      * @param context contains all relevant data structures, incl builtins.
      */
    public abstract Template handle(WebContext context)
          throws HandlerException;
 
 
    /**
      * This method is called at the end of a request and is responsible
      * for cleaning up the Context at the end of the request. You may
      * not need to do anything here, but it is sometimes important if
      * you have an open database connection in your context that you 
      * need to close. The default implementation calls wc.clear().
      */
    public void destroyContext(WebContext wc) 
       throws HandlerException
    {
    }
 
 
    /**
      * Override this method to implement any startup/init code 
      * you require. The broker will have been created before this 
      * method is called; the default implementation does nothing.
      * This is called when the servlet environment initializes 
      * the servlet for use via the init() method.
      * @exception ServletException to indicate initialization failed
      */
    protected void start() throws ServletException { }
       
    /**
      * Override this method to implement any shutdown code you require.
      * The broker may be destroyed just after this method exits. This 
      * is called when the servlet environment shuts down the servlet 
      * via the shutdown() method. The default implementation does nothing.
      */
    protected void stop() { }
 
 
    /**
      * This method returns the WebMacro object which will be used to load,
      * access, and manage the Broker. The default implementation is to 
      * return a new WM() object. You could override it and return a WM 
      * object constructed with a particular configuration file, or some 
      * other implementation of the WebMacro interface.
      */
    public WebMacro initWebMacro() throws InitException
    {
       return new WM();
    }
 
    /**
      * This method must return a cloneable WebContext which can be 
      * cloned for use in responding to individual requests. Each 
      * incoming request will receive a clone of the returned object
      * as its context. The default implementation is to return 
      * a new WebContext(getBroker());
      */
    public WebContext initWebContext() throws InitException
    {
       return new WebContext(_broker);
    }
 
 }
 
