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
 
 import dk.dbc.opensearch.common.fedora.FedoraNamespaceContext.FedoraNamespace;
 import dk.dbc.opensearch.common.types.OpenSearchTransformException;
 import java.io.ByteArrayOutputStream;
 import java.util.HashMap;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.ParserConfigurationException;
 import org.junit.BeforeClass;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.custommonkey.xmlunit.SimpleNamespaceContext;
 import org.custommonkey.xmlunit.XMLUnit;
 
/**
 * \todo: bug 9758, three test ignoret
 */

 
 public class FedoraRelsExtTest
 {
 
     FedoraRelsExt instance;
     static final String pid = "test:1";
     static final String coll_pid = "work:1";
     static final String expected_greenfield = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:fedora=\"info:fedora/fedora-system:def/relations-external#\" xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"><rdf:Description rdf:about=\"info:fedora/" + pid + "\"></rdf:Description></rdf:RDF>";
     static final String expected_relsext = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:fedora=\"info:fedora/fedora-system:def/relations-external#\" xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"><rdf:Description rdf:about=\"info:fedora/" + pid + "\"><fedora:isMemberOfCollection>" + coll_pid + "</fedora:isMemberOfCollection></rdf:Description></rdf:RDF>";
 
        
     @Before
     public void Setup() throws ParserConfigurationException
     {
         //we're asserting stuff about a document with the pid "test:1"
         instance = new FedoraRelsExt( "test:1" );
 
     }
 
 
     @BeforeClass
     public static void SetupClass()
     {
         HashMap<String, String> m = new HashMap<String, String>();
         m.put( "x", "info:fedora/fedora-system:def/foxml#" );
         SimpleNamespaceContext ctx = new SimpleNamespaceContext( m );
         XMLUnit.setIgnoreAttributeOrder( true );
         XMLUnit.setXpathNamespaceContext( ctx );
 
     }
 
 
     /**
      * Conformance test that verifies the minimal expectancies of a RELS-EXT stream
      */
    @Ignore
     @Test
     public void testRelsExtXML() throws OpenSearchTransformException
     {
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         instance.serialize( baos, null );
 
         String xml = new String( baos.toByteArray() );
 
         assertEquals( expected_greenfield, xml );
     }
 
 
     /**
      * Verifies the expectancy that we're able to add a relation to the rels-ext
      * stream
      */
    @Ignore
     @Test
     public void testAddRelation() throws OpenSearchTransformException
     {
         // lets try adding the triple "test:1 isMemberOfCollection work:1"
         instance.addRelationship( new QName( FedoraNamespace.FEDORARELSEXT.getURI(),
                 "isMemberOfCollection",
                 FedoraNamespace.FEDORA.getPrefix() ),
                 new QName( "",
                 "1",
                 FedoraNamespace.WORK.getPrefix() ) );
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         instance.serialize( baos, null );
 
         String xml = new String( baos.toByteArray() );
         assertEquals( expected_relsext, xml );
 
     }
 
 
     /**
      * Adding multiple relations is disallowed, and the second addition leaves
      * the object unchanged.
      */
    @Ignore
     @Test
     public void testIdenticalRelationsDisallowed() throws OpenSearchTransformException
     {
         QName pred = new QName( FedoraNamespace.FEDORARELSEXT.getURI(),
                 "isMemberOfCollection",
                 FedoraNamespace.FEDORA.getPrefix() );
         QName obj = new QName( "", "1", FedoraNamespace.WORK.getPrefix() );
 
         boolean allowed;
 
         // lets try adding the triple "test:1 isMemberOfCollection work:1"
         allowed = instance.addRelationship( pred, obj );
         assertTrue( allowed );
 
         // and let's add it again
         allowed = instance.addRelationship( pred, obj );
         assertFalse( allowed );
 
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         instance.serialize( baos, null );
 
         String xml = new String( baos.toByteArray() );
 
         // and thus we'll have the same relsext as we would if we had tried the
         // addition only once.
         assertEquals( expected_relsext, xml );
 
     }
 
 
 }
