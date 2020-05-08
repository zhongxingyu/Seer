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
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 
 import java.util.jar.JarFile;
 import java.util.jar.JarInputStream;
 import java.util.jar.Manifest;
 import java.util.zip.ZipEntry;
 
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
 
 	private static HashMap<String, PluginInfo> initializedPlugins;
 
 	private static URLClassLoader classLoader;
 
 	private static boolean usingWebstart;
 
 	private static String cyVersion = new CytoscapeVersion().getMajorVersion();
 
 	/**
 	 * Replaces CytoscapeInit.getClassLoader()
 	 * 
 	 * @return URLClassLoader used to load plugins.
 	 */
 	public static URLClassLoader getClassLoader() {
 		return classLoader;
 	}
 
 	/**
 	 * Replaces CytoscapeInit.getResourcePlugins()
 	 * 
 	 * @return Set<String> of resource plugins
 	 */
 	public static List<String> getResourcePlugins() {
 		return resourcePlugins;
 	}
 
 	/**
 	 * Replaces CytoscapeInit.getPluginURLs()
 	 * 
 	 * @return Set<URL> of plugin URL's
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
 			System.err.println("Directory not yet set up, can't delete");
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
 			System.err.println("USING WEBSTART: "
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
 		pluginMgr = null;
 	}
 
 	// create plugin manager
 	private PluginManager(PluginTracker Tracker) {
 		setWebstart();
 		try {
 			String trackerFileName = "track_plugins.xml";
 
 			if (tempDir == null) {
 				if (usingWebstartManager()) {
 					tempDir = new File(CytoscapeInit.getConfigDirectory(),
 							"webstart"
 									+ File.separator
 									+ (new CytoscapeVersion())
 											.getMajorVersion() + File.separator
 									+ "plugins");
 					;
 					this.removeWebstartInstalls();
 
 					trackerFileName = "track_webstart_plugins.xml";
 				} else {
 					tempDir = new File(CytoscapeInit
 							.getConfigVersionDirectory(), "plugins");
 				}
 			}
 
 			if (!tempDir.exists()) {
 				System.err.println("Creating directories for "
 						+ tempDir.getAbsolutePath());
 				if (!tempDir.mkdirs()) {
 					Cytoscape.exit(-1);
 				}
 			}
 
 			if (Tracker != null) {
 				pluginTracker = Tracker;
 			} else {
 				pluginTracker = new PluginTracker(tempDir.getParentFile(),
 						trackerFileName);
 			}
 		} catch (java.io.IOException E) {
 			E.printStackTrace(); // TODO do something useful with error
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
 		System.out.println("Registering " + Plugin.toString());
 
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
		System.err.println("     Registering " + Plugin.getClass().getName()
				+ " " + Jar.getName());
 		try {
 			PluginProperties pp = new PluginProperties(Plugin);
 			PluginObj = pp.fillPluginInfoObject(PluginObj);
 
 		} catch (IOException ioe) {
 			System.err.println("ERROR registering plugin: " + ioe.getMessage());
 			System.err
 					.println(Plugin.getClass().getName()
 							+ " loaded but not registered, this will not affect the operation of the plugin");
 		} catch (Exception e) {
 			System.err.println("ERROR registering plugin: ");
 			e.printStackTrace();
 		} finally {
 			if (PluginObj == null) { // still null, create a default one
 				PluginObj = new PluginInfo();
 				PluginObj.setCytoscapeVersion(cyVersion);
 				PluginObj.setName(Plugin.getClass().getName());
 				PluginObj.setObjectVersion(0.1);
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
 			if (addToTracker) {
 				System.out.println("--- Registering PLUGIN " + PluginObj.getName());
 				pluginTracker.addDownloadable(PluginObj, PluginStatus.CURRENT);
 			}
 		}
 		return PluginObj;
 	}
 
 	private void registerTheme(CytoscapePlugin Plugin, JarFile Jar,
 			ThemeInfo ThemeObj) {
 		System.out.println("--- Registering THEME " + ThemeObj.getName());
 		for (PluginInfo plugin : ThemeObj.getPlugins()) {
 			if (plugin.getPluginClassName().equals(Plugin.getClass().getName())) {
 				System.out.println(plugin.getName());
 				PluginInfo updatedPlugin = registerPlugin(Plugin, Jar, plugin, false);
 				ThemeObj.replacePlugin(plugin, updatedPlugin);
 			}
 		}
 		pluginTracker.addDownloadable(ThemeObj, PluginStatus.CURRENT);
 	}
 
 	// TODO would be better to fix how initializedPlugins are tracked...
 	private void cleanCurrentList() {
 		List<DownloadableInfo> CurrentList = this
 				.getDownloadables(PluginStatus.CURRENT);
 		for (DownloadableInfo info : CurrentList) {
 			if (info.getType().equals(DownloadableType.PLUGIN)) {
 				PluginInfo pInfo = (PluginInfo) info;
 				if (!initializedPlugins.containsKey(pInfo.getPluginClassName())) {
 					pluginTracker
 							.removeDownloadable(info, PluginStatus.CURRENT);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets all plugins on the "install" list to "current"
 	 */
 	public void install() {
 		for (DownloadableInfo info : this
 				.getDownloadables(PluginStatus.INSTALL)) {
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
 	 * occur at start up, CytoscapeInit should be the only class to call this.
 	 * 
 	 * @throws ManagerException
 	 *             If all files fail to delete
 	 * @throws WebstartException
 	 *             If this method is called from a webstart instance
 	 */
 	public void delete() throws ManagerException {
 		String ErrorMsg = "Failed to completely delete the following installed components:\n";
 		boolean deleteError = false;
 		List<DownloadableInfo> ToDelete = pluginTracker
 				.getDownloadableListByStatus(PluginStatus.DELETE);
 
 		for (DownloadableInfo infoObj : ToDelete) {
 			Installable ins = null;
 			switch (infoObj.getType()) {
 			case PLUGIN:
 				ins = new InstallablePlugin((PluginInfo) infoObj);
 				break;
 			case THEME:
 				ins = new InstallableTheme((ThemeInfo) infoObj);
 				break;
 			}
 
 			try {
 				if (ins.uninstall()) {
 					pluginTracker.removeDownloadable(infoObj,
 							PluginStatus.DELETE);
 					pluginTracker.removeDownloadable(infoObj,
 							PluginStatus.CURRENT);
 				} // TODO um.....XXXX
 			} catch (ManagerException me) {
 				deleteError = true;
 				ErrorMsg += infoObj.getName() + " v"
 						+ infoObj.getObjectVersion() + "\n";
 				me.printStackTrace();
 			}
 
 			if (deleteError) {
 				throw new ManagerException(ErrorMsg);
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
 		Installable ins = null;
 		switch (Info.getType()) {
 		case PLUGIN:
 			ins = new InstallablePlugin((PluginInfo) Info);
 			break;
 		case THEME:
 			ins = new InstallableTheme((ThemeInfo) Info);
 			break;
 		}
 		return ins.findUpdates();
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
 
 		Installable ins = null;
 		switch (currentObj.getType()) {
 		case PLUGIN:
 			ins = new InstallablePlugin((PluginInfo) currentObj);
 			break;
 		case THEME:
 			ins = new InstallableTheme((ThemeInfo) currentObj);
 			break;
 		}
 
 		ins.update(newObj, taskMonitor);
 
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
 
 		// run a check for plugins in a theme
 		List<DownloadableInfo> CurrentAndInstalled = new ArrayList<DownloadableInfo>();
 		CurrentAndInstalled.addAll(this.getDownloadables(PluginStatus.CURRENT));
 		CurrentAndInstalled.addAll(this.getDownloadables(PluginStatus.INSTALL));
 		for (DownloadableInfo info : CurrentAndInstalled) {
 			switch (Obj.getType()) {
 			case THEME: // check for other themes that include the same plugins
 				if (info.getType().equals(DownloadableType.THEME)) {
 					ThemeInfo Theme = (ThemeInfo) Obj;
 					for (PluginInfo plugin : Theme.getPlugins()) {
 						if (((ThemeInfo) info).containsPlugin(plugin)) {
 							throw new ManagerException(
 									Obj.getName()
 											+ " cannot be downloaded, it includes plugins that are installed in the theme '"
 											+ info.getName() + "'");
 						}
 					}
 				}
 				break;
 			case PLUGIN: // check for themes that include this plugin
 				if (info.getType().equals(DownloadableType.THEME)
 						&& ((ThemeInfo) info).containsPlugin((PluginInfo) Obj)) {
 					throw new ManagerException(Obj.getName()
 							+ " cannot be downloaded, the theme '"
 							+ info.getName()
 							+ "' includes a version of this plugin");
 				}
 				break;
 			}
 		}
 
 		Installable installable = null;
 		switch (Obj.getType()) {
 		case PLUGIN:
 			installable = new InstallablePlugin((PluginInfo) Obj);
 			break;
 		case THEME:
 			installable = new InstallableTheme((ThemeInfo) Obj);
 			break;
 		}
 		installable.install(taskMonitor);
 		pluginTracker.addDownloadable(Obj, PluginStatus.INSTALL);
 		return installable.getInfoObj();
 	}
 
 	/*
 	 * Methods for loading plugins when Cytoscape starts up. These have been
 	 * moved from CytoscapeInit
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
 	 * @throws MalformedURLException
 	 */
 	public void loadPlugin(PluginInfo p) throws MalformedURLException,
 			IOException, ClassNotFoundException, PluginException {
 		List<URL> ToLoad = new ArrayList<URL>();
 
 		for (String FileName : p.getFileList()) {
 			if (FileName.endsWith(".jar")) {
 				ToLoad.add(jarURL(FileName));
 			}
 		}
 		// don't need to register if we have the info object
 		loadURLPlugins(ToLoad, false);
 		if (duplicateLoadError) {
 			throwDuplicateError();
 		}
 
 	}
 
 	/**
 	 * Parses the plugin input strings and transforms them into the appropriate
 	 * URLs or resource names. The method first checks to see if the
 	 */
 	public void loadPlugins(List<String> p) throws MalformedURLException,
 			IOException, ClassNotFoundException, PluginException {
 
 		Set<String> PluginsSeen = new HashSet<String>();
 
 		// Parse the plugin strings and determine whether they're urls,
 		// files, directories, class names, or manifest file names.
 		for (String currentPlugin : p) {
 			if (PluginsSeen.contains(currentPlugin))
 				continue;
 
 			File f = new File(currentPlugin);
 
 			// If the file name ends with .jar add it to the list as a url.
 			if (currentPlugin.endsWith(".jar")) {
 				PluginsSeen.add(f.getAbsolutePath());
 
 				// If the name doesn't match a url, turn it into one.
 				if (!currentPlugin.matches(FileUtil.urlPattern)) {
 					System.out.println(" - file: " + f.getAbsolutePath());
 					pluginURLs.add(jarURL(f.getAbsolutePath()));
 				} else {
 					System.out.println(" - url: " + f.getAbsolutePath());
 					pluginURLs.add(jarURL(currentPlugin));
 				}
 			} else if (!f.exists()) {
 				// If the file doesn't exists, assume
 				// that it's a resource plugin.
 				System.out.println(" - classpath: " + currentPlugin);
 				resourcePlugins.add(currentPlugin);
 			} else if (f.isDirectory()) {
 				// If the file is a directory, load
 				// all of the jars in the directory.
 				System.out.println(" - directory: " + f.getAbsolutePath());
 
 				for (String fileName : f.list()) {
 					if (!fileName.endsWith(".jar")) {
 						continue;
 					}
 					PluginsSeen.add(f.getAbsolutePath()
 							+ System.getProperty("file.separator") + fileName);
 					pluginURLs.add(jarURL(f.getAbsolutePath()
 							+ System.getProperty("file.separator") + fileName));
 				}
 			} else {
 				// Assume the file is a manifest (i.e. list of jar names)
 				// and make urls out of them.
 				System.out.println(" - file manifest: " + f.getAbsolutePath());
 
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
 							System.err.println("Plugin location specified in "
 									+ currentPlugin + " is not a valid url: "
 									+ pluginLoc + " -- NOT adding it.");
 						}
 					}
 				}
 			}
 		}
 		// now load the plugins in the appropriate manner
 		loadURLPlugins(pluginURLs, true);
 		loadResourcePlugins(resourcePlugins);
 		cleanCurrentList();
 		if (duplicateLoadError) {
 			throwDuplicateError();
 		}
 	}
 
 	private void throwDuplicateError() throws PluginException {
 		String Msg = "The following plugins were not loaded due to duplicate class definitions:\n";
 		for (String dup : duplicateClasses)
 			Msg += "\t" + dup + "\n";
 		throw new PluginException(Msg);
 	}
 
 	/**
 	 * Load all plugins by using the given URLs loading them all on one
 	 * URLClassLoader, then interating through each Jar file looking for classes
 	 * that are CytoscapePlugins
 	 */
 	private void loadURLPlugins(List<URL> pluginUrls, boolean register)
 			throws IOException {
 		URL[] urls = new URL[pluginUrls.size()];
 		pluginUrls.toArray(urls);
 
 		duplicateClasses = new ArrayList<String>();
 		duplicateLoadError = false;
 
 		for (URL url : urls) {
 			try {
 				addClassPath(url);
 			} catch (Exception e) {
 				throw new IOException("Classloader Error.");
 			}
 		}
 
 		// the creation of the class loader automatically loads the plugins
 		classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
 
 		// iterate through the given jar files and find classes that are
 		// assignable from CytoscapePlugin
 		for (int i = 0; i < urls.length; ++i) {
 			System.out.println("");
 			System.out.println("attempting to load plugin url: ");
 			System.out.println(urls[i]);
 
 			JarURLConnection jc = (JarURLConnection) urls[i].openConnection();
 			// Ensure we are reading the real content from urls[i],
 			// and not some out-of-date cached content:
 			jc.setUseCaches(false);
 			JarFile jar = jc.getJarFile();
 
 			// if the jar file is null, do nothing
 			if (jar == null) {
 				continue;
 			}
 
 			// try to get class name from the manifest file
 			String className = getPluginClass(jar.getName(),
 					PluginInfo.FileType.JAR);
 
 			if (className != null) {
 				Class pc = getPluginClass(className);
 
 				if (pc != null) {
 					System.out.println("Loading from manifest");
 					// loadPlugin(pc, jar.getName(), register);
 					loadPlugin(pc, jar, register);
 					continue;
 				}
 			}
 
 			// new-school failed, so revert to old school. Search through the
 			// jar entries
 			Enumeration entries = jar.entries();
 
 			if (entries == null) {
 				System.out.println("Jar file " + jar.getName()
 						+ " has no entries");
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
 					// loadPlugin(pc, jar.getName(), register);
 					loadPlugin(pc, jar, register);
 					break;
 				}
 			}
 			if (totalPlugins == 0) {
 				System.out
 						.println("No plugin found in specified jar - assuming it's a library.");
 			}
 		}
 		System.out.println("");
 	}
 
 	// these are jars that *may or may not* extend CytoscapePlugin but may be
 	// used by jars that do
 	private void loadResourcePlugins(List<String> resourcePlugins)
 			throws ClassNotFoundException {
 		// attempt to load resource plugins
 		for (String resource : resourcePlugins) {
 			System.out.println("");
 			System.out.println("attempting to load plugin resourse: "
 					+ resource);
 
 			// try to get the class
 			Class rclass = null;
 			rclass = Class.forName(resource);
 			loadPlugin(rclass, null, true);
 		}
 		System.out.println("");
 	}
 
 	private void loadPlugin(Class plugin, JarFile jar, boolean register) {
 		if (CytoscapePlugin.class.isAssignableFrom(plugin)
 				&& !loadedPlugins.contains(plugin.getName())) {
 			try {
 				Object obj = CytoscapePlugin.loadPlugin(plugin);
 				if (obj != null) {
 					loadedPlugins.add(plugin.getName());
 					if (register) {
 						register((CytoscapePlugin) obj, jar);
 					}
 				}
 			} catch (InstantiationException inse) {
 				inse.printStackTrace();
 			} catch (IllegalAccessException ille) {
 				ille.printStackTrace();
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
 	private Class getPluginClass(String name) {
 		Class c = null;
 
 		try {
 			c = classLoader.loadClass(name);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 
 			return null;
 		} catch (NoClassDefFoundError e) {
 			e.printStackTrace();
 
 			return null;
 		}
 
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
 
 	/*
 	 * Iterate through all class files, return the subclass of CytoscapePlugin.
 	 * Similar to CytoscapeInit, however only plugins with manifest files that
 	 * describe the class of the CytoscapePlugin are valid.
 	 */
 	private String getPluginClass(String FileName, PluginInfo.FileType Type)
 			throws IOException {
 		String PluginClassName = null;
 
 		switch (Type) {
 		case JAR:
 			JarFile Jar = new JarFile(FileName);
 			PluginClassName = getManifestAttribute(Jar.getManifest());
 			Jar.close();
 			break;
 
 		case ZIP:
 			List<ZipEntry> Entries = ZipUtil
 					.getAllFiles(FileName, "\\w+\\.jar");
 			if (Entries.size() <= 0) {
 				String[] FilePath = FileName.split("/");
 				FileName = FilePath[FilePath.length - 1];
 				throw new IOException(
 						FileName
 								+ " does not contain any jar files or is not a zip file.");
 			}
 
 			for (ZipEntry Entry : Entries) {
 				String EntryName = Entry.getName();
 
 				InputStream is = ZipUtil.readFile(FileName, EntryName);
 				JarInputStream jis = new JarInputStream(is);
 				PluginClassName = getManifestAttribute(jis.getManifest());
 				jis.close();
 				is.close();
 			}
 		}
 		;
 		return PluginClassName;
 	}
 
 	/*
 	 * Gets the manifest file value for the Cytoscape-Plugin attribute
 	 */
 	private String getManifestAttribute(Manifest m) {
 		String Value = null;
 		if (m != null) {
 			Value = m.getMainAttributes().getValue("Cytoscape-Plugin");
 		}
 		return Value;
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
 
 	/**
 	 * @deprecated Use {@link cytoscape.plugin.PluginManagerInquireTask} will be
 	 *             removed June 2008
 	 */
 	private class InquireTask implements cytoscape.task.Task {
 
 		private String url;
 
 		private PluginInquireAction actionObj;
 
 		private cytoscape.task.TaskMonitor taskMonitor;
 
 		public InquireTask(String Url, PluginInquireAction Obj) {
 			url = Url;
 			actionObj = Obj;
 		}
 
 		public void setTaskMonitor(TaskMonitor monitor)
 				throws IllegalThreadStateException {
 			taskMonitor = monitor;
 		}
 
 		public void halt() {
 			// not implemented
 		}
 
 		public String getTitle() {
 			return "Attempting to connect to " + url;
 		}
 
 		public void run() {
 			List<DownloadableInfo> Results = null;
 
 			taskMonitor.setStatus(actionObj.getProgressBarMessage());
 			taskMonitor.setPercentCompleted(-1);
 
 			try {
 				Results = PluginManager.this.inquire(url);
 			} catch (Exception e) {
 
 				if (e.getClass().equals(java.lang.NullPointerException.class)) {
 					e = new org.jdom.JDOMException(
 							"XML was incorrectly formed", e);
 				}
 				actionObj.setExceptionThrown(e);
 			} finally {
 				taskMonitor.setPercentCompleted(100);
 				actionObj.inquireAction(Results);
 			}
 		}
 	}
 
 }
