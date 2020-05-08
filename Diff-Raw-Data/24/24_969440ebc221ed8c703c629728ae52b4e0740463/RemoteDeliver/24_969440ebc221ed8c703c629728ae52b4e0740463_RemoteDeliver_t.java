 package net.praqma.hudson.notifier;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.remoting.Pipe;
 import hudson.remoting.VirtualChannel;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.Cool;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.utils.BuildNumber;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.clearcase.ucm.view.UCMView;
 import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.util.debug.PraqmaLogger;
 import net.praqma.util.debug.PraqmaLogger.Logger;
 
 /**
  * 
  * @author wolfgang
  * 
  */
 class RemoteDeliver implements FileCallable<Integer>
 {
 	private static final long serialVersionUID = 1L;
 	private String jobName;
 	private String buildNumber;
 
 	private String baseline;
 	private String stream;
 
 	private Status status;
 	private BuildListener listener;
 
 	private String id = "";
 	
 	/*
 	private boolean apply4level;
 	private String alternateTarget;
 	private String baselineName;
 	*/
 	private String component;
 	private String loadModule;
 	
 	UCMDeliver ucmDeliver = null;
 	
 	private Logger logger = null;
 	private PrintStream hudsonOut = null;
 	private Pipe pipe = null;
 	//private OutputStream out = null;
 	
 	/* Advanced */
 	
 	public RemoteDeliver( Result result, Status status, BuildListener listener,
 								/* Common values */
 								String component, String loadModule, String baseline,
 								String jobName, String buildNumber, UCMDeliver ucmDeliver,
 								Logger logger, Pipe pipe )
 	{
 		this.jobName = jobName;
 		this.buildNumber = buildNumber;
 
 		this.id = "[" + jobName + "::" + buildNumber + "]";
 
 		this.baseline = baseline;
 		this.stream = "";
 
 		this.status      = status;
 		this.listener    = listener;
 		
 		this.component       = component;
 		this.loadModule      = loadModule;
 
 		
 		this.ucmDeliver = ucmDeliver;
 				
 		this.logger = logger;
 		this.pipe   = pipe;
 	}
 	
 
 	public Integer invoke( File workspace, VirtualChannel channel ) throws IOException
 	{
 		PraqmaLogger.getLogger( logger );
 		/* Make sure that the local log file is not written */
 		logger.setLocalLog( null );
 		Cool.setLogger( logger );
 		hudsonOut = listener.getLogger();
 		UCM.setContext( UCM.ContextType.CLEARTOOL );
 				
 		status.addToLog( logger.info( "Starting remote deliver task" ) );
 		
 		/* Create the baseline object */
 		Baseline baseline = null;
 		try
 		{
 			baseline = UCMEntity.getBaseline( this.baseline );
 		}
 		catch ( UCMException e )
 		{
 			status.addToLog( logger.debug( id + "could not create Baseline object:" + e.getMessage() ) );
 			if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
 			throw new IOException( "[PUCM] Could not create Baseline object: " + e.getMessage() );
 		}
 		
 		/* Create the development stream object */
 		/* Append vob to dev stream */
		this.stream = "pucm_" + System.getenv( "COMPUTERNAME" ) + "_" + jobName + "@" + baseline.getPvobString();
 		
 		Stream stream = null;
 		try
 		{
 			status.addToLog( logger.info( id + "Trying to create source Stream " + this.stream ) );
 			stream = UCMEntity.getStream( this.stream );
 		}
 		catch ( UCMException e )
 		{
 			status.addToLog( logger.debug( id + "could not create Stream object:" + e.getMessage() ) );
 			if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
 			throw new IOException( "[PUCM] Could not create Stream object: " + e.getMessage() );
 		}
 				
 		/* Create the component object */
 		Component component = null;
 		try
 		{
 			component = UCMEntity.getComponent( this.component );
 		}
 		catch ( UCMException e )
 		{
 			status.addToLog( logger.debug( id + "could not create Component object:" + e.getMessage() ) );
 			if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
 			throw new IOException( "[PUCM] Could not create Component object: " + e.getMessage() );
 		}
 		
 		/* Get the target Stream */
 		Stream target = null;
 		if( ucmDeliver.alternateTarget.length() > 0 )
 		{
 			try
 			{
 				status.addToLog( logger.info( id + "Trying to create target Stream " + ucmDeliver.alternateTarget ) );
 				target = UCMEntity.getStream( ucmDeliver.alternateTarget );
 			}
 			catch ( UCMException e )
 			{
 				status.addToLog( logger.debug( id + "could not create target Stream object: " + e.getMessage() ) );
 				if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
 				throw new IOException( "[PUCM] Could not create target Stream object: " + e.getMessage() );
 			}
 		}
 		else
 		{
 			try
 			{
 				target = stream.getDefaultTarget();
 			}
 			catch ( UCMException e )
 			{
 				status.addToLog( logger.debug( id + "The Stream did not have a default target: " + e.getMessage() ) );
 				if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
 				throw new IOException( "[PUCM] The Stream did not have a default target: " + e.getMessage() );
 			}
 		}
 		
 		status.addToLog( logger.debug( id + "Target stream is " + target.getFullyQualifiedName() ) );
 		
 		/* Trying to verify the build number attributes */
 		/* Four level version number */
 		String number = "";
 		/* Get version number from project+component */
 		if( ucmDeliver.versionFrom.equals( "project" ) )
 		{
 			status.addToLog( logger.debug( id + "Using project setting" ) );
 			
 			try
 			{
 				Project project = target.getProject();
 				number = BuildNumber.getBuildNumber( project );
 			}
 			catch ( UCMException e )
 			{
 				status.addToLog( logger.warning( id + "Could not get four level version" ) );
 				status.addToLog( logger.warning( e ) );
 				if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
 				throw new IOException( "Could not get four level version: " + e.getMessage() );
 			}
 		}
 		/* Get version number from project+component */
 		else if( ucmDeliver.versionFrom.equals( "settings" ) )
 		{	
 			status.addToLog( logger.debug( id + "Using settings" ) );
 			
 			/* Verify settings */
 			if( ucmDeliver.buildnumberMajor.length() > 0 && ucmDeliver.buildnumberMinor.length() > 0 && ucmDeliver.buildnumberPatch.length() > 0 )
 			{
 				number = "__" + ucmDeliver.buildnumberMajor + "_" + ucmDeliver.buildnumberMinor + "_" + ucmDeliver.buildnumberPatch + "_";
 				
 				/* Get the sequence number from the component */
 				if( ucmDeliver.buildnumberSequenceSelector.equals( "component" ) )
 				{
 					status.addToLog( logger.debug( id + "Get sequence from project " + component ) );
 					
 					try
 					{
 						Project project = target.getProject();
 						int seq = BuildNumber.getNextBuildSequence( project );
 						number += seq;
 					}
 					catch ( UCMException e )
 					{
 						status.addToLog( logger.warning( id + "Could not get sequence number from component" ) );
 						status.addToLog( logger.warning( e ) );
 						if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
 						throw new IOException( "Could not get sequence number from component: " + e.getMessage() );
 					}
 				}
 				/* Use the current build number from jenkins */
 				else
 				{
 					status.addToLog( logger.debug( id + "Getting sequence from build number" ) );
 					number += this.buildNumber;
 				}
 			}
 			else
 			{
 				status.addToLog( logger.warning( id + "Creating error message" ) );
 				String error = ( ucmDeliver.buildnumberMajor.length()    == 0 ? "Major missing. " : "" ) +
 							   ( ucmDeliver.buildnumberMinor.length()    == 0 ? "Minor missing. " : "" ) +
 							   ( ucmDeliver.buildnumberPatch.length()    == 0 ? "Patch missing. " : "" );
 				
 				status.addToLog( logger.warning( id + "Missing information in build numbers: " + error ) );
 				throw new IOException( "Missing build number information: " + error );
 			}
 		}
 		else
 		{
 			/* No op = none */
 		}
 		
 		
 		/* Make deliver view */
 		SnapshotView view;
 		try
 		{
 			view = makeDeliverView( target, workspace );
 		}
 		catch ( ScmException e )
 		{
 			status.addToLog( logger.warning( id + "could not create deliver view: " + e.getMessage() ) );
 			status.addToLog( logger.warning( e ) );
 			throw new IOException( "[PUCM] Could not create deliver view: " + e.getMessage() );
 		}
 		
 		boolean makebl = true;
 		
 		/* Make the deliver */
 		try
 		{
 			status.addToLog( logger.info( id + "Trying to deliver the Baseline to " + target.getFullyQualifiedName() ) );
 			status.addToLog( logger.info( id + "The view is " + view.GetViewRoot().getAbsolutePath() + ". Tag=" + view.GetViewtag() ) );
 			if( stream.isReadOnly() )
 			{
 				status.addToLog( logger.debug( id + "The stream is read only" ) );
 				//baseline.deliver( null, target, view.GetViewRoot(), null, true, true, true );
 				if( !baseline.deliver( baseline.getStream(), null, view.GetViewRoot(), view.GetViewtag(), true, true, true ) )
 				{
 					status.addToLog( logger.debug( id + "The deliver was empty, no changes" ) );
 					makebl = false;
 					
 					/* Ok ok, we throw an exception*/
 					throw new IOException( "Could not deliver, the change set was empty" );
 				}
 			}
 			else
 			{
 				status.addToLog( logger.debug( id + "The stream is writable" ) );
 				stream.deliver( null, target, view.GetViewRoot(), null, true, true, true );
 			}
 		}
 		catch ( UCMException e )
 		{
 			hudsonOut.print( "[PUCM] Deliver operation failed. " );
 			if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
 			status.addToLog( logger.warning( id + "The baseline could not be delivered" + e.getMessage() ) );
 			status.addToLog( logger.warning( e ) );
 			try
 			{
 				if( stream.isDelivering() )
 				{
 					hudsonOut.print( "Trying to cancel..." );
 					stream.cancelDeliver( view.GetViewRoot() );
 				}
 				else
 				{
 					status.addToLog( logger.warning( id + "Failed to deliver" ) );
 					status.addToLog( logger.warning( e ) );
 					hudsonOut.println( "" );
 					hudsonOut.println( "Error was: " + e.getMessage() );
 					throw new IOException( "Deliver operation failed: " + e.getMessage() );
 				}
 			}
 			catch( UCMException e1 )
 			{
 				status.addToLog( logger.warning( id + "Could not cancel non-trivial deliver" ) );
 				status.addToLog( logger.warning( e1 ) );
 				if( e.stdout != null ){	hudsonOut.println( e1.stdout ); }
 				hudsonOut.println( "Failed" );
 				throw new IOException( "Deliver operation failed and could not cancel: " + e.getMessage() );
 			}
 			
 			hudsonOut.println( "Done" );
 			throw new IOException( "The baseline could not be delivered and was cancelled: " + e.getMessage() );
 		}
 		
 		/* Make baseline */
 		if( ucmDeliver.baselineName.length() > 0 && makebl )
 		{
 			
 			/* Create the baseline */
 			Baseline newbl = null;
 			System.out.println( "The baseline is " + ucmDeliver.baselineName + number );
 			try
 			{
 				status.addToLog( logger.info( id + "Creating new baseline " + ucmDeliver.baselineName + number ) );
 				newbl = Baseline.create( ucmDeliver.baselineName + number, component, view.GetViewRoot(), false, false );
 				hudsonOut.println( "[PUCM] Created baseline " + ucmDeliver.baselineName + number );
 			}
 			catch ( UCMException e )
 			{
     			status.addToLog( logger.warning( id + "Could not get view for workspace. " + e.getMessage() ) );
     			hudsonOut.println( "[PUCM] Failed creating baseline " + ucmDeliver.baselineName + number );
     			if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
     			throw new IOException( "Could not create baseline: " + e.getMessage() );
 			}
 		}
 
 		/* End of deliver */
 		
 		//osw.close();
 		
 		status.addToLog( logger.warning( id + "Remote deliver finished normally" ) );
 
 		return 1;
 	}
 	
 	
 	private SnapshotView makeDeliverView( Stream stream, File workspace ) throws ScmException
 	{
 		/* Replace evil characters with less evil characters */
 		String newJobName = jobName.replaceAll( "\\s", "_" );
 		
 		String viewtag = newJobName + "_" + System.getenv( "COMPUTERNAME" ) + "_" + stream.getShortname();
 		hudsonOut.println( "[PUCM] Trying to make deliver view " + viewtag );
 		
 		File viewroot = new File( workspace.getPath() + File.separator + "deliverview_" + stream.getShortname() );
 		
 		status.addToLog( logger.debug( id + "Deliver: " + viewroot.getAbsolutePath() + ". Tag=" + viewtag ) );
 		status.addToLog( logger.debug( id + "Stream is " + stream.getFullyQualifiedName() ) );
 
 		hudsonOut.println( "[PUCM] viewtag: " + viewtag );
 		
 		SnapshotView sv = null;
 
 		try
 		{
 			if( viewroot.exists() )
 			{
 				hudsonOut.println( "[PUCM] Reusing viewroot: " + viewroot.toString() );
 			}
 			else
 			{
 				if ( viewroot.mkdir() )
 				{
 					hudsonOut.println( "[PUCM] Created folder for viewroot:  " + viewroot.toString() );
 				}
 				else
 				{
 					status.addToLog( logger.warning( id + "View root does not exist and could not be created" ) );
 					throw new ScmException( "Could not create folder for viewroot:  " + viewroot.toString() );
 				}
 			}
 		}
 		catch( Exception e )
 		{
 			status.addToLog( logger.warning( id + "View root could not be initialized" ) );
 			throw new ScmException( "Could not make workspace (for viewroot " + viewroot.toString() + "). Cause: " + e.getMessage() );
 
 		}
 
 		if( UCMView.ViewExists( viewtag ) )
 		{
 			hudsonOut.println( "[PUCM] Reusing viewtag: " + viewtag + "\n" );
 			try
 			{
 				SnapshotView.ViewrootIsValid( viewroot );
 				hudsonOut.println( "[PUCM] Viewroot is valid in ClearCase" );
 			}
 			catch( UCMException ucmE )
 			{
 				try
 				{
 					hudsonOut.println( "[PUCM] Viewroot not valid - now regenerating.... " );
 					SnapshotView.RegenerateViewDotDat( viewroot, viewtag );
 				}
 				catch( UCMException ucmEe )
 				{
 					status.addToLog( logger.warning( id + "Could regenerate workspace." ) );
 					if( ucmEe.stdout != null ){	hudsonOut.println( ucmEe.stdout ); }
 					throw new ScmException( "Could not make workspace - could not regenerate view: " + ucmEe.getMessage() + " Type: " + "" );
 				}
 			}
 
 			hudsonOut.print( "[PUCM] Getting snapshotview..." );
 			try
 			{
 				sv = UCMView.GetSnapshotView( viewroot );
 				hudsonOut.println( " DONE" );
 			}
 			catch( UCMException e )
 			{
 				status.addToLog( logger.warning( id + "Could not get view for workspace. " + e.getMessage() ) );
 				if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
 				throw new ScmException( "Could not get view for workspace. " + e.getMessage() );
 			}
 		}
 		else
 		{
 			try
 			{
 				sv = SnapshotView.Create( stream, viewroot, viewtag );
 
 				hudsonOut.println( "[PUCM] View doesn't exist. Created new view in local workspace: " + viewroot.getAbsolutePath() );
 				status.addToLog( logger.log( "The view did not exist and created a new" ) );
 			}
 			catch( UCMException e )
 			{
 				status.addToLog( logger.warning( id + "The view could not be created" ) );
 				status.addToLog( logger.warning( e ) );
 				if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
 				throw new ScmException( "View not found in this region, but view with viewtag '" + viewtag + "' might exists in the other regions. Try changing the region Hudson or the slave runs in." );
 			}
 		}
 
 		try
 		{
 			hudsonOut.print( "[PUCM] Updating deliver view using " + loadModule.toLowerCase() + " modules..." );
 
 			sv.Update( true, true, true, false, COMP.valueOf( loadModule.toUpperCase() ), null );
 			hudsonOut.println( " DONE" );
 		}
 		catch( UCMException e )
 		{
 			if( e.stdout != null ){	hudsonOut.println( e.stdout ); }
 			throw new ScmException( "Could not update snapshot view. " + e.getMessage() );
 		}
 		
 		return sv;
 	}
 
 }
