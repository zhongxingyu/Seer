 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects;
 
 import javax.inject.Inject;
 
 import org.jboss.forge.addon.facets.FacetFactory;
 import org.jboss.forge.addon.gradle.projects.facets.GradleJavaSourceFacet;
 import org.jboss.forge.addon.gradle.projects.facets.GradleResourceFacet;
 import org.jboss.forge.addon.projects.Project;
 import org.jboss.forge.addon.projects.ProjectLocator;
 import org.jboss.forge.addon.resource.DirectoryResource;
 
 /**
  * @author Adam Wyłuda
  */
 public class GradleProjectLocator implements ProjectLocator
 {
    @Inject
    private FacetFactory facetFactory;
 
    @Override
    public Project createProject(DirectoryResource targetDir)
    {
       Project project = new GradleProject(targetDir);
 
       try
       {
          facetFactory.install(project, GradleFacet.class);
//         facetFactory.install(project, GradleMetadataFacet.class);
//         facetFactory.install(project, GradlePackagingFacet.class);
//         facetFactory.install(project, GradleDependencyFacet.class);
          facetFactory.install(project, GradleResourceFacet.class);
          facetFactory.install(project, GradleJavaSourceFacet.class);
       }
       catch (RuntimeException e)
       {
          throw new IllegalStateException("Could not install Gradle into Project located at ["
                   + targetDir.getFullyQualifiedName() + "]");
       }
 
       return project;
    }
 
    @Override
    public boolean containsProject(DirectoryResource resource)
    {
       return resource.getChild("build.gradle").exists();
    }
 }
