 package com.cedarsoft.couchdb.test.utils;
 
 import com.cedarsoft.test.utils.JsonUtils;
 import org.junit.*;
 import sun.security.krb5.internal.crypto.Des;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Collection;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class DesignDocumentsTest {
 
   private URL resource;
 
   @Before
   public void setUp( ) throws Exception {
     resource = getClass( ).getResource( "views/doc1/file1.map.js" );
   }
 
   @Test
   public void testBaseDir( ) throws Exception {
     File dir = DesignDocuments.guessBaseDir( resource );
     assertThat( dir ).isDirectory( );
     assertThat( dir.getName( ) ).isEqualTo( "doc1" );
 
     Collection<? extends File> files = DesignDocuments.listJsFiles( dir );
    assertThat( files ).hasSize( 2 );
   }
 
   @Test
   public void testGetIt( ) throws Exception {
     File dir = DesignDocuments.guessBaseDir( getClass( ).getResource( "views/doc1/file1.map.js" ) );
     assertThat( dir.getName( ) ).isEqualTo( "doc1" );
 
     DesignDocuments.DesignDocument designDocument = DesignDocuments.createDesignDocument( "doc1", DesignDocuments.listJsFiles( dir ) );
     assertThat( designDocument.getViews( ) ).hasSize( 2 );
 
     JsonUtils.assertJsonEquals( getClass().getResource( "designDoc.json" ), designDocument.createJson() );
   }
 
 }
