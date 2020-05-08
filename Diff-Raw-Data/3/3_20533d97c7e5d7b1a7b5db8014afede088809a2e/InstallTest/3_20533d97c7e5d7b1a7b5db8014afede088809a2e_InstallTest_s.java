 package org.sonatype.aether.ant;
 
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.sonatype.aether.test.util.TestFileUtils;
 
 public class InstallTest
     extends AntBuildsTest
 {
 
     private File defaultRepoPath;
 
     @Override
     protected void setUp()
         throws Exception
     {
         super.setUp();
         defaultRepoPath = defaultLocalRepository;
         TestFileUtils.delete( new File( defaultRepoPath, "test" ) );
 
         configureProject( "src/test/ant/Install.xml" );
     }
 
     @Override
     protected void tearDown()
         throws Exception
     {
         super.tearDown();
         TestFileUtils.delete( new File( defaultRepoPath, "test" ) );
     }
 
     public void testInstallGlobalPom()
     {
         executeTarget( "testInstallGlobalPom" );
         long tstamp = System.currentTimeMillis();
 
         assertLogContaining( "Installing" );
         
         assertUpdatedFile( tstamp, defaultRepoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT.pom" );
     }
 
     public void testInstallOverrideGlobalPom()
     {
         executeTarget( "testInstallOverrideGlobalPom" );
         long tstamp = System.currentTimeMillis();
 
         assertLogContaining( "Installing" );
 
         assertUpdatedFile( tstamp, defaultRepoPath, "test/other/0.1-SNAPSHOT/other-0.1-SNAPSHOT.pom" );
     }
 
     public void testInstallOverrideGlobalPomByRef()
     {
         long tstamp = System.currentTimeMillis();
         executeTarget( "testInstallOverrideGlobalPomByRef" );
 
         assertLogContaining( "Installing" );
        File defaultRepoPath = new File( System.getProperty( "user.home" ), ".m2/repository/" );
 
         assertUpdatedFile( tstamp, defaultRepoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT.pom" );
         assertUpdatedFile( tstamp, defaultRepoPath, "test/other/0.1-SNAPSHOT/other-0.1-SNAPSHOT.pom" );
     }
 
     public void testDefaultRepo()
     {
         executeTarget( "testDefaultRepo" );
         long tstamp = System.currentTimeMillis();
 
         assertLogContaining( "Installing" );
        File defaultRepoPath = new File( System.getProperty( "user.home" ), ".m2/repository/" );
 
         assertUpdatedFile( tstamp, defaultRepoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT.pom" );
         assertUpdatedFile( tstamp, defaultRepoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT-ant.xml" );
     }
 
     public void testCustomRepo()
         throws IOException
     {
         File repoPath = new File( "target/local-repo-custom" );
         TestFileUtils.delete( repoPath );
 
         executeTarget( "testCustomRepo" );
         long tstamp = System.currentTimeMillis();
 
         System.out.println( getLog() );
         assertLogContaining( "Installing" );
 
         assertUpdatedFile( tstamp, repoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT.pom" );
         assertUpdatedFile( tstamp, repoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT-ant.xml" );
         TestFileUtils.delete( repoPath );
     }
 
     private void assertUpdatedFile( long tstamp, File repoPath, String path )
     {
         File file = new File( repoPath, path );
         assertThat( "File does not exist in default repo: " + file.getAbsolutePath(), file.exists() );
         assertThat( "Files were not updated for 1s before/after timestamp",
                     file.lastModified(),
                     allOf( greaterThanOrEqualTo( ( ( tstamp - 500 ) / 1000 ) * 1000 ),
                            lessThanOrEqualTo( tstamp + 2000 ) ) );
     }
 }
