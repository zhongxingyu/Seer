 package dbc.opensearch.components.datadock.tests;
 /** \brief UnitTest for CargoContainerT **/
 
 import static org.junit.Assert.*;
 import org.junit.*;
 
 import java.io.InputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
//import dbc.opensearch.components.datadock.*;
import dbc.opensearch.components.datadock.CargoContainer;
 
 /**
  * 
  */
 public class CargoContainerTest {
 
     CargoContainer cargo;
     String teststring;
 
     @Before public void SetUp()throws UnsupportedEncodingException{
         teststring = "æøå";
         InputStream data = new ByteArrayInputStream( teststring.getBytes( "UTF-8" ) );
         try{
             cargo = new CargoContainer( data, "text/xml", "dk", "stm", "faktalink" );
         } catch ( IOException ioe ){
             System.out.println( ioe.toString() );
         }
     }
 
     /**
      * 
      */
     @Test public void testStreamSizeInContainer() {
         //utf-8 uses two bytes per danish letter
         int expectedLength = 6;
         assertTrue( expectedLength == cargo.getStreamLength() );
     }
 
     @Test(expected = NullPointerException.class) 
     public void testStreamCannotBeEmpty()throws IOException{
         InputStream is = new ByteArrayInputStream( new byte[0] );
         CargoContainer co = null;
         co = new CargoContainer( is, "", "", "", "" );
     }
 
     /** \todo: need real users and possibly a constructor-check instead of this */
     /** \todo: and this only really makes sense as a static method */
     // @Test public void testAllowedSubmitter() {
     //     assertTrue( cargo.checkSubmitter( "stm" ) );
     // }
 
     /** \todo: need real users and possibly a constructor-check instead of this */
     /** \todo: and this only really makes sense as a static method */
     // @Test public void testDisallowedSubmitter() {
     //     assertFalse( cargo.checkSubmitter( "NonExistantSubmitter" ) );
     // }
 
     @Test public void testGetByteArrayPreservesUTF8()throws IOException, UnsupportedEncodingException{
         byte[] sixBytes = cargo.getDataBytes();
         assertTrue( teststring.equals( new String( sixBytes, "UTF-8" ) ) );
     }
 
     @Test public void testgetDataBAOSPreservesUTF8()throws IOException, UnsupportedEncodingException{
         ByteArrayOutputStream baos = cargo.getDataBAOS();
         assertTrue( teststring.equals( baos.toString( "UTF-8" ) ) );
     }
 
     @Test public void testGetMimetype(){
         assertTrue( "text/xml".equals( cargo.getMimeType()) );
     }
 
 }
