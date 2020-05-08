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
 
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.project.Project;
 import ivyplug.bundles.IvyPlugBundle;
 import ivyplug.dependencies.DependencySyncManager;
 import ivyplug.dependencies.LibraryDependency;
 import ivyplug.ui.configuration.project.IvyProjectConfigurationProjectComponent;
 import ivyplug.ui.messages.Message;
 import ivyplug.ui.messages.MessagesProjectComponent;
 import org.apache.ivy.core.module.descriptor.Artifact;
 import org.apache.ivy.core.module.descriptor.DefaultArtifact;
 import org.apache.ivy.core.module.id.ModuleRevisionId;
 import org.apache.ivy.core.report.ArtifactDownloadReport;
 import org.apache.ivy.core.report.DownloadStatus;
 import org.apache.ivy.core.report.ResolveReport;
 import org.apache.ivy.core.resolve.IvyNode;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
  * @since 31.01.2011
  */
 public class ReimportManager {
 
     private static ReimportManager instance;
 
     private ReimportManager() {}
 
     public static ReimportManager getInstance() {
         if (instance == null) {
             instance = new ReimportManager();
         }
         return instance;
     }
 
     public List<IvyModule> removeProjectModulesFromArtifactsReports(Map<String, IvyModule> projectModules,
                                                                      List<ArtifactDownloadReport> artifactDownloadReports) {
         List<IvyModule> result = new ArrayList<IvyModule>();
         for (int i = artifactDownloadReports.size() - 1; i > -1; i--) {
             ArtifactDownloadReport artifactDownloadReport = artifactDownloadReports.get(i);
             ModuleRevisionId moduleRevisionId = artifactDownloadReport.getArtifact().getModuleRevisionId();
             String projectModule = moduleRevisionId.getOrganisation() + ":" + moduleRevisionId.getName();
             IvyModule module = projectModules.get(projectModule);
             if (module != null) {
                 result.add(module);
                 artifactDownloadReports.remove(artifactDownloadReport);
             }
         }
         return result;
     }
 
     public void informAboutFailedDependencies(Module module, List<ArtifactDownloadReport> failedDependencies) {
         Project project = module.getProject();
         Message[] messages = new Message[failedDependencies.size()];
         int i = 0;
         for (ArtifactDownloadReport failedDependency : failedDependencies) {
             messages[i++] = new Message(Message.Type.ERROR, toMessage(failedDependency));
         }
         MessagesProjectComponent messagesProjectComponent = project.getComponent(MessagesProjectComponent.class);
        messagesProjectComponent.showInNewTab(module, messages);
     }
 
     public void addArtifactDependencies(Module module, List<ArtifactDownloadReport> artifactDownloadReports) {
         DependencySyncManager dependencySyncManager = module.getComponent(DependencySyncManager.class);
         for (ArtifactDownloadReport artifactDownloadReport : artifactDownloadReports) {
             Artifact artifact = artifactDownloadReport.getArtifact();
             LibraryDependency.ArtifactType type = getType(artifact);
             if (type == null)
                 continue;
             ModuleRevisionId moduleRevisionId = artifact.getModuleRevisionId();
             dependencySyncManager.addLibraryDependency(moduleRevisionId.getOrganisation(), moduleRevisionId.getName(),
                     moduleRevisionId.getRevision(), type, artifactDownloadReport.getLocalFile());
         }
     }
 
     public void addModuleDependencies(Module module, Module dependency) {
         DependencySyncManager dependencySyncManager = module.getComponent(DependencySyncManager.class);
         dependencySyncManager.addModuleDependency(dependency);
     }
 
     public void commitChanges(Module module) {
         DependencySyncManager dependencySyncManager = module.getComponent(DependencySyncManager.class);
         IvyProjectConfigurationProjectComponent projectConfigurationComponent =
                 module.getProject().getComponent(IvyProjectConfigurationProjectComponent.class);
         dependencySyncManager.commit(projectConfigurationComponent.getConfiguration().isAutoCleanup());
     }
 
     private String[] toMessage(ArtifactDownloadReport report) {
         String downloadDetails = report.getDownloadDetails();
         String[] result;
         if (downloadDetails == null ||
             downloadDetails.equals(ArtifactDownloadReport.MISSING_ARTIFACT) ||
             downloadDetails.trim().isEmpty()) {
             result = new String[1];
         } else {
             result = new String[2];
             result[1] = IvyPlugBundle.message("ivyexception.reason", downloadDetails);
         }
         result[0] = IvyPlugBundle.message("failed.to.locate.dependency", report.getArtifact());
         return result;
     }
 
     private LibraryDependency.ArtifactType getType(Artifact artifact) {
         LibraryDependency.ArtifactType result = null;
         String artifactType = artifact.getType();
         if ("jar".equalsIgnoreCase(artifactType))
                 result = LibraryDependency.ArtifactType.CLASSES;
         else
         if ("source".equalsIgnoreCase(artifactType))
                 result = LibraryDependency.ArtifactType.SOURCES;
         else
         if ("javadoc".equalsIgnoreCase(artifactType))
                 result = LibraryDependency.ArtifactType.JAVADOCS;
         return result;
     }
 
     public static class IvyModule {
 
         private Module module;
         private List<ArtifactDownloadReport> failedArtifactsReports;
         private List<ArtifactDownloadReport> successfulArtifacts;
 
         public IvyModule(Module module) {
             this(module, null);
         }
 
         public IvyModule(Module module, ResolveReport resolveReport) {
             this.module = module;
             failedArtifactsReports = new ArrayList<ArtifactDownloadReport>();
             successfulArtifacts = new ArrayList<ArtifactDownloadReport>();
             if (resolveReport != null) {
                 for (ArtifactDownloadReport artifactDownloadReport : resolveReport.getAllArtifactsReports()) {
                     if (artifactDownloadReport.getLocalFile() == null) {
                         failedArtifactsReports.add(artifactDownloadReport);
                     } else {
                         successfulArtifacts.add(artifactDownloadReport);
                     }
                 }
                 for (IvyNode ivyNode : resolveReport.getUnresolvedDependencies()) {
                     ModuleRevisionId resolvedId = ivyNode.getResolvedId();
                     String type = resolvedId.getAttribute("type");
                     String ext = resolvedId.getAttribute("ext");
                     Artifact artifact = new DefaultArtifact(resolvedId, new Date(ivyNode.getPublication()),
                             resolvedId.getName(), type == null ? "jar" : type, ext == null ? "ext" : ext);
                     ArtifactDownloadReport artifactDownloadReport = new ArtifactDownloadReport(artifact);
                     artifactDownloadReport.setDownloadStatus(DownloadStatus.FAILED);
                     failedArtifactsReports.add(artifactDownloadReport);
                 }
             }
         }
 
         public Module getModule() {
             return module;
         }
 
         public List<ArtifactDownloadReport> getFailedArtifactsReports() {
             return failedArtifactsReports;
         }
 
         public List<ArtifactDownloadReport> getSuccessfulArtifactsReports() {
             return successfulArtifacts;
         }
     }
 
 }
