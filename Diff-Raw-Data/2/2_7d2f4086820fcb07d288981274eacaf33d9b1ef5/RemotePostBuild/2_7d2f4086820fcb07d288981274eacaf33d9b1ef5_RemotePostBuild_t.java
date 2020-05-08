 package net.praqma.hudson.notifier;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.remoting.VirtualChannel;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.logging.Logger;
 import net.praqma.clearcase.exceptions.ClearCaseException;
 import net.praqma.clearcase.exceptions.TagException;
 import net.praqma.clearcase.exceptions.TagException.Type;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.Tag;
 import net.praqma.clearcase.util.ExceptionUtils;
 import net.praqma.hudson.Config;
 import net.praqma.hudson.scm.Unstable;
 
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
 	private Baseline sourcebaseline;
 	private Baseline targetbaseline;
 	private Stream sourcestream;
 	private Stream targetstream;
 	private boolean makeTag = false;
 	private boolean recommend = false;
 	private Status status;
 	private BuildListener listener;
 	private String id = "";
 	private PrintStream hudsonOut = null;
 	private Unstable unstable;
 
 
     private boolean any;
 
 	public RemotePostBuild( Result result, Status status, BuildListener listener,
 	/* Values for */
 	boolean makeTag, boolean recommended, Unstable unstable, boolean any,
 	/* Common values */
 	Baseline sourcebaseline, Baseline targetbaseline, Stream sourcestream, Stream targetstream, String displayName, String buildNumber ) {
 
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
 
         this.any = any;
 	}
     
     @Override
 	public Status invoke( File workspace, VirtualChannel channel ) throws IOException {
 		hudsonOut = listener.getLogger();
 
 		Logger logger = Logger.getLogger( RemotePostBuild.class.getName()  );
 
 		String newPLevel;
 
 		logger.info( "Starting PostBuild task" );
 
 		String noticeString = "";
 
 		/* Create the Tag object */
 		Tag tag = null;
 		if( makeTag ) {
             logger.fine( "Trying to get/make tag" );
 			try {
 				// Getting tag to set buildstatus
 				tag = sourcebaseline.getTag( this.displayName, this.buildNumber );
 				status.setTagAvailable( true );
 			} catch( Exception e ) {
 				hudsonOut.println( "Unable to get tag for " + sourcebaseline.getNormalizedName() );
 			}
 		}
 
 		/* The build was a success and the deliver did not fail */
 		if( result.equals( Result.SUCCESS ) ) {
             
 			status.setRecommended( true );
 
 			if( status.isTagAvailable() ) {
                 logger.fine( "Tag is available" );
 				if( status.isStable() ) {
 					tag.setEntry( "buildstatus", "SUCCESS" );
 				} else {
 					tag.setEntry( "buildstatus", "UNSTABLE" );
 				}
 			}
 
 			try {
                 /* Promote baseline, not any */
                 if( !any ) {
                     if( hasRemoteMastership() ) {
                         logger.fine( "Source/Target masterships differ, hasRemoteMastership = true" );
                         printPostedOutput( sourcebaseline );
                         noticeString = "*";
                     } else {
                         Project.PromotionLevel pl;
                         if( !status.isStable() && !unstable.treatSuccessful() ) {
                             /* Treat the not stable build as unsuccessful */
                             pl = sourcebaseline.demote();
                             hudsonOut.println( CCUCMNotifier.logShortPrefix + " Baseline " + sourcebaseline.getShortname() + " is " + pl.toString() + "." );
                         } else {
                             /* Treat the build as successful */
                             pl = sourcebaseline.promote();
                             hudsonOut.print( CCUCMNotifier.logShortPrefix + " Baseline " + sourcebaseline.getShortname() + " promoted to " + pl.toString() );
                             if( !status.isStable() ) {
                                 hudsonOut.println( ", even though the build is unstable." );
                             } else {
                                 hudsonOut.println( "." );
                             }
                         }
                         status.setPromotedLevel( pl );
                     }
                 } else {
                     status.setPromotedLevel( sourcebaseline.getPromotionLevel( false ) );
                 }
 
 				/* Recommend the Baseline */
 				if( recommend ) {
 					try {
 						targetstream.recommendBaseline( targetbaseline );
 						hudsonOut.println( CCUCMNotifier.logShortPrefix+" Baseline " + targetbaseline.getShortname() + " is now recommended." );
 					} catch( ClearCaseException e ) {
 						status.setStable( false );
 						status.setRecommended( false );
 						hudsonOut.println( CCUCMNotifier.logShortPrefix +" Could not recommend Baseline " + targetbaseline.getShortname() + ": " + e.getMessage() );
 						logger.warning( id + "Could not recommend baseline: " + e.getMessage() );
 					}
 				}
 			} catch( Exception e ) {
 				status.setStable( false );
 				/*
 				 * as it will not make sense to recommend if we cannot promote,
 				 * we do this:
 				 */
 				if( recommend ) {
 					status.setRecommended( false );
 					hudsonOut.println( CCUCMNotifier.logShortPrefix +" Could not promote baseline " + sourcebaseline.getShortname() + " and will not recommend " + targetbaseline.getShortname() + ". " + e.getMessage() );
 					logger.warning( id + "Could not promote baseline and will not recommend. " + e.getMessage() );
 				} else {
 					/*
 					 * As we will not recommend if we cannot promote, it's ok to
 					 * break method here
 					 */
 					hudsonOut.println( CCUCMNotifier.logShortPrefix+" Could not promote baseline " + sourcebaseline.getShortname() + ". " + e.getMessage() );
 					logger.warning( id + "Could not promote baseline. " + e.getMessage() );
 				}
 			}
 
 		} else { /* The build failed */
 			status.setRecommended( false );
 
 			hudsonOut.println( "[" + Config.nameShort + "] Build failed." );
 
 			if( status.isTagAvailable() ) {
 				tag.setEntry( "buildstatus", "FAILURE" );
 			}
 
             /* Demote baseline, not any */
            if( !any ) {
                 try {
                     if( hasRemoteMastership() ) {
                         printPostedOutput( sourcebaseline );
                         noticeString = "*";
                     } else {
                         logger.warning( id + "Demoting baseline" );
                         Project.PromotionLevel pl = sourcebaseline.demote();
                         status.setPromotedLevel( pl );
                         hudsonOut.println( CCUCMNotifier.logShortPrefix + " Baseline " + sourcebaseline.getShortname() + " is " + sourcebaseline.getPromotionLevel( true ).toString() + "." );
                     }
                 } catch( Exception e ) {
                     status.setStable( false );
                     // throw new NotifierException(
                     // "Could not demote baseline. " + e.getMessage() );
                     hudsonOut.println( CCUCMNotifier.logShortPrefix +" Could not demote baseline " + sourcebaseline.getShortname() + ". " + e.getMessage() );
                     logger.warning( id + "Could not demote baseline. " + e.getMessage() );
                 }
             } else {
                 status.setPromotedLevel( sourcebaseline.getPromotionLevel( false ) );
             }
 
 		}
 
 		Exception failBuild = null;
 		
 		/* Persist the Tag */
 		if( makeTag ) {
 			if( tag != null ) {
 				try {
 					if( hasRemoteMastership() ) {
 						hudsonOut.println( CCUCMNotifier.logShortPrefix + " Baseline not marked with tag as it has different mastership" );
 					} else {
 						tag = tag.persist();
 						hudsonOut.println( CCUCMNotifier.logShortPrefix + " Baseline now marked with tag: \n" + tag.stringify() );
 					}
 				} catch( Exception e ) {
 					hudsonOut.println( CCUCMNotifier.logShortPrefix + " Could not change tag in ClearCase. Contact ClearCase administrator to do this manually." );
 					if( e instanceof TagException && ((TagException) e).getType().equals( Type.NO_SUCH_HYPERLINK ) ) {
 						logger.severe( "Hyperlink type not found, failing build" );
 						hudsonOut.println( "Hyperlink type not found, failing build" );
 						failBuild = e;
 					} else {
 						ExceptionUtils.print( e, hudsonOut, false );
 						ExceptionUtils.log( e, true );
 					}
 				}
 			} else {
 				logger.warning( id + "Tag object was null" );
 				hudsonOut.println( CCUCMNotifier.logShortPrefix + " Tag object was null, tag not set." );
 			}
 		}
 		newPLevel = sourcebaseline.getPromotionLevel( true ).toString();
 
 		if( this.sourcestream.equals( this.targetstream ) ) {
 			status.setBuildDescr( setDisplaystatusSelf( newPLevel + noticeString, targetbaseline.getShortname() ) );
 		} else {
 			status.setBuildDescr( setDisplaystatus( sourcebaseline.getShortname(), newPLevel + noticeString, targetbaseline.getShortname(), status.getErrorMessage() ) );
 		}
 		
 		if( failBuild != null ) {
 			throw new IOException( failBuild );
 		}		
 		
 		logger.info( id + "Remote post build finished normally" );
 		return status;
 	}
 
 	private void printPostedOutput( Baseline sourcebaseline ) throws ClearCaseException {
 		hudsonOut.println( CCUCMNotifier.logShortPrefix + " Baseline " + sourcebaseline.getShortname() + " was a posted delivery, and has a different mastership." );
 		hudsonOut.println( CCUCMNotifier.logShortPrefix + " Its promotion level cannot be updated, but is left as " + sourcebaseline.getPromotionLevel( true ).toString() );
 	}
 
 	private boolean hasRemoteMastership() throws ClearCaseException {
 		return !sourcebaseline.getMastership().equals( targetbaseline.getMastership() );
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
 
 		if( plevel.equals( "REJECTED" ) ) {
 			try {
 				s += "<small>" + source + " made by " + sourcebaseline.getUser() + " was <b>" + plevel + "</b></small>";
 
 			} catch( Exception e ) {
 				hudsonOut.print( e );
 			}
 		} else {
 			s += "<small>" + source + " <b>" + plevel + "</b></small>";
 		}
 		if( status.isRecommended() && recommend ) {
 			s += "<br/><small>" + target + " <b>recommended</b></small>";
 		}
 
 		if( error != null ) {
 			s += "<br/><small>Failed with <b>" + error + "</b></small>";
 		}
 
 		return s;
 	}
 }
