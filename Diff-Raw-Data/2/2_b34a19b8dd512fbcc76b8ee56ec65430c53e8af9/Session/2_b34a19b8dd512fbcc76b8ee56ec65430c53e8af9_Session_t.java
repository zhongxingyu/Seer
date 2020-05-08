 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
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
 import org.wings.comet.Comet;
 import org.wings.comet.CometWingServlet;
 import org.wings.sdnd.SDragAndDropManager;
 import org.wings.sdnd.SDragAndDropManager;
 import org.wings.sdnd.SDragAndDropManager;
 import org.wings.dnd.DragAndDropManager;
 import org.wings.event.*;
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.externalizer.ExternalizedResource;
 import org.wings.plaf.*;
 import org.wings.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.swing.event.EventListenerList;
 import java.beans.PropertyChangeListener;
 import java.io.Serializable;
 import java.util.*;
 import java.awt.datatransfer.Clipboard;
 
 /**
  * This class represents a wingS session meaning an application user session instance.
  * Please do not mix this with a servlet {@link javax.servlet.http.HttpSession}!
  * <p/>A wings Session is a session instance hold by the global {@link WingServlet} servlet. It aggregates all per--user-session
  * data (mainly the root {@link SFrame}s and provides some information about the client like the browser {@link #getUserAgent()},
  * the current character encoding {@link #getCharacterEncoding()} or the used Locale {@link #getLocale()}.
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  */
 public class Session implements PropertyService, Serializable {
 
     private final static Log log = LogFactory.getLog(Session.class);
 
     /**
      * The property name of the locale
      */
     public final static String LOCALE_PROPERTY = "locale";
 
     /**
      * The property name of the sessions character encoding
      */
     public final static String CHARACTER_ENCODING_PROPERTY = "characterEncoding";
 
     /**
      * The property name of the look&feel
      */
     public final static String LOOK_AND_FEEL_PROPERTY = "lookAndFeel";
 
     private final SessionStatistics statistics = new SessionStatistics();
 
     /**
      * Every session has its own {@link CGManager}.
      *
      */
     private CGManager CGManager = new CGManager();
 
     private SToolTipManager toolTipManager = new SToolTipManager();
 
     private ReloadManager reloadManager = null;
 
     private MenuManager menuManager = null;
 
     private transient ExternalizeManager externalizeManager;
 
     private LowLevelEventDispatcher dispatcher = new LowLevelEventDispatcher(this);
 
     private final HashMap<String, Object> props = new HashMap<String, Object>();
 
     private final HashSet<SFrame> frames = new HashSet<SFrame>();
 
     private long uniqueIdCounter = 1;
 
     /**
      * Maximum upload content length. This is used by the {@link org.wings.session.SessionServlet}
      * to avoid denial of service attacks.
      */
     private int maxContentLength = 64;
 
     private transient ServletContext servletContext;
 
     private Browser browser;
 
     protected transient HttpServletResponse servletResponse;
 
     protected transient HttpServletRequest servletRequest;
 
     private String redirectAddress;
 
     private String exitAddress;
 
     private Locale locale = Locale.getDefault();
 
     private boolean localeFromHeader = true;
 
     private DragAndDropManager dndManager;
     private SDragAndDropManager sDndManager;
     private Clipboard clipboard;
     private SCursor cursor;
     
     private ScriptManager scriptManager;
 
     private Comet comet = null;
 
     private HttpServlet wingServlet;
 
     /**
      * Which locales are supported by this servlet. If null, every locale from
      * the userAgent is accepted. If not null only locales listed in this array
      * are supported.
      */
     private Locale[] supportedLocales = null;
 
     /**
      * The current character encoding used for the communication with the clients userAgent.
      * If <code>null</code> then the current characterEncoding is determined by the current
      * session Locale via the charset.properties map.
      */
     private String characterEncoding = null;
 
 
     /**
      * Store here only weak references.
      */
     protected final EventListenerList listenerList = new EventListenerList();
 
     private final WeakPropertyChangeSupport propertyChangeSupport = new WeakPropertyChangeSupport(this);
     private ResourceMapper resourceMapper;
     private Localizer localizer;
 
 
     public final SessionStatistics getStatistics() {
         return statistics;
     }
 
     static boolean collectStatistics = true;
 
     static final SRequestListener SESSION_STATISTIC_COLLECTOR = new SRequestListener() {
         public void processRequest(SRequestEvent e) {
             Session session = SessionManager.getSession();
             if (session == null) {
                 /* while exiting or destroy() the session: it
                  * might already be null in the session manager.
                  */
                 return;
             }
             switch (e.getType()) {
                 case SRequestEvent.DISPATCH_START:
                     session.getStatistics().startDispatching();
                     break;
                 case SRequestEvent.DISPATCH_DONE:
                     session.getStatistics().endDispatching();
                     break;
                 case SRequestEvent.DELIVER_START:
                     session.getStatistics().startDelivering();
                     break;
                 case SRequestEvent.DELIVER_DONE:
                     session.getStatistics().endDelivering();
                     break;
                 case SRequestEvent.REQUEST_START:
                     session.getStatistics().startRequest();
                     break;
                 case SRequestEvent.REQUEST_END:
                     session.getStatistics().endRequest();
                     break;
             }
         }
     };
 
 
     public Session() {
         //log.debug("new session()");
         if (collectStatistics) {
             WingsStatistics.getStatistics().incrementSessionCount();
             WingsStatistics.getStatistics().incrementActiveSessionCount();
             WingsStatistics.getStatistics().incrementAllocatedSessionCount();
 
             addRequestListener(SESSION_STATISTIC_COLLECTOR);
         } // end of if ()
     }
 
     public Session(HttpServlet wingServlet) {
         this();
         this.wingServlet = wingServlet;
     }
 
     /**
      * Detect user agent (userAgent). Copy init parameters. Set max content length for uploads / requests.
      * Install look and feel.
      *
      * @param servletConfig a <code>ServletConfig</code> value
      * @param request a <code>HttpServletRequest</code> value
      * @param response
      * @throws ServletException if an error occurs
      */
     public void init(ServletConfig servletConfig, HttpServletRequest request, HttpServletResponse response) throws ServletException {
         servletContext = request.getSession().getServletContext();
         setServletRequest(request);
         setServletResponse(response);
         setUserAgentFromRequest(request);
 
         if (isCometWingServletEnabled()) {
             comet = new Comet(this, wingServlet);
             comet.getConnectionManager().setBrowserId(request, response);
         }
 
         initProps(servletConfig);
         initMaxContentLength();
 
         try {
             LookAndFeel lookAndFeel = LookAndFeelFactory.getLookAndFeelFactory().create();
             CGManager.setLookAndFeel(lookAndFeel);
         } catch (Exception ex) {
             log.fatal("could not load look and feel: " +
                     servletContext.getInitParameter("wings.lookandfeel.factory"), ex);
             throw new ServletException(ex);
         }
     }
 
     /**
      * Detect user agent (userAgent). Copy init parameters. Set max content length for uploads / requests.
      * Install look and feel.
      *
      * @param request a <code>HttpServletRequest</code> value
      * @throws ServletException if an error occurs
      */
     public void init(HttpServletRequest request) throws ServletException {
         servletContext = request.getSession().getServletContext();
         setServletRequest(request);
         setUserAgentFromRequest(request);
 
         initProps(request.getSession().getServletContext());
         initMaxContentLength();
 
         try {
             LookAndFeel lookAndFeel = LookAndFeelFactory.getLookAndFeelFactory().create();
             CGManager.setLookAndFeel(lookAndFeel);
         } catch (Exception ex) {
             log.fatal("could not load look and feel: " +
             servletContext.getInitParameter("wings.lookandfeel.factory"), ex);
             throw new ServletException(ex);
         }
     }
 
     protected void initMaxContentLength() {
         String maxCL = getServletContext().getInitParameter("content.maxlength");
         if (maxCL != null) {
             try {
                 maxContentLength = Integer.parseInt(maxCL);
             } catch (NumberFormatException e) {
                 log.warn("invalid content.maxlength: " + maxCL, e);
             }
         }
     }
 
     /**
      * Copy the init parameters.
      */
     protected void initProps(ServletConfig servletConfig) {
         Enumeration params = servletConfig.getInitParameterNames();
         while (params.hasMoreElements()) {
             String name = (String) params.nextElement();
             props.put(name, servletConfig.getInitParameter(name));
         }
     }
 
     protected void initProps(ServletContext servletContext) {
         Enumeration params = servletContext.getInitParameterNames();
         while (params.hasMoreElements()) {
             String name = (String) params.nextElement();
             props.put(name, servletContext.getInitParameter(name));
         }
     }
 
     void setServletRequest(HttpServletRequest servletRequest) {
         this.servletRequest = servletRequest;
     }
 
 
     public HttpServletRequest getServletRequest() {
         return servletRequest;
     }
 
     /**
      * Sets the current servlet response in progress. Used by wingS framework
      */
     void setServletResponse(HttpServletResponse servletResponse) {
         this.servletResponse = servletResponse;
     }
 
     /**
      * The current HTTP servlet response which the framework will deliver
      * after processing the current request. This is a part of the Servlet
      * architecture.
      *
      * @return The current servlet response about to send to the client
      */
     public HttpServletResponse getServletResponse() {
         return servletResponse;
     }
 
     /**
      * The current servlet context provided by the underlying
      * servlet container.
      * This value is retrieved from the initial servlet request
      * for this session.
      *
      * @return The current servlet context provided by the underlying
      * servlet container.
      */
     public ServletContext getServletContext() {
         return servletContext;
     }
 
     /**
      * Override the current reload manager.
      *
      * @param reloadManager You customized reload manager implementation.
      */
     public void setReloadManager(ReloadManager reloadManager) {
         this.reloadManager = reloadManager;
     }
 
     /**
      * The reload manager responsible for the component invalidation
      * of the components contained in this wingS session. (Epoch counter)
      *
      * @return Lazily constructs {@link DefaultReloadManager} if no other reload
      * manager has been set
      */
     public ReloadManager getReloadManager() {
         if (reloadManager == null)
             reloadManager = new DefaultReloadManager();
         return reloadManager;
     }
 
     public MenuManager getMenuManager() {
         if (menuManager == null)
             menuManager = new MenuManager();
         return menuManager;
     }
 
     /**
      * The Externalize manager is response to provide all {@link org.wings.Resource}
      * via HTTP to the client.
      *
      * @return The externalize manager responsible to externalize all sort
      * of resources contained in this session.
      */
     public ExternalizeManager getExternalizeManager() {
         if (externalizeManager == null)
             externalizeManager = new ExternalizeManager();
         return externalizeManager;
     }
 
     /**
      * The Script manager collects scripts
      *
      * @return The script manager responsible to script all sort
      * of resources contained in this session.
      */
     public ScriptManager getScriptManager() {
         if (scriptManager == null)
             scriptManager = new ScriptManager();
         return scriptManager;
     }
 
     /**
      * The CG manager is responsible to provide the renderer implementation (aka. PLAF)
      * for a given component class.
      *
      * @return The current CG manager
      */
     public CGManager getCGManager() {
         return CGManager;
     }
 
     /**
      * @return The tooltip manager object containing configuration values on the components
      * tooltip behaviour
      */
     public SToolTipManager getToolTipManager() {
         return toolTipManager;
     }
 
     /**
      * Returns the current browser of the client detected by the
      * <code>User-Agent</code> parameter of the initial HTTP request
      * for this user session.
      *
      * @return A {@link Browser} object providing browser type, os type and
      *         other informations from the HTTP <code>User-Agent</code> string.
      */
     public Browser getUserAgent() {
         return browser;
     }
 
     /* *  This would be a better naming!
      * Returns the current browser of the client detected by the
      * <code>User-Agent</code> parameter of the initial HTTP request
      * for this user session.
      *
      * @return A {@link Browser} object providing browser type, os type and
      *         other informations from the HTTP <code>User-Agent</code> string.
      * /
     public Browser getBrowser() {
         return browser;
     } */
 
 
     /**
      * Describe <code>setUserAgentFromRequest</code> method here.
      *
      * @param request a <code>HttpServletRequest</code> value
      */
     public void setUserAgentFromRequest(HttpServletRequest request) {
         try {
             final String userAgentString = request.getHeader("User-Agent");
             browser = new Browser(userAgentString);
             log.debug("Browser is detected as " + browser+". User-Agent was: "+userAgentString);
             log.debug("major version = "+browser.getMajorVersion()+", id = "+browser.getBrowserType().getId());
             log.debug("short name = "+browser.getBrowserType().getShortName());
         } catch (Exception ex) {
             log.warn("Cannot get User-Agent from request", ex);
         }
     }
 
     /**
      * The low level event dispatcher is responsible for taking an HTTP request,
      * parse it contents and delegate the so called low level events to the
      * registered {@link org.wings.LowLevelEventListener}s (i.e. Buttons, etc.)
      *
      * @return The low level event dispatcher responsible for this session.
      */
     public LowLevelEventDispatcher getDispatcher() {
         return dispatcher;
     }
 
     /**
      * Describe <code>addFrame</code> method here.
      *
      * @param frame a <code>SFrame</code> value
      */
     public void addFrame(SFrame frame) {
         frames.add(frame);
     }
 
     /**
      * Describe <code>removeFrame</code> method here.
      *
      * @param frame a <code>SFrame</code> value
      */
     public void removeFrame(SFrame frame) {
         frames.remove(frame);
     }
 
     /**
      * Describe <code>frames</code> method here.
      *
      * @return a <code>Set</code> value
      */
     public Set<SFrame> getFrames() {
         return Collections.unmodifiableSet(frames);
     }
 
     /**
      * The root frame is the first shown frame.
      *
      * @return a <code>SFrame</code> value
      */
     public SFrame getRootFrame() {
         if (frames.isEmpty())
             return null;
 
         SFrame rootFrame = frames.iterator().next();
         while (rootFrame.getParent() != null)
             rootFrame = (SFrame) rootFrame.getParent();
 
         return rootFrame;
     }
 
 
     public SComponent getComponentByName( String name ) {
         return getComponentByName( this.getRootFrame(), name );
     }
 
     /**
      * Search in the given SContainer for the SComponent with the given name.
      * @param container The SContainer where you want to search for the SComponent with the given name.
      * @param name The Name of the SComponent
      * @return the SComponent with the given name
      */
     public SComponent getComponentByName( SContainer container, String name ) {
        SComponent component = null;
        SComponent[] components = container.getComponents();
        for ( int x = 0, y = components.length ; x < y ; x++ ) {
            SComponent component_x = components[x];
            if ( component_x.getName().equals(name) ) {
                component = component_x;
                break;
            } else if ( component_x instanceof SContainer ) {
                component = getComponentByName( (SContainer)component_x, name );
                if ( component != null ) {
                    break;
                }
            }
        }
        return component;
     }
 
     /**
      * Describe <code>getProperties</code> method here.
      *
      * @return a <code>Map</code> value
      */
     public final Map getProperties() {
         return Collections.unmodifiableMap(props);
     }
 
     /**
      * Gets the session property indicated by the specified key.
      *
      * @param key the name of the session property.
      * @return the string value of the session property,
      *         or <code>null</code> if there is no property with that key.
      */
     public Object getProperty(String key) {
         return props.get(key);
     }
 
     /**
      * Gets the session property indicated by the specified key.
      *
      * @param key the name of the session property.
      * @param def a default value.
      * @return the string value of the session property,
      *         or the default value if there is no property with that key.
      * @see org.wings.session.PropertyService#getProperties()
      */
     public Object getProperty(String key, Object def) {
         if (!props.containsKey(key)) {
             return def;
         } else {
             return props.get(key);
         }
     }
 
     /**
      * Sets the session property indicated by the specified key.
      *
      * @param key   the name of the session property.
      * @param value the value of the session property.
      * @return the previous value of the session property,
      *         or <code>null</code> if it did not have one.
      * @see org.wings.session.PropertyService#getProperty(java.lang.String)
      * @see org.wings.session.PropertyService#getProperty(java.lang.String, java.lang.Object)
      */
     public Object setProperty(String key, Object value) {
         Object old = props.put(key, value);
         propertyChangeSupport.firePropertyChange(key, old, value);
         return old;
     }
 
     /* @see PropertyService */
     public boolean containsProperty(String key) {
         return props.containsKey(key);
     }
 
     /* @see PropertyService */
     public Object removeProperty(String key) {
         Object old = props.remove(key);
         propertyChangeSupport.firePropertyChange(key, old, null);
         return old;
     }
 
     public void addPropertyChangeListener(PropertyChangeListener listener) {
         propertyChangeSupport.addPropertyChangeListener(listener);
     }
 
 
     public void removePropertyChangeListener(PropertyChangeListener listener) {
         propertyChangeSupport.removePropertyChangeListener(listener);
     }
 
     /**
      * Describe <code>addPropertyChangeListener</code> method here.
      *
      * @param propertyName a <code>String</code> value
      * @param listener     a <code>PropertyChangeListener</code> value
      */
     public void addPropertyChangeListener(String propertyName,
                                           PropertyChangeListener listener) {
         propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
     }
 
     /**
      * Describe <code>removePropertyChangeListener</code> method here.
      *
      * @param propertyName a <code>String</code> value
      * @param listener     a <code>PropertyChangeListener</code> value
      */
     public void removePropertyChangeListener(String propertyName,
                                              PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
     }
 
 
     /**
      * Sets the locale for this session. A property change event is fired, if the locale has actually changed.
      *
      * @param locale the locale to be associated with this session.
      */
     public void setLocale(Locale locale) throws IllegalArgumentException {
         Locale old = this.locale;
         if (locale == null || this.locale.equals(locale))
             return;
 
         this.locale = locale;
         propertyChangeSupport.firePropertyChange(LOCALE_PROPERTY, old, locale);
     }
 
     /**
      * The Locale of the current session. This Locale reflects the Locale of the clients userAgent.
      *
      * @return a <code>Locale</code> value
      */
     public Locale getLocale() {
         return locale;
     }
 
     /**
      * Indicates if the wings session servlet should adopt the clients Locale provided by the
      * browsers in the HTTP header.
      *
      * @param adoptLocale if true, try to determine, false ignore
      */
     public final void setLocaleFromHeader(boolean adoptLocale) {
         localeFromHeader = adoptLocale;
         if (localeFromHeader)
             determineLocale();
     }
 
     /**
      * Indicates if the wings session servlet should adopt the clients Locale provided by the
      * browsers in the HTTP header.
      */
     public final boolean getLocaleFromHeader() {
         return localeFromHeader;
     }
 
     /**
      * sets the locales, supported by this application. If empty or <em>null</em>, all locales are supported.
      */
     public final void setSupportedLocales(Locale[] locales) {
         supportedLocales = locales;
         localeFromHeader = true;
         determineLocale();
     }
 
     void determineLocale() {
         if (supportedLocales == null)
             setLocale(servletRequest.getLocale());
 
         Enumeration<Locale> requestedLocales = servletRequest.getLocales();
         if (supportedLocales != null) {
             while (requestedLocales.hasMoreElements()) {
                 Locale locale = requestedLocales.nextElement();
                 for (int i = 0; i < supportedLocales.length; i++) {
                     Locale supportedLocale = supportedLocales[i];
                     if (locale.equals(supportedLocale)) {
                         setLocale(supportedLocale);
                         return;
                     }
                 }
             }
             log.warn("locale not supported " + locale);
             setLocale(supportedLocales[0]);
         }
         else
             setLocale(requestedLocales.nextElement());
     }
 
     /**
      * Returns the locales, supported by this application. If empty or <em>null</em>, all locales are supported.
      */
     public final Locale[] getSupportedLocales() {
         return supportedLocales;
     }
 
 
     /**
      * The current character encoding used for the communication with the clients userAgent.
      * If <code>null</code> then the current characterEncoding is determined by the current
      * session Locale via the charset.properties map.
      *
      * @param characterEncoding The charcterEncoding which should be enforces for this session (i.e. "utf-8"),
      *                          or <code>null</code> if it should be determined by the clients userAgent Locale.
      */
     public void setCharacterEncoding(String characterEncoding) {
         String oldEncoding = this.characterEncoding;
         this.characterEncoding = characterEncoding;
         propertyChangeSupport.firePropertyChange(CHARACTER_ENCODING_PROPERTY, oldEncoding, characterEncoding);
     }
 
     /**
      * The current character encoding used for the communication with the clients userAgent.
      * If <code>null</code> then the current characterEncoding is determined by the current
      * session Locale via the charset.properties map.
      *
      * @return The characterEncoding set for this sesson or determined by the current Locale.
      */
     public String getCharacterEncoding() {
         if (this.characterEncoding == null) {
             return LocaleCharSet.getInstance().getCharSet(getLocale());
         } else {
             return this.characterEncoding;
         }
     }
 
     private final long getUniqueId() {
         return uniqueIdCounter++;
     }
 
     /**
      * Creates a session context unique ID, that can be used as an identifier,
      * i.e. it is guaranteed to start with a letter
      *
      * @return a <code>String</code> value
      */
     public String createUniqueId() {
         return StringUtil.toIdentifierString(getUniqueId());
     }
 
     /**
      * Get the maximum content length (file size) for a post
      * request.
      *
      * @return maximum size in kB (1024 Byte)
      * @see org.wings.session.MultipartRequest
      */
     public final int getMaxContentLength() {
         return maxContentLength;
     }
 
     /**
      * Set the maximum content length (file size) for a post
      * request.
      *
      * @param l size in kB (1024 Byte)
      * @see org.wings.session.MultipartRequest
      */
     public final void setMaxContentLength(int l) {
         maxContentLength = l;
     }
 
     protected void destroy() {
 
         try {
             firePrepareExit(true);
         } catch (ExitVetoException ex) {
             // ignore this, because no veto possible
         }
 
         if (collectStatistics) {
             WingsStatistics.getStatistics().decrementActiveSessionCount();
         } // end of if ()
 
 
         /*
         Iterator it = frames.iterator();
         while (it.hasNext()) {
             SContainer container = ((SFrame) it.next()).getContentPane();
             if (container != null)
                 container.removeAll();
         }
 
        reloadManager.clear();
         reloadManager = null;
         if (externalizeManager != null) // eexternalizeManager is transient!
             externalizeManager.clear();
         externalizeManager = null;
         dispatcher.clear();
         dispatcher = null;
 
         frames.clear();
         props.clear();
         */
 
         Object[] listeners = listenerList.getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             listenerList.remove((Class) listeners[i], (EventListener) listeners[i + 1]);
         } // end of for (int i=0; i<; i++)
 
     }
 
     /**
      * Exit the current session and redirect to other URL.
      * <p/>
      * This removes the session and its associated
      * application from memory. The userAgent is redirected to the given
      * URL. Note, that it is not even possible for the user to re-enter
      * the application with the BACK-button, since all information is
      * removed.
      * <p/>
      * <em>Always</em> exit an application by calling an
      * <code>exit()</code> method, especially, if it is an application
      * that requires a login and thus handles sensitive information accessible
      * through the session. Usually, you will call this on behalf of an
      * event within an <code>ActionListener.actionPerformed()</code> like for
      * a pressed 'EXIT'-Button.
      *
      * @param redirectAddress the address, the userAgent is redirected after
      *                        removing this session. This must be a String
      *                        containing the complete URL (no relative URL)
      *                        to the place to be redirected. If 'null', nothing
      *                        happens.
      */
     public void exit(String redirectAddress) {
         this.exitAddress = redirectAddress;
         for (SFrame frame : frames)
             frame.hide();
     }
 
     /**
      * Exit the current session and redirect to new application instance.
      * <p/>
      * This removes the session and its associated
      * application from memory. The userAgent is redirected to the same
      * application with a fresh session. Note, that it is not even
      * possible for the user to re-enter the old application with the
      * BACK-button, since all information is removed.
      * <p/>
      * <em>Always</em> exit an application by calling an
      * <code>exit()</code> method, especially, if it is an application
      * that requires an login and thus handles sensitive information accessible
      * through the session. Usually, you will call this on behalf of an
      * event within an <code>ActionListener.actionPerformed()</code> like for
      * a pressed 'EXIT'-Button.
      */
     public void exit() {
         exit("");
     }
 
     public String getExitAddress() {
         return exitAddress;
     }
 
     public String getRedirectAddress() {
         return redirectAddress;
     }
 
     public void setRedirectAddress(String redirectAddress) {
         this.redirectAddress = redirectAddress;
     }
 
 
     public void addExitListener(SExitListener listener) {
         listenerList.add(SExitListener.class,
                 listener);
     }
 
 
     public void removeExitListener(SExitListener listener) {
         listenerList.remove(SExitListener.class,
                 listener);
     }
 
     public SExitListener[] getExitListeners() {
         return (SExitListener[]) listenerList.getListeners(SExitListener.class);
     }
 
 
     /**
      * Fire an RequestEvent at each registered listener.
      */
     final void firePrepareExit() throws ExitVetoException {
         firePrepareExit(false);
     }
 
     final void firePrepareExit(boolean ignoreVeto) throws ExitVetoException {
         SExitEvent event = null;
 
         Object[] listeners = listenerList.getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == SExitListener.class) {
                 // Lazily create the event:
                 if (event == null) {
                     event = new SExitEvent(this);
                 }
                 try {
                     ((SExitListener) listeners[i + 1]).prepareExit(event);
                 } catch (ExitVetoException ex) {
                     if (!ignoreVeto) {
                         throw ex;
                     }
                 }
             }
         }
     }
 
 
     public void addRequestListener(SRequestListener listener) {
         listenerList.add(SRequestListener.class, listener);
     }
 
 
     public void removeRequestListener(SRequestListener listener) {
         listenerList.remove(SRequestListener.class, listener);
     }
 
     /**
      * Fire an RequestEvent at each registered listener.
      */
     void fireRequestEvent(int type) {
         fireRequestEvent(type, null);
     }
 
     /**
      * Fire an RequestEvent at each registered listener.
      */
     void fireRequestEvent(int type, ExternalizedResource resource) {
         SRequestEvent event = null;
 
         Object[] listeners = listenerList.getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == SRequestListener.class) {
                 // Lazily create the event:
                 if (event == null) {
                     event = new SRequestEvent(this, type, resource);
                 }
                 ((SRequestListener) listeners[i + 1]).processRequest(event);
             }
         }
     }
 
     protected void finalize() {
         log.debug("gc session");
         if (collectStatistics) {
             WingsStatistics.getStatistics().decrementAllocatedSessionCount();
         } // end of if ()
     }
 
     public SCursor getCursor() {
         if(this.cursor == null)
             this.cursor = new SCursor();
         return this.cursor;
     }
 
     public Clipboard getClipboard() {
         if(this.clipboard == null)
             this.clipboard = new Clipboard("wingS Clipboard");
 
         return this.clipboard;
     }
     
     public boolean hasSDragAndDropManager() {
         return sDndManager != null;
     }
 
     public SDragAndDropManager getSDragAndDropManager() {
         if(sDndManager == null)
             sDndManager = new SDragAndDropManager();
         return sDndManager;
     }
     
     public boolean hasDragAndDropManager() {
         return dndManager != null;
     }
 
     public DragAndDropManager getDragAndDropManager() {
         if (dndManager == null) {
             dndManager = new DragAndDropManager();
         }
         return dndManager;
     }
 
     public void setDndManager(DragAndDropManager dndManager) {
         this.dndManager = dndManager;
     }
 
     public ResourceMapper getResourceMapper() {
         return resourceMapper;
     }
 
     public void setResourceMapper(ResourceMapper resourceMapper) {
         this.resourceMapper = resourceMapper;
     }
 
     public Comet getComet() {
         return comet;
     }
 
     public boolean isCometWingServletEnabled() {
         return (wingServlet instanceof CometWingServlet);
     }
 
     public Localizer getLocalizer() {
         if (localizer == null)
             localizer = new Localizer() {
                 public String getString(String key) {
                     return ResourceBundle.getBundle("Bundle", getLocale()).getString(key);
                 }
             };
         return localizer;
     }
 
     public void setLocalizer(Localizer localizer) {
         this.localizer = localizer;
     }
 }
