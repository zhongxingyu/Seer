 /******************************************************************
  * File:        TestBugs.java
  * Created by:  Dave Reynolds
  * Created on:  22-Aug-2003
  * 
  * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  * [See end of file]
 * $Id: TestBugs.java,v 1.5 2003-08-22 16:05:11 der Exp $
  *****************************************************************/
 package com.hp.hpl.jena.reasoner.rulesys.test;
 
 import java.io.ByteArrayInputStream;
 
 import com.hp.hpl.jena.ontology.*;
 import com.hp.hpl.jena.ontology.daml.DAMLModel;
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.reasoner.*;
 import com.hp.hpl.jena.util.PrintUtil;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.vocabulary.*;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 //import java.util.*;
 
 /**
  * Unit tests for reported bugs in the rule system.
  * 
  * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.5 $ on $Date: 2003-08-22 16:05:11 $
  */
 public class TestBugs extends TestCase {
 
     /**
      * Boilerplate for junit
      */ 
     public TestBugs( String name ) {
         super( name ); 
     }
     
     /**
      * Boilerplate for junit.
      * This is its own test suite
      */
     public static TestSuite suite() {
         return new TestSuite( TestBugs.class );
 //        TestSuite suite = new TestSuite();
 //        suite.addTest(new TestBugs( "testEquivalentClass1" ));
 //        return suite;
     }  
 
     /**
      * Report of NPE during processing on an ontology with a faulty intersection list,
      * from Hugh Winkler.
     * 
     * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
     * @version $Revision: 1.5 $ on $Date: 2003-08-22 16:05:11 $
      */
     public void testIntersectionNPE() {
         Model base = ModelFactory.createDefaultModel();
         base.read("file:testing/reasoners/bugs/bad-intersection.owl");
         boolean foundBadList = false;
         try {
             InfGraph infgraph = ReasonerRegistry.getOWLReasoner().bind(base.getGraph());
             ExtendedIterator ci = infgraph.find(null, RDF.Nodes.type, OWL.Class.asNode());
             ci.close();
         } catch (ReasonerException e) {
             foundBadList = true;
         }
         assertTrue("Correctly detected the illegal list", foundBadList);
     }
     
     /**
      * Report of problems with cardinality v. maxCardinality usage in classification,
      * from Hugh Winkler.
     * 
     * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
     * @version $Revision: 1.5 $ on $Date: 2003-08-22 16:05:11 $
      */
     public void testCardinality1() {
         Model base = ModelFactory.createDefaultModel();
         base.read("file:testing/reasoners/bugs/cardFPTest.owl");
         InfModel test = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), base);
         String NAMESPACE = "urn:foo#";
         Resource aDocument = test.getResource(NAMESPACE + "aDocument");
         Resource documentType = test.getResource(NAMESPACE + "Document");
         assertTrue("Cardinality-based classification", test.contains(aDocument, RDF.type, documentType));
     }
     
     /**
      * Report of functor literals leaking out of inference graphs and raising CCE
      * in iterators.
      */
     public void testFunctorCCE() {
         Model base = ModelFactory.createDefaultModel();
         base.read("file:testing/reasoners/bugs/cceTest.owl");
         InfModel test = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), base);
 
         boolean b = anyInstancesOfNothing(test);
         ResIterator rIter = test.listSubjects();
         while (rIter.hasNext()) { 
             Resource res = rIter.nextResource();
         }
     }
     
     /** Helper function used in testFunctorCCE */
     private boolean anyInstancesOfNothing(Model model) { 
         boolean hasAny = false;
         try {
             ExtendedIterator it = model.listStatements(null, RDF.type, OWL.Nothing);
             hasAny = it.hasNext();
             it.close();
         } catch (ConversionException x) {
             hasAny = false;
         }
         return hasAny;
     }
     
     /**
      * Report of functor literals leaking out in DAML processing as well.
      */
     public void testDAMLCCE() {
         DAMLModel m = ModelFactory.createDAMLModel();
         m.read( "file:testing/reasoners/bugs/literalLeak.daml", 
                 "http://www.daml.org/2001/03/daml+oil-ex",  null );
         ResIterator rIter = m.listSubjects();
         while (rIter.hasNext()) { 
             Resource res = rIter.nextResource();
             if (res.getNode().isLiteral()) {
                 assertTrue("Error in resource " + res, false);
             }
         }
     }
     
     public static final String INPUT_SUBCLASS =
         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
         "" +
         "<rdf:RDF" + 
         "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +
         "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"" +
         "    xmlns:daml=\"http://www.daml.org/2001/03/daml+oil#\"" +
         "    xmlns:ex=\"http://localhost:8080/axis/daml/a.daml#\"" +
         "    xml:base=\"http://localhost:8080/axis/daml/a.daml\">" +
         " " +
         "    <daml:Ontology rdf:about=\"\">" +
         "        <daml:imports rdf:resource=\"http://www.daml.org/2001/03/daml+oil\"/>" +
         "    </daml:Ontology>" +
         " " +
         "    <daml:Class rdf:ID=\"cls1\"/>" +
         "    <daml:Class rdf:ID=\"cls2\">" +
         "        <daml:subClassOf rdf:resource=\"#cls1\"/>" +
         "    </daml:Class>" +
         "    <ex:cls2 rdf:ID=\"test\"/>" +
         "</rdf:RDF>";
 
     /**
      * This test exposes an apparent problem in the reasoners.  If the input data is 
      * changed from daml:subClassOf to rdfs:subClassOf, the asserts all pass.  As is, 
      * the assert for res has rdf:type cls1 fails.
      */
     public void testSubClass() {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM_RDFS_INF, null);
         
         String base = "http://localhost:8080/axis/daml/a.daml#";
         model.read( new ByteArrayInputStream( INPUT_SUBCLASS.getBytes() ), base );
         OntResource res = (OntResource) model.getResource( base+"test").as(OntResource.class);
             
         OntClass cls1 = (OntClass) model.getResource(base+"cls1").as(OntClass.class);
         OntClass cls2 = (OntClass) model.getResource(base+"cls2").as(OntClass.class);
         
         assertTrue( "cls2 should be a super-class of cls1", cls2.hasSuperClass( cls1 ) );
         assertTrue( "res should have rdf:type cls1", res.hasRDFType( cls1 ) );
         assertTrue( "res should have rdf:type cls2", res.hasRDFType( cls2 ) );
     }
 
     public static final String INPUT_SUBPROPERTY =
         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
         "" +
         "<rdf:RDF" + 
         "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +
         "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"" +
         "    xmlns:daml=\"http://www.daml.org/2001/03/daml+oil#\"" +
         "    xmlns=\"urn:x-hp-jena:test#\"" +
         "    xml:base=\"urn:x-hp-jena:test\">" +
         " " +
         "    <daml:Ontology rdf:about=\"\">" +
         "        <daml:imports rdf:resource=\"http://www.daml.org/2001/03/daml+oil\"/>" +
         "    </daml:Ontology>" +
         " " +
         "    <daml:Class rdf:ID=\"A\"/>" +
         "" +
         "    <daml:ObjectProperty rdf:ID=\"p\" />" +
         "    <daml:ObjectProperty rdf:ID=\"q\">" +
         "        <daml:subPropertyOf rdf:resource=\"#p\"/>" +
         "    </daml:ObjectProperty>" +
         "" +
         "    <A rdf:ID=\"a0\"/>" +
         "    <A rdf:ID=\"a1\">" +
         "       <q rdf:resource=\"#a0\" />" +
         "    </A>" +
         "</rdf:RDF>";
 
     /**
      * This test exposes an apparent problem in the reasoners.  If the input data is 
      * changed from daml:subPropertyOf to rdfs:subPropertyOf, the asserts all pass.  As is, 
      * the assert for a1 p a0 fails.
      */
     public void testSubProperty() {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM_RDFS_INF, null);
         
         String base = "urn:x-hp-jena:test#";
         model.read( new ByteArrayInputStream( INPUT_SUBPROPERTY.getBytes() ), base );
         
         OntResource a0 = (OntResource) model.getResource( base+"a0").as(OntResource.class);
         OntResource a1 = (OntResource) model.getResource( base+"a1").as(OntResource.class);
             
         ObjectProperty p = model.getObjectProperty( base+"p" );
         ObjectProperty q = model.getObjectProperty( base+"q" );
         
         assertTrue("subProp relation present", q.hasProperty(RDFS.subPropertyOf, p));
         assertTrue( "a1 q a0", a1.hasProperty( q, a0 ) );   // asserted
         assertTrue( "a1 p a0", a1.hasProperty( p, a0 ) );   // entailed
     }
 
     /**
      * Test  problems with inferring equivalence of some simple class definitions,
      * reported by Jeffrey Hau.
      */
     public void testEquivalentClass1() {
         Model base = ModelFactory.createDefaultModel();
         base.read("file:testing/reasoners/bugs/equivalentClassTest.owl");
         InfModel test = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), base);
         String NAMESPACE = "urn:foo#";
         Resource A = test.getResource(NAMESPACE + "A");
         Resource B = test.getResource(NAMESPACE + "B");
         assertTrue("hasValue equiv deduction", test.contains(A, OWL.equivalentClass, B));
     }
 
     // debug assistant
     private void tempList(Model m, Resource s, Property p, RDFNode o) {
         System.out.println("Listing of " + PrintUtil.print(s) + " " + PrintUtil.print(p) + " " + PrintUtil.print(o));
         for (StmtIterator i = m.listStatements(s, p, o); i.hasNext(); ) {
             System.out.println(" - " + i.next());
         }
     }    
 }
 
 /*
     (c) Copyright Hewlett-Packard Company 2003
     All rights reserved.
 
     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions
     are met:
 
     1. Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
 
     2. Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
 
     3. The name of the author may not be used to endorse or promote products
        derived from this software without specific prior written permission.
 
     THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
     IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
     IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
     INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
     NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
     THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
