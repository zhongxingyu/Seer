 package com.patrick_vane.unrealscript.editor.console;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 
 public class UDKLaunchLogConsolePrintStream extends PrintStream
 {
 	private static final String		LAUNCH_LOG_TAG					= "ScriptLog: ";
 	private static final int		LAUNCH_LOG_TAG_LENGTH			= LAUNCH_LOG_TAG.length();
	private static final String		LAUNCH_WARNING1_TAG				= "ScriptWarning: ";
 	private static final int		LAUNCH_WARNING1_TAG_LENGTH		= LAUNCH_WARNING1_TAG.length();
	private static final String		LAUNCH_WARNING2_TAG				= "Warning: ";
 	private static final int		LAUNCH_WARNING2_TAG_LENGTH		= LAUNCH_WARNING2_TAG.length();
	private static final String		LAUNCH_ERROR1_TAG				= "ScriptError: ";
 	private static final int		LAUNCH_ERROR1_TAG_LENGTH		= LAUNCH_ERROR1_TAG.length();
	private static final String		LAUNCH_ERROR2_TAG				= "Error: ";
 	private static final int		LAUNCH_ERROR2_TAG_LENGTH		= LAUNCH_ERROR2_TAG.length();
 	
 	private int						logMode							= 0;
 	
 	
 	public UDKLaunchLogConsolePrintStream()
 	{
 		super
 		(
 			new OutputStream()
 			{
 				@Override
 				public void write( int b ) throws IOException
 				{
 				}
 			}
 		);
 	}
 	
 	
 	@Override
 	public void println( String line )
 	{
 		synchronized( UDKLaunchLogConsole.getSynchronizer() )
 		{
 			if( line.startsWith("Running: ") )
 			{
 				UDKLaunchLogConsole.out.println( line );
 				return;
 			}
 			
 			int logCharAt 		= line.indexOf( LAUNCH_LOG_TAG );
 			int warning1CharAt 	= line.indexOf( LAUNCH_WARNING1_TAG );
 			int warning2CharAt 	= line.indexOf( LAUNCH_WARNING2_TAG );
 			int error1CharAt 	= line.indexOf( LAUNCH_ERROR1_TAG );
 			int error2CharAt 	= line.indexOf( LAUNCH_ERROR2_TAG );
 			
 			String prefix = "";
 			if( logCharAt >= 0 )
 			{
 				logMode = 1;
 				if( logCharAt > 0 )
 					prefix = line.substring( 0, logCharAt ) + " ";
 				UDKLaunchLogConsole.out.println( prefix + "[INFO] " + line.substring(logCharAt+LAUNCH_LOG_TAG_LENGTH) );
 			}
 			else if( warning1CharAt >= 0 )
 			{
 				logMode = 2;
 				if( warning1CharAt > 0 )
 					prefix = line.substring( 0, warning1CharAt ) + " ";
 				UDKLaunchLogConsole.warn.println( prefix + "[WARNING] " + line.substring(warning1CharAt+LAUNCH_WARNING1_TAG_LENGTH) );
 			}
 			else if( warning1CharAt >= 0 )
 			{
 				logMode = 2;
 				if( warning2CharAt > 0 )
 					prefix = line.substring( 0, warning2CharAt ) + " ";
 				UDKLaunchLogConsole.warn.println( prefix + "[WARNING] " + line.substring(warning2CharAt+LAUNCH_WARNING2_TAG_LENGTH) );
 			}
 			else if( error1CharAt >= 0 )
 			{
 				logMode = 3;
 				if( error1CharAt > 0 )
 					prefix = line.substring( 0, error1CharAt ) + " ";
 				UDKLaunchLogConsole.err.println( prefix + "[ERROR] " + line.substring(error1CharAt+LAUNCH_ERROR1_TAG_LENGTH) );
 			}
 			else if( error2CharAt >= 0 )
 			{
 				logMode = 3;
 				if( error2CharAt > 0 )
 					prefix = line.substring( 0, error2CharAt ) + " ";
 				UDKLaunchLogConsole.err.println( prefix + "[ERROR] " + line.substring(error2CharAt+LAUNCH_ERROR2_TAG_LENGTH) );
 			}
 			else
 			{
 				if( logMode > 0 )
 				{
 					if( line.startsWith("\t") )
 					{
 						if( logMode == 1 )
 							UDKLaunchLogConsole.out.println( line );
 						else if( logMode == 2 )
 							UDKLaunchLogConsole.warn.println( line );
 						else if( logMode == 3 )
 							UDKLaunchLogConsole.err.println( line );
 					}
 					else
 					{
 						logMode = 0;
 					}
 				}
 			}
 		}
 	}
 }
