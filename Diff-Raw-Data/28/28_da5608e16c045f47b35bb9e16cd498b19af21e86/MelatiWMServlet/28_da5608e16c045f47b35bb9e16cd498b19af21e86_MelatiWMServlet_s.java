 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * -------------------------------------
  *  Copyright (C) 2000 William Chesters
  * -------------------------------------
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * A copy of the GPL should be in the file org/melati/COPYING in this tree.
  * Or see http://melati.org/License.html.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense.  When WebMacro's "Simple Public License" is
  * finalised, we'll offer it as an alternative license for Melati.
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati;
 
 import java.util.*;
 import java.io.*;
 import org.webmacro.*;
 import org.webmacro.util.*;
 import org.webmacro.engine.*;
 import org.webmacro.servlet.*;
 import org.webmacro.resource.TemplateProvider;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 /**
  * FIXME this it not pretty (I blame Jason ;) ).
  * Lifted largely from WMServlet.
  */
 
 /**
   * This handler gets called if a normal handler could not 
   * be constructed--it writes out an error message 
   * explaining what went wrong.
   */
 
 final class ErrorHandler implements Handler
 {
 
    private Template _errorTmpl = null;
 
    /**
      * The default error handler simply returns its template
      * @see TemplateStore
      * @exception HandlerException if you don't want to handle the connect
      * @return A Template which can be used to interpret the connection
      */
    public Template accept(WebContext c)
       throws HandlerException 
    {
 
       if (_errorTmpl == null) {
          try {
             String name = (String) c.getBroker().getValue(
                   Broker.CONFIG_TYPE, MelatiWMServlet.ERROR_TEMPLATE);
             _errorTmpl = (Template) c.getBroker().getValue(
                   TemplateProvider.TYPE, name);
          } catch (Exception e) { } 
          finally {
             if (_errorTmpl == null) {
                try {
                   _errorTmpl = (Template) c.getBroker().getValue(
                      TemplateProvider.TYPE, MelatiWMServlet.ERROR_TEMPLATE_DEFAULT);
                } catch (Exception e) {
                   throw new HandlerException("Could not load error handler");
                }
             }
          }
       }
       return _errorTmpl;
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
 
 
 /**
   * Flusher replaces need for Thread.stop
 */
 final class Flusher extends Thread {
     private PrintWriter out = null;
     private boolean stopTask = false;
 
     private int getPauseLength() {
         return 2000;
     }
 
     public Flusher(PrintWriter aOut) {
         out = aOut;
     }
 
     public synchronized boolean getStopTask() {
         return stopTask;
     }
 
     public synchronized void setStopTask(boolean aStopTask) {
         stopTask = aStopTask;
     }
 
     public void run() {
       try {
         while (!getStopTask()) {
           Thread.sleep( getPauseLength() );
           out.flush();
         }
       }
       catch (Exception e) {}
     }
 }
 
 public abstract class MelatiWMServlet extends HttpServlet {
 
    private WebMacro _wm = null;
    private Broker _broker = null;
    private WebContext _wc;
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
    final static protected Log _log = new Log("WMServlet", "WebMacro Abstract Servlet");
 
    /**
      * null means all OK
      */
    protected String _problem = "Not yet initialized: Your servlet API tried to access WebMacro without first calling init()!!!";
 
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
       return new WC(_broker);
    }
 
   protected void start() throws ServletException { }
   protected void stop() {}
 
   public synchronized void init(ServletConfig sc) throws ServletException {
     super.init(sc);
     init();
   }
 
   public void init() throws ServletException {
     // locate a Broker
 
     if (_wm == null) {
        try {
           _wm = initWebMacro();
           _broker = _wm.getBroker();
        } catch (InitException e) {
           _log.exception(e);
           _log.error("Could not initialize the broker!\n"
                 + "*** Check that WebMacro.properties was in your servlet\n"
                 + "*** classpath, in a similar place to webmacro.jar \n"
                 + "*** and that all values were set correctly.\n");
           _problem = e.getMessage(); 
 	  e.printStackTrace();
           return;
        }
     }
 
     // set up WebContext
     try {
        _wc = initWebContext();
     } catch (InitException e) {
        _log.exception(e);
        _log.error("Failed to initialize a WebContext, the initWebContext\n"
              + "method returned an exception: " + e);
        _problem = e.getMessage();
        return;
     }
 
     try {
        start();
        _problem = null; 
     } catch (ServletException e) {
        _log.exception(e);
        _problem = "WebMacro application code failed to initialize: \n"
           + e + "\n" + "This error is the result of a failure in the\n"
               + "code supplied by the application programmer.\n";
     }
 
     _started = true;
 
   }
 
   final public synchronized void destroy() {
      stop();
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
 
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
       throws ServletException, IOException {
     doRequest(_wc.clone(req,resp));
   }
 
   protected void doPost(HttpServletRequest req, HttpServletResponse resp)
       throws ServletException, IOException {
     if (_wc == null)
       init();
     doRequest(_wc.clone(req,resp));
   }
 
    protected void doRequest(WebContext context)
       throws ServletException, IOException {
        if (_problem != null) {
           init();
           if (_problem != null) {
              try { 
                 Writer out = context.getResponse().getWriter();
                 out.write(_problem); 
                 out.flush();
                 out.close();
              } catch (Exception e) {
                 _log.error(_problem); 
              }
              return;
           }
        }
        try {
          /* context.getRequest().setContentType("text/html"); */
          try {
            Template t = _handle(context);
            if (t != null)
              expand(t, context);
          }
          catch (Exception e) {
            Template t = _handleException(context, e);
            if (t != null)
              expand(t, context);
          }
        } catch (HandlerException e) {
           _log.exception(e);
           Template tmpl = error(context, 
              "Your handler was unable to process the request successfully " +
              "for some reason. Here are the details:<p>" + e);
           expandOrCarp(tmpl,context);  
        } catch (Exception e) {
           _log.exception(e);
           Template tmpl = error(context, 
              "The handler WebMacro used to handle this request failed for " +
              "some reason. This is likely a bug in the handler written " +
              "for this application. Here are the details:<p>" + e);
           expandOrCarp(tmpl, context);
        }
        finally {
          try {
            Writer out = context.getResponse().getWriter();
            out.flush();
            out.close();
          }
          catch (Exception e) {
          }
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
          _log.error("Could not find error variable in Config: " + e2);
       } catch(Exception e2) {
          _log.error("Unable to use ErrorHandler: " + e2);
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
      * Retrieve a template from the "template" provider. Equivalent to 
      * getBroker().getValue(TemplateProvider.TYPE,key)
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
    final public Object getURL(String url)
       throws NotFoundException
    {
       return _wm.getURL(url);
    }
 
 
    /**
      * Retrieve configuration information from the "config" provider.
      * Equivalent to getBrker().getValue(Config.TYPE,key)
      * @exception NotFoundException could not locate requested information
      */
    final public String getConfig(String key) 
       throws NotFoundException
    {
       return _wm.getConfig(key);
    }
 
   public static final String UNROLL = "__UNROLL__";
 
   protected void expand(Template tmpl, WebContext context)
       throws WebMacroException, IOException {
    if (context.get(UNROLL) != null) {
      final PrintWriter out = context.getResponse().getWriter();
/*      
      Thread flusher =
	  new Thread() {
	    public void run() {
	      try {
		for (;;) {
		  Thread.sleep(2000);
		  out.flush();
		}
	      }
	      catch (Exception e) {}
	    }
          };
*/
       Flusher flusher = new Flusher(out);
 
       flusher.start();
       try {
 	tmpl.write(out, context);
       }
       finally {
//	flusher.stop();
         flusher.setStopTask(true);
       }
     }
     else {
       CharArrayWriter buffer = new CharArrayWriter(2000);
       tmpl.write(buffer, context);
       buffer.writeTo(context.getResponse().getWriter());
     }
   }
 
   protected void expandOrCarp(Template tmpl, WebContext context) {
     Writer out = null;
     try {
       expand(tmpl, context);
     }
     catch (IOException e) {
       // ignore disconnect
     }
     catch (Exception e) {
       _log.exception(e);
 
       String error =
         "WebMacro encountered an error while executing a  template:\n"
         + ((tmpl != null) ?  (tmpl  + ": " + e + "\n") :
            ("The template failed to load; double check the "
             + "TemplatePath in your webmacro.properties file."));
 
       _log.warning(error);
 
       try { context.getResponse().getWriter().write(error); }
       catch (Exception ignore) { }
     }
   }
 
   protected abstract Template _handle(WebContext context)
       throws Exception;
 
   protected abstract Template _handleException(
       WebContext context, Exception exception)
           throws Exception;
 }
