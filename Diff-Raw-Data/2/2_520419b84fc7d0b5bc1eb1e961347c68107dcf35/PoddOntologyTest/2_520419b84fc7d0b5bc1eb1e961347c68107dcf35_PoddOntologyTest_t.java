 /**
  * 
  */
 package com.github.podd.ontology.test;
 
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParser;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
 import org.semanticweb.owlapi.formats.RioRDFOntologyFormatFactory;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
 import org.semanticweb.owlapi.profiles.OWLProfile;
 import org.semanticweb.owlapi.profiles.OWLProfileRegistry;
 import org.semanticweb.owlapi.profiles.OWLProfileReport;
 import org.semanticweb.owlapi.profiles.OWLProfileViolation;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
 import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
 import org.semanticweb.owlapi.rio.RioParserImpl;
 
 /**
  * Parameterized test to validate PODD ontologies.
  * 
  * @author kutila
  * 
  */
 @RunWith(value = Parameterized.class)
 public class PoddOntologyTest
 {
     /**
      * NOTE: The static OWLOntologyManager instance is reused through all the tests so that loaded
      * ontologies are kept in memory and able to satisfy import requirements in later ontologies.
      * 
      */
     private static OWLOntologyManager owlOntologyManager;
     
     // ----------- parameters for junit test----------------
     private String filename;
     private String mimeType;
     private int statementCount;
     
     @BeforeClass
     public static void beforeClass() throws Exception
     {
         PoddOntologyTest.owlOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
     }
     
     /**
      * Parameterized constructor
      * 
      * @param number
      */
     public PoddOntologyTest(final String filename, final String mimeType, final int statementCount)
     {
         this.filename = filename;
         this.mimeType = mimeType;
         this.statementCount = statementCount;
     }
     
     @Parameters
     public static Collection<Object[]> data()
     {
         final Object[][] data =
                 new Object[][] {
                         { "/ontologies/dcTerms.owl", "application/rdf+xml", 39 },
                         { "/ontologies/foaf.owl", "application/rdf+xml", 38 },
                        { "/ontologies/poddUser.owl", "application/rdf+xml", 188 },
                         { "/ontologies/poddBase.owl", "application/rdf+xml", 282 },
                         { "/ontologies/poddScience.owl", "application/rdf+xml", 1125 },
                         { "/ontologies/poddPlant.owl", "application/rdf+xml", 77 },
                         { "/ontologies/poddAnimal.owl", "application/rdf+xml", 171 }, 
                         };
         return Arrays.asList(data);
     }
     
     /**
      * Test the ontology can be parsed into RDF and contains the expected number of statements.
      */
     @Test
     public void testForValidRDF() throws Exception
     {
         final InputStream inputStream = this.getClass().getResourceAsStream(this.filename);
         Assert.assertNotNull("Null resource " + this.filename, inputStream);
         
         final RDFFormat format = RDFFormat.forMIMEType(this.mimeType);
         final RDFParser rdfParser = Rio.createParser(format);
         final StatementCollector collector = new StatementCollector();
         rdfParser.setRDFHandler(collector);
         
         rdfParser.parse(inputStream, "http://purl.org/podd/ns/XYZ");
         
         Assert.assertEquals("Incorrect number of statements for " + this.filename, this.statementCount, collector
                 .getStatements().size());
     }
     
     /**
      * Test the ontology is in OWL-DL profile.
      */
     @Test
     public void testForValidOWLDLOntology() throws Exception
     {
         final InputStream inputStream = this.getClass().getResourceAsStream(this.filename);
         Assert.assertNotNull("Null resource " + this.filename, inputStream);
         
         final RDFFormat format = RDFFormat.forMIMEType(this.mimeType);
         final RDFParser rdfParser = Rio.createParser(format);
         final StatementCollector collector = new StatementCollector();
         rdfParser.setRDFHandler(collector);
         
         rdfParser.parse(inputStream, "http://purl.org/podd/ns/XYZ");
         
         Assert.assertEquals("Incorrect number of statements for " + this.filename, this.statementCount, collector
                 .getStatements().size());
         
         // - proceed to OWL ontology validation
         
         final RioRDFOntologyFormatFactory ontologyFormatFactory =
                 (RioRDFOntologyFormatFactory)OWLOntologyFormatFactoryRegistry.getInstance()
                         .getByMIMEType(this.mimeType);
         final RioParserImpl owlParser = new RioParserImpl(ontologyFormatFactory);
         
         final OWLOntology nextOntology = PoddOntologyTest.owlOntologyManager.createOntology();
         final RioMemoryTripleSource owlSource = new RioMemoryTripleSource(collector.getStatements().iterator());
         
         owlParser.parse(owlSource, nextOntology);
         
         // verify: ontology is not empty
         Assert.assertFalse("Loaded ontology was empty", nextOntology.isEmpty());
         
         // hard-coded since all our ontologies are expected to be in OWL-DL
         final OWLProfile nextProfile = OWLProfileRegistry.getInstance().getProfile(OWLProfile.OWL2_DL);
         
         // verify: check ontology in profile
         final OWLProfileReport profileReport = nextProfile.checkOntology(nextOntology);
         if (!profileReport.isInProfile())
         {
             for (OWLProfileViolation v : profileReport.getViolations())
             {
                 System.out.println(" Profile violation = " + v.toString());
             }
             Assert.fail("Ontology not in profile");
         }
 
         // verify: check consistency
         final OWLReasonerFactory reasonerFactory =
                 OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
         final OWLReasoner reasoner = reasonerFactory.createReasoner(nextOntology);
         
         Assert.assertTrue("Ontology is not consistent", reasoner.isConsistent());
     }
     
 }
