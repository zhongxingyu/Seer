 package com.cedarsoft.couchdb.test.utils;
 
 import com.cedarsoft.couchdb.DesignDocument;
 import com.cedarsoft.couchdb.DesignDocuments;
 import com.cedarsoft.test.utils.JsonUtils;
 import org.junit.*;
 import org.xml.sax.SAXException;
 
 import javax.annotation.Nonnull;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.List;
 
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
     assertThat( files ).hasSize( 3 );
   }
 
   @Test
   public void testGetIt( ) throws Exception {
     File dir = DesignDocuments.guessBaseDir( getClass( ).getResource( "views/doc1/file1.map.js" ) );
     assertThat( dir.getName( ) ).isEqualTo( "doc1" );
 
     DesignDocument designDocument = DesignDocuments.createDesignDocument( "doc1", DesignDocuments.listJsFiles( dir ) );
     assertThat( designDocument.getViews( ) ).hasSize( 2 );
 
     JsonUtils.assertJsonEquals( getClass( ).getResource( "designDoc.json" ), designDocument.createJson( ) );
   }
 
   @Test
   public void testApi( ) throws Exception {
     DesignDocument designDocument = DesignDocuments.createDesignDocument( getClass( ).getResource( "views/doc1/file1.map.js" ) );
     verifyDoc1( designDocument );
   }
 
   @Test
   public void testEmpty( ) throws Exception {
     DesignDocument designDocument = DesignDocuments.createDesignDocument( getClass( ).getResource( "views/empty/none.txt" ) );
     assertThat( designDocument.hasViews( ) ).isFalse( );
   }
 
   @Test
   public void testPathNames( ) throws Exception {
     DesignDocument designDocument = DesignDocuments.createDesignDocument( getClass( ).getResource( "views/doc1/file1.map.js" ) );
     assertThat( designDocument.getDesignDocumentPath( ) ).isEqualTo( "_design/doc1" );
 
     assertThat( new DesignDocument( "asdf" ).getDesignDocumentPath( ) ).isEqualTo( "_design/asdf" );
     assertThat( new DesignDocument( "1233" ).getDesignDocumentPath( ) ).isEqualTo( "_design/1233" );
   }
 
   @Test
   public void testAllViews( ) throws Exception {
     List<? extends DesignDocument> designDocuments = DesignDocuments.createDesignDocuments( getClass( ).getResource( "views/doc1/file1.map.js" ) );
    assertThat( designDocuments ).hasSize( 2 );
 
     verifyDoc( designDocuments.get( 0 ) );
     verifyDoc( designDocuments.get( 1 ) );
   }
 
   private void verifyDoc( @Nonnull DesignDocument designDocument ) throws SAXException, IOException {
     if ( designDocument.getId( ).equals( "doc1" ) ) {
       verifyDoc1( designDocument );
     } else if ( designDocument.getId( ).equals( "doc2" ) ) {
       verifyDoc2( designDocument );
     } else {
       throw new AssertionError( "invalid id <" + designDocument.getId( ) + ">" );
     }
   }
 
   private void verifyDoc1( @Nonnull DesignDocument designDocument ) throws SAXException, IOException {
     assertThat( designDocument.getId( ) ).isEqualTo( "doc1" );
     JsonUtils.assertJsonEquals( getClass( ).getResource( "designDoc.json" ), designDocument.createJson( ) );
   }
 
   private void verifyDoc2( @Nonnull DesignDocument designDocument ) throws SAXException, IOException {
     assertThat( designDocument.getId( ) ).isEqualTo( "doc2" );
     JsonUtils.assertJsonEquals( getClass( ).getResource( "designDoc2.json" ), designDocument.createJson( ) );
   }
 
 }
