 package controllers;                                                                                                                                     
                           
 import static models.Keys.PASSWORD;
 import static models.Keys.USERNAME;
 import static models.Repository.createEntity;
 import static models.Repository.db;
 import static models.Repository.entities;
 import static models.Repository.updateEntity;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import models.Repository;
 import models.User;
 
 import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import actions.BasicAuth;
 import actions.BasicAuthAction;
 import datomic.Entity;
 
 @BasicAuth
 public class Users extends Controller
 {
    private static final Logger LOGGER = LoggerFactory.getLogger( Users.class );
    
     public static Result list() 
     {   
         List<User> users = new LinkedList<User>();
         
         for ( Entity entity : entities( "[:find ?p :in $ :where [?p :username]]", db() ) )
         {
             users.add( new User( entity ) );
         }
         
         return ok( Json.toJson( users ) );
     }
     
     public static Result history()
     {
         return ok( Json.toJson( Repository.history( ":username" ) ) );
     }
 
     public static Result find( Long id )
     {
         Entity entity = db().entity( id );
         
         if ( entity != null )
         {
             return ok( Json.toJson( new User( entity ) ) );
         }
         
         return notFound();
     }
 
     public static Result create()
     {
         try
         {
             JsonNode node = request().body().asJson();
             
             if ( node.get( USERNAME ) == null )
             {
                 return badRequest( "Expected username" );
             }
             
             if ( node.get( PASSWORD ) == null )
             {
                 return badRequest( "Expected password" );
             }
 
             Map<String,Object> map = new HashMap<String,Object>();
             map.put( USERNAME, node.get( USERNAME ).getTextValue() );
             map.put( PASSWORD, BasicAuthAction.toMd5( node.get( PASSWORD ).getTextValue() ) );
             
             return created( Json.toJson( new User( createEntity( map ) ) ) );
         }
         catch( Exception e )
         {
             e.printStackTrace();
             return badRequest( e.getMessage() );
         }
     }
     
     public static Result update( Long id )
     {
         Entity entity = db().entity( id );
         
         if ( entity != null )
         {
             User user = new User( request().body().asJson() );
             updateEntity( id, user.asMap() );            
             return noContent();
         }
         
         return notFound();
     }
 
     public static Result delete( Long id )
     {
         if ( id != -1L )
         {
             delete( id );            
             return noContent();
         }
         
         return notFound();
     }    
 }
