 package commandproxy.core;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.simpleframework.http.Request;
 import org.simpleframework.http.Response;
 import org.simpleframework.http.core.Container;
 
 import commandproxy.core.commands.ChangeEncoding;
 import commandproxy.core.commands.Exec;
 import commandproxy.core.commands.Open;
 import commandproxy.core.commands.Restart;
 
 public class Proxy implements Container {
 	// All the commands we know
 	private Hashtable<String, Command> commands = new Hashtable<String, Command>();
 	private String key; 
 	
 	
 	/**
 	 * Create a new command proxy
 	 * 
 	 * No requests can be made to this instance without providing the correct 
 	 * security key. 
 	 * Turn this feature of by passing in <i>null</i> as key-value, however, 
 	 * this is only suggested when in debug mode.   
 	 * 
 	 * @param key The security key
 	 */
 	public Proxy( String key ){
 		this.key = key; 
 		
 		// Register the built-in commands
 		registerCommand( new Open() );
 		registerCommand( new Exec() );
 		registerCommand( new ChangeEncoding() );
 		registerCommand( new Restart() );
 	}
 	
 	/**
 	 * Adds commands from a plugin-loader
 	 */
 	public void loadPlugins( PluginLoader loader ){
 		// Register plugins from the plugin-loader
 		loader.loadPlugins( commands ); 
 	}
 
 	
 	public void handle( Request req, Response res ){
 		res.add( "Content-type", "text/html; charset=UTF-8" ); 
 		Log.debug.println( "==============================" );
 
 		String path = req.getPath().getName(); 
 		Command command = commands.get( path );  
 		PrintStream out;
 		Map<String, String> parameters;
 		
 		// Do we even have a handler for this?
 		if( command == null ){
 			Log.debug.println( "Invalid command requested: " + path ); 
 			abort( res, "Command not found: " + path );  
 			return; 
 		}
 		
 		try{
 			// Connect to the client
 			out = res.getPrintStream();
 			
 			// Get the request parameters
 			parameters = req.getForm();
 		}
 		catch( IOException e ){
 			Log.debug.println( "Invalid post parameters" ); 
 			abort( res, "Can't read post parameters" );
 			e.printStackTrace(); 
 			return; 
 		}
 		
 		Log.debug.println( "Request: " + path );
 		for( String key : parameters.keySet() ){
 			Log.debug.printf( "  %10s: %s\n", key, parameters.get( key ) ); 
 		}
 		
 		if( key != null && !key.equals( parameters.get( "commandproxyKey" ) ) ){
 			Log.debug.println( "Invalid security key" ); 
 			abort( res, "Invalid security key provided" ); 
			return; 
 		}
 		
 		// Awsome, we have everything we need. 
 		// Now let's execute that thing!
 		JSONObject result = null; 
 		try{
 			result = command.execute( parameters );
 		}
 		catch( Exception e ){
 			abort( res, e.getMessage() );
 		} 
 		
 		Log.debug.println( "------------------------------" ); 
 		Log.debug.println( "Response: " );
 		if( result == null ){
 			result = new JSONObject(); 
 		}
 		
 		try {
 			Log.debug.println( result.toString( 2 ) );
 		}
 		catch (JSONException e) {
 			e.printStackTrace();
 		}
 		out.println( result.toString() );
 		
 		out.flush(); 
 		out.close(); 
 	}
 	
 	
 	/**
 	 * Registers a command
 	 * @param command The command object
 	 */
 	public void registerCommand( Command command ){
 		commands.put( command.getName(), command ); 
 	}
 	
 	/**
 	 * Fails with a certain message and response code...
 	 * 
 	 * No data can be written to the response after this call
 	 * 
 	 * @param res The response object
 	 * @param code The http status code
 	 * @param message An error message, to be a little debug-friendlier
 	 */
 	private void abort( Response res, String message ){
 		try{
 			JSONObject result = new JSONObject();
 			result.put( "error", "Execution failed: " + message ); 
 
 			res.getPrintStream().print( result.toString() ); 
 			res.getPrintStream().flush(); 
 			res.close();
 		}
 		catch( Exception ex ){
 			ex.printStackTrace(); 
 		}
 	}
 	
 	
 	/**
 	 * Finds a file for you
 	 * @return 
 	 */
 	public static File getFile( String filename, Map<String, String> params ){
 		if( filename == null ){
 			return null; 
 		}
 		else{
 			return new File( filename ); 
 		}
 	}
 }
