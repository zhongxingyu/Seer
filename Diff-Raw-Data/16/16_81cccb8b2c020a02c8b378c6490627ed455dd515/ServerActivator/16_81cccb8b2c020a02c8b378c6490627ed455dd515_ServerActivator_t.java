 /*******************************************************************************
  * Copyright (c) May 20, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: 
  * 	Martin Taal - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.server.internal;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.osgi.service.datalocation.Location;
 import org.eclipse.osgi.service.debug.DebugOptions;
 import org.eclipse.osgi.service.debug.DebugOptionsListener;
 import org.eclipse.osgi.service.debug.DebugTrace;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.netxforge.netxstudio.common.properties.IPropertiesProvider;
 import com.netxforge.netxstudio.common.properties.PropertiesUtil;
 import com.netxforge.netxstudio.server.ServerModule;
 import com.netxforge.netxstudio.server.ServerUtils;
 
 /**
  * The following tracing options are supported.
  * 
  * {@link #PLUGIN_ID }
  * 
  * @author Christophe
  * 
  */
 public class ServerActivator implements BundleActivator, DebugOptionsListener,
 		IPropertiesProvider {
 
 	private static final String NETXSERVER_PROPERTIES_FILE_NAME = "netxserver.properties";
 
 	private static BundleContext context;
 
 	private static ServerActivator INSTANCE;
 
 	private static final String PLUGIN_ID = "com.netxforge.netxstudio.server";
 	private static final String DEBUG_OPTION = "/debug";
 
 	// public tracing options.
 	public static final String TRACE_SERVER_CDO_OPTION = "/trace.server.cdo";
 	public static final String TRACE_SERVER_COMMIT_INFO_CDO_OPTION = "/trace.server.commitinfo";
 
 	// fields to cache the debug flags
 	public static boolean DEBUG = false;
 	public static DebugTrace TRACE = null;
 
 	public void optionsChanged(DebugOptions options) {
 		DEBUG = options.getBooleanOption(PLUGIN_ID + DEBUG_OPTION, false);
 		TRACE = options.newDebugTrace(PLUGIN_ID);
 	}
 
 	public static BundleContext getContext() {
 		return context;
 	}
 
 	public static ServerActivator getInstance() {
 		return INSTANCE;
 	}
 
 	private Injector injector;
 
 	private Properties properties;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
 		INSTANCE = this;
 		ServerActivator.context = bundleContext;
 		injector = Guice.createInjector(ServerModule.getModule());
 
 		// Set the Locale
 
 		Locale currentLocal = Locale.getDefault();
 		System.out.println("CURRENT Locale: country = "
 				+ currentLocal.getDisplayCountry() + "language = "
 				+ currentLocal.getDisplayLanguage());
 		Locale.setDefault(Locale.UK);
		
 		currentLocal = Locale.getDefault();
 		System.out.println("NEW Locale: country = "
 				+ currentLocal.getDisplayCountry() + "language = "
 				+ currentLocal.getDisplayLanguage());
 
 		// Get the workspace location property
 		String workspaceLocation = System.getProperty("osgi.instance.area");
 		System.out.println("Workspace location: " + workspaceLocation);
 
 		// Get the Instance location property (Same as workspace).
 		Location instanceLocation = Platform.getInstanceLocation();
 		System.out.println("Instance location: "
 				+ instanceLocation.getURL().toExternalForm());
 
 		// Get the Configuration location property
 		Location configurationLocation = Platform.getConfigurationLocation();
 		System.out.println("Configuration location: "
 				+ configurationLocation.getURL().toExternalForm());
 
 		// Note: The DataArea is never initialized, as we have no knowledge of
 		// the Platform,
 		// As we are an OSGI bundle, we need to explicitly invoke a platform
 		// call, which initializes
 		// the DataArea class. This state location method does the job for us.
 		IPath stateLocation = Platform.getStateLocation(context.getBundle());
 		System.out.println("State location for:"
 				+ context.getBundle().getSymbolicName() + " = "
 				+ stateLocation.toString());
 
 		String bundleLocation = context.getBundle().getLocation();
 		System.out.println("Bundle location for:"
 				+ context.getBundle().getSymbolicName() + " = "
 				+ bundleLocation);
 
 		Dictionary<String, String> props = new Hashtable<String, String>(4);
 		props.put(DebugOptions.LISTENER_SYMBOLICNAME, PLUGIN_ID);
 		context.registerService(DebugOptionsListener.class.getName(), this,
 				props);
 
 		// Test log4J. Remove later...
 		Logger logger = Logger.getLogger(ServerActivator.class);
 		logger.info("Info starting");
 		logger.warn("Warning starting");
 		logger.error("Error starting");
 		logger.debug("Debug starting");
 
 		// Read the server properties
 		PropertiesUtil pu = injector.getInstance(PropertiesUtil.class);
 		pu.readProperties(instanceLocation, NETXSERVER_PROPERTIES_FILE_NAME,
 				getProperties());
 
 	}
 
 	public Properties getProperties() {
 		if (properties == null) {
 			this.properties = new Properties();
 		}
 		return properties;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		ServerActivator.context = null;
 		ServerUtils.getInstance().deActivate();
 		PropertiesUtil pu = injector.getInstance(PropertiesUtil.class);
 		pu.writeProperties(Platform.getInstanceLocation(),
 				NETXSERVER_PROPERTIES_FILE_NAME, getProperties());
 	}
 
 	public Injector getInjector() {
 		return injector;
 	}
 
 }
