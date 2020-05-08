 /*
  * This file is part of VisualVM for IDEA
  *
  * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice,
  *       this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above copyright notice,
  *       this list of conditions and the following disclaimer in the documentation
  *       and/or other materials provided with the distribution.
  *
  *     * Neither the name of the copyright holder nor the names of its contributors
  *       may be used to endorse or promote products derived from this software
  *       without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.orfjackal.visualvm4idea.plugin;
 
 import com.intellij.execution.*;
 import com.intellij.execution.configurations.*;
 import com.intellij.execution.runners.*;
 import com.intellij.execution.ui.RunContentDescriptor;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.options.SettingsEditor;
 import com.intellij.openapi.project.Project;
 import net.orfjackal.visualvm4idea.plugin.config.JdkVersion;
 import net.orfjackal.visualvm4idea.plugin.server.*;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * @author Esko Luontola
  * @since 14.10.2008
  */
 public class CpuProfilerRunner extends JavaPatchableProgramRunner<CpuProfilerSettings> {
     private static final Logger logger = Logger.getInstance(CpuProfilerRunner.class.getName());
 
     private final VisualVmCommandSender visualvm;
 
     public CpuProfilerRunner() {
         this.visualvm = VisualVmCommandSender.getInstance();
     }
 
     @NotNull
     public String getRunnerId() {
         return CpuProfilerExecutor.EXECUTOR_ID;
     }
 
     public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return executorId.equals(CpuProfilerExecutor.EXECUTOR_ID);
     }
 
     public void patch(JavaParameters javaParameters, RunnerSettings settings, boolean beforeExecution) throws ExecutionException {
         CpuProfilerSettings profilerSettings = (CpuProfilerSettings) settings.getData();
         profilerSettings.configureOnPatch(javaParameters);
 
         // see: com.intellij.debugger.impl.DebuggerManagerImpl.createDebugParameters()
         // javaParameters.getVMParametersList().replaceOrAppend(...);
 
         // http://profiler.netbeans.org/docs/help/5.5/attach.html#direct_attach
         javaParameters.getVMParametersList().prepend(VisualVmUtil.getAppUniqueIdCommand(profilerSettings));
         javaParameters.getVMParametersList().prepend(VisualVmUtil.getAppProfilerCommand(JdkVersion.JDK15));
     }
 
     protected RunContentDescriptor doExecute(Project project, Executor executor, RunProfileState state,
                                              RunContentDescriptor contentToReuse, ExecutionEnvironment env) throws ExecutionException {
         return DefaultJavaProgramRunnerWrapper.doExecute(this, project, executor, state, contentToReuse, env);
     }
 
     public void onProcessStarted(RunnerSettings settings, ExecutionResult executionResult) {
         CpuProfilerSettings profilerSettings = (CpuProfilerSettings) settings.getData();
 
         visualvm.beginProfilingApplicationCPU(
                 profilerSettings.getAppUniqueId(),
                 VisualVmCommandSender.PROFILER_PORT,
                 profilerSettings.profileNewRunnables,
                 profilerSettings.getClassesToProfileFrom(),
                 profilerSettings.getFilterType(),
                 profilerSettings.getFilterValue()
         );
     }
 
     public CpuProfilerSettings createConfigurationData(ConfigurationInfoProvider settingsProvider) {
         return new CpuProfilerSettings();
     }
 
     public void checkConfiguration(RunnerSettings settings, ConfigurationPerRunnerSettings configurationPerRunnerSettings)
             throws RuntimeConfigurationException {
         CpuProfilerSettings profilerSettings = (CpuProfilerSettings) settings.getData();
         VisualVmUtil.checkCurrentConfig();
     }
 
     public SettingsEditor<CpuProfilerSettings> getSettingsEditor(Executor executor, RunConfiguration configuration) {
         return new CpuProfilerSettingsEditor();
     }
 }
