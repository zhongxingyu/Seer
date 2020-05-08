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
 package org.lunifera.runtime.web.vaadin.osgi.webapp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.servlet.ServletException;
 
 import org.apache.shiro.web.servlet.IniShiroFilter;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.equinox.http.servlet.ExtendedHttpService;
 import org.lunifera.runtime.web.vaadin.osgi.Activator;
 import org.lunifera.runtime.web.vaadin.osgi.common.IVaadinApplication;
 import org.lunifera.runtime.web.vaadin.osgi.common.VaadinConstants;
 import org.lunifera.runtime.web.vaadin.osgi.common.VaadinStatusCodes;
 import org.lunifera.runtime.web.vaadin.osgi.servlet.VaadinOSGiServlet;
 import org.lunifera.runtime.web.vaadin.osgi.servlet.WebResourcesHttpContext;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.NamespaceException;
 import org.osgi.util.tracker.ServiceTracker;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.vaadin.server.UIProvider;
 
 /**
  * Default implementation of i {@link IVaadinApplication}.
  */
 @SuppressWarnings("deprecation")
 public class VaadinApplication implements IVaadinApplication {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(VaadinApplication.class);
 
 	private final String RESOURCE_BASE = "/VAADIN";
 
 	// OSGi
 	private BundleContext context;
 	private HttpServiceTracker serviceTracker;
 
 	// properties
 	private final String id;
 	private IStatus status;
 	private String name = VaadinConstants.DEFAULT_APPLICATION_NAME;
 	private String uiAlias = VaadinConstants.DEFAULT_UI_ALIAS;
 	private String widgetset = "";
 	private String httpApplication = "";
 
 	private List<UIProvider> uiProviders = new ArrayList<UIProvider>(1);
 	private ExtendedHttpService httpService;
 
 	// lifecycle
 	private boolean started;
 	private final Lock accessLock = new ReentrantLock();
 
 	// web
 	private IniShiroFilter filter;
 	private VaadinOSGiServlet servlet;
 	private WebResourcesHttpContext defaultContext;
 	private String servletAlias;
 
 	public VaadinApplication(String id, BundleContext context) {
 		if (id == null) {
 			throw new NullPointerException("Id must not be null!");
 		}
 		this.id = id;
 		this.context = context;
 	}
 
 	@Override
 	public IStatus getStatus() {
 		return status != null ? status : Status.OK_STATUS;
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
 	 * Sets the name of that application.
 	 * 
 	 * @param name
 	 */
 	public void setName(String name) {
 		this.name = isStringValid(name) ? name
 				: VaadinConstants.DEFAULT_APPLICATION_NAME;
 	}
 
 	@Override
 	public String getUIAlias() {
 		return uiAlias;
 	}
 
 	/**
 	 * Sets the alias that is used to access the vaadin UI.
 	 * 
 	 * @param uiAlias
 	 */
 	public void setUIAlias(String uiAlias) {
 		this.uiAlias = isStringValid(uiAlias) ? uiAlias
 				: VaadinConstants.DEFAULT_UI_ALIAS;
 	}
 
 	@Override
 	public String getHttpApplication() {
 		return httpApplication;
 	}
 
 	/**
 	 * Sets the name of the {@link IHttpApplication} that vaadin application
 	 * should be deployed at.
 	 * 
 	 * @param httpApplication
 	 *            the httpApplication to set
 	 */
 	public void setHttpApplication(String httpApplication) {
 		this.httpApplication = isStringValid(httpApplication) ? httpApplication
 				: "";
 	}
 
 	@Override
 	public String getWidgetSetName() {
 		return widgetset;
 	}
 
 	/**
 	 * Sets the name of the widgetset
 	 * 
 	 * @param widgetset
 	 */
 	public void setWidgetSetName(String widgetset) {
 		this.widgetset = isStringValid(widgetset) ? widgetset : "";
 	}
 
 	/**
 	 * Returns true if the string is not <code>null</code> and not empty string.
 	 * 
 	 * @param value
 	 * @return
 	 */
 	private boolean isStringValid(String value) {
 		return value != null && !value.trim().equals("");
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
 			// start the tracker to observe handler
 			try {
 				serviceTracker = new HttpServiceTracker(context, this);
 				serviceTracker.open();
 			} catch (InvalidSyntaxException e) {
 				logger.error("{}", e);
 				setStatus(VaadinStatusCodes.createHttpServiceTracker(e));
 
 				// stop the application
 				stop(true);
 				return;
 			}
 
 			// add all yet existing handler from the tracker
 			ExtendedHttpService httpService = serviceTracker.getService();
 			if (httpService != null) {
 				try {
 					setHttpService(httpService);
 				} catch (AppException e) {
 					logger.error(String
 							.format("Stopping vaadin application %s since setting http service caused a problem.",
 									getName()));
 					setStatus(VaadinStatusCodes.createSettingHttpService(e));
 
 					// stop the application
 					stop(true);
 					return;
 				}
 			}
 
 			started = true;
 		} finally {
 			accessLock.unlock();
 		}
 
 		setStatus(VaadinStatusCodes.OK_STATUS);
 	}
 
 	/**
 	 * Is called by the service tracker to add a new http service.
 	 * 
 	 * @param httpService
 	 */
 	protected void httpServiceAdded(ExtendedHttpService httpService) {
 		if (!isStarted()) {
 			return;
 		}
 
 		// ensure that only one threads manipulates the contents
 		accessLock.lock();
 		try {
 			setHttpService(httpService);
 		} catch (AppException e) {
 			logger.error(String
 					.format("Stopping vaadin application %s since setting http service caused a problem.",
 							getName()));
 			setStatus(VaadinStatusCodes.createSettingHttpService(e));
 
 			// stop the application
 			stop();
 			return;
 
 		} finally {
 			accessLock.unlock();
 		}
 
 		setStatus(VaadinStatusCodes.OK_STATUS);
 	}
 
 	/**
 	 * Is called by the service tracker to remove a registered http service.
 	 * 
 	 * @param httpService
 	 */
 	protected void httpServiceRemoved(ExtendedHttpService httpService) {
 		if (!isStarted()) {
 			return;
 		}
 
 		// ensure that only one threas manipulates the contents
 		accessLock.lock();
 		try {
 			unsetHttpService(httpService);
 		} finally {
 			accessLock.unlock();
 		}
 	}
 
 	/**
 	 * Sets the given http service to the internal cache.
 	 * 
 	 * @param httpService
 	 * @throws AppException
 	 */
 	protected void setHttpService(ExtendedHttpService httpService)
 			throws AppException {
 		if (this.httpService != null) {
 			logger.error("HttpService already present. Abort operation!");
 			return;
 		}
 
 		this.httpService = httpService;
 
 		registerWebArtifacts();
 	}
 
 	/**
 	 * Registers servlets, filters and resources.
 	 * 
 	 * @throws AppException
 	 */
 	protected void registerWebArtifacts() throws AppException {
 		Hashtable<String, String> properties = new Hashtable<String, String>();
 		properties.put(VaadinConstants.APPLICATION_ID, getId());
 		properties.put(VaadinConstants.APPLICATION_NAME, getName());
 		properties.put(VaadinConstants.WIDGETSET, getWidgetSetName());
 		properties.put("widgetset", getWidgetSetName());
 
 		servlet = new VaadinOSGiServlet(this);
 		servletAlias = String.format("/%s", getUIAlias());
 		defaultContext = new WebResourcesHttpContext(Activator
 				.getBundleContext().getBundle());
 		filter = new IniShiroFilter();
 		try {
 			try {
 				httpService.registerFilter("/", filter, properties,
 						defaultContext);
 			} catch (Exception e) {
 				logger.error("{}", e);
 				throw new AppException(e);
 			}
 			httpService.registerResources(RESOURCE_BASE, RESOURCE_BASE,
 					defaultContext);
 			httpService.registerServlet(servletAlias, servlet, properties,
 					defaultContext);
 		} catch (ServletException e) {
 			logger.error("{}", e);
 			throw new AppException(e);
 		} catch (NamespaceException e) {
 			logger.error("{}", e);
 			throw new AppException(e);
 		}
 	}
 
 	/**
 	 * Sets the current status.
 	 * 
 	 * @param status
 	 */
 	private void setStatus(IStatus status) {
 		this.status = status;
 
 		if (status != null && status != VaadinStatusCodes.OK_STATUS) {
 			logger.warn("Status was set to vaadin application {}: {}",
 					getName(), status);
 		}
 	}
 
 	/**
 	 * Unregisters all registered servlets, filters and resources.
 	 */
 	protected void unregisterWebArtifacts() {
 		try {
 			if (servletAlias != null) {
 				httpService.unregister(servletAlias);
 			}
 		} catch (Exception e) {
 			logger.warn("{}", e);
 		} finally {
 			servletAlias = null;
 			servlet = null;
 		}
 
 		try {
 			if (filter != null) {
 				httpService.unregisterFilter(filter);
 			}
 		} catch (Exception e) {
 			logger.warn("{}", e);
 		} finally {
 			filter = null;
 		}
 
 		try {
 			httpService.unregister(RESOURCE_BASE);
 		} catch (Exception e) {
 			logger.warn("{}", e);
 		} finally {
 		}
 	}
 
 	/**
 	 * Removes the given http service from the internal cache.
 	 * 
 	 * @param httpService
 	 */
 	protected void unsetHttpService(ExtendedHttpService httpService) {
 		if (this.httpService == null) {
 			logger.error("HttpService can not be unset! No instance set yet!");
 			return;
 		}
 
 		if (this.httpService != httpService) {
 			logger.error("Tries to unset different http service. Operation aborted.");
 			return;
 		}
 
 		// unregister all registered artifacts
 		unregisterWebArtifacts();
 
 		this.httpService = null;
 	}
 
 	/**
 	 * Is called to stop the application. All resources should be unregistered
 	 * and the http service will become disposed.
 	 */
 	public void stop() {
 		stop(false);
 	}
 
 	/**
 	 * Stops the application. If true, then the started state will no be
 	 * checked.
 	 * 
 	 * @param force
 	 */
 	protected void stop(boolean force) {
 		if (!force && !started) {
 			logger.debug("HttpApplication {} not started", getName());
 			return;
 		}
 
 		// ensure that only one threads manipulates the contents
 		accessLock.lock();
 		try {
 			if (serviceTracker != null) {
 				serviceTracker.close();
 				serviceTracker = null;
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
 
 			uiProviders = null;
 
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
 	 * Adds a new {@link UIProvider} to the web application.
 	 * 
 	 * @param provider
 	 */
 	public void addUIProvider(UIProvider provider) {
 		if (!uiProviders.contains(provider)) {
 			uiProviders.add(provider);
 		}
 	}
 
 	/**
 	 * Removes an {@link UIProvider} from the web application.
 	 * 
 	 * @param provider
 	 */
 	public void removeUIProvider(UIProvider provider) {
 		uiProviders.remove(provider);
 	}
 
 	@Override
 	public List<UIProvider> getUiProviders() {
 		return Collections.unmodifiableList(uiProviders);
 	}
 
 	/**
 	 * Creates a filter string.
 	 * 
 	 * @param httpApplication
 	 * @return
 	 */
 	private String createHttpServiceFilterString(String httpApplication) {
		String filterString = null;
		if (httpApplication != null && !httpApplication.equals("")) {
			filterString = String
					.format("(&(objectClass=org.eclipse.equinox.http.servlet.ExtendedHttpService)(lunifera.http.name=%s))",
							httpApplication);
		} else {
			filterString = String
					.format("(objectClass=org.eclipse.equinox.http.servlet.ExtendedHttpService)",
							httpApplication);
		}
 		return filterString;
 	}
 
 	public class HttpServiceTracker extends
 			ServiceTracker<ExtendedHttpService, ExtendedHttpService> {
 
 		@SuppressWarnings("unused")
 		private Logger logger = LoggerFactory
 				.getLogger(HttpServiceTracker.class);
 
 		public HttpServiceTracker(BundleContext ctx,
 				IVaadinApplication webApplication)
 				throws InvalidSyntaxException {
 			super(ctx, ctx
 					.createFilter(createHttpServiceFilterString(webApplication
 							.getHttpApplication())), null);
 		}
 
 		@Override
 		public ExtendedHttpService addingService(
 				ServiceReference<ExtendedHttpService> reference) {
 			ExtendedHttpService service = super.addingService(reference);
 
 			VaadinApplication.this.httpServiceAdded(service);
 
 			return service;
 		}
 
 		@Override
 		public void removedService(
 				ServiceReference<ExtendedHttpService> reference,
 				ExtendedHttpService service) {
 			super.removedService(reference, service);
 
 			VaadinApplication.this.httpServiceRemoved(service);
 		}
 	}
 }
