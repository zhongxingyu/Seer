 /*******************************************************************************
  * Copyright (c) 2011 Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.remoteservice.internal.tm.restlet.servlet;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.ecf.remoteservice.internal.Activator;
import org.eclipselabs.restlet.IApplicationProvider;
import org.eclipselabs.restlet.IResourceProvider;
 import org.osgi.service.log.LogService;
 
 public class RestletComponent {
 
 	public static final String RESTLET_URLPREFIX_ = "org.eclipse.ecf.remoteservice.restlet.exporturl";
 	public static final String RESTLET_URLPREFIX_PROTOCOL = System.getProperty(
 			RESTLET_URLPREFIX_ + ".protocol", "http");
 	public static final String RESTLET_URLPREFIX_HOST = System.getProperty(
 			RESTLET_URLPREFIX_ + ".host", "localhost");
 	public static final String RESTLET_URLPREFIX_PORT = System.getProperty(
 			RESTLET_URLPREFIX_ + ".port", "8080");
 	public static final String RESTLET_SERVICE_EXPORTED_INTERFACES = "restlet.service.exported.interfaces";
 
 	class HttpService {
 		private org.osgi.service.http.HttpService httpService;
 		@SuppressWarnings("rawtypes")
 		private Map properties;
 
 		public HttpService(org.osgi.service.http.HttpService httpService,
 				@SuppressWarnings("rawtypes") Map properties) {
 			this.httpService = httpService;
 			this.properties = properties;
 		}
 
 		public org.osgi.service.http.HttpService getHttpService() {
 			return this.httpService;
 		}
 
 		@SuppressWarnings("rawtypes")
 		public Map getProperties() {
 			return this.properties;
 		}
 	}
 
 	private HttpService httpService;
 	private LogService logService;
 
 	class ApplicationProvider {
 		private IApplicationProvider applicationProvider;
 		@SuppressWarnings("rawtypes")
 		private Map properties;
 
 		public ApplicationProvider(IApplicationProvider applicationProvider,
 				@SuppressWarnings("rawtypes") Map properties) {
 			this.applicationProvider = applicationProvider;
 			this.properties = properties;
 		}
 
 		public IApplicationProvider getApplicationProvider() {
 			return this.applicationProvider;
 		}
 
 		@SuppressWarnings("rawtypes")
 		public Map getProperties() {
 			return this.properties;
 		}
 	}
 
 	private HashSet<ApplicationProvider> applicationProviders = new HashSet<ApplicationProvider>();
 
 	private Map<String, RestletApplicationServlet> registeredServlets = Collections
 			.synchronizedMap(new HashMap<String, RestletApplicationServlet>());
 	private Object restletTopologyManagerLock = new Object();
 	private RestletTopologyManager restletTopologyManager;
 
 	void bindLogService(LogService logService) {
 		this.logService = logService;
 	}
 
 	void unbindLogService(LogService logService) {
 		if (this.logService == logService)
 			this.logService = null;
 	}
 
 	void bindApplicationProvider(IApplicationProvider applicationProvider,
 			@SuppressWarnings("rawtypes") Map properties) {
 		ApplicationProvider ap = new ApplicationProvider(applicationProvider,
 				properties);
 		applicationProviders.add(ap);
 		if (httpService != null)
 			registerAndExport(ap);
 	}
 
 	private ApplicationProvider removeApplicationProvider(
 			IApplicationProvider applicationProvider) {
 		for (Iterator<ApplicationProvider> i = applicationProviders.iterator(); i
 				.hasNext();) {
 			ApplicationProvider ap = i.next();
 			IApplicationProvider iap = ap.getApplicationProvider();
 			if (iap == applicationProvider) {
 				i.remove();
 				return ap;
 			}
 		}
 		return null;
 	}
 
 	void unbindApplicationProvider(IApplicationProvider applicationProvider) {
 		ApplicationProvider ap = removeApplicationProvider(applicationProvider);
 		if (httpService != null)
 			unregisterServletAndUnexport(ap);
 	}
 
 	private Map<String, Map<?, ?>> resourceProviderProperties = new HashMap<String, Map<?, ?>>();
 
 	void bindResourceProvider(IResourceProvider resourceProvider,
 			Map<?, ?> rpProperties) {
 		if (resourceProvider != null
 				&& rpProperties
 						.get(RESTLET_SERVICE_EXPORTED_INTERFACES) != null) {
 			String[] paths = resourceProvider.getPaths();
 			if (paths != null)
 				for (int i = 0; i < paths.length; i++)
 					resourceProviderProperties.put(paths[i], rpProperties);
 		}
 	}
 
 	void unbindResourceProvider(IResourceProvider resourceProvider) {
 		if (resourceProvider != null) {
 			String[] paths = resourceProvider.getPaths();
 			if (paths != null)
 				for (int i = 0; i < paths.length; i++)
 					resourceProviderProperties.remove(paths[i]);
 		}
 	}
 
 	void bindHttpService(org.osgi.service.http.HttpService httpService,
 			@SuppressWarnings("rawtypes") Map httpServiceProperties) {
 		this.httpService = new HttpService(httpService, httpServiceProperties);
 		for (ApplicationProvider applicationProvider : applicationProviders)
 			registerAndExport(applicationProvider);
 	}
 
 	private org.osgi.service.http.HttpService getHttpService() {
 		return this.httpService.getHttpService();
 	}
 
 	void unbindHttpService(org.osgi.service.http.HttpService httpService) {
 		if (getHttpService() == httpService) {
 			for (ApplicationProvider applicationProvider : applicationProviders)
 				unregisterServletAndUnexport(applicationProvider);
 			this.httpService = null;
 		}
 	}
 
 	private RestletTopologyManager getRestletTopologyManager() {
 		synchronized (restletTopologyManagerLock) {
 			if (restletTopologyManager == null) {
 				restletTopologyManager = new RestletTopologyManager(
 						Activator.getContext(), this);
 			}
 		}
 		return restletTopologyManager;
 	}
 
 	private String getHost(@SuppressWarnings("rawtypes") Map properties) {
 		String h = (String) properties.get(RESTLET_URLPREFIX_ + ".host");
 		if (h == null)
 			h = RESTLET_URLPREFIX_HOST;
 		if (h.equals("<hostname>")) {
 			try {
 				h = InetAddress.getLocalHost().getCanonicalHostName();
 			} catch (UnknownHostException e) {
 			}
 		}
 		return h;
 	}
 
 	private int getPort(@SuppressWarnings("rawtypes") Map properties) {
 		String p = (String) properties.get(RESTLET_URLPREFIX_ + ".port");
 		if (p == null)
 			p = RESTLET_URLPREFIX_PORT;
 		int result = -1;
 		try {
 			result = new Integer(p).intValue();
 		} catch (NumberFormatException e) {
 		}
 		return result;
 	}
 
 	private String getUrlPrefix(@SuppressWarnings("rawtypes") Map properties) {
 		if (properties == null)
 			return null;
 		String urlPrefix = (String) properties.get(RESTLET_URLPREFIX_
 				+ ".urlprefix");
 		if (urlPrefix != null)
 			return urlPrefix;
 		StringBuffer sb = new StringBuffer();
 		// protocol
 		String protocol = (String) properties.get(RESTLET_URLPREFIX_
 				+ ".protocol");
 		sb.append((protocol != null) ? protocol : RESTLET_URLPREFIX_PROTOCOL)
 				.append("://");
 		// host
 		String host = getHost(properties);
 		sb.append((host != null) ? host : RESTLET_URLPREFIX_HOST);
 		// port
 		int port = getPort(properties);
 		if (port > 0)
 			sb.append(":").append(port);
 		return sb.toString();
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private Map dictionaryToMap(Dictionary<String, Object> dict) {
 		if (dict == null)
 			return null;
 		Map map = new HashMap();
 		for (Enumeration<String> e = dict.keys(); e.hasMoreElements();) {
 			String key = e.nextElement();
 			Object val = dict.get(key);
 			map.put(key, val);
 		}
 		return map;
 	}
 
 	protected String getRemoteUrlPrefix(ApplicationProvider applicationProvider) {
 		String result = null;
 		// First check http service
 		@SuppressWarnings("rawtypes")
 		Map httpServiceProperties = this.httpService.getProperties();
 		String httpUrlPrefix = getUrlPrefix(httpServiceProperties);
 		if (httpUrlPrefix != null)
 			result = httpUrlPrefix;
 		// Then check applicationProvider properties
 		@SuppressWarnings("rawtypes")
 		Map apProperties = applicationProvider.getProperties();
 		String apUrlPrefix = getUrlPrefix(apProperties);
 		if (apUrlPrefix != null)
 			result = apUrlPrefix;
 		// Then check applicationProvider initParams
 		@SuppressWarnings("rawtypes")
 		Map apInitParams = dictionaryToMap(applicationProvider
 				.getApplicationProvider().getInitParms());
 		String apInitUrlPrefix = getUrlPrefix(apInitParams);
 		if (apInitUrlPrefix != null)
 			result = apInitUrlPrefix;
 		return result;
 	}
 
 	private void registerAndExport(ApplicationProvider applicationProvider) {
 		// create RemoteApplicationSevlet
 		RestletApplicationServlet restletApplicationServlet = new RestletApplicationServlet(
 				getRemoteUrlPrefix(applicationProvider), applicationProvider);
 
 		String alias = restletApplicationServlet.getAlias();
 		try {
 			getHttpService().registerServlet(alias, restletApplicationServlet,
 					restletApplicationServlet.getInitParams(),
 					restletApplicationServlet.getContext());
 			registeredServlets.put(alias, restletApplicationServlet);
 			// Now export
 			getRestletTopologyManager().exportRemoteApplicationServlet(
 					restletApplicationServlet);
 		} catch (Exception e) {
 			if (logService != null)
 				logService.log(LogService.LOG_ERROR,
 						"Failed to register the application servlet at alias: '"
 								+ alias + "'", e);
 		}
 	}
 
 	private void unregisterServletAndUnexport(
 			ApplicationProvider applicationProvider) {
 
 		String alias = applicationProvider.getApplicationProvider().getAlias();
 		RestletApplicationServlet restletApplicationServlet = registeredServlets
 				.remove(alias);
 		// unregister servlet
 		if (restletApplicationServlet != null) {
 			// unexport/unadvertise
 			getRestletTopologyManager().unexportRemoteApplicationServlet(
 					restletApplicationServlet);
 			try {
 				getHttpService().unregister(alias);
 			} catch (Throwable t) {
 				if (logService != null)
 					logService.log(LogService.LOG_ERROR,
 							"Failed to unregister the application servlet at alias: '"
 									+ alias + "'", t);
 			}
 		}
 
 	}
 
 	public Map<?, ?> getResourceProviderProperties(String path) {
 		return resourceProviderProperties.get(path);
 	}
 
 }
