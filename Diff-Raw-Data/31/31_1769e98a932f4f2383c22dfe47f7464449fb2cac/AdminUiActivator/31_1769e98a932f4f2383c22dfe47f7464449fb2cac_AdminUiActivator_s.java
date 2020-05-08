 /*******************************************************************************
  * Copyright (c) 2010, 2011 AGETO Service GmbH and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html.
  *
  * Contributors:
  *      Mike Tschierschke - initial API and implementation
  *******************************************************************************/
 package org.eclipse.gyrex.admin.ui.internal;
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.URL;
 
 import org.eclipse.equinox.http.servlet.HttpServiceServlet;
 
 import org.eclipse.gyrex.boot.internal.app.ServerApplication;
 import org.eclipse.gyrex.common.runtime.BaseBundleActivator;
 import org.eclipse.gyrex.server.Platform;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jetty.http.HttpSchemes;
 import org.eclipse.jetty.http.security.Constraint;
 import org.eclipse.jetty.http.ssl.SslContextFactory;
 import org.eclipse.jetty.security.ConstraintMapping;
 import org.eclipse.jetty.security.ConstraintSecurityHandler;
 import org.eclipse.jetty.security.SecurityHandler;
 import org.eclipse.jetty.security.authentication.BasicAuthenticator;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.server.session.HashSessionManager;
 import org.eclipse.jetty.server.session.SessionHandler;
 import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.util.thread.QueuedThreadPool;
 import org.eclipse.jface.resource.ImageDescriptor;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The activator of the admin ui bundle. Serves also images.
  */
 public class AdminUiActivator extends BaseBundleActivator {
 
 	/** ADMIN */
 	protected static final String ADMIN_DEFAULT_ROLE = "admin";
 
 	/** ADMIN */
 	public static final String ADMIN_ALIAS = "admin";
 
 	/** the plug-in id */
 	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui"; //$NON-NLS-1$
 
 	/** the default port for the admin server */
	public static final int DEFAULT_ADMIN_PORT = 3110;
 
 	/** server type for the admin server */
 	public static final String TYPE_ADMIN = SYMBOLIC_NAME + ".http";
 	private static final Logger LOG = LoggerFactory.getLogger(AdminUiActivator.class);
 
 	private static volatile AdminUiActivator instance;
 
 	private static volatile Server server;
 
 	public static final String ID_TABBED_PROPERTIES_CONFIG = "org.eclipse.gyrex.admin.ui.content.properties";
 
 	protected static final String PROPERTY_ADMIN_SECURE = "gyrex.admin.secure";
 	protected static final String PROPERTY_ADMIN_AUTH = "gyrex.admin.auth";
 
 	/**
 	 * Returns an image descriptor for the image file at the given plug-in
 	 * relative path
 	 * 
 	 * @param path
 	 *            the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(final String path) {
 		final URL entry = instance.getBundle().getEntry(path);
 		final ImageDescriptor descriptor = ImageDescriptor.createFromURL(entry);
 		return descriptor;
 	}
 
 	/**
 	 * Returns the instance.
 	 * 
 	 * @return the instance
 	 */
 	public static AdminUiActivator getInstance() {
 		final AdminUiActivator activator = instance;
 		if (activator == null) {
 			throw new IllegalStateException("inactive");
 		}
 		return activator;
 	}
 
 	private AdminUiHttpServiceTracker adminUiHttpServiceTracker;
 
 	/**
 	 * The constructor
 	 */
 	public AdminUiActivator() {
 		super(SYMBOLIC_NAME);
 	}
 
 	private void addNonSslConnector(final Server server) {
 		final SelectChannelConnector connector = new SelectChannelConnector();
 
 		connector.setPort(DEFAULT_ADMIN_PORT);
		connector.setMaxIdleTime(200000);
		connector.setAcceptors(2);
		connector.setStatsOn(false);
 		connector.setLowResourcesConnections(20000);
 		connector.setLowResourcesMaxIdleTime(5000);
 		connector.setForwarded(true);
 
 		server.addConnector(connector);
 	}
 
 	private void addSslConnector(final Server server) {
 
 		try {
 
 			final File keystoreFile = Platform.getStateLocation(AdminUiActivator.getInstance().getBundle()).append("jettycerts").toFile();
 			if (!keystoreFile.isFile()) {
 				if (!keystoreFile.getParentFile().isDirectory() && !keystoreFile.getParentFile().mkdirs()) {
 					throw new IllegalStateException("Error creating directory for jetty ssl certificates");
 				}
 
 				final InputStream stream = getBundle().getEntry("cert/jettycerts.jks").openStream();
 				FileUtils.copyInputStreamToFile(stream, keystoreFile);
 				IOUtils.closeQuietly(stream);
 			}
 
			final SslSelectChannelConnector connector = new SslSelectChannelConnector(new SslContextFactory(keystoreFile.getCanonicalPath()));
 
 			connector.setPort(DEFAULT_ADMIN_PORT);
			connector.setMaxIdleTime(200000);
			connector.setAcceptors(2);
			connector.setStatsOn(false);
 			connector.setLowResourcesConnections(20000);
 			connector.setLowResourcesMaxIdleTime(5000);
 			connector.setForwarded(true);
 
 			connector.setConfidentialPort(DEFAULT_ADMIN_PORT);
 			connector.setConfidentialScheme(HttpSchemes.HTTPS);
 			connector.setIntegralPort(DEFAULT_ADMIN_PORT);
 			connector.setIntegralScheme(HttpSchemes.HTTPS);
 
			// Poorly documented api. How to set it in another way?
			connector.setPassword("changeit");
			connector.setKeyPassword("changeit");

 			server.addConnector(connector);
 		} catch (final Exception e) {
 			throw new IllegalStateException("Error configuring jetty ssl connector for admin ui.", e);
 		}
 
 	}
 
 	/**
 	 * @param password
 	 * @param contextHandler
 	 * @param realmFile
 	 * @return
 	 */
 	private SecurityHandler createSecurityHandler(final Handler baseHandler, final String username, final String password) {
 		final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
 		final ConstraintMapping authenticationContraintMapping = new ConstraintMapping();
 		final Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, ADMIN_DEFAULT_ROLE);
 		constraint.setAuthenticate(true);
 		authenticationContraintMapping.setConstraint(constraint);
 		authenticationContraintMapping.setPathSpec("/*");
 		securityHandler.addConstraintMapping(authenticationContraintMapping);
 		securityHandler.setAuthenticator(new BasicAuthenticator());
 		securityHandler.setHandler(baseHandler);
 		securityHandler.setLoginService(new SimpleAdminLoginService(username, password));
 		return securityHandler;
 	}
 
 	@Override
 	protected void doStart(final BundleContext context) throws Exception {
 		instance = this;
 
 		// activate default servlet redirect
 		adminUiHttpServiceTracker = new AdminUiHttpServiceTracker(context);
 		adminUiHttpServiceTracker.open();
 
 		// start the admin server
 		new Job("Start Jetty Admin Server") {
 
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				try {
 					startServer();
 				} catch (final Exception e) {
 					LOG.error("Failed to start Jetty Admin server.", e);
 					ServerApplication.shutdown(new IllegalStateException("Unable to start Jetty admin server.", e));
 					return Status.CANCEL_STATUS;
 				}
 				return Status.OK_STATUS;
 			}
 		}.schedule(100l);
 
 	}
 
 	@Override
 	protected void doStop(final BundleContext context) throws Exception {
 		instance = null;
 
 		stopServer();
 
 		adminUiHttpServiceTracker.close();
 		adminUiHttpServiceTracker = null;
 	}
 
 	private void startServer() {
 		try {
 			server = new Server();
 
 			if (Boolean.getBoolean(PROPERTY_ADMIN_SECURE)) {
 				addSslConnector(server);
 			} else {
 				addNonSslConnector(server);
 			}
 
 			// tweak server
			server.setSendServerVersion(true);
 			server.setSendDateHeader(true); // required by some (older) browsers to support caching
 			server.setGracefulShutdown(5000);
 
 			// set thread pool
 			final QueuedThreadPool threadPool = new QueuedThreadPool(5);
 			threadPool.setName("jetty-server-admin");
 			server.setThreadPool(threadPool);
 
 			final ServletContextHandler contextHandler = new ServletContextHandler();
 			contextHandler.setSessionHandler(new SessionHandler(new HashSessionManager()));
 
 			final ServletHolder sh = new ServletHolder(new HttpServiceServlet());
 			// The field other.info is required to link the servlet to the rap application
 			sh.getInitParameters().put("other.info", TYPE_ADMIN);
 			sh.getInitParameters().put(Constants.SERVICE_VENDOR, "Eclipse Gyrex");
 			sh.getInitParameters().put(Constants.SERVICE_DESCRIPTION, "HTTP Service for administrative purposes only!");
 			sh.setRunAsRole(ADMIN_DEFAULT_ROLE);
 
 			contextHandler.addServlet(sh, "/*");
 
 			if (Boolean.getBoolean(PROPERTY_ADMIN_SECURE)) {
 				// do authentication?
 				final String authenticationPhrase = System.getProperty(PROPERTY_ADMIN_AUTH);
 				final String[] segments = authenticationPhrase.split("/");
 				if (segments.length != 3) {
 					throw new IllegalArgumentException("Illegal authentication configuration. Must be three string separated by '/'");
 				} else if (!StringUtils.equals(segments[0], "BASIC")) {
 					throw new IllegalArgumentException("Illegal authentication configuration. Only method 'BASIC' is supported. Found " + segments[0]);
 				}
 
 				server.setHandler(createSecurityHandler(contextHandler, segments[1], segments[2]));
 			} else {
 				server.setHandler(contextHandler);
 			}
 			server.start();
 
 		} catch (final Exception e) {
 			throw new IllegalStateException("Error starting jetty for admin ui", e);
 		}
 	}
 
 	private void stopServer() {
 		try {
 			server.stop();
 			server = null;
 		} catch (final Exception e) {
 			throw new IllegalStateException("Error stopping jetty for admin ui", e);
 		}
 	}
 }
