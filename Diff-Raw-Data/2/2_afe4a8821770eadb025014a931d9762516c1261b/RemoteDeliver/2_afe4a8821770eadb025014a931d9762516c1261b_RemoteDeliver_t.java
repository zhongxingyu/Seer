 package net.praqma.hudson.remoting;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.VirtualChannel;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.praqma.clearcase.Deliver;
 import net.praqma.clearcase.exceptions.ClearCaseException;
 import net.praqma.clearcase.exceptions.CleartoolException;
 import net.praqma.clearcase.exceptions.DeliverException;
 import net.praqma.clearcase.ucm.entities.Activity;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.Version;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.hudson.Config;
 import net.praqma.hudson.Util;
 import net.praqma.hudson.exception.DeliverNotCancelledException;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.hudson.scm.ClearCaseChangeset;
 
 /**
  * 
  * @author wolfgang
  * 
  */
 public class RemoteDeliver implements FileCallable<EstablishResult> {
 
 	private static final long serialVersionUID = 1L;
 	
 	private Logger logger;
 	
 	private String jobName;
 	private String baseline;
 	private String destinationstream;
 	private BuildListener listener;
 	private String id = "";
 	private SnapshotView snapview;
 
 	/*
 	 * private boolean apply4level; private String alternateTarget; private
 	 * String baselineName;
 	 */
 	private String loadModule;
 	private PrintStream out = null;
 	private String viewtag = "";
 	private boolean forceDeliver;
 	private PrintStream pstream;
 	private File workspace;
 
 	public RemoteDeliver( String destinationstream, BuildListener listener, String loadModule, String baseline, String jobName, boolean forceDeliver ) {
 		this.jobName = jobName;
 
 		this.baseline = baseline;
 		this.destinationstream = destinationstream;
 
 		this.listener = listener;
 		this.loadModule = loadModule;
 		
 		this.forceDeliver = forceDeliver;
 	}
 
 	public EstablishResult invoke( File workspace, VirtualChannel channel ) throws IOException {
 
 		out = listener.getLogger();
 		
 		logger = Logger.getLogger( RemoteDeliver.class.getName() );
 
 		logger.fine( "Starting remote deliver" );
 
 		this.workspace = workspace;
 
 		/* Create the baseline object */
 		Baseline baseline = null;
 		try {
 			baseline = Baseline.get( this.baseline ).load();
 		} catch( Exception e ) {
 			throw new IOException( "Could not create Baseline object: " + e.getMessage(), e );
 		}
 		
 		logger.fine( baseline + " created" );
 
 		/* Create the development stream object */
 		/* Append vob to dev stream */
 
 		Stream destinationStream = null;
 		try {
 			destinationStream = Stream.get( this.destinationstream ).load();
 		} catch( Exception e ) {
 			throw new IOException( "Could not create destination Stream object: " + e.getMessage(), e );
 		}
 		
 		logger.fine( destinationStream + " created" );
 
 		/* Make deliver view */
 		try {
 			snapview = makeDeliverView( destinationStream, workspace );
 		} catch( Exception e ) {
 			throw new IOException( "Could not create deliver view: " + e.getMessage(), e );
 		}
 		
 		logger.fine( "View: " + workspace );
 
 		String diff = "";
 		ClearCaseChangeset changeset = new ClearCaseChangeset();
 
 		try {
 			List<Activity> bldiff = Version.getBaselineDiff( destinationStream, baseline, true, snapview.getViewRoot() );
 			out.print( "[" + Config.nameShort + "] Found " + bldiff.size() + " activit" + ( bldiff.size() == 1 ? "y" : "ies" ) + ". " );
 
 			int c = 0;
 			for( Activity a : bldiff ) {
 				c += a.changeset.versions.size();
 				for( Version version : a.changeset.versions ) {
 					changeset.addChange( version.getFullyQualifiedName(), version.getUser() );
 				}
 			}
 			out.println( c + " version" + ( c == 1 ? "" : "s" ) + " involved" );
 			diff = Util.createChangelog( bldiff, baseline );
 		} catch( Exception e1 ) {
 			out.println( "[" + Config.nameShort + "] Unable to create change log: " + e1.getMessage() );
 		}
 		
 		logger.fine( "Changeset created" );
 
 		EstablishResult er = new EstablishResult( viewtag );
 		er.setView( snapview );
 		er.setMessage( diff );
 		er.setChangeset( changeset );
 
 		/* Make the deliver. Inline manipulation of er */
 		try {
 			deliver( baseline, destinationStream, forceDeliver, 2 );
 		} catch( Exception e ) {
 			throw new IOException( e );
 		}
 
 		/* End of deliver */
 		return er;
 	}
 
 	private void deliver( Baseline baseline, Stream dstream, boolean forceDeliver, int triesLeft ) throws IOException, DeliverNotCancelledException, ClearCaseException {
 		logger.config( "Delivering " + baseline.getShortname() + " to " + dstream.getShortname() + ". Tries left: " + triesLeft );
 		if( triesLeft < 1 ) {
 			out.println( "[" + Config.nameShort + "] Unable to deliver, giving up." );
 			throw new DeliverNotCancelledException( "Unable to force cancel deliver" );
 		}
 
 		Deliver deliver = null;
 		try {
 			out.println( "[" + Config.nameShort + "] Starting deliver(tries left: " + triesLeft + ")" );
 			deliver = new Deliver( baseline, baseline.getStream(), dstream, snapview.getViewRoot(), snapview.getViewtag() );
 			deliver.deliver( true, false, true, false );
 
 		} catch( DeliverException e ) {
 			logger.log( Level.FINE, "Failed to deliver", e );
 			
 			if( e.getType().equals( DeliverException.Type.DELIVER_IN_PROGRESS ) ) {
 				out.println( "[" + Config.nameShort + "] Deliver already in progress" );
 
 				if( forceDeliver ) {
 	
 					/**
 					 * rollback deliver.. *******A DELIVER OPERATION IS ALREADY IN
 					 * PROGRESS. Details about the delivery:
 					 * 
 					 * <b Deliver operation in progress on stream
 					 * "stream:pds316_deliver_test@\PDS_PVOB"/> Started by "PDS316"
 					 * on "2011-09-28T11:23:53+02:00" Using integration activity
 					 * "deliver.pds316_deliver_test.20110928.112353". <bUsing view
 					 * "pds316_deliver_test_int" />. Baselines will be delivered to
 					 * the default target stream "stream:deliver_test_int@\PDS_PVOB"
 					 * in project "project:deliver_test@\PDS_PVOB".
 					 * 
 					 * Baselines to be delivered:
 					 * 
 					 * *******Please try again later.
 					 */
 	
 					String msg = e.getMessage();
 					//String msg = deliver.getStatus();
 					String stream = null;
 					String oldViewtag = null;
 	
 					out.println( "[" + Config.nameShort + "] Forcing this deliver." );
 					out.println( "MESSAGE IS " + e.getMessage() );
 	
 					Pattern STREAM_PATTERN = Pattern.compile( "Deliver operation .* on stream \\\"(.*)\\\"", Pattern.MULTILINE );
 					Pattern TAG_PATTERN = Pattern.compile( "Using view \\\"(.*)\\\".", Pattern.MULTILINE );
 	
 					Matcher mSTREAM = STREAM_PATTERN.matcher( msg );
 					if( mSTREAM.find() ) {
 						stream = mSTREAM.group( 1 );
 					}
 	
 					Matcher mTAG = TAG_PATTERN.matcher( msg );
 					if( mTAG.find() ) {
 						oldViewtag = mTAG.group( 1 );
 					}
 					
 					/* Validate arguments */
 					if( oldViewtag == null || stream == null ) {
 						logger.fine( "Unable to get arguments for rollback, trying to get status" );
 						//String status = deliver.getStatus();
 						String status = Deliver.getStatus( dstream );
 						logger.fine( "Deliver rollback status: " + status );
 						/* Get stream */
 						mSTREAM = STREAM_PATTERN.matcher( status );
 						if( mSTREAM.find() ) {
 							stream = mSTREAM.group( 1 );
 						} else {
 							throw new DeliverNotCancelledException( "Could not force deliver cancel, no stream found" );
 						}
 						
 						/* Get old view tag */
 						mTAG = TAG_PATTERN.matcher( status );
 						if( mTAG.find() ) {
 							oldViewtag = mTAG.group( 1 );
 						} else {
 							throw new DeliverNotCancelledException( "Could not force deliver cancel, no view tag found" );
 						}
 					}
 	
 					File newView = null;
 					if( oldViewtag == null ) {
 						newView = snapview.getViewRoot();
 					} else {
						newView = new File( workspace, "rm_delv_view" );
 					}
 	
 					try {
 						// rolling back the previous deliver operation
 						Stream ostream = Stream.get( stream, baseline.getPVob() ).load(); //.deliverRollBack( oldViewtag, newView );
 						Deliver.rollBack( oldViewtag, ostream, newView );
 					} catch( ClearCaseException ex ) {
 						throw ex;
 					}
 	
 					// Recursive method call of INVOKE(...);
 					logger.config( "Trying to deliver again..." );
 					deliver( baseline, dstream, forceDeliver, triesLeft - 1 );
 					
 				/* Not forcing this deliver */
 				} else {
 					throw e;
 				}
 				
 			/* Another deliver is not in progress */
 			} else {
 				throw e;
 			}
 		} catch( CleartoolException e ) {
 			logger.warning( "Unable to get status from stream: " + e.getMessage() );
 			throw new IOException( e );
 		} catch( Exception e ) {
 			logger.warning( "Unable deliver: " + e.getMessage() );
 			
 			throw new IOException( e );
 		} 
 	}
 
 	private SnapshotView makeDeliverView( Stream stream, File workspace ) throws ScmException {
 		/* Replace evil characters with less evil characters */
 		String newJobName = jobName.replaceAll( "\\s", "_" );
 
 		viewtag = "CCUCM_" + newJobName + "_" + System.getenv( "COMPUTERNAME" ) + "_" + stream.getShortname();
 
 		File viewroot = new File( workspace, "view" );
 
 		return Util.makeView( stream, workspace, listener, loadModule, viewroot, viewtag );
 	}
 
 	public SnapshotView getSnapShotView() {
 		return this.snapview;
 	}
 }
