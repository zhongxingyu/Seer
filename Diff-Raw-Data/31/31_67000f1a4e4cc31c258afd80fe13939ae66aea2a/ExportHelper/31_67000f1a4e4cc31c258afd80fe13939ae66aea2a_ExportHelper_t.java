 /*
  * Copyright (C) 2008 The Android Open Source Project
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
 
 package com.android.ide.eclipse.adt.internal.project;
 
 import com.android.ide.eclipse.adt.AdtConstants;
 import com.android.ide.eclipse.adt.AdtPlugin;
 import com.android.ide.eclipse.adt.AndroidPrintStream;
 import com.android.ide.eclipse.adt.internal.build.BuildHelper;
 import com.android.ide.eclipse.adt.internal.build.DexException;
 import com.android.ide.eclipse.adt.internal.build.NativeLibInJarException;
 import com.android.ide.eclipse.adt.internal.build.ProguardExecException;
 import com.android.ide.eclipse.adt.internal.build.ProguardResultException;
 import com.android.ide.eclipse.adt.internal.sdk.ProjectState;
 import com.android.ide.eclipse.adt.internal.sdk.Sdk;
 import com.android.ide.eclipse.adt.io.IFileWrapper;
 import com.android.sdklib.SdkConstants;
 import com.android.sdklib.build.ApkCreationException;
 import com.android.sdklib.build.DuplicateFileException;
 import com.android.sdklib.internal.project.ProjectProperties;
 import com.android.sdklib.xml.AndroidManifest;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.security.PrivateKey;
 import java.security.cert.X509Certificate;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 
 /**
  * Export helper to export release version of APKs.
  */
 public final class ExportHelper {
 
     private final static String TEMP_PREFIX = "android_";  //$NON-NLS-1$
 
     /**
      * Exports a release version of the application created by the given project.
      * @param project the project to export
      * @param outputFile the file to write
      * @param key the key to used for signing. Can be null.
      * @param certificate the certificate used for signing. Can be null.
      * @param monitor
      */
     public static void exportReleaseApk(IProject project, File outputFile, PrivateKey key,
             X509Certificate certificate, IProgressMonitor monitor) throws CoreException {
 
         // the export, takes the output of the precompiler & Java builders so it's
         // important to call build in case the auto-build option of the workspace is disabled.
         // Also enable post compilation and dependency building
         ProjectHelper.build(project, monitor, true, true);
 
         // if either key or certificate is null, ensure the other is null.
         if (key == null) {
             certificate = null;
         } else if (certificate == null) {
             key = null;
         }
 
         try {
             // check if the manifest declares debuggable as true. While this is a release build,
             // debuggable in the manifest will override this and generate a debug build
             IResource manifestResource = project.findMember(SdkConstants.FN_ANDROID_MANIFEST_XML);
             if (manifestResource.getType() != IResource.FILE) {
                 throw new CoreException(new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                         String.format("%1$s missing.", SdkConstants.FN_ANDROID_MANIFEST_XML)));
             }
 
             IFileWrapper manifestFile = new IFileWrapper((IFile) manifestResource);
             boolean debugMode = AndroidManifest.getDebuggable(manifestFile);
 
             AndroidPrintStream fakeStream = new AndroidPrintStream(null, null, new OutputStream() {
                 @Override
                 public void write(int b) throws IOException {
                     // do nothing
                 }
             });
 
             BuildHelper helper = new BuildHelper(project,
                     fakeStream, fakeStream,
                     debugMode, false /*verbose*/);
 
             // get the list of library projects
             ProjectState projectState = Sdk.getProjectState(project);
             List<IProject> libProjects = projectState.getFullLibraryProjects();
 
             // Step 1. Package the resources.
 
             // tmp file for the packaged resource file. To not disturb the incremental builders
             // output, all intermediary files are created in tmp files.
             File resourceFile = File.createTempFile(TEMP_PREFIX, AdtConstants.DOT_RES);
             resourceFile.deleteOnExit();
 
             // Make sure the PNG crunch cache is up to date
             helper.updateCrunchCache();
 
             // package the resources.
             helper.packageResources(
                     project.getFile(SdkConstants.FN_ANDROID_MANIFEST_XML),
                     libProjects,
                     null,   // res filter
                     0,      // versionCode
                     resourceFile.getParent(),
                     resourceFile.getName());
 
             // Step 2. Convert the byte code to Dalvik bytecode
 
             // tmp file for the packaged resource file.
             File dexFile = File.createTempFile(TEMP_PREFIX, AdtConstants.DOT_DEX);
             dexFile.deleteOnExit();
 
             ProjectState state = Sdk.getProjectState(project);
             String proguardConfig = state.getProperties().getProperty(
                     ProjectProperties.PROPERTY_PROGUARD_CONFIG);
 
             boolean runProguard = false;
             File proguardConfigFile = null;
             if (proguardConfig != null && proguardConfig.length() > 0) {
                 proguardConfigFile = new File(proguardConfig);
                 if (proguardConfigFile.isAbsolute() == false) {
                     proguardConfigFile = new File(project.getLocation().toFile(), proguardConfig);
                 }
                 runProguard = proguardConfigFile.isFile();
             }
 
             String[] dxInput;
 
             if (runProguard) {
                 // the output of the main project (and any java-only project dependency)
                 String[] projectOutputs = helper.getProjectJavaOutputs();
 
                 // create a jar from the output of these projects
                 File inputJar = File.createTempFile(TEMP_PREFIX, AdtConstants.DOT_JAR);
                 inputJar.deleteOnExit();
 
                 JarOutputStream jos = new JarOutputStream(new FileOutputStream(inputJar));
                 for (String po : projectOutputs) {
                     File root = new File(po);
                     if (root.exists()) {
                         addFileToJar(jos, root, root);
                     }
                 }
                 jos.close();
 
                 // get the other jar files
                 String[] jarFiles = helper.getCompiledCodePaths(false /*includeProjectOutputs*/,
                         null /*resourceMarker*/);
 
                 // destination file for proguard
                 File obfuscatedJar = File.createTempFile(TEMP_PREFIX, AdtConstants.DOT_JAR);
                 obfuscatedJar.deleteOnExit();
 
                 // run proguard
                 helper.runProguard(proguardConfigFile, inputJar, jarFiles, obfuscatedJar,
                         new File(project.getLocation().toFile(), SdkConstants.FD_PROGUARD));
 
                 // dx input is proguard's output
                 dxInput = new String[] { obfuscatedJar.getAbsolutePath() };
             } else {
                 // no proguard, simply get all the compiled code path: project output(s) +
                 // jar file(s)
                 dxInput = helper.getCompiledCodePaths(true /*includeProjectOutputs*/,
                         null /*resourceMarker*/);
             }
 
             IJavaProject javaProject = JavaCore.create(project);
             List<IProject> javaProjects = ProjectHelper.getReferencedProjects(project);
             List<IJavaProject> referencedJavaProjects = BuildHelper.getJavaProjects(
                     javaProjects);
 
             helper.executeDx(javaProject, dxInput, dexFile.getAbsolutePath());
 
             // Step 3. Final package
 
             helper.finalPackage(
                     resourceFile.getAbsolutePath(),
                     dexFile.getAbsolutePath(),
                     outputFile.getAbsolutePath(),
                     javaProject,
                     libProjects,
                     referencedJavaProjects,
                     key,
                     certificate,
                     null); //resourceMarker
 
             // success!
         } catch (CoreException e) {
             throw e;
         } catch (ProguardResultException e) {
             String msg = String.format("Proguard returned with error code %d. See console",
                     e.getErrorCode());
             AdtPlugin.printErrorToConsole(project, msg);
             AdtPlugin.printErrorToConsole(project, (Object[]) e.getOutput());
             throw new CoreException(new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                     msg, e));
         } catch (ProguardExecException e) {
             String msg = String.format("Failed to run proguard: %s", e.getMessage());
             AdtPlugin.printErrorToConsole(project, msg);
             throw new CoreException(new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                     msg, e));
         } catch (DuplicateFileException e) {
             String msg = String.format(
                     "Found duplicate file for APK: %1$s\nOrigin 1: %2$s\nOrigin 2: %3$s",
                     e.getArchivePath(), e.getFile1(), e.getFile2());
             AdtPlugin.printErrorToConsole(project, msg);
             throw new CoreException(new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                     e.getMessage(), e));
         } catch (NativeLibInJarException e) {
             String msg = e.getMessage();
 
             AdtPlugin.printErrorToConsole(project, msg);
             AdtPlugin.printErrorToConsole(project, (Object[]) e.getAdditionalInfo());
             throw new CoreException(new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                     e.getMessage(), e));
         } catch (DexException e) {
             throw new CoreException(new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                     e.getMessage(), e));
         } catch (ApkCreationException e) {
             throw new CoreException(new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                     e.getMessage(), e));
         } catch (Exception e) {
             throw new CoreException(new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                     "Failed to export application", e));
         }
     }
 
     /**
      * Exports an unsigned release APK after prompting the user for a location.
      *
      * <strong>Must be called from the UI thread.</strong>
      *
      * @param project the project to export
      */
     public static void exportUnsignedReleaseApk(final IProject project) {
         Shell shell = Display.getCurrent().getActiveShell();
 
         // create a default file name for the apk.
         String fileName = project.getName() + AdtConstants.DOT_ANDROID_PACKAGE;
 
         // Pop up the file save window to get the file location
         FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
 
         fileDialog.setText("Export Project");
         fileDialog.setFileName(fileName);
 
         final String saveLocation = fileDialog.open();
         if (saveLocation != null) {
             new Job("Android Release Export") {
                 @Override
                 protected IStatus run(IProgressMonitor monitor) {
                     try {
                         exportReleaseApk(project,
                                 new File(saveLocation),
                                 null, //key
                                 null, //certificate
                                 monitor);
 
                         // this is unsigned export. Let's tell the developers to run zip align
                         AdtPlugin.displayWarning("Android IDE Plug-in", String.format(
                                 "An unsigned package of the application was saved at\n%1$s\n\n" +
                                 "Before publishing the application you will need to:\n" +
                                 "- Sign the application with your release key,\n" +
                                 "- run zipalign on the signed package. ZipAlign is located in <SDK>/tools/\n\n" +
                                 "Aligning applications allows Android to use application resources\n" +
                                 "more efficiently.", saveLocation));
 
                         return Status.OK_STATUS;
                     } catch (CoreException e) {
                         AdtPlugin.displayError("Android IDE Plug-in", String.format(
                                 "Error exporting application:\n\n%1$s", e.getMessage()));
                         return e.getStatus();
                     }
                 }
             }.schedule();
         }
     }
 
     /**
      * Adds a file to a jar file.
      * The <var>rootDirectory</var> dictates the path of the file inside the jar file. It must be
      * a parent of <var>file</var>.
      * @param jar the jar to add the file to
      * @param file the file to add
      * @param rootDirectory the rootDirectory.
      * @throws IOException
      */
     private static void addFileToJar(JarOutputStream jar, File file, File rootDirectory)
             throws IOException {
         if (file.isDirectory()) {
             for (File child: file.listFiles()) {
                 addFileToJar(jar, child, rootDirectory);
             }
 
         } else if (file.isFile()) {
             // check the extension
             String name = file.getName();
             if (name.toLowerCase().endsWith(AdtConstants.DOT_CLASS) == false) {
                 return;
             }
 
             String rootPath = rootDirectory.getAbsolutePath();
             String path = file.getAbsolutePath();
             path = path.substring(rootPath.length()).replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
             if (path.charAt(0) == '/') {
                 path = path.substring(1);
             }
 
             JarEntry entry = new JarEntry(path);
             entry.setTime(file.lastModified());
             jar.putNextEntry(entry);
 
             // put the content of the file.
             byte[] buffer = new byte[1024];
             int count;
             BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             while ((count = bis.read(buffer)) != -1) {
                 jar.write(buffer, 0, count);
             }
             jar.closeEntry();
         }
     }
 }
