 /*
 * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.eclipse.javascript.jstestdriver.ui.runner;
 
 import com.google.eclipse.javascript.jstestdriver.core.Server;
 import com.google.eclipse.javascript.jstestdriver.ui.launch.JavascriptLaunchConfigurationHelper;
 import com.google.eclipse.javascript.jstestdriver.ui.launch.LaunchConfigurationConstants;
 import com.google.inject.AbstractModule;
 import com.google.inject.Module;
 import com.google.inject.Singleton;
 import com.google.inject.name.Names;
 import com.google.jstestdriver.ActionRunner;
 import com.google.jstestdriver.ConfigurationParser;
 import com.google.jstestdriver.IDEPluginActionBuilder;
 import com.google.jstestdriver.JsTestDriverModule.BrowserCount;
 import com.google.jstestdriver.JsTestDriverModule.BrowserCountProvider;
 import com.google.jstestdriver.PluginLoader;
 import com.google.jstestdriver.guice.TestResultPrintingModule;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Factory which knows how to produce {@link ActionRunner}s for purposes like
  * Dry runs and Test runs. Convenience class.
  *
  * @author shyamseshadri@gmail.com (Shyam Seshadri)
  */
 public class ActionRunnerFactory {
   private static final Logger logger =
       Logger.getLogger(ActionRunnerFactory.class.getCanonicalName());
 
   private final JavascriptLaunchConfigurationHelper configurationHelper =
       new JavascriptLaunchConfigurationHelper();
 
   /**
    * Gets an action runner for a dry run of all the tests.
    *
    * @param configuration the launch configuration
    * @return {@link ActionRunner} setup for a dry run of all the tests
    * @throws FileNotFoundException when the configuration file is not found
    */
   public ActionRunner getDryActionRunner(ILaunchConfiguration configuration)
       throws FileNotFoundException {
     return getActionBuilder(configuration).dryRunFor(Arrays.asList("all")).build();
   }
 
   /**
    * Gets an action runner for a dry run of specified tests.
    *
    * @param configuration the launch configuration
    * @param tests the list of tests for which the dry run is needed
    * @return {@link ActionRunner} setup for a dry run of specified tests
    * @throws FileNotFoundException when the configuration file is not found
    */
   public ActionRunner getDryActionRunner(ILaunchConfiguration configuration, List<String> tests)
       throws FileNotFoundException {
     return getActionBuilder(configuration).dryRunFor(tests).build();
   }
 
   /**
    * Gets an action runner for running all the tests.
    *
    * @param configuration the launch configuration
    * @return {@link ActionRunner} setup for a running all the tests
    * @throws FileNotFoundException when the configuration file is not found
    */
   public ActionRunner getAllTestsActionRunner(ILaunchConfiguration configuration)
       throws FileNotFoundException {
     return getActionBuilder(configuration).addAllTests().build();
   }
 
   /**
    * Gets an action runner for resetting all the captured browsers.
    *
    * @param configuration the launch configuration
    * @return {@link ActionRunner} setup for a resetting all the captured
    *         browsers
    * @throws FileNotFoundException when the configuration file is not found
    */
   public ActionRunner getResetBrowsersActionRunner(ILaunchConfiguration configuration)
       throws FileNotFoundException {
     return getActionBuilder(configuration).resetBrowsers().build();
   }
 
   /**
    * Gets an action runner for running just the specified tests.
    *
    * @param configuration the launch configuration
    * @param testCases a list of tests which need to be executed
    * @return {@link ActionRunner} setup for running specific tests
    * @throws FileNotFoundException when the configuration file is not found
    */
   public ActionRunner getSpecificTestsActionRunner(ILaunchConfiguration configuration,
       List<String> testCases) throws FileNotFoundException {
     return getActionBuilder(configuration).addTests(testCases).build();
   }
 
   private IDEPluginActionBuilder getActionBuilder(ILaunchConfiguration configuration)
       throws FileNotFoundException {
     String serverUrl = Server.getInstance().getServerUrl();
     String projectName = "";
     String confFileName = "";
     try {
         projectName = configuration.getAttribute(
             LaunchConfigurationConstants.PROJECT_NAME, "");
         confFileName = configuration.getAttribute(
             LaunchConfigurationConstants.CONF_FILENAME, "");
       } catch (CoreException e) {
         logger.log(Level.SEVERE, "", e);
       }
       
     final File runfilesDir = new File(".");
 
     ConfigurationParser parser =
         configurationHelper.getConfigurationParser(projectName, confFileName);
 
     IDEPluginActionBuilder pluginActionBuilder =
         new IDEPluginActionBuilder(parser, serverUrl,
            new EclipseResponseStreamFactory());
     pluginActionBuilder.install(new AbstractModule() {
       @Override
       protected void configure() {
         bind(File.class).annotatedWith(Names.named("basePath"))
                         .toInstance(runfilesDir);
         bind(Integer.class).annotatedWith(BrowserCount.class)
             .toProvider(BrowserCountProvider.class).in(Singleton.class);
         bind(PrintStream.class)
             .annotatedWith(Names.named("outputStream")).toInstance(System.out);
       }
     });
     List<Module> modules = new PluginLoader().load(parser.getPlugins());
     for (Module module : modules) {
       pluginActionBuilder.install(module);
     }
     pluginActionBuilder.install(new TestResultPrintingModule(""));
     return pluginActionBuilder;
   }
 }
