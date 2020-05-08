 /*
  * Copyright 2011 SOFTEC sa. All rights reserved.
  *
  * This source code is licensed under the Creative Commons
  * Attribution-NonCommercial-NoDerivs 3.0 Luxembourg
  * License.
  *
  * To view a copy of this license, visit
  * http://creativecommons.org/licenses/by-nc-nd/3.0/lu/
  * or send a letter to Creative Commons, 171 Second Street,
  * Suite 300, San Francisco, California, 94105, USA.
  */
 
 package org.codehaus.mojo.javascript;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.mojo.javascript.titanium.TitaniumBuilder;
 import org.codehaus.mojo.javascript.titanium.TitaniumUtils;
 
 import java.io.File;
 import java.util.UUID;
 
 /**
  * @phase test
  * @goal titanium-jasmine
  * @plexus.component role="org.codehaus.mojo.javascript.TitaniumJasmineMojo" role-hint="titanium"
  */
 public class TitaniumJasmineMojo extends AbstractTitaniumPackageMojo {
 
     /**
      * <p>The test package execution mode.</p>
      * <p>Allow the execution of the package on an emulator/device.</p>
      * <p>Values are:</p>
      * <dl>
      *   <dt>none</dt>
      *   <dd>Do not execute. (Default value)</dd>
      *   <dt>emulator</dt>
      *   <dd>Execute on an emulator whose settings are specified in {@link VirtualDevice}.</dd>
      *   <dt>device</dt>
      *   <dd>Execute on a connected device.</dd>
      * </dl>
      *
      * If not specified, the value of {@link #executeMode} will be taken.
      * @parameter expression="${testExecuteMode}"
      */
     private String testExecuteMode;
 
     /**
      * @parameter default-value="${project.basedir}${file.separator}src${file.separator}test${file.separator}javascript" expression="${jsunitTestSourceDirectory}"
      */
     protected File jasmineTestSourceDirectory;
 
     /**
      * The output directory of the test files.
      * @parameter default-value="${project.build.testOutputDirectory}"
      */
     protected File testOutputDirectory;
 
     protected File getPlatformTestOutputDirectory() {
         return new File(testOutputDirectory, platform);
     }
 
     /**
      * Set this to 'true' to bypass unit tests entirely. Its use is NOT
      * RECOMMENDED, but quite convenient on occasion.
      *
      * @parameter expression="${maven.test.skip}"
      */
     protected boolean skipTests;
 
     protected String getAppId() {
         return project.getGroupId() + "." + project.getArtifactId() + ".test";
     }
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         if(skipTests || !jasmineTestSourceDirectory.exists() || !testOutputDirectory.exists()) {
             getLog().info("No Jasmine tests, skipping Jasmine tests preparation.");
             return;
         }
 
         TitaniumUtils.checkVirtualDevice(platform, getTitaniumSettings(), getVirtualDevice());
 
         if (!checkPomSettings()) {
             return;
         }
 
         if (testExecuteMode == null) {
             testExecuteMode = executeMode;
         }
 
         if (testExecuteMode.equals("none")) {
             getLog().info("Skipping jasmine test as testExecuteMode is none");
             return;
         }
 
         if (platform.compareToIgnoreCase("android") == 0) {
             File androidBuilder = resolveAndroidBuilder();
             TitaniumBuilder tiBuilder = new TitaniumBuilder(androidBuilder,
                     null,
                     titaniumSettings.getAndroidSdk());
             packageTestAndroid(tiBuilder);
         } else if (platform.compareToIgnoreCase("iphone") == 0
                 || platform.compareToIgnoreCase("ipad") == 0
                 || platform.compareToIgnoreCase("universal") == 0) {
             File iosBuilder = resolveIOSBuilder();
             TitaniumBuilder tiBuilder = new TitaniumBuilder(null,
                     iosBuilder,
                     null);
             packageTestIphone(tiBuilder);
         } else {
             throw new MojoExecutionException("Unsupported platform: " + platform);
         }
     }
 
     private void packageTestAndroid(TitaniumBuilder tiBuilder)
             throws MojoExecutionException, MojoFailureException {
         try {
             if (testExecuteMode.equals("virtual")) {
                 tiBuilder.launchOnAndroidEmulator(project.getName(),
                                                 getPlatformTestOutputDirectory(),
                                                 getAppId(),
                                                 getAndroidAPI(),
                                                 getVirtualDevice().getAndroidAPI(),
                                                 getVirtualDevice().getSkin(),
                                                 getVirtualDevice().getWait(),
                                                 getLog());
 
             } else if (testExecuteMode.equals("device")) {
                 ProcessBuilder pb = tiBuilder.createAndroidBuilderProcess("install",
                         project.getName() + " Test",
                         getPlatformTestOutputDirectory().getAbsolutePath(),
                         getAppId(),
                        getVirtualDevice().getAndroidAPI(),
                         getVirtualDevice().getSkin());
                 Process deviceProcess = pb.start();
                 getLog().info("Deploying on device ");
                 TitaniumBuilder.logProcess(deviceProcess, getLog());
                 getLog().info("done");
             }
         } catch (MojoFailureException e) {
             throw e;
         } catch (Throwable t) {
             throw new MojoExecutionException("Error while executing android builder", t);
         }
     }
 
     private void packageTestIphone(TitaniumBuilder tiBuilder) throws MojoExecutionException, MojoFailureException {
         if (testExecuteMode.equals("virtual")) {
             tiBuilder.launchIphoneEmulator(getIosVersion(),
                     getPlatformTestOutputDirectory().getAbsolutePath(),
                     project.getName() + " Test",
                     platform,
                     getVirtualDevice().getFamily(),
                     getLog());
         } else if (testExecuteMode.equals("device")) {
             File tiAppFile = new File(getPlatformTestOutputDirectory(), "tiapp.xml");
             try {
                 if (tiAppFile.exists()) {
                     Tiapp tiApp = getTitaniumSettings().getTiappFromXML(tiAppFile);
                 }  else {
                     getLog().warn("Unable to find " + tiAppFile.getAbsolutePath()
                     + ". uuid will be random.");
                 }
 
             } catch (Throwable t) {
                 getLog().error("Error while parsing " + tiAppFile.getAbsolutePath()
                 + ". The application uuid will be random ");
             }
             tiBuilder.launchIphoneDevice(getIosVersion(),
                     getPlatformTestOutputDirectory(),
                     getAppId(),
                     project.getName() + " Test",
                     getTitaniumSettings().getIosDevelopmentProvisioningProfile(),
                     getTitaniumSettings().getIosDevelopmentCertificate(),
                     getVirtualDevice().getFamily(),
                     getLog());
         }
     }
 }
