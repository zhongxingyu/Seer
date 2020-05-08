 package net.praqma.hudson.remoting;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Set;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.LoggerSetting;
 import net.praqma.util.debug.appenders.StreamAppender;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.Pipe;
 import hudson.remoting.VirtualChannel;
 
 public class CreateRemoteBaseline implements FileCallable<Baseline> {
 
 	private static final long serialVersionUID = -8984877325832486334L;
 
 	private String baseName;
 	private Component component;
 	private File view;
 	private BuildListener listener;
 	private String username;
 	private Pipe pipe;
 	private LoggerSetting loggerSetting;
 	private PrintStream pstream;
 
 	public CreateRemoteBaseline( String baseName, Component component, File view, String username, BuildListener listener, Pipe pipe, PrintStream pstream, LoggerSetting loggerSetting ) {
 		this.baseName = baseName;
 		this.component = component;
 		this.view = view;
 		this.listener = listener;
 		this.username = username;
 		this.pipe = pipe;
 		this.loggerSetting = loggerSetting;
 		this.pstream = pstream;
     }
 
     @Override
     public Baseline invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
         PrintStream out = listener.getLogger();
 
     	StreamAppender app = null;
     	if( pipe != null ) {
 	    	PrintStream toMaster = new PrintStream( pipe.getOut() );
 	    	app = new StreamAppender( toMaster );
 	    	app.lockToCurrentThread();
 	    	Logger.addAppender( app );
 	    	app.setSettings( loggerSetting );
     	} else if( pstream != null ) {
 	    	app = new StreamAppender( pstream );
 	    	app.lockToCurrentThread();
 	    	Logger.addAppender( app );
 	    	app.setSettings( loggerSetting );    		
     	}
 
     	Baseline bl = null;
     	try {
			bl = Baseline.create( baseName, component, view, true, false );
 		} catch (UCMException e) {
         	Logger.removeAppender( app );
         	throw new IOException( "Unable to create Baseline:" + e.getMessage() );
 		}
     	
     	Logger.removeAppender( app );
 
     	return bl;
     }
 
 }
