 package com.computas.sublima.query.service;
 
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import junit.framework.TestCase;
 import org.z3950.zing.cql.CQLParseException;
 
 import java.io.IOException;
 
 import com.computas.sublima.query.exceptions.UnsupportedCQLFeatureException;
 
 /**
  * CQL2SPARQL Tester.
  *
  * @author <Authors name>
  * @since <pre>08/05/2008</pre>
  * @version 1.0
  */
 public class CQL2SPARQLTest extends TestCase {
     private String expectedPrefix;
     private String expectedSuffix;
 
     public CQL2SPARQLTest(String name) {
         super(name);
     }
 
     public void setUp() throws Exception {
         expectedPrefix = "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>\n" +
                "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                 "DESCRIBE ?resource ?rest WHERE {\n" +
                 "?lit pf:textMatch '";
        expectedSuffix = "' .\n?resource sub:literals ?lit .\n" +
                 "?resource ?p ?rest .\n" +
                 "}";
         super.setUp();
     }
 
     public void tearDown() throws Exception {
         super.tearDown();
     }
 
     public static Test suite() {
         return new TestSuite(CQL2SPARQLTest.class);
     }
 
     public void testTerm() throws CQLParseException, IOException, UnsupportedCQLFeatureException {
         CQL2SPARQL translator = new CQL2SPARQL("vondt");
         assertEquals("Query not as expected:", expectedPrefix + "vondt*" + expectedSuffix, translator.Level0());
     }
 
     public void testTermThreeWords() throws CQLParseException, IOException, UnsupportedCQLFeatureException {
          CQL2SPARQL translator = new CQL2SPARQL("\"vondt i magen\"");
         assertEquals("Query not as expected:", expectedPrefix + "vondt* AND i* AND magen*" + expectedSuffix, translator.Level0());
      }
     public void testTermThreeWordsQuoted() throws CQLParseException, IOException, UnsupportedCQLFeatureException {
           CQL2SPARQL translator = new CQL2SPARQL("\"vondt \\\"i magen\\\"\"");
          assertEquals("Query not as expected:", expectedPrefix + "vondt* AND \"i magen\"" + expectedSuffix, translator.Level0());
       }
 
      public void testLevel0IndexAndTerm() throws CQLParseException, IOException {
         CQL2SPARQL translator = new CQL2SPARQL("dc.title=vondt");
          try {
              String out = translator.Level0();
 		 }	catch (UnsupportedCQLFeatureException tmp) {
 			 assertTrue(true);
 		 }
     }
 
     public void testLevel0RelationAndTerm() throws CQLParseException, IOException {
        CQL2SPARQL translator = new CQL2SPARQL("dc.description any vondt");
         try {
             String out = translator.Level0();
         }	catch (UnsupportedCQLFeatureException tmp) {
             assertTrue(true);
         }
     }
 
     public void testLevel0Complex() throws CQLParseException, IOException {
        CQL2SPARQL translator = new CQL2SPARQL("dc.title any vondt sortBy dc.subject/sort.ascending");
         try {
             String out = translator.Level0();
         }	catch (UnsupportedCQLFeatureException tmp) {
             assertTrue(true);
         }
    }
 
     public void testUnparseable() throws UnsupportedCQLFeatureException, IOException {
         try {
             CQL2SPARQL translator = new CQL2SPARQL("any vondt");
             String out = translator.Level0();
         }	catch (CQLParseException tmp) {
             assertTrue(true);
         }
     }
 
 
 }
