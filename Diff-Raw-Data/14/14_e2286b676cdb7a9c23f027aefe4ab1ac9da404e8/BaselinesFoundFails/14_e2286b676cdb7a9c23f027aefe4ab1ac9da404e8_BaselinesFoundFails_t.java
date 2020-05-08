 package net.praqma.hudson.test.integration.self;
 
 import hudson.model.AbstractBuild;
 import hudson.model.Result;
 import net.praqma.clearcase.test.junit.CoolTestCase;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
 import net.praqma.hudson.test.CCUCMTestCase;
 import net.praqma.util.debug.Logger;
 
 public class BaselinesFoundFails extends CCUCMTestCase {
 
 	private static Logger logger = Logger.getLogger();
 
 	public void testNoOptions() throws Exception {
 		AbstractBuild<?, ?> build = initiateBuild( false, false, false, true );
 		
 		/* Build validation */
 		assertTrue( build.getResult().isBetterOrEqualTo( Result.SUCCESS ) );
 		
 		/* Expected build baseline */
 		logger.info( "Build baseline: " + getBuildBaseline( build ) );
 		
 		Baseline baseline = CoolTestCase.context.baselines.get( "model-1" );
 		
 		assertBuildBaseline( baseline, build );
 		assertFalse( isRecommended( baseline, build ) );
 		assertNull( getTag( baseline, build ) );
 		samePromotionLevel( baseline, PromotionLevel.REJECTED );
 	}
 	
 	
 	public void testRecommended() throws Exception {
 		AbstractBuild<?, ?> build = initiateBuild( true, false, false, true );
 		
 		/* Build validation */
 		assertTrue( build.getResult().isBetterOrEqualTo( Result.SUCCESS ) );
 		
 		/* Expected build baseline */
 		logger.info( "Build baseline: " + getBuildBaseline( build ) );
 		
 		Baseline baseline = CoolTestCase.context.baselines.get( "model-1" );
 		
 		assertBuildBaseline( baseline, build );
 		assertFalse( isRecommended( baseline, build ) );
 		assertNull( getTag( baseline, build ) );
 		samePromotionLevel( baseline, PromotionLevel.REJECTED );
 	}
 	
 	public void testTagged() throws Exception {
		makeTagType();
 		AbstractBuild<?, ?> build = initiateBuild( false, true, false, true );
 		
 		/* Build validation */
 		assertTrue( build.getResult().isBetterOrEqualTo( Result.SUCCESS ) );
 		
 		/* Expected build baseline */
 		logger.info( "Build baseline: " + getBuildBaseline( build ) );
 		
 		Baseline baseline = CoolTestCase.context.baselines.get( "model-1" );
 		
 		assertBuildBaseline( baseline, build );
 		assertFalse( isRecommended( baseline, build ) );
 		assertNotNull( getTag( baseline, build ) );
 		samePromotionLevel( baseline, PromotionLevel.REJECTED );
 	}
 }
