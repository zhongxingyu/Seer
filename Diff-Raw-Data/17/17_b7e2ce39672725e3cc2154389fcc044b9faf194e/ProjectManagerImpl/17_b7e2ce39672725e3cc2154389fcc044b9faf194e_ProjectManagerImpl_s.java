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
 
 import java.util.List;
 
 import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.persistence.PersistenceManager;
import org.openengsb.core.persistence.PersistenceService;
 import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
 import org.openengsb.opencit.core.projectmanager.ProjectAlreadyExistsException;
 import org.openengsb.opencit.core.projectmanager.ProjectManager;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 import org.openengsb.opencit.core.projectmanager.model.Project.State;
 import org.osgi.framework.BundleContext;
 import org.springframework.osgi.context.BundleContextAware;
 
 public class ProjectManagerImpl implements ProjectManager, BundleContextAware {
 
     private PersistenceManager persistenceManager;
 
     private PersistenceService persistence;
 
     private ContextCurrentService contextService;
 
     private BundleContext bundleContext;
 
     public void init() {
         this.persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
     }
 
     @Override
     public void createProject(Project project) throws ProjectAlreadyExistsException {
         checkId(project.getId());
         try {
             persistence.create(project);
         } catch (PersistenceException e) {
             throw new RuntimeException(e);
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
         } catch (PersistenceException e) {
             throw new RuntimeException("Could not update project", e);
         }
     }
 
     @Override
     public void updateCurrentContextProjectState(State state) throws NoSuchProjectException {
         String projectId = contextService.getCurrentContextId();
         Project project = getProject(projectId);
         project.setState(state);
         updateProject(project);
     }
 
     public void setPersistenceManager(PersistenceManager persistenceManager) {
         this.persistenceManager = persistenceManager;
     }
 
     @Override
     public void setBundleContext(BundleContext bundleContext) {
         this.bundleContext = bundleContext;
     }
 
     public void setContextService(ContextCurrentService contextService) {
         this.contextService = contextService;
     }
 
 }
