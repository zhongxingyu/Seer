 package net.praqma.hudson.scm;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 
 import org.junit.Test;
 
 import hudson.FilePath;
 import hudson.model.FreeStyleBuild;
 import hudson.model.Result;
 import hudson.model.FreeStyleProject;
 import net.praqma.hudson.CCUCMBuildAction;
 import net.praqma.jenkins.utils.test.ClearCaseJenkinsTestCase;
 import net.praqma.util.debug.Logger;
 
 public class ScmTest extends ClearCaseJenkinsTestCase {
 	
 	private static Logger logger = Logger.getLogger();
 	
 	@Test
 	public void test1() throws Exception {
 		String uniqueTestVobName = "ccucm" + coolTest.uniqueTimeStamp;
 		coolTest.variables.put( "vobname", uniqueTestVobName );
 		coolTest.variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
 		
 		coolTest.bootStrap();
 		FreeStyleProject project = createFreeStyleProject( "ccucm" );
 		
		CCUCMScm scm = new CCUCMScm( "Model@" + coolTest.getPVob(), "INITIAL", "ALL", false, "self", "cc1_one_dev@" + coolTest.getPVob(), "successful", false, "", true, true, false, true, "jenkins" );
 		
 		project.setScm( scm );
 		
 		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
 		
 		System.out.println( "Workspace: " + b.getWorkspace() );
 		
 		System.out.println( "Logfile: " + b.getLogFile() );
 		
 		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
 		String line = "";
 		while( ( line = br.readLine() ) != null ) {
 			System.out.println( "[JENKINS] " + line );
 		}
 
 		br.close();
 		
 		/* Validation */
 		assertTrue( b.getResult().isBetterOrEqualTo( Result.SUCCESS ) );
 		
 		/* Workspace validation */
 		logger.info( "Checking workspace" );
 		int check = 0;
 		List<FilePath> rootDirs = b.getWorkspace().listDirectories();
 		for( FilePath f : rootDirs ) {
 			if( f.getBaseName().equals( "Model" ) ) {
 				check |= 1;
 			}
 		}
 		
 		assertEquals( 1, check );
 		
 		/* Check the build baseline */
 		logger.info( "Getting action" );
 		CCUCMBuildAction action = b.getAction( CCUCMBuildAction.class );
 		logger.debug( "Asserting baseline" );
 		assertNotNull( action.getBaseline() );
 		if( action.getBaseline() != null ) {
 			logger.debug( "Asserting baseline promotion level" );
 			action.getBaseline().getPromotionLevel( true );
 		}
 	}
 	
 	
 	@Test
 	public void testChildPolling() throws Exception {
 		String uniqueTestVobName = "ccucm" + coolTest.uniqueTimeStamp;
 		coolTest.variables.put( "vobname", uniqueTestVobName );
 		coolTest.variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
 		
 		coolTest.bootStrap();
 		FreeStyleProject project = createFreeStyleProject( "ccucm" );
 		
 		CCUCMScm scm = new CCUCMScm( "Model@" + coolTest.getPVob(), "INITIAL", "ALL", false, "child", "cc1_one_int@" + coolTest.getPVob(), "successful", false, "My-super-hot-baseline", true, true, false, true, "jenkins" );
 		
 		project.setScm( scm );
 		
 		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
 		
 		System.out.println( "Workspace: " + b.getWorkspace() );
 		
 		System.out.println( "Logfile: " + b.getLogFile() );
 		
 		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
 		String line = "";
 		while( ( line = br.readLine() ) != null ) {
 			System.out.println( "[JENKINS] " + line );
 		}
 
 		br.close();
 		
 		/* Validation */
 		assertTrue( b.getResult().isBetterOrEqualTo( Result.SUCCESS ) );
 		
 		/* Workspace validation */
 		logger.info( "Checking workspace" );
 		int check = 0;
 		List<FilePath> rootDirs = b.getWorkspace().listDirectories();
 		for( FilePath f : rootDirs ) {
 			if( f.getBaseName().equals( "Model" ) ) {
 				check |= 1;
 			}
 		}
 		
 		assertEquals( 1, check );
 		
 		/* Check the build baseline */
 		logger.info( "Getting action" );
 		CCUCMBuildAction action = b.getAction( CCUCMBuildAction.class );
 		logger.debug( "Asserting baseline" );
 		assertNotNull( action.getBaseline() );
 		if( action.getBaseline() != null ) {
 			logger.debug( "Asserting baseline promotion level" );
 			action.getBaseline().getPromotionLevel( true );
 		}
 	}
 	
 	/*
 	@Test
 	public void testWrongStream() throws Exception {
 		String uniqueTestVobName = "ccucm" + coolTest.uniqueTimeStamp;
 		coolTest.variables.put( "vobname", uniqueTestVobName );
 		coolTest.variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
 		
 		coolTest.bootStrap();
 		FreeStyleProject project = createFreeStyleProject( "ccucm" );
 		
 		CCUCMScm scm = new CCUCMScm( "_System@", "INITIAL", "ALL", false, "self", "__int@", "successful", false, "", true, true, false, true, "jenkins" );
 		
 		project.setScm( scm );
 		
 		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
 		
 		System.out.println( "Workspace: " + b.getWorkspace() );
 		
 		System.out.println( "Logfile: " + b.getLogFile() );
 		
 		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
 		String line = "";
 		while( ( line = br.readLine() ) != null ) {
 			System.out.println( "[JENKINS] " + line );
 		}
 
 		br.close();
 	}
 	*/
 }
