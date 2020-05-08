 package net.praqma.drmemory;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 
 import net.praqma.drmemory.exceptions.NoSuchResultException;
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.Logger.LogLevel;
 import net.praqma.util.debug.appenders.ConsoleAppender;
 import net.praqma.util.execute.CmdResult;
 import net.praqma.util.execute.CommandLine;
 import net.praqma.util.execute.CommandLineInterface;
 
 public class DrMemory {
 
 	private File application;
 	private String parameters = "";
 	
 	private static String drmemory = "drmemory.exe";
 	
 	private File logDir;
 	
 	private static boolean skipRun = false;
 	private boolean quiet = true;
 	
 	private CommandLineInterface cli = CommandLine.getInstance();
 	
 	private static Logger logger = Logger.getLogger();
 
 	public DrMemory() {
 	}
 	
 	public DrMemory( File application ) {
 		this.application = application;
 	}
 	
 	public DrMemory( File application, String parameters ) {
 		this.application = application;
 		this.parameters = parameters;
 	}
 	
 	public static void enableLogging() {
 		ConsoleAppender app = new ConsoleAppender();
 		app.setMinimumLevel( LogLevel.DEBUG );
 		Logger.addAppender( app );
 	}
 	
 	public static void setExecutable( String drmemory ) {
 		DrMemory.drmemory = drmemory;
 	}
 	
 	public void setApplication( String application ) {
 		this.application = new File( application );
 	}
 	
 	public void setLogDir( File dir ) {
 		this.logDir = dir;
 		
 		logger.debug( "Logs are at " + this.logDir );
 	}
 	
 	public File getResultFile() throws NoSuchResultException {
 		
 		/* Get dirs in path */
 		File[] paths = logDir.listFiles( new FilenameFilter() {
 			public boolean accept( File file, String name ) {
 				return file.isDirectory() && !name.matches( "\\.{1,2}" );
 			}
 		} );
 		
 		/* For each path under log dir */
 		for( File path : paths ) {
 			File[] files = path.listFiles( new FilenameFilter() {
 				public boolean accept( File file, String name ) {
 					return file.isFile() && name.equalsIgnoreCase( "result.txt" );
 				}
 			} );
 			
 			/* If there exists one result, use it */
 			if( files.length > 0 ) {
 				return files[0];
 			}
 		}
 		
 		throw new NoSuchResultException();
 	}
 	
 	public void start() throws IOException {
		String cmd = drmemory + ( logDir != null ? " -logdir \"" + logDir + "\"" : "" ) + " -batch" + ( quiet ? " -quiet" : "" ) + " -- \"" + application + "\" " + parameters;
 		logger.debug( "CMD: " + cmd );
 		CmdResult result = null;
 		try {
 			if( !skipRun ) {
 				result = cli.run( cmd );
 			}
 		} catch( Exception e ) {
 			throw new IOException( "Unable to execute " + cmd + ": " + e.getMessage() );
 		}
 	}
 	
 	public static void skipRun() {
 		skipRun = true;
 	}
 
 }
