 package net.praqma.jenkins.plugin.ava;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.util.debug.appenders.StreamAppender;
 import net.praqma.vcs.AVA;
 import net.praqma.vcs.VersionControlSystems;
 import net.praqma.vcs.model.AbstractBranch;
 import net.praqma.vcs.model.AbstractReplay;
 import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
 import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
 import net.praqma.vcs.model.exceptions.UnableToCheckoutCommitException;
 import net.praqma.vcs.model.exceptions.UnableToReplayException;
 import net.praqma.vcs.model.exceptions.UnsupportedBranchException;
 import net.praqma.vcs.persistence.XMLStrategy;
 import net.praqma.vcs.util.ClearCaseUCM;
 import net.praqma.vcs.util.Cycle;
 import net.praqma.vcs.util.VCS;
 import net.praqma.vcs.util.configuration.AbstractConfiguration;
 import net.praqma.vcs.util.configuration.exception.ConfigurationException;
 import net.praqma.vcs.util.configuration.implementation.ClearCaseConfiguration;
 import net.praqma.vcs.util.configuration.implementation.MercurialConfiguration;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.VirtualChannel;
 
 public class RemoteSetup implements FileCallable<Boolean> {
 	
 	private static final long serialVersionUID = -4591556492458099617L;
 	
 	private AbstractConfiguration source;
 	private AbstractConfiguration target;
 	
 	private BuildListener listener;
 	private String workspacePathName;
 	
 	private boolean printDebug;
 
 	public RemoteSetup( BuildListener listener, AbstractConfiguration source, AbstractConfiguration target, String workspacePathName, boolean printDebug ) {
 		this.source = source;
 		this.target = target;
 		
 		this.listener = listener;
 		this.workspacePathName = workspacePathName;
 		
 		this.printDebug = printDebug;
 	}
 
 	public Boolean invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {
 		PrintStream out = listener.getLogger();
 		
 		StreamAppender app = new StreamAppender( out );
 		app.setTemplate( "[%level]%space %message%newline" );
 		net.praqma.util.debug.Logger.addAppender( app );
 		if( printDebug ) {
 			app.setMinimumLevel( net.praqma.util.debug.Logger.LogLevel.DEBUG );
 		} else {
 			app.setMinimumLevel( net.praqma.util.debug.Logger.LogLevel.INFO );
 		}
 		
 		/* TODO Somehow detect clearcase configuration */
 		UCM.setContext( UCM.ContextType.CLEARTOOL );
 		
 		File p = new File( "ava.xml" );
 		out.println( "AVA:XML: " + p.getAbsolutePath() );
 		p.createNewFile();
 		try {
 			new AVA( new XMLStrategy( p ) );
 		} catch( IllegalStateException e ) {
 			/* Whoops, AVA already defined */
 		}
 		
 		this.source = checkConfiguration( out, source, workspacePathName, false );
 		this.target = checkConfiguration( out, target, workspacePathName, true );
 		
 		try {
 			out.println( "[AVA] Generating source branch" );
 			try {
 				source.generate();
 			} catch (ConfigurationException e) {
 				e.printStackTrace();
 				throw new IOException( "Could not generate source: " + e.getMessage() );
 			}
 			
 			/* Try to set the parent of the source */
 			if( source instanceof ClearCaseConfiguration ) {
 				ClearCaseConfiguration ccc = (ClearCaseConfiguration)source;
 				ccc.setParentStream( ccc.getFoundationBaseline().getStream() );
 				out.println( "[AVA] Setting source parent stream to " + ccc.getStreamName() );
 			}
 			
 			out.println( "[AVA] Generating target branch" );
 			try {
 				target.generate();
 			} catch (ConfigurationException e) {
 				e.printStackTrace();
 				throw new IOException( "Could not generate target: " + e.getMessage() );
 			}
 			
 			/* Try to set the parent of the target */
 			if( target instanceof ClearCaseConfiguration ) {
 				ClearCaseConfiguration ccc = (ClearCaseConfiguration)target;
 				ccc.setParentStream( ccc.getFoundationBaseline().getStream() );
 				out.println( "[AVA] Setting source parent stream to " + ccc.getStreamName() );
 			}
 			
 			out.println( "[AVA] Source configuration: " + source.toString() );
 			out.println( "[AVA] Target configuration: " + target.toString() );
 			
 			AbstractBranch sourceBranch = this.source.getBranch();
 			
 			out.println( "[AVA] Source branch: " + sourceBranch.toString() );
 			
 			AbstractReplay replay = target.getReplay();
 			
 			out.println( "[AVA] Target branch: " + target.getBranch().toString() );
 			
 			out.println( "[AVA] Initializing cycle" );
 			Cycle.cycle( sourceBranch, replay, null );
 		} catch (ElementNotCreatedException e) {
 			e.printStackTrace();
 			throw new IOException( "Some elements were not created: " + e.getMessage() );
 		} catch (ElementDoesNotExistException e) {
 			e.printStackTrace();
 			throw new IOException( "Some elements were not found: " + e.getMessage() );
 		} catch (UnableToCheckoutCommitException e) {
 			e.printStackTrace();
 			throw new IOException( "Could not checkout commit: " + e.getMessage() );
 		} catch (UnableToReplayException e) {
 			e.printStackTrace();
 			throw new IOException( "Could not replay: " + e.getMessage() );
 		} catch (UnsupportedBranchException e) {
 			e.printStackTrace();
 			throw new IOException( "UNSUPPORTED BRANCH: " + e.getMessage() );
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new IOException( e.getMessage() );
 		}
 		
 		return true;
 	}
 	
 	private AbstractConfiguration checkConfiguration( PrintStream out, AbstractConfiguration config, String workspacePathName, boolean input ) throws IOException {
 		if( config != null ) {
 			/* ClearCase UCM */
 			if( config instanceof ClearCaseConfiguration ) {
 				ClearCaseConfiguration ccc = (ClearCaseConfiguration)config;
 				/* Determine missing options */
 				if( ccc.getPathName().length() == 0 ) {
 					File path = new File( workspacePathName );
 					if( path.exists() ) {
 						try {
 							/* Generate new configuration based on workspace */
 							out.println( "[AVA] Generating configuration based on workspace" );
 							config = ClearCaseUCM.getConfigurationFromView( path, input );
 							ccc = (ClearCaseConfiguration) config;
 							ccc.iDontCare();
 						} catch ( Exception e ) {
 							e.printStackTrace();
 							throw new IOException( "Unable to get view from workspace: " + e.getMessage() );
 						}
 					} else {
 						throw new IOException( "Could not determine path name of configuration" );
 					}
 				} else {
 					File path = new File( config.getPathName() );
 					try {
 						config = ClearCaseUCM.getConfigurationFromView( path, input );
 					} catch ( Exception e ) {
 						e.printStackTrace();
 						throw new IOException( "Unable to get view from path: " + e.getMessage() );
 					}
 				}
 				
 			/* Mercurial */
 			} else if( config instanceof MercurialConfiguration ) {
 				MercurialConfiguration mc = (MercurialConfiguration)config;
 				if( mc.getPathName().length() == 0 ) {
 					out.println( "[AVA] USING HG WS" );
 					File path = new File( workspacePathName );
 					if( path.exists()) {
 						mc.setPathName( workspacePathName );
 						out.println( "[AVA] Setting path to " + path );
 					}
 				}
 			}
 		} else {
 			File path = new File( workspacePathName );
 			VersionControlSystems vcs = VCS.determineVCS( path );
 			out.println( "[AVA] Generating " + vcs + " configuration based on workspace" );
 			switch( vcs ) {
 			case ClearCase:
 				try {
 					config = ClearCaseUCM.getConfigurationFromView( path, input );
 				} catch (Exception e) {
 					out.println( "Unable to create ClearCase configuration: " + e.getMessage() );
 					e.printStackTrace();
 				}
 				break;
 				
 			case Mercurial:
 				config = new MercurialConfiguration( path, null );
 				break;
 			}
 		}
 		
 		return config;
 	}
 
 }
