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
 package org.eclipse.jst.j2ee.internal.plugin;
 
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Vector;
 
 import org.eclipse.core.internal.boot.PlatformURLConnection;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceStatus;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdapterManager;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.IJavaModel;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.UIContextDetermination;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.application.ApplicationFactory;
 import org.eclipse.jst.j2ee.application.ApplicationPackage;
 import org.eclipse.jst.j2ee.application.internal.impl.ApplicationFactoryImpl;
 import org.eclipse.jst.j2ee.applicationclient.internal.modulecore.util.AppClientEditAdapterFactory;
 import org.eclipse.jst.j2ee.common.internal.impl.J2EEResourceFactoryRegistry;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveInit;
 import org.eclipse.jst.j2ee.internal.common.VirtualArchiveComponentAdapterFactory;
 import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
 import org.eclipse.jst.j2ee.internal.modulecore.util.EarEditAdapterFactory;
 import org.eclipse.jst.j2ee.internal.validation.ResourceUtil;
import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceExtensionRegistry;
 import org.eclipse.jst.j2ee.internal.xml.J2EEXmlDtDEntityResolver;
 import org.eclipse.jst.j2ee.refactor.listeners.J2EEElementChangedListener;
 import org.eclipse.jst.j2ee.refactor.listeners.ProjectRefactoringListener;
 import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.internal.impl.ReferencedComponentXMIResourceFactory;
 import org.eclipse.wst.common.componentcore.internal.impl.WTPResourceFactoryRegistry;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.frameworks.internal.WTPPlugin;
 import org.eclipse.wst.common.frameworks.internal.operations.IHeadlessRunnableWithProgress;
 import org.eclipse.wst.common.internal.emf.resource.ReferencedXMIFactoryImpl;
 import org.eclipse.wst.common.internal.emf.utilities.DOMUtilities;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModel;
 import org.eclipse.wst.validation.internal.operations.ValidatorManager;
 import org.eclipse.wst.validation.internal.plugin.ValidationPlugin;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
 /**
  * This is a top-level class of the j2ee plugin.
  * 
  * @see AbstractUIPlugin for additional information on UI plugins
  */
 
 public class J2EEPlugin extends WTPPlugin implements ResourceLocator {
 
 	public static final String PLUGIN_ID = "org.eclipse.jst.j2ee"; //$NON-NLS-1$
 	public static final String UI_PLUGIN_ID = "org.eclipse.jst.j2ee.ui"; //$NON-NLS-1$
 	protected final IPath iconsFolder = new Path(Platform.getBundle(PLUGIN_ID).getEntry("icons").getPath()); //$NON-NLS-1$
 	// LibDir Change Listener
 	public static final String LIBDIRCHANGE_BUILDER_ID = "org.eclipse.jst.j2ee.web.LibDirBuilder"; //$NON-NLS-1$
 	// LibCopy builder ID
 	public static final String LIBCOPY_BUILDER_ID = PLUGIN_ID + ".LibCopyBuilder"; //$NON-NLS-1$
 	// Validation part of the plugin
 	public static final String VALIDATION_BUILDER_ID = ValidationPlugin.VALIDATION_BUILDER_ID; // plugin
 //	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
 //	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$	
 	// id
 	// of
 	// the
 	// validation
 	// builder//$NON-NLS-1$
 	//Global ResourceSet (somewhat global)
 	private static boolean defaultIsWorkspaceRelativeSchema = false;
 
 	private List fextendedEditModels;
 	private static IPath location;
 
 	public static IStatus OK_STATUS = new Status(IStatus.OK, PLUGIN_ID, 0, "OK", null); //$NON-NLS-1$
 
 	public static final String[] ICON_DIRS = new String[]{"icons/full/obj16", //$NON-NLS-1$
 				"icons/full/cview16", //$NON-NLS-1$
 				"icons/full/ctool16", //$NON-NLS-1$
 				"icons/full/clcl16", //$NON-NLS-1$
 				"icons/full/ovr16", //$NON-NLS-1$
 				"icons/full/extra", //$NON-NLS-1$
 				"icons/full/wizban", //$NON-NLS-1$
 				"icons", //$NON-NLS-1$
 				""}; //$NON-NLS-1$
 
 	protected static Boolean EJB_AVAILABLE = null;
 	private static Boolean HAS_DEV_ROLE;
 
 	private J2EEPreferences preferences = null;
 	public static J2EEPlugin INSTANCE;
 
 	/**
 	 * Create the J2EE plugin and cache its default instance
 	 */
 	public J2EEPlugin() {
 		super();
 		if (INSTANCE == null)
 			INSTANCE = this;
 		try {
 			resourceBundle = ResourceBundle.getBundle("org.eclipse.jst.j2ee"); //$NON-NLS-1$
 		} catch (MissingResourceException x) {
 			resourceBundle = null;
 			
 		}
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (7/16/2001 7:38:56 PM)
 	 * 
 	 * @return boolean
 	 */
 	public static boolean defaultIsWorkspaceRelativeSchema() {
 		return defaultIsWorkspaceRelativeSchema;
 	}
 
 	/**
 	 * Get the one application factory.
 	 */
 	public ApplicationFactory getApplicationFactory() {
 		return (ApplicationFactory) getApplicationPackage().getEFactoryInstance();
 	}
 
 	/**
 	 * Get the one ejb package.
 	 */
 	public ApplicationPackage getApplicationPackage() {
 		return ApplicationFactoryImpl.getPackage();
 	}
 
 	/**
 	 * Get the plugin singleton.
 	 */
 	static public J2EEPlugin getDefault() {
 		return INSTANCE;
 	}
 
 	/*
 	 * Javadoc copied from interface.
 	 */
 	public URL getBaseURL() {
 		return getBundle().getEntry("/"); //$NON-NLS-1$
 	}
 
 
 	public Object[] getJ2EEWebProjectMigrationExtensions() {
 
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint pct = registry.getExtensionPoint("J2EEWebProjectMigrationExtension"); //$NON-NLS-1$
 		List ret = new Vector();
 		if (pct != null) {
 			IExtension[] extension = pct.getExtensions();
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
 		}
 		return ret.toArray();
 	}
 
 	public EditModel getExtendedEditModel(String editModelKey) {
 
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint pct = registry.getExtensionPoint("EditModelExtension"); //$NON-NLS-1$
 
 		IExtension[] extension = pct.getExtensions();
 		for (int l = 0; l < extension.length; ++l) {
 			IExtension config = extension[l];
 
 			IConfigurationElement[] cElems = config.getConfigurationElements();
 			for (int i = 0; i < cElems.length; i++) {
 				IConfigurationElement d = cElems[i];
 				if (d.getName().equals("editmodel") && editModelKey.equals(d.getAttribute("key"))) //$NON-NLS-1$ //$NON-NLS-2$
 				{ // editmodel class
 					try {
 						return (EditModel) d.createExecutableExtension("run"); //$NON-NLS-1$
 
 					} catch (Exception ex) {
 						return null;
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public List getExtendedEditModels() {
 
 		if (fextendedEditModels != null)
 			return fextendedEditModels;
 		List editModels = new ArrayList();
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint pct = registry.getExtensionPoint("EditModelExtension"); //$NON-NLS-1$
 
 		IExtension[] extension = pct.getExtensions();
 		for (int l = 0; l < extension.length; ++l) {
 			IExtension config = extension[l];
 
 			IConfigurationElement[] cElems = config.getConfigurationElements();
 			for (int i = 0; i < cElems.length; i++) {
 				IConfigurationElement d = cElems[i];
 				if (d.getName().equals("editmodel")) //$NON-NLS-1$
 				{
 					// editmodel class
 					try {
 						editModels.add(d.createExecutableExtension("run")); //$NON-NLS-1$
 
 					} catch (Exception ex) {
 						return null;
 					}
 				}
 			}
 		}
 		fextendedEditModels = editModels;
 		return fextendedEditModels;
 	}
 
 	/**
 	 * This gets a .gif from the icons folder.
 	 */
 	public static URL getImageURL(String key, Bundle bundle) {
 		String gif = "/" + key + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
 		IPath path = null;
 		for (int i = 0; i < ICON_DIRS.length; i++) {
 			path = new Path(ICON_DIRS[i]).append(gif);
 			if (Platform.find(bundle,path) == null)
 				continue;
 			try {
 				return new URL( bundle.getEntry("/"), path.toString()); //$NON-NLS-1$ 
 			} catch (MalformedURLException exception) {
 				org.eclipse.jem.util.logger.proxy.Logger.getLogger().logWarning(J2EEPluginResourceHandler.getString("Load_Image_Error_", new Object[]{key})); //$NON-NLS-1$
 				exception.printStackTrace();
 				continue;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This gets a .gif from the icons folder.
 	 */
 	public Object getImage(String key) {
 		return getImageURL(key,this.getBundle());
 	}
 
 	public static IPath getInstallLocation() {
 		if (location == null) {
 			URL url = getInstallURL();
 			try {
 				String installLocation = ((PlatformURLConnection) url.openConnection()).getURLAsLocal().getFile();
 				location = new Path(installLocation);
 			} catch (IOException e) {
 				org.eclipse.jem.util.logger.proxy.Logger.getLogger().logWarning(J2EEPluginResourceHandler.getString("Install_Location_Error_", new Object[]{url}) + e); //$NON-NLS-1$
 			}
 		}
 		return location;
 	}
 
 	public static URL getInstallURL() {
 		return getDefault().getBundle().getEntry("/"); //$NON-NLS-1$ 
 	}
 
 	protected static IJavaModel getJavaModel() {
 		return JemProjectUtilities.getJavaModel();
 	}
 
 	protected static IJavaModel getJavaModel(IProject aProject) {
 		if (aProject != null)
 			return JemProjectUtilities.getJavaModel();
 		return null;
 	}
 
 	public static IJavaProject getJavaProject(IProject project) {
 		IJavaModel model = getJavaModel(project);
 		if (model != null)
 			return model.getJavaProject(project.getName());
 		return null;
 	}
 
 	/**
 	 * Retrieves a hashtable of a logger's preferences initially from the
 	 * com.ibm.etools.logging.util.loggingDefaults extension point if specified in the
 	 * com.ibm.etools.logging.util plugin.xml file. If specified, the
 	 * com.ibm.etools.logging.util.loggingOptions extension point preferences in the parameter
 	 * plugin's plugin.xml file are returned.
 	 * 
 	 * The logger's preferences are stored in the return hashtable using the static instance
 	 * variables in LoggerStateHashKeys as keys.
 	 * 
 	 * @param plugin
 	 *            the Plugin polled for their logger's preferences in the plugin.xml file
 	 * @return hashtable of a logger's preferences
 	 */
 	/*
 	 * public java.util.Hashtable getMsgLoggerConfig(org.eclipse.core.runtime.Plugin plugin) {
 	 * return (new PluginHelperImpl().getMsgLoggerConfig(plugin)); }
 	 */
 	/**
 	 * Get the singleton instance.
 	 */
 	public static J2EEPlugin getPlugin() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Return the plugin directory location- the directory that all the plugins are located in (i.e.
 	 * d:\installdir\plugin)
 	 */
 	public static IPath getPluginLocation(String pluginId) {
 		//IExtensionRegistry registry = Platform.getExtensionRegistry();
 		Bundle bundle = Platform.getBundle(pluginId);
 		if (bundle != null) {
 			try {
 				IPath installPath = new Path(bundle.getEntry("/").toExternalForm()).removeTrailingSeparator(); //$NON-NLS-1$ 
 				String installStr = Platform.asLocalURL(new URL(installPath.toString())).getFile();
 				return new Path(installStr);
 			} catch (IOException e) {
 			};
 		}
 		return null;
 	}
 
 	protected void initializeDefaultPluginPreferences() {
 		getJ2EEPreferences().initializeDefaultPreferences();
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
 		try {
 			return Platform.getResourceString(getBundle(), key);
 		} catch (MissingResourceException ex) {
 			//TODO Don't throw error - just show unresolved key
 			//Logger.getLogger().logError(ex);
 			return '!' + key + '!';
 		}
 	}
 
 	/*
 	 * Javadoc copied from interface.
 	 */
 	public String getString(String key, Object[] substitutions) {
 		return MessageFormat.format(getString(key), substitutions);
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (7/16/2001 7:38:56 PM)
 	 * 
 	 * @param newDefaultIsWorkspaceRelativeSchema
 	 *            boolean
 	 */
 	public static void setDefaultIsWorkspaceRelativeSchema(boolean newDefaultIsWorkspaceRelativeSchema) {
 		defaultIsWorkspaceRelativeSchema = newDefaultIsWorkspaceRelativeSchema;
 	}
 
 	/**
 	 * This method should be used to determine if the EJB UI support is currently packaged in the
 	 * image.
 	 */
 	public static boolean isEJBSupportAvailable() {
 		if (EJB_AVAILABLE == null) {
 			//TODO We need a better mechanism for the detection of the EJB support.
 			//IPluginDescriptor desc =
 			// Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.jst.j2ee.internal.internal.ejb.ui");
 			// //$NON-NLS-1$
 			// EJB_AVAILABLE = new Boolean(desc != null);
 			EJB_AVAILABLE = Boolean.TRUE;
 		}
 		return EJB_AVAILABLE.booleanValue();
 	}
 
 	/**
 	 * This method should be used to determine if the workbench is running in UI or Headless
 	 * 
 	 * @deprecated use {@link org.eclipse.wst.common.frameworks.internal.UIContextDetermination}
 	 */
 	public static boolean isHeadless() {
 		return UIContextDetermination.getCurrentContext() == UIContextDetermination.HEADLESS_CONTEXT;
 	}
 
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 				
 		J2EEXmlDtDEntityResolver.INSTANCE = new CatalogJ2EEXmlDtDEntityResolver();
 		DOMUtilities.setDefaultEntityResolver(J2EEXmlDtDEntityResolver.INSTANCE); 
 		//Have to do the next line immediately to fix timing problems with factory registration
 		ArchiveInit.init(false);
 			
 		//ModuleMaps are the maps from modules in an ear project to the j2ee projects for the
 		// modules
 		org.eclipse.jst.j2ee.internal.earcreation.modulemap.ModulemapInit.init(false);
 		ensureFactoryRegistration();
 		ValidatorManager.setResourceUtilClass(ResourceUtil.class);
 		IAdapterManager manager = Platform.getAdapterManager();
 		manager.registerAdapters(new EarEditAdapterFactory(), ArtifactEditModel.class);
 		manager.registerAdapters(new AppClientEditAdapterFactory(), ArtifactEditModel.class);
 		WSDLServiceExtensionRegistry.getInstance();
		
 		manager.registerAdapters(new VirtualArchiveComponentAdapterFactory(), VirtualArchiveComponent.class );
 		
 		final ProjectRefactoringListener listener = new ProjectRefactoringListener();//ProjectDependencyCache.getCache());
 		// register the project rename/delete refactoring listener
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
 				IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_DELETE);
 		
 		// register the IElementChangedLister that updates the .component file in 
 		// response to .classpath changes
 		JavaCore.addElementChangedListener(new J2EEElementChangedListener(), ElementChangedEvent.POST_CHANGE);
 		
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(J2EEComponentClasspathUpdater.getInstance(), IResourceChangeEvent.POST_CHANGE);
 	}
 	
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(J2EEComponentClasspathUpdater.getInstance());
 	}
 
 	/*
 	 * need to make sure the correct factories get loaded when we are in the UI
 	 */
 	private void ensureFactoryRegistration() {
 		//Inside the workbench we do not want the global override defined here.
 		ensureDefaultReferencedXMIResourceFactoryRegistration();
 	}
 
 	/**
 	 *  
 	 */
 	private void ensureDefaultReferencedXMIResourceFactoryRegistration() {
 		J2EEResourceFactoryRegistry reg1 = J2EEResourceFactoryRegistry.INSTANCE;
 		WTPResourceFactoryRegistry reg2 = WTPResourceFactoryRegistry.INSTANCE;
 		ReferencedXMIFactoryImpl factory = new ReferencedComponentXMIResourceFactory();
 		reg1.getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, factory);
 		reg2.getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, factory);
 		// add xmi because other plugins are registering it globally
 		reg1.getExtensionToFactoryMap().put("xmi", factory); //$NON-NLS-1$
 		reg2.getExtensionToFactoryMap().put("xmi", factory); //$NON-NLS-1$
 	}
 
 	/**
 	 * This is currently just a convenience api for the AAW to turn off unwanted actions. It will
 	 * currently be initialized based on the existence of the AAW plugin.
 	 */
 	public static boolean hasDevelopmentRole() {
 		if (HAS_DEV_ROLE == null) {
 			Bundle desc = Platform.getBundle("org.eclipse.jst.j2ee.assembly"); //$NON-NLS-1$
 			HAS_DEV_ROLE = desc != null ? Boolean.FALSE : Boolean.TRUE;
 		}
 		return HAS_DEV_ROLE.booleanValue();
 	}
 
 	public static IWorkspaceRunnable getWorkspaceRunnable(final IHeadlessRunnableWithProgress op) {
 		return new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				try {
 					op.run(monitor);
 				} catch (InvocationTargetException e) {
 					throw new CoreException(newErrorStatus(0, e.getMessage(), e));
 				} catch (InterruptedException e) {
 					throw new CoreException(newErrorStatus(0, e.getMessage(), e));
 				}
 			}
 		};
 	}
 
 	public String getPluginID() {
 		return PLUGIN_ID;
 	}
 
 	/**
 	 * @return Returns the preferences.
 	 */
 	public J2EEPreferences getJ2EEPreferences() {
 		if (this.preferences == null)
 			this.preferences = new J2EEPreferences(this);
 		return this.preferences;
 	}
 	public String getString(String key, boolean translate) {
 		// TODO For now...  translate not supported
 		return getString(key);
 	}
 
 	public String getString(String key, Object[] substitutions, boolean translate) {
 		// TODO For now...  translate not supported
 		return getString(key,substitutions);
 	}
 	
 
 	/**
 	 * Record an error against this plugin's log. 
 	 * 
 	 * @param aCode
 	 * @param aMessage
 	 * @param anException
 	 */
 	public static void logError(int aCode, String aMessage,
 			Throwable anException) {
 		getDefault().getLog().log(
 				createErrorStatus(aCode, aMessage, anException));
 	}
 
 	/**
 	 * 
 	 * Record a message against this plugin's log. 
 	 * 
 	 * @param severity
 	 * @param aCode
 	 * @param aMessage
 	 * @param exception
 	 */
 	public static void log(int severity, int aCode, String aMessage,
 			Throwable exception) {
 		log(createStatus(severity, aCode, aMessage, exception));
 	}
 
 	/**
 	 * 
 	 * Record a status against this plugin's log. 
 	 * 
 	 * @param aStatus
 	 */
 	public static void log(IStatus aStatus) {
 		getDefault().getLog().log(aStatus);
 	}
 
 	/**
 	 * Create a status associated with this plugin.
 	 *  
 	 * @param severity
 	 * @param aCode
 	 * @param aMessage
 	 * @param exception
 	 * @return A status configured with this plugin's id and the given parameters.
 	 */
 	public static IStatus createStatus(int severity, int aCode,
 			String aMessage, Throwable exception) {
 		return new Status(severity, PLUGIN_ID, aCode,
 				aMessage != null ? aMessage : "No message.", exception); //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 * @param aCode
 	 * @param aMessage
 	 * @param exception
 	 * @return A status configured with this plugin's id and the given parameters.
 	 */
 	public static IStatus createErrorStatus(int aCode, String aMessage,				
 			Throwable exception) { 
 		return createStatus(IStatus.ERROR, aCode, aMessage != null ? aMessage : exception.toString(), exception);
 	} 
 
 }
