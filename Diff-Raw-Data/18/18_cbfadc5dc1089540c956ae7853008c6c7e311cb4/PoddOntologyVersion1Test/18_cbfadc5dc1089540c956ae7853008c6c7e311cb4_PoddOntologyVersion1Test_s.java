 /**
  * 
  */
 package com.github.podd.ontology.test;
 
 import java.util.Arrays;
 import java.util.Collection;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 /**
  * Parameterized test to validate PODD ontologies.
  * 
  * @author kutila
  * 
  */
 @RunWith(value = Parameterized.class)
 public class PoddOntologyVersion1Test extends AbstractPoddOntologyVersionTest
 {
     public static final String VERSION_1_PATH = "/ontologies/version/1/";
     @Parameters(name = "{0}")
     public static Collection<Object[]> data()
     {
         final Object[][] data =
                 new Object[][] {
                         // schema ontologies
                         { PoddOntologyVersion1Test.VERSION_1_PATH + "dcTerms.owl", "application/rdf+xml", 47 },
                         { PoddOntologyVersion1Test.VERSION_1_PATH + "foaf.owl", "application/rdf+xml", 38 },
                         { PoddOntologyVersion1Test.VERSION_1_PATH + "poddUser.owl", "application/rdf+xml", 188 },
                         { PoddOntologyVersion1Test.VERSION_1_PATH + "poddBase.owl", "application/rdf+xml", 331 },
                         { PoddOntologyVersion1Test.VERSION_1_PATH + "poddScience.owl", "application/rdf+xml", 1449 },
                         { PoddOntologyVersion1Test.VERSION_1_PATH + "poddPlant.owl", "application/rdf+xml", 217 },
                         { PoddOntologyVersion1Test.VERSION_1_PATH + "poddAnimal.owl", "application/rdf+xml", 139 },
                         { PoddOntologyVersion1Test.VERSION_1_PATH + "poddDataRepository.owl", "application/rdf+xml", 47 },
                        { "/ontologies/crop-ontology-715.owl", "application/rdf+xml", 2173 },
                 
                 // artifacts to test
                 // { "/test/artifacts/basic-1.rdf", "application/rdf+xml", 26 },
                 // { "/test/artifacts/basic-2.rdf", "application/rdf+xml", 97 },
                 // { "/test/artifacts/basic-2-internal-objects.rdf", "application/rdf+xml", 29 },
                 // { "/test/artifacts/basicProject-1-internal-object.rdf", "application/rdf+xml", 26
                 // },
                 
                 // { "/test/artifacts/basic-1.ttl", "text/turtle", 32 },
                 // { "/test/artifacts/basic-2.ttl", "text/turtle", 97 },
                 // { "/test/artifacts/3-topobjects.ttl", "text/turtle", 34 },
                 
                 // { "/ontologies/dcTermsInferred.rdf", "application/rdf+xml", 16 },
                 // { "/ontologies/foafInferred.rdf", "application/rdf+xml", 37 },
                 // { "/ontologies/poddUserInferred.rdf", "application/rdf+xml", 87 },
                 // { "/ontologies/poddBaseInferred.rdf", "application/rdf+xml", 183 },
                 // { "/ontologies/poddScienceInferred.rdf", "application/rdf+xml", 472 },
                 // { "/ontologies/poddPlantInferred.rdf", "application/rdf+xml", 495 },
                 
                 // Inconsistent Ontology
                 // { "/test/artifacts/bad-twoLeadInstitutions.rdf", "application/rdf+xml", 22 },
                 };
         return Arrays.asList(data);
     }
     
     /**
      * Parameterized constructor
      * 
      * @param number
      */
     public PoddOntologyVersion1Test(final String filename, final String mimeType, final int statementCount)
     {
         super(filename, mimeType, statementCount);
     }
 }
