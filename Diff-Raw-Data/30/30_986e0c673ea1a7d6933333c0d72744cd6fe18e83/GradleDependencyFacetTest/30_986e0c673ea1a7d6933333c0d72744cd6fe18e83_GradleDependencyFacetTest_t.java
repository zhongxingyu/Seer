 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects.facets;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.forge.addon.dependencies.Coordinate;
 import org.jboss.forge.addon.dependencies.Dependency;
 import org.jboss.forge.addon.dependencies.DependencyQuery;
 import org.jboss.forge.addon.dependencies.DependencyRepository;
 import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
 import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
 import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
 import org.jboss.forge.addon.gradle.projects.GradleTestProjectProvider;
 import org.jboss.forge.addon.gradle.projects.ProjectAssert;
 import org.jboss.forge.addon.projects.Project;
 import org.jboss.forge.addon.projects.facets.DependencyFacet;
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
 public class GradleDependencyFacetTest
 {
    @Deployment
    @Dependencies({
             @AddonDependency(name = "org.jboss.forge.addon:resources", version = "2.0.0-SNAPSHOT"),
             @AddonDependency(name = "org.jboss.forge.addon:projects", version = "2.0.0-SNAPSHOT"),
             @AddonDependency(name = "org.jboss.forge.addon:parser-java", version = "2.0.0-SNAPSHOT"),
             @AddonDependency(name = "org.jboss.forge.addon:gradle", version = "2.0.0-SNAPSHOT")
    })
    public static ForgeArchive getDeployment()
    {
       return GradleTestProjectProvider.getDeployment();
    }
 
    @Inject
    private GradleTestProjectProvider projectProvider;
    private Project project;
    private DependencyFacet facet;
 
    @Before
    public void setUp()
    {
       project = projectProvider.create();
       facet = project.getFacet(DependencyFacet.class);
    }
 
    @After
    public void cleanUp()
    {
       projectProvider.clean();
    }
 
    @Test
    public void testAddDirectDependency()
    {
       facet.addDirectDependency(
                DependencyBuilder
                         .create()
                         .setArtifactId("mydep")
                         .setGroupId("mygroup")
                         .setVersion("myversion")
                         .setScopeType("runtime"));
 
       Project theSameProject = projectProvider.findProject();
       DependencyFacet theNewFacet = theSameProject.getFacet(DependencyFacet.class);
 
       ProjectAssert.assertContainsDependency(theNewFacet.getDependencies(),
                "runtime", "mygroup", "mydep", "myversion");
    }
 
    @Test
    public void testAddManagedDependency()
    {
       facet.addManagedDependency(
                DependencyBuilder
                         .create()
                         .setArtifactId("mydep")
                         .setGroupId("mygroup")
                         .setVersion("myversion")
                         .setScopeType("runtime"));
 
       Project theSameProject = projectProvider.findProject();
       DependencyFacet theNewFacet = theSameProject.getFacet(DependencyFacet.class);
 
       ProjectAssert.assertContainsDependency(theNewFacet.getManagedDependencies(),
                "runtime", "mygroup", "mydep", "myversion");
    }
 
    @Test
    public void testAddDirectManagedDependency()
    {
       facet.addDirectManagedDependency(
                DependencyBuilder
                         .create()
                         .setArtifactId("mydep")
                         .setGroupId("mygroup")
                         .setVersion("myversion")
                         .setScopeType("runtime"));
 
       Project theSameProject = projectProvider.findProject();
       DependencyFacet theNewFacet = theSameProject.getFacet(DependencyFacet.class);
 
       ProjectAssert.assertContainsDependency(theNewFacet.getManagedDependencies(),
                "runtime", "mygroup", "mydep", "myversion");
    }
 
    @Test
    public void testAddRepository()
    {
       facet.addRepository("RepoName", "http://repo.com/");
 
       Project sameProject = projectProvider.findProject();
       DependencyFacet sameFacet = sameProject.getFacet(DependencyFacet.class);
 
       ProjectAssert.assertContainsRepository(sameFacet.getRepositories(), "http://repo.com/");
    }
 
    @Test
    public void testGetDependencies()
    {
       List<Dependency> deps = facet.getDependencies();
 
       ProjectAssert.assertContainsDependency(deps, "compile", "org.slf4j", "slf4j-api", "1.7.5");
       ProjectAssert.assertContainsDependency(deps, "compile", "org.slf4j", "slf4j-simple", "1.7.5");
       ProjectAssert.assertContainsDependency(deps, "test", "junit", "junit", "4.11");
       ProjectAssert.assertContainsDependency(deps, "runtime", "com.google.code.guice", "guice", "1.0");
       
       ProjectAssert.assertNotContainsDependency(deps, "runtime", "org.jboss.netty", "netty", "3.2.9.Final");
       ProjectAssert.assertContainsDirectDependency(deps, "org.mockito", "mockito-all");
    }
 
    @Test
    public void testGetDependenciesInScopes()
    {
       List<Dependency> deps = facet.getDependenciesInScopes("test", "runtime");
 
       ProjectAssert.assertNotContainsDependency(deps, "compile", "org.slf4j", "slf4j-api", "1.7.5");
       ProjectAssert.assertNotContainsDependency(deps, "compile", "org.slf4j", "slf4j-simple", "1.7.5");
       ProjectAssert.assertContainsDependency(deps, "test", "junit", "junit", "4.11");
       ProjectAssert.assertContainsDependency(deps, "runtime", "com.google.code.guice", "guice", "1.0");
    }
 
    @Test
    public void testGetDirectDependency()
    {
       Dependency dep = facet.getDirectDependency(DependencyBuilder.create("junit:junit:4.11"));
 
       assertNotNull(dep);
       assertEquals("junit", dep.getCoordinate().getGroupId());
       assertEquals("junit", dep.getCoordinate().getArtifactId());
       assertEquals("4.11", dep.getCoordinate().getVersion());
      assertEquals("test", dep.getScopeType());
    }
 
    @Test
    public void testGetDirectDependencyOnManaged()
    {
      Dependency dep = facet.getDirectDependency(DependencyBuilder.create("org.apache.commons:commons-exec:1.1"));
 
       assertNull(dep);
    }
 
    @Test
    public void testGetEffectiveDependenciesNonTransitive()
    {
       List<Dependency> deps = facet.getEffectiveDependencies();
 
       ProjectAssert.assertContainsDependency(deps, "compile", "org.slf4j", "slf4j-api", "1.7.5");
       ProjectAssert.assertContainsDependency(deps, "compile", "org.slf4j", "slf4j-simple", "1.7.5");
       ProjectAssert.assertContainsDependency(deps, "test", "junit", "junit", "4.11");
       ProjectAssert.assertContainsDependency(deps, "runtime", "com.google.code.guice", "guice", "1.0");
 
       // Non direct dependencies
       ProjectAssert.assertContainsDependency(deps, "runtime", "org.jboss.netty", "netty", "3.2.9.Final");
       ProjectAssert.assertContainsDependency(deps, "test", "org.mockito", "mockito-all", "1.9.5");
    }
    
    @Test
    public void testGetEffectiveDependenciesTransitive()
    {
       List<Dependency> deps = facet.getEffectiveDependencies();
 
       ProjectAssert.assertContainsDependency(deps, "test", "org.hamcrest", "hamcrest-core", "1.3");
    }
 
    @Test
   public void testGetEffectiveDependenciesInScopesNonTransitive()
    {
       List<Dependency> deps = facet.getEffectiveDependenciesInScopes("test", "runtime");
 
       ProjectAssert.assertNotContainsDependency(deps, "compile", "org.slf4j", "slf4j-api", "1.7.5");
       ProjectAssert.assertNotContainsDependency(deps, "compile", "org.slf4j", "slf4j-simple", "1.7.5");
       ProjectAssert.assertContainsDependency(deps, "test", "junit", "junit", "4.11");
       ProjectAssert.assertContainsDependency(deps, "runtime", "com.google.code.guice", "guice", "1.0");
      ProjectAssert.assertContainsDependency(deps, "runtime", "org.jboss.netty", "netty", "3.2.9.Final");
      ProjectAssert.assertContainsDependency(deps, "test", "org.mockito", "mockito-all", "1.9.5");
    }
 
    @Test
    public void testGetEffectiveDependency()
    {
       Dependency dep = facet
                .getEffectiveDependency(DependencyBuilder.create("org.jboss.netty:netty:3.2.9.Final"));
 
       assertNotNull(dep);
       assertEquals("runtime", dep.getScopeType());
    }
 
    @Test
    public void testGetEffectiveManagedDependency()
    {
       Dependency dep = facet.getEffectiveManagedDependency(DependencyBuilder.create("org.mockito:mockito-all:1.9.5"));
 
       assertNotNull(dep);
      assertEquals("test", dep.getScopeType());
    }
 
    @Test
    public void testGetManagedDependencies()
    {
       List<Dependency> deps = facet.getManagedDependencies();
 
      ProjectAssert.assertContainsDependency(deps, "compile", "org.apache.commons", "commons-exec", "1.1");
       ProjectAssert.assertContainsDependency(deps, "runtime", "org.codehaus.groovy", "groovy", "2.1.6");
       ProjectAssert.assertNotContainsDependency(deps, "testCompile", "org.mockito", "mockito-all", "1.9.5");
    }
 
    @Test
    public void testGetDirectManagedDependency()
    {
       Dependency dep =
                facet.getDirectManagedDependency(DependencyBuilder.create("org.codehaus.groovy:groovy:2.1.6"));
 
       assertEquals("runtime", dep.getScopeType());
    }
 
    @Test
    public void testGetRepositories()
    {
       List<DependencyRepository> repos = facet.getRepositories();
 
       ProjectAssert.assertContainsRepository(repos, "http://maven-repo.com/");
    }
 
    @Test
    public void testHasDirectDependency()
    {
       assertTrue(facet.hasDirectDependency(DependencyBuilder.create("org.slf4j:slf4j-api:1.7.5")));
    }
 
    @Test
    public void testHasDirectDependencyNot()
    {
       assertFalse(facet.hasDirectDependency(DependencyBuilder.create("org.non:existing:30")));
    }
 
    @Test
    public void testHasEffectiveDependency()
    {
       assertTrue(facet.hasEffectiveDependency(
                DependencyBuilder.create("org.mockito:mockito-all:1.9.5")));
    }
 
    @Test
    public void tastHasEffectiveDependencyNot()
    {
       assertFalse(facet.hasEffectiveDependency(
                DependencyBuilder.create("org.non:existing:30")));
    }
 
    @Test
    public void testHasEffectiveManagedDependency()
    {
       assertTrue(facet.hasEffectiveManagedDependency(
               DependencyBuilder.create("org.mockito:mockito-all:1.9.5")));
    }
 
    @Test
    public void testHasEffectiveManagedDependencyNot()
    {
       assertFalse(facet.hasEffectiveManagedDependency(
                DependencyBuilder.create("org.non:existing:30")));
    }
 
    @Test
    public void testHasDirectManagedDependency()
    {
       assertTrue(facet.hasDirectManagedDependency(
                DependencyBuilder.create("org.codehaus.groovy:groovy:2.1.6")));
    }
 
    @Test
    public void testHasDirectManagedDependencyNot()
    {
       assertFalse(facet.hasDirectManagedDependency(
                DependencyBuilder.create("org.non:existing:30")));
    }
 
    @Test
    public void testHasRepository()
    {
       assertTrue(facet.hasRepository("http://maven-repo.com/"));
    }
 
    @Test
    public void testHasRepositoryNot()
    {
       assertFalse(facet.hasRepository("gopher://nope.com/"));
    }
 
    @Test
    public void testRemoveDependency()
    {
       Dependency dep = DependencyBuilder.create("org.slf4j:slf4j-api:1.7.5").setScopeType("compile");
       assertTrue(facet.hasDirectDependency(dep));
 
       facet.removeDependency(dep);
 
       Project sameProject = projectProvider.findProject();
       DependencyFacet sameFacet = sameProject.getFacet(DependencyFacet.class);
       List<Dependency> deps = sameFacet.getDependencies();
 
       ProjectAssert.assertNotContainsDependency(deps, "compile", "org.slf4j", "slf4j-api", "1.7.5");
    }
 
    @Test
    public void testRemoveManagedDependency()
    {
      Dependency dep = DependencyBuilder.create("org.apache.commons:commons-exec:1.1");
       assertTrue(facet.hasDirectManagedDependency(dep));
 
       facet.removeManagedDependency(dep);
 
       Project sameProject = projectProvider.findProject();
       DependencyFacet sameFacet = sameProject.getFacet(DependencyFacet.class);
       List<Dependency> managedDeps = sameFacet.getManagedDependencies();
 
      ProjectAssert.assertNotContainsDependency(managedDeps, "compile", 
               "org.apache.commons", "commons-exec", "1.1");
    }
 
    @Test
    public void testRemoveRepository()
    {
       facet.removeRepository("http://maven-repo.com/");
 
       Project sameProject = projectProvider.findProject();
       DependencyFacet sameFacet = sameProject.getFacet(DependencyFacet.class);
       List<DependencyRepository> repos = sameFacet.getRepositories();
 
       ProjectAssert.assertNotContainsRepository(repos, "http://maven-repo.com");
    }
 
    @Test
    public void testResolveAvailableVersionsForDependency()
    {
       Dependency dep = DependencyBuilder.create("junit:junit:[4.9,)");
       List<Coordinate> coords = facet.resolveAvailableVersions(dep);
       testResolveAvailableVersions(coords);
    }
 
    @Test
    public void testResolveAvailableVersionsForGavs()
    {
       List<Coordinate> coords = facet.resolveAvailableVersions("junit:junit:[4.9,)");
       testResolveAvailableVersions(coords);
    }
 
    @Test
    public void testResolveAvailableVersionsForQuery()
    {
       DependencyQuery query = DependencyQueryBuilder.create("junit:junit:[4.9,)");
       List<Coordinate> coords = facet.resolveAvailableVersions(query);
       testResolveAvailableVersions(coords);
    }
 
    private void testResolveAvailableVersions(List<Coordinate> coords)
    {
       ProjectAssert.assertContainsCoordinate(coords,
                CoordinateBuilder.create().setArtifactId("junit").setGroupId("junit").setVersion("4.9"));
       ProjectAssert.assertContainsCoordinate(coords,
                CoordinateBuilder.create().setArtifactId("junit").setGroupId("junit").setVersion("4.11"));
       ProjectAssert.assertNotContainsCoordinate(coords,
                CoordinateBuilder.create().setArtifactId("junit").setGroupId("junit").setVersion("4.8"));
    }
 
    @Test
    public void testResolveProperties()
    {
       Dependency inputDep = DependencyBuilder.create("org.somedep:artifact:${ext.someVersion}");
 
       Dependency outputDep = facet.resolveProperties(inputDep);
 
       assertNotNull(outputDep);
       assertEquals("org.somedep", outputDep.getCoordinate().getGroupId());
       assertEquals("artifact", outputDep.getCoordinate().getArtifactId());
       assertEquals(".45", outputDep.getCoordinate().getVersion());
    }
 }
