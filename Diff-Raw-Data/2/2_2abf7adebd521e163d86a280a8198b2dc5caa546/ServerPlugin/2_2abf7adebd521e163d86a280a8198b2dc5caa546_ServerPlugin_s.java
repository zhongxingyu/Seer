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
 package org.eclipse.wst.server.core.internal;
 
 import java.io.*;
 import java.util.*;
 import java.text.DateFormat;
 
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.server.core.IModuleArtifact;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 /**
  * The main server plugin class.
  */
 public class ServerPlugin extends Plugin {
 	public static final String PROJECT_PREF_FILE = ".serverPreference";
 	
 	private static final String SHUTDOWN_JOB_FAMILY = "org.eclipse.wst.server.core.family";
 	
 	protected static final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
 	protected static int num = 0;
 	
 	// cached copy of all launchable adapters
 	private static List launchableAdapters;
 
 	// cached copy of all launchable clients
 	private static List clients;
 	
 	// cached copy of all module factories
 	private static List moduleFactories;
 
 	// singleton instance of this class
 	private static ServerPlugin singleton;
 
 	// cached copy of all publish tasks
 	private static List publishTasks;
 	
 	//	cached copy of all server monitors
 	private static List monitors;
 	
 	//	cached copy of all runtime locators
 	private static List runtimeLocators;
 	
 	// cached copy of all module artifact adapters
 	private static List moduleArtifactAdapters;
 	
 	// bundle listener
 	private BundleListener bundleListener;
 
 	private static final String TEMP_DATA_FILE = "tmp-data.xml";
 
 	class TempDir {
 		String path;
 		int age;
 	}
 
 	// temp directories - String key to TempDir
 	protected Map tempDirHash;
 
 	/**
 	 * server core plugin id
 	 */
 	public static final String PLUGIN_ID = "org.eclipse.wst.server.core";
 
 	/**
 	 * Create the ServerPlugin.
 	 */
 	public ServerPlugin() {
 		super();
 		singleton = this;
 	}
 
 	/**
 	 * Returns the singleton instance of this plugin.
 	 *
 	 * @return org.eclipse.wst.server.core.internal.plugin.ServerPlugin
 	 */
 	public static ServerPlugin getInstance() {
 		return singleton;
 	}
 
 	/**
 	 * Returns a temporary directory that the requestor can use
 	 * throughout it's lifecycle. This is primary to be used by
 	 * server instances for working directories, instance specific
 	 * files, etc.
 	 *
 	 * <p>As long as the same key is used to call this method on
 	 * each use of the workbench, this method directory will return
 	 * the same directory. If the directory is not requested over a
 	 * period of time, the directory may be deleted and a new one
 	 * will be assigned on the next request. For this reason, a
 	 * server instance should request the temp directory on startup
 	 * if it wants to store files there. In all cases, the instance
 	 * should have a backup plan anyway, as this directory may be
 	 * deleted accidentally.</p>
 	 *
 	 * @param key
 	 * @return java.io.File
 	 */
 	public IPath getTempDirectory(String key) {
 		if (key == null)
 			return null;
 	
 		// first, look through hash of current directories
 		IPath statePath = ServerPlugin.getInstance().getStateLocation();
 		try {
 			TempDir dir = (TempDir) tempDirHash.get(key);
 			if (dir != null) {
 				dir.age = 0;
 				return statePath.append(dir.path);
 			}
 		} catch (Exception e) {
 			// ignore
 		}
 	
 		// otherwise, create a new directory
 	
 		// find first free directory
 		String path = null;
 		File dir = null;
 		int count = 0;
 		while (dir == null || dir.exists()) {
 			path = "tmp" + count;
 			dir = statePath.append(path).toFile();
 			count ++;
 		}
 	
 		dir.mkdirs();
 	
 		TempDir d = new TempDir();
 		d.path = path;
 		tempDirHash.put(key, d);
 		saveTempDirInfo();
 		return statePath.append(path);
 	}
 	
 	/**
 	 * Remove a temp directory.
 	 * @param key
 	 */
 	public void removeTempDirectory(String key) {
 		if (key == null)
 			return;
 		
 		IPath statePath = ServerPlugin.getInstance().getStateLocation();
 		try {
 			TempDir dir = (TempDir) tempDirHash.get(key);
 			if (dir != null) {
 				tempDirHash.remove(key);
 				saveTempDirInfo();
 				deleteDirectory(statePath.append(dir.path).toFile(), null);
 			}
 		} catch (Exception e) {
 			Trace.trace(Trace.WARNING, "Could not remove temp directory", e);
 		}
 	}
 	
 	/**
 	 * Load the temporary directory information.
 	 */
 	private void loadTempDirInfo() {
 		Trace.trace(Trace.FINEST, "Loading temporary directory information");
 		IPath statePath = ServerPlugin.getInstance().getStateLocation();
 		String filename = statePath.append(TEMP_DATA_FILE).toOSString();
 	
 		tempDirHash = new HashMap();
 		try {
 			IMemento memento = XMLMemento.loadMemento(filename);
 	
 			IMemento[] children = memento.getChildren("temp-directory");
 			int size = children.length;
 			for (int i = 0; i < size; i++) {
 				String key = children[i].getString("key");
 	
 				TempDir d = new TempDir();
 				d.path = children[i].getString("path");
 				d.age = children[i].getInteger("age").intValue();
 				d.age++;
 	
 				tempDirHash.put(key, d);
 			}
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Could not load temporary directory information: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Convenience method for logging.
 	 *
 	 * @param status org.eclipse.core.runtime.IStatus
 	 */
 	public static void log(IStatus status) {
 		getInstance().getLog().log(status);
 	}
 
 	/**
 	 * Save the temporary directory information.
 	 */
 	private void saveTempDirInfo() {
 		// save remaining directories
 		IPath statePath = ServerPlugin.getInstance().getStateLocation();
 		String filename = statePath.append(TEMP_DATA_FILE).toOSString();
 	
 		try {
 			XMLMemento memento = XMLMemento.createWriteRoot("temp-directories");
 	
 			Iterator iterator = tempDirHash.keySet().iterator();
 			while (iterator.hasNext()) {
 				String key = (String) iterator.next();
 				TempDir d = (TempDir) tempDirHash.get(key);
 	
 				if (d.age < 5) {
 					IMemento child = memento.createChild("temp-directory");
 					child.putString("key", key);
 					child.putString("path", d.path);
 					child.putInteger("age", d.age);
 				} else
 					deleteDirectory(statePath.append(d.path).toFile(), null);
 			}
 	
 			memento.saveToFile(filename);
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Could not save temporary directory information", e);
 		}
 	}
 
 	protected void initializeDefaultPluginPreferences() {
 		ServerPreferences.getInstance().setDefaults();
 	}
 
 	/**
 	 * @see Plugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		Trace.trace(Trace.CONFIG, "----->----- Server Core plugin startup ----->-----");
 		super.start(context);
 		
 		initializeDefaultPluginPreferences();
 
 		// load temp directory information
 		loadTempDirInfo();
 		
 		bundleListener = new BundleListener() {
 			public void bundleChanged(BundleEvent event) {
 				String bundleId = event.getBundle().getSymbolicName();
 				//System.out.println(event.getType() + " " + bundleId);
 				if (BundleEvent.STOPPED == event.getType() && ResourceManager.getInstance().isActiveBundle(bundleId))
 					stopBundle(bundleId);
 			}
 		};
 		context.addBundleListener(bundleListener);
 	}
 
 	protected void stopBundle(final String bundleId) {
 		class StopJob extends Job {
 			public StopJob() {
 				super("Disposing servers");
 			}
 			
 			public boolean belongsTo(Object family) {
 				return SHUTDOWN_JOB_FAMILY.equals(family);
 			}
 
 			public IStatus run(IProgressMonitor monitor2) {
 				ResourceManager.getInstance().shutdownBundle(bundleId);
 				return new Status(IStatus.OK, PLUGIN_ID, 0, "", null);
 			}
 		}
 		
 		StopJob job = new StopJob();
 		job.setUser(false);
 		job.schedule();
 	}
 
 	/**
 	 * @see Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		Trace.trace(Trace.CONFIG, "-----<----- Server Core plugin shutdown -----<-----");
 		super.stop(context);
 		
 		ResourceManager.shutdown();
 		ServerMonitorManager.shutdown();
 		
 		try {
 			Platform.getJobManager().join(SHUTDOWN_JOB_FAMILY, null);
 		} catch (Exception e) {
 			Trace.trace(Trace.WARNING, "Error waiting for shutdown job", e);
 		}
 		context.removeBundleListener(bundleListener);
 	}
 
 	public static String[] tokenize(String param, String delim) {
 		if (param == null)
 			return new String[0];
 		
 		List list = new ArrayList();
 		
 		StringTokenizer st = new StringTokenizer(param, delim);
 		while (st.hasMoreTokens()) {
 			String str = st.nextToken();
 			if (str != null && str.length() > 0)
 				list.add(str.trim());
 		}
 
 		String[] s = new String[list.size()];
 		list.toArray(s);
 		return s;
 	}
 
 	protected static List getModuleTypes(IConfigurationElement[] elements) {
 		List list = new ArrayList();
 		if (elements == null)
 			return list;
 	
 		int size = elements.length;
 		for (int i = 0; i < size; i++) {
 			String[] types = tokenize(elements[i].getAttribute("types"), ",");
 			String[] versions = tokenize(elements[i].getAttribute("versions"), ",");
 			int sizeT = types.length;
 			int sizeV = versions.length;
 			for (int j = 0; j < sizeT; j++) {
 				for (int k = 0; k < sizeV; k++) {
 					ModuleType module = new ModuleType(types[j], versions[k]);
 					list.add(module);
 				}
 			}
 		}
 		return list;
 	}
 	
 	public static String generateId() {
 		String s = df.format(new Date()).toString() + num++;
 		s = s.replace(' ', '_');
 		s = s.replace(':', '_');
 		s = s.replace('/', '_');
 		s = s.replace('\\', '_');
 		return s;
 	}
 
 	/**
 	 * Returns true if ids contains id.
 	 * 
 	 * @param ids
 	 * @param id
 	 * @return true if the id is supported
 	 */
 	public static boolean supportsType(String[] ids, String id) {
 		if (id == null || id.length() == 0)
 			return false;
 
 		if (ids == null)
 			return true;
 		
 		int size = ids.length;
 		for (int i = 0; i < size; i++) {
 			if (ids[i].endsWith("*")) {
 				if (id.length() >= ids[i].length() && id.startsWith(ids[i].substring(0, ids[i].length() - 1)))
 					return true;
 			} else if (id.equals(ids[i]))
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Recursively delete a directory.
 	 *
 	 * @param dir java.io.File
 	 * @param monitor a progress monitor, or <code>null</code> if no progress
 	 *    reporting is required
 	 */
 	public static void deleteDirectory(File dir, IProgressMonitor monitor) {
 		try {
 			if (!dir.exists() || !dir.isDirectory())
 				return;
 	
 			File[] files = dir.listFiles();
 			int size = files.length;
 			monitor = ProgressUtil.getMonitorFor(monitor);
 			monitor.beginTask(NLS.bind(Messages.deletingTask, new String[] {dir.getAbsolutePath()}), size * 10);
 	
 			// cycle through files
 			for (int i = 0; i < size; i++) {
 				File current = files[i];
 				if (current.isFile()) {
 					current.delete();
 					monitor.worked(10);
 				} else if (current.isDirectory()) {
 					monitor.subTask(NLS.bind(Messages.deletingTask, new String[] {current.getAbsolutePath()}));
 					deleteDirectory(current, ProgressUtil.getSubMonitorFor(monitor, 10));
 				}
 			}
 			dir.delete();
 			monitor.done();
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error deleting directory " + dir.getAbsolutePath(), e);
 		}
 	}
 	
 	/**
 	 * Returns an array of all known launchable adapters.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @return a possibly-empty array of launchable adapters {@link ILaunchableAdapter}
 	 */
 	public static ILaunchableAdapter[] getLaunchableAdapters() {
 		if (launchableAdapters == null)
 			loadLaunchableAdapters();
 		ILaunchableAdapter[] la = new ILaunchableAdapter[launchableAdapters.size()];
 		launchableAdapters.toArray(la);
 		return la;
 	}
 
 	/**
 	 * Returns an array of all known client instances.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @return a possibly-empty array of client instances {@link IClient}
 	 */
 	public static IClient[] getClients() {
 		if (clients == null)
 			loadClients();
 		IClient[] c = new IClient[clients.size()];
 		clients.toArray(c);
 		return c;
 	}
 	
 	/**
 	 * Load the launchable adapters extension point.
 	 */
 	private static synchronized void loadLaunchableAdapters() {
 		if (launchableAdapters != null)
 			return;
 		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .launchableAdapters extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ServerPlugin.PLUGIN_ID, "launchableAdapters");
 
 		int size = cf.length;
 		launchableAdapters = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				launchableAdapters.add(new LaunchableAdapter(cf[i]));
 				Trace.trace(Trace.EXTENSION_POINT, "  Loaded launchableAdapter: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load launchableAdapter: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .launchableAdapters extension point -<-");
 	}
 
 	/**
 	 * Load the client extension point.
 	 */
 	private static synchronized void loadClients() {
 		if (clients != null)
 			return;
 		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .clients extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ServerPlugin.PLUGIN_ID, "clients");
 
 		int size = cf.length;
 		clients = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				clients.add(new Client(cf[i]));
 				Trace.trace(Trace.EXTENSION_POINT, "  Loaded clients: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load clients: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .clients extension point -<-");
 	}
 	
 	/**
 	 * Returns an array of all known publish tasks.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @return a possibly-empty array of publish tasks instances {@link IPublishTask}
 	 */
 	public static IPublishTask[] getPublishTasks() {
 		if (publishTasks == null)
 			loadPublishTasks();
 		IPublishTask[] st = new IPublishTask[publishTasks.size()];
 		publishTasks.toArray(st);
 		return st;
 	}
 	
 	/**
 	 * Load the publish task extension point.
 	 */
 	private static synchronized void loadPublishTasks() {
 		if (publishTasks != null)
 			return;
 		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .publishTasks extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ServerPlugin.PLUGIN_ID, "publishTasks");
 
 		int size = cf.length;
 		publishTasks = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				publishTasks.add(new PublishTask(cf[i]));
 				Trace.trace(Trace.EXTENSION_POINT, "  Loaded publishTask: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load publishTask: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		
		sortOrderedList(publishTasks);
		
 		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .publishTasks extension point -<-");
 	}
 	
 	/**
 	 * Sort the given list of IOrdered items into indexed order.
 	 *
 	 * @param list java.util.List
 	 * @return java.util.List
 	 */
 	private static List sortOrderedList(List list) {
 		if (list == null)
 			return null;
 
 		int size = list.size();
 		for (int i = 0; i < size - 1; i++) {
 			for (int j = i + 1; j < size; j++) {
 				IOrdered a = (IOrdered) list.get(i);
 				IOrdered b = (IOrdered) list.get(j);
 				if (a.getOrder() > b.getOrder()) {
 					Object temp = a;
 					list.set(i, b);
 					list.set(j, temp);
 				}
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * Returns an array of all known module module factories.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @return the array of module factories {@link ModuleFactory}
 	 */
 	public static ModuleFactory[] getModuleFactories() {
 		if (moduleFactories == null)
 			loadModuleFactories();
 		
 		ModuleFactory[] mf = new ModuleFactory[moduleFactories.size()];
 		moduleFactories.toArray(mf);
 		return mf;
 	}
 	
 	/**
 	 * Returns the module factory with the given id, or <code>null</code>
 	 * if none. This convenience method searches the list of known
 	 * module factories ({@link #getModuleFactories()}) for the one a matching
 	 * module factory id ({@link ModuleFactory#getId()}). The id may not be null.
 	 *
 	 * @param id the module factory id
 	 * @return the module factory, or <code>null</code> if there is no module factory
 	 * with the given id
 	 */
 	public static ModuleFactory findModuleFactory(String id) {
 		if (id == null)
 			throw new IllegalArgumentException();
 
 		if (moduleFactories == null)
 			loadModuleFactories();
 		
 		Iterator iterator = moduleFactories.iterator();
 		while (iterator.hasNext()) {
 			ModuleFactory factory = (ModuleFactory) iterator.next();
 			if (id.equals(factory.getId()))
 				return factory;
 		}
 		return null;
 	}
 	
 	/**
 	 * Load the module factories extension point.
 	 */
 	private static synchronized void loadModuleFactories() {
 		if (moduleFactories != null)
 			return;
 		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .moduleFactories extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ServerPlugin.PLUGIN_ID, "moduleFactories");
 
 		int size = cf.length;
 		moduleFactories = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				moduleFactories.add(new ModuleFactory(cf[i]));
 				Trace.trace(Trace.EXTENSION_POINT, "  Loaded moduleFactories: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load moduleFactories: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		sortOrderedList(moduleFactories);
 		
 		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .moduleFactories extension point -<-");
 	}
 	
 	/**
 	 * Returns all projects contained by the server. This included the
 	 * projects that are in the configuration, as well as their
 	 * children, and their children...
 	 *
 	 * @param server a server
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @return a possibly-empty array of module instances {@link IModule}
 	 */
 	/*public static IModule[] getAllContainedModules(IServer server, IProgressMonitor monitor) {
 		//Trace.trace("> getAllContainedModules: " + getName(configuration));
 		List modules = new ArrayList();
 		if (server == null)
 			return new IModule[0];
 
 		// get all of the directly contained projects
 		IModule[] deploys = server.getModules();
 		if (deploys == null || deploys.length == 0)
 			return new IModule[0];
 
 		int size = deploys.length;
 		for (int i = 0; i < size; i++) {
 			if (deploys[i] != null && !modules.contains(deploys[i]))
 				modules.add(deploys[i]);
 		}
 
 		//Trace.trace("  getAllContainedModules: root level done");
 
 		// get all of the module's children
 		int count = 0;
 		while (count < modules.size()) {
 			IModule module = (IModule) modules.get(count);
 			try {
 				IModule[] children = server.getChildModules(module, monitor);
 				if (children != null) {
 					size = children.length;
 					for (int i = 0; i < size; i++) {
 						if (children[i] != null && !modules.contains(children[i]))
 							modules.add(children[i]);
 					}
 				}
 			} catch (Exception e) {
 				Trace.trace(Trace.SEVERE, "Error getting child modules for: " + module.getName(), e);
 			}
 			count ++;
 		}
 
 		//Trace.trace("< getAllContainedModules");
 
 		IModule[] modules2 = new IModule[modules.size()];
 		modules.toArray(modules2);
 		return modules2;
 	}*/
 	
 	/**
 	 * Returns an array of all known server monitor instances.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @return a possibly-empty array of server monitor instances {@link IServerMonitor}
 	 */
 	public static IServerMonitor[] getServerMonitors() {
 		if (monitors == null)
 			loadServerMonitors();
 		IServerMonitor[] sm = new IServerMonitor[monitors.size()];
 		monitors.toArray(sm);
 		return sm;
 	}
 	
 	/**
 	 * Load the server monitor extension point.
 	 */
 	private static synchronized void loadServerMonitors() {
 		if (monitors != null)
 			return;
 		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .serverMonitors extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ServerPlugin.PLUGIN_ID, "internalServerMonitors");
 
 		int size = cf.length;
 		monitors = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				monitors.add(new ServerMonitor(cf[i]));
 				Trace.trace(Trace.EXTENSION_POINT, "  Loaded serverMonitor: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load serverMonitor: " + cf[i].getAttribute("id"), t);
 			}
 		}
 	
 		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .serverMonitors extension point -<-");
 	}
 	
 	/**
 	 * Returns an array of all known runtime locator instances.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @return a possibly-empty array of runtime locator instances {@link IRuntimeLocator}
 	 */
 	public static IRuntimeLocator[] getRuntimeLocators() {
 		if (runtimeLocators == null)
 			loadRuntimeLocators();
 		IRuntimeLocator[] rl = new IRuntimeLocator[runtimeLocators.size()];
 		runtimeLocators.toArray(rl);
 		return rl;
 	}
 	
 	/**
 	 * Load the runtime locators.
 	 */
 	private static synchronized void loadRuntimeLocators() {
 		if (runtimeLocators != null)
 			return;
 		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .runtimeLocators extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ServerPlugin.PLUGIN_ID, "runtimeLocators");
 
 		int size = cf.length;
 		runtimeLocators = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				RuntimeLocator runtimeLocator = new RuntimeLocator(cf[i]);
 				runtimeLocators.add(runtimeLocator);
 				Trace.trace(Trace.EXTENSION_POINT, "  Loaded runtimeLocator: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load runtimeLocator: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		
 		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .runtimeLocators extension point -<-");
 	}
 	
 	/**
 	 * Returns an array of all module artifact adapters.
 	 *
 	 * @return a possibly empty array of module artifact adapters
 	 */
 	protected static ModuleArtifactAdapter[] getModuleArtifactAdapters() {
 		if (moduleArtifactAdapters == null)
 			loadModuleArtifactAdapters();
 		
 		ModuleArtifactAdapter[] moa = new ModuleArtifactAdapter[moduleArtifactAdapters.size()];
 		moduleArtifactAdapters.toArray(moa);
 		return moa;
 	}
 	
 	/**
 	 * Load the module artifact adapters extension point.
 	 */
 	private static synchronized void loadModuleArtifactAdapters() {
 		if (moduleArtifactAdapters != null)
 			return;
 		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .moduleArtifactAdapters extension point ->-");
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ServerPlugin.PLUGIN_ID, "moduleArtifactAdapters");
 
 		int size = cf.length;
 		moduleArtifactAdapters = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			try {
 				moduleArtifactAdapters.add(new ModuleArtifactAdapter(cf[i]));
 				Trace.trace(Trace.EXTENSION_POINT, "  Loaded moduleArtifactAdapter: " + cf[i].getAttribute("id"));
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "  Could not load moduleArtifactAdapter: " + cf[i].getAttribute("id"), t);
 			}
 		}
 		
 		// sort by index to put lower numbers first in order
 		size = moduleArtifactAdapters.size();
 		for (int i = 0; i < size-1; i++) {
 			for (int j = i+1; j < size; j++) {
 				ModuleArtifactAdapter a = (ModuleArtifactAdapter) moduleArtifactAdapters.get(i);
 				ModuleArtifactAdapter b = (ModuleArtifactAdapter) moduleArtifactAdapters.get(j);
 				if (a.getPriority() < b.getPriority()) {
 					moduleArtifactAdapters.set(i, b);
 					moduleArtifactAdapters.set(j, a);
 				}
 			}
 		}
 		
 		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .moduleArtifactAdapters extension point -<-");
 	}
 	
 	/**
 	 * Returns <code>true</code> if a module artifact may be available for the given object,
 	 * and <code>false</code> otherwise.
 	 *
 	 * @param obj an object
 	 * @return <code>true</code> if there is a module artifact adapter
 	 */
 	public static boolean hasModuleArtifact(Object obj) {
 		Trace.trace(Trace.FINEST, "ServerUIPlugin.hasModuleArtifact() " + obj);
 		ModuleArtifactAdapter[] adapters = getModuleArtifactAdapters();
 		if (adapters != null) {
 			int size = adapters.length;
 			for (int i = 0; i < size; i++) {
 				try {
 					if (adapters[i].isEnabled(obj)) {
 						Trace.trace(Trace.FINER, "Run On Server for " + obj + " is enabled by " + adapters[i].getId());
 						return true;
 					}
 				} catch (CoreException ce) {
 					Trace.trace(Trace.WARNING, "Could not use moduleArtifactAdapter", ce);
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Returns a module artifact if one can be found without loading plugins.
 	 * 
 	 * @param obj
 	 * @return a module artifact, or null
 	 */
 	public static IModuleArtifact getModuleArtifact(Object obj) {
 		Trace.trace(Trace.FINEST, "ServerUIPlugin.getModuleArtifact() " + obj);
 		ModuleArtifactAdapter[] adapters = getModuleArtifactAdapters();
 		if (adapters != null) {
 			int size = adapters.length;
 			for (int i = 0; i < size; i++) {
 				try {
 					if (adapters[i].isEnabled(obj)) {
 						IModuleArtifact ma = adapters[i].getModuleArtifact(obj);
 						if (ma != null)
 							return ma;
 						/*if (Platform.getAdapterManager().hasAdapter(obj, MODULE_ARTIFACT_CLASS)) {
 							return (IModuleArtifact) Platform.getAdapterManager().getAdapter(obj, MODULE_ARTIFACT_CLASS);
 						}*/
 					}
 				} catch (Exception e) {
 					Trace.trace(Trace.WARNING, "Could not use moduleArtifactAdapter " + adapters[i], e);
 				}
 			}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Returns a module artifact if possible, loading any plugins required.
 	 * 
 	 * @param obj
 	 * @return a module artifact, or null
 	 */
 	public static IModuleArtifact loadModuleArtifact(Object obj) {
 		Trace.trace(Trace.FINEST, "ServerUIPlugin.loadModuleArtifact() " + obj);
 		ModuleArtifactAdapter[] adapters = getModuleArtifactAdapters();
 		if (adapters != null) {
 			int size = adapters.length;
 			for (int i = 0; i < size; i++) {
 				try {
 					if (adapters[i].isEnabled(obj)) {
 						IModuleArtifact ma = adapters[i].getModuleArtifact(obj);
 						if (ma != null)
 							return ma;
 						/*if (Platform.getAdapterManager().hasAdapter(obj, MODULE_ARTIFACT_CLASS)) {
 							return (IModuleArtifact) Platform.getAdapterManager().loadAdapter(obj, MODULE_ARTIFACT_CLASS);
 						}*/
 					}
 				} catch (Exception e) {
 					Trace.trace(Trace.WARNING, "Could not use moduleArtifactAdapter " + adapters[i], e);
 				}
 			}
 		}
 		
 		return null;
 	}
 }
