 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.api.test;
 
 import java.nio.file.Path;
 import java.util.Collections;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 import org.junit.rules.Timeout;
 import org.openrdf.model.URI;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.sail.memory.MemoryStore;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PODD;
 
 /**
  * @author kutila
  * 
  */
 public abstract class AbstractPoddRepositoryManagerTest
 {
     protected final Logger log = LoggerFactory.getLogger(this.getClass());
     protected final ValueFactory vf = PODD.VF;
     
     @Rule
     public Timeout timeout = new Timeout(300000);
     
     @Rule
     public TemporaryFolder tempDir = new TemporaryFolder();
     
     private PoddRepositoryManager testRepositoryManager;
     
     private URI schemaGraph;
     
     private URI artifactGraph;
     
     private InferredOWLOntologyID testA1;
     private InferredOWLOntologyID testB1;
     private InferredOWLOntologyID testB2;
     private InferredOWLOntologyID testC1;
     private InferredOWLOntologyID testC3;
     private InferredOWLOntologyID testImportOntologyID1;
     private InferredOWLOntologyID testImportOntologyID2;
     private InferredOWLOntologyID testImportOntologyID3;
     private InferredOWLOntologyID testImportOntologyID4;
     private URI testImportOntologyUri1;
     private URI testImportOntologyUri2;
     private URI testImportOntologyUri3;
     private URI testImportOntologyUri4;
     private URI testImportVersionUri1;
     private URI testImportVersionUri2;
     private URI testImportVersionUri3;
     private URI testImportVersionUri4;
     
     private InferredOWLOntologyID testOntologyID;
     private URI testOntologyUri1;
     private URI testInferredUri1;
     
     private URI testOntologyUriA;
     private URI testOntologyUriB;
     private URI testOntologyUriC;
     
     private URI testPoddBaseUri;
     private URI testPoddBaseUriV1;
     private URI testPoddBaseUriV2;
     private InferredOWLOntologyID testPoddBaseV1;
     private InferredOWLOntologyID testPoddBaseV2;
     
     private URI testPoddDcUri;
     private URI testPoddDcUriV1;
     private URI testPoddDcUriV2;
     private InferredOWLOntologyID testPoddDcV1;
     private InferredOWLOntologyID testPoddDcV2;
     
     private URI testPoddFoafUri;
     private URI testPoddFoafUriV1;
     private URI testPoddFoafUriV2;
     private InferredOWLOntologyID testPoddFoafV1;
     private InferredOWLOntologyID testPoddFoafV2;
     
     private URI testPoddPlantUri;
     private URI testPoddPlantUriV1;
     private URI testPoddPlantUriV2;
     private InferredOWLOntologyID testPoddPlantV1;
     private InferredOWLOntologyID testPoddPlantV2;
     
     private URI testPoddScienceUri;
     private URI testPoddScienceUriV1;
     private URI testPoddScienceUriV2;
     private InferredOWLOntologyID testPoddScienceV1;
     private InferredOWLOntologyID testPoddScienceV2;
     
     private URI testPoddUserUri;
     private URI testPoddUserUriV1;
     private URI testPoddUserUriV2;
     private InferredOWLOntologyID testPoddUserV1;
     private InferredOWLOntologyID testPoddUserV2;
     
     private URI testVersionUri1;
     private URI testVersionUriA1;
     private URI testVersionUriB1;
     private URI testVersionUriB2;
     private URI testVersionUriC1;
     private URI testVersionUriC3;
     private Path testTempRepositoryManagerPath;
     private Repository managementRepository;
     
     private final InferredOWLOntologyID owlid(final IRI ontologyUri, final IRI versionUri, final IRI inferredUri)
     {
         return new InferredOWLOntologyID(ontologyUri, versionUri, inferredUri);
     }
     
     private final InferredOWLOntologyID owlid(final URI ontologyUri, final URI versionUri)
     {
         return this.owlid(ontologyUri, versionUri, null);
     }
     
     private final InferredOWLOntologyID owlid(final URI ontologyUri, final URI versionUri, final URI inferredUri)
     {
         return new InferredOWLOntologyID(ontologyUri, versionUri, inferredUri);
     }
     
     private final URI uri(final String uri)
     {
         return PODD.VF.createURI(uri);
     }
     
     /**
      * @return A new instance of PoddOWLManager, for each call to this method
      * @throws Exception
      */
     protected abstract PoddRepositoryManager getNewPoddRepositoryManagerInstance(Repository managementRepository,
             Path tempDirPath) throws RepositoryException, Exception;
     
     @Before
     public void setUp() throws Exception
     {
         this.testPoddDcUri = this.uri("http://purl.org/podd/ns/dcTerms");
         this.testPoddDcUriV1 = this.uri("http://purl.org/podd/ns/version/dcTerms/1");
         this.testPoddDcUriV2 = this.uri("http://purl.org/podd/ns/version/dcTerms/2");
         this.testPoddDcV1 = this.owlid(this.testPoddDcUri, this.testPoddDcUriV1);
         this.testPoddDcV2 = this.owlid(this.testPoddDcUri, this.testPoddDcUriV2);
         
         this.testPoddFoafUri = this.uri("http://purl.org/podd/ns/foaf");
         this.testPoddFoafUriV1 = this.uri("http://purl.org/podd/ns/version/foaf/1");
         this.testPoddFoafUriV2 = this.uri("http://purl.org/podd/ns/version/foaf/2");
         this.testPoddFoafV1 = this.owlid(this.testPoddFoafUri, this.testPoddFoafUriV1);
         this.testPoddFoafV2 = this.owlid(this.testPoddFoafUri, this.testPoddFoafUriV2);
         
         this.testPoddUserUri = this.uri("http://purl.org/podd/ns/poddUser");
         this.testPoddUserUriV1 = this.uri("http://purl.org/podd/ns/version/poddUser/1");
         this.testPoddUserUriV2 = this.uri("http://purl.org/podd/ns/version/poddUser/2");
         this.testPoddUserV1 = this.owlid(this.testPoddUserUri, this.testPoddUserUriV1);
         this.testPoddUserV2 = this.owlid(this.testPoddUserUri, this.testPoddUserUriV2);
         
         this.testPoddBaseUri = this.uri("http://purl.org/podd/ns/poddBase");
         this.testPoddBaseUriV1 = this.uri("http://purl.org/podd/ns/version/poddBase/1");
         this.testPoddBaseUriV2 = this.uri("http://purl.org/podd/ns/version/poddBase/2");
         this.testPoddBaseV1 = this.owlid(this.testPoddBaseUri, this.testPoddBaseUriV1);
         this.testPoddBaseV2 = this.owlid(this.testPoddBaseUri, this.testPoddBaseUriV2);
         
         this.testPoddScienceUri = this.uri("http://purl.org/podd/ns/poddScience");
         this.testPoddScienceUriV1 = this.uri("http://purl.org/podd/ns/version/poddScience/1");
         this.testPoddScienceUriV2 = this.uri("http://purl.org/podd/ns/version/poddScience/2");
         this.testPoddScienceV1 = this.owlid(this.testPoddScienceUri, this.testPoddScienceUriV1);
         this.testPoddScienceV2 = this.owlid(this.testPoddScienceUri, this.testPoddScienceUriV2);
         
         this.testPoddPlantUri = this.uri("http://purl.org/podd/ns/poddPlant");
         this.testPoddPlantUriV1 = this.uri("http://purl.org/podd/ns/version/poddPlant/1");
         this.testPoddPlantUriV2 = this.uri("http://purl.org/podd/ns/version/poddPlant/2");
         this.testPoddPlantV1 = this.owlid(this.testPoddPlantUri, this.testPoddPlantUriV1);
         this.testPoddPlantV2 = this.owlid(this.testPoddPlantUri, this.testPoddPlantUriV2);
         
         this.testOntologyUri1 = this.uri("urn:test:ontology:uri:1");
         this.testVersionUri1 = this.uri("urn:test:ontology:uri:1:version:1");
         this.testInferredUri1 = this.uri("urn:inferred:test:ontology:uri:1:version:1");
         this.testOntologyID = this.owlid(this.testOntologyUri1, this.testVersionUri1, this.testInferredUri1);
         
         this.testImportOntologyUri1 = this.uri("urn:test:import:ontology:uri:1");
         this.testImportVersionUri1 = this.uri("urn:test:import:ontology:uri:1:version:1");
         this.testImportOntologyID1 = this.owlid(this.testImportOntologyUri1, this.testImportVersionUri1);
         
         this.testImportOntologyUri2 = this.uri("urn:test:import:ontology:uri:2");
         this.testImportVersionUri2 = this.uri("urn:test:import:ontology:uri:2:version:1");
         this.testImportOntologyID2 = this.owlid(this.testImportOntologyUri2, this.testImportVersionUri2);
         
         this.testImportOntologyUri3 = this.uri("urn:test:import:ontology:uri:3");
         this.testImportVersionUri3 = this.uri("urn:test:import:ontology:uri:3:version:1");
         this.testImportOntologyID3 = this.owlid(this.testImportOntologyUri3, this.testImportVersionUri3);
         
         this.testImportOntologyUri4 = this.uri("urn:test:import:ontology:uri:4");
         this.testImportVersionUri4 = this.uri("urn:test:import:ontology:uri:4:version:1");
         this.testImportOntologyID4 = this.owlid(this.testImportOntologyUri4, this.testImportVersionUri4);
         
         this.testOntologyUriA = this.uri("http://example.org/podd/ns/poddA");
         this.testVersionUriA1 = this.uri("http://example.org/podd/ns/version/poddA/1");
         this.testA1 = this.owlid(this.testOntologyUriA, this.testVersionUriA1);
         
         this.testOntologyUriB = this.uri("http://example.org/podd/ns/poddB");
         this.testVersionUriB1 = this.uri("http://example.org/podd/ns/version/poddB/1");
         this.testB1 = this.owlid(this.testOntologyUriB, this.testVersionUriB1);
         this.testVersionUriB2 = this.uri("http://example.org/podd/ns/version/poddB/2");
         this.testB2 = this.owlid(this.testOntologyUriB, this.testVersionUriB2);
         
         this.testOntologyUriC = this.uri("http://example.org/podd/ns/poddC");
         this.testVersionUriC1 = this.uri("http://example.org/podd/ns/version/poddC/1");
         this.testC1 = this.owlid(this.testOntologyUriC, this.testVersionUriC1);
         this.testVersionUriC3 = this.uri("http://example.org/podd/ns/version/poddC/3");
         this.testC3 = this.owlid(this.testOntologyUriC, this.testVersionUriC3);
         testTempRepositoryManagerPath = tempDir.newFolder("test-podd-base-directory").toPath();
         this.schemaGraph = PODD.VF.createURI("urn:test:schema-graph");
         this.artifactGraph = PODD.VF.createURI("urn:test:artifact-graph");
         
         managementRepository = new SailRepository(new MemoryStore());
         managementRepository.initialize();
         
         setupManager();
     }
     
     private void setupManager() throws Exception
     {
         this.testRepositoryManager =
                 this.getNewPoddRepositoryManagerInstance(managementRepository, testTempRepositoryManagerPath);
         this.testRepositoryManager.setSchemaManagementGraph(schemaGraph);
         this.testRepositoryManager.setArtifactManagementGraph(artifactGraph);
         
     }
     
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception
     {
         this.testRepositoryManager.shutDown();
         this.testRepositoryManager = null;
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getArtifactManagementGraph()}.
      */
     @Test
     public final void testGetArtifactManagementGraph() throws Exception
     {
         Assert.assertNotNull("Artifact management graph was null",
                 this.testRepositoryManager.getArtifactManagementGraph());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getFileRepositoryManagementGraph()}.
      */
     @Test
     public final void testGetFileRepositoryManagementGraph() throws Exception
     {
         Assert.assertNotNull("File repository management graph was null",
                 this.testRepositoryManager.getFileRepositoryManagementGraph());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getNewTemporaryRepository()}.
      */
     @Test
     public final void testGetNewTemporaryRepository() throws Exception
     {
         Repository newTempRepository = null;
         RepositoryConnection tempRepositoryConnection = null;
         try
         {
             newTempRepository = this.testRepositoryManager.getNewTemporaryRepository();
             Assert.assertNotNull("New temporary repository was null", newTempRepository);
             Assert.assertTrue("New temporary repository was not initialized", newTempRepository.isInitialized());
             tempRepositoryConnection = newTempRepository.getConnection();
             tempRepositoryConnection.begin();
             Assert.assertEquals("New temporary repository was not empty", 0, tempRepositoryConnection.size());
         }
         finally
         {
             if(tempRepositoryConnection != null && tempRepositoryConnection.isActive())
             {
                 tempRepositoryConnection.rollback();
             }
             if(tempRepositoryConnection != null && tempRepositoryConnection.isOpen())
             {
                 tempRepositoryConnection.close();
             }
             if(newTempRepository != null)
             {
                 newTempRepository.shutDown();
             }
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getManagementRepository()}.
      */
     @Test
     public final void testGetManagementRepository() throws Exception
     {
         Assert.assertNotNull("Management repository was null", this.testRepositoryManager.getManagementRepository());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getManagementRepository()}.
      */
     @Test
     public final void testGetPermanentRepositorySingleSchema() throws Exception
     {
         Repository permanentRepository1 =
                 this.testRepositoryManager
                         .getPermanentRepository(Collections.<OWLOntologyID> singleton(testOntologyID));
         Assert.assertNotNull("Permanent repository was null", permanentRepository1);
         
         Repository permanentRepository2 =
                 this.testRepositoryManager.getPermanentRepository(Collections
                         .<OWLOntologyID> singleton(this.testOntologyID));
         Assert.assertNotNull("Permanent repository was null", permanentRepository2);
         
         // Must be exactly the same object
         Assert.assertEquals(permanentRepository1, permanentRepository2);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getManagementRepository()}.
      */
     @Test
     public final void testGetPermanentRepositorySingleSchemaReload() throws Exception
     {
         // Verify sanity first
         Repository permanentRepository1 =
                 this.testRepositoryManager
                         .getPermanentRepository(Collections.<OWLOntologyID> singleton(testOntologyID));
         Assert.assertNotNull("Permanent repository was null", permanentRepository1);
         
         Repository permanentRepository2 =
                 this.testRepositoryManager.getPermanentRepository(Collections
                         .<OWLOntologyID> singleton(this.testOntologyID));
         Assert.assertNotNull("Permanent repository was null", permanentRepository2);
         
         // Must be exactly the same object
         Assert.assertEquals(permanentRepository1, permanentRepository2);
         
         // shutdown the repository manager
         this.testRepositoryManager.shutDown();
         
         // Reload a repository manager on this path
         PoddRepositoryManager reloadedRepositoryManager =
                 getNewPoddRepositoryManagerInstance(managementRepository, testTempRepositoryManagerPath);
         
         Assert.assertNotNull(reloadedRepositoryManager);
         
         // Repeat the double load process on the existing repository to test the other possible code
         // paths
         Repository permanentRepository3 =
                 reloadedRepositoryManager.getPermanentRepository(Collections.<OWLOntologyID> singleton(testOntologyID));
         Assert.assertNotNull("Permanent repository was null", permanentRepository3);
         
         Repository permanentRepository4 =
                 reloadedRepositoryManager.getPermanentRepository(Collections
                         .<OWLOntologyID> singleton(this.testOntologyID));
         Assert.assertNotNull("Permanent repository was null", permanentRepository4);
         
         // Must be exactly the same object
         Assert.assertEquals(permanentRepository3, permanentRepository4);
         
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getManagementRepository()}.
      */
     @Test
     public final void testGetPermanentRepositoryEmptySchemaSet() throws Exception
     {
         Assert.assertNotNull("Permanent repository was null",
                 this.testRepositoryManager.getPermanentRepository(Collections.<OWLOntologyID> emptySet()));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getManagementRepository()}.
      */
     @Test
     public final void testGetPermanentRepositoryNull() throws Exception
     {
         try
         {
             this.testRepositoryManager.getPermanentRepository(null);
             Assert.fail("Did not receive the expected exception");
         }
         catch(final NullPointerException e)
         {
             
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getSchemaManagementGraph()}.
      */
     @Test
     public final void testGetSchemaManagementGraph() throws Exception
     {
         Assert.assertNotNull("Schema management graph was null", this.testRepositoryManager.getSchemaManagementGraph());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#setArtifactManagementGraph(org.openrdf.model.URI)}
      * .
      */
     @Test
     public final void testSetArtifactManagementGraph() throws Exception
     {
         final URI testArtifactMgtGraph = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-graph");
         this.testRepositoryManager.setArtifactManagementGraph(testArtifactMgtGraph);
         Assert.assertEquals("Artifact graph was not correctly set", testArtifactMgtGraph,
                 this.testRepositoryManager.getArtifactManagementGraph());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#setFileRepositoryManagementGraph(org.openrdf.model.URI)}
      * .
      */
     @Test
     public final void testSetFileRepositoryManagementGraph() throws Exception
     {
         final URI testFileRepositoryMgtGraph =
                 ValueFactoryImpl.getInstance().createURI("urn:test:file-repository-graph");
         this.testRepositoryManager.setFileRepositoryManagementGraph(testFileRepositoryMgtGraph);
         Assert.assertEquals("File Repository graph was not correctly set", testFileRepositoryMgtGraph,
                 this.testRepositoryManager.getFileRepositoryManagementGraph());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#setSchemaManagementGraph(org.openrdf.model.URI)}
      * .
      */
     @Test
     public final void testSetSchemaManagementGraph() throws Exception
     {
         final URI testSchemaMgtGraph = ValueFactoryImpl.getInstance().createURI("urn:my-test:schema-management-graph");
         this.testRepositoryManager.setSchemaManagementGraph(testSchemaMgtGraph);
         Assert.assertEquals("Schema graph was not correctly set", testSchemaMgtGraph,
                 this.testRepositoryManager.getSchemaManagementGraph());
     }
     
 }
