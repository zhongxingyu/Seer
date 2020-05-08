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
 
 package org.openengsb.opencit.core.projectmanager;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
 import org.openengsb.opencit.core.projectmanager.internal.ProjectManagerImpl;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 import org.openengsb.opencit.core.projectmanager.model.Project.State;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
 public class ProjectManagerImplTest {
 
     private ProjectManagerImpl projectManager;
     private PersistenceService persistenceMock;
     private ContextCurrentService contextMock;
 
     @Before
     public void setUp() throws Exception {
         projectManager = new ProjectManagerImpl();
         projectManager.setBundleContext(Mockito.mock(BundleContext.class));
 
         persistenceMock = Mockito.mock(PersistenceService.class);
         contextMock = Mockito.mock(ContextCurrentService.class);
 
         Mockito.when(contextMock.getCurrentContextId()).thenReturn("test");
 
         PersistenceManager persistenceManagerMock = Mockito.mock(PersistenceManager.class);
         Mockito.when(persistenceManagerMock.getPersistenceForBundle(Mockito.any(Bundle.class))).thenReturn(
             persistenceMock);
         projectManager.setPersistenceManager(persistenceManagerMock);
         projectManager.setContextService(contextMock);
         projectManager.init();
     }
 
     private void addTestData() {
         Project project = new Project("test");
         project.setState(State.OK);
         Mockito.when(persistenceMock.query(Mockito.any(Project.class))).thenReturn(
             Arrays.asList(new Project[]{ project }));
     }
 
     @Test
     public void createProject_shouldWork() throws ProjectAlreadyExistsException, PersistenceException {
         Project project = new Project("foo");
         projectManager.createProject(project);
         Mockito.verify(persistenceMock).create(project);
     }
 
     @Test(expected = ProjectAlreadyExistsException.class)
     public void createProjectTwice_shouldFail() throws ProjectAlreadyExistsException {
         addTestData();
         Project project = new Project("test");
         projectManager.createProject(project);
     }
 
     @Test
     public void getAllProjects_shouldWork() {
         addTestData();
         List<Project> allProjects = projectManager.getAllProjects();
         assertThat(allProjects.size(), is(1));
         assertThat(allProjects.get(0).getId(), is("test"));
     }
 
     @Test
     public void getProject_souldWork() throws NoSuchProjectException {
         addTestData();
         Project project = projectManager.getProject("test");
         assertThat(project.getId(), is("test"));
         assertThat(project.getState(), is(State.OK));
     }
 
     @Test(expected = NoSuchProjectException.class)
     public void getProject_souldFail() throws NoSuchProjectException {
         projectManager.getProject("test");
     }
 
     @Test
     public void updateProject_shouldWork() throws NoSuchProjectException, PersistenceException {
         addTestData();
         Project project = new Project("test");
         project.setState(State.IN_PROGRESS);
         projectManager.updateProject(project);
         Mockito.verify(persistenceMock).update(Mockito.refEq(new Project("test"), "state"), Mockito.refEq(project));
     }
 
     @Test(expected = NoSuchProjectException.class)
     public void updateProject_souldFail() throws NoSuchProjectException {
         projectManager.updateProject(new Project("test"));
     }
 
     @Test
     public void updateCurrentContextProjectState_shouldWork() throws NoSuchProjectException {
         addTestData();
         projectManager.updateCurrentContextProjectState(State.FAILURE);
         Project project = projectManager.getProject("test");
         assertThat(project.getState(), is(State.FAILURE));
     }
 
     @Test(expected = NoSuchProjectException.class)
     public void updateCurrentContextProjectState_shouldFail() throws NoSuchProjectException {
         projectManager.updateCurrentContextProjectState(State.FAILURE);
     }
 
 }
