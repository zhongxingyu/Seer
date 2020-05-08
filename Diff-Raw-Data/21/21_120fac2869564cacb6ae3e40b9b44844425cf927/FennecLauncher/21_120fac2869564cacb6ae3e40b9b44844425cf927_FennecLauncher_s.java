 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Eclipse Public License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.eclipse.org/org/documents/epl-v10.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.mozilla.ide.eclipse.fennec;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import com.android.SdkConstants;
 import com.android.ddmlib.AndroidDebugBridge;
 import com.android.ide.common.xml.ManifestData;
 import com.android.ide.eclipse.adt.AdtConstants;
 import com.android.ide.eclipse.adt.AdtPlugin;
 import com.android.ide.eclipse.adt.internal.launch.AndroidLaunch;
 import com.android.ide.eclipse.adt.internal.launch.AndroidLaunchConfiguration;
 import com.android.ide.eclipse.adt.internal.launch.AndroidLaunchController;
 import com.android.ide.eclipse.adt.internal.launch.LaunchConfigDelegate;
 import com.android.ide.eclipse.adt.internal.project.AndroidManifestHelper;
 import com.android.ide.eclipse.adt.internal.project.BaseProjectHelper;
 import com.android.ide.eclipse.adt.internal.project.ProjectHelper;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 
 /**
  * Implementation of an eclipse LaunchConfigurationDelegate to launch android
  * application in debug.
  */
 // XXX: only use this launcher if project is fennec project (create project XML
 // config entry?)
 @SuppressWarnings("restriction")
 public class FennecLauncher extends LaunchConfigDelegate {
     
     private static final int INVALID_DEBUG_PORT = -1;
 
     /* (non-Javadoc)
      * @see com.android.ide.eclipse.adt.internal.launch.LaunchConfigDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
      */
     @Override
     public void launch(ILaunchConfiguration configuration, String mode,
             ILaunch launch, IProgressMonitor monitor) throws CoreException {
         // We need to check if it's a standard launch or if it's a launch
         // to debug an application already running.
         
         int debugPort = -1;
         try {
             Method getPortForConfig = AndroidLaunchController.class.getDeclaredMethod("getPortForConfig", ILaunchConfiguration.class);
             getPortForConfig.setAccessible(true);
             debugPort = (int) getPortForConfig.invoke(null, configuration);
         } catch (IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException | NoSuchMethodException e) {
             e.printStackTrace();
         }
 
         // get the project
         IProject project = getProject(configuration);
 
         // first we make sure the launch is of the proper type
         AndroidLaunch androidLaunch = null;
         if (launch instanceof AndroidLaunch) {
             androidLaunch = (AndroidLaunch)launch;
         } else {
             // wrong type, not sure how we got there, but we don't do
             // anything else
             AdtPlugin.printErrorToConsole(project, "Wrong Launch Type!");
             return;
         }
 
         // if we have a valid debug port, this means we're debugging an app
         // that's already launched.
         if (debugPort != INVALID_DEBUG_PORT) {
             AndroidLaunchController.launchRemoteDebugger(debugPort, androidLaunch, monitor);
             return;
         }
 
         if (project == null) {
             AdtPlugin.printErrorToConsole("Couldn't get project object!");
             androidLaunch.stopLaunch();
             return;
         }
 
         // disabled for Fennec - can we do our own incremental build?
         //ProjectHelper.doFullIncrementalDebugBuild(project, monitor);
 
         // check if the project has errors, and abort in this case.
         if (ProjectHelper.hasError(project, true)) {
             AdtPlugin.displayError("Android Launch",
                     "Your project contains error(s), please fix them before running your application.");
             return;
         }
 
         AdtPlugin.printToConsole(project, "------------------------------"); //$NON-NLS-1$
         AdtPlugin.printToConsole(project, "Android Launch!");
 
         // check if the project is using the proper sdk.
         // if that throws an exception, we simply let it propagate to the caller.
         if (checkAndroidProject(project) == false) {
             AdtPlugin.printErrorToConsole(project, "Project is not an Android Project. Aborting!");
             androidLaunch.stopLaunch();
             return;
         }
         
         // Check adb status and abort if needed.
         AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();
         if (bridge == null || bridge.isConnected() == false) {
             try {
                 int connections = -1;
                 int restarts = -1;
                 if (bridge != null) {
                     connections = bridge.getConnectionAttemptCount();
                     restarts = bridge.getRestartAttemptCount();
                 }
 
                 // if we get -1, the device monitor is not even setup (anymore?).
                 // We need to ask the user to restart eclipse.
                 // This shouldn't happen, but it's better to let the user know in case it does.
                 if (connections == -1 || restarts == -1) {
                     AdtPlugin.printErrorToConsole(project,
                             "The connection to adb is down, and a severe error has occured.",
                             "You must restart adb and Eclipse.",
                             String.format(
                                     "Please ensure that adb is correctly located at '%1$s' and can be executed.",
                                     AdtPlugin.getOsAbsoluteAdb()));
                     return;
                 }
 
                 if (restarts == 0) {
                     AdtPlugin.printErrorToConsole(project,
                             "Connection with adb was interrupted.",
                             String.format("%1$s attempts have been made to reconnect.", connections),
                             "You may want to manually restart adb from the Devices view.");
                 } else {
                     AdtPlugin.printErrorToConsole(project,
                             "Connection with adb was interrupted, and attempts to reconnect have failed.",
                             String.format("%1$s attempts have been made to restart adb.", restarts),
                             "You may want to manually restart adb from the Devices view.");
 
                 }
                 return;
             } finally {
                 androidLaunch.stopLaunch();
             }
         }
 
         // since adb is working, we let the user know
         // TODO have a verbose mode for launch with more info (or some of the less useful info we now have).
         AdtPlugin.printToConsole(project, "adb is running normally.");
 
         // make a config class
         AndroidLaunchConfiguration config = new AndroidLaunchConfiguration();
 
         // fill it with the config coming from the ILaunchConfiguration object
         config.set(configuration);
 
         // get the launch controller singleton
         AndroidLaunchController controller = AndroidLaunchController.getInstance();
 
        // Copy the moz-generated package. /fennec/App.apk should be symlinked
         // to the objdir apk.
         // XXX: make this location configurable
        IFile file = project.getFile("/fennec/App.apk");
        IPath dest = getApplicationPackage(project);
         if (dest == null) {
             androidLaunch.stopLaunch();
             return;
         }
        file.copy(dest, true, null);
 
         // get the application package
         IFile applicationPackage = ProjectHelper.getApplicationPackage(project);
         if (applicationPackage == null) {
             androidLaunch.stopLaunch();
             return;
         }
 
         // we need some information from the manifest
         ManifestData manifestData = AndroidManifestHelper.parseForData(project);
 
         if (manifestData == null) {
             AdtPlugin.printErrorToConsole(project, "Failed to parse AndroidManifest: aborting!");
             androidLaunch.stopLaunch();
             return;
         }
 
         doLaunch(configuration, mode, monitor, project, androidLaunch, config, controller,
                 applicationPackage, manifestData);
     }
 
     /**
      * Returns the IProject object matching the name found in the configuration
      * object under the name
      * <code>IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME</code>
      * @param configuration
      * @return The IProject object or null
      */
     private IProject getProject(ILaunchConfiguration configuration){
         // get the project name from the config
         String projectName;
         try {
             projectName = configuration.getAttribute(
                     IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
         } catch (CoreException e) {
             return null;
         }
 
         // get the current workspace
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
 
         // and return the project with the name from the config
         return workspace.getRoot().getProject(projectName);
     }
 
     /**
      * Checks the project is an android project.
      * @param project The project to check
      * @return true if the project is an android SDK.
      * @throws CoreException
      */
     private boolean checkAndroidProject(IProject project) throws CoreException {
         // check if the project is a java and an android project.
         if (project.hasNature(JavaCore.NATURE_ID) == false) {
             String msg = String.format("%1$s is not a Java project!", project.getName());
             AdtPlugin.displayError("Android Launch", msg);
             return false;
         }
 
         if (project.hasNature(AdtConstants.NATURE_DEFAULT) == false) {
             String msg = String.format("%1$s is not an Android project!", project.getName());
             AdtPlugin.displayError("Android Launch", msg);
             return false;
         }
 
         return true;
     }
     
     /**
      * Returns the android package file as an IFile object for the specified
      * project.
      * @param project The project
      * @return The android package as an IFile object or null if not found.
      */
    private static IPath getApplicationPackage(IProject project) {
         // get the output folder
         IFolder outputLocation = BaseProjectHelper.getAndroidOutputFolder(project);
 
         if (outputLocation == null) {
             AdtPlugin.printErrorToConsole(project,
                     "Failed to get the output location of the project. Check build path properties"
                     );
            AdtPlugin.displayError("BRN", "FAIL");
             return null;
         }
 
 
         // get the package path
         String packageName = project.getName() + SdkConstants.DOT_ANDROID_PACKAGE;
        return outputLocation.getFullPath().append(packageName);
     }
 }
