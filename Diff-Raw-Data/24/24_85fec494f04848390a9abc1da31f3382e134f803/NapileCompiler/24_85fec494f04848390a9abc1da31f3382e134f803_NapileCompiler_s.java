 /*
  * Copyright 2010-2013 napile.org
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
 
 package org.napile.idea.plugin.compiler;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jetbrains.annotations.NotNull;
 import org.napile.asm.LangVersion;
 import org.napile.asm.io.xml.out.AsmXmlFileWriter;
 import org.napile.asm.tree.members.ClassNode;
 import org.napile.compiler.NapileFileType;
 import org.napile.compiler.analyzer.AnalyzeContext;
 import org.napile.compiler.analyzer.AnalyzeExhaust;
 import org.napile.compiler.analyzer.AnalyzerFacade;
 import org.napile.compiler.codegen.CompilationErrorHandler;
 import org.napile.compiler.codegen.GenerationState;
 import org.napile.compiler.codegen.Progress;
 import org.napile.compiler.common.messages.AnalyzerWithCompilerReport;
 import org.napile.compiler.common.messages.CompilerMessageLocation;
 import org.napile.compiler.common.messages.CompilerMessageSeverity;
 import org.napile.compiler.common.messages.MessageCollector;
 import org.napile.compiler.lang.psi.NapileFile;
 import org.napile.idea.plugin.module.ModuleCollector;
 import com.google.common.base.Predicates;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.compiler.CompileContext;
 import com.intellij.openapi.compiler.CompileScope;
 import com.intellij.openapi.compiler.CompilerMessageCategory;
 import com.intellij.openapi.compiler.TranslatingCompiler;
 import com.intellij.openapi.compiler.ex.CompileContextEx;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleUtil;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.util.Chunk;
 import com.intellij.util.Function;
 import com.intellij.vcsUtil.VcsUtil;
 
 /**
  * @author VISTALL
  */
 public class NapileCompiler implements TranslatingCompiler
 {
 	@Override
 	public boolean isCompilableFile(VirtualFile virtualFile, CompileContext compileContext)
 	{
 		if(!(virtualFile.getFileType() instanceof NapileFileType))
 			return false;
 
 		return true;
 	}
 
 	@NotNull
 	@Override
 	public String getDescription()
 	{
 		return "Napile Compiler";
 	}
 
 	@Override
 	public boolean validateConfiguration(CompileScope compileScope)
 	{
 		return true;
 	}
 
 	@Override
 	public void compile(final CompileContext compileContext, Chunk<Module> moduleChunk, final VirtualFile[] virtualFiles, OutputSink outputSink)
 	{
 		if(virtualFiles.length == 0)
 			return;
 
 		final Set<Module> productionModules = new HashSet<Module>();
 		final Set<Module> testModules = new HashSet<Module>();
 		for(VirtualFile file : virtualFiles)
 		{
 			final Module moduleForFile = ModuleUtil.findModuleForFile(file, compileContext.getProject());
 			if(moduleForFile == null)
 				continue;
 
 			final boolean inTests = ((CompileContextEx) compileContext).isInTestSourceContent(file);
 			if(inTests)
 				testModules.add(moduleForFile);
 			else
 				productionModules.add(moduleForFile);
 		}
 
 		@Deprecated
 		final Module module = compileContext.getModuleByFile(virtualFiles[0]);
 
 		ApplicationManager.getApplication().runReadAction(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				runInProcess(compileContext, mergeContextForModules(compileContext.getProject(), productionModules, false), compileContext.getModuleOutputDirectory(module));
 				runInProcess(compileContext, mergeContextForModules(compileContext.getProject(), testModules, true), compileContext.getModuleOutputDirectoryForTests(module));
 			}
 		});
 	}
 
 	private static AnalyzeContext mergeContextForModules(Project project, Set<Module> modules, boolean test)
 	{
 		Set<NapileFile> files = new HashSet<NapileFile>();
 		Set<VirtualFile> bootpath = new HashSet<VirtualFile>();
 		Set<VirtualFile> classpath = new HashSet<VirtualFile>();
 		for(Module module : modules)
 		{
 			final AnalyzeContext analyzeContext = ModuleCollector.getAnalyzeContext(project, null, test, module);
 			files.addAll(analyzeContext.getFiles());
 			bootpath.addAll(analyzeContext.getBootpath());
 			classpath.addAll(analyzeContext.getClasspath());
 		}
 		return new AnalyzeContext(files, bootpath, classpath);
 	}
 
 	private static void runInProcess(final CompileContext compileContext, final AnalyzeContext context, final VirtualFile outDir)
 	{
 		AnalyzerWithCompilerReport analyzerWithCompilerReport = new AnalyzerWithCompilerReport(new MessageCollector()
 		{
 			@Override
 			public void report(@NotNull CompilerMessageSeverity severity, @NotNull String message, @NotNull CompilerMessageLocation location)
 			{
 				CompilerMessageCategory category = null;
 				switch(severity)
 				{
 					case ERROR:
 					case EXCEPTION:
 						category = CompilerMessageCategory.ERROR;
 						break;
 					case WARNING:
 						category = CompilerMessageCategory.WARNING;
 						break;
 					case INFO:
 					case LOGGING:
 						category = CompilerMessageCategory.INFORMATION;
 						break;
 				}
 				VirtualFile virtualFile = VcsUtil.getVirtualFile(location.getPath());
 				compileContext.addMessage(category, message, virtualFile == null ? null : virtualFile.getUrl(), location.getLine(), location.getColumn());
 			}
 		});
 
 		analyzerWithCompilerReport.analyzeAndReport(new Function<Void, AnalyzeExhaust>()
 		{
 			@NotNull
 			@Override
 			public AnalyzeExhaust fun(Void v)
 			{
 				return AnalyzerFacade.analyzeFiles(compileContext.getProject(), context, Predicates.<NapileFile>alwaysTrue());
 			}
 		}, context.getFiles());
 
 		AnalyzeExhaust analyzeExhaust = analyzerWithCompilerReport.hasErrors() ? null : analyzerWithCompilerReport.getAnalyzeExhaust();
 		if(analyzeExhaust == null)
 			return;
 
 		GenerationState generationState = new GenerationState(compileContext.getProject(), Progress.DEAF, analyzeExhaust, context.getFiles());
 		generationState.compileAndGenerate(CompilationErrorHandler.THROW_EXCEPTION);
 
 		AsmXmlFileWriter writer = new AsmXmlFileWriter(new File(outDir.getPath()));
 		for(ClassNode classNode : generationState.getClassNodes().values())
 			writer.write(LangVersion.CURRENT, classNode);
 	}
 }
