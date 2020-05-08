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
 
 import info.aduna.iteration.Iterations;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.util.GraphUtil;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.openrdf.sail.memory.MemoryStore;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.MetadataPolicy;
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.utils.DebugUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.OntologyUtils;
 import com.github.podd.utils.PODD;
 import com.github.podd.utils.PoddObjectLabel;
 
 /**
  * @author kutila
  * 
  */
 public abstract class AbstractPoddSesameManagerTest
 {
     /* test artifact pair with PURLs */
     
     protected Logger log = LoggerFactory.getLogger(this.getClass());
     
     private PoddSesameManager testPoddSesameManager;
     
     private Repository testRepository;
     private RepositoryConnection testRepositoryConnection;
     
     private URI artifactGraph;
     
     private URI schemaGraph;
     
     public abstract PoddSesameManager getNewPoddSesameManagerInstance();
     
     /**
      * Helper method for testing
      * {@link com.github.podd.api.PoddSesameManager#isPublished(OWLOntologyID, RepositoryConnection)}
      * 
      */
     private boolean internalTestIsPublished(final boolean isPublished, final String testResourcePath,
             final int expectedSize, final URI contextVersionURI, final URI managementGraph) throws Exception
     {
         // prepare: load the ontology into the test repository
         final InputStream inputStream = this.getClass().getResourceAsStream(testResourcePath);
         this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, contextVersionURI);
         Assert.assertEquals("Not the expected number of statements in Repository", expectedSize,
                 this.testRepositoryConnection.size(contextVersionURI));
         
         // prepare: build an OWLOntologyID
         final IRI ontologyIRI =
                 this.testPoddSesameManager.getOntologyIRI(this.testRepositoryConnection, contextVersionURI);
         final InferredOWLOntologyID ontologyID =
                 new InferredOWLOntologyID(ontologyIRI, IRI.create(contextVersionURI),
                         IRI.create("urn:not:actually:inferred"));
         this.testRepositoryConnection.add(ontologyIRI.toOpenRDFURI(), RDF.TYPE, OWL.ONTOLOGY, managementGraph);
         this.testRepositoryConnection.add(ontologyIRI.toOpenRDFURI(), OWL.VERSIONIRI, contextVersionURI,
                 managementGraph);
         
         final InferredOWLOntologyID publishedOntologyID =
                 this.testPoddSesameManager.setPublished(isPublished, ontologyID, this.testRepositoryConnection,
                         managementGraph);
         
         return this.testPoddSesameManager.isPublished(publishedOntologyID, this.testRepositoryConnection,
                 managementGraph);
     }
     
     /**
      * Loads the statements in the specified resource paths as the asserted and inferred statements
      * of an ontology. The contexts to load into are identified from the <i>OWL:VersionIRI</i>
      * values in both files.
      * 
      * NOTE: This method does not update any management graphs.
      * 
      * @param resourcePath
      *            Points to a resource containing asserted statements
      * @param inferredResourcePath
      *            Points to a resource containing the inferred statements
      * @param format
      *            The Format of both resources
      * @return An InferredOWLOntologyID for the loaded ontology
      * @throws Exception
      */
     private InferredOWLOntologyID loadOntologyFromResource(final String resourcePath,
             final String inferredResourcePath, final RDFFormat format) throws Exception
     {
         final InputStream resourceStream = this.getClass().getResourceAsStream(resourcePath);
         Assert.assertNotNull("Resource was null", resourceStream);
         
         final Model concreteModel = Rio.parse(resourceStream, "", format);
         
         Model inferredModel = new LinkedHashModel();
         if(inferredResourcePath != null)
         {
             final InputStream inferredResourceStream = this.getClass().getResourceAsStream(inferredResourcePath);
             Assert.assertNotNull("Inferred resource was null", inferredResourceStream);
             
             // load inferred statements into a Model
             inferredModel = Rio.parse(inferredResourceStream, "", format);
             
             // extract version IRI which is also the inferred IRI
             this.testRepositoryConnection.add(inferredModel,
                     GraphUtil.getUniqueSubjectURI(inferredModel, RDF.TYPE, OWL.ONTOLOGY));
         }
         
         final URI ontologyURI = GraphUtil.getUniqueSubjectURI(concreteModel, RDF.TYPE, OWL.ONTOLOGY);
         this.log.debug("ontology URI: {}", ontologyURI);
         // dump the statements into the correct context of the Repository
         this.testRepositoryConnection.add(concreteModel,
                 GraphUtil.getUniqueObjectURI(concreteModel, ontologyURI, OWL.VERSIONIRI));
         
         final Model totalModel = new LinkedHashModel(concreteModel);
         totalModel.addAll(inferredModel);
         
         final Collection<InferredOWLOntologyID> results = OntologyUtils.modelToOntologyIDs(totalModel);
         Assert.assertEquals(1, results.size());
         
         return results.iterator().next();
     }
     
     /**
      * This method loads all PODD schema ontologies.
      */
     private List<InferredOWLOntologyID> loadSchemaOntologies() throws Exception
     {
         return this.loadSchemaOntologies(this.testRepositoryConnection, this.schemaGraph);
     }
     
     /**
      * Load all PODD schema ontologies and their inferred statements into the given
      * RepositoryConnection.
      * 
      * @param conn
      * @return
      * @throws Exception
      */
     protected abstract List<InferredOWLOntologyID> loadSchemaOntologies(RepositoryConnection conn,
             URI schemaManagementGraph) throws Exception;
     
     /**
      * Helper method which populates a graph with artifact management triples.
      * 
      * @return The URI of the test artifact management graph
      * @throws Exception
      */
     private URI populateArtifactManagementGraph() throws Exception
     {
         final URI testOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/99-99/artifact:99");
         final URI testVersionURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/99-99/version:1");
         final URI testInferredURI =
                 ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/99-99/version:1");
         
         this.testRepositoryConnection.add(testOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.artifactGraph);
         this.testRepositoryConnection.add(testInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.artifactGraph);
         this.testRepositoryConnection.add(testOntologyURI, OWL.VERSIONIRI, testVersionURI, this.artifactGraph);
         this.testRepositoryConnection
                 .add(testOntologyURI, PODD.OMV_CURRENT_VERSION, testVersionURI, this.artifactGraph);
         this.testRepositoryConnection.add(testOntologyURI, PODD.PODD_BASE_CURRENT_INFERRED_VERSION, testInferredURI,
                 this.artifactGraph);
         this.testRepositoryConnection.add(testVersionURI, PODD.PODD_BASE_INFERRED_VERSION, testInferredURI,
                 this.artifactGraph);
         
         return this.artifactGraph;
     }
     
     /**
      * Helper method which populates a graph with schema management triples.
      * 
      * @return The URI of the test schema management graph
      * @throws Exception
      */
     private URI populateSchemaManagementGraph() throws Exception
     {
         final URI pbBaseOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase");
         final URI pbVersionURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddBase/1");
         final URI pbInferredURI =
                 ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/ns/version/poddBase/1");
         
         final URI pScienceOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddScience");
         final URI pScienceVersionURI =
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddScience/27");
         final URI pScienceInferredURI =
                 ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/ns/version/poddScience/43");
         
         final URI pPlantOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddPlant");
         final URI pPlantVersionURI =
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddPlant/1");
         final URI pPlantInferredURI =
                 ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/ns/version/poddPlant/1");
         
         final URI pPlantVersionURIv2 =
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddPlant/2");
         final URI pPlantInferredURIv2 =
                 ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/ns/version/poddPlant/2");
         
         // Podd-Base
         this.testRepositoryConnection.add(pbBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pbInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pbBaseOntologyURI, PODD.OWL_VERSION_IRI, pbVersionURI, this.schemaGraph);
         this.testRepositoryConnection.add(pbVersionURI, PODD.PODD_BASE_INFERRED_VERSION, pbInferredURI,
                 this.schemaGraph);
         this.testRepositoryConnection.add(pbBaseOntologyURI, PODD.OMV_CURRENT_VERSION, pbVersionURI, this.schemaGraph);
         this.testRepositoryConnection.add(pbBaseOntologyURI, PODD.PODD_BASE_CURRENT_INFERRED_VERSION, pbInferredURI,
                 this.schemaGraph);
         /*
          * The Management graph for Podd-Base created above is as follows.
          * 
          * <http://purl.org/podd/ns/poddBase> <RDF:Type> <OWL:Ontology>
          * 
          * <urn:inferred:http://purl.org/podd/ns/version/poddBase/1> <RDF:Type> <OWL:Ontology>
          * 
          * <http://purl.org/podd/ns/poddBase> <OWL:VersionIRI>
          * <http://purl.org/podd/ns/version/poddBase/1>
          * 
          * <http://purl.org/podd/ns/version/poddBase/1> <poddBase:InferredVersion>
          * <urn:inferred:http://purl.org/podd/ns/version/poddBase/1>
          * 
          * <http://purl.org/podd/ns/poddBase> <OMV:CurrentVersion>
          * <http://purl.org/podd/ns/version/poddBase/1>
          * 
          * <http://purl.org/podd/ns/poddBase> <poddBase:CurrentInferredVersion>
          * <urn:inferred:http://purl.org/podd/ns/version/poddBase/1>
          */
         
         // Podd-Science
         this.testRepositoryConnection.add(pScienceOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pScienceInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pScienceOntologyURI, PODD.OWL_VERSION_IRI, pScienceVersionURI,
                 this.schemaGraph);
         this.testRepositoryConnection.add(pScienceOntologyURI, PODD.OMV_CURRENT_VERSION, pScienceVersionURI,
                 this.schemaGraph);
         this.testRepositoryConnection.add(pScienceOntologyURI, OWL.IMPORTS, pbVersionURI, this.schemaGraph);
         this.testRepositoryConnection.add(pScienceOntologyURI, PODD.PODD_BASE_CURRENT_INFERRED_VERSION,
                 pScienceInferredURI, this.schemaGraph);
         this.testRepositoryConnection.add(pScienceVersionURI, PODD.PODD_BASE_INFERRED_VERSION, pScienceInferredURI,
                 this.schemaGraph);
         
         // Podd-Plant
         this.testRepositoryConnection.add(pPlantOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantVersionURIv2, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantInferredURIv2, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         
         this.testRepositoryConnection.add(pPlantOntologyURI, PODD.OWL_VERSION_IRI, pPlantVersionURI, this.schemaGraph);
         this.testRepositoryConnection
                 .add(pPlantOntologyURI, PODD.OWL_VERSION_IRI, pPlantVersionURIv2, this.schemaGraph);
         
         this.testRepositoryConnection.add(pPlantOntologyURI, PODD.OMV_CURRENT_VERSION, pPlantVersionURIv2,
                 this.schemaGraph);
         this.testRepositoryConnection.add(pPlantOntologyURI, OWL.IMPORTS, pScienceVersionURI, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantOntologyURI, OWL.IMPORTS, pbVersionURI, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantOntologyURI, PODD.PODD_BASE_CURRENT_INFERRED_VERSION,
                 pPlantInferredURIv2, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantVersionURI, PODD.PODD_BASE_INFERRED_VERSION, pPlantInferredURI,
                 this.schemaGraph);
         
         this.testRepositoryConnection.add(pPlantVersionURIv2, PODD.PODD_BASE_INFERRED_VERSION, pPlantInferredURIv2,
                 this.schemaGraph);
         
         return this.schemaGraph;
     }
     
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception
     {
         this.artifactGraph = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-mgt-graph:");
         this.schemaGraph = ValueFactoryImpl.getInstance().createURI("urn:test:schema-mgt-graph:");
         
         this.testPoddSesameManager = this.getNewPoddSesameManagerInstance();
         Assert.assertNotNull("Null implementation of test OWLManager", this.testPoddSesameManager);
         
         // create a memory Repository for tests
         this.testRepository = new SailRepository(new MemoryStore());
         this.testRepository.initialize();
         this.testRepositoryConnection = this.testRepository.getConnection();
         this.testRepositoryConnection.begin();
     }
     
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception
     {
         this.testRepositoryConnection.rollback();
         this.testRepositoryConnection.close();
         this.testRepository.shutDown();
         
         this.testPoddSesameManager = null;
     }
     
     @Test
     public void testDeleteOntologiesSingleValid() throws Exception
     {
         // prepare: create artifact management graph
         final URI artifactGraph = this.populateArtifactManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentArtifactVersion(
                         IRI.create("http://purl.org/podd/99-99/version:1"), this.testRepositoryConnection,
                         artifactGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getInferredOntologyIRI());
         
         this.testPoddSesameManager.deleteOntologies(Arrays.asList(inferredOntologyID), this.testRepositoryConnection,
                 this.testRepositoryConnection, artifactGraph);
         
         try
         {
             this.testPoddSesameManager.getCurrentArtifactVersion(inferredOntologyID.getOntologyIRI(),
                     this.testRepositoryConnection, artifactGraph);
             Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             Assert.assertEquals("Not the expected exception", "This IRI does not refer to a managed ontology",
                     e.getMessage());
             Assert.assertEquals(inferredOntologyID.getOntologyIRI(), e.getUnmanagedOntologyIRI());
         }
         
     }
     
     @Test
     public void testFillMissingLabels() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         final URI[] contexts =
                 this.testPoddSesameManager.versionAndSchemaContexts(ontologyID, this.testRepositoryConnection,
                         this.schemaGraph);
         
         final String[] objectUris =
                 { "http://purl.org/podd/basic-1-20130206/object:2966",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                         "http://purl.org/podd/ns/poddScience#WildType_NotApplicable",
                         "http://purl.org/podd/ns/poddPlant#DeltaTporometer-63",
                         "http://purl.org/podd/ns/poddBase#DisplayType_LongText" };
         
         final String[] expectedLabels =
                 { "Project#2012-0006_ Cotton Leaf Morphology", "Demo genotype", "Squeekee material", "Not Applicable",
                         "Delta-T porometer", null };
         
         // prepare: Model with test data
         final Model testModel = new LinkedHashModel();
         for(final String s : objectUris)
         {
             testModel.add(PODD.VF.createURI(s), RDFS.LABEL, PODD.VF.createLiteral("?blank"));
         }
         
         final Model resultModel =
                 this.testPoddSesameManager.fillMissingLabels(testModel, this.testRepositoryConnection, contexts);
         
         // verify: each URI has the expected label
         for(int i = 0; i < objectUris.length; i++)
         {
             final String objectString =
                     resultModel.filter(PODD.VF.createURI(objectUris[i]), RDFS.LABEL, null).objectString();
            Assert.assertEquals("Not the expected label: " + objectUris[i], expectedLabels[i], objectString);
         }
     }
     
     @Test
     public void testGetAllCurrentSchemaOntologyVersions() throws Exception
     {
         this.populateSchemaManagementGraph();
         
         final Set<IRI> expectedIriList =
                 new HashSet<>(Arrays.asList(IRI.create("http://purl.org/podd/ns/poddBase"),
                         IRI.create("http://purl.org/podd/ns/version/poddBase/1"),
                         IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1"),
                         IRI.create("http://purl.org/podd/ns/poddScience"),
                         IRI.create("http://purl.org/podd/ns/version/poddScience/27"),
                         IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddScience/43"),
                         IRI.create("http://purl.org/podd/ns/poddPlant"),
                         IRI.create("http://purl.org/podd/ns/version/poddPlant/2"),
                         IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddPlant/2")));
         
         final Set<InferredOWLOntologyID> allSchemaOntologyVersions =
                 this.testPoddSesameManager.getAllCurrentSchemaOntologyVersions(this.testRepositoryConnection,
                         this.schemaGraph);
         
         Assert.assertEquals("Incorrect number of schema ontologies", 3, allSchemaOntologyVersions.size());
         for(final InferredOWLOntologyID ontoID : allSchemaOntologyVersions)
         {
             Assert.assertTrue("Missing ontology IRI", expectedIriList.contains(ontoID.getOntologyIRI()));
             Assert.assertTrue("Missing version IRI", expectedIriList.contains(ontoID.getVersionIRI()));
             Assert.assertTrue("Missing inferred IRI", expectedIriList.contains(ontoID.getInferredOntologyIRI()));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getAllValidMembers(InferredOWLOntologyID, URI, RepositoryConnection)}
      * .
      * 
      * Tests retrieving all possible values for Collection types
      */
     @Ignore("Method made private to remove it eventually")
     @Test
     public void testGetAllValidMembers() throws Exception
     {
         final ValueFactory vf = PODD.VF;
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         // Collections to test
         final URI[] collectionsToTest =
                 { vf.createURI(PODD.PODD_SCIENCE, "hasPlatformType"), vf.createURI(PODD.PODD_SCIENCE, "hasSex"),
                         vf.createURI(PODD.PODD_SCIENCE, "hasSoftware"),
                         vf.createURI(PODD.PODD_BASE, "hasPublicationStatus"),
                         vf.createURI(PODD.PODD_SCIENCE, "hasControl"), vf.createURI(PODD.PODD_SCIENCE, "hasWildType"),
                         vf.createURI(PODD.PODD_SCIENCE, "hasANZSRC"), vf.createURI(PODD.PODD_SCIENCE, "Platform"), };
         
         final URI[][] expectedMembers =
                 {
                         { vf.createURI(PODD.PODD_SCIENCE, "PlatformType_Software"),
                                 vf.createURI(PODD.PODD_SCIENCE, "PlatformType_Hardware"),
                                 vf.createURI(PODD.PODD_SCIENCE, "PlatformType_HardwareSoftware") },
                         
                         { vf.createURI(PODD.PODD_SCIENCE, "Sex_NotApplicable"),
                                 vf.createURI(PODD.PODD_SCIENCE, "Sex_Unknown"),
                                 vf.createURI(PODD.PODD_SCIENCE, "Sex_Hermaphrodite"),
                                 vf.createURI(PODD.PODD_SCIENCE, "Sex_Female"),
                                 vf.createURI(PODD.PODD_SCIENCE, "Sex_Male") },
                         
                         {}, // <poddScience:Software> is not a Collection
                         
                         { vf.createURI(PODD.PODD_BASE, "Published"), vf.createURI(PODD.PODD_BASE, "NotPublished"), },
                         
                         { vf.createURI(PODD.PODD_SCIENCE, "HasControl_Yes"),
                                 vf.createURI(PODD.PODD_SCIENCE, "HasControl_No"),
                                 vf.createURI(PODD.PODD_SCIENCE, "HasControl_NotApplicable"),
                                 vf.createURI(PODD.PODD_SCIENCE, "HasControl_Unknown") },
                         
                         { vf.createURI(PODD.PODD_SCIENCE, "WildType_Yes"),
                                 vf.createURI(PODD.PODD_SCIENCE, "WildType_No"),
                                 vf.createURI(PODD.PODD_SCIENCE, "WildType_NotApplicable"),
                                 vf.createURI(PODD.PODD_SCIENCE, "WildType_Unknown") },
                         
                         { vf.createURI(PODD.PODD_SCIENCE, "ANZSRC-NotApplicable"),
                                 vf.createURI(PODD.PODD_SCIENCE, "ANZSRC06-Biological-Sciences"),
                                 vf.createURI(PODD.PODD_SCIENCE, "ANZSRC07-Agriculture-and-Veterinary-Sciences"),
                                 vf.createURI(PODD.PODD_SCIENCE, "ANZSRC11-Medical-and-Health-Sciences"),
                                 vf.createURI(PODD.PODD_SCIENCE, "ANZSRC05-Environmental-Sciences"),
                                 vf.createURI(PODD.PODD_SCIENCE, "ANZSRC10-Technology")
                         
                         },
                         
                         {}, // IMPORTANT: <poddScience:Platform> is not a Collection
                         
                 };
         
         // iterate through test data
         for(final URI element : collectionsToTest)
         {
             // final List<URI> members =
             // this.testPoddSesameManager.getAllValidMembers(ontologyID, collectionsToTest[i],
             // this.testRepositoryConnection);
             // Assert.assertEquals("Not the expected number of members", expectedMembers[i].length,
             // members.size());
             
             // final List<URI> expectedMembersList = Arrays.asList(expectedMembers[i]);
             // for(final URI resultObject : members)
             // {
             // Assert.assertTrue("Unexpected member found",
             // expectedMembersList.contains(resultObject));
             // }
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCardinalityValues(InferredOWLOntologyID, URI, Collection, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetCardinalityValueWithPoddObject() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final URI projectObject =
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130206/object:2966");
         final URI publication45 =
                 ValueFactoryImpl.getInstance().createURI(
                         "http://purl.org/podd/basic-2-20130206/artifact:1#publication45");
         
         final URI[][] testData =
                 {
                         { projectObject,
                                 ValueFactoryImpl.getInstance().createURI(PODD.PODD_BASE, "hasLeadInstitution"),
                                 PODD.PODD_BASE_CARDINALITY_EXACTLY_ONE },
                         { publication45, ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasAbstract"),
                                 PODD.PODD_BASE_CARDINALITY_ZERO_OR_ONE },
                         { projectObject, ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasANZSRC"),
                                 PODD.PODD_BASE_CARDINALITY_ONE_OR_MANY },
                         { publication45, ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasYear"),
                                 PODD.PODD_BASE_CARDINALITY_ZERO_OR_ONE },
                         { publication45, ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasAuthors"),
                                 PODD.PODD_BASE_CARDINALITY_ZERO_OR_ONE }, };
         
         for(final URI[] element : testData)
         {
             final Collection<URI> nextProperty = Arrays.asList(element[1]);
             final Map<URI, URI> cardinalityValue =
                     this.testPoddSesameManager.getCardinalityValues(ontologyID, element[0], nextProperty,
                             this.testRepositoryConnection, this.schemaGraph);
             Assert.assertEquals("Could not find cardinality for: " + nextProperty, 1, cardinalityValue.size());
             Assert.assertTrue("Did not find cardinality for: " + nextProperty, cardinalityValue.containsKey(element[1]));
             Assert.assertEquals("Not the expected cardinality value", element[2], cardinalityValue.get(element[1]));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCardinalityValues(URI, Collection, boolean, RepositoryConnection, URI...)}
      * .
      */
     @Test
     public void testGetCardinalityValueWithPoddObjectType() throws Exception
     {
         // prepare: load schema ontologies
         final List<InferredOWLOntologyID> loadedSchemaOntologies = this.loadSchemaOntologies();
         
         // prepare: build list of Contexts
         final List<URI> contexts = new ArrayList<URI>();
         for(final InferredOWLOntologyID ontologyID : loadedSchemaOntologies)
         {
             contexts.add(ontologyID.getVersionIRI().toOpenRDFURI());
         }
         
         // prepare: test data
         final URI projectType = ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "Project");
         final URI publicationType = ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "Publication");
         
         final URI[][] testData =
                 {
                         { projectType, ValueFactoryImpl.getInstance().createURI(PODD.PODD_BASE, "hasLeadInstitution"),
                                 PODD.PODD_BASE_CARDINALITY_EXACTLY_ONE },
                         { publicationType, ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasAbstract"),
                                 PODD.PODD_BASE_CARDINALITY_ZERO_OR_ONE },
                         { projectType, ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasANZSRC"),
                                 PODD.PODD_BASE_CARDINALITY_ONE_OR_MANY },
                         { publicationType, ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasYear"),
                                 PODD.PODD_BASE_CARDINALITY_ZERO_OR_ONE },
                         { publicationType, ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasAuthors"),
                                 PODD.PODD_BASE_CARDINALITY_ZERO_OR_ONE }, };
         
         for(final URI[] element : testData)
         {
             final Collection<URI> nextProperty = Arrays.asList(element[1]);
             final Map<URI, URI> cardinalityValue =
                     this.testPoddSesameManager.getCardinalityValues(element[0], nextProperty, true,
                             this.testRepositoryConnection, contexts.toArray(new URI[0]));
             Assert.assertEquals("Could not find cardinality for: " + nextProperty, 1, cardinalityValue.size());
             Assert.assertTrue("Did not find cardinality for: " + nextProperty, cardinalityValue.containsKey(element[1]));
             Assert.assertEquals("Not the expected cardinality value", element[2], cardinalityValue.get(element[1]));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentArtifactVersionWithNullOntologyIRI() throws Exception
     {
         // prepare: create artifact management graph
         final URI artifactGraph = this.populateArtifactManagementGraph();
         
         try
         {
             this.testPoddSesameManager.getCurrentArtifactVersion(null, this.testRepositoryConnection, artifactGraph);
             Assert.fail("Should have thrown a RuntimeException");
         }
         catch(final RuntimeException e)
         {
             Assert.assertTrue("Not a NullPointerException as expected", e instanceof NullPointerException);
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentArtifactVersionWithOntologyIRI() throws Exception
     {
         // prepare: create artifact management graph
         final URI artifactGraph = this.populateArtifactManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentArtifactVersion(
                         IRI.create("http://purl.org/podd/99-99/version:1"), this.testRepositoryConnection,
                         artifactGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentArtifactVersionWithUnmanagedOntologyIRI() throws Exception
     {
         // prepare: create artifact management graph
         final URI artifactGraph = this.populateArtifactManagementGraph();
         
         final IRI ontologyIRI = IRI.create("http://purl.org/podd/no-such-artifact:999");
         try
         {
             this.testPoddSesameManager.getCurrentArtifactVersion(ontologyIRI, this.testRepositoryConnection,
                     artifactGraph);
             Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             Assert.assertEquals("Not the expected exception", "This IRI does not refer to a managed ontology",
                     e.getMessage());
             Assert.assertEquals(ontologyIRI, e.getUnmanagedOntologyIRI());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentArtifactVersionWithVersionIRI() throws Exception
     {
         // prepare: create artifact management graph
         final URI artifactGraph = this.populateArtifactManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentArtifactVersion(
                         IRI.create("http://purl.org/podd/99-99/version:1"), this.testRepositoryConnection,
                         artifactGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentSchemaVersionWithNullOntologyIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         try
         {
             this.testPoddSesameManager.getCurrentSchemaVersion(null, this.testRepositoryConnection, schemaGraph);
             Assert.fail("Should have thrown a RuntimeException");
         }
         catch(final NullPointerException e)
         {
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentSchemaVersionWithOntologyIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentSchemaVersion(IRI.create("http://purl.org/podd/ns/poddBase"),
                         this.testRepositoryConnection, schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version",
                 IRI.create("http://purl.org/podd/ns/version/poddBase/1"), inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentSchemaVersionWithUnmanagedOntologyIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         final IRI ontologyIRI = IRI.create("http://purl.org/podd/ns/version/poddBase/999");
         try
         {
             this.testPoddSesameManager.getCurrentSchemaVersion(ontologyIRI, this.testRepositoryConnection, schemaGraph);
             Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
         }
         catch(final UnmanagedSchemaIRIException e)
         {
             Assert.assertEquals("Not the expected exception", "This IRI does not refer to a managed ontology",
                     e.getMessage());
             Assert.assertEquals(ontologyIRI, e.getOntologyID());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentSchemaVersionWithVersionIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentSchemaVersion(
                         IRI.create("http://purl.org/podd/ns/version/poddScience/27"), this.testRepositoryConnection,
                         schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version",
                 IRI.create("http://purl.org/podd/ns/version/poddScience/27"), inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddScience/43"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      * A non-current version IRI is passed in to sesameManager.getCurrentSchemaVersion() with the
      * aim of getting the current Ontology ID in return.
      */
     @Test
     public void testGetCurrentSchemaVersionWithVersionIRINotCurrent() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentSchemaVersion(
                         IRI.create("http://purl.org/podd/ns/version/poddPlant/1"), this.testRepositoryConnection,
                         schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version",
                 IRI.create("http://purl.org/podd/ns/version/poddPlant/2"), inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddPlant/2"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getDirectImports(RepositoryConnection, URI)}.
      */
     @Test
     public void testGetDirectImports() throws Exception
     {
         final String resourcePath = TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT;
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
         Assert.assertNotNull("Could not find resource", inputStream);
         
         final Repository testRepository = new SailRepository(new MemoryStore());
         testRepository.initialize();
         
         this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
         
         // invoke method under test:
         final Set<URI> importedOntologyIRIs =
                 this.testPoddSesameManager.getDirectImports(this.testRepositoryConnection, context);
         
         // verify:
         Assert.assertNotNull("No imports could be found", importedOntologyIRIs);
         Assert.assertEquals("Incorrect number of imports found", 4, importedOntologyIRIs.size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getObjectData(InferredOWLOntologyID, URI, RepositoryConnection)}
      * .
      */
     @Test
     public void testGetObjectData() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final String[] objectUris =
                 { "http://purl.org/podd/basic-1-20130206/object:2966",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                         "http://purl.org/podd/ns/poddScience#WildType_NotApplicable", // NOT a PODD
                                                                                       // Object
                 };
         
         final Object[][] expectedResults =
                 {
                         { 20, 1, "Project#2012-0006_ Cotton Leaf Morphology",
                                 "http://purl.org/podd/basic-2-20130206/artifact:1" },
                         { 4, 1, "Demo genotype", "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Material" },
                         { 5, 1, "Squeekee material",
                                 "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Investigation" },
                         { 5, 2, "Not Applicable" }, };
         
         // test in a loop these PODD objects for their details
         for(int i = 0; i < objectUris.length; i++)
         {
             final URI objectUri = ValueFactoryImpl.getInstance().createURI(objectUris[i]);
             
             final URI[] contexts =
                     this.testPoddSesameManager.versionAndSchemaContexts(ontologyID, this.testRepositoryConnection,
                             this.schemaGraph);
             
             final Model model =
                     this.testPoddSesameManager.getObjectData(ontologyID, objectUri, this.testRepositoryConnection,
                             contexts);
             
             // verify: statement count
             Assert.assertNotNull("NULL model as result", model);
             
             DebugUtils.printContents(model);
             
             Assert.assertEquals("Not the expected no. of statements in model", expectedResults[i][0], model.size());
             
             // verify: parent object
             final Model parentFilter = model.filter(null, null, objectUri);
             Assert.assertEquals("More than expected parent found", expectedResults[i][1], parentFilter.subjects()
                     .size());
             if(parentFilter.subjects().size() == 1)
             {
                 Assert.assertTrue(
                         "Expected Parent is missing",
                         parentFilter.subjects().contains(
                                 ValueFactoryImpl.getInstance().createURI(expectedResults[i][3].toString())));
             }
             
             // verify: label
             Assert.assertEquals("Not the expected label", expectedResults[i][2], model.filter(null, RDFS.LABEL, null)
                     .objectString());
         }
         
         Assert.assertTrue("Expected empty Model for NULL object",
                 this.testPoddSesameManager.getObjectData(ontologyID, null, this.testRepositoryConnection).isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getObjectDetailsForDisplay(InferredOWLOntologyID, URI, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetObjectDetailsForDisplayWithPublicationObject() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final URI objectUri =
                 ValueFactoryImpl.getInstance().createURI(
                         "http://purl.org/podd/basic-2-20130206/artifact:1#publication45");
         
         final Model displayModel =
                 this.testPoddSesameManager.getObjectDetailsForDisplay(ontologyID, objectUri,
                         this.testRepositoryConnection, this.schemaGraph);
         
         // verify:
         Assert.assertNotNull("Display Model is null", displayModel);
         Assert.assertFalse("Display Model is empty", displayModel.isEmpty());
         Assert.assertEquals("Display Model not of expected size", 13, displayModel.size());
         Assert.assertEquals("Not the expected no. of statements about object", 6,
                 displayModel.filter(objectUri, null, null).size());
         
         // verify: a string search for some content
         Assert.assertTrue("Expected content missing in display model",
                 displayModel.toString().contains("Proceedings of the IEEE eScience 2010"));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getObjectDetailsForDisplay(InferredOWLOntologyID, URI, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetObjectDetailsForDisplayWithTopObject() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final URI objectUri =
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130206/object:2966");
         
         final Model displayModel =
                 this.testPoddSesameManager.getObjectDetailsForDisplay(ontologyID, objectUri,
                         this.testRepositoryConnection, this.schemaGraph);
         
         // verify:
         Assert.assertNotNull("Display Model is null", displayModel);
         Assert.assertFalse("Display Model is empty", displayModel.isEmpty());
         Assert.assertEquals("Display Model not of expected size", 32, displayModel.size());
         Assert.assertEquals("Not the expected no. of statements about object", 13,
                 displayModel.filter(objectUri, null, null).size());
         
         Assert.assertEquals(
                 "Expected 1 hasLeadInstitution statement",
                 1,
                 displayModel
                         .filter(objectUri,
                                 ValueFactoryImpl.getInstance().createURI(
                                         "http://purl.org/podd/ns/poddBase#hasLeadInstitution"), null).size());
         
         Assert.assertEquals(
                 "Unexpected Lead Institution",
                 "CSIRO HRPPC",
                 displayModel
                         .filter(objectUri,
                                 ValueFactoryImpl.getInstance().createURI(
                                         "http://purl.org/podd/ns/poddBase#hasLeadInstitution"), null).objectString());
         
         Assert.assertTrue(
                 "Expected content missing in display model",
                 displayModel.toString().contains(
                         "PODD - Towards An Extensible, Domain-agnostic Scientific Data Management System"));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getObjectLabel(InferredOWLOntologyID, URI, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetObjectLabel() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final String[] objectUris =
                 { "http://purl.org/podd/basic-1-20130206/object:2966",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                         "http://purl.org/podd/ns/poddScience#WildType_NotApplicable", };
         
         final String[] expectedLabels =
                 { "Project#2012-0006_ Cotton Leaf Morphology", "Demo genotype", "Squeekee material", "Not Applicable", };
         final String[] expectedDescriptions = { "Characterising normal and okra leaf shapes", null, null, null };
         
         // test in a loop these PODD objects for their types
         for(int i = 0; i < objectUris.length; i++)
         {
             final URI objectUri = ValueFactoryImpl.getInstance().createURI(objectUris[i]);
             
             final PoddObjectLabel objectLabel =
                     this.testPoddSesameManager.getObjectLabel(ontologyID, objectUri, this.testRepositoryConnection,
                             this.schemaGraph);
             
             // verify:
             Assert.assertNotNull("PoddObjectLabel was null", objectLabel);
             Assert.assertEquals("Incorrect Object URI", objectUri, objectLabel.getObjectURI());
             Assert.assertEquals("Wrong Label", expectedLabels[i], objectLabel.getLabel());
             Assert.assertEquals("Wrong Description", expectedDescriptions[i], objectLabel.getDescription());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getObjectTypeContainsMetadata(URI, RepositoryConnection, URI...)}
      * .
      */
     @Test
     public void testGetObjectTypeContainsMetadata() throws Exception
     {
         // prepare: load schema ontologies
         this.loadSchemaOntologies();
         
         // prepare: the contexts to search in - (load an artifact and get its imported schemas)
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         final Set<URI> directImports =
                 this.testPoddSesameManager.getDirectImports(ontologyID, this.testRepositoryConnection);
         final List<URI> contexts = new ArrayList<URI>(directImports);
         
         // Format: Object Type, expected model size, expected relationship count, expected child
         // object type count
         final Object[][] testData =
                 { { PODD.VF.createURI(PODD.PODD_SCIENCE, "Investigation"), 89, 9, 16 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Material"), 61, 7, 10 }, };
         
         for(final Object[] element : testData)
         {
             final URI objectTypeToTest = (URI)element[0];
             final int expectedModelSize = (int)element[1];
             final int expectedChildRelationshipCount = (int)element[2];
             final int expectedChildObjectTypes = (int)element[3];
             
             final Model model =
                     this.testPoddSesameManager.getObjectTypeContainsMetadata(objectTypeToTest,
                             this.testRepositoryConnection, contexts.toArray(new URI[0]));
             
             if(expectedModelSize != model.size())
             {
                 DebugUtils.printContents(model);
             }
             // verify:
             Assert.assertEquals("Not the expected statement count in Model", expectedModelSize, model.size());
             Assert.assertEquals("Not the expected no. of child relationships", expectedChildRelationshipCount, model
                     .filter(null, OWL.ONPROPERTY, null).objects().size());
             Assert.assertEquals("Not the expected no. of child object types", expectedChildObjectTypes,
                     model.filter(null, OWL.ALLVALUESFROM, null).objects().size());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getObjectTypeMetadata(URI, boolean, MetadataPolicy, RepositoryConnection, URI...)}
      * .
      */
     @Test
     public void testGetObjectTypeMetadata() throws Exception
     {
         // prepare: load schema ontologies
         this.loadSchemaOntologies();
         
         // prepare: the contexts to search in - (load an artifact and get its imported schemas)
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         final Set<URI> directImports =
                 this.testPoddSesameManager.getDirectImports(ontologyID, this.testRepositoryConnection);
         final List<URI> contexts = new ArrayList<URI>(directImports);
         
         // Format: Object Type, includeDoNotDisplayProperties, includeContainsSubProperties,
         // expected model size, expected property count, do-not-display statement count
         final Object[][] testData =
                 {
                         { PODD.VF.createURI(PODD.PODD_BASE, "NoSuchObjectType"), false, MetadataPolicy.INCLUDE_ALL, 0,
                                 -1, 0 },
                         
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Project"), false, MetadataPolicy.INCLUDE_ALL, 156, 18,
                                 0 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Project"), false, MetadataPolicy.EXCLUDE_CONTAINS, 88,
                                 9, 0 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Project"), true, MetadataPolicy.INCLUDE_ALL, 293, 34,
                                 13 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Project"), false, MetadataPolicy.ONLY_CONTAINS, 69, 9,
                                 0 },
                         
                         // cannot "contain" any Child Objects
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Publication"), false, MetadataPolicy.INCLUDE_ALL, 93,
                                 11, 0 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Publication"), true, MetadataPolicy.INCLUDE_ALL, 119,
                                 15, 3 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Publication"), false, MetadataPolicy.ONLY_CONTAINS, 13,
                                 2, 0 },
                         
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Environment"), false, MetadataPolicy.INCLUDE_ALL, 73,
                                 9, 0 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Environment"), true, MetadataPolicy.INCLUDE_ALL, 99,
                                 13, 3 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Environment"), false, MetadataPolicy.ONLY_CONTAINS, 29,
                                 4, 0 },
                         
                         { PODD.VF.createURI(PODD.PODD_PLANT, "FieldConditions"), false, MetadataPolicy.INCLUDE_ALL, 89,
                                 11, 0 },
                         { PODD.VF.createURI(PODD.PODD_PLANT, "FieldConditions"), true, MetadataPolicy.INCLUDE_ALL, 115,
                                 15, 3 },
                         { PODD.VF.createURI(PODD.PODD_PLANT, "FieldConditions"), false, MetadataPolicy.ONLY_CONTAINS,
                                 29, 4, 0 },
                         
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Material"), false, MetadataPolicy.INCLUDE_ALL, 199, 22,
                                 0 },
                 
                 };
         
         for(final Object[] element : testData)
         {
             final URI objectType = (URI)element[0];
             final boolean includeDoNotDisplayProperties = (Boolean)element[1];
             final MetadataPolicy policy = (MetadataPolicy)element[2];
             final int expectedTripleCount = (int)element[3];
             final int expectedPropertyCount = (int)element[4];
             final int expectedNonDisplayablePropertyCount = (int)element[5];
             
             final Model model =
                     this.testPoddSesameManager.getObjectTypeMetadata(objectType, includeDoNotDisplayProperties, policy,
                             this.testRepositoryConnection, contexts.toArray(new URI[0]));
             
             if(expectedTripleCount != model.size())
             {
                 // DebugUtils.printContents(model);
                 Rio.write(model, System.out, RDFFormat.NQUADS);
             }
             
             // verify:
             Assert.assertEquals("Not the expected statement count in Model", expectedTripleCount, model.size());
             Assert.assertEquals("Not the expected no. of properties", expectedPropertyCount,
                     model.filter(objectType, null, null).size() - 1);
             Assert.assertEquals("Not the expected no. of non-displayable properties",
                     expectedNonDisplayablePropertyCount, model.filter(null, PODD.PODD_BASE_DO_NOT_DISPLAY, null).size());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getObjectTypes(InferredOWLOntologyID, URI, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetObjectTypes() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID1 =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final String[] objectUris =
                 { "http://purl.org/podd/basic-1-20130206/object:2966",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#publication45",
                         "mailto:helen.daily@csiro.au", "http://purl.org/podd/ns/poddScience#WildType_NotApplicable", };
         
         final String[] expectedTypes =
                 { "http://purl.org/podd/ns/poddScience#Project", "http://purl.org/podd/ns/poddScience#Genotype",
                         "http://purl.org/podd/ns/poddScience#Material",
                         "http://purl.org/podd/ns/poddScience#Publication", "http://purl.org/podd/ns/poddUser#User",
                         "http://purl.org/podd/ns/poddScience#WildTypeAssertion", };
         
         // test in a loop these PODD objects for their types
         for(int i = 0; i < objectUris.length; i++)
         {
             final URI objectUri = ValueFactoryImpl.getInstance().createURI(objectUris[i]);
             
             final List<URI> objectTypes =
                     this.testPoddSesameManager.getObjectTypes(ontologyID1, objectUri, this.testRepositoryConnection,
                             this.schemaGraph);
             
             // verify:
             Assert.assertNotNull("Type was null", objectTypes);
             Assert.assertFalse("No Type found", objectTypes.isEmpty());
             Assert.assertEquals("Wrong type", ValueFactoryImpl.getInstance().createURI(expectedTypes[i]),
                     objectTypes.get(0));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetOntologiesEmptyAllVersions() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final Collection<InferredOWLOntologyID> ontologies =
                 this.testPoddSesameManager.getOntologies(true, this.testRepositoryConnection, context);
         
         Assert.assertNotNull(ontologies);
         Assert.assertTrue(ontologies.isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetOntologiesEmptyOnlyCurrentVersions() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final Collection<InferredOWLOntologyID> ontologies =
                 this.testPoddSesameManager.getOntologies(true, this.testRepositoryConnection, context);
         
         Assert.assertNotNull(ontologies);
         Assert.assertTrue(ontologies.isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetOntologiesSingleAllVersions() throws Exception
     {
         final URI context = this.populateArtifactManagementGraph();
         
         final Collection<InferredOWLOntologyID> ontologies =
                 this.testPoddSesameManager.getOntologies(false, this.testRepositoryConnection, context);
         
         Assert.assertNotNull(ontologies);
         Assert.assertEquals(1, ontologies.size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetOntologiesSingleOnlyCurrentVersions() throws Exception
     {
         final URI context = this.populateArtifactManagementGraph();
         
         final Collection<InferredOWLOntologyID> ontologies =
                 this.testPoddSesameManager.getOntologies(true, this.testRepositoryConnection, context);
         
         Assert.assertNotNull(ontologies);
         Assert.assertEquals(1, ontologies.size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologyIRI(RepositoryConnection, URI)}.
      */
     @Test
     public void testGetOntologyIRI() throws Exception
     {
         final String resourcePath = TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT;
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
         Assert.assertNotNull("Could not find resource", inputStream);
         
         this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
         
         // invoke method under test:
         final IRI ontologyIRI = this.testPoddSesameManager.getOntologyIRI(this.testRepositoryConnection, context);
         
         // verify:
         Assert.assertNotNull("Ontology IRI was null", ontologyIRI);
         Assert.assertEquals("Wrong Ontology IRI", "urn:temp:uuid:artifact:1", ontologyIRI.toString());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologyVersion(IRI, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetOntologyVersionWithArtifact() throws Exception
     {
         // prepare: create artifact management graph
         final URI artifactGraph = this.populateArtifactManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getOntologyVersion(IRI.create("http://purl.org/podd/99-99/version:1"),
                         this.testRepositoryConnection, artifactGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected version", IRI.create("http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologyVersion(IRI, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetOntologyVersionWithNonCurrentSchema() throws Exception
     {
         // prepare: create artifact management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getOntologyVersion(
                         IRI.create("http://purl.org/podd/ns/version/poddPlant/1"), this.testRepositoryConnection,
                         schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected version", IRI.create("http://purl.org/podd/ns/version/poddPlant/1"),
                 inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddPlant/1"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologyVersion(IRI, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetOntologyVersionWithNonExistentSchema() throws Exception
     {
         // prepare: create artifact management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getOntologyVersion(
                         IRI.create("http://purl.org/podd/ns/version/poddPlant/3"), this.testRepositoryConnection,
                         schemaGraph);
         
         // verify:
         Assert.assertNull("Expected NULL inferredOntologyID", inferredOntologyID);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getParentDetails(URI, RepositoryConnection, URI...)}
      * .
      */
     @Test
     public void testGetParentDetails() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final URI[] contexts =
                 this.testPoddSesameManager.versionAndSchemaContexts(ontologyID, this.testRepositoryConnection,
                         this.schemaGraph);
         
         // object, expected statement count, expected parent
         final Object[][] testData =
                 {
                         { "http://purl.org/podd/basic-1-20130206/object:2966", 0, "" },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#publication45", 1,
                                 "http://purl.org/podd/basic-1-20130206/object:2966" },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype", 1,
                                 "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Material" },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial", 1,
                                 "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Investigation" },
                         { "http://purl.org/podd/ns/poddScience#ANZSRC_NotApplicable", 0, "" }, };
         
         for(final Object[] element : testData)
         {
             final URI objectUri = ValueFactoryImpl.getInstance().createURI(element[0].toString());
             final int expectedStatementCount = (int)element[1];
             
             final Model model =
                     this.testPoddSesameManager.getParentDetails(objectUri, this.testRepositoryConnection, contexts);
             
             Assert.assertEquals("Unexpected no. of statements", expectedStatementCount, model.size());
             if(expectedStatementCount == 1)
             {
                 final URI expectedParent = ValueFactoryImpl.getInstance().createURI(element[2].toString());
                 Assert.assertTrue("Not the expected parent", model.subjects().contains(expectedParent));
             }
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getSchemaVersion(IRI, RepositoryConnection, URI)}
      * . Null is passed in to sesameManager.getSchemaVersion() and a NullPointerException is
      * expected.
      * 
      */
     @Test
     public void testGetSchemaVersionWithNullOntologyIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         try
         {
             this.testPoddSesameManager.getSchemaVersion(null, this.testRepositoryConnection, schemaGraph);
             Assert.fail("Should have thrown a RuntimeException");
             
         }
         catch(final NullPointerException e)
         {
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getSchemaVersion(IRI, RepositoryConnection, URI)}
      * . An ontology IRI is passed in to sesameManager.getSchemaVersion(). The current version's
      * Ontology ID should be returned.
      */
     @Test
     public void testGetSchemaVersionWithOntologyIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID ontologyID =
                 this.testPoddSesameManager.getSchemaVersion(IRI.create("http://purl.org/podd/ns/poddPlant"),
                         this.testRepositoryConnection, schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL OntologyID", ontologyID);
         Assert.assertEquals("Not the expected version", IRI.create("http://purl.org/podd/ns/version/poddPlant/2"),
                 ontologyID.getVersionIRI());
         Assert.assertEquals("Not the expected inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddPlant/2"),
                 ontologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getSchemaVersion(IRI, RepositoryConnection, URI)}
      * . An unknown IRI is passed in to sesameManager.getSchemaVersion() and an
      * UnmanagedSchemaIRIException is expected.
      */
     @Test
     public void testGetSchemaVersionWithUnmanagedVersionIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         final IRI unmanagedSchemaVersionIri = IRI.create("http://purl.org/podd/ns/version/poddPlant/9999");
         
         try
         {
             this.testPoddSesameManager.getSchemaVersion(unmanagedSchemaVersionIri, this.testRepositoryConnection,
                     schemaGraph);
             Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
         }
         catch(final UnmanagedSchemaIRIException e)
         {
             Assert.assertEquals("Not the expected exception", "This IRI does not refer to a managed ontology",
                     e.getMessage());
             Assert.assertEquals(unmanagedSchemaVersionIri, e.getOntologyID());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getSchemaVersion(IRI, RepositoryConnection, URI)}
      * . The current version IRI is passed in to sesameManager.getSchemaVersion() with the aim of
      * getting the Ontology ID of that version in return.
      */
     @Test
     public void testGetSchemaVersionWithVersionIRICurrent() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID ontologyID =
                 this.testPoddSesameManager.getSchemaVersion(IRI.create("http://purl.org/podd/ns/version/poddPlant/2"),
                         this.testRepositoryConnection, schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL ontologyID", ontologyID);
         Assert.assertEquals("Not the expected version", IRI.create("http://purl.org/podd/ns/version/poddPlant/2"),
                 ontologyID.getVersionIRI());
         Assert.assertEquals("Not the expected inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddPlant/2"),
                 ontologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getSchemaVersion(IRI, RepositoryConnection, URI)}
      * . A non-current version IRI is passed in to sesameManager.getSchemaVersion() with the aim of
      * getting the Ontology ID of that version in return.
      */
     @Test
     public void testGetSchemaVersionWithVersionIRINotCurrent() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID ontologyID =
                 this.testPoddSesameManager.getSchemaVersion(IRI.create("http://purl.org/podd/ns/version/poddPlant/1"),
                         this.testRepositoryConnection, schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL ontologyID", ontologyID);
         Assert.assertEquals("Not the expected version", IRI.create("http://purl.org/podd/ns/version/poddPlant/1"),
                 ontologyID.getVersionIRI());
         Assert.assertEquals("Not the expected inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddPlant/1"),
                 ontologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getTopObjectIRI(InferredOWLOntologyID, RepositoryConnection)}
      * .
      */
     @Test
     public void testGetTopObjectIRI() throws Exception
     {
         // prepare: load test artifact
         final InferredOWLOntologyID nextOntologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final URI topObjectList =
                 this.testPoddSesameManager.getTopObjectIRI(nextOntologyID, this.testRepositoryConnection);
         
         Assert.assertEquals("Not the expected top object URI",
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130206/object:2966"),
                 topObjectList);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getTopObjects(InferredOWLOntologyID, RepositoryConnection)}
      * . Test retrieving Top Objects from an artifact with exactly one top object.
      */
     @Test
     public void testGetTopObjectsFromArtifactWithOneTopObject() throws Exception
     {
         // prepare: load test artifact
         final InferredOWLOntologyID nextOntologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         // DebugUtils.printContexts(testRepositoryConnection);
         // DebugUtils.printContents(testRepositoryConnection,
         // nextOntologyID.getVersionIRI().toOpenRDFURI());
         
         final List<URI> topObjectList =
                 this.testPoddSesameManager.getTopObjects(nextOntologyID, this.testRepositoryConnection);
         
         Assert.assertEquals("Expected 1 top object", 1, topObjectList.size());
         Assert.assertEquals("Not the expected top object URI",
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130206/object:2966"),
                 topObjectList.get(0));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getTopObjects(InferredOWLOntologyID, RepositoryConnection)}
      * . Test retrieving Top Objects from an artifact which has more than one top object. A PODD
      * artifact should currently have only 1 top object.
      */
     @Test
     public void testGetTopObjectsFromArtifactWithSeveralTopObjects() throws Exception
     {
         final String testResourcePath = TestConstants.TEST_ARTIFACT_BAD_3_TOP_OBJECTS;
         // prepare: load test artifact
         final InferredOWLOntologyID nextOntologyID =
                 this.loadOntologyFromResource(testResourcePath, null, RDFFormat.TURTLE);
         
         final List<URI> topObjectList =
                 this.testPoddSesameManager.getTopObjects(nextOntologyID, this.testRepositoryConnection);
         
         Assert.assertEquals("Expected 3 top objects", 3, topObjectList.size());
         
         final List<String> expectedUriList =
                 Arrays.asList(new String[] { "http://purl.org/podd/basic-1-20130205/object:2966",
                         "http://purl.org/podd/basic-1-20130205/object:2977",
                         "http://purl.org/podd/basic-1-20130205/object:2988" });
         
         for(final URI topObjectUri : topObjectList)
         {
             Assert.assertTrue("Unexpected top object", expectedUriList.contains(topObjectUri.stringValue()));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getWeightedProperties(InferredOWLOntologyID, URI, RepositoryConnection)}
      * . getWeightedProperties() is invoked for an internal object of an artifact.
      */
     @Test
     public void testGetWeightedPropertiesOfAnInternalObject() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID nextOntologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         Assert.assertEquals("http://purl.org/podd/basic-2-20130206/artifact:1", nextOntologyID.getOntologyIRI()
                 .toString());
         Assert.assertEquals("http://purl.org/podd/basic-2-20130206/artifact:version:1", nextOntologyID.getVersionIRI()
                 .toString());
         Assert.assertEquals(
                 "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/basic-2-20130206/artifact:1:version:1",
                 nextOntologyID.getInferredOntologyIRI().toString());
         
         final URI internalObjectUri =
                 ValueFactoryImpl.getInstance().createURI(
                         "http://purl.org/podd/basic-2-20130206/artifact:1#publication45");
         
         final URI[] contexts =
                 this.testPoddSesameManager.versionAndSchemaContexts(nextOntologyID, this.testRepositoryConnection,
                         this.schemaGraph);
         final List<URI> orderedPropertyUris =
                 this.testPoddSesameManager.getWeightedProperties(internalObjectUri, false,
                         this.testRepositoryConnection, contexts);
         
         // verify:
         Assert.assertEquals("Incorrect number of statements about Internal Object", 4, orderedPropertyUris.size());
         
         final String[] expectedUris =
                 { "http://purl.org/podd/ns/poddScience#hasAbstract", "http://purl.org/podd/ns/poddScience#publishedIn",
                         "http://purl.org/podd/ns/poddScience#hasYear", "http://purl.org/dc/terms/creator", };
         for(int i = 0; i < orderedPropertyUris.size(); i++)
         {
             Assert.assertEquals("Property URI not in expected position",
                     ValueFactoryImpl.getInstance().createURI(expectedUris[i]), orderedPropertyUris.get(i));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getWeightedProperties(InferredOWLOntologyID, URI, RepositoryConnection)}
      * .
      * 
      * getWeightedProperties() is invoked for the top object of an artifact.
      */
     @Test
     public void testGetWeightedPropertiesOfATopObject() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID nextOntologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final URI topObjectUri =
                 this.testPoddSesameManager.getTopObjectIRI(nextOntologyID, this.testRepositoryConnection);
         
         final URI[] contexts =
                 this.testPoddSesameManager.versionAndSchemaContexts(nextOntologyID, this.testRepositoryConnection,
                         this.schemaGraph);
         final List<URI> orderedPropertyUris =
                 this.testPoddSesameManager.getWeightedProperties(topObjectUri, false, this.testRepositoryConnection,
                         contexts);
         
         // verify:
         Assert.assertEquals("Incorrect number of statements about Top Object", 10, orderedPropertyUris.size());
         
         final String[] expectedUris =
                 { "http://purl.org/podd/ns/poddBase#hasLeadInstitution",
                         "http://purl.org/podd/ns/poddBase#hasStartDate",
                         "http://purl.org/podd/ns/poddScience#hasAnalysis",
                         "http://purl.org/podd/ns/poddScience#hasInvestigation",
                         "http://purl.org/podd/ns/poddScience#hasProcess",
                         "http://purl.org/podd/ns/poddScience#hasProjectPlan",
                         "http://purl.org/podd/ns/poddScience#hasPublication",
                         "http://purl.org/podd/ns/poddScience#hasANZSRC", "http://purl.org/podd/ns/poddBase#createdAt",
                         "http://purl.org/dc/terms/creator", };
         for(int i = 0; i < orderedPropertyUris.size(); i++)
         {
             Assert.assertEquals("Property URI not in expected position",
                     ValueFactoryImpl.getInstance().createURI(expectedUris[i]), orderedPropertyUris.get(i));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getWeightedProperties(InferredOWLOntologyID, URI, boolean, RepositoryConnection)}
      * .
      * 
      * getWeightedProperties() is invoked for the top object of an artifact with
      * <i>excludeContainsProperties</i> set to true.
      * 
      */
     @Test
     public void testGetWeightedPropertiesOfATopObjectWithoutContainsProperties() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID nextOntologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final URI topObjectUri =
                 this.testPoddSesameManager.getTopObjectIRI(nextOntologyID, this.testRepositoryConnection);
         
         final URI[] contexts =
                 this.testPoddSesameManager.versionAndSchemaContexts(nextOntologyID, this.testRepositoryConnection,
                         this.schemaGraph);
         final List<URI> orderedPropertyUris =
                 this.testPoddSesameManager.getWeightedProperties(topObjectUri, true, this.testRepositoryConnection,
                         contexts);
         
         // verify:
         Assert.assertEquals("Incorrect number of statements about Top Object", 5, orderedPropertyUris.size());
         
         final String[] expectedUris =
                 { "http://purl.org/podd/ns/poddBase#hasLeadInstitution",
                         "http://purl.org/podd/ns/poddBase#hasStartDate",
                         "http://purl.org/podd/ns/poddScience#hasANZSRC", "http://purl.org/podd/ns/poddBase#createdAt",
                         "http://purl.org/dc/terms/creator", };
         for(int i = 0; i < orderedPropertyUris.size(); i++)
         {
             Assert.assertEquals("Property URI not in expected position",
                     ValueFactoryImpl.getInstance().createURI(expectedUris[i]), orderedPropertyUris.get(i));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Test
     public void testIsPublishedWithEmptyOntology() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         try
         {
             this.testPoddSesameManager.isPublished(null, this.testRepositoryConnection, context);
             Assert.fail("Should have thrown a NullPointerException");
         }
         catch(final NullPointerException e)
         {
             Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Test
     public void testIsPublishedWithNullOntology() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         try
         {
             this.testPoddSesameManager.isPublished(null, this.testRepositoryConnection, context);
             Assert.fail("Should have thrown a NullPointerException");
         }
         catch(final NullPointerException e)
         {
             Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
      * . This test depends on imported PODD Base ontology being resolvable from
      * http://purl.org/podd/ns/poddBase.
      */
     @Test
     public void testIsPublishedWithPublishedArtifact() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final String testResourcePath = TestConstants.TEST_ARTIFACT_BASIC_PROJECT_PUBLISHED;
         final URI versionUri = ValueFactoryImpl.getInstance().createURI("urn:temp:uuid:artifact:version:55");
         
         final boolean isPublished = this.internalTestIsPublished(true, testResourcePath, 21, versionUri, context);
         Assert.assertEquals("Did not identify artifact as Published", true, isPublished);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
      * . This test depends on imported PODD Base ontology being resolvable from
      * http://purl.org/podd/ns/poddBase.
      */
     @Test
     public void testIsPublishedWithUnPublishedArtifact() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final String testResourcePath = TestConstants.TEST_ARTIFACT_BASIC_PROJECT_1;
         final URI versionUri = ValueFactoryImpl.getInstance().createURI("urn:temp:artifact:version:1");
         final boolean isPublished =
                 this.internalTestIsPublished(false, testResourcePath,
                         TestConstants.TEST_ARTIFACT_BASIC_PROJECT_1_CONCRETE_TRIPLES, versionUri, context);
         Assert.assertEquals("Did not identify artifact as Not Published", false, isPublished);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#searchOntologyLabels(String, InferredOWLOntologyID, int, int, RepositoryConnection, URI...)}
      */
     @Test
     public void testSearchOntologyLabelsForPlatforms() throws Exception
     {
         // prepare:
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final String searchTerm = "ME";
         final URI[] searchTypes =
                 { PODD.VF.createURI(PODD.PODD_SCIENCE, "Platform"), PODD.VF.createURI(OWL.NAMESPACE, "NamedIndividual") };
         final URI[] contexts =
                 this.testPoddSesameManager.versionAndSchemaContexts(ontologyID, this.testRepositoryConnection,
                         this.schemaGraph);
         final Model result =
                 this.testPoddSesameManager.searchOntologyLabels(searchTerm, searchTypes, 1000, 0,
                         this.testRepositoryConnection, contexts);
         
         // verify:
         Assert.assertNotNull("NULL result", result);
         
         DebugUtils.printContents(result);
         
         Assert.assertEquals("Not the expected number of search results", 9, result.size());
         
         Assert.assertEquals("Expected Platform SPAD Meter not found", 1,
                 result.filter(null, null, PODD.VF.createLiteral("SPAD Meter")).size());
         Assert.assertEquals("Expected Platform Pyrometer not found", 1,
                 result.filter(null, null, PODD.VF.createLiteral("Pyrometer")).size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#searchOntologyLabels(String, InferredOWLOntologyID, int, int, RepositoryConnection, URI...)}
      */
     @Test
     public void testSearchOntologyLabelsOther() throws Exception
     {
         // prepare:
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         final String searchTerm = "";
         final URI[] searchTypes =
                 { PODD.VF.createURI(PODD.PODD_SCIENCE, "WildTypeAssertion"),
                         PODD.VF.createURI(OWL.NAMESPACE, "NamedIndividual") };
         final URI[] contexts =
                 this.testPoddSesameManager.versionAndSchemaContexts(ontologyID, this.testRepositoryConnection,
                         this.schemaGraph);
         final Model result =
                 this.testPoddSesameManager.searchOntologyLabels(searchTerm, searchTypes, 1000, 0,
                         this.testRepositoryConnection, contexts);
         
         // verify:
         Assert.assertNotNull("NULL result", result);
         Assert.assertEquals("Not the expected number of search results", 4, result.size());
         
         Assert.assertEquals("Expected Literal 'Not Applicable' not found", 1,
                 result.filter(null, null, PODD.VF.createLiteral("Not Applicable")).size());
         Assert.assertEquals("Expected Literal 'No' not found", 1, result
                 .filter(null, null, PODD.VF.createLiteral("No")).size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateCurrentManagedSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      */
     @Test
     public final void testUpdateCurrentManagedSchemaOntologyVersionWithoutUpdate() throws Exception
     {
         final IRI pOntologyIRI = IRI.create("http://purl.org/podd/ns/poddBase");
         final IRI pVersionIRI = IRI.create("http://purl.org/podd/ns/version/poddBase/1");
         final IRI pInferredVersionIRI = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1");
         final InferredOWLOntologyID nextOntologyID =
                 new InferredOWLOntologyID(pOntologyIRI, pVersionIRI, pInferredVersionIRI);
         
         // invoke method under test
         this.testPoddSesameManager.updateManagedSchemaOntologyVersion(nextOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         
         this.verifyManagementGraphContents(6, this.schemaGraph, pOntologyIRI, pVersionIRI, pInferredVersionIRI);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateCurrentManagedSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      */
     @Test
     public final void testUpdateCurrentManagedSchemaOntologyVersionWithUpdate() throws Exception
     {
         final IRI pOntologyIRI = IRI.create("http://purl.org/podd/ns/poddBase");
         final IRI pVersionIRI = IRI.create("http://purl.org/podd/ns/version/poddBase/1");
         final IRI pInferredVersionIRI = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1");
         final InferredOWLOntologyID nextOntologyID =
                 new InferredOWLOntologyID(pOntologyIRI, pVersionIRI, pInferredVersionIRI);
         
         // first setting of schema versions in mgt graph
         this.testPoddSesameManager.updateManagedSchemaOntologyVersion(nextOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         this.verifyManagementGraphContents(6, this.schemaGraph, pOntologyIRI, pVersionIRI, pInferredVersionIRI);
         
         final IRI pVersionIRIUpdated = IRI.create("http://purl.org/podd/ns/version/poddBase/4");
         final IRI pInferredVersionIRIUpdated = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/5");
         final InferredOWLOntologyID nextOntologyIDUpdated =
                 new InferredOWLOntologyID(pOntologyIRI, pVersionIRIUpdated, pInferredVersionIRIUpdated);
         
         // invoke with "updateCurrent" disallowed
         this.testPoddSesameManager.updateManagedSchemaOntologyVersion(nextOntologyIDUpdated, false,
                 this.testRepositoryConnection, this.schemaGraph);
         
         // verify only inferred ontology version is updated
         this.verifyManagementGraphContents(9, this.schemaGraph, pOntologyIRI, pVersionIRI, pInferredVersionIRIUpdated);
         
         // invoke with "updateCurrent" allowed
         this.testPoddSesameManager.updateManagedSchemaOntologyVersion(nextOntologyIDUpdated, true,
                 this.testRepositoryConnection, this.schemaGraph);
         
         // verify both ontology current version and inferred ontology version haven been updated
         this.verifyManagementGraphContents(9, this.schemaGraph, pOntologyIRI, pVersionIRIUpdated,
                 pInferredVersionIRIUpdated);
         
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      * 
      * Tests that when updating an artifact version, repository content for previous versions of the
      * artifact (both asserted and inferred statements) are deleted.
      * 
      */
     @Test
     public final void testUpdateManagedPoddArtifactVersionForDeletingPreviousVersionContent() throws Exception
     {
         // prepare: add entries in the artifact graph for a test artifact
         final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
         final IRI pVersionIRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
         final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
         final InferredOWLOntologyID nextOntologyIDv1 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         // prepare: add dummy statements in relevant contexts to represent test artifact
         final URI subject = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
         this.testRepositoryConnection.add(subject, PODD.PODD_BASE_HAS_PUBLICATION_STATUS, PODD.PODD_BASE_NOT_PUBLISHED,
                 pVersionIRIv1.toOpenRDFURI());
         
         final URI inferredSubject = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
         this.testRepositoryConnection.add(inferredSubject, PODD.PODD_BASE_HAS_PUBLICATION_STATUS,
                 PODD.PODD_BASE_NOT_PUBLISHED, pInferredVersionIRIv1.toOpenRDFURI());
         
         // verify: contexts populated for test artifact
         Assert.assertEquals("Asserted graph should have 1 statement", 1,
                 this.testRepositoryConnection.size(pVersionIRIv1.toOpenRDFURI()));
         Assert.assertEquals("Inferred graph should have 1 statement", 1,
                 this.testRepositoryConnection.size(pInferredVersionIRIv1.toOpenRDFURI()));
         
         // invoke method under test
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                 this.testRepositoryConnection, this.artifactGraph);
         
         // verify: artifact management graph
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         // prepare: version 2 of test artifact
         final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
         final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
         final InferredOWLOntologyID nextOntologyIDv2 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
         
         // prepare: add dummy statements in relevant contexts for version 2 of test artifact
         final URI subject2 = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
         this.testRepositoryConnection.add(subject2, PODD.PODD_BASE_HAS_PUBLICATION_STATUS,
                 PODD.PODD_BASE_NOT_PUBLISHED, pVersionIRIv2.toOpenRDFURI());
         
         final URI inferredSubject2 = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
         this.testRepositoryConnection.add(inferredSubject2, PODD.PODD_BASE_HAS_PUBLICATION_STATUS,
                 PODD.PODD_BASE_NOT_PUBLISHED, pInferredVersionIRIv2.toOpenRDFURI());
         
         // verify: contexts populated for test artifact
         Assert.assertEquals("Asserted graph should have 1 statement", 1,
                 this.testRepositoryConnection.size(pVersionIRIv2.toOpenRDFURI()));
         Assert.assertEquals("Inferred graph should have 1 statement", 1,
                 this.testRepositoryConnection.size(pInferredVersionIRIv2.toOpenRDFURI()));
         
         // invoke method under test
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, true,
                 this.testRepositoryConnection, this.artifactGraph);
         
         if(this.log.isDebugEnabled())
         {
             DebugUtils.printContexts(this.testRepositoryConnection);
             DebugUtils.printContents(this.testRepositoryConnection, this.artifactGraph);
         }
         
         // verify:
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
         
         DebugUtils.printContents(this.testRepositoryConnection, pInferredVersionIRIv1.toOpenRDFURI());
         
         Assert.assertEquals("Old asserted graph should be deleted", 0,
                 this.testRepositoryConnection.size(pVersionIRIv1.toOpenRDFURI()));
         Assert.assertEquals("Old inferred graph should be deleted", 0,
                 this.testRepositoryConnection.size(pInferredVersionIRIv1.toOpenRDFURI()));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      * 
      * Details of an existing artifact are updated in the management graph.
      */
     @Test
     public final void testUpdateManagedPoddArtifactVersionWithExistingArtifact() throws Exception
     {
         // prepare: add entries in the artifact graph for a test artifact
         final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
         final IRI pVersion1IRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
         final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
         final InferredOWLOntologyID nextOntologyIDv1 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersion1IRIv1, pInferredVersionIRIv1);
         
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                 this.testRepositoryConnection, this.artifactGraph);
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersion1IRIv1, pInferredVersionIRIv1);
         
         // prepare: update artifact version
         final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
         final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
         final InferredOWLOntologyID nextOntologyIDv2 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
         
         // invoke method under test
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, true,
                 this.testRepositoryConnection, this.artifactGraph);
         
         // verify: new version overwrites all references to the old version, and number of
         // statements stays the same
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      * 
      * Details of a new artifact are added to the management graph.
      */
     @Test
     public final void testUpdateManagedPoddArtifactVersionWithNewArtifact() throws Exception
     {
         // prepare: add entries in the artifact graph for a test artifact
         final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
         final IRI pVersionIRI = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
         final IRI pInferredVersionIRI = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
         final InferredOWLOntologyID nextOntologyID =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRI, pInferredVersionIRI);
         
         // invoke method under test
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyID, false,
                 this.testRepositoryConnection, this.artifactGraph);
         
         // verify:
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRI, pInferredVersionIRI);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      * 
      * Details of an existing artifact are updated in the management graph, with "updateCurrent" =
      * false. The "current version" does not change for base/asserted ontology while the current
      * inferred version is updated.
      */
     @Test
     public final void testUpdateManagedPoddArtifactVersionWithoutUpdateCurrent() throws Exception
     {
         // prepare: add entries in the artifact graph for a test artifact
         final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
         final IRI pVersionIRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
         final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
         final InferredOWLOntologyID nextOntologyIDv1 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                 this.testRepositoryConnection, this.artifactGraph);
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         // prepare: version 2
         final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
         final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
         final InferredOWLOntologyID nextOntologyIDv2 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
         
         // invoke with "updateCurrent" disallowed
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, false,
                 this.testRepositoryConnection, this.artifactGraph);
         
         // verify:
         this.verifyManagementGraphContents(11, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      */
     @Test
     public final void testUpdateManagedPoddArtifactVersionWithUpdate() throws Exception
     {
         // prepare: add entries in the artifact graph for a test artifact
         final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
         final IRI pVersionIRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
         final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
         final InferredOWLOntologyID nextOntologyIDv1 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                 this.testRepositoryConnection, this.artifactGraph);
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         // prepare: version 2
         final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
         final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
         final InferredOWLOntologyID nextOntologyIDv2 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
         
         // invoke with "updateCurrent" disallowed
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, false,
                 this.testRepositoryConnection, this.artifactGraph);
         
         if(this.log.isDebugEnabled())
         {
             DebugUtils.printContexts(this.testRepositoryConnection);
             DebugUtils.printContents(this.testRepositoryConnection, this.artifactGraph);
         }
         
         // verify: version 2 is inserted, as verified by the extra statements, but the current
         // versions are not modified this time
         this.verifyManagementGraphContents(11, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         // update to version 3
         final IRI pVersionIRIv3 = IRI.create("http://purl.org/abc-def/artifact:1:version:3");
         final IRI pInferredVersionIRIv3 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:3");
         final InferredOWLOntologyID nextOntologyIDv3 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv3, pInferredVersionIRIv3);
         
         // invoke with "updateCurrent" allowed
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv3, true,
                 this.testRepositoryConnection, this.artifactGraph);
         
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv3, pInferredVersionIRIv3);
     }
     
     @Test
     public void testVersionContexts() throws Exception
     {
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         URI[] versionAndSchemaContexts = this.testPoddSesameManager.versionContexts(ontologyID);
         
         Assert.assertNotNull(versionAndSchemaContexts);
         for(URI nextUri : versionAndSchemaContexts)
         {
             Assert.assertNotNull(nextUri);
         }
         Assert.assertEquals(1, versionAndSchemaContexts.length);
     }
     
     @Test
     public void testVersionAndInferredContexts() throws Exception
     {
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         URI[] versionAndSchemaContexts = this.testPoddSesameManager.versionAndInferredContexts(ontologyID);
         
         Assert.assertNotNull(versionAndSchemaContexts);
         for(URI nextUri : versionAndSchemaContexts)
         {
             Assert.assertNotNull(nextUri);
         }
         Assert.assertEquals(2, versionAndSchemaContexts.length);
     }
     
     @Test
     public void testVersionAndSchemaContexts() throws Exception
     {
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         URI[] versionAndSchemaContexts =
                 this.testPoddSesameManager.versionAndSchemaContexts(ontologyID, this.testRepositoryConnection,
                         this.schemaGraph);
         
         Assert.assertNotNull(versionAndSchemaContexts);
         for(URI nextUri : versionAndSchemaContexts)
         {
             Assert.assertNotNull(nextUri);
         }
         Assert.assertEquals(6, versionAndSchemaContexts.length);
     }
     
     @Test
     public void testVersionAndInferredAndSchemaContexts() throws Exception
     {
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TestConstants.TEST_ARTIFACT_20130206,
                         TestConstants.TEST_ARTIFACT_20130206_INFERRED, RDFFormat.TURTLE);
         
         URI[] versionAndSchemaContexts =
                 this.testPoddSesameManager.versionAndInferredAndSchemaContexts(ontologyID,
                         this.testRepositoryConnection, this.schemaGraph);
         
         Assert.assertNotNull(versionAndSchemaContexts);
         for(URI nextUri : versionAndSchemaContexts)
         {
             Assert.assertNotNull(nextUri);
         }
         Assert.assertEquals(7, versionAndSchemaContexts.length);
     }
     
     /**
      * Helper method to verify the contents of a management graph
      * 
      * @param graphSize
      *            Expected size of the management graph
      * @param testGraph
      *            The management context/graph
      * @param ontologyIRI
      *            Ontology/artifact IRI to check against
      * @param expectedVersionIRI
      *            Expected current version IRI of the given ontology/artifact
      * @param expectedInferredVersionIRI
      *            Expected inferred version of the given ontology/artifact
      * @throws Exception
      */
     private void verifyManagementGraphContents(final int graphSize, final URI testGraph, final IRI ontologyIRI,
             final IRI expectedVersionIRI, final IRI expectedInferredVersionIRI) throws Exception
     {
         Assert.assertEquals("Graph not of expected size", graphSize, this.testRepositoryConnection.size(testGraph));
         
         final Model stmtList =
                 new LinkedHashModel(Iterations.asList(this.testRepositoryConnection.getStatements(null,
                         PODD.OMV_CURRENT_VERSION, null, false, testGraph)));
         Assert.assertEquals("Graph should have one OMV_CURRENT_VERSION statement", 1, stmtList.size());
         Assert.assertEquals("Wrong ontology IRI", ontologyIRI.toOpenRDFURI(), stmtList.subjects().iterator().next());
         Assert.assertEquals("Wrong version IRI", expectedVersionIRI.toOpenRDFURI(), stmtList.objectURI());
         
         final Model inferredVersionStatementList =
                 new LinkedHashModel(Iterations.asList(this.testRepositoryConnection.getStatements(null,
                         PODD.PODD_BASE_CURRENT_INFERRED_VERSION, null, false, testGraph)));
         Assert.assertEquals("Graph should have one CURRENT_INFERRED_VERSION statement", 1,
                 inferredVersionStatementList.size());
         Assert.assertEquals("Wrong ontology IRI", ontologyIRI.toOpenRDFURI(), inferredVersionStatementList.subjects()
                 .iterator().next());
         Assert.assertEquals("Wrong version IRI", expectedInferredVersionIRI.toOpenRDFURI(),
                 inferredVersionStatementList.objectURI());
     }
 }
