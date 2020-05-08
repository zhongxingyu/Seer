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
 package dk.dbc.opensearch.common.fedora;
 
 
 
 import dk.dbc.opensearch.common.fedora.FedoraAdministration;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.IndexingAlias;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.ValidationException;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.xml.sax.SAXException;
 import org.custommonkey.xmlunit.*;
 import org.custommonkey.xmlunit.exceptions.XpathException;
 import java.util.HashMap;
 import org.custommonkey.xmlunit.SimpleNamespaceContext;
 import org.apache.commons.codec.binary.Base64;
 import dk.dbc.opensearch.xsd.types.DatastreamTypeCONTROL_GROUPType;
 
 
 /**
  *
  */
 public class FedoraAdministrationTest// extends XMLTestCase
 {
 
     CargoContainer cargo;
     String origStr;
 
     static String utf8Str = "æøå";
     static Date now = new Date ( System.currentTimeMillis() );
    static String timeNow = (new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.S" )).format( now );
     static String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
         "<digitalObject xmlns=\"info:fedora/fedora-system:def/foxml#\" VERSION=\"1.1\" PID=\"dbc:1\"><objectProperties><property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"Active\"/><property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"label_1\"/><property NAME=\"info:fedora/fedora-system:def/model#ownerId\" VALUE=\"dbc\"/><property NAME=\"info:fedora/fedora-system:def/model#createdDate\" VALUE=\"" + timeNow + "\"/><property NAME=\"info:fedora/fedora-system:def/view#lastModifiedDate\" VALUE=\"" + timeNow + "\"/></objectProperties><datastream ID=\"originalData.0\" CONTROL_GROUP=\"M\" STATE=\"A\" VERSIONABLE=\"false\"><datastreamVersion ID=\"originalData.0.0\" LABEL=\"test [text/xml]\" CREATED=\"" + timeNow + "+02:00\" MIMETYPE=\"text/xml\" SIZE=\"6\"><binaryContent>w6bDuMOl</binaryContent></datastreamVersion></datastream><datastream ID=\"adminData\" CONTROL_GROUP=\"M\" STATE=\"A\" VERSIONABLE=\"false\"><datastreamVersion ID=\"adminData.0\" LABEL=\"admin [text/xml]\" CREATED=\"" + timeNow + "+02:00\" MIMETYPE=\"text/xml\" SIZE=\"247\"><binaryContent>PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48YWRtaW4tc3RyZWFtPjxpbmRleGluZ2FsaWFzIG5hbWU9ImFydGljbGUiLz48c3RyZWFtcz48c3RyZWFtIGZvcm1hdD0idGVzdCIgaWQ9Im9yaWdpbmFsRGF0YS4wIiBpbmRleD0iMCIgbGFuZz0iZW5nIiBtaW1ldHlwZT0idGV4dC94bWwiIHN0cmVhbU5hbWVUeXBlPSJvcmlnaW5hbERhdGEiIHN1Ym1pdHRlcj0iZGJjIi8+PC9zdHJlYW1zPjwvYWRtaW4tc3RyZWFtPg==</binaryContent></datastreamVersion></datastream></digitalObject>";
 
 
     @BeforeClass
     public static void SetupClass()
     {
         HashMap m = new HashMap();
         m.put("x", "info:fedora/fedora-system:def/foxml#");
         SimpleNamespaceContext ctx = new SimpleNamespaceContext(m);
         XMLUnit.setXpathNamespaceContext( ctx );
     }
 
     @Before
     public void SetUp() throws IOException, MarshalException, ValidationException, ParseException, ParserConfigurationException, SAXException, TransformerConfigurationException, TransformerException
     {
         byte[] cargoBytes =  utf8Str.getBytes( "UTF-8" );
         cargo = new CargoContainer( );
         cargo.add( DataStreamType.getDataStreamNameFrom( "originalData" ), "test", "dbc", "eng", "text/xml", IndexingAlias.getIndexingAlias( "article" ) , cargoBytes);
         byte[] b = FedoraAdministration.constructFoxml( cargo, "dbc:1", "label_1", now );
         origStr = new String( b );
 
     }
 
     @After
     public void TearDown()
     {
         cargo = null;
     }
 
     /** 
      * The following tests aims at validating that the fields in the
      * objectProperties are set correctly. The xpath expressions are
      * somewhat naive and relies on a specific ordering of the
      * elements which - luckily - is preserved by the serialization in
      * the constructFoxml method.
      * 
      * The first test validates the serialization itself. Failure in
      * this test does not imply that the subsequent XMLAsserts should
      * fail.
      */
     @Test public void testDigitalObjectSerialization() throws SAXException, IOException
     {
         XMLAssert.assertXMLEqual( "Comparing test xml to control xml", expected, origStr );
     }
 
     /** 
      * Tests that the correct pid was inserted in the digital object properties
      */
     @Test
     public void testCorrectPIDInsertion() throws SAXException, IOException, XpathException
     {
         XMLAssert.assertXpathEvaluatesTo( "dbc:1", "/x:digitalObject[1]/@PID", origStr);
     }
 
     /** 
      * Tests that the correct state was inserted in the digital object properties
      */
     @Test
     public void testState() throws SAXException, IOException, XpathException
     {
         XMLAssert.assertXpathEvaluatesTo( "Active", "/x:digitalObject[1]/x:objectProperties[1]/x:property[1]/@VALUE", origStr );
     }
 
     /** 
      * Tests that the correct label was inserted in the digital object properties
      */
     @Test
     public void testLabel() throws SAXException, IOException, XpathException
     {
         XMLAssert.assertXpathEvaluatesTo( "label_1", "/x:digitalObject[1]/x:objectProperties[1]/x:property[2]/@VALUE", origStr );
     }
 
     /** 
      * Tests that the correct owner was inserted in the digital object properties
      */
     @Test
     public void testOwner() throws SAXException, IOException, XpathException
     {
         XMLAssert.assertXpathEvaluatesTo( "dbc", "/x:digitalObject[1]/x:objectProperties[1]/x:property[3]/@VALUE", origStr );
     }
 
     /** 
      * Tests that the correct timestamp was inserted in the digital
      * object properties. The format of the datestring is internally
      * validated (by the serialization framework).
      */
     @Test
     public void testTimestamp() throws SAXException, IOException, XpathException
     {
         XMLAssert.assertXpathEvaluatesTo( timeNow, "/x:digitalObject[1]/x:objectProperties[1]/x:property[4]/@VALUE", origStr );
     }
 
     /** 
      * Tests that the datastream id is set to the first added cargoobject
      */
     @Test
     public void testDatastreamID() throws SAXException, IOException, XpathException
     {
         XMLAssert.assertXpathEvaluatesTo( "originalData.0", "/x:digitalObject[1]/x:datastream[1]/@ID", origStr );
 
     }
 
     /** 
      * A test that really serves two purposes, but in one test because
      * if one fails, both fails.  The first test ensures that the
      * binary data was written to the digital object, the second, that
      * the encoding of the string was correct.
      * 
      */
     @Test
     public void testBinaryContent() throws SAXException, IOException, XpathException
     {
         XMLAssert.assertXpathExists( "/x:digitalObject[1]/x:datastream[1]/x:datastreamVersion[1]/x:binaryContent[1]", origStr );
 
         byte[] encodedBytes  = Base64.encodeBase64( utf8Str.getBytes() );
         XMLAssert.assertXpathEvaluatesTo( new String( encodedBytes ), "/x:digitalObject[1]/x:datastream[1]/x:datastreamVersion[1]/x:binaryContent[1]", origStr );
     }
 
     @Test 
     public void testControlGroup() throws SAXException, IOException, XpathException
     {
         XMLAssert.assertXpathEvaluatesTo( DatastreamTypeCONTROL_GROUPType.M.toString(), "/x:digitalObject[1]/x:datastream[1]/@CONTROL_GROUP", origStr );
     }
 
     @Test 
     public void testHasAdminStreamInDigitalObjectAfterSerialization() throws SAXException, IOException, XpathException
     {
         XMLAssert.assertXpathEvaluatesTo( "adminData", "/x:digitalObject[1]/x:datastream[2]/@ID", origStr );
 
     }
 
     @Test 
     public void testCorrectAdminStream() throws SAXException, IOException, XpathException
     {
         
         String adminDataString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><admin-stream><indexingalias name=\"article\"/><streams><stream format=\"test\" id=\"originalData.0\" index=\"0\" lang=\"eng\" mimetype=\"text/xml\" streamNameType=\"originalData\" submitter=\"dbc\"/></streams></admin-stream>";
 
         byte[] encodedBytes  = Base64.encodeBase64( adminDataString.getBytes() );
         XMLAssert.assertXpathEvaluatesTo( new String( encodedBytes ), "/x:digitalObject[1]/x:datastream[2]/x:datastreamVersion[1]/x:binaryContent[1]", origStr );
         
     }
 }
