 package com.atlassian.maven.plugins.amps;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import com.atlassian.maven.plugins.amps.util.GoogleAmpsTracker;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.execution.ReactorManager;
 import org.apache.maven.model.Plugin;
 import org.apache.maven.model.PluginManagement;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectBuilder;
 import org.apache.maven.project.ProjectBuilder;
 import org.apache.maven.project.ProjectBuilderConfiguration;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.ProjectBuildingRequest;
 import org.apache.maven.project.ProjectBuildingResult;
 import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 import org.jfrog.maven.annomojo.annotations.MojoGoal;
 import org.jfrog.maven.annomojo.annotations.MojoParameter;
 import org.jfrog.maven.annomojo.annotations.MojoRequiresProject;
 
 import static java.util.Collections.singletonList;
 
 /**
  * Run the webapp without a plugin project
  */
 @MojoGoal ("run-standalone")
 @MojoRequiresProject (false)
 public class RunStandaloneMojo extends AbstractProductHandlerMojo
 {
     private final String
         GROUP_ID = "com.atlassian.amps",
         ARTIFACT_ID = "standalone";
 
     @MojoParameter (expression = "${component.org.apache.maven.project.MavenProjectBuilder}", required = true, readonly = true)
     private MavenProjectBuilder projectBuilder;
 
     private Artifact getStandaloneArtifact()
     {
         final String version = getPluginInformation().getVersion();
         return artifactFactory.createProjectArtifact(GROUP_ID, ARTIFACT_ID, version);
     }
 
     protected void doExecute() throws MojoExecutionException, MojoFailureException
     {
         getGoogleTracker().track(GoogleAmpsTracker.RUN_STANDALONE);
 
         try
         {
             MavenGoals goals;
             Xpp3Dom configuration;
 
             try
             {
                 /* Maven 3 */
                 Object o = getMavenContext().getSession().lookup("org.apache.maven.project.ProjectBuilder");
                 goals = createMavenGoals((ProjectBuilder) o);
 
                 /* When we run with Maven 3 the configuration from the pom isn't automatically picked up
                  * by the mojo executor. Grab it manually from pluginManagement.
                  */
                 PluginManagement mgmt = goals.getContextProject().getBuild().getPluginManagement();
                 Plugin plugin = (Plugin) mgmt.getPluginsAsMap().get("com.atlassian.maven.plugins:maven-amps-plugin");
 
                 configuration = (Xpp3Dom) plugin.getConfiguration();
             }
             catch (ComponentLookupException e)
             {
                 /* Maven 2 */
                 goals = createMavenGoals(projectBuilder);
                 configuration = new Xpp3Dom("configuration");
             }
 
             goals.executeAmpsRecursively(getPluginInformation().getVersion(), "run", configuration);
         }
         catch (Exception e)
         {
             throw new MojoExecutionException(e.getMessage(), e);
         }
     }
 
     protected MavenGoals createMavenGoals(MavenProjectBuilder projectBuilder) throws MojoExecutionException, MojoFailureException
     {
         final String version = getPluginInformation().getVersion();
         final Artifact artifact = artifactFactory.createProjectArtifact(GROUP_ID, ARTIFACT_ID, version);
         try
         {
             // overall goal here is to create a new MavenContext / MavenGoals for the standalone project
             final MavenContext oldContext = getMavenContext();
 
             // construct new project for new context
             final MavenProject
                 oldProject = oldContext.getProject(),
                 newProject = projectBuilder.buildFromRepository(artifact, repositories, localRepository);
 
             // horrible hack #1: buildFromRepository() doesn't actually set the project's remote repositories
             newProject.setRemoteArtifactRepositories(oldProject.getRemoteArtifactRepositories());
             newProject.setPluginArtifactRepositories(oldProject.getPluginArtifactRepositories());
 
             // horrible hack #2: we need to modify the session to use the new project as its reactor
             final List<MavenProject> newReactor = singletonList(newProject);
             final MavenSession
                 oldSession = oldContext.getSession(),
                 newSession = new MavenSession(
                     oldSession.getContainer(),
                     oldSession.getSettings(),
                     oldSession.getLocalRepository(),
                     oldSession.getEventDispatcher(),
                     new ReactorManager(newReactor),
                     oldSession.getGoals(),
                     oldSession.getExecutionRootDirectory(),
                     oldSession.getExecutionProperties(),
                     oldSession.getUserProperties(),
                     oldSession.getStartTime()
                 );
 
             // horrible hack #3: we need to create a base directory from scratch, and convince the project to like it
             final String baseDir = System.getProperty("user.dir") + "/amps-standalone/";
             newProject.setFile(new File(baseDir, "pom.xml"));
 
             newProject.getProperties().putAll(oldProject.getProperties());
 
             ProjectBuilderConfiguration projectBuilderConfiguration =
                     getProjectBuilderConfigurationFromMavenSession(newSession);
 
             projectBuilder.calculateConcreteState(newProject, projectBuilderConfiguration);
 
             // finally, execute run goal against standalone project
             final MavenContext newContext = oldContext.with(
                 newProject,
                 newReactor,
                 newSession);
             return new MavenGoals(newContext);
         }
         catch (Exception e)
         {
             throw new MojoExecutionException(e.getMessage(), e);
         }
     }
 
     /**
      * MavenSession.getProjectBuilderConfiguration() works at runtime with Maven 2 but isn't
      * available at compile time when we build with Maven 3 artifacts.
      */
     private static ProjectBuilderConfiguration getProjectBuilderConfigurationFromMavenSession(MavenSession session)
         throws MojoExecutionException, InvocationTargetException, IllegalAccessException
     {
         try
         {
             Method m = MavenSession.class.getMethod("getProjectBuilderConfiguration");
             return (ProjectBuilderConfiguration) m.invoke(session);
         }
         catch (NoSuchMethodException e)
         {
             throw new MojoExecutionException("Maven 3 is not supported for run-standalone", e);
         }
     }
 
     protected MavenGoals createMavenGoals(ProjectBuilder projectBuilder)
             throws MojoExecutionException, MojoFailureException, ProjectBuildingException,
             IOException
     {
         // overall goal here is to create a new MavenContext / MavenGoals for the standalone project
         final MavenContext oldContext = getMavenContext();
 
         MavenSession oldSession = oldContext.getSession();
 
         ProjectBuildingRequest pbr = oldSession.getProjectBuildingRequest();
 
         // hack #1 from before
         pbr.setRemoteRepositories(oldSession.getCurrentProject().getRemoteArtifactRepositories());
         pbr.setPluginArtifactRepositories(oldSession.getCurrentProject().getPluginArtifactRepositories());
 
        pbr.getSystemProperties().setProperty("project.basedir", "amps-standalone");
 
         ProjectBuildingResult result = projectBuilder.build(getStandaloneArtifact(), false, pbr);
 
         final List<MavenProject> newReactor = singletonList(result.getProject());
 
         MavenSession newSession = oldSession.clone();
         newSession.setProjects(newReactor);
 
         final MavenContext newContext = oldContext.with(
             result.getProject(),
             newReactor,
             newSession);
 
         return new MavenGoals(newContext);
     }
 }
