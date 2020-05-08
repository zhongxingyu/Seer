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
 
 //import dk.dbc.opensearch.common.fedora.FedoraHandle;
 import dk.dbc.opensearch.common.metadata.DublinCoreElement;
 import dk.dbc.opensearch.common.metadata.MetaData;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.IndexingAlias;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.xml.XMLUtils;
 import dk.dbc.opensearch.components.datadock.DatadockJob;
 import dk.dbc.opensearch.common.types.IIdentifier;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 //import dk.dbc.opensearch.common.fedora.ObjectRepositoryException;
 
 import javax.xml.rpc.ServiceException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.xml.sax.InputSource;
 
 import java.net.MalformedURLException;
 import org.apache.commons.configuration.ConfigurationException;
 import java.io.IOException;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import mockit.Mock;
 import mockit.MockClass;
 import mockit.Mocked;
 import org.w3c.dom.Document;
 import static mockit.Mockit.setUpMocks;
 import static mockit.Mockit.tearDownMocks;
 
 
 public class MarcxchangeHarvesterTest
 {
     private MarcxchangeHarvester harvestPlugin;
     private CargoContainer cc;
     String submitter = "dbc";
     String format = "marcxchange";
     String language = "da";
 
     DatadockJob ddjob;
 
     static final String testPid1 = "test:1";
     static final String referenceData = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><referencedata><info submitter=\"775100\" format=\"ebrary\" lang=\"dk\"/></referencedata>";
     static final String data = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><ting:container xmlns:ting=\"http://www.dbc.dk/ting\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.bs.dk/standards/MarcXchange\" xmlns:dkabm=\"http://biblstandard.dk/abm/namespace/dkabm/\" xmlns:ISO639-2=\"http://lcweb.loc.gov/standards/iso639-2/\" xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:ac=\"http://biblstandard.dk/ac/namespace/\" xmlns:dkdcplus=\"http://biblstandard.dk/abm/namespace/dkdcplus/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><dkabm:record><ac:identifier>84813133|870971</ac:identifier><ac:source>Artikelbasen</ac:source><dc:title>Testtitel</dc:title><dc:creator>Bente Bundg@ård</dc:creator><dc:subject>Orange Order</dc:subject><dc:subject xsi:type='dkdcplus:DK5'>93.8</dc:subject><dc:subject xsi:type='dkdcplus:DK5-Text'>Irlands historie</dc:subject><dc:subject xsi:type='dkdcplus:DBCF'>marcher</dc:subject><dc:subject xsi:type='dkdcplus:DBCF'>historie</dc:subject><dc:date>1999</dc:date><dc:type xsi:type='dkdcplus:BibDK-Type'>Avisartikel</dc:type><dc:format>S. 11</dc:format><dc:language xsi:type='dcterms:ISO639-2'>dan</dc:language><dc:language>Dansk</dc:language><dcterms:isPartOf>Information 1999-02-26</dcterms:isPartOf><dcterms:spatial xsi:type='dkdcplus:DBCF'>Nordirland</dcterms:spatial><dcterms:spatial xsi:type='dkdcplus:DBCF'>Portadown</dcterms:spatial></dkabm:record><collection><record format=\"danMARC2\" type=\"Bibliographic\"><leader>00687naai022002770004500</leader><datafield tag=\"001\" ind1=\"0\" ind2=\"0\"><subfield code=\"a\">8 481 313 3</subfield><subfield code=\"b\">870971</subfield><subfield code=\"c\">20051128145709</subfield><subfield code=\"d\">19990226</subfield><subfield code=\"f\">a</subfield></datafield><datafield tag=\"004\" ind1=\"0\" ind2=\"0\"><subfield code=\"r\">n</subfield><subfield code=\"a\">i</subfield></datafield><datafield tag=\"008\" ind1=\"0\" ind2=\"0\"><subfield code=\"t\">a</subfield><subfield code=\"u\">f</subfield><subfield code=\"a\">1999</subfield><subfield code=\"b\">dk</subfield><subfield code=\"l\">dan</subfield><subfield code=\"v\">0</subfield><subfield code=\"r\">an</subfield></datafield><datafield tag=\"009\" ind1=\"0\" ind2=\"0\"><subfield code=\"a\">a</subfield><subfield code=\"g\">xx</subfield></datafield><datafield tag=\"016\" ind1=\"0\" ind2=\"0\"><subfield code=\"a\">0 324 309 5</subfield></datafield><datafield tag=\"032\" ind1=\"0\" ind2=\"0\"><subfield code=\"a\">ABU200549</subfield><subfield code=\"a\">DAR199908</subfield></datafield><datafield tag=\"086\" ind1=\"0\" ind2=\"0\"><subfield code=\"a\">Irlands historie</subfield></datafield><datafield tag=\"245\" ind1=\"0\" ind2=\"0\"><subfield code=\"a\">Orangemændene viger ikke</subfield></datafield><datafield tag=\"300\" ind1=\"0\" ind2=\"0\"><subfield code=\"a\">S. 11</subfield></datafield><datafield tag=\"557\" ind1=\"0\" ind2=\"0\"><subfield code=\"a\">Information</subfield><subfield code=\"j\">1999</subfield><subfield code=\"V\">1999-02-26</subfield><subfield code=\"v\">1999-02-26</subfield></datafield><datafield tag=\"610\" ind1=\"0\" ind2=\"0\"><subfield code=\"a\">Orange Order</subfield></datafield><datafield tag=\"652\" ind1=\"0\" ind2=\"0\"><subfield code=\"m\">93.8</subfield></datafield><datafield tag=\"666\" ind1=\"0\" ind2=\"0\"><subfield code=\"0\"></subfield><subfield code=\"f\">marcher</subfield></datafield><datafield tag=\"666\" ind1=\"0\" ind2=\"0\"><subfield code=\"0\"></subfield><subfield code=\"f\">historie</subfield></datafield><datafield tag=\"666\" ind1=\"0\" ind2=\"0\"><subfield code=\"0\"></subfield><subfield code=\"e\">Nordirland</subfield></datafield><datafield tag=\"666\" ind1=\"0\" ind2=\"0\"><subfield code=\"0\"></subfield><subfield code=\"e\">Portadown</subfield></datafield><datafield tag=\"700\" ind1=\"0\" ind2=\"0\"><subfield code=\"a\">Bundg@ård</subfield><subfield code=\"h\">Bente</subfield></datafield></record></collection></ting:container>";
     static final String invalidData = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><ting:hest xmlns:ting=\"http://www.dbc.dk/ting\">&</ting:hest>";
     static final byte[] databytes = data.getBytes();;
     static final byte[] invaliddatabytes = invalidData.getBytes();
     static final String noData = "";
     static final byte[] noDataBytes = noData.getBytes();
 
     @Mocked IIdentifier mockIdentifier;
     
     @MockClass( realClass = CargoContainer.class )
     public static class MockCargoContainer
     {
         @Mock public static long add( DataStreamType dataStreamName,
                      String format,
                      String submitter,
                      String language,
                      String mimetype,
                      IndexingAlias alias,
                      byte[] data ) throws IOException
         {
             throw new IOException( "test" );
         }
     }
 
     @MockClass( realClass = XPath.class )
     public static class MockXPathCompileXPathException
     {
         @Mock public XPathExpression compile( String expression ) throws XPathExpressionException
         {
            System.out.println( "hat0");
             throw new XPathExpressionException( "test" );
         }
     }
      
     @MockClass( realClass = XPathExpression.class )
     public static class MockXPathExpression
     {
         @Mock public XPathExpression compile( InputSource source ) throws XPathExpressionException
         {
            System.out.println( "hat1");
             throw new XPathExpressionException( "test" );
         }
     }
 
 
     @Before
     public void setUp() throws Exception
     {
 
         Document xmldata = XMLUtils.documentFromString( referenceData );
 
         ddjob = new DatadockJob( mockIdentifier, xmldata );
     }
 
     @After
     public void tearDown()
     {
         tearDownMocks();
         ddjob = null;
     }
 
     @Test
     public void getCargoContainerTest() throws Exception
     {
         harvestPlugin = new MarcxchangeHarvester();
         cc = harvestPlugin.getCargoContainer( ddjob, databytes );
         //There is data in the returned CargoContainer
         assertEquals( 1, cc.getCargoObjectCount() );
         //The added data has been given the correct DataStreamType
         assertEquals( true, cc.hasCargo( DataStreamType.OriginalData ) );
         //the pid given by the mocked fedoraHandle
         assertEquals( null, cc.getIdentifier() );
         //the metadata is of type dublincore and has the correct title
         assertEquals( "Testtitel" , cc.getDublinCoreMetaData().getDCValue( DublinCoreElement.ELEMENT_TITLE ) );
 
         //assertEquals( 1, cc.getMetaData().size() );
 
 
     }
 
     /**
      * The MarcxchangeHarvester plugin will recieve invalid data and
      * try to evaluate an xpath expression. This should throw a PluginException
      * with a nested XPathExpressionException
      */
    @Test
     public void getCargoContainerWithInvalidData() throws Exception
     {       
         harvestPlugin = new MarcxchangeHarvester();
         try{
             cc = harvestPlugin.getCargoContainer( ddjob, invaliddatabytes );
         }
         catch( PluginException pe )
         {
            assertEquals( "Expecting the nested exception to be XPathExpressionException", XPathExpressionException.class, pe.getException().getClass() );
         }
     }

     @Test
     public void testPluginType() throws Exception
     {        
         harvestPlugin = new MarcxchangeHarvester();
         assertEquals( PluginType.HARVEST, harvestPlugin.getPluginType() );
 
     }
     
     /**
      * tests the behaviour when no bytes are given to the plugin
      * An IllegalArgumentException should be thrown by the CargoContainer.add
      * method and be caught and wrapped in a PluginException
      */
     @Test( expected = IllegalArgumentException.class )
     public void noDataGivenTest() throws Exception
     {      
         harvestPlugin = new MarcxchangeHarvester();
         try
         {
             cc = harvestPlugin.getCargoContainer( ddjob, noDataBytes );
         }
         catch( PluginException pe )
         {
             throw pe.getException();
         }
 
     }
 
     /**
      * tests the behavoiour when the cargoContainers add method fails with a IOException
      */
     @Test( expected = IOException.class )
     public void cargoContainerCantAddDataTest() throws Exception
     {
         setUpMocks( MockCargoContainer.class );
       harvestPlugin = new MarcxchangeHarvester();
         try
         {
             cc = harvestPlugin.getCargoContainer( ddjob, databytes );
         }
         catch( PluginException pe )
         {
             throw pe.getException();
         }  
     }
 }
