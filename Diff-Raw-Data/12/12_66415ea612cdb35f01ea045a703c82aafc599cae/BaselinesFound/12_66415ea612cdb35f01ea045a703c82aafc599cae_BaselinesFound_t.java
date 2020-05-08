 package net.praqma.hudson.test.integration.self;
 
 import static org.junit.Assert.*;
 
 import org.junit.ClassRule;
 import org.junit.Rule;
 import org.junit.Test;
 
 import hudson.model.AbstractBuild;
 import hudson.model.Result;
 import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
 import net.praqma.clearcase.test.junit.ClearCaseRule;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
 import net.praqma.hudson.test.CCUCMRule;
 import net.praqma.hudson.test.SystemValidator;
 import net.praqma.junit.TestDescription;

import java.util.logging.Logger;
 
 public class BaselinesFound {
 	
 	@ClassRule
 	public static CCUCMRule jenkins = new CCUCMRule();
 	
 	@Rule
 	public static ClearCaseRule ccenv = new ClearCaseRule( "ccucm" );
 	
	private static Logger logger = Logger.getLogger( BaselinesFound.class.getName() );
 	
 	public AbstractBuild<?, ?> initiateBuild( String projectName, boolean recommend, boolean tag, boolean description, boolean fail ) throws Exception {
 		return jenkins.initiateBuild( projectName, "self", "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob(), recommend, tag, description, fail, false );
 	}
 
 	@Test
 	@ClearCaseUniqueVobName( name = "self-nop" )
 	@TestDescription( title = "Self polling", text = "baseline available" )
 	public void testNoOptions() throws Exception {
 		AbstractBuild<?, ?> build = initiateBuild( ccenv.getUniqueName(), false, false, false, false );
 		
 		Baseline baseline = ccenv.context.baselines.get( "model-1" );
 		SystemValidator validator = new SystemValidator( build )
 		.validateBuild( Result.SUCCESS )
 		.validateBuiltBaseline( PromotionLevel.BUILT, baseline, false )
 		.validate();
 	}
 	
 	@Test
 	@ClearCaseUniqueVobName( name = "self-recommended" )
 	@TestDescription( title = "Self polling", text = "baseline available", configurations = { "Recommend = true" } )
 	public void testRecommended() throws Exception {
 		AbstractBuild<?, ?> build = initiateBuild( ccenv.getUniqueName(), true, false, false, false );
 		
 		Baseline baseline = ccenv.context.baselines.get( "model-1" );
 		SystemValidator validator = new SystemValidator( build )
 		.validateBuild( Result.SUCCESS )
 		.validateBuiltBaseline( PromotionLevel.BUILT, baseline, true )
 		.validate();
 	}
 	
 	@ClearCaseUniqueVobName( name = "self-description" )
 	@TestDescription( title = "Self polling", text = "baseline available", configurations = { "Set description = true" } )
 	public void testDescription() throws Exception {
 		AbstractBuild<?, ?> build = initiateBuild( ccenv.getUniqueName(), false, false, true, false );
 		
 		Baseline baseline = ccenv.context.baselines.get( "model-1" );
 		SystemValidator validator = new SystemValidator( build )
 		.validateBuild( Result.SUCCESS )
 		.validateBuiltBaseline( PromotionLevel.BUILT, baseline, false )
 		.validate();
 	}
 	
 	@Test
 	@ClearCaseUniqueVobName( name = "self-tagged" )
 	@TestDescription( title = "Self polling", text = "baseline available", configurations = { "Set tag = true" } )
 	public void testTagged() throws Exception {
 		jenkins.makeTagType( ccenv.getPVob() );
 		AbstractBuild<?, ?> build = initiateBuild( ccenv.getUniqueName(), false, true, false, false );
 		
 		Baseline baseline = ccenv.context.baselines.get( "model-1" );
 		SystemValidator validator = new SystemValidator( build )
 		.validateBuild( Result.SUCCESS )
 		.validateBuiltBaseline( PromotionLevel.BUILT, baseline, false )
 		.validate();
 	}
 }
