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
 package com.google.jstestdriver.idea;
 
 import com.google.jstestdriver.idea.ui.ConfigurationForm;
 import com.intellij.execution.ExecutionException;
 import com.intellij.execution.Executor;
 import com.intellij.execution.configurations.ConfigurationFactory;
 import com.intellij.execution.configurations.ModuleBasedConfiguration;
 import com.intellij.execution.configurations.RunConfiguration;
 import com.intellij.execution.configurations.RunConfigurationModule;
 import com.intellij.execution.configurations.RunProfileState;
 import com.intellij.execution.configurations.RuntimeConfigurationException;
 import com.intellij.execution.runners.ExecutionEnvironment;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleManager;
 import com.intellij.openapi.options.SettingsEditor;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.InvalidDataException;
 import com.intellij.openapi.util.JDOMExternalizer;
 import com.intellij.openapi.util.WriteExternalException;
 import org.jdom.Element;
 import org.jetbrains.annotations.NotNull;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * One configured instance of the Run Configuration. The user can create several different configs
  * and save them all.
  * @author alexeagle@google.com (Alex Eagle)
  */
 public class JSTestDriverConfiguration extends ModuleBasedConfiguration<RunConfigurationModule> {
 
   private final ConfigurationFactory jsTestDriverConfigurationFactory;
 
   public String settingsFile;
   private String serverAddress;
   private ServerType serverType;
 
   public JSTestDriverConfiguration(Project project,
                                    ConfigurationFactory jsTestDriverConfigurationFactory,
                                    String pluginName) {
     super(pluginName, new RunConfigurationModule(project), jsTestDriverConfigurationFactory);
     this.jsTestDriverConfigurationFactory = jsTestDriverConfigurationFactory;
   }
 
   public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
     return new ConfigurationForm();
   }
 
   public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env)
       throws ExecutionException {
     return new TestRunnerState(this, getProject(), env, getConfigurationModule());
   }
 
   @Override
   public void checkConfiguration() throws RuntimeConfigurationException {
     if (settingsFile == null) {
       throw new RuntimeConfigurationException("Settings file is required");
     }
     if (!new File(settingsFile).exists()) {
       throw new RuntimeConfigurationException("Settings file does not exist: " + settingsFile);
     }
   }
 
  @Override
   public Collection<Module> getValidModules() {
     List<Module> modules = new ArrayList<Module>();
     Module[] allModules = ModuleManager.getInstance(getProject()).getModules();
     modules.addAll(Arrays.asList(allModules));
     return modules;
   }
 
  @Override
   protected ModuleBasedConfiguration createInstance() {
     return new JSTestDriverConfiguration(getConfigurationModule().getProject(),
         jsTestDriverConfigurationFactory, getName());
   }
 
   @Override
   public void readExternal(Element element) throws InvalidDataException {
     super.readExternal(element);
     readModule(element);
     settingsFile = JDOMExternalizer.readString(element, "settingsFile");
     serverAddress = JDOMExternalizer.readString(element, "serverAddress");
     String serverTypeStr = JDOMExternalizer.readString(element, "serverType");
     if (serverTypeStr != null) {
       serverType = ServerType.valueOf(serverTypeStr);
     }
   }
 
   @Override
   public void writeExternal(Element element) throws WriteExternalException {
     super.writeExternal(element);
     writeModule(element);
     JDOMExternalizer.write(element, "settingsFile", settingsFile);
     JDOMExternalizer.write(element, "serverAddress", serverAddress);
     if (serverType != null) {
       JDOMExternalizer.write(element, "serverType", serverType.name());
     }
   }
 
   public String getSettingsFile() {
     return settingsFile;
   }
 
   public void setSettingsFile(String settingsFile) {
     this.settingsFile = settingsFile;
   }
 
   public String getServerAddress() {
     return serverAddress;
   }
 
   public void setServerAddress(String serverAddress) {
     this.serverAddress = serverAddress;
   }
 
   public void setServerType(ServerType serverType) {
     this.serverType = serverType;
   }
 
   public ServerType getServerType() {
     return serverType;
   }
 }
