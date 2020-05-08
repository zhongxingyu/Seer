 package net.praqma.hudson.scm;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.utils.BaselineDiff;
 import net.praqma.clearcase.Cool;
 import net.praqma.clearcase.ucm.entities.Activity;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.entities.Version;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
 import net.praqma.hudson.*;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.hudson.scm.EstablishResult.ResultType;
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.Logger.LogLevel;
 import net.praqma.util.debug.appenders.StreamAppender;
 import net.praqma.util.structure.Tuple;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.Pipe;
 import hudson.remoting.VirtualChannel;
 
 public class CheckoutTask implements FileCallable<EstablishResult> {
 
 	private static final long serialVersionUID = -7029877626574728221L;
 	private PrintStream hudsonOut;
 	private String jobname;
 	private SnapshotView sv;
 	private String loadModule;
 	private Baseline bl;
 	private String buildProject;
 	private Pipe pipe;
 	private Stream targetStream;
 	private String baselinefqname;
 	private BuildListener listener;
 	private Integer jobNumber;
 	private String id = "";
 
 	private String log = "";
 	
 	private boolean any = false;
 	
 	
 	private Logger logger;
 	private Set<String> subscriptions;
 
 	public CheckoutTask( BuildListener listener, String jobname, Integer jobNumber, Stream targetStream, String loadModule, String baselinefqname, String buildProject, boolean any, Pipe pipe, Set<String> subscriptions ) {
 		this.jobname = jobname;
 		this.jobNumber = jobNumber;
 		this.targetStream = targetStream;
 		this.loadModule = loadModule;
 		this.baselinefqname = baselinefqname;
 		this.buildProject = buildProject;
 		this.listener = listener;
 		this.pipe = pipe;
 		
 		this.subscriptions = subscriptions;
 		
 		this.any = any;
 
 		this.id = "[" + jobname + "::" + jobNumber + "]";
 	}
 
 	@Override
 	public EstablishResult invoke( File workspace, VirtualChannel channel ) throws IOException {
 
 		hudsonOut = listener.getLogger();
 		
     	logger = Logger.getLogger();
 
 		UCM.setContext( UCM.ContextType.CLEARTOOL );
         
     	StreamAppender app = null;
     	if( pipe != null ) {
 	    	PrintStream toMaster = new PrintStream( pipe.getOut() );	    	
 	    	app = new StreamAppender( toMaster );
 	    	Logger.addAppender( app );
 	    	app.setSubscriptions( subscriptions );
 	    	app.setMinimumLevel( LogLevel.DEBUG );
     	}
 
 		logger.info( "Starting CheckoutTask" );
 
 		String diff = "";
 		String viewtag = makeViewtag();
 		
 		EstablishResult er = new EstablishResult();
 		er.setResultType( ResultType.SUCCESS );
 		ClearCaseChangeset changeset = new ClearCaseChangeset();
 
 		try {
 			hudsonOut.println("1");
 			Stream devstream = getDeveloperStream( "stream:" + viewtag, Config.getPvob( targetStream ) );
			hudsonOut.println("2: " + devstream);
			devstream.load();
 			Baseline foundation = devstream.getFoundationBaseline();
 			hudsonOut.println("3a");
 			logger.error( "----->" + foundation );
 			logger.error( "----->" + foundation.getStream() );
 			Stream s = foundation.getStream();
 			logger.error( "<-----" );
 			hudsonOut.println("3b");
 			if( !s.equals( targetStream ) ) {
 				hudsonOut.println( "[" + Config.nameShort + "] The foundation baseline " + foundation.getShortname() + " does not match the stream " + targetStream.getShortname() + ". Changelog will probably be bogus." );
 			}			
 			hudsonOut.println("4");
 			makeWorkspace( workspace, viewtag );
 			hudsonOut.println("5");
 			List<Activity> bldiff = null;
 			if( any) {
 				bldiff = Version.getBaselineDiff( foundation, bl, true, sv.getViewRoot() );
 			} else {
 				bldiff = bl.getDifferences( sv );
 			}
 			//List<Activity> bldiff = Version.getBaselineDiff( bl, null, true, sv.getViewRoot() );
 			diff = Util.createChangelog( bldiff, bl );
 			hudsonOut.print( "[" + Config.nameShort + "] Found " + bldiff.size() + " activit" + ( bldiff.size() == 1 ? "y" : "ies" ) + ". " );
 			int c = 0;
 			for( Activity a : bldiff ) {
 				c += a.changeset.versions.size();
 				for( Version version : a.changeset.versions ) {
 					changeset.addChange( version.getFullyQualifiedName(), version.getUser() );
 				}
 			}
 			hudsonOut.println( c + " version" + ( c == 1 ? "" : "s" ) + " involved" );
 			
 			logger.info( "CheckoutTask finished normally" );
 			
 		} catch (net.praqma.hudson.exception.ScmException e) {
 			logger.warning( id + "SCM exception: " + e.getMessage() );
 			hudsonOut.println( "[" + Config.nameShort + "] SCM exception: " + e.getMessage() );
 			er.setResultType( ResultType.INITIALIZE_WORKSPACE_ERROR );
 		} catch (UCMException e) {
 			logger.debug( id + "Could not get changes. " + e.getMessage() );
 			logger.info( e );
 			hudsonOut.println( e.stdout );
 			hudsonOut.println( "[" + Config.nameShort + "] Could not get changes. " + e.getMessage() );
 		}
 
 		er.setLog( log );
 		er.setMessage( diff );
 		er.setViewtag( viewtag );
 		er.setChangeset( changeset );
 		Logger.removeAppender( app );
 		return er;
 	}
 	
 	private String makeViewtag() {
 		String newJobName = jobname.replaceAll("\\s", "_");
 		String viewtag = "CCUCM_" + newJobName + "_" + System.getenv("COMPUTERNAME");
 		
 		return viewtag;
 	}
 
 	private void makeWorkspace( File workspace, String viewtag ) throws ScmException {
 		// We know we have a stream (st), because it is set in
 		// baselinesToBuild()
 		try {
 			bl = Baseline.getBaseline( baselinefqname );
 		} catch (UCMException e) {
 			throw new ScmException( "Could not get stream. Job might run on machine with different region. " + e.getMessage() );
 		}
 		if( workspace != null ) {
 			logger.debug( id + "workspace: " + workspace.getAbsolutePath() );
 		} else {
 			logger.debug( id + "workspace must be null???" );
 		}
 
 		File viewroot = new File( workspace, "view" );
 		
 		Stream devstream = null;
 
 		devstream = getDeveloperStream( "stream:" + viewtag, Config.getPvob( targetStream ) );
 
 		sv = Util.makeView( devstream, workspace, listener, loadModule, viewroot, viewtag, false );
 		
 
 		// Now we have to rebase - if a rebase is in progress, the
 		// old one must be stopped and the new started instead
 		if( devstream.isRebaseInProgress() ) {
 			hudsonOut.print( "[" + Config.nameShort + "] Cancelling previous rebase." );
 			devstream.cancelRebase();
 			hudsonOut.println( " DONE" );
 		}
 		// The last boolean, complete, must always be true from CCUCM
 		// as we are always working on a read-only stream according
 		// to LAK
 		hudsonOut.print( "[" + Config.nameShort + "] Rebasing development stream (" + devstream.getShortname() + ") against parent stream (" + targetStream.getShortname() + ")" );
 		devstream.rebase( sv, bl, true );
 		hudsonOut.println( " DONE" );
 		
 		try {
             hudsonOut.println("[" + Config.nameShort + "] Updating view using " + loadModule.toLowerCase() + " modules");
             sv.Update(true, true, true, false, COMP.valueOf(loadModule.toUpperCase()), null);
         } catch (UCMException e) {
             if (e.stdout != null) {
                 hudsonOut.println(e.stdout);
             }
             throw new ScmException("Could not update snapshot view. " + e.getMessage());
         }
 	}
 
 	private Stream getDeveloperStream( String streamname, String pvob ) throws ScmException {
 		Stream devstream = null;
 
 		try {
 			if( Stream.streamExists( streamname + pvob ) ) {
 				devstream = Stream.getStream( streamname + pvob, false );
 			} else {
 				if( buildProject.equals( "" ) ) {
 					buildProject = null;
 				}
 				devstream = Stream.create( Config.getIntegrationStream( bl, buildProject ), streamname + pvob, true, bl );
 			}
 		}
 		/*
 		 * This tries to handle the issue where the project hudson is not
 		 * available
 		 */
 		catch (ScmException se) {
 			throw se;
 
 		} catch (Exception e) {
 			throw new ScmException( "Could not get stream: " + e.getMessage() );
 		}
 
 		return devstream;
 	}
 
 	public SnapshotView getSnapshotView() {
 		return sv;
 	}
 
 	public BaselineDiff getBaselineDiffs() throws UCMException {
 		return bl.getDifferences( sv );
 	}
 
 }
