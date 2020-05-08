 /*
  * #%L
  * it-xcode-maven-plugin
  * %%
  * Copyright (C) 2012 SAP AG
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 package com.sap.prd.mobile.ios.mios;
 
 import static junit.framework.Assert.fail;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.it.VerificationException;
 import org.apache.maven.it.Verifier;
 import org.junit.Test;
 
 public class XCodeVerificationChecksTest extends XCodeTest
 {
   @Test
   public void testVerifyWithError() throws Exception
   {
     final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
     final String testName = getTestName();
 
     final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
 
     prepareRemoteRepository(remoteRepositoryDirectory);
 
     final File checkDefinitions = new File(getTestExecutionDirectory(testName, "MyApp"), "checkDefinitions.xml");
     
     Properties pomReplacements = new Properties();
     pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
     pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
 
     Map<String, String> additionalSystemProperties = new HashMap<String, String>();
     additionalSystemProperties.put("xcode.verification.checks.skip", "false");
     additionalSystemProperties.put("xcode.verification.checks.definitionFile", "file:" + checkDefinitions.getAbsolutePath());
 
     test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"),
           "deploy",
           THE_EMPTY_LIST,
           null, pomReplacements, new NullProjectModifier());
     
     Verifier v = new Verifier(getTestExecutionDirectory(testName, "MyApp").getAbsolutePath());
     
     try
     {
         test(v, testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
               "verify",
               Arrays.asList("-X"),
               additionalSystemProperties, pomReplacements, new ProjectModifier() {
 
                 @Override
                 void execute() throws Exception
                 {
                   prepareCheckDefinitionFile(new File(".",
                         "src/test/resources/checkDefinitions.xml"), checkDefinitions, "ERROR");
                 }
           
         });
         fail();
     } catch(VerificationException ex) {
       v.verifyTextInLog("Adding transitive dependency 'junit:junit:4.8.2'");
       v.verifyTextInLog("Omitting transitive dependency 'org.sonatype.sisu:sisu-guice:2.9.1'");
       v.verifyTextInLog("Verification check 'com.sap.prd.mobile.ios.mios.TestMetadataCheck failed. 7430190670433136460");
     }
   }
 
   @Test
   public void testVerifyMissingCheckDefinitionFile() throws Exception
   {
     final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
     final String testName = getTestName();
 
     final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
 
     prepareRemoteRepository(remoteRepositoryDirectory);
 
     final File checkDefinitions = new File(getTestExecutionDirectory(testName, "MyApp"), "checkDefinitions.xml");
     
     Properties pomReplacements = new Properties();
     pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
     pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
 
     Map<String, String> additionalSystemProperties = new HashMap<String, String>();
     additionalSystemProperties.put("xcode.verification.checks.skip", "false");
     additionalSystemProperties.put("xcode.verification.checks.definitionFile", "file:" + checkDefinitions.getAbsolutePath());
 
     test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"),
           "deploy",
           THE_EMPTY_LIST,
           null, pomReplacements, new NullProjectModifier());
     
     Verifier v = new Verifier(getTestExecutionDirectory(testName, "MyApp").getAbsolutePath());
     
     try
     {
         test(v, testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
               "verify",
               THE_EMPTY_LIST,
               additionalSystemProperties, pomReplacements, new NullProjectModifier());
         fail();
     } catch(VerificationException ex) {
       v.verifyTextInLog("Cannot get check definitions");
     }
   }
 
   
   @Test
   public void testVerifyWithWarning() throws Exception
   {
     final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
     final String testName = getTestName();
 
     final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
 
     prepareRemoteRepository(remoteRepositoryDirectory);
 
     final File checkDefinitions = new File(getTestExecutionDirectory(testName, "MyApp"), "checkDefinitions.xml");
     
     Properties pomReplacements = new Properties();
     pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
     pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
 
     Map<String, String> additionalSystemProperties = new HashMap<String, String>();
     additionalSystemProperties.put("xcode.verification.checks.skip", "false");
     additionalSystemProperties.put("xcode.verification.checks.definitionFile", "file:" + checkDefinitions.getAbsolutePath());
 
     test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"),
           "deploy",
           THE_EMPTY_LIST,
           null, pomReplacements, new NullProjectModifier());
     
     Verifier v = test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
               "verify",
               Arrays.asList("-X"),
               additionalSystemProperties, pomReplacements, new ProjectModifier() {
 
                 @Override
                 void execute() throws Exception
                 {
                   prepareCheckDefinitionFile(new File(".",
                         "src/test/resources/checkDefinitions.xml"), checkDefinitions, "WARNING");
                 }
           
         });
       v.verifyTextInLog("Adding transitive dependency 'junit:junit:4.8.2'");
       v.verifyTextInLog("Omitting transitive dependency 'org.sonatype.sisu:sisu-guice:2.9.1'");
       v.verifyTextInLog("[WARNING] Verification check 'com.sap.prd.mobile.ios.mios.TestMetadataCheck failed. 7430190670433136460");
   }
 
   
   private void prepareCheckDefinitionFile(File source, File target, String severity) throws IOException
   {
    String checks = FileUtils.readFileToString(source);
    checks = checks.replaceAll("\\$\\{xcode.maven.plugin.version\\}", getMavenXcodePluginVersion());
    checks = checks.replaceAll("\\$\\{SERVERITY\\}", severity);
    FileUtils.writeStringToFile(target, checks);
     
   }
 
   @Test
   public void testVerificationCheckSkipped() throws Exception
   {
     final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
     final String testName = getTestName();
 
     final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
 
     prepareRemoteRepository(remoteRepositoryDirectory);
 
     Properties pomReplacements = new Properties();
     pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
     pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
 
     Verifier verifier = test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
           "com.sap.prd.mobile.ios.mios:xcode-maven-plugin:"
                 + getMavenXcodePluginVersion() + ":verification-check",
           THE_EMPTY_LIST,
           null, pomReplacements, new NullProjectModifier());
 
     String message = "Verification check goal has been skipped intentionally since parameter 'xcode.verification.checks.skip' is 'true'.";
 
     try {
       verifier.verifyTextInLog(message);
     }
     catch (VerificationException ex) {
       fail("Expected log message (" + message + ") was not present.");
     }
   }
 
   @Test
   public void testVerificationCheckNoProtocolSpecified() throws Exception
   {
     final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
     final String testName = getTestName();
 
     final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
 
     prepareRemoteRepository(remoteRepositoryDirectory);
 
     Properties pomReplacements = new Properties();
     pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
     pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
 
     Map<String, String> additionalSystemProperties = new HashMap<String, String>();
     additionalSystemProperties.put("xcode.verification.checks.skip", "false");
     additionalSystemProperties.put("xcode.verification.checks.definitionFile", (new File(".",
           "src/test/resources/verifications-error.xml")).getAbsolutePath());
 
     Verifier v = new Verifier(getTestExecutionDirectory(testName, "MyApp").getAbsolutePath());
     try {
       test(v, testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
             "com.sap.prd.mobile.ios.mios:xcode-maven-plugin:"
                   + getMavenXcodePluginVersion() + ":verification-check",
             THE_EMPTY_LIST,
             additionalSystemProperties, pomReplacements, new NullProjectModifier());
       fail("Expected Exception was not thrown");
     }
     catch (Exception ex) {
       assertTrue(ex.getMessage().contains(
             "Provide a protocol [http, https, file] for parameter 'xcode.verification.checks.definitionFile'"));
     }
   }
 
   @Test
  public void testVerificationCheckClassPathNotExtendedSpecified() throws Exception
   {
     final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
     final String testName = getTestName();
 
     final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
 
     prepareRemoteRepository(remoteRepositoryDirectory);
 
     Properties pomReplacements = new Properties();
     pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
     pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
 
     Map<String, String> additionalSystemProperties = new HashMap<String, String>();
     additionalSystemProperties.put("xcode.verification.checks.skip", "false");
     additionalSystemProperties.put("xcode.verification.checks.definitionFile", "file:"
           + (new File(".", "src/test/resources/verifications-error.xml")).getAbsolutePath());
 
     try {
       test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
             "com.sap.prd.mobile.ios.mios:xcode-maven-plugin:"
                   + getMavenXcodePluginVersion() + ":verification-check",
             THE_EMPTY_LIST,
             additionalSystemProperties, pomReplacements, new NullProjectModifier());
       fail("Expected Exception was not thrown");
     }
     catch (Exception ex) {
      assertTrue(ex.getMessage().contains("May be your classpath has not been properly extended."));
      assertTrue(ex.getMessage().contains(
            "Additional dependencies need to be provided with 'xcode.additionalClasspathElements'."));
     }
   }
 }
