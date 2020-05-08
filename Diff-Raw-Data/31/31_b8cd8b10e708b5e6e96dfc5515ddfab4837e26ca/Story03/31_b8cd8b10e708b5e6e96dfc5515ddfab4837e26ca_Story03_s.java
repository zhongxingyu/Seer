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
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
 import net.praqma.hudson.test.CCUCMRule;
 import net.praqma.hudson.test.SystemValidator;
 import net.praqma.junit.TestDescription;
 import net.praqma.util.debug.Logger;
 
 import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
 import net.praqma.clearcase.test.junit.ClearCaseRule;
 
 import java.util.logging.Level;
 
 import static org.junit.Assert.*;
 
 public class Story03 extends BaseTestClass {
 	
 	@Rule
 	public static ClearCaseRule ccenv = new ClearCaseRule( "ccucm-story03", "setup-story3.xml" );
 
 	private static Logger logger = Logger.getLogger();
 
 	@Test
 	@ClearCaseUniqueVobName( name = "story03a" )
 	@TestDescription( title = "Story 03a", text = "No new baseline on dev stream, poll on child" )
 	public void story03a() throws Exception {
 		/* Flip promotion level */
 		Baseline baseline = ccenv.context.baselines.get( "model-1" );
 		baseline.setPromotionLevel( PromotionLevel.REJECTED );
 		
 		AbstractBuild<?, ?> build = jenkins.initiateBuild( ccenv.getUniqueName(), "self", "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob(), false, false, false, false, false );
 
 		SystemValidator validator = new SystemValidator( build )
		.validateBuild( Result.NOT_BUILT )
 		.validateBuiltBaselineNotFound()
 		.validate();
 	}
 	
 	@Test
 	@ClearCaseUniqueVobName( name = "story03b" )
 	@TestDescription( title = "Story 03b, polling", text = "No new baseline on dev stream, poll on child" )
 	public void story03b() throws Exception {
 		/* Flip promotion level */
 		Baseline baseline = ccenv.context.baselines.get( "model-1" );
 		baseline.setPromotionLevel( PromotionLevel.INITIAL );
 		
 		/* First build must succeed to get a workspace */
 		AbstractBuild<?, ?> build = jenkins.initiateBuild( ccenv.getUniqueName(), "self", "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob(), false, false, false, false, false );
 		AbstractProject<?, ?> project = build.getProject();
 		assertTrue( build.getResult().isBetterOrEqualTo( Result.SUCCESS ) );		
 				
 		TaskListener listener = jenkins.createTaskListener();
 		PollingResult result = project.poll( listener );
 
 		/* Polling result validation */
 		assertEquals( PollingResult.NO_CHANGES, result );
 		
 	}
 		
 }
