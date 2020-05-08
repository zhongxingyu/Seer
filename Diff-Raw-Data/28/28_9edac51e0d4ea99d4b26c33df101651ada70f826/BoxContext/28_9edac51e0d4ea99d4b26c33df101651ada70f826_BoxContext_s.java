 package org.scriptbox.box.container;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.script.ScriptContext;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 
 import org.scriptbox.box.events.BoxContextListener;
 import org.scriptbox.box.events.BoxInvocationContext;
 import org.scriptbox.util.common.collection.Iterators;
 import org.scriptbox.util.common.obj.ParameterizedRunnable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class BoxContext {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger( BoxContext.class );
 	
 	private static final ThreadLocal<BoxContext> current = new ThreadLocal<BoxContext>();
 	
 	private String name;
 	private String language;
     private List<BoxContextListener> listeners;
     
 	private ScriptEngineManager factory = new ScriptEngineManager();
     private ScriptEngine engine;
     private Lookup beans = new MapLookup();
     private Box box;
     
     public static BoxContext getCurrentContext() {
     	BoxContext ret = current.get();
     	if( ret == null ) {
     		throw new RuntimeException( "Not currently executing in BoxContext" );
     	}
     	return ret;
     }
     
 	public static void with( BoxContext context, ParameterizedRunnable<BoxContext> runnable ) throws Exception {
 		current.set( context );
 		try {
 			runnable.run( context );
 		}
 		finally {
 			current.remove();
 		}
 	}
 	
     public BoxContext( Box box ) {
     	this.box = box;
     }
     
     public BoxContext( Box box, String language, String name ) {
     	this.box = box;
     	this.name = name;
     	this.language = language;
     }
 
     
     public Box getBox() {
 		return box;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setLanguage(String language) {
 		this.language = language;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getLanguage() {
 		return language;
 	}
 
 	public Lookup getBeans() {
 		return beans;
 	}
 
 	public Lookup getScriptVariables() {
 		return new Lookup() {
 			public void put(String name, Object obj) {
 				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "put: " + name + "=" + obj ); }
 				engine.put( name, obj );
 			}
 			@SuppressWarnings("unchecked")
 			public <X> X get(String name, Class<X> cls) {
 				return (X)engine.get( name );
 			}
 			public <X> X getEx(String name, Class<X> cls) {
 				X ret = get( name, cls );
 				if( ret == null ) {
 					throw new RuntimeException( "Could not find bean: '" + name + "' of type: " + cls );
 				}
 				return ret;
 			}
 			public Object remove(String name) {
 				Object ret = engine.get( name );
 				engine.put( name, null );
 				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "remove: " + name + "=" + ret ); }
 				return ret;
 			}
 			@SuppressWarnings("unchecked")
 			public <X> X remove(String name, Class<X> cls) {
 				return (X)remove( name );
 			}
 			public Map<String, Object> getAll() {
 				return engine.getBindings(ScriptContext.ENGINE_SCOPE);
 			}
 			@SuppressWarnings("unchecked")
 			public <X> Set<X> getAll( Class<X> cls ) {
 				Set<X> ret = new HashSet<X>();
 				for( Object obj : engine.getBindings(ScriptContext.ENGINE_SCOPE).values() ) {
 					if( obj != null && cls.isInstance(obj) ) {
 						ret.add( (X)obj );
 					}
 				}
 				return ret;
 			}
 		};
 	}
 	public ScriptEngineManager getFactory() {
 		return factory;
 	}
 
 	public ScriptEngine getEngine() {
 		return engine;
 	}
 
 	
 	public List<BoxContextListener> getListeners() {
 		return listeners;
 	}
 
 	public void setListeners(List<BoxContextListener> listeners) {
 		this.listeners = listeners;
 	}
 
 	public void run( final BoxScript script ) throws Exception {
 		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "run: executing script: " + script ); }
 		try {
 			current.set( this );
 			BoxScript.with( script, new ParameterizedRunnable<BoxScript>() {
 				public void run( final BoxScript bscript ) throws Exception{
 					BoxInvocationContext ctx = new BoxInvocationContext( engine, bscript, listeners );
 					ctx.next();
			        
			    	engine.put( "args", bscript.getArguments() );
			    	engine.eval( bscript.getScriptText() );
			    	
 				}
 			} );
 		}
 		finally {
 			current.remove();
 		}
     }
     
 	public void start() throws Exception {
 		factory = new ScriptEngineManager();
 	    engine = factory.getEngineByName(language);
 	    
 	    Logger lgr = LoggerFactory.getLogger(name);
 	    engine.put( "logger", lgr );
 	    engine.put( "LOGGER", lgr );
 	    
         callListeners( new ParameterizedRunnable<BoxContextListener>() {
         	public void run( BoxContextListener listener ) throws Exception {
         		listener.contextCreated( BoxContext.this );
         	}
         } );
 	}
 	
     public void shutdown() throws Exception {
         callListeners( new ParameterizedRunnable<BoxContextListener>() {
         	public void run( BoxContextListener listener ) throws Exception {
         		listener.contextShutdown( BoxContext.this );
         	}
         } );
     }
    
     protected void callListeners( ParameterizedRunnable<BoxContextListener> runner ) throws Exception {
         Iterators.callAndCollectExceptions(listeners, runner ).raiseException("Error calling listeners");
     }
     
 }
