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
 
 /**
  * \file MarcxchangeHarvesterTest.java
  * \brief Unittest for MarcxchangeHarvester class
  */
 
 
 package dk.dbc.opensearch.plugins;
 
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.components.datadock.DatadockJob;
 import dk.dbc.opensearch.components.harvest.IIdentifier;
import dk.dbc.opensearch.common.pluginframework.PluginException;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import mockit.Mock;
 import mockit.MockClass;
 import mockit.Mockit;
 import static org.easymock.classextension.EasyMock.*;
 
 
 public class MarcxchangeHarvesterTest
 {
     private MarcxchangeHarvester harvestPlugin;
     private CargoContainer cc;
     String submitter = "dbc";
     String format = "marcxchange";
     String language = "DA";
     Document referenceData;
     DocumentBuilderFactory docBuilderFactory;
     DocumentBuilder docBuilder;
 
     IIdentifier mockIdentifier;
 
     Document buildTestDocument( String submitter, String format, String language, boolean infoIsNull, boolean rootIsNull ) throws ParserConfigurationException
     {
         docBuilderFactory = DocumentBuilderFactory.newInstance();
         docBuilder = docBuilderFactory.newDocumentBuilder();
         Document theDocument = docBuilder.newDocument();
         if( !rootIsNull )
         {
             Element root = theDocument.createElement( "referencedata" );
             if( !infoIsNull )
             {
                 Element info = theDocument.createElement( "info" );
                 
                 info.setAttribute( "submitter", submitter );
                 info.setAttribute( "format", format );
                 info.setAttribute( "language", language );
                 root.appendChild( (Node)info );
                 
             }
             theDocument.appendChild( (Node)root);
         }
         return theDocument;
     }
     
     @MockClass( realClass = MarcxchangeHarvester.class )
     public static class MockMarcxchangeHarvester
     {
         @Mock( invocations = 5 ) 
         public String getDCVariable( byte[] bytes, String xPathStr )
         {
             return "DCVariable";
         }
     }
 
     @MockClass( realClass = CargoContainer.class )
     public static class MockCargoContainer
     {
 
         @Mock( invocations = 1 )
         public CargoObject getCargoObject( DataStreamType type )
         {
             return null;
         }
     }
 
     @Before
     public void setUp()
     {
         mockIdentifier = createMock( IIdentifier.class );
     }
 
     @After
     public void tearDown()
     {
         Mockit.tearDownMocks();
         reset( mockIdentifier );
     }
 
     @Test
     public void getCargoContainerTest() throws Exception
     {
         //setup
         Mockit.setUpMocks( MockMarcxchangeHarvester.class );
         String dataString = "dataString";
         byte[] data = dataString.getBytes();
         referenceData = buildTestDocument( submitter, format, language, false, false );
         DatadockJob ddj = new DatadockJob( submitter, format, mockIdentifier, referenceData );
         //expectations
 
         //replay
         replay( mockIdentifier );
 
         //do stuff
         harvestPlugin = new MarcxchangeHarvester();
         CargoContainer cc = harvestPlugin.getCargoContainer( ddj, data );
 
         //verify
         verify( mockIdentifier );
 
     } 
 
    @Test( expected = PluginException.class )
     public void constructDCFailsTest() throws Exception
     {
         Mockit.setUpMocks( MockCargoContainer.class );
          String dataString = "dataString";
         byte[] data = dataString.getBytes();
         referenceData = buildTestDocument( submitter, format, language, false, false );
         DatadockJob ddj = new DatadockJob( submitter, format, mockIdentifier, referenceData );
         //replay
         replay( mockIdentifier );
 
         //do stuff
         harvestPlugin = new MarcxchangeHarvester();
         CargoContainer cc = harvestPlugin.getCargoContainer( ddj, data );
 
         //verify
         verify( mockIdentifier );
 
     }
 }
