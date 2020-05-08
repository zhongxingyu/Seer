 /**
  * Copyright 2010 OpenEngSB Division, Vienna University of Technology
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.opencit.core.projectmanager.internal;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.openengsb.core.common.Domain;
 import org.openengsb.core.common.context.ContextCurrentService;
 import org.openengsb.core.common.persistence.PersistenceException;
 import org.openengsb.core.common.persistence.PersistenceManager;
 import org.openengsb.core.common.persistence.PersistenceService;
 import org.openengsb.core.common.workflow.WorkflowService;
 import org.openengsb.domain.report.ReportDomain;
 import org.openengsb.domain.scm.ScmDomain;
 import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
 import org.openengsb.opencit.core.projectmanager.ProjectAlreadyExistsException;
 import org.openengsb.opencit.core.projectmanager.ProjectManager;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 import org.openengsb.opencit.core.projectmanager.model.Project.State;
 import org.osgi.framework.BundleContext;
 import org.springframework.osgi.context.BundleContextAware;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 public class ProjectManagerImpl implements ProjectManager, BundleContextAware {
 
     private PersistenceManager persistenceManager;
 
     private PersistenceService persistence;
 
     private ContextCurrentService contextService;
 
     private BundleContext bundleContext;
 
     private ScmDomain scmDomain;
 
     private WorkflowService workflowService;
 
     private Map<String, ScmStatePoller> pollers = new HashMap<String, ScmStatePoller>();
 
     private long timeout = 600000L;
 
     private ReportDomain reportDomain;
 
     private AuthenticationManager authenticationManager;
 
     public void setAuthenticationManager(AuthenticationManager authenticationManager) {
         this.authenticationManager = authenticationManager;
     }
 
     public void init() {
         persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
         startPollingForPresentProjects();
     }
 
     private void startPollingForPresentProjects() {
         List<Project> projects = getAllProjects();
         for (Project project : projects) {
             setupAndStartScmPoller(project);
         }
     }
 
     @Override
     public void createProject(Project project) throws ProjectAlreadyExistsException {
         checkId(project.getId());
         try {
             persistence.create(project);
             setupProject(project);
         } catch (PersistenceException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void setupProject(Project project) {
         String oldCurrent = contextService.getThreadLocalContext();
         createAndSetContext(project);
         setDefaultConnectors(project);
         if (oldCurrent != null) {
             contextService.setThreadLocalContext(oldCurrent);
         }
         setupAndStartScmPoller(project);
     }
 
     private void setupAndStartScmPoller(final Project project) {
         Thread thread = new Thread(){
             @Override
             public void run() {
                 SecurityContextHolder.clearContext();
                 ScmStatePoller poller = new ScmStatePoller(authenticationManager);
                 poller.setProjectId(project.getId());
                 poller.setContextService(contextService);
                 poller.setTimeout(timeout);
                 poller.setScm(scmDomain);
                 poller.setWorkflowService(workflowService);
                 pollers.put(project.getId(), poller);
                 poller.start();
             }
         };
         thread.start();
         try {
             thread.join();
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void createAndSetContext(Project project) {
         try {
             contextService.createContext(project.getId());
         } catch (IllegalArgumentException iae) {
             // ignore - means that context already exists
         }
         contextService.setThreadLocalContext(project.getId());
     }
 
     private void setDefaultConnectors(Project project) {
         Map<Class<? extends Domain>, String> services = project.getServices();
         if (services == null) return;
         for (Entry<Class<? extends Domain>, String> entry : services.entrySet()) {
             String domain = entry.getKey().getSimpleName();
             String id = entry.getValue();
             contextService.putValue("domain/" + domain + "/defaultConnector/id", id);
         }
     }
 
     private void checkId(String id) throws ProjectAlreadyExistsException {
         List<Project> projects = persistence.query(new Project(id));
         if (!projects.isEmpty()) {
             throw new ProjectAlreadyExistsException("Project with id '" + id + "' already exists.");
         }
     }
 
     @Override
     public List<Project> getAllProjects() {
         return persistence.query(new Project(null));
     }
 
     @Override
     public Project getProject(String projectId) throws NoSuchProjectException {
         List<Project> projects = persistence.query(new Project(projectId));
         if (projects.isEmpty()) {
             throw new NoSuchProjectException("No project with id '" + projectId + "' found.");
         }
         return projects.get(0);
     }
 
     @Override
     public void updateProject(Project project) throws NoSuchProjectException {
         Project old = getProject(project.getId());
         try {
             persistence.update(old, project);
             String oldContext = contextService.getThreadLocalContext();
             contextService.setThreadLocalContext(project.getId());
             setDefaultConnectors(project);
             contextService.setThreadLocalContext(oldContext);
         } catch (PersistenceException e) {
             throw new RuntimeException("Could not update project", e);
         }
     }
 
     @Override
     public void updateCurrentContextProjectState(State state) throws NoSuchProjectException {
         String projectId = contextService.getThreadLocalContext();
         Project project = getProject(projectId);
         project.setState(state);
         updateProject(project);
     }
 
     @Override
     public Project getCurrentContextProject() throws NoSuchProjectException {
         String projectId = contextService.getThreadLocalContext();
         return getProject(projectId);
     }
 
     @Override
     public void deleteProject(String projectId) throws NoSuchProjectException {
         Project project = getProject(projectId);
         try {
             ScmStatePoller scmStatePoller = pollers.get(projectId);
             if (scmStatePoller != null) {
                 scmStatePoller.stop();
                 pollers.remove(projectId);
             }
             persistence.delete(project);
             reportDomain.removeCategory(projectId);
         } catch (PersistenceException e) {
             throw new RuntimeException("Could not delete project " + projectId, e);
         }
     }
 
     public void setPersistenceManager(PersistenceManager persistenceManager) {
         this.persistenceManager = persistenceManager;
     }
 
     public void setReportDomain(ReportDomain reportDomain) {
         this.reportDomain = reportDomain;
     }
 
     @Override
     public void setBundleContext(BundleContext bundleContext) {
         this.bundleContext = bundleContext;
     }
 
     public void setContextService(ContextCurrentService contextService) {
         this.contextService = contextService;
     }
 
     public void setWorkflowService(WorkflowService workflowService) {
         this.workflowService = workflowService;
     }
 
     public void setScmDomain(ScmDomain scmDomain) {
         this.scmDomain = scmDomain;
     }
 
     public void setTimeout(long timeout) {
         this.timeout = timeout;
     }
 
 }
