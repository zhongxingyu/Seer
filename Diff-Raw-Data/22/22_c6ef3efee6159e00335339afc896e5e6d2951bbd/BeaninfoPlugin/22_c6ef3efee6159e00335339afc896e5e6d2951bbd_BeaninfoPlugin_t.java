 package org.eclipse.jem.internal.beaninfo.core;
 /*******************************************************************************
  * Copyright (c)  2001, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: BeaninfoPlugin.java,v $
 *  $Revision: 1.3 $  $Date: 2004/04/19 19:11:10 $ 
  */
 
 
 import java.util.*;
 import java.util.logging.Level;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.*;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.jdt.core.IClasspathContainer;
 
 import org.eclipse.jem.internal.beaninfo.adapters.*;
 import org.eclipse.jem.internal.proxy.core.IConfigurationContributionInfo;
 import org.eclipse.jem.internal.proxy.core.ProxyPlugin;
 import org.eclipse.jem.internal.proxy.core.ProxyPlugin.ContributorExtensionPointInfo;
 
 import com.ibm.wtp.common.logger.proxy.Logger;
 import com.ibm.wtp.logger.proxyrender.EclipseLogger;
 
 /**
  * The plugin class for the org.eclipse.jem.internal.proxy.core plugin.
  */
 
 public class BeaninfoPlugin extends Plugin {
 	public static final String PI_BEANINFO_PLUGINID = "org.eclipse.jem.beaninfo";	// Plugin ID, used for QualifiedName. //$NON-NLS-1$
 	public static final String PI_BEANINFO_OVERRIDES = "overrides";	// ID of the overrides extension point. //$NON-NLS-1$
 	
 	private static BeaninfoPlugin BEANINFO_PLUGIN = null;
 		
 	public BeaninfoPlugin(IPluginDescriptor pluginDescriptor) {	
 		super(pluginDescriptor);
 		BEANINFO_PLUGIN = this;
 	}
 	
 	/**
 	 * Accessor method to get the singleton plugin.
 	 */
 	public static BeaninfoPlugin getPlugin() {
 		return BEANINFO_PLUGIN;
 	}
 	
 	
 	private Map containerIdsToBeaninfoEntryContributions;
 	private Map pluginToBeaninfoEntryContributions;
 	private Map containerIdsToContributors;
 	private Map pluginToContributors;
 	/*
 	 * Override contributions from extension point.
 	 * ocFragments: Array of fragments paths. When a match is found for a path, the index
 	 * 		is the index into the ocContainerIds and ocPluginIds array for the contributions.
 	 * ocContainerIds: The first dimension is the index of the fragment that the list of OverrideContributions is for.
 	 * 		The second dimension is the array of contributions for that fragment, one per container id.
 	 * ocPluginIds: The first dimension is the index of the fragment that the list of OverrideContributions is for.
 	 * 		The second dimension is the array of contributions for that fragment, one per plugin id.
 	 * 
 	 * If a particular fragment doesn't have any entries of container and/or plugin, then EMPTY_OC is used for that
 	 * entry so that we don't need to check for null.
 	 * 
 	 * How this is used is for a particular path requested, the ocFragments will be searched for the fragments that
 	 * are appropriate, then the index of the entry is used to walk through the OC[] array returned from the ocContainerIds
 	 * or ocPluginIds. Each contribution would be checked to see if the container id/plugin id is in the visible classpath (through 
 	 * the info data stored in the persistent property). If it is, then the overrides from that contribution will be used.
 	 */
 	private IPath ocFragments[];
 	private OverrideContribution[][] ocContainerIds;
 	private OverrideContribution[][] ocPluginIds;
 	
 	private static final OverrideContribution[] EMPTY_OC = new OverrideContribution[0];	// Used for an empty contribution list for a fragment.
 
 	public BeaninfoEntry[] getContainerIdBeanInfos(String containerID) {
 		if (containerIdsToBeaninfoEntryContributions == null)
 			processBeanInfoContributionExtensionPoint();
 		return (BeaninfoEntry[]) containerIdsToBeaninfoEntryContributions.get(containerID);
 	}
 	
 	public BeaninfoEntry[] getPluginBeanInfos(String pluginid) {
 		if (pluginToBeaninfoEntryContributions == null)
 			processBeanInfoContributionExtensionPoint();
 		return (BeaninfoEntry[]) pluginToBeaninfoEntryContributions.get(pluginid);
 	}
 	
 	public IConfigurationElement[] getPluginContributors(String pluginid) {
 		if (pluginToContributors == null)
 			processBeanInfoContributionExtensionPoint();
 		return (IConfigurationElement[]) pluginToContributors.get(pluginid);
 	}	
 	
 	public IConfigurationElement[] getContainerIdContributors(String containerID) {
 		if (containerIdsToContributors == null)
 			processBeanInfoContributionExtensionPoint();
 		return (IConfigurationElement[]) containerIdsToContributors.get(containerID);
 	}	
 	
 	public static final String PI_BEANINFO_CONTRIBUTION_EXTENSION_POINT = PI_BEANINFO_PLUGINID+".registrations";
 	public static final String PI_REGISTRATION = "registration";
 	public static final String PI_BEANINFO = "beaninfo"; 
 	public static final String PI_OVERRIDE = "override";
 	public static final String PI_CONTRIBUTOR = "contributor";
 	public static final String PI_PACKAGE = "package";
 	public static final String PI_PATH = "path";
 	
 	protected void processBeanInfoContributionExtensionPoint() {
 		ContributorExtensionPointInfo info = ProxyPlugin.processContributionExtensionPoint(PI_BEANINFO_CONTRIBUTION_EXTENSION_POINT);
 		ConfigurationElementReader reader = new ConfigurationElementReader();
 		// Process the container IDs first.
 		containerIdsToBeaninfoEntryContributions = new HashMap(info.containerToContributions.size());
 		Map fragmentsToIds = new HashMap(info.containerToContributions.size());
 		for (Iterator iter = info.containerToContributions.entrySet().iterator(); iter.hasNext();) {
 			Map.Entry entry= (Map.Entry) iter.next();
 			String containerid = (String) entry.getKey();
 			IConfigurationElement[] configElements = (IConfigurationElement[]) entry.getValue();
 			for (int i = 0; i < configElements.length; i++) {
 				IConfigurationElement element = configElements[i];
 				if (PI_REGISTRATION.equals(element.getName())) {
 					IConfigurationElement[] children = element.getChildren();
 					for (int j = 0; j < children.length; j++) {
 						IConfigurationElement child = children[j];
 						if (PI_BEANINFO.equals(child.getName())) {
 							// This is a beaninfo entry
 							BeaninfoEntry be = BeaninfoEntry.readEntry(reader, child, null);
 							if (be != null)
 								addEntry(containerIdsToBeaninfoEntryContributions, containerid, be);
 						} else if (PI_OVERRIDE.equals(child.getName())) {
 							addOverrideEntry(fragmentsToIds, true, containerid, child);
 						}
 					}
 				} else if (PI_CONTRIBUTOR.equals(element.getName())) {
 						if (containerIdsToContributors == null)
 							containerIdsToContributors = new HashMap(5);	// These are rare, don't create until necessary.
 						addEntry(containerIdsToContributors, containerid, element);
 					}
 				}
 			}
 			
 		// Now go through and turn all of the contribution lists into arrays.
 		for (Iterator iter = containerIdsToBeaninfoEntryContributions.entrySet().iterator(); iter.hasNext();) {
 			Map.Entry entry = (Map.Entry) iter.next();
 			entry.setValue(((List) entry.getValue()).toArray(new BeaninfoEntry[((List) entry.getValue()).size()]));
 		}
 		
 		if (containerIdsToContributors == null)
 			containerIdsToContributors = Collections.EMPTY_MAP;	// Since we don't have any.
 		else {
 			for (Iterator iter = containerIdsToContributors.entrySet().iterator(); iter.hasNext();) {
 				Map.Entry entry = (Map.Entry) iter.next();
 				entry.setValue(((List) entry.getValue()).toArray(new IConfigurationElement[((List) entry.getValue()).size()]));
 			}			
 		}
 				
 		// Now process the plugin IDs.
 		pluginToBeaninfoEntryContributions = new HashMap(info.pluginToContributions.size());		
 		for (Iterator iter = info.pluginToContributions.entrySet().iterator(); iter.hasNext();) {
 			Map.Entry entry= (Map.Entry) iter.next();
 			String pluginId = (String) entry.getKey();
 			IConfigurationElement[] configElements = (IConfigurationElement[]) entry.getValue();
 			for (int i = 0; i < configElements.length; i++) {
 				IConfigurationElement element = configElements[i];
 				if (PI_REGISTRATION.equals(element.getName())) {
 					IConfigurationElement[] children = element.getChildren();
 					for (int j = 0; j < children.length; j++) {
 						IConfigurationElement child = children[j];
 						if (PI_BEANINFO.equals(child.getName())) {
 							// This is a beaninfo entry
 							BeaninfoEntry be = BeaninfoEntry.readEntry(reader, child, null);
 							if (be != null)
 								addEntry(pluginToBeaninfoEntryContributions, pluginId, be);
 						} else if (PI_OVERRIDE.equals(child.getName())) {
 							addOverrideEntry(fragmentsToIds, false, pluginId, child);
 						}
 					}
 				} else if (PI_CONTRIBUTOR.equals(element.getName())) {
 						if (pluginToContributors == null)
 							pluginToContributors = new HashMap(5);	// These are rare, don't create until necessary.
 						addEntry(pluginToContributors, pluginId, element);
 					}
 				}
 			}
 			
 		// Now go through and turn all of the contribution lists into arrays.
 		for (Iterator iter = pluginToBeaninfoEntryContributions.entrySet().iterator(); iter.hasNext();) {
 			Map.Entry entry = (Map.Entry) iter.next();
 			entry.setValue(((List) entry.getValue()).toArray(new BeaninfoEntry[((List) entry.getValue()).size()]));
 		}
 		
 		if (pluginToContributors == null)
 				pluginToContributors = Collections.EMPTY_MAP;	// Since we don't have any.
 		else {
 			for (Iterator iter = pluginToContributors.entrySet().iterator(); iter.hasNext();) {
 				Map.Entry entry = (Map.Entry) iter.next();
 				entry.setValue(((List) entry.getValue()).toArray(new IConfigurationElement[((List) entry.getValue()).size()]));
 			}			
 		}
 		
 		// Now handle the entire list of fragments.
 		ocFragments = new IPath[fragmentsToIds.size()];
 		ocContainerIds = new OverrideContribution[ocFragments.length][];
 		ocPluginIds = new OverrideContribution[ocFragments.length][];
 		Iterator iter;
 		int fragIndex;
 		for (iter = fragmentsToIds.entrySet().iterator(), fragIndex=0; iter.hasNext(); fragIndex++) {
 			Map.Entry mapEntry = (Map.Entry) iter.next();
 			ocFragments[fragIndex] = (IPath) mapEntry.getKey();
 			Map[] mapValue = (Map[]) mapEntry.getValue();
 			if (mapValue[0] == null)
 				ocContainerIds[fragIndex] = EMPTY_OC;
 			else {
 				Map containers = mapValue[0];
 				OverrideContribution[] ocContribution = ocContainerIds[fragIndex] = new OverrideContribution[containers.size()];
 				int ocIndex;
 				Iterator ocIterator;
 				for (ocIterator = containers.entrySet().iterator(), ocIndex=0; ocIterator.hasNext(); ocIndex++) {
 					Map.Entry containerEntry = (Map.Entry) ocIterator.next();
 					OverrideContribution oc = ocContribution[ocIndex] = new OverrideContribution();
 					oc.id = (String) containerEntry.getKey();
 					List[] ocLists = (List[]) containerEntry.getValue();
 					oc.pluginIds = (String[]) ocLists[0].toArray(new String[ocLists[0].size()]);
 					oc.paths = (String[]) ocLists[1].toArray(new String[ocLists[1].size()]);
 				}
 			}
 			if (mapValue[1] == null)
 				ocPluginIds[fragIndex] = EMPTY_OC;
 			else {
 				Map plugins = mapValue[1];
 				OverrideContribution[] ocContribution = ocPluginIds[fragIndex] = new OverrideContribution[plugins.size()];
 				int ocIndex;
 				Iterator ocIterator;
 				for (ocIterator = plugins.entrySet().iterator(), ocIndex=0; ocIterator.hasNext(); ocIndex++) {
 					Map.Entry pluginEntry = (Map.Entry) ocIterator.next();
 					OverrideContribution oc = ocContribution[ocIndex] = new OverrideContribution();
 					oc.id = (String) pluginEntry.getKey();
 					List[] ocLists = (List[]) pluginEntry.getValue();
 					oc.pluginIds = (String[]) ocLists[0].toArray(new String[ocLists[0].size()]);
 					oc.paths = (String[]) ocLists[1].toArray(new String[ocLists[1].size()]);
 				}
 			}			
 		}
 	}
 	
 	/*
 	 * Add an entry to the map. If the key doesn't exist, create an entry as an array. Then add the entry to array.
 	 */
 	private void addEntry(Map map, Object key, Object entry) {
 		List mapEntry = (List) map.get(key);
 		if (mapEntry == null) {
 			mapEntry = new ArrayList(1);
 			map.put(key, mapEntry);
 		}
 		mapEntry.add(entry);
 	}
 	
 	/*
 	 * Add an entry to the map.
 	 * id is the container/plugin id.
 	 * 
 	 * The structure of the map is:
 	 * 	key: fragment name
 	 * 	value: Map[2], where [0] is for container ids, and [1] is for plugin ids.
 	 * 		Map[x]:
 	 * 			key: container/plugin id
 	 * 			value: List[2], where [0] is list of plugin ids for the override, and [1] is list of paths for the override files relative to that plugin id.
 	 * 
 	 * After all done these maps/list will be boiled down to the arrays that will be used for lookup.
 	 */
 	private void addOverrideEntry(Map map, boolean container, Object id, IConfigurationElement entry) {
 		
 		String packageName = entry.getAttributeAsIs(PI_PACKAGE);
 		String plugin = null;
 		String pathString = entry.getAttributeAsIs(PI_PATH);
 		IPath fragment = null; 
 		if (packageName != null && packageName.length() > 0 && pathString != null && pathString.length() > 0) { 
 			fragment = new Path(packageName.replace('.', '/'));
 			if (pathString.charAt(pathString.length()-1) != '/')
 				pathString += '/';
 			if (pathString.charAt(0) != '/')
 				plugin = entry.getDeclaringExtension().getNamespace();
 			else {
 				if (pathString.length() > 4) {
 					int pend = pathString.indexOf('/', 1);
 					if (pend == -1 || pend >= pathString.length()-1)
 						return;	// invalid
 					plugin = pathString.substring(1, pend);
 					pathString = pathString.substring(pend+1);
 				} else
 					return;	// invalid
 			}
 		}
 		if (pathString.length() < 2)
 			return;	// invalid
 
 		Map[] mapEntry = (Map[]) map.get(fragment);
 		if (mapEntry == null) {
 			mapEntry = new HashMap[2];
 			map.put(fragment, mapEntry);
 		}
 		
 		if (container) {
 			if (mapEntry[0] == null)
 				mapEntry[0] = new HashMap(2);
 		} else {
 			if (mapEntry[1] == null)
 				mapEntry[1] = new HashMap(2);			
 		}
 		
 		List[] idEntry = (List[]) mapEntry[container ? 0 : 1].get(id);
 		if (idEntry == null) {
 			idEntry = new List[] {new ArrayList(1), new ArrayList(1)};
 			mapEntry[container ? 0 : 1].put(id, idEntry);
 		}
 
 		idEntry[0].add(plugin);
 		idEntry[1].add(pathString);
 	}	
 			
 	/*
 	 * This is an list of overrides that are available as a contribution for a specific fragment.
 	 * <ul>
 	 * <li>The id of this contribution. Either container or plugin id depending on which list it was in..
 	 * <li>The plugins array lists the plugin ids for all of the paths in this contribution.
 	 * <li>The paths array lists the folder path under that corresponding plugin from "pluginIds".
 	 * </ul> 
 	 * <p>
 	 * 
 	 * @since 1.0.0
 	 */
 	private static class OverrideContribution {
 		public String id;
 		public String[] pluginIds;
 		public String[] paths;
 	}
 	
 	/**
 	 * The runnable to use to override. This will be called in sequence
 	 * for each override path found. It is send in on the apply overrides call.
 	 * 
 	 * @since 1.0.0
 	 * @see BeaninfoPlugin#applyOverrides(IProject, String, String, ResourceSet, IOverrideRunnable)
 	 */
 	public interface IOverrideRunnable {
 		/**
 		 * This will be called with the path to use. It will be called over and over for every
 		 * override path found for a package. The path will be complete, including trailing '/'.
 		 * It will be in a URI format for a directory. Just append the filename to get a complete path.
 		 * 
 		 * @param overridePath
 		 * 
 		 * @since 1.0.0
 		 */
 		public void run(String overridePath);
 		
 		/**
 		 * This will be called with the actual resource to use. This will be called by special contributors that want
 		 * a special explicit override resource to be used.
 		 * <p>
 		 * Contributors should use the ResourceSet that was passed into them. This is so that anything java class that
 		 * the override resource points to will be found.
 		 * <p>
 		 * This resource will be automatically removed by BeanInfo after being applied. It must not be left around because
 		 * in the process of being applied it will be modified, so it could not be reused. 
 		 *  
 		 * @param overrideResource
 		 * 
 		 * @since 1.0.0
 		 */
 		public void run(Resource overrideRes);
 	}
 	
 	/**
 	 * This will be passed
 	 * 
 	 * @since 1.0.0
 	 */
 	public interface IContributorOverrideRunnable extends IOverrideRunnable {
 		
 		/**
 		 * This can be called by BeanInfo contributor for overrides to see if the path (path is for run(path) method)
 		 * has already been contributed once for this class. It can be used to save time. However, not necessary because
 		 * BeanInfo will not permit it to be contributed more than once for a class.
 		 * 
 		 * @param path
 		 * @return <code>true</code> if used already.
 		 * 
 		 * @since 1.0.0
 		 */
 		public boolean pathContributed(String path);
 		
 		/**
 		 * This can be called by BeanInfo contributor for overrides to see if the URI (path is for run(resource) method)
 		 * has already been contributed once for this class. It can be used to save time. However, not necessary because
 		 * BeanInfo will not permit the URI to be contributed more than once for a class.
 		 * 
 		 * @param resourceURI
 		 * @return <code>true</code> if used already.
 		 * 
 		 * @since 1.0.0
 		 */
 		public boolean resourceContributed(URI resourceURI);
 	}
 	
 	/**
 	 * Apply the runnable to all of the override paths that are applicable to the 
 	 * given package name. The package name uses '.' to delineate the fragments of the name,
 	 * i.e. use "<code>java.lang</code>" as a package name.
 	 * 
 	 * @param project the project to run against.
 	 * @param packageName
 	 * @param className class name of the class that is being overridden.
 	 * @param resource set that contributors can use to temporarily load dynamic override files.
 	 * @param runnable
 	 * 
 	 * @since 1.0.0
 	 */
 	public void applyOverrides(IProject project, String packageName, final String className, final ResourceSet rset, final IOverrideRunnable runnable) {
 		final IPath packagePath = new Path(packageName.replace('.', '/')+'/');
 		try {
 			IConfigurationContributionInfo info = (IConfigurationContributionInfo) project.getSessionProperty(BeaninfoNature.CONFIG_INFO_SESSION_KEY);
 			final IBeanInfoContributor[] explicitContributors = (IBeanInfoContributor[]) project.getSessionProperty(BeaninfoNature.BEANINFO_CONTRIBUTORS_SESSION_KEY);
			if (ocFragments == null)
				processBeanInfoContributionExtensionPoint();	// We haven't processed them yet.
 			for (int fragmentIndex = 0; fragmentIndex < ocFragments.length; fragmentIndex++) {
 				if (ocFragments[fragmentIndex].isPrefixOf(packagePath)) {
 					String leftOver = null;	// The left over portion of the package. This will be set first time needed. 
 					OverrideContribution[] cntrContributions = ocContainerIds[fragmentIndex];
 					for (int ocindex = 0; ocindex < cntrContributions.length; ocindex++) {
 						OverrideContribution contribution = cntrContributions[ocindex];
 						Boolean visible = (Boolean) info.getContainerIds().get(contribution.id);
 						if (visible != null && visible.booleanValue()) {
 							for (int cindex = 0; cindex < contribution.pluginIds.length; cindex++) {
 								IPluginDescriptor pd = Platform.getPluginRegistry().getPluginDescriptor(contribution.pluginIds[cindex]);
 								if (pd != null) {
 									if (leftOver == null)
 										leftOver = getLeftOver(ocFragments[fragmentIndex], packagePath);
 									runnable.run(pd.getInstallURL().toString()+contribution.paths[cindex]+leftOver);
 								}
 							}
 						}
 					}
 					
 					OverrideContribution[] pluginContributions = ocPluginIds[fragmentIndex];
 					for (int ocindex = 0; ocindex < pluginContributions.length; ocindex++) {
 						OverrideContribution contribution = pluginContributions[ocindex];
 						Boolean visible = (Boolean) info.getPluginIds().get(contribution.id);
 						if (visible != null && visible.booleanValue()) {
 							for (int cindex = 0; cindex < contribution.pluginIds.length; cindex++) {
 								IPluginDescriptor pd = Platform.getPluginRegistry().getPluginDescriptor(contribution.pluginIds[cindex]);
 								if (pd != null) {
 									if (leftOver == null)
 										leftOver = getLeftOver(ocFragments[fragmentIndex], packagePath);
 									runnable.run(pd.getInstallURL().toString()+contribution.paths[cindex]+leftOver);
 								}
 							}
 						}
 					}
 				}
 			}
 
 			final Set usedPaths = new HashSet(10);	// Set of used paths. So that the contributors don't supply a path already used. This could cause problems if they did.
 			final IContributorOverrideRunnable contribRunnable = new IContributorOverrideRunnable() {
 				public void run(String overridePath) {
 					if (!usedPaths.contains(overridePath)) {
 						usedPaths.add(overridePath);
 						runnable.run(overridePath);
 					}
 				}
 				
 				public void run(Resource overrideRes) {
 					if (!usedPaths.contains(overrideRes.getURI())) {
 						usedPaths.add(overrideRes.getURI());
 						try {
 							runnable.run(overrideRes);
 						} finally {
 							overrideRes.getResourceSet().getResources().remove(overrideRes);
 						}
 					}
 				}
 				
 				public boolean pathContributed(String path) {
 					return usedPaths.contains(path);
 				}
 				
 				public boolean resourceContributed(URI resourceURI) {
 					return usedPaths.contains(resourceURI);
 				}
 			};
 			
 			// Run through the containers that implement IBeanInfoContributor.
 			for (Iterator iter = info.getContainers().entrySet().iterator(); iter.hasNext();) {
 				Map.Entry mapEntry = (Map.Entry) iter.next();
 				final IClasspathContainer container = (IClasspathContainer) mapEntry.getKey();
 				if (container instanceof IBeanInfoContributor && ((Boolean) mapEntry.getValue()).booleanValue()) {
 					Platform.run(new ISafeRunnable() {
 						public void handleException(Throwable exception) {
 							// Standard run logs to .log
 						}
 						public void run() throws Exception {
 							((IBeanInfoContributor) container).runOverrides(packagePath, className, rset, contribRunnable);						
 						}
 					});
 				}
 			}
 			
 			// Run through the explicint contributors.
 			for (int i=0; i<explicitContributors.length; i++) {
 				final int ii = i;
 				Platform.run(new ISafeRunnable() {
 					public void handleException(Throwable exception) {
 						// Standard run logs to .log
 					}
 					public void run() throws Exception {
 						explicitContributors[ii].runOverrides(packagePath, className, rset, contribRunnable);						
 					}
 				});
 			}			
 		} catch (CoreException e) {
 			getLogger().log(e, Level.INFO);
 		}
 	}
 	
 	private String getLeftOver(IPath fragment, IPath packagePath) {
 		return packagePath.removeFirstSegments(fragment.segmentCount()).toString();
 	}
 	
 	private Logger logger;
 	public Logger getLogger() {
 		if (logger == null)
 			logger = EclipseLogger.getEclipseLogger(this);
 		return logger;
 	}
 }
 
 
