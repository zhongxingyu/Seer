 /*
  * Copyright 2011 Stanley Shyiko
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
 package ivyplug;
 
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.actionSystem.LangDataKeys;
 import com.intellij.openapi.actionSystem.Presentation;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleManager;
 import com.intellij.openapi.progress.ProgressIndicator;
 import com.intellij.openapi.progress.Task;
 import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
 import com.intellij.util.net.HttpConfigurable;
 import ivyplug.bundles.IvyPlugBundle;
 import ivyplug.dependencies.ProjectDependenciesManager;
 import ivyplug.ui.messages.Message;
 import ivyplug.ui.messages.MessagesProjectComponent;
 import org.apache.ivy.core.module.id.ModuleRevisionId;
 import org.apache.ivy.core.report.ArtifactDownloadReport;
 import org.apache.ivy.core.report.ResolveReport;
 import org.jetbrains.annotations.NotNull;
 
import javax.swing.*;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
  * @since 31.01.2011
  */
 public class ReimportAllIvyModulesAction extends AnAction {
 
     @Override
     public void update(AnActionEvent e) {
         Presentation presentation = e.getPresentation();
         Project project = e.getData(LangDataKeys.PROJECT);
         presentation.setVisible(project != null);
     }
 
     public void actionPerformed(AnActionEvent e) {
         final Project project = e.getData(LangDataKeys.PROJECT);
         if (project == null) {
             return;
         }
         HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();
         httpConfigurable.setAuthenticator();
         new Task.Backgroundable(project, IvyPlugBundle.message("synchronizing.data"), false) {
             public void run(@NotNull ProgressIndicator indicator) {
                 indicator.setText(IvyPlugBundle.message("preparing.to.synchronize.ivy.dependencies"));
                 indicator.setFraction(0.0);
                 ModuleManager moduleManager = ModuleManager.getInstance(project);
                 ReimportManager reimportManager = ReimportManager.getInstance();
                 MessagesProjectComponent messagesProjectComponent = project.getComponent(MessagesProjectComponent.class);
                 messagesProjectComponent.closeOurMessageTabs();
                 try {
                     Map<String, ReimportManager.IvyModule> ivyModules = getIvyModules(moduleManager, indicator);
                    if (ivyModules.size() == 0) {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                Messages.showInfoMessage(IvyPlugBundle.message("no.ivy.modules.found.message"),
                                        IvyPlugBundle.message("no.ivy.modules.found.title"));
                            }
                        });
                    }
                     for (ReimportManager.IvyModule ivyModule : ivyModules.values()) {
                         List<ArtifactDownloadReport> failedArtifactsReports = ivyModule.getFailedArtifactsReports();
                         List<ArtifactDownloadReport> successfulArtifactsReports = ivyModule.getSuccessfulArtifactsReports();
                         // remove project modules from ivy reports and add them as module dependencies
                         List<ReimportManager.IvyModule> projectModules = reimportManager.removeProjectModulesFromArtifactsReports(ivyModules, failedArtifactsReports);
                         projectModules.addAll(reimportManager.removeProjectModulesFromArtifactsReports(ivyModules, successfulArtifactsReports));
                         for (ReimportManager.IvyModule projectModule : projectModules) {
                             reimportManager.addModuleDependencies(ivyModule.getModule(), projectModule.getModule());
                         }
                         // add artifact dependencies
                         reimportManager.addArtifactDependencies(ivyModule.getModule(), successfulArtifactsReports);
                         // commit all changes
                         reimportManager.commitChanges(ivyModule.getModule());
                         if (!failedArtifactsReports.isEmpty())
                             reimportManager.informAboutFailedDependencies(ivyModule.getModule(), failedArtifactsReports);
                     }
                     ProjectDependenciesManager projectDependenciesManager = project.getComponent(ProjectDependenciesManager.class);
                     projectDependenciesManager.removeUnusedLibraries();
                 } catch (IvyException ex) {
                     messagesProjectComponent.show(ex.getModule(), new Message(Message.Type.ERROR, ex.getMessage(),
                                                   IvyPlugBundle.message("ivyexception.reason", ex.getCause().getMessage())));
                 }
                 indicator.setFraction(1.0);
             }
         }.queue();
     }
 
     private Map<String, ReimportManager.IvyModule> getIvyModules(ModuleManager moduleManager, ProgressIndicator indicator) throws IvyException {
         Map<String, ReimportManager.IvyModule> result = new HashMap<String, ReimportManager.IvyModule>();
         Module[] modules = moduleManager.getModules();
         for (Module projectModule : modules) {
             IvyModuleComponent ivyModuleComponent = projectModule.getComponent(IvyModuleComponent.class);
             if (ivyModuleComponent.isIvyModule()) {
                 try {
                     indicator.setText(IvyPlugBundle.message("preparing.to.synchronize.module.ivy.dependencies", projectModule.getName()));
                     ResolveReport resolveReport = ivyModuleComponent.resolve(indicator);
                     ModuleRevisionId moduleRevisionId = resolveReport.getModuleDescriptor().getModuleRevisionId();
                     result.put(moduleRevisionId.getOrganisation() + ":" + moduleRevisionId.getName(),
                             new ReimportManager.IvyModule(projectModule, resolveReport));
                 } catch (IvyException ex) {
                     throw new IvyException(projectModule, ex.getMessage(), ex.getCause());
                 }
             }
         }
         return result;
     }
 }
