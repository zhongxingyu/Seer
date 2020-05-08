 package org.dhappy.mimis;
 
 import netscape.javascript.JSObject;
 import netscape.javascript.JSException;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.activation.MimetypesFileTypeMap;
 
 import javax.swing.JApplet;
 
 import java.util.Arrays;
 import java.util.Map;
 import java.util.List;
 import java.util.Stack;
 import java.util.Queue;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Collection;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.concurrent.Executors;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 
 import java.security.PrivilegedAction;
 import java.security.AccessController;
 
 import java.util.logging.Logger;
 
 import com.sun.net.httpserver.Headers;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser.Order;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.NotFoundException;
 import org.neo4j.graphdb.TransactionFailureException;
 
 public class CacheAgentApplet extends JApplet {
     private static Logger log =
 	Logger.getLogger( CacheAgentApplet.class.getName() );
 
     JSObject window;
     Map<String,Object> store = new HashMap<String,Object>();
     String mimisValueKey = "__mimis_value";
     String dbPath = ".mimis/neo4j/db";
 
     public interface Callback {
         public Object call( Object data );
     }
 
     public interface DynamicValue {
         public Object value( Callback callback );
     }
 
     protected static EmbeddedGraphDatabase graphDb;
     HttpServer server;
 
     public Object localGet( String path ) {
         return localGet( path, null );
     }
 
     public Object localGet( String path, Callback callback ) {
         Map<String,Object> root = store;
         Object ret = null;
 
         Queue<String> elems = new LinkedList<String>();
         elems.addAll( Arrays.asList( path.split( "[./,]" ) ) );
 
         while( ! elems.isEmpty() && root != null ) {
             String elem = elems.poll();
             ret = root.get( elem );
             if( ret instanceof Map || ret == null ) {
                 root = (Map<String,Object>)ret;
             }
         }
         if( ! elems.isEmpty() ) {
             ret = null;
         } else if( ret instanceof Map ) {
             Map<String,Object> map = (Map<String,Object>)ret;
             Object val = map.get( mimisValueKey );
             if( val != null ) {
                 ret = val;
             }
         }
         
         if( ret instanceof DynamicValue ) {
             ret = ((DynamicValue)ret).value( callback );
         } else if( callback != null ) {
             callback.call( ret );
         }
 
         return ret;
     }
     
     public Object browserGet( String path ) {
         JSObject root = window;
         Object ret = null;
 
         Queue<String> elems = new LinkedList<String>();
         elems.addAll( Arrays.asList( path.split( "[./,]" ) ) );
 
         while( ! elems.isEmpty() && root != null && ret == null ) {
             String elem = elems.poll();
             ret = root.getMember( elem );
             if( ! elems.isEmpty()
                 && ( ret instanceof JSObject || ret == null ) ) {
                 root = (JSObject)ret;
                 ret = null;
             }
         }
         if( ! elems.isEmpty() ) {
             ret = null;
         }
         return ret;
     }
 
     Map<Integer,Callback> callbacks =
         new HashMap<Integer,Callback>();
     public Object browserGet( String path, Callback callback ) {
         JSObject browser = (JSObject)browserGet( "mimis.browser" );
         log.info( "get1:" + path );
         int id = callback.hashCode();
         JSObject jsCallback = (JSObject)window.eval
             ( "(function() { return function( data ) {"
               + "mimis.applet.callback( arguments.callee.mimis_id, data )"
               + "} })()" );
         jsCallback.setMember( "mimis_id", id );
         callbacks.put( id, callback );
         Object ret =
             browser.call( "get", new Object[] { path, jsCallback } );
         return ret;
     }
 
     public Object callback( int id, Object data ) {
         log.info( "cb:" + id );
         return callbacks.get( id ).call( data );
     }
 
     class MyHandler implements HttpHandler {
         public void handle( HttpExchange exchange ) throws IOException {
             String requestMethod = exchange.getRequestMethod();
             if( requestMethod.equalsIgnoreCase( "GET" ) ) {
                 Headers responseHeaders = exchange.getResponseHeaders();
                 responseHeaders.set( "Content-Type", "text/plain" );
                 exchange.sendResponseHeaders( 200, 0 );
 
                 OutputStream responseBody = exchange.getResponseBody();
                 Headers requestHeaders = exchange.getRequestHeaders();
                 Set<String> keySet = requestHeaders.keySet();
                 Iterator<String> iter = keySet.iterator();
                 while( iter.hasNext() ) {
                     String key = iter.next();
                     List values = requestHeaders.get( key );
                     String s = key + " = " + values.toString() + "\n";
                     responseBody.write( s.getBytes() );
                 }
                 responseBody.close();
             }
         }
     }
 
     // Called when the applet is loaded into the browser.
     public void init() {
         log.info( "Initializing Applet" );
 
         final JApplet container = this;
 
         window = JSObject.getWindow( container );
 
         log.info( "Loading Neo4j From: " + dbPath );
 
         graphDb = (EmbeddedGraphDatabase)AccessController.doPrivileged
             ( new PrivilegedAction() {
                     public Object run() {
                         try {
                             return new EmbeddedGraphDatabase( dbPath );
                         } catch( TransactionFailureException tfe ) {
                             log.warning( "DB Fail: " + tfe.getMessage() );
                         }
                         return null;
                     }
                 } );
 
         try {
             InetSocketAddress addr = new InetSocketAddress( 80 );
             server = HttpServer.create( addr, 0 );
             
             server.createContext( "/", new MyHandler() );
             server.setExecutor( Executors.newCachedThreadPool() );
             server.start();
             log.info( "Server is listening on port: " + addr.getPort() );
         } catch( IOException ioe ) {
             log.warning( "Server Init Failed: " + ioe.getMessage() );
         }
         
         try {
             //container.setContentPane( chooser );
             try {
                 localSet( "local.hostname",
                           new DynamicValue() {
                               String hostname = null;
                               
                               public Object value( Callback callback ) {
                                   if( hostname == null ) {
                                       hostname = "";
                                       try {
                                           InetAddress localMachine = InetAddress.getLocalHost();
                                           hostname = localMachine.getHostName();
                                       } catch( UnknownHostException uhe ) {}
                                   }
                                   if( callback != null ) {
                                       callback.call( hostname );
                                   }
                                   return hostname;
                               }
                               
                               public String toString() {
                                   return (String)value( null );
                               }
                           } );
 
                 localSet( "file.separator",
                           new DynamicValue() {
                               public Object value( Callback callback ) {
                                   if( callback != null ) {
                                       callback.call( File.separator );
                                   }
                                   return File.separator;
                               }
                           } );
             } catch( JSException e ) {
                 log.warning( "Callback Failed: " + e.getMessage() );
             }
         } catch( Exception e ) {
             e.printStackTrace();
             log.warning( "Failed to Load: " + e.getMessage()
                          + " (" + e.getClass().getName() + ")" );
         }
     }
 
     public void stop() {
         if( graphDb != null ) {
             graphDb.shutdown();
         }
         if( server != null ) {
             server.stop( 0 );
         }
         /*
         if( connection != null ) {
             connection.disconnect();
             connection = null;
         }
         */
     }
 
     public Object localSet( String path, Object value ) {
         Map<String,Object> root = store;
 
         for( String elem : path.split( "[./,]" ) ) {
             Object next = root.get( elem );
             if( next == null ) {
                 next = new HashMap<String,Object>();
                 root.put( elem, next );
             }
             if( ! ( next instanceof Map ) ) {
                 Map<String,Object> map = new HashMap<String,Object>();
                 map.put( mimisValueKey, next );
                 root.put( elem, map );
                 next = map;
             }
             root = (Map<String,Object>)next;
         }
         root.put( mimisValueKey, value );
         return root;
     }
 
     public Object get( String key ) {
         log.info( "Single param get" );
         return get( key, null );
     }
 
     public Object get( String key, final Object callback ) {
         Callback castCallback = null;
         if( callback instanceof Callback ) {
             castCallback = (Callback)callback;
         } else if( callback instanceof JSObject ) {
             castCallback = new Callback() {
                     public Object call( Object data ) {
                         if( ! ( data instanceof JSObject ) ) {
                             data = makeJSObject( data );
                         }
                         ((JSObject)callback).call( "call", new Object[] { callback, data } );
                         return data;
                     }
                 };
         } else if( callback != null ) {
             log.warning( getClass().getName()
                          + ".get called with callback type: "
                          + callback.getClass().getName() );
         }
 
         Object ret = localGet( key, castCallback );
         return ret;
     }
 
    public Callback castCallback( final Object callback ) {
         Callback castCallback = null;
         if( callback instanceof Callback ) {
             castCallback = (Callback)callback;
         } else if( callback instanceof JSObject ) {
             castCallback = new Callback() {
                     public Object call( Object data ) {
                         if( ! ( data instanceof JSObject ) ) {
                             data = makeJSObject( data );
                         }
                        ((JSObject)callback).call
                            ( "call", new Object[] { callback, data } );
                         return data;
                     }
                 };
         } else if( callback != null ) {
             log.warning( getClass().getName()
                         + " called with callback type: "
                          + callback.getClass().getName() );
         }
        return castCallback;
    }
 
    public Object listen( String key, Object callback ) {
        Callback castCallback = castCallback( callback );
         Object ret = localGet( key, castCallback );
         return ret;
     }
 
     public Object root() {
         // It would be nice if this could return a wrapper that
         // lazily greates entries
         return null;
     }
 
 
     public Object set( String key, Object value ) {
         return makeJSObject( localSet( key, value ) );
     }
 
     public Object list( final String path ) {
         log.info( "list0: " + path );
         return list( path, null );
     }
 
     public Object list( final String path, final Object callback ) {
         Callback castCallback = null;
         if( callback instanceof Callback ) {
             castCallback = (Callback)callback;
         } else if( callback instanceof JSObject ) {
             castCallback = new Callback() {
                     public Object call( Object data ) {
                         log.info( "Callback:" + path );
                         if( ! ( data instanceof JSObject ) ) {
                             data = makeJSObject( data );
                         }
                         ((JSObject)callback).call( "call", new Object[] { callback, data } );
                         return data;
                     }
                 };
         } else {
             log.warning( getClass().getName()
                          + ".get called with callback type: "
                          + callback.getClass().getName() );
         }
 
         log.info( "list: " + path );
         Object ret = null;
         if( path.startsWith( "file:" ) ) {
             ret = fileList( path.substring( 5 ), castCallback );
         } else if( path.startsWith( "xmpp:" ) ) {
             //ret = chatList( path.substring( 5 ), castCallback );
         }
         return ret;
     }
 
     public Object fileList( final String path, Callback callback ) {
         Stack<Map<String,Object>> paths = null;
         try {
             paths =
                 (Stack<Map<String,Object>>)AccessController.doPrivileged
                 ( new PrivilegedAction() {
                         public Object run() {
                             MimetypesFileTypeMap type = new MimetypesFileTypeMap();
                             Stack<Map<String,Object>> paths =
                                 new Stack<Map<String,Object>>();
                             File[] files =
                                 ( path.length() == 0 )
                                 ? File.listRoots()
                                 : (new File( path )).listFiles();
                             
                             for( File file : files ) {
                                 Map<String,Object> obj =
                                     new HashMap<String,Object>();
                                 try {
                                     String name = file.getName();
                                     if( name.length() == 0 ) {
                                         name = file.getCanonicalPath();
                                     }
                                     obj.put( "name", name );
                                     obj.put( "size",
                                              ( path.length() == 0
                                                ? file.getTotalSpace()
                                                : file.length() ) );
                                     obj.put( "readable", file.canRead() );
                                     obj.put( "writable", file.canWrite() );
                                     obj.put( "modified", file.lastModified() );
                                     obj.put( "type",
                                              ( file.isDirectory()
                                                ? "text/directory"
                                                : type.getContentType( file ) ) );
                                     paths.push( obj );
                                 } catch( IOException ioe ) {
                                     log.warning( ioe.getMessage() );
                                 }
                             }
                             return paths;
                         }
                     } );
             if( callback != null ) {
                 callback.call( paths );
             }
         } catch( Exception e ) {
             log.warning( e.getClass().getName() );
         }
         
         return paths;
     }
 
     public Object makeJSObject( Object input ) {
         Object ret = null;
         
         try {
             if( input.getClass().isArray() ) {
                 input = Arrays.asList( input );
             }
             if( input instanceof List ) {
                 log.info( ((List)input).size() + " : " + input.toString() );
                 String script = "[]"; //"(function() { return [] })()";
                 JSObject obj = (JSObject)window.eval( script );
                 int index = 0;
                 for( Object child : (List)input ) {
                     obj.setSlot( index++, makeJSObject( child ) );
                 }
                 ret = obj;
             } else if( input instanceof Map ) {
                 String script = "(function() { return {} })()";
                 JSObject obj = (JSObject)window.eval( script );
                 for( Map.Entry<String, Object> entry :
                          ( (Map<String, Object>)input ).entrySet() ) {
                     obj.setMember( entry.getKey(),
                                    makeJSObject( entry.getValue() ) );
                 }
                 ret = obj;
             } else if( input instanceof JSObject
                        || input instanceof Integer
                        || input instanceof Boolean
                        || input instanceof String ) {
                 ret = input;
             } else {
                 ret = input;
             }
         } catch( Exception e ) {
             log.warning( e.getClass().getName() + ": " + e.getMessage() );
         }
         
         return ret;
     }
 }
