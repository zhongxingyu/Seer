 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
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
 import java.lang.ref.WeakReference;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.swing.event.EventListenerList;
 import org.wings.DefaultReloadManager;
 import org.wings.ReloadManager;
 import org.wings.SContainer;
 import org.wings.SFrame;
 import org.wings.event.SRequestEvent;
 import org.wings.event.SRequestListener;
 import org.wings.event.WeakRequestListenerProxy;
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.externalizer.ExternalizedResource;
 import org.wings.plaf.CGManager;
 import org.wings.plaf.LookAndFeelFactory;
 import org.wings.util.StringUtil;
 import org.wings.util.WeakPropertyChangeSupport;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @version $Revision$
  */
 public final class Session
     implements PropertyService, Serializable {
 
     private static Logger logger = Logger.getLogger("org.wings.session");
 
     /**
      * The property name of the locale
      *
      */
     public static String LOCALE_PROPERTY = "locale";
 
     /**
      * The property name of the look&feel
      *
      */
     public static String LOOK_AND_FEEL_PROPERTY = "lookAndFeel";
 
     /**
      * Every session has its own {@link CGManager}. 
      *
      */
     private CGManager cgManager = new CGManager();
 
     private ReloadManager reloadManager = null;
     private ExternalizeManager extManager = new ExternalizeManager();
     private LowLevelEventDispatcher dispatcher = new LowLevelEventDispatcher();
 
     private final HashMap props = new HashMap();
     private final HashSet frames = new HashSet();
 
     private int uniqueIdCounter = 1;
     
     /**
      * Maximum upload content length. This is used by the {@link SessionServlet}
      * to avoid denial of service attacks.
      */
     private int maxContentLength = 64;
 
     private transient ServletContext servletContext;
     
     private transient Browser browser;
 
     private transient HttpServletResponse servletResponse;
 
     private transient HttpServletRequest servletRequest;
 
     private String redirectAddress;
     private String exitAddress;
 
 
     /**
      * Store here only weak references. 
      *
      */
     private final EventListenerList listenerList = new EventListenerList();
 
     public static final int getOverallSessions() {
         return SessionServlet.getOverallSessions();
     }
 
     public static final int getActiveSessions() {
         return SessionServlet.getActiveSessions();
     }
    
 
     /**
      * TODO: documentation
      *
      */
     public Session()  {
     }
 
     /**
      * TODO: documentation
      *
      * @param config
      * @param request a <code>HttpServletRequest</code> value
      * @exception ServletException if an error occurs
      */
     public void init(ServletConfig config, HttpServletRequest request) throws ServletException {
         servletContext = config.getServletContext();
         setServletRequest(request);
         setUserAgentFromRequest(request);
 
         if (config == null)
             return;
 
         initProps(config);
         initMaxContentLength(config);
 
         try {
             getCGManager().setLookAndFeel(LookAndFeelFactory.createLookAndFeel());
         } catch (Exception ex) {
             logger.log(Level.SEVERE, "could not load look and feel: " +
                        config.getInitParameter("wings.lookandfeel.factory"), ex);
             throw new ServletException(ex);
         }
 
     }
 
     /**
      * Describe <code>initMaxContentLength</code> method here.
      *
      * @param config a <code>ServletConfig</code> value
      */
     protected void initMaxContentLength(ServletConfig config) {
         String maxCL = config.getInitParameter("content.maxlength");
         if (maxCL != null) {
             try {
                 maxContentLength = Integer.parseInt(maxCL);
             }
             catch (NumberFormatException e) {
                 logger.log(Level.WARNING, "invalid content.maxlength: " + maxCL, e);
             }
         }
     }
 
     /**
      * Copy the init parameters.
      *
      * @param config
      */
     protected void initProps(ServletConfig config) {
         Enumeration params = config.getInitParameterNames();
         while(params.hasMoreElements()) {
             String name = (String)params.nextElement();
             props.put(name, config.getInitParameter(name));
         }
     }
 
     void setServletRequest(HttpServletRequest servletRequest) {
         this.servletRequest = servletRequest;
     }
     
     
     /**
      * Describe <code>getServletRequest</code> method here.
      *
      * @return a <code>HttpServletRequest</code> value
      */
     public HttpServletRequest getServletRequest() {
         return servletRequest;
     }
 
     void setServletResponse(HttpServletResponse servletResponse) {
         this.servletResponse = servletResponse;
     }
     /**
      * Describe <code>getServletResponse</code> method here.
      *
      * @return a <code>HttpServletResponse</code> value
      */
     public HttpServletResponse getServletResponse() {
         return servletResponse;
     }
 
     /**
      * Describe <code>getServletContext</code> method here.
      *
      * @return a <code>ServletContext</code> value
      */
     public ServletContext getServletContext() {
         return servletContext;
     }
 
     /**
      * Describe <code>getCGManager</code> method here.
      *
      * @return a <code>CGManager</code> value
      */
     public CGManager getCGManager() {
         return cgManager;
     }
 
     /**
      * Describe <code>setReloadManager</code> method here.
      *
      * @param reloadManager a <code>ReloadManager</code> value
      */
     public void setReloadManager(ReloadManager reloadManager) {
         this.reloadManager = reloadManager;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public ReloadManager getReloadManager() {
         if (reloadManager == null)
             reloadManager = new DefaultReloadManager();
         return reloadManager;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public ExternalizeManager getExternalizeManager() {
         return extManager;
     }
 
     /**
      * Get the user agent (browser) used for
      * this session by the user.
      * @return a <code>Browser</code> value
      */
     public Browser getUserAgent() {
         return browser;
     }
 
     /**
      * Describe <code>setUserAgentFromRequest</code> method here.
      *
      * @param request a <code>HttpServletRequest</code> value
      */
     public void setUserAgentFromRequest(HttpServletRequest request) {
         try {
             browser = new Browser(request.getHeader("User-Agent"));
             logger.fine("User-Agent is " + browser);
         } catch (Exception ex)  {
             logger.log(Level.WARNING, "Cannot get User-Agent from request", ex);
         }
     }
 
     /**
      * TODO: documentation
      *
      * @return
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
     public Set frames() {
         return frames;
     }
 
     /**
      * The root frame is the first shown frame.
      * @return a <code>SFrame</code> value
      */
     public SFrame getRootFrame() {
         if ( frames.size() == 0)
             return null;
 
         SFrame rootFrame = (SFrame)frames.iterator().next();
         while (rootFrame.getParent() != null)
             rootFrame = (SFrame)rootFrame.getParent();
 
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
      * @param      key   the name of the session property.
      * @return     the string value of the session property,
      *             or <code>null</code> if there is no property with that key.
      */
     public Object getProperty(String key) {
         return props.get(key);
     }
 
     /**
      * Gets the session property indicated by the specified key.
      *
      * @param      key   the name of the session property.
      * @param      def   a default value.
      * @return     the string value of the session property,
      *             or the default value if there is no property with that key.
      * @see        org.wings.session.PropertyService#getProperties()
      */
     public synchronized Object getProperty(String key, Object def) {
         if ( !props.containsKey(key) ) {
             return def;
         } else {
             return props.get(key);
         }
     }
 
     /**
      * Sets the session property indicated by the specified key.
      *
      * @param      key   the name of the session property.
      * @param      value the value of the session property.
      * @return     the previous value of the session property,
      *             or <code>null</code> if it did not have one.
      * @see        org.wings.session.PropertyService#getProperty(java.lang.String)
      * @see        org.wings.session.PropertyService#getProperty(java.lang.String, java.lang.String)
      */
     public synchronized Object setProperty(String key, Object value) {
         //System.err.print("DefaultSession.setProperty");
         Object old = props.put(key, value);
         propertyChangeSupport.firePropertyChange(key, old, value);
         return old;
     }
 
     private final WeakPropertyChangeSupport propertyChangeSupport = 
         new WeakPropertyChangeSupport(this);
 
     /**
      * TODO: documentation
      *
      * @param listener
      */
     public void addPropertyChangeListener(PropertyChangeListener listener) {
         propertyChangeSupport.addPropertyChangeListener(listener);
     }
 
     /**
      * TODO: documentation
      *
      * @param listener
      */
     public void removePropertyChangeListener(PropertyChangeListener listener) {
         propertyChangeSupport.removePropertyChangeListener(listener);
     }
 
     /**
      * Describe <code>addPropertyChangeListener</code> method here.
      *
      * @param propertyName a <code>String</code> value
      * @param listener a <code>PropertyChangeListener</code> value
      */
     public void addPropertyChangeListener(String propertyName, 
                                           PropertyChangeListener listener) {
         propertyChangeSupport.addPropertyChangeListener(propertyName,listener);
     }
 
     /**
      * Describe <code>removePropertyChangeListener</code> method here.
      *
      * @param propertyName a <code>String</code> value
      * @param listener a <code>PropertyChangeListener</code> value
      */
     public void removePropertyChangeListener(String propertyName, 
                                              PropertyChangeListener listener) {
         propertyChangeSupport.addPropertyChangeListener(propertyName,listener);
     }
     
     private Locale locale = Locale.getDefault();
 
     /**
      * TODO: documentation
      *
      * @param l
      */
     public void setLocale(Locale l) {
         if ( l==null || locale.equals(l) )
             return;
         locale = l;
         propertyChangeSupport.firePropertyChange(LOCALE_PROPERTY, locale, l);
     }
 
     /**
      * TODO: documentation
      * @return a <code>Locale</code> value
      */
     public final Locale getLocale() {
         return locale;
     }
 
     private final synchronized int getUniqueId() {
         return uniqueIdCounter++;
     }
 
     /**
      * Describe <code>createUniqueId</code> method here.
      *
      * @return a <code>String</code> value
      */
     public final String createUniqueId() {
         return StringUtil.toShortestAlphaNumericString(getUniqueId());
     }
 
     /**
      * Get the maximum content length (file size) for a post
      * request.
      * @return maximum size in kB (1024 Byte)
      * @see org.wings.session.MultipartRequest
      */
     public final int getMaxContentLength() {
         return maxContentLength;
     }
     
     /**
      * Set the maximum content length (file size) for a post
      * request.
      * @param l size in kB (1024 Byte)
      * @see org.wings.session.MultipartRequest
      */
     public final void setMaxContentLength(int l) {
         maxContentLength = l;
     }
 
     /**
      * Describe <code>destroy</code> method here.
      *
      */
     protected void destroy() {
         Iterator it = frames.iterator();
         while (it.hasNext()) {
             SContainer container = ((SFrame)it.next()).getContentPane();
             if (container != null)
                 container.removeAll();
         }
         
         reloadManager.clear();
         reloadManager = null;
         extManager.clear();
         extManager = null;
         dispatcher.clear();
         dispatcher = null;
 
         frames.clear();
         props.clear();
 
         Object[] listeners = listenerList.getListenerList(); 
        for ( int i = listeners.length-2; i>=0; i -= 2 ) {
             listenerList.remove(listeners[i].getClass(), (EventListener)listeners[i+1]);
         } // end of for (int i=0; i<; i++)
     }
 
     /**
      * Exit the current session and redirect to other URL.
      *
      * This removes the session and its associated
      * application from memory. The browser is redirected to the given
      * URL. Note, that it is not even possible for the user to re-enter 
      * the application with the BACK-button, since all information is 
      * removed. 
      *
      * <em>Always</em> exit an application by calling an 
      * <code>exit()</code> method, especially, if it is an application 
      * that requires a login and thus handles sensitive information accessible
      * through the session. Usually, you will call this on behalf of an 
      * event within an <code>ActionListener.actionPerformed()</code> like for 
      * a pressed 'EXIT'-Button.
      *
      * @param redirectAddress the address, the browser is redirected after
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
      *
      * This removes the session and its associated
      * application from memory. The browser is redirected to the same
      * application with a fresh session. Note, that it is not even
      * possible for the user to re-enter the old application with the 
      * BACK-button, since all information is removed. 
      * 
      * <em>Always</em> exit an application by calling an 
      * <code>exit()</code> method, especially, if it is an application 
      * that requires an login and thus handles sensitive information accessible
      * through the session. Usually, you will call this on behalf of an 
      * event within an <code>ActionListener.actionPerformed()</code> like for 
      * a pressed 'EXIT'-Button.
      */
     public void exit() { exit(""); }
 
     String getExitAddress() {
         return exitAddress;
     }
 
     String getRedirectAddress() {
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
 
     /**
      * TODO: documentation
      *
      * @param listener
      */
     public void addRequestListener(SRequestListener listener) {
         listenerList.add(SRequestListener.class,
                          new WeakRequestListenerProxy(listener));
     }
 
     /**
      * TODO: documentation
      *
      * @param listener
      */
     public void removeRequestListener(SRequestListener listener) {
         listenerList.remove(SRequestListener.class,
                             new WeakRequestListenerProxy(listener));
     }
 
     /**
      * Fire an RequestEvent at each registered listener.
      */
     final void fireRequestEvent(int type) {
         fireRequestEvent(type, null);
     }
 
     /**
      * Fire an RequestEvent at each registered listener.
      */
     final void fireRequestEvent(int type, ExternalizedResource resource) {
         SRequestEvent event = null;
         
         Object[] listeners = listenerList.getListenerList();
         for ( int i = listeners.length-2; i>=0; i -= 2 ) {
             if ( listeners[i]==SRequestListener.class ) {
                 // Lazily create the event:
                 if ( event==null ) {
                     event = new SRequestEvent(this, type, resource);
                 }
                 ((SRequestListener)listeners[i+1]).processRequest(event);
             }
         }
     }
 
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
