 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.client.login;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBException;
 import org.jdesktop.wonderland.client.ClientPlugin;
 import org.jdesktop.wonderland.client.comms.LoginFailureException;
 import org.jdesktop.wonderland.client.comms.LoginParameters;
 import org.jdesktop.wonderland.client.comms.SessionStatusListener;
 import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;
 import org.jdesktop.wonderland.client.comms.WonderlandSessionImpl;
 import org.jdesktop.wonderland.common.modules.ModulePluginList;
 import org.jdesktop.wonderland.client.modules.ModuleUtils;
 import org.jdesktop.wonderland.common.JarURI;
 import sun.misc.Service;
 
 /**
  * Manager for all the sessions for a particular server
  *
  * @author jkaplan
  */
 public class ServerSessionManager {
     private static final Logger logger =
             Logger.getLogger(ServerSessionManager.class.getName());
 
     /** where on the server to find the details object */
     private static final String DETAILS_URL =
             "wonderland-web-front/resources/ServerDetails";
 
 
     /** the default object to use when creating sessions */
     private static SessionCreator<?> defaultSessionCreator =
             new DefaultSessionCreator();
 
     /** the server this manager represents */
     private String serverURL;
 
     /** details about the server (read from server in constructor) */
     private ServerDetails details;
 
     /** whether or not we are authenticated to the server */
     private LoginControl loginControl;
 
     /** the session for this login */
     private final Set<WonderlandSession> sessions = new HashSet<WonderlandSession>();
 
     /** the primary session */
     private WonderlandSession primarySession;
     private final Object primarySessionLock = new Object();
 
     /** session lifecycle listeners */
     private Set<SessionLifecycleListener> lifecycleListeners =
             new CopyOnWriteArraySet<SessionLifecycleListener>();
 
     /**
      * Constructor is private, use getInstance() instead.
      * @param serverURL the url to connect to
      * @throws IOException if there is an error connecting to the server
      */
     ServerSessionManager(String serverURL) throws IOException {
         // load the server details
         try {
             URL detailsURL = new URL(new URL(serverURL), DETAILS_URL);
             //this.details = ServerDetails.decode(new InputStreamReader(detailsURL.openStream()));
 
 	    URLConnection detailsURLConn = detailsURL.openConnection();
 	    detailsURLConn.setRequestProperty("Accept", "application/xml");
 
 	    this.details = ServerDetails.decode(new InputStreamReader(detailsURLConn.getInputStream()));
         } catch (JAXBException jbe) {
             IOException ioe = new IOException("Error reading server details " +
                                               "from: " + serverURL);
             ioe.initCause(jbe);
             throw ioe;
         }
 
         // set the server URL to the canonical URL sent by the server
         this.serverURL = details.getServerURL();
     }
 
     /**
      * Get the server URL this server session manager represents.  This is the
      * canonical URL returned by the server that was originally requested,
      * not necessarily the original URL that was passed in
      * @return the canonical server URL
      */
     public String getServerURL() {
         return serverURL;
     }
 
     /**
      * Get the server URL as a string: <server name>:<port>
      * @return <server name>:<port>
      */
     public String getServerNameAndPort() {
         try {
             URL tmpURL = new URL(serverURL);
             String server = tmpURL.getHost();
             if (tmpURL.getPort() != -1) {
                 server = server + ":" + tmpURL.getPort();
             }
             return server;
         } catch (MalformedURLException ex) {
             Logger.getLogger(ServerSessionManager.class.getName()).log(Level.WARNING, null, ex);
             return null;
         }
     }
     
     /**
      * Get the details for this server
      * @return the details for this server
      */
     public ServerDetails getDetails() {
         return details;
     }
 
     /**
      * Create a new WonderlandSession using the default session creator
      * @return the newly created session
      * @throws LoginFailureException if there is a problem creating the
      * session with the login credentials from this manager
      */
     public WonderlandSession createSession()
         throws LoginFailureException
     {
         return createSession(defaultSessionCreator);
     }
 
     /**
      * Create a new WonderlandSession using a custom session creator
      * @param creator the SessionCreator to use when creating the session
      * @return the newly created session
      * @throws LoginFailureException if there is a problem creating the
      * session with the login credentials from this manager
      */
     public synchronized <T extends WonderlandSession> T
             createSession(SessionCreator<T> creator)
         throws LoginFailureException
     {
         AuthenticationInfo authInfo = getDetails().getAuthInfo();
 
         // create the login control if necessary
         if (loginControl == null) {
             loginControl = createLoginControl(authInfo);
         }
 
         // see if we are already logged in
         if (!loginControl.isAuthenticated()) {
             requestLogin(loginControl);
         }
 
         // choose a Darkstar server to connect to
         DarkstarServer ds = getDetails().getDarkstarServers()[0];
         WonderlandServerInfo serverInfo =
                 new WonderlandServerInfo(ds.getHostname(), ds.getPort());
 
         // use the session creator to create a new session
         T session = creator.createSession(serverInfo,
                                           loginControl.getClassLoader());
 
         // Register this session with LoginManager
         LoginManager.addSession(session, this);
 
         // log in to the session
         session.login(loginControl.getLoginParameters());
 
         // the session was created successfully.  Add it to our list of
         // sessions, and add a listener to remove it when it disconnects
         session.addSessionStatusListener(new SessionStatusListener() {
             public void sessionStatusChanged(WonderlandSession session,
                                              Status status)
             {
                 if (status.equals(Status.DISCONNECTED)) {
                     sessions.remove(session);
                 }
             }
 
         });
         sessions.add(session);
         fireSessionCreated(session);
 
         // returnh the session
         return session;
     }
 
     /**
      * Get all sessions
      * @return a list of all sessions
      */
     public synchronized Collection<WonderlandSession> getAllSessions() {
         return new ArrayList(sessions);
     }
 
     /**
      * Get all sessions that implement the given type
      * @param clazz the class of session to get
      */
     public <T extends WonderlandSession> Collection<T>
             getAllSession(Class<T> clazz)
     {
         Collection<T> out = new ArrayList<T>();
         synchronized (sessions) {
             for (WonderlandSession session : sessions) {
                 if (clazz.isAssignableFrom(session.getClass())) {
                     out.add((T) session);
                 }
             }
         }
 
         return out;
     }
 
     /**
      * Get the primary session
      * @return the primary session
      */
     public WonderlandSession getPrimarySession() {
         // use a separate lock for the primary session because other threads
         // may need access to the primary session during login, for example
         // during a call to initialize a client plugin
         synchronized (primarySessionLock) {
             return primarySession;
         }
     }
 
     /**
      * Set the primary session
      * @param primary the primary session
      */
     public void setPrimarySession(WonderlandSession primarySession) {
         synchronized (primarySessionLock) {
             this.primarySession = primarySession;
         }
 
         firePrimarySession(primarySession);
     }
 
     /**
      * Add a lifecycle listener.  This will receive messages for all
      * clients that are created or change status
      * @param listener the listener to add
      */
     public void addLifecycleListener(SessionLifecycleListener listener) {
         lifecycleListeners.add(listener);
     }
 
     /**
      * Remove a lifecycle listener.
      * @param listener the listener to remove
      */
     public void removeLifecycleListener(SessionLifecycleListener listener) {
         lifecycleListeners.remove(listener);
     }
 
     /**
      * Create a new LoginControl of the appropriate type
      * @param authInfo the authentication info
      * @return a new LoginControl for the given type
      */
     protected LoginControl createLoginControl(AuthenticationInfo info) {
          switch (info.getType()) {
             case NONE:
                 return new NoAuthLoginControl();
             case WEB_SERVICE:
                 return new UserPasswordLoginControl();
             case WEB:
                 return new WebURLLoginControl(info.getAuthURL());
             default:
                 throw new IllegalStateException("Unknown login type " +
                                                 info.getType());
         }
     }
 
     /**
      * Request login from the given login control object
      * @param loginControl the login control object to get login info from
      * throws LoginFailureException if the login fails or is cancelled
      */
     protected void requestLogin(LoginControl control)
         throws LoginFailureException
     {
         // see if we already have a login in progress
         if (!control.isAuthenticating()) {
             control.requestLogin(LoginManager.getLoginUI());
         }
 
         // wait for the login to complete
         try {
             boolean result = control.waitForLogin();
             if (!result) {
                 throw new LoginFailureException("Login cancelled");
             }
         } catch (InterruptedException ie) {
             throw new LoginFailureException(ie);
         }
     }
 
     /**
      * Notify any registered lifecycle listeners that a new session was created
      * @param session the client that was created
      */
     private void fireSessionCreated(WonderlandSession session) {
         for (SessionLifecycleListener listener : lifecycleListeners) {
             listener.sessionCreated(session);
         }
     }
 
     /**
      * Notify any registered lifecycle listeners that a session was declared
      * the primary session
      * @param session the client that was declared primary
      */
     private void firePrimarySession(WonderlandSession session) {
         for (SessionLifecycleListener listener : lifecycleListeners) {
             listener.primarySession(session);
         }
     }
 
     /**
      * Set up the classloader with module jar URLs for this server
      * @param serverURL the URL of the server to connect to
      * @return the classloader setup with this server's URLs
      */
     private ClassLoader setupClassLoader(String serverURL) {
         // TODO: use the serverURL
         ModulePluginList list = ModuleUtils.fetchPluginJars(serverURL);
         List<URL> urls = new ArrayList<URL>();
         if (list==null) {
             logger.warning("Unable to configure classlaoder, falling back to " +
                            "system classloader");
             return getClass().getClassLoader();
         }
 
         for (JarURI uri : list.getJarURIs()) {
             try {
                 URL url = uri.toURL();
                 
                 // check the filter to see if we should add this URL
                 if (LoginManager.getPluginFilter().shouldDownload(this, url)) {
                     urls.add(url);
                 }
             } catch (Exception excp) {
                 excp.printStackTrace();
            }
         }
 
         return new URLClassLoader(urls.toArray(new URL[0]),
                                   getClass().getClassLoader());
     }
 
     /**
      * Initialize plugins
      */
     private void initPlugins(ClassLoader loader) {
         // At this point, we have successfully logged in to the server,
         // and the session should be connected.
         Iterator<ClientPlugin> it = Service.providers(ClientPlugin.class,
                                                       loader);
         while (it.hasNext()) {
             ClientPlugin plugin = it.next();
 
             // check with the filter to see if we should load this plugin
             if (LoginManager.getPluginFilter().shouldInitialize(this, plugin)) {
                 try {
                     plugin.initialize(this);
                 } catch(Exception e) {
                     logger.log(Level.WARNING, "Error initializing plugin " +
                                plugin.getClass().getName(), e);
                 } catch(Error e) {
                     logger.log(Level.WARNING, "Error initializing plugin " +
                                plugin.getClass().getName(), e);
                 }
             }
         }
     }
 
     public abstract class LoginControl {
         private boolean started = false;
         private boolean finished = false;
         private boolean success = false;
 
         private LoginParameters params;
         private ClassLoader classLoader;
 
         /**
          * Get the server URL for this login control object
          * @return the server URL to connect to
          */
         public String getServerURL() {
             return ServerSessionManager.this.getServerURL();
         }
 
         /**
          * Determine if login is complete and successful.
          * @return true of the login is complete and successful, false
          * if the login is in progress or failed.
          */
         public synchronized boolean isAuthenticated() {
             return finished && success;
         }
 
         /**
          * Determine if login is in progress.  This will return true
          * if a login has been requested from the client, but they
          * have not yet responded.
          * @return true if a login is in progress, or false if not
          */
         public synchronized boolean isAuthenticating() {
             return started && !finished;
         }
 
         /**
          * Request a login from the given login UI
          */
         public void requestLogin(LoginUI ui) {
             synchronized (this) {
                 started = true;
             }
         }
 
         /**
          * Get the classloader to use when connecting to the Darkstar server.
          * This method is only valid when isAuthenticated() returns true.
          * @return the classloader to use
          */
         public synchronized ClassLoader getClassLoader() {
             if (!isAuthenticated()) {
                 throw new IllegalStateException("Not authenticated");
             }
 
             return classLoader;
         }
 
         /**
          * Get the LoginParameters to use when connecting to the Darkstar
          * server.
          * This method is only valid when isAuthenticated() returns true.
          * @return the LoginParameters to use
          */
         public synchronized LoginParameters getLoginParameters() {
             if (!isAuthenticated()) {
                 throw new IllegalStateException("Not authenticated");
             }
 
             return params;
         }
 
         /**
          * Indicate that the login attempt was successful, and pass in
          * the LoginParameters that should be sent to the Darkstar server
          * to create a session.
          * <p>
          * This method indicates that login has been successful, so
          * sets up the plugin classloader for use in session creation. Once
          * the classloader is setup, it notifies any listeners that login
          * is complete.
          *
          * @param loginParams the parameters to login with. A null
          * LoginParameters object indicates that the login attempt has failed.
          */
         protected synchronized void loginComplete(LoginParameters params) {
             this.params = params;
             if (params != null) {
                 // setup the classloader
                 this.classLoader = setupClassLoader(getServerURL());
 
                 // initialize plugins
                 initPlugins(classLoader);
 
                 // if we get here, the login has succeeded
                 this.success = true;
             }
 
             this.started = false;
             this.finished = true;
             notify();
         }
 
         /**
          * Cancel the login in progress
          */
         public synchronized void cancel() {
             loginComplete(null);
         }
 
         /**
          * Wait for the current login in progress to end
          * @return true if the login is successful, or false if not
          * @throws InterruptedException if the thread is interrupted before
          * the login parameters are determined
          */
         protected synchronized boolean waitForLogin()
             throws InterruptedException
         {
             while (isAuthenticating()) {
                 wait();
             }
 
             return isAuthenticated();
         }
     }
 
     public class NoAuthLoginControl extends LoginControl {
         @Override
         public void requestLogin(LoginUI ui) {
             super.requestLogin(ui);
             ui.requestLogin(this);
         }
 
         public void authenticate(String username, String fullname)
             throws LoginFailureException
         {
             // no other authentication to do, just do the Darkstar login
             // with all the data packed into the username
             String packed = formatUsername(username, fullname);
             loginComplete(new LoginParameters(packed, new char[0]));
         }
         
         /**
          * Combine values into a single username argument that can be
          * passed to the Darkstar server to populate our identity.
          */
         protected String formatUsername(String username, String fullname) {
             StringBuffer sb = new StringBuffer();
             sb.append("un=" + username + ";");
             sb.append("fn=" + fullname + ";");
             return sb.toString();
         }
     }
 
     public class UserPasswordLoginControl extends LoginControl {
         @Override
         public void requestLogin(LoginUI ui) {
             super.requestLogin(ui);
             ui.requestLogin(this);
         }
 
         public boolean authenticate(String username, char[] password) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
     }
 
     public class WebURLLoginControl extends LoginControl {
         private String url;
 
         public WebURLLoginControl(String url) {
             this.url = url;
         }
 
         public String getURL() {
             return url;
         }
 
         @Override
         public void requestLogin(LoginUI ui) {
             super.requestLogin(ui);
             ui.requestLogin(this);
         }
     }
 
     public static class DefaultSessionCreator
             implements SessionCreator<WonderlandSession>
     {
         public WonderlandSession createSession(WonderlandServerInfo serverInfo,
                                                ClassLoader loader)
         {
             return new WonderlandSessionImpl(serverInfo, loader);
         }
     }
 }
