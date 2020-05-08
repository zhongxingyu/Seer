 /*
  * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 package org.nuxeo.ide.webengine.server;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.ILaunchGroup;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.nuxeo.ide.webengine.Nuxeo;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  *
  */
 public class Launcher implements IJavaLaunchConfigurationConstants {
 
     public static String MAIN = "org.nuxeo.runtime.launcher.Main";
     public static String JAVA_OPTS = "-Ddebug=true -Djava.rmi.server.RMIClassLoaderSpi=org.nuxeo.runtime.launcher.NuxeoRMIClassLoader -Dsun.lang.ClassLoader.allowArraySyntax=true";
     public static String JAVA_OPTS_DERBY = "-Dderby.system.home=data/derby";
     public static String PROGRAM_ARGS = "/org.nuxeo.osgi.application.Main bundles/.:lib/.:config -home .";
 
 
     
     public static void run(Configuration config) throws CoreException {
         if (!config.isValid()) {
             throw new CoreException(new Status(IStatus.ERROR, Nuxeo.PLUGIN_ID, "Invaid Launch Configuration! No home folder specified."));
         }
         ILaunchConfiguration configuration = createLaunchConfiguration(config);
         DebugUITools.launch(configuration, ILaunchManager.RUN_MODE);
         ILaunchGroup gr = DebugUITools.getLaunchGroup(configuration, ILaunchManager.RUN_MODE);
         System.out.println(gr.getCategory());
     }
 
     public static void debug(Configuration config) throws CoreException {
         if (!config.isValid()) {
             throw new CoreException(new Status(IStatus.ERROR, Nuxeo.PLUGIN_ID, "Invaid Launch Configuration! No home folder specified."));
         }
         ILaunchConfiguration configuration = createLaunchConfiguration(config);
         DebugUITools.launch(configuration, ILaunchManager.DEBUG_MODE);
     }
 
     
     public static String args(Configuration config) throws CoreException {
         StringBuilder buf = new StringBuilder();
         // compute osgi bundle name
         String osgi = "bundles/nuxeo-runtime-osgi-1.5-SNAPSHOT.jar";
         File bundles = new File(config.getHome(), "bundles");
         for (String b : bundles.list()) {
             if (b.startsWith("nuxeo-runtime-osgi")) {
                 osgi = "bundles/"+b;
                 break;
             }
         }
         buf.append(osgi).append(PROGRAM_ARGS).append(" ");
         if (!config.projects.isEmpty()) {            
             buf.append("-post-bundles ");
             for (IProject prj : config.projects) {
                IPath location = prj.getLocation(); // absolute location on file system
                //IPath prjPath = prj.getFullPath();
                 IJavaProject jprj = JavaCore.create(prj);
                IPath outPath = jprj.getOutputLocation();
                outPath = outPath.removeFirstSegments(1);
                outPath = location.append(outPath);
                String ospath = outPath.toOSString();
                 buf.append(ospath).append(":");
             }
             buf.setLength(buf.length()-1);
         }
         return buf.toString();
     }
     
     public static String vmargs(String jar) {
         return vmargs(jar, "");
     }
 
     public static String vmargs(String jar, String extra) {
         return JAVA_OPTS + " " + JAVA_OPTS_DERBY + " " + extra; 
     }
     
     public static List<String> createClassPath(java.io.File ... files) throws CoreException {
         ArrayList<String> paths = new ArrayList<String>();
         for (int i=0; i<files.length; i++) {
             IPath path = new Path(files[i].getAbsolutePath());
             IRuntimeClasspathEntry bootstrapEntry = 
                 JavaRuntime.newArchiveRuntimeClasspathEntry(path);
              bootstrapEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
              paths.add(bootstrapEntry.getMemento());
         }
         // don't forget the JVM bootstrap classes
         IPath systemLibsPath = new Path(JavaRuntime.JRE_CONTAINER);
         IRuntimeClasspathEntry systemLibsEntry =
             JavaRuntime.newRuntimeContainerClasspathEntry(systemLibsPath,
                IRuntimeClasspathEntry.STANDARD_CLASSES);
         paths.add(systemLibsEntry.getMemento());
         
         //if you want tools.jar on the class path
 //        IVMInstall jre = JavaRuntime.getDefaultVMInstall();         
 //        File jdkHome = jre.getInstallLocation();
 //        IPath toolsPath = new Path(jdkHome.getAbsolutePath())
 //               .append("lib")
 //               .append("tools.jar");
 //        IRuntimeClasspathEntry toolsEntry =
         return paths;
     }
 
     
     
     public static ILaunchConfiguration createLaunchConfiguration(Configuration config) throws CoreException {
         ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
         ILaunchConfigurationType type =
             manager.getLaunchConfigurationType(ID_JAVA_APPLICATION);
         ILaunchConfiguration[] configurations =
             manager.getLaunchConfigurations(type);
         // remove previous config - TODO reuse it and use Configuration ids
         for (int i = 0; i < configurations.length; i++) {
             ILaunchConfiguration configuration = configurations[i];
             if (configuration.getName().equals("WebEngine ["+config.id+"]")) {
                 configuration.delete();
                 break;
             }
         }        
         ILaunchConfigurationWorkingCopy workingCopy =
             type.newInstance(null, "WebEngine ["+config.id+"]");
 
 
         // attach to a project
         // do not attach for now since we have an external classpath
         //workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, config.projects.get(0).getName());
         
         // specify the JVM to use
         IVMInstall jre = null;
         if (!config.projects.isEmpty()) { // use the JVM used by  the first project
             jre = JavaRuntime.getVMInstall(JavaCore.create(config.projects.get(0)));
             if (jre != null) {
             }
         } else {
             jre = JavaRuntime.getDefaultVMInstall();
         }
         jre = JavaRuntime.getDefaultVMInstall();
         
         // don't know what to put in this attribute ...
         //workingCopy.setAttribute(ATTR_JRE_CONTAINER_PATH, jre.get);                
 //      workingCopy.setAttribute(ATTR_VM_INSTALL_NAME, jre.getName());
 //      workingCopy.setAttribute(ATTR_VM_INSTALL_TYPE, jre.getVMInstallType().getId());
         
         // main class
         workingCopy.setAttribute(ATTR_MAIN_TYPE_NAME, MAIN);
         // JVM arguments
         String vmargs = vmargs(config.launcher.getAbsolutePath());
         workingCopy.setAttribute(ATTR_VM_ARGUMENTS, vmargs);        
         // program arguments
         String args = args(config);
         workingCopy.setAttribute(ATTR_PROGRAM_ARGUMENTS, args);
         
         // working directory
         workingCopy.setAttribute(ATTR_WORKING_DIRECTORY,
              config.getHome().getAbsolutePath());
 
         //class path
         List<String> classpath = createClassPath(config.launcher);
         workingCopy.setAttribute(ATTR_CLASSPATH, classpath);
         workingCopy.setAttribute(ATTR_DEFAULT_CLASSPATH,  false);
 
 //        System.out.println("VMARGS: "+vmargs);
 //        System.out.println("ARGS:  "+args);
 //        System.out.println("CLASSPATH: "+classpath);
         
         //workingCopy.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
         // create the launch configuration
         return workingCopy.doSave();        
     }
     
 }
