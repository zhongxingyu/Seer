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
  * \file
  * \brief
  */
 package dk.dbc.opensearch.fedora;
 
 import dk.dbc.opensearch.types.CargoContainer;
 import dk.dbc.opensearch.types.DataStreamType;
 
 import org.fcrepo.server.types.gen.FieldSearchQuery;
 import org.fcrepo.server.types.gen.FieldSearchResult;
 import org.fcrepo.server.types.gen.ObjectFields;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.xml.rpc.ServiceException;
 
 
 import mockit.Mock;
 import mockit.MockClass;
 import static mockit.Mockit.setUpMocks;
 import static mockit.Mockit.tearDownMocks;
 
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.LogManager;
 import org.custommonkey.xmlunit.SimpleNamespaceContext;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.fcrepo.client.FedoraClient;
 import org.fcrepo.server.access.FedoraAPIA;
 import org.fcrepo.server.types.gen.DatastreamDef;
 import org.fcrepo.server.types.gen.MIMETypedStream;
 import org.fcrepo.server.types.gen.ObjectMethodsDef;
 import org.fcrepo.server.types.gen.ObjectProfile;
 import org.fcrepo.server.types.gen.Property;
 import org.fcrepo.server.types.gen.RepositoryInfo;
 import org.junit.After;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * \Todo testen dækker ikke alle metoder. Ret op på dette.
  */
 public class FcrepoReaderTest
 {
     FcrepoReader instance;
     static final String samePid = "test:1";
     static final String testPid = "test:2";
     static final String testdPid = "test:3";
     static final String expectedFoxml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foxml:digitalObject xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" PID=\"test:1\" VERSION=\"1.1\" xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd\" xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\"><foxml:objectProperties><foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/><foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"testFormat\"/><foxml:property NAME=\"info:fedora/fedora-system:def/model#ownerId\" VALUE=\"testSubmitter\"/><foxml:property NAME=\"info:fedora/fedora-system:def/model#createdDate\" VALUE=\"2009-09-26T21:27:00.065\"/><foxml:property NAME=\"info:fedora/fedora-system:def/view#lastModifiedDate\" VALUE=\"2009-09-26T21:27:00.065\"/></foxml:objectProperties><foxml:datastream CONTROL_GROUP=\"M\" ID=\"originalData.0\" STATE=\"A\" VERSIONABLE=\"false\"><foxml:datastreamVersion CREATED=\"2009-09-26T21:27:00.058\" ID=\"originalData.0.0\" LABEL=\"testFormat\" MIMETYPE=\"text/xml\" SIZE=\"1\"><foxml:binaryContent>IA==</foxml:binaryContent></foxml:datastreamVersion></foxml:datastream><foxml:datastream CONTROL_GROUP=\"X\" ID=\"adminData\" STATE=\"A\" VERSIONABLE=\"true\"><foxml:datastreamVersion CREATED=\"2009-09-26T21:27:00.269\" ID=\"adminData.0\" LABEL=\"administration stream\" MIMETYPE=\"text/xml\" SIZE=\"221\"><foxml:xmlContent><admin-stream><indexingalias name=\"docbook\"/><streams><stream format=\"testFormat\" id=\"originalData.0\" index=\"0\" lang=\"da\" mimetype=\"text/xml\" streamNameType=\"originalData\" submitter=\"testSubmitter\"/></streams></admin-stream></foxml:xmlContent></foxml:datastreamVersion></foxml:datastream></foxml:digitalObject>";
     static final String administrationStream = "<admin-stream><streams><stream format=\"testFormat\" id=\"originalData.0\" index=\"0\" lang=\"da\" mimetype=\"text/xml\" streamNameType=\"originalData\" submitter=\"testSubmitter\"/><stream format=\"testFormat\" id=\"DC\" index=\"0\" lang=\"da\" mimetype=\"text/xml\" streamNameType=\"dublinCoreData\" submitter=\"testSubmitter\"/></streams></admin-stream>";
     static final String dublinCoreStream = "<?xml version=\"1.0\"?><oai_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\"><dc:title>Test title</dc:title><dc:identifier>test:1</dc:identifier></oai_dc:dc>";
 
     public static class MockFedoraAPIA implements FedoraAPIA
     {
         public RepositoryInfo describeRepository() throws RemoteException
         {
             return null;
         }
 
 
         public ObjectProfile getObjectProfile( String string, String string1 ) throws RemoteException
         {
             return null;
         }
 
 
         public ObjectMethodsDef[] listMethods( String string, String string1 ) throws RemoteException
         {
             return null;
         }
 
 
         public DatastreamDef[] listDatastreams( String string, String string1 ) throws RemoteException
         {
             return new DatastreamDef[]
                     {
                         new DatastreamDef( "Stream1", "Label1", "Mimetype1" ),
                         new DatastreamDef( "Stream2", "Label2", "Mimetype2" ),
                     };
         }
 
 
         public MIMETypedStream getDatastreamDissemination( String pid, String datastreamId, String asOfDateTime ) throws RemoteException
         {
             byte[] retarray = null;
             if( pid.equals( samePid ) )
             {
                 if( datastreamId.equals( "originalData.0" ) )
                 {
                     retarray = "original data".getBytes();
                 }
                 else if( datastreamId.equals( "DC" ) )
                 {
                     retarray = dublinCoreStream.getBytes();
                 }
                 else if( datastreamId.equals( "adminData" ) )
                 {
                     retarray = administrationStream.getBytes();
                 }
             }
             else if( pid.equals( testPid ) ) // with no dc data
             {
                 if( datastreamId.equals( "originalData.0" ) )
                 {
                     retarray = "original data".getBytes();
                 }
                 else if( datastreamId.equals( "adminData" ) )
                 {
                     retarray = administrationStream.getBytes();
                 }
             }
             else
             {
                 retarray = "".getBytes();
             }
             return new MIMETypedStream( null, retarray, null );
         }
 
 
         public MIMETypedStream getDissemination( String string, String string1, String string2, Property[] prprts, String string3 ) throws RemoteException
         {
             return null;
         }
 
 
         public FieldSearchResult findObjects( String[] strings, NonNegativeInteger nni, FieldSearchQuery fsq ) throws RemoteException
         {
             FieldSearchResult fsr = new FieldSearchResult();
             fsr.setResultList(
                     new ObjectFields[]
                     {
                         new ObjectFields( samePid, null, "A", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null ),
                         new ObjectFields( testPid, null, "I", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null ),
                         new ObjectFields( testdPid, null, "D", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null )
                     } );
             return fsr;
         }
 
 
         public FieldSearchResult resumeFindObjects( String string ) throws RemoteException
         {
             return null;
         }
 
 
         public String[] getObjectHistory( String string ) throws RemoteException
         {
             return null;
         }
     }
 
     @MockClass(realClass = FedoraClient.class)
     public static class MockFedoraClient
     {
         @Mock
         public void $init( String url, String user, String passwd )
         {
         }
 
 
         @Mock
         public FedoraAPIA getAPIA() throws ServiceException, IOException
         {
             return new MockFedoraAPIA();
         }
     }
 
 
     @BeforeClass
     public static void generalSetup()
     {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel( Level.DEBUG );
 
         HashMap<String, String> m = new HashMap<String, String>();
         m.put( "x", "info:fedora/fedora-system:def/foxml#" );
         SimpleNamespaceContext ctx = new SimpleNamespaceContext( m );
         XMLUnit.setXpathNamespaceContext( ctx );
     }
 
 
     @Before
     public void setUp() throws Exception
     {
         setUpMocks( MockFedoraClient.class );
         instance = new FcrepoReader( "Host", "Port", "User", "Pass" );
     }
 
 
     @After
     public void tearDown() throws Exception
     {
         tearDownMocks();
     }
 
 
     @Test
     public void testGetObject() throws Exception
     {
         String identifier = "test:1";
         CargoContainer result = instance.getObject( identifier );
         assertEquals( identifier, result.getIdentifierAsString() );
     }
 
 
     @Test
     public void testHasObject() throws Exception
     {
         String identifier = "test:1";
         boolean result = instance.hasObject( identifier );
         assertTrue( result );
     }
 
 
     @Test
     public void testGetObjectReturnsCorrectDCStream() throws Exception
     {
         String identifier = "test:1";
         CargoContainer result = instance.getObject( identifier );
         assertTrue( null != result.getCargoObject( DataStreamType.DublinCoreData ) );
     }
 
 
     @Test(expected = IllegalStateException.class)
     public void testGetObjectThatDoesntExist() throws Exception
     {
         String identifier = "null:1";
         CargoContainer result = instance.getObject( identifier );
         assertTrue( result == null );
     }
 
 
     @Test
     public void testGetIdentifiersByState() throws Exception
     {
         Set< String > x = new HashSet< String >();
         x.add( "A" );
         x.add( "I" );
         List<OpenSearchCondition> conditions = new ArrayList<OpenSearchCondition>();
         conditions.add( new OpenSearchCondition( FedoraObjectFields.PID, OpenSearchCondition.Operator.EQUALS, "*" ) );
 
         List<String> result = instance.getIdentifiersByState( conditions, 100, x );
         assertTrue( result.size() == 2 );
         assertEquals( samePid, result.get( 0 ) );
         assertEquals( testPid, result.get( 1 ) );
     }
 
     @Test
     public void testListDatastreamIds() throws Exception
     {
         String identifier = "test:1";
         String[] expected = { "Stream1", "Stream2"};
 
         String[] result = instance.listDatastreamIds( identifier );
 
         assertArrayEquals( expected , result );
     }
 
     @Test
     public void testGetDatastreamDissemination() throws Exception
     {
         String identifier = "test:1";
 
         byte[] result = instance.getDatastreamDissemination( identifier, "adminData" );
         assertArrayEquals( administrationStream.getBytes(), result );
     }
 }
