 package net.praqma.jenkins.plugin.ava;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.util.debug.appenders.StreamAppender;
 import net.praqma.vcs.AVA;
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
 import net.praqma.vcs.util.configuration.AbstractConfiguration;
 import net.praqma.vcs.util.configuration.exception.ConfigurationException;
 import net.praqma.vcs.util.configuration.implementation.ClearCaseConfiguration;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.VirtualChannel;
 
 public class RemoteSetup implements FileCallable<Boolean> {
 	
 	private static final long serialVersionUID = -4591556492458099617L;
 	
 	private AbstractConfiguration source;
 	private AbstractConfiguration target;
 	
 	private BuildListener listener;
 	private String workspacePathName;
 
 	public RemoteSetup( BuildListener listener, AbstractConfiguration source, AbstractConfiguration target, String workspacePathName ) {
 		this.source = source;
 		this.target = target;
 		
 		this.listener = listener;
 		this.workspacePathName = workspacePathName;
 	}
 
 	public Boolean invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {
 		PrintStream out = listener.getLogger();
 		
 		StreamAppender app = new StreamAppender( out );
 		app.setTemplate( "[%level]%space %message%newline" );
 		net.praqma.util.debug.Logger.addAppender( app );
 		app.setMinimumLevel( net.praqma.util.debug.Logger.LogLevel.DEBUG );
 		
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
 			
			/* Try */
			if( source instanceof ClearCaseConfiguration ) {
				ClearCaseConfiguration ccc = (ClearCaseConfiguration)source;
				ccc.setParentStream( ccc.getFoundationBaseline().getStream() );
				out.println( "[AVA] Setting output stream to " + ccc.getStreamName() );
			}
			
 			out.println( "[AVA] Generating target branch" );
 			try {
 				target.generate();
 			} catch (ConfigurationException e) {
 				e.printStackTrace();
 				throw new IOException( "Could not generate target: " + e.getMessage() );
 			}
 			
 			out.println( "[AVA] Source configuration:\n" + source.toString() );
 			out.println( "[AVA] Target configuration:\n" + target.toString() );
 			
 			out.println( "[AVA] Getting source branch" );
 			AbstractBranch sourceBranch = this.source.getBranch();
 			
 			out.println( "[AVA] Source branch: " );
 			out.println( sourceBranch.toString() );
 	
 			out.println( "[AVA] Getting replay" );
 			AbstractReplay replay = target.getReplay();
 			
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
 		/* ClearCase UCM */
 		if( config instanceof ClearCaseConfiguration ) {
 			ClearCaseConfiguration ccc = (ClearCaseConfiguration)config;
 			/* Determine missing options */
 			if( ccc.getPathName().length() == 0 ) {
 				File path = new File( workspacePathName );
 				if( path.exists()) {
 					try {
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
 		}
 		
 		return config;
 	}
 
 }
