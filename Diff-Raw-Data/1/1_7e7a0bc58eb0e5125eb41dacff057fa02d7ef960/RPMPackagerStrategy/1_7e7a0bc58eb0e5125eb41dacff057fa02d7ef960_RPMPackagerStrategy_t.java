 /*******************************************************************************
  * Copyright 2011 iovation
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.javadrop.packaging.impl;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.tools.ant.Project;
 import org.freecompany.redline.ant.RedlineTask;
 import org.freecompany.redline.ant.RpmFileSet;
 import org.javadrop.runner.RunnerStrategy;
 import org.javadrop.runner.impl.JavaAppStrategy;
 import org.javadrop.runner.impl.JettyStrategy;
 import org.javadrop.runner.impl.MainServiceStrategy;
 
 /**
  * This packager strategy uses the 'redline' java rpm generation code to make an
  * RPM.
  * 
  * The 'mapping' this rpm strategy uses is simple. It takes the destination
  * files and mirrors them on the
  * 
  * @author gcooperpdx
  * 
  */
 public class RPMPackagerStrategy extends BasePackagerStrategy {
 
     @Override
     public void postProcessArtifacts(RunnerStrategy runner,
             File workingDirectory) {
         // Rename any files necessary.
         Map<File, File> mapFiles = runner.getArtifactRenames(workingDirectory);
         for (Entry<File, File> mapFile : mapFiles.entrySet()) {
             File oldFile = mapFile.getKey();
             File newFile = mapFile.getValue();
             newFile.getParentFile().mkdirs();
             
             oldFile.renameTo(newFile);
         }
     }
 
     @Override
     public void createPackage(File packagerDirectory, File workingDirectory,
             Collection<RunnerStrategy> runners, Log log)
             throws MojoExecutionException {
         // TODO Bunch of items that need to be parameterized properly.
         // File filename = new File(packagerDirectory,
         // "rpmtest-1.0-1.noarch.rpm");
         if (packagerDirectory == null) {
             get_log().error("'packagerDirectory' is null");
             throw new MojoExecutionException("'packagerDirectory' is null");
         }
         Project project = new Project();
         //project.setCoreLoader(getClass().getClassLoader());
         project.setCoreLoader(ClassLoader.getSystemClassLoader());
         project.init();
         RedlineTask task = new RedlineTask();
         task.setProject(project);
         task.setDestination(packagerDirectory);
         task.setName(getRequiredParam("PKG_NAME"));
         task.setArchitecture("NOARCH");
         task.setLicense("proprietary");
         // RPM has VERY SUBTLE and very bad behavior with text in the version.  It appears to work but doesn't upon
         // rpm -e 
         task.setVersion(getRequiredParam("PKG_VERSION").replaceFirst("-SNAPSHOT", ""));
         task.setRelease(getRequiredParam("PKG_RELEASE"));
         task.setGroup("Application/Office");
         task.setSourcePackage(getRequiredParam("PKG_NAME")
                 + getRequiredParam("PKG_VERSION") + ".src.rpm");
         task.setPreInstallScript(new File(workingDirectory + File.separator
                 + "rpm" + File.separator + "preinstall.sh"));
         task.setPostInstallScript(new File(workingDirectory + File.separator
                 + "rpm" + File.separator + "postinstall.sh"));
         task.setPostUninstallScript(new File(workingDirectory + File.separator
                 + "rpm" + File.separator + "postremove.sh"));
 
         // Get the mapping for the files that the runner(s) need to install.
         for (RunnerStrategy runner : runners) {
             Map<File, Collection<File>> installSet = runner
                     .getInstallSet(workingDirectory);
             for (Map.Entry<File, Collection<File>> instEntry : installSet
                     .entrySet()) {
                 String leafDirName = instEntry.getKey().getName();
                 File installDir;
                 if (isServiceDir(leafDirName)) {
                     installDir = new File(getServiceDirectory());
                 } else {
                     installDir = new File(getInstallLoc() + File.separator
                             + leafDirName);
                 }
 
                 // If destination directory isn't available, bail.
                 for (File destFile : instEntry.getValue()) {
                     RpmFileSet fs = new RpmFileSet();
                     // fs.setDir(installDir);
                     fs.setPrefix(installDir.getAbsolutePath()); // getInstallLoc()
                                                                 // +
                                                                 // installDir.getinstEntry.get"/etc");
                     File sourceFile = new File(instEntry.getKey().getName()
                             + File.separator + destFile.getName());
                     fs.setFile(sourceFile);
                                                                      // File("source/test/prein.sh"));
                     File sourceDir = new File(workingDirectory.getPath()
                             + File.separator + instEntry.getKey());
                     fs.setDir(sourceDir);
 
                     fs.setConfig(true);
                     fs.setNoReplace(true);
                     fs.setDoc(true);
                     fs.setGroup(getGroup());
                     fs.setGid(getGid());
                     fs.setUserName(getUser());
                     fs.setUid(getUid());
                    fs.setFileMode("0755");
                     task.addRpmfileset(fs);
                 }
             }
         }
 
         // Create the rpm
         task.execute();
     }
 
     /**
      * Strips the '-SNAPSHOT' text from a string
      * @param snapshotStr String that MAY have '-SNAPSHOT' inside of it
      * @return The string with -SNAPSHOT removed
      */
     private String snapshotStripped(String requiredParam) {
         return requiredParam.replaceFirst("-SNAPSHOT", "");
     }
 
     /**
      * Checks to see if the given directory 'leaf' name is that of the service
      * script directory for the system.
      * 
      * @param dirName
      *            Leaf name of the directory in question
      * @return True if 'dirName' is the name of the os-specific location for
      *         service scripts
      */
     private boolean isServiceDir(String dirName) {
         return "init.d".equals(dirName);
     }
 
     /**
      * Get the directory that contains the service launching scripts. Currently
      * this is very CentOS oriented
      * 
      * @return The system-specific path for where the service start scripts are.
      */
     private String getServiceDirectory() {
         return File.separator + "etc" + File.separator + "rc.d"
                 + File.separator + "init.d";
     }
 
     @Override
     public Map<File, File> getConversionFiles(File outputDirectory,
             RunnerStrategy runner) {
 
         Map<File, File> conversionFiles = new HashMap<File, File>();
 
         if ((runner instanceof MainServiceStrategy)
                 || (runner instanceof JettyStrategy)
                 || (runner instanceof JavaAppStrategy)) {
             conversionFiles.put(new File(getPrefix() + File.separator
                     + "postinstall.vm"), new File(outputDirectory
                     + File.separator + "rpm" + File.separator
                     + "postinstall.sh"));
             conversionFiles.put(new File(getPrefix() + File.separator
                     + "postremove.vm"),
                     new File(outputDirectory + File.separator + "rpm"
                             + File.separator + "postremove.sh"));
             conversionFiles.put(new File(getPrefix() + File.separator
                     + "preinstall.vm"),
                     new File(outputDirectory + File.separator + "rpm"
                             + File.separator + "preinstall.sh"));
         }
 
         return conversionFiles;
     }
 
     /**
      * Returns the install location
      * 
      * @return Where the package is to be installed... As a string.
      * @throws MojoExecutionException
      *             Thrown if required variable is missing
      */
     protected String getRequiredParam(String parameter)
             throws MojoExecutionException {
         String value = packagerVariables.get(parameter);
         if (value == null) {
             throw new MojoExecutionException("Missing required parameter: "
                     + parameter);
         }
         return value;
     }
 
     private String getPrefix() {
         return "org" + File.separator + "javadrop" + File.separator
                 + "packagerstrategy" + File.separator + "rpm";
     }
 
 }
