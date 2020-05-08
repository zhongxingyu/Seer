 package com.chiorichan;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.EnumMap;
 import java.util.Map;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.fusesource.jansi.Ansi;
 import org.fusesource.jansi.Ansi.Attribute;
 
 import com.google.common.base.Strings;
 
 public class ConsoleLogManager
 {
 	public static Logger a;
 	public static Logger global = Logger.getLogger( "" );
 	
 	private int lineCount = 999;
 	
 	public ConsoleLogManager(String s)
 	{
 		a = Logger.getLogger( s );
 	}
 	
 	public void init()
 	{
 		Console console = Loader.getConsole();
 		ConsoleHandler consolehandler = new TerminalConsoleHandler( console.reader );
 		
 		a.addHandler( consolehandler );
 		
 		for ( java.util.logging.Handler handler : global.getHandlers() )
 		{
 			global.removeHandler( handler );
 		}
 		
 		consolehandler.setFormatter( new ConsoleLogFormatter( console ) );
 		global.addHandler( consolehandler );
 		
 		try
 		{
 			String pattern = (String) Loader.getOptions().valueOf( "log-pattern" );
 			
 			String tmpDir = System.getProperty( "java.io.tmpdir" );
 			String homeDir = System.getProperty( "user.home" );
 			if ( tmpDir == null )
 			{
 				tmpDir = homeDir;
 			}
 			
 			File parent = new File( pattern ).getParentFile();
 			StringBuilder fixedPattern = new StringBuilder();
 			String parentPath = "";
 			if ( parent != null )
 			{
 				parentPath = parent.getPath();
 			}
 			
 			int i = 0;
 			while ( i < parentPath.length() )
 			{
 				char ch = parentPath.charAt( i );
 				char ch2 = 0;
 				if ( i + 1 < parentPath.length() )
 				{
 					ch2 = Character.toLowerCase( pattern.charAt( i + 1 ) );
 				}
 				
 				if ( ch == '%' )
 				{
 					if ( ch2 == 'h' )
 					{
 						i += 2;
 						fixedPattern.append( homeDir );
 						continue;
 					}
 					else if ( ch2 == 't' )
 					{
 						i += 2;
 						fixedPattern.append( tmpDir );
 						continue;
 					}
 					else if ( ch2 == '%' )
 					{
 						// Even though we don't care about this we have to skip it to avoid matching %%t
 						i += 2;
 						fixedPattern.append( "%%" );
 						continue;
 					}
 					else if ( ch2 != 0 )
 					{
 						throw new java.io.IOException( "log-pattern can only use %t and %h for directories, got %" + ch2 );
 					}
 				}
 				
 				fixedPattern.append( ch );
 				i++;
 			}
 			
 			// Try to create needed parent directories
 			parent = new File( fixedPattern.toString() );
 			if ( parent != null )
 			{
 				parent.mkdirs();
 			}
 			
 			int limit = (Integer) Loader.getOptions().valueOf( "log-limit" );
 			int count = (Integer) Loader.getOptions().valueOf( "log-count" );
 			boolean append = (Boolean) Loader.getOptions().valueOf( "log-append" );
 			FileHandler filehandler = new FileHandler( pattern, limit, count, append );
 			
 			filehandler.setFormatter( new ConsoleLogFormatter( console ) );
 			a.addHandler( filehandler );
 			global.addHandler( filehandler );
 		}
 		catch ( Exception exception )
 		{
 			a.log( Level.WARNING, "Failed to log to server.log", exception );
 		}
 	}
 	
 	public Logger getLogger()
 	{
 		return a;
 	}
 	
 	public void highlight( String msg )
 	{
 		log( Level.INFO, ChatColor.AQUA + msg );
 	}
 	
 	public void info( String s )
 	{
 		log( Level.INFO, ChatColor.WHITE + s );
 	}
 	
 	public void warning( String s )
 	{
 		log( Level.WARNING, ChatColor.GOLD + s );
 	}
 	
 	public void warning( String s, Object... aobject )
 	{
 		a.log( Level.WARNING, ChatColor.GOLD + s, aobject );
 	}
 	
 	public void warning( String s, Throwable throwable )
 	{
 		log( Level.WARNING, ChatColor.GOLD + s, throwable );
 	}
 	
 	public void severe( String s )
 	{
 		log( Level.SEVERE, ChatColor.RED + s );
 	}
 	
 	public void severe( String s, Throwable throwable )
 	{
 		log( Level.SEVERE, ChatColor.RED + s, throwable );
 	}
 	
 	public void panic( Throwable e )
 	{
 		severe( e.getMessage(), e );
 		System.exit( 1 );
 	}
 	
 	public void panic( String var1 )
 	{
 		severe( var1 );
 		System.exit( 1 );
 	}
 	
 	public void fine( String var1 )
 	{
 		a.log( Level.FINE, var1 );
 	}
 	
 	public void finer( String var1 )
 	{
 		a.log( Level.FINER, var1 );
 	}
 	
 	public void finest( String var1 )
 	{
 		a.log( Level.FINEST, var1 );
 	}
 	
 	private void printHeader()
 	{
 		if ( lineCount > 40 )
 		{
 			lineCount = 0;
 			log( Level.FINE, ChatColor.GOLD + "<CLIENT ID>     <MESSAGE>" );
 		}
 		
 		lineCount++;
 	}
 	
 	public void log( Level l, String client, String msg )
 	{
 		if ( client.length() < 15 )
 		{
 			client = client + Strings.repeat( " ", 15 - client.length() );
 		}
 		
 		printHeader();
 		
 		log( l, ChatColor.LIGHT_PURPLE + client + " " + ChatColor.AQUA + msg );
 	}
 	
 	public void log( Level l, String msg, Throwable t )
 	{
 		a.log( l, msg, t );
 	}
 	
 	public void log( Level l, String msg )
 	{
 		a.log( l, msg );
 	}
 	
 	public String[] multilineColorRepeater( String var1 )
 	{
 		return multilineColorRepeater( var1.split( "\\n" ) );
 	}
 	
 	public String[] multilineColorRepeater( String[] var1 )
 	{
 		String color = ChatColor.getLastColors( var1[0] );
 		StringBuilder sb = new StringBuilder();
 		
 		for ( int l = 0; l < var1.length; l++ )
 		{
 			var1[l] = color + var1[l];
 		}
 		
 		return var1;
 	}
}
