 /*
  * Copyright 2012 htfv (Aliaksei Lahachou)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.htfv.maven.plugins.testing;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
import java.util.Properties;
 
 import org.apache.commons.lang3.ObjectUtils;
 import org.apache.maven.Maven;
 import org.apache.maven.execution.DefaultMavenExecutionRequest;
 import org.apache.maven.execution.MavenExecutionRequest;
 import org.apache.maven.execution.MavenExecutionResult;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Exclusion;
 import org.apache.maven.model.Plugin;
 import org.apache.maven.model.PluginExecution;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.PlexusTestCase;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 @RunWith(JUnit4.class)
 public abstract class MavenTest
 {
     private static class PlexusContainer extends PlexusTestCase
     {
         @Override
         protected <T> T lookup(final Class<T> componentClass) throws Exception
         {
             return super.lookup(componentClass);
         }
 
         public void shutdown() throws Exception
         {
             tearDown();
         }
     }
 
     private static final List<String> DEFAULT_GOALS = Arrays.asList("validate");
 
     private static PlexusContainer plexusContainer;
 
     /**
      * Loaded test project.
      */
     private static MavenProject testProject;
 
     @BeforeClass
     public static void initializePlexusContainer()
     {
         plexusContainer = new PlexusContainer();
     }
 
     @AfterClass
     public static void shutdownPlexusContainer() throws Exception
     {
         if (plexusContainer != null)
         {
             plexusContainer.shutdown();
             plexusContainer = null;
         }
     }
 
     @AfterClass
     public static void unloadTestProject()
     {
         testProject = null;
     }
 
     /**
      * Reference to the TCCL before the test method was invoked.
      */
     private ClassLoader oldClassLoader;
 
     /**
      * Returns a list of goals to be executed on the loaded Maven project. Test
      * classes may override this method to customize the list of goals. Default
      * is 'validate'.
      *
      * @return list of goals to be executed.
      */
     protected List<String> getGoals()
     {
         return DEFAULT_GOALS;
     }
 
     protected abstract String getPOM();
 
     protected List<String> getProfiles()
     {
         return Collections.emptyList();
     }
 
     protected MavenProject getTestProject() throws Exception
     {
         if (testProject != null)
         {
             return testProject;
         }
 
         //
         // Create Maven execution request and set path to the test project
         // POM.
         //
 
        Properties userProperties = new Properties();

        userProperties.put("testProjectVersion", ProjectProperties.getInstance().getVersion());

         MavenExecutionRequest request = new DefaultMavenExecutionRequest();
 
        request.setActiveProfiles(getProfiles());
         request.setGoals(getGoals());
         request.setPom(new File(ProjectProperties.getInstance().getBasedir(), getPOM()));
        request.setUserProperties(userProperties);
         request.setWorkspaceReader(new ProjectWorkspaceReader());
 
         //
         // Execute Maven request.
         //
 
         Maven                maven  = plexusContainer.lookup(Maven.class);
         MavenExecutionResult result = maven.execute(request);
 
         //
         // Re-throw the first exception.
         //
 
         for (Throwable e : result.getExceptions())
         {
             throw (Exception) e;
         }
 
         return testProject = result.getProject();
     }
 
     /**
      * Finds the fist {@link Dependency} in the test project which matches the
      * given {@code groupId, artifactId, classifier,} and {@code type}.
      *
      * @param groupId
      *            {@code groupId} of the dependency to find.
      * @param artifactId
      *            {@code artifactId} of the dependency to find.
      * @param classifier
      *            {@code classifier} of the dependency to find.
      * @param type
      *            {@code type} of the dependency to find.
      *
      * @return the fist dependency in the test project which matches the given
      *         parameters or {@code null} if none could be found.
      *
      * @throws Exception
      *             if test project could not be loaded.
      */
     protected Dependency getTestProjectDependency(final String groupId, final String artifactId,
             final String classifier, final String type) throws Exception
     {
         MavenProject testProject = getTestProject();
 
         if (testProject == null)
         {
             return null;
         }
 
         for (Dependency dependency : getTestProject().getDependencies())
         {
             if (ObjectUtils.equals(dependency.getGroupId(), groupId)
                     && ObjectUtils.equals(dependency.getArtifactId(), artifactId)
                     && ObjectUtils.equals(dependency.getType(), type)
                     && ObjectUtils.equals(dependency.getClassifier(), classifier))
             {
                 return dependency;
             }
         }
 
         return null;
     }
 
     protected Exclusion getTestProjectDependencyExclusion(final String dependencyGroupId,
             final String dependencyArtifactId, final String dependencyClassifier,
             final String dependencyType, final String exclusionGroupId,
             final String exclusionArtifactId) throws Exception
     {
         Dependency dependency = getTestProjectDependency(
                 dependencyGroupId, dependencyArtifactId, dependencyClassifier, dependencyType);
 
         if (dependency == null)
         {
             return null;
         }
 
         for (Exclusion exclusion : dependency.getExclusions())
         {
             if (ObjectUtils.equals(exclusion.getGroupId(), exclusionGroupId)
                     && ObjectUtils.equals(exclusion.getArtifactId(), exclusionArtifactId))
             {
                 return exclusion;
             }
         }
 
         return null;
     }
 
     /**
      * Finds the fist {@link Plugin} in the test project which matches the given
      * {@code groupId} and {@code artifactId}.
      *
      * @param groupId
      *            {@code groupId} of the plugin to find.
      * @param artifactId
      *            {@code artifactId} of the plugin to find.
      *
      * @return the fist plugin in the test project which matches the given
      *         parameters or {@code null} if none could be found.
      *
      * @throws Exception
      *             if test project could not be loaded.
      */
     protected Plugin getTestProjectPlugin(final String groupId, final String artifactId)
             throws Exception
     {
         MavenProject testProject = getTestProject();
 
         if (testProject == null)
         {
             return null;
         }
 
         for (Plugin plugin : testProject.getBuild().getPlugins())
         {
             if (ObjectUtils.equals(plugin.getGroupId(), groupId)
                     && ObjectUtils.equals(plugin.getArtifactId(), artifactId))
             {
                 return plugin;
             }
         }
 
         return null;
     }
 
     protected PluginExecution getTestProjectPluginExecution(final String groupId,
             final String artifactId, final String executionId) throws Exception
     {
         Plugin plugin = getTestProjectPlugin(groupId, artifactId);
 
         if (plugin == null)
         {
             return null;
         }
 
         for (PluginExecution execution : plugin.getExecutions())
         {
             if (ObjectUtils.equals(execution.getId(), executionId))
             {
                 return execution;
             }
         }
 
         return null;
     }
 
     protected String getTestProjectProperty(final String key) throws Exception
     {
         return getTestProject().getProperties().getProperty(key);
     }
 
     @After
     public void restoreOldClassLoader()
     {
         Thread.currentThread().setContextClassLoader(oldClassLoader);
     }
 
     @Before
     public void setTestClassLoader()
     {
         Thread      currentThread = Thread.currentThread();
         ClassLoader classLoader   = oldClassLoader = currentThread.getContextClassLoader();
 
         if (classLoader == null)
         {
             classLoader = this.getClass().getClassLoader();
         }
 
         currentThread.setContextClassLoader(new MavenTestClassLoader(classLoader));
     }
 }
