 package net.praqma.hudson.test;
 
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.FreeStyleBuild;
 import hudson.model.FreeStyleProject;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.jvnet.hudson.test.TestBuilder;
 
 import net.praqma.clearcase.exceptions.ClearCaseException;
 import net.praqma.clearcase.exceptions.CleartoolException;
 import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
 import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
 import net.praqma.clearcase.test.junit.CoolTestCase;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.HyperLink;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.Tag;
 import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
 import net.praqma.clearcase.util.ExceptionUtils;
 import net.praqma.hudson.CCUCMBuildAction;
 import net.praqma.hudson.scm.CCUCMScm;
 import net.praqma.jenkins.utils.test.ClearCaseJenkinsTestCase;
 import net.praqma.util.debug.Logger;
 
 public class CCUCMTestCase extends ClearCaseJenkinsTestCase {
 	
 	private static Logger logger = Logger.getLogger();
 	
 	public String setupCC( boolean installTag ) throws Exception {
 		//String uniqueTestVobName = "ccucm" + coolTest.uniqueTimeStamp;
 		String uniqueTestVobName = "ccucm" + CoolTestCase.getUniqueTimestamp();
 		coolTest.variables.put( "vobname", uniqueTestVobName );
 		coolTest.variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
 		
 		coolTest.bootStrap();
 		
 		if( installTag ) {
 			makeTagType();
 		}
 		
 		return uniqueTestVobName;
 	}
 	
	public FreeStyleProject setupProject( String projectName, String uniqueTestVobName, String type, String stream, boolean recommend, boolean tag, boolean description ) throws Exception {
 
 		logger.info( "Setting up build for self polling, recommend:" + recommend + ", tag:" + tag + ", description:" + description );
 		
 		FreeStyleProject project = createFreeStyleProject( "ccucm-project-" + projectName );
 		
 		// boolean createBaseline, String nameTemplate, boolean forceDeliver, boolean recommend, boolean makeTag, boolean setDescription
 		CCUCMScm scm = new CCUCMScm( "Model@" + coolTest.getPVob(), "INITIAL", "ALL", false, type, stream, "successful", false, "", true, recommend, tag, description, "jenkins" );
 		
 		project.setScm( scm );
 		
 		return project;
 	}
 	
	public AbstractBuild<?, ?> initiateBuild( String projectName, String uniqueTestVobName, String type, String stream, boolean recommend, boolean tag, boolean description, boolean fail ) throws Exception {
		FreeStyleProject project = setupProject( projectName, uniqueTestVobName, type, stream, recommend, tag, description );
 		
 		FreeStyleBuild build = null;
 		
 		if( fail ) {
 			project.getBuildersList().add(new TestBuilder() {
 				@Override
 			    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
 			        return false;
 			    }
 			});
 		}
 		
 		try {
 			build = project.scheduleBuild2( 0 ).get();
 		} catch( Exception e ) {
 			logger.info( "Build failed(" + (fail?"on purpose":"it should not?") + "): " + e.getMessage() );
 		}
 		
 		logger.info( "Build info for: " + build );
 		
 		logger.info( "Workspace: " + build.getWorkspace() );
 		
 		logger.info( "Logfile: " + build.getLogFile() );
 		
 		logger.info( "DESCRIPTION: " + build.getDescription() );
 		
 		logger.info( "-------------------------------------------------\nJENKINS LOG: " );
 		printList( getLog( build ) );
 		logger.info( "\n-------------------------------------------------\n" );
 		
 		return build;
 	}
 	
 	public void printList( List<String> list ) {
 		for( String l : list ) {
 			logger.debug( l );
 		}
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
 				logger.debug( "BRB: " + rb );
 				if( baseline.equals( rb ) ) {
 					return true;
 				}
 			}
 		} catch( Exception e ) {
 			logger.debug( "" );
 			ExceptionUtils.log( e, true );
 		}
 		
 		return false;
 	}
 	
 	public void makeTagType() throws CleartoolException {
 		logger.info( "Creating hyper link type TAG" );
 		HyperLink.createType( Tag.__TAG_NAME, coolTest.getPVob(), null );
 	}
 	
 	public Tag getTag( Baseline baseline, AbstractBuild<?, ?> build ) throws ClearCaseException {
 		logger.fatal( "Getting tag with \"" + build.getParent().getDisplayName() + "\" - \"" + build.getNumber() + "\"" );
 		logger.fatal( "--->" + build.getParent().getDisplayName() );
 		Tag tag = Tag.getTag( baseline, build.getParent().getDisplayName(), build.getNumber()+"", false );
 		
 		if( tag != null ) {
 			logger.info( "TAG: " + tag.stringify() );
 		} else {
 			logger.info( "TAG WAS NULL" );
 		}
 		
 		return tag;
 	}
 	
 	public void samePromotionLevel( Baseline baseline, PromotionLevel level ) throws ClearCaseException {
 		logger.info( "Current promotion level: " + baseline.getPromotionLevel( false ) );
 		baseline.load();
 		logger.info( "Future promotion level: " + baseline.getPromotionLevel( false ) );
 		assertEquals( level, baseline.getPromotionLevel( false ) );
 	}
 	
 	public void testCreatedBaseline( AbstractBuild<?, ?> build ) {
 		CCUCMBuildAction action = getBuildAction( build );
 		assertNotNull( action.getCreatedBaseline() );
 	}
 	// build.getRootDir(), "ccucmSCM.log"
 	public void testLogExistence( AbstractBuild<?, ?> build ) {
 		File scmLog = new File( build.getRootDir(), "ccucmSCM.log" );
 		File pubLog = new File( build.getRootDir(), "ccucmNOTIFIER.log" );
 		
 		assertTrue( scmLog.exists() );
 		assertTrue( pubLog.exists() );
 	}
 	
 	public void testCCUCMPolling( AbstractProject<?, ?> project ) {
 		File polldir = new File( project.getRootDir(), "ccucm-poll-logs" );
 		
 		assertTrue( polldir.exists() );
 	}
 }
