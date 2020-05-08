 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.session;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.*;
 import org.wings.event.ExitVetoException;
 import org.wings.event.SRequestEvent;
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.externalizer.ExternalizedResource;
 import org.wings.io.Device;
 import org.wings.io.DeviceFactory;
 import org.wings.io.ServletDevice;
 import org.wings.resource.DynamicCodeResource;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.*;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Locale;
 import java.util.Arrays;
 
 /**
  * The servlet engine creates for each user a new HttpSession. This
  * HttpSession can be accessed by all Serlvets running in the engine. A
  * WingServlet creates one wings SessionServlet per HTTPSession and stores
  * it in its context.
  * <p>As the SessionServlets acts as Wrapper for the WingsServlet, you can
  * access from there as used the  ServletContext and the HttpSession.
  * Additionally the SessionServlet containts also the wingS-Session with
  * all important services and the superordinated SFrame. To this SFrame all
  * wings-Components and hence the complete application state is attached.
  * The developer can access from any place via the SessionManager a
  * reference to the wingS-Session. Additionally the SessionServlet
  * provides access to the all containing HttpSession.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  */
 final class SessionServlet
         extends HttpServlet
         implements HttpSessionBindingListener {
     private final transient static Log log = LogFactory.getLog(SessionServlet.class);
 
     /**
      * The parent {@link WingServlet}
      */
     protected transient HttpServlet parent = this;
 
     /**
      * The session.
      */
     private transient /* --- ATTENTION! This disable session serialization! */ Session session;
 
     private boolean firstRequest = true;
 
     /** Refer to comment in {@link #doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)} */
     private String exitSessionWorkaround;
 
     /**
      * Default constructor.
      */
     protected SessionServlet() {
     }
 
     /**
      * Sets the parent servlet contianint this wings session
      * servlet (WingsServlet, delegating its requests to the SessionServlet).
      */
     protected final void setParent(HttpServlet p) {
         if (p != null) parent = p;
     }
 
     public final Session getSession() {
         return session;
     }
 
     /**
      * Overrides the session set for setLocaleFromHeader by a request parameter.
      * Hence you can force the wings session to adopt the clients Locale.
      */
     public final void setLocaleFromHeader(String[] args) {
         if (args == null)
             return;
 
         for (int i = 0; i < args.length; i++) {
             try {
                 getSession().setLocaleFromHeader(Boolean.valueOf(args[i]).booleanValue());
             } catch (Exception e) {
                 log.error("setLocaleFromHeader", e);
             }
         }
     }
 
     /**
      * The Locale of the current wings session servlet is determined by
      * the locale transmitted by the browser. The request parameter
      * <PRE>LocaleFromHeader</PRE> can override the behaviour
      * of a wings session servlet to adopt the clients browsers Locale.
      *
      * @param req The request to determine the local from.
      */
     protected final void handleLocale(HttpServletRequest req) {
         setLocaleFromHeader(req.getParameterValues("LocaleFromHeader"));
 
         if (getSession().getLocaleFromHeader()) {
             for (Enumeration en = req.getLocales(); en.hasMoreElements();) {
                 Locale locale = (Locale) en.nextElement();
                 try {
                     getSession().setLocale(locale);
                     return;
                 } catch (Exception ex) {
                     log.warn("locale not supported " + locale);
                 } // end of try-catch
             } // end of for ()
         }
     }
 
     // jetzt kommen alle Servlet Methoden, die an den parent deligiert
     // werden
 
 
     public ServletContext getServletContext() {
         if (parent != this)
             return parent.getServletContext();
         else
             return super.getServletContext();
     }
 
 
     public String getInitParameter(String name) {
         if (parent != this)
             return parent.getInitParameter(name);
         else
             return super.getInitParameter(name);
     }
 
 
     public Enumeration getInitParameterNames() {
         if (parent != this)
             return parent.getInitParameterNames();
         else
             return super.getInitParameterNames();
     }
 
     /**
      * Delegates log messages to the according WingsServlet or alternativly
      * to the HttpServlet logger.
      *
      * @param msg The logmessage
      */
     public void log(String msg) {
         if (parent != this)
             parent.log(msg);
         else
             super.log(msg);
     }
 
 
     public String getServletInfo() {
         if (parent != this)
             return parent.getServletInfo();
         else
             return super.getServletInfo();
     }
 
 
     public ServletConfig getServletConfig() {
         if (parent != this)
             return parent.getServletConfig();
         else
             return super.getServletConfig();
     }
 
     // bis hierhin
 
     /**
      * init
      */
     public final void init(ServletConfig config,
                            HttpServletRequest request,
                            HttpServletResponse response) throws ServletException {
         try {
             session = new Session();
             SessionManager.setSession(session);
 
             // set request.url in session, if used in constructor of wings main classs
             if (request.isRequestedSessionIdValid()) {
                 // this will fire an event, if the encoding has changed ..
                 ((PropertyService) session).setProperty("request.url",
                         new RequestURL("", getSessionEncoding(response)));
             }
 
             session.init(config, request, response);
 
 
             try {
                 String mainClassName = config.getInitParameter("wings.mainclass");
                 Class mainClass = null;
                 try {
                     mainClass = Class.forName(mainClassName, true,
                             Thread.currentThread()
                             .getContextClassLoader());
                 } catch (ClassNotFoundException e) {
                     // fallback, in case the servlet container fails to set the
                     // context class loader.
                     mainClass = Class.forName(mainClassName);
                 }
                 Object main = mainClass.newInstance();
             } catch (Exception ex) {
                 log.fatal("could not load wings.mainclass: " +
                         config.getInitParameter("wings.mainclass"), ex);
                 throw new ServletException(ex);
             }
         } finally {
             // The session was set by the constructor. After init we
             // expect that only doPost/doGet is called, which set the
             // session also. So remove it here.
             SessionManager.removeSession();
         }
     }
 
     /**
      * this method references to
      * {@link #doGet(HttpServletRequest, HttpServletResponse)}
      */
     public final void doPost(HttpServletRequest req, HttpServletResponse res)
             throws IOException {
         //value chosen to limit denial of service
         if (req.getContentLength() > getSession().getMaxContentLength() * 1024) {
             res.setContentType("text/html");
             ServletOutputStream out = res.getOutputStream();
             out.println("<html><head><meta http-equiv=\"expires\" content=\"0\"><title>Too big</title></head>");
             out.println("<body><h1>Error - content length &gt; " +
                     getSession().getMaxContentLength() + "k");
             out.println("</h1></body></html>");
         } else {
             doGet(req, res);
         }
         // sollte man den obigen Block nicht durch folgende Zeile ersetzen?
         //throw new RuntimeException("this method must never be called!");
         // bsc: Wieso?
     }
 
 
     /**
      * Verarbeitet Informationen vom Browser:
      * <UL>
      * <LI> setzt Locale
      * <LI> Dispatch Get Parameter
      * <LI> feuert Form Events
      * </UL>
      * Ist synchronized, damit nur ein Frame gleichzeitig bearbeitet
      * werden kann.
      */
     public final synchronized void doGet(HttpServletRequest req,
                                          HttpServletResponse response) {
         // Special case: You double clicked i.e. a "logout button"
         // First request arrives, second is on hold. First invalidates session and sends redirect as response,
         // but browser ignores and expects response in second request. But second request has longer a valid session.
         if (session == null) {
             try {
                 response.sendRedirect(exitSessionWorkaround != null ? exitSessionWorkaround : "");
                 return;
             } catch (IOException e) {
                 log.info("Session exit workaround failed to to IOException (triple click?)");
             }
         }
 
         SessionManager.setSession(session);
         session.setServletRequest(req);
         session.setServletResponse(response);
 
         session.fireRequestEvent(SRequestEvent.REQUEST_START);
 
         // in case, the previous thread did not clean up.
         SForm.clearArmedComponents();
 
         Device outputDevice = null;
 
         ReloadManager reloadManager = session.getReloadManager();
         
         boolean isErrorHandling = false;
         
         try {
             /*
              * The tomcat 3.x has a bug, in that it does not encode the URL
              * sometimes. It does so, when there is a cookie, containing some
              * tomcat sessionid but that is invalid (because, for instance,
              * we restarted the tomcat in-between). 
              * [I can't think of this being the correct behaviour, so I assume
              *  it is a bug. ]
              *
              * So we have to workaround this here: if we actually got the
              * session id from a cookie, but it is not valid, we don't do
              * the encodeURL() here: we just leave the requestURL as it is
              * in the properties .. and this is url-encoded, since
              * we had set it up in the very beginning of this session 
              * with URL-encoding on  (see WingServlet::newSession()).
              *
              * Vice versa: if the requestedSessionId is valid, then we can
              * do the encoding (which then does URL-encoding or not, depending
              * whether the servlet engine detected a cookie).
              * (hen)
              */
             RequestURL requestURL = null;
             if (req.isRequestedSessionIdValid()) {
                 requestURL = new RequestURL("", getSessionEncoding(response));
                 // this will fire an event, if the encoding has changed ..
                 ((PropertyService) session).setProperty("request.url",
                         requestURL);
             }
 
             if (log.isDebugEnabled()) {
                 log.debug("request URL: " + requestURL);
                 log.debug("HTTP header:");
                 for (Enumeration en = req.getHeaderNames(); en.hasMoreElements();) {
                     String header = (String) en.nextElement();
                     log.debug("   " + header + ": " + req.getHeader(header));
                 }
             }
             handleLocale(req);
 
             Enumeration en = req.getParameterNames();
             Cookie[] cookies = req.getCookies();
 
             // are there parameters/low level events to dispatch
             if (en.hasMoreElements()) {
                 // only fire DISPATCH_START if we have parameters to dispatch
                 session.fireRequestEvent(SRequestEvent.DISPATCH_START);
 
                 if (cookies != null) {
                     //dispatch cookies
                     for (int i = 0; i < cookies.length; i++) {
                         Cookie cookie = cookies[i];
                         String paramName = cookie.getName();
                         String value = cookie.getValue();
     
                         if (log.isDebugEnabled())
                             log.debug("dispatching cookie " + paramName + " = " + value);
     
                         session.getDispatcher().dispatch(paramName, new String[] { value });
                     }
                 }
                 while (en.hasMoreElements()) {
                     String paramName = (String) en.nextElement();
                     String[] value = req.getParameterValues(paramName);
 
                     if (log.isDebugEnabled())
                         log.debug("dispatching " + paramName + " = " + Arrays.asList(value));
 
                     session.getDispatcher().dispatch(paramName, value);
                 }
 
                 SForm.fireEvents();
             
                 // only fire DISPATCH DONE if we have parameters to dispatch
                 session.fireRequestEvent(SRequestEvent.DISPATCH_DONE);
             }
 
             session.fireRequestEvent(SRequestEvent.PROCESS_REQUEST);
             session.getDispatcher().invokeRunnables();
             
             // if the user chose to exit the session as a reaction on an
             // event, we got an URL to redirect after the session.
             /*
              * where is the right place?
              * The right place is 
              *    - _after_ we processed the events 
              *        (e.g. the 'Pressed Exit-Button'-event or gave
              *         the user the chance to exit this session in the custom
              *         processRequest())
              *    - but _before_ the rendering of the page,
              *      because otherwise an redirect won't work, since we must
              *      not have sent anything to the output stream).
              */
             if (session.getExitAddress() != null) {
 
                 try {
                     session.firePrepareExit();
                     session.fireRequestEvent(SRequestEvent.REQUEST_END);
 
                     String redirectAddress;
                     if (session.getExitAddress().length() > 0) {
                         // redirect to user requested URL.
                         redirectAddress = session.getExitAddress();
                     } else {
                         // redirect to a fresh session.
                         redirectAddress = req.getRequestURL().toString();
                     }
                     req.getSession().invalidate(); // calls destroy implicitly
                     response.sendRedirect(redirectAddress);
                     exitSessionWorkaround = redirectAddress;
                     return;
                 } catch (ExitVetoException ex) {
                     session.exit(null);
                 } // end of try-catch
             }
 
             if (session.getRedirectAddress() != null) {
                 // redirect to user requested URL.
                 response.sendRedirect(session.getRedirectAddress());
                 session.setRedirectAddress(null);
                 return;
             }
             
             // invalidate frames and resources
             reloadManager.notifyCGs();
             reloadManager.invalidateResources();
 
             // deliver resource. The
             // externalizer is able to handle static and dynamic resources
             ExternalizeManager extManager = getSession().getExternalizeManager();
             String pathInfo = req.getPathInfo().substring(1);
             log.debug("pathInfo: " + pathInfo);
 
             /*
              * if we have no path info, or the special '_' path info
              * (that should be explained somewhere, Holger), then we
              * deliver the toplevel Frame of this application.
              */
             String externalizeIdentifier = null;
             if (pathInfo == null || pathInfo.length() == 0 || "_".equals(pathInfo) || firstRequest) {
                 externalizeIdentifier = retrieveCurrentRootFrameResource().getId();
                 firstRequest = false;
             } else {
                 externalizeIdentifier = pathInfo;
             }
             
             // Retrieve externalized resource.
             ExternalizedResource extInfo = extManager.getExternalizedResource(externalizeIdentifier);
 
             // Special case handling: We request a .html resource of a session which is not accessible.
             // This happens some times and leads to a 404, though it should not be possible.
             if (extInfo == null && pathInfo != null && pathInfo.length() > 0 && pathInfo.endsWith(".html")) {
                 log.info("Found a request to an invalid .html during a valid session. Redirecting to root frame.");
                 response.sendRedirect(retrieveCurrentRootFrameResource().getURL().toString());
                 return;
             }
 
             if (extInfo != null) {
                 outputDevice = DeviceFactory.createDevice(extInfo);
 
                 session.fireRequestEvent(SRequestEvent.DELIVER_START, extInfo);
                 extManager.deliver(extInfo, response, outputDevice);
                 session.fireRequestEvent(SRequestEvent.DELIVER_DONE, extInfo);
 
             } else {
                 handleUnknownResourceRequested(req, response);
             }
 
         } catch (Throwable e) {
             // TODO better error handling, this is pretty flexible, yet dirty
             /* custom error handling...implement it in SFrame            */
 
             SFrame defaultFrame = null;
             if (session.getFrames().size() > 0) {
                 defaultFrame = (SFrame) session.getFrames().iterator().next();
                 while (defaultFrame.getParent() != null)
                     defaultFrame = (SFrame) defaultFrame.getParent();
             }
             if (defaultFrame != null && defaultFrame.handleError(e)) {
                 log.warn("exception, trying to handle it via SFrame.handleError(): ", e);
                 // maybe just call defaultFrame.write(new ServletDevice(out)); instead of doGet()
                 doGet(req, response);
                 isErrorHandling = true;
             } else {
                 log.fatal("exception handling failed: ", e);
                 handleException(response, e);
             }
 
         } finally {
             if (session != null) {
                 session.fireRequestEvent(SRequestEvent.REQUEST_END);
             }
 
             if (outputDevice != null) {
                 try {
                     outputDevice.close();
                 } catch (Exception e) {
                 }
             }
 
             /*
              * the session might be null due to destroy().
              */
             if (session != null && !isErrorHandling) {
                 reloadManager.clear();
                 session.setServletRequest(null);
                 session.setServletResponse(null);
             }
 
 
             // make sure that the session association to the thread is removed
             // from the SessionManager
             SessionManager.removeSession();
             SForm.clearArmedComponents();
         }
     }
 
     /**
      * Searches the current session for the root HTML frame and returns the Resource
      * representing this root HTML frame (i.e. for you to retrieve the externalizer id
      * via <code>getId()</code>-method).
      * @return Resource of the root HTML frame
      */
     private Resource retrieveCurrentRootFrameResource() throws ServletException {
         log.debug("delivering default frame");
 
         if (session.getFrames().size() == 0)
             throw new ServletException("no frame visible");
 
         // get the first frame from the set and walk up the hierarchy.
         // this should work in most cases. if there are more than one
         // toplevel frames, the developer has to care about the resource
         // ids anyway ..
         SFrame defaultFrame = (SFrame) session.getFrames().iterator().next();
         while (defaultFrame.getParent() != null)
             defaultFrame = (SFrame) defaultFrame.getParent();
 
         return defaultFrame.getDynamicResource(DynamicCodeResource.class);
     }
 
     /**
      * In case of an error, display an error page to the user. This is only
      * done if there is a properties <code>wings.error.handler</code> defined
      * in the web.xml file. If the property is present, the following steps
      * are performed:
      * <li> Load the class named by the value of that property, using the
      *      current thread's context class loader,
      * <li> Instantiate that class using its zero-argument constructor,
      * <li> Cast the instance to ExceptionHandler,
      * <li> Invoke the handler's <tt>handle</tt> method, passing it the
      *      <tt>thrown</tt> argument that was passed to this method.
      * </ol>
      *
      * @see DefaultExceptionHandler
      * @param res the HTTP Response to use
      * @param thrown the Exception to report
      */
     protected void handleException(HttpServletResponse res, Throwable thrown) {
         try {
             // Try to return HTTP status code 500.
             // In many cases this will be too late (already stream output written)
            if (session.getUserAgent().getBrowserType() != BrowserType.IE)
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 
             ServletOutputStream out = res.getOutputStream();
             ServletDevice device = new ServletDevice(out);
 
             String className = (String)session.getProperty("wings.error.handler");
             if (className == null)
                 className = DefaultExceptionHandler.class.getName();
 
             ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
             Class clazz = classLoader.loadClass(className);
             ExceptionHandler exceptionHandler = (ExceptionHandler)clazz.newInstance();
             exceptionHandler.handle(device, thrown);
         }
         catch (Exception e) {
             log.warn(e.getMessage(), thrown);
         }
     }
 
     /**
      * This method is called, whenever a Resource is requested whose
      * name is not known within this session.
      *
      * @param req the causing HttpServletRequest
      * @param res the HttpServletResponse of this request
      */
     protected void handleUnknownResourceRequested(HttpServletRequest req,
                                                   HttpServletResponse res)
             throws IOException {
         res.setStatus(HttpServletResponse.SC_NOT_FOUND);
         res.setContentType("text/html");
         res.getOutputStream().println("<h1>404 Not Found</h1>Unknown Resource Requested");
     }
 
     /**
      * --- HttpSessionBindingListener --- *
      */
 
 
     public void valueBound(HttpSessionBindingEvent event) {
     }
 
 
     public void valueUnbound(HttpSessionBindingEvent event) {
         destroy();
     }
 
     /**
      * get the Session Encoding, that is appended to each URL.
      * Basically, this is response.encodeURL(""), but unfortuntatly, this
      * empty encoding isn't supported by Tomcat 4.x anymore.
      */
     public static String getSessionEncoding(HttpServletResponse response) {
         if (response == null) return "";
         // encode dummy non-empty URL.
         String enc = response.encodeURL("foo").substring(3);
         return enc;
     }
 
     /**
      * Destroy and cleanup the session servlet.
      */
     public void destroy() {
         log.info("destroy called");
         try {
             if (session != null) {
                 // Session is needed on destroying the session
                 SessionManager.setSession(session);
                 session.destroy();
             }
 
             // hint the gc.
             parent = null;
             session = null;
         } catch (Exception ex) {
             log.error("destroy", ex);
         } finally {
             SessionManager.removeSession();
         }
     }
 
     /**
      * A check if this session servlet seems to be alive or is i.e. invalid because it
      * was deserialized.
      *
      * @return <code>true</code>, if this session servlet seems to be valid and alive.
      */
     public boolean isValid() {
         return session != null && parent != null;
     }
 }
 
 
