 package net.praqma.hudson.test.integration.userstories;
 
 import org.junit.ClassRule;
 import org.junit.Rule;
 import org.junit.Test;
 
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.FreeStyleProject;
 import hudson.model.Result;
 import hudson.model.TaskListener;
 import hudson.scm.PollingResult;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.hudson.scm.CCUCMScm;
 import net.praqma.hudson.test.CCUCMRule;
 import net.praqma.util.debug.Logger;
 
 import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
 import net.praqma.clearcase.test.junit.ClearCaseRule;
 
 import static org.junit.Assert.*;
 
 public class Story02 {
 
 	@ClassRule
 	public static CCUCMRule jenkins = new CCUCMRule();
 	
 	@Rule
 	public static ClearCaseRule ccenv = new ClearCaseRule( "ccucm-story02" );
 
 	private static Logger logger = Logger.getLogger();
 
 	@Test
 	@ClearCaseUniqueVobName( name = "story02b" )
	public void story01b() throws Exception {
 		
 		AbstractBuild<?, ?> build = jenkins.initiateBuild( "story02b", "self", "_System@" + ccenv.getPVob(), "three_int@" + ccenv.getPVob(), false, false, false, false, false );
 
 		/* Build validation */
 		assertTrue( build.getResult().isBetterOrEqualTo( Result.NOT_BUILT ) );
 		
 		/* Expected build baseline */
 		Baseline buildBaseline = jenkins.getBuildBaselineNoAssert( build );
 		assertNull( buildBaseline );
 	}
 	
 	@Test
 	@ClearCaseUniqueVobName( name = "story02d" )
	public void story01d() throws Exception {
 		
 		/* First build must succeed to get a workspace */
 		AbstractBuild<?, ?> build = jenkins.initiateBuild( "story02d", "self", "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob(), false, false, false, false, false );
 		AbstractProject<?, ?> project = build.getProject();
 		assertTrue( build.getResult().isBetterOrEqualTo( Result.SUCCESS ) );		
 		
 		/* New scm with wrong component */
 		CCUCMScm scm = new CCUCMScm( "_System@" + ccenv.getPVob(), "INITIAL", "ALL", false, "self", "three_int@" + ccenv.getPVob(), "successful", false, "", true, false, false, false, "jenkins" );
 		project.setScm( scm );
 		
 		TaskListener listener = jenkins.createTaskListener();
 		PollingResult result = project.poll( listener );
 
 		/* Polling result validation */
 		assertEquals( PollingResult.NO_CHANGES, result );
 		
 	}
 		
 }
