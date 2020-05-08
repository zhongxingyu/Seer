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
 package org.javadrop;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.channels.Channels;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.maven.plugin.testing.AbstractMojoTestCase;
 import org.freecompany.redline.ReadableChannelWrapper;
 import org.freecompany.redline.Scanner;
 import org.freecompany.redline.header.AbstractHeader.Entry;
 import org.freecompany.redline.header.Format;
 import org.freecompany.redline.header.Header;
 import org.junit.Test;
 
 /**
  * Simplistic sanity unit test for seeing that the scriptgen is doing something
  * reasonable.
  * 
  * @author gcooperpdx
  * 
  */
 public class InstallPackageGenMojoTest extends AbstractMojoTestCase {
     /**
      * Test directory defaults to current directory. Intended to be set by the
      * maven pom.
      */
     private File scriptOutputDir;
 
     @Override
     public void setUp() throws Exception {
         // Required for mojo lookup to work
         super.setUp();
 
         scriptOutputDir = new File(getBasedir() + "/target/testdata");
         scriptOutputDir.mkdirs();
     }
 
     @Override
     public void tearDown() throws Exception {
         super.tearDown();
         recursiveRmDir(scriptOutputDir);
     }
 
     /**
      * Note that this doesn't check for links back to parents so it is pretty
      * brain-dead. However, it is designed to clean up the testdata which is a
      * known quantity.
      * 
      * @param directory
      *            Directory to be deleted
      */
     private void recursiveRmDir(File directory) {
         File[] contents = directory.listFiles();
         for (File fileContent : contents) {
             if (fileContent.isDirectory()) {
                 recursiveRmDir(fileContent);
             } else {
                 fileContent.delete();
             }
         }
 
         directory.delete();
     }
 
     /**
      * This tests to see if the mojo fails when no serviceType has been defined
      * 
      * @throws Exception
      */
 //    @Test
 //    public void testMissingServiceType() throws Exception {
 //        File testPom = getTestFile("src/test/resources/missing_servicetype_pom.xml");
 //
 //        InstallPackageGenMojo mojo;
 //        mojo = (InstallPackageGenMojo) lookupMojo("scriptgen", testPom);
 //        assertNotNull(mojo);
 //        mojo.setOutputDirectory(scriptOutputDir);
 //
 //        // Should throw an exception
 //        try {
 //            mojo.execute();
 //            fail("Expected to fail because of missing serviceType variable");
 //        } catch (MojoExecutionException ex) {
 //            // Just fall through as the exception is expected.
 //        }
 //
 //        // Now analyze the directories for proper contents
 //        File folderFile = new File(getBasedir()
 //                + "/target/testdata/filtered/bin");
 //        File[] contents = folderFile.listFiles();
 //        assertNull(contents);
 //
 //        folderFile = new File(getBasedir() + "/target/testdata/filtered/init.d");
 //        contents = folderFile.listFiles();
 //        assertNull(contents);
 //
 //        folderFile = new File(getBasedir()
 //                + "/target/testdata/filtered/rpm-scripts");
 //        contents = folderFile.listFiles();
 //        assertNull(contents);
 //    }
 //
 //    /**
 //     * This tests to see if the mojo fails when invalid serviceType has been defined
 //     * 
 //     * @throws Exception
 //     */
 //    @Test
 //    public void testBadServiceType() throws Exception {
 //        File testPom = getTestFile("src/test/resources/bad_servicetype_pom.xml");
 //
 //        InstallPackageGenMojo mojo;
 //        mojo = (InstallPackageGenMojo) lookupMojo("scriptgen", testPom);
 //        assertNotNull(mojo);
 //        mojo.setOutputDirectory(scriptOutputDir);
 //
 //        // Should throw an exception
 //        try {
 //            mojo.execute();
 //            fail("Expected to fail because of missing serviceType variable");
 //        } catch (MojoExecutionException ex) {
 //            // Just fall through as the exception is expected.
 //        }
 //
 //        // Now analyze the directories for proper contents
 //        File folderFile = new File(getBasedir()
 //                + "/target/testdata/filtered/bin");
 //        File[] contents = folderFile.listFiles();
 //        assertNull(contents);
 //
 //        folderFile = new File(getBasedir() + "/target/testdata/filtered/init.d");
 //        contents = folderFile.listFiles();
 //        assertNull(contents);
 //
 //        folderFile = new File(getBasedir()
 //                + "/target/testdata/filtered/rpm-scripts");
 //        contents = folderFile.listFiles();
 //        assertNull(contents);
 //    }
 
     /**
      * This does a basic sanity check to see that the directory structure is
      * correct. It currently does not go into details with determining what the
      * contents of the scripts are. This would be a worthy extension.
      * 
      * @throws Exception
      */
     @Test
     public void testStandAloneScriptCreation() throws Exception {
         File testPom = getTestFile("src/test/resources/service_test_pom.xml");
 
         InstallPackageGenMojo mojo;
         mojo = (InstallPackageGenMojo) lookupMojo("javadrop", testPom);
         assertNotNull(mojo);
         mojo.setWorkingDirectory(scriptOutputDir);
         mojo.setPackageDirectory(scriptOutputDir);
         mojo.execute();
 
         // Now analyze the directories for proper contents
         File folderFile = new File(getBasedir()
                 + "/target/testdata/runners/bin");
         File[] contents = folderFile.listFiles();
         assertEquals(1, contents.length);
         assertEquals("testservice.sh", contents[0].getName());
         String fileResult = readFileAsString(getBasedir() + "/target/testdata/runners/bin/testservice.sh");
         assertTrue("JMX Port config not found", fileResult.contains("-Dcom.sun.management.jmxremote.port=1098"));
 
         folderFile = new File(getBasedir() + "/target/testdata/runners/init.d");
         contents = folderFile.listFiles();
         assertEquals(1, contents.length);
         assertEquals("testservice", contents[0].getName());
 
         // Check to make sure service script is for stand-alone mode
         fileResult = readFileAsString(getBasedir() + "/target/testdata/runners/init.d/testservice");
         assertFalse("Unexpected JETTY reference found", fileResult.contains("JETTY"));
 
         folderFile = new File(getBasedir()
                 + "/target/testdata/rpm");
         contents = folderFile.listFiles();
         assertEquals(3, contents.length);
         Set<String> fileSet = new HashSet<String>();
         fileSet.add("postinstall.sh");
         fileSet.add("postremove.sh");
         fileSet.add("preinstall.sh");
         for (File fileContent : contents) {
             assertTrue(fileSet.contains(fileContent.getName()));
         }
     }
 
     /**
      * This does a basic sanity check to see that the directory structure is
      * correct. It currently does not go into details with determining what the
      * contents of the scripts are. This would be a worthy extension.
      * 
      * @throws Exception
      */
     @Test
     public void testJettyScriptCreation() throws Exception {
         createDummyWarFile();
         
     	File testPom = getTestFile("src/test/resources/jetty_service_test_pom.xml");
 
         InstallPackageGenMojo mojo;
         mojo = (InstallPackageGenMojo) lookupMojo("javadrop", testPom);
         assertNotNull(mojo);
         mojo.setWorkingDirectory(scriptOutputDir);
         mojo.setPackageDirectory(scriptOutputDir);
         mojo.execute();
 
         // Now analyze the directories for proper contents
         jettyResults(1,"jtytestsvc");
     }
 
     private void createDummyWarFile() throws IOException {
         // Simulate the creation of the war file by the build in the target directory
 //    	new File(scriptOutputDir.getAbsolutePath() + File.separator + "war").mkdirs();
    	File dummyWarFile = new File(scriptOutputDir.getAbsolutePath() + File.separator + "jtytestsvc-1.0.war");
     	dummyWarFile.createNewFile();
     }
     /**
      * Tests that a java application can be packaged up.
      * 
      * @throws Exception
      */
     @Test
     public void testJavaAppCreation() throws Exception {
     	// Dummy up some java lib to go in the rpm
     	new File(scriptOutputDir.getAbsolutePath() + File.separator + "lib").mkdirs();
     	File dummyFile = new File(scriptOutputDir.getAbsolutePath() + File.separator + "lib/dummy.jar");
     	dummyFile.createNewFile();
     	
         File testPom = getTestFile("src/test/resources/java_app_test_pom.xml");
 
         InstallPackageGenMojo mojo;
         mojo = (InstallPackageGenMojo) lookupMojo("javadrop", testPom);
         assertNotNull(mojo);
         mojo.setWorkingDirectory(scriptOutputDir);
         mojo.setPackageDirectory(scriptOutputDir);
         mojo.execute();
 
         javaAppResults(1);
     }
     
     
     /**
      * This does a basic sanity check to see that the directory structure is
      * correct for a 'multi' strategy project.
      * 
      * @throws Exception
      */
     @Test
     public void testMultiScriptCreation() throws Exception {
         createDummyWarFile();
         
     	// Dummy up some java lib to go in the rpm
     	new File(scriptOutputDir.getAbsolutePath() + File.separator + "lib").mkdirs();
     	File dummyFile = new File(scriptOutputDir.getAbsolutePath() + File.separator + "lib/dummy.jar");
     	dummyFile.createNewFile();
     	
         File testPom = getTestFile("src/test/resources/multi_runners_test_pom.xml");
 
         InstallPackageGenMojo mojo;
         mojo = (InstallPackageGenMojo) lookupMojo("javadrop", testPom);
         assertNotNull(mojo);
         mojo.setWorkingDirectory(scriptOutputDir);
         mojo.setPackageDirectory(scriptOutputDir);
         mojo.execute();
 
         jettyResults(2, "jtestapp");
         javaAppResults(2);
     }
 
     private void javaAppResults(int numRunScripts) throws Exception
     {
     	        // Now analyze the directories for proper contents
         File folderFile = new File(getBasedir()
                 + "/target/testdata/runners/bin");
         File[] contents = folderFile.listFiles();
         assertEquals(numRunScripts, contents.length);
         boolean foundAppscript = false;
         for (File runscript : contents) {
         	if (runscript.getName().equals("jtestapp.sh")) foundAppscript = true;
         }
         assertTrue("Expecting to find app run script", foundAppscript);
         String fileResult = readFileAsString(getBasedir() + "/target/testdata/runners/bin/jtestapp.sh");
         assertTrue("JMX Port config not found", fileResult.contains("-Dcom.sun.management.jmxremote.port=1093"));
 
 
         folderFile = new File(getBasedir()
                 + "/target/testdata/rpm/");
         contents = folderFile.listFiles();
         assertEquals(3, contents.length);
         Set<String> fileSet = new HashSet<String>();
         fileSet.add("postinstall.sh");
         fileSet.add("postremove.sh");
         fileSet.add("preinstall.sh");
         for (File fileContent : contents) {
             assertTrue(fileSet.contains(fileContent.getName()));
         }
         
         // Check to see if the rpm is there.
        File rpmFile = new File(getBasedir() + "/target/testdata/jtestapp-1.0-1309218173.noarch.rpm");
         checkRPMFile(rpmFile, "dummy.jar", "/usr/local/iovation/jtestapp/lib/");
     }
     
     
     private void jettyResults(int numRunScripts, String testPrefix) throws Exception
     {
         // Now analyze the directories for proper contents
         File folderFile = new File(getBasedir()
                 + "/target/testdata/runners/bin");
         File[] contents = folderFile.listFiles();
         assertEquals(numRunScripts, contents.length);
         
         boolean foundJettyscript = false;
         for (File runscript : contents) {
         	if (runscript.getName().equals("jtytestsvc.sh")) foundJettyscript = true;
         }
         assertTrue("Expecting to find jetty run script",foundJettyscript);
         String fileResult = readFileAsString(getBasedir() + "/target/testdata/runners/bin/jtytestsvc.sh");
         assertTrue("JMX Port config not found", fileResult.contains("-Dcom.sun.management.jmxremote.port=1093"));
 
         folderFile = new File(getBasedir() + "/target/testdata/runners/init.d");
         contents = folderFile.listFiles();
         assertEquals(1, contents.length);
         assertEquals("jtytestsvc", contents[0].getName());
 
         // Check to make sure service script is for jetty mode
         fileResult = readFileAsString(getBasedir() + "/target/testdata/runners/init.d/jtytestsvc");
 //        assertTrue("JETTY reference not found", fileResult.contains("JETTY"));
 
 
         folderFile = new File(getBasedir()
                 + "/target/testdata/rpm/");
         contents = folderFile.listFiles();
         assertEquals(3, contents.length);
         Set<String> fileSet = new HashSet<String>();
         fileSet.add("postinstall.sh");
         fileSet.add("postremove.sh");
         fileSet.add("preinstall.sh");
         for (File fileContent : contents) {
             assertTrue(fileSet.contains(fileContent.getName()));
         }
         
         folderFile = new File(getBasedir() + "/target/testdata/runners/conf");
         contents = folderFile.listFiles();
         assertEquals(4, contents.length);
         fileResult = readFileAsString(getBasedir() + "/target/testdata/runners/conf/jetty-spring.xml");
         assertTrue("Missing or incorrect test-service-facade context name", fileResult.contains("p:contextPath=\"/jtytestsvc\" p:extractWAR="));
         assertTrue("Missing or incorrect test-service-facade war name", fileResult.contains("p:war=\"war/jtytestsvc.war\""));
        File rpmFile = new File(getBasedir() + "/target/testdata/" + testPrefix + "-1.0-1309218173.noarch.rpm");
         
         checkRPMFile(rpmFile, "jtytestsvc", "/usr/local/iovation/" + testPrefix + "/war/");
     }
     
     
     /**
      * This is a little horrid as it sucks a file into a String. However, for the purposes of
      * testing it seems fine so long as it's not abused. (makes testing file contents easier)
      * 
      * @param filePath File to load in
      * @return String of THE ENTIRE FILE (so be careful)
      * @throws java.io.IOException
      */
     private String readFileAsString(String filePath)
     throws java.io.IOException{
         StringBuffer fileData = new StringBuffer(1000);
         BufferedReader reader = new BufferedReader(
                 new FileReader(filePath));
         char[] buf = new char[1024];
         int numRead=0;
         while((numRead=reader.read(buf)) != -1){
             fileData.append(buf, 0, numRead);
         }
         reader.close();
         return fileData.toString();
     }
 
     //@SuppressWarnings("unchecked")
 	private void checkRPMFile(File rpmFile, String basename, String dirname) throws FileNotFoundException, Exception 
     {
     	Scanner scanner = new Scanner();
         Format format = scanner.run(new ReadableChannelWrapper(Channels.newChannel(new FileInputStream(rpmFile))));
     	
         Entry<String[]> stringEntries = (Entry<String[]>) format.getHeader().getEntry(Header.HeaderTag.BASENAMES);
         
         String[] bnames = stringEntries.getValues();
         if (!arrayContains(bnames, basename)) {
         	fail("Basename not found in RPM: " + basename);
         }
         
         stringEntries = (Entry<String[]>) format.getHeader().getEntry(Header.HeaderTag.DIRNAMES);
         String[] dnames = stringEntries.getValues();
         if (!arrayContains(dnames, dirname)) {
         	fail("Dirname not found in RPM: " + dirname);
         }
     }
     
     private boolean arrayContains(String [] sarray, String toFind)
     {
     	for (String astr : sarray) {
     		if (astr.equalsIgnoreCase(toFind)) return true;
     	}
     	return false;
     }
 
 }
