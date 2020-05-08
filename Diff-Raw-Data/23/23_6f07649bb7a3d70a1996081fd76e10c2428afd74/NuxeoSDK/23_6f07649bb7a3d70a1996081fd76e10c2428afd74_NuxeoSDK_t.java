 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.sdk;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.sdk.comp.ComponentIndex;
 import org.nuxeo.ide.sdk.comp.ComponentIndexManager;
 import org.nuxeo.ide.sdk.deploy.Deployment;
 import org.nuxeo.ide.sdk.index.Index;
 import org.nuxeo.ide.sdk.server.ServerController;
 import org.nuxeo.ide.sdk.ui.NuxeoNature;
 import org.nuxeo.ide.sdk.ui.SDKClassPathBuilder;
 import org.nuxeo.ide.sdk.ui.SDKClassPathContainerInitializer;
 import org.osgi.service.prefs.BackingStoreException;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class NuxeoSDK {
 
     /**
      * The Nuxeo SDK instance on the active Eclipse Workspace.
      */
     private static volatile NuxeoSDK instance = null;
 
     private static ListenerList sdkChangedListeners = new ListenerList();
 
     private static ListenerList deploymentChangedListeners = new ListenerList();
 
     static void initialize() throws BackingStoreException {
         SDKInfo info = SDKRegistry.getDefaultSDK();
         if (info != null) {
             instance = new NuxeoSDK(info);
         }
     }
 
     public static NuxeoSDK getDefault() {
         return instance;
     }
 
     public static NuxeoSDK setDefault(SDKInfo info) {
         boolean changed = false;
         NuxeoSDK sdk = getDefault();
         if (sdk == null) {
             if (info != null) {
                 sdk = new NuxeoSDK(info);
                 changed = true;
             }
         } else {
             if (info == null) {
                 sdk = null;
                 changed = true;
             } else {
                 if (!sdk.info.equals(info)) {
                     sdk.destroy();
                     sdk = new NuxeoSDK(info);
                     changed = true;
                 }
             }
         }
         if (changed) {
             instance = sdk;
             reload();
         }
         return sdk;
     }
 
     public static void reload() {
         NuxeoSDK sdk = instance;
         fireSDKChanged(sdk);
         if (sdk != null) {
            synchronized (sdk) {
                sdk.compIndexMgr.flushCache();
                sdk.classpath = null;
                sdk.testClasspath = null;
                sdk.index = null;
                sdk.testIndex = null;
             }
         }
        try {
            reloadSDKClasspathContainer();
        } catch (CoreException e) {
            UI.showError("Failed to rebuild Nuxeo Projects", e);
        }
     }
 
     public static void dispose() {
         sdkChangedListeners = null;
         instance = null;
     }
 
     public static void addSDKChangedListener(SDKChangedListener listener) {
         sdkChangedListeners.add(listener);
     }
 
     public static void removeSDKChangedListener(SDKChangedListener listener) {
         sdkChangedListeners.remove(listener);
     }
 
     private static void fireSDKChanged(NuxeoSDK sdk) {
         for (Object listener : sdkChangedListeners.getListeners()) {
             ((SDKChangedListener) listener).handleSDKChanged(sdk);
         }
     }
 
     public static void addDeploymentChangedListener(
             DeploymentChangedListener listener) {
         deploymentChangedListeners.add(listener);
     }
 
     public static void removeDeploymentChangedListener(
             DeploymentChangedListener listener) {
         deploymentChangedListeners.remove(listener);
     }
 
     public static void fireDeployementChanged(NuxeoSDK sdk,
             Deployment deployment) {
         for (Object listener : deploymentChangedListeners.getListeners()) {
             ((DeploymentChangedListener) listener).deploymentChanged(sdk,
                     deployment);
         }
     }
 
     protected SDKInfo info;
 
     protected File root;
 
     protected ServerController controller;
 
     protected ComponentIndexManager compIndexMgr;
 
     protected volatile Index index;
 
     protected volatile Index testIndex;
 
     /**
      * SDK classpath cache
      */
     protected volatile IClasspathEntry[] classpath;
 
     protected volatile IClasspathEntry[] testClasspath;
 
     // /**
     // * Class index (class -> artifact). ONly initialized at demand. Used to
     // * generate maven dependencies.
     // */
     // protected volatile Index index;
 
     public NuxeoSDK(SDKInfo info) {
         this.info = info;
         this.root = info.getInstallDirectory();
         this.controller = new ServerController(info);
         this.compIndexMgr = new ComponentIndexManager(this.root);
         controller.addServerLifeCycleListener(compIndexMgr);
     }
 
     protected void destroy() {
         compIndexMgr.destroy(); // TODO may be use a listener here?
         compIndexMgr = null;
         // TODO stop controller or it is using listeners?
         controller = null;
         info = null;
         root = null;
     }
 
     public ComponentIndexManager getComponentIndexManager() {
         return compIndexMgr;
     }
 
     public File getInstallDirectory() {
         return root;
     }
 
     public SDKInfo getInfo() {
         return info;
     }
 
     public void flushComponentIndex() {
         compIndexMgr.flushCache();
     }
 
     public ComponentIndex getComponentIndex() {
         return getComponentIndexManager().getIndex();
     }
 
     public Index getArtifactIndex() {
         Index _index = index;
         if (_index == null) {
             synchronized (this) {
                 index = Index.load(new File(root, SDKInfo.SDK_ARTIFACTS_PATH),
                         SDKInfo.SDK_ARTIFACTS_FILE);
                 _index = index;
             }
         }
         return _index;
     }
 
     public Index getTestArtifactIndex() {
         Index _index = testIndex;
         if (_index == null) {
             synchronized (this) {
                 testIndex = Index.load(new File(root,
                         SDKInfo.SDK_TEST_ARTIFACTS_PATH),
                         SDKInfo.SDK_TEST_ARTIFACTS_FILE);
                 _index = testIndex;
             }
         }
         return _index;
     }
 
     public String getVersion() {
         return info.getVersion();
     }
 
     public String getLocation() {
         return info.getPath();
     }
 
     public ServerController getController() {
         return controller;
     }
 
     public File getLibDir() {
         return new File(root, "nxserver/lib");
     }
 
     public File getSysLibDir() {
         return new File(root, "lib");
     }
 
     public File getBundlesDir() {
         return new File(root, "nxserver/bundles");
     }
 
     public File getLibSrcDir() {
         return new File(root, "sdk/sources");
     }
 
     public File getBundlesSrcDir() {
         return new File(root, "sdk/sources");
     }
 
     public File getTestsDir() {
         return new File(root, "sdk/tests");
     }
 
     public IClasspathEntry[] getClasspathEntries() {
         IClasspathEntry[] cache = classpath;
         if (cache == null) {
             synchronized (this) {
                 cache = classpath;
                 if (cache == null) {
                     classpath = SDKClassPathBuilder.build(this);
                     cache = classpath;
                 }
             }
         }
         return cache;
     }
 
     public IClasspathEntry[] getTestClasspathEntries() {
         IClasspathEntry[] cache = testClasspath;
         if (cache == null) {
             synchronized (this) {
                 cache = testClasspath;
                 if (cache == null) {
                     testClasspath = SDKClassPathBuilder.buildTests(this);
                     cache = testClasspath;
                 }
             }
         }
         return cache;
     }
 
     /**
      * Reload projects on server
      */
     public void reloadDeployment(Deployment deployment) throws Exception {
         fireDeployementChanged(this, deployment);
         controller.writeDevBundles(deployment);
     }
 
     public static void rebuildProjects() {
         doBuildOperation(IncrementalProjectBuilder.FULL_BUILD, null);
     }
 
     public static void rebuildNuxeoProjects() throws CoreException {
         List<IProject> nxProjects = getNuxeoProjects();
         if (!nxProjects.isEmpty()) {
             doBuildOperation(IncrementalProjectBuilder.FULL_BUILD, nxProjects);
         }
     }
 
     public static void rebuildNuxeoProject(IProject project)
             throws CoreException {
         List<IProject> nxProjects = getNuxeoProjects();
         if (!nxProjects.isEmpty()) {
             doBuildOperation(IncrementalProjectBuilder.FULL_BUILD,
                     Collections.singletonList(project));
         }
     }
 
     private static void doBuildOperation(final int buildType,
             final List<IProject> projects) {
         Job buildJob = new Job("Building Workspace") {
             protected IStatus run(IProgressMonitor monitor) {
                 int ticks = 100;
                 String message = "Rebuilding All ...";
                 if (projects != null) {
                     ticks = projects.size();
                     message = "Rebuilding Nuxeo Projects ...";
                 }
                 monitor.beginTask(message, ticks);
                 try {
                     if (projects == null) {
                         ResourcesPlugin.getWorkspace().build(buildType,
                                 new SubProgressMonitor(monitor, 100));
                     } else {
                         for (IProject project : projects) {
                             project.build(buildType, new SubProgressMonitor(
                                     monitor, 1));
                         }
                     }
                 } catch (CoreException e) {
                     return e.getStatus();
                 } finally {
                     monitor.done();
                 }
                 return Status.OK_STATUS;
             }
 
             public boolean belongsTo(Object family) {
                 return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
             }
         };
         buildJob.setUser(true);
         buildJob.schedule();
     }
 
     public static List<IProject> getNuxeoProjects() throws CoreException {
         ArrayList<IProject> nxProjects = new ArrayList<IProject>();
         for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
             if (project.hasNature(NuxeoNature.ID)) {
                 nxProjects.add(project);
             }
         }
         return nxProjects;
     }
 
     public static List<IJavaProject> getNuxeoJavaProjects()
             throws CoreException {
         ArrayList<IJavaProject> nxProjects = new ArrayList<IJavaProject>();
         for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
             if (project.isOpen() && project.hasNature(NuxeoNature.ID)) {
                 nxProjects.add(JavaCore.create(project));
             }
         }
         return nxProjects;
     }
 
     public static void reloadSDKClasspathContainer() throws CoreException {
         List<IJavaProject> nxProjects = getNuxeoJavaProjects();
         if (!nxProjects.isEmpty()) {
             SDKClassPathContainerInitializer initializer = new SDKClassPathContainerInitializer();
             initializer.initialize(nxProjects.toArray(new IJavaProject[nxProjects.size()]));
         }
     }
 
     public URL getRemoteLocation(String path) {
         return info.getRemoteLocation(path);
     }
 
     public String getPid() throws IOException {
         return info.getPid();
     }
 
 }
