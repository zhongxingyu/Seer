 /**
  * Copyright (c) 2012 Committers of lunifera.org.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Based on org.eclipse.gyrex.http.jetty.internal.app.ApplicationContext (Gunnar Wagenknecht)
  * 
  * Contributors:
  *    Florian Pirchner - initial API and implementation
  */
 package org.lunifera.runtime.web.http.internal;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.servlet.Filter;
 import javax.servlet.Servlet;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.equinox.http.servlet.ExtendedHttpService;
 import org.eclipse.jetty.http.PathMap;
 import org.eclipse.jetty.servlet.FilterHolder;
 import org.eclipse.jetty.servlet.FilterMapping;
 import org.eclipse.jetty.servlet.ServletHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.servlet.ServletMapping;
 import org.eclipse.jetty.util.StringUtil;
 import org.eclipse.jetty.util.URIUtil;
 import org.lunifera.runtime.web.http.HttpConstants;
 import org.lunifera.runtime.web.http.IHttpApplication;
 import org.lunifera.runtime.web.http.IResourceProvider;
 import org.lunifera.runtime.web.http.internal.resource.HttpApplicationResourceServlet;
 import org.lunifera.runtime.web.jetty.IHandlerProvider;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceFactory;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.http.NamespaceException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HttpApplication implements IHttpApplication {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(HttpApplication.class);
 
 	// OSGi
 	private BundleContext context;
 	private ServiceRegistration<?> httpServiceRegistration;
 	private ServiceRegistration<IHandlerProvider> jettyHandlerProviderRegistration;
 	private final HttpServiceServiceFactory httpServiceFactory = new HttpServiceServiceFactory();
 
 	// properties
 	private final String id;
 	private String name;
 	private String contextPath = "/";
 	private String jettyServer;
 
 	// lifecycle
 	private boolean started;
 	private final Lock accessLock = new ReentrantLock();
 
 	// servlet artifacts
 	private HttpApplicationServletContextHandler servletContext;
 	private Set<String> registeredAlias = new HashSet<String>();
 	private List<Registration> registeredArtifacts = Collections
 			.synchronizedList(new ArrayList<HttpApplication.Registration>());
 	private final Map<Object, BundleResourceMonitor> bundleMonitors = new HashMap<Object, BundleResourceMonitor>();
 
 	public HttpApplication(String id, BundleContext context) {
 		if (id == null) {
 			throw new NullPointerException("Id must not be null!");
 		}
 		this.id = id;
 		this.context = context;
 	}
 
 	/**
 	 * @return the servletContext
 	 */
 	public HttpApplicationServletContextHandler getServletContext() {
 		return servletContext;
 	}
 
 	@Override
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * Returns the name of that http application.
 	 * 
 	 * @return
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Returns the context path this application will register a
 	 * {@link HttpService} at.
 	 * 
 	 * @return
 	 */
 	public String getContextPath() {
 		return contextPath;
 	}
 
 	@Override
 	public String getJettyServer() {
 		return jettyServer;
 	}
 
 	/**
 	 * Sets the name of that application.
 	 * 
 	 * @param name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Sets the context path of that application.
 	 * 
 	 * @param contextPath
 	 */
 	public void setContextPath(String contextPath) {
 		this.contextPath = contextPath;
 	}
 
 	/**
 	 * Sets the name of the jetty server this web application should be
 	 * installed at.
 	 * 
 	 * @param jettyServer
 	 *            the jettyServer to set
 	 */
 	public void setJettyServer(String jettyServer) {
 		this.jettyServer = jettyServer;
 	}
 
 	/**
 	 * A callback method that allows the application to handle the request too.
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws IOException
 	 * @throws ServletException
 	 */
 	public void handleRequest(final HttpServletRequest request,
 			final HttpServletResponse response) throws IOException,
 			ServletException {
 
 		// check security
 		if (!handleSecurity(request, response)) {
 			return;
 		}
 
 		// let the application context handle the request
 		if (servletContext.getApplicationScopeHandler()
 				.handleHttpApplicationCallback(request, response)) {
 			return;
 		}
 
 		// return 404, no resource registered
 		response.sendError(HttpServletResponse.SC_NOT_FOUND);
 	}
 
 	/**
 	 * Handles the security.
 	 * 
 	 * @param request
 	 * @param response
 	 * @return
 	 */
 	protected boolean handleSecurity(HttpServletRequest request,
 			HttpServletResponse response) {
 		return true;
 	}
 
 	public boolean isStarted() {
 		return started;
 	}
 
 	/**
 	 * Is called to start the application. All resources should be registered
 	 * and the http service is registered.
 	 */
 	public void start() {
 		if (started) {
 			logger.debug("HttpApplication {} is already started", getName());
 			return;
 		}
 
 		// ensure that only one threas manipulates the contents
 		accessLock.lock();
 		try {
 			servletContext = new HttpApplicationServletContextHandler(this);
 
 			// activate current registrations
 			//
 			try {
 				List<Registration> tempRegistrations = new ArrayList<Registration>(
 						registeredArtifacts);
 				registeredArtifacts.clear();
 				for (Registration reg : tempRegistrations) {
 					if (reg.isServlet()) {
 						registerServlet(reg.alias, (Servlet) reg.context,
 								reg.params);
 					} else if (reg.isFilter()) {
 						registerFilter(reg.alias, (Filter) reg.context,
 								reg.params);
 					} else if (reg.isResource()) {
 						registerResource(reg.alias, reg.name,
 								(IResourceProvider) reg.context);
 					}
 				}
 			} catch (ServletException e) {
 				logger.error("{}", e);
 				throw new RuntimeException(e);
 			} catch (NamespaceException e) {
 				logger.error("{}", e);
 				throw new RuntimeException(e);
 			}
 
 			// register jetty handler
 			//
 			if (jettyHandlerProviderRegistration == null) {
 				Hashtable<String, Object> properties = new Hashtable<String, Object>();
 				properties.put(HttpConstants.APPLICATION_ID, getId());
 				properties.put(HttpConstants.APPLICATION_NAME, getName());
 				properties.put(HttpConstants.CONTEXT_PATH, getContextPath());
 				if (getJettyServer() != null) {
 					properties.put(HttpConstants.JETTY_SERVER_NAME,
 							getJettyServer());
 				}
 				jettyHandlerProviderRegistration = context.registerService(
 						IHandlerProvider.class, new JettyHandlerProvider(
 								servletContext), properties);
 			}
 
 			// register http service
 			//
 			if (httpServiceRegistration == null) {
 				Hashtable<String, Object> properties = new Hashtable<String, Object>();
 				properties.put(HttpConstants.APPLICATION_ID, getId());
 				properties.put(HttpConstants.APPLICATION_NAME, getName());
 				properties.put(HttpConstants.CONTEXT_PATH, getContextPath());
 				if (getJettyServer() != null) {
 					properties.put(HttpConstants.JETTY_SERVER_NAME,
 							getJettyServer());
 				}
 				httpServiceRegistration = context.registerService(
 						new String[] { HttpService.class.getName(),
 								ExtendedHttpService.class.getName() },
 						httpServiceFactory, properties);
 			}
 		} finally {
 			accessLock.unlock();
 			started = true;
 		}
 	}
 
 	/**
 	 * Is called to stop the application. All resources should be unregistered
 	 * and the http service will become disposed.
 	 */
 	public void stop() {
 		if (!started) {
 			logger.debug("HttpApplication {} not started", getName());
 			return;
 		}
 
 		// ensure that only one threas manipulates the contents
 		accessLock.lock();
 		try {
 			// reset bundle listener
 			//
 			for (BundleResourceMonitor monitor : bundleMonitors.values()) {
 				monitor.remove();
 			}
 			bundleMonitors.clear();
 
 			// destroy the servlet context
 			//
 			if (servletContext != null) {
 				try {
 					servletContext.stop();
 					servletContext.destroy();
 				} catch (Exception e) {
 					logger.error("{}", e);
 				}
 				servletContext = null;
 			}
 
 			// clear aliases
 			//
 			registeredAlias.clear();
 
 			// unregister http service
 			//
 			if (httpServiceRegistration != null) {
 				httpServiceRegistration.unregister();
 				httpServiceRegistration = null;
 			}
 
 			// unregister jetty handler
 			//
 			if (jettyHandlerProviderRegistration != null) {
 				jettyHandlerProviderRegistration.unregister();
 				jettyHandlerProviderRegistration = null;
 			}
 		} finally {
 			accessLock.unlock();
 			started = false;
 		}
 	}
 
 	/**
 	 * Destroys the http application.
 	 */
 	public void destroy() {
 		try {
 			accessLock.lock();
 			if (isStarted()) {
 				stop();
 			}
 
 			if (registeredArtifacts != null) {
 				registeredArtifacts.clear();
 			}
 		} finally {
 			accessLock.unlock();
 		}
 	}
 
 	/**
 	 * Only for testing issues!!!
 	 * 
 	 * @param context
 	 */
 	protected void setBundleContext(BundleContext context) {
 		this.context = context;
 	}
 
 	/**
 	 * Registers the given servlet by the alias.
 	 * 
 	 * @param alias
 	 * @param servlet
 	 * @param initparams
 	 * @throws ServletException
 	 * @throws NamespaceException
 	 */
 	public void registerServlet(String alias, Servlet servlet,
 			Map<String, String> initparams) throws ServletException,
 			NamespaceException {
 
 		final String pathSpec = normalizeAliasToPathSpec(alias);
 
 		// ensure that only one threas manipulates the contents
 		accessLock.lock();
 		try {
 			registerAlias(alias);
 			registeredArtifacts
 					.add(new Registration(alias, servlet, initparams));
 
 			// track bundle de-activation
 			addBundleResourceMonitor(alias, servlet.getClass());
 
 			ServletHolder holder = new ServletHolder(servlet);
 			if (initparams != null) {
 				holder.setInitParameters(initparams);
 			}
 			servletContext.getServletHandler().addServletWithMapping(holder,
 					pathSpec);
 			// start the servlet if it is necessary
 			initializeServletIfNecessary(alias, holder);
 		} finally {
 			accessLock.unlock();
 		}
 	}
 
 	/**
 	 * Registers the resource provider at the application.
 	 * 
 	 * @param alias
 	 * @param name
 	 * @param provider
 	 * @throws NamespaceException
 	 */
 	public void registerResource(String alias, String name,
 			IResourceProvider provider) throws NamespaceException {
 
 		final String pathSpec = normalizeAliasToPathSpec(alias);
 
 		// ensure that only one threas manipulates the contents
 		accessLock.lock();
 		try {
 			registerAlias(alias);
 			registeredArtifacts.add(new Registration(alias, provider, name));
 
 			// track bundle de-activation
 			addBundleResourceMonitor(alias, provider.getClass());
 
 			servletContext.addResource(pathSpec, provider);
 
 			// register a resource servlet to make the resources accessible
 			final ServletHolder holder = new ServletHolder(
 					new HttpApplicationResourceServlet(servletContext));
 			// based on gyrex settings
 			holder.setInitParameter("dirAllowed", "false");
 			holder.setInitParameter("maxCacheSize", "2000000");
 			holder.setInitParameter("maxCachedFileSize", "254000");
 			holder.setInitParameter("maxCachedFiles", "1000");
 			holder.setInitParameter("useFileMappedBuffer", "true");
 			servletContext.getServletHandler().addServletWithMapping(holder,
 					pathSpec);
 
 			// initialize resource servlet if application already started
 			try {
 				initializeServletIfNecessary(alias, holder);
 			} catch (final ServletException e) {
 				throw new IllegalStateException(String.format(
 						"Unhandled error registering resources. %s",
 						e.getMessage()), e);
 			}
 		} finally {
 			accessLock.unlock();
 		}
 	}
 
 	/**
 	 * Unregisters the resource registered under the given alias.
 	 * 
 	 * @param alias
 	 */
 	public void unregister(String alias) {
 		final String pathSpec = normalizeAliasToPathSpec(alias);
 
 		unregisterAlias(alias);
 		// remove bundle monitor
 		removeBundleResourceMonitor(alias);
 
 		servletContext.removeResource(pathSpec);
 
 		// collect list of new mappings and remaining servlets
 		final ServletHandler servletHandler = servletContext
 				.getServletHandler();
 		boolean removedSomething = false;
 		final ServletMapping[] mappings = servletHandler.getServletMappings();
 		if (mappings == null) {
 			return;
 		}
 		final List<ServletMapping> newMappings = new ArrayList<ServletMapping>(
 				mappings.length);
 		final Set<String> mappedServlets = new HashSet<String>(mappings.length);
 		for (final ServletMapping mapping : mappings) {
 			final String[] pathSpecs = mapping.getPathSpecs();
 			for (final String spec : pathSpecs) {
 				if (pathSpec.equals(spec)) {
 					mapping.setPathSpecs(ArrayUtil.removeFromArray(
 							mapping.getPathSpecs(), spec));
 					removedSomething = true;
 				}
 			}
 			if (mapping.getPathSpecs().length > 0) {
 				newMappings.add(mapping);
 				mappedServlets.add(mapping.getServletName());
 			}
 		}
 
 		// sanity check
 		if (!removedSomething)
 			throw new IllegalStateException("alias '" + alias
 					+ "' registered but nothing removed");
 
 		// find servlets to remove
 		final ServletHolder[] servlets = servletHandler.getServlets();
 		final List<ServletHolder> servletsToRemove = new ArrayList<ServletHolder>(
 				servlets.length);
 		final List<ServletHolder> newServlets = new ArrayList<ServletHolder>(
 				servlets.length);
 		for (final ServletHolder servlet : servlets) {
 			if (!mappedServlets.contains(servlet.getName())) {
 				servletsToRemove.add(servlet);
 			} else {
 				newServlets.add(servlet);
 			}
 		}
 
 		// update mappings and servlets
 		servletHandler.setServlets(newServlets
 				.toArray(new ServletHolder[newServlets.size()]));
 		servletHandler.setServletMappings(newMappings
 				.toArray(new ServletMapping[newMappings.size()]));
 
 		// stop removed servlets
 		for (final ServletHolder servlet : servletsToRemove) {
 			try {
 				servlet.doStop();
 			} catch (final Exception e) {
 				// ignore
 			}
 		}
 	}
 
 	/**
 	 * Removes the alias from the internal registration.
 	 * 
 	 * @param alias
 	 */
 	private void removeAliasFromRegistration(String alias) {
 		for (Registration reg : registeredArtifacts
 				.toArray(new Registration[registeredArtifacts.size()])) {
 			if (reg.isServlet() && reg.getAlias().equals(alias)) {
 				registeredArtifacts.remove(reg);
 			}
 		}
 	}
 
 	/**
 	 * Removes the filter from the internal registration.
 	 * 
 	 * @param alias
 	 */
 	private void removeFilterFromRegistration(Filter filter) {
 		for (Registration reg : registeredArtifacts
 				.toArray(new Registration[registeredArtifacts.size()])) {
 			if (reg.isFilter() && reg.getContext() == filter) {
 				registeredArtifacts.remove(reg);
 			}
 		}
 	}
 
 	/**
 	 * Registers the given filter under the alias.
 	 * 
 	 * @param alias
 	 * @param filter
 	 * @param initparams
 	 * @throws NamespaceException
 	 */
 	public void registerFilter(String alias, Filter filter,
 			Map<String, String> initparams) throws NamespaceException {
 
 		final String pathSpec = normalizeAliasToPathSpec(alias);
 
 		// ensure that only one threas manipulates the contents
 		accessLock.lock();
 		try {
 			addBundleResourceMonitor(filter);
 
 			// create holder
 			final FilterHolder holder = new FilterHolder(filter);
 			if (initparams != null) {
 				holder.setInitParameters(initparams);
 			}
 
 			// register filter
 			servletContext.getServletHandler().addFilterWithMapping(holder,
 					pathSpec, null);
 		} finally {
 			accessLock.unlock();
 		}
 	}
 
 	/**
 	 * Unregisters the given filter
 	 * 
 	 * @param filter
 	 */
 	public void unregisterFilter(Filter filter) {
 
 		// removes the filter from internal registration
 		removeFilterFromRegistration(filter);
 
 		// collect list of remaining filters and filters to remove
 		final ServletHandler servletHandler = servletContext
 				.getServletHandler();
 		final FilterHolder[] filters = servletHandler.getFilters();
 		final List<FilterHolder> newfilters = new ArrayList<FilterHolder>(
 				filters.length);
 		final Set<FilterHolder> toRemove = new HashSet<FilterHolder>(
 				filters.length);
 		final Set<String> toRemoveNames = new HashSet<String>(filters.length);
 		for (final FilterHolder filterHolder : filters) {
 			if (filterHolder.getFilter() == filter) {
 				toRemove.add(filterHolder);
 				toRemoveNames.add(filterHolder.getName());
 			} else {
 				newfilters.add(filterHolder);
 			}
 		}
 
 		// sanity check
 		if (toRemove.isEmpty()) {
 			throw new IllegalStateException("filter '" + filter + "' not found");
 		}
 
 		// collect remaining mappings
 		final FilterMapping[] mappings = servletHandler.getFilterMappings();
 		final List<FilterMapping> newMappings = new ArrayList<FilterMapping>(
 				mappings.length);
 		for (final FilterMapping mapping : mappings) {
 			final String filterName = mapping.getFilterName();
			if (!toRemoveNames.contains(filterName)) {
 				newMappings.add(mapping);
 			}
 		}
 
 		// update mappings and servlets
 		servletHandler.setFilters(newfilters
 				.toArray(new FilterHolder[newfilters.size()]));
 		servletHandler.setFilterMappings(newMappings
 				.toArray(new FilterMapping[newMappings.size()]));
 
 		// stop removed filters
 		for (final FilterHolder filterHolder : toRemove) {
 			try {
 				filterHolder.doStop();
 			} catch (final Exception e) {
 				// ignore
 			}
 		}
 	}
 
 	private void registerAlias(final String alias) throws NamespaceException {
 		if (registeredAlias.contains(alias)) {
 			logger.error("Alias {} was already registered!", alias);
 			throw new NamespaceException(alias);
 		}
 		registeredAlias.add(alias);
 	}
 
 	/**
 	 * Unregister all artifacts associated with the given alias.
 	 * 
 	 * @param alias
 	 */
 	private void unregisterAlias(final String alias) {
 		if (!registeredAlias.contains(alias)) {
 			logger.error("Alias {} was not registered!", alias);
 			throw new IllegalArgumentException(String.format(
 					"Alias %s was not registered", alias));
 		}
 		registeredAlias.remove(alias);
 
 		// remove the artifact for the alias from the internal registration
 		removeAliasFromRegistration(alias);
 	}
 
 	/**
 	 * Initializes the servlet if it is necessary.
 	 * 
 	 * @param alias
 	 * @param holder
 	 * @throws ServletException
 	 */
 	private void initializeServletIfNecessary(final String alias,
 			final ServletHolder holder) throws ServletException {
 		if (servletContext.getServletHandler().isStarted()
 				|| servletContext.getServletHandler().isStarting()) {
 			try {
 				holder.start();
 			} catch (final Exception e) {
 				// attempt a clean unregister
 				try {
 					unregister(alias);
 				} catch (final Exception e2) {
 					logger.error("{}", e);
 				}
 				// fail
 				throw new ServletException(String.format(
 						"Error starting servlet. %s", e.getMessage()), e);
 			}
 		}
 	}
 
 	/**
 	 * Checks and normalizes an OSGi alias to the path spec (as used by Jetty's
 	 * {@link PathMap}).
 	 * 
 	 * @param alias
 	 *            the alias
 	 * @return the path spec
 	 * @throws IllegalArgumentException
 	 *             if the alias is invalid
 	 */
 	private String normalizeAliasToPathSpec(final String alias)
 			throws IllegalArgumentException {
 		// sanity check alias
 		if (null == alias)
 			throw new IllegalArgumentException("alias must not be null");
 		if (!alias.startsWith(URIUtil.SLASH) && !alias.startsWith("*."))
 			throw new IllegalArgumentException(
 					"alias must start with '/' or '*.'");
 		if (alias.endsWith("/*"))
 			throw new IllegalArgumentException("alias must not end with '/*'");
 		if (!URIUtil.SLASH.equals(alias)
 				&& StringUtil.endsWithIgnoreCase(alias, URIUtil.SLASH))
 			throw new IllegalArgumentException("alias must not end with '/'");
 
 		// use extension alias as is
 		if (alias.startsWith("*."))
 			return alias;
 
 		// make all other aliases implicit to simulate OSGi prefix matching
 		// note, '/' must also be made implicit so that internally it matches as
 		// '/*'
 		return URIUtil.SLASH.equals(alias) ? "/*" : alias.concat("/*");
 	}
 
 	/**
 	 * Register a bundle listener that will unregister the alias if the bundle
 	 * of the given class will stop.
 	 * 
 	 * @param alias
 	 * @param bundleClass
 	 */
 	private void addBundleResourceMonitor(final String alias,
 			final Class<?> bundleClass) {
 		final Bundle bundle = FrameworkUtil.getBundle(bundleClass);
 		if (bundle != null) {
 			final BundleContext bundleContext = bundle.getBundleContext();
 			if (null != bundleContext) {
 				final BundleResourceMonitor monitor = new BundleResourceMonitor(
 						alias, bundleContext);
 				bundleMonitors.put(alias, monitor);
 				monitor.activate();
 			}
 		}
 	}
 
 	/**
 	 * Register a bundle listener that will unregister the alias if the bundle
 	 * of the given class will stop.
 	 * 
 	 * @param alias
 	 * @param bundleClass
 	 */
 	private void addBundleResourceMonitor(final Filter filter) {
 		final Bundle bundle = FrameworkUtil.getBundle(filter.getClass());
 		if (bundle != null) {
 			final BundleContext bundleContext = bundle.getBundleContext();
 			if (null != bundleContext) {
 				final BundleResourceMonitor monitor = new BundleResourceMonitor(
 						filter, bundleContext);
 				bundleMonitors.put(filter, monitor);
 				monitor.activate();
 			}
 		}
 	}
 
 	/**
 	 * Removes the bundle listener that will unregister the alias if the bundle
 	 * of the given class was unregistered.
 	 * 
 	 * @param alias
 	 */
 	private void removeBundleResourceMonitor(final String alias) {
 		final BundleResourceMonitor monitor = bundleMonitors.remove(alias);
 		if (monitor != null) {
 			monitor.remove();
 		}
 	}
 
 	/**
 	 * An internal factory class to create and destroy http services.
 	 */
 	private class HttpServiceServiceFactory implements
 			ServiceFactory<ExtendedHttpService> {
 		@Override
 		public ExtendedHttpService getService(Bundle bundle,
 				ServiceRegistration<ExtendedHttpService> registration) {
 			return new HttpServiceImpl(HttpApplication.this.context,
 					HttpApplication.this);
 		}
 
 		@Override
 		public void ungetService(Bundle bundle,
 				ServiceRegistration<ExtendedHttpService> registration,
 				ExtendedHttpService service) {
 			((HttpServiceImpl) service).destroy();
 		}
 	}
 
 	private final class BundleResourceMonitor implements BundleListener {
 		private final String alias;
 		private final long bundleId;
 		private final Filter filter;
 		private BundleContext bundleContext;
 
 		BundleResourceMonitor(final String alias,
 				final BundleContext bundleContext) {
 			this.alias = alias;
 			this.filter = null;
 			this.bundleContext = bundleContext;
 			bundleId = bundleContext.getBundle().getBundleId();
 		}
 
 		BundleResourceMonitor(final Filter filter,
 				final BundleContext bundleContext) {
 			this.alias = null;
 			this.filter = filter;
 			this.bundleContext = bundleContext;
 			bundleId = bundleContext.getBundle().getBundleId();
 		}
 
 		void activate() {
 			try {
 				bundleContext.addBundleListener(this);
 			} catch (final Exception e) {
 				// ignore
 			}
 		}
 
 		@Override
 		public void bundleChanged(final BundleEvent event) {
 			if (bundleId != event.getBundle().getBundleId())
 				// ignore events for different bundles
 				// (clarify if we should ever get those here; I got a stacktrace
 				// once that indicates this)
 				return;
 			if (event.getType() == Bundle.STOPPING) {
 				try {
 					if (filter != null) {
 						unregisterFilter(filter);
 					} else {
 						unregister(alias);
 					}
 				} catch (final Exception e) {
 					// ignore
 				} finally {
 					remove();
 				}
 			}
 		}
 
 		void remove() {
 			try {
 				bundleContext.removeBundleListener(this);
 			} catch (final Exception e) {
 				// ignore
 			}
 			bundleContext = null;
 		}
 	}
 
 	private static class Registration {
 		private final String alias;
 		private final Object context;
 		private final String name;
 		private final Map<String, String> params;
 
 		public Registration(String alias, Object context,
 				Map<String, String> params) {
 			super();
 			this.alias = alias;
 			this.context = context;
 			this.params = params != null ? new HashMap<String, String>(params)
 					: null;
 			this.name = null;
 		}
 
 		public Registration(String alias, Object context, String name) {
 			super();
 			this.alias = alias;
 			this.context = context;
 			this.name = name;
 			this.params = null;
 		}
 
 		/**
 		 * Returns true if context is servlet.
 		 * 
 		 * @return
 		 */
 		public boolean isServlet() {
 			return context instanceof Servlet;
 		}
 
 		/**
 		 * Returns true if context is filter.
 		 * 
 		 * @return
 		 */
 		public boolean isFilter() {
 			return context instanceof Filter;
 		}
 
 		/**
 		 * Returns true if context is a resource provider.
 		 * 
 		 * @return
 		 */
 		public boolean isResource() {
 			return context instanceof IResourceProvider;
 		}
 
 		/**
 		 * @return the alias
 		 */
 		public String getAlias() {
 			return alias;
 		}
 
 		/**
 		 * @return the context
 		 */
 		public Object getContext() {
 			return context;
 		}
 	}
 }
