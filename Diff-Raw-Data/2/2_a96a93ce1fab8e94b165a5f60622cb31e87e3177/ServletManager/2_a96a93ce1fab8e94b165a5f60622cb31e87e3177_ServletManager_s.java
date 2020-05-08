 package org.paxle.gui.impl;
 
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.servlet.Servlet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.tools.view.VelocityView;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.http.HttpContext;
 import org.osgi.service.http.HttpService;
 import org.paxle.gui.ALayoutServlet;
 import org.paxle.gui.IServletManager;
 
 public class ServletManager implements IServletManager, ManagedService {
 	public static final String PID = IServletManager.class.getName();
 	
 	/* ================================================================
 	 * Properties configurable via CM
 	 * ================================================================ */
 	private static final String CM_PROPERTY_PATH = PID + ".pathPrefix";
 	
 	/**
 	 * All registeres {@link Servlet servlets}
 	 */
 	private HashMap<String, Servlet> servlets = new HashMap<String, Servlet>();
 	
 	/**
 	 * All registered resources
 	 */
 	private HashMap<String, String> resources = new HashMap<String, String>();
 	
 	/**
 	 * The {@link HttpContext} that should be used to register a given 
 	 * {@link #servlets servlet} or {@link #resources resource}.
 	 */
 	private HashMap<String, HttpContext> httpContexts = new HashMap<String, HttpContext>();
 	
 	/**
 	 * The OSGI {@link HttpService Http-Service}
 	 */
 	private HttpService http = null;
 	
 	/**
 	 * Default Servlet properties
 	 */
 	private final Hashtable<String, String> defaultProps;
 	
 	/**
 	 * The default context that is used to register servlets and resources.
 	 * Leaving this to <code>null</code> means that the {@link HttpService}
 	 * creates a new context for each resource/servlet
 	 */
 	private HttpContext defaultContext = null;
 	
 	/**
 	 * For logging
 	 */
 	private final Log logger;
 	
 	/**
 	 * A prefix that should be used for each servlet- and resource-alias
 	 */
 	private String pathPrefix = "";
 	
 	public ServletManager(String defaultBundleLocation) {
 		if (defaultBundleLocation != null && defaultBundleLocation.endsWith("/")) {
 			defaultBundleLocation = defaultBundleLocation.substring(0,defaultBundleLocation.length()-1);
 		}
 		
 		// add some properties to the servlet props
 		this.defaultProps = new Hashtable<String, String>();
 		
 		this.defaultProps.put(VelocityView.PROPERTIES_KEY, "/resources/config/velocity.properties");
		this.defaultProps.put(VelocityView.TOOLS_KEY, "/resources/config/velocity-toolbox.xml");
 		this.defaultProps.put(VelocityView.LOAD_DEFAULTS_KEY,"false");
 		
 		this.defaultProps.put("bundle.location.default",defaultBundleLocation);		
 		this.logger = LogFactory.getLog(this.getClass());
 	}
 	
 	public String getFullAlias(String alias) {
 		return this.getFullAlias(this.pathPrefix,alias);
 	}
 	
 	public String getFullAlias(String path, String alias) {
 		if (path == null) path = "";
 		if (path.length() > 1 && !path.startsWith("/")) path = "/" + path;
 		if (path.endsWith("/")) path = path.substring(0, path.length() - 1);					
 		
 		String fullAlias = path + alias;
 		if (fullAlias.length() > 1 && fullAlias.endsWith("/")) {
 			fullAlias = fullAlias.substring(0,fullAlias.length()-1);
 		}
 		return fullAlias;
 	}
 	
 	public void addServlet( String alias, Servlet servlet) {
 		this.addServlet(alias, servlet, this.defaultContext);	
 	}
 	
 	public void addServlet(String alias, Servlet servlet, HttpContext httpContext) {
 		this.addServlet(alias, servlet, httpContext, false);
 		
 	}
 	
 	private synchronized void addServlet(String alias, Servlet servlet, HttpContext httpContext, boolean intern) {
 		if (!intern) {
 			this.servlets.put(alias, servlet);
 			this.httpContexts.put(alias, httpContext);
 		}
 		
 		if (this.http != null) {
 			try {
 				/* Configure classloader properly.
 				 * 
 				 * This is very important for velocity to load the toolboxes and
 				 * GUI configuration files.
 				 */
 //				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());						
 				
 				final String fullAlias = this.getFullAlias(alias);
 				this.logger.info(String.format(
 						"Registering servlet '%s' for alias '%s'.", 
 						servlet.getClass().getName(), 
 						fullAlias
 				));
 				Hashtable<String, String> props = (Hashtable<String, String>) this.defaultProps.clone();
 				if (servlet instanceof ALayoutServlet && ((ALayoutServlet)servlet).getBundleLocation() != null) {
 					props.put("bundle.location", ((ALayoutServlet)servlet).getBundleLocation());
 				}
 				this.http.registerServlet(fullAlias, servlet, props, httpContext);
 			} catch (Throwable e) {
 				this.logger.error(String.format("Unexpected '%s' while registering servlet '%s' for alias '%s'.",
 						e.getClass().getName(),
 						servlet.getClass().getName(),
 						alias
 				),e);
 			}
 		}
 	}
 	
 	public void removeServlet(String alias) {	
 		this.removeServlet(alias, false);
 	}
 	
 	private synchronized void removeServlet(String alias, boolean intern) {
 		final Servlet servlet = this.servlets.get(alias);
 		if (!intern) {
 			this.servlets.remove(alias);		
 			this.httpContexts.remove(alias);
 		}
 		
 		final String fullAlias = this.getFullAlias(alias);
 		this.logger.info(String.format(
 				"Unregistering servlet '%s' for alias '%s'.", 
 				servlet.getClass().getName(), 
 				fullAlias
 		));
 		
 		if (this.http != null) {
 			try {
 				this.http.unregister(fullAlias);
 			} catch (Throwable e) {
 				this.logger.error(String.format("Unexpected '%s' while unregistering servlet '%s' for alias '%s'.",
 						e.getClass().getName(),
 						servlet.getClass().getName(),
 						alias
 				),e);
 			}
 		}
 	}
 	
 	public void addResources(String alias, String name) {
 		this.addResources(alias, name, this.defaultContext);
 	}
 	
 	public void addResources(String alias, String name, HttpContext httpContext) {
 		this.addResources(alias, name, httpContext, false);
 	}
 	
 	private synchronized void addResources(String alias, String name, HttpContext httpContext, boolean intern) {
 		if (!intern) {
 			this.resources.put(alias, name);
 			this.httpContexts.put(alias, httpContext);
 		}
 		
 		if (this.http != null) {
 			try {
 				String fullAlias = this.getFullAlias(alias);
 				this.logger.info(String.format(
 						"Registering resource '%s' for alias '%s'.", 
 						name, 
 						fullAlias
 				));
 				this.http.registerResources(fullAlias, name, httpContext);
 			} catch (Throwable e) {
 				this.logger.error(String.format(
 						"Unexpected '%s' while registering resource '%s' for alias '%s'.",
 						e.getClass().getName(),
 						name,
 						alias
 				),e);
 			}
 		}
 	}
 	
 	public void removeResource( String alias) {
 		this.removeResource(alias, false);
 	}
 	
 	private synchronized void removeResource(String alias, boolean intern) {
 		final String name = this.resources.get(alias);
 		if (!intern) {
 			this.resources.remove(alias);
 			this.httpContexts.remove(alias);
 		}
 		
 		final String fullAlias = this.getFullAlias(alias);
 		this.logger.info(String.format(
 				"Unregistering resource '%s' for alias '%s'.", 
 				name, 
 				fullAlias
 		));		
 		
 		if (this.http != null) {
 			try {
 				this.http.unregister(fullAlias);
 			} catch (Throwable e) {
 				this.logger.error(String.format(
 						"Unexpected '%s' while unregistering resource '%s' for alias '%s'.",
 						e.getClass().getName(),
 						name,
 						alias
 				),e);
 			}			
 		}
 	}
 	
 	public synchronized void setHttpService( HttpService httpService) {		
 		if (httpService == null) {
 			// don't unregister, otherwise the http-service destroys our servlets!
 			this.unregisterAll();
 			this.http = null;
 		} else {
 			this.http = httpService;
 			this.registerAll();
 		}
 	}
 	
 	/**
 	 * Function to register all known servlets and resources
 	 * @see #registerAllResources()
 	 * @see #registerAllServlets()
 	 */
 	private void registerAll() {
 		this.registerAllResources();
 		this.registerAllServlets();
 	}
 	
 	/**
 	 * Function to register all known servlets. 
 	 * This function loops through {@link #servlets} and calls 
 	 * {@link #addServlet(String, Servlet, HttpContext, boolean)} for each entry.
 	 */
 	private void registerAllServlets() {
 		if (this.http == null) return;
 		
 		/* Configure classloader properly.
 		 * 
 		 * This is very important for velocity to load the toolboxes and
 		 * GUI configuration files.
 		 */
 //		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());		
 		
 		// register all servlets
 		for (Map.Entry<String, Servlet> entry: this.servlets.entrySet()) {
 			String alias = entry.getKey();
 			Servlet servlet =  entry.getValue();
 			
 			HttpContext context = this.httpContexts.containsKey(alias)
 				? this.httpContexts.get(alias)
 				: defaultContext;			
 
 			this.addServlet(alias, servlet, context, true);
 		}	
 	}
 	
 	/**
 	 * Function to register all resources.
 	 * This function loops through {@link #resources} and calls
 	 * {@link #addResources(String, String, HttpContext, boolean)} for each entry.
 	 * 
 	 */
 	private void registerAllResources() {
 		if (this.http == null) return;
 		
 		// register all servlets
 		for (Map.Entry<String, String> entry : this.resources.entrySet()) {
 			String name = entry.getValue();
 			String alias = entry.getKey();
 			
 			HttpContext context = this.httpContexts.containsKey(alias)
 				? this.httpContexts.get(alias)
 				: defaultContext;
 			
 			this.addResources(alias, name, context, true);
 		}			
 	}
 		
 	/**
 	 * Function to unregister all servlets and resources
 	 * @see #unregisterAllServlets()
 	 * @see #unregisterAllResources()
 	 */
 	private void unregisterAll() {
 		this.unregisterAllServlets();
 		this.unregisterAllResources();
 	}	
 	
 	/**
 	 * Function to unregister all servlets
 	 */
 	private synchronized void unregisterAllServlets() {
 		if (this.http == null) return;
 		
 		for (Map.Entry<String, Servlet> entry : this.servlets.entrySet()) {
 			String alias = entry.getKey();
 			this.removeServlet(alias, true);
 		}
 	}	
 	
 	/**
 	 * Function to unregister resources that were added
 	 * to {@link #resources}
 	 */
 	public synchronized void unregisterAllResources() {
 		if (this.http == null) return;
 
 		for (Map.Entry<String, String> entry : this.resources.entrySet()) {
 			String alias = entry.getKey();
 			this.removeResource(alias, true);
 		}		
 	}
 	
 	/**
 	 * Unregisters all known servlets and resources
 	 */
 	public void close() {
 		// unregister everything
 		this.unregisterAll();
 		
 		// clear maps
 		this.resources.clear();
 		this.servlets.clear();
 		this.httpContexts.clear();
 	}
 	
 	/**
 	 * @see IServletManager#getServlets()
 	 */
 	public Map<String, Servlet> getServlets() {
 		return Collections.unmodifiableMap(this.servlets);
 	}
 	
 	/**
 	 * @see IServletManager#getResources()
 	 */
 	public Map<String, String> getResources() {
 		return Collections.unmodifiableMap(this.resources);
 	}
 
 	/**
 	 * @see IServletManager#hasServlet(String)
 	 */
 	public boolean hasServlet(String alias) {
 		return this.servlets.containsKey(alias);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void updated(Dictionary properties) throws ConfigurationException {
 		if (properties == null) return;
 		
 		boolean settingsChanged = false;
 		String newPathPrefix = null;
 		
 		Enumeration<String> keys = properties.keys();
 		while (keys.hasMoreElements()) {
 			String key = keys.nextElement();
 			if (key.equals(CM_PROPERTY_PATH)) {
 				String path = (String) properties.get(key);
 				if (path == null) path = "";
 				if (path.length() > 1 && !path.startsWith("/")) path = "/" + path;
 				if (path.endsWith("/")) path = path.substring(0, path.length() - 1);				
 				
 				if (!this.pathPrefix.equals(path)) {
 					newPathPrefix = path;
 					settingsChanged = true;
 				}
 			}
 		}
 		
 		if (settingsChanged) {
 			try {
 				// allow the config-servlet to finish redirect
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			this.unregisterAll();
 			this.pathPrefix = newPathPrefix;
 			this.registerAll();
 		}
 	}
 }
