 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.ac.ebi.chemet.io.mapping;
 
 import java.util.ArrayList;
 import java.util.List;
 import junit.framework.TestCase;
 import uk.ac.ebi.resource.classification.ECNumber;
 import uk.ac.ebi.resource.protein.UniProtIdentifier;
 
 /**
  *
  * @author johnmay
  */
 public class UniProtECMapperTest extends TestCase {
 
     public UniProtECMapperTest( String testName ) {
         super( testName );
     }
 
     /**
      * Test of parseKey method, of class UniProtECMapper.
      */
     public void testParseKey() {
         UniProtECMapper mapper = new UniProtECMapper( null , 0 , 1 );
 
         UniProtIdentifier upid_q6gzx4 = mapper.parseKey( "Q6GZX4" );
         assertEquals( "Q6GZX4" , upid_q6gzx4.getAccession() );
         // swissprot prefix
         UniProtIdentifier upid_sp_q6gzx4 = mapper.parseKey( "sp|Q6GZX4" );
         assertEquals( "Q6GZX4" , upid_sp_q6gzx4.getAccession() );
 
         // trembl prefix
         UniProtIdentifier upid_tr_q6gzx4 = mapper.parseKey( "tr|Q6GZX4" );
         assertEquals( "Q6GZX4" , upid_tr_q6gzx4.getAccession() );
     }
 
     /**
      * Test of parseValue method, of class UniProtECMapper.
      */
     public void testParseValue() {
         UniProtECMapper mapper = new UniProtECMapper( null , 0 , 1 );
 
 
         String ecs = "1.1.1.1;1.1.1.86";
 
         List<ECNumber> ecs_parsed = new ArrayList( mapper.parseValue( ecs ) );
         assertEquals( 2 , ecs_parsed.size() );
        assertTrue(ecs_parsed.contains(new ECNumber("1.1.1.1")));
        assertTrue( ecs_parsed.contains(new ECNumber( "1.1.1.86" )) );
 
         String ecs_withSpace = "1.1.3.1; 5.2.-.-";
        ecs_parsed = new ArrayList( mapper.parseValue( ecs_withSpace ));
        assertEquals(2, ecs_parsed.size());
        assertTrue(ecs_parsed.contains(new ECNumber("1.1.3.1")));
        assertTrue( ecs_parsed.contains(new ECNumber( "5.2.-.-" )) );
 
     }
 
 }
