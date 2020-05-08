 package com.cedarsoft.couchdb;
 
 import com.cedarsoft.couchdb.io.ActionFailedExceptionSerializer;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.filter.ClientFilter;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.Base64;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.jcouchdb.db.Database;
 import org.jcouchdb.util.CouchDBUpdater;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class CouchUtils {
   @Nonnull
   private final CouchDatabase database;
 
   public CouchUtils( @Nonnull CouchDatabase database ) {
     this.database = database;
   }
 
   public void create() throws ActionFailedException {
     ClientResponse response = database.getDbRoot().put( ClientResponse.class );
     ActionResponse.verifyNoError( response );
   }
 
   /**
    * Returns whether the given database exists
    *
    * @return true if the db exists, false otherwise
    */
   public boolean exists() throws ActionFailedException {
     ClientResponse response = database.getDbRoot().get( ClientResponse.class );
     if ( response.getStatus() == 200 ) {
       return true;
     }
     if ( response.getStatus() == 404 ) {
       return false;
     }
 
     throw new ActionFailedExceptionSerializer().deserialize( response.getStatus(), response.getEntityInputStream() );
   }
 
   public void uploadViews( @Nonnull URL viewResource ) throws URISyntaxException, IOException {
     CouchDBUpdater updater = new CouchDBUpdater();
     updater.setCreateDatabase( false );
     updater.setDatabase( createInternalDb() );
 
     File file = new File( viewResource.toURI() );
     File viewsDir = file.getParentFile().getParentFile();
 
     assertTrue( viewsDir.isDirectory() );
     updater.setDesignDocumentDir( viewsDir );
 
     updater.updateDesignDocuments();
   }
 
   @Nonnull
   public Database createInternalDb() {
     //    return new Database( database.getDbRoot().getUriBuilder().replacePath( "" ).build().toString(), database.getDbName() );
     URI uri = database.getDbRoot().getURI();
     Database internalDb = new Database( uri.getHost(), uri.getPort(), database.getDbName() );
 
     UsernamePasswordCredentials credentials = extractCredentials( database.getClientFilters() );
     if ( credentials != null ) {
       internalDb.getServer().setCredentials( AuthScope.ANY, credentials );
     }
     return internalDb;
   }
 
   @Nullable
  public static UsernamePasswordCredentials extractCredentials( @Nonnull ClientFilter[] clientFilters ) {
     for ( ClientFilter clientFilter : clientFilters ) {
       if ( clientFilter instanceof HTTPBasicAuthFilter ) {
         return extractCredentials( ( HTTPBasicAuthFilter ) clientFilter );
       }
     }
     return null;
   }
 
   @Nonnull
  public static UsernamePasswordCredentials extractCredentials( @Nonnull HTTPBasicAuthFilter clientFilter ) {
     try {
       Field field = HTTPBasicAuthFilter.class.getDeclaredField( "authentication" );
       field.setAccessible( true );
       String value = ( String ) field.get( clientFilter );
 
      String userPass = new String( Base64.decode( value.substring( "Basic ".length() ) ) );
       int index = userPass.indexOf( ":" );
 
       String user = userPass.substring( 0, index );
       String pass = userPass.substring( index + 1 );
 
       return new UsernamePasswordCredentials( user, pass );
     } catch ( Exception e ) {
       throw new RuntimeException( e );
     }
   }
 
   @Nonnull
   public CouchDatabase getDatabase() {
     return database;
   }
 }
