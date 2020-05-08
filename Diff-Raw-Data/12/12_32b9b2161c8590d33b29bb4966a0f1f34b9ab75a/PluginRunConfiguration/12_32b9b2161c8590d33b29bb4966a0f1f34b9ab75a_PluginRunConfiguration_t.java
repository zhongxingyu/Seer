 /*
  * Copyright 2000-2012 JetBrains s.r.o.
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
 package org.jetbrains.idea.devkit.run;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Set;
 
 import org.consulo.sdk.SdkPointerManager;
 import org.consulo.sdk.SdkUtil;
 import org.consulo.util.pointers.Named;
 import org.consulo.util.pointers.NamedPointer;
 import org.consulo.util.pointers.NamedPointerManager;
 import org.jdom.Element;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.jetbrains.idea.devkit.DevKitBundle;
import com.intellij.compiler.CompilerConfiguration;
 import com.intellij.execution.ExecutionException;
 import com.intellij.execution.Executor;
 import com.intellij.execution.configurations.ConfigurationFactory;
 import com.intellij.execution.configurations.JavaCommandLineState;
 import com.intellij.execution.configurations.JavaParameters;
 import com.intellij.execution.configurations.ModuleRunProfile;
 import com.intellij.execution.configurations.ParametersList;
 import com.intellij.execution.configurations.RunConfiguration;
 import com.intellij.execution.configurations.RunConfigurationBase;
 import com.intellij.execution.configurations.RunProfileState;
 import com.intellij.execution.configurations.RuntimeConfigurationException;
 import com.intellij.execution.filters.TextConsoleBuilderFactory;
 import com.intellij.execution.runners.ExecutionEnvironment;
 import com.intellij.openapi.application.PathManager;
 import com.intellij.openapi.components.ServiceManager;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.options.SettingsEditor;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.projectRoots.JavaSdkType;
 import com.intellij.openapi.projectRoots.Sdk;
 import com.intellij.openapi.util.DefaultJDOMExternalizer;
 import com.intellij.openapi.util.InvalidDataException;
 import com.intellij.openapi.util.NotNullFactory;
 import com.intellij.openapi.util.SystemInfo;
 import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VfsUtilCore;
 import com.intellij.packaging.artifacts.Artifact;
 import com.intellij.packaging.artifacts.ArtifactPointerUtil;
 import com.intellij.packaging.impl.artifacts.ArtifactUtil;
 
 public class PluginRunConfiguration extends RunConfigurationBase implements ModuleRunProfile
 {
 	private static final String JAVA_SDK = "java-sdk";
 	private static final String CONSULO_SDK = "consulo-sdk";
 	private static final String ARTIFACT = "artifact";
 	private static final String NAME = "name";
 	public String VM_PARAMETERS;
 	public String PROGRAM_PARAMETERS;
 	private NamedPointer<Sdk> myJavaSdkPointer;
 	private NamedPointer<Sdk> myConsuloSdkPointer;
 	private NamedPointer<Artifact> myArtifactPointer;
 
 	protected PluginRunConfiguration(Project project, ConfigurationFactory factory, String name)
 	{
 		super(project, factory, name);
 	}
 
 	private static void fillParameterList(ParametersList list, @Nullable String value)
 	{
 		if(value == null)
 		{
 			return;
 		}
 
 		for(String parameter : value.split(" "))
 		{
 			if(parameter != null && parameter.length() > 0)
 			{
 				list.add(parameter);
 			}
 		}
 	}
 
 	@Nullable
 	private static <T extends Named> NamedPointer<T> readPointer(String childName, Element parent, NotNullFactory<NamedPointerManager<T>> fun)
 	{
 		final NamedPointerManager<T> namedPointerManager = fun.create();
 
 		Element child = parent.getChild(childName);
 		if(child != null)
 		{
 			final String attributeValue = child.getAttributeValue(NAME);
 			if(attributeValue != null)
 			{
 				return namedPointerManager.create(attributeValue);
 			}
 		}
 		return null;
 	}
 
 	private static void writePointer(String childName, Element parent, NamedPointer<?> pointer)
 	{
 		if(pointer == null)
 		{
 			return;
 		}
 		Element element = new Element(childName);
 		element.setAttribute(NAME, pointer.getName());
 
 		parent.addContent(element);
 	}
 
 	@Override
 	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
 	{
 		return new PluginRunConfigurationEditor(getProject());
 	}
 
 	@Override
 	public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException
 	{
 		final Sdk javaSdk = myJavaSdkPointer == null ? null : myJavaSdkPointer.get();
 		if(javaSdk == null)
 		{
 			throw new ExecutionException(DevKitBundle.message("run.configuration.no.java.sdk"));
 		}
 
 		final Sdk consuloSdk = myConsuloSdkPointer == null ? null : myConsuloSdkPointer.get();
 		if(consuloSdk == null)
 		{
 			throw new ExecutionException(DevKitBundle.message("run.configuration.no.consulo.sdk"));
 		}
 
 		final Artifact artifact = myArtifactPointer == null ? null : myArtifactPointer.get();
 		if(artifact == null)
 		{
 			throw new ExecutionException(DevKitBundle.message("run.configuration.no.plugin.artifact"));
 		}
 
 		final String dataPath;
 		try
 		{
			CompilerConfiguration compilerPathsManager = CompilerConfiguration.getInstance(env.getProject());
 
			String path = VfsUtilCore.urlToPath(compilerPathsManager.getCompilerOutputUrl());
			File temp = new File(path, "sandbox");
 			dataPath = temp.getCanonicalPath();
 		}
 		catch(IOException e)
 		{
 			throw new ExecutionException(e);
 		}
 
 		final JavaCommandLineState state = new JavaCommandLineState(env)
 		{
 			@Override
 			protected JavaParameters createJavaParameters() throws ExecutionException
 			{
 
 				final JavaParameters params = new JavaParameters();
 
 				ParametersList vm = params.getVMParametersList();
 
 				fillParameterList(vm, VM_PARAMETERS);
 				fillParameterList(params.getProgramParametersList(), PROGRAM_PARAMETERS);
 
 				@NonNls String libPath = consuloSdk.getHomePath() + File.separator + "lib";
 				vm.add("-Xbootclasspath/a:" + libPath + File.separator + "boot.jar");
 
 				vm.defineProperty(PathManager.PROPERTY_CONFIG_PATH, dataPath + File.separator + "config");
 				vm.defineProperty(PathManager.PROPERTY_SYSTEM_PATH, dataPath + File.separator + "system");
 				vm.defineProperty(PathManager.PROPERTY_PLUGINS_PATH, artifact.getOutputPath());
 
 				if(SystemInfo.isMac)
 				{
 					vm.defineProperty("idea.smooth.progress", "false");
 					vm.defineProperty("apple.laf.useScreenMenuBar", "true");
 				}
 
 				if(SystemInfo.isXWindow)
 				{
 					if(VM_PARAMETERS == null || !VM_PARAMETERS.contains("-Dsun.awt.disablegrab"))
 					{
 						vm.defineProperty("sun.awt.disablegrab", "true"); // See http://devnet.jetbrains.net/docs/DOC-1142
 					}
 
 				}
 				params.setWorkingDirectory(consuloSdk.getHomePath() + File.separator + "bin" + File.separator);
 
 				params.setJdk(javaSdk);
 
 				params.getClassPath().addFirst(libPath + File.separator + "log4j.jar");
 				params.getClassPath().addFirst(libPath + File.separator + "jdom.jar");
 				params.getClassPath().addFirst(libPath + File.separator + "trove4j.jar");
 				params.getClassPath().addFirst(libPath + File.separator + "util.jar");
 				params.getClassPath().addFirst(libPath + File.separator + "extensions.jar");
 				params.getClassPath().addFirst(libPath + File.separator + "bootstrap.jar");
 				params.getClassPath().addFirst(libPath + File.separator + "idea.jar");
 				params.getClassPath().addFirst(((JavaSdkType) javaSdk.getSdkType()).getToolsPath(javaSdk));
 
 				params.setMainClass("com.intellij.idea.Main");
 
 				return params;
 			}
 		};
 
 		state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
 		return state;
 	}
 
 	@Override
 	public void checkConfiguration() throws RuntimeConfigurationException
 	{
 
 	}
 
 	@Override
 	public void readExternal(Element element) throws InvalidDataException
 	{
 		DefaultJDOMExternalizer.readExternal(this, element);
 
 		myJavaSdkPointer = readPointer(JAVA_SDK, element, new NotNullFactory<NamedPointerManager<Sdk>>()
 		{
 			@NotNull
 			@Override
 			public NamedPointerManager<Sdk> create()
 			{
 				return ServiceManager.getService(SdkPointerManager.class);
 			}
 		});
 
 		myConsuloSdkPointer = readPointer(CONSULO_SDK, element, new NotNullFactory<NamedPointerManager<Sdk>>()
 		{
 			@NotNull
 			@Override
 			public NamedPointerManager<Sdk> create()
 			{
 				return ServiceManager.getService(SdkPointerManager.class);
 			}
 		});
 
 		myArtifactPointer = readPointer(ARTIFACT, element, new NotNullFactory<NamedPointerManager<Artifact>>()
 		{
 			@NotNull
 			@Override
 			public NamedPointerManager<Artifact> create()
 			{
 				return ArtifactPointerUtil.getPointerManager(getProject());
 			}
 		});
 
 		super.readExternal(element);
 	}
 
 	@Override
 	public void writeExternal(Element element) throws WriteExternalException
 	{
 		DefaultJDOMExternalizer.writeExternal(this, element);
 
 		writePointer(JAVA_SDK, element, myJavaSdkPointer);
 		writePointer(CONSULO_SDK, element, myConsuloSdkPointer);
 		writePointer(ARTIFACT, element, myArtifactPointer);
 
 		super.writeExternal(element);
 	}
 
 	@Nullable
 	public String getArtifactName()
 	{
 		return myArtifactPointer == null ? null : myArtifactPointer.getName();
 	}
 
 	public void setArtifactName(@Nullable String name)
 	{
 		myArtifactPointer = name == null ? null : ArtifactPointerUtil.getPointerManager(getProject()).create(name);
 	}
 
 	@Nullable
 	public String getJavaSdkName()
 	{
 		return myJavaSdkPointer == null ? null : myJavaSdkPointer.getName();
 	}
 
 	public void setJavaSdkName(@Nullable String name)
 	{
 		myJavaSdkPointer = name == null ? null : SdkUtil.createPointer(name);
 	}
 
 	@Nullable
 	public String getConsuloSdkName()
 	{
 		return myConsuloSdkPointer == null ? null : myConsuloSdkPointer.getName();
 	}
 
 	public void setConsuloSdkName(@Nullable String name)
 	{
 		myConsuloSdkPointer = name == null ? null : SdkUtil.createPointer(name);
 	}
 
 	@NotNull
 	@Override
 	public Module[] getModules()
 	{
 		Artifact artifact = myArtifactPointer == null ? null : myArtifactPointer.get();
 		if(artifact == null)
 		{
 			return Module.EMPTY_ARRAY;
 		}
 		final Set<Module> modules = ArtifactUtil.getModulesIncludedInArtifacts(Collections.singletonList(artifact), getProject());
 
 		return modules.isEmpty() ? Module.EMPTY_ARRAY : modules.toArray(new Module[modules.size()]);
 	}
 }
