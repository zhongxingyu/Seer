 package org.paxle.gui.impl;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.service.http.HttpContext;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.http.NamespaceException;
 import org.paxle.gui.IServletManager;
 
 public class ServletManager implements IServletManager {
 	
 	/**
 	 * All registeres servlets
 	 */
 	private HashMap<String, Servlet> servlets = new HashMap<String, Servlet>();
 	
 	/**
 	 * All registered resources
 	 */
 	private HashMap<String, String> resources = new HashMap<String, String>();
 	
 	/**
 	 * Http Service
 	 */
 	private HttpService http = null;
 	
 	private MenuManager menuManager = null;
 	
 	/**
 	 * Default Servlet properties
 	 */
 	private Hashtable<String, String> defaultProps = null;
 	
 	private HttpContext defaultContext = null;
 	
 	private Log logger = null;
 	
 	public ServletManager(MenuManager menuManager, String bundleLocation) {
 		this.menuManager = menuManager;
 		
 		// add some properties to the servlet props
 		this.defaultProps = new Hashtable<String, String>();
 		this.defaultProps.put("org.apache.velocity.properties", "/resources/config/velocity.properties");
 		this.defaultProps.put("org.apache.velocity.toolbox", "/resources/config/velocity.toolbox");
 //		this.defaultProps.put("bundle.location",context.getBundle().getLocation());
 		this.defaultProps.put("bundle.location",bundleLocation);
 		
 		this.logger = LogFactory.getLog(this.getClass());
 	}
 	
 	synchronized void addServlet(String alias, Servlet servlet) {
 		this.servlets.put(alias, servlet);
 		if (this.http != null) {
 			try {
 				/* Configure classloader properly.
 				 * 
 				 * This is very important for velocity to load the toolboxes and
 				 * GUI configuration files.
 				 */
 				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());						
 				
 				this.logger.info(String.format("Registering servlet '%s' for alias '%s'.", servlet.getClass().getName(), alias));
 				this.http.registerServlet(alias, servlet, defaultProps, defaultContext);
 			} catch (Throwable e) {
 				this.logger.error(String.format("Unexpected '%s' while registering servlet '%s' for alias '%s'.",
 						e.getClass().getName(),
 						servlet.getClass().getName(),
 						alias
 				),e);
 			}
 		}
 	}
 	
 	synchronized void removeServlet(String alias) {
 		this.servlets.remove(alias);
 		if (this.http != null) {
 			this.http.unregister(alias);
 		}
 	}
 	
 	synchronized void addResources(String alias, String name) {
		this.addResources(alias, name, this.defaultContext);
	}
	
	synchronized void addResources(String alias, String name, HttpContext httpContext) {
 		this.resources.put(alias, name);
 		if (this.http != null) {
 			try {
 				this.logger.info(String.format("Registering resource '%s' for alias '%s'.", name, alias));
				this.http.registerResources(alias, name, httpContext);
 			} catch (Throwable e) {
 				this.logger.error(String.format("Unexpected '%s' while registering resource '%s' for alias '%s'.",
 						e.getClass().getName(),
 						name,
 						alias
 				),e);
 			}
 		}
 	}
 	
 	synchronized void removeResource(String alias) {
 		this.resources.remove(alias);
 		if (this.http != null) {
 			this.http.unregister(alias);
 		}
 	}
 	
 	synchronized void setHttpService(HttpService httpService) {		
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
 	 * Function to register all known servlets
 	 */
 	private void registerAllServlets() {
 		if (this.http == null) return;
 		
 		/* Configure classloader properly.
 		 * 
 		 * This is very important for velocity to load the toolboxes and
 		 * GUI configuration files.
 		 */
 		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());		
 		
 		// register all servlets
 		Iterator<Map.Entry<String, Servlet>> i = this.servlets.entrySet().iterator();
 		while (i.hasNext()) {
 			Map.Entry<String, Servlet> entry = i.next();
 			try {
 				this.http.registerServlet(entry.getKey(), entry.getValue(), defaultProps, defaultContext);
 			} catch (ServletException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NamespaceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}	
 	}
 	
 	/**
 	 * Function to register all known resources
 	 */
 	private void registerAllResources() {
 		if (this.http == null) return;
 		
 		// register all servlets
 		Iterator<Map.Entry<String, String>> i = this.resources.entrySet().iterator();
 		while (i.hasNext()) {
 			Map.Entry<String, String> entry = i.next();
 			try {
 				this.http.registerResources(entry.getKey(), entry.getValue(), defaultContext);
 			} catch (NamespaceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
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
 		
 		Iterator<Map.Entry<String, Servlet>> i = this.servlets.entrySet().iterator();
 		while (i.hasNext()) {				
 			Map.Entry<String, Servlet> entry = i.next();
 			this.http.unregister(entry.getKey());
 		}
 	}	
 	
 	/**
 	 * Function to unregister all resources
 	 */
 	private synchronized void unregisterAllResources() {
 		if (this.http == null) return;
 		
 		Iterator<Map.Entry<String, String>> i = this.resources.entrySet().iterator();
 		while (i.hasNext()) {				
 			Map.Entry<String, String> entry = i.next();
 			this.http.unregister(entry.getKey());
 		}		
 	}
 	
 	/**
 	 * Unregisters all known servlets and resources
 	 */
 	public void close() {
 		this.unregisterAll();
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
 }
