 package net.praqma;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 
 
 /**
  * A simple logger class.
  * @author wolfgang
  *
  */
 class Debug
 {
 	private static Debug logger               = null;
 	private static FileWriter fw              = null;
 	private static BufferedWriter out         = null;
 	private static String path                = "./";
 	private static SimpleDateFormat format    = null;
 	private static SimpleDateFormat logformat = null;
 	private static Calendar nowDate           = null;
 	
 	private static boolean enabled            = true;
 	
 	private static final String filesep       = System.getProperty( "file.separator" );
 	private static final String linesep       = System.getProperty( "line.separator" );
 	
 	private static ArrayList<String> trace    = null;
 	
 	
 	private Debug()
 	{
 		nowDate   = Calendar.getInstance();
 		
 		format    = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
 		logformat = new SimpleDateFormat( "yyyyMMdd" );
 		//logformat = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
 		
 		trace = new ArrayList<String>();
 		
 		NewDate( nowDate );
 	}
 	
 	public void disable()
 	{
 		enabled = false;
 	}
 	
 	public void enable()
 	{
 		enabled = true;
 	}
 	
 	public void setPath( String path )
 	{
 		this.path = path;
 		NewDate( nowDate );
 	}
 	
 	public void setPathHomeLogs()
 	{
 		this.path = System.getProperty( "user.home" ) + filesep + "logs" + filesep;
 		NewDate( nowDate );
 	}
 	
 	private static Calendar GetDate( Calendar c )
 	{
 		Calendar c2 = Calendar.getInstance(  );
 		c2.clear();
 		c2.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
 		
 		//c2.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
 		//c2.set( Calendar.MILLISECOND, c.get( Calendar.MILLISECOND ) );
 
 		return c2;
 	}
 	
 	private static void NewDate( Calendar n )
 	{
 		nowDate = n;
 		
 		if( fw != null )
 		{
 			try
 			{
 				fw.close();
 				out.close();
 			}
 			catch ( IOException e )
 			{
 				System.err.println( "Could not close file writer and/or buffered writer." );
 			}
 		}
 		
 		try
 		{
 
 			fw = new FileWriter( path +  "debug_" + logformat.format( nowDate.getTime() ) + ".log" , true );
 			
 		}
 		catch ( IOException e )
 		{
 			System.err.println( "Cannot use the specified path, \"" + path + "\". Defaulting to current working directory." );
 			path = "./";
 			try
 			{
 				fw = new FileWriter( path +  "debug_" + logformat.format( nowDate.getTime() ) + ".log" , true );
 			}
 			catch ( IOException e1 )
 			{
 				System.err.println( "Failed to use current working directory. Quitting!" );
 				System.exit( 1 );
 			}
 		}
 		
 		out = new BufferedWriter( fw );
 	}
 	
 	public static Debug GetLogger()
 	{
 		if( logger == null )
 		{
 			logger = new Debug();
 		}
 		
 		return logger;
 	}
 	
 	public void stacktrace()
 	{
 		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 		StringBuffer sb = new StringBuffer();
 		sb.append( "Tracing" + linesep );
 		for( int i = 2 ; i < stack.length ; i++ )
 		{
 			sb.append( stack[i].getClassName() + "::" + stack[i].getMethodName() + "," + stack[i].getLineNumber() + linesep );
 		}
 		
 		_log( sb.toString(), "trace" );
 	}
 	
 	public void trace_function( )
 	{
 		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 		trace.add( stack[2].getClassName() + "::" + stack[2].getMethodName() );
 	}
 	
 	public void print_trace( )
 	{
 		print_trace( true );
 	}
 	
 	public void print_trace( boolean tolog )
 	{
 		StringBuffer sb = new StringBuffer();
 		sb.append( "Function tracing" + linesep );
 		 
 		for( int i = 0 ; i < trace.size() ; i++ )
 		{
 			if( tolog )
 			{
 				sb.append( "[" + i + "] " + trace.get( i ) + linesep );
 			}
 			else
 			{
 				System.out.println( trace.get( i ) );
 			}
 		}
 		if( tolog )
 		{
 			_log( sb.toString(), "trace" );
 		}
 	}
 	
 	public void log( String msg )
 	{
 		_log( msg, "info" );
 	}
 	
 	public void debug( String msg )
 	{
 		_log( msg, "debug" );
 	}
 	
	public void warning( String msg )
	{
		_log( msg, "warning" );
	}
	
	public void error( String msg )
	{
		_log( msg, "error" );
	}
	
 	public void log( String msg, String type )
 	{
 		_log( msg, type );
 	}
 	
 	private void _log( String msg, String type )
 	{
 		if( !enabled )
 		{
 			return;
 		}
 		
 		/* Check if the date is changed */
 		Calendar now = Calendar.getInstance();
 
 		if( GetDate( now ).after( GetDate( nowDate ) ) )
 		{
 			NewDate( now );
 		}
 		
 		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 
 		
 		try
 		{
 			out.write( format.format( now.getTime() ) + " [" + type + "] " + stack[3].getClassName() + "::" + stack[3].getMethodName() + "," + stack[3].getLineNumber() + ": " + msg + linesep );
 			out.flush();
 		}
 		catch ( IOException e )
 		{
 			e.printStackTrace();
 		}
 	}
 }
 
 
 
 
