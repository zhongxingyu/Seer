 /*******************************************************************************
  * Copyright (c) 2008, 2011 Gunnar Wagenknecht and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html.
  *
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  *******************************************************************************/
 package org.eclipse.gyrex.admin.ui.internal;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Filter;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpService;
 import org.osgi.util.tracker.ServiceTracker;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * HTTP service tracker for Gyrex Admin HTTP service.
  */
 public class AdminUiHttpServiceTracker extends ServiceTracker<HttpService, HttpService> {
 
 	private final static class AdminRootServlet extends HttpServlet {
 		/** serialVersionUID */
 		private static final long serialVersionUID = -7985169474502980696L;
 
 		@Override
 		protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
 			// check if the 'homepage' is requested
 			final String pathInfo = req.getPathInfo();
 			if ((null != pathInfo) && !pathInfo.equals("") && !pathInfo.equals("/")) {
 				// another page is requested, fail with not found
 				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Page Not Found");
 				return;
 			}
 
 			// redirect
 			resp.sendRedirect(req.getContextPath() + AdminUiActivator.ADMIN_ALIAS);
 		}
 	}
 
 	private static final Logger LOG = LoggerFactory.getLogger(AdminUiHttpServiceTracker.class);
 
 	/** ROOT_ALIAS */
 	private static final String ROOT_ALIAS = "/";
 
 	/**
 	 * filter string for the admin http service (value
 	 * <code>(&(objectClass=org.osgi.service.http.HttpService)(other.info=org.eclipse.gyrex.http.admin))</code>
 	 * )
 	 */
 	static final String FILTER_ADMIN_HTTP_SERVICE = "(&(objectClass=" + HttpService.class.getName() + ")(other.info=" + AdminUiActivator.TYPE_ADMIN + "))";
 
 	private static Filter createFilter(final BundleContext context) {
 		try {
 			return context.createFilter(FILTER_ADMIN_HTTP_SERVICE);
 		} catch (final InvalidSyntaxException e) {
 			// this should never happen because we tested the filter
 			throw new IllegalStateException("error in implementation: " + e);
 		}
 	}
 
 	/**
 	 * Creates and returns new admin service tracker instance.
 	 * 
 	 * @param context
 	 *            the bundle context (may not be <code>null</code>)
 	 */
 	public AdminUiHttpServiceTracker(final BundleContext context) {
 		super(context, createFilter(context), null);
 	}
 
	/* (non-Javadoc)
	 * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
	 */
 	@Override
 	public HttpService addingService(final ServiceReference<HttpService> reference) {
 		final HttpService httpService = super.addingService(reference); // calls context.getService(reference);
 		if (null == httpService) {
 			return null;
 		}
 
 		// create the root servlet to redirect to the admin interface
 		final AdminRootServlet rootServlet = new AdminRootServlet();
 
 		// register the root servlet
 		try {
 			httpService.registerServlet(ROOT_ALIAS, rootServlet, null, null);
 		} catch (final Exception e) {
 			LOG.error("An error occurred while registering the root servlet. {}", e.getMessage(), e);
 		}
 		return httpService;
 	}
 
	/* (non-Javadoc)
	 * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
	 */
 	@Override
 	public void removedService(final ServiceReference<HttpService> reference, final HttpService service) {
 		final HttpService httpService = service;
 		httpService.unregister(ROOT_ALIAS);
 
 		super.removedService(reference, service); // calls context.ungetService(reference);
 	}
 }
