 package net.praqma.hudson.test.integration.self;
 
 import java.util.List;
 
 import hudson.model.AbstractBuild;
 import hudson.model.FreeStyleBuild;
 import hudson.model.FreeStyleProject;
 import hudson.model.Result;
 import net.praqma.clearcase.exceptions.ClearCaseException;
 import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
 import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
 import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
 import net.praqma.clearcase.test.junit.CoolTestCase;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.util.ExceptionUtils;
 import net.praqma.hudson.CCUCMBuildAction;
 import net.praqma.hudson.scm.CCUCMScm;
 import net.praqma.jenkins.utils.test.ClearCaseJenkinsTestCase;
 import net.praqma.util.debug.Logger;
 
 public class BaselinesFound extends ClearCaseJenkinsTestCase {
 
 	private static Logger logger = Logger.getLogger();
 	
 	public FreeStyleProject setupProject( boolean recommend, boolean tag, boolean description ) throws Exception {
		//String uniqueTestVobName = "ccucm" + coolTest.uniqueTimeStamp;
		String uniqueTestVobName = "ccucm" + CoolTestCase.getUniqueTimestamp();
 		coolTest.variables.put( "vobname", uniqueTestVobName );
 		coolTest.variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
 		
 		coolTest.bootStrap();
 		
 		logger.info( "Setting up build for self polling, recommend:" + recommend + ", tag:" + tag + ", description:" + description );
 		
 		FreeStyleProject project = createFreeStyleProject( "ccucm-project-" + uniqueTestVobName );
 		
 		// boolean createBaseline, String nameTemplate, boolean forceDeliver, boolean recommend, boolean makeTag, boolean setDescription
 		CCUCMScm scm = new CCUCMScm( "Model@" + coolTest.getPVob(), "INITIAL", "ALL", false, "self", uniqueTestVobName + "_one_int@" + coolTest.getPVob(), "successful", false, "", true, recommend, tag, description, "jenkins" );
 		
 		project.setScm( scm );
 		
 		return project;
 	}
 	
 	public AbstractBuild<?, ?> initiateBuild( boolean recommend, boolean tag, boolean description, boolean fail ) throws Exception {
 		FreeStyleProject project = setupProject( recommend, tag, description );
 		
 		FreeStyleBuild build = project.scheduleBuild2( 0 ).get();
 		
 		logger.info( "Build info for: " + build );
 		
 		logger.info( "Workspace: " + build.getWorkspace() );
 		
 		logger.info( "Logfile: " + build.getLogFile() );
 		
 		logger.info( "DESCRIPTION: " + build.getDescription() );
 		
 		logger.info( "-------------------------------------------------\nJENKINS LOG: " + getLog( build ) + "\n-------------------------------------------------\n" );
 		
 		return build;
 	}
 	
 	public CCUCMBuildAction getBuildAction( AbstractBuild<?, ?> build ) {
 		/* Check the build baseline */
 		logger.info( "Getting ccucm build action from " + build );
 		CCUCMBuildAction action = build.getAction( CCUCMBuildAction.class );
 		assertNotNull( action.getBaseline() );
 		return action;
 	}
 	
 	public Baseline getBuildBaseline( AbstractBuild<?, ?> build ) {
 		CCUCMBuildAction action = getBuildAction( build );
 		assertNotNull( action.getBaseline() );
 		return action.getBaseline();
 	}
 	
 	public void assertBuildBaseline( Baseline baseline, AbstractBuild<?, ?> build ) {
 		assertEquals( baseline, getBuildBaseline( build ) );
 	}
 	
 	public boolean isRecommended( Baseline baseline, AbstractBuild<?, ?> build ) throws ClearCaseException {
 		CCUCMBuildAction action = getBuildAction( build );
 		Stream stream = action.getStream().load();
 		
 		try {
 			List<Baseline> baselines = stream.getRecommendedBaselines();
 			
 			logger.info( "Recommended baselines: " + baselines );
 			
 			for( Baseline rb : baselines ) {
 				if( baselines.equals( rb ) ) {
 					return true;
 				}
 			}
 		} catch( Exception e ) {
 			ExceptionUtils.log( e, true );
 		}
 		
 		return false;
 	}
 	
 	public void testNoOptions() throws Exception {
 		AbstractBuild<?, ?> build = initiateBuild( false, false, false, false );
 		
 		/* Build validation */
 		assertTrue( build.getResult().isBetterOrEqualTo( Result.SUCCESS ) );
 		
 		/* Expected build baseline */
 		logger.info( "Build baseline: " + getBuildBaseline( build ) );
 		
 		Baseline baseline = CoolTestCase.context.baselines.get( "model-1" );
 		
 		assertBuildBaseline( baseline, build );
 				
 		assertFalse( isRecommended( baseline, build ) );
 	}
 	
 	
 	public void testRecommended() throws Exception {
 		AbstractBuild<?, ?> build = initiateBuild( true, false, false, false );
 		
 		/* Build validation */
 		assertTrue( build.getResult().isBetterOrEqualTo( Result.SUCCESS ) );
 		
 		/* Expected build baseline */
 		logger.info( "Build baseline: " + getBuildBaseline( build ) );
 		
 		Baseline baseline = CoolTestCase.context.baselines.get( "model-1" );
 		
 		assertBuildBaseline( baseline, build );
 				
 		assertTrue( isRecommended( baseline, build ) );
 	}
 }
