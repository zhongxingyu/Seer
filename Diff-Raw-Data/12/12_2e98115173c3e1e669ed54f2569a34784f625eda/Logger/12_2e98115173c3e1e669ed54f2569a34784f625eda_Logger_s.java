 package net.praqma.util.debug;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.regex.Matcher;
 
 import net.praqma.util.debug.appenders.Appender;
 
 public class Logger {
 
 	private static final int levelMaxlength = 8;
 	
 
 	private static final String filesep = System.getProperty( "file.separator" );
	private static final String linesep = System.getProperty( "line.separator" );
 
 	private static Logger instance = null;
 	
 	private boolean append = true;
 	
 	private Date current;
 	
 	private static String filename = "logger_";
 	
 	private File loggerPath;
 	private File loggerFile;
 
 	private static SimpleDateFormat datetimeformat  = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
 	private static SimpleDateFormat fileformat = new SimpleDateFormat( "yyyyMMdd" );
 	private static SimpleDateFormat timeformat = new SimpleDateFormat( "HH:mm:ss" );
 	private static SimpleDateFormat dateformat = new SimpleDateFormat( "yyyy-MM-dd" );
 	
 	private static List<Appender> appenders = new CopyOnWriteArrayList<Appender>();
 
 	private FileWriter fw;
 	private PrintWriter out;
 	
 	public enum LogLevel {
 		DEBUG,
 		VERBOSE,
 		INFO,
 		WARNING,
 		ERROR,
 		FATAL
 	}
 	
 	private Logger() {
 		
 	}
 	
 	public static Logger getLogger() {
 		if( instance == null ) {
 			instance = new Logger();
 		}
 		
 		return instance;
 	}
 	
 	private boolean initialize() {
 		/* CWD */
 		if( loggerPath == null ) {
 			loggerPath = new File( System.getProperty("user.dir") );
 		}
 		
 		String format = instance.fileformat.format( new Date() );
 		loggerFile = new File( loggerPath, filename + format + ".log" );
 		
 		/* Existence + creation */
 		if( !loggerPath.exists() ) {
 			boolean created = false;
 			try {
 				created = loggerFile.mkdirs();
 			} catch (Exception e) {
 				created = false;
 			}
 
 			if( !created ) {
 				return false;
 			}
 		}
 		
 		if( fw != null ) {
 			try {
 				fw.close();
 				out.close();
 			} catch (IOException e) {
 				System.err.println( "Could not close file writer and/or buffered writer." );
 			}
 		}
 
 		try {
 			fw = new FileWriter( loggerFile, append );
 
 		} catch (IOException e) {
 			System.err.println( "Could not create logger. Quitting!" );
 			System.exit( 1 );
 		}
 
 		out = new PrintWriter( fw );
 		
 		return true;
 	}
 	
 	public static void addAppender( Appender appender ) {
 		appenders.add( appender );
 	}
 	
 	public static void removeAppender( Appender appender ) {
 		if( appender != null ) {
 			appenders.remove( appender );
 			appender.getOut().close();
 		}
 	}
 
 	public static void setFilename( String filename1 ) {
 		filename = filename1;
 	}
 	
 	
 	public String objectToString( Object t ) {
 		if( t instanceof Throwable ) {
 			Writer sw = new StringWriter();
 			PrintWriter pw = new PrintWriter( sw );
 			( (Throwable) t ).printStackTrace( pw );
 
 			return sw.toString();
 		} else {
 			return String.valueOf( t );
 		}
 	}
 	
 	/* Log */
 	
 	public void log( Object message ) {
 		log( message, LogLevel.INFO, null, 3 );
 	}
 	
 	public void log( Object message, String tag ) {
 		log( message, LogLevel.INFO, tag, 3 );
 	}
 	
 	/* Debug */
 	
 	public void debug( Object message ) {
 		log( message, LogLevel.DEBUG, null, 3 );
 	}
 	
 	public void debug( Object message, String tag ) {
 		log( message, LogLevel.DEBUG, tag, 3 );
 	}
 	
 	/* Debug */
 	
 	public void verbose( Object message ) {
 		log( message, LogLevel.VERBOSE, null, 3 );
 	}
 	
 	public void verbose( Object message, String tag ) {
 		log( message, LogLevel.VERBOSE, tag, 3 );
 	}
 	
 	/* Info */
 	
 	public void info( Object message ) {
 		log( message, LogLevel.INFO, null, 3 );
 	}
 	
 	public void info( Object message, String tag ) {
 		log( message, LogLevel.INFO, tag, 3 );
 	}
 	
 	/* Warning */
 	
 	public void warning( Object message ) {
 		log( message, LogLevel.WARNING, null, 3 );
 	}
 	
 	public void warning( Object message, String tag ) {
 		log( message, LogLevel.WARNING, tag, 3 );
 	}
 	
 	/* Error */
 	
 	public void error( Object message ) {
 		log( message, LogLevel.ERROR, null, 3 );
 	}
 	
 	public void error( Object message, String tag ) {
 		log( message, LogLevel.ERROR, tag, 3 );
 	}
 	
 	/* Fatal */
 	
 	public void fatal( Object message ) {
 		log( message, LogLevel.FATAL, null, 3 );
 	}
 	
 	public void fatal( Object message, String tag ) {
 		log( message, LogLevel.FATAL, tag, 3 );
 	}
 	
 	/* Log = info? */
 	
 	public void log( Object message, LogLevel level ) {
 		log( message, level, null, 3 );
 	}
 	
 	public void log( Object message, LogLevel level, String tag ) {
 		log( message, level, tag, 3 );
 	}
 	
 	private String parseTemplate( Map<String, String> keywords, String template ) {
 		Set<String> keys = keywords.keySet();
 		for( String key : keys ) {
 			//System.out.println( key + "=" + keywords.get( key ) );
 			try {
 				template = template.replaceAll( key, keywords.get( key ) );
 			} catch( Exception e ) {
 				e.printStackTrace();
 			}
 		}
 		
 		return template;
 	}
 	
 	/**
 	 * 
 	 * @param input
 	 * @deprecated as of 0.1.17, use {@link Appender#write}
 	 */
 	public void redirect( InputStream input ) {
 		BufferedReader in = new BufferedReader( new InputStreamReader( input ) );
 		String line = "";
 		try {
 			while( ( line = in.readLine() ) != null ) {
 				writeAppenders( line );
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static Set<String> getSubscriptions() {
 		Set<String> all = new LinkedHashSet<String>();
 		for( Appender a : appenders ) {
 			all.addAll( a.getSubscriptions() );
 		}
 		
 		return all;
 	}
 	
 	public static LoggerSetting getLoggerSettings( LogLevel level ) {
 		LoggerSetting settings = new LoggerSetting();
 		settings.setSubscriptions( Logger.getSubscriptions() );
 		settings.setMinimumLevel( level );
 		
 		return settings;
 	}
 	
 	public static int getNumberOfAppenders() {
 		return appenders.size();
 	}
 	
 	/**
 	 * Write a specific message to appenders
 	 * @param message
 	 * @param level
 	 */
 	private void writeAppenders( String message ) {
 		for( Appender a : appenders ) {
 			a.getOut().write( message + linesep );
 			a.getOut().flush();
 		}
 	}
 	
 	private void log( Object message, LogLevel level, String tag, int depth ) {
 		Date now = new Date();
 		
 		if( current == null ) {
 			instance.initialize();
 		}
 		
 		Map<String, String> keywords = new HashMap<String, String>();
 		
 		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 		
 		keywords.put( "%class", stack[depth].getClassName() );
 		keywords.put( "%threadid", "[" + Thread.currentThread().getId()+ "]" );
 		keywords.put( "%threadname", Thread.currentThread().getName() );
 		keywords.put( "%thread", "[(" + Thread.currentThread().getId()+ ")" + Thread.currentThread().getName() + "]" );
 		keywords.put( "%method", stack[depth].getMethodName() );
 		String subscribable = stack[depth].getClassName() + "." + stack[depth].getMethodName();
 		keywords.put( "%stack", Matcher.quoteReplacement( stack[depth].getClassName() + "::" + stack[depth].getMethodName() + "," + stack[depth].getLineNumber() ) );
 		keywords.put( "%caller", Matcher.quoteReplacement( stack[depth+1].getClassName() + "::" + stack[depth+1].getMethodName() + "," + stack[depth+1].getLineNumber() ) );
 		keywords.put( "%line", stack[depth].getLineNumber()+"" );
 		keywords.put( "%datetime", datetimeformat.format( now ) );
 		keywords.put( "%date", dateformat.format( now ) );
 		keywords.put( "%time", timeformat.format( now ) );
 		if( level != null ) {
 			keywords.put( "%level", level.toString() );
 			keywords.put( "%space", new String( new char[Logger.levelMaxlength - level.toString().length()] ).replace( "\0", " " ) );
 		} else {
 			keywords.put( "%level", "" );
 			keywords.put( "%space", new String( new char[Logger.levelMaxlength] ).replace( "\0", " " ) );
 		}
 		keywords.put( "%message", Matcher.quoteReplacement( objectToString( message ) ) );
 		//keywords.put( "%newline", linesep );
 		keywords.put( "%newline", "\n" );
 		
 		if( tag != null ) {
 			keywords.put( "%tag", tag );
 		} else {
 			keywords.put( "%tag", "" );
 		}
 
 		/* Writing */
 		for( Appender a : appenders ) {
 			//System.out.print( subscribable + ": " );
 			if( !a.isEnabled() || a.getMinimumLevel().ordinal() > level.ordinal() ) {
 				//System.out.println( subscribable + " is not enabled" );
 				continue;
 			}
 			
 			/* Check tags, if tag for appender is defined, a log tag must be provided */
 			if( a.getTag() != null && ( tag == null || !tag.equals( a.getTag() ) ) ) {
 				//System.out.println( subscribable + " did not have tag" );
 				continue;
 			}
 			
 			if( a.getTag() != null ) {
 				System.out.println( a.getTag() + "=" + tag );
 			}
 			
 			/* Check subscriptions */
 			if( !a.isSubscribeAll() && !a.isSubscribed( subscribable ) ) {
 				//System.out.println( subscribable + " is not subscribed" );
 				continue;
 			}
 			
 			if( a.getThreadId() != null && !a.getThreadId().equals( getThreadId( Thread.currentThread() ) ) ) {
 				//System.out.println( a.getThreadId() + " is not the same as " + Thread.currentThread().getId() );
 				continue;
 			}
 			
 			String finalmsg = parseTemplate( keywords, a.getTemplate() );
 			if( !a.onBeforeLogging() ) {
 				//System.out.println( subscribable + " on before logging" );
 				continue;
 			}
 			
 			//System.out.println( "written" );
 			
 			a.getOut().write( finalmsg );
 			a.getOut().flush();
 		}
 	}
 	
 	public static String getThreadId( Thread t ) {
 		return t.getId() + "::" + t.getName();
 	}
 }
