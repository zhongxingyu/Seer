 /*******************************************************************************
  * Copyright (c) 2001, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jem.internal.proxy.core;
 /*
  *  $RCSfile: ProxyPlugin.java,v $
 *  $Revision: 1.47 $  $Date: 2005/03/01 15:32:29 $ 
  */
 
 
 import java.io.*;
 import java.net.URL;
 import java.util.*;
 import java.util.logging.Level;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.*;
 import org.eclipse.jdt.core.*;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.osgi.service.resolver.BundleSpecification;
 import org.osgi.framework.*;
 
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.util.logger.proxyrender.EclipseLogger;
 
 
 /**
  * The plugin class for the org.eclipse.jem.internal.proxy.core plugin.
  */
 
 public class ProxyPlugin extends Plugin {
 	
 	/**
 	 * This interface is for a listener that needs to know if this plugin (ProxyPlugin) is being shutdown. 
 	 * It is needed because there are some extensions that get added dynamically that need to know when the
 	 * plugin is being shutdown. Can't use new bundle listener for this because it notifies AFTER shutdown.
 	 * 
 	 * @since 1.0.0
 	 */
 	public interface IProxyPluginShutdownListener {
 		/**
 		 * ProxyPlugin is in shutdown.
 		 * 
 		 * @since 1.0.0
 		 */
 		public void shutdown();
 	}
 	
 	private static ProxyPlugin PROXY_PLUGIN = null;
 	public static final String PREFERENCES_VM_NOVERIFY_KEY = "JEM_PREFERENCES_VM_NOVERIFY_KEY"; // Key for NOVERIFY option of VM
 		
 	// If this is set to true, then in development mode and it will try for proxy jars in directories.
 	private boolean devMode;
 	
 	private ListenerList shutdownListeners;
 
 	public ProxyPlugin() {
 		super();
 		PROXY_PLUGIN = this;
 		devMode = Platform.inDevelopmentMode();  	
 	}
 
 	/**
 	 * Access the singleton
 	 * @return the singleton plugin
 	 * 
 	 * @since 1.0.0
 	 */
 	public static ProxyPlugin getPlugin() {
 		return PROXY_PLUGIN;
 	}
 	
 	private Logger logger;
 	public Logger getLogger() {
 		if (logger == null)
 			logger = EclipseLogger.getEclipseLogger(this);
 		return logger;
 	}	
 
 	/**
 	 * This will take the bundle and file name and make it local and return that
 	 * fully qualified. It will look in fragments, but only returns first found. If there can be multiples use
 	 * the one for bundles and it fragments.
 	 * <p>
 	 * If we are in development and it will pick it up from the path
 	 * that is listed in the proxy.jars file located in the bundle passed in. This allows development code to be
 	 * used in place of the actual runtime jars. If the runtime jars are found,
 	 * they will be used.
 	 * <p>
 	 * For example if looking for file runtime/xyz.jar in bundle abc, then in bundle directory for abc,
 	 * there should be a file called proxy.jars. This should only be in development, this file should not
 	 * be distributed for production. It would be distributed in the SDK environment when testing is desired.
 	 * <p>
 	 * The format of the file is:
 	 * 	runtimefile=/projectname/builddirectory
 	 * <p>
 	 * For this to work when the actual jar is not found, the Eclipse must of been started in 
 	 * dev mode (i.e. the bundle location will be a project within the developer Eclipse. That way
 	 * we can go up one level for the current install location and assume the above projectname
 	 * will be found relative to the directory.
 	 * <p>
 	 * For the above example:
 	 * 	runtime/xyz.jar=/xyzproject/bin
 	 * <p>
 	 * It will return "." if file can't be found. It means nothing but it won't cause jvm to crash.
 	 * 
 	 * @param bundle
 	 * @param filenameWithinBundle
 	 * @return the path to the file or <code>"."</code> if not found.
 	 * 
 	 * @since 1.0.0
 	 */
 	public String localizeFromBundleOnly(Bundle bundle, String filenameWithinBundle) {
 		URL url = urlLocalizeFromBundleOnly(bundle, filenameWithinBundle);
 		return url != null ? getFileFromURL(url) : "."; //$NON-NLS-1$
 	}
 	public String localizeFromBundleAndFragments(Bundle bundle, String filenameWithinBundle) {
 		URL url = urlLocalizeFromBundleAndFragments(bundle, filenameWithinBundle);
 		return url != null ? getFileFromURL(url) : "."; //$NON-NLS-1$
 	}
 	
 	/**
 	 * Just like localizeFromBundle except it will return an array of Strings. It will look for the filename
 	 * within the bundle and any fragments of the bundle. If none are found, an empty array will be returned.
 	 * <p>
 	 * To find the files in the fragments that are in the runtime path (i.e. libraries), it will need to use a suffix,
 	 * This is because the JDT will get confused if a runtime jar in a fragment has the same name
 	 * as a runtime jar in the main bundle.
 	 * NOTE: This is obsolete. JDT no longer has this problem. So we can find libraries in fragments that have the
 	 * same file path. 
 	 * <p>
 	 * So we will use the following search pattern:
 	 * <ol>
 	 * <li>Find in all of the fragments those that match the name exactly in the same paths if paths are supplied.</li>
 	 * <li>Find in all of the fragments, in their runtime path (library stmt), those that match the name 
 	 *    but have a suffix the same as the uniqueid of the fragment (preceeded by a period). This is so that it can be easily
 	 *    found but yet be unique in the entire list of fragments. For example if looking for "runtime/xyz.jar"
 	 *    and we have fragment "a.b.c.d.frag", then in the runtime path we will look for the file
 	 *    "runtime/xyz.a.b.c.d.frag.jar". Note: This is obsolete. Still here only for possible old code. Will go 
 	 *    away in future.</li>
 	 * <p>
 	 * If the files in the fragments are not in the fragments library path then it can have the same name. NOTE: Obsolete,
 	 * JDT can now handle same name.
 	 * <p>
 	 * This is useful for nls where the nls for the filename will be in one or more of the fragments of the plugin.
 	 * 
 	 * @param bundle
 	 * @param filenameWithinBundle
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	public String[] localizeAllFromBundleAndFragments(Bundle bundle, String filenameWithinBundle) {
 		URL[] urls = urlLocalizeAllFromBundleAndFragments(bundle, filenameWithinBundle);
 		String[] result = new String[urls.length];
 		for (int i = 0; i < urls.length; i++) {
 			result[i] = getFileFromURL(urls[i]);
 		}
 		return result;
 	}
 	
 	private static String getFileFromURL(URL url) {
 		// We need to do this in a device independent way. The URL will always put a leading '/' in the
 		// file part of the URL, but on Windows we need to have this '/' removed. Some JRE's don't understand it.
 		return new File(url.getFile()).getAbsolutePath();
 
 	}
 
 	public URL urlLocalizeFromBundleAndFragments(Bundle bundle, String filenameWithinBundle) {
 		return urlLocalizeFromBundleAndFragments(bundle, new Path(filenameWithinBundle));
 	}
 	public URL urlLocalizeFromBundleAndFragments(Bundle bundle, IPath filenameWithinBundle) {
 		try {
 			URL pvm = Platform.find(bundle, filenameWithinBundle);
 			if (pvm != null)
 				return Platform.asLocalURL(pvm);
 		} catch (IOException e) {
 		}
 		if (devMode) {
 			URL[] urls = findDevAllFromBundleAndFragments(bundle, filenameWithinBundle.toString());
 			if (urls.length > 0)
 				return urls[0];
 			else
 				return null;
 		} else
 			return null;
 	}
 	
 	/**
 	 * Like <code>localizeFromBundleAndFragments</code> except it returns URL's instead.
 	 * 
 	 * @param bundle
 	 * @param filenameWithinBundle
 	 * @return
 	 * 
 	 * @see ProxyPlugin#localizeFromBundleAndFragments(Bundle, String)
 	 * @since 1.0.0
 	 */
 	public URL[] urlLocalizeAllFromBundleAndFragments(Bundle bundle, String filenameWithinBundle) {
 		return urlLocalizeAllBundleAndFragments(bundle, Platform.getFragments(bundle), filenameWithinBundle);
 	}
 	public URL[] urlLocalizeAllFromBundleAndFragments(Bundle bundle, IPath filenameWithinBundle) {
 		return urlLocalizeAllBundleAndFragments(bundle, Platform.getFragments(bundle), filenameWithinBundle.toString());
 	}
 	
 	/**
 	 * @param bundle
 	 * @param filenameWithinBundle
 	 * @param fragments
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	private URL[] urlLocalizeAllBundleAndFragments(Bundle bundle, Bundle[] fragments, String filenameWithinBundle) {
 
 		ArrayList urls = new ArrayList((fragments == null ? 0 : fragments.length) + 1);
 		URL url = internalUrlLocalizeFromBundleOnly(bundle, filenameWithinBundle);
 		if (url != null)
 			urls.add(url);
 		if (fragments != null) {
 			for (int i = 0; i < fragments.length; i++) {
 				Bundle fragment = fragments[i];
 				url = internalUrlLocalizeFromBundleOnly(fragment, filenameWithinBundle);
 				if (url != null)
 					urls.add(url);
 			}
 		}
 		return (URL[]) urls.toArray(new URL[urls.size()]);
 	}
 
 	private static final String PROXYJARS = "proxy.jars";	//$NON-NLS-1$
 	
 	/**
 	 * @see ProxyPlugin#localizeFromBundle(Bundle, String)
 	 * 
 	 * This is just a helper to return a url instead.
 	 * 
 	 * @param bundle
 	 * @param filenameWithinBundle
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	public URL urlLocalizeFromBundleOnly(Bundle bundle, String filenameWithinBundle) {
 		// If the filenameWithinBundle begins with one of these special characters,
 		// it might be in a fragment.
 		if (filenameWithinBundle.charAt(0) == '$'
 				&& (filenameWithinBundle.regionMatches(true, 0, "$nl$", 0, "$nl$".length())
 						|| filenameWithinBundle.regionMatches(true, 0, "$os$", 0, "$os$".length()) || filenameWithinBundle.regionMatches(true, 0,
 						"$ws$", 0, "$ws$".length())))
 			return urlLocalizeFromBundleAndFragments(bundle, filenameWithinBundle);
 		try {
 			URL pvm = new URL(bundle.getEntry("/"), filenameWithinBundle);
 			pvm = verifyFound(Platform.asLocalURL(pvm));
 			if (pvm != null)
 				return pvm;
 		} catch (IOException e) {
 		}
 		return findDev(bundle, filenameWithinBundle);
 
 	}
 	
 	protected URL internalUrlLocalizeFromBundleOnly(Bundle bundle, String filenameWithinBundle) {
 		try {
 			URL pvm = bundle.getEntry(filenameWithinBundle);
 			if (pvm != null)
 				return Platform.asLocalURL(pvm);
 		} catch (IOException e) {
 		}
 		return findDev(bundle, filenameWithinBundle);		
 		
 	}	
 	
 	private URL verifyFound(URL pvm) throws IOException {
 		if (devMode) {
 			// Need to test if found in devmode. Otherwise we will just assume it is found. If not found on remote and moved to cache, an IOException would be thrown.
 			if (pvm != null) {
 				InputStream ios = null;
 				try {
 					ios = pvm.openStream();
 					if (ios != null)
 						return pvm; // Found it, so return it.
 				} finally {
 					if (ios != null)
 						ios.close();
 				}
 			}
 		} else
 			return pvm;
 		return null;
 	}
 
 	/**
 	 * @see ProxyPlugin#localizeFromBundle(bundle, String)
 	 * 
 	 * This is just a helper to return a url instead.
 	 * 
 	 * @param bundle
 	 * @param filenameWithinBundle
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	public URL urlLocalizeFromBundleOnly(Bundle bundle, IPath filenameWithinBundle) {					
 		return urlLocalizeFromBundleOnly(bundle, filenameWithinBundle.toString());
 	}
 	
 	private URL findDev(Bundle bundle, String filenameWithinBundle) {
 		if (devMode) {
 			// Got this far and in dev mode means it wasn't found, so we'll try for development style.
 			// It is assumed that in dev mode, we are running with the IDE as local and any 
 			// build outputs will be local so local file protocol will be returned
 			// from Platform.resolve(). We won't be running in dev mode with our entireplugin being in a jar,
 			// or on a separate system.
 			try {
 				URL pvm = bundle.getEntry(PROXYJARS);
 				if (pvm != null) {
 					InputStream ios = null;
 					try {
 						ios = pvm.openStream();
 						Properties props = new Properties();
 						props.load(ios);
 						String pathString = props.getProperty(filenameWithinBundle.toString());
 						if (pathString != null) {
 							URL url = Platform.resolve(bundle.getEntry("/"));	// It is assumed that if in debug mode, then this plugin is an imported plugin within the developement workspace.
 							if (url.getProtocol().equals("file")) {
 								File file = new File(url.getFile()).getParentFile();	// This gets us to workspace root of development workspace.
 								file = new File(file, pathString);
 								return file.toURL();
 							}
 						}
 					} finally {
 						if (ios != null)
 							ios.close();
 					}
 				}
 			} catch (IOException e) {
 			}
 		}
 		
 		return null;
 
 	}
 	private URL[] findDevAllFromBundleAndFragments(Bundle bundle, String filenameWithinBundle) {
 		Bundle [] fragments = Platform.getFragments(bundle);
 		ArrayList urls = new ArrayList((fragments == null ? 0 : fragments.length) + 1);
 		URL url = findDev(bundle, filenameWithinBundle);
 		if (url != null)
 			urls.add(url);
 		if (fragments != null) {
 			for (int i = 0; i < fragments.length; i++) {
 				Bundle fragment = fragments[i];
 				url = findDev(fragment, filenameWithinBundle);
 				if (url != null)
 					urls.add(url);
 			}
 		}
 		return (URL[]) urls.toArray(new URL[urls.size()]);
 	}	
 	/**
 	 * A helper to order the plugins into pre-req order. 
 	 * If A eventually depends on B, then B will be ahead of A in the
 	 * list of plugins. (I.e. B is a pre-req somewhere of A).
 	 *  
 	 * @param bundlesToOrder - Bundles of interest. The results will have these in thiee correct order.
 	 * @return An array of the Bundlers in there order from no prereqs in set to the leaves.
 	 * 
 	 * @since 1.0.0
 	 */
 	public static Bundle[] orderPlugins(final Set bundlesToOrder) {
 		Map prereqsMap = new HashMap(bundlesToOrder.size()*3);
 		int ndx = bundlesToOrder.size();
 		Bundle[] result = new Bundle[ndx];
 		Map dependents = getDependentCounts(false, bundlesToOrder, prereqsMap);	// We want the inactive ones too. That way have complete order. They can be ignored later if necessary.
 		// keep iterating until all have been visited. This will actually find them in reverse order from what we
 		// want, i.e. it will find the leaves first. So we will build result array in reverse order.
 		while (!dependents.isEmpty()) {
 			// loop over the dependents list.  For each entry, if there are no dependents, visit
 			// the bundle and remove it from the list.  Make a copy of the keys so we don't end up
 			// with concurrent accesses (since we are deleting the values as we go)
 			Iterator ib = dependents.entrySet().iterator();
 			while (ib.hasNext()) {
 				Map.Entry entry = (Map.Entry) ib.next();
 				Bundle bundle = (Bundle) entry.getKey() ;
 				int[] count = (int[]) entry.getValue();
 				if (count != null && count[0] <= 0) {
 					if (bundlesToOrder.contains(bundle)) {
 						result[--ndx] = bundle;
 						if (ndx == 0)
 							return result;	// We've ordered all that we care about. Anything left over is unimportant.
 					}
 					ib.remove();
 					// decrement the dependent count for all of the prerequisites.
 					Bundle[] requires = getPrereqs(bundle, prereqsMap);
 					for (int j = 0; j < requires.length; j++) {
 						Bundle prereq = requires[j];
 						int[] countPrereq = (int[]) dependents.get(prereq);
 						if (countPrereq != null)
 							--countPrereq[0];
 					}
 				}
 			}
 		}		
 		return result;
 	}
 	
 	/**
 	 * Get all of the prereqs for this bundle, all of the way down to the root.
 	 * They will be in top-down depth-first order. There won't be duplicates. They will show up
 	 * only once the first time they are found.
 	 * 
 	 * @param bundle
 	 * @return list of all pre-reqs.
 	 * 
 	 * @since 1.0.0
 	 */
 	public static List getAllPrereqs(Bundle bundle) {
 		List prereqs = new ArrayList();
 		getAllPrereqs(bundle, prereqs, new HashMap());
 		return prereqs;
 	}
 	
 	private static void getAllPrereqs(Bundle bundle, List prereqs, Map prereqsMap) {
 		Bundle[] prs = getPrereqs(bundle, prereqsMap);
 		for (int i = 0; i < prs.length; i++) {
 			Bundle pre = prs[i];
 			if (prereqsMap.containsKey(pre))
 				continue;	// Already processed this one once.
 			prereqs.add(pre);	// Add to the list of pre-reqs accumulated so far.
 			getAllPrereqs(pre, prereqs, prereqsMap);
 		}
 	}
 	
 	private static Bundle[] getPrereqs(Bundle bundle, Map prereqsMap) {
 		Bundle[] prereqs = (Bundle[]) prereqsMap.get(bundle);
 		if (prereqs == null) {
 			prereqs = getPrereqs(bundle);
 			prereqsMap.put(bundle, prereqs);
 		}
 		return prereqs;
 	}
 	
 	public static Bundle[] getPrereqs(Bundle bundle) {
 		Bundle[] l = (Bundle[]) pluginRequiredMap.get(bundle.getSymbolicName());
 		if (l == null) {
 			BundleSpecification specs[] = Platform.getPlatformAdmin().getState(false).getBundle(bundle.getBundleId()).getRequiredBundles();
 			ArrayList bundles = new ArrayList(specs.length);
 			for (int i = 0; i < specs.length; i++) {
 				Bundle b = Platform.getBundle(specs[i].getName());
 				if (b != null)
 					bundles.add(b);
 			}
 			l = (Bundle[]) bundles.toArray(new Bundle[bundles.size()]);
 			pluginRequiredMap.put(bundle.getSymbolicName(), l);
 		}
 		return l;
 	}
 	
 	private static Map getDependentCounts(boolean activeOnly, Set startingSet, Map prereqsMap) {
 		Map dependents = new HashMap(startingSet.size());
 		// build a table of all dependent counts.  The table is keyed by descriptor and
 		// the value the integer number of dependent plugins.
 		List processNow = new ArrayList(startingSet);
 		List processNext = new ArrayList(processNow.size());
 		if (!processNow.isEmpty()) {
 			// Go through the first time from the starting set to get an entry into the list.
 			// If there is an entry, then it won't be marked for processNext. Only new entries
 			// are added to processNext in the following loop.
 			int pnSize = processNow.size();
 			for (int i = 0; i < pnSize; i++) {
 				Bundle bundle = (Bundle) processNow.get(i);
 				if (activeOnly && bundle.getState() != Bundle.ACTIVE)
 					continue;
 				// ensure there is an entry for this descriptor (otherwise it will not be visited)
 				int[] entry = (int[]) dependents.get(bundle);
 				if (entry == null)
 					dependents.put(bundle, new int[1]);
 			}
 		}
 		
 		// Now process the processNow to find the requireds, increment them, and add to processNext if never found before.
 		while (!processNow.isEmpty()) {
 			processNext.clear();
 			int pnSize = processNow.size();
 			for (int i = 0; i < pnSize; i++) {
 				Bundle bundle = (Bundle) processNow.get(i);
 				if (activeOnly && bundle.getState() != Bundle.ACTIVE)
 					continue;			
 				Bundle[] requires = getPrereqs(bundle, prereqsMap);
 				for (int j = 0; j < requires.length; j++) {
 					Bundle prereq = requires[j];
 					if (prereq == null || activeOnly
 							&& bundle.getState() != Bundle.ACTIVE)
 						continue;
 					int[] entry = (int[]) dependents.get(prereq);
 					if (entry == null) {
 						dependents.put(prereq, new int[]{1});
 						processNext.add(prereq); // Never processed before, so we add it to the next process loop.
 					} else
 						++entry[0];
 				}
 			}
 			
 			// Now swap the lists so that we processNext will be now and visa-versa.
 			List t = processNext;
 			processNext = processNow;
 			processNow = t;
 		}
 		return dependents;
 	}
 		
 	/**
 	 * Add a shutdown listener
 	 * @param listener
 	 * 
 	 * @since 1.0.0
 	 */
 	public void addProxyShutdownListener(IProxyPluginShutdownListener listener) {
 		if (shutdownListeners == null)
 			shutdownListeners = new ListenerList();
 		shutdownListeners.add(listener);
 	}
 
 	/**
 	 * Remove a shutdown listener
 	 * @param listener
 	 * 
 	 * @since 1.0.0
 	 */
 	public void removeProxyShutdownListener(IProxyPluginShutdownListener listener) {
 		if (shutdownListeners != null)
 			shutdownListeners.remove(listener);
 	}
 	
 	private ILaunchConfigurationListener launchListener = new ILaunchConfigurationListener() {
 		public void launchConfigurationAdded(ILaunchConfiguration configuration) {
 			try {
 				if (!configuration.isWorkingCopy() && IProxyConstants.ID_PROXY_LAUNCH_GROUP.equals(configuration.getCategory()))
 					startCleanupJob();
 			} catch (Exception e) {
 			}
 		}
 
 		public void launchConfigurationChanged(ILaunchConfiguration configuration) {
 			try {
 				if (!configuration.isWorkingCopy() && IProxyConstants.ID_PROXY_LAUNCH_GROUP.equals(configuration.getCategory()))
 					startCleanupJob();
 			} catch (Exception e) {
 			}
 		}
 
 		public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
 			try {
 				// On delete you can't tell the category or anything because all of that info has already removed.
 				if (!configuration.isWorkingCopy())
 					startCleanupJob();
 			} catch (Exception e) {
 			}
 		}
 	};
 	
 	private Job cleanupJob = new Job(ProxyMessages.getString("ProxyPlugin.CleanupDefaultProxyLaunchConfigurations")) { //$NON-NLS-1$
 		{
 			setSystem(true);	// So it doesn't show up in progress monitor. No need to interrupt user.
 			setPriority(Job.SHORT);	// A quick running job.
 		}
 		protected IStatus run(IProgressMonitor monitor) {
 			synchronized (this) {
 				if (monitor.isCanceled())
 					return Status.CANCEL_STATUS;
 			}
 			// all we want to do is find out if any launch configurations (from proxy launch group) exist for
 			// a project. If they don't, then unset the project's property. If they do, and the property is not
 			// set, then set it to NOT_SET to indicate not set, but there are some configs for it.
 			// We just gather the project names that have launch configurations.
 			try {
 				Set projectNames = new HashSet();
 				ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
 				for (int i = 0; i < configs.length; i++) {
 					if (IProxyConstants.ID_PROXY_LAUNCH_GROUP.equals(configs[i].getCategory())
 						&& (ProxyLaunchSupport.ATTR_PRIVATE == null || !configs[i].getAttribute(ProxyLaunchSupport.ATTR_PRIVATE, false)))
 						projectNames.add(configs[i].getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""));
 				}
 
 				IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
 				IJavaElement[] children = model.getChildren();
 				int cancelCount = 10;
 				for (int j = 0; j < children.length; j++) {
 					if (children[j].getElementType() == IJavaElement.JAVA_PROJECT) {
 						if (--cancelCount <= 0)
 							synchronized (this) {
 								cancelCount = 10;	// Rest for next set of ten.
 								// Checking on every 10 projects because they may be many projects, while only few configs.
 								// This way it will stop sooner.
 								if (monitor.isCanceled())
 									return Status.CANCEL_STATUS;
 							}						
 						IProject p = ((IJavaProject) children[j]).getProject();
 						if (projectNames.contains(p.getName())) {
 							// This project has a launch config. If it has a setting, then do nothing, else need to put on not set. 
 							if (p.getPersistentProperty(ProxyPlugin.PROPERTY_LAUNCH_CONFIGURATION) == null)
 								p.getProject().setPersistentProperty(
 									ProxyPlugin.PROPERTY_LAUNCH_CONFIGURATION,
 									ProxyLaunchSupport.NOT_SET);
 						} else {
 							// This project has no launch configs. Remove any setting if it exists.
 							p.setPersistentProperty(ProxyPlugin.PROPERTY_LAUNCH_CONFIGURATION, (String) null);
 						}
 					}
 				}
 				return Status.OK_STATUS;
 			} catch (CoreException e) {
 				return e.getStatus();
 			}
 		}
 	};
 	
 	private void startCleanupJob() {
 		cleanupJob.cancel();	// Stop what we are doing.
 		cleanupJob.schedule(1000l);	// Schedule to start in one second.
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(launchListener);
 		context.addBundleListener(new BundleListener() {
 			public void bundleChanged(BundleEvent event) {
 				if (event.getBundle() != ProxyPlugin.this.getBundle())
 					return;	// Not of interest to us.
 				switch (event.getType()) {
 					case BundleEvent.STARTED:
 						context.removeBundleListener(this);	// Since we don't care anymore
 						startCleanupJob();
 						break;
 					case BundleEvent.STOPPED:
 					case BundleEvent.UNINSTALLED:
 					case BundleEvent.UNRESOLVED:
 						context.removeBundleListener(this);	// We stopped before we started, so remove ourselves.
 						break;
 				}
 			}
 		});
 		getPluginPreferences().setDefault(PREFERENCES_VM_NOVERIFY_KEY, true);
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		// Handle case where debug plugin shuts down before we do since order not guarenteed.
 		if (DebugPlugin.getDefault() != null)
 			DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(launchListener);
 		cleanupJob.cancel();	// Stop what we are doing.		
 		if (shutdownListeners != null) {
 			Object[] listeners = shutdownListeners.getListeners();
 			for (int i = 0; i < listeners.length; i++) {
 				((IProxyPluginShutdownListener) listeners[i]).shutdown();
 			}
 		}		
 		super.stop(context);
 	}
 	
 	public static final String PI_CONFIGURATION_CONTRIBUTION_EXTENSION_POINT = "org.eclipse.jem.proxy.contributors"; //$NON-NLS-1$
 	public static final String PI_CONTAINER = "container"; //$NON-NLS-1$
 	public static final String PI_PLUGIN = "plugin"; //$NON-NLS-1$
 	public static final String PI_CLASS = "class"; //$NON-NLS-1$
 	public static final Map pluginRequiredMap = new HashMap(50);
 	
 	/*
 	 * Map of container id's to their ordered array of contribution config elements.
 	 */
 	protected Map containerToContributions = null;
 	/*
 	 * Map of plugin id's to their ordered array of contribution config elements.
 	 */
 	protected Map pluginToContributions = null;
 
 	/**
 	 * These are public only so that jem.ui can access this constant. Not meant to be accessed by others.
 	 */
 	public static final QualifiedName PROPERTY_LAUNCH_CONFIGURATION = new QualifiedName("org.eclipse.jem.proxy", "proxyLaunchConfiguration"); //$NON-NLS-1$ //$NON-NLS-2$
 	
 	/**
 	 * Return the plugin ordered array of configuration elements for the given container, or <code>null</code> if not contributed.
 	 * 
 	 * @param containerid
 	 * @return Array of configuration elements or <code>null</code> if this container has no contributions.
 	 * 
 	 * @since 1.0.0
 	 */
 	public synchronized IConfigurationElement[] getContainerConfigurations(String containerid) {
 		if (containerToContributions == null)
 			processProxyContributionExtensionPoint();
 		return (IConfigurationElement[]) containerToContributions.get(containerid);
 	}
 
 	/**
 	 * Return the plugin ordered array of configuration elements for the given plugin, or <code>null</code> if not contributed.
 	 * 
 	 * @param pluginid
 	 * @return Array of configuration elements or <code>null</code> if this plugin has no contributions.
 	 * 
 	 * @since 1.0.0
 	 */
 	public synchronized IConfigurationElement[] getPluginConfigurations(String pluginid) {
 		if (pluginToContributions == null)
 			processProxyContributionExtensionPoint();
 		return (IConfigurationElement[]) pluginToContributions.get(pluginid);
 	}
 	
 	protected synchronized void processProxyContributionExtensionPoint() {
 		ContributorExtensionPointInfo info = processContributionExtensionPoint(PI_CONFIGURATION_CONTRIBUTION_EXTENSION_POINT);
 		containerToContributions = info.containerToContributions;
 		pluginToContributions = info.pluginToContributions;
 	}
 	
 	
 	/**
 	 * Result form processContributionExtensionPoint.
 	 * 
 	 * @see ProxyPlugin#processContributionExtensionPoint(String)
 	 * @since 1.0.0
 	 */
 	public static class ContributorExtensionPointInfo {
 		/**
 		 * Map of container ids (String) to contributions (IConfigurationElement[]) that was found with that id. For each container,
 		 * the contributions will be listed in plugin prereq order.
 		 */
 		public Map containerToContributions;
 		
 		/**
 		 * Map of plugin ids (String) to contributions (IConfigurationElement[]) that was found with that id. For each plugin,
 		 * the contributions will be listed in plugin prereq order.
 		 */
 		public Map pluginToContributions;
 	}
 
 	/**
 	 * Process the extension point looking contributors. It will find entries that have the "container" or "plugin" attributes
 	 * set on them.
 	 * 
 	 * @param extensionPoint fully-qualified extension point id, including plugin id of the extension point.
 	 * @return the contributor info record.
 	 * 
 	 * @since 1.0.0
 	 */
 	public static ContributorExtensionPointInfo processContributionExtensionPoint(String extensionPoint) {	
 		// We are processing this once because it is accessed often (once per vm per project).
 		// This can add up so we get it together once here.
 		IExtensionPoint extp = Platform.getExtensionRegistry().getExtensionPoint(extensionPoint);
 		ContributorExtensionPointInfo result = new ContributorExtensionPointInfo();
 		if (extp == null) {
 			result.containerToContributions = Collections.EMPTY_MAP;
 			result.pluginToContributions = Collections.EMPTY_MAP;
 			return result;
 		}
 		
 		IExtension[] extensions = extp.getExtensions();
 		// Need to be in plugin order so that first ones processed have no dependencies on others.
 		HashMap bundlesToExtensions = new HashMap(extensions.length);
 		for (int i = 0; i < extensions.length; i++) {
 			Bundle bundle = Platform.getBundle(extensions[i].getNamespace());
 			IExtension[] ext = (IExtension[]) bundlesToExtensions.get(bundle);
 			if (ext == null)
 				bundlesToExtensions.put(bundle, new IExtension[] {extensions[i]});
 			else {
 				// More than one extension defined in this plugin.
 				IExtension[] newExt = new IExtension[ext.length + 1];
 				System.arraycopy(ext, 0, newExt, 0, ext.length);
 				newExt[newExt.length-1] = extensions[i];
 				bundlesToExtensions.put(bundle, newExt);
 			}
 		}
 		
 		// Now order them so we process in required order.
 		// If it is the first time, we are about to find out many inter-dependencies
 		// notify the cache on a start/end of a transaction so that updates (if new
 		// dependencies) are updated only once
 		Bundle[] ordered = ProxyPlugin.orderPlugins(bundlesToExtensions.keySet());
 		result.containerToContributions = new HashMap(ordered.length);
 		result.pluginToContributions = new HashMap(ordered.length);
 		for (int i = 0; i < ordered.length; i++) {
 			IExtension[] exts = (IExtension[]) bundlesToExtensions.get(ordered[i]);
 			for (int j = 0; j < exts.length; j++) {
 				IConfigurationElement[] configs = exts[j].getConfigurationElements();
 				// Technically we expect the config elements to have a name of "contributor", but since that
 				// is all that can be there, we will ignore it. The content is what is important.
 				for (int k = 0; k < configs.length; k++) {
 					String container = configs[k].getAttributeAsIs(PI_CONTAINER);
 					if (container != null) {
 						List contributions = (List) result.containerToContributions.get(container);
 						if (contributions == null) {
 							contributions = new ArrayList(1);
 							result.containerToContributions.put(container, contributions);
 						}
 						contributions.add(configs[k]);
 					}
 					String plugin = configs[k].getAttributeAsIs(PI_PLUGIN);
 					if (plugin != null) {
 						List contributions = (List) result.pluginToContributions.get(plugin);
 						if (contributions == null) {
 							contributions = new ArrayList(1);
 							result.pluginToContributions.put(plugin, contributions);
 						}
 						contributions.add(configs[k]);
 					}
 				}
 			} 
 		}
 		
 		// Now go through and turn all of the contribution lists into arrays.
 		for (Iterator iter = result.containerToContributions.entrySet().iterator(); iter.hasNext();) {
 			Map.Entry entry = (Map.Entry) iter.next();
 			entry.setValue(((List) entry.getValue()).toArray(new IConfigurationElement[((List) entry.getValue()).size()]));
 		}
 		for (Iterator iter = result.pluginToContributions.entrySet().iterator(); iter.hasNext();) {
 			Map.Entry entry = (Map.Entry) iter.next();
 			entry.setValue(((List) entry.getValue()).toArray(new IConfigurationElement[((List) entry.getValue()).size()]));
 		}
 
 		return result;
 	}
 	
 	/**
 	 * For the given java project, return the maps of container paths and plugins found. The keys will be of type as specified for the parms
 	 * while the value will be Boolean, true if it was visible, and false if it wasn't.
 	 * For example if <code>/SWT_CONTAINER/subpath1</code> is found in the projects path (or from required projects), then
 	 * the container id will be added to the map. They come from the raw classpath entries of the projects.
 	 *
 	 * @param jproject
 	 * @param containerIds This map will be filled in with container ids as keys (type is <code>java.lang.String</code>) that are found in the projects build path. The value will be a Boolean, true if this container id was visible to the project (i.e. was in the project or was exported from a required project).
 	 * @param containers This map will be filled in with classpath containers as keys found in the projects build path. The value will be a Boolean as in container ids map.
 	 * @param pluginIds This map will be filled in with plugin ids as keys (type is <code>java.lang.String</code>) that are found in the projects build path. The value will be a Boolean as in container ids map.
 	 * @param projects This map will be filled in with project paths (except the top project) as keys (type is <code>org.eclipse.core.runtime.IPath</code>) that are found in the projects build path. The value will be a Boolean as in container ids map.
 	 * 
 	 * @since 1.0.0
 	 */
 	public void getIDsFound(IJavaProject jproject, Map containerIds, Map containers, Map pluginIds, Map projects) throws JavaModelException {		
 		IPath projectPath = jproject.getProject().getFullPath();
 		projects.put(projectPath, Boolean.TRUE);		
 		expandProject(projectPath, containerIds, containers, pluginIds, projects, true, true);
 		projects.remove(projectPath);	// Don't need to include itself now, was needed for testing so if ciruclar we don't get into a loop.
 	}
 	
 	/*
 	 * The passed in visible flag tells if this project is visible and its contents are visible if they are exported.
 	 * Only exception is if first is true, then all contents are visible to the top level project.
 	 */
 	private void expandProject(IPath projectPath, Map containerIds, Map containers, Map pluginIds, Map projects, boolean visible, boolean first) throws JavaModelException {
 		IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(projectPath.lastSegment());
 		if (res == null)
 			return;	// Not exist so don't delve into it.
 		IJavaProject project = (IJavaProject)JavaCore.create(res);
 		if (project == null || !project.exists() || !project.getProject().isOpen())
 			return;	// Not exist as a java project or not open, so don't delve into it.
 
 		IClasspathEntry[] entries = project.getRawClasspath();
 		for (int i = 0; i < entries.length; i++) {
 			IClasspathEntry entry = entries[i];
 			Boolean currentFlag = null;	// Current setting value.
 			boolean newFlag;	// The new setting value. 
 			switch (entry.getEntryKind()) {
 				case IClasspathEntry.CPE_PROJECT:
 					// Force true if already true, or this is the first project, or this project is visible and the entry is exported. These override a previous false.
 					currentFlag = (Boolean) projects.get(entry.getPath());
 					newFlag = (currentFlag != null && currentFlag.booleanValue()) || first || (visible && entry.isExported());
 					if (currentFlag == null || currentFlag.booleanValue() != newFlag)
 						projects.put(entry.getPath(),  newFlag ? Boolean.TRUE : Boolean.FALSE );
 					if (currentFlag == null)
 						expandProject(entry.getPath(), containerIds, containers, pluginIds, projects, visible && entry.isExported(), false);
 					break;
 				case IClasspathEntry.CPE_CONTAINER:
 					if (!first && "org.eclipse.jdt.launching.JRE_CONTAINER".equals(entry.getPath().segment(0)))
 						break;	// The first project determines the JRE, so any subsequent ones can be ignored.
 					currentFlag = (Boolean) containerIds.get(entry.getPath().segment(0));
 					newFlag = (currentFlag != null && currentFlag.booleanValue()) || first || (visible && entry.isExported());					
 					if (currentFlag == null || currentFlag.booleanValue() != newFlag)					
 						containerIds.put(entry.getPath().segment(0), newFlag ? Boolean.TRUE : Boolean.FALSE );					
 
 					IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
 					// Force true if already true, or this is the first project, or this project is visible and the entry is exported. These override a previous false.
 					currentFlag = (Boolean) containers.get(container);
 					newFlag = (currentFlag != null && currentFlag.booleanValue()) || first || (visible && entry.isExported());
 					if (currentFlag == null || currentFlag.booleanValue() != newFlag)					
 						containers.put(container,  newFlag ? Boolean.TRUE : Boolean.FALSE );
 					break;
 				case IClasspathEntry.CPE_VARIABLE:
 					// We only care about JRE_LIB. If we have that, then we will treat it as JRE_CONTAINER. Only
 					// care about first project too, because the first project is the one that determines the JRE type.
 					if (first && "JRE_LIB".equals(entry.getPath().segment(0))) { //$NON-NLS-1$
 						currentFlag = (Boolean) containerIds.get("org.eclipse.jdt.launching.JRE_CONTAINER");	//$NON-NLS-1$
 						if (currentFlag == null || !currentFlag.booleanValue())						
 							containerIds.put("org.eclipse.jdt.launching.JRE_CONTAINER", Boolean.TRUE); //$NON-NLS-1$
 					}
 					break;
 				default:
 					break;
 			}
 		}		
 		
 		findPlugins(pluginIds, visible, first, project);
 	}
 	
 	/**
 	 * Find the plugins that the given project references, either directly or indirectly.
 	 * <p>
 	 * The map will be of plugin ids to a Boolean. If the boolean is <code>BooleanTRUE</code>,
 	 * then the plugin is visible to the given project. the visible and first flags
 	 * will modify this. If first is true, then all direct plugins will be visible,
 	 * else only exported plugins will be visible. If visible is false and first is false, then it doesn't matter, all of the
 	 * plugins will not be visible. 
 	 * <p>
 	 * Visible means that classes in the plugin can be referenced directly from code. Not visible
 	 * means that they can only be referenced from some other plugin in the list. In other words,
 	 * visible ones can be directly referenced, but invisible ones can only be referenced from
 	 * plugins that can see it.
 	 * <p>
 	 * For most uses, first and visible should be true. Then it will treat the project as the toplevel
 	 * project and will return true for those that are visible to it, either directly or indirectly.
 	 * These flags were added for more special cases where may be calling on a project that is deeper
 	 * down in the classpath were visibilty has already been decided.
 	 * <p>
 	 * Note: PDE must be installed for this to return anything, otherwise it will leave
 	 * the map alone.
 	 * 
 	 * @param pluginIds map of pluginIds->Boolean(TRUE is visible)
 	 * @param visible <code>true</code> means this project is visible, so any plugins visible to it will be visible, else none will be visible.
 	 * @param first <code>true</code> if this is the top project of interest. This means that all plugins within the project are visible. Else only exported projects will be visible.
 	 * @param project project to start looking from
 	 * 
 	 * @since 1.0.2
 	 */
 	public void findPlugins(Map pluginIds, boolean visible, boolean first, IJavaProject project) {
 		try {
 			// To prevent unnecessary loading of the PDE plugin, find the plugins only if this project is a PDE plugin project.
			if (project.getProject().hasNature("org.eclipse.pde.PluginNature")) {
 				IPDEProcessForPlugin pdeprocess = getPDEProcessForPlugin();
 				if (pdeprocess != null)
 					pdeprocess.findPlugins(project, pluginIds, visible, first); // expand the plugins for this project, if any.
 			}
 		} catch (CoreException e) {
 		}
 	}
 
 	/*
 	 * Interface for processing Plugins. Used when PDE plugin is present in the installation. 
 	 * 
 	 * @since 1.0.2
 	 */
 	interface IPDEProcessForPlugin {
 
 		/*
 		 * Go through the project and find all of the plugins it references, either directly or through
 		 * the referenced plugins, and mark them as visible or not.
 		 */
 		public abstract void findPlugins(IJavaProject project, Map pluginIds, boolean visible, boolean first);
 	}
 	
 	/*
 	 * Try to get the pde process for plugin. If already tried once and not found, then forget it.
 	 * <package-protected> because PDEContributeClasspath needs it too.
 	 */
 	private IPDEProcessForPlugin pdeProcessForPlugin;
 	private boolean triedPDEProcess;
 	IPDEProcessForPlugin getPDEProcessForPlugin() {
 		if (!triedPDEProcess) {
 			triedPDEProcess = true;
 			if (Platform.getBundle("org.eclipse.pde.core") != null) {
 				try {
 					Class classPDEProcess = Class.forName("org.eclipse.jem.internal.proxy.core.PDEProcessForPlugin");
 					pdeProcessForPlugin = (IPDEProcessForPlugin) classPDEProcess.newInstance();
 				} catch (ClassNotFoundException e) {
 					// Not found, do nothing.
 				} catch (InstantiationException e) {
 					getLogger().log(e, Level.WARNING);
 				} catch (IllegalAccessException e) {
 					getLogger().log(e, Level.WARNING);
 				}
 			}
 		}
 		return pdeProcessForPlugin;
 	}
 	
 	/**
 	 * This tries to find a jar in the bundle specified, and the attached source using the
 	 * PDE source location extension point. The jar must exist for source to be attachable.
 	 * The source must be in the standard PDE source plugin. I.e. it must be in a directory
 	 * of the name "bundlename_bundleversion", and in the same path from there as in the
 	 * jar, plus the name must be "jarnamesrc.zip".
 	 * <p>
 	 * The returned URL's will not be Platform.resolve(). They will be in form returned from
 	 * Platform.find().
 	 * 
 	 * @param bundle bundle to search, will search fragments too.
 	 * @param filepath filepath from the root of the bundle/fragment where the jar will be found. 
 	 * @return two URL's. [0] is the URL to the jar, <code>null</code> if not found, [2] is the URL to the source zip, <code>null</code> if not found.
 	 * 
 	 * @since 1.0.0
 	 */
 	public URL[] findPluginJarAndAttachedSource(Bundle bundle, IPath filepath) {
 		// This is a bit kludgy, but the algorithm is to find the file first, and then get the root url of the bundle/fragment
 		// that matches the found file. This will be used to calculate the name of the directory under the source. From there
 		// all of the source extensions will be searched for the source zip file.
 		// This is assuming that find returns a url where the file part of the url is a standard path and doesn't have
 		// things like special chars to indicate within a jar. That would appear when it is resolved, but I think that the
 		// unresolved ones from find are typically "jarbundle://nnn/path" or something like that. This is a gray area.
 		URL jarURL = Platform.find(bundle, filepath);
 		if (jarURL == null)
 			return new URL[2];
 		
 		// Found it, so let's try to find which bundle/fragment it was found in.
 		String jarString = jarURL.toExternalForm();
 		// First the bundle itself.
 		String installLoc = bundle.getEntry("/").toExternalForm();
 		URL sourceURL = null;
 		if (jarString.startsWith(installLoc))
 			sourceURL = getSrcFrom(bundle, installLoc, jarString);
 		else {
 			// Now look in the fragments.
 			Bundle[] frags = Platform.getFragments(bundle);
 			for (int i = 0; i < frags.length; i++) {
 				installLoc = frags[i].getEntry("/").toExternalForm();
 				if (jarString.startsWith(installLoc)) {
 					sourceURL = getSrcFrom(frags[i], installLoc, jarString);
 					break;
 				}
 			}
 		}
 		return new URL[] {jarURL, sourceURL};
 	}
 	
 	private URL getSrcFrom(Bundle bundle, String installLoc, String jarString) {
 		// format of path in a PDE source plugin is (under the "src" directory from the extension point),
 		// "bundlename_bundleversion/pathOfJar/jarnamesrc.zip". However there is no way to know
 		// which extension has the source in it, so we need to search them all.
 		
 		IPath srcPath = new Path(bundle.getSymbolicName()+"_"+ (String) bundle.getHeaders("").get(Constants.BUNDLE_VERSION)); //$NON-NLS-1$ $NON-NLS-2$
 		srcPath = srcPath.append(new Path(jarString.substring(installLoc.length())));
 		if (srcPath.segmentCount() < 2)
 			return null;	// Something is not right. No jar name.
 		srcPath = srcPath.removeFileExtension();	// Remove the .jar.
 		String jarName = srcPath.lastSegment();	// This should be the jar name.
 		srcPath = srcPath.removeLastSegments(1).append(jarName+"src.zip");
 		
 		// Now look through all of the src extensions. Can't tell if the extension is from a fragment or a bundle, so we need to
 		// use Platform.find() to look in the bundle and fragment. So we may get a dup search if there is a fragment source 
 		// (for example platform source and win32 platform source (which is a fragment of platform source).
 		IConfigurationElement[] ces = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.pde.core.source");
 		for (int i = 0; i < ces.length; i++) {
 			IPath srcsrch = new Path(ces[i].getAttributeAsIs("path")).append(srcPath);
 			Bundle srcBundle = Platform.getBundle(ces[i].getDeclaringExtension().getNamespace());
 			URL srcUrl = Platform.find(srcBundle, srcsrch);
 			if (srcUrl != null) {
 				return srcUrl;
 			}
 		}
 		return null;
 	}
 }
