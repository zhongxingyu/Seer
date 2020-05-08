 /*******************************************************************************
  * Copyright (c) 2012 Martin Reiterer, Alexej Strelzow. All rights reserved.
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors: Martin Reiterer - initial API and implementation Alexej
  * Strelzow - moved object management to RBManager, Babel integration
  ******************************************************************************/
 package org.eclipse.babel.tapiji.tools.core.ui;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.babel.core.configuration.DirtyHack;
 import org.eclipse.babel.core.factory.MessageFactory;
 import org.eclipse.babel.core.message.IMessage;
 import org.eclipse.babel.core.message.IMessagesBundle;
 import org.eclipse.babel.core.message.IMessagesBundleGroup;
 import org.eclipse.babel.core.message.manager.IResourceDeltaListener;
 import org.eclipse.babel.core.message.manager.RBManager;
 import org.eclipse.babel.core.util.FileUtils;
 import org.eclipse.babel.core.util.NameUtils;
 import org.eclipse.babel.tapiji.tools.core.Logger;
 import org.eclipse.babel.tapiji.tools.core.model.IResourceBundleChangedListener;
 import org.eclipse.babel.tapiji.tools.core.model.IResourceDescriptor;
 import org.eclipse.babel.tapiji.tools.core.model.IResourceExclusionListener;
 import org.eclipse.babel.tapiji.tools.core.model.ResourceDescriptor;
 import org.eclipse.babel.tapiji.tools.core.model.exception.ResourceBundleException;
 import org.eclipse.babel.tapiji.tools.core.model.manager.IStateLoader;
 import org.eclipse.babel.tapiji.tools.core.model.manager.ResourceBundleChangedEvent;
 import org.eclipse.babel.tapiji.tools.core.model.manager.ResourceExclusionEvent;
 import org.eclipse.babel.tapiji.tools.core.ui.analyzer.ResourceBundleDetectionVisitor;
 import org.eclipse.babel.tapiji.tools.core.util.FragmentProjectUtils;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.JavaCore;
 
 public class ResourceBundleManager {
 
     public static String defaultLocaleTag = "[default]"; // TODO externalize
 
     /*** CONFIG SECTION ***/
     private static boolean checkResourceExclusionRoot = false;
 
     /*** MEMBER SECTION ***/
     private static Map<IProject, ResourceBundleManager> rbmanager = new HashMap<IProject, ResourceBundleManager>();
 
     public static final String RESOURCE_BUNDLE_EXTENSION = ".properties";
 
     // project-specific
     private Map<String, Set<IResource>> resources = new HashMap<String, Set<IResource>>();
 
     private Map<String, String> bundleNames = new HashMap<String, String>();
 
     private Map<String, List<IResourceBundleChangedListener>> listeners = new HashMap<String, List<IResourceBundleChangedListener>>();
 
     private List<IResourceExclusionListener> exclusionListeners = new ArrayList<IResourceExclusionListener>();
 
     // global
     private static Set<IResourceDescriptor> excludedResources = new HashSet<IResourceDescriptor>();
 
     private static Map<String, Set<IResource>> allBundles = new HashMap<String, Set<IResource>>();
 
     // private static IResourceChangeListener changelistener; //
     // RBChangeListener -> see stateLoader!
 
     public static final String NATURE_ID = "org.eclipse.babel.tapiji.tools.core.ui.nature";
 
     public static final String BUILDER_ID = Activator.PLUGIN_ID
             + ".I18NBuilder";
 
     /* Host project */
     private IProject project = null;
 
     /** State-Serialization Information **/
     private static boolean state_loaded = false;
 
     private static IStateLoader stateLoader;
 
     // Define private constructor
     private ResourceBundleManager(IProject project) {
         this.project = project;
 
         RBManager.getInstance(project).addResourceDeltaListener(
                 new IResourceDeltaListener() {
 
                     /**
                      * {@inheritDoc}
                      */
                     @Override
                     public void onDelete(IMessagesBundleGroup bundleGroup) {
                         resources.remove(bundleGroup.getResourceBundleId());
                     }
 
                     /**
                      * {@inheritDoc}
                      */
                     @Override
                     public void onDelete(String resourceBundleId,
                             IResource resource) {
                         resources.get(resourceBundleId).remove(resource);
                     }
                 });
     }
 
     public static ResourceBundleManager getManager(IProject project) {
         // check if persistant state has been loaded
         if (!state_loaded) {
             IStateLoader stateLoader = getStateLoader();
             if (stateLoader != null) {
                 stateLoader.loadState();
                 state_loaded = true;
                 excludedResources = stateLoader.getExcludedResources();
             } else {
                 Logger.logError("State-Loader uninitialized! Unable to restore project state.");
             }
         }
 
         // set host-project
         if (FragmentProjectUtils.isFragment(project)) {
             project = FragmentProjectUtils.getFragmentHost(project);
         }
 
         ResourceBundleManager manager = rbmanager.get(project);
         if (manager == null) {
             manager = new ResourceBundleManager(project);
             rbmanager.put(project, manager);
             manager.detectResourceBundles();
         }
         return manager;
     }
 
     public Set<Locale> getProvidedLocales(String bundleName) {
         RBManager instance = RBManager.getInstance(project);
 
         Set<Locale> locales = new HashSet<Locale>();
         IMessagesBundleGroup group = instance
                 .getMessagesBundleGroup(bundleName);
         if (group == null) {
             return locales;
         }
 
         for (IMessagesBundle bundle : group.getMessagesBundles()) {
             locales.add(bundle.getLocale());
         }
         return locales;
     }
 
     public static String getResourceBundleName(IResource res) {
         String name = res.getName();
         String regex = "^(.*?)" //$NON-NLS-1$
                 + "((_[a-z]{2,3})|(_[a-z]{2,3}_[A-Z]{2})" //$NON-NLS-1$
                 + "|(_[a-z]{2,3}_[A-Z]{2}_\\w*))?(\\." //$NON-NLS-1$
                 + res.getFileExtension() + ")$"; //$NON-NLS-1$
         return name.replaceFirst(regex, "$1"); //$NON-NLS-1$
     }
 
     protected boolean isResourceBundleLoaded(String bundleName) {
         return RBManager.getInstance(project).containsMessagesBundleGroup(
                 bundleName);
     }
 
     protected void unloadResource(String bundleName, IResource resource) {
         // TODO implement more efficient
         unloadResourceBundle(bundleName);
         // loadResourceBundle(bundleName);
     }
 
     public static String getResourceBundleId(IResource resource) {
         String packageFragment = "";
 
         IJavaElement propertyFile = JavaCore.create(resource.getParent());
         if (propertyFile != null && propertyFile instanceof IPackageFragment) {
             packageFragment = ((IPackageFragment) propertyFile)
                     .getElementName();
         }
 
         return (packageFragment.length() > 0 ? packageFragment + "." : "")
                 + getResourceBundleName(resource);
     }
 
     public void addBundleResource(IResource resource) {
         if (resource.isDerived()) {
             return;
         }
 
         String bundleName = getResourceBundleId(resource);
         Set<IResource> res;
 
         if (!resources.containsKey(bundleName)) {
             res = new HashSet<IResource>();
         } else {
             res = resources.get(bundleName);
         }
 
         res.add(resource);
         resources.put(bundleName, res);
         allBundles.put(bundleName, new HashSet<IResource>(res));
         bundleNames.put(bundleName, getResourceBundleName(resource));
 
         // Fire resource changed event
         ResourceBundleChangedEvent event = new ResourceBundleChangedEvent(
                 ResourceBundleChangedEvent.ADDED, bundleName,
                 resource.getProject());
         this.fireResourceBundleChangedEvent(bundleName, event);
     }
 
     protected void removeAllBundleResources(String bundleName) {
         unloadResourceBundle(bundleName);
         resources.remove(bundleName);
         // allBundles.remove(bundleName);
         listeners.remove(bundleName);
     }
 
     public void unloadResourceBundle(String name) {
         RBManager instance = RBManager.getInstance(project);
         instance.deleteMessagesBundleGroup(name);
     }
 
     public IMessagesBundleGroup getResourceBundle(String name) {
         RBManager instance = RBManager.getInstance(project);
         return instance.getMessagesBundleGroup(name);
     }
 
     public Collection<IResource> getResourceBundles(String bundleName) {
         return resources.get(bundleName);
     }
 
     public List<String> getResourceBundleNames() {
         List<String> returnList = new ArrayList<String>();
 
         Iterator<String> it = resources.keySet().iterator();
         while (it.hasNext()) {
             returnList.add(it.next());
         }
         return returnList;
     }
 
     public IResource getResourceFile(String file) {
         String regex = "^(.*?)" + "((_[a-z]{2,3})|(_[a-z]{2,3}_[A-Z]{2})"
                 + "|(_[a-z]{2,3}_[A-Z]{2}_\\w*))?(\\." + "properties" + ")$";
         String bundleName = file.replaceFirst(regex, "$1");
         IResource resource = null;
 
         for (IResource res : resources.get(bundleName)) {
             if (res.getName().equalsIgnoreCase(file)) {
                 resource = res;
                 break;
             }
         }
 
         return resource;
     }
 
     public void fireResourceBundleChangedEvent(String bundleName,
             ResourceBundleChangedEvent event) {
         List<IResourceBundleChangedListener> l = listeners.get(bundleName);
 
         if (l == null) {
             return;
         }
 
         for (IResourceBundleChangedListener listener : l) {
             listener.resourceBundleChanged(event);
         }
     }
 
     public void registerResourceBundleChangeListener(String bundleName,
             IResourceBundleChangedListener listener) {
         List<IResourceBundleChangedListener> l = listeners.get(bundleName);
         if (l == null) {
             l = new ArrayList<IResourceBundleChangedListener>();
         }
         l.add(listener);
         listeners.put(bundleName, l);
     }
 
     public void unregisterResourceBundleChangeListener(String bundleName,
             IResourceBundleChangedListener listener) {
         List<IResourceBundleChangedListener> l = listeners.get(bundleName);
         if (l == null) {
             return;
         }
         l.remove(listener);
         listeners.put(bundleName, l);
     }
 
     protected void detectResourceBundles() {
         try {
             project.accept(new ResourceBundleDetectionVisitor(getProject()));
 
             IProject[] fragments = FragmentProjectUtils.lookupFragment(project);
             if (fragments != null) {
                 for (IProject p : fragments) {
                     p.accept(new ResourceBundleDetectionVisitor(getProject()));
                 }
             }
         } catch (CoreException e) {
         }
     }
 
     public IProject getProject() {
         return project;
     }
 
     public List<String> getResourceBundleIdentifiers() {
         List<String> returnList = new ArrayList<String>();
 
         // TODO check other resource bundles that are available on the curren
         // class path
         Iterator<String> it = this.resources.keySet().iterator();
         while (it.hasNext()) {
             returnList.add(it.next());
         }
 
         return returnList;
     }
 
     public static List<String> getAllResourceBundleNames() {
         List<String> returnList = new ArrayList<String>();
 
         for (IProject p : getAllSupportedProjects()) {
             if (!FragmentProjectUtils.isFragment(p)) {
                 Iterator<String> it = getManager(p).resources.keySet()
                         .iterator();
                 while (it.hasNext()) {
                     returnList.add(p.getName() + "/" + it.next());
                 }
             }
         }
         return returnList;
     }
 
     public static Set<IProject> getAllSupportedProjects() {
         IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
                 .getProjects();
         Set<IProject> projs = new HashSet<IProject>();
 
         for (IProject p : projects) {
             try {
                 if (p.isOpen() && p.hasNature(NATURE_ID)) {
                     projs.add(p);
                 }
             } catch (CoreException e) {
                 // TODO Auto-generated catch block
                 Logger.logError(e);
             }
         }
         return projs;
     }
 
     public String getKeyHoverString(String rbName, String key) {
         try {
             RBManager instance = RBManager.getInstance(project);
             IMessagesBundleGroup bundleGroup = instance
                     .getMessagesBundleGroup(rbName);
             if (!bundleGroup.containsKey(key)) {
                 return null;
             }
 
             String hoverText = "<html><head></head><body>";
 
             for (IMessage message : bundleGroup.getMessages(key)) {
                 String displayName = message.getLocale() == null ? "Default"
                         : message.getLocale().getDisplayName();
                 String value = message.getValue();
                 hoverText += "<b><i>" + displayName + "</i></b><br/>"
                         + value.replace("\n", "<br/>") + "<br/><br/>";
             }
             return hoverText + "</body></html>";
         } catch (Exception e) {
             // silent catch
             return "";
         }
     }
 
     public boolean isKeyBroken(String rbName, String key) {
         IMessagesBundleGroup messagesBundleGroup = RBManager.getInstance(
                 project).getMessagesBundleGroup(rbName);
         if (messagesBundleGroup == null) {
             return true;
         } else {
             return !messagesBundleGroup.containsKey(key);
         }
 
         // if (!resourceBundles.containsKey(rbName))
         // return true;
         // return !this.isResourceExisting(rbName, key);
     }
 
     protected void excludeSingleResource(IResource res) {
         IResourceDescriptor rd = new ResourceDescriptor(res);
         org.eclipse.babel.tapiji.tools.core.ui.utils.EditorUtils
                 .deleteAuditMarkersForResource(res);
 
         // exclude resource
         excludedResources.add(rd);
         Collection<Object> changedExclusoins = new HashSet<Object>();
         changedExclusoins.add(res);
         fireResourceExclusionEvent(new ResourceExclusionEvent(changedExclusoins));
 
         // Check if the excluded resource represents a resource-bundle
         if (org.eclipse.babel.tapiji.tools.core.ui.utils.RBFileUtils
                 .isResourceBundleFile(res)) {
             String bundleName = getResourceBundleId(res);
             Set<IResource> resSet = resources.remove(bundleName);
             if (resSet != null) {
                 resSet.remove(res);
 
                 if (!resSet.isEmpty()) {
                     resources.put(bundleName, resSet);
                     unloadResource(bundleName, res);
                 } else {
                     rd.setBundleId(bundleName);
                     unloadResourceBundle(bundleName);
                     try {
                         res.getProject().build(
                                 IncrementalProjectBuilder.FULL_BUILD,
                                 BUILDER_ID, null, null);
                     } catch (CoreException e) {
                         Logger.logError(e);
                     }
                 }
 
                 fireResourceBundleChangedEvent(getResourceBundleId(res),
                         new ResourceBundleChangedEvent(
                                 ResourceBundleChangedEvent.EXCLUDED,
                                 bundleName, res.getProject()));
             }
         }
     }
 
     public void excludeResource(IResource res, IProgressMonitor monitor) {
         try {
             if (monitor == null) {
                 monitor = new NullProgressMonitor();
             }
 
             final List<IResource> resourceSubTree = new ArrayList<IResource>();
             res.accept(new IResourceVisitor() {
 
                 @Override
                 public boolean visit(IResource resource) throws CoreException {
                     Logger.logInfo("Excluding resource '"
                             + resource.getFullPath().toOSString() + "'");
                     resourceSubTree.add(resource);
                     return true;
                 }
 
             });
 
             // Iterate previously retrieved resource and exclude them from
             // Internationalization
             monitor.beginTask(
                     "Exclude resources from Internationalization context",
                     resourceSubTree.size());
             try {
                 for (IResource resource : resourceSubTree) {
                     excludeSingleResource(resource);
                     org.eclipse.babel.tapiji.tools.core.ui.utils.EditorUtils
                             .deleteAuditMarkersForResource(resource);
                     monitor.worked(1);
                 }
             } catch (Exception e) {
                 Logger.logError(e);
             } finally {
                 monitor.done();
             }
         } catch (CoreException e) {
             Logger.logError(e);
         }
     }
 
     public void includeResource(IResource res, IProgressMonitor monitor) {
         if (monitor == null) {
             monitor = new NullProgressMonitor();
         }
 
         final Collection<Object> changedResources = new HashSet<Object>();
         IResource resource = res;
 
         if (!excludedResources.contains(new ResourceDescriptor(res))) {
             while (!(resource instanceof IProject || resource instanceof IWorkspaceRoot)) {
                 if (excludedResources
                         .contains(new ResourceDescriptor(resource))) {
                     excludeResource(resource, monitor);
                     changedResources.add(resource);
                     break;
                 } else {
                     resource = resource.getParent();
                 }
             }
         }
 
         try {
             res.accept(new IResourceVisitor() {
 
                 @Override
                 public boolean visit(IResource resource) throws CoreException {
                     changedResources.add(resource);
                     return true;
                 }
             });
 
             monitor.beginTask("Add resources to Internationalization context",
                     changedResources.size());
             try {
                 for (Object r : changedResources) {
                     excludedResources.remove(new ResourceDescriptor(
                             (IResource) r));
                     monitor.worked(1);
                 }
 
             } catch (Exception e) {
                 Logger.logError(e);
             } finally {
                 monitor.done();
             }
         } catch (Exception e) {
             Logger.logError(e);
         }
 
         try {
             res.touch(null);
         } catch (CoreException e) {
             Logger.logError(e);
         }
 
         // Check if the included resource represents a resource-bundle
         if (org.eclipse.babel.tapiji.tools.core.ui.utils.RBFileUtils
                 .isResourceBundleFile(res)) {
             String bundleName = getResourceBundleId(res);
             boolean newRB = resources.containsKey(bundleName);
 
             this.addBundleResource(res);
             this.unloadResourceBundle(bundleName);
             // this.loadResourceBundle(bundleName);
 
             if (newRB) {
                 try {
                     resource.getProject().build(
                             IncrementalProjectBuilder.FULL_BUILD, BUILDER_ID,
                             null, null);
                 } catch (CoreException e) {
                     Logger.logError(e);
                 }
             }
             fireResourceBundleChangedEvent(getResourceBundleId(res),
                     new ResourceBundleChangedEvent(
                             ResourceBundleChangedEvent.INCLUDED, bundleName,
                             res.getProject()));
         }
 
         fireResourceExclusionEvent(new ResourceExclusionEvent(changedResources));
     }
 
     protected void fireResourceExclusionEvent(ResourceExclusionEvent event) {
         for (IResourceExclusionListener listener : exclusionListeners) {
             listener.exclusionChanged(event);
         }
     }
 
     public static boolean isResourceExcluded(IResource res) {
         IResource resource = res;
 
         if (!state_loaded) {
             IStateLoader stateLoader = getStateLoader();
             if (stateLoader != null) {
                 stateLoader.loadState();
             } else {
                 Logger.logError("State-Loader uninitialized! Unable to restore state.");
             }
         }
 
         boolean isExcluded = false;
 
         do {
             if (excludedResources.contains(new ResourceDescriptor(resource))) {
                 if (org.eclipse.babel.tapiji.tools.core.ui.utils.RBFileUtils
                         .isResourceBundleFile(resource)) {
                     Set<IResource> resources = allBundles
                             .remove(getResourceBundleName(resource));
                     if (resources == null) {
                         resources = new HashSet<IResource>();
                     }
                     resources.add(resource);
                     allBundles.put(getResourceBundleName(resource), resources);
                 }
 
                 isExcluded = true;
                 break;
             }
             resource = resource.getParent();
         } while (resource != null
                 && !(resource instanceof IProject || resource instanceof IWorkspaceRoot)
                 && checkResourceExclusionRoot);
 
         return isExcluded; // excludedResources.contains(new
         // ResourceDescriptor(res));
     }
 
     public IFile getRandomFile(String bundleName) {
         try {
             Collection<IMessagesBundle> messagesBundles = RBManager
                     .getInstance(project).getMessagesBundleGroup(bundleName)
                     .getMessagesBundles();
             IMessagesBundle bundle = messagesBundles.iterator().next();
             return FileUtils.getFile(bundle);
         } catch (Exception e) {
             Logger.logError(e);
         }
         return null;
     }
 
     @Deprecated
     protected static boolean isResourceExcluded(IProject project, String bname) {
         Iterator<IResourceDescriptor> itExcl = excludedResources.iterator();
         while (itExcl.hasNext()) {
             IResourceDescriptor rd = itExcl.next();
             if (project.getName().equals(rd.getProjectName())
                     && bname.equals(rd.getBundleId())) {
                 return true;
             }
         }
         return false;
     }
 
     public static ResourceBundleManager getManager(String projectName) {
         for (IProject p : getAllSupportedProjects()) {
             if (p.getName().equalsIgnoreCase(projectName)) {
                 // check if the projectName is a fragment and return the manager
                 // for the host
                 if (FragmentProjectUtils.isFragment(p)) {
                     return getManager(FragmentProjectUtils.getFragmentHost(p));
                 } else {
                     return getManager(p);
                 }
             }
         }
         return null;
     }
 
     public IFile getResourceBundleFile(String resourceBundle, Locale l) {
         IFile res = null;
         Set<IResource> resSet = resources.get(resourceBundle);
 
         if (resSet != null) {
             for (IResource resource : resSet) {
                Locale refLoc = NameUtils.getLocaleByName(resourceBundle,
                         resource.getName());
                 if (refLoc == null
                         && l == null
                         || (refLoc != null && refLoc.equals(l) || l != null
                                 && l.equals(refLoc))) {
                     res = resource.getProject().getFile(
                             resource.getProjectRelativePath());
                     break;
                 }
             }
         }
 
         return res;
     }
 
     public Set<IResource> getAllResourceBundleResources(String resourceBundle) {
         return allBundles.get(resourceBundle);
     }
 
     public void registerResourceExclusionListener(
             IResourceExclusionListener listener) {
         exclusionListeners.add(listener);
     }
 
     public void unregisterResourceExclusionListener(
             IResourceExclusionListener listener) {
         exclusionListeners.remove(listener);
     }
 
     public boolean isResourceExclusionListenerRegistered(
             IResourceExclusionListener listener) {
         return exclusionListeners.contains(listener);
     }
 
     public static void unregisterResourceExclusionListenerFromAllManagers(
             IResourceExclusionListener excludedResource) {
         for (ResourceBundleManager mgr : rbmanager.values()) {
             mgr.unregisterResourceExclusionListener(excludedResource);
         }
     }
 
     public void addResourceBundleEntry(String resourceBundleId, String key,
             Locale locale, String message) throws ResourceBundleException {
 
         RBManager instance = RBManager.getInstance(project);
         IMessagesBundleGroup bundleGroup = instance
                 .getMessagesBundleGroup(resourceBundleId);
         IMessage entry = bundleGroup.getMessage(key, locale);
 
         if (entry == null) {
             DirtyHack.setFireEnabled(false);
 
             IMessagesBundle messagesBundle = bundleGroup
                     .getMessagesBundle(locale);
             IMessage m = MessageFactory.createMessage(key, locale);
             m.setText(message);
             messagesBundle.addMessage(m);
 
             FileUtils.writeToFile(messagesBundle);
             instance.fireResourceChanged(messagesBundle);
 
             DirtyHack.setFireEnabled(true);
 
             // notify the PropertyKeySelectionTree
             instance.fireEditorChanged();
         }
     }
 
     public void saveResourceBundle(String resourceBundleId,
             IMessagesBundleGroup newBundleGroup) throws ResourceBundleException {
 
         // RBManager.getInstance().
     }
 
     public void removeResourceBundleEntry(String resourceBundleId,
             List<String> keys) throws ResourceBundleException {
 
         RBManager instance = RBManager.getInstance(project);
         IMessagesBundleGroup messagesBundleGroup = instance
                 .getMessagesBundleGroup(resourceBundleId);
 
         DirtyHack.setFireEnabled(false);
 
         for (String key : keys) {
             messagesBundleGroup.removeMessages(key);
         }
 
         instance.writeToFile(messagesBundleGroup);
 
         DirtyHack.setFireEnabled(true);
 
         // notify the PropertyKeySelectionTree
         instance.fireEditorChanged();
     }
 
     public boolean isResourceExisting(String bundleId, String key) {
         boolean keyExists = false;
         IMessagesBundleGroup bGroup = getResourceBundle(bundleId);
 
         if (bGroup != null) {
             keyExists = bGroup.isKey(key);
         }
 
         return keyExists;
     }
 
     public static void rebuildProject(IResource resource) {
         try {
             resource.touch(null);
         } catch (CoreException e) {
             Logger.logError(e);
         }
     }
 
     public Set<Locale> getProjectProvidedLocales() {
         Set<Locale> locales = new HashSet<Locale>();
 
         for (String bundleId : getResourceBundleNames()) {
             Set<Locale> rb_l = getProvidedLocales(bundleId);
             if (!rb_l.isEmpty()) {
                 Object[] bundlelocales = rb_l.toArray();
                 for (Object l : bundlelocales) {
                     /* TODO check if useful to add the default. For now the default is not
                      * being used and is ignored as it is null */
                     if (l != null && !locales.contains(l)) {
                         locales.add((Locale) l);
                     }
                 }
             }
         }
         return locales;
     }
 
     public static IStateLoader getStateLoader() {
         if (stateLoader == null) {
 
             IExtensionPoint extp = Platform.getExtensionRegistry()
                     .getExtensionPoint(
                             "org.eclipse.babel.tapiji.tools.core"
                                     + ".stateLoader");
             IConfigurationElement[] elements = extp.getConfigurationElements();
 
             if (elements.length != 0) {
                 try {
                     stateLoader = (IStateLoader) elements[0]
                             .createExecutableExtension("class");
                 } catch (CoreException e) {
                     Logger.logError(e);
                 }
             }
         }
 
         return stateLoader;
 
     }
 
     public static void saveManagerState() {
         IStateLoader stateLoader = getStateLoader();
         if (stateLoader != null) {
             stateLoader.saveState();
         }
     }
 
 }
