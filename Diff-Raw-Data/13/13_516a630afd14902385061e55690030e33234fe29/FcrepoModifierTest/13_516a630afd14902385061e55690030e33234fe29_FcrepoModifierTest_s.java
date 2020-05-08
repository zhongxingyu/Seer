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
 
 import dk.dbc.opensearch.metadata.DBCBIB;
 import dk.dbc.opensearch.types.CargoContainer;
 import dk.dbc.opensearch.types.DataStreamType;
 import java.io.ByteArrayInputStream;
 import org.fcrepo.server.types.gen.Datastream;
 
 import org.fcrepo.server.types.gen.FieldSearchQuery;
 import org.fcrepo.server.types.gen.FieldSearchResult;
 import org.fcrepo.server.types.gen.ObjectFields;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.rpc.ServiceException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import mockit.Expectations;
 
 
 import mockit.Mock;
 import mockit.MockClass;
 import mockit.Mocked;
 import mockit.Mocked;
 import static mockit.Mockit.setUpMocks;
 import static mockit.Mockit.tearDownMocks;
 
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.LogManager;
 import org.custommonkey.xmlunit.SimpleNamespaceContext;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.easymock.EasyMock;
 import org.fcrepo.client.FedoraClient;
 import org.fcrepo.server.management.FedoraAPIM;
 import org.fcrepo.server.types.gen.DatastreamDef;
 import org.fcrepo.server.types.gen.MIMETypedStream;
 import org.fcrepo.server.types.gen.ObjectMethodsDef;
 import org.fcrepo.server.types.gen.ObjectProfile;
 import org.fcrepo.server.types.gen.Property;
 import org.fcrepo.server.types.gen.RelationshipTuple;
 import org.fcrepo.server.types.gen.RepositoryInfo;
 import org.fcrepo.server.types.gen.Validation;
 import org.junit.After;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.xml.sax.InputSource;
 
 /**
  * \Todo testen dækker ikke alle metoder. Ret op på dette.
  */
 public class FcrepoModifierTest
 {
     FcrepoModifier instance;
     static FedoraAPIM apimInstance = new MockFedoraAPIM();
     static final String samePid = "test:1";
     static final String testPid = "test:2";
     static final String testdPid = "test:3";
     static final String expectedFoxml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foxml:digitalObject xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" PID=\"test:1\" VERSION=\"1.1\" xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd\" xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\"><foxml:objectProperties><foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/><foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"testFormat\"/><foxml:property NAME=\"info:fedora/fedora-system:def/model#ownerId\" VALUE=\"testSubmitter\"/><foxml:property NAME=\"info:fedora/fedora-system:def/model#createdDate\" VALUE=\"2009-09-26T21:27:00.065\"/><foxml:property NAME=\"info:fedora/fedora-system:def/view#lastModifiedDate\" VALUE=\"2009-09-26T21:27:00.065\"/></foxml:objectProperties><foxml:datastream CONTROL_GROUP=\"M\" ID=\"originalData.0\" STATE=\"A\" VERSIONABLE=\"false\"><foxml:datastreamVersion CREATED=\"2009-09-26T21:27:00.058\" ID=\"originalData.0.0\" LABEL=\"testFormat\" MIMETYPE=\"text/xml\" SIZE=\"1\"><foxml:binaryContent>IA==</foxml:binaryContent></foxml:datastreamVersion></foxml:datastream><foxml:datastream CONTROL_GROUP=\"X\" ID=\"adminData\" STATE=\"A\" VERSIONABLE=\"true\"><foxml:datastreamVersion CREATED=\"2009-09-26T21:27:00.269\" ID=\"adminData.0\" LABEL=\"administration stream\" MIMETYPE=\"text/xml\" SIZE=\"221\"><foxml:xmlContent><admin-stream><indexingalias name=\"docbook\"/><streams><stream format=\"testFormat\" id=\"originalData.0\" index=\"0\" lang=\"da\" mimetype=\"text/xml\" streamNameType=\"originalData\" submitter=\"testSubmitter\"/></streams></admin-stream></foxml:xmlContent></foxml:datastreamVersion></foxml:datastream></foxml:digitalObject>";
     static final String administrationStream = "<admin-stream><streams><stream format=\"testFormat\" id=\"originalData.0\" index=\"0\" lang=\"da\" mimetype=\"text/xml\" streamNameType=\"originalData\" submitter=\"testSubmitter\"/><stream format=\"testFormat\" id=\"DC\" index=\"0\" lang=\"da\" mimetype=\"text/xml\" streamNameType=\"dublinCoreData\" submitter=\"testSubmitter\"/></streams></admin-stream>";
     static final String dublinCoreStream = "<?xml version=\"1.0\"?><oai_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\"><dc:title>Test title</dc:title><dc:identifier>test:1</dc:identifier></oai_dc:dc>";
 
     public static class MockFedoraAPIM implements FedoraAPIM
     {
         public String ingest(byte[] objectXML, String format, String logMessage) throws RemoteException
         {
             String newPid = "new:1";
 
             NamespaceContext nsc = new FedoraNamespaceContext();
             XPath xpath = XPathFactory.newInstance().newXPath();
             xpath.setNamespaceContext( nsc );
             XPathExpression xPathExpression = null;
             String xPathStr = "/foxml:digitalObject[1]/@PID";
             InputSource dataInput = new InputSource( new ByteArrayInputStream( objectXML ) );
             try
             {
                 xPathExpression = xpath.compile( xPathStr );
                 String foundPid = xPathExpression.evaluate( dataInput );
 
                 if( foundPid.equals( samePid ) )
                 {
                     return samePid;
                 }
                 else if( foundPid.equals( testPid ) )
                 {
                     return testPid;
                 }
                 return newPid;
             }
             catch( XPathExpressionException e )
             {
                 throw new RemoteException("Exception parsing XML", e);
             }
         }
 
         public String modifyObject(String pid, String state, String label, String ownerId, String logMessage) throws RemoteException
         {
             if (samePid.equals( pid ))
             {
                 return "pid";
             }
             else
             {
                 throw new RemoteException();
             }
         }
 
         public byte[] getObjectXML(String pid) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public byte[] export(String pid, String format, String context) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public String purgeObject(String pid, String logMessage, boolean force) throws RemoteException
         {
             if( pid.equals( samePid ) || pid.equals( testPid ) )
             {
                 return "timestamp";
             }
 
             return null;
         }
 
         public String addDatastream(String pid, String dsID, String[] altIDs, String dsLabel, boolean versionable, String MIMEType, String formatURI, String dsLocation, String controlGroup, String dsState, String checksumType, String checksum, String logMessage) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public String modifyDatastreamByReference(String pid, String dsID, String[] altIDs, String dsLabel, String MIMEType, String formatURI, String dsLocation, String checksumType, String checksum, String logMessage, boolean force) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public String modifyDatastreamByValue(String pid, String dsID, String[] altIDs, String dsLabel, String MIMEType, String formatURI, byte[] dsContent, String checksumType, String checksum, String logMessage, boolean force) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public String setDatastreamState(String pid, String dsID, String dsState, String logMessage) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public String setDatastreamVersionable(String pid, String dsID, boolean versionable, String logMessage) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public String compareDatastreamChecksum(String pid, String dsID, String versionDate) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public Datastream getDatastream(String pid, String dsID, String asOfDateTime) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public Datastream[] getDatastreams(String pid, String asOfDateTime, String dsState) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public Datastream[] getDatastreamHistory(String pid, String dsID) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public String[] purgeDatastream(String pid, String dsID, String startDT, String endDT, String logMessage, boolean force) throws RemoteException
         {
             if( pid.startsWith( "test" ) )
             {
                 return new String[] { "timestamp" };
             }
             return new String[] { };
         }
 
         public String[] getNextPID(NonNegativeInteger numPIDs, String pidNamespace) throws RemoteException
         {
             return new String[]{pidNamespace+":1"};
         }
 
         public RelationshipTuple[] getRelationships(String pid, String relationship) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
         }
 
         public boolean addRelationship(String pid, String relationship, String object, boolean isLiteral, String datatype) throws RemoteException
         {
             return true;
         }
 
         public boolean purgeRelationship(String pid, String relationship, String object, boolean isLiteral, String datatype) throws RemoteException
         {
             if( !pid.startsWith( "object:" ) )
             {
                 return false;
             }
             if( !pid.startsWith( "subject:" ) )
             {
                 return false;
             }
             return true;
         }
 
         public Validation validate(String pid, String asOfDateTime) throws RemoteException
         {
             throw new UnsupportedOperationException( "Not supported yet." );
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
         public FedoraAPIM getAPIM() throws ServiceException, IOException
         {
             return apimInstance;
             //return new MockFedoraAPIM();
         }
     }
 
 
     @BeforeClass
     public static void generalSetup()
     {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel( Level.OFF );
 
         HashMap<String, String> m = new HashMap<String, String>();
         m.put( "x", "info:fedora/fedora-system:def/foxml#" );
         SimpleNamespaceContext ctx = new SimpleNamespaceContext( m );
         XMLUnit.setXpathNamespaceContext( ctx );
     }
 
 
     @Before
     public void setUp() throws Exception
     {
         setUpMocks( MockFedoraClient.class );
         instance = new FcrepoModifier( "Host", "Port", "User", "Pass" );
     }
 
 
     @After
     public void tearDown() throws Exception
     {
         tearDownMocks();
     }
 
 
     @Test
     public void testDeleteObject() throws Exception
     {
         final String identifier = "test:1";
         final String logMessage = "logMessage";
 
         instance.deleteObject( identifier, logMessage );
     }
 
     @Test( expected = ObjectRepositoryException.class )
     public void testDeleteObjectException() throws Exception
     {
         String identifier = "xyz";
         String logMessage = "logMessage";
 
         instance.deleteObject( identifier, logMessage );
     }
 
     @Test
     public void testaddObjectRelationOther() throws Exception
     {
         String identifier = "test:1";
         String subject = "test:2";
 
         boolean res = instance.addUncheckedObjectRelation( identifier, "", subject );
         assertTrue( res);
     }
 
     @Test
     public void testaddObjectRelationToSelfFails() throws Exception
     {
         String identifier = "test:1";
 
         boolean res = instance.addUncheckedObjectRelation( identifier, "", identifier );
         // Adding relation to self is not allowed
         assertFalse( res);
     }
 
     @Test
     public void testpurgeObjectRelation( ) throws Exception {
         instance.removeObjectRelation(new PID("object:1"), DBCBIB.IS_MEMBER_OF_WORK , "Subject:1");
     }
 
 
     @Test
     public void testStoreObject() throws Exception
     {
         CargoContainer cargo = new CargoContainer( );
         cargo.setIdentifier( new PID("test:1") );
         cargo.add( DataStreamType.OriginalData, "testFormat", "testSubmitter", "da", "text/xml", " ".getBytes() );
         // static final String dublinCore    = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dc:title xmlns:dc=\"hej\">æøå</dc:title>";
         String dublinCore    = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dc:title xmlns:dc=\"hej\">test:1</dc:title>";
         cargo.add( DataStreamType.DublinCoreData, "testFormat", "testSubmitter", "da", "text/xml", dublinCore.getBytes() );
         String logmessage = "log";
         String expResult = "test:1";
         String result = instance.storeObject( cargo, logmessage, "test");
         assertEquals( expResult, result );
     }
 
     @Test( expected = IllegalStateException.class )
     public void testStoreObjectWithEmptyCargo() throws Exception
     {
         CargoContainer cargo = new CargoContainer();
         // Throws exception on empty cargo object
         String res = instance.storeObject( cargo, "logmessage", "defaultPidNamespace" );
     }
 
     @Test
     public void testStoreObjectWithEmptyIdentifierFails() throws Exception
     {
         CargoContainer cargo = new CargoContainer();
         cargo.add( DataStreamType.OriginalData, "testFormat", "testSubmitter", "da", "text/xml", " ".getBytes() );
         // cargo.setIndexingAlias( "docbook", DataStreamType.OriginalData );
         String logmessage = "log";
         String storeObject = instance.storeObject( cargo, logmessage, "test");
         assertEquals( "test:1", storeObject );
     }
 
     @Test
     public void testPurgeObject() throws Exception
     {
         String identifier = "test:1";
         String logmessage = "log";
 
         instance.purgeObject( identifier, logmessage );
     }
 
 
     @Test( expected = ObjectRepositoryException.class )
     public void testPurgeObjectFailsWithNonExistingObject() throws Exception
     {
         String identifier = "idontexist";
         String logmessage = "log";
 
         instance.purgeObject( identifier, logmessage );
     }
 
     @Test
     public void testPurgeDatastream() throws Exception
     {
         String identifier = "test:1";
         String logmessage = "log";
 
         instance.purgeDatastream( identifier, "RELS-EXT", null, null, logmessage, false );
     }
 
 
 }
