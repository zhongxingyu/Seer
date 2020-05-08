 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.disttools;
 
 import org.testng.annotations.Test;
 import org.testng.Assert;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.collections.EnumerationUtils;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 import org.apache.tools.ant.MagicNames;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.input.InputHandler;
 import org.apache.tools.ant.input.InputRequest;
 
 import java.io.*;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipEntry;
 import java.util.*;
 
 /**
  * <p>
  * Performs basic tests on the flexive distribution package, flexive-dist.zip.
  * </p>
  * <p>
  * The location of the ZIP file must be set using the flexive.test.dist.file system property.
  * </p>
  * <p>
  * For these tests to work, the flexive-dist.zip is extracted to a temporary directory
  * under System.getProperty("java.io.tmpdir"), then the packaged ant is invoked programmatically.
  * For some tests the java compiler (javac) has to be in the system path.
  * </p>
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class DistPackageTest {
     /**
      * An ANT input handler that works off a predefined set of inputs.
      */
     private static class SimpleInputHandler implements InputHandler {
         private final Queue<String> inputs = new LinkedList<String>();
 
         public SimpleInputHandler(String... inputs) {
             this.inputs.addAll(Arrays.asList(inputs));
         }
 
         public void handleInput(InputRequest request) throws BuildException {
             request.setInput(this.inputs.remove());
         }
     }
 
     @Test(groups = "dist")
     public void basicDistPackageAnalysis() throws IOException {
         final ZipFile file = getDistFile();
 
         // parse zipfile entries, extract names
         final Enumeration<? extends ZipEntry> entries = file.entries();
         final List<String> entryNames = new ArrayList<String>();
         while (entries.hasMoreElements()) {
             final ZipEntry entry = entries.nextElement();
             entryNames.add(entry.getName());
         }
 
         // first check some mandatory files
         for (String requiredEntry: new String[] {
                 // flexive JAR distribution
                 "/lib/flexive-ant.jar", "/lib/flexive-ejb.jar", "/lib/flexive-extractor.jar",
                 "/lib/flexive-shared.jar",
                "/lib/flexive-sqlParser.jar", "/lib/flexive-ui-shared.jar",
 
                 // basic build files
                 "/build.xml", "/build.project.xml", "/build.component.xml", "/database.properties",
 
                 // basic plugins
                 "/applications/flexive-plugin-jsf-core.jar",
         }) {
             Assert.assertTrue(entryNames.contains("flexive-dist" + requiredEntry));
         }
     }
 
     @Test(groups = "dist")
     public void extractDistPackageTest() throws IOException {
         final File distDir = extractDistPackage();
         final Project project = createProject(distDir);
 
         // create a project called "test"
         project.setInputHandler(new SimpleInputHandler(
                 "project.create",   // input for the main build menu
                 "test",             // project name
                 "y"                 // accept input
         ));
         final String testProjectPath = distDir.getAbsolutePath() + File.separator + ".." + File.separator + "test";
         try {
             project.executeTarget(project.getDefaultTarget());
 
             // now execute ant in the project directory, should create a file called dist/test.ear
             final Project testProject = createProject(new File(testProjectPath));
             testProject.executeTarget(testProject.getDefaultTarget());
             final String earPath = testProjectPath + File.separator + "dist" + File.separator + "test.ear";
             Assert.assertTrue(new File(earPath).exists(), earPath + " not found");
         } finally {
             // clean up
             deleteDirectory(new File(testProjectPath));
         }
     }
 
     private Project createProject(File basedir) {
         final File buildFile = new File(basedir.getPath() + File.separator + "build.xml");
         final Project project = new Project();
         project.setBaseDir(basedir);
         project.init();
         project.setUserProperty(MagicNames.ANT_FILE, buildFile.getAbsolutePath());
         ProjectHelper.configureProject(project, buildFile);
         return project;
     }
 
     /**
      * Extracts the flexive-dist.zip package and returns the (temporary) directory handle. All extracted
      * files will be registered for deletion on the JVM exit.
      *
      * @return  the (temporary) directory handle.
      * @throws IOException  if the package could not be extracted
      */
     private File extractDistPackage() throws IOException {
         final ZipFile file = getDistFile();
         final File tempDir = createTempDir();
         try {
             final Enumeration<? extends ZipEntry> entries = file.entries();
             while (entries.hasMoreElements()) {
                 final ZipEntry zipEntry = entries.nextElement();
                 final String path = getTempPath(tempDir, zipEntry);
                 if (zipEntry.isDirectory()) {
                     final File dir = new File(path);
                     dir.mkdirs();
                     dir.deleteOnExit();
                 } else {
                     final InputStream in = file.getInputStream(zipEntry);
                     final OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
                     // copy streams
                     final byte[] buffer = new byte[32768];
                     int len;
                     while ((len = in.read(buffer)) > 0) {
                         out.write(buffer, 0, len);
                     }
                     in.close();
                     out.close();
                     new File(path).deleteOnExit();
                 }
             }
         } finally {
             file.close();
         }
         return new File(tempDir + File.separator + "flexive-dist");
     }
 
     private String getTempPath(File tempDir, ZipEntry zipEntry) {
         return tempDir.getPath() + File.separator + zipEntry.getName();
     }
 
     private ZipFile getDistFile() throws IOException {
         final String name = System.getProperty("flexive.test.dist.file");
         assert StringUtils.isNotBlank(name) : "flexive.test.dist.file property not set.";
         return new ZipFile(name);
     }
 
     private File createTempDir() {
         final String tempDir = System.getProperty("java.io.tmpdir");
         assert StringUtils.isNotBlank(tempDir);
         final File dir = new File(tempDir + File.separator + "flexive-dist-tests-temp-" + RandomStringUtils.randomAlphanumeric(32));
         dir.deleteOnExit();
         dir.mkdirs();
         return dir;
     }
 
     private static void deleteDirectory(File dir) {
         if (!dir.exists()) {
             return;
         }
         if (dir.isDirectory()) {
             for (File file: dir.listFiles()) {
                 deleteDirectory(file);
             }
         }
         dir.delete();
     }
 }
