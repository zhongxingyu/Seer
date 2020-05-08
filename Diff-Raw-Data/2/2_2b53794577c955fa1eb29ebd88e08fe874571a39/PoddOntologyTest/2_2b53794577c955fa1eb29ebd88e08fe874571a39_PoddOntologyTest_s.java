 /**
  * 
  */
 package com.github.podd.ontology.test;
 
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Set;
 
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParser;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
 import org.semanticweb.owlapi.formats.RioRDFOntologyFormatFactory;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
 import org.semanticweb.owlapi.profiles.OWLProfile;
 import org.semanticweb.owlapi.profiles.OWLProfileRegistry;
 import org.semanticweb.owlapi.profiles.OWLProfileReport;
 import org.semanticweb.owlapi.profiles.OWLProfileViolation;
 import org.semanticweb.owlapi.reasoner.Node;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
 import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
 import org.semanticweb.owlapi.rio.RioParserImpl;
 
 import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
 import com.clarkparsia.owlapi.explanation.ExplanationGenerator;
 import com.clarkparsia.owlapi.explanation.io.ExplanationRenderer;
 import com.clarkparsia.owlapi.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;
 import com.clarkparsia.owlapi.explanation.util.ExplanationProgressMonitor;
 
 /**
  * Parameterized test to validate PODD ontologies.
  * 
  * @author kutila
  * 
  */
 @RunWith(value = Parameterized.class)
 public class PoddOntologyTest
 {
     public static final String VERSION_1_PATH = "/ontologies/version/1/";
     /**
      * NOTE: The static OWLOntologyManager instance is reused through all the tests so that loaded
      * ontologies are kept in memory and able to satisfy import requirements in later ontologies.
      * 
      */
     private static OWLOntologyManager owlOntologyManager;
     
     @BeforeClass
     public static void beforeClass() throws Exception
     {
         PoddOntologyTest.owlOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
     }
     
     @Parameters(name="{0}")
     public static Collection<Object[]> data()
     {
         final Object[][] data =
                 new Object[][] {
                         // schema ontologies
                        { VERSION_1_PATH + "dcTerms.owl", "application/rdf+xml", 43 },
                         { VERSION_1_PATH + "foaf.owl", "application/rdf+xml", 38 },
                         { VERSION_1_PATH + "poddUser.owl", "application/rdf+xml", 188 },
                         { VERSION_1_PATH + "poddBase.owl", "application/rdf+xml", 343 },
                         { VERSION_1_PATH + "poddScience.owl", "application/rdf+xml", 1298 },
                         { VERSION_1_PATH + "poddPlant.owl", "application/rdf+xml", 214 },
                         { VERSION_1_PATH + "poddAnimal.owl", "application/rdf+xml", 139 },
                         { VERSION_1_PATH + "poddDataRepository.owl", "application/rdf+xml", 47 },
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
     
     // ----------- parameters for junit test----------------
     private String filename;
     
     private String mimeType;
     
     private int statementCount;
     
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
         final StatementCollector collector = new StatementCollector(new LinkedHashModel());
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
         if(!profileReport.isInProfile())
         {
             for(final OWLProfileViolation v : profileReport.getViolations())
             {
                 System.out.println(" Profile violation = " + v.toString());
             }
             Assert.fail("Ontology not in profile");
         }
         
         // verify: check consistency
         final OWLReasonerFactory reasonerFactory =
                 OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
         final OWLReasoner reasoner = reasonerFactory.createReasoner(nextOntology);
         
         // Check for any inconsistent classes and if so render them out as explanations
         final Node<OWLClass> unsatisfiableClasses = reasoner.getUnsatisfiableClasses();
         final Set<OWLClass> entitiesMinusBottom = unsatisfiableClasses.getEntitiesMinusBottom();
         if(!entitiesMinusBottom.isEmpty())
         {
             final ExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
             final PrintWriter out = new PrintWriter(System.out);
             
             renderer.startRendering(out);
             for(final OWLClass nextUnsatisfiableClass : entitiesMinusBottom)
             {
                 final ExplanationGenerator explanator =
                         new DefaultExplanationGenerator(PoddOntologyTest.owlOntologyManager, reasonerFactory,
                                 nextOntology, (ExplanationProgressMonitor)null);
                 final Set<Set<OWLAxiom>> explanations = explanator.getExplanations(nextUnsatisfiableClass);
                 out.println("next unsatisfiable class: " + nextUnsatisfiableClass.getIRI());
                 renderer.render((OWLAxiom)null, explanations);
             }
             renderer.endRendering();
         }
         
         Assert.assertTrue("Ontology is not consistent", reasoner.isConsistent());
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
         final StatementCollector collector = new StatementCollector(new LinkedHashModel());
         rdfParser.setRDFHandler(collector);
         
         rdfParser.parse(inputStream, "http://purl.org/podd/ns/XYZ");
         
         Assert.assertEquals("Incorrect number of statements for " + this.filename, this.statementCount, collector
                 .getStatements().size());
     }
 }
