 package com.artezio.rerunplugin;
 
import com.intellij.execution.*;
import com.intellij.execution.executors.DefaultRunExecutor;
 import com.intellij.execution.impl.RunManagerImpl;
 import com.intellij.execution.ui.RunContentDescriptor;
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
 import com.intellij.openapi.wm.ToolWindow;
 import com.intellij.openapi.wm.ToolWindowManager;
 import com.intellij.ui.content.Content;
 
 import java.util.Collection;
 import java.util.List;
 
 /**
  * User: Galoshin Evgeniy
  * Date: 16.05.13 15:09
  */
 public class RerunPlugin extends AnAction {
 
     public void actionPerformed(final AnActionEvent e) {
         Project project = e.getProject();
         if (project == null) {
             return;
         }
         ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Run");
         if (window == null) {
             return;
         }
 
         for (final Content content : window.getContentManager().getContents()) {
             final RunManagerImpl runManager = (RunManagerImpl) RunManager.getInstance(project);
             final Collection<RunnerAndConfigurationSettings> allConfigurations = runManager.getSortedConfigurations();
 
             for (RunnerAndConfigurationSettings runConfiguration : allConfigurations) {
                 System.out.println(runConfiguration.getName());
 
                 ExecutionManager executionManager = ExecutionManager.getInstance(project);
 
                RunContentDescriptor descriptor = content.getUserData(new Key<RunContentDescriptor>("Descriptor"));
                RunnerAndConfigurationSettings configurationByName = runManager.findConfigurationByName(runConfiguration.getName());
                if (configurationByName == null) {
                    continue;
                }
                List<ExecutionTarget> targets = ExecutionTargetManager.getTargetsFor(project, configurationByName);

                for (ExecutionTarget target : targets) {
                    executionManager.restartRunProfile(project,
                            DefaultRunExecutor.getRunExecutorInstance(),
                            target,
                            configurationByName,
                            descriptor);
                 }
             }
         }
     }
 
     private void turnOffCheckbox(Content content) {
         content.setIcon(IconLoader.getIcon("/resources/images/checkbox_0.png"));
     }
 
     private void turnOnCheckbox(Content content) {
         content.setIcon(IconLoader.getIcon("/resources/images/checkbox_1.png"));
     }
 }
 
 
