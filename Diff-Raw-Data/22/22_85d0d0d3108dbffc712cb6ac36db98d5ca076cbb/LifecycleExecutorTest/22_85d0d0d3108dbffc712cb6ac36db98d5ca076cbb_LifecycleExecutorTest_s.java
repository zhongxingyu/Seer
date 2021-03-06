 package org.apache.maven.lifecycle;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.AbstractCoreMavenComponentTestCase;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.model.Plugin;
 import org.apache.maven.plugin.MojoExecution;
 import org.apache.maven.plugin.PluginManager;
 import org.apache.maven.plugin.descriptor.MojoDescriptor;
 import org.apache.maven.plugin.descriptor.PluginDescriptor;
 import org.codehaus.plexus.component.annotations.Requirement;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 
 public class LifecycleExecutorTest
     extends AbstractCoreMavenComponentTestCase
 {
     @Requirement
     private DefaultLifecycleExecutor lifecycleExecutor;
     
     protected void setUp()
         throws Exception
     {
         super.setUp();
         lifecycleExecutor = (DefaultLifecycleExecutor) lookup( LifecycleExecutor.class );
     }
 
     protected String getProjectsDirectory()
     {
         return "src/test/projects/lifecycle-executor";
     }
         
     // -----------------------------------------------------------------------------------------------
     // 
     // -----------------------------------------------------------------------------------------------    
     
     public void testLifecyclePhases()
     {
         assertNotNull( lifecycleExecutor.getLifecyclePhases() );
     }
 
     // -----------------------------------------------------------------------------------------------
     // Tests which exercise the lifecycle executor when it is dealing with default lifecycle phases.
     // -----------------------------------------------------------------------------------------------
     
     public void testLifecycleQueryingUsingADefaultLifecyclePhase()
         throws Exception
     {   
         File pom = getProject( "project-with-additional-lifecycle-elements" );
         MavenSession session = createMavenSession( pom );
         assertEquals( "project-with-additional-lifecycle-elements", session.getCurrentProject().getArtifactId() );
         assertEquals( "1.0", session.getCurrentProject().getVersion() );
        List<MojoDescriptor> lifecyclePlan = lifecycleExecutor.calculateLifecyclePlan( "package", session );
         
         // resources:resources
         // compiler:compile
         // plexus-component-metadata:generate-metadata
         // resources:testResources
         // compiler:testCompile
         // plexus-component-metadata:generate-test-metadata
         // surefire:test
         // jar:jar
         
        assertEquals( "resources:resources", lifecyclePlan.get( 0 ).getFullGoalName() );
        assertEquals( "compiler:compile", lifecyclePlan.get( 1 ).getFullGoalName() );
        assertEquals( "plexus-component-metadata:generate-metadata", lifecyclePlan.get( 2 ).getFullGoalName() );
        assertEquals( "resources:testResources", lifecyclePlan.get( 3 ).getFullGoalName() );
        assertEquals( "compiler:testCompile", lifecyclePlan.get( 4 ).getFullGoalName() );
        assertEquals( "plexus-component-metadata:generate-test-metadata", lifecyclePlan.get( 5 ).getFullGoalName() );
        assertEquals( "surefire:test", lifecyclePlan.get( 6 ).getFullGoalName() );
        assertEquals( "jar:jar", lifecyclePlan.get( 7 ).getFullGoalName() );        
     }    
     
     public void testLifecycleExecutionUsingADefaultLifecyclePhase()
         throws Exception
     {
         File pom = getProject( "project-with-additional-lifecycle-elements" );
         MavenSession session = createMavenSession( pom );        
         assertEquals( "project-with-additional-lifecycle-elements", session.getCurrentProject().getArtifactId() );
         assertEquals( "1.0", session.getCurrentProject().getVersion() );                                
         lifecycleExecutor.execute( session );
     }    
     
     public void testLifecyclePluginsRetrievalForDefaultLifecycle()
         throws Exception
     {
         List<Plugin> plugins = new ArrayList<Plugin>( lifecycleExecutor.getPluginsBoundByDefaultToAllLifecycles( "jar" ) );  
         
         for( Plugin plugin : plugins )
         {
             System.out.println( plugin.getArtifactId() );
         }
         
         assertEquals( 8, plugins.size() );
     }
     
     public void testPluginConfigurationCreation()
         throws Exception
     {
         File pom = getProject( "project-with-additional-lifecycle-elements" );
         MavenSession session = createMavenSession( pom );
         MojoDescriptor mojoDescriptor = lifecycleExecutor.getMojoDescriptor( "org.apache.maven.plugins:maven-remote-resources-plugin:1.0:process", session.getCurrentProject(), session.getLocalRepository() );
        Xpp3Dom dom = lifecycleExecutor.convert( mojoDescriptor.getMojoConfiguration() );
         System.out.println( dom );
     }
         
 }
