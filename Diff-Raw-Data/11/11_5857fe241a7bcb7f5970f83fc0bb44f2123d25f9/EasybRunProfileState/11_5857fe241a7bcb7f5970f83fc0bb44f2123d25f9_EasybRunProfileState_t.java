 package org.easyb.idea.runner;
 
 import com.intellij.execution.DefaultExecutionResult;
 import com.intellij.execution.ExecutionException;
 import com.intellij.execution.ExecutionResult;
 import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
 import com.intellij.execution.configurations.JavaCommandLineState;
 import com.intellij.execution.configurations.JavaParameters;
 import com.intellij.execution.configurations.RunnerSettings;
 import com.intellij.execution.process.ProcessHandler;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.roots.ModuleRootManager;
 import com.intellij.openapi.roots.ProjectRootManager;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.util.PathUtil;
 import groovy.lang.GroovyObject;
 import org.disco.easyb.BehaviorRunner;
 import org.easyb.plugin.remoting.RemoteExecutionListener;
 import org.easyb.plugin.ui.swing.SwingEasybBuilder;
 
 public class EasybRunProfileState extends JavaCommandLineState {
     private Module module;
     private String specificationPath;
     private SwingEasybBuilder builder;
 
     protected EasybRunProfileState(RunnerSettings runnerSettings, ConfigurationPerRunnerSettings configurationSettings,
             Module module, String specificationPath) {
         super(runnerSettings, configurationSettings);
         this.module = module;
         this.specificationPath = specificationPath;
         this.builder = new SwingEasybBuilder();
     }
 
     public ExecutionResult execute() throws ExecutionException {
         EasybConsoleView console = new EasybConsoleView(builder.getView());
         ProcessHandler processHandler = startProcess();
         processHandler.addProcessListener(new EasybProcessListener(builder.getPresenter()));
         return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler));
     }
 
     protected JavaParameters createJavaParameters() throws ExecutionException {
         RemoteExecutionListener listener = new RemoteExecutionListener();
         listener.setReceiver(builder.getPresenter());
         listener.start();
 
         JavaParameters javaParameters = new JavaParameters();
         javaParameters.setJdk(ModuleRootManager.getInstance(module).getJdk());
         javaParameters.getClassPath().add(PathUtil.getJarPathForClass(BehaviorRunner.class));
         addProjectClasspath(javaParameters);
        javaParameters.getClassPath().add(PathUtil.getJarPathForClass(getClass()));
        javaParameters.getClassPath().add(PathUtil.getJarPathForClass(GroovyObject.class));
         javaParameters.setMainClass("org.easyb.plugin.remoting.RemoteRunner");
         javaParameters.getProgramParametersList().add(Integer.toString(listener.getPort()));
         javaParameters.getProgramParametersList().add(specificationPath);
         return javaParameters;
     }
 
     private void addProjectClasspath(JavaParameters javaParameters) {
         for (VirtualFile file : getProjectClasspath()) {
             javaParameters.getClassPath().add(file);
         }
     }
 
    public Module[] getModulesToCompile() {
        return new Module[]{module};
    }

     @SuppressWarnings("deprecation")
     private VirtualFile[] getProjectClasspath() {
         return ProjectRootManager.getInstance(module.getProject()).getFullClassPath();
     }
 }
