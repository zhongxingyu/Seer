 package net.praqma.hudson.test.integration.userstories;
 
 import net.praqma.hudson.test.BaseTestClass;
 import net.praqma.util.test.junit.LoggingRule;
 import org.junit.ClassRule;
 import org.junit.Rule;
 import org.junit.Test;
 
 import hudson.model.AbstractBuild;
 import hudson.model.Result;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
 import net.praqma.hudson.test.CCUCMRule;
 import net.praqma.hudson.test.SystemValidator;
 import net.praqma.junit.DescriptionRule;
 import net.praqma.junit.TestDescription;
 import net.praqma.util.debug.Logger;
 
 import net.praqma.clearcase.test.junit.ClearCaseRule;
 
 import java.util.logging.Level;
 
 public class Story10 extends BaseTestClass {
 	
 	@Rule
 	public static ClearCaseRule ccenv = new ClearCaseRule( "ccucm-story10", "setup-story10.xml" );
 	
 	@Rule
 	public static DescriptionRule desc = new DescriptionRule();
 
 	private static Logger logger = Logger.getLogger();
 
 	@Test
 	@TestDescription( title = "Story 10", text = "New baseline, bl1, on dev stream, dev1, poll on child, create baselines, but wrong baseline template", configurations = { "Create baselines = true", "Name template = [fail]" } )
 	public void story10() throws Exception {
 		
 		AbstractBuild<?, ?> build = jenkins.initiateBuild( ccenv.getUniqueName(), "child", "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob(), false, false, false, false, true, false, "[what]-)(/&" );
 
 		Baseline b = ccenv.context.baselines.get( "model-1" ).load();
 		
 		SystemValidator validator = new SystemValidator( build )
 		.validateBuild( Result.FAILURE )
		.validateBuiltBaselineNotFound()
 		.validateCreatedBaseline( false )
 		.validate();
 	}
 	
 }
