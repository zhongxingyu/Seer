 /**
  * 
  */
 package com.github.podd.api.test;
 
 import java.io.InputStream;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openrdf.model.Graph;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.GraphImpl;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.query.GraphQuery;
 import org.openrdf.query.GraphQueryResult;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.impl.DatasetImpl;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
 import org.semanticweb.owlapi.formats.RioRDFOntologyFormatFactory;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.profiles.OWLProfileReport;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
 import org.semanticweb.owlapi.rio.RioParser;
 import org.semanticweb.owlapi.rio.RioParserImpl;
 
 import com.github.podd.api.PoddArtifactManager;
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.api.PoddProcessorStage;
 import com.github.podd.api.PoddSchemaManager;
 import com.github.podd.api.file.PoddFileReference;
 import com.github.podd.api.file.PoddFileReferenceManager;
 import com.github.podd.api.file.PoddFileReferenceProcessor;
 import com.github.podd.api.file.PoddFileReferenceProcessorFactory;
 import com.github.podd.api.file.PoddFileReferenceProcessorFactoryRegistry;
 import com.github.podd.api.purl.PoddPurlManager;
 import com.github.podd.api.purl.PoddPurlProcessor;
 import com.github.podd.api.purl.PoddPurlProcessorFactory;
 import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
 import com.github.podd.api.purl.PoddPurlReference;
 import com.github.podd.utils.InferredOWLOntologyID;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public abstract class AbstractPoddArtifactManagerTest
 {
     
     protected abstract PoddArtifactManager getNewArtifactManager();
     
     protected abstract PoddPurlProcessorFactory getNewDoiPurlProcessorFactory();
     
     protected abstract PoddFileReferenceManager getNewFileReferenceManager();
     
     protected abstract PoddPurlProcessorFactory getNewHandlePurlProcessorFactory();
     
     protected abstract PoddFileReferenceProcessorFactory getNewHttpFileReferenceProcessorFactory();
     
     protected abstract PoddOWLManager getNewOWLManager();
     
     protected abstract PoddPurlManager getNewPurlManager();
     
     protected abstract OWLReasonerFactory getNewReasonerFactory();
 
     protected abstract PoddSchemaManager getNewSchemaManager();
     
     protected abstract PoddFileReferenceProcessorFactory getNewSSHFileReferenceProcessorFactory();
     
     protected abstract PoddPurlProcessorFactory getNewUUIDPurlProcessorFactory();
     
     /**
      * @throws java.lang.Exception
      */
     @SuppressWarnings("null")
     @Before
     public void setUp() throws Exception
     {
         // FIXME: This needs to be a constant
         final URI poddFileReferenceType =
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase#PoddFileReference");
         
         final PoddFileReferenceProcessorFactoryRegistry testFileRegistry =
                 new PoddFileReferenceProcessorFactoryRegistry();
         // clear any automatically added entries that may come from META-INF/services entries on the
         // classpath
         testFileRegistry.clear();
         // In practice, the following factories would be automatically added to the registry,
         // however for testing we want to explicitly add the ones we want to support for each test
         testFileRegistry.add(this.getNewSSHFileReferenceProcessorFactory());
         testFileRegistry.add(this.getNewHttpFileReferenceProcessorFactory());
         
         final PoddPurlProcessorFactoryRegistry testPurlRegistry = new PoddPurlProcessorFactoryRegistry();
         testPurlRegistry.clear();
         testPurlRegistry.add(this.getNewDoiPurlProcessorFactory());
         testPurlRegistry.add(this.getNewHandlePurlProcessorFactory());
         testPurlRegistry.add(this.getNewUUIDPurlProcessorFactory());
         
         final PoddFileReferenceManager testFileReferenceManager = this.getNewFileReferenceManager();
         testFileReferenceManager.setProcessorFactoryRegistry(testFileRegistry);
         
         final PoddPurlManager testPurlManager = this.getNewPurlManager();
         testPurlManager.setPurlProcessorFactoryRegistry(testPurlRegistry);
         
         final PoddOWLManager testOWLManager = this.getNewOWLManager();
         testOWLManager.setReasonerFactory(this.getNewReasonerFactory());
         
         final PoddArtifactManager testArtifactManager = this.getNewArtifactManager();
         testArtifactManager.setFileReferenceManager(testFileReferenceManager);
         testArtifactManager.setPurlManager(testPurlManager);
         testArtifactManager.setOwlManager(testOWLManager);
         
         final PoddSchemaManager testSchemaManager = this.getNewSchemaManager();
         testSchemaManager.setOwlManager(testOWLManager);
         
         final InputStream inputStream = this.getClass().getResourceAsStream("/testArtifact.rdf");
         // MIME type should be either given by the user, detected from the content type on the
         // request, or autodetected using the Any23 Mime Detector
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         final InferredOWLOntologyID resultArtifactId = testArtifactManager.loadArtifact(inputStream, format);
         
         // INSIDE the loadArtifact method...
         
         // connection to the temporary repository that the artifact RDF triples will be stored while
         // they are initially parsed by OWLAPI.
         final RepositoryConnection temporaryRepositoryConnection = null;
         
         final URI randomContext = ValueFactoryImpl.getInstance().createURI(UUID.randomUUID().toString());
         
         // Load the artifact RDF triples into a random context in the temp repository, which may be
         // shared between different uploads
         temporaryRepositoryConnection.add(inputStream, "", format, randomContext);
         
         // TODO: SPARQL query to extract the Ontology IRI and Version IRI from the
         // temporaryRepositoryConnection
         
         // Create an initial OWLOntologyID for the uploaded ontology, after checking that the
         // Version IRI is distinct for the given Ontology IRI
         final OWLOntologyID tempArtifactId = null;
         
         // TODO If the OWLOntologyID is not distinct, then modify it to be distinct
         
         // return the results, setting the results variable to be the same as the internalResults
         // variable from inside of extractFileReferences
         // Ie, return internalResults; results = internalResults;
         
         final Set<PoddPurlReference> purlResults =
                 testArtifactManager.getPurlManager().extractPurlReferences(temporaryRepositoryConnection,
                         tempArtifactId.getVersionIRI().toOpenRDFURI());
         
         // perform the conversion of the URIs, possibly in bulk, as they are all given to this
         // method together
         testArtifactManager.getPurlManager().convertTemporaryUris(purlResults, temporaryRepositoryConnection,
                 tempArtifactId.getVersionIRI().toOpenRDFURI());
         
         // Then work on the file references
         
         // calls, to setup the results collection
         final Set<PoddFileReference> fileReferenceResults =
                 testArtifactManager.getFileReferenceManager().extractFileReferences(temporaryRepositoryConnection,
                         tempArtifactId.getVersionIRI().toOpenRDFURI());
         
         // INSIDE the extractFileReferences method....
         final Set<PoddFileReference> internalFileReferenceResults =
                 Collections.newSetFromMap(new ConcurrentHashMap<PoddFileReference, Boolean>());
         
         for(final PoddFileReferenceProcessorFactory nextProcessorFactory : testArtifactManager
                .getFileReferenceManager().getFileProcessorFactoryRegistry().getByStage(PoddProcessorStage.RDF_PARSING))
         {
             final StringBuilder sparqlQuery = new StringBuilder();
             sparqlQuery.append("CONSTRUCT { ");
             sparqlQuery.append(nextProcessorFactory.getSPARQLConstructBGP());
             sparqlQuery.append(" } WHERE { ");
             sparqlQuery.append(nextProcessorFactory.getSPARQLConstructWhere());
             sparqlQuery.append(" } ");
             if(!nextProcessorFactory.getSPARQLGroupBy().isEmpty())
             {
                 sparqlQuery.append(" GROUP BY ");
                 sparqlQuery.append(nextProcessorFactory.getSPARQLGroupBy());
             }
             
             final GraphQuery graphQuery =
                     temporaryRepositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery.toString());
             
             // Create a new dataset to specify the contexts that the query will be allowed to access
             final DatasetImpl dataset = new DatasetImpl();
             // The following URIs are passed in as the context to the
             // extractFileReferences(RepositoryConnection,URI...) method
             final URI artifactGraphUri = null;
             
             // for(URI artifactGraphUri : contexts)
             dataset.addDefaultGraph(artifactGraphUri);
             dataset.addNamedGraph(artifactGraphUri);
             // end for loop
             
             // if the stage is after inferencing, the inferred graph URI would be one of the
             // contexts as well
             final URI artifactInferredGraphUri = null;
             dataset.addDefaultGraph(artifactInferredGraphUri);
             dataset.addNamedGraph(artifactInferredGraphUri);
             
             // set the dataset for the query to be our artificially constructed dataset
             graphQuery.setDataset(dataset);
             
             final GraphQueryResult queryResult = graphQuery.evaluate();
             
             // If the query matched anything, then for each of the file references in the resulting
             // construct statements, we create a file reference and add it to the results
             if(!queryResult.hasNext())
             {
                 final Graph graph = new GraphImpl();
                 while(queryResult.hasNext())
                 {
                     graph.add(queryResult.next());
                 }
                 
                 final Iterator<Statement> match = graph.match(null, RDF.TYPE, poddFileReferenceType);
                 
                 while(match.hasNext())
                 {
                     final Statement nextStatement = match.next();
                     
                     final Graph fileReferenceGraph = new GraphImpl();
                     
                     // For generality, in practice for now, use graph.match as below...
                     nextProcessorFactory.getSPARQLConstructWhere((URI)nextStatement.getSubject());
                     
                     final Iterator<Statement> match2 = graph.match(nextStatement.getSubject(), null, null);
                     
                     while(match2.hasNext())
                     {
                         fileReferenceGraph.add(match2.next());
                     }
                     
                     // This processor factory matches the graph that we wish to use, so we create a
                     // processor instance now to create the file reference
                     // NOTE: This object cannot be shared as we do not specify that it needs to be
                     // threadsafe
                     final PoddFileReferenceProcessor processor = nextProcessorFactory.getProcessor();
                     
                     // create a reference out of the resulting graph
                     internalFileReferenceResults.add(processor.createReference(fileReferenceGraph));
                 }
             }
         }
         
         // optionally verify the file references
         testArtifactManager.getFileReferenceManager().verifyFileReferences(fileReferenceResults,
                 temporaryRepositoryConnection, tempArtifactId.getVersionIRI().toOpenRDFURI());
         
         // TODO: Optionally remove invalid file references or mark them as invalid using RDF
         // statements/OWL Classes
         
         // Before loading the statements into OWLAPI, ensure that the schema ontologies are cached
         // in memory
         
         // TODO: For each OWL:IMPORTS statement, call the following
         final IRI schemaOntologyIRI = null;
         // Get the current version
         final InferredOWLOntologyID ontologyVersion =
                 testSchemaManager.getCurrentSchemaOntologyVersion(schemaOntologyIRI);
         // Make sure it is cached in memory. This will not attempt to load the ontology again if it
         // is already cached or already being loaded
         testArtifactManager.getOWLManager().cacheSchemaOntology(ontologyVersion, temporaryRepositoryConnection);
         
         // Load the statements into an OWLAPI OWLOntology
         final RioMemoryTripleSource owlSource =
                 new RioMemoryTripleSource(temporaryRepositoryConnection.getStatements(null, null, null, true,
                         tempArtifactId.getVersionIRI().toOpenRDFURI()));
         owlSource.setNamespaces(temporaryRepositoryConnection.getNamespaces());
         
         final OWLOntology nextOntology = testArtifactManager.getOWLManager().loadOntology(owlSource);
         
         // Inside of PoddOWLManager.loadOntology().....
         final RioParser owlParser =
                 new RioParserImpl((RioRDFOntologyFormatFactory)OWLOntologyFormatFactoryRegistry.getInstance()
                         .getByMIMEType(mimeType));
         // nextOntology = this.manager.createOntology();
         owlParser.parse(owlSource, nextOntology);
         
         // Check the OWLAPI OWLOntology against an OWLProfile to make sure it is in profile
         final OWLProfileReport profileReport =
                 testArtifactManager.getOWLManager().getReasonerProfile().checkOntology(nextOntology);
         if(!profileReport.isInProfile())
         {
             testArtifactManager.getOWLManager().removeCache(nextOntology.getOntologyID());
         }
         
         // create an OWL Reasoner using the Pellet library and ensure that the reasoner thinks the
         // ontology is consistent so far
         // Use the factory that we found to create a reasoner over the ontology
         final OWLReasoner nextReasoner = testArtifactManager.getOWLManager().createReasoner(nextOntology);
         
         // Test that the ontology was consistent with this reasoner
         // This ensures in the case of Pellet that it is in the OWL2-DL profile
         // if(!nextReasoner.isConsistent() || nextReasoner.getUnsatisfiableClasses().getSize() > 0)
         // Check the OWLAPI OWLOntology using a reasoner for .isConsistent
         if(!nextReasoner.isConsistent())
         {
             testArtifactManager.getOWLManager().removeCache(nextOntology.getOntologyID());
         }
         
         // Once the reasoner determines that the ontology is consistent, copy the statements to a
         // permanent repository connection
         final RepositoryConnection permanentRepositoryConnection = null;
         
         // TODO: Copy the statements to permanentRepositoryConnection
         
         // NOTE: At this stage, a client could be notified, and the artifact could be streamed back
         // to them from permanentRepositoryConnection
         
         final InferredOWLOntologyID inferredOWLOntologyID =
                 testArtifactManager.getOWLManager().generateInferredOntologyID(nextOntology.getOntologyID());
         
         // Use an OWLAPI InferredAxiomGenerator together with the reasoner to create inferred
         // axioms to store in the database
         // Serialise the inferred statements back to a different context in the permanent
         // repository connection
         // The contexts to use within the permanent repository connection are all encapsulated in
         // the InferredOWLOntologyID object
         testArtifactManager.getOWLManager().inferStatements(inferredOWLOntologyID, permanentRepositoryConnection);
         
         // make sure in a finally block to remove the cache for the ontology
         testArtifactManager.getOWLManager().removeCache(inferredOWLOntologyID);
         
         // return InferredOWLOntologyID() with the context of the inferred statements
         
     }
     
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception
     {
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#deregisterProcessor(com.github.podd.api.PoddProcessorFactory, com.github.podd.api.PoddProcessorStage)}
      * .
      */
     @Test
     public final void testDeregisterProcessor()
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#getProcessors(com.github.podd.api.PoddProcessorStage)}
      * .
      */
     @Test
     public final void testGetProcessors()
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testLoadArtifact()
     {
         
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Test
     public final void testPublishArtifact()
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#registerProcessor(com.github.podd.api.PoddProcessorFactory, com.github.podd.api.PoddProcessorStage)}
      * .
      */
     @Test
     public final void testRegisterProcessor()
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImport(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Test
     public final void testUpdateSchemaImport()
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
 }
