 package net.praqma.hudson.notifier;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.remoting.Pipe;
 import hudson.remoting.VirtualChannel;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.PipedOutputStream;
 import java.io.PrintStream;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.Cool;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.Tag;
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.hudson.Config;
 import net.praqma.hudson.scm.Unstable;
 import net.praqma.util.debug.PraqmaLogger;
 import net.praqma.util.debug.PraqmaLogger.Logger;
 
 /**
  * 
  * @author wolfgang
  * 
  */
 class RemotePostBuild implements FileCallable<Status> {
 	private static final long serialVersionUID = 1L;
 	private String displayName;
 	private String buildNumber;
 
 	private Result result;
 
 	private String sourcebaseline;
 	private String targetbaseline;
 	private String sourcestream;
 	private String targetstream;
 
 	private boolean makeTag = false;
 	private boolean recommend = false;
 	private Status status;
 	private BuildListener listener;
 
 	private String id = "";
 
 	private Logger logger = null;
 	private PrintStream hudsonOut = null;
 	private Unstable unstable;
 
 	private Pipe pipe = null;
 	private BufferedWriter bw = null;
 	private PipedOutputStream pout = null;
 
 	public RemotePostBuild( Result result, Status status, BuildListener listener,
 							/* Values for */
 							boolean makeTag, boolean recommended, Unstable unstable,
 							/* Common values */
 							String sourcebaseline, String targetbaseline, String sourcestream, String targetstream, String displayName, String buildNumber, Logger logger/*
 																												 * ,
 																												 * PipedOutputStream
 																												 * pout
 																												 */, Pipe pipe ) {
 		this.displayName = displayName;
 		this.buildNumber = buildNumber;
 
 		this.id = "[" + displayName + "::" + buildNumber + "]";
 
 		this.sourcebaseline = sourcebaseline;
 		this.targetbaseline = targetbaseline;
 		this.sourcestream = sourcestream;
 		this.targetstream = targetstream;
 
 		this.unstable = unstable;
 		
 		this.result = result;
 
 		this.makeTag = makeTag;
 		this.recommend = recommended;
 
 		this.status = status;
 		this.listener = listener;
 
 		this.logger = logger;
 		this.pipe = pipe;
 		/*
 		 * this.pout = pout;
 		 */
 	}
 
 	public Status invoke( File workspace, VirtualChannel channel ) throws IOException {
 		PraqmaLogger.getLogger( logger );
 		/* Make sure that the local log file is not written */
 		logger.setLocalLog( null );
 		Cool.setLogger( logger );
 		hudsonOut = listener.getLogger();
 		UCM.setContext( UCM.ContextType.CLEARTOOL );
 
 		String newPLevel = "";
 
 		status.addToLog( logger.info( "Starting PostBuild task" ) );
 
 		/* Create the source baseline object */
 		Baseline sourcebaseline = null;
 		try {
 			sourcebaseline = UCMEntity.getBaseline( this.sourcebaseline );
 		} catch (UCMException e) {
 			status.addToLog( logger.debug( id + "could not create source Baseline object:" + e.getMessage() ) );
 			throw new IOException( "[" + Config.nameShort + "] Could not create source Baseline object: " + e.getMessage() );
 		}
 		
 		/* Create the target baseline object */
 		Baseline targetbaseline = null;
 		try {
 			targetbaseline = UCMEntity.getBaseline( this.targetbaseline );
 		} catch (UCMException e) {
 			status.addToLog( logger.debug( id + "could not create target Baseline object:" + e.getMessage() ) );
 			throw new IOException( "[" + Config.nameShort + "] Could not create target Baseline object: " + e.getMessage() );
 		}
 
 		/* Create the source stream object */
 		Stream sourcestream = null;
 		try {
 			sourcestream = UCMEntity.getStream( this.sourcestream );
 		} catch (UCMException e) {
 			status.addToLog( logger.debug( id + "could not create source Stream object:" + e.getMessage() ) );
 			throw new IOException( "[" + Config.nameShort + "] Could not create source Stream object: " + e.getMessage() );
 		}
 		
 		/* Create the target stream object */
 		Stream targetstream = null;
 		try {
 			targetstream = UCMEntity.getStream( this.targetstream );
 		} catch (UCMException e) {
 			status.addToLog( logger.debug( id + "could not create target Stream object:" + e.getMessage() ) );
 			throw new IOException( "[" + Config.nameShort + "] Could not create target Stream object: " + e.getMessage() );
 		}
 
 		status.addToLog( logger.warning( id + "Streams and baselines created" ) );
 
 		/* Create the Tag object */
 		Tag tag = null;
 		if( makeTag ) {
 			try {
 				// Getting tag to set buildstatus
 				tag = sourcebaseline.getTag( this.displayName, this.buildNumber );
 				status.setTagAvailable( true );
 			} catch (UCMException e) {
 				hudsonOut.println( "[" + Config.nameShort + "] Could not get Tag: " + e.getMessage() );
 				status.addToLog( logger.warning( id + "Could not get Tag: " + e.getMessage() ) );
 			}
 		}
 
 		/* The build was a success and the deliver did not fail */
 		if( result.equals( Result.SUCCESS ) && status.isStable() ) {
 			if( status.isTagAvailable() ) {
 				tag.setEntry( "buildstatus", "SUCCESS" );
 			}
 
 			try {
 				Project.Plevel pl = sourcebaseline.promote();
 				status.setPromotedLevel( pl );
 				status.setPLevel( true );
 				hudsonOut.println( "[" + Config.nameShort + "] Baseline " + sourcebaseline.getShortname() + " promoted to " + sourcebaseline.getPromotionLevel( true ).toString() + "." );
 			} catch (UCMException e) {
 				status.setStable( false );
 				/*
 				 * as it will not make sense to recommend if we cannot
 				 * promote, we do this:
 				 */
 				if( recommend ) {
 					status.setRecommended( false );
 					hudsonOut.println( "[" + Config.nameShort + "] Could not promote baseline " + sourcebaseline.getShortname() + " and will not recommend " + targetbaseline.getShortname() + ". " + e.getMessage() );
 					status.addToLog( logger.warning( id + "Could not promote baseline and will not recommend. " + e.getMessage() ) );
 				} else {
 					/*
 					 * As we will not recommend if we cannot promote, it's
 					 * ok to break method here
 					 */
 					hudsonOut.println( "[" + Config.nameShort + "] Could not promote baseline " + sourcebaseline.getShortname() + ". " + e.getMessage() );
 					status.addToLog( logger.warning( id + "Could not promote baseline. " + e.getMessage() ) );
 				}
 			}
 			
 			/* Recommend the Baseline */
 			if( recommend ) {
 				try {
 					if( status.isPLevel() ) {
 						targetstream.recommendBaseline( targetbaseline );
 						hudsonOut.println( "[" + Config.nameShort + "] Baseline " + targetbaseline.getShortname() + " is now recommended." );
 					}
 				} catch ( UCMException e ) {
 					status.setStable( false );
 					status.setRecommended( false );
 					hudsonOut.println( "[" + Config.nameShort + "] Could not recommend Baseline " + targetbaseline.getShortname() + ": " + e.getMessage() );
 					status.addToLog( logger.warning( id + "Could not recommend baseline: " + e.getMessage() ) );
 				}
 			}
 		}
 		/* The build failed or the deliver failed */
 		else {
 			/* Do not set as recommended at all */
 			if( recommend ) {
 				status.setRecommended( false );
 			}
 
 			/* The build failed */
 			if( result.equals( Result.FAILURE ) ) {
 				hudsonOut.println( "[" + Config.nameShort + "] Build failed." );
 
 				if( status.isTagAvailable() ) {
 					tag.setEntry( "buildstatus", "FAILURE" );
 				}
 
 				try {
 					status.addToLog( logger.warning( id + "Demoting baseline" ) );
 					Project.Plevel pl = sourcebaseline.demote();
 					status.setPromotedLevel( pl );
 					status.setPLevel( true );
 					hudsonOut.println( "[" + Config.nameShort + "] Baseline " + sourcebaseline.getShortname() + " is " + sourcebaseline.getPromotionLevel( true ).toString() + "." );
 				} catch (Exception e) {
 					status.setStable( false );
 					// throw new NotifierException(
 					// "Could not demote baseline. " + e.getMessage() );
 					hudsonOut.println( "[" + Config.nameShort + "] Could not demote baseline " + sourcebaseline.getShortname() + ". " + e.getMessage() );
 					status.addToLog( logger.warning( id + "Could not demote baseline. " + e.getMessage() ) );
 				}
 
 			}
 			/*
 			 * The build is unstable, or something in the middle.... TODO Maybe
 			 * not else if
 			 */
 			else if( !result.equals( Result.FAILURE ) ) {
 				if( status.isTagAvailable() ) {
 					tag.setEntry( "buildstatus", "UNSTABLE" );
 				}
 
 				try {
 					Project.Plevel pl = Project.Plevel.INITIAL;
 
 					if( unstable.treatSuccessful() ) {
 						pl = sourcebaseline.promote();
 						hudsonOut.println( "[" + Config.nameShort + "] Baseline " + sourcebaseline.getShortname() + " is promoted, even though the build is unstable." );
 					} else {
 						pl = sourcebaseline.demote();
 					}
 					status.setPromotedLevel( pl );
 					status.setPLevel( true );
 					hudsonOut.println( "[" + Config.nameShort + "] Baseline " + sourcebaseline.getShortname() + " is " + sourcebaseline.getPromotionLevel( true ).toString() + "." );
 				} catch (Exception e) {
 					status.setStable( false );
 					hudsonOut.println( "[" + Config.nameShort + "] Could not demote baseline " + sourcebaseline.getShortname() + ". " + e.getMessage() );
 					status.addToLog( logger.warning( id + "Could not demote baseline. " + e.getMessage() ) );
 				}
 
 				/* Recommend the Baseline */
 				if( recommend ) {
 					try {
 						if( status.isPLevel() ) {
 							targetstream.recommendBaseline( targetbaseline );
 							hudsonOut.println( "[" + Config.nameShort + "] Baseline " + targetbaseline.getShortname() + " is now recommended." );
 						}
 					} catch (Exception e) {
 						status.setStable( false );
 						status.setRecommended( false );
 						hudsonOut.println( "[" + Config.nameShort + "] Could not recommend baseline " + targetbaseline.getShortname() + ". Reason: " + e.getMessage() );
 						status.addToLog( logger.warning( id + "Could not recommend baseline. Reason: " + e.getMessage() ) );
 					}
 				}
 
 			}
 			/* Result not handled by CCUCM */
 			else {
 				tag.setEntry( "buildstatus", result.toString() );
 				status.addToLog( logger.log( id + "Buildstatus (Result) was " + result + ". Not handled by plugin." ) );
 				hudsonOut.println( "[" + Config.nameShort + "] Baselines not changed. Buildstatus: " + result );
 			}
 		}
 
 		/* Persist the Tag */
 		if( makeTag ) {
 			if( tag != null ) {
 				try {
 					tag = tag.persist();
 					hudsonOut.println( "[" + Config.nameShort + "] Baseline now marked with tag: \n" + tag.stringify() );
 				} catch (Exception e) {
 					hudsonOut.println( "[" + Config.nameShort + "] Could not change tag in ClearCase. Contact ClearCase administrator to do this manually." );
 				}
 			} else {
 				logger.warning( id + "Tag object was null" );
 				hudsonOut.println( "[" + Config.nameShort + "] Tag object was null, tag not set." );
 			}
 		}
 
 		try {
 			newPLevel = sourcebaseline.getPromotionLevel( true ).toString();
 		} catch (UCMException e) {
 			logger.log( id + " Could not get promotionlevel." );
 			hudsonOut.println( "[" + Config.nameShort + "] Could not get promotion level." );
 		}
 
 		if( this.sourcestream.equals( this.targetstream )) {
 			status.setBuildDescr( setDisplaystatusSelf( newPLevel, targetbaseline.getShortname() ) );
 		} else {
 			status.setBuildDescr( setDisplaystatus( sourcebaseline.getShortname(), newPLevel, targetbaseline.getShortname(), status.getErrorMessage() ) );
 		}
 
 		status.addToLog( logger.warning( id + "Remote post build finished normally" ) );
 
 		/*
 		 * if( out != null ) { out.close(); }
 		 */
 
 		return status;
 	}
 
 	private String setDisplaystatusSelf( String plevel, String fqn ) {
 		String s = "";
 
 		// Get shortname
		s += "<small>" + fqn + " <b>" + plevel + "</b></small>";
 
 		if( recommend ) {
 			if( status.isRecommended() ) {
 				s += "<br/><B><small>Recommended</small></B>";
 			} else {
 				s += "<br/><B><small>Could not recommend</small></B>";
 			}
 		}
 		return s;
 	}
 	
 	private String setDisplaystatus( String source, String plevel, String target, String error ) {
 		String s = "";
 
 		s += "<small>" + source + " <b>" + plevel + "</b></small>";
 
		if( status.isRecommended() && recommend ) {
 			s += "<br/><small>" + target + " <b>recommended</b></small>";
 		}
 		
 		if( error != null ) {
 			s += "<br/><small>Failed with <b>" + error + "</b></small>";
 		}
 
 		return s;
 	}
 
 }
