 package models;
 
 import static datomic.Connection.TEMPIDS;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import play.mvc.Http.Context;
 import util.AppException;
 import util.ToMany;
 import datomic.Connection;
 import datomic.Database;
 import datomic.Entity;
 import datomic.Peer;
 import datomic.Util;
 
 public class Repository
 {
    private static final Logger LOGGER = LoggerFactory.getLogger( Repository.class );
    
     private static final String uri = "datomic:free://localhost:4334/playtomic";
     
     public static void main( String[] args )
     {
         System.out.println( "Deleting database..." );
         Peer.deleteDatabase( uri );
         System.out.println( "Creating database..." );
         Peer.createDatabase( uri );
 
         load( new File( "conf/schema.edn" ) );
         load( new File( "data/users.edn" ) );
     }
     
     private static Connection CONN = null;
     
     private static Connection conn()
     {
         if ( CONN != null ) return CONN;        
         CONN = Peer.connect( uri );
         return CONN;
     }
     
     public static Database db()
     {
         return conn().db();
     }
     
     public static Long identify( String query, Object ... params )
     {
         Collection<List<Object>> results = Peer.q( query, params );
         if ( results.size() > 0 ) return (Long)results.iterator().next().get( 0 );
         return -1L;
     }
 
     @SuppressWarnings("rawtypes")
     public static void load( File file )
     {
         try
         {
             // need to invoke Peer *before* reading schema!
             Repository.conn(); 
             System.out.println( "Loading " + file );
             Reader reader = new FileReader( file );
             transact( (List)Util.readAll( reader ).get(0) );
         }
         catch( Exception e )
         {
             throw new RuntimeException( e.getMessage(), e );
         }
     }
     
     public static Collection<List<Object>> values( String query, Object ... params )
     {
         return Peer.q( query, params );
     }
     
     public static List<Entity> entities( String query, Object ... params )
     {
         List<Entity> result = new LinkedList<Entity>();
         
         for( List<Object> list : Peer.q( query, params ) )
         {            
             result.add( db().entity( list.get( 0 ) ) );
         }
         
         return result;        
     }
 
     public static Entity entity( String query, Object ... params )
     {
         for( List<Object> list : Peer.q( query, params ) )
         {            
             return db().entity( list.get( 0 ) );
         }
         
         return null;
     }
     
     @SuppressWarnings("rawtypes")
     private static Map metadata()
     {
         Map<String,Object> map = new HashMap<String,Object>();
         
         map.put( ":db/id", Peer.tempid( "db.part/tx" ) );
         
         Entity user = (Entity)Context.current().args.get( ":user" );
         
         if ( user != null )
         {
             map.put( ":user", user.get( ":db/id" ) );
         }
         
         return map;
     }
     
     @SuppressWarnings("rawtypes")
     public static Entity createEntity( Map<String,Object> userdata )
     {
         Object tempid = Peer.tempid( "db.part/user" );
         userdata.put( ":db/id", tempid );
         Map result = transact( Util.list( userdata, metadata() ) );
         return db().entity( Peer.resolveTempid( db(), result.get( TEMPIDS ), tempid ) );
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public static void updateEntity( Long id, Map<String,Object> userdata )
     {
         List list = new ArrayList();
         
         Entity entity = db().entity( id );
         
         if ( entity != null )
         {
             for ( String key : userdata.keySet() )
             {
                 Object oldValue = entity.get( key );
                 Object newValue = userdata.get( key );
                 
                LOGGER.info(
                         "key: " + key + ", " +
                         "old: " + oldValue + ( oldValue != null ? " (" + oldValue.getClass() + ") " : " " ) +
                         "new: " + newValue + ( newValue != null ? " (" + newValue.getClass() + ") " : " " ) );                        
 
                 if ( ! ( oldValue == null && newValue == null ) )
                 {
                     if ( oldValue instanceof Set || newValue instanceof Set )
                     {
                         for ( Object next : ToMany.additions( (Set)oldValue, (Set)newValue ) )
                         {
                             list.add( Util.list( ":db/add", id, key, next ) );
                         }
 
                         for ( Object next : ToMany.retractions( (Set)oldValue, (Set)newValue ) )
                         {
                             list.add( Util.list( ":db/retract", id, key, next ) );
                         }                        
                     }
                     else
                     {
                         if ( newValue != null )
                         {
                             list.add( Util.list( ":db/add", id, key, newValue ) );
                         }
                         else
                         {
                             list.add( Util.list( ":db/retract", id, key, oldValue ) );
                         }
                     }
                 }
             }
         }
         else
         {
             throw new AppException( "Entity with id " + id + " does not exist." ); 
         }
         
         if ( list.size() > 0 )
         {
             list.add( metadata() );
            
            System.out.println( "Update: " + list );
            
             transact( list );
         }
     }
     
     public static void deleteEntity( Long id )
     {
         transact( Util.list( Util.list( ":db.fn/retractEntity", id ), metadata() ) );
     }
     
     public static List<Change> history( String attribute )
     {
         List<Change> history = new LinkedList<Change>();
         
         final String query = "[:find ?add ?e ?k ?v ?dt :in $ ?attr :where " +
                              "[?e ?a ?v ?tx ?add]" +
                              "[?e ?attr _]" +
                              "[?a :db/ident ?k]" +
                              "[?tx :db/txInstant ?dt]" +
                              "[?tx :user ?u]" +
                              "]";
                                      
         Collection<List<Object>> found = Peer.q( query, db().history(), attribute );
         
        System.out.println( "Found: " + found.size() );
        
         for ( List<Object> list : found )
         {
             history.add( new Change( list ) );
         }
         
         Collections.sort( history, new Change.Comparator() );
         
         return history;
     }
     
     @SuppressWarnings({ "rawtypes" })
     private static Map transact( List list )
     {
         try
         {
             return conn().transact( list ).get();
         }
         catch( ExecutionException e )
         {
             if ( e.getCause() != null )
             {
                 throw new AppException( e.getCause().getMessage(), e );
             }
             
             throw new AppException( "Got ExecutionException while invoking transact()", e );
         }
         catch( InterruptedException e )
         {
             throw new AppException( "Transaction got interrupted", e );
         }        
     }    
 }
