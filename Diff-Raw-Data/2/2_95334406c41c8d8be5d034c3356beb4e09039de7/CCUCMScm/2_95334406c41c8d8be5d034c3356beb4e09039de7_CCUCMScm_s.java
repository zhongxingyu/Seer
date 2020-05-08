 package net.praqma.hudson.scm;
 
 import hudson.AbortException;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.TaskListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Hudson;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.PollingResult;
 import hudson.scm.SCMDescriptor;
 import hudson.scm.SCMRevisionState;
 import hudson.scm.SCM;
 import hudson.tasks.Publisher;
 import hudson.util.FormValidation;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.praqma.clearcase.exceptions.ClearCaseException;
 import net.praqma.clearcase.exceptions.DeliverException;
 import net.praqma.clearcase.exceptions.DeliverException.Type;
 import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCMEntity.LabelStatus;
 import net.praqma.clearcase.util.ExceptionUtils;
 import net.praqma.hudson.CCUCMBuildAction;
 import net.praqma.hudson.Config;
 import net.praqma.hudson.Util;
 import net.praqma.hudson.exception.CCUCMException;
 import net.praqma.hudson.exception.DeliverNotCancelledException;
 import net.praqma.hudson.exception.TemplateException;
 import net.praqma.hudson.nametemplates.NameTemplate;
 import net.praqma.hudson.notifier.CCUCMNotifier;
 import net.praqma.hudson.remoting.*;
 import net.praqma.hudson.scm.Polling.PollingType;
 import net.praqma.util.execute.AbnormalProcessTerminationException;
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.export.Exported;
 
 /**
  * is responsible for everything regarding Hudsons connection to ClearCase
  * pre-build. This class defines all the files required by the user. The
  * information can be entered on the config page. 
  */
 public class CCUCMScm extends SCM {
 
     private static Logger logger = Logger.getLogger( CCUCMScm.class.getName() );
 	
 	/* Currently only for testing */
 	private Boolean multisitePolling;
 
 	private Project.PromotionLevel plevel;
 	private String loadModule;
 	private String component;
 	private String stream;
 	private String bl;
 	private StringBuffer pollMsgs = new StringBuffer();
 	private Stream integrationstream;
 	private boolean doPostBuild = true;
 	private String buildProject;
 	private String jobName = "";
 	private Integer jobNumber;
 	private String id = "";
 
 	private boolean forceDeliver;
 
 	/* Old notifier fields */
 	private boolean recommend;
 	private boolean makeTag;
 	private boolean setDescription;
 	private Unstable treatUnstable;
 	private boolean createBaseline;
 	private String nameTemplate;
 
 	private Polling polling;
 	private String viewtag = "";
 	private Baseline lastBaseline;
 
 	private static DateFormat dateFormatter = new SimpleDateFormat( "yyyyMMdd" );
 
 	/**
 	 * Default constructor, mainly used for unit tests.
 	 */
 	public CCUCMScm() {
 	}
 
 	@DataBoundConstructor
 	public CCUCMScm( String component, String levelToPoll, String loadModule, boolean newest, String polling, String stream, String treatUnstable, 
 			         boolean createBaseline, String nameTemplate, boolean forceDeliver, boolean recommend, boolean makeTag, boolean setDescription, String buildProject ) {
 
 		this.component = component;
 		this.loadModule = loadModule;
 		this.stream = stream;
 		this.buildProject = buildProject;
 
 		this.polling = new Polling( polling );
 		this.treatUnstable = new Unstable( treatUnstable );
 
 		this.createBaseline = createBaseline;
 		this.nameTemplate = nameTemplate;
 
 		this.forceDeliver = forceDeliver;
 		this.recommend = recommend;
 		this.makeTag = makeTag;
 		this.setDescription = setDescription;
 		this.plevel = Util.getLevel( levelToPoll );
 	}
 
 	@Override
 	public boolean checkout( AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile ) throws IOException, InterruptedException {
 		/* Prepare job variables */
 
 		jobName = build.getParent().getDisplayName().replace( ' ', '_' );
 		jobNumber = build.getNumber();
 		this.id = "[" + jobName + "::" + jobNumber + "]";
 
 		PrintStream out = listener.getLogger();
 
         /* Printing short description of build */
 		String version = Hudson.getInstance().getPlugin( "clearcase-ucm-plugin" ).getWrapper().getVersion();
 		out.println( "[" + Config.nameShort + "] ClearCase UCM Plugin version " + version );
 		out.println( "[" + Config.nameShort + "] Allow for slave polling: " + this.getSlavePolling() );
 		out.println( "[" + Config.nameShort + "] Poll for posted deliveries: " + this.getMultisitePolling() );
 		out.println( "[" + Config.nameShort + "] Forcing deliver: " + forceDeliver );
 
 		logger.info( id + "CCUCMSCM checkout v. " + version );
 		
 		/* Check for ClearCase on remote */
 		try {
 			workspace.act( new RemoteClearCaseCheck() );
 		} catch( AbnormalProcessTerminationException e ) {
 			ExceptionUtils.print( e, out, true );
 			build.setDescription( e.getMessage() );
 			throw new AbortException( e.getMessage() );
 		}
 
 		doPostBuild = true;
 
         /* Make build action */
         /* Store the configuration */
         CCUCMBuildAction action = null;
         try {
             action = getBuildAction();
         } catch( UnableToInitializeEntityException e ) {
             Util.println( out, e );
             throw new AbortException( e.getMessage() );
         }
 
         action.setBuild( build );
         build.addAction( action );
 
         out.println( "LISTENER IS " + listener );
         //action.setListener( listener );
 
 		/* Determining the user has parameterized a Baseline */
 		String baselineInput = getBaselineValue( build );
 
 		/* The special Baseline parameter case */
 		if( build.getBuildVariables().get( baselineInput ) != null ) {
 			logger.fine( "Baseline parameter: " + baselineInput );
 			action.setPolling( new Polling( PollingType.none ) );
             polling = action.getPolling();
             try {
                 resolveBaselineInput( build, baselineInput, action, listener );
             } catch( Exception e ) {
                 logger.log( Level.WARNING, "Resolving baseline input failed", e );
                 Util.println( out, "No Baselines found" );
             }
         } else {
 			out.println( "[" + Config.nameShort + "] Polling streams: " + polling.toString() );
             try {
                 resolveBaseline( workspace, build.getProject(), action, listener );
             } catch( CCUCMException e ) {
                 Util.println( out, "No Baselines found" );
                 logger.log( Level.WARNING, "Resolving baseline failed", e );
             }
         }
 
 
 
 		/* If a baseline is found */
 		if( action.getBaseline() != null ) {
 			out.println( "[" + Config.nameShort + "] Using " + action.getBaseline().getNormalizedName() );
 
 			//baselineName = state.getBaseline().getFullyQualifiedName();
 
 			if( polling.isPollingSelf() || !polling.isPolling() ) {
 				logger.fine( "Initializing workspace" );
 				initializeWorkspace( build, workspace, changelogFile, listener, action );
 			} else {
 				/* Only start deliver when NOT polling self */
 				logger.fine( "Deliver" );
                 try {
                     beginDeliver( build, action, listener, changelogFile );
                 } catch( CCUCMException e ) {
                     e.printStackTrace( out );
                     throw new AbortException( "Unable to begin deliver" );
                 }
             }
 
 			action.setViewTag( viewtag );
 		}
 
 		out.println( "[" + Config.nameShort + "] Pre build steps done" );
 
         boolean used = false;
         for( Publisher p : build.getParent().getPublishersList() ) {
             logger.fine( "NOTIFIER: " + p.toString() );
             if( p instanceof CCUCMNotifier ) {
                 used = true;
                 break;
             }
         }
 
         if( !used ) {
             logger.info( "Adding notifier to project" );
             build.getParent().getPublishersList().add( new CCUCMNotifier() );
         }
 
 
         /* If there's a result let's find out whether a baseline is found or not */
         if( action.getBaseline() == null ) {
             out.println( "[" + Config.nameShort + "] Finished processing; the baseline is null, this could pose as a problem!" );
         } else {
             out.println( "[" + Config.nameShort + "] Finished processing " + action.getBaseline() );
         }
 
 		return true;
 	}
 
 	private boolean checkInput( TaskListener listener ) {
 		PrintStream out = listener.getLogger();
 
 		/* Check baseline template */
 		if( createBaseline ) {
 			/* Sanity check */
 			if( polling.isPollingOther() ) {
 				if( nameTemplate != null && nameTemplate.length() > 0 ) {
 					try {
 						NameTemplate.testTemplate( nameTemplate );
 					} catch( TemplateException e ) {
 						out.println( "[" + Config.nameShort + "] The template could not be parsed correctly: " + e.getMessage() );
 						return false;
 					}
 				} else {
 					out.println( "[" + Config.nameShort + "] A valid template must be provided to create a Baseline" );
 					return false;
 				}
 			} else {
 				out.println( "[" + Config.nameShort + "] You cannot create a baseline in this mode" );
 			}
 		}
 
 		/* Check polling vs plevel */
 		if( plevel == null ) {
 			if( polling.isPollingSelf() ) {
 				return true;
 			} else {
 				out.println( "[" + Config.nameShort + "] You cannot poll any on other than self" );
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	private boolean initializeWorkspace( AbstractBuild<?, ?> build, FilePath workspace, File changelogFile, BuildListener listener, CCUCMBuildAction action ) {
 
 		PrintStream consoleOutput = listener.getLogger();
 
 		EstablishResult er = null;
 		try {
             CheckoutTask ct = new CheckoutTask( listener, jobName, build.getNumber(), action.getStream(), loadModule, action.getBaseline(), buildProject, ( plevel == null ) );
             er = workspace.act( ct );
 			String changelog = er.getMessage();
 
 			this.viewtag = er.getViewtag();
 
 			/* Write change log */
 			try {
 				FileOutputStream fos = new FileOutputStream( changelogFile );
 				fos.write( changelog.getBytes() );
 				fos.close();
 			} catch( IOException e ) {
 				logger.fine( id + "Could not write change log file" );
 				consoleOutput.println( "[" + Config.nameShort + "] Could not write change log file" );
 			}
 
 		} catch( Exception e ) {
 			consoleOutput.println( "[" + Config.nameShort + "] Unable to initialize workspace" );
 			Exception cause = (Exception) e.getCause();
 			
 			if( cause != null ) {
 				try {
 					throw cause;
 				} catch( Exception e1 ) {
 					ExceptionUtils.print( cause, consoleOutput, true );
 					ExceptionUtils.log( cause, true );
 				}
 			} else {
 				ExceptionUtils.print( cause, consoleOutput, true );
 				ExceptionUtils.log( cause, true );
 			}
 			
 			doPostBuild = false;
 			return false;
 		}
 
 		return true;
 	}
 
     /**
      * Resolve the {@link Baseline} parameter and store the information in the Action
      * @param build
      * @param baselineInput
      * @param action
      * @param listener
      * @throws UnableToInitializeEntityException
      * @throws CCUCMException
      */
 	public void resolveBaselineInput( AbstractBuild<?, ?> build, String baselineInput, CCUCMBuildAction action, BuildListener listener ) throws UnableToInitializeEntityException, CCUCMException {
 
 		PrintStream consoleOutput = listener.getLogger();
 
 		String baselinename = build.getBuildVariables().get( baselineInput );
         action.setBaseline( Baseline.get( baselinename ) );
 
         /* Load the baseline */
         RemoteUtil.loadEntity( build.getWorkspace(), action.getBaseline(), true );
 
         action.setStream( action.getBaseline().getStream() );
         consoleOutput.println( "[" + Config.nameShort + "] Starting parameterized build with a Baseline." );
 
         action.setComponent( action.getBaseline().getComponent() );
         action.setStream( action.getBaseline().getStream() );
 	}
 
 	public String getBaselineValue( AbstractBuild<?, ?> build ) {
 		Collection<?> c = build.getBuildVariables().keySet();
 		Iterator<?> i = c.iterator();
 
 		while( i.hasNext() ) {
 			String next = i.next().toString();
 			if( next.equalsIgnoreCase( "baseline" ) ) {
 				return next;
 			}
 		}
 
 		return null;
 	}
 
     /**
      * Resolve the {@link Baseline} to be build
      * @param workspace
      * @param project
      * @param action
      * @param listener
      * @throws CCUCMException is thrown if no valid baselines are found
      */
 	private void resolveBaseline( FilePath workspace, AbstractProject<?, ?> project, CCUCMBuildAction action, BuildListener listener ) throws CCUCMException {
         logger.fine( "Resolving Baseline from the Stream " + action.getStream().getNormalizedName() );
 		PrintStream out = listener.getLogger();
 
         printParameters( out );
 
         /* The Stream must be loaded */
         action.setStream( (Stream) RemoteUtil.loadEntity( workspace, action.getStream(), getSlavePolling() ) );
 
         List<Baseline> baselines = null;
 
         CCUCMBuildAction lastAction = getLastAction( project );
         Date date = null;
         if( lastAction != null ) {
             date = lastAction.getBaseline().getDate();
         }
 
         /* Find the Baselines and store them */
         /* Old skool self polling */
         if( polling.isPollingSelf() ) {
             baselines = getValidBaselinesFromStream( workspace, plevel, action.getStream(), action.getComponent(), date );
         } else {
             baselines = getBaselinesFromStreams( workspace, listener, out, action.getStream(), action.getComponent(), polling.isPollingChilds(), date );
         }
 
         /* if we did not find any baselines we should return false */
         if( baselines.size() < 1 ) {
             throw new CCUCMException( "No valid Baselines found" );
         }
 
         action.setBaselines( baselines );
         action.setBaseline( selectBaseline( action.getBaselines(), plevel ) );
 
         if( action.getBaselines() == null || action.getBaselines().size() < 1 ) {
             throw new CCUCMException( "Unable to get Baselines" );
         }
 
         /* Print the baselines to jenkins out */
         printBaselines( action.getBaselines(), out );
         out.println( "" );
 	}
 
 	public void beginDeliver( AbstractBuild<?, ?> build, CCUCMBuildAction state, BuildListener listener, File changelogFile ) throws CCUCMException {
 		FilePath workspace = build.getWorkspace();
 		PrintStream consoleOutput = listener.getLogger();
 		boolean result = true;
 
 		EstablishResult er = new EstablishResult();
 
 		try {
 			logger.config( "Starting remote deliver" );
 
             RemoteDeliver rmDeliver = new RemoteDeliver( state.getStream().getFullyQualifiedName(), listener, loadModule, state.getBaseline().getFullyQualifiedName(), build.getParent().getDisplayName(), state.doForceDeliver() );
             er = workspace.act( rmDeliver );
 
 			CCUCMBuildAction action = build.getAction( CCUCMBuildAction.class );
 			action.setViewPath( er.getView().getViewRoot() );
 			action.setViewTag( er.getViewtag() );
 			
 			//state.setSnapView( er.getView() );
 			this.viewtag = er.getViewtag();
 
 			/* Write change log */
 			try {
 				FileOutputStream fos = new FileOutputStream( changelogFile );
 				fos.write( er.getMessage().getBytes() );
 				fos.close();
 			} catch( IOException e ) {
 				logger.fine( id + "Could not write change log file" );
 				consoleOutput.println( "[" + Config.nameShort + "] Could not write change log file" );
 			}
 
 			consoleOutput.println( "[" + Config.nameShort + "] Deliver successful" );
 			
 		/* Deliver failed */
 		} catch( Exception e ) {
 			consoleOutput.println( "[" + Config.nameShort + "] Deliver failed" );
 			result = false;
 			
 			/* Check for exception types */
 			Exception cause = (Exception) net.praqma.util.ExceptionUtils.unpackFrom( IOException.class, e );
 			
 			consoleOutput.println( "[" + Config.nameShort + "] Cause: " + cause.getClass() );
 			
 			/* Re-throw */
 			try {
 				ExceptionUtils.log( cause, true );
 				throw cause;
 			} catch( DeliverException de ) {
 				
 				consoleOutput.println( "[" + Config.nameShort + "] " + de.getType() );
 				
 				/* We need to store this information anyway */
 				state.setViewPath( de.getDeliver().getViewContext() );
 				state.setViewTag( de.getDeliver().getViewtag() );
 				
 				/* The deliver is started, cancel it */
 				if( de.isStarted() ) {
 					try {
 						consoleOutput.print( "[" + Config.nameShort + "] Cancelling deliver. " );
 						RemoteUtil.completeRemoteDeliver( workspace, listener, state.getBaseline(), state.getStream(), de.getDeliver().getViewtag(), de.getDeliver().getViewContext(), false );
 						consoleOutput.println( "Success" );
 
 						/* Make sure, that the post step is not run */
 						state.setNeedsToBeCompleted( false );
 
 					} catch( Exception ex ) {
 						consoleOutput.println( "[" + Config.nameShort + "] Failed to cancel deliver" );
 						consoleOutput.println( "[" + Config.nameShort + "] Original error:" );
 						ExceptionUtils.print( de, consoleOutput, true );
 						consoleOutput.println( "[" + Config.nameShort + "] Cancellation error:" );
 						ExceptionUtils.print( ex, consoleOutput, true );
 						logger.log( Level.WARNING, "", de );
                         logger.log( Level.WARNING, "", ex );
 					}
 				} else {
 					logger.fine( id + "No need for completing deliver" );
 					state.setNeedsToBeCompleted( false );
 				}
 				
 				/* Write something useful to the output */
 				if( de.getType().equals( Type.MERGE_ERROR ) ) {
 					consoleOutput.println( "[" + Config.nameShort + "] Changes need to be manually merged, The stream " + state.getBaseline().getStream().getShortname() + " must be rebased to the most recent baseline on " + state.getStream().getShortname() + " - During the rebase the merge conflict should be solved manually. Hereafter create a new baseline on " + state.getBaseline().getStream().getShortname() + "." );
                     state.setError( "merge error" );
 				}
 
                 throw new CCUCMException( e );
 				
 			/* Force deliver not cancelled */
 			} catch( DeliverNotCancelledException e1 ) {
 				consoleOutput.println( "[" + Config.nameShort + "] Failed to force cancel existing deliver" );
 				state.setNeedsToBeCompleted( false );
 			} catch( Exception e1 ) {
                 logger.log( Level.WARNING, "", e );
 				ExceptionUtils.print( e, consoleOutput, true );
 				throw new CCUCMException( e );
 			}
 		}
 
 
 		try {
 			state.setStream( Stream.get( stream ) );
 		} catch( ClearCaseException e ) {
 			consoleOutput.println( "[" + Config.nameShort + "] " + e.getMessage() );
             logger.log( Level.WARNING, "", e );
             throw new CCUCMException( e );
 		}
 	}
 
 	@Override
 	public ChangeLogParser createChangeLogParser() {
 		return new ChangeLogParserImpl();
 	}
 
     /*
 	private String baselineName = "";
 	
 	public String getBaselineName() {
 		return baselineName;
 	}
 	*/
 
 	@Override
 	public void buildEnvVars( AbstractBuild<?, ?> build, Map<String, String> env ) {
 		super.buildEnvVars( build, env );
 		
 		String CC_BASELINE = "";
 		String CC_VIEWPATH = "";
 		String CC_VIEWTAG  = "";
 		
 		try {
 			
 			CCUCMBuildAction action = build.getAction( CCUCMBuildAction.class );
 			CC_BASELINE = action.getBaseline().getFullyQualifiedName();
 		} catch( Exception e1 ) {
 			System.out.println( "Failed to get baseline: " + e1.getMessage() );
 		}
 		
 		/* View tag */
 		CC_VIEWTAG = viewtag;
 		
 		/* View path */
 		String workspace = env.get( "WORKSPACE" );
 		if( workspace != null ) {
 			CC_VIEWPATH = workspace + File.separator + "view";
 		} else {
 			CC_VIEWPATH = "";
 		}
 
 		env.put( "CC_BASELINE", CC_BASELINE );
 		env.put( "CC_VIEWTAG", CC_VIEWTAG );
 		env.put( "CC_VIEWPATH", CC_VIEWPATH );
 	}
 
 	/**
 	 * This method polls the version control system to see if there are any
 	 * changes in the source code.
 	 * 
 	 */
 	@Override
 	public PollingResult compareRemoteRevisionWith( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState rstate ) throws IOException, InterruptedException {
 
 		/**/
 		try {
 			workspace.act( new RemoteClearCaseCheck() );
 		} catch( AbnormalProcessTerminationException e )  {
 			throw new AbortException( e.getMessage() );
 		}
 		
 		jobName = project.getDisplayName().replace( ' ', '_' );
 		jobNumber = project.getNextBuildNumber();
 		this.id = "[" + jobName + "::" + jobNumber + "]";
 
 		PollingResult p = PollingResult.NO_CHANGES;
 
 		/* Interrupt polling if: */
 		if( this.getMultisitePolling() ) {
 			/* multisite polling and a build is in progress */
 			if( project.isBuilding() ) {
 				logger.info( "A build already building - cancelling poll" );
 	            return PollingResult.NO_CHANGES;
 			}
 		} else {
 			/* not multisite polling and a the project is already in queue */
 			if( project.isInQueue() ) {
 				logger.fine( "A build already in queue - cancelling poll" );
 	            return PollingResult.NO_CHANGES;
 			}
 		}
 
 		logger.fine( "Need for polling" );
 
 		PrintStream out = listener.getLogger();
 
         Stream stream = null;
         Component component = null;
         try {
             stream = Stream.get( this.stream );
             component = Component.get( this.component );
         } catch( UnableToInitializeEntityException e ) {
             Util.println( out, e );
             throw new AbortException( "Unable initialize ClearCase entities" );
         }
 
         logger.fine( "Let's go!" );
 
 		/* Check input */
 		if( checkInput( listener ) ) {
 			try {
 				List<Baseline> baselines = null;
 
                 Baseline foundBaseline = null;
                 CCUCMBuildAction lastAction = getLastAction( project );
                 Date date = null;
                 if( lastAction != null ) {
                     date = lastAction.getBaseline().getDate();
                     lastBaseline = lastAction.getBaseline();
                 }
 
 				/* Old skool self polling */
 				if( polling.isPollingSelf() ) {
 					baselines = getValidBaselinesFromStream( workspace, plevel, stream, component, date );
 				} else {
 					/* Find the Baselines and store them */
 					baselines = getBaselinesFromStreams( workspace, listener, out, stream, component, polling.isPollingChilds(), date );
 				}
 
 				if( baselines.size() > 0 ) {
 					p = PollingResult.BUILD_NOW;
 
                     /* The next Baseline */
 
 
 					/* If ANY */
 					if( plevel == null ) {
 						boolean newer = false;
 
 						/*
 						 * if the newest found baseline is newer than the last
 						 * baseline, build it If there's no last baseline, build
 						 * it
 						 */
 						if( lastBaseline != null ) {
 
 							try {
 								out.println( "The last baseline: " + lastBaseline.stringify() );
 							} catch( Exception e ) {
 								out.println( "Could not stringify last: " + e.getMessage() );
 								e.printStackTrace( out );
 							}
 
 							try {
 								out.println( "The found baseline: " + foundBaseline.stringify() );
 							} catch( Exception e ) {
 								out.println( "Could not stringify state baseline" );
 							}
 
 							if( foundBaseline.getDate().after( lastBaseline.getDate() ) ) {
 								newer = true;
 							}
 						} else {
 							newer = true;
 						}
 
 						if( !newer ) {
 							p = PollingResult.NO_CHANGES;
 						}
 					}
 
 				} else {
 					p = PollingResult.NO_CHANGES;
 				}
 
 			} catch( CCUCMException e ) {
 				out.println( "Error while retrieving baselines: " + e.getMessage() );
 				logger.warning( "Error while retrieving baselines: " + e.getMessage() );
 				p = PollingResult.NO_CHANGES;
 			}
 		}
 
 		return p;
 	}
 
     /**
      * Get the {@link Baseline}s from a {@link Stream}s related Streams.
      * @param workspace
      * @param listener
      * @param consoleOutput
      * @param stream
      * @param component
      * @param pollingChildStreams
      * @return A list of {@link Baseline}'s
      */
 	private List<Baseline> getBaselinesFromStreams( FilePath workspace, TaskListener listener, PrintStream consoleOutput, Stream stream, Component component, boolean pollingChildStreams, Date date ) {
 
 		List<Stream> streams = null;
 		List<Baseline> baselines = new ArrayList<Baseline>();
 
 		try {
 			streams = RemoteUtil.getRelatedStreams( workspace, listener, stream, pollingChildStreams, this.getSlavePolling(), this.getMultisitePolling() );
 		} catch( CCUCMException e1 ) {
 			e1.printStackTrace( consoleOutput );
 			logger.warning( "Could not retrieve streams: " + e1.getMessage() );
 			consoleOutput.println( "[" + Config.nameShort + "] No streams found" );
 			return baselines;
 		}
 
 		consoleOutput.println( "[" + Config.nameShort + "] Scanning " + streams.size() + " stream" + ( streams.size() == 1 ? "" : "s" ) + " for baselines." );
 
 		int c = 1;
 		for( Stream s : streams ) {
 			try {
 				consoleOutput.printf( "[" + Config.nameShort + "] [%02d] %s ", c, s.getShortname() );
 				c++;
 				List<Baseline> found = RemoteUtil.getRemoteBaselinesFromStream( workspace, component, s, plevel, this.getSlavePolling(), this.getMultisitePolling(), date );
 				for( Baseline b : found ) {
 					baselines.add( b );
 				}
 				consoleOutput.println( found.size() + " baseline" + ( found.size() == 1 ? "" : "s" ) + " found" );
 			} catch( CCUCMException e ) {
 				consoleOutput.println( "No baselines: " + e.getMessage() );
 			}
 		}
 
 		consoleOutput.println( "" );
 
 		return baselines;
 	}
 
     /**
      * Given the {@link Stream}, {@link Component} and {@link net.praqma.clearcase.ucm.entities.Project.PromotionLevel} a list of
      * valid {@link Baseline}s is returned.
      * @param workspace
      * @param plevel
      * @param stream
      * @param component
      * @return A list of {@link Baseline}'s
      * @throws CCUCMException
      */
 	private List<Baseline> getValidBaselinesFromStream( FilePath workspace, Project.PromotionLevel plevel, Stream stream, Component component, Date date ) throws CCUCMException {
 		logger.fine( id + "Retrieving valid baselines." );
 
 		/* The baseline list */
 		List<Baseline> baselines = new ArrayList<Baseline>();
 
 		try {
 			baselines = RemoteUtil.getRemoteBaselinesFromStream( workspace, component, stream, plevel, this.getSlavePolling(), this.getMultisitePolling(), date );
 		} catch( CCUCMException e1 ) {
 			logger.fine( "No baselines: " + e1.getMessage() );
 			throw new CCUCMException("Unable to get baselines from " + stream.getShortname(), e1 );
 		}
 
 		return baselines;
 	}
 
     /**
      * Returns the last {@link CCUCMBuildAction}, that has a valid {@link Baseline}
      * @param project
      * @return An Action
      */
     public static CCUCMBuildAction getLastAction( AbstractProject<?, ?> project ) {
         for( AbstractBuild<?, ?> b = project.getLastBuild() ; b != null ; b = b.getPreviousBuild() ) {
             CCUCMBuildAction action = b.getAction( CCUCMBuildAction.class );
             if( action != null && action.getBaseline() != null ) {
                 return action;
             }
         }
 
         return null;
     }
 
 
     private CCUCMBuildAction getBuildAction() throws UnableToInitializeEntityException {
         CCUCMBuildAction action = new CCUCMBuildAction( Stream.get( stream ), Component.get( component ) );
 
         action.setDescription( setDescription );
         action.setMakeTag( makeTag );
         action.setRecommend( recommend );
         action.setForceDeliver( forceDeliver );
         action.setPromotionLevel( plevel );
         action.setUnstable( treatUnstable );
         action.setLoadModule( loadModule );
 
         /* Deliver and template */
         action.setCreateBaseline( createBaseline );
 
         /* Trim template, strip out quotes */
         if( nameTemplate.matches( "^\".+\"$" ) ) {
             nameTemplate = nameTemplate.substring( 1, nameTemplate.length() - 1 );
         }
         action.setNameTemplate( nameTemplate );
 
         action.setPolling( polling );
 
         return action;
     }
 
 
 	@Override
 	public SCMRevisionState calcRevisionsFromBuild( AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener ) throws IOException, InterruptedException {
 		SCMRevisionState scmRS = null;
 
 		if( bl != null ) {
 			scmRS = new SCMRevisionStateImpl();
 		}
 		return scmRS;
 	}
 
 	private Baseline selectBaseline( List<Baseline> baselines, Project.PromotionLevel plevel ) {
 		if( baselines.size() > 0 ) {
 			if( plevel != null ) {
 				return baselines.get( 0 );
 			} else {
 				return baselines.get( baselines.size() - 1 );
 			}
 		} else {
 			return null;
 		}
 	}
 
 	private void printParameters( PrintStream ps ) {
 		ps.println( "[" + Config.nameShort + "] Getting baselines for :" );
 		ps.println( "[" + Config.nameShort + "] * Stream:          " + stream );
 		ps.println( "[" + Config.nameShort + "] * Component:       " + component );
		ps.println( "[" + Config.nameShort + "] * Promotion level: " + plevel.name() );
 		ps.println( "" );
 	}
 
 	public void printBaselines( List<Baseline> baselines, PrintStream ps ) {
 		if( baselines != null ) {
 			ps.println( "[" + Config.nameShort + "] Retrieved " + baselines.size() + " baseline" + ( baselines.size() == 1 ? "" : "s" ) + ":" );
 			if( !( baselines.size() > 8 ) ) {
 				for( Baseline b : baselines ) {
 					ps.println( "[" + Config.nameShort + "] + " + b.getShortname() + "(" + b.getDate() + ")" );
 				}
 			} else {
 				int i = baselines.size();
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( 0 ).getShortname() + "(" + baselines.get( 0 ).getDate() + ")" );
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( 1 ).getShortname() + "(" + baselines.get( 1 ).getDate() + ")" );
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( 2 ).getShortname() + "(" + baselines.get( 2 ).getDate() + ")" );
 				ps.println( "[" + Config.nameShort + "]   ..." );
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( i - 3 ).getShortname() + "(" + baselines.get( i - 3 ).getDate() + ")" );
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( i - 2 ).getShortname() + "(" + baselines.get( i - 2 ).getDate() + ")" );
 				ps.println( "[" + Config.nameShort + "] + " + baselines.get( i - 1 ).getShortname() + "(" + baselines.get( i - 1 ).getDate() + ")" );
 			}
 		}
 	}
 
 	/*
 	 * The following getters and booleans (six in all) are used to display saved
 	 * userdata in Hudsons gui
 	 */
 	public String getLevelToPoll() {
 		return plevel.name();
 	}
 
 	public String getComponent() {
 		return component;
 	}
 
 	public String getStream() {
 		return stream;
 	}
 
 	public String getLoadModule() {
 		return loadModule;
 	}
 
 	/*
 	 * getStreamObject() and getBaseline() are used by CCUCMNotifier to get the
 	 * Baseline and Stream in use, but does not work with concurrent builds!!!
 	 */
 	public Stream getStreamObject() {
 		return integrationstream;
 	}
 
 	@Exported
 	public String getBaseline() {
 		return bl;
 	}
 
 	public boolean getSlavePolling() {
 		CCUCMScmDescriptor desc = (CCUCMScmDescriptor) this.getDescriptor();
 		return desc.getSlavePolling();
 
 	}
 
 	public boolean getMultisitePolling() {
 		if( this.multisitePolling != null ) {
 			return this.multisitePolling;
 		} else {
 			CCUCMScmDescriptor desc = (CCUCMScmDescriptor) this.getDescriptor();
 			return desc.getMultisitePolling();
 		}
 	}
 
 	@Exported
 	public String getPolling() {
 		return polling.toString();
 	}
 
 	@Exported
 	public String getTreatUnstable() {
 		return treatUnstable.toString();
 	}
 
 	@Exported
 	public boolean doPostbuild() {
 		return doPostBuild;
 	}
 
 	public String getBuildProject() {
 		return buildProject;
 	}
 
 	public boolean getForceDeliver() {
 		return forceDeliver;
 	}
 
 	public boolean isCreateBaseline() {
 		return this.createBaseline;
 	}
 
 	public String getNameTemplate() {
 		return this.nameTemplate;
 	}
 
 	public boolean isMakeTag() {
 		return this.makeTag;
 	}
 
 	public boolean isSetDescription() {
 		return this.setDescription;
 	}
 
 	public boolean isRecommend() {
 		return this.recommend;
 	}
 	
 	public void setMultisitePolling( boolean mp ) {
 		this.multisitePolling = mp;
 	}
 
 	/**
 	 * This class is used to describe the plugin to Hudson
 	 * 
 	 * @author Troels Selch
 	 * @author Margit Bennetzen
 	 * 
 	 */
 	@Extension
 	public static class CCUCMScmDescriptor extends SCMDescriptor<CCUCMScm> implements hudson.model.ModelObject {
 
 		private boolean slavePolling;
 		private boolean multisitePolling;
 		private List<String> loadModules;
 
 		public CCUCMScmDescriptor() {
 			super( CCUCMScm.class, null );
 			loadModules = getLoadModules();
 			load();
 		}
 
 		/**
 		 * This method is called, when the user saves the global Hudson
 		 * configuration.
 		 */
 		@Override
 		public boolean configure( org.kohsuke.stapler.StaplerRequest req, JSONObject json ) throws FormException {
 			try {
 				String s = json.getString( "slavePolling" );
 				if( s != null ) {
 					slavePolling = Boolean.parseBoolean( s );
 				}
 				s = json.getString( "multisitePolling" );
 				if( s != null ) {
 					multisitePolling = Boolean.parseBoolean( s );
 				}
 			} catch( Exception e ) {
 				e.getMessage();
 			}
 
 			save();
 
 			return true;
 		}
 
 		public boolean getSlavePolling() {
 			return slavePolling;
 		}
 
 		public boolean getMultisitePolling() {
 			return multisitePolling;
 		}
 		/**
 		 * This is called by Hudson to discover the plugin name
 		 */
 		@Override
 		public String getDisplayName() {
 			return "ClearCase UCM";
 		}
 
 		/**
 		 * This method is called by the scm/CCUCM/global.jelly to validate the
 		 * input without reloading the global configuration page
 		 * 
 		 * @param value
 		 * @return
 		 */
 		public FormValidation doExecutableCheck( @QueryParameter String value ) {
 			return FormValidation.validateExecutable( value );
 		}
 
 		public FormValidation doCheckTemplate( @QueryParameter String value ) throws FormValidation {
 			try {
 				NameTemplate.testTemplate( NameTemplate.trim( value ) );
 				return FormValidation.ok( "The template seems ok" );
 			} catch( TemplateException e ) {
 				throw FormValidation.error( "Does not appear to be a valid template: " + e.getMessage() );
 			}
 		}
 
 		public void doLevelCheck( @QueryParameter String polling, @QueryParameter String level ) throws FormValidation {
 			System.out.println( "LEVEL CHECK: " + polling + " + " + level );
 			if( level.equalsIgnoreCase( "any" ) && !polling.equals( "self" ) ) {
 				throw FormValidation.error( "You can only combine self and any" );
 			}
 		}
 
 		@Override
 		public CCUCMScm newInstance( StaplerRequest req, JSONObject formData ) throws FormException {
 			try {
 				String polling = formData.getString( "polling" );
 				String level = formData.getString( "levelToPoll" );
 
 				if( level.equalsIgnoreCase( "any" ) ) {
 					if( !polling.equalsIgnoreCase( "self" ) ) {
 						throw new FormException( "You can only use any with self polling", "polling" );
 					}
 				}
 			} catch( JSONException e ) {
 				throw new FormException( "You missed some fields: " + e.getMessage(), "CCUCM.polling" );
 			}
 			CCUCMScm instance = req.bindJSON( CCUCMScm.class, formData );
 			/* TODO This is actually where the Notifier check should be!!! */
 			return instance;
 		}
 
 		/**
 		 * Used by Hudson to display a list of valid promotion levels to build
 		 * from. The list of promotion levels is hard coded in
 		 * net.praqma.hudson.Config.java
 		 * 
 		 * @return
 		 */
 		public List<String> getLevels() {
 			return Config.getLevels();
 		}
 
 		/**
 		 * Used by Hudson to display a list of loadModules (whether to poll all
 		 * or only modifiable elements
 		 * 
 		 * @return
 		 */
 		public List<String> getLoadModules() {
 			loadModules = new ArrayList<String>();
 			loadModules.add( "All" );
 			loadModules.add( "Modifiable" );
 			return loadModules;
 		}
 	}
 }
