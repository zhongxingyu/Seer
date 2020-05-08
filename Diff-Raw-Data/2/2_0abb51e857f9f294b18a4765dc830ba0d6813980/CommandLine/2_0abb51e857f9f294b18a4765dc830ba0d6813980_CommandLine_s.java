 package net.praqma.util.execute;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * CLI class
  * 
  * @author wolfgang
  * 
  */
 public class CommandLine implements CommandLineInterface {
 	protected Logger logger = Logger.getLogger( CommandLine.class.getName() );
 	protected static final String linesep = System.getProperty( "line.separator" );
 
 	private static CommandLine instance = new CommandLine();
 
 	private String os = null;
 	private OperatingSystem thisos = OperatingSystem.WINDOWS;
 	private String[] cmd = null;
 	private int last = 0;
 
 	private static Recorder recorder;
 	
 	public void setRecorder( Recorder recorder ) {
 		this.recorder = recorder;
 	}
 
 	private CommandLine() {
 		os = System.getProperty( "os.name" );
 		logger.finer( "Running on " + os );
 		if( os.matches( "^.*(?i)windows.*$" ) ) {
 			logger.finer( "Using special windows environment" );
 			cmd = new String[3];
 			cmd[0] = "cmd.exe";
 			cmd[1] = "/C";
 			last = 2;
 		} else {
 			thisos = OperatingSystem.UNIX;
 			cmd = new String[3];
 			cmd[0] = "bash";
 			cmd[1] = "-c";
 			last = 2;
 		}
 	}
 
 	public OperatingSystem getOS() {
 		return thisos;
 	}
 
 	public static CommandLine getInstance() {
 		return instance;
 	}
 
 	public CmdResult run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException {
 		return run( cmd, null, true, false, null );
 	}
 
 	public CmdResult run( String cmd, File dir ) throws CommandLineException, AbnormalProcessTerminationException {
 		return run( cmd, dir, true, false, null );
 	}
 
 	public CmdResult run( String cmd, File dir, boolean merge ) throws CommandLineException, AbnormalProcessTerminationException {
 		return run( cmd, dir, merge, false, null );
 	}
 
 	public CmdResult run( String cmd, File dir, boolean merge, boolean ignore ) throws CommandLineException, AbnormalProcessTerminationException {
 		return run( cmd, dir, merge, ignore, null );
 	}
 
 	/**
 	 * Execute a command line operation.
 	 * 
 	 * @param cmd
 	 *            The command itself
 	 * @param dir
 	 *            The working directory
 	 * @param merge
 	 *            Merge stderror with stdout
 	 * @param ignore
 	 *            Ignore any abnormal process terminations. This will allow the
 	 *            output to be returned without exceptions to be thrown.
 	 * @return
 	 * @throws CommandLineException
 	 * @throws AbnormalProcessTerminationException
 	 */
     @Override
 	public synchronized CmdResult run( String cmd, File dir, boolean merge, boolean ignore, Map<String, String> variables ) throws CommandLineException, AbnormalProcessTerminationException {
 		/*
 		 * String[] cmds = new String[3]; cmds[0] = "cmd.exe"; cmds[1] = "/C";
 		 * cmds[2] = cmd;
 		 */
 
 		// cmd = this.cmd + cmd;
 		this.cmd[last] = cmd;
 		
 		logger.config( "$ " + cmd );
 
 		try {
 			ProcessBuilder pb = new ProcessBuilder( this.cmd );
 			pb.redirectErrorStream( merge );
 			// pb.environment().put( key, value )
 
 			if( dir != null ) {
 				logger.config( "Executing command in " + dir );
 				pb.directory( dir );
 			}
 
 			/* If any variables, put them in the environment */
 			if( variables != null && variables.size() > 0 ) {
 				logger.fine( "CommandLine: " + variables );
 				Map<String, String> env = pb.environment();
 				Set<String> keys = variables.keySet();
 				for( String key : keys ) {
 					env.put( key, variables.get( key ) );
 				}
 			}
 
 			CmdResult result = new CmdResult();
 			Process p = pb.start();
 
 			/* Starting Gobbler threads */
 			StreamGobbler output = new StreamGobbler( p.getInputStream() );
 			StreamGobbler errors = new StreamGobbler( p.getErrorStream() );
 			p.getOutputStream().close();
 
 			output.start();
 			errors.start();
 
 			int exitValue = 0;
 			try {
 				exitValue = p.waitFor();
 			} catch( InterruptedException e ) {
 				p.destroy();
 			} finally {
 				Thread.interrupted();
 			}
 
 			try {
 				output.join();
 			} catch( InterruptedException e ) {
 				logger.severe( "Could not join output thread" );
 			}
 
 			try {
 				errors.join();
 			} catch( InterruptedException e ) {
 				logger.severe( "Could not join errors thread" );
 			}
 
 			/* Closing streams */
 			p.getErrorStream().close();
 			p.getInputStream().close();
 
 			
 			/* If enabled, record the command */
 			if( recorder != null ) {
 				if( merge ) {
 					recorder.addCommand( cmd, exitValue, dir, output.sres.toString() );
 				} else {
 					recorder.addCommand( cmd, exitValue, dir, errors.sres.toString() );
 				}
 			}
 
 			/* Abnormal process termination, with error out as message */
 			if( exitValue != 0 ) {
				logger.fine( "Abnormal process termination(" + exitValue + "): " + output.sres.toString() );
 
 				/*
 				 * Only throw the exception if it is not ignored, this is
 				 * default
 				 */
 				if( !ignore ) {
 					if( merge ) {
 						throw new AbnormalProcessTerminationException( output.sres.toString(), cmd, exitValue );
 					} else {
 						throw new AbnormalProcessTerminationException( errors.sres.toString(), cmd, exitValue );
 					}
 				}
 			}
 
 			/* Setting command result */
 			result.stdoutBuffer = output.sres;
 			result.stdoutList = output.lres;
 
 			result.errorBuffer = errors.sres;
 			result.errorList = errors.lres;
 
 			return result;
 		} catch( IOException e ) {
 			logger.warning( "Could not execute the command \"" + cmd + "\" correctly: " + e.getMessage() );
 			throw new CommandLineException( "Could not execute the command \"" + cmd + "\" correctly: " + e.getMessage() );
 		}
 	}
 
 }
