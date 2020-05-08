 package net.praqma.hudson.test;
 
 import hudson.Launcher;
 import hudson.model.*;
 import hudson.scm.ChangeLogSet;
 import hudson.scm.ChangeLogSet.Entry;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.logging.Logger;
 
 import hudson.tasks.Builder;
 import org.jvnet.hudson.test.JenkinsRule;
 import org.jvnet.hudson.test.TestBuilder;
 
 import net.praqma.clearcase.PVob;
 import net.praqma.clearcase.exceptions.ClearCaseException;
 import net.praqma.clearcase.exceptions.CleartoolException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.HyperLink;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.Tag;
 import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
 import net.praqma.clearcase.util.ExceptionUtils;
 import net.praqma.hudson.CCUCMBuildAction;
 import net.praqma.hudson.scm.CCUCMScm;
 import net.praqma.hudson.scm.ChangeLogEntryImpl;
 
 import static org.junit.Assert.*;
 
 public class CCUCMRule extends JenkinsRule {
 	
 	private static Logger logger = Logger.getLogger( CCUCMRule.class.getName() );
 
 	private CCUCMScm scm;
 
     public static class ProjectCreator {
 
         public enum Type {
             self,
             child
         }
         String name;
         Type type = Type.self;
         String component;
         String stream;
         boolean recommend = false;
         boolean tag = false;
         boolean description = false;
         boolean createBaseline = false;
         boolean forceDeliver = false;
 
         String template = "";
         PromotionLevel promotionLevel = PromotionLevel.INITIAL;
 
         Class<? extends TopLevelItem> projectClass = FreeStyleProject.class;
 
         public ProjectCreator( String name, String component, String stream ) {
             this.name = name;
             this.component = component;
             this.stream = stream;
         }
 
         public ProjectCreator setType( Type type ) {
             this.type = type;
             return this;
         }
 
         public ProjectCreator setTagged( boolean tagged ) {
             this.tag = tagged;
             return this;
         }
 
         public ProjectCreator setDescribed( boolean described ) {
             this.description = described;
             return this;
         }
 
         public ProjectCreator setRecommend( boolean recommend ) {
             this.recommend = recommend;
             return this;
         }
 
         public ProjectCreator setCreateBaseline( boolean createBaseline ) {
             this.createBaseline = createBaseline;
             return this;
         }
 
         public ProjectCreator setPromotionLevel( PromotionLevel level ) {
             this.promotionLevel = level;
 
             return this;
         }
 
         public Project getProject() throws IOException {
             logger.info( "Setting up build for self polling, recommend:" + recommend + ", tag:" + tag + ", description:" + description );
 
             System.out.println( "==== [Setting up ClearCase UCM project] ====" );
             System.out.println( " * Stream         : " + stream );
             System.out.println( " * Component      : " + component );
             System.out.println( " * Level          : " + promotionLevel );
             System.out.println( " * Polling        : " + type );
             System.out.println( " * Recommend      : " + recommend );
             System.out.println( " * Tag            : " + tag );
             System.out.println( " * Description    : " + description );
             System.out.println( " * Create baseline: " + createBaseline );
             System.out.println( " * Template       : " + template );
             System.out.println( " * Force deliver  : " + forceDeliver );
             System.out.println( "============================================" );
 
             Project project = (Project) Hudson.getInstance().createProject( projectClass, name );
 
             // boolean createBaseline, String nameTemplate, boolean forceDeliver, boolean recommend, boolean makeTag, boolean setDescription
             //CCUCMScm scm = new CCUCMScm( component, "INITIAL", "ALL", false, type, stream, "successful", createBaseline, "[project]_build_[number]", forceDeliver, recommend, tag, description, "jenkins" );
            CCUCMScm scm = new CCUCMScm( component, promotionLevel.name(), "ALL", false, type.name(), stream, "successful", createBaseline, template, forceDeliver, recommend, tag, description, "" );
             project.setScm( scm );
 
             return project;
         }
     }
 	
 	public FreeStyleProject setupProject( String projectName, String type, String component, String stream, boolean recommend, boolean tag, boolean description, boolean createBaseline ) throws Exception {
 		return setupProject( projectName, type, component, stream, recommend, tag, description, createBaseline, false );
 	}
 	
 	public FreeStyleProject setupProject( String projectName, String type, String component, String stream, boolean recommend, boolean tag, boolean description, boolean createBaseline, boolean forceDeliver ) throws Exception {
 		return setupProject( projectName, type, component, stream, recommend, tag, description, createBaseline, forceDeliver, "[project]_build_[number]" );
 	}
 	
 	public FreeStyleProject setupProject( String projectName, String type, String component, String stream, boolean recommend, boolean tag, boolean description, boolean createBaseline, boolean forceDeliver, String template ) throws Exception {
         return setupProject(projectName, type, component, stream, recommend, tag, description, createBaseline, forceDeliver, template, "INITIAL" );
     }
 
     public FreeStyleProject setupProject( String projectName, String type, String component, String stream, boolean recommend, boolean tag, boolean description, boolean createBaseline, boolean forceDeliver, String template, String promotionLevel ) throws Exception {
 	
 		logger.info( "Setting up build for self polling, recommend:" + recommend + ", tag:" + tag + ", description:" + description );
 		
 		System.out.println( "==== [Setting up ClearCase UCM project] ====" );
 		System.out.println( " * Stream         : " + stream );
 		System.out.println( " * Component      : " + component );
 		System.out.println( " * Level          : " + promotionLevel );
 		System.out.println( " * Polling        : " + type );
 		System.out.println( " * Recommend      : " + recommend );
 		System.out.println( " * Tag            : " + tag );
 		System.out.println( " * Description    : " + description );
 		System.out.println( " * Create baseline: " + createBaseline );
 		System.out.println( " * Template       : " + template );
 		System.out.println( " * Force deliver  : " + forceDeliver );
 		System.out.println( "============================================" );
 		
 		FreeStyleProject project = createFreeStyleProject( "ccucm-project-" + projectName );
 		
 		// boolean createBaseline, String nameTemplate, boolean forceDeliver, boolean recommend, boolean makeTag, boolean setDescription
 		//CCUCMScm scm = new CCUCMScm( component, "INITIAL", "ALL", false, type, stream, "successful", createBaseline, "[project]_build_[number]", forceDeliver, recommend, tag, description, "jenkins" );
 		CCUCMScm scm = new CCUCMScm( component, promotionLevel, "ALL", false, type, stream, "successful", createBaseline, template, forceDeliver, recommend, tag, description, "" );
 		this.scm = scm;
 		project.setScm( scm );
 		
 		return project;
 	}
 	
 	public CCUCMScm getCCUCM( String type, String component, String stream, String promotionLevel, boolean recommend, boolean tag, boolean description, boolean createBaseline, boolean forceDeliver, String template ) {
 		System.out.println( "==== [Setting up ClearCase UCM project] ====" );
 		System.out.println( " * Stream         : " + stream );
 		System.out.println( " * Component      : " + component );
 		System.out.println( " * Level          : " + promotionLevel );
 		System.out.println( " * Polling        : " + type );
 		System.out.println( " * Recommend      : " + recommend );
 		System.out.println( " * Tag            : " + tag );
 		System.out.println( " * Description    : " + description );
 		System.out.println( " * Create baseline: " + createBaseline );
 		System.out.println( " * Template       : " + template );
 		System.out.println( " * Force deliver  : " + forceDeliver );
 		System.out.println( "============================================" );
 		
 		CCUCMScm scm = new CCUCMScm( component, promotionLevel, "ALL", false, type, stream, "successful", createBaseline, template, forceDeliver, recommend, tag, description, "" );
 		
 		return scm;
 	}
 	
 	public FreeStyleProject createProject( String name, CCUCMScm ccucm ) throws IOException {
 		FreeStyleProject project = createFreeStyleProject( name );
 		project.setScm( ccucm );
 		
 		return project;
 	}
 	
 	public CCUCMScm getScm() {
 		return this.scm;
 	}
 	
 	public AbstractBuild<?, ?> initiateBuild( String projectName, String type, String component, String stream, boolean recommend, boolean tag, boolean description, boolean fail, boolean createBaseline ) throws Exception {
 		return initiateBuild( projectName, type, component, stream, recommend, tag, description, fail, createBaseline, false );
 	}
 	
 	public AbstractBuild<?, ?> initiateBuild( String projectName, String type, String component, String stream, boolean recommend, boolean tag, boolean description, boolean fail, boolean createBaseline, boolean forceDeliver ) throws Exception {
 		return initiateBuild( projectName, type, component, stream, recommend, tag, description, fail, createBaseline, forceDeliver, "[project]_build_[number]" );
 	}
 
 	public AbstractBuild<?, ?> initiateBuild( String projectName, String type, String component, String stream, boolean recommend, boolean tag, boolean description, boolean fail, boolean createBaseline, boolean forceDeliver, String template ) throws Exception {
         return  initiateBuild(projectName, type, component, stream, recommend, tag, description, fail, createBaseline, forceDeliver, template, "INITIAL" );
     }
 
     public AbstractBuild<?, ?> initiateBuild( String projectName, String type, String component, String stream, boolean recommend, boolean tag, boolean description, boolean fail, boolean createBaseline, boolean forceDeliver, String template, String promotionLevel ) throws Exception {
 		FreeStyleProject project = setupProject( projectName, type, component, stream, recommend, tag, description, createBaseline, forceDeliver, template, promotionLevel );
 		
 		FreeStyleBuild build = null;
 		
 		if( fail ) {
 			project.getBuildersList().add(new TestBuilder() {
 				@Override
 			    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
 			        return false;
 			    }
 			});
 		}
 		
 		return buildProject( project, fail );
 	}
 
     public static class ProjectBuilder {
         Project<?, ?> project;
 
         boolean fail = false;
 
         boolean displayOutput = true;
 
         public ProjectBuilder( Project<?, ?> project ) {
             this.project = project;
         }
 
         public ProjectBuilder failBuild( boolean cancel ) {
             this.fail = cancel;
             return this;
         }
 
         public AbstractBuild build() throws ExecutionException, InterruptedException, IOException {
             if( fail ) {
                 logger.info( "Failing " + project );
                 project.getBuildersList().add(new Failer() );
             } else {
                 /* Should remove fail task */
                 project.getBuildersList().remove( Failer.class );
             }
 
             Future<? extends Build<?, ?>> futureBuild = project.scheduleBuild2( 0, new Cause.UserCause() );
 
             AbstractBuild build = futureBuild.get();
 
             if( displayOutput ) {
                 logger.info( "Build info for: " + build );
                 logger.info( "Workspace: " + build.getWorkspace() );
                 logger.info( "Logfile: " + build.getLogFile() );
                 logger.info( "DESCRIPTION: " + build.getDescription() );
 
                 logger.info( "-------------------------------------------------\nJENKINS LOG: " );
                 logger.info( getLog( build ) );
                 logger.info( "\n-------------------------------------------------\n" );
             }
 
             return build;
         }
     }
 
     public static class Failer extends TestBuilder {
 
         @Override
         public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener ) throws InterruptedException, IOException {
             return false;
         }
     }
 	
 	public AbstractBuild<?, ?> buildProject( AbstractProject<?, ?> project, boolean fail ) throws IOException {
 		AbstractBuild<?, ?> build = null;
 		try {
 			build = project.scheduleBuild2( 0, new Cause.UserCause() ).get();
 		} catch( Exception e ) {
 			logger.info( "Build failed(" + (fail?"on purpose":"it should not?") + "): " + e.getMessage() );
 		}
 		
 		logger.info( "Build info for: " + build );
 		
 		logger.info( "Workspace: " + build.getWorkspace() );
 		
 		logger.info( "Logfile: " + build.getLogFile() );
 		
 		logger.info( "DESCRIPTION: " + build.getDescription() );
 		
 		logger.info( "-------------------------------------------------\nJENKINS LOG: " );
 		logger.info( getLog( build ) );
 		logger.info( "\n-------------------------------------------------\n" );
 		
 		return build;
 	}
 	
 	public void printList( List<String> list ) {
 		for( String l : list ) {
 			logger.fine( l );
 		}
 	}
 	
 	public CCUCMBuildAction getBuildAction( AbstractBuild<?, ?> build ) {
 		/* Check the build baseline */
 		logger.info( "Getting ccucm build action from " + build );
 		CCUCMBuildAction action = build.getAction( CCUCMBuildAction.class );
 		return action;
 	}
 	
 	public Baseline getBuildBaseline( AbstractBuild<?, ?> build ) {
 		CCUCMBuildAction action = getBuildAction( build );
 		assertNotNull( action.getBaseline() );
 		return action.getBaseline();
 	}
 	
 	public Baseline getBuildBaselineNoAssert( AbstractBuild<?, ?> build ) {
 		CCUCMBuildAction action = getBuildAction( build );
 		return action.getBaseline();
 	}
 
 	public Baseline getCreatedBaseline( AbstractBuild<?, ?> build ) {
 		CCUCMBuildAction action = getBuildAction( build );
 		return action.getCreatedBaseline();
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
 				logger.fine( "BRB: " + rb );
 				if( baseline.equals( rb ) ) {
 					return true;
 				}
 			}
 		} catch( Exception e ) {
 			logger.fine( "" );
 			ExceptionUtils.log( e, true );
 		}
 		
 		return false;
 	}
 	
 	public void makeTagType( PVob pvob) throws CleartoolException {
 		logger.info( "Creating hyper link type TAG" );
 		HyperLink.createType( Tag.__TAG_NAME, pvob, null );
 	}
 	
 	public Tag getTag( Baseline baseline, AbstractBuild<?, ?> build ) throws ClearCaseException {
 		logger.severe( "Getting tag with \"" + build.getParent().getDisplayName() + "\" - \"" + build.getNumber() + "\"" );
 		logger.severe( "--->" + build.getParent().getDisplayName() );
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
 	
 	public void testNotCreatedBaseline( AbstractBuild<?, ?> build ) {
 		CCUCMBuildAction action = getBuildAction( build );
 		assertNull( action.getCreatedBaseline() );
 	}
 		
 	
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
 	
 	public List<User> getActivityUsers( AbstractBuild<?, ?> build ) {
 		ChangeLogSet<? extends Entry> ls = build.getChangeSet();
 		
 		Object[] items = ls.getItems();
 		
 		System.out.println( "ITEMS: " + items );
 		
 		List<User> users = new ArrayList<User>();
 		
 		for( Object item : items ) {
 			try {
 				users.add( ((ChangeLogEntryImpl)item).getAuthor() );
 			} catch( Exception e ) {
 				
 			}
 		}
 		
 		return users;
 	}
 }
