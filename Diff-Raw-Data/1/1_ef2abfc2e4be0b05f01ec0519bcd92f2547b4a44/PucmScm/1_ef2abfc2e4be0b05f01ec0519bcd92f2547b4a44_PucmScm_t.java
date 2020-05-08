 package net.praqma.hudson.scm;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.FilePath.FileCallable;
 import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.FreeStyleBuild;
 import hudson.model.ParameterValue;
 import hudson.model.TaskListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Job;
 import hudson.remoting.VirtualChannel;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.PollingResult;
 import hudson.scm.SCMDescriptor;
 import hudson.scm.SCMRevisionState;
 import hudson.scm.SCM;
 import hudson.util.FormValidation;
 import hudson.util.VariableResolver;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Activity;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.utils.BaselineList;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.entities.Version;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
 import net.praqma.clearcase.ucm.view.UCMView;
 import net.praqma.clearcase.ucm.utils.BaselineDiff;
 import net.praqma.hudson.Config;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.hudson.scm.PucmState.State;
 import net.praqma.util.debug.Logger;
 import net.sf.json.JSONObject;
 //import net.praqma.hudson.Version;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.export.Exported;
 
 /**
  * CC4HClass is responsible for everything regarding Hudsons connection to
  * ClearCase pre-build. This class defines all the files required by the user.
  * The information can be entered on the config page.
  * 
  * @author Troels Selch S�rensen
  * @author Margit Bennetzen
  * 
  */
 public class PucmScm extends SCM
 {
 
 	private String levelToPoll;
 	private String loadModule;
 	private String component;
 	private String stream;
 	private boolean newest;
 	private Baseline bl;
 	private List<String> levels = null;
 	private List<String> loadModules = null;
 	//private BaselineList baselines;
 	private boolean compRevCalled;
 	private StringBuffer pollMsgs = new StringBuffer();
 	private Stream integrationstream;
 	private Component comp;
 	private SnapshotView sv = null;
 	private boolean doPostBuild = true;
 	private String buildProject;
 	
 	private String jobName = "";
 	private Integer jobNumber;
 	
 	private String id = "";
 
 	protected static Logger logger = Logger.getLogger();
 	
 	public static PucmState pucm = new PucmState();
 
 	/**
 	 * The constructor is used by Hudson to create the instance of the plugin
 	 * needed for a connection to ClearCase. It is annotated with
 	 * <code>@DataBoundConstructor</code> to tell Hudson where to put the
 	 * information retrieved from the configuration page in the WebUI.
 	 * 
 	 * @param component
 	 *            defines the component needed to find baselines.
 	 * @param levelToPoll
 	 *            defines the level to poll ClearCase for.
 	 * @param loadModule
 	 *            tells if we should load all modules or only the ones that are
 	 *            modifiable.
 	 * @param stream
 	 *            defines the stream needed to find baselines.
 	 * @param newest
 	 *            tells whether we should build only the newest baseline.
 	 * @param newerThanRecommended
 	 *            tells whether we should look at all baselines or only ones
 	 *            newer than the recommended baseline
 	 */
 	@DataBoundConstructor
 	public PucmScm( String component, String levelToPoll, String loadModule, String stream, boolean newest, boolean testing, String buildProject )
 	{
 		logger.trace_function();
 		logger.debug( "PucmSCM constructor" );
 		this.component = component;
 		this.levelToPoll = levelToPoll;
 		this.loadModule = loadModule;
 		this.stream = stream;
 		this.newest = newest;
 		this.buildProject = buildProject;
 	}
 
 	@Override
 	public boolean checkout( AbstractBuild build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile ) throws IOException, InterruptedException
 	{
 		logger.trace_function();
 		logger.debug( "PucmSCM checkout" );
 		boolean result = true;
 		
 		// consoleOutput Printstream is from Hudson, so it can only be accessed
 		// here
 		PrintStream consoleOutput = listener.getLogger();
 		consoleOutput.println( "[PUCM] Praqmatic UCM v." + net.praqma.hudson.Version.version + " - SCM section started" );
 		
 		/* Recalculate the states */
 		int count = pucm.recalculate( build.getProject() );
 		logger.info( "Removed " + count + " from states." );
 		
 		doPostBuild = true;
 
 		jobName   = build.getParent().getDisplayName();
 		jobNumber = build.getNumber();
 		
 		/* If we polled, we should get the same object created at that point */
 		State state = pucm.getState( jobName, jobNumber );
 		
 		logger.debug( id + "The initial state:\n" + state.stringify() );
 		
 		this.id = "[" + jobName + "::" + jobNumber + "]";
 		
 		if ( build.getBuildVariables().get( "include_classes" ) != null )
 		{
 			String[] is = build.getBuildVariables().get( "include_classes" ).toString().split( "," );
 			for( String i : is )
 			{
 				logger.includeClass( i.trim() );
 			}
 		}
 
 		if ( build.getBuildVariables().get( "pucm_baseline" ) != null )
 		{
 			Stream integrationstream = null;
 			String baselinename = (String) build.getBuildVariables().get( "pucm_baseline" );
 			try
 			{
 				state.setBaseline( UCMEntity.GetBaseline( baselinename ) );
 				integrationstream = bl.GetStream();
				state.setStream( state.getBaseline().GetStream() );
 				consoleOutput.println( "[PUCM] Starting parameterized build with a pucm_baseline.\nUsing baseline: " + baselinename + " from integrationstream " + integrationstream.GetShortname() );
 			}
 			catch ( UCMException e )
 			{
 				consoleOutput.println( "[PUCM] Could not find baseline from parameter." );
 				state.setPostBuild( false );
 				result = false;
 			}
 		}
 		else
 		{
 
 			// compRevCalled tells whether we have polled for baselines to build
 			// -
 			// so if we haven't polled, we do it now
 			if ( !compRevCalled )
 			{
 				try
 				{
 					//baselinesToBuild( component, stream, build.getProject(), build.getParent().getDisplayName(), build.getNumber() );
 					baselinesToBuild( build.getProject(), state );
 				}
 				catch ( ScmException e )
 				{
 					pollMsgs.append( e.getMessage() );
 					result = false;
 				}
 			}
 
 			compRevCalled = false;
 
 			// pollMsgs are set in either compareRemoteRevisionWith() or
 			// baselinesToBuild()
 			consoleOutput.println( pollMsgs );
 			pollMsgs = new StringBuffer();
 		}
 
 		if ( result )
 		{
 			try
 			{
 				/* Force the Baseline to be loaded */
 				try
 				{
 					bl.Load();
 				}
 				catch( UCMException e )
 				{
 					logger.debug( id + "Could not load Baseline" );
 					consoleOutput.println( "[PUCM] Could not load Baseline." );
 				}
 				
 				/* Check parameters */
 				if( listener == null )
 				{
 					consoleOutput.println( "[PUCM] Listener is null" );
 				}
 				
 				if( jobName == null )
 				{
 					consoleOutput.println( "[PUCM] jobname is null" );
 				}
 				
 				if( build == null )
 				{
 					consoleOutput.println( "[PUCM] BUILD is null" );
 				}
 				
 				if( stream == null )
 				{
 					consoleOutput.println( "[PUCM] stream is null" );
 				}
 				
 				if( loadModule == null )
 				{
 					consoleOutput.println( "[PUCM] loadModule is null" );
 				}
 				
 				if( buildProject == null )
 				{
 					consoleOutput.println( "[PUCM] buildProject is null" );
 				}
 				
 				if( bl == null )
 				{
 					consoleOutput.println( "[PUCM] bl is null" );
 				}
 				
 				CheckoutTask ct = new CheckoutTask( listener, jobName, build.getNumber(), stream, loadModule, bl.GetFQName(), buildProject );
 				String changelog = workspace.act( ct );
 			
 				/* Write change log */
 				try
 				{
 					FileOutputStream fos = new FileOutputStream( changelogFile );
 					fos.write( changelog.getBytes() );
 					fos.close();
 				}
 				catch( IOException e )
 				{
 					logger.debug( id + "Could not write change log file" );
 					consoleOutput.println( "[PUCM] Could not write change log file" );
 				}
 				
 				//doPostBuild = changelog.length() > 0 ? true : false;
 			}
 			catch ( Exception e )
 			{
 				consoleOutput.println( "[PUCM] An unknown error occured: " + e.getMessage() );
 				doPostBuild = false;
 				result = false;
 			}
 		}
 		
 		//pucm.add( new PucmState( build.getParent().getDisplayName(), build.getNumber(), bl, null, comp, doPostBuild ) );
 		//state.save();
 		
 		logger.debug( id + "The CO state:\n" + state.stringify() );
 
 		return result;
 	}
 	
 	
 
 	@Override
 	public ChangeLogParser createChangeLogParser()
 	{
 		logger.trace_function();
 		return new ChangeLogParserImpl();
 	}
 	
 	@Override
 	public PollingResult compareRemoteRevisionWith( AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState baseline ) throws IOException, InterruptedException
 	{
 		this.id = "[" + project.getDisplayName() + "::" + project.getNextBuildNumber() + "]";
 		
 		logger.trace_function();
 		logger.debug( id + "PucmSCM Pollingresult" );
 		
 		/* Make a state object, which is only temporary, only to determine if there's baselines to build this object will be stored in checkout  */
 		jobName   = project.getDisplayName();
 		jobNumber = project.getNextBuildNumber(); /* This number is not the final job number */
 		
 		State state = pucm.getState( jobName, jobNumber );
 
 		PollingResult p;
 		try
 		{
 			//baselinesToBuild( component, stream, project, project.getDisplayName(), project.getNextBuildNumber() );
 			baselinesToBuild( project, state );
 			compRevCalled = true;
 			logger.info( id + "Polling result = BUILD NOW" );
 			p = PollingResult.BUILD_NOW;
 		}
 		catch ( ScmException e )
 		{
 			logger.info( id + "Polling result = NO CHANGES" );
 			p = PollingResult.NO_CHANGES;
 			PrintStream consoleOut = listener.getLogger();
 			consoleOut.println( pollMsgs + "\n" + e.getMessage() );
 			pollMsgs = new StringBuffer();
 			logger.debug( id + "Removed job " + state.getJobNumber() + " from list" );
 			state.remove();
 		}
 		
 		logger.debug( id + "FINAL Polling result = " + p.change.toString() );
 		
 		return p;
 	}
 		
 
 	@Override
 	public SCMRevisionState calcRevisionsFromBuild( AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener ) throws IOException, InterruptedException
 	{
 		logger.trace_function();
 		logger.debug( id + "PucmSCM calcRevisionsFromBuild" );
 		// PrintStream hudsonOut = listener.getLogger();
 		SCMRevisionStateImpl scmRS = null;
 
 		if ( !( bl == null ) )
 		{
 			scmRS = new SCMRevisionStateImpl();
 		}
 		return scmRS;
 	}
 	
 
 	private void baselinesToBuild( AbstractProject<?, ?> project, State state ) throws ScmException
 	{
 		logger.trace_function();
 		
 		/* Store the component to the state */
 		try
 		{
 			state.setComponent( UCMEntity.GetComponent( component, false ) );
 		}
 		catch ( UCMException e )
 		{
 			throw new ScmException( "Could not get component. " + e.getMessage() );
 		}
 
 		/* Store the stream to the state */
 		try
 		{
 			state.setStream( UCMEntity.GetStream( stream, false ) );
 		}
 		catch ( UCMException e )
 		{
 			throw new ScmException( "Could not get stream. " + e.getMessage() );
 		}
 		
 		state.setPlevel( Project.Plevel.valueOf( levelToPoll ) );
 		
 		/* The baseline list */
 		BaselineList baselines = null;
 
 		try
 		{
 			pollMsgs.append( "[PUCM] Getting all baselines for :\n[PUCM] * Stream:         " );
 			pollMsgs.append( stream );
 			pollMsgs.append( "\n[PUCM] * Component:      " );
 			pollMsgs.append( component );
 			pollMsgs.append( "\n[PUCM] * Promotionlevel: " );
 			pollMsgs.append( levelToPoll );
 			pollMsgs.append( "\n" );
 
 			baselines = state.getComponent().GetBaselines( state.getStream(), state.getPlevel() );
 		}
 		catch ( UCMException e )
 		{
 			throw new ScmException( "Could not retrieve baselines from repository. " + e.getMessage() );
 		}
 
 
 		if ( baselines.size() > 0 )
 		{
 			printBaselines( baselines );
 			
 			logger.debug( id + "PUCM=" + pucm.stringify() );
 
 			try
 			{
 				List<Baseline> baselinelist = state.getStream().GetRecommendedBaselines();
 				pollMsgs.append( "\n[PUCM] Recommended baseline(s): \n" );
 				for ( Baseline b : baselinelist )
 				{
 					pollMsgs.append( b.GetShortname() + "\n" );
 				}
 				
 				/* Determine the baseline to build */
 				
 				bl = null;
 				state.setBaseline( null );
 				/* For each baseline retrieved from ClearCase */
 				//for ( Baseline b : baselinelist )
 				int start = 0;
 				int stop = baselines.size();
 				int increment = 1;
 				if( newest )
 				{
 					start = baselines.size() - 1;
 					//stop = 0;
 					increment = -1;
 				}
 				
 				/* Find the Baseline to build */
 				for( int i = start ; i < stop && i >= 0 ; i += increment )
 				{
 					/* The current baseline */
 					Baseline b = baselines.get( i );
 					
 					State cstate = pucm.getStateByBaseline( jobName, b.GetFQName() );
 					
 					/* The baseline is in progress, determine if the job is still running */
 					//if( thisJob.containsKey( b.GetFQName() ) )
 					if( cstate != null )
 					{
 						//Integer bnum = thisJob.get( b.GetFQName() );
 						//State
 						Integer bnum = cstate.getJobNumber();
 						Object o = project.getBuildByNumber( bnum );
 						Build bld = (Build)o;
 						/* The job is not running */
 						if( !bld.isLogUpdated() )
 						{
 							logger.debug( id + "Job " + bld.getNumber() + " is not building, using baseline: " + b );
 							bl = b;
 							//thisJob.put( b.GetFQName(), state.getJobNumber() );
 							break;
 						}
 						else
 						{
 							logger.debug( id + "Job " + bld.getNumber() + " is building " + cstate.getBaseline().GetFQName() );
 						}
 					}
 					/* The baseline is available */
 					else
 					{
 						bl = b;
 						//thisJob.put( b.GetFQName(), state.getJobNumber() );
 						logger.debug( id + "The baseline " + b + " is available" );
 						break;
 					}
 				}
 				
 				if( bl == null )
 				{
 					logger.log( id + "No baselines available on chosen parameters." );
 					throw new ScmException( "No baselines available on chosen parameters." );
 				}
 				
 				pollMsgs.append( "\n[PUCM] Building baseline: " + bl + "\n" );
 				
 				state.setBaseline( bl );
 				
 				/* Store the baseline to build */
 				//thisJob.put( bl.GetFQName(), state.getJobNumber() );
 			}
 			catch ( UCMException e )
 			{
 				throw new ScmException( "Could not get recommended baselines. " + e.getMessage() );
 			}
 		}
 		else
 		{
 			throw new ScmException( "No baselines on chosen parameters." );
 		}
 		
 		//logger.debug( id + "PRINTING THIS JOB:" );
 		//logger.debug( net.praqma.util.structure.Printer.mapPrinterToString( thisJob ) );
 	}
 
 	private void printBaselines( BaselineList baselines )
 	{
 		pollMsgs.append( "[PUCM] Retrieved baselines:\n" );
 		if ( !( baselines.size() > 20 ) )
 		{
 			for ( Baseline b : baselines )
 			{
 				pollMsgs.append( b.GetShortname() );
 				pollMsgs.append( "\n" );
 			}
 		}
 		else
 		{
 			int i = baselines.size();
 			pollMsgs.append( "[PUCM] There are " + i + " baselines - only printing first and last three\n" );
 			pollMsgs.append( baselines.get( 0 ).GetShortname() + "\n" );
 			pollMsgs.append( baselines.get( 1 ).GetShortname() + "\n" );
 			pollMsgs.append( baselines.get( 2 ).GetShortname() + "\n" );
 			pollMsgs.append( "...\n" );
 			pollMsgs.append( baselines.get( i - 3 ).GetShortname() + "\n" );
 			pollMsgs.append( baselines.get( i - 2 ).GetShortname() + "\n" );
 			pollMsgs.append( baselines.get( i - 1 ).GetShortname() + "\n" );
 		}
 	}
 
 	/*
 	 * The following getters and booleans (six in all) are used to display saved
 	 * userdata in Hudsons gui
 	 */
 
 	public String getLevelToPoll()
 	{
 		logger.trace_function();
 		return levelToPoll;
 	}
 
 	public String getComponent()
 	{
 		logger.trace_function();
 		return component;
 	}
 
 	public String getStream()
 	{
 		logger.trace_function();
 		return stream;
 	}
 
 	public String getLoadModule()
 	{
 		logger.trace_function();
 		return loadModule;
 	}
 
 	public boolean isNewest()
 	{
 		logger.trace_function();
 		return newest;
 	}
 
 	/*
 	 * getStreamObject() and getBaseline() are used by PucmNotifier to get the
 	 * Baseline and Stream in use
 	 */
 
 	public Stream getStreamObject()
 	{
 		logger.trace_function();
 		return integrationstream;
 	}
 
 	@Exported
 	public Baseline getBaseline()
 	{
 		logger.trace_function();
 		return bl;
 	}
 
 	@Exported
 	public boolean doPostbuild()
 	{
 		logger.trace_function();
 		return doPostBuild;
 	}
 
 	public String getBuildProject()
 	{
 		logger.trace_function();
 		return buildProject;
 	}
 
 	/**
 	 * This class is used to describe the plugin to Hudson
 	 * 
 	 * @author Troels Selch S�rensen
 	 * @author Margit Bennetzen
 	 * 
 	 */
 	@Extension
 	public static class PucmScmDescriptor extends SCMDescriptor<PucmScm>
 	{
 
 		private String cleartool;
 		private List<String> loadModules;
 
 		public PucmScmDescriptor()
 		{
 			super( PucmScm.class, null );
 			logger.trace_function();
 			loadModules = getLoadModules();
 			load();
 			Config.setContext();
 		}
 
 		/**
 		 * This method is called, when the user saves the global Hudson
 		 * configuration.
 		 */
 		@Override
 		public boolean configure( org.kohsuke.stapler.StaplerRequest req, JSONObject json ) throws FormException
 		{
 			logger.trace_function();
 			cleartool = req.getParameter( "PUCM.cleartool" ).trim();
 			save();
 			return true;
 		}
 
 		/**
 		 * This is called by Hudson to discover the plugin name
 		 */
 		@Override
 		public String getDisplayName()
 		{
 			logger.trace_function();
 			return "Praqmatic UCM";
 		}
 
 		/**
 		 * This method is called by the scm/Pucm/global.jelly to validate the
 		 * input without reloading the global configuration page
 		 * 
 		 * @param value
 		 * @return
 		 */
 		public FormValidation doExecutableCheck( @QueryParameter String value )
 		{
 			logger.trace_function();
 			return FormValidation.validateExecutable( value );
 		}
 
 		/**
 		 * Called by Hudson. If the user does not input a command for Hudson to
 		 * use when polling, default value is returned
 		 * 
 		 * @return
 		 */
 		public String getCleartool()
 		{
 			logger.trace_function();
 			if ( cleartool == null || cleartool.equals( "" ) )
 			{
 				return "cleartool";
 			}
 			return cleartool;
 		}
 
 		/**
 		 * Used by Hudson to display a list of valid promotion levels to build
 		 * from. The list of promotion levels is hard coded in
 		 * net.praqma.hudson.Config.java
 		 * 
 		 * @return
 		 */
 		public List<String> getLevels()
 		{
 			logger.trace_function();
 			return Config.getLevels();
 		}
 
 		/**
 		 * Used by Hudson to display a list of loadModules (whether to poll all
 		 * or only modifiable elements
 		 * 
 		 * @return
 		 */
 		public List<String> getLoadModules()
 		{
 			logger.trace_function();
 			loadModules = new ArrayList<String>();
 			loadModules.add( "All" );
 			loadModules.add( "Modifiable" );
 			return loadModules;
 		}
 
 	}
 }
