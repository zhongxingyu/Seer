 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.core.internal;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.VMRunnerConfiguration;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
 import org.eclipse.wst.server.core.IRuntimeType;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.osgi.framework.BundleContext;
 /**
  * The main server tooling plugin class.
  */
 public class JavaServerPlugin extends Plugin {
 	/**
 	 * Java server plugin id
 	 */
 	public static final String PLUGIN_ID = "org.eclipse.jst.server.core";
 
 	// singleton instance of this class
 	private static JavaServerPlugin singleton;
 
 	//	cached copy of all runtime classpath providers
 	private static List runtimeClasspathProviders;
 
 	//	cached copy of all runtime component providers
 	private static List runtimeComponentProviders;
 
 	//	cached copy of all runtime facet mappings
 	private static List runtimeFacetMappings;
 
 	// cached copy of all server profilers
 	private static List serverProfilers;
 	
 	// runtime listener
 	private static IRuntimeLifecycleListener runtimeListener;
 
 	/**
 	 * Create the JavaServerPlugin.
 	 */
 	public JavaServerPlugin() {
 		super();
 		singleton = this;
 	}
 
 	/**
 	 * Returns the singleton instance of this plugin.
 	 *
 	 * @return a singleton instance
 	 */
 	public static JavaServerPlugin getInstance() {
 		return singleton;
 	}
 
 	/**
 	 * @see Plugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		
 		runtimeListener = new IRuntimeLifecycleListener() {
 			public void runtimeAdded(IRuntime runtime) {
 				handleRuntimeChange(runtime, 0);
 			}
 
 			public void runtimeChanged(IRuntime runtime) {
 				handleRuntimeChange(runtime, 1);
 			}
 
 			public void runtimeRemoved(IRuntime runtime) {
 				handleRuntimeChange(runtime, 2);
 			}
 		};
 		
 		ServerCore.addRuntimeLifecycleListener(runtimeListener);
 	}
 
 	/**
 	 * @see Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context2) throws Exception {
 		ServerCore.removeRuntimeLifecycleListener(runtimeListener);
 		super.stop(context2);
 	}
 
 	/**
 	 * Handle a runtime change by potentially updating the classpath container.
 	 * 
 	 * @param runtime a runtime
 	 */
 	protected void handleRuntimeChange(final IRuntime runtime, final int act) {
 		if (runtime == null)
 			throw new IllegalArgumentException();
 		
 		Trace.trace(Trace.FINEST, "Possible runtime change: " + runtime);
 		
		if (runtime.getRuntimeType() == null)
			return;
		
 		final RuntimeClasspathProviderWrapper rcpw = findRuntimeClasspathProvider(runtime.getRuntimeType());
 		if (rcpw != null && (rcpw.hasRuntimeClasspathChanged(runtime) || act != 1)) {
 			final IPath serverContainerPath = new Path(RuntimeClasspathContainer.SERVER_CONTAINER)
 				.append(rcpw.getId()).append(runtime.getId());
 			
 			class RebuildRuntimeReferencesJob extends Job {
 				public RebuildRuntimeReferencesJob() {
 					super(NLS.bind(Messages.updateClasspathContainers, runtime.getName()));
 				}
 
 				public boolean belongsTo(Object family) {
 					return ServerUtil.SERVER_JOB_FAMILY.equals(family);
 				}
 
 				public IStatus run(IProgressMonitor monitor) {
 					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 					if (projects != null) {
 						int size = projects.length;
 						for (int i = 0; i < size; i++) {
 							if (projects[i].isAccessible()) {
 								try {
 									if (!projects[i].isNatureEnabled(JavaCore.NATURE_ID))
 										continue;
 									
 									IJavaProject javaProject = JavaCore.create(projects[i]);
 									
 									boolean found = false;
 									IClasspathEntry[] ce = javaProject.getRawClasspath();
 									for (int j = 0; j < ce.length; j++) {
 										if (ce[j].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
 											if (serverContainerPath.isPrefixOf(ce[j].getPath()))
 												found = true;
 										}
 									}
 									
 									Trace.trace(Trace.FINEST, "Classpath change on: " + projects[i] + " " + found);
 									
 									if (found) {
 										IRuntime runtime2 = runtime;
 										if (act == 2)
 											runtime2 = null;
 										RuntimeClasspathContainer container = new RuntimeClasspathContainer(projects[i],
 												serverContainerPath, rcpw, runtime2, runtime.getId());
 										JavaCore.setClasspathContainer(serverContainerPath, new IJavaProject[] { javaProject },
 												new IClasspathContainer[] {container}, null);
 									}
 								} catch (Exception e) {
 									Trace.trace(Trace.SEVERE, "Could not update classpath container", e);
 								}
 							}
 						}
 					}
 					
 					return Status.OK_STATUS;
 				}
 			}
 			RebuildRuntimeReferencesJob job = new RebuildRuntimeReferencesJob();
 			job.schedule();
 		}
 	}
 
 	/**
 	 * Convenience method for logging.
 	 *
 	 * @param status a status
 	 */
 	private static void log(IStatus status) {
 		getInstance().getLog().log(status);
 	}
 
 	/*public static void logError(String msg) {
 		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, null));
 	}*/
 
 	public static void logWarning(String msg) {
 		log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.OK, msg, null));
 	}
 
 	/**
 	 * Returns an array of all known runtime classpath provider instances.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the
 	 * result.
 	 * </p>
 	 * 
 	 * @return a possibly-empty array of runtime classpath provider instances
 	 *         {@link RuntimeClasspathProviderWrapper}
 	 */
 	public static RuntimeClasspathProviderWrapper[] getRuntimeClasspathProviders() {
 		if (runtimeClasspathProviders == null)
 			loadRuntimeClasspathProviders();
 		
 		RuntimeClasspathProviderWrapper[] rth = new RuntimeClasspathProviderWrapper[runtimeClasspathProviders.size()];
 		runtimeClasspathProviders.toArray(rth);
 		return rth;
 	}
 
 	/**
 	 * Returns the runtime classpath provider that supports the given runtime type, or <code>null</code>
 	 * if none. This convenience method searches the list of known runtime
 	 * classpath providers ({@link #getRuntimeClasspathProviders()}) for the one with
 	 * a matching runtime type.
 	 * The runtimeType may not be null.
 	 *
 	 * @param runtimeType a runtime type
 	 * @return the runtime classpath provider instance, or <code>null</code> if
 	 *   there is no runtime classpath provider with the given id
 	 */
 	public static RuntimeClasspathProviderWrapper findRuntimeClasspathProvider(IRuntimeType runtimeType) {
 		if (runtimeType == null)
 			throw new IllegalArgumentException();
 
 		if (runtimeClasspathProviders == null)
 			loadRuntimeClasspathProviders();
 		
 		Iterator iterator = runtimeClasspathProviders.iterator();
 		while (iterator.hasNext()) {
 			RuntimeClasspathProviderWrapper runtimeClasspathProvider = (RuntimeClasspathProviderWrapper) iterator.next();
 			if (runtimeClasspathProvider.supportsRuntimeType(runtimeType))
 				return runtimeClasspathProvider;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the runtime classpath provider with the given id, or <code>null</code>
 	 * if none. This convenience method searches the list of known runtime
 	 * classpath providers ({@link #getRuntimeClasspathProviders()}) for the one with
 	 * a matching runtime classpath provider id ({@link RuntimeClasspathProviderWrapper#getId()}).
 	 * The id may not be null.
 	 *
 	 * @param id the runtime classpath provider id
 	 * @return the runtime classpath provider instance, or <code>null</code> if
 	 *   there is no runtime classpath provider with the given id
 	 */
 	public static RuntimeClasspathProviderWrapper findRuntimeClasspathProvider(String id) {
 		if (id == null)
 			throw new IllegalArgumentException();
 
 		if (runtimeClasspathProviders == null)
 			loadRuntimeClasspathProviders();
 		
 		Iterator iterator = runtimeClasspathProviders.iterator();
 		while (iterator.hasNext()) {
 			RuntimeClasspathProviderWrapper runtimeClasspathProvider = (RuntimeClasspathProviderWrapper) iterator.next();
 			if (id.equals(runtimeClasspathProvider.getId()))
 				return runtimeClasspathProvider;
 		}
 		return null;
 	}
 
 	/**
 	 * Load the runtime classpath providers.
 	 */
 	private static synchronized void loadRuntimeClasspathProviders() {
 		if (runtimeClasspathProviders != null)
 			return;
 		Trace.trace(Trace.CONFIG, "->- Loading .runtimeClasspathProviders extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JavaServerPlugin.PLUGIN_ID, "runtimeClasspathProviders");
 		
 		int size = cf.length;
 		List list = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				list.add(new RuntimeClasspathProviderWrapper(cf[i]));
 				Trace.trace(Trace.CONFIG, "  Loaded runtimeClasspathProviders: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load runtimeClasspathProviders: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		runtimeClasspathProviders = list;
 		
 		Trace.trace(Trace.CONFIG, "-<- Done loading .runtimeClasspathProviders extension point -<-");
 	}
 	
 	/**
 	 * Returns an array of all known runtime classpath provider instances.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @return a possibly-empty array of runtime classpath provider instances
 	 *    {@link RuntimeClasspathProviderWrapper}
 	 */
 	public static RuntimeFacetMapping[] getRuntimeFacetMapping() {
 		if (runtimeFacetMappings == null)
 			loadRuntimeFacetMapping();
 		
 		RuntimeFacetMapping[] rfm = new RuntimeFacetMapping[runtimeFacetMappings.size()];
 		runtimeFacetMappings.toArray(rfm);
 		return rfm;
 	}
 
 	/**
 	 * Returns the runtime component provider that supports the given runtime type, or <code>null</code>
 	 * if none. This convenience method searches the list of known runtime
 	 * component providers for the one with a matching runtime type.
 	 * The runtimeType may not be null.
 	 *
 	 * @param runtimeType a runtime type
 	 * @return the runtime component provider instance, or <code>null</code> if
 	 *   there is no runtime component provider with the given id
 	 */
 	public static RuntimeComponentProviderWrapper findRuntimeComponentProvider(IRuntimeType runtimeType) {
 		if (runtimeType == null)
 			throw new IllegalArgumentException();
 
 		if (runtimeComponentProviders == null)
 			loadRuntimeComponentProviders();
 		
 		Iterator iterator = runtimeComponentProviders.iterator();
 		while (iterator.hasNext()) {
 			RuntimeComponentProviderWrapper runtimeComponentProvider = (RuntimeComponentProviderWrapper) iterator.next();
 			if (runtimeComponentProvider.supportsRuntimeType(runtimeType))
 				return runtimeComponentProvider;
 		}
 		return null;
 	}
 
 	/**
 	 * Load the runtime component providers.
 	 */
 	private static synchronized void loadRuntimeComponentProviders() {
 		if (runtimeComponentProviders != null)
 			return;
 		Trace.trace(Trace.CONFIG, "->- Loading .runtimeComponentProviders extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JavaServerPlugin.PLUGIN_ID, "internalRuntimeComponentProviders");
 		
 		int size = cf.length;
 		List list = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				list.add(new RuntimeComponentProviderWrapper(cf[i]));
 				Trace.trace(Trace.CONFIG, "  Loaded runtimeComponentProviders: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load runtimeComponentProvider: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		runtimeComponentProviders = list;
 		
 		Trace.trace(Trace.CONFIG, "-<- Done loading .runtimeComponentProviders extension point -<-");
 	}
 
 	/**
 	 * Load the runtime facet mappings.
 	 */
 	private static synchronized void loadRuntimeFacetMapping() {
 		if (runtimeFacetMappings != null)
 			return;
 		Trace.trace(Trace.CONFIG, "->- Loading .runtimeFacetMapping extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JavaServerPlugin.PLUGIN_ID, "runtimeFacetMappings");
 
 		int size = cf.length;
 		List list = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				list.add(new RuntimeFacetMapping(cf[i]));
 				Trace.trace(Trace.CONFIG, "  Loaded runtimeFacetMapping: " + cf[i].getAttribute("runtimeTypeId"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load runtimeFacetMapping: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		runtimeFacetMappings = list;
 		
 		Trace.trace(Trace.CONFIG, "-<- Done loading .runtimeFacetMapping extension point -<-");
 	}
 	
 	/**
 	 * Returns an array of all known server profiler instances.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @return a possibly-empty array of server profiler instances
 	 *    {@link ServerProfiler}
 	 */
 	public static ServerProfiler[] getServerProfilers() {
 		if (serverProfilers == null)
 			loadServerProfilers();
 		
 		ServerProfiler[] sp = new ServerProfiler[serverProfilers.size()];
 		serverProfilers.toArray(sp);
 		return sp;
 	}
 
 	/**
 	 * Load the server profilers.
 	 */
 	private static synchronized void loadServerProfilers() {
 		if (serverProfilers != null)
 			return;
 		Trace.trace(Trace.CONFIG, "->- Loading .serverProfilers extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JavaServerPlugin.PLUGIN_ID, "serverProfilers");
 		
 		int size = cf.length;
 		List list = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				list.add(new ServerProfiler(cf[i]));
 				Trace.trace(Trace.CONFIG, "  Loaded serverProfiler: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load serverProfiler: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		serverProfilers = list;
 		
 		Trace.trace(Trace.CONFIG, "-<- Done loading .serverProfilers extension point -<-");
 	}
 
 	public static void configureProfiling(ILaunch launch, IVMInstall vmInstall, VMRunnerConfiguration vmConfig, IProgressMonitor monitor) throws CoreException {
 		ServerProfiler[] sp = JavaServerPlugin.getServerProfilers();
 		if (sp == null || sp.length == 0)
 			throw new CoreException(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, Messages.errorNoProfiler, null));
 		
 		sp[0].process(launch, vmInstall, vmConfig, monitor);
 	}
 }
