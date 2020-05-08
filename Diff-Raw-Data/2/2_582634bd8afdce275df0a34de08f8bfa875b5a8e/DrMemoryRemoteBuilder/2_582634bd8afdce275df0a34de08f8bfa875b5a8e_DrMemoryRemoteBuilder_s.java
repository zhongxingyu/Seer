 package net.praqma.jenkins.plugin.drmemory;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import net.praqma.drmemory.DrMemory;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.VirtualChannel;
 
 public class DrMemoryRemoteBuilder implements FileCallable<Boolean> {
 	
 	private String executable;
 	private String arguments;
 	private String logPath;
 	
 	private BuildListener listener;
 
 	public DrMemoryRemoteBuilder( String executable, String arguments, String logPath, BuildListener listener ) {
 		this.executable = executable;
 		this.arguments = arguments;
 		this.logPath = logPath;
 		
 		this.listener = listener;
 	}
 
 	@Override
 	public Boolean invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {
 		
 		PrintStream out = listener.getLogger();
 		
 		File executable = new File( workspace, this.executable );
 		out.println( "Executing " + executable + " " + arguments );
 		DrMemory dm = new DrMemory( executable, arguments );                
 		
 		File logpath = new File( workspace, this.logPath );
 		logpath.mkdirs();
 		out.println( "Setting log path to " + logpath );
 		dm.setLogDir( logpath );
 		
 		try {
 			dm.start();
 		} catch( Exception e ) {
 			throw new IOException(String.format( "Dr. Memory retuned with a non-zero exit code. Message was:%n%s",e.getMessage()) );
 		}
 		
 		return true;
 	}
 	
 }
