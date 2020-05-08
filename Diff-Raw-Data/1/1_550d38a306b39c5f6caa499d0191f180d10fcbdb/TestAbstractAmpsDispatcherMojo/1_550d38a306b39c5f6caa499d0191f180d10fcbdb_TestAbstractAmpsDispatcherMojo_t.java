 package com.atlassian.maven.plugins.ampsdispatcher;
 
 import junit.framework.TestCase;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.PluginManager;
 import org.apache.maven.model.Plugin;
 import org.apache.maven.model.Build;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class TestAbstractAmpsDispatcherMojo extends TestCase
 {
 
     MavenProject project;
     MavenSession session;
     PluginManager pluginManager;
     AbstractAmpsDispatcherMojo mojo;
 
     @Override
     protected void setUp() throws Exception
     {
         super.setUp();
         project = new MavenProject();
         session = mock(MavenSession.class);
         pluginManager = mock(PluginManager.class);
         mojo = new AbstractAmpsDispatcherMojo(){};
         mojo.pluginManager = pluginManager;
         mojo.project = project;
         mojo.session = session;
     }
 
     public void testDetectAmpsProduct()
     {
         assertPlugin(true, "maven-refapp-plugin");
         assertPlugin(true, "maven-confluence-plugin");
         assertPlugin(true, "maven-jira-plugin");
         assertPlugin(true, "maven-confluence-plugin");
         assertPlugin(true, "maven-bamboo-plugin");
        assertPlugin(true, "maven-fecru-plugin");
         assertPlugin(false, "maven-refappsd-plugin");
         assertPlugin(false, "mas");
     }
 
     public void testDetermineGoal()
     {
         when(session.getGoals()).thenReturn(Arrays.asList("foo"));
         assertEquals("foo", mojo.determineGoal());
 
         when(session.getGoals()).thenReturn(Arrays.asList("foo:bar"));
         assertEquals("bar", mojo.determineGoal());
 
         when(session.getGoals()).thenReturn(Arrays.asList("foo:bar:baz"));
         assertEquals("baz", mojo.determineGoal());
 
         when(session.getGoals()).thenReturn(Arrays.asList("foo", "bar"));
         assertEquals("foo", mojo.determineGoal());
     }
 
     private void assertPlugin(boolean expected, String artifactId)
     {
         List<Plugin> buildPlugins = new ArrayList<Plugin>();
         Plugin plugin = new Plugin();
         plugin.setGroupId("com.atlassian.maven.plugins");
         plugin.setArtifactId(artifactId);
         buildPlugins.add(plugin);
         Build build = new Build();
         build.setPlugins(buildPlugins);
         project.setBuild(build);
         assertEquals(expected, artifactId.equals(mojo.detectAmpsProduct()));
     }
 
 }
