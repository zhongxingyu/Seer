 /*
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.emr.task;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.api.context.UserContext;
 import org.openmrs.module.emr.EmrContext;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class TaskServiceTest {
 
     TaskServiceImpl service;
     EmrContext context;
     UserContext userContext;
 
     @Before
     public void setUp() throws Exception {
         service = new TaskServiceImpl();
         userContext = mock(UserContext.class);
         context = new EmrContext();
         context.setUserContext(userContext);
     }
 
     @Test
     public void shouldGetAvailableTasks() {
         SimpleTaskDescriptor taskA = new SimpleTaskDescriptor();
         taskA.setId("A");
         SimpleTaskDescriptor taskB = new SimpleTaskDescriptor();
         taskB.setId("B");
 
         List<TaskDescriptor> allTasks = new ArrayList<TaskDescriptor>();
         allTasks.add(taskA);
         allTasks.add(taskB);
         service.setAllTasksInternal(allTasks);
         service.setAllTaskFactoriesInternal(new ArrayList<TaskFactory>());
 
         when(userContext.hasPrivilege(taskA.getRequiredPrivilegeName())).thenReturn(false);
         when(userContext.hasPrivilege(taskB.getRequiredPrivilegeName())).thenReturn(true);
 
        List<TaskDescriptor> availableTasks = service.getAvailableTasks(context);
         Assert.assertEquals(1, availableTasks.size());
         Assert.assertEquals(taskB, availableTasks.get(0));
     }
 
     @Test
     public void shouldGetAvailableTasksIncludingThoseFromFactories() {
         SimpleTaskDescriptor taskA = new SimpleTaskDescriptor();
         taskA.setId("defined-directly");
         final SimpleTaskDescriptor taskB = new SimpleTaskDescriptor();
         taskB.setId("from-factory-1");
         final SimpleTaskDescriptor taskC = new SimpleTaskDescriptor();
         taskC.setId("from-factory-2");
 
         TaskFactory factory = new TaskFactory() {
             @Override
             public List<TaskDescriptor> getTaskDescriptors(EmrContext emrContext) {
                 List<TaskDescriptor> ret = new ArrayList<TaskDescriptor>();
                 ret.add(taskB);
                 ret.add(taskC);
                 return ret;
             }
         };
 
         List<TaskDescriptor> allTasks = new ArrayList<TaskDescriptor>();
         allTasks.add(taskA);
 
         service.setAllTasksInternal(allTasks);
         service.setAllTaskFactoriesInternal(asList(factory));
 
         when(userContext.hasPrivilege(taskA.getRequiredPrivilegeName())).thenReturn(true);
         when(userContext.hasPrivilege(taskB.getRequiredPrivilegeName())).thenReturn(true);
         when(userContext.hasPrivilege(taskC.getRequiredPrivilegeName())).thenReturn(false);
 
        List<TaskDescriptor> availableTasks = service.getAvailableTasks(context);
         assertThat(availableTasks.size(), is(2));
         assertThat(availableTasks, containsInAnyOrder((TaskDescriptor) taskA, (TaskDescriptor) taskB));
     }
 
     @Test
     public void shouldSortTasksBasedOnPriority() {
         List<TaskDescriptor> allTasks = new ArrayList<TaskDescriptor>();
 
         SimpleTaskDescriptor taskA = new SimpleTaskDescriptor();
         taskA.setId("A");
         taskA.setPriority(3);
         allTasks.add(taskA);
 
         SimpleTaskDescriptor taskC = new SimpleTaskDescriptor();
         taskC.setId("C");
         taskC.setPriority(1);
         allTasks.add(taskC);
 
         SimpleTaskDescriptor taskB = new SimpleTaskDescriptor();
         taskB.setId("B");
         taskB.setPriority(2);
         allTasks.add(taskB);
 
         service.setAllTasksInternal(allTasks);
         service.setAllTaskFactoriesInternal(new ArrayList<TaskFactory>());
 
         when(userContext.hasPrivilege(anyString())).thenReturn(true);
 
        List<TaskDescriptor> availableTasks = service.getAvailableTasks(context);
         Assert.assertEquals(3, availableTasks.size());
         Assert.assertEquals(taskA, availableTasks.get(0));
         Assert.assertEquals(taskB, availableTasks.get(1));
         Assert.assertEquals(taskC, availableTasks.get(2));
     }
 
 }
