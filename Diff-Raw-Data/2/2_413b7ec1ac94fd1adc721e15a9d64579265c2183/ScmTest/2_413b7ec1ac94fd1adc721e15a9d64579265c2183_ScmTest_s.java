 package net.praqma.hudson.scm;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.junit.Test;
 
 import hudson.model.FreeStyleBuild;
 import hudson.model.FreeStyleProject;
 import net.praqma.jenkins.utils.test.ClearCaseJenkinsTestCase;
 
 public class ScmTest extends ClearCaseJenkinsTestCase {
 	
 	@Test
 	public void test1() throws Exception {
		String uniqueTestVobName = "ccucm" + uniqueTimeStamp;
 		coolTest.variables.put( "vobname", uniqueTestVobName );
 		coolTest.variables.put( "pvobname", uniqueTestVobName );
 		
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
 
 	}
 }
