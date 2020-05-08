 /**
  * 
  */
 package com.github.podd.api.test;
 
 import info.aduna.iteration.Iterations;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.RDFWriter;
 import org.openrdf.rio.Rio;
 import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
 import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
 import org.semanticweb.owlapi.io.StreamDocumentSource;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddArtifactManager;
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.api.PoddSchemaManager;
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.api.file.PoddFileReferenceManager;
 import com.github.podd.api.file.PoddFileReferenceProcessorFactory;
 import com.github.podd.api.file.PoddFileReferenceProcessorFactoryRegistry;
 import com.github.podd.api.purl.PoddPurlManager;
 import com.github.podd.api.purl.PoddPurlProcessorFactory;
 import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
 import com.github.podd.exception.EmptyOntologyException;
 import com.github.podd.exception.InconsistentOntologyException;
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.utils.DebugUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public abstract class AbstractPoddArtifactManagerTest
 {
     
     protected Logger log = LoggerFactory.getLogger(this.getClass());
     
     private PoddArtifactManager testArtifactManager;
     private PoddRepositoryManager testRepositoryManager;
     private PoddSchemaManager testSchemaManager;
     private PoddSesameManager testSesameManager;
     
     private RepositoryConnection testRepositoryConnection;
     
     private URI schemaGraph;
     
     private URI artifactGraph;
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of PoddArtifactManager
      * for each invocation.
      * 
      * @return A new empty instance of an implementation of PoddArtifactManager.
      */
     protected abstract PoddArtifactManager getNewArtifactManager();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * PoddPurlProcessorFactory that can process DOI references for each invocation.
      * 
      * @return A new empty instance of an implementation of PoddPurlProcessorFactory that can
      *         process DOI references.
      */
     protected abstract PoddPurlProcessorFactory getNewDoiPurlProcessorFactory();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * PoddFileReferenceManager.
      * 
      * @return A new empty instance of an implementation of PoddFileReferenceManager.
      */
     protected abstract PoddFileReferenceManager getNewFileReferenceManager();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * PoddPurlProcessorFactory that can process Handle references for each invocation.
      * 
      * @return A new empty instance of an implementation of PoddPurlProcessorFactory that can
      *         process Handle references.
      */
     protected abstract PoddPurlProcessorFactory getNewHandlePurlProcessorFactory();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * PoddFileReferenceProcessorFactory that can process HTTP-based file references for each
      * invocation.
      * 
      * @return A new empty instance of an implementation of PoddFileReferenceProcessorFactory that
      *         can process HTTP-based file references.
      */
     protected abstract PoddFileReferenceProcessorFactory getNewHttpFileReferenceProcessorFactory();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of {@link PoddOWLManager}
      * .
      * 
      * @return A new empty instance of an implementation of PoddOWLManager.
      */
     protected abstract PoddOWLManager getNewOWLManager();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * {@link PoddPurlManager}.
      * 
      * @return A new empty instance of an implementation of PoddPurlManager.
      */
     protected abstract PoddPurlManager getNewPurlManager();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * {@link OWLReasonerFactory} that can be used with the {@link PoddOWLManager}.
      * 
      * @return A new empty instance of an implementation of OWLReasonerFactory.
      */
     protected abstract OWLReasonerFactory getNewReasonerFactory();
     
     /**
      * Concrete tests must override this to provide a new, initialised, instance of
      * {@link PoddRepositoryManager} with the desired {@link Repository} for this test.
      * 
      * @return A new, initialised. instance of {@link PoddRepositoryManager}
      * @throws OpenRDFException
      *             If there were problems creating or initialising the Repository.
      */
     protected abstract PoddRepositoryManager getNewRepositoryManager() throws OpenRDFException;
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * {@link PoddSchemaManager}.
      * 
      * @return A new empty instance of an implementation of PoddSchemaManager.
      */
     protected abstract PoddSchemaManager getNewSchemaManager();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * {@link PoddSesameManager}.
      * 
      * @return
      */
     protected abstract PoddSesameManager getNewSesameManager();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * {@link PoddFileReferenceProcessorFactory} that can process SSH-based file references for each
      * invocation.
      * 
      * @return A new empty instance of an implementation of PoddFileReferenceProcessorFactory that
      *         can process SSH-based file references.
      */
     protected abstract PoddFileReferenceProcessorFactory getNewSSHFileReferenceProcessorFactory();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * {@link PoddPurlProcessorFactory} that can process UUID references for each invocation.
      * 
      * @return A new empty instance of an implementation of PoddPurlProcessorFactory that can
      *         process UUID references.
      */
     protected abstract PoddPurlProcessorFactory getNewUUIDPurlProcessorFactory();
     
     /**
      * Helper method which loads, infers and stores a given ontology using the PoddOWLManager.
      * 
      * @param resourcePath
      * @param format
      * @param assertedStatementCount
      * @param inferredStatementCount
      * @param repositoryConnection
      *            TODO
      * @return
      * @throws Exception
      */
     private InferredOWLOntologyID loadInferStoreOntology(final String resourcePath, final RDFFormat format,
             final long assertedStatementCount, final long inferredStatementCount,
             final RepositoryConnection repositoryConnection) throws Exception
     {
         // load ontology to OWLManager
         final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
         Assert.assertNotNull("Could not find resource", inputStream);
         final OWLOntologyDocumentSource owlSource =
                 new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                         format.getDefaultMIMEType()));
         
         final OWLOntology loadedBaseOntology = this.testArtifactManager.getOWLManager().loadOntology(owlSource);
         
         repositoryConnection.begin();
         
         this.testArtifactManager.getOWLManager().dumpOntologyToRepository(loadedBaseOntology, repositoryConnection);
         
         // infer statements and dump to repository
         final InferredOWLOntologyID inferredOntologyID =
                 this.testArtifactManager.getOWLManager().inferStatements(loadedBaseOntology, repositoryConnection);
         
         // verify statement counts
         final URI versionURI = loadedBaseOntology.getOntologyID().getVersionIRI().toOpenRDFURI();
         Assert.assertEquals("Wrong statement count", assertedStatementCount, repositoryConnection.size(versionURI));
         
         final URI inferredOntologyURI = inferredOntologyID.getInferredOntologyIRI().toOpenRDFURI();
         Assert.assertEquals("Wrong inferred statement count", inferredStatementCount,
                 repositoryConnection.size(inferredOntologyURI));
         
         repositoryConnection.commit();
         
         return inferredOntologyID;
     }
     
     /**
      * Helper method which loads the three PODD schema ontologies: PODD-Base, PODD-Science and
      * PODD-Plant.
      * 
      * This method is not called from the setUp() method since some tests require not loading all
      * schema ontologies.
      * 
      * @throws Exception
      */
     private void loadSchemaOntologies() throws Exception
     {
         // prepare: load schema ontologies
         final InferredOWLOntologyID inferredDctermsOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_DCTERMS, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_INFERRED, this.testRepositoryConnection);
         final InferredOWLOntologyID inferredFoafOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_FOAF, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_INFERRED, this.testRepositoryConnection);
         final InferredOWLOntologyID inferredPUserOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_USER, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_INFERRED, this.testRepositoryConnection);
         final InferredOWLOntologyID inferredPBaseOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_BASE, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED, this.testRepositoryConnection);
         final InferredOWLOntologyID inferredPScienceOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_SCIENCE, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED, this.testRepositoryConnection);
         final InferredOWLOntologyID inferredPPlantOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_PLANT, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_PLANT_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_PLANT_INFERRED, this.testRepositoryConnection);
         
         // prepare: update schema management graph
         this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredDctermsOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredFoafOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPUserOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPBaseOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPScienceOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPPlantOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
     }
     
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception
     {
         this.schemaGraph = ValueFactoryImpl.getInstance().createURI("urn:test:schema-graph");
         this.artifactGraph = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-graph");
         
         this.testRepositoryManager = this.getNewRepositoryManager();
         this.testRepositoryManager.setSchemaManagementGraph(this.schemaGraph);
         this.testRepositoryManager.setArtifactManagementGraph(this.artifactGraph);
         
         this.testRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
         
         final PoddFileReferenceProcessorFactoryRegistry testFileRegistry =
                 new PoddFileReferenceProcessorFactoryRegistry();
         // clear any automatically added entries that may come from META-INF/services entries on the
         // classpath
         testFileRegistry.clear();
         
         final PoddPurlProcessorFactoryRegistry testPurlRegistry = new PoddPurlProcessorFactoryRegistry();
         testPurlRegistry.clear();
         final PoddPurlProcessorFactory uuidFactory = this.getNewUUIDPurlProcessorFactory();
         Assert.assertNotNull("UUID factory was null", uuidFactory);
         testPurlRegistry.add(uuidFactory);
         
         /**
          * // In practice, the following factories would be automatically added to the registry, //
          * however for testing we want to explicitly add the ones we want to support for each test
          * PoddFileReferenceProcessorFactory sshFactory =
          * this.getNewSSHFileReferenceProcessorFactory();
          * Assert.assertNotNull("SSH factory was null", sshFactory);
          * testFileRegistry.add(sshFactory);
          * 
          * PoddFileReferenceProcessorFactory httpFactory =
          * this.getNewHttpFileReferenceProcessorFactory();
          * Assert.assertNotNull("HTTP factory was null", httpFactory);
          * testFileRegistry.add(httpFactory);
          */
         
         final PoddFileReferenceManager testFileReferenceManager = this.getNewFileReferenceManager();
         testFileReferenceManager.setProcessorFactoryRegistry(testFileRegistry);
         
         /**
          * // FIXME: Implement these purl processor factories PoddPurlProcessorFactory doiFactory =
          * this.getNewDoiPurlProcessorFactory(); testPurlRegistry.add(doiFactory);
          * Assert.assertNotNull("DOI factory was null", httpFactory);
          * 
          * PoddPurlProcessorFactory handleFactory = this.getNewHandlePurlProcessorFactory();
          * testPurlRegistry.add(handleFactory); Assert.assertNotNull("Handle factory was null",
          * handleFactory);
          **/
         
         final PoddPurlManager testPurlManager = this.getNewPurlManager();
         testPurlManager.setPurlProcessorFactoryRegistry(testPurlRegistry);
         
         final PoddOWLManager testOWLManager = this.getNewOWLManager();
         testOWLManager.setReasonerFactory(this.getNewReasonerFactory());
         final OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
         Assert.assertNotNull("Null implementation of OWLOntologymanager", manager);
         testOWLManager.setOWLOntologyManager(manager);
         
         this.testSchemaManager = this.getNewSchemaManager();
         this.testSchemaManager.setOwlManager(testOWLManager);
         this.testSchemaManager.setRepositoryManager(this.testRepositoryManager);
         
         this.testSesameManager = this.getNewSesameManager();
         
         this.testArtifactManager = this.getNewArtifactManager();
         this.testArtifactManager.setRepositoryManager(this.testRepositoryManager);
         this.testArtifactManager.setFileReferenceManager(testFileReferenceManager);
         this.testArtifactManager.setPurlManager(testPurlManager);
         this.testArtifactManager.setOwlManager(testOWLManager);
         this.testArtifactManager.setSchemaManager(this.testSchemaManager);
         this.testArtifactManager.setSesameManager(this.testSesameManager);
         
     }
     
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception
     {
         this.testArtifactManager = null;
         
         try
         {
             if(this.testRepositoryConnection.isActive())
             {
                 this.testRepositoryConnection.rollback();
             }
         }
         finally
         {
             if(this.testRepositoryConnection.isOpen())
             {
                 this.testRepositoryConnection.close();
             }
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#deleteArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      * Tests that the artifact manager can delete an artifact when there was a single version
      * loaded, and the version is given to the deleteArtifact method.
      */
     @Test
     public final void testDeleteArtifactWithVersionSingle() throws Exception
     {
         this.loadSchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         // MIME type should be either given by the user, detected from the content type on the
         // request, or autodetected using the Any23 Mime Detector
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         // invoke test method
         final InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
         
         // verify:
         this.verifyLoadedArtifact(resultArtifactId, 7,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         
         Assert.assertTrue("Could not delete artifact", this.testArtifactManager.deleteArtifact(resultArtifactId));
         
         try
         {
             this.testArtifactManager.getArtifactByIRI(resultArtifactId.getOntologyIRI());
             
             Assert.fail("Current contract is to throw an exception when someone tries to get an artifact that does not exist");
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             Assert.assertNotNull("Exception did not contain the requested artifact IRI", e.getOntologyID());
             
             Assert.assertEquals("IRI on the exception did not match our expected IRI",
                     resultArtifactId.getOntologyIRI(), e.getOntologyID());
         }
         
     }
     
     @Test
     public final void testGetFileReferenceManager() throws Exception
     {
         Assert.assertNotNull("File Reference Manager was null", this.testArtifactManager.getFileReferenceManager());
     }
     
     @Test
     public final void testGetOWLManager() throws Exception
     {
         Assert.assertNotNull("OWL Manager was null", this.testArtifactManager.getOWLManager());
     }
     
     @Test
     public final void testGetPurlManager() throws Exception
     {
         Assert.assertNotNull("Purl Manager was null", this.testArtifactManager.getPurlManager());
     }
     
     @Test
     public final void testGetRepositoryManager() throws Exception
     {
         Assert.assertNotNull("Repository Manager was null", this.testArtifactManager.getRepositoryManager());
     }
     
     @Test
     public final void testGetSchemaManager() throws Exception
     {
         Assert.assertNotNull("Schema Manager was null", this.testArtifactManager.getSchemaManager());
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddArtifactManager#listPublishedArtifacts()}. .
      */
     @Test
     public final void testListPublishedArtifacts() throws Exception
     {
         this.loadSchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         // MIME type should be either given by the user, detected from the content type on the
         // request, or autodetected using the Any23 Mime Detector
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         final InferredOWLOntologyID unpublishedArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
         this.verifyLoadedArtifact(unpublishedArtifactId, 7,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         
         // invoke method under test
         final InferredOWLOntologyID publishedArtifactId =
                 this.testArtifactManager.publishArtifact(unpublishedArtifactId);
         
         Assert.assertNotNull(publishedArtifactId);
         
         final Collection<InferredOWLOntologyID> listPublishedArtifacts =
                 this.testArtifactManager.listPublishedArtifacts();
         
         this.log.info("published artifacts: {}", listPublishedArtifacts);
         
         Assert.assertNotNull(listPublishedArtifacts);
         Assert.assertEquals(1, listPublishedArtifacts.size());
         
         final InferredOWLOntologyID nextArtifact = listPublishedArtifacts.iterator().next();
         Assert.assertEquals(unpublishedArtifactId.getOntologyIRI(), nextArtifact.getOntologyIRI());
         
         final Collection<InferredOWLOntologyID> listUnpublishedArtifacts =
                 this.testArtifactManager.listUnpublishedArtifacts();
         
         Assert.assertNotNull(listUnpublishedArtifacts);
         Assert.assertTrue(listUnpublishedArtifacts.isEmpty());
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddArtifactManager#listPublishedArtifacts()}. .
      */
     @Test
     public final void testListUnpublishedArtifacts() throws Exception
     {
         this.loadSchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         // MIME type should be either given by the user, detected from the content type on the
         // request, or autodetected using the Any23 Mime Detector
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         final InferredOWLOntologyID unpublishedArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
         this.verifyLoadedArtifact(unpublishedArtifactId, 7,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         
         final Collection<InferredOWLOntologyID> listPublishedArtifacts =
                 this.testArtifactManager.listPublishedArtifacts();
         
         Assert.assertNotNull(listPublishedArtifacts);
         Assert.assertTrue(listPublishedArtifacts.isEmpty());
         
         final Collection<InferredOWLOntologyID> listUnpublishedArtifacts =
                 this.testArtifactManager.listUnpublishedArtifacts();
         
         Assert.assertNotNull(listUnpublishedArtifacts);
         Assert.assertEquals(1, listUnpublishedArtifacts.size());
         
         final InferredOWLOntologyID nextArtifact = listUnpublishedArtifacts.iterator().next();
         Assert.assertEquals(unpublishedArtifactId.getOntologyIRI(), nextArtifact.getOntologyIRI());
         Assert.assertEquals(unpublishedArtifactId, nextArtifact);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testLoadArtifactBasicSuccess() throws Exception
     {
         this.loadSchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         // MIME type should be either given by the user, detected from the content type on the
         // request, or autodetected using the Any23 Mime Detector
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         // invoke test method
         final InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
         
         // verify:
         this.verifyLoadedArtifact(resultArtifactId, 7, 32, 295, false);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testLoadArtifactWithEmptyOntology() throws Exception
     {
         final InputStream inputStream = this.getClass().getResourceAsStream("/test/ontologies/empty.owl");
         final RDFFormat format = Rio.getParserFormatForMIMEType("application/rdf+xml", RDFFormat.RDFXML);
         
         try
         {
             // invoke test method
             this.testArtifactManager.loadArtifact(inputStream, format);
             Assert.fail("Should have thrown an EmptyOntologyException");
         }
         catch(final EmptyOntologyException e)
         {
             Assert.assertEquals("Exception does not have expected message.", "Loaded ontology is empty", e.getMessage());
             Assert.assertTrue("The ontology is not empty", e.getOntology().isEmpty());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testLoadArtifactWithInconsistency() throws Exception
     {
         this.loadSchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream("/test/artifacts/error-twoLeadInstitutions-1.rdf");
         // MIME type should be either given by the user, detected from the content type on the
         // request, or autodetected using the Any23 Mime Detector
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         try
         {
             // invoke test method
             this.testArtifactManager.loadArtifact(inputStream, format);
             Assert.fail("Should have thrown an InconsistentOntologyException");
         }
         catch(final InconsistentOntologyException e)
         {
             final OWLReasoner reasoner = e.getReasoner();
             Assert.assertFalse("Reasoner says ontology is consistent", reasoner.isConsistent());
             Assert.assertEquals("Not the expected Root Ontology", "urn:temp:inconsistentArtifact:1", reasoner
                     .getRootOntology().getOntologyID().getOntologyIRI().toString());
             Assert.assertEquals("Not the expected error message", "Ontology is inconsistent", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      * 
      * This test attempts to load an RDF/XML serialized artifact after wrongly specifying the MIME
      * type as turtle. The exception thrown depends on the expected and actual MIME type
      * combination.
      */
     @Test
     public final void testLoadArtifactWithIncorrectFormat() throws Exception
     {
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         try
         {
             // invoke test method with the invalid RDF Format of TURTLE
             this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
             Assert.fail("Should have thrown an RDFParseException");
         }
         catch(final RDFParseException e)
         {
             Assert.assertTrue(e.getMessage().startsWith("Not a valid"));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testLoadArtifactWithMissingSchemaOntologiesInRepository() throws Exception
     {
         // prepare: load schema ontologies
         final InferredOWLOntologyID inferredDctermsOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_DCTERMS, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_INFERRED, this.testRepositoryConnection);
         final InferredOWLOntologyID inferredFoafOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_FOAF, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_INFERRED, this.testRepositoryConnection);
         final InferredOWLOntologyID inferredPUserOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_USER, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_INFERRED, this.testRepositoryConnection);
         final InferredOWLOntologyID inferredPBaseOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_BASE, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED, this.testRepositoryConnection);
         final InferredOWLOntologyID inferredPScienceOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_SCIENCE, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED, this.testRepositoryConnection);
         
         // prepare: update schema management graph
         this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredDctermsOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredFoafOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPUserOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPBaseOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         // PODD-Science ontology is not added to schema management graph
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         final RDFFormat format = Rio.getParserFormatForMIMEType("application/rdf+xml", RDFFormat.RDFXML);
         
         try
         {
             // invoke test method
             this.testArtifactManager.loadArtifact(inputStream, format);
             Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
         }
         catch(final UnmanagedSchemaIRIException e)
         {
             Assert.assertEquals("The cause should have been the missing PODD Science ontology",
                     inferredPScienceOntologyID.getBaseOWLOntologyID().getOntologyIRI(), e.getOntologyID());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      * 
      * Tests loading an artifact where the source RDF statements do not contain a version IRI.
      * 
      */
     @Test
     public final void testLoadArtifactWithNoVersionIRIInSource() throws Exception
     {
         this.loadSchemaOntologies();
         
         // load artifact
         final InputStream inputStream4FirstArtifact =
                 this.getClass().getResourceAsStream("/test/artifacts/project-with-no-version-info.rdf");
         final InferredOWLOntologyID firstArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream4FirstArtifact, RDFFormat.RDFXML);
         
         // verify
         this.verifyLoadedArtifact(firstArtifactId, 7, 33, 296, false);
         Assert.assertEquals("Version IRI of loaded ontology not expected value", firstArtifactId.getOntologyIRI()
                 .toString().concat(":version:1"), firstArtifactId.getVersionIRI().toString());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      * 
      * Tests loading an artifact which imports a previous version of a schema ontology (i.e.
      * poddScience v1)
      */
     @Test
     public final void testLoadArtifactWithNonCurrentSchemaVersionImport() throws Exception
     {
         // prepare:
         this.loadSchemaOntologies();
         
         // prepare: load poddScience v2
         final InferredOWLOntologyID inferredPScienceOntologyID =
                 this.loadInferStoreOntology("/test/ontologies/poddScienceV2.owl", RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED, this.testRepositoryConnection);
         this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPScienceOntologyID, true,
                 this.testRepositoryConnection, this.schemaGraph);
         
         // load test artifact
         final InputStream inputStream4Artifact =
                 this.getClass().getResourceAsStream("/test/artifacts/project-imports-sciencev1-version.rdf");
         final InferredOWLOntologyID artifactId =
                 this.testArtifactManager.loadArtifact(inputStream4Artifact, RDFFormat.RDFXML);
         
         this.verifyLoadedArtifact(artifactId, 7, 33, 296, false);
         
         // verify:
         RepositoryConnection nextRepositoryConnection = null;
         try
         {
             nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
             nextRepositoryConnection.begin();
             
             String[] expectedImports = { 
                     "http://purl.org/podd/ns/version/dcTerms/1", 
                     "http://purl.org/podd/ns/version/poddUser/1",
                     "http://purl.org/podd/ns/version/poddBase/1",
                     "http://purl.org/podd/ns/version/poddScience/1", // an older version
                     };
             
             // verify: no. of import statements
             final int importStatementCount =
                     Iterations.asList(
                             nextRepositoryConnection.getStatements(null, OWL.IMPORTS, null, false, artifactId
                                     .getVersionIRI().toOpenRDFURI())).size();
             Assert.assertEquals("Graph should have 4 import statements", 4, importStatementCount);
             
             for(int i = 0; i < expectedImports.length; i++)
             {
                 final List<Statement> importStatements =
                         Iterations.asList(nextRepositoryConnection.getStatements(null, OWL.IMPORTS, ValueFactoryImpl
                                 .getInstance().createURI(expectedImports[i]), false, artifactId.getVersionIRI()
                                 .toOpenRDFURI()));
                 
                 Assert.assertEquals("Expected 1 import statement per schema", 1, importStatements.size());
             }
         }
         finally
         {
             if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
             {
                 nextRepositoryConnection.rollback();
             }
             if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
             {
                 nextRepositoryConnection.close();
             }
             nextRepositoryConnection = null;
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      * 
      * Tests loading two artifacts one after the other.
      * 
      */
     @Test
     public final void testLoadArtifactWithTwoDistinctArtifacts() throws Exception
     {
         this.loadSchemaOntologies();
         
         // load 1st artifact
         final InputStream inputStream4FirstArtifact =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         final InferredOWLOntologyID firstArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream4FirstArtifact, RDFFormat.RDFXML);
         
         this.verifyLoadedArtifact(firstArtifactId, 7,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         
         // load 2nd artifact
         final InputStream inputStream4SecondArtifact =
                 this.getClass().getResourceAsStream("/test/artifacts/basicProject-2.rdf");
         final InferredOWLOntologyID secondArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream4SecondArtifact, RDFFormat.RDFXML);
         
         this.verifyLoadedArtifact(firstArtifactId, 14,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         this.verifyLoadedArtifact(secondArtifactId, 14, 29, 290, true);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      * 
      * Tests loading two versions of the same artifact one after the other.
      * 
      * The two source RDF files have PURLs instead of temporary URIs since they both need to be
      * identified as the same artifact.
      */
     @Test
     public final void testLoadArtifactWithTwoVersionsOfSameArtifact() throws Exception
     {
         this.loadSchemaOntologies();
         
         // load 1st artifact
         final InputStream inputStream4FirstArtifact =
                 this.getClass().getResourceAsStream("/test/artifacts/project-with-purls-v1.rdf");
         final InferredOWLOntologyID firstArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream4FirstArtifact, RDFFormat.RDFXML);
         
         this.verifyLoadedArtifact(firstArtifactId, 7, 29, 290, false);
         
         // load 2nd artifact
         final InputStream inputStream4SecondArtifact =
                 this.getClass().getResourceAsStream("/test/artifacts/project-with-purls-v2.rdf");
         final InferredOWLOntologyID secondArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream4SecondArtifact, RDFFormat.RDFXML);
         
         Assert.assertEquals("Both versions should have the same artifact ID", firstArtifactId.getOntologyIRI(),
                 secondArtifactId.getOntologyIRI());
         
         Assert.assertFalse("Two versions should NOT have the same Version IRI", firstArtifactId.getVersionIRI()
                 .toString().equals(secondArtifactId.getVersionIRI().toString()));
         
         this.verifyLoadedArtifact(secondArtifactId, 7, 25, 282, false);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      * 
      * Tests that the version IRI in the source file is ignored.
      * 
      */
     @Test
     public final void testLoadArtifactWithVersionIRIInSourceIgnored() throws Exception
     {
         this.loadSchemaOntologies();
         
         // load 1st artifact
         final InputStream inputStream4Artifact =
                 this.getClass().getResourceAsStream("/test/artifacts/basicProject-1-published.rdf");
         final InferredOWLOntologyID artifactId =
                 this.testArtifactManager.loadArtifact(inputStream4Artifact, RDFFormat.RDFXML);
         
         this.verifyLoadedArtifact(artifactId, 7, 29, 290, true);
         
         Assert.assertFalse("Version IRI in source should have been ignored", artifactId.getVersionIRI().toString()
                 .endsWith(":55"));
         Assert.assertTrue("New generated Version IRI should start from 1", artifactId.getVersionIRI().toString()
                 .endsWith(":1"));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Test
     public final void testPublishArtifactBasicSuccess() throws Exception
     {
         this.loadSchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         // MIME type should be either given by the user, detected from the content type on the
         // request, or autodetected using the Any23 Mime Detector
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         final InferredOWLOntologyID unpublishedArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
         this.verifyLoadedArtifact(unpublishedArtifactId, 7,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         
         // invoke method under test
         final InferredOWLOntologyID publishedArtifactId =
                 this.testArtifactManager.publishArtifact(unpublishedArtifactId);
         
         // verify: publication status is correctly updated
         RepositoryConnection nextRepositoryConnection = null;
         try
         {
             nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
             nextRepositoryConnection.begin();
             
             // verify: a single PUBLICATION_STATUS in asserted ontology
             final List<Statement> publicationStatusStatementList =
                     Iterations.asList(nextRepositoryConnection.getStatements(null,
                             PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS, null, false, publishedArtifactId
                                     .getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Graph should have one HAS_PUBLICATION_STATUS statement.", 1,
                     publicationStatusStatementList.size());
             
             // verify: artifact is PUBLISHED
             Assert.assertEquals("Wrong publication status", PoddRdfConstants.PODDBASE_PUBLISHED.toString(),
                     publicationStatusStatementList.get(0).getObject().toString());
         }
         finally
         {
             if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
             {
                 nextRepositoryConnection.rollback();
             }
             if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
             {
                 nextRepositoryConnection.close();
             }
             nextRepositoryConnection = null;
         }
         
         // FIXME: How do we get information about whether an artifact is published and other
         // metadata like who can access the artifact?
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      */
     @Test
     public final void testUpdateArtifactAddNewPoddObjectWithMerge() throws Exception
     {
         this.loadSchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         
         final InferredOWLOntologyID artifactId = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactId, 7, 97, 393, false);
         
         final InputStream editInputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_PUBLICATION_OBJECT);
         InferredOWLOntologyID updatedArtifact =
                 this.testArtifactManager.updateArtifact(artifactId.getOntologyIRI().toOpenRDFURI(), editInputStream,
                         RDFFormat.TURTLE, false);
         
         
         // verify:
         RepositoryConnection nextRepositoryConnection = null;
         try
         {
             nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
             nextRepositoryConnection.begin();
             
             verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                     108, nextRepositoryConnection);
             
             // verify: 2 publications exist
             final List<Statement> testList =
                     Iterations.asList(nextRepositoryConnection.getStatements(null, ValueFactoryImpl
                             .getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "hasPublication"), null, false, updatedArtifact
                             .getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Graph should have 2 publications", 2, testList.size());
             
             // verify: newly added publication exists
             Assert.assertTrue("New publication is missing", 
                     testList.get(0).getObject().toString().endsWith("#publication46") || 
                     testList.get(1).getObject().toString().endsWith("#publication46"));
         }
         finally
         {
             if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
             {
                 nextRepositoryConnection.rollback();
             }
             if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
             {
                 nextRepositoryConnection.close();
             }
             nextRepositoryConnection = null;
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      */
     @Test
    public final void testUpdateArtifactModifyPoddObjectWithReplace() throws Exception
     {
         this.loadSchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         
         final InferredOWLOntologyID artifactId = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
         final int assertedStatementCount = 97;
         this.verifyLoadedArtifact(artifactId, 7, assertedStatementCount, 393, false);
         
         final InputStream editInputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFIED_PUBLICATION_OBJECT);
         InferredOWLOntologyID updatedArtifact =
                 this.testArtifactManager.updateArtifact(artifactId.getOntologyIRI().toOpenRDFURI(), editInputStream,
                         RDFFormat.TURTLE, true);
         
         
         // verify:
         RepositoryConnection nextRepositoryConnection = null;
         try
         {
             nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
             nextRepositoryConnection.begin();
             
             verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                     assertedStatementCount, nextRepositoryConnection);
             
             // verify: still only 1 publication
             final List<Statement> testList =
                     Iterations.asList(nextRepositoryConnection.getStatements(null, ValueFactoryImpl
                             .getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "hasPublication"), null, false, updatedArtifact
                             .getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Incorrect no. of hasPublication statements", 1, testList.size());
             
             // verify: publication info has been updated
             final List<Statement> testList2 = Iterations.asList(nextRepositoryConnection.getStatements(
                             null, 
                             ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "hasYear"), 
                             null, 
                             false, updatedArtifact.getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Incorrect no. of hasYear statements", 1, testList2.size());
             Assert.assertEquals("Publication Year has not bee updated", "2011", testList2.get(0).getObject().stringValue());
             
         }
         finally
         {
             if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
             {
                 nextRepositoryConnection.rollback();
             }
             if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
             {
                 nextRepositoryConnection.close();
             }
             nextRepositoryConnection = null;
         }
     }    
 
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      */
     @Test
     public final void testUpdateArtifactAddToNonExistentArtifact() throws Exception
     {
         this.loadSchemaOntologies();
         
         final URI nonExistentArtifactURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-3-no-such-artifact");
         
         final InputStream editInputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFIED_PUBLICATION_OBJECT);
         
         try
         {
             this.testArtifactManager.updateArtifact(nonExistentArtifactURI, editInputStream,
                     RDFFormat.TURTLE, true);
             Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
         }
         catch (UnmanagedArtifactIRIException e)
         {
             Assert.assertEquals("Exception not due to expected IRI", nonExistentArtifactURI, e.getOntologyID().toOpenRDFURI());
         }
     }    
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      * 
      * NOTE: Once file reference validation is implemented in the FileReferenceManager this test will fail.
      * The referred file will have to be created for validation to pass.
      */
     @Test
     public final void testUpdateArtifactAddNewPoddObjectWithFileReferences() throws Exception
     {
         this.loadSchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         
         final InferredOWLOntologyID artifactId = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactId, 7, 97, 393, false);
         
         final InputStream editInputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT);
         InferredOWLOntologyID updatedArtifact =
                 this.testArtifactManager.updateArtifact(artifactId.getOntologyIRI().toOpenRDFURI(), editInputStream,
                         RDFFormat.RDFXML, false);
         
         
         // verify:
         RepositoryConnection nextRepositoryConnection = null;
         try
         {
             nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
             nextRepositoryConnection.begin();
             
             DebugUtils.printContents(nextRepositoryConnection, updatedArtifact.getVersionIRI().toOpenRDFURI());
             
             verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                     105, nextRepositoryConnection);
             
             // verify: file reference object
             final List<Statement> fileRefList =
                     Iterations.asList(nextRepositoryConnection.getStatements(null, ValueFactoryImpl.getInstance()
                             .createURI(PoddRdfConstants.PODD_BASE, "hasFileReference"), null, false, updatedArtifact
                             .getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Graph should have 1 file reference", 1, fileRefList.size());
 
             Assert.assertTrue("File reference value incorrect",
                     fileRefList.get(0).getObject().stringValue().endsWith("publication-pdf-a"));
         }
         finally
         {
             if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
             {
                 nextRepositoryConnection.rollback();
             }
             if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
             {
                 nextRepositoryConnection.close();
             }
             nextRepositoryConnection = null;
         }
 
     }
 
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      */
     @Ignore
     @Test
     public final void testUpdateArtifactWithDanglingObjects() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }    
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      */
     @Ignore
     @Test
     public final void testUpdateArtifactDeletePoddObject() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }    
 
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      */
     @Ignore
     @Test
     public final void testUpdateArtifactMovePoddObject() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }    
 
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImport(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Ignore
     @Test
     public final void testUpdateSchemaImport() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Helper method to write Repository graphs to files when required.
      * 
      * NOTE 1: This test is to be regularly ignored as it does not test any functionality.
      * 
      * NOTE 2: Schemas and artifacts are loaded through the standard PODD manager API classes. One
      * effect of this is that any existing version IRI in the source file of an artifact is ignored.
      * Therefore, the inferred triples will import the internally generated version.
      * 
      * @since 06/03/2013
      */
     @Ignore
     @Test
     public final void testWriteInferredOntologyToFile() throws Exception
     {
         this.loadSchemaOntologies();
         final InputStream inputStream = this.getClass().getResourceAsStream("/test/artifacts/basic-20130206.ttl");
         final RDFFormat readFormat = RDFFormat.TURTLE;
         final InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, readFormat);
         
         dumpRdfToFile(resultArtifactId.getInferredOntologyIRI().toOpenRDFURI(),
                 "/home/kutila/basic-20130206-inferred.ttl", RDFFormat.TURTLE);
         
 /*            
         String[] contexts = {
                 // "http://purl.org/podd/ns/version/dcTerms/1",
                 "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/dcTerms/1",
                 // "http://purl.org/podd/ns/version/foaf/1",
                 "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/foaf/1",
                 // "http://purl.org/podd/ns/version/poddUser/1",
                 "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddUser/1",
                 // "http://purl.org/podd/ns/version/poddBase/1",
                 "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddBase/1",
                 // "http://purl.org/podd/ns/version/poddScience/1",
                 "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddScience/1",
                 // "http://purl.org/podd/ns/version/poddPlant/1",
                 "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddPlant/1", };
             
         String[] fileNames = {
                 // "dcTerms",
                 "dcTermsInferred",
                 // "foaf",
                 "foafInferred",
                 // "poddUser",
                 "poddUserInferred",
                 // "poddBase",
                 "poddBaseInferred",
                 // "poddScience",
                 "poddScienceInferred",
                 // "poddPlant",
                 "poddPlantInferred", };
             
         for(int i = 0; i < contexts.length; i++)
         {
             URI context = ValueFactoryImpl.getInstance().createURI(contexts[i]);
             final RDFFormat writeFormat = RDFFormat.RDFXML;
             String path = "/home/kutila/";
             
             dumpRdfToFile(context, (path + fileNames[i]), writeFormat);
         }
 */    
     }
     
     /**
      * Write contents of specified context to a file
      * 
      * @param context
      * @param filename
      * @param writeFormat
      * @throws IOException
      * @throws OpenRDFException
      */
     public void dumpRdfToFile(URI context, String filename, final RDFFormat writeFormat) throws IOException,
         OpenRDFException
     {
         String outFilename = filename + "." + writeFormat.getFileExtensions().get(0);
         
         RDFWriter writer = Rio.createWriter(writeFormat, new FileOutputStream(filename));
         writer.handleNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
         writer.handleNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
         writer.handleNamespace("owl", "http://www.w3.org/2002/07/owl#");
         writer.handleNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
         writer.handleNamespace("xml", "http://www.w3.org/XML/1998/namespace");
         
         writer.handleNamespace("dc", "http://purl.org/podd/ns/dcTerms#");
         
         writer.startRDF();
         
         List<Statement> inferredList =
                 Iterations.asList(this.testRepositoryConnection.getStatements(null, null, null, false, context));
         for(Statement s : inferredList)
         {
             writer.handleStatement(s);
         }
         writer.endRDF();
         this.log.info("Wrote {} statements to file {}", inferredList.size(), outFilename);
     }
     
     /**
      * Helper method to verify the contents of artifact management graph
      * 
      * @param repositoryConnection
      * @param graphSize
      *            Expected size of the graph
      * @param testGraph
      *            The Graph/context to be tested
      * @param ontologyIRI
      *            The ontology/artifact
      * @param versionIRI
      *            Version IRI of the ontology/artifact
      * @param inferredVersionIRI
      *            Inferred version of the ontology/artifact
      * @throws Exception
      */
     private void verifyArtifactManagementGraphContents(final RepositoryConnection repositoryConnection,
             final int graphSize, final URI testGraph, final IRI ontologyIRI, final IRI versionIRI,
             final IRI inferredVersionIRI) throws Exception
     {
         Assert.assertEquals("Graph not of expected size", graphSize, repositoryConnection.size(testGraph));
         
         // verify: OWL_VERSION
         final List<Statement> stmtList =
                 Iterations.asList(repositoryConnection.getStatements(ontologyIRI.toOpenRDFURI(),
                         PoddRdfConstants.OWL_VERSION_IRI, null, false, testGraph));
         Assert.assertEquals("Graph should have one OWL_VERSION statement", 1, stmtList.size());
         Assert.assertEquals("Wrong OWL_VERSION in Object", versionIRI.toString(), stmtList.get(0).getObject()
                 .toString());
         
         // verify: OMV_CURRENT_VERSION
         final List<Statement> currentVersionStatementList =
                 Iterations.asList(repositoryConnection.getStatements(ontologyIRI.toOpenRDFURI(),
                         PoddRdfConstants.OMV_CURRENT_VERSION, null, false, testGraph));
         Assert.assertEquals("Graph should have one OMV_CURRENT_VERSION statement", 1,
                 currentVersionStatementList.size());
         Assert.assertEquals("Wrong OMV_CURRENT_VERSION in Object", versionIRI.toString(), currentVersionStatementList
                 .get(0).getObject().toString());
         
         // verify: INFERRED_VERSION
         final List<Statement> inferredVersionStatementList =
                 Iterations.asList(repositoryConnection.getStatements(versionIRI.toOpenRDFURI(),
                         PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null, false, testGraph));
         Assert.assertEquals("Graph should have one INFERRED_VERSION statement", 1, inferredVersionStatementList.size());
         Assert.assertEquals("Wrong INFERRED_VERSION in Object", inferredVersionIRI.toString(),
                 inferredVersionStatementList.get(0).getObject().toString());
         
         // verify: CURRENT_INFERRED_VERSION
         final List<Statement> currentInferredVersionStatementList =
                 Iterations.asList(repositoryConnection.getStatements(ontologyIRI.toOpenRDFURI(),
                         PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null, false, testGraph));
         Assert.assertEquals("Graph should have one CURRENT_INFERRED_VERSION statement", 1,
                 currentInferredVersionStatementList.size());
         Assert.assertEquals("Wrong CURRENT_INFERRED_VERSION in Object", inferredVersionIRI.toString(),
                 currentInferredVersionStatementList.get(0).getObject().toString());
     }
     
     /**
      * Helper method to verify that the given InferredOWLOntologyID represents an artifact that has
      * been successfully loaded.
      * 
      * @param inferredOntologyId
      *            Identifies the loaded ontology
      * @param mgtGraphSize
      *            Expected size of the artifact management graph
      * @param assertedStatementCount
      *            Number of asserted statements in repository for this artifact
      * @param inferredStatementCount
      *            Number of inferred statements in repository for this artifact
      * @param isPublished
      *            True if the artifact is Published, false otherwise
      * @throws Exception
      */
     private void verifyLoadedArtifact(final InferredOWLOntologyID inferredOntologyId, final int mgtGraphSize,
             final long assertedStatementCount, final long inferredStatementCount, final boolean isPublished)
         throws Exception
     {
         // verify: ontology ID has all details
         Assert.assertNotNull("Null ontology ID", inferredOntologyId);
         Assert.assertNotNull("Null ontology IRI", inferredOntologyId.getOntologyIRI());
         Assert.assertNotNull("Null ontology version IRI", inferredOntologyId.getVersionIRI());
         Assert.assertNotNull("Null inferred ontology IRI", inferredOntologyId.getInferredOntologyIRI());
         
         RepositoryConnection nextRepositoryConnection = null;
         try
         {
             nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
             nextRepositoryConnection.begin();
             
             // verify: size of asserted graph
             Assert.assertEquals("Incorrect number of asserted statements for artifact", assertedStatementCount,
                     nextRepositoryConnection.size(inferredOntologyId.getVersionIRI().toOpenRDFURI()));
             
             // verify: size of inferred graph
             Assert.assertEquals("Incorrect number of inferred statements for artifact", inferredStatementCount,
                     nextRepositoryConnection.size(inferredOntologyId.getInferredOntologyIRI().toOpenRDFURI()));
             
             // verify: artifact management graph contents
             this.verifyArtifactManagementGraphContents(nextRepositoryConnection, mgtGraphSize,
                     this.testRepositoryManager.getArtifactManagementGraph(), inferredOntologyId.getOntologyIRI(),
                     inferredOntologyId.getVersionIRI(), inferredOntologyId.getInferredOntologyIRI());
             
             // verify: a single PUBLICATION_STATUS in asserted ontology
             final List<Statement> publicationStatusStatementList =
                     Iterations.asList(nextRepositoryConnection.getStatements(null,
                             PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS, null, false, inferredOntologyId
                                     .getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Graph should have one HAS_PUBLICATION_STATUS statement", 1,
                     publicationStatusStatementList.size());
             
             // verify: value of PUBLICATION_STATUS in asserted ontology
             String publishedState = PoddRdfConstants.PODDBASE_NOT_PUBLISHED.toString();
             if(isPublished)
             {
                 publishedState = PoddRdfConstants.PODDBASE_PUBLISHED.toString();
             }
             Assert.assertEquals("Wrong publication status", publishedState, publicationStatusStatementList.get(0)
                     .getObject().toString());
         }
         finally
         {
             if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
             {
                 nextRepositoryConnection.rollback();
             }
             if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
             {
                 nextRepositoryConnection.close();
             }
             nextRepositoryConnection = null;
         }
     }
     
     /**
      * Helper method to verify that an updated artifact has expected version IRI etc.
      * 
      * @param updatedArtifact
      * @param expectedVersionIri
      * @param expectedConcreteStatementCount
      * @param nextRepositoryConnection
      * @throws Exception
      */
     private void verifyUpdatedArtifact(InferredOWLOntologyID updatedArtifact, String expectedVersionIri,
             long expectedConcreteStatementCount, RepositoryConnection nextRepositoryConnection) throws Exception
     {
         Assert.assertEquals("Unexpected concrete statement count", expectedConcreteStatementCount,
                 nextRepositoryConnection.size(updatedArtifact.getVersionIRI().toOpenRDFURI()));
         
         // verify: owl:versionIRI incremented in graph
         final List<Statement> versionIRIs =
                 Iterations.asList(nextRepositoryConnection.getStatements(null, PoddRdfConstants.OWL_VERSION_IRI, null,
                         false, updatedArtifact.getVersionIRI().toOpenRDFURI()));
         Assert.assertEquals("Should have only 1 version IRI", 1, versionIRIs.size());
         Assert.assertEquals("Version IRI not expected value", expectedVersionIri, versionIRIs.get(0).getObject()
                 .stringValue());
         
         // verify: current version updated in management graph
         InferredOWLOntologyID currentArtifactVersion =
                 this.testArtifactManager.getSesameManager().getCurrentArtifactVersion(updatedArtifact.getOntologyIRI(),
                         nextRepositoryConnection, artifactGraph);
         Assert.assertEquals("Unexpected Version IRI in management graph", expectedVersionIri, currentArtifactVersion
                 .getVersionIRI().toString());
     }
 
    
 }
