 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.gradle.jarjar.com.google.common.collect.Lists;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.forge.addon.gradle.projects.model.GradleModelBuilder;
 import org.jboss.forge.addon.gradle.projects.model.GradleTask;
 import org.jboss.forge.addon.gradle.projects.model.GradleTaskBuilder;
 import org.jboss.forge.addon.projects.Project;
 import org.jboss.forge.addon.resource.FileResource;
 import org.jboss.forge.addon.resource.ResourceFactory;
 import org.jboss.forge.arquillian.AddonDependency;
 import org.jboss.forge.arquillian.Dependencies;
 import org.jboss.forge.arquillian.archive.ForgeArchive;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * @author Adam Wyłuda
  */
 @RunWith(Arquillian.class)
 public class GradleFacetTest
 {
    @Deployment
    @Dependencies({
             @AddonDependency(name = "org.jboss.forge.addon:resources"),
             @AddonDependency(name = "org.jboss.forge.addon:projects"),
             @AddonDependency(name = "org.jboss.forge.addon:parser-java"),
             @AddonDependency(name = "org.jboss.forge.addon:gradle"),
             @AddonDependency(name = "org.jboss.forge.addon:maven")
    })
    public static ForgeArchive getDeployment()
    {
       return GradleTestProjectProvider.getDeployment(
                GradleTestProjectProvider.SIMPLE_RESOURCES_PATH,
                GradleTestProjectProvider.SIMPLE_RESOURCES);
    }
 
    @Inject
    private GradleTestProjectProvider projectProvider;
    @Inject
    private ResourceFactory resourceFactory;
 
    private Project project;
    private GradleFacet facet;
 
    @Before
    public void setUp()
    {
       project = projectProvider.create("",
                GradleTestProjectProvider.SIMPLE_RESOURCES_PATH,
                GradleTestProjectProvider.SIMPLE_RESOURCES);
       facet = project.getFacet(GradleFacet.class);
    }
 
    @After
    public void cleanUp()
    {
       projectProvider.clean();
    }
 
    @Test
    public void testReadTaskList()
    {
       List<GradleTask> tasks = facet.getModel().getEffectiveTasks();
 
       List<String> taskNames = Lists.newArrayListWithCapacity(tasks.size());
       for (GradleTask task : tasks)
       {
          taskNames.add(task.getName());
       }
 
       assertTrue(taskNames.contains("abc"));
       assertTrue(taskNames.contains("ghi"));
       assertTrue(taskNames.contains("taskNum1"));
       assertTrue(taskNames.contains("taskNum2"));
       assertTrue(taskNames.contains("taskNum3"));
       assertTrue(taskNames.contains("taskNum4"));
       assertTrue(taskNames.contains("taskNum5"));
    }
 
    @Test
    public void testCreateTask()
    {
       GradleModelBuilder model = GradleModelBuilder.create(facet.getModel());
       model.addTask(
                GradleTaskBuilder
                         .create()
                         .setName("myTask")
                         .setDependsOn(GradleTaskBuilder.create().setName("abc"))
                         .setType("Copy")
                         .setCode("println 'myTask!'"));
       facet.setModel(model);
 
       Project theSameProject = projectProvider.findProject();
       GradleFacet newGradleFacet = theSameProject.getFacet(GradleFacet.class);
 
       boolean containsMyTask = false;
       for (GradleTask task : newGradleFacet.getModel().getEffectiveTasks())
       {
          if (task.getName().equals("myTask"))
          {
             containsMyTask = true;
             break;
          }
       }
       assertTrue(containsMyTask);
    }
 
    @Test
    public void testGetModelNotNull()
    {
       assertNotNull(facet.getModel());
    }
 
    @Test
    public void testExecuteTask() throws IOException
    {
       assertTrue(facet.executeTask("someOutput"));
       String output = ((FileResource<?>) resourceFactory.create(new File(project.getRootDirectory()
                .getFullyQualifiedName(), "output.txt"))).getContents();
       assertEquals("XYZ", output);
    }
 
    @Test
    public void testExecuteTaskWithProfile() throws IOException
    {
       assertTrue(facet.executeTask("testProfileOutput", "test"));
       String output = ((FileResource<?>) resourceFactory.create(new File(project.getRootDirectory()
                .getFullyQualifiedName(), "testOutput.txt"))).getContents();
       assertEquals("TEST", output);
    }
 }
