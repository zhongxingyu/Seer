 package org.sonatype.nexus.integrationtests.nexus511;
 
 import java.io.File;
 
 import org.apache.maven.it.VerificationException;
 import org.apache.maven.it.Verifier;
 import org.junit.Before;
 import org.junit.Test;
 import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
 import org.sonatype.nexus.integrationtests.TestContainer;
 
 /**
  * Tests deploy to nexus using mvn deploy
  */
 public class Nexus511MavenDeployTest
     extends AbstractMavenNexusIT
 {
 
     static
     {
         TestContainer.getInstance().getTestContext().setSecureTest( true );
     }
 
     private Verifier verifier;
 
    @SuppressWarnings( "unchecked" )
     @Before
     public void createVerifier()
         throws Exception
     {
         File mavenProject = getTestFile( "maven-project" );
         File settings = getTestFile( "server.xml" );
         verifier = createVerifier( mavenProject, settings );

        // need to force it to run test at grid
        verifier.getCliOptions().add(
                                      "-DaltDeploymentRepository=nexus-test-harness-repo::default::"
                                          + getBaseNexusUrl() + "content/repositories/nexus-test-harness-repo" );
     }
 
     @Test
     public void deploy()
         throws Exception
     {
         try
         {
             verifier.executeGoal( "deploy" );
             verifier.verifyErrorFreeLog();
         }
         catch ( VerificationException e )
         {
             failTest( verifier );
         }
     }
 
     @SuppressWarnings( "unchecked" )
     @Test
     public void privateDeploy()
         throws Exception
     {
         // try to deploy without servers authentication tokens
         verifier.getCliOptions().clear();
         verifier.getCliOptions().add( "-X" );
 
        // need to force it to run test at grid
         verifier.getCliOptions().add(
                                       "-DaltDeploymentRepository=nexus-test-harness-repo::default::"
                                           + getBaseNexusUrl() + "content/repositories/nexus-test-harness-repo" );
 
         try
         {
             verifier.executeGoal( "deploy" );
             verifier.verifyErrorFreeLog();
             failTest( verifier );
         }
         catch ( VerificationException e )
         {
             // Expected exception
         }
     }
 
 }
