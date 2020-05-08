 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 			
 package com.adito.boot;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.prefs.Preferences;
 
 import javax.net.ssl.TrustManager;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Whilst Adito is largely a standard web application, it has
  * requirements of its environment above and beyond this.
  * <p>
  * Then environment that services these requires is known as the Context. There
  * should be a single instance of the implementation of this interface and it
  * should be registered with
  * {@link com.adito.boot.ContextHolder#setContext(Context)}.
  * <p>
  * The instance of the context may then be accessed in the web application and
  * boot classes using {@link com.adito.boot.ContextHolder#getContext()}.
  * <p>
  * The context responsibilities include :-
  * <ul>
  * <li>Service control. I.e. shutdown and restart</li>
  * <li>Provide locations to store configuration, temporary files, logs etc</li>
  * <li>Provide an HTTP server to serve web content and handle custom HTTP
  * connections</li>
  * <li>Provide and manager databases used for storing configuration and
  * Adito resources</li>
  * <li>Obfuscating / deobfuscating passwords</li>
  * <li>Configuring the class loader</li>
  * <li>Providing storewhere to store <i>Context properties</i>, i.e. those
  * that are only applicable to this Context implementation</li>
  * </ul>
  * 
  */
 public interface Context {
 
     enum HandlerProtocol {
         HTTPS_PROTOCOL, HTTP_PROTOCOL, BOTH_PROTOCOLS
     };
 
     /**
      * Get if the context is currently running in setup mode.
      * 
      * @return running in setup mode.
      */
     public boolean isSetupMode();
 
     /**
      * Get if the context may be restarted.
      * 
      * @return restart available
      */
     public boolean isRestartAvailableMode();
 
     /**
      * Shut down the context, possibly restarting when done.
      * 
      * @param restart restart when shutdown complete
      */
     public void shutdown(boolean restart);
 
     /**
      * Get the current version of Adito.
      * 
      * @return current version
      */
     public VersionInfo.Version getVersion();
 
     /**
      * Get the directory when configuration files are stored.
      * 
      * @return configuration file directory
      */
     public File getConfDirectory();
 
     /**
      * Get the directory where tempory files are stored
      * 
      * @return temporary directory
      */
     public File getTempDirectory();
 
     /**
      * Get the directory where logs are stored
      * 
      * @return logs
      */
     public File getLogDirectory();
 
     /**
      * Get the directory where database files are stored.
      * 
      * @return database files
      */
     public File getDBDirectory();
 
     /**
      * Get the directory where application extensions are stored.
      * 
      * @return application extension directory
      */
     public File getApplicationDirectory();
 
     /**
      * Get the main thread.
      * 
      * @return main thread
      */
     public Thread getMainThread();
 
     /**
      * Add a new location that contains web resources. Web resources include
      * things suchs as HTML files, Images, CSS, JSP etc.
      * <p>
      * When serving content, the containing web server must search any added
      * resource bases for the requested resource starting with the first added
      * resource base until something is found.
      * <p>
      * This is a key part of the extension architecture
      * 
      * @param url url to add
      */
     public void addResourceBase(URL url);
 
     /**
      * Remove a location from the list of locations that contains web resources.
      * 
      * @param url url to remove
      * @see Context#addResourceBase(URL)
      */
     public void removeResourceBase(URL url);
 
     /**
      * Get the list of current resource bases
      * 
      * @return resources bases
      */
     public Collection<URL> getResourceBases();
 
     /**
      * Return the host name on which Adito is running.
      * 
      * @return hostname
      */
     public String getHostname();
 
     /**
      * Return the actual port Adito is running on. This may be different
      * to the port in the configuration if the server was invoked with the port
      * argument.
      * 
      * @return port server is running on
      */
     public int getPort();
 
     /**
      * Add a location to the class loader. Any classes found at this location
      * should then made available to all plugins, the core and any JSP pages.
      * 
      * @param u location of classes
      */
     public void addContextLoaderURL(URL u);
 
     /**
      * Add a custom request handler.
      * 
      * @param requestHandler request handler
      */
     public void registerRequestHandler(RequestHandler requestHandler);
 
     /**
      * Add a custom request handler.
      * 
      * @param requestHandler request handler
      */
     public void registerRequestHandler(RequestHandler requestHandler, HandlerProtocol protocol);
 
     /**
      * Remove a custom request handler
      * 
      * @param requestHandler request handler
      */
     public void deregisterRequestHandler(RequestHandler requestHandler);
 
     /**
      * Obfuscate a password in a way that it be de-obfuscated with
      * {@link #deobfuscatePassword(String)}.
      * 
      * @param password password to obfuscate
      * @return de-obfuscated password
      */
     public String obfuscatePassword(String password);
 
     /**
      * De-ebfuscate a password that has been obfuscated with
      * {@link #obfuscatePassword(String)}.
      * 
      * @param password obfuscated password
      * @return deobfuscated password
      */
     public String deobfuscatePassword(String password);
 
     /**
      * Set the trust manager to use for incoming SSL connections.
      * 
      * @param trustManager trust manager to set
      * @param require required
      */
    public void setTrustManager(TrustManager trustManager, boolean require);
 
     /**
      * Add a new web application
      * 
      * @param contextPathSpec path (either / or /path/*)
      * @param webApp path to webapp or WAR file
      * @throws IOException on any error
      * @throws Exception
      */
     public void addWebApp(String contextPathSpec, String webApp) throws Exception;
 
     /**
      * Add a listener to those being notified of events from the <i>Context</i>.
      * 
      * @param contextListener listener to add
      */
     public void addContextListener(ContextListener contextListener);
 
     /**
      * remove a listener from those being notified of events from the <i>Context</i>.
      * 
      * @param contextListener listener to remove
      */
     public void removeContextListener(ContextListener contextListener);
 
     /**
      * Get the root preferences node. Any component of Adito core or its
      * plugins may use this to store configuration information.
      * 
      * @return root preferences node
      */
     public Preferences getPreferences();
 
     /**
      * Get the property class used for system configuration
      * 
      * @return system config property class
      */
     public PropertyClass getConfig();
 
     /**
      * Get the list of URLs that the context loader uses as its class path
      * 
      * @return context loader class path
      */
     public URL[] getContextLoaderClassPath();
 
     /**
      * Get the context class loadeer
      * 
      * @return context class loader
      */
     public ClassLoader getContextLoader();
 
     /**
      * Create an alias for the given URI to the given location.
      * 
      * @param uri uri to alias
      * @param location actual location
      */
     public void setResourceAlias(String uri, String location);
 
     /**
      * Remove an alias for the given URI.
      * 
      * @param uri uri to alias
      */
     public void removeResourceAlias(String uri);
 
     /**
      * Get the boot progress monitor. Calling methods on the monitor will have
      * no effect after the server has started
      * 
      * @return boot progress monitor
      */
     public BootProgressMonitor getBootProgressMonitor();
 
     /**
      * Create a HttpServletRequest from a RequestHandlerRequest. This only works
      * for RequestAdapter implementations as others are already
      * HttpServletRequests.
      * 
      * @param request
      * @return
      */
     public HttpServletRequest createServletRequest(RequestHandlerRequest request);
 
     /**
      * Create a HttpServletResponse from a RequestHandlerResponse. This only
      * works for ResponseAdapter implementations as others are already
      * HttpServletResponse.
      * 
      * @param response
      * @param request
      * @return
      */
     public HttpServletResponse createServletResponse(RequestHandlerResponse response, HttpServletRequest request);
 
     /**
      * Update a sessions last accessed time.
      * 
      * @param session session
      */
     public void access(HttpSession session);
}
