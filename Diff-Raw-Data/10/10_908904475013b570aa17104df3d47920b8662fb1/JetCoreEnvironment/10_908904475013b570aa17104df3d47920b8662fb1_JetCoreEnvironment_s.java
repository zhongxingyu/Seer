 /*
  * Copyright 2010-2012 JetBrains s.r.o.
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
 
 package org.napile.compiler.common;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jetbrains.annotations.NotNull;
 import org.napile.compiler.NXmlFileType;
 import org.napile.compiler.NapileFileType;
 import org.napile.compiler.config.CompilerConfiguration;
 import org.napile.compiler.lang.parsing.NapileParserDefinition;
 import org.napile.compiler.lang.psi.NapileFile;
 import org.napile.compiler.lang.psi.impl.file.NXmlFileViewProviderFactory;
 import org.napile.compiler.lang.resolve.NapileFilesProvider;
 import com.intellij.core.CoreApplicationEnvironment;
 import com.intellij.mock.MockApplication;
 import com.intellij.mock.MockProject;
 import com.intellij.openapi.Disposable;
 import com.intellij.openapi.fileTypes.FileType;
 import com.intellij.openapi.fileTypes.FileTypeExtension;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.roots.PackageIndex;
 import com.intellij.openapi.util.Disposer;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.FileTypeFileViewProviders;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiManager;
 
 /**
  * @author yole
  */
 public class JetCoreEnvironment
 {
 	private final Disposable parentDisposable;
 	private final CoreApplicationEnvironment applicationEnvironment;
 	private final JetCoreProjectEnvironment projectEnvironment;
 
 	private final List<NapileFile> sourceFiles = new ArrayList<NapileFile>();
 
 	private final CompilerConfiguration configuration;
 
 	private boolean initialized = false;
 
 	public JetCoreEnvironment(Disposable parentDisposable, @NotNull CompilerConfiguration configuration)
 	{
 		this.parentDisposable = parentDisposable;
 		this.configuration = configuration.copy();
 		this.configuration.setReadOnly(true);
 
 		this.applicationEnvironment = new CoreApplicationEnvironment(parentDisposable);
 		applicationEnvironment.registerFileType(NapileFileType.INSTANCE, NapileFileType.INSTANCE.getDefaultExtension());
 		applicationEnvironment.registerFileType(NXmlFileType.INSTANCE, NXmlFileType.INSTANCE.getDefaultExtension());
 		applicationEnvironment.registerParserDefinition(new NapileParserDefinition());
//		for(CodeInjection injection : CodeInjectionManager.INSTANCE.getCodeInjections())
//			applicationEnvironment.registerParserDefinition(injection);
 
 		addExplicitExtension(FileTypeFileViewProviders.INSTANCE, NXmlFileType.INSTANCE, new NXmlFileViewProviderFactory());
 
 		projectEnvironment = new JetCoreProjectEnvironment(parentDisposable, applicationEnvironment);
 
 
 		final MockProject project = projectEnvironment.getProject();
 		project.registerService(NapileFilesProvider.class, new CompilerFilesProvider(this));
 
 		for(File path : configuration.getList(CompilerConfigurationKeys.CLASSPATH_KEY))
 			addToClasspath(path);
 
 		for(String path : configuration.getList(CompilerConfigurationKeys.SOURCE_ROOTS_KEY))
 			addSources(path);
 
 		PackageIndex packageIndex = PackageIndex.getInstance(project);
 
 		VirtualFile[] virtualFiles = packageIndex.getDirectoriesByPackageName("gen", true);
 
 	/*	for(VirtualFile v : virtualFiles)
 		{
 			VfsUtilCore.visitChildrenRecursively(v, new VirtualFileVisitor()
 			{
 				@Override
 				public boolean visitFile(@NotNull VirtualFile file)
 				{
 					if(file.isDirectory())
 						return true;
 					PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
 
 					return super.visitFile(file);
 				}
 			});
 		}   */
 
 		initialized = true;
 	}
 
 
 	protected <T> void addExplicitExtension(final FileTypeExtension<T> instance, final FileType fileType, final T object)
 	{
 		instance.addExplicitExtension(fileType, object);
 		Disposer.register(parentDisposable, new Disposable()
 		{
 			@Override
 			public void dispose()
 			{
 				instance.removeExplicitExtension(fileType, object);
 			}
 		});
 	}
 
 	public CompilerConfiguration getConfiguration()
 	{
 		return configuration;
 	}
 
 	@NotNull
 	public MockApplication getApplication()
 	{
 		return applicationEnvironment.getApplication();
 	}
 
 	@NotNull
 	public Project getProject()
 	{
 		return projectEnvironment.getProject();
 	}
 
 	private void addSources(File file)
 	{
 		if(file.isDirectory())
 		{
 			File[] files = file.listFiles();
 			if(files != null)
 			{
 				for(File child : files)
 					addSources(child);
 			}
 		}
 		else
 		{
 			VirtualFile fileByPath = applicationEnvironment.getLocalFileSystem().findFileByPath(file.getAbsolutePath());
 			if(fileByPath != null)
 			{
 				PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(fileByPath);
 				if(psiFile instanceof NapileFile)
 					sourceFiles.add((NapileFile) psiFile);
 			}
 		}
 	}
 
 	private void addSources(String path)
 	{
 		if(path == null)
 			return;
 
 		VirtualFile vFile = applicationEnvironment.getLocalFileSystem().findFileByPath(path);
 		if(vFile == null)
 			throw new CompileEnvironmentException("File/directory not found: " + path);
 
 		if(!vFile.isDirectory() && vFile.getFileType() != NapileFileType.INSTANCE)
 			throw new CompileEnvironmentException("Not a Kotlin file: " + path);
 
 		addSources(new File(path));
 	}
 
 	public void addToClasspath(File path)
 	{
 		if(initialized)
 			throw new IllegalStateException("Cannot add class path when JetCoreEnvironment is already initialized");
 
 		if(path.isFile())
 			projectEnvironment.addJarToClassPath(path);
 		else
 		{
 			final VirtualFile root = applicationEnvironment.getLocalFileSystem().findFileByPath(path.getAbsolutePath());
 			if(root == null)
 			{
 				throw new IllegalArgumentException("trying to add non-existing file to classpath: " + path);
 			}
 			projectEnvironment.addSourcesToClasspath(root);
 		}
 	}
 
 	public List<NapileFile> getSourceFiles()
 	{
 		return sourceFiles;
 	}
 }
