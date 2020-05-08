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
 package com.google.jstestdriver.eclipse.ui.launch;
 
 import static java.lang.String.format;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.jstestdriver.ActionFactory;
 import com.google.jstestdriver.ActionFactoryModule;
 import com.google.jstestdriver.ActionRunner;
 import com.google.jstestdriver.ConfigurationParser;
 import com.google.jstestdriver.IDEPluginActionBuilder;
 import com.google.jstestdriver.eclipse.core.Server;
 import com.google.jstestdriver.eclipse.internal.core.Logger;
 import com.google.jstestdriver.eclipse.internal.core.ProjectHelper;
 import com.google.jstestdriver.eclipse.ui.Activator;
 import com.google.jstestdriver.eclipse.ui.WorkbenchPreferencePage;
 
 /**
  * @author shyamseshadri@google.com (Shyam Seshadri)
  *
  */
 public class ActionRunnerFactory {
   private final ProjectHelper projectHelper = new ProjectHelper();
   private final Injector injector = Guice.createInjector(new ActionFactoryModule());
   private final Logger logger = new Logger();
   
   public ActionRunner getDryActionRunner(ILaunchConfiguration configuration) {
     return getActionBuilder(configuration).dryRun().build();
   }
   
   public ActionRunner getAllTestsActionRunner(ILaunchConfiguration configuration) {
     return getActionBuilder(configuration).addAllTests().build();
   }
 
   public ActionRunner getResetBrowsersActionRunner(ILaunchConfiguration configuration) {
     return getActionBuilder(configuration).resetBrowsers().build();
   }
 
   public ActionRunner getSpecificTestsActionRunner(ILaunchConfiguration configuration,
       List<String> testCases) {
     return getActionBuilder(configuration).addTests(testCases).build();
   }
 
   private IDEPluginActionBuilder getActionBuilder(ILaunchConfiguration configuration) {
     int port = Activator.getDefault().getPreferenceStore().getInt(
         WorkbenchPreferencePage.PREFERRED_SERVER_PORT);
     String serverUrl = format(Server.SERVER_URL, port);
     String projectName = "";
     String confFileName = "";
     try {
       projectName = configuration.getAttribute(
           LaunchConfigurationConstants.PROJECT_NAME, "");
       confFileName = configuration.getAttribute(
           LaunchConfigurationConstants.CONF_FILENAME, "");
     } catch (CoreException e) {
       logger.logException(e);
     }
     ConfigurationParser configurationParser = projectHelper.getConfigurationParser(projectName,
         confFileName);
    return new IDEPluginActionBuilder(configurationParser, serverUrl,
        injector.getInstance(ActionFactory.class), new EclipseResponseStreamFactory());
   }
 }
