 package com.artezio.rerunplugin;
 
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
 import com.intellij.execution.impl.RunManagerImpl;
 import com.intellij.execution.ui.RunContentDescriptor;
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.IconLoader;
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
 
                List<RunContentDescriptor> descriptors = executionManager.getContentManager().getAllDescriptors();
                for (RunContentDescriptor descriptor : descriptors) {
                    descriptor.getRestarter().run();
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
 
 
