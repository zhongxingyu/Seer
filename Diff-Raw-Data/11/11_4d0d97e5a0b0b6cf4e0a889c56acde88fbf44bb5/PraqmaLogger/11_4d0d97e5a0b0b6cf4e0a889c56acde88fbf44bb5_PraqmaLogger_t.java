 package net.praqma.util.debug;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 
 /**
  * A simple logger class.
  * @author wolfgang
  *
  */
 public class PraqmaLogger
 {
 	private static PraqmaLogger plogger       = null;
 	private static FileWriter fw              = null;
 	//private static BufferedWriter out         = null;
 	private static BufferedWriter out         = null;
 	private static String path                = "./";
 	private static SimpleDateFormat format    = null;
 	private static SimpleDateFormat logformat = null;
 	private static Calendar nowDate           = null;
 	
 	private static File file                  = null;
 	
 	private static boolean enabled            = true;
 	private static boolean traceEnabled       = false;
 	
 	private static final String filesep       = System.getProperty( "file.separator" );
 	private static final String linesep       = System.getProperty( "line.separator" );
 	
 	private static ArrayList<String> trace    = null;
 	
 	private static boolean append             = false;
 	
 	/* Styling */
 	private static final int typemaxlength    = 8;
 	private static final int methodmaxlength  = 55;
 	private static final boolean indent       = false;
 
 	
 	
 	private PraqmaLogger( boolean append, boolean homePath )
 	{
 		PraqmaLogger.append = append;
 		
 		nowDate   = Calendar.getInstance();
 		
 		format    = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
 		logformat = new SimpleDateFormat( "yyyyMMdd" );
 		
 		trace = new ArrayList<String>();
 		
 		if( homePath )
 		{
 			setPathHomeLogs();
 		}
 		else
 		{
 			newDate( nowDate );
 		}
 		
 		PraqmaLogger.addSubscriptions();
 	}
 	
 
 	public static Logger getLogger( boolean append, boolean homePath )
 	{
 		if( plogger == null )
 		{
 			plogger = new PraqmaLogger( append, homePath );
 		}
 		
 		return new Logger( plogger );
 	}
 	
 	public static Logger getLogger( boolean append )
 	{
 		if( plogger == null )
 		{
 			plogger = new PraqmaLogger( append, true );
 		}		
 		
 		return new Logger( plogger );
 	}
 	
 	public static Logger getLogger( )
 	{
 		if( plogger == null )
 		{
 			plogger = new PraqmaLogger( true, true );
 		}
 		
 		return new Logger( plogger );
 	}
 	
 	public static Logger getLogger( Logger l )
 	{
 		if( plogger == null )
 		{
 			plogger = new PraqmaLogger( true, true );
 		}
 		
 		l.setLogger( plogger );
 		l.out = null;
 		return l;
 	}
 	
 	
 	/**
 	 * 
 	 * @author wolfgang
 	 *
 	 */
 	public static class Logger implements Serializable
 	{
 		private static final long serialVersionUID = 1L;
 		
 		public List<String> exclude = new ArrayList<String>();
 		public List<String> include = new ArrayList<String>();
 		public boolean all = false;
 		public boolean enabled = true;
 		
 		transient private PraqmaLogger logger = null;
 		
 		transient FileWriter fw = null;
 		transient BufferedWriter out = null;
 		
 		/* Constructor */
 		Logger( PraqmaLogger logger )
 		{
 			this.logger = logger;
 			addSubscriptions();
 		}
 		
 		public Logger()
 		{
 			System.out.println( "Default constructor" );
 		}
 		
 		public void setLocalLog( File log )
 		{
 			try
 			{
 				fw = new FileWriter( log, true );
 			}
			catch ( Exception e1 )
 			{
 				return;
 			}
 			
 			System.out.println( "Local log set to " + log.getAbsoluteFile() );
 			this.out = new BufferedWriter( fw );
			if( this.out == null)
			{
				System.out.println( "WHAT? NULL?" );
			}
			else
			{
				System.out.println( "YAY NOT NULL!" );
			}
 		}
 		
 		public BufferedWriter getLocalLog()
 		{
 			return this.out;
 		}
 		
 		void setLogger( PraqmaLogger pl )
 		{
 			this.logger = pl;
 		}
 		
 		/* Subscriptions */
 		public void subscribe( String s )
 		{
 			if( !isIncluded( s ) )
 			{
 				System.out.println( "INCLUDING " + s );
 				include.add( s );
 			}
 		}
 		
 		private void addSubscriptions()
 		{
 			String includes = System.getenv( "include_classes" );
 			// For now
 			if( includes == null )
 			{
 				//this.all = true;
 			}
 			else
 			{
 				String[] is = includes.split( "," );
 				for( String i : is )
 				{
 					this.subscribe( i );
 				}
 			}
 		}
 
 		/* INCLUDE EXCLUDE TESTS */
 		public boolean isExcluded( String name )
 		{
 			for( String s : exclude )
 			{
 				if( name.startsWith( s ) )
 				{
 					return true;
 				}
 			}
 			
 			return false;
 		}
 		
 		public boolean isIncluded( String name )
 		{
 			for( String s : include )
 			{
 				if( name.startsWith( s ) )
 				{
 					return true;
 				}
 			}
 			
 			return false;
 		}
 		
 	
 		public void disable()
 		{
 			enabled = false;
 		}
 		
 		public void enable()
 		{
 			enabled = true;
 		}
 		
 		public void enableTrace()
 		{
 			traceEnabled = true;
 		}
 		
 		public String getPath()
 		{
 			return logger.getPath();
 		}
 		
 		public String toString()
 		{
 			return net.praqma.util.structure.Printer.listPrinterToString( this.include );
 		}
 
 		/* Logging */		
 		public void print( Object msg )
 		{
 			System.out.println( msg );
 		}
 		
 		public String log( Object msg )
 		{
 			return logger._log( msg, "info", this, 4 );
 		}
 		
 		public String debug( Object msg )
 		{
 			return logger._log( msg, "debug", this, 4 );
 		}
 		
 		public String info( Object msg )
 		{
 			return logger._log( msg, "info", this, 4 );
 		}
 		
 		public String warning( Object msg )
 		{
 			/* Testing! */
 			//System.err.println( msg );
 			return logger._log( msg, "warning", this, 4 );
 		}
 		
 		public String exceptionWarning( Object msg )
 		{
 			//System.err.println( msg );
 			return logger._log( msg, "warning", this, 5 );
 		}
 		
 		public String error( Object msg )
 		{
 			/* Testing */
 			//System.err.println( msg );
 			return logger._log( msg, "error", this, 4 );
 		}
 		
 		public String log( Object msg, String type )
 		{
 			return logger._log( msg, type, this, 4 );
 		}
 		
 		public String empty( Object msg )
 		{
 			return logger._log( msg, null, this, 4 );
 		}
 		
 		/**/
 		public void trace_function()
 		{
 			
 		}
 		
 	}
 	
 	
 	private static List<String> exclude = new ArrayList<String>();
 	private static List<String> include = new ArrayList<String>();
 	private boolean all = false;
 	
 	public static boolean isIncluded( String name )
 	{
 		for( String s : include )
 		{
 			if( name.startsWith( s ) )
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	private static void addSubscriptions()
 	{
 		String includes = System.getenv( "include_global_classes" );
 		// For now
 		if( includes == null )
 		{
 			//System.out.println( "NULL" );
 			//this.all = true;
 		}
 		else
 		{
 			String[] is = includes.split( "," );
 			for( String i : is )
 			{
 				PraqmaLogger.subscribe( i );
 			}
 		}
 	}
 	
 	public static void subscribe( String s )
 	{
 		if( !isIncluded( s ) )
 		{
 			include.add( s );
 		}
 	}
 
 		
 	
 	public void setPath( String path )
 	{
 		PraqmaLogger.path = path;
 		newDate( nowDate );
 	}
 	
 	public boolean setPathHomeLogs()
 	{
 		String path = System.getProperty( "user.home" ) + filesep + "logs" + filesep;
 		File file = new File( path );
 		
 		/* Existence + creation */
 		if( !file.exists() )
 		{
 			boolean created = false;
 			try
 			{
 				created = file.mkdir();
 			}
 			catch ( Exception e )
 			{
 				created = false;
 			}
 			
 			if( !created )
 			{
 				return false;
 			}
 		}
 		
 		PraqmaLogger.path = path;
 		
 		newDate( nowDate );
 		
 		return true;
 	}
 	
 	private static Calendar getDate( Calendar c )
 	{
 		Calendar c2 = Calendar.getInstance(  );
 		c2.clear();
 		c2.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
 		
 		//c2.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
 		//c2.set( Calendar.MILLISECOND, c.get( Calendar.MILLISECOND ) );
 
 		return c2;
 	}
 	
 	private static void newDate( Calendar n )
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
 			file = new File( path + "debug_" + logformat.format( nowDate.getTime() ) + ".log" );
 			fw = new FileWriter( file , append );
 			
 		}
 		catch ( IOException e )
 		{
 			//System.err.println( "Cannot use the specified path, \"" + path + "\". Defaulting to current working directory." );
 			//path = "./";
 			try
 			{
 				file = new File( "debug_" + logformat.format( nowDate.getTime() ) + ".log" );
 				fw = new FileWriter( file, append );
 			}
 			catch ( IOException e1 )
 			{
 				System.err.println( "Failed to use current working directory. Quitting!" );
 				System.exit( 1 );
 			}
 		}
 		
 		//System.out.println( "LOGGER USING " + file.getAbsolutePath() );
 		
 		out = new BufferedWriter( fw );
 	}
 	
 	public String getPath()
 	{
 		return file.getAbsolutePath();
 	}
 
 	
 	public String objectToString( Object t )
 	{
 		if( t instanceof Throwable )
 		{
 			Writer sw = new StringWriter();
 			PrintWriter pw = new PrintWriter( sw );
 			( (Throwable) t ).printStackTrace( pw );
 			
 			return sw.toString();
 		}
 		else
 		{
 			return String.valueOf( t );
 		}
 	}
 
 	
 	private String _log( Object msg, String type, Logger l, int size )
 	{
 		if( !l.enabled )
 		{
 			return null;
 		}
 		
 		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 		String name = stack[size].getClassName();
 		//System.out.println( "NAME=" + name );
 
 		String myMsg = null;
 		if( l.isIncluded( name ) || PraqmaLogger.isIncluded( name ) || l.all || this.all )
 		{
 		
 			/* Check if the date is changed */
 			Calendar now = Calendar.getInstance();
 	
 			if( getDate( now ).after( getDate( nowDate ) ) )
 			{
 				newDate( now );
 			}
 	
 			if( type != null )
 			{
 				if( type.length() > PraqmaLogger.typemaxlength )
 				{
 					type = type.substring( 0, 8 );
 				}
 				String stackMsg = stack[size].getClassName() + "::" + stack[size].getMethodName() + "," + stack[size].getLineNumber();
 				String msg_ = format.format( now.getTime() ) + " [" + type + "] " + new String(new char[PraqmaLogger.typemaxlength - type.length()]).replace("\0", " ") + stackMsg;
 				myMsg = msg_ + ": " + objectToString( msg ) + linesep;
 			}
 			else
 			{
 				myMsg = objectToString( msg ) + linesep;
 			}
 			
 			/* Writing */
 			try
 			{
 				out.write( myMsg );
 				out.flush();
 			}
 			catch( Exception e )
 			{
 				System.out.println( "Could not write to log." );
 			}
 			
 			/* Local */
 			System.out.println( "THIS SHOULD ALLWAYS GET WRITTEN!" );
 			if(l.getLocalLog() != null)
 			{
 				System.out.println( "LOCAL LOG IS NOT NULL" );
 			}
 			else
 			{
 				System.out.println( "LOCAL LOG _IS_ NULL" );
 			}
 			if( l.getLocalLog() != null )
 			{
 				try
 				{
 					System.out.println( "Writing to local log..." );
 					l.getLocalLog().write( myMsg );
 					l.getLocalLog().flush();
 				}
 				catch( Exception e )
 				{
 					System.out.println( "Could not write to local log" );
 				}
 			}
 		}
 		
 		return myMsg;
 	}
 }
 
 
 
 
