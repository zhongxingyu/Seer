 package net.praqma.util.debug.appenders;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.LoggerSetting;
 import net.praqma.util.debug.Logger.LogLevel;
 
 /**
  * Appender class for the logger
  * @author wolfgang
  *
  */
 public class Appender {
 	private LogLevel minimumLevel = LogLevel.INFO;
 	protected PrintWriter out;
 	private Set<String> subscriptions = new LinkedHashSet<String>();
 	private boolean enabled = true;
 	private boolean subscribeAll = true;
 	
 	private String threadId = null;
 	
 	protected String template = "%datetime %level %space %thread%stack %message%newline";
 	
 	private String tag;
 	
 	private Logger logger = Logger.getLogger();
 	
 	public Appender() {
 		
 	}
 	
 	public Appender( PrintWriter out ) {
 		this.setOut( out );
 	}
 	
 	public Appender( PrintWriter out, LogLevel level ) {
 		this.setOut( out );
 		this.minimumLevel = level;
 	}
 	
 	public <T> void subscribe( Class<T> tclass ) {
 		if( !subscriptions.contains( tclass.getCanonicalName() ) ) {
 			subscriptions.add( tclass.getCanonicalName() );
 			subscribeAll = false;
 		}
 	}
 	
 	public void subscribe( String tclass ) {
 		if( !subscriptions.contains( tclass ) ) {
 			subscriptions.add( tclass );
 			subscribeAll = false;
 		}
 	}
 	
 	public void setSubscriptions( Set<String> subscriptions ) {
 		System.out.println( "Setting subs: " + subscriptions );
 		this.subscriptions = subscriptions;
 	}
 	
 	public void setSettings( LoggerSetting settings ) {
 		logger.debug( "Setting, " + settings.getMinimumLevel() + ": " + settings.getSubscriptions() );
 		this.minimumLevel = settings.getMinimumLevel();
 		this.setSubscriptions( settings.getSubscriptions() );
 	}
 	
 	public boolean onBeforeLogging() {
 		return true;
 	}
 	
 	public Set<String> getSubscriptions() {
 		return subscriptions;
 	}
 	
 	public boolean isSubscribed( String str ) {
 		for( String s : subscriptions ) {
 			if( s.startsWith( str ) ) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	public LogLevel getMinimumLevel() {
 		return minimumLevel;
 	}
 
 	public void setMinimumLevel( LogLevel minimumLevel ) {
 		this.minimumLevel = minimumLevel;
 	}
 
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	public void setEnabled( boolean enabled ) {
 		this.enabled = enabled;
 	}
 
 	public boolean isSubscribeAll() {
 		return subscribeAll;
 	}
 
 	public void setSubscribeAll( boolean subscribeAll ) {
 		this.subscribeAll = subscribeAll;
 	}
 
 	public PrintWriter getOut() {
 		return out;
 	}
 
 	public void setOut( PrintWriter out ) {
 		this.out = out;
 	}
 
 	public String getTemplate() {
 		return template;
 	}
 
 	public void setTemplate( String template ) {
 		this.template = template;
 	}
 
 	public String getTag() {
 		return tag;
 	}
 
 	public void setTag( String tag ) {
 		this.tag = tag;
 	}
 	
 	public void lockToCurrentThread() {
 		String tid = Logger.getThreadId( Thread.currentThread() );
 		threadId = tid;
 	}
 	
 	public String getThreadId() {
 		return threadId;
 	}
 	
 	/**
 	 * Write the given input stream to the appender
 	 * @param input
 	 */
 	public void write( InputStream input ) {
 		BufferedReader in = new BufferedReader( new InputStreamReader( input ) );
 		String line = "";
 		try {
 			while( ( line = in.readLine() ) != null ) {
				getOut().write( line + Logger.linesep );
 				getOut().flush();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
