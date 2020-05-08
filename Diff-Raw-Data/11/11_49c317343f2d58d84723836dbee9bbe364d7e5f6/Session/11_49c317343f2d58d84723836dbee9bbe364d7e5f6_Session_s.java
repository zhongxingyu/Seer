 /*
  * $Id$
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
 
 
 import java.beans.PropertyChangeListener;
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.swing.event.EventListenerList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.DefaultReloadManager;
 import org.wings.ReloadManager;
 import org.wings.SContainer;
 import org.wings.SFrame;
 import org.wings.SToolTipManager;
 import org.wings.event.ExitVetoException;
 import org.wings.event.SExitEvent;
 import org.wings.event.SExitListener;
 import org.wings.event.SRequestEvent;
 import org.wings.event.SRequestListener;
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.externalizer.ExternalizedResource;
 import org.wings.plaf.CGManager;
 import org.wings.plaf.LookAndFeel;
 import org.wings.plaf.LookAndFeelFactory;
 import org.wings.util.LocaleCharSet;
 import org.wings.util.StringUtil;
 import org.wings.util.WeakPropertyChangeSupport;
 
 /**
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @version $Revision$
  */
 public class Session
         implements PropertyService, Serializable {
 
     private final transient static Log log = LogFactory.getLog(Session.class);
 
     /**
      * The property name of the locale
      */
     public final static String LOCALE_PROPERTY = "locale";
 
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
 
     private ExternalizeManager externalizeManager = new ExternalizeManager();
 
     private LowLevelEventDispatcher dispatcher = new LowLevelEventDispatcher();
 
     private final HashMap props = new HashMap();
 
     private final HashSet frames = new HashSet();
 
     private long uniqueIdCounter = 1;
 
     /**
     * Maximum upload content length. This is used by the {@link SessionServlet}
      * to avoid denial of service attacks.
      */
     private int maxContentLength = 64;
 
     private transient ServletContext servletContext;
 
     private transient Browser userAgent;
 
     protected transient HttpServletResponse servletResponse;
 
     protected transient HttpServletRequest servletRequest;
 
     private String redirectAddress;
 
     private String exitAddress;
 
     private Locale locale = Locale.getDefault();
 
     private boolean localeFromHeader = true;
 
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
         if (collectStatistics) {
             WingsStatistics.getStatistics().incrementSessionCount();
             WingsStatistics.getStatistics().incrementActiveSessionCount();
             WingsStatistics.getStatistics().incrementAllocatedSessionCount();
 
             addRequestListener(SESSION_STATISTIC_COLLECTOR);
         } // end of if ()
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
 
         initProps();
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
         String maxCL = servletContext.getInitParameter("content.maxlength");
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
     protected void initProps() {
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
 
     void setServletResponse(HttpServletResponse servletResponse) {
         this.servletResponse = servletResponse;
     }
 
     public HttpServletResponse getServletResponse() {
         return servletResponse;
     }
 
     public ServletContext getServletContext() {
         return servletContext;
     }
 
     public void setReloadManager(ReloadManager reloadManager) {
         this.reloadManager = reloadManager;
     }
 
     public ReloadManager getReloadManager() {
         if (reloadManager == null)
             reloadManager = new DefaultReloadManager();
         return reloadManager;
     }
 
     public ExternalizeManager getExternalizeManager() {
         return externalizeManager;
     }
 
     public CGManager getCGManager() {
         return CGManager;
     }
 
     public SToolTipManager getToolTipManager() {
         return toolTipManager;
     }
 
     /**
      * Get the user agent (userAgent) used for
      * this session by the user.
      *
      * @return a <code>Browser</code> value
      */
     public Browser getUserAgent() {
         return userAgent;
     }
 
     /**
      * Describe <code>setUserAgentFromRequest</code> method here.
      *
      * @param request a <code>HttpServletRequest</code> value
      */
     public void setUserAgentFromRequest(HttpServletRequest request) {
         try {
             userAgent = new Browser(request.getHeader("User-Agent"));
             log.debug("User-Agent is " + userAgent);
         } catch (Exception ex) {
             log.warn("Cannot get User-Agent from request", ex);
         }
     }
 
 
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
     public Set getFrames() {
         return frames;
     }
 
     /**
      * The root frame is the first shown frame.
      *
      * @return a <code>SFrame</code> value
      */
     public SFrame getRootFrame() {
         if (frames.size() == 0)
             return null;
 
         SFrame rootFrame = (SFrame) frames.iterator().next();
         while (rootFrame.getParent() != null)
             rootFrame = (SFrame) rootFrame.getParent();
 
         return rootFrame;
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
         //System.err.print("DefaultSession.setProperty");
         Object old = props.put(key, value);
         propertyChangeSupport.firePropertyChange(key, old, value);
         return old;
     }
 
     public boolean containsProperty(String key) {
         return props.containsKey(key);
     }
 
     public Object removeProperty(String key) {
         //System.err.print("DefaultSession.setProperty");
         Object old = props.remove(key);
         propertyChangeSupport.firePropertyChange(key, old, null);
         return old;
     }
 
     private final WeakPropertyChangeSupport propertyChangeSupport =
             new WeakPropertyChangeSupport(this);
 
 
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
         propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
     }
 
 
     /**
      * sets a new locale for this session. The locale is <em>only</em> set,
      * if it is one of the supported locales {@link #setSupportedLocales},
      * otherwise an IllegalArgumentException is thrown.
      *
      * @param l the locale to be associated with this session.
      * @throws IllegalArgumentException if this locale is not supported, as
      *                                  predefined with {@link #setSupportedLocales}.
      */
     public void setLocale(Locale l) throws IllegalArgumentException {
         if (l == null || locale.equals(l))
             return;
         if (supportedLocales == null ||
                 supportedLocales.length == 0 ||
                 Arrays.asList(supportedLocales).contains(l)) {
             locale = l;
             propertyChangeSupport.firePropertyChange(LOCALE_PROPERTY, locale, l);
             log.info("Set Locale " + l);
         } else
             throw new IllegalArgumentException("Locale " + l + " not supported");
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
         this.characterEncoding = characterEncoding;
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
     public final String createUniqueId() {
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
 
     /**
      * Describe <code>destroy</code> method here.
      */
     protected void destroy() {
 
         try {
             firePrepareExit(true);
         } catch (ExitVetoException ex) {
             // ignore this, because no veto possible
         }
 
         if (collectStatistics) {
             WingsStatistics.getStatistics().decrementActiveSessionCount();
         } // end of if ()
 
 
         Iterator it = frames.iterator();
         while (it.hasNext()) {
             SContainer container = ((SFrame) it.next()).getContentPane();
             if (container != null)
                 container.removeAll();
         }
 
         reloadManager.clear();
         reloadManager = null;
         externalizeManager.clear();
         externalizeManager = null;
         dispatcher.clear();
         dispatcher = null;
 
         frames.clear();
         props.clear();
 
 
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
 
     /**
      * Describe <code>setRedirectAddress</code> method here.
      *
      * @param redirectAddress a <code>String</code> value
      */
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
 
     /**
      * Describe <code>getExitListeners</code> method here.
      *
      * @return a <code>SExitListener[]</code> value
      */
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
         log.info("gc session");
         if (collectStatistics) {
             WingsStatistics.getStatistics().decrementAllocatedSessionCount();
         } // end of if ()
     }
 }
 
 
