 /*
  File: PluginManager.java 
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 
 package cytoscape.plugin;
 
 import cytoscape.*;
 
 import cytoscape.util.FileUtil;
 import cytoscape.util.ZipUtil;
 import cytoscape.task.TaskMonitor;
 import cytoscape.logger.CyLogger;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.JarURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ArrayList; // import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 
 import java.util.jar.JarFile;
 import java.util.jar.JarInputStream;
 import java.util.jar.Manifest;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 /**
  * @author skillcoy
  * 
  */
 public class PluginManager {
 	protected PluginTracker pluginTracker;
 
 	private boolean duplicateLoadError;
 
 	private List<String> duplicateClasses;
 
 	private static PluginManager pluginMgr = null;
 
 	private static File tempDir;
 
 	private static List<java.net.URL> pluginURLs;
 
 	private static List<String> resourcePlugins;
 
 	private static Set<String> loadedPlugins;
 
 	private static Set<Throwable> loadingErrors;
 
 	private static HashMap<String, PluginInfo> initializedPlugins;
 
 	private static URLClassLoader classLoader;
 
 	private static boolean usingWebstart;
 
 	private static String cyVersion = new CytoscapeVersion().getMajorVersion();
 
 	private static CyLogger logger = CyLogger.getLogger(PluginManager.class);
 
 	/**
 	/**
 	 * Returns list of loading exceptions.
 	 */
 	public List<Throwable> getLoadingErrors() {
 		if (pluginTracker.hasCorruptedElements()) {
 			loadingErrors
 					.add(new TrackerException(
 							"Corrupted elements removed from the Plugin Tracker.  Some plugins may need to be reinstalled."));
 		}
 		return new ArrayList<Throwable>(loadingErrors);
 	}
 
 	/**
 	 * Clears the loading error list. Ideally this should be called after
 	 * checking the list each time.
 	 */
 	public void clearErrorList() {
 		pluginTracker.clearCorruptedElements();
 		loadingErrors.clear();
 	}
 
 	/**
 	 * @return URLClassLoader used to load plugins at startup.
 	 */
 	public static URLClassLoader getClassLoader() {
 		return classLoader;
 	}
 
 	/**
 	 * @return Set<String> of resource plugins from startup
 	 */
 	public static List<String> getResourcePlugins() {
 		return resourcePlugins;
 	}
 
 	/**
 	 * @return Set<URL> of plugin URL's from startup
 	 */
 	public static List<java.net.URL> getPluginURLs() {
 		return pluginURLs;
 	}
 
 	/**
 	 * Returns true/false based on the System property. This is what is checked
 	 * to find out if install/delete/download methods are permitted.
 	 * 
 	 * @return true if Cytoscape is in webstart
 	 */
 	public static boolean usingWebstartManager() {
 		return usingWebstart;
 	}
 
 	/**
 	 * Deletes everything under the webstart install directory. Nothing in here
 	 * should stick around.
 	 * 
 	 * @return True if all files deleted successfully
 	 */
 	protected boolean removeWebstartInstalls() {
 		if (tempDir == null) {
 			logger.warn("Directory not yet set up, can't delete");
 			return false;
 		}
 		return recursiveDeleteFiles(tempDir.getParentFile());
 	}
 
 	/**
 	 * Get the PluginManager object.
 	 * 
 	 * @return PluginManager
 	 */
 	public static PluginManager getPluginManager() {
 		if (pluginMgr == null) {
 			pluginMgr = new PluginManager(null);
 		}
 		return pluginMgr;
 	}
 
 	/**
 	 * @param loc
 	 *            Location of plugin download/install directory. If this method
 	 *            is not called the default is .cytoscape/[cytoscape
 	 *            version]/plugins
 	 */
 	public static void setPluginManageDirectory(String loc) {
 		tempDir = new File(loc);
 		if (!tempDir.getAbsolutePath().contains(cyVersion)) {
 			tempDir = new File(tempDir, cyVersion);
 		}
 	}
 
 	/**
 	 * 
 	 * @return The current version directory under .cytoscape that includes the
 	 *         plugins/ directory.
 	 * 
 	 * Ex. /<user dir>/.cytoscape/2.6/plugins
 	 */
 	public File getPluginManageDirectory() {
 		return tempDir;
 	}
 
 	/*
 	 * Just checks the system property 'javawebstart.version' which is only set
 	 * when running as a webstart.
 	 */
 	private static void setWebstart() {
 		if (System.getProperty("javawebstart.version") != null
 				&& System.getProperty("javawebstart.version").length() > 0) {
 			logger.info("USING WEBSTART: "
 					+ System.getProperty("javawebstart.version"));
 			usingWebstart = true;
 		} else {
 			usingWebstart = false;
 		}
 	}
 
 	/**
 	 * This should ONLY be used by tests!!
 	 * 
 	 * @param Tracker
 	 * @return
 	 */
 	protected static PluginManager getPluginManager(PluginTracker Tracker) {
 		if (pluginMgr == null) {
 			pluginMgr = new PluginManager(Tracker);
 		}
 		return pluginMgr;
 	}
 
 	/**
 	 * This is used in testing to isolate each test case. DO NOT USE THIS IN
 	 * CYTOSCAPE RUNTIME CODE
 	 */
 	protected void resetManager() {
 		if (pluginTracker != null && pluginMgr != null) {
 			pluginTracker.delete();
 			pluginTracker = null;
 			recursiveDeleteFiles(tempDir);
 			pluginMgr = null;
 		}
 	}
 
 	// create plugin manager
 	private PluginManager(PluginTracker Tracker) {
 		// XXX is this needed anymore?
 		loadingErrors = new HashSet<Throwable>();
 
 		setWebstart();
 		String trackerFileName = "track_plugins.xml";
 
 		if (tempDir == null) {
 			if (usingWebstartManager()) {
 				tempDir = new File(CytoscapeInit.getConfigDirectory(),
 						"webstart" + File.separator
 								+ (new CytoscapeVersion()).getMajorVersion()
 								+ File.separator + "plugins");
 				removeWebstartInstalls();
 				trackerFileName = "track_webstart_plugins.xml";
 			} else {
 				tempDir = new File(CytoscapeInit.getConfigVersionDirectory(), "plugins");
 			}
 		} else if (!tempDir.getAbsolutePath().endsWith("/plugins")) {
 			tempDir = new File(tempDir, "plugins");
 		}
 
 		if (!tempDir.exists()) {
 			logger.info("Creating directories for "
 					+ tempDir.getAbsolutePath());
 			if (!tempDir.mkdirs()) {
 				Cytoscape.exit(-1);
 			}
 		}
 
 		if (Tracker != null) {
 			pluginTracker = Tracker;
 		} else {
 			try {
 				pluginTracker = new PluginTracker(tempDir.getParentFile(),
 						trackerFileName);
 			} catch (IOException ioe) {
 				// ioe.printStackTrace();
 				loadingErrors.add(ioe);
 			} catch (TrackerException te) {
 				// te.printStackTrace();
 				loadingErrors.add(te);
 			} finally { // document should be cleaned out by now
 				try {
 					pluginTracker = new PluginTracker(tempDir.getParentFile(),
 							trackerFileName);
 				} catch (Exception e) {
 					logger.warn("Unable to read plugin tracking file", e);
 					// this could go on forever, surely there's a better way!
 				}
 			}
 		}
 		pluginURLs = new ArrayList<java.net.URL>();
 		loadedPlugins = new HashSet<String>();
 		initializedPlugins = new HashMap<String, PluginInfo>();
 		resourcePlugins = new ArrayList<String>();
 	}
 
 	/**
 	 * Get a list of downloadable objects by status. CURRENT: currently
 	 * installed INSTALL: objects to be installed DELETE: objects to be deleted
 	 * 
 	 * @param Status
 	 * @return
 	 */
 	public List<DownloadableInfo> getDownloadables(PluginStatus Status) {
 		return pluginTracker.getDownloadableListByStatus(Status);
 	}
 
 	/**
 	 * Calls the given url, expects document describing plugins available for
 	 * download
 	 * 
 	 * @param Url
 	 * @return List of PluginInfo objects
 	 */
 	public List<DownloadableInfo> inquire(String Url) throws IOException,
 			org.jdom.JDOMException {
 		List<DownloadableInfo> infoObjs = null;
 		PluginFileReader Reader = new PluginFileReader(Url);
 		infoObjs = Reader.getDownloadables();
 		return infoObjs;
 	}
 
 	/**
 	 * Registers a currently installed plugin with tracking object. Only useful
 	 * if the plugin was not installed via the install process.
 	 * 
 	 * @param Plugin
 	 * @param JarFileName
 	 */
 	protected void register(CytoscapePlugin Plugin, JarFile Jar) {
 		logger.info("Registering " + Plugin.toString());
 
 		DownloadableInfo InfoObj = ManagerUtil.getInfoObject(Plugin.getClass());
 		if (InfoObj != null && InfoObj.getType().equals(DownloadableType.THEME)) {
 			this.registerTheme(Plugin, Jar, (ThemeInfo) InfoObj);
 		} else {
 			this.registerPlugin(Plugin, Jar, (PluginInfo) InfoObj, true);
 		}
 	}
 
 	private PluginInfo registerPlugin(CytoscapePlugin Plugin, JarFile Jar,
 			PluginInfo PluginObj, boolean addToTracker) {
 		// try to get it from the file
 		// XXX PROBLEM: what to do about a plugin that attempts to register
 		// itself and is not compatible with the current version?
 		logger.info("     Registering " + Plugin.getClass().getName());
 		try {
 			PluginProperties pp = new PluginProperties(Plugin);
 			PluginObj = pp.fillPluginInfoObject(PluginObj);
 
 		} catch (IOException ioe) {
 			logger.warn("ERROR registering plugin: " + ioe.getMessage(), ioe);
 			logger.warn(Plugin.getClass().getName()
 							+ " loaded but not registered, this will not affect the operation of the plugin");
 		} catch (Exception e) {
 			logger.warn("ERROR registering plugin: ", e);
 		} finally {
 			if (PluginObj == null) { // still null, create a default one
 				PluginObj = new PluginInfo();
 				PluginObj.addCytoscapeVersion(cyVersion);
 				PluginObj.setName(Plugin.getClass().getName());
 				PluginObj.setObjectVersion("0.1");
 			}
 
 			PluginObj.setPluginClassName(Plugin.getClass().getName());
 			if (!usingWebstart && Jar != null) {
 				PluginObj.setInstallLocation(Jar.getName());
 				PluginObj.addFileName(Jar.getName());
 			}
 			PluginObj.setFiletype(PluginInfo.FileType.JAR);
 
 			initializedPlugins.put(PluginObj.getPluginClassName(), PluginObj);
 			// TODO This causes a bug where theme plugins essentially get added
 			// to the current list twice
 			logger.info("Track plugin: " + addToTracker);
 			if (addToTracker) {
 				pluginTracker.addDownloadable(PluginObj, PluginStatus.CURRENT);
 			}
 		}
 		return PluginObj;
 	}
 
 	private void registerTheme(CytoscapePlugin Plugin, JarFile Jar,
 			ThemeInfo ThemeObj) {
 		logger.info("--- Registering THEME " + ThemeObj.getName());
 		for (PluginInfo plugin : ThemeObj.getPlugins()) {
 			if (plugin.getPluginClassName().equals(Plugin.getClass().getName())) {
 				logger.info(plugin.getName());
 				PluginInfo updatedPlugin = registerPlugin(Plugin, Jar, plugin,
 						false);
 				ThemeObj.replacePlugin(plugin, updatedPlugin);
 			}
 		}
 		pluginTracker.addDownloadable(ThemeObj, PluginStatus.CURRENT);
 	}
 
 	// TODO would be better to fix how initializedPlugins are tracked...
 	private void cleanCurrentList() {
 		List<DownloadableInfo> CurrentList = getDownloadables(PluginStatus.CURRENT);
 		for (DownloadableInfo info : CurrentList) {
 			if (info.getType().equals(DownloadableType.PLUGIN)) {
 				PluginInfo pInfo = (PluginInfo) info;
 				if (!initializedPlugins.containsKey(pInfo.getPluginClassName())) {
 					pluginTracker.removeDownloadable(info, PluginStatus.CURRENT);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets all plugins on the "install" list to "current"
 	 */
 	public void install() {
 		for (DownloadableInfo info : getDownloadables(PluginStatus.INSTALL)) {
 			install(info);
 		}
 	}
 
 	/**
 	 * Change the given downloadable object from "install" to "current" status
 	 * 
 	 * @param obj
 	 */
 	public void install(DownloadableInfo obj) {
 		pluginTracker.removeDownloadable(obj, PluginStatus.INSTALL);
 		pluginTracker.addDownloadable(obj, PluginStatus.CURRENT);
 
 		// mark all webstart-installed plugins for deletion
 		if (usingWebstartManager()) {
 			pluginTracker.addDownloadable(obj, PluginStatus.DELETE);
 		}
 	}
 
 	/**
 	 * Marks the given object for deletion the next time Cytoscape is restarted.
 	 * 
 	 * @param Obj
 	 */
 	public void delete(DownloadableInfo Obj) throws WebstartException {
 		checkWebstart();
 		pluginTracker.addDownloadable(Obj, PluginStatus.DELETE);
 	}
 
 	/**
 	 * Takes all objects on the "to-delete" list and deletes them. This can only
 	 * occur at start up.
 	 * 
 	 * @throws ManagerException
 	 *             If all files fail to delete
 	 * @throws WebstartException
 	 *             If this method is called from a webstart instance
 	 */
 	public void delete() throws ManagerException {
 		List<DownloadableInfo> toDelete = pluginTracker.getDownloadableListByStatus(
 		                                                             PluginStatus.DELETE);
 
 		for (DownloadableInfo infoObj : toDelete) {
 			Installable ins = infoObj.getInstallable();
 
 			try {
 				if (ins.uninstall()) {
 					pluginTracker.removeDownloadable(infoObj, PluginStatus.DELETE);
 					pluginTracker.removeDownloadable(infoObj, PluginStatus.CURRENT);
 				} // TODO um.....XXXX
 			} catch (Exception me) {
 				throw new ManagerException( 
 				          "Failed to completely delete the following installed components:\n" + 
 						  infoObj.getName() + " v" + infoObj.getObjectVersion() + "\n", me);
 			}
 		}
 	}
 
 	protected static boolean recursiveDeleteFiles(File file) {
 		if (file.isDirectory())
 			for (File f : file.listFiles())
 				recursiveDeleteFiles(f);
 
 		boolean del = file.delete();
 		// Utterly f*#king retarded, but apparently necessary since sometimes
 		// directories don't realize they're empty...
 		if (!del) {
 			for (int i = 0; i < 1000 && file.exists(); i++) {
 				System.gc();
 				del = file.delete();
 			}
 		}
 
 		return del;
 	}
 
 	private void checkWebstart() throws WebstartException {
 		if (usingWebstart) {
 			throw new WebstartException();
 		}
 	}
 
 	/**
 	 * Get list of plugins that would update the given plugin.
 	 * 
 	 * @param Info
 	 * @return List<PluginInfo>
 	 * @throws ManagerException
 	 */
 	public List<DownloadableInfo> findUpdates(DownloadableInfo Info)
 			throws IOException, org.jdom.JDOMException {
 		return Info.getInstallable().findUpdates();
 	}
 
 	/**
 	 * Finds the given version of the new object, sets the old object for
 	 * deletion and downloads new object to temporary directory
 	 * 
 	 * @param Current
 	 *            DownloadableInfo object currently installed
 	 * @param New
 	 *            DownloadableInfo object to install
 	 * @throws IOException
 	 *             Fails to download the file.
 	 * @throws ManagerException
 	 *             If the objects don't match or the new one is not a newer
 	 *             version.
 	 */
 	public void update(DownloadableInfo Current, DownloadableInfo New)
 			throws IOException, ManagerException, WebstartException {
 		update(Current, New, null);
 	}
 
 	/**
 	 * Finds the given version of the new object, sets the old object for
 	 * deletion and downloads new object to temporary directory
 	 * 
 	 * @param Current
 	 *            PluginInfo object currently installed
 	 * @param New
 	 *            PluginInfo object to install
 	 * @param taskMonitor
 	 *            TaskMonitor for downloads
 	 * @throws IOException
 	 *             Fails to download the file.
 	 * @throws ManagerException
 	 *             If the plugins don't match or the new one is not a newer
 	 *             version.
 	 */
 	public void update(DownloadableInfo currentObj, DownloadableInfo newObj,
 			cytoscape.task.TaskMonitor taskMonitor) throws IOException,
 			ManagerException, WebstartException {
 
 		if (!currentObj.getType().equals(newObj.getType())) {
 			throw new ManagerException(
 					"Cannot update an object of one download type to an object of a different download type");
 		}
 		currentObj.getInstallable().update(newObj, taskMonitor);
 
 		pluginTracker.addDownloadable(currentObj, PluginStatus.DELETE);
 		pluginTracker.addDownloadable(newObj, PluginStatus.INSTALL);
 	}
 
 	/**
 	 * Downloads given object to the temporary directory.
 	 * 
 	 * @param Obj
 	 *            PluginInfo object to be downloaded
 	 * @return File downloaded
 	 */
 	public DownloadableInfo download(DownloadableInfo Obj) throws IOException,
 			ManagerException {
 		return this.download(Obj, null);
 	}
 
 	/**
 	 * Downloads given object to the temporary directory. Uses a task monitor if
 	 * available.
 	 * 
 	 * @param Obj
 	 *            PluginInfo object to be downloaded
 	 * @param taskMonitor
 	 *            TaskMonitor
 	 * @param tempDirectory
 	 *            Download to a different temporary directory. Default is
 	 *            .cytoscape/plugins/[cytoscape version number]
 	 * @return File downloaded
 	 */
 	public DownloadableInfo download(DownloadableInfo Obj,
 			TaskMonitor taskMonitor) throws IOException, ManagerException {
 		// run a check for plugins 
 		List<DownloadableInfo> CurrentAndInstalled = new ArrayList<DownloadableInfo>();
 		CurrentAndInstalled.addAll(this.getDownloadables(PluginStatus.CURRENT));
 		CurrentAndInstalled.addAll(this.getDownloadables(PluginStatus.INSTALL));
 		
 		List<DownloadableInfo> FlattenedList = this.flattenDownloadableList(CurrentAndInstalled);
 		
 		for (DownloadableInfo currentlyInstalled : FlattenedList) {
 			DownloadableInfo CurrentlyInstalled = null;
 			if (currentlyInstalled.getParent() != null) {
 				CurrentlyInstalled = currentlyInstalled.getParent();
 			} else {
 				CurrentlyInstalled = currentlyInstalled;
 			}
 				
 			if (Obj.equals(currentlyInstalled) || Obj.equalsDifferentObjectVersion(currentlyInstalled)) {
 				throw new ManagerException(Obj.toString() + " cannot be installed, it is already loaded in: " + CurrentlyInstalled.toString());
 			}
 				
 			if (Obj.getType().equals(DownloadableType.THEME)) {
 				for (PluginInfo themePlugin: ((ThemeInfo) Obj).getPlugins()) {
 					if (themePlugin.equalsDifferentObjectVersion(currentlyInstalled)) {
 						throw new ManagerException(Obj.toString() + " cannot be installed a plugin contained within the theme is already present: " 
 								+ CurrentlyInstalled.toString());
 					}
 				}
 			}
 		}
 
 		Installable installable = Obj.getInstallable();
 		installable.install(taskMonitor);
 		pluginTracker.addDownloadable(Obj, PluginStatus.INSTALL);
 		return installable.getInfoObj();
 	}
 
 	private List<DownloadableInfo> flattenDownloadableList(List<DownloadableInfo> list) {
 		List<DownloadableInfo> FlattenedList = new ArrayList<DownloadableInfo>();
 		for (DownloadableInfo info: list) {
 			switch (info.getType()) {
 			case THEME:
 				FlattenedList.addAll(((ThemeInfo) info).getPlugins());
 			case PLUGIN:
 				FlattenedList.add(info);
 			}
 		}
 		return FlattenedList;
 	}
 	
 	/*
 	 * Methods for loading plugins when Cytoscape starts up.
 	 */
 	public void loadPlugin(DownloadableInfo i) throws MalformedURLException,
 			IOException, ClassNotFoundException, PluginException {
 		switch (i.getType()) {
 		case PLUGIN:
 			loadPlugin((PluginInfo) i);
 			break;
 		case THEME:
 			ThemeInfo Info = (ThemeInfo) i;
 			for (PluginInfo p : Info.getPlugins())
 				loadPlugin(p);
 			break;
 		case FILE: // currently there is no FileInfo type
 			break;
 		}
 	}
 
 	/**
 	 * Load a single plugin based on the PluginInfo object given
 	 * 
 	 * @param PluginInfo
 	 *            The plugin to load
 	 * @throws ManagerException
 	 */
 	public void loadPlugin(PluginInfo p) throws ManagerException {
 		List<URL> ToLoad = new ArrayList<URL>();
 
 		for (String FileName : p.getFileList()) {
 			if (FileName.endsWith(".jar")) {
 				try {
 					ToLoad.add(jarURL(FileName));
 				} catch (MalformedURLException mue) {
 					// mue.printStackTrace();
 					loadingErrors.add(mue);
 				}
 			}
 		}
 		// don't need to register if we have the info object
 		InstallablePlugin insp = new InstallablePlugin(p);
 		loadURLPlugins(ToLoad, false);
 
 		if (duplicateLoadError) {
 			insp.uninstall();
 			pluginTracker.removeDownloadable(p, PluginStatus.CURRENT);
 			addDuplicateError();
 		}
 
 	}
 
 	/**
 	 * Load a single plugin based on the File object given
 	 * 
 	 * @param plugin
 	 *            The plugin to load
 	 * @throws MalformedURLException
 	 */
 	public void loadPlugin(File plugin) throws MalformedURLException, PluginException {
 		String fileName = plugin.getAbsolutePath();
 		if (fileName.endsWith(".jar")) {
 			try {
 				String className = JarUtil.getPluginClass(fileName,
 							PluginInfo.FileType.JAR);
 
 				// See if we already have this className registered
 				try {
 					Class pluginClass = getPluginClass(className); // Get the plugin class
 					// If this succeeded, we're in trouble -- this class is already
 					// registered
 					throw new PluginException("Duplicate class name: "+className+".  \n"+
 					                          "You may need to delete a previously installed plugin.");
 				} catch (ClassNotFoundException cnfe1) {
 					// This is what we want....
 				}
 
 				// We don't want to register because we're going to play a little
 				// fast and loose with the uniqueID, so we need to contruct things
 				// ourselves
 
 				try {
 					addClassPath(jarURL(fileName));
 
 					// OK, now hand-craft the registration
 					Class pluginClass = getPluginClass(className); // Get the plugin class
 					Object obj = CytoscapePlugin.loadPlugin(pluginClass);
 					PluginInfo base = new PluginInfo(plugin.getName(), className);
 					registerPlugin((CytoscapePlugin)obj, new JarFile(plugin), base, true);
 				} catch (Throwable t) {
 					throw new PluginException("Classloader Error: " + jarURL(fileName), t);
 				}
 			} catch (IOException ioe) {
 				throw new PluginException("Unable to read plugin jar: "+ioe.getMessage(), ioe);
 			}
 
 		}
 
 		if (duplicateLoadError) {
 			addDuplicateError();
 		}
 	}
 
 	/**
 	 * Parses the plugin input strings and transforms them into the appropriate
 	 * URLs or resource names. The method first checks to see if the
 	 */
 	public void loadPlugins(List<String> p) {
 		Set<String> PluginsSeen = new HashSet<String>();
 
 		// Parse the plugin strings and determine whether they're urls,
 		// files, directories, class names, or manifest file names.
 		for (String currentPlugin : p) {
 			try {
 				if (PluginsSeen.contains(currentPlugin))
 					continue;
 
 				if (currentPlugin.contains(".cytoscape")) {
 					logger.info(currentPlugin);
 				}
 
 				File f = new File(currentPlugin);
 
 				// If the file name ends with .jar add it to the list as a url.
 				if (currentPlugin.endsWith(".jar")) {
 					PluginsSeen.add(f.getAbsolutePath());
 
 					// If the name doesn't match a url, turn it into one.
 					if (!currentPlugin.matches(FileUtil.urlPattern)) {
 						logger.info(" - file: " + f.getAbsolutePath());
 						pluginURLs.add(jarURL(f.getAbsolutePath()));
 					} else {
 						logger.info(" - url: " + f.getAbsolutePath());
 						pluginURLs.add(jarURL(currentPlugin));
 					}
 				} else if (!f.exists()) {
 					// If the file doesn't exists, assume
 					// that it's a resource plugin.
 					logger.info(" - classpath: " + currentPlugin);
 					resourcePlugins.add(currentPlugin);
 				} else if (f.isDirectory()) {
 					// If the file is a directory, load
 					// all of the jars in the directory.
 					logger.info(" - directory: " + f.getAbsolutePath());
 
 					for (String fileName : f.list()) {
 						if (!fileName.endsWith(".jar")) {
 							continue;
 						}
 						PluginsSeen.add(f.getAbsolutePath()
 								+ System.getProperty("file.separator")
 								+ fileName);
 						pluginURLs.add(jarURL(f.getAbsolutePath()
 								+ System.getProperty("file.separator")
 								+ fileName));
 					}
 				} else {
 					// Assume the file is a manifest (i.e. list of jar names)
 					// and make urls out of them.
 					logger.info(" - file manifest: "
 							+ f.getAbsolutePath());
 
 					String text = FileUtil.getInputString(currentPlugin);
 
 					String[] allLines = text.split(System
 							.getProperty("line.separator"));
 					for (String pluginLoc : allLines) {
 						if (pluginLoc.endsWith(".jar")) {
 							PluginsSeen.add(pluginLoc);
 							if (pluginLoc.matches(FileUtil.urlPattern)) {
 								pluginURLs.add(jarURL(pluginLoc));
 							} else {
 								// TODO this should have a better error
 								// perhaps, throw an exception??
 								logger.warn("Plugin location specified in "
 												+ currentPlugin
 												+ " is not a valid url: "
 												+ pluginLoc
 												+ " -- NOT adding it.");
 								loadingErrors.add(new PluginException(
 										"Plugin location specified in "
 												+ currentPlugin
 												+ " is not a valid url: "
 												+ pluginLoc
 												+ " -- NOT adding it."));
 							}
 						}
 					}
 				}
 			// Catching Throwable because Errors (e.g. NoClassDefFoundError) could 
 			// cause Cytoscape to crash, which plugins should definitely not do.  
 			} catch (Throwable t) {
 				loadingErrors.add(new PluginException("problem loading plugin: "+currentPlugin,t));
 				t.printStackTrace();
 			}
 		}
 		// now load the plugins in the appropriate manner
 		loadURLPlugins(pluginURLs, true);
 		loadResourcePlugins(resourcePlugins);
 
 		cleanCurrentList();
 		if (duplicateLoadError)
 			addDuplicateError();
 	}
 
 	private void addDuplicateError() {
 		String Msg = "The following plugins were not loaded due to duplicate class definitions:\n";
 		for (String dup : duplicateClasses)
 			Msg += "\t" + dup + "\n";
 		logger.warn(Msg);
 		loadingErrors.add(new DuplicatePluginClassException(Msg));
 	}
 
 	/**
 	 * Load all plugins by using the given URLs loading them all on one
 	 * URLClassLoader, then interating through each Jar file looking for classes
 	 * that are CytoscapePlugins
 	 */
 	private void loadURLPlugins(List<URL> pluginUrls, boolean register) {
 		URL[] urls = new URL[pluginUrls.size()];
 		pluginUrls.toArray(urls);
 
 		duplicateClasses = new ArrayList<String>();
 		duplicateLoadError = false;
 
 		for (URL url : urls) {
 			try {
 				addClassPath(url);
 			// Catching Throwable because Errors (e.g. NoClassDefFoundError) could 
 			// cause Cytoscape to crash, which plugins should definitely not do.  
 			} catch (Throwable t) {
 				loadingErrors.add(new PluginException("Classloader Error: " + url, t));
 			}
 		}
 
 		// the creation of the class loader automatically loads the plugins
 		if ( usingWebstartManager() )
 			// Note: For the case of websatart, we should use the following statement to get classLoader
 			// The URLs will be a list of URLs pointed to the jars at source website. This may solve the 
 			//Class not found exception, because webstart does not have access to the local jar files
 			// in the class path.
 			classLoader = (URLClassLoader)this.getClass().getClassLoader();
 			//classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader(); 
 		else
 			classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
 	
 		
 		// iterate through the given jar files and find classes that are
 		// assignable from CytoscapePlugin
 		for (int i = 0; i < urls.length; ++i) {
 
 			try {
 				logger.info("attempting to load plugin url: "+urls[i]);
 
 				JarURLConnection jc = (JarURLConnection) urls[i]
 						.openConnection();
 				// Ensure we are reading the real content from urls[i],
 				// and not some out-of-date cached content:
 				jc.setUseCaches(false);
 				JarFile jar = jc.getJarFile();
 
 				// if the jar file is null, do nothing
 				if (jar == null) {
 					continue;
 				}
 
 				// try to get class name from the manifest file
				String className = JarUtil.getPluginClass(jar.getName(),
						PluginInfo.FileType.JAR);
 
 				if (className != null) {
 					Class pc = getPluginClass(className);
 
 					if (pc != null) {
 						logger.info("Loading from manifest");
 						loadPlugin(pc, jar, register);
 						continue;
 					}
 				}
 
 				// new-school failed, so revert to old school. Search through
 				// the jar entries
 				Enumeration entries = jar.entries();
 
 				if (entries == null) {
 					loadingErrors.add(new PluginException("Jar file "
 							+ jar.getName()
 							+ " has no entries, skipped loading."));
 					logger.warn("Jar file "
 							+ jar.getName()
 							+ " has no entries, skipped loading.");
 					continue;
 				}
 
 				int totalPlugins = 0;
 				while (entries.hasMoreElements()) {
 					// get the entry
 					String entry = entries.nextElement().toString();
 
 					if (entry.endsWith("class")) {
 						// convert the entry to an assignable class name
 						entry = entry.replaceAll("\\.class$", "");
 						// A regex to match the two known types of file
 						// separators. We can't use File.separator because
 						// the system the jar was created on is not
 						// necessarily the same is the one it is running on.
 						entry = entry.replaceAll("/|\\\\", ".");
 
 						Class pc = getPluginClass(entry);
 						if (pc == null) {
 							continue;
 						}
 						totalPlugins++;
 						loadPlugin(pc, jar, register);
 						break;
 					}
 				}
 				if (totalPlugins == 0) {
 					logger.info("No plugin found in specified jar - assuming it's a library.");
 				}
 			// Catching Throwable because Errors (e.g. NoClassDefFoundError) could 
 			// cause Cytoscape to crash, which plugins should definitely not do.  
 			} catch (Throwable t) {
 				loadingErrors.add(new PluginException("problem loading plugin URL: " + urls[i], t));
 				t.printStackTrace();
 			}
 		}
 	}
 
 	// these are jars that *may or may not* extend CytoscapePlugin but may be
 	// used by jars that do
 	private void loadResourcePlugins(List<String> resourcePlugins) {
 		// attempt to load resource plugins
 		for (String resource : resourcePlugins) {
 			logger.info("attempting to load plugin resourse: " + resource);
 
 			// try to get the class
 			try {
 				Class rclass = Class.forName(resource);
 				loadPlugin(rclass, null, true);
 			// Catching Throwable because Errors (e.g. NoClassDefFoundError) could 
 			// cause Cytoscape to crash, which plugins should definitely not do.  
 			} catch (Throwable t) {
 				loadingErrors.add(new PluginException("problem loading plugin resource: " + resource, t));
 			}
 		}
 	}
 
 	private void loadPlugin(Class plugin, JarFile jar, boolean register)
 			throws PluginException {
 		if (CytoscapePlugin.class.isAssignableFrom(plugin)
 				&& !loadedPlugins.contains(plugin.getName())) {
 
 			Object obj = CytoscapePlugin.loadPlugin(plugin);
 			if (obj != null) {
 				loadedPlugins.add(plugin.getName());
 				if (register) {
 					register((CytoscapePlugin) obj, jar);
 				}
 			}
 
 		} else if (loadedPlugins.contains(plugin.getName())) {
 			duplicateClasses.add(plugin.getName());
 			duplicateLoadError = true;
 		}
 	}
 
 	/**
 	 * Determines whether the class with a particular name extends
 	 * CytoscapePlugin by attempting to load the class first.
 	 * 
 	 * @param name
 	 *            the name of the putative plugin class
 	 */
 	private Class getPluginClass(String name) throws ClassNotFoundException,
 			NoClassDefFoundError {
 		Class c;
 		if ( usingWebstartManager() )
 			c = Class.forName(name,false,classLoader);
 		else
 			c = classLoader.loadClass(name);
 
 		if (CytoscapePlugin.class.isAssignableFrom(c))
 			return c;
 		else
 			return null;
 	}
 
 	// creates a URL object from a jar file name
 	private static URL jarURL(String urlString) throws MalformedURLException {
 		String uString;
 		if (urlString.matches(FileUtil.urlPattern)) {
 			uString = "jar:" + urlString + "!/";
 		} else {
 			uString = "jar:file:" + urlString + "!/";
 		}
 		return new URL(uString);
 	}
 
 	/**
 	 * This will be used to add plugin jars' URL to the System Loader's
 	 * classpath.
 	 * 
 	 * @param url
 	 * @throws NoSuchMethodException
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 */
 	private void addClassPath(URL url) throws NoSuchMethodException,
 			IllegalAccessException, InvocationTargetException {
 		Method method = URLClassLoader.class.getDeclaredMethod("addURL",
 				new Class[] { URL.class });
 		method.setAccessible(true);
 		method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { url });
 	}
 }
