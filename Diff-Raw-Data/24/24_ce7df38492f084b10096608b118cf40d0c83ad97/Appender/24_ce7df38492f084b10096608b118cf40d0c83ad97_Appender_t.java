 package net.praqma.util.debug.appenders;
 
 import java.io.PrintWriter;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.LoggerSetting;
 import net.praqma.util.debug.Logger.LogLevel;
 
 public class Appender {
 	private LogLevel minimumLevel = LogLevel.INFO;
 	protected PrintWriter out;
 	private Set<String> subscriptions = new LinkedHashSet<String>();
 	private boolean enabled = true;
 	private boolean subscribeAll = true;
 	
	protected String template = "%datetime %level %space [%tag] %stack %message%newline";
 	
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
 }
