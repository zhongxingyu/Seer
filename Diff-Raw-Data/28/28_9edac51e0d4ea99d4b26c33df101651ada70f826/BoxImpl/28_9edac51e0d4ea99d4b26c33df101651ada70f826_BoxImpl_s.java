 package org.scriptbox.box.remoting;
 
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.scriptbox.box.container.Box;
 import org.scriptbox.box.container.BoxContext;
 import org.scriptbox.box.container.StatusProvider;
 import org.scriptbox.box.controls.BoxService;
 import org.scriptbox.box.controls.BoxServices;
 import org.scriptbox.util.common.error.Errors;
 import org.scriptbox.util.common.error.ExceptionHelper;
 import org.scriptbox.util.common.obj.ParameterizedRunnable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class BoxImpl implements BoxInterface {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger( BoxImpl.class );
 	
 	private Box box;
 
 	public Box getBox() {
 		return box;
 	}
 
 	public void setBox(Box box) {
 		this.box = box;
 	}
 
 	public void start() throws Exception {
 		box.start();
 		BoxServices.start(getBoxServices(), null).raiseException( "Error starting services");
 	}
 	
 	public void shutdown() throws Exception {
 		Errors<BoxService> errors = BoxServices.shutdown(getBoxServices()).logError( "Error stopping services", LOGGER );
 		box.shutdown();
 		errors.raiseException( "Error stopping services" );
 	}
 	
 	@Override
 	public void createContext(String language, String contextName) throws Exception {
 		box.createContext( language, contextName );
 	}
 
 	@Override
 	public void startContext(String contextName, List arguments ) throws Exception {
 		BoxContext ctx = box.getContextEx( contextName );
 		BoxServices.start(getContextServices(contextName), arguments).raiseException( "Error starting services");
 	}
 	
 	@Override
 	public void stopContext(String contextName ) throws Exception {
 		BoxServices.stop(getContextServices(contextName)).raiseException( "Error suspending services");
 	}
 
 	@Override
 	public void shutdownContext(String contextName) throws Exception {
 		Errors<BoxService> errors = BoxServices.shutdown(getContextServices(contextName));
 		errors.logError( "Error", LOGGER);
 		box.shutdownContext( contextName );
 		errors.raiseException( "Error shutting down services");
 	}
 
 	@Override
 	public void shutdownAllContexts() throws Exception {
 		Errors<BoxService> errors = new Errors<BoxService>();
 		for( BoxContext context : box.getContexts().values() ) {
 			errors.addAll( BoxServices.shutdown(context.getBeans().getAll(BoxService.class)) );
 		}
 		errors.logError( "Error", LOGGER);
 		box.shutdownAllContexts();
 		errors.raiseException( "Error shutting down services");
 	}
 
 	@Override
 	public void loadScript(String contextName, String scriptName, String script, List arguments) throws Exception{
 		box.run( contextName, scriptName, script, arguments );
 	}
 
 	public String status() throws Exception {
 		final StringBuilder builder = new StringBuilder( 1024 );
 		Set<StatusProvider> bstats = box.getBeans().getAll( StatusProvider.class );
 		if( bstats != null && bstats.size() > 0 ) {
 			builder.append( "Box{\n" );
 			buildStatus( builder, bstats );
 			builder.append( "}\n" );
 			
 		}
 		Map<String,BoxContext> contexts = box.getContexts();
 		for( Map.Entry<String,BoxContext> entry : contexts.entrySet() ) {
 			BoxContext.with(entry.getValue(), new ParameterizedRunnable<BoxContext>() {
 				public void run( BoxContext context ) {
 					Set<StatusProvider> cstats = context.getBeans().getAll(StatusProvider.class );
					if( cstats != null && cstats.size() > 0 ) {
						builder.append( "Context[" + context.getName() + "] {\n" );
						buildStatus( builder, cstats );
						builder.append( "}" );
					}
 				}
 			} );
 		}
 		return builder.toString();
 	}
 	
 	private void buildStatus( StringBuilder builder, Set<StatusProvider> stats ) {
 		if( stats != null && stats.size() > 0 ) {
 			for( StatusProvider stat : stats ) {
 				try {
 					builder.append( "    " + stat.status() + "\n" );
 				}
 				catch( Exception ex ) {
 					LOGGER.error( "Error getting status", ex );
 					builder.append( ExceptionHelper.toString(ex) );
 				}
 			}
 		}
 	}
 	
 	private Set<BoxService> getBoxServices() {
 		return box.getBeans().getAll(BoxService.class);
 	}
 	private Set<BoxService> getContextServices( String contextName ) {
 		return  box.getContextEx(contextName).getBeans().getAll(BoxService.class);
 	}
 }
