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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.List;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.eclipse.core.runtime.Assert;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.http.HttpContext;
 import org.restlet.Application;
 import org.restlet.Context;
import org.restlet.ext.osgi.IApplicationProvider;
 import org.restlet.ext.servlet.ServerServlet;
 
 public class RestletApplicationServlet extends ServerServlet {
 
 	private static final long serialVersionUID = -9195052367086478796L;
 	public static final String SERVLETCONFIG_ATTRIBUTE = "javax.servlet.ServletConfig";
 
 	private String uriPrefix;
 	private transient IApplicationProvider applicationProvider;
 	private String alias;
 	private Dictionary<String, Object> initParams;
 	private HttpContext httpContext;
 
 	private Application application;
 
 	private List<ServiceRegistration<?>> exportedRegistrations = Collections
 			.synchronizedList(new ArrayList<ServiceRegistration<?>>());
 
 	void addServiceRegistration(ServiceRegistration<?> serviceRegistration) {
 		exportedRegistrations.add(serviceRegistration);
 	}
 
 	ServiceRegistration<?>[] getServiceRegistrations() {
 		return exportedRegistrations
 				.toArray(new ServiceRegistration[exportedRegistrations.size()]);
 	}
 
 	@Override
 	public void destroy() {
 		uriPrefix = null;
 		applicationProvider = null;
 		alias = null;
 		initParams = null;
 		httpContext = null;
 		application = null;
 		exportedRegistrations.clear();
 		super.destroy();
 	}
 
 	public RestletApplicationServlet(String uriPrefix,
 			RestletComponent.ApplicationProvider applicationProvider) {
 		Assert.isNotNull(uriPrefix);
 		this.uriPrefix = uriPrefix;
 		Assert.isNotNull(applicationProvider);
 		this.applicationProvider = applicationProvider.getApplicationProvider();
 		// Call the applicationProvider methods *once* right here
 		this.alias = this.applicationProvider.getAlias();
 		this.initParams = this.applicationProvider.getInitParms();
 		this.httpContext = this.applicationProvider.getContext();
 	}
 
 	public String getUriPrefix() {
 		return uriPrefix;
 	}
 
 	public String getAlias() {
 		return alias;
 	}
 
 	public Dictionary<String, Object> getInitParams() {
 		return initParams;
 	}
 
 	public HttpContext getContext() {
 		return httpContext;
 	}
 
 	IApplicationProvider getApplicationProvider() {
 		return applicationProvider;
 	}
 
 	@Override
 	protected Application createApplication(Context context) {
 		return application;
 	}
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		Context childContext = getComponent().getContext().createChildContext();
 		childContext.getAttributes().put(SERVLETCONFIG_ATTRIBUTE,
 				getServletConfig());
 		// Create the actual application eagerly right here
 		application = applicationProvider.createApplication(childContext);
 	}
 
 	public String getFullURI(String path) {
 		String contextPath = getServletContext().getContextPath();
 		if (contextPath != null)
 			path = contextPath + path;
 		return uriPrefix + path;
 	}
 
 }
