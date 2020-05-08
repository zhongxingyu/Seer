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
 import org.nuxeo.ide.common.IOUtils;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.sdk.deploy.Deployment;
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
 
     private static ListenerList listeners = new ListenerList();
 
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
                     sdk = new NuxeoSDK(info);
                     changed = true;
                 }
             }
         }
         if (changed) {
             instance = sdk;
             fireSDKChanged(sdk);
             try {
                 reloadSDKClasspathContainer();
             } catch (CoreException e) {
                 UI.showError("Failed to rebuild Nuxeo Projects", e);
             }
         }
         return sdk;
     }
 
     public static void reload() {
         NuxeoSDK sdk = instance;
         if (sdk != null) {
             try {
                 synchronized (sdk) {
                     sdk.classpath = null;
                 }
                 reloadSDKClasspathContainer();
             } catch (CoreException e) {
                 UI.showError("Failed to rebuild Nuxeo Projects", e);
             }
         }
     }
 
     public static void dispose() {
         listeners = null;
         instance = null;
     }
 
     public static void addSDKChangedListener(SDKChangedListener listener) {
         listeners.add(listener);
     }
 
     public static void removeSDKChangedListener(SDKChangedListener listener) {
         listeners.remove(listener);
     }
 
     private static void fireSDKChanged(NuxeoSDK sdk) {
         for (Object listener : listeners.getListeners()) {
             ((SDKChangedListener) listener).handleSDKChanged(sdk);
         }
     }
 
     protected SDKInfo info;
 
     protected File root;
 
     /**
      * SDK classpath cache
      */
     protected volatile IClasspathEntry[] classpath;
 
     // /**
     // * Class index (class -> artifact). ONly initialized at demand. Used to
     // * generate maven dependencies.
     // */
     // protected volatile Index index;
 
     protected ServerController server;
 
     public NuxeoSDK(SDKInfo info) {
         this.info = info;
         this.root = info.getInstallDirectory();
     }
 
     public File getInstallDirectory() {
         return root;
     }
 
     public SDKInfo getInfo() {
         return info;
     }
 
     public String getVersion() {
         return info.getVersion();
     }
 
     public String getLocation() {
         return info.getPath();
     }
 
     public ServerController getServer() {
         return new ServerController(info);
     }
 
     public File getLibDir() {
         return new File(root, "nxserver/lib");
     }
 
     public File getBundlesDir() {
         return new File(root, "nxserver/bundles");
     }
 
     public File getLibSrcDir() {
         return new File(root, "nxserver/sdk/sources");
     }
 
     public File getBundlesSrcDir() {
         return new File(root, "nxserver/sdk/sources");
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
 
     /**
      * Reload projects on server
      */
     public void reloadDeployment(Deployment deployment) throws Exception {
         File file = new File(root, "nxserver/dev.bundles");
         String content = deployment.getContentAsString();
         IOUtils.writeFile(file, content);
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
 
 }
