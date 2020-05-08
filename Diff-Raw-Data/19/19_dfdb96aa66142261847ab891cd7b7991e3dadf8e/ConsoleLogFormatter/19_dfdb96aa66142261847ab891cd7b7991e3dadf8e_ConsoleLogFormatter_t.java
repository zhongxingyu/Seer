 package com.chiorichan;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.EnumMap;
 import java.util.Map;
 import java.util.logging.Formatter;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 
 import org.fusesource.jansi.Ansi;
 import org.fusesource.jansi.Ansi.Attribute;
 
 public class ConsoleLogFormatter extends Formatter
 {
 	private final SimpleDateFormat date;
 	public Map<ChatColor, String> replacements = new EnumMap<ChatColor, String>( ChatColor.class );
 	public ChatColor[] colors = ChatColor.values();
 	public boolean debugMode = false;
 	
 	public ConsoleLogFormatter(Console console)
 	{
 		SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
 		
 		if ( date == null )
 		{
 			date = new SimpleDateFormat( "HH:mm:ss" );
 		}
 		
 		this.date = date;
 		
 		replacements.put( ChatColor.BLACK, Ansi.ansi().fg( Ansi.Color.BLACK ).boldOff().toString() );
 		replacements.put( ChatColor.DARK_BLUE, Ansi.ansi().fg( Ansi.Color.BLUE ).boldOff().toString() );
 		replacements.put( ChatColor.DARK_GREEN, Ansi.ansi().fg( Ansi.Color.GREEN ).boldOff().toString() );
 		replacements.put( ChatColor.DARK_AQUA, Ansi.ansi().fg( Ansi.Color.CYAN ).boldOff().toString() );
 		replacements.put( ChatColor.DARK_RED, Ansi.ansi().fg( Ansi.Color.RED ).boldOff().toString() );
 		replacements.put( ChatColor.DARK_PURPLE, Ansi.ansi().fg( Ansi.Color.MAGENTA ).boldOff().toString() );
 		replacements.put( ChatColor.GOLD, Ansi.ansi().fg( Ansi.Color.YELLOW ).boldOff().toString() );
 		replacements.put( ChatColor.GRAY, Ansi.ansi().fg( Ansi.Color.WHITE ).boldOff().toString() );
 		replacements.put( ChatColor.DARK_GRAY, Ansi.ansi().fg( Ansi.Color.BLACK ).bold().toString() );
 		replacements.put( ChatColor.BLUE, Ansi.ansi().fg( Ansi.Color.BLUE ).bold().toString() );
 		replacements.put( ChatColor.GREEN, Ansi.ansi().fg( Ansi.Color.GREEN ).bold().toString() );
 		replacements.put( ChatColor.AQUA, Ansi.ansi().fg( Ansi.Color.CYAN ).bold().toString() );
 		replacements.put( ChatColor.RED, Ansi.ansi().fg( Ansi.Color.RED ).bold().toString() );
 		replacements.put( ChatColor.LIGHT_PURPLE, Ansi.ansi().fg( Ansi.Color.MAGENTA ).bold().toString() );
 		replacements.put( ChatColor.YELLOW, Ansi.ansi().fg( Ansi.Color.YELLOW ).bold().toString() );
 		replacements.put( ChatColor.WHITE, Ansi.ansi().fg( Ansi.Color.WHITE ).bold().toString() );
 		replacements.put( ChatColor.MAGIC, Ansi.ansi().a( Attribute.BLINK_SLOW ).toString() );
 		replacements.put( ChatColor.BOLD, Ansi.ansi().a( Attribute.INTENSITY_BOLD ).toString() );
 		replacements.put( ChatColor.STRIKETHROUGH, Ansi.ansi().a( Attribute.STRIKETHROUGH_ON ).toString() );
 		replacements.put( ChatColor.UNDERLINE, Ansi.ansi().a( Attribute.UNDERLINE ).toString() );
 		replacements.put( ChatColor.ITALIC, Ansi.ansi().a( Attribute.ITALIC ).toString() );
 		replacements.put( ChatColor.FAINT, Ansi.ansi().a( Attribute.INTENSITY_FAINT ).toString() );
 		replacements.put( ChatColor.NEGATIVE, Ansi.ansi().a( Attribute.NEGATIVE_ON ).toString() );
 		replacements.put( ChatColor.RESET, Ansi.ansi().a( Attribute.RESET ).fg( Ansi.Color.DEFAULT ).toString() );
 	}
 	
 	public ChatColor getLevelColor( Level var1 )
 	{
 		if ( var1 == Level.FINEST || var1 == Level.FINER || var1 == Level.FINE )
 		{
 			return ChatColor.WHITE;
 		}
 		else if ( var1 == Level.INFO )
 		{
 			return ChatColor.AQUA;
 		}
 		else if ( var1 == Level.WARNING )
 		{
 			return ChatColor.GOLD;
 		}
 		else if ( var1 == Level.SEVERE )
 		{
 			return ChatColor.RED;
 		}
 		else if ( var1 == Level.CONFIG )
 		{
 			return ChatColor.WHITE;
 		}
 		else
 		{
 			return ChatColor.WHITE;
 		}
 	}
 	
 	public String handleAltColors( String var1 )
 	{
 		if ( Loader.getConsole().AnsiSupported() )
 		{
 			var1 = ChatColor.translateAlternateColorCodes( '&', var1 ) + ChatColor.RESET;
 			
 			String result = ChatColor.translateAlternateColorCodes( '&', var1 );
 			for ( ChatColor color : colors )
 			{
 				if ( replacements.containsKey( color ) )
 				{
 					var1 = var1.replaceAll( "(?i)" + color.toString(), replacements.get( color ) );
 				}
 				else
 				{
 					var1 = var1.replaceAll( "(?i)" + color.toString(), "" );
 				}
 			}
 		}
 		
 		return var1;
 	}
 	
 	@Override
 	public String format( LogRecord record )
 	{
 		StringBuilder builder = new StringBuilder();
 		Throwable ex = record.getThrown();
 		
		builder.append( ChatColor.RESET + "" + ChatColor.GRAY );
 		builder.append( date.format( record.getMillis() ) );
 		
 		if ( debugMode )
 		{
 			StackTraceElement[] var1 = Thread.currentThread().getStackTrace();
 			
 			for ( StackTraceElement var2 : var1 )
 			{
 				if ( !var2.getClassName().toLowerCase().contains( "java" ) && !var2.getClassName().toLowerCase().contains( "log" ) && !var2.getMethodName().equals( "sendMessage" ) && !var2.getMethodName().equals( "sendRawMessage" ) )
 				{
 					builder.append( " " + var2.getClassName() );
 					builder.append( "$" + var2.getMethodName() );
 					builder.append( ":" + var2.getLineNumber() );
 					break;
 				}
 			}
 		}
 		
 		builder.append( " [" );
 		builder.append( getLevelColor( record.getLevel() ) );
 		builder.append( record.getLevel().getLocalizedName().toUpperCase() );
		builder.append( ChatColor.GRAY );
 		builder.append( "] " );
 		builder.append( ChatColor.WHITE );
 		builder.append( formatMessage( record ) );
 		builder.append( '\n' );
 		
 		if ( ex != null )
 		{
 			StringWriter writer = new StringWriter();
 			ex.printStackTrace( new PrintWriter( writer ) );
 			builder.append( writer );
 		}
 		
 		return handleAltColors( builder.toString() );
 	}
 	
 }
