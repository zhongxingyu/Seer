 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects;
 
 import javax.inject.Inject;
 
 import org.jboss.forge.addon.projects.Project;
 import org.jboss.forge.addon.projects.ProjectFactory;
 import org.jboss.forge.addon.resource.DirectoryResource;
 import org.jboss.forge.addon.resource.FileResource;
 import org.jboss.forge.addon.resource.ResourceFactory;
 import org.jboss.forge.arquillian.archive.ForgeArchive;
 import org.jboss.forge.furnace.Furnace;
 import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
 import org.jboss.forge.furnace.services.Exported;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 
 /**
  * @author Adam Wyłuda
  */
 @Exported
 public class GradleTestProjectProvider
 {
    static final String[] RESOURCES = new String[] {
             "build.gradle",
             "test-profile.gradle",
             "settings.gradle",
             "src/main/interfaces/org/testproject/Service.java",
             "src/main/images/forge.txt",
             "src/test/mocks/org/testproject/TestMainClass.java",
             "src/test/templates/pom.xml"
    };
 
    public static ForgeArchive getDeployment()
    {
       ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                .addBeansXML()
                .addClass(GradleTestProjectProvider.class)
                .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi", "2.0.0-SNAPSHOT"),
                         AddonDependencyEntry.create("org.jboss.forge.addon:resources", "2.0.0-SNAPSHOT"),
                         AddonDependencyEntry.create("org.jboss.forge.addon:gradle", "2.0.0-SNAPSHOT"),
                         AddonDependencyEntry.create("org.jboss.forge.addon:projects", "2.0.0-SNAPSHOT")
                );
       for (String resource : RESOURCES)
       {
          archive = archive.addAsResource(resource);
       }
       return archive;
    }
 
    @Inject
    private Furnace furnace;
    @Inject
    private ProjectFactory projectFactory;
    @Inject
    private ResourceFactory resourceFactory;
 
    private DirectoryResource projectDir;
 
    public Project create()
    {
       DirectoryResource addonDir = resourceFactory.create(furnace.getRepositories().get(0).getRootDirectory()).reify(
                DirectoryResource.class);
       projectDir = addonDir.createTempResource();
 
       initFiles(RESOURCES);
 
       return findProject();
    }
 
    public Project findProject()
    {
       return projectFactory.findProject(projectDir);
    }
 
    public void clean()
    {
       projectDir.delete(true);
    }
 
    private void initFiles(String... files)
    {
       for (String file : files)
       {
          FileResource<?> res = projectDir.getChild(file).reify(FileResource.class);
          res.createNewFile();
          res.setContents(getClass().getResourceAsStream("/" + file));
       }
    }
 }
