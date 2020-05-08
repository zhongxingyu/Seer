 /*
  * Copyright 2010-2012 napile.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.napile.idea.plugin.run;
 
import java.util.Arrays;
import java.util.Collection;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
 import com.intellij.execution.ExecutionException;
 import com.intellij.execution.Executor;
 import com.intellij.execution.configurations.ConfigurationFactory;
 import com.intellij.execution.configurations.ModuleBasedConfiguration;
 import com.intellij.execution.configurations.RunConfiguration;
 import com.intellij.execution.configurations.RunProfileState;
 import com.intellij.execution.filters.TextConsoleBuilder;
 import com.intellij.execution.filters.TextConsoleBuilderFactory;
 import com.intellij.execution.runners.ExecutionEnvironment;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleManager;
 import com.intellij.openapi.options.SettingsEditor;
 import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectSdkTable;
 import com.intellij.openapi.projectRoots.Sdk;
 import com.intellij.openapi.util.InvalidDataException;
 import com.intellij.openapi.util.WriteExternalException;
 import com.intellij.util.xmlb.XmlSerializer;
 
 /**
  * @author VISTALL
  * @since 20:30/22.09.12
  */
 public class NapileRunConfiguration extends ModuleBasedConfiguration<NapileRunConfigurationModule>
 {
 	public String mainClass;
 	public String jdkName;
 	public String napileJvm = "C:/napile.jvm/*;";
 
 	public NapileRunConfiguration(Project project, String name, ConfigurationFactory f)
 	{
 		super(name, new NapileRunConfigurationModule(project), f);
 	}
 
 	@Override
 	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
 	{
 		return new NapileRunSettingsEditor(getProject());
 	}
 
 	@Override
 	public Collection<Module> getValidModules()
 	{
 		Module[] modules = ModuleManager.getInstance(getProject()).getModules();
 		return Arrays.asList(modules);
 	}
 
 	@Override
 	protected ModuleBasedConfiguration createInstance()
 	{
 		return new NapileRunConfiguration(getProject(), getName(), getFactory());
 	}
 
 	@Nullable
 	@Override
 	public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException
 	{
 		NapileRunningState state = new NapileRunningState(env);
 
 		final TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(getProject());
 
 		state.setConsoleBuilder(builder);
 		return state;
 	}
 
 	@Override
 	public void readExternal(final Element element) throws InvalidDataException
 	{
 		super.readExternal(element);
 
 		readModule(element);
 
 		XmlSerializer.deserializeInto(this, element);
 	}
 
 	@Override
 	public void writeExternal(final Element element) throws WriteExternalException
 	{
 		super.writeExternal(element);
 
 		writeModule(element);
 
 		XmlSerializer.serializeInto(this, element);
 	}
 
 	@Nullable
 	public Sdk findSdk()
 	{
		return ProjectSdkTable.getInstance().findSdk(jdkName);
 	}
 }
