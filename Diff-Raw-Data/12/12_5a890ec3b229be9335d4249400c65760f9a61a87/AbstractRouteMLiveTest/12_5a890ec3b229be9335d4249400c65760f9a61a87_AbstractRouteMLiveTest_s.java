 package org.commonjava.routem.rest.live;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import java.io.File;
 import java.lang.reflect.Type;
 import java.net.MalformedURLException;
 
 import javax.inject.Inject;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.log4j.Level;
 import org.commonjava.couch.test.fixture.LoggingFixture;
 import org.commonjava.routem.data.RouteDataManager;
 import org.commonjava.util.logging.Logger;
 import org.commonjava.web.common.model.Listing;
 import org.commonjava.web.common.ser.JsonSerializer;
 import org.commonjava.web.test.fixture.TestWarArchiveBuilder;
 import org.commonjava.web.test.fixture.WebFixture;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 
 import com.google.gson.reflect.TypeToken;
 
 public abstract class AbstractRouteMLiveTest
 // extends AbstractRESTCouchTest
 {
 
     @BeforeClass
     public static void logging()
     {
         LoggingFixture.setupLogging( Level.DEBUG );
     }
 
     protected static WebArchive createWar( final Class<?> cls )
     {
         return new TestWarArchiveBuilder( cls ).withExtraClasses( AbstractRouteMLiveTest.class )
                                                .withLog4jProperties()
                                                .withLibrariesIn( new File( "target/dependency" ) )
                                                .build();
     }
 
     // @Inject
     // @RouteMData
     // protected CouchManager couchManager;
 
     @Inject
     protected RouteDataManager dataManager;
 
     protected final Logger logger = new Logger( getClass() );
 
     @Before
     public final void setupTest()
         throws Exception
     {
         logger.info( "Installing DB using dataManager: %s", dataManager );
         dataManager.install();
 
         final ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager();
         ccm.setMaxTotal( 20 );
     }
 
     protected static final String HOST = "localhost";
 
     protected static final int PORT = 8080;
 
     @Inject
     protected JsonSerializer serializer;
 
     @Rule
     public WebFixture http = new WebFixture();
 
     protected void disableRedirection()
     {
         http.disableRedirection();
     }
 
     protected void enableRedirection()
     {
         http.enableRedirection();
     }
 
     protected void assertLocationHeader( final HttpResponse response, final String value )
     {
         final Header[] headers = response.getHeaders( "Location" );
         assertThat( headers, notNullValue() );
         assertThat( headers.length, equalTo( 1 ) );
 
         final String header = headers[0].getValue();
         assertThat( header, equalTo( value ) );
     }
 
     protected <T> T get( final String url, final Class<T> type )
         throws Exception
     {
         return http.get( url, type );
     }
 
     protected void get( final String url, final int expectedStatus )
         throws Exception
     {
         http.get( url, expectedStatus );
     }
 
     protected HttpResponse getWithResponse( final String url, final int expectedStatus )
         throws Exception
     {
         return http.getWithResponse( url, expectedStatus );
     }
 
     protected HttpResponse getWithResponse( final String url, final int expectedStatus, final String accept )
         throws Exception
     {
         return http.getWithResponse( url, expectedStatus, accept );
     }
 
     protected <T> Listing<T> getListing( final String url, final TypeToken<Listing<T>> typeToken )
         throws Exception
     {
         return http.getListing( url, typeToken );
     }
 
     protected HttpResponse delete( final String url )
         throws Exception
     {
         return http.delete( url );
     }
 
     protected HttpResponse post( final String url, final Object value, final int status )
         throws Exception
     {
         return http.post( url, value, status );
     }
 
     protected HttpResponse post( final String url, final Object value, final Type type, final int status )
         throws Exception
     {
         return http.post( url, value, type, status );
     }
 
    protected String resourceUrl( final String path )
         throws MalformedURLException
     {
         return http.resourceUrl( path );
     }
 
     protected String apiVersion()
     {
         return "1.0";
     }
 
 }
