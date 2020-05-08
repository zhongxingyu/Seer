 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.web.plugin;
 
 import java.io.IOException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.List;
 import java.util.Vector;
 
import org.eclipse.core.internal.boot.PlatformURLConnection;
 import org.eclipse.core.resources.IResourceStatus;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IAdapterManager;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
import org.eclipse.jst.j2ee.internal.plugin.J2EEPluginResourceHandler;
 import org.eclipse.jst.j2ee.internal.web.util.WebEditAdapterFactory;
 import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.frameworks.internal.WTPPlugin;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
 
 /**
  * This is a top-level class of the j2ee plugin.
  *  
  */
 
 public class WebPlugin extends WTPPlugin implements ResourceLocator {
 	// Default instance of the receiver
 	private static WebPlugin inst;
 	protected final IPath iconsFolder = new Path(Platform.getBundle(PLUGIN_ID).getEntry("icons").getPath()); //$NON-NLS-1$
 	// Links View part of the plugin
 	//public static final String LINKS_BUILDER_ID =
 	// "com.ibm.etools.links.management.linksbuilder";//$NON-NLS-1$
 	public static final String LINKS_BUILDER_ID = "com.ibm.etools.webtools.additions.linksbuilder"; //$NON-NLS-1$
 	// LibDir Change Listener
 	public static final String LIBDIRCHANGE_BUILDER_ID = "com.ibm.etools.webtools.LibDirBuilder"; //$NON-NLS-1$
 	public static final String PLUGIN_ID = "org.eclipse.jst.j2ee.web"; //$NON-NLS-1$
 	// LibCopy builder ID
 	public static final String LIBCOPY_BUILDER_ID = "org.eclipse.jst.j2ee.LibCopyBuilder"; //$NON-NLS-1$
 	// Validation part of the plugin
 	//Global ResourceSet (somewhat global)
 	private static IPath location;
 	public static final String[] ICON_DIRS = new String[]{"icons/full/obj16", //$NON-NLS-1$
 				"icons/full/cview16", //$NON-NLS-1$
 				"icons/full/ctool16", //$NON-NLS-1$
 				"icons/full/clcl16", //$NON-NLS-1$
 				"icons/full/ovr16", //$NON-NLS-1$
 				"icons/full/extra", //$NON-NLS-1$
 				"icons/full/wizban", //$NON-NLS-1$
 				"icons", //$NON-NLS-1$
 				""}; //$NON-NLS-1$
 
 	/**
 	 * Create the J2EE plugin and cache its default instance
 	 */
 	public WebPlugin() {
 		super();
 		if (inst == null)
 			inst = this;
 	}
 
 	/**
 	 * Get the plugin singleton.
 	 */
 	static public WebPlugin getDefault() {
 		return inst;
 	}
 
 	/*
 	 * Javadoc copied from interface.
 	 */
 	public URL getBaseURL() {
 		return getBundle().getEntry("/"); //$NON-NLS-1$
 	}
 
 	public Object[] getJ2EEWebProjectMigrationExtensions() {
 
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint pct = registry.getExtensionPoint(getBundle().getSymbolicName(), "J2EEWebProjectMigrationExtension"); //$NON-NLS-1$
 
 		IExtension[] extension = pct.getExtensions();
 		List ret = new Vector();
 		for (int l = 0; l < extension.length; ++l) {
 			IExtension config = extension[l];
 
 			IConfigurationElement[] cElems = config.getConfigurationElements();
 			for (int i = 0; i < cElems.length; i++) {
 				IConfigurationElement d = cElems[i];
 				if (d.getName().equals("migration")) { //$NON-NLS-1$ 
 					try {
 						Object me = d.createExecutableExtension("run"); //$NON-NLS-1$
 
 						ret.add(me);
 					} catch (Exception ex) {
 						// ignore this extension, keep going
 					}
 				}
 			}
 		}
 
 		return ret.toArray();
 	}
 
 	/**
 	 * This gets a .gif from the icons folder.
 	 */
 	public Object getImage(String key) {
 		return J2EEPlugin.getImageURL(key, getBundle());
 	}
 
 	// ISSUE: this method is never used in WTP. Seems no need to be API
 	public static IPath getInstallLocation() {
 		if (location == null) {
 			String installLocation = getDefault().getBundle().getLocation();
 			location = new Path(installLocation);
 		}
 		return location;
 	}
 
 	public static URL getInstallURL() {
 		return getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Get the singleton instance.
 	 */
 	public static WebPlugin getPlugin() {
 		return inst;
 	}
 
 	/**
 	 * Return the plugin directory location- the directory that all the plugins are located in (i.e.
 	 * d:\installdir\plugin)
 	 */
 	public static IPath getPluginLocation(String pluginId) {
 		Bundle bundle = Platform.getBundle(pluginId);
 		if (bundle != null) {
 			try {
 				IPath installPath = new Path(bundle.getEntry("/").toExternalForm()).removeTrailingSeparator();  //$NON-NLS-1$
 				String installStr = Platform.asLocalURL(new URL(installPath.toString())).getFile();
 				return new Path(installStr);
 			} catch (IOException e) {
 				//Do nothing
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Return the Servlets Jar file path preference.
 	 * 
 	 * @return String the file path to the servlets jar, or null if never specified.
 	 * @deprecated - the preference store is no longer on this plugin because of the UI dependency.
 	 */
 	public String getPreferenceServletsJar() {
 		//	return getPreferenceStore().getString(IWebToolingCoreConstants.PROP_SERVLET_JAR);
 		return "THIS IS THE WRONG PATH - NEED TO CHANGE IMPLEMENTATION!!!!!"; //$NON-NLS-1$
 	}
 
 	/**
 	 * Return the Servlets Jar file path preference.
 	 * 
 	 * @return String the file path to the servlets jar, or null if never specified.
 	 * @deprecated - the preference store is no longer on this plugin because of the UI dependency.
 	 */
 	public String getPreferenceWebASJar() {
 		//return getPreferenceStore().getString(IWebToolingCoreConstants.PROP_WEBAS_JAR);
 		return "THIS IS THE WRONG PATH - NEED TO CHANGE IMPLEMENTATION!!!!!"; //$NON-NLS-1$
 	}
 
 	public static IWorkspace getWorkspace() {
 		return ResourcesPlugin.getWorkspace();
 	}
 
 	/**
 	 * If this is called from an operation, in response to some other exception that was caught,
 	 * then the client code should throw {@link com.ibm.etools.wft.util.WFTWrappedException};
 	 * otherwise this can still be used to signal some other error condition within the operation,
 	 * or to throw a core exception in a context other than executing an operation
 	 * 
 	 * Create a new IStatus of type ERROR using the J2EEPlugin ID. aCode is just an internal code.
 	 */
 	public static IStatus newErrorStatus(int aCode, String aMessage, Throwable exception) {
 		return newStatus(IStatus.ERROR, aCode, aMessage, exception);
 	}
 
 	/**
 	 * If this is called from an operation, in response to some other exception that was caught,
 	 * then the client code should throw {@link com.ibm.etools.wft.util.WFTWrappedException};
 	 * otherwise this can still be used to signal some other error condition within the operation,
 	 * or to throw a core exception in a context other than executing an operation
 	 * 
 	 * Create a new IStatus of type ERROR, code OPERATION_FAILED, using the J2EEPlugin ID
 	 */
 	public static IStatus newErrorStatus(String aMessage, Throwable exception) {
 		return newErrorStatus(0, aMessage, exception);
 	}
 
 	/**
 	 * If this is called from an operation, in response to some other exception that was caught,
 	 * then the client code should throw {@link com.ibm.etools.wft.util.WFTWrappedException};
 	 * otherwise this can still be used to signal some other error condition within the operation.
 	 * 
 	 * Create a new IStatus of type ERROR, code OPERATION_FAILED, using the J2EEPlugin ID
 	 */
 	public static IStatus newOperationFailedStatus(String aMessage, Throwable exception) {
 		return newStatus(IStatus.ERROR, IResourceStatus.OPERATION_FAILED, aMessage, exception);
 	}
 
 	/**
 	 * Create a new IStatus with a severity using the J2EEPlugin ID. aCode is just an internal code.
 	 */
 	public static IStatus newStatus(int severity, int aCode, String aMessage, Throwable exception) {
 		return new Status(severity, PLUGIN_ID, aCode, aMessage, exception);
 	}
 
 	/*
 	 * Javadoc copied from interface.
 	 */
 	public String getString(String key) {
 		return Platform.getResourceString(getBundle(), key);
 	}
 
 	/*
 	 * Javadoc copied from interface.
 	 */
 	public String getString(String key, Object[] substitutions) {
 		return MessageFormat.format(getString(key), substitutions);
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 	}
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		//WebAppResourceFactory.register(WTPResourceFactoryRegistry.INSTANCE);
 		IAdapterManager manager = Platform.getAdapterManager();
 		manager.registerAdapters(new WebEditAdapterFactory(), ArtifactEditModel.class);
 	}
 
 
     /*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.WTPPlugin#getPluginID()
 	 */
 	public String getPluginID() {
 		return PLUGIN_ID;
 	}
 	
 	public static void log( final Exception e )
 	{
 		final ILog log = WebPlugin.getDefault().getLog();
 		final String msg = "Encountered an unexpected exception.";
 		
 		log.log( new Status( IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, e ) );
 	}
 	public String getString(String key, boolean translate) {
 		// TODO For now...  translate not supported
 		return getString(key);
 	}
 
 	public String getString(String key, Object[] substitutions, boolean translate) {
 		// TODO For now...  translate not supported
 		return getString(key,substitutions);
 	}
 
 }
