 package net.praqma.hudson.test.integration.userstories;
 
 import net.praqma.hudson.test.BaseTestClass;
 import net.praqma.util.test.junit.LoggingRule;
 import org.junit.ClassRule;
 import org.junit.Rule;
 import org.junit.Test;
 
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Result;
 import hudson.model.TaskListener;
 import hudson.scm.PollingResult;
 import net.praqma.hudson.scm.CCUCMScm;
 import net.praqma.hudson.test.CCUCMRule;
 import net.praqma.hudson.test.SystemValidator;
 import net.praqma.junit.TestDescription;
 import net.praqma.util.debug.Logger;
 
 import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
 import net.praqma.clearcase.test.junit.ClearCaseRule;
 
 import java.util.logging.Level;
 
 import static org.junit.Assert.*;
 
 public class Story02 extends BaseTestClass {
 	
 	@Rule
 	public static ClearCaseRule ccenv = new ClearCaseRule( "ccucm-story02" );
 
 	private static Logger logger = Logger.getLogger();
 
 	@Test
 	@ClearCaseUniqueVobName( name = "story02b" )
 	@TestDescription( title = "Story 02b", text = "New baseline, bl1, on integration stream, poll on self, wrong stream." )
 	public void story02b() throws Exception {
 		
 		AbstractBuild<?, ?> build = jenkins.initiateBuild( ccenv.getUniqueName(), "self", "_System@" + ccenv.getPVob(), "three_int@" + ccenv.getPVob(), false, false, false, false, false );
 
 		SystemValidator validator = new SystemValidator( build )
		.validateBuild( Result.FAILURE )
 		.validateBuiltBaselineNotFound()
 		.validate();
 	}
 	
 	@Test
 	@ClearCaseUniqueVobName( name = "story02d" )
 	@TestDescription( title = "Story 02d, polling", text = "New baseline, bl1, on integration stream, poll on self, wrong stream." )
 	public void story02d() throws Exception {
 		
 		/* First build must succeed to get a workspace */
 		AbstractBuild<?, ?> build = jenkins.initiateBuild( ccenv.getUniqueName(), "self", "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob(), false, false, false, false, false );
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
