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
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.StandardCharsets;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 import org.junit.rules.Timeout;
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.RDFParser;
 import org.openrdf.rio.RDFWriter;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.UnsupportedRDFormatException;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.openrdf.sail.memory.MemoryStore;
 import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
 import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
 import org.semanticweb.owlapi.io.StreamDocumentSource;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.DanglingObjectPolicy;
 import com.github.podd.api.DataReferenceVerificationPolicy;
 import com.github.podd.api.MetadataPolicy;
 import com.github.podd.api.PoddArtifactManager;
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.api.PoddSchemaManager;
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.api.UpdatePolicy;
 import com.github.podd.api.file.DataReferenceManager;
 import com.github.podd.api.file.DataReferenceProcessorFactory;
 import com.github.podd.api.file.DataReferenceProcessorRegistry;
 import com.github.podd.api.purl.PoddPurlManager;
 import com.github.podd.api.purl.PoddPurlProcessorFactory;
 import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
 import com.github.podd.exception.ArtifactModifyException;
 import com.github.podd.exception.DeleteArtifactException;
 import com.github.podd.exception.DisconnectedObjectException;
 import com.github.podd.exception.DuplicateArtifactIRIException;
 import com.github.podd.exception.EmptyOntologyException;
 import com.github.podd.exception.InconsistentOntologyException;
 import com.github.podd.exception.OntologyNotInProfileException;
 import com.github.podd.exception.PoddException;
 import com.github.podd.exception.PublishedArtifactModifyException;
 import com.github.podd.exception.SchemaManifestException;
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.exception.UnmanagedArtifactVersionException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.utils.DebugUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.OntologyUtils;
 import com.github.podd.utils.PODD;
 import com.github.podd.utils.PoddObjectLabel;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public abstract class AbstractPoddArtifactManagerTest
 {
     /**
      * All of the unit tests individually timeout after 600 seconds.
      */
     @Rule
     public Timeout timeout = new Timeout(600000);
     
     @Rule
     public TemporaryFolder tempDir = new TemporaryFolder();
     
     protected Logger log = LoggerFactory.getLogger(this.getClass());
     
     protected PoddArtifactManager testArtifactManager;
     private PoddRepositoryManager testRepositoryManager;
     private PoddSchemaManager testSchemaManager;
     protected PoddSesameManager testSesameManager;
     
     protected RepositoryConnection testManagementConnection;
     
     protected URI schemaGraph;
     
     private URI artifactGraph;
     
     /**
      * Write contents of specified context to a file
      * 
      * @param context
      * @param filename
      * @param writeFormat
      * @throws IOException
      * @throws OpenRDFException
      */
     public void dumpRdfToFile(final URI context, final String filename, final RDFFormat writeFormat)
         throws IOException, OpenRDFException
     {
         final String outFilename = filename + "." + writeFormat.getFileExtensions().get(0);
         
         final RDFWriter writer = Rio.createWriter(writeFormat, new FileOutputStream(filename));
         writer.handleNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
         writer.handleNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
         writer.handleNamespace("owl", "http://www.w3.org/2002/07/owl#");
         writer.handleNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
         writer.handleNamespace("xml", "http://www.w3.org/XML/1998/namespace");
         
         writer.handleNamespace("dc", "http://purl.org/podd/ns/dcTerms#");
         
         writer.startRDF();
         
         final List<Statement> inferredList =
                 Iterations.asList(this.testManagementConnection.getStatements(null, null, null, false, context));
         for(final Statement s : inferredList)
         {
             writer.handleStatement(s);
         }
         writer.endRDF();
         this.log.info("Wrote {} statements to file {}", inferredList.size(), outFilename);
     }
     
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
     protected abstract DataReferenceManager getNewFileReferenceManager();
     
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
     protected abstract DataReferenceProcessorFactory getNewHttpFileReferenceProcessorFactory();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of {@link PoddOWLManager}
      * .
      * 
      * @param reasonerFactory
      *            TODO
      * 
      * @return A new empty instance of an implementation of PoddOWLManager.
      */
     protected abstract PoddOWLManager getNewOWLManager(OWLOntologyManagerFactory manager,
             OWLReasonerFactory reasonerFactory);
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * {@link PoddPurlManager}.
      * 
      * @return A new empty instance of an implementation of PoddPurlManager.
      */
     protected abstract PoddPurlManager getNewPurlManager();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * {@link OWLOntologyManagerFactory} that can be used with the {@link PoddOWLManager}.
      * 
      * @return A new empty instance of an implementation of {@link OWLOntologyManagerFactory}.
      */
     protected abstract OWLOntologyManagerFactory getNewOWLOntologyManagerFactory();
     
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
      * @throws Exception
      *             If there were problems creating or initialising the Repository.
      */
     protected abstract PoddRepositoryManager getNewRepositoryManager() throws Exception;
     
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
      * {@link DataReferenceProcessorFactory} that can process SSH-based file references for each
      * invocation.
      * 
      * @return A new empty instance of an implementation of PoddFileReferenceProcessorFactory that
      *         can process SSH-based file references.
      */
     protected abstract DataReferenceProcessorFactory getNewSSHFileReferenceProcessorFactory();
     
     /**
      * Concrete tests must override this to provide a new, empty, instance of
      * {@link PoddPurlProcessorFactory} that can process UUID references for each invocation.
      * 
      * @return A new empty instance of an implementation of PoddPurlProcessorFactory that can
      *         process UUID references.
      */
     protected abstract PoddPurlProcessorFactory getNewUUIDPurlProcessorFactory();
     
     private final void internalTestExportObjectMetadata(final InferredOWLOntologyID artifactID) throws Exception
     {
         
         // Format: Object Type, includeDoNotDisplayProperties, includeContainsSubProperties,
         // expected model size, expected property count, do-not-display statement count
         final Object[][] testData =
                 {
                         { PODD.VF.createURI(PODD.PODD_BASE, "NoSuchObjectType"), false, MetadataPolicy.INCLUDE_ALL, 0,
                                 0, 0 },
                         
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Project"), false, MetadataPolicy.INCLUDE_ALL, 156, 19,
                                 0 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Project"), false, MetadataPolicy.EXCLUDE_CONTAINS, 88,
                                 10, 0 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Project"), true, MetadataPolicy.INCLUDE_ALL, 293, 35,
                                 13 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Project"), false, MetadataPolicy.ONLY_CONTAINS, 63, 11,
                                 0 },
                         
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Publication"), false, MetadataPolicy.INCLUDE_ALL, 93,
                                 12, 0 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Publication"), true, MetadataPolicy.INCLUDE_ALL, 119,
                                 16, 3 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Publication"), false, MetadataPolicy.ONLY_CONTAINS, 21,
                                 4, 0 },
                         
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Environment"), false, MetadataPolicy.INCLUDE_ALL, 73,
                                 10, 0 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Environment"), true, MetadataPolicy.INCLUDE_ALL, 99,
                                 14, 3 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Environment"), false, MetadataPolicy.ONLY_CONTAINS, 33,
                                 6, 0 },
                         
                         // to expose issue #96 - add child Process has no fields
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Process"), false, MetadataPolicy.INCLUDE_ALL, 38, 6, 0 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Process"), true, MetadataPolicy.INCLUDE_ALL, 64, 10, 3 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Process"), false, MetadataPolicy.ONLY_CONTAINS, 27, 5,
                                 0 },
                         { PODD.VF.createURI(PODD.PODD_SCIENCE, "Process"), false, MetadataPolicy.EXCLUDE_CONTAINS, 18,
                                 3, 0 },
                         
                         { PODD.VF.createURI(PODD.PODD_PLANT, "FieldConditions"), false, MetadataPolicy.INCLUDE_ALL, 89,
                                 12, 0 },
                         { PODD.VF.createURI(PODD.PODD_PLANT, "FieldConditions"), true, MetadataPolicy.INCLUDE_ALL, 115,
                                 16, 3 },
                         { PODD.VF.createURI(PODD.PODD_PLANT, "FieldConditions"), false, MetadataPolicy.ONLY_CONTAINS,
                                 33, 6, 0 }, };
         
         for(final Object[] element : testData)
         {
             final URI objectType = (URI)element[0];
             final boolean includeDoNotDisplayProperties = (Boolean)element[1];
             
             final MetadataPolicy containsPropertyPolicy = (MetadataPolicy)element[2];
             final int expectedTripleCount = (int)element[3];
             final int expectedPropertyCount = (int)element[4];
             final int expectedNonDisplayablePropertyCount = (int)element[5];
             
             final ByteArrayOutputStream output = new ByteArrayOutputStream();
             
             this.testArtifactManager.exportObjectMetadata(objectType, output, RDFFormat.TURTLE,
                     includeDoNotDisplayProperties, containsPropertyPolicy, artifactID);
             
             // parse output into a Model
             final Model model = Rio.parse(new ByteArrayInputStream(output.toByteArray()), "", RDFFormat.TURTLE);
             
             if(expectedTripleCount != model.size())
             {
                 DebugUtils.printContents(model);
             }
             
             // verify:
             Assert.assertEquals("Not the expected statement count in Model", expectedTripleCount, model.size());
             Assert.assertEquals("Not the expected no. of properties", expectedPropertyCount,
                     model.filter(objectType, null, null).size());
             Assert.assertEquals("Not the expected no. of non-displayable properties",
                     expectedNonDisplayablePropertyCount, model.filter(null, PODD.PODD_BASE_DO_NOT_DISPLAY, null).size());
         }
     }
     
     /**
      * Internal helper method to carry out invoking updateArtifact()
      * 
      * @param resourcePath
      * @param resourceFormat
      * @param mgtGraphSize
      * @param assertedStatementCount
      * @param inferredStatementCount
      * @param isPublished
      * @param fragmentPath
      * @param fragmentFormat
      * @param updateObjectUris
      * @return
      * @throws Exception
      */
     private InferredOWLOntologyID internalTestUpdateArtifact(final String resourcePath, final RDFFormat resourceFormat,
             final int mgtGraphSize, final long assertedStatementCount, final long inferredStatementCount,
             final boolean isPublished, final String fragmentPath, final RDFFormat fragmentFormat,
             final UpdatePolicy updatePolicy, final DanglingObjectPolicy danglingObjectPolicy,
             final DataReferenceVerificationPolicy verifyFileReferences, final Collection<URI> updateObjectUris)
         throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
         
         final InferredOWLOntologyID artifactId = this.testArtifactManager.loadArtifact(inputStream, resourceFormat);
         this.verifyLoadedArtifact(artifactId, mgtGraphSize, assertedStatementCount, inferredStatementCount, isPublished);
         
         final InputStream editInputStream = this.getClass().getResourceAsStream(fragmentPath);
         final Model model =
                 this.testArtifactManager.updateArtifact(artifactId.getOntologyIRI().toOpenRDFURI(), artifactId
                         .getVersionIRI().toOpenRDFURI(), updateObjectUris, editInputStream, fragmentFormat,
                         updatePolicy, danglingObjectPolicy, verifyFileReferences);
         return OntologyUtils.modelToOntologyIDs(model).get(0);
     }
     
     /**
      * Helper method which loads, infers and stores a given ontology using the PoddOWLManager.
      * 
      * @param inputStream
      * @param format
      * @param assertedStatementCount
      * @param inferredStatementCount
      * @param repositoryConnection
      *            TODO
      * @param dependentSchemaOntologies
      * @return
      * @throws Exception
      */
     private InferredOWLOntologyID loadInferStoreSchema(final InputStream inputStream, final RDFFormat format,
             final long assertedStatementCount, final long inferredStatementCount,
             final RepositoryConnection repositoryConnection, Set<? extends OWLOntologyID> dependentSchemaOntologies)
         throws Exception
     {
         // load ontology to OWLManager
         final OWLOntologyDocumentSource owlSource =
                 new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                         format.getDefaultMIMEType()));
         
         final InferredOWLOntologyID inferredOntologyID =
                 this.testArtifactManager.getOWLManager().loadAndInfer(owlSource, repositoryConnection, null,
                         dependentSchemaOntologies, repositoryConnection, this.schemaGraph);
         
         this.testSesameManager.updateManagedSchemaOntologyVersion(inferredOntologyID, true, repositoryConnection,
                 this.schemaGraph);
         
         // verify statement counts
         final URI versionURI = inferredOntologyID.getVersionIRI().toOpenRDFURI();
         
         final long assertedSize = repositoryConnection.size(versionURI);
         // Assert.assertEquals("Wrong statement count", assertedStatementCount, assertedSize);
         if(assertedStatementCount != assertedSize)
         {
             this.log.warn("Wrong asserted statement count: expected={} actual={} ontology={}", assertedStatementCount,
                     assertedSize, inferredOntologyID);
         }
         
         final URI inferredOntologyURI = inferredOntologyID.getInferredOntologyIRI().toOpenRDFURI();
         final long inferredSize = repositoryConnection.size(inferredOntologyURI);
         // Assert.assertEquals("Wrong inferred statement count", inferredStatementCount,
         // inferredSize);
         if(inferredStatementCount != inferredSize)
         {
             this.log.warn("Wrong inferred statement count: expected={} actual={} ontology={}", inferredStatementCount,
                     inferredSize, inferredOntologyID);
         }
         
         return inferredOntologyID;
     }
     
     /**
      * Helper method which loads, infers and stores a given ontology using the PoddOWLManager.
      * 
      * @param resourcePath
      * @param format
      * @param assertedStatementCount
      * @param inferredStatementCount
      * @param repositoryConnection
      *            TODO
      * @param dependentSchemaOntologies
      * @return
      * @throws Exception
      */
     protected InferredOWLOntologyID loadInferStoreSchema(final String resourcePath, final RDFFormat format,
             final long assertedStatementCount, final long inferredStatementCount,
             final RepositoryConnection repositoryConnection, Set<? extends OWLOntologyID> dependentSchemaOntologies)
         throws Exception
     {
         // load ontology to OWLManager
         final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
         Assert.assertNotNull("Could not find resource: " + resourcePath, inputStream);
         
         return this.loadInferStoreSchema(inputStream, format, assertedStatementCount, inferredStatementCount,
                 repositoryConnection, dependentSchemaOntologies);
     }
     
     /**
      * Helper method which loads version 1 for the three PODD schema ontologies (and their
      * dependencies): PODD-Base, PODD-Science and PODD-Plant.
      * 
      * This method is not called from the setUp() method since some tests require not loading all
      * schema ontologies.
      * 
      * @throws Exception
      */
     protected abstract List<InferredOWLOntologyID> loadVersion1SchemaOntologies() throws Exception;
     
     /**
      * Helper method which loads version 1 for the three PODD schema ontologies (and their
      * dependencies): PODD-Base, PODD-Science and PODD-Plant.
      * 
      * This method is not called from the setUp() method since some tests require not loading all
      * schema ontologies.
      * 
      * @throws Exception
      */
     protected abstract List<InferredOWLOntologyID> loadVersion2SchemaOntologies() throws Exception;
     
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception
     {
         this.schemaGraph = PODD.VF.createURI("urn:test:schema-graph");
         this.artifactGraph = PODD.VF.createURI("urn:test:artifact-graph");
         
         this.testRepositoryManager = this.getNewRepositoryManager();
         this.testRepositoryManager.setSchemaManagementGraph(this.schemaGraph);
         this.testRepositoryManager.setArtifactManagementGraph(this.artifactGraph);
         
         this.testManagementConnection = this.testRepositoryManager.getManagementRepository().getConnection();
         
         this.setupNonRepositoryManagers();
     }
     
     /**
      * @param testFileRegistry
      */
     private final void setupNonRepositoryManagers()
     {
         final DataReferenceProcessorRegistry testFileRegistry = new DataReferenceProcessorRegistry();
         // FIXME: Why are we clearing here
         testFileRegistry.clear();
         
         final PoddPurlProcessorFactoryRegistry testPurlRegistry = new PoddPurlProcessorFactoryRegistry();
         // FIXME: Why are we clearing here
         testPurlRegistry.clear();
         
         final PoddPurlProcessorFactory uuidFactory = this.getNewUUIDPurlProcessorFactory();
         Assert.assertNotNull("UUID factory was null", uuidFactory);
         testPurlRegistry.add(uuidFactory);
         
         final DataReferenceManager testFileReferenceManager = this.getNewFileReferenceManager();
         testFileReferenceManager.setDataProcessorRegistry(testFileRegistry);
         
         final PoddPurlManager testPurlManager = this.getNewPurlManager();
         testPurlManager.setPurlProcessorFactoryRegistry(testPurlRegistry);
         
         OWLOntologyManagerFactory manager = getNewOWLOntologyManagerFactory();
         Assert.assertNotNull("Null implementation of OWLOntologyManagerFactory", manager);
         final PoddOWLManager testOWLManager = this.getNewOWLManager(manager, this.getNewReasonerFactory());
         
         this.testSesameManager = this.getNewSesameManager();
         
         this.testSchemaManager = this.getNewSchemaManager();
         AbstractPoddArtifactManagerTest.setupTestSchemaManager(this.testSchemaManager, testOWLManager,
                 this.testRepositoryManager, this.testSesameManager);
         
         this.testArtifactManager = this.getNewArtifactManager();
         AbstractPoddArtifactManagerTest.setupTestArtifactManager(this.testArtifactManager, this.testRepositoryManager,
                 testFileReferenceManager, testPurlManager, testOWLManager, this.testSchemaManager,
                 this.testSesameManager);
     }
     
     private static final void setupTestSchemaManager(final PoddSchemaManager result, final PoddOWLManager owlManager,
             final PoddRepositoryManager repositoryManager, final PoddSesameManager sesameManager)
     {
         result.setOwlManager(owlManager);
         result.setRepositoryManager(repositoryManager);
         result.setSesameManager(sesameManager);
     }
     
     private static final void setupTestArtifactManager(final PoddArtifactManager result,
             final PoddRepositoryManager repositoryManager, final DataReferenceManager dataReferenceManager,
             final PoddPurlManager purlManager, final PoddOWLManager owlManager, final PoddSchemaManager schemaManager,
             final PoddSesameManager sesameManager)
     {
         result.setRepositoryManager(repositoryManager);
         result.setDataReferenceManager(dataReferenceManager);
         result.setPurlManager(purlManager);
         result.setOwlManager(owlManager);
         result.setSchemaManager(schemaManager);
         result.setSesameManager(sesameManager);
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
             if(this.testManagementConnection.isActive())
             {
                 this.log.warn("Found active transaction after test");
                 // this.testRepositoryConnection.rollback();
             }
         }
         finally
         {
             try
             {
                 if(this.testManagementConnection.isOpen())
                 {
                     this.testManagementConnection.close();
                 }
             }
             finally
             {
                 this.testRepositoryManager.shutDown();
                 this.testRepositoryManager = null;
                 this.testManagementConnection = null;
             }
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#attachDataReferences(URI, URI, InputStream, RDFFormat, DataReferenceVerificationPolicy)}
      * .
      */
     @Test
     public final void testAttachFileReferencesWithoutVerification() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         
         final InferredOWLOntologyID artifactId = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactId, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final InferredOWLOntologyID updatedArtifact;
         try (final InputStream editInputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT);)
         {
             
             updatedArtifact =
                     this.testArtifactManager.attachDataReferences(artifactId,
                             Rio.parse(editInputStream, "", RDFFormat.RDFXML),
                             DataReferenceVerificationPolicy.DO_NOT_VERIFY);
         }
         // verify:
         RepositoryConnection managementConnection = null;
         RepositoryConnection permanentConnection = null;
         try
         {
             managementConnection = this.testRepositoryManager.getManagementRepository().getConnection();
             managementConnection.begin();
             
             Set<? extends OWLOntologyID> schemaImports = this.testArtifactManager.getSchemaImports(updatedArtifact);
             permanentConnection = this.testRepositoryManager.getPermanentRepository(schemaImports).getConnection();
             
             this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES + 8, managementConnection);
             
             // verify: file reference object
             final List<Statement> fileRefList =
                     Iterations.asList(permanentConnection.getStatements(null, PODD.PODD_BASE_HAS_DATA_REFERENCE, null,
                             false, updatedArtifact.getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Graph should have 1 file reference", 1, fileRefList.size());
             
             Assert.assertTrue("File reference value incorrect",
                     fileRefList.get(0).getObject().stringValue().endsWith("object-rice-scan-34343-a"));
         }
         finally
         {
             if(permanentConnection != null && permanentConnection.isOpen())
             {
                 permanentConnection.close();
             }
             permanentConnection = null;
             
             if(managementConnection != null && managementConnection.isActive())
             {
                 managementConnection.rollback();
             }
             if(managementConnection != null && managementConnection.isOpen())
             {
                 managementConnection.close();
             }
             managementConnection = null;
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#deleteArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      * Issue #91: Tests that deleting an artifact clears it from the OWLOntologyManager's cache too.
      * Loads a test artifact with PURLs (so that it has the same IRIs internally), deletes it and
      * then loads it again.
      */
     @Test
     public final void testDeleteArtifactClearsOntologyManager() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         
         final InferredOWLOntologyID resultArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(resultArtifactId, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         // invoke test method: DELETE artifact
         Assert.assertTrue("Could not delete artifact", this.testArtifactManager.deleteArtifact(resultArtifactId));
         
         try
         {
             this.testArtifactManager.getArtifact(resultArtifactId.getOntologyIRI());
             Assert.fail("Current contract is to throw an exception when someone tries to get an artifact that does not exist");
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             Assert.assertNotNull("Exception did not contain the requested artifact IRI", e.getUnmanagedOntologyIRI());
             Assert.assertEquals("IRI on the exception did not match our expected IRI",
                     resultArtifactId.getOntologyIRI(), e.getUnmanagedOntologyIRI());
         }
         
         // try to load same artifact again. will fail if the OWLOntologyManager's cache was not
         // cleared
         final InputStream inputStream2 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         
         final InferredOWLOntologyID resultArtifactId2 =
                 this.testArtifactManager.loadArtifact(inputStream2, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(resultArtifactId2, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#deleteArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      * Tests that the artifact manager cannot delete a published artifact.
      */
     @Test
     public final void testDeleteArtifactWhenPublished() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         // invoke test method
         InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
         
         resultArtifactId = this.testArtifactManager.publishArtifact(resultArtifactId);
         
         // verify:
         this.verifyLoadedArtifact(resultArtifactId, 12,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, true);
         
         try
         {
             this.testArtifactManager.deleteArtifact(resultArtifactId);
             Assert.fail("Should have failed to delete Published artifact");
         }
         catch(final DeleteArtifactException e)
         {
             Assert.assertNotNull("Exception did not contain the artifact ID", e.getArtifact());
             Assert.assertEquals("IRI on the exception did not match our expected IRI",
                     resultArtifactId.getOntologyIRI(), e.getArtifact().getOntologyIRI());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#deleteArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      * Tests that the artifact manager cannot delete a published artifact.
      */
     @Test
     public final void testDeleteArtifactWhenUnmanaged() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         final InferredOWLOntologyID resultArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream, RDFFormat.RDFXML);
         
         // verify:
         this.verifyLoadedArtifact(resultArtifactId, 11,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         
         boolean deleted = this.testArtifactManager.deleteArtifact(resultArtifactId);
         Assert.assertTrue("Should have deleted artifact successfully", deleted);
         
         try
         {
             this.testArtifactManager.deleteArtifact(resultArtifactId);
             Assert.fail("Did not find expected exception");
         }
         catch(DeleteArtifactException e)
         {
             Assert.assertNotNull(e.getCause());
             Assert.assertTrue(e.getCause() instanceof UnmanagedArtifactIRIException);
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
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         // MIME type should be either given by the user, detected from the content type on the
         // request, or autodetected using the Any23 Mime Detector
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         // invoke test method
         final InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
         
         // verify:
         this.verifyLoadedArtifact(resultArtifactId, 11,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         
         Assert.assertTrue("Could not delete artifact", this.testArtifactManager.deleteArtifact(resultArtifactId));
         
         try
         {
             this.testArtifactManager.getArtifact(resultArtifactId.getOntologyIRI());
             
             Assert.fail("Current contract is to throw an exception when someone tries to get an artifact that does not exist");
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             Assert.assertNotNull("Exception did not contain the requested artifact IRI", e.getUnmanagedOntologyIRI());
             
             Assert.assertEquals("IRI on the exception did not match our expected IRI",
                     resultArtifactId.getOntologyIRI(), e.getUnmanagedOntologyIRI());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#deleteObject(String, String, String, boolean)}
      * .
      * 
      * Tests that the artifact manager can delete a PODD Object.
      */
     @Test
     public final void testDeleteObjectSuccess() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadVersion1SchemaOntologies();
         final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactID = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactID, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final Object[][] testData =
                 { { "http://purl.org/podd/basic-2-20130206/artifact:1#My_Treatment1", 86, false },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#publication45", 76, false },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial", 64, true }, };
         
         for(final Object[] element : testData)
         {
             final URI objectToDelete = PODD.VF.createURI((String)element[0]);
             final int expectedArtifactSize = (int)element[1];
             final boolean cascade = (boolean)element[2];
             
             // perform test action: delete object
             final InferredOWLOntologyID modifiedArtifactId =
                     this.testArtifactManager.deleteObject(artifactID.getOntologyIRI().toOpenRDFURI(), artifactID
                             .getVersionIRI().toOpenRDFURI(), objectToDelete, cascade);
             
             // verify:
             final Model artifactModel = this.testArtifactManager.exportArtifact(modifiedArtifactId, false);
             Assert.assertEquals("Reduction in artifact size incorrect", expectedArtifactSize, artifactModel.size());
             
             Assert.assertTrue("Object was not deleted", artifactModel.filter(objectToDelete, null, null).isEmpty());
             Assert.assertTrue("Object was not deleted", artifactModel.filter(null, null, objectToDelete).isEmpty());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#deleteObject(String, String, String, boolean)}
      * .
      * 
      * Tests that deleting an object which has child objects without setting the cascade option is
      * not allowed.
      */
     @Test
     public final void testDeleteObjectWithChildrenNoCascade() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadVersion1SchemaOntologies();
         final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactID = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactID, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final URI objectToDelete =
                 PODD.VF.createURI("http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial");
         final boolean cascade = false;
         
         // perform test action: delete object
         try
         {
             this.testArtifactManager.deleteObject(artifactID.getOntologyIRI().toOpenRDFURI(), artifactID
                     .getVersionIRI().toOpenRDFURI(), objectToDelete, cascade);
             Assert.fail("Should not have allowed deletion");
         }
         catch(final DisconnectedObjectException e)
         {
             final Set<URI> disconnectedObjects = e.getDisconnectedObjects();
             System.out.println(disconnectedObjects);
             
             final Model artifactModel = this.testArtifactManager.exportArtifact(artifactID, false);
             Assert.assertEquals("Reduction in artifact size incorrect",
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, artifactModel.size());
             Assert.assertFalse("Object was deleted", artifactModel.filter(objectToDelete, null, null).isEmpty());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#deleteObject(String, String, String, boolean)}
      * .
      * 
      * Tests deleting an object which belongs to a Published artifact is not allowed.
      */
     @Test
     public final void testDeleteObjectWithPublishedArtifact() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadVersion1SchemaOntologies();
         final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactID = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactID, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final InferredOWLOntologyID publishedArtifact = this.testArtifactManager.publishArtifact(artifactID);
         
         final URI objectToDelete = PODD.VF.createURI("http://purl.org/podd/basic-2-20130206/artifact:1#publication45");
         
         // perform test action: delete object
         try
         {
             this.testArtifactManager.deleteObject(publishedArtifact.getOntologyIRI().toOpenRDFURI(), publishedArtifact
                     .getVersionIRI().toOpenRDFURI(), objectToDelete, false);
             Assert.fail("Should not have allowed deletion");
         }
         catch(final PublishedArtifactModifyException e)
         {
             Assert.assertEquals("Failure not due to published Artifact", publishedArtifact, e.getArtifactID());
             final Model artifactModel = this.testArtifactManager.exportArtifact(artifactID, false);
             Assert.assertEquals("Reduction in artifact size incorrect",
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, artifactModel.size());
             Assert.assertFalse("Object was deleted", artifactModel.filter(objectToDelete, null, null).isEmpty());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#deleteObject(String, String, String, boolean)}
      * .
      * 
      * Tests deleting an object which is connected to another object via a refersToXXX link.
      */
     @Test
     public final void testDeleteObjectWithReferredToLinks() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadVersion1SchemaOntologies();
         final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_WITH_REFERSTO);
         final InferredOWLOntologyID artifactID = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactID, 12, TestConstants.TEST_ARTIFACT_WITH_REFERSTO_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_WITH_REFERSTO_INFERRED_TRIPLES, false);
         
         final URI objectToDelete =
                 PODD.VF.createURI("http://purl.org/podd/basic-2-20130206/artifact:1#Demo_genotype_3");
         final boolean cascade = true;
         
         // perform test action: delete object
         final InferredOWLOntologyID modifiedArtifactId =
                 this.testArtifactManager.deleteObject(artifactID.getOntologyIRI().toOpenRDFURI(), artifactID
                         .getVersionIRI().toOpenRDFURI(), objectToDelete, cascade);
         
         // verify:
         final Model artifactModel = this.testArtifactManager.exportArtifact(modifiedArtifactId, false);
         Assert.assertEquals("Reduction in artifact size incorrect", 72, artifactModel.size());
         Assert.assertTrue("Object still exists as an object of some statement",
                 artifactModel.filter(null, null, objectToDelete).isEmpty());
         Assert.assertTrue("Object exists as a subject of some statement",
                 artifactModel.filter(objectToDelete, null, null).isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#deleteObject(String, String, String, boolean)}
      * .
      * 
      * Tests that deleting a Top Object (i.e. a Project object) is not allowed with the
      * deleteObject() method.
      */
     @Test
     public final void testDeleteObjectWithTopObject() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadVersion1SchemaOntologies();
         final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactID = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactID, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final URI objectToDelete = PODD.VF.createURI("http://purl.org/podd/basic-1-20130206/object:2966");
         final boolean cascade = true;
         
         // perform test action: delete object
         try
         {
             this.testArtifactManager.deleteObject(artifactID.getOntologyIRI().toOpenRDFURI(), artifactID
                     .getVersionIRI().toOpenRDFURI(), objectToDelete, cascade);
             Assert.fail("Should not have allowed deletion");
         }
         catch(final ArtifactModifyException e)
         {
             Assert.assertEquals("Failure not due to object to delete", objectToDelete, e.getObjectUri());
             
             final Model artifactModel = this.testArtifactManager.exportArtifact(artifactID, false);
             Assert.assertEquals("Reduction in artifact size incorrect",
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, artifactModel.size());
             Assert.assertFalse("Object was deleted", artifactModel.filter(objectToDelete, null, null).isEmpty());
         }
     }
     
     @Test
     public final void testExportArtifact() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final Model model = this.testArtifactManager.exportArtifact(artifactIDv1, false);
         
         Assert.assertFalse("Exported artifact was empty", model.isEmpty());
         Assert.assertEquals("Incorrect statement count in exported artifact",
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, model.size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#exportObjectMetadata(URI, java.io.OutputStream, RDFFormat, boolean, MetadataPolicy, InferredOWLOntologyID)}
      * .
      * 
      * Test to expose issue #96. poddScience:ProjectPlan only has the two default properties of
      * title and description. Issue also affected 'Process' and 'Protocol'.
      */
     @Test
     public final void testExportObjectMetadataForProjectPlan() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final ByteArrayOutputStream output = new ByteArrayOutputStream();
         
         final URI objectType = PODD.VF.createURI(PODD.PODD_SCIENCE, "Process");
         
         this.testArtifactManager.exportObjectMetadata(objectType, output, RDFFormat.TURTLE, false,
                 MetadataPolicy.EXCLUDE_CONTAINS, artifactIDv1);
         
         // parse output into a Model
         final ByteArrayInputStream bin = new ByteArrayInputStream(output.toByteArray());
         final RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
         final Model model = new LinkedHashModel();
         rdfParser.setRDFHandler(new StatementCollector(model));
         rdfParser.parse(bin, "");
         
         // verify:
         Assert.assertEquals("Not the expected number of statements", 18, model.size());
         Assert.assertTrue("Missing statement: 'Process a OWL:Class'", model.contains(objectType, RDF.TYPE, OWL.CLASS));
         Assert.assertTrue("No label for rdfs:label", model.contains(RDFS.LABEL, RDFS.LABEL, null));
         Assert.assertTrue("No label for rdfs:comment", model.contains(RDFS.COMMENT, RDFS.LABEL, null));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#exportObjectMetadata(URI, java.io.OutputStream, RDFFormat, boolean, MetadataPolicy, InferredOWLOntologyID)}
      * .
      */
     @Test
     public final void testExportObjectMetadataWithArtifact() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         this.internalTestExportObjectMetadata(artifactIDv1);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#exportObjectMetadata(URI, java.io.OutputStream, RDFFormat, boolean, MetadataPolicy, InferredOWLOntologyID)}
      * .
      */
     @Test
     public final void testExportObjectMetadataWithArtifactOther() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         this.internalTestExportObjectMetadata(artifactIDv1);
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         final InferredOWLOntologyID unpublishedArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream, RDFFormat.RDFXML);
         this.verifyLoadedArtifact(unpublishedArtifactId, 22,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         
         // Test that after the second artifact is added the numbers do not change
         this.internalTestExportObjectMetadata(artifactIDv1);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#exportObjectMetadata(URI, java.io.OutputStream, RDFFormat, boolean, MetadataPolicy, InferredOWLOntologyID)}
      * .
      */
     @Test
     public final void testExportObjectMetadataWithoutArtifact() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         this.internalTestExportObjectMetadata(null);
     }
     
     @Test
     public final void testFillMissingData() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
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
         
         final Model resultModel = this.testArtifactManager.fillMissingData(artifactIDv1, testModel);
         
         // verify: each URI has the expected label
         for(int i = 0; i < objectUris.length; i++)
         {
             final String objectString =
                     resultModel.filter(PODD.VF.createURI(objectUris[i]), RDFS.LABEL, null).objectString();
            Assert.assertNotNull("No label for: " + objectUris[i], objectString);
             Assert.assertEquals("Not the expected label for: " + objectUris[i], expectedLabels[i], objectString);
         }
     }
     
     @Test
     public final void testGetChildObjects() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID ontologyID = this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(ontologyID, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final Object[][] testData =
                 { { "http://purl.org/podd/basic-1-20130206/object:2966", 6 },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#publication45", 0 },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype", 0 },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Investigation", 3 },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial", 1 },
                         { "http://purl.org/podd/ns/poddScience#WildType_NotApplicable", 0 }, };
         
         for(final Object[] element : testData)
         {
             final URI objectUri = PODD.VF.createURI(element[0].toString());
             final int expectedChildObjectCount = (int)element[1];
             
             final Set<URI> childObjects = this.testArtifactManager.getChildObjects(ontologyID, objectUri);
             Assert.assertEquals("Not the expected number of child objects", expectedChildObjectCount,
                     childObjects.size());
         }
     }
     
     @Test
     public final void testGetFileReferenceManager() throws Exception
     {
         Assert.assertNotNull("File Reference Manager was null", this.testArtifactManager.getDataReferenceManager());
     }
     
     /**
      * Test that the schema contexts work after using
      * {@link PoddArtifactManager#loadArtifact(InputStream, RDFFormat)}
      * 
      * @throws Exception
      */
     @Test
     public final void testSchemaContexts() throws Exception
     {
         List<InferredOWLOntologyID> schemaOntologies = this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         URI[] schemaContexts =
                 this.testArtifactManager.getSesameManager().schemaContexts(artifactIDv1, testManagementConnection,
                         schemaGraph, artifactGraph);
         
         Assert.assertEquals(6, schemaContexts.length);
         
         Set<URI> schemaUris = new HashSet<>(Arrays.asList(schemaContexts));
         
         for(InferredOWLOntologyID nextSchemaOntology : schemaOntologies)
         {
             Assert.assertTrue(schemaUris.contains(nextSchemaOntology.getVersionIRI().toOpenRDFURI()));
         }
     }
     
     /**
      * Test that the schema contexts work after using
      * {@link PoddArtifactManager#loadArtifact(InputStream, RDFFormat)}
      * 
      * @throws Exception
      */
     @Test
     public final void testVersionSchemaContexts() throws Exception
     {
         List<InferredOWLOntologyID> schemaOntologies = this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         URI[] schemaContexts =
                 this.testArtifactManager.getSesameManager().versionAndSchemaContexts(artifactIDv1,
                         testManagementConnection, schemaGraph, artifactGraph);
         
         Assert.assertEquals(7, schemaContexts.length);
         
         Set<URI> schemaUris = new HashSet<>(Arrays.asList(schemaContexts));
         
         Assert.assertTrue(schemaUris.contains(artifactIDv1.getVersionIRI().toOpenRDFURI()));
         
         for(InferredOWLOntologyID nextSchemaOntology : schemaOntologies)
         {
             Assert.assertTrue(schemaUris.contains(nextSchemaOntology.getVersionIRI().toOpenRDFURI()));
         }
     }
     
     /**
      * Test that the schema contexts work after using
      * {@link PoddArtifactManager#loadArtifact(InputStream, RDFFormat)}
      * 
      * @throws Exception
      */
     @Test
     public final void testVersionInferredSchemaContexts() throws Exception
     {
         List<InferredOWLOntologyID> schemaOntologies = this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         URI[] schemaContexts =
                 this.testArtifactManager.getSesameManager().versionAndInferredAndSchemaContexts(artifactIDv1,
                         testManagementConnection, schemaGraph, artifactGraph);
         
         Assert.assertEquals(8, schemaContexts.length);
         
         Set<URI> schemaUris = new HashSet<>(Arrays.asList(schemaContexts));
         
         Assert.assertTrue(schemaUris.contains(artifactIDv1.getVersionIRI().toOpenRDFURI()));
         Assert.assertTrue(schemaUris.contains(artifactIDv1.getInferredOntologyIRI().toOpenRDFURI()));
         
         for(InferredOWLOntologyID nextSchemaOntology : schemaOntologies)
         {
             Assert.assertTrue(schemaUris.contains(nextSchemaOntology.getVersionIRI().toOpenRDFURI()));
         }
     }
     
     @Test
     public final void testGetObjectTypes() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final Object[][] testData =
                 {
                         { "http://purl.org/podd/basic-1-20130206/object:2966", 1,
                                 "http://purl.org/podd/ns/poddScience#Project" },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#publication45", 1,
                                 "http://purl.org/podd/ns/poddScience#Publication" },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype", 1,
                                 "http://purl.org/podd/ns/poddScience#Genotype" },
                         { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial", 1,
                                 "http://purl.org/podd/ns/poddScience#Material" },
                         { "http://purl.org/podd/ns/poddScience#WildType_NotApplicable", 1,
                                 "http://purl.org/podd/ns/poddScience#WildTypeAssertion" }, };
         
         for(final Object[] element : testData)
         {
             final URI objectUri = PODD.VF.createURI(element[0].toString());
             final int expectedStatementCount = (int)element[1];
             
             final List<PoddObjectLabel> list = this.testArtifactManager.getObjectTypes(artifactIDv1, objectUri);
             
             this.log.info("Results for object <{}> are {}", objectUri, list);
             
             Assert.assertEquals("Unexpected no. of statements", expectedStatementCount, list.size());
             if(expectedStatementCount == 1)
             {
                 final URI expectedType = PODD.VF.createURI(element[2].toString());
                 Assert.assertEquals("Not the expected type", expectedType, list.get(0).getObjectURI());
             }
         }
     }
     
     @Test
     public final void testGetOWLManager() throws Exception
     {
         Assert.assertNotNull("OWL Manager was null", this.testArtifactManager.getOWLManager());
     }
     
     @Test
     public final void testGetParentDetails() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
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
             final URI objectUri = PODD.VF.createURI(element[0].toString());
             final int expectedStatementCount = (int)element[1];
             
             final Model model = this.testArtifactManager.getParentDetails(artifactIDv1, objectUri);
             
             Assert.assertEquals("Unexpected no. of statements: " + objectUri, expectedStatementCount, model.size());
             if(expectedStatementCount == 1)
             {
                 final URI expectedParent = PODD.VF.createURI(element[2].toString());
                 Assert.assertTrue("Not the expected parent: " + objectUri + " " + expectedParent, model.subjects()
                         .contains(expectedParent));
             }
         }
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
     
     @Test
     public final void testGetSchemaImportsNull() throws Exception
     {
         try
         {
             this.testArtifactManager.getSchemaImports(null);
             Assert.fail("Did not receive expected exception");
         }
         catch(NullPointerException e)
         {
             
         }
     }
     
     @Test
     public final void testGetSchemaImportsNonExistingArtifact() throws Exception
     {
         try
         {
             this.testArtifactManager.getSchemaImports(new InferredOWLOntologyID(IRI
                     .create("urn:artifact:does:not:exist"), IRI.create("urn:artifact:version:does:not:exist"), IRI
                     .create("urn:inferred:artifact:does:not:exist")));
             Assert.fail("Did not receive expected exception");
         }
         catch(UnmanagedArtifactIRIException e)
         {
             
         }
     }
     
     @Test
     public final void testGetSchemaImportsVersion1Artifact() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         final InferredOWLOntologyID artifactID = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.RDFXML);
         this.verifyLoadedArtifact(artifactID, 11, TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         
         Set<? extends OWLOntologyID> schemaImports = this.testArtifactManager.getSchemaImports(artifactID);
         
         DebugUtils.printContents(testManagementConnection, artifactGraph);
         
         Assert.assertFalse("No schema imports detected", schemaImports.isEmpty());
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddArtifactManager#listPublishedArtifacts()}. .
      */
     @Test
     public final void testListPublishedArtifacts() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         final InferredOWLOntologyID unpublishedArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream, RDFFormat.RDFXML);
         this.verifyLoadedArtifact(unpublishedArtifactId, 11,
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
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         // MIME type should be either given by the user, detected from the content type on the
         // request, or autodetected using the Any23 Mime Detector
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         final InferredOWLOntologyID unpublishedArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
         this.verifyLoadedArtifact(unpublishedArtifactId, 11,
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
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         // MIME type should be either given by the user, detected from the content type on the
         // request, or autodetected using the Any23 Mime Detector
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         // invoke test method
         final InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
         
         // verify:
         this.verifyLoadedArtifact(resultArtifactId, 11,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
     }
     
     @Ignore("TODO: Enable periodically to debug concurrency issues")
     @Test
     public final void testLoadArtifactConcurrency() throws Exception
     {
         // prepare:
         this.loadVersion1SchemaOntologies();
         
         // load test artifact
         final InputStream inputStream4Artifact =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1);
         
         Assert.assertNotNull("Could not find test resource: " + TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1,
                 inputStream4Artifact);
         
         final String nextTestArtifact = IOUtils.toString(inputStream4Artifact);
         
         final AtomicInteger count = new AtomicInteger(0);
         final CountDownLatch openLatch = new CountDownLatch(1);
         final int threadCount = 15;
         final CountDownLatch closeLatch = new CountDownLatch(threadCount);
         for(int i = 0; i < threadCount; i++)
         {
             final int number = i;
             final Runnable runner = new Runnable()
                 {
                     @Override
                     public void run()
                     {
                         try
                         {
                             openLatch.await();
                             for(int j = 0; j < 5; j++)
                             {
                                 final ByteArrayInputStream inputStream =
                                         new ByteArrayInputStream(nextTestArtifact.getBytes(StandardCharsets.UTF_8));
                                 final InferredOWLOntologyID artifactId =
                                         AbstractPoddArtifactManagerTest.this.testArtifactManager.loadArtifact(
                                                 inputStream, RDFFormat.RDFXML);
                             }
                             count.incrementAndGet();
                         }
                         catch(OpenRDFException | PoddException | IOException | OWLException e)
                         {
                             e.printStackTrace();
                             Assert.fail("Failed in test: " + number);
                         }
                         catch(final InterruptedException ie)
                         {
                             ie.printStackTrace();
                             Assert.fail("Failed in test: " + number);
                         }
                         finally
                         {
                             closeLatch.countDown();
                         }
                     }
                 };
             new Thread(runner, "TestThread" + number).start();
         }
         // all threads are waiting on the latch.
         openLatch.countDown(); // release the latch
         // all threads are now running concurrently.
         closeLatch.await();
         // Verify that there were no failures, as the count is only incremented for successes, where
         // the closeLatch must always be called, even for failures
         Assert.assertEquals(threadCount, count.get());
         
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testLoadArtifactWithEmptyOntology() throws Exception
     {
         final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_INVALID_ARTIFACT_EMPTY);
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
             Assert.assertTrue("The ontology is not empty", (e.getOntology() == null || e.getOntology().isEmpty()));
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
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BAD_2_LEAD_INSTITUTES);
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
             Assert.assertEquals("Not the expected Root Ontology", "urn:temp:inconsistentArtifact:1", e.getOntologyID()
                     .getOntologyIRI().toString());
             Assert.assertTrue("Not the expected error message", e.getMessage().startsWith("Ontology is inconsistent"));
             
             e.getDetailsAsModel(PODD.VF.createBNode());
             
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
     @Ignore("TODO: Enable manual upload tests")
     @Test
     public final void testLoadArtifactWithMissingSchemaOntologiesInRepository() throws Exception
     {
         // Keep track of the ontologies that have been loaded to ensure they are in memory when
         // inferring the next schema
         Set<InferredOWLOntologyID> loadedOntologies = new LinkedHashSet<>();
         // prepare: load schema ontologies
         final InferredOWLOntologyID inferredDctermsOntologyID =
                 this.loadInferStoreSchema(PODD.PATH_PODD_DCTERMS_V1, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_INFERRED, this.testManagementConnection,
                         loadedOntologies);
         loadedOntologies.add(inferredDctermsOntologyID);
         final InferredOWLOntologyID inferredFoafOntologyID =
                 this.loadInferStoreSchema(PODD.PATH_PODD_FOAF_V1, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_INFERRED, this.testManagementConnection,
                         loadedOntologies);
         loadedOntologies.add(inferredFoafOntologyID);
         final InferredOWLOntologyID inferredPUserOntologyID =
                 this.loadInferStoreSchema(PODD.PATH_PODD_USER_V1, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_INFERRED, this.testManagementConnection,
                         loadedOntologies);
         loadedOntologies.add(inferredPUserOntologyID);
         final InferredOWLOntologyID inferredPBaseOntologyID =
                 this.loadInferStoreSchema(PODD.PATH_PODD_BASE_V1, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED, this.testManagementConnection,
                         loadedOntologies);
         loadedOntologies.add(inferredPBaseOntologyID);
         final InferredOWLOntologyID inferredPScienceOntologyID =
                 this.loadInferStoreSchema(PODD.PATH_PODD_SCIENCE_V1, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED, this.testManagementConnection,
                         loadedOntologies);
         loadedOntologies.add(inferredPScienceOntologyID);
         
         // prepare: update schema management graph
         this.testSesameManager.updateManagedSchemaOntologyVersion(inferredDctermsOntologyID, false,
                 this.testManagementConnection, this.schemaGraph);
         this.testSesameManager.updateManagedSchemaOntologyVersion(inferredFoafOntologyID, false,
                 this.testManagementConnection, this.schemaGraph);
         this.testSesameManager.updateManagedSchemaOntologyVersion(inferredPUserOntologyID, false,
                 this.testManagementConnection, this.schemaGraph);
         this.testSesameManager.updateManagedSchemaOntologyVersion(inferredPBaseOntologyID, false,
                 this.testManagementConnection, this.schemaGraph);
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
      * Tests loading an artifact which imports a previous version of a schema ontology (i.e.
      * poddScience v1)
      */
     @Test
     public final void testLoadArtifactWithNonCurrentSchemaVersionImport() throws Exception
     {
         // prepare:
         List<InferredOWLOntologyID> version1SchemaOntologies = this.loadVersion1SchemaOntologies();
         
         // prepare: load poddScience v2
         final InferredOWLOntologyID inferredPScienceOntologyID =
                 this.loadInferStoreSchema("/test/ontologies/poddScienceVXYZ.owl", RDFFormat.RDFXML, 1265, 220,
                         this.testManagementConnection, new LinkedHashSet<InferredOWLOntologyID>(
                                 version1SchemaOntologies));
         this.testSesameManager.updateManagedSchemaOntologyVersion(inferredPScienceOntologyID, true,
                 this.testManagementConnection, this.schemaGraph);
         
         // load test artifact
         final InputStream inputStream4Artifact =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1);
         final InferredOWLOntologyID artifactId =
                 this.testArtifactManager.loadArtifact(inputStream4Artifact, RDFFormat.RDFXML);
         
         this.verifyLoadedArtifact(artifactId, 11, TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1_INFERRED_TRIPLES, false);
         
         // verify:
         RepositoryConnection managementConnection = null;
         RepositoryConnection permanentConnection = null;
         try
         {
             managementConnection = this.testRepositoryManager.getManagementRepository().getConnection();
             
             Set<? extends OWLOntologyID> schemaImports = this.testArtifactManager.getSchemaImports(artifactId);
             permanentConnection = this.testRepositoryManager.getPermanentRepository(schemaImports).getConnection();
             
             final String[] expectedImports =
                     { "http://purl.org/podd/ns/version/dcTerms/1", "http://purl.org/podd/ns/version/poddUser/1",
                             "http://purl.org/podd/ns/version/poddBase/1",
                             "http://purl.org/podd/ns/version/poddScience/1", // an older version
                     };
             
             // verify: no. of import statements
             final int importStatementCount =
                     Iterations.asList(
                             permanentConnection.getStatements(null, OWL.IMPORTS, null, false, artifactId
                                     .getVersionIRI().toOpenRDFURI())).size();
             Assert.assertEquals("Graph should have 4 import statements", 4, importStatementCount);
             
             for(final String expectedImport : expectedImports)
             {
                 final List<Statement> importStatements =
                         Iterations.asList(permanentConnection.getStatements(null, OWL.IMPORTS, ValueFactoryImpl
                                 .getInstance().createURI(expectedImport), false, artifactId.getVersionIRI()
                                 .toOpenRDFURI()));
                 
                 Assert.assertEquals("Expected 1 import statement per schema", 1, importStatements.size());
             }
         }
         finally
         {
             if(permanentConnection != null && permanentConnection.isOpen())
             {
                 permanentConnection.close();
             }
             permanentConnection = null;
             if(managementConnection != null && managementConnection.isOpen())
             {
                 managementConnection.close();
             }
             managementConnection = null;
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
     @Ignore("TODO: How was this previously supported??")
     @Test
     public final void testLoadArtifactWithNoVersionIRIInSource() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // load artifact
         final InputStream inputStream4FirstArtifact =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_NO_VERSION_INFO);
         final InferredOWLOntologyID firstArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream4FirstArtifact, RDFFormat.RDFXML);
         
         // verify
         this.verifyLoadedArtifact(firstArtifactId, 7, TestConstants.TEST_ARTIFACT_NO_VERSION_INFO_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_NO_VERSION_INFO_INFERRED_TRIPLES, false);
         Assert.assertEquals("Version IRI of loaded ontology not expected value", firstArtifactId.getOntologyIRI()
                 .toString().concat(":version:1"), firstArtifactId.getVersionIRI().toString());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      * 
      * Tests loading two versions of the same artifact one after the other. This fails as the
      * modified version should be "updated" and not "loaded".
      * 
      * The two source RDF files have PURLs instead of temporary URIs since they both need to be
      * identified as the same artifact.
      */
     @Test
     public final void testLoadArtifactWithSameArtifactTwiceFails() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // load 1st artifact
         final InputStream inputStream4FirstArtifact =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_PURLS_v1);
         final InferredOWLOntologyID firstArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream4FirstArtifact, RDFFormat.RDFXML);
         
         this.verifyLoadedArtifact(firstArtifactId, 11, TestConstants.TEST_ARTIFACT_PURLS_v1_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_PURLS_v1_INFERRED_TRIPLES, false);
         
         // load 2nd artifact
         final InputStream inputStream4SecondArtifact =
                 this.getClass().getResourceAsStream("/test/artifacts/project-with-purls-v2.rdf");
         try
         {
             this.testArtifactManager.loadArtifact(inputStream4SecondArtifact, RDFFormat.RDFXML);
             Assert.fail("Should not allow a duplicate artifact to be loaded");
         }
         catch(final DuplicateArtifactIRIException e)
         {
             Assert.assertEquals("Duplicate does not have expected ontology IRI", firstArtifactId.getOntologyIRI(),
                     e.getDuplicateOntologyIRI());
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
         this.loadVersion1SchemaOntologies();
         
         // load 1st artifact
         final InputStream inputStream4FirstArtifact =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         final InferredOWLOntologyID firstArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream4FirstArtifact, RDFFormat.RDFXML);
         
         this.verifyLoadedArtifact(firstArtifactId, 11,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         
         // load 2nd artifact
         final InputStream inputStream4SecondArtifact =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_PROJECT_2);
         final InferredOWLOntologyID secondArtifactId =
                 this.testArtifactManager.loadArtifact(inputStream4SecondArtifact, RDFFormat.RDFXML);
         
         this.verifyLoadedArtifact(firstArtifactId, 22,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         this.verifyLoadedArtifact(secondArtifactId, 22, TestConstants.TEST_ARTIFACT_BASIC_PROJECT_2_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_PROJECT_2_INFERRED_TRIPLES, true);
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
         this.loadVersion1SchemaOntologies();
         
         // load 1st artifact
         final InputStream inputStream4Artifact =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_PROJECT_PUBLISHED);
         final InferredOWLOntologyID artifactId =
                 this.testArtifactManager.loadArtifact(inputStream4Artifact, RDFFormat.RDFXML);
         
         this.verifyLoadedArtifact(artifactId, 11, TestConstants.TEST_ARTIFACT_BASIC_PROJECT_PUBLISHED_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_PROJECT_PUBLISHED_INFERRED_TRIPLES, true);
         
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
         this.loadVersion1SchemaOntologies();
         
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         // MIME type should be either given by the user, detected from the content type on the
         // request, or autodetected using the Any23 Mime Detector
         final String mimeType = "application/rdf+xml";
         final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
         
         final InferredOWLOntologyID unpublishedArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
         this.verifyLoadedArtifact(unpublishedArtifactId, 11,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
         
         // invoke method under test
         final InferredOWLOntologyID publishedArtifactId =
                 this.testArtifactManager.publishArtifact(unpublishedArtifactId);
         
         // verify: publication status is correctly updated
         RepositoryConnection nextRepositoryConnection = null;
         try
         {
             nextRepositoryConnection = this.testRepositoryManager.getManagementRepository().getConnection();
             nextRepositoryConnection.begin();
             
             // verify: a single PUBLICATION_STATUS in asserted ontology
             final List<Statement> publicationStatusStatementList =
                     Iterations.asList(nextRepositoryConnection.getStatements(null,
                             PODD.PODD_BASE_HAS_PUBLICATION_STATUS, null, false,
                             this.testRepositoryManager.getArtifactManagementGraph()));
             Assert.assertEquals("Graph should have one HAS_PUBLICATION_STATUS statement.", 1,
                     publicationStatusStatementList.size());
             
             // verify: artifact is PUBLISHED
             Assert.assertEquals("Wrong publication status", PODD.PODD_BASE_PUBLISHED.toString(),
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
      * {@link com.github.podd.api.PoddArtifactManager#searchForOntologyLabels(InferredOWLOntologyID, String, URI[])}
      */
     @Test
     public final void testSearchForOntologyLabelsWithPlatforms() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // prepare: upload a test artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final String searchTerm = "lat";
         final URI[] searchTypes =
                 { PODD.VF.createURI(PODD.PODD_SCIENCE, "Platform"), PODD.VF.createURI(OWL.NAMESPACE, "NamedIndividual") };
         
         final Model result = this.testArtifactManager.searchForOntologyLabels(artifactIDv1, searchTerm, searchTypes);
         
         // verify:
         Assert.assertNotNull("NULL result", result);
         Assert.assertEquals("Not the expected number of search results", 1, result.size());
         Assert.assertEquals("Expected custom Platform 1 not found", 1,
                 result.filter(null, null, PODD.VF.createLiteral("Platform 1")).size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * . Tests adding a Podd Object inconsistent with the schema ontologies fails.
      */
     @Test
     public final void testUpdateArtifactAddInconsistentObject() throws Exception
     {
         try
         {
             this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 12,
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                     TestConstants.TEST_ARTIFACT_FRAGMENT_INCONSISTENT_OBJECT, RDFFormat.TURTLE,
                     UpdatePolicy.MERGE_WITH_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                     DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
             Assert.fail("Should have thrown an InconsistentOntologyException");
         }
         catch(final InconsistentOntologyException e)
         {
             Assert.assertTrue("Not the expected error message", e.getMessage().startsWith("Ontology is inconsistent"));
             
             e.getDetailsAsModel(PODD.VF.createBNode());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      * 
      * Tests adding multiple new PODD objects to an artifact.
      */
     @Test
     public final void testUpdateArtifactAddNewPoddObjectsWithMerge() throws Exception
     {
         final List<URI> objectUriList =
                 Arrays.asList(PODD.VF.createURI("urn:temp:uuid:object-rice-scan-34343-a"),
                         PODD.VF.createURI("urn:temp:uuid:publication35"),
                         PODD.VF.createURI("urn:temp:uuid:publication46"));
         
         final InferredOWLOntologyID updatedArtifact =
                 this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 12,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                         TestConstants.TEST_ARTIFACT_FRAGMENT_MULTIPLE_OBJECTS_TTL, RDFFormat.TURTLE,
                         UpdatePolicy.MERGE_WITH_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                         DataReferenceVerificationPolicy.DO_NOT_VERIFY, objectUriList);
         
         // verify:
         RepositoryConnection managementConnection = null;
         RepositoryConnection permanentConnection = null;
         try
         {
             managementConnection = this.testRepositoryManager.getManagementRepository().getConnection();
             
             Set<? extends OWLOntologyID> schemaImports = this.testArtifactManager.getSchemaImports(updatedArtifact);
             permanentConnection = this.testRepositoryManager.getPermanentRepository(schemaImports).getConnection();
             
             this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES + 28, managementConnection);
             
             // verify: 2 publications exist
             final List<Statement> testList =
                     Iterations.asList(permanentConnection.getStatements(null,
                             ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasPublication"), null, false,
                             updatedArtifact.getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Graph should have 2 publications", 3, testList.size());
             
             // verify: newly added publication exists
             Assert.assertTrue("New publication is missing",
                     testList.get(0).getObject().toString().endsWith("publication46")
                             || testList.get(1).getObject().toString().endsWith("publication46")
                             || testList.get(2).getObject().toString().endsWith("publication46"));
         }
         finally
         {
             if(permanentConnection != null && permanentConnection.isOpen())
             {
                 permanentConnection.close();
             }
             permanentConnection = null;
             if(managementConnection != null && managementConnection.isOpen())
             {
                 managementConnection.close();
             }
             managementConnection = null;
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      * 
      * Edit an artifact by adding a Platform pre-defined in a schema ontology and a new custom
      * Platform.
      */
     @Test
     public final void testUpdateArtifactAddNewPoddObjectsWithPlatforms() throws Exception
     {
         final List<URI> objectUriList = Arrays.asList(
         // a temporary URI for a Platform being newly added
                 PODD.VF.createURI("urn:temp:uuid:object-rice-scanner-platform"),
                 // a Platform that is pre-defined in PODD Plant Ontology
                 PODD.VF.createURI("http://purl.org/podd/ns/poddPlant#PlantScan-6e"));
         
         final InferredOWLOntologyID updatedArtifact =
                 this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 12,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                         TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_PLATFORM_OBJECTS, RDFFormat.TURTLE,
                         UpdatePolicy.MERGE_WITH_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                         DataReferenceVerificationPolicy.DO_NOT_VERIFY, objectUriList);
         
         // verify:
         RepositoryConnection managementConnection = null;
         RepositoryConnection permanentConnection = null;
         try
         {
             managementConnection = this.testRepositoryManager.getManagementRepository().getConnection();
             Set<? extends OWLOntologyID> schemaImports = this.testArtifactManager.getSchemaImports(updatedArtifact);
             permanentConnection = this.testRepositoryManager.getPermanentRepository(schemaImports).getConnection();
             
             this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES + 8, managementConnection);
             
             // verify: platform object
             final List<Statement> platformList =
                     Iterations.asList(permanentConnection.getStatements(null,
                             ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasPlatform"), null, false,
                             updatedArtifact.getVersionIRI().toOpenRDFURI()));
             
             // 2 added in the test plus a platform that was defined in the initially uploaded
             // artifact
             Assert.assertEquals("Not the expected number of Platforms", 3, platformList.size());
             
             // verify: correct set of platforms
             Assert.assertTrue(
                     "PlantScan Platform is missing",
                     platformList.get(0).getObject().toString().endsWith("PlantScan-6e")
                             || platformList.get(1).getObject().toString().endsWith("PlantScan-6e")
                             || platformList.get(2).getObject().toString().endsWith("PlantScan-6e"));
             
             Assert.assertTrue("Rice-Scanner Platform is missing",
                     platformList.get(0).getObject().toString().endsWith("object-rice-scanner-platform")
                             || platformList.get(1).getObject().toString().endsWith("object-rice-scanner-platform")
                             || platformList.get(2).getObject().toString().endsWith("object-rice-scanner-platform"));
         }
         finally
         {
             if(permanentConnection != null && permanentConnection.isOpen())
             {
                 permanentConnection.close();
             }
             permanentConnection = null;
             
             if(managementConnection != null && managementConnection.isOpen())
             {
                 managementConnection.close();
             }
             managementConnection = null;
         }
         
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      * 
      * NOTE: Once file reference validation is implemented in the DataReferenceManager this test
      * will fail. The referred file will have to be created for validation to pass.
      */
     @Test
     public final void testUpdateArtifactAddNewPoddObjectWithFileReferences() throws Exception
     {
         final InferredOWLOntologyID updatedArtifact =
                 this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 12,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                         TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT, RDFFormat.RDFXML,
                         UpdatePolicy.MERGE_WITH_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                         DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
         
         // verify:
         RepositoryConnection managementConnection = null;
         RepositoryConnection permanentConnection = null;
         try
         {
             managementConnection = this.testRepositoryManager.getManagementRepository().getConnection();
             Set<? extends OWLOntologyID> schemaImports = this.testArtifactManager.getSchemaImports(updatedArtifact);
             permanentConnection = this.testRepositoryManager.getPermanentRepository(schemaImports).getConnection();
             
             this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES + 8, managementConnection);
             
             // verify: file reference object
             final List<Statement> fileRefList =
                     Iterations.asList(permanentConnection.getStatements(null,
                             ValueFactoryImpl.getInstance().createURI(PODD.PODD_BASE, "hasDataReference"), null, false,
                             updatedArtifact.getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Graph should have 1 file reference", 1, fileRefList.size());
             
             Assert.assertTrue("File reference value incorrect",
                     fileRefList.get(0).getObject().stringValue().endsWith("object-rice-scan-34343-a"));
         }
         finally
         {
             if(permanentConnection != null && permanentConnection.isOpen())
             {
                 permanentConnection.close();
             }
             permanentConnection = null;
             
             if(managementConnection != null && managementConnection.isOpen())
             {
                 managementConnection.close();
             }
             managementConnection = null;
         }
         
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      */
     @Test
     public final void testUpdateArtifactAddNewPoddObjectWithMerge() throws Exception
     {
         final InferredOWLOntologyID updatedArtifact =
                 this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 12,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                         TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_PUBLICATION_OBJECT, RDFFormat.TURTLE,
                         UpdatePolicy.MERGE_WITH_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                         DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
         
         // verify:
         RepositoryConnection managementConnection = null;
         RepositoryConnection permanentConnection = null;
         try
         {
             managementConnection = this.testRepositoryManager.getManagementRepository().getConnection();
             Set<? extends OWLOntologyID> schemaImports = this.testArtifactManager.getSchemaImports(updatedArtifact);
             permanentConnection = this.testRepositoryManager.getPermanentRepository(schemaImports).getConnection();
             
             this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES + 10, managementConnection);
             
             // verify: 2 publications exist
             final List<Statement> testList =
                     Iterations.asList(permanentConnection.getStatements(null,
                             ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasPublication"), null, false,
                             updatedArtifact.getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Graph should have 2 publications", 2, testList.size());
             
             // verify: newly added publication exists
             Assert.assertTrue("New publication is missing",
                     testList.get(0).getObject().toString().endsWith("#publication46")
                             || testList.get(1).getObject().toString().endsWith("#publication46"));
         }
         finally
         {
             if(permanentConnection != null && permanentConnection.isOpen())
             {
                 permanentConnection.close();
             }
             permanentConnection = null;
             
             if(managementConnection != null && managementConnection.isOpen())
             {
                 managementConnection.close();
             }
             managementConnection = null;
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
         this.loadVersion1SchemaOntologies();
         
         final URI nonExistentArtifactURI =
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-3-no-such-artifact");
         
         final InputStream editInputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFIED_PUBLICATION_OBJECT);
         
         try
         {
             this.testArtifactManager.updateArtifact(nonExistentArtifactURI, nonExistentArtifactURI,
                     Collections.<URI> emptyList(), editInputStream, RDFFormat.TURTLE, UpdatePolicy.REPLACE_EXISTING,
                     DanglingObjectPolicy.FORCE_CLEAN, DataReferenceVerificationPolicy.DO_NOT_VERIFY);
             Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             Assert.assertEquals("Exception not due to expected IRI", nonExistentArtifactURI, e
                     .getUnmanagedOntologyIRI().toOpenRDFURI());
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
         final InferredOWLOntologyID updatedArtifact =
                 this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 12,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                         TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFIED_PUBLICATION_OBJECT, RDFFormat.TURTLE,
                         UpdatePolicy.REPLACE_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                         DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
         
         // verify:
         RepositoryConnection managementConnection = null;
         RepositoryConnection permanentConnection = null;
         try
         {
             managementConnection = this.testRepositoryManager.getManagementRepository().getConnection();
             Set<? extends OWLOntologyID> schemaImports = this.testArtifactManager.getSchemaImports(updatedArtifact);
             permanentConnection = this.testRepositoryManager.getPermanentRepository(schemaImports).getConnection();
             
             this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, managementConnection);
             
             // verify: still only 1 publication
             final List<Statement> testList =
                     Iterations.asList(permanentConnection.getStatements(null,
                             ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasPublication"), null, false,
                             updatedArtifact.getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Incorrect no. of hasPublication statements", 1, testList.size());
             
             // verify: publication info has been updated
             final List<Statement> testList2 =
                     Iterations.asList(permanentConnection.getStatements(null,
                             ValueFactoryImpl.getInstance().createURI(PODD.PODD_SCIENCE, "hasYear"), null, false,
                             updatedArtifact.getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Incorrect no. of hasYear statements", 1, testList2.size());
             Assert.assertEquals("Publication Year has not bee updated", "2011", testList2.get(0).getObject()
                     .stringValue());
             
         }
         finally
         {
             if(permanentConnection != null && permanentConnection.isOpen())
             {
                 permanentConnection.close();
             }
             permanentConnection = null;
             
             if(managementConnection != null && managementConnection.isOpen())
             {
                 managementConnection.close();
             }
             managementConnection = null;
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      */
     @Test
     public final void testUpdateArtifactMovePoddObject() throws Exception
     {
         final InferredOWLOntologyID updatedArtifact =
                 this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 12,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                         TestConstants.TEST_ARTIFACT_FRAGMENT_MOVE_DEMO_INVESTIGATION, RDFFormat.TURTLE,
                         UpdatePolicy.REPLACE_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                         DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
         
         // verify:
         RepositoryConnection managementConnection = null;
         RepositoryConnection permanentConnection = null;
         try
         {
             managementConnection = this.testRepositoryManager.getManagementRepository().getConnection();
             Set<? extends OWLOntologyID> schemaImports = this.testArtifactManager.getSchemaImports(updatedArtifact);
             permanentConnection = this.testRepositoryManager.getPermanentRepository(schemaImports).getConnection();
             
             this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, managementConnection);
             
             if(this.log.isDebugEnabled())
             {
                 DebugUtils.printContents(permanentConnection, updatedArtifact.getVersionIRI().toOpenRDFURI());
             }
             
             // verify: SqueekeMaterial is now under My_Treatment1
             Assert.assertEquals(
                     "Graph should have 1 statement",
                     1,
                     Iterations.asList(
                             permanentConnection.getStatements(
                                     ValueFactoryImpl.getInstance().createURI(
                                             "http://purl.org/podd/basic-2-20130206/artifact:1#My_Treatment1"),
                                     null,
                                     ValueFactoryImpl.getInstance().createURI(
                                             "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial"),
                                     false, updatedArtifact.getVersionIRI().toOpenRDFURI())).size());
         }
         finally
         {
             if(permanentConnection != null && permanentConnection.isOpen())
             {
                 permanentConnection.close();
             }
             permanentConnection = null;
             
             if(managementConnection != null && managementConnection.isOpen())
             {
                 managementConnection.close();
             }
             managementConnection = null;
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      */
     @Test
     public final void testUpdateArtifactWithDanglingObjects() throws Exception
     {
         final InferredOWLOntologyID updatedArtifact =
                 this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 12,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                         TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                         TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFY_DEMO_INVESTIGATION, RDFFormat.TURTLE,
                         UpdatePolicy.REPLACE_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                         DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
         
         // verify:
         RepositoryConnection managementConnection = null;
         RepositoryConnection permanentConnection = null;
         try
         {
             managementConnection = this.testRepositoryManager.getManagementRepository().getConnection();
             Set<? extends OWLOntologyID> schemaImports = this.testArtifactManager.getSchemaImports(updatedArtifact);
             permanentConnection = this.testRepositoryManager.getPermanentRepository(schemaImports).getConnection();
             
             this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES - 12, managementConnection);
             
             // verify: dangling objects are no longer in the updated artifact
             final String[] danglingObjects =
                     { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                             "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_genotype_3",
                             "http://purl.org/podd/basic-2-20130206/artifact:1#Sequence_A", };
             for(final String deletedObject : danglingObjects)
             {
                 final URI deletedObjURI = ValueFactoryImpl.getInstance().createURI(deletedObject);
                 Assert.assertEquals(
                         "Dangling object should not exist",
                         0,
                         Iterations.asList(
                                 permanentConnection.getStatements(deletedObjURI, null, null, false, updatedArtifact
                                         .getVersionIRI().toOpenRDFURI())).size());
             }
         }
         finally
         {
             if(permanentConnection != null && permanentConnection.isOpen())
             {
                 permanentConnection.close();
             }
             permanentConnection = null;
             
             if(managementConnection != null && managementConnection.isOpen())
             {
                 managementConnection.close();
             }
             managementConnection = null;
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * .
      */
     @Test
     public final void testUpdateArtifactWithDanglingObjectsWithoutForce() throws Exception
     {
         try
         {
             this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 12,
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                     TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                     TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFY_DEMO_INVESTIGATION, RDFFormat.TURTLE,
                     UpdatePolicy.REPLACE_EXISTING, DanglingObjectPolicy.REPORT,
                     DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
             Assert.fail("Should have thrown an Exception to indicate that dangling objects will result");
         }
         catch(final DisconnectedObjectException e)
         {
             Assert.assertEquals("Update leads to disconnected PODD objects", e.getMessage());
             Assert.assertEquals(4, e.getDisconnectedObjects().size());
             
             final String[] danglingObjects =
                     { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                             "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_genotype_3",
                             "http://purl.org/podd/basic-2-20130206/artifact:1#Sequence_A", };
             for(final String danglingObject : danglingObjects)
             {
                 final URI danglingObjectURI = ValueFactoryImpl.getInstance().createURI(danglingObject);
                 Assert.assertTrue("Expected dangling object not present",
                         e.getDisconnectedObjects().contains(danglingObjectURI));
             }
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
      * . Tests attempting to update an artifact when an old version of the artifact has been used as
      * the basis of the update. This could occur due to concurrent updates.
      * 
      * Currently verifies that a failure occurs.
      */
     @Test
     public final void testUpdateArtifactWithOldVersion() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // upload artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         // upload another version of artifact
         final InputStream inputStream2 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final Model model =
                 this.testArtifactManager.updateArtifact(artifactIDv1.getOntologyIRI().toOpenRDFURI(), artifactIDv1
                         .getVersionIRI().toOpenRDFURI(), Collections.<URI> emptyList(), inputStream2, RDFFormat.TURTLE,
                         UpdatePolicy.REPLACE_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                         DataReferenceVerificationPolicy.DO_NOT_VERIFY);
         final InferredOWLOntologyID artifactIDv2 = OntologyUtils.modelToOntologyIDs(model).get(0);
         
         this.verifyLoadedArtifact(artifactIDv2, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         System.out.println(artifactIDv1);
         System.out.println(artifactIDv2);
         
         final InputStream editInputStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFY_DEMO_INVESTIGATION);
         try
         {
             this.testArtifactManager.updateArtifact(artifactIDv1.getOntologyIRI().toOpenRDFURI(), artifactIDv1
                     .getVersionIRI().toOpenRDFURI(), Collections.<URI> emptyList(), editInputStream, RDFFormat.TURTLE,
                     UpdatePolicy.REPLACE_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                     DataReferenceVerificationPolicy.DO_NOT_VERIFY);
             Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
         }
         catch(final UnmanagedArtifactVersionException e)
         {
             Assert.assertEquals("Exception not due to the expected artifact version", artifactIDv1.getVersionIRI(),
                     e.getUnmanagedVersionIRI());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
      * .
      */
     @Test
     public final void testUpdateSchemaImportsEmptyNewSchemas() throws Exception
     {
         final List<InferredOWLOntologyID> schemaOntologies = this.loadVersion1SchemaOntologies();
         
         // upload artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         try
         {
             this.testArtifactManager.updateSchemaImports(new InferredOWLOntologyID(artifactIDv1.getOntologyIRI(),
                     artifactIDv1.getVersionIRI(), artifactIDv1.getInferredOntologyIRI()),
                     new LinkedHashSet<OWLOntologyID>(schemaOntologies), new HashSet<OWLOntologyID>());
             Assert.fail("Removing essential ontologies should generate an OWL-related exception");
         }
         catch(final OntologyNotInProfileException e)
         {
             Assert.assertEquals(artifactIDv1.getOntologyIRI(), e.getOntology().getOntologyID().getOntologyIRI());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
      * .
      */
     @Test
     public final void testUpdateSchemaImportsToVersion2() throws Exception
     {
         final List<InferredOWLOntologyID> version1SchemaOntologies = this.loadVersion1SchemaOntologies();
         
         // upload artifact while we only have version 1 schemas loaded
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         // Upload version 2 schemas
         final List<InferredOWLOntologyID> version2SchemaOntologies = this.loadVersion2SchemaOntologies();
         
         // Update from version 1 to version 2
         this.testArtifactManager.updateSchemaImports(new InferredOWLOntologyID(artifactIDv1.getOntologyIRI(),
                 artifactIDv1.getVersionIRI(), artifactIDv1.getInferredOntologyIRI()), new LinkedHashSet<OWLOntologyID>(
                 version1SchemaOntologies), new LinkedHashSet<OWLOntologyID>(version2SchemaOntologies));
         
         final InferredOWLOntologyID afterUpdate = this.testArtifactManager.getArtifact(artifactIDv1.getOntologyIRI());
         
         Assert.assertEquals(afterUpdate.getOntologyIRI(), artifactIDv1.getOntologyIRI());
         // Verify version for artifact changed after the schema import update, to ensure that they
         // are distinct internally
         Assert.assertNotEquals(afterUpdate.getVersionIRI(), artifactIDv1.getVersionIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
      * .
      */
     @Test
     public final void testUpdateSchemaImportsToVersion2AfterReload() throws Exception
     {
         final List<InferredOWLOntologyID> version1SchemaOntologies = this.loadVersion1SchemaOntologies();
         
         // upload artifact while we only have version 1 schemas loaded
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         // Simulate reloading all of the application except for the repository, which is independent
         // and should be long lived in practice
         this.setupNonRepositoryManagers();
         
         // Upload version 2 schemas
         final List<InferredOWLOntologyID> version2SchemaOntologies = this.loadVersion2SchemaOntologies();
         
         // Update from version 1 to version 2
         this.testArtifactManager.updateSchemaImports(new InferredOWLOntologyID(artifactIDv1.getOntologyIRI(),
                 artifactIDv1.getVersionIRI(), artifactIDv1.getInferredOntologyIRI()), new LinkedHashSet<OWLOntologyID>(
                 version1SchemaOntologies), new LinkedHashSet<OWLOntologyID>(version2SchemaOntologies));
         
         final InferredOWLOntologyID afterUpdate = this.testArtifactManager.getArtifact(artifactIDv1.getOntologyIRI());
         
         Assert.assertEquals(afterUpdate.getOntologyIRI(), artifactIDv1.getOntologyIRI());
         // Verify version for artifact changed after the schema import update, to ensure that they
         // are distinct internally
         Assert.assertNotEquals(afterUpdate.getVersionIRI(), artifactIDv1.getVersionIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
      * .
      */
     @Test
     public final void testUpdateSchemaImportsToVersion2AfterReload2() throws Exception
     {
         final List<InferredOWLOntologyID> version1SchemaOntologies = this.loadVersion1SchemaOntologies();
         
         // upload artifact while we only have version 1 schemas loaded
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         // Simulate reloading all of the application except for the repository, which is independent
         // and should be long lived in practice
         this.setupNonRepositoryManagers();
         
         // Upload version 2 schemas
         final List<InferredOWLOntologyID> version2SchemaOntologies = this.loadVersion2SchemaOntologies();
         
         // Simulate reloading all of the application except for the repository, which is independent
         // and should be long lived in practice
         this.setupNonRepositoryManagers();
         
         // Update from version 1 to version 2
         this.testArtifactManager.updateSchemaImports(new InferredOWLOntologyID(artifactIDv1.getOntologyIRI(),
                 artifactIDv1.getVersionIRI(), artifactIDv1.getInferredOntologyIRI()), new LinkedHashSet<OWLOntologyID>(
                 version1SchemaOntologies), new LinkedHashSet<OWLOntologyID>(version2SchemaOntologies));
         
         final InferredOWLOntologyID afterUpdate = this.testArtifactManager.getArtifact(artifactIDv1.getOntologyIRI());
         
         Assert.assertEquals(afterUpdate.getOntologyIRI(), artifactIDv1.getOntologyIRI());
         // Verify version for artifact changed after the schema import update, to ensure that they
         // are distinct internally
         Assert.assertNotEquals(afterUpdate.getVersionIRI(), artifactIDv1.getVersionIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
      * .
      */
     @Test
     public final void testUpdateSchemaImportsEmptyOldSchemas() throws Exception
     {
         final List<InferredOWLOntologyID> schemaOntologies = this.loadVersion1SchemaOntologies();
         
         // upload artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final InferredOWLOntologyID updateSchemaImports =
                 this.testArtifactManager.updateSchemaImports(new InferredOWLOntologyID(artifactIDv1.getOntologyIRI(),
                         artifactIDv1.getVersionIRI(), artifactIDv1.getInferredOntologyIRI()),
                         new HashSet<OWLOntologyID>(), new LinkedHashSet<OWLOntologyID>(schemaOntologies));
         
         Assert.assertEquals(updateSchemaImports.getOntologyIRI(), artifactIDv1.getOntologyIRI());
         Assert.assertNotEquals(updateSchemaImports.getVersionIRI(), artifactIDv1.getVersionIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
      * .
      */
     @Ignore("TODO: Enable manual upload tests")
     @Test
     public final void testUpdateSchemaImportsEmptySchemas() throws Exception
     {
         final List<InferredOWLOntologyID> schemaOntologies = this.loadVersion1SchemaOntologies();
         
         // upload artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         final InferredOWLOntologyID updateSchemaImports =
                 this.testArtifactManager.updateSchemaImports(new InferredOWLOntologyID(artifactIDv1.getOntologyIRI(),
                         artifactIDv1.getVersionIRI(), artifactIDv1.getInferredOntologyIRI()),
                         new HashSet<OWLOntologyID>(), new HashSet<OWLOntologyID>());
         
         Assert.assertEquals(updateSchemaImports.getOntologyIRI(), artifactIDv1.getOntologyIRI());
         Assert.assertNotEquals(updateSchemaImports.getVersionIRI(), artifactIDv1.getVersionIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
      * .
      */
     @Test
     public final void testUpdateSchemaImportsNull() throws Exception
     {
         try
         {
             this.testArtifactManager.updateSchemaImports(null, new HashSet<OWLOntologyID>(),
                     new HashSet<OWLOntologyID>());
             Assert.fail("Should have thrown an IllegalArgumentException");
         }
         catch(final IllegalArgumentException e)
         {
             Assert.assertTrue(e.getMessage().contains("Artifact was null"));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
      * .
      */
     @Test
     public final void testUpdateSchemaImportsUnmanagedArtifact() throws Exception
     {
         try
         {
             this.testArtifactManager.updateSchemaImports(
                     new InferredOWLOntologyID(IRI.create("urn:test:ontology:nonexistent"), IRI
                             .create("urn:test:ontology:version:nonexistent"), IRI
                             .create("urn:test:ontology:inferredversion:nonexistent")), new HashSet<OWLOntologyID>(),
                     new HashSet<OWLOntologyID>());
             Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             Assert.assertEquals("Exception not due to the expected artifact IRI",
                     IRI.create("urn:test:ontology:nonexistent"), e.getUnmanagedOntologyIRI());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
      * .
      */
     @Test
     public final void testUpdateSchemaImportsUnmanagedVersion() throws Exception
     {
         this.loadVersion1SchemaOntologies();
         
         // upload artifact
         final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         final InferredOWLOntologyID artifactIDv1 =
                 this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
         this.verifyLoadedArtifact(artifactIDv1, 12, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                 TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
         
         try
         {
             this.testArtifactManager.updateSchemaImports(
                     new InferredOWLOntologyID(artifactIDv1.getOntologyIRI(), IRI
                             .create("urn:test:ontology:version:nonexistent"), artifactIDv1.getInferredOntologyIRI()),
                     new HashSet<OWLOntologyID>(), new HashSet<OWLOntologyID>());
             Assert.fail("Should have thrown an UnmanagedArtifactVersionException");
         }
         catch(final UnmanagedArtifactVersionException e)
         {
             Assert.assertEquals("Exception not due to the expected artifact version",
                     IRI.create("urn:test:ontology:version:nonexistent"), e.getUnmanagedVersionIRI());
         }
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
         this.loadVersion1SchemaOntologies();
         final InputStream inputStream = this.getClass().getResourceAsStream("/test/artifacts/basic-20130206.ttl");
         final RDFFormat readFormat = RDFFormat.TURTLE;
         final InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, readFormat);
         
         this.dumpRdfToFile(resultArtifactId.getInferredOntologyIRI().toOpenRDFURI(),
                 "/home/kutila/basic-20130206-inferred.ttl", RDFFormat.TURTLE);
         
         /*
          * String[] contexts = { // "http://purl.org/podd/ns/version/dcTerms/1",
          * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/dcTerms/1", //
          * "http://purl.org/podd/ns/version/foaf/1",
          * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/foaf/1", //
          * "http://purl.org/podd/ns/version/poddUser/1",
          * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddUser/1", //
          * "http://purl.org/podd/ns/version/poddBase/1",
          * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddBase/1", //
          * "http://purl.org/podd/ns/version/poddScience/1",
          * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddScience/1", //
          * "http://purl.org/podd/ns/version/poddPlant/1",
          * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddPlant/1", };
          * 
          * String[] fileNames = { // "dcTerms", "dcTermsInferred", // "foaf", "foafInferred", //
          * "poddUser", "poddUserInferred", // "poddBase", "poddBaseInferred", // "poddScience",
          * "poddScienceInferred", // "poddPlant", "poddPlantInferred", };
          * 
          * for(int i = 0; i < contexts.length; i++) { URI context =
          * ValueFactoryImpl.getInstance().createURI(contexts[i]); final RDFFormat writeFormat =
          * RDFFormat.RDFXML; String path = "/home/kutila/";
          * 
          * dumpRdfToFile(context, (path + fileNames[i]), writeFormat); }
          */
     }
     
     /**
      * Helper method to verify the contents of artifact management graph
      * 
      * @param repositoryConnection
      * @param graphSize
      *            Expected size of the graph
      * @param managementGraph
      *            The Graph/context to be tested
      * @param ontologyIRI
      *            The ontology/artifact
      * @param versionIRI
      *            Version IRI of the ontology/artifact
      * @param inferredVersionIRI
      *            Inferred version of the ontology/artifact
      * @throws RepositoryException
      * @throws Exception
      */
     private void verifyArtifactManagementGraphContents(final RepositoryConnection repositoryConnection,
             final int graphSize, final URI managementGraph, final IRI ontologyIRI, final IRI versionIRI,
             final IRI inferredVersionIRI) throws RepositoryException
     {
         Assert.assertEquals("Graph not of expected size", graphSize, repositoryConnection.size(managementGraph));
         
         // verify: OWL_VERSION
         final List<Statement> stmtList =
                 Iterations.asList(repositoryConnection.getStatements(ontologyIRI.toOpenRDFURI(), PODD.OWL_VERSION_IRI,
                         null, false, managementGraph));
         Assert.assertEquals("Graph should have one OWL_VERSION statement", 1, stmtList.size());
         Assert.assertEquals("Wrong OWL_VERSION in Object", versionIRI.toString(), stmtList.get(0).getObject()
                 .toString());
         
         // verify: OMV_CURRENT_VERSION
         final List<Statement> currentVersionStatementList =
                 Iterations.asList(repositoryConnection.getStatements(ontologyIRI.toOpenRDFURI(),
                         PODD.OMV_CURRENT_VERSION, null, false, managementGraph));
         Assert.assertEquals("Graph should have one OMV_CURRENT_VERSION statement", 1,
                 currentVersionStatementList.size());
         Assert.assertEquals("Wrong OMV_CURRENT_VERSION in Object", versionIRI.toString(), currentVersionStatementList
                 .get(0).getObject().toString());
         
         // verify: INFERRED_VERSION
         final List<Statement> inferredVersionStatementList =
                 Iterations.asList(repositoryConnection.getStatements(versionIRI.toOpenRDFURI(),
                         PODD.PODD_BASE_INFERRED_VERSION, null, false, managementGraph));
         Assert.assertEquals("Graph should have one INFERRED_VERSION statement", 1, inferredVersionStatementList.size());
         Assert.assertEquals("Wrong INFERRED_VERSION in Object", inferredVersionIRI.toString(),
                 inferredVersionStatementList.get(0).getObject().toString());
         
         // verify: CURRENT_INFERRED_VERSION
         final List<Statement> currentInferredVersionStatementList =
                 Iterations.asList(repositoryConnection.getStatements(ontologyIRI.toOpenRDFURI(),
                         PODD.PODD_BASE_CURRENT_INFERRED_VERSION, null, false, managementGraph));
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
      * @throws OpenRDFException
      * @throws RepositoryException
      * @throws UnmanagedSchemaIRIException
      * @throws UnmanagedArtifactVersionException
      * @throws UnmanagedArtifactIRIException
      * @throws SchemaManifestException
      * @throws IOException
      * @throws UnsupportedRDFormatException
      */
     private void verifyLoadedArtifact(final InferredOWLOntologyID inferredOntologyId, final int mgtGraphSize,
             final long assertedStatementCount, final long inferredStatementCount, final boolean isPublished)
         throws RepositoryException, OpenRDFException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException,
         UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException, IOException
     
     {
         // verify: ontology ID has all details
         Assert.assertNotNull("Null ontology ID", inferredOntologyId);
         Assert.assertNotNull("Null ontology IRI", inferredOntologyId.getOntologyIRI());
         Assert.assertNotNull("Null ontology version IRI", inferredOntologyId.getVersionIRI());
         Assert.assertNotNull("Null inferred ontology IRI", inferredOntologyId.getInferredOntologyIRI());
         
         RepositoryConnection managementConnection = null;
         RepositoryConnection permanentConnection = null;
         try
         {
             Set<? extends OWLOntologyID> schemaOntologies =
                     this.testArtifactManager.getSchemaImports(inferredOntologyId);
             
             Assert.assertFalse("Could not find schema imports, so cannot find permanent repository!",
                     schemaOntologies.isEmpty());
             
             managementConnection = this.testRepositoryManager.getManagementRepository().getConnection();
             
             permanentConnection = this.testRepositoryManager.getPermanentRepository(schemaOntologies).getConnection();
             
             if(permanentConnection.size(inferredOntologyId.getVersionIRI().toOpenRDFURI()) != assertedStatementCount)
             {
                 DebugUtils.printContents(permanentConnection, inferredOntologyId.getVersionIRI().toOpenRDFURI());
             }
             // verify: size of asserted graph
             Assert.assertEquals("Incorrect number of asserted statements for artifact", assertedStatementCount,
                     permanentConnection.size(inferredOntologyId.getVersionIRI().toOpenRDFURI()));
             
             // verify: size of inferred graph
             Assert.assertEquals("Incorrect number of inferred statements for artifact", inferredStatementCount,
                     permanentConnection.size(inferredOntologyId.getInferredOntologyIRI().toOpenRDFURI()));
             
             // verify: artifact management graph contents
             this.verifyArtifactManagementGraphContents(managementConnection, mgtGraphSize,
                     this.testRepositoryManager.getArtifactManagementGraph(), inferredOntologyId.getOntologyIRI(),
                     inferredOntologyId.getVersionIRI(), inferredOntologyId.getInferredOntologyIRI());
         }
         finally
         {
             if(permanentConnection != null && permanentConnection.isOpen())
             {
                 permanentConnection.close();
             }
             permanentConnection = null;
             if(managementConnection != null && managementConnection.isOpen())
             {
                 managementConnection.close();
             }
             managementConnection = null;
         }
     }
     
     /**
      * Helper method to verify that an updated artifact has expected version IRI etc.
      * 
      * @param updatedArtifact
      * @param expectedVersionIri
      * @param expectedConcreteStatementCount
      * @param managementConnection
      * @throws Exception
      */
     private void verifyUpdatedArtifact(final InferredOWLOntologyID updatedArtifact, final String expectedVersionIri,
             final long expectedConcreteStatementCount, final RepositoryConnection managementConnection)
         throws Exception
     {
         Set<? extends OWLOntologyID> schemaImports = this.testArtifactManager.getSchemaImports(updatedArtifact);
         
         RepositoryConnection permanentConnection =
                 this.testRepositoryManager.getPermanentRepository(schemaImports).getConnection();
         
         try
         {
             Assert.assertEquals("Unexpected concrete statement count", expectedConcreteStatementCount,
                     permanentConnection.size(updatedArtifact.getVersionIRI().toOpenRDFURI()));
             
             // verify: owl:versionIRI incremented in graph
             final List<Statement> versionIRIs =
                     Iterations.asList(permanentConnection.getStatements(null, PODD.OWL_VERSION_IRI, null, false,
                             updatedArtifact.getVersionIRI().toOpenRDFURI()));
             Assert.assertEquals("Should have only 1 version IRI", 1, versionIRIs.size());
             Assert.assertEquals("Version IRI not expected value", expectedVersionIri, versionIRIs.get(0).getObject()
                     .stringValue());
             
             // verify: current version updated in management graph
             final InferredOWLOntologyID currentArtifactVersion =
                     this.testArtifactManager.getSesameManager().getCurrentArtifactVersion(
                             updatedArtifact.getOntologyIRI(), managementConnection, this.artifactGraph);
             Assert.assertEquals("Unexpected Version IRI in management graph", expectedVersionIri,
                     currentArtifactVersion.getVersionIRI().toString());
         }
         finally
         {
             permanentConnection.close();
         }
     }
     
 }
