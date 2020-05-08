 package net.praqma.hudson.notifier;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.Serializable;
 import java.util.List;
 
 import org.kohsuke.stapler.StaplerRequest;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Cool;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.Tag;
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.hudson.exception.NotifierException;
 import net.praqma.hudson.scm.PucmScm;
 import net.praqma.hudson.scm.PucmState.State;
 import net.praqma.util.debug.PraqmaLogger;
 import net.praqma.util.debug.PraqmaLogger.Logger;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.TaskListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Hudson;
 import hudson.model.Hudson.MasterComputer;
 import hudson.model.Node;
 
 import hudson.remoting.Callable;
 import hudson.remoting.Channel.Listener;
 import hudson.remoting.DelegatingCallable;
 import hudson.remoting.VirtualChannel;
 import hudson.scm.SCM;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 import net.sf.json.JSONObject;
 
 /**
  * PucmNotifier perfoms the user-chosen PUCM post-build actions
  * 
  * @author Troels Selch S�rensen
  * @author Margit Bennetzen
  * 
  */
 public class PucmNotifier extends Notifier
 {
 	private boolean promote;
 	private boolean recommended;
 	private Baseline baseline;
 	private PrintStream hudsonOut;
 	private Stream st;
 	private boolean makeTag;
 	private boolean setDescription;
 	private Status status;
 
 	private String id = "";
 
 	private Logger logger = null;
 
 	/**
 	 * This constructor is used in the inner class <code>DescriptorImpl</code>.
 	 * 
 	 * @param promote
 	 *            if <code>true</code>, the baseline will be promoted after the
 	 *            build.
 	 * @param recommended
 	 *            if <code>true</code>, the baseline will be marked
 	 *            'recommended' in ClearCase.
 	 * @param makeTag
 	 *            if <code>true</code>, pucm will set a Tag() on the baseline in
 	 *            ClearCase.
 	 */
 	public PucmNotifier( boolean promote, boolean recommended, boolean makeTag, boolean setDescription )
 	{
 		this.promote = promote;
 		this.recommended = recommended;
 		this.makeTag = makeTag;
 		this.setDescription = setDescription;
 	}
 
 	@Override
 	public boolean needsToRunAfterFinalized()
 	{
 		return true;
 	}
 
 	public BuildStepMonitor getRequiredMonitorService()
 	{
 		return BuildStepMonitor.NONE;
 	}
 
 	@Override
 	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener ) throws InterruptedException, IOException
 	{
 		logger = PraqmaLogger.getLogger();
 		logger.trace_function();
 		boolean result = true;
 		hudsonOut = listener.getLogger();
 		
 		if ( build.getBuildVariables().get( "include_classes" ) != null )
 		{
 			String[] is = build.getBuildVariables().get( "include_classes" ).toString().split( "," );
 			for( String i : is )
 			{
 				logger.subscribe( i.trim() );
 			}
 		}
 		
 		Cool.setLogger( logger );
 
 		status = new Status();
 
 		this.id = "[" + build.getParent().getDisplayName() + "::" + build.getNumber() + "]";
 
 		SCM scmTemp = null;
 		if ( result )
 		{
 			scmTemp = build.getProject().getScm();
 			if ( !( scmTemp instanceof PucmScm ) )
 			{
 				listener.fatalError( "[PUCM] Not a PUCM scm. This Post build action can only be used when polling from ClearCase with PUCM plugin." );
 				result = false;
 			}
 		}
 
 		State pstate = null;
 		if ( result )
 		{
 			// PucmScm scm = (PucmScm) scmTemp;
 			// PucmState state = scm.getPucmState(
 			// build.getParent().getDisplayName(), build.getNumber() );
 			pstate = PucmScm.pucm.getState( build.getParent().getDisplayName(), build.getNumber() );
 
 			// if ( scm.doPostbuild() )
 			if ( pstate.doPostBuild() && pstate.getBaseline() != null )
 			{
 				logger.debug( id + "Post build" );
 
 				String bl = pstate.getBaseline().GetFQName();
 
 				/* If no baselines found bl will be null */
 				if ( bl != null )
 				{
 					try
 					{
 						baseline = UCMEntity.GetBaseline( bl );
 					}
 					catch ( UCMException e )
 					{
 						logger.warning( id + "Could not initialize baseline." );
 						baseline = null;
 					}
 
 					// st = scm.getStreamObject();
 					st = pstate.getStream();
 					if ( baseline == null )
 					{
 						// If baseline is null, the user has already been
 						// notified
 						// in Console output from PucmScm.checkout()
 						result = false;
 					}
 				}
 				else
 				{
 					result = false;
 				}
 
 			}
 			else
 			{
 				// Not performing any post build actions.
 				result = false;
 			}
 		}
 
 		if ( result )
 		{
 			try
 			{
 				processBuild( build, launcher, listener, pstate );
 				if ( setDescription )
 				{
 					build.setDescription( status.getBuildDescr() );
 					hudsonOut.println( "[PUCM] Description set." );
 				}
 
 			}
 			catch ( NotifierException ne )
 			{
 				hudsonOut.println( ne.getMessage() );
 			}
 			catch ( IOException e )
 			{
 				hudsonOut.println( "[PUCM] Couldn't set build description." );
 			}
 		}
 
 		/*
 		 * Removing baseline and job from collection, do this no matter what as
 		 * long as the SCM is pucm
 		 */
 		if ( ( scmTemp instanceof PucmScm ) && baseline != null )
 		{
 			boolean done2 = pstate.remove();
 			logger.debug( id + "Removing job " + build.getNumber() + " from collection: " + done2 );
 
 			logger.debug( "PUCM FINAL=" + PucmScm.pucm.stringify() );
 		}
 
 		logger.debug( id + "The final state:\n" + pstate.stringify() );
 
 		return result;
 	}
 
 	/**
 	 * 
 	 * @author wolfgang
 	 * 
 	 */
 	private static class RemotePostBuildTask implements Callable<Status, IOException>, Serializable
 	{
 		private static final long serialVersionUID = 1L;
 		private String displayName;
 		private String buildNumber;
 
 		private Result result;
 
 		private String baseline;
 		private String stream;
 
 		private boolean makeTag = false;
 		private boolean promote = false;
 		private boolean recommended = false;
 		private Status status;
 		private BuildListener listener;
 
 		private String id = "";
 		
 		private Logger logger = null;
 		
 		public RemotePostBuildTask( Result result, Status status, BuildListener listener, boolean makeTag, boolean promote, boolean recommended, String baseline, String stream, String displayName, String buildNumber, Logger logger )
 		{
 			this.displayName = displayName;
 			this.buildNumber = buildNumber;
 
 			this.id = "[" + displayName + "::" + buildNumber + "]";
 
 			this.baseline = baseline;
 			this.stream = stream;
 
 			this.result = result;
 
 			this.makeTag = makeTag;
 			this.promote = promote;
 			this.recommended = recommended;
 
 			this.status      = status;
 			this.listener    = listener;
 			
 			this.logger = logger;
 		}
 
 		public Status call() throws IOException
 		{
 			PraqmaLogger.getLogger( logger );
 			PrintStream hudsonOut = listener.getLogger();
 			UCM.SetContext( UCM.ContextType.CLEARTOOL );
 
 			String newPLevel = "";
 			
 			status.addToLog( logger.info( "Starting PostBuild task" ) );
 			
 			/* Create the baseline object */
 			Baseline baseline = null;
 			try
 			{
 				baseline = UCMEntity.GetBaseline( this.baseline );
 			}
 			catch ( UCMException e )
 			{
 				status.addToLog( logger.debug( id + "could not create Baseline object:" + e.getMessage() ) );
 				throw new IOException( "[PUCM] Could not create Baseline object: " + e.getMessage() );
 			}
 
 			/* Create the stream object */
 			Stream stream = null;
 			try
 			{
 				stream = UCMEntity.GetStream( this.stream );
 			}
 			catch ( UCMException e )
 			{
 				status.addToLog( logger.debug( id + "could not create Stream object:" + e.getMessage() ) );
 				throw new IOException( "[PUCM] Could not create Stream object: " + e.getMessage() );
 			}
 
 			/* Create the Tag object */
 			Tag tag = null;
 			if ( makeTag )
 			{
 				try
 				{
 					// Getting tag to set buildstatus
 					tag = baseline.GetTag( this.displayName, this.buildNumber );
 					status.setTagAvailable( true );
 				}
 				catch ( UCMException e )
 				{
 					hudsonOut.println( "[PUCM] Could not get Tag: " + e.getMessage() );
 					status.addToLog( logger.warning( id + "Could not get Tag: " + e.getMessage() ) );
 				}
 			}
 
 			hudsonOut.println( "[PUCM] Build result: " + result );
 			/* The build was a success */
 			if ( result.equals( Result.SUCCESS ) )
 			{
 				if ( status.isTagAvailable() )
 				{
 					tag.SetEntry( "buildstatus", "SUCCESS" );
 				}
 
 				if ( promote )
 				{
 					try
 					{
 						baseline.Promote();
 						status.setPLevel( true );
 						hudsonOut.println( "[PUCM] Baseline promoted to " + baseline.GetPromotionLevel( true ).toString() + "." );
 					}
 					catch ( UCMException e )
 					{
 						status.setStable( false );
 						// build.setResult( Result.UNSTABLE );
 						// as it will not make sense to recommend if we cannot
 						// promote, we do this:
 						if ( recommended )
 						{
 							recommended = false;
 							// throw new NotifierException(
 							// "Could not promote baseline and will not recommend. "
 							// + e.getMessage() );
 							hudsonOut.println( "[PUCM] Could not promote baseline and will not recommend. " + e.getMessage() );
 							status.addToLog( logger.warning( id + "Could not promote baseline and will not recommend. " + e.getMessage() ) );
 						}
 						else
 						{
 							// As we will not recommend if we cannot promote,
 							// it's
 							// ok to break method here
 							// throw new NotifierException(
 							// "Could not promote baseline. " + e.getMessage()
 							// );
 							hudsonOut.println( "[PUCM] Could not promote baseline. " + e.getMessage() );
 							status.addToLog( logger.warning( id + "Could not promote baseline. " + e.getMessage() ) );
 						}
 					}
 				}
 				/* Recommend the Baseline */
 				if ( recommended )
 				{
 					try
 					{
 						if ( status.isPLevel() )
 						{
 							stream.RecommendBaseline( baseline );
 							status.setRecommended( true );
 							hudsonOut.println( "[PUCM] Baseline " + baseline.GetShortname() + " is now recommended." );
 						}
 					}
 					catch ( Exception e )
 					{
 						// build.setResult( Result.UNSTABLE );
 						// throw new NotifierException(
 						// "Could not recommend baseline. Reason: " +
 						// e.getMessage() );
 						status.setStable( false );
 						hudsonOut.println( "[PUCM] Could not recommend baseline. Reason: " + e.getMessage() );
 						status.addToLog( logger.warning( id + "Could not recommend baseline. Reason: " + e.getMessage() ) );
 					}
 				}
 			}
 			/* The build failed */
 			else
 			{
 				if ( result.equals( Result.FAILURE ) )
 				{
 					hudsonOut.println( "[PUCM] Build failed." );
 
 					if ( status.isTagAvailable() )
 					{
 						tag.SetEntry( "buildstatus", "FAILURE" );
 					}
 					if ( promote )
 						try
 						{
 							baseline.Demote();
 							status.setPLevel( true );
 							hudsonOut.println( "[PUCM] Baseline is " + baseline.GetPromotionLevel( true ).toString() + "." );
 						}
 						catch ( Exception e )
 						{
 							status.setStable( false );
 							// throw new NotifierException(
 							// "Could not demote baseline. " + e.getMessage() );
 							hudsonOut.println( "[PUCM] Could not demote baseline. " + e.getMessage() );
 							status.addToLog( logger.warning( id + "Could not demote baseline. " + e.getMessage() ) );
 						}
 				}
 				/* Result not handled by PUCM */
 				else
 				{
 					tag.SetEntry( "buildstatus", result.toString() );
 					status.addToLog( logger.log( id + "Buildstatus (Result) was " + result + ". Not handled by plugin." ) );
 					hudsonOut.println( "[PUCM] Baseline not changed. Buildstatus: " + result );
 				}
 			}
 			/* Persist the Tag */
 			if ( makeTag )
 			{
 				try
 				{
 					tag = tag.Persist();
 					hudsonOut.println( "[PUCM] Baseline now marked with tag: \n" + tag.Stringify() );
 				}
 				catch ( Exception e )
 				{
 					hudsonOut.println( "[PUCM] Could not change tag in ClearCase. Contact ClearCase administrator to do this manually." );
 				}
 			}
 
 			try
 			{
 				newPLevel = baseline.GetPromotionLevel( true ).toString();
 			}
 			catch(UCMException e)
 			{
 				logger.log( id + " Could not get promotionlevel." );
 				hudsonOut.println( "[PUCM] Could not get promotion level." );
 			}
 			
 			status.setBuildDescr( setDisplaystatus( newPLevel, baseline.GetShortname() ) );
 
 			return status;
 		}
 
 		private String setDisplaystatus( String plevel, String fqn )
 		{
 			String s = "";
 
 			// Get shortname
 			s += "<small>" + fqn + "</small>";
 
 			// Get plevel:
 			s += "<BR/><small>" + plevel + "</small>";
 
 			if ( recommended )
 			{
 				if ( status.isRecommended() )
 					s += "<BR/<B><small>Recommended</small></B>";
 				else
 					s += "<BR/><B><small>Could not recommend</small></B>";
 			}
 			return s;
 		}
 
 	}
 
 	private void processBuild( AbstractBuild build, Launcher launcher, BuildListener listener, State pstate ) throws NotifierException
 	{
 		Result buildResult = build.getResult();
 
 		VirtualChannel ch = launcher.getChannel();
 
 		if ( ch == null )
 		{
 			logger.debug( "The channel was null" );
 		}
 
 		try
 		{
 			logger.debug( id + "Trying to run TagTask" );
 			status = ch.call( new RemotePostBuildTask( buildResult, status, listener, makeTag, promote, recommended, pstate.getBaseline().GetFQName(), pstate.getStream().GetFQName(), build.getParent().getDisplayName(), Integer.toString( build.getNumber() ), logger ) );
 			logger.empty( status.getLog() );
 		}
 		catch ( Exception e )
 		{
 			logger.debug( id + "Something went wrong: " + e.getMessage() );
 		}
 
 		status.setBuildStatus( buildResult );
 
 		if ( !status.isStable() )
 		{
 			build.setResult( Result.UNSTABLE );
 		}
 	}
 
 	public boolean isPromote()
 	{
 		logger.trace_function();
 		return promote;
 	}
 
 	public boolean isRecommended()
 	{
 		logger.trace_function();
 		return recommended;
 	}
 
 	public boolean isMakeTag()
 	{
 		return makeTag;
 	}
 
 	public boolean isSetDescription()
 	{
 		return setDescription;
 	}
 
 	/**
 	 * This class is used by Hudson to define the plugin.
 	 * 
 	 * @author Troels Selch S�rensen
 	 * @author Margit Bennetzen
 	 * 
 	 */
 	@Extension
 	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher>
 	{
 		public DescriptorImpl()
 		{
 			super( PucmNotifier.class );
 			//logger.trace_function();
 			load();
 		}
 
 		@Override
 		public String getDisplayName()
 		{
 			//logger.trace_function();
 			return "Praqmatic UCM";
 		}
 
 		/**
 		 * Hudson uses this method to create a new instance of
 		 * <code>PucmNotifier</code>. The method gets information from Hudson
 		 * config page. This information is about the configuration, which
 		 * Hudson saves.
 		 */
 		@Override
 		public Notifier newInstance( StaplerRequest req, JSONObject formData ) throws FormException
 		{
 			//logger.trace_function();
 			boolean promote = req.getParameter( "Pucm.promote" ) != null;
 			boolean recommended = req.getParameter( "Pucm.recommended" ) != null;
 			boolean makeTag = req.getParameter( "Pucm.makeTag" ) != null;
 			boolean setDescription = req.getParameter( "Pucm.setDescription" ) != null;
 			save();
 			return new PucmNotifier( promote, recommended, makeTag, setDescription );
 		}
 
 		@Override
 		public boolean isApplicable( Class<? extends AbstractProject> arg0 )
 		{
 			//logger.trace_function();
 			return true;
 		}
 	}
 }
