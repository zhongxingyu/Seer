 package com.atlassian.maven.plugins.refapp;
 
 import java.io.File;
 
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * Run the integration tests against the refapp
  *
  * @requiresDependencyResolution integration-test
  * @goal integration-test
  *
  */
 public class IntegrationTestMojo
 extends AbstractRefappMojo {
 
     /**
      * Pattern for to use to find integration tests
      *
      * @parameter expression="${functionalTestPattern}"
      */
     private final String functionalTestPattern = "it/**";
 
     /**
      * The directory containing generated test classes of the project being tested.
      *
      * @parameter expression="${project.build.testOutputDirectory}"
      * @required
      */
     private File testClassesDirectory;
 
     /**
      * Whether the reference application will not be started or not
      *
      * @parameter expression="${noRefapp}"
      */
     private final boolean noRefapp = false;
 
 
     public void execute()
     throws MojoExecutionException
     {
         if (!new File(testClassesDirectory, "it").exists())
         {
             getLog().info("No integration tests found");
             return;
         }
         final MavenGoals goals = new MavenGoals(project, session, pluginManager, getLog());
 
         final String pluginJar = targetDirectory.getAbsolutePath() + "/" + finalName + ".jar";
 
         int actualHttpPort = 0;
         if (!noRefapp)
         {
             // Copy the refapp war to target
             final File refappWar = goals.copyRefappWar(targetDirectory, determineVersion());
 
             final File combinedRefappWar = addArtifacts(goals, refappWar);
 
             actualHttpPort = goals.startRefapp(combinedRefappWar, containerId, server, httpPort, contextPath, jvmArgs);
 
         }
         goals.runTests(containerId, functionalTestPattern, actualHttpPort, contextPath, pluginJar);
 
         if (!noRefapp)
         {
             goals.stopRefapp(containerId);
         }
 
     }
 }
