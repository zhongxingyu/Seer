 /*
    
 This file is part of opensearch.
 Copyright © 2009, Dansk Bibliotekscenter a/s, 
 Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
 opensearch is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 opensearch is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 
 package dk.dbc.opensearch.common.types;
 
 
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoMimeType;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.CargoObjectInfo;
 import dk.dbc.opensearch.common.types.DataStreamType;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 
 import static org.junit.Assert.*;
 import org.junit.*;
 
 
 public class CargoObjectInfoTest
 {
 
     // Default values to use in CargoObjectInfo:
     DataStreamType test_datastreamtype = DataStreamType.OriginalData;
     CargoMimeType test_mimetype = CargoMimeType.TEXT_XML;;
     String test_language = new String("da");
     String test_submitter = new String("DBC");
     String test_format = new String("test_format");
     long test_id = 0L;
 
     // default CargoObjectInfo used in several tests:
     CargoObjectInfo default_coi = null;
 
     @Before
     public void setUp()
     {
 	default_coi = new CargoObjectInfo( test_datastreamtype, test_mimetype, test_language,
 					   test_submitter, test_format, test_id );
     }
     
     @Test ( expected = IllegalArgumentException.class )
     public void testConstructorDataStreamTypeNotNull()
     {
 	new CargoObjectInfo( null, test_mimetype, test_language,
 			     test_submitter, test_format, test_id );
     }
 
     @Test ( expected = IllegalArgumentException.class )
     public void testConstructorMimeTypeNotNull()
     {
 	new CargoObjectInfo( test_datastreamtype, null, test_language,
 			     test_submitter, test_format, test_id );
     }
 
     @Test ( expected = IllegalArgumentException.class )
     public void testConstructorLanguageNotNull()
     {
 	new CargoObjectInfo( test_datastreamtype, test_mimetype, null,
 			     test_submitter, test_format, test_id );
     }
 
     @Test ( expected = IllegalArgumentException.class )
     public void testConstructorSubmitterNotNull()
     {
 	new CargoObjectInfo( test_datastreamtype, test_mimetype, test_language,
 			     null, test_format, test_id );
     }
 
     @Test ( expected = IllegalArgumentException.class )
     public void testConstructorFormatNotNull()
     {
 	new CargoObjectInfo( test_datastreamtype, test_mimetype, test_language,
 			     test_submitter, null, test_id );
     }
 
     @Test
     public void testGetDataStreamType()
     {
 	assertTrue( default_coi.getDataStreamType().equals( test_datastreamtype ) );
     }
 
     /**
      * This one is a little bit tricky - we use a variable of
      * CargoMimeType as parameter, but when we retrieve the variable
      * it is converted to a String - through
      * CargoMimeType.getMimeType(). This test is therefore more of a
      * whitebox test than a blackbox test.
      */    
     @Test 
     public void testGetMimeType() 
     {
 	assertTrue( default_coi.getMimeType().equals( test_mimetype.getMimeType() ) );
     }
 
     @Test 
     public void testGetLanguage() 
     {    	
 	assertTrue( default_coi.getLanguage().equals( test_language ) );
     }
 
     @Test 
     public void testGetSubmitter() 
     {    	
 	assertTrue( default_coi.getSubmitter().equals( test_submitter ) );
     }
 
     @Test 
     public void testGetFormat() 
     {
 	assertTrue( default_coi.getFormat().equals( test_format ) );
     }
 
     @Test 
     public void testGetId() 
     {
 	assertTrue( default_coi.getId() == test_id );
     }
 
     /**
      * We can not test the getTimestamp exactly, but the documentation
      * says that the timestamp is set on the time of
      * creation. Therefore we can get a timestamp immediately before
      * creating a CargoObjectInfo and immediately after. The
      * getTimestamp() should return a timestamp somewhere between
      * those two timestamps.
      */
     @Test
     public void testGetTimestamp()
     {
 	Date timestampBefore = new Date();
 	CargoObjectInfo coi = new CargoObjectInfo( DataStreamType.OriginalData, CargoMimeType.TEXT_XML,
 						   "da", "Submitter", "Format", 0L);
 	Date timestampAfter = new Date();
 
 	assertTrue( coi.getTimestamp() >= timestampBefore.getTime() );
 	assertTrue( coi.getTimestamp() <= timestampAfter.getTime() );
     }


    /**
     * A test of toString according to the javadoc.
     */
    @Test
    public void testToString()
    {
	String expectedResult = String.format( "CargoObjectInfo[ %s , %s , %s , %s , %s , %s ]",
					       test_datastreamtype.toString(), test_mimetype.toString(),
					       test_language, test_submitter, test_format, test_id );

	assertTrue( default_coi.toString().equals( expectedResult ) );
    }
 }
