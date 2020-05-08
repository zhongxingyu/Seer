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
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.semanticweb.owlapi.io.UnparsableOntologyException;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 
 import com.github.ansell.propertyutil.PropertyUtil;
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.api.PoddSchemaManager;
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.exception.EmptyOntologyException;
 import com.github.podd.exception.PoddException;
 import com.github.podd.exception.SchemaManifestException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * Abstract test to verify that the PoddSchemaManager API contract is followed by implementations.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public abstract class AbstractPoddSchemaManagerTest
 {
     protected PoddSchemaManager testSchemaManager;
     private PoddOWLManager testOwlManager;
     private PoddRepositoryManager testRepositoryManager;
     private OWLOntologyManager owlapiManager;
     private PoddSesameManager testSesameManager;
     
     /**
      * 
      * @return A new instance of {@link OWLOntologyManager}, for each call to this method.
      */
     protected abstract OWLOntologyManager getNewOwlOntologyManagerInstance();
     
     /**
      * 
      * @return A new instance of {@link PoddOWLManager}, for each call to this method.
      */
     protected abstract PoddOWLManager getNewPoddOwlManagerInstance();
     
     /**
      * 
      * @return A new instance of {@link PoddRepositoryManager}, for each call to this method.
      */
     protected abstract PoddRepositoryManager getNewPoddRepositoryManagerInstance();
     
     /**
      * 
      * @return A new instance of {@link PoddSchemaManager}, for each call to this method.
      */
     protected abstract PoddSchemaManager getNewPoddSchemaManagerInstance();
     
     /**
      * 
      * @return A new instance of {@link PoddSesameManager}, for each call to this method.
      */
     protected abstract PoddSesameManager getNewPoddSesameManagerInstance();
     
     /**
      * 
      * @return A new empty instance of an implementation of {@link OWLReasonerFactory}.
      */
     protected abstract OWLReasonerFactory getNewReasonerFactory();
     
     /**
      * Internal helper test method for
      * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntologyVersion(IRI)}
      * 
      * @param inputVersionIri
      *            The test input
      * @param expectedOntologyIri
      *            Ontology IRI of expected result
      * @param expectedVersionIri
      *            Version IRI of expected result
      * @throws Exception
      */
     private final void internalTestGetSchemaOntologyVersion(final String inputVersionIri,
             final String expectedOntologyIri, final String expectedVersionIri) throws Exception
     {
         // prepare: load schema ontologies into PODD
         this.loadSchemaOntologies();
         final InputStream in = this.getClass().getResourceAsStream("/test/ontologies/poddPlantV2.owl");
         this.testSchemaManager.uploadSchemaOntology(in, RDFFormat.RDFXML);
         
         final InferredOWLOntologyID ontologyID =
                 this.testSchemaManager.getSchemaOntologyVersion(IRI.create(inputVersionIri));
         Assert.assertEquals("Expected version IRI does not match received value", IRI.create(expectedVersionIri),
                 ontologyID.getVersionIRI());
         Assert.assertEquals("Expected ontology IRI does not match received value", IRI.create(expectedOntologyIri),
                 ontologyID.getOntologyIRI());
     }
     
     /**
      * This internal method loads schema ontologies to PODD. Should be used as a setUp() mechanism
      * where needed.
      */
     private void loadSchemaOntologies() throws Exception
     {
         this.loadSchemaOntologies(new PropertyUtil("podd").get(PoddRdfConstants.KEY_SCHEMAS,
                 PoddRdfConstants.PATH_DEFAULT_SCHEMAS));
     }
     
     private void loadSchemaOntologies(final String schemaManifest) throws OpenRDFException, IOException, OWLException,
         PoddException
     {
         Model model = null;
         try (final InputStream schemaManifestStream = this.getClass().getResourceAsStream(schemaManifest);)
         {
             final RDFFormat format = Rio.getParserFormatForFileName(schemaManifest, RDFFormat.RDFXML);
             model = Rio.parse(schemaManifestStream, "", format);
         }
         this.testSchemaManager.uploadSchemaOntologies(model);
         
         // final String[] schemaResourcePaths =
         // { PoddRdfConstants.PATH_PODD_DCTERMS, PoddRdfConstants.PATH_PODD_FOAF,
         // PoddRdfConstants.PATH_PODD_USER,
         // PoddRdfConstants.PATH_PODD_BASE, PoddRdfConstants.PATH_PODD_SCIENCE,
         // PoddRdfConstants.PATH_PODD_PLANT,
         // // PoddRdfConstants.PATH_PODD_ANIMAL,
         // };
         // for(final String schemaResourcePath : schemaResourcePaths)
         // {
         // final InputStream in = this.getClass().getResourceAsStream(schemaResourcePath);
         // this.testSchemaManager.uploadSchemaOntology(in, RDFFormat.RDFXML);
         // }
     }
     
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception
     {
         this.testSchemaManager = this.getNewPoddSchemaManagerInstance();
         
         this.testRepositoryManager = this.getNewPoddRepositoryManagerInstance();
         this.testSchemaManager.setRepositoryManager(this.testRepositoryManager);
         
         this.testSesameManager = this.getNewPoddSesameManagerInstance();
         this.testSchemaManager.setSesameManager(this.testSesameManager);
         
         this.testOwlManager = this.getNewPoddOwlManagerInstance();
         this.testOwlManager.setReasonerFactory(this.getNewReasonerFactory());
         
         this.owlapiManager = this.getNewOwlOntologyManagerInstance();
         this.testOwlManager.setOWLOntologyManager(this.owlapiManager);
         this.testSchemaManager.setOwlManager(this.testOwlManager);
     }
     
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception
     {
         this.testSchemaManager = null;
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testDownloadSchemaOntologyMatchingOntologyID() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testDownloadSchemaOntologyNotMatchingVersion() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testDownloadSchemaOntologyNullOntologyID() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testDownloadSchemaOntologyNullOutputStream() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testDownloadSchemaOntologyOnlyOntologyIRI() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testDownloadSchemaOntologyWithInferencesMatchingOntologyID() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testDownloadSchemaOntologyWithInferencesNoInferencesFound() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testDownloadSchemaOntologyWithInferencesNotMatchingVersion() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testDownloadSchemaOntologyWithInferencesNullOntologyID() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testDownloadSchemaOntologyWithInferencesNullOutputStream() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testDownloadSchemaOntologyWithInferencesOnlyOntologyIRI() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
      * . Passes in an ontology IRI and retrieves the current version of the Ontology.
      * 
      */
     @Test
     public final void testGetCurrentSchemaOntologyVersionMatchesOntologyIRI() throws Exception
     {
         // prepare: load schema ontologies into PODD
         this.loadSchemaOntologies();
         final InputStream in = this.getClass().getResourceAsStream("/test/ontologies/poddPlantV2.owl");
         this.testSchemaManager.uploadSchemaOntology(in, RDFFormat.RDFXML);
         
         final String[] testIRIs =
                 { "http://purl.org/podd/ns/poddUser", "http://purl.org/podd/ns/poddBase",
                         "http://purl.org/podd/ns/poddScience", "http://purl.org/podd/ns/poddPlant", };
         
         final String[] expectedVersionIRIs =
                 { "http://purl.org/podd/ns/version/poddUser/1", "http://purl.org/podd/ns/version/poddBase/1",
                         "http://purl.org/podd/ns/version/poddScience/1", "http://purl.org/podd/ns/version/poddPlant/2", };
         
         for(int i = 0; i < testIRIs.length; i++)
         {
             final IRI testIRI = IRI.create(testIRIs[i]);
             final InferredOWLOntologyID ontologyID = this.testSchemaManager.getCurrentSchemaOntologyVersion(testIRI);
             Assert.assertEquals("Input IRI does not match ontology IRI of current version", testIRI,
                     ontologyID.getOntologyIRI());
             Assert.assertEquals("Expected Version IRI does not match current version",
                     IRI.create(expectedVersionIRIs[i]), ontologyID.getVersionIRI());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
      * . Passes in a version IRI (of the current version) and retrieves the current version of the
      * Ontology.
      */
     @Test
     public final void testGetCurrentSchemaOntologyVersionMatchesOntologyVersionIRICurrent() throws Exception
     {
         // prepare: load schema ontologies into PODD
         this.loadSchemaOntologies();
         final InputStream in = this.getClass().getResourceAsStream("/test/ontologies/poddPlantV2.owl");
         this.testSchemaManager.uploadSchemaOntology(in, RDFFormat.RDFXML);
         
         final String[] testIRIs =
                 { "http://purl.org/podd/ns/version/poddUser/1", "http://purl.org/podd/ns/version/poddBase/1",
                         "http://purl.org/podd/ns/version/poddScience/1", "http://purl.org/podd/ns/version/poddPlant/2", };
         
         for(final String testIRI2 : testIRIs)
         {
             final IRI testIRI = IRI.create(testIRI2);
             final InferredOWLOntologyID ontologyID = this.testSchemaManager.getCurrentSchemaOntologyVersion(testIRI);
             Assert.assertEquals("Input IRI does not match Version IRI of current version", testIRI,
                     ontologyID.getVersionIRI());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
      * . Passes in a version IRI (of an older version) and retrieves the current version of the
      * Ontology.
      */
     @Test
     public final void testGetCurrentSchemaOntologyVersionMatchesOntologyVersionIRINotCurrent() throws Exception
     {
         // prepare: load schema ontologies into PODD
         this.loadSchemaOntologies();
         final InputStream in = this.getClass().getResourceAsStream("/test/ontologies/poddPlantV2.owl");
         this.testSchemaManager.uploadSchemaOntology(in, RDFFormat.RDFXML);
         
         final String[] testIRIs =
                 { "http://purl.org/podd/ns/version/poddUser/1", "http://purl.org/podd/ns/version/poddBase/1",
                         "http://purl.org/podd/ns/version/poddScience/1", "http://purl.org/podd/ns/version/poddPlant/1", // an
                                                                                                                         // older
                                                                                                                         // version
                                                                                                                         // of
                                                                                                                         // PODD:Plant
                 };
         
         final String[] expectedVersionIRIs =
                 { "http://purl.org/podd/ns/version/poddUser/1", "http://purl.org/podd/ns/version/poddBase/1",
                         "http://purl.org/podd/ns/version/poddScience/1", "http://purl.org/podd/ns/version/poddPlant/2", // expected
                                                                                                                         // current
                                                                                                                         // Version
                                                                                                                         // IRI
                                                                                                                         // of
                                                                                                                         // PODD:Plant
                 };
         
         for(int i = 0; i < testIRIs.length; i++)
         {
             final IRI testIRI = IRI.create(testIRIs[i]);
             final InferredOWLOntologyID ontologyID = this.testSchemaManager.getCurrentSchemaOntologyVersion(testIRI);
             Assert.assertEquals("Expected current version IRI does not match received value",
                     IRI.create(expectedVersionIRIs[i]), ontologyID.getVersionIRI());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      */
     @Test
     public final void testGetCurrentSchemaOntologyVersionNoMatches() throws Exception
     {
         final IRI testInputIri = IRI.create("http://purl.org/podd/noSuchSchema");
         try
         {
             this.testSchemaManager.getCurrentSchemaOntologyVersion(testInputIri);
             Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
         }
         catch(final UnmanagedSchemaIRIException e)
         {
             Assert.assertEquals("This IRI does not refer to a managed ontology", e.getMessage());
             Assert.assertEquals(testInputIri, e.getOntologyID());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      */
     @Test
     public final void testGetCurrentSchemaOntologyVersionNull() throws Exception
     {
         try
         {
             this.testSchemaManager.getCurrentSchemaOntologyVersion(null);
             Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
         }
         catch(final UnmanagedSchemaIRIException e)
         {
             Assert.assertEquals("NULL is not a managed schema ontology", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      * Test that the designated current version is retrieved when an ontology IRI is given and there
      * are multiple versions available.
      */
     @Ignore
     @Test
     public final void testGetSchemaOntologyIRIMatchesOntologyIRIMultipleVersions() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     @Test
     public final void testGetSchemaOntologies() throws Exception
     {
         this.loadSchemaOntologies();
         final Set<InferredOWLOntologyID> schemaOntologies = this.testSchemaManager.getSchemaOntologies();
         Assert.assertEquals(6, schemaOntologies.size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
      * .
      */
     @Ignore
     @Test
     public final void testGetSchemaOntologyIRIMatchesOntologyIRIOneVersion() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
      * .
      */
     @Ignore
     @Test
     public final void testGetSchemaOntologyIRIMatchesVersionIRI() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
      * .
      */
     @Ignore
     @Test
     public final void testGetSchemaOntologyIRINull() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Ignore
     @Test
     public final void testGetSchemaOntologyOWLOntologyIDNullOntologyID() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Ignore
     @Test
     public final void testGetSchemaOntologyOWLOntologyIDNullVersionIRI() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Ignore
     @Test
     public final void testGetSchemaOntologyOWLOntologyIDOntologyExists() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Ignore
     @Test
     public final void testGetSchemaOntologyOWLOntologyIDOntologyIRIDoesNotExist() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Ignore
     @Test
     public final void testGetSchemaOntologyOWLOntologyIDOntologyVersionDoesNotExist() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#getSchemaOntologyVersion(IRI)} .
      * Passes in a version IRI (of the current version) and retrieves the same version of the
      * Ontology.
      */
     @Test
     public final void testGetSchemaOntologyVersionMatchesOntologyIRI() throws Exception
     {
         this.internalTestGetSchemaOntologyVersion("http://purl.org/podd/ns/poddPlant",
                 "http://purl.org/podd/ns/poddPlant", "http://purl.org/podd/ns/version/poddPlant/2");
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#getSchemaOntologyVersion(IRI)} .
      * Passes in a version IRI (of the current version) and retrieves the same version of the
      * Ontology.
      */
     @Test
     public final void testGetSchemaOntologyVersionMatchesOntologyVersionIRICurrent() throws Exception
     {
         this.internalTestGetSchemaOntologyVersion("http://purl.org/podd/ns/version/poddPlant/2",
                 "http://purl.org/podd/ns/poddPlant", "http://purl.org/podd/ns/version/poddPlant/2");
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#getSchemaOntologyVersion(IRI)} .
      * Passes in a version IRI (of an older version) and retrieves that version of the Ontology.
      */
     @Test
     public final void testGetSchemaOntologyVersionMatchesOntologyVersionIRINotCurrent() throws Exception
     {
         this.internalTestGetSchemaOntologyVersion("http://purl.org/podd/ns/version/poddPlant/1",
                 "http://purl.org/podd/ns/poddPlant", "http://purl.org/podd/ns/version/poddPlant/1");
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#getSchemaOntologyVersion(IRI)} .
      */
     @Test
     public final void testGetSchemaOntologyVersionNoMatches() throws Exception
     {
         final IRI inputVersionIri = IRI.create("http://purl.org/podd/ns/version/poddPlant/999");
         
         // prepare: load schema ontologies into PODD
         this.loadSchemaOntologies();
         final InputStream in = this.getClass().getResourceAsStream("/test/ontologies/poddPlantV2.owl");
         this.testSchemaManager.uploadSchemaOntology(in, RDFFormat.RDFXML);
         
         try
         {
             this.testSchemaManager.getSchemaOntologyVersion(inputVersionIri);
             Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
         }
         catch(final UnmanagedSchemaIRIException e)
         {
             Assert.assertEquals("This IRI does not refer to a managed ontology", e.getMessage());
             Assert.assertEquals(inputVersionIri, e.getOntologyID());
         }
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#getSchemaOntologyVersion(IRI)} .
      * Passes in null as the version IRI.
      */
     @Test
     public final void testGetSchemaOntologyVersionNull() throws Exception
     {
         try
         {
             this.testSchemaManager.getSchemaOntologyVersion(null);
             Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
         }
         catch(final UnmanagedSchemaIRIException e)
         {
             Assert.assertEquals("NULL is not a managed schema ontology", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#setCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Ignore
     @Test
     public final void testSetCurrentSchemaOntologyVersionOneVersionOfOntology() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#setCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Ignore
     @Test
     public final void testSetCurrentSchemaOntologyVersionTwoVersionsOfOntologyChange() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#setCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      */
     @Ignore
     @Test
     public final void testSetCurrentSchemaOntologyVersionTwoVersionsOfOntologyNoChange() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntologies(Model)} .
      * 
      * Loads the standard set of PODD ontologies that are used.
      * 
      */
     @Test
     public final void testUploadSchemaOntologies() throws Exception
     {
         this.loadSchemaOntologies();
         
         Assert.assertEquals(6, this.testSchemaManager.getCurrentSchemaOntologies().size());
         Assert.assertEquals(6, this.testSchemaManager.getSchemaOntologies().size());
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntologies(Model)} .
      * 
      * Loads a set of test ontologies that each have a single class. 
      */
     @Test
     public final void testUploadSchemaOntologiesA1B1() throws Exception
     {
         this.loadSchemaOntologies("/test/schema-manifest-a1b1.ttl");
         
         Assert.assertEquals(2, this.testSchemaManager.getCurrentSchemaOntologies().size());
         Assert.assertEquals(2, this.testSchemaManager.getSchemaOntologies().size());
     }
 
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntologies(Model)} .
      * 
      * Loads a set of test ontologies that each have a single class. 
      */
     @Test
     public final void testUploadSchemaOntologiesA1B1C1() throws Exception
     {
         this.loadSchemaOntologies("/test/schema-manifest-a1b1c1.ttl");
         
         Assert.assertEquals(3, this.testSchemaManager.getCurrentSchemaOntologies().size());
         Assert.assertEquals(3, this.testSchemaManager.getSchemaOntologies().size());
     }
 
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntologies(Model)} .
      * 
      * Loads a set of test ontologies that each have a single class. Schema ontology versions loaded
      * are: Av1, Bv1, Cv1 and Cv2 (Cv2 is the current version of C).
      */
     @Test
     public final void testUploadSchemaOntologiesA1B1C2() throws Exception
     {
         this.loadSchemaOntologies("/test/schema-manifest-a1b1c2.ttl");
         
         Assert.assertEquals("Incorrect no. of current schema ontologies", 3, this.testSchemaManager
                 .getCurrentSchemaOntologies().size());
         Assert.assertEquals("Incorrect no. of total schema ontologies", 4, this.testSchemaManager.getSchemaOntologies()
                 .size());
         
         // verify: older version of poddC is also present in repository
         final InferredOWLOntologyID poddC1 =
                 this.testSchemaManager.getSchemaOntologyVersion(IRI
                         .create("http://example.org/podd/ns/version/poddC/1"));
         Assert.assertNotNull(poddC1);
     }
 
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntologies(Model)} .
      * 
      * Loads a set of test ontologies that each have a single class. Ontologies B and C have two
      * versions each.
      */
     @Test
     public final void testUploadSchemaOntologiesA1B2C2() throws Exception
     {
         this.loadSchemaOntologies("/test/schema-manifest-a1b2c3.ttl");
         
         Assert.assertEquals("Incorrect no. of current schema ontologies", 3, this.testSchemaManager
                 .getCurrentSchemaOntologies().size());
         Assert.assertEquals("Incorrect no. of total schema ontologies", 5, this.testSchemaManager.getSchemaOntologies()
                 .size());
     }
 
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntologies(Model)} .
      * 
      * Loads a set of test ontologies that each have a single class.
      * 
      * NOTE: This scenario where Ontology B has two versions but Ontology C which imports B has only
      * one version is not expected to occur in a production environment.
      */
     @Test
     public final void testUploadSchemaOntologiesA1B2C1() throws Exception
     {
         this.loadSchemaOntologies("/test/schema-manifest-a1b2c1.ttl");
 
         Assert.assertEquals("Incorrect no. of current schema ontologies", 3, this.testSchemaManager
                 .getCurrentSchemaOntologies().size());
         Assert.assertEquals("Incorrect no. of total schema ontologies", 4, this.testSchemaManager.getSchemaOntologies()
                 .size());
     }
 
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntologies(Model)} .
      * 
      * Loads a set of test ontologies that each have a single class. There are two versions of each
     * ontology with no interdependency between versions.
      */
     @Test
     public final void testUploadSchemaOntologiesABC4() throws Exception
     {
         this.loadSchemaOntologies("/test/schema-manifest-abc4.ttl");
 
         Assert.assertEquals("Incorrect no. of current schema ontologies", 3, this.testSchemaManager
                 .getCurrentSchemaOntologies().size());
         Assert.assertEquals("Incorrect no. of total schema ontologies", 6, this.testSchemaManager.getSchemaOntologies()
                 .size());
     }
 
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntologies(Model)} .
      * 
      * Tests with a schema-manifest where imports are specified as Ontology IRIs and not version
      * IRIs.
      */
     @Test
     public final void testUploadSchemaOntologiesInvalidWithOntologyIRIImports() throws Exception
     {
         // prepare: load invalid test schema-manifest file
         final String schemaManifest = "/test/bad-schema-manifest-a1b1-import-ontology-iri.ttl";
         Model model = null;
         try (final InputStream schemaManifestStream = this.getClass().getResourceAsStream(schemaManifest);)
         {
             final RDFFormat format = Rio.getParserFormatForFileName(schemaManifest, RDFFormat.RDFXML);
             model = Rio.parse(schemaManifestStream, "", format);
         }
         
         try
         {
             this.testSchemaManager.uploadSchemaOntologies(model);
             Assert.fail("Should have failed to load schema ontologies");
         }
         catch(SchemaManifestException e)
         {
             Assert.assertEquals("http://example.org/podd/ns/version/poddB/1", e.getSchemaOntologyIRI().toString());
         }
         
         // verify: no schema ontologies have been loaded
         Assert.assertEquals(0, this.testSchemaManager.getCurrentSchemaOntologies().size());
     }
 
     /**
      * Test method for {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntologies(Model)} .
      * 
      * Tests with a schema-manifest where imports are specified under Ontology IRIs.
      * 
      */
     @Test
     public final void testUploadSchemaOntologiesInvalidManifestFormat() throws Exception
     {
         // prepare: load invalid test schema-manifest file
         final String schemaManifest = "/test/bad-schema-manifest-format.ttl";
         Model model = null;
         try (final InputStream schemaManifestStream = this.getClass().getResourceAsStream(schemaManifest);)
         {
             final RDFFormat format = Rio.getParserFormatForFileName(schemaManifest, RDFFormat.RDFXML);
             model = Rio.parse(schemaManifestStream, "", format);
         }
         
         try
         {
             this.testSchemaManager.uploadSchemaOntologies(model);
             Assert.fail("Should have failed to load schema ontologies");
         }
         catch(SchemaManifestException e)
         {
             Assert.assertTrue("Not the expected Exception", e.getMessage().contains("Imports should be associated with version IRI"));
             Assert.assertEquals("Failure not due to expected ontology", "http://example.org/podd/ns/poddB", e
                     .getSchemaOntologyIRI().toString());
         }
         
         // verify: no schema ontologies have been loaded
         final Set<InferredOWLOntologyID> schemaOntologies = this.testSchemaManager.getCurrentSchemaOntologies();
         Assert.assertEquals(0, schemaOntologies.size());
     }
 
     
     @Test
     public final void testUploadSchemaOntologiesMissingCurrentVersionIRI() throws Exception
     {
         // prepare: load invalid test schema-manifest file
         final String schemaManifest = "/test/bad-schema-manifest-missing-current-version.ttl";
         Model model = null;
         try (final InputStream schemaManifestStream = this.getClass().getResourceAsStream(schemaManifest);)
         {
             final RDFFormat format = Rio.getParserFormatForFileName(schemaManifest, RDFFormat.RDFXML);
             model = Rio.parse(schemaManifestStream, "", format);
         }
         
         try
         {
             this.testSchemaManager.uploadSchemaOntologies(model);
             Assert.fail("Should have failed to load schema ontologies");
         }
         catch(SchemaManifestException e)
         {
             Assert.assertTrue("Not the expected Exception", e.getMessage().contains("Did not find a current version"));
             Assert.assertEquals("Failure not due to expected ontology", "http://example.org/podd/ns/poddB", e
                     .getSchemaOntologyIRI().toString());
         }
     }
 
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testUploadSchemaOntologyEmpty() throws Exception
     {
         try
         {
             final InputStream testInputStream = this.getClass().getResourceAsStream("/test/ontologies/empty.owl");
             
             this.testSchemaManager.uploadSchemaOntology(testInputStream, RDFFormat.RDFXML);
             
             Assert.fail("Did not receive expected exception");
         }
         catch(final EmptyOntologyException e)
         {
             Assert.assertEquals("Message was not as expected", "Loaded ontology is empty", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testUploadSchemaOntologyIDOverrideEmpty() throws Exception
     {
         try
         {
             final IRI emptyOntologyIRI = IRI.create("urn:test:empty:ontology:");
             final IRI emptyVersionIRI = IRI.create("urn:test:empty:version:");
             final OWLOntologyID emptyOntologyID = new OWLOntologyID(emptyOntologyIRI, emptyVersionIRI);
             this.owlapiManager.createOntology(emptyOntologyID);
             
             final InputStream testInputStream = this.getClass().getResourceAsStream("/test/ontologies/empty.owl");
             
             this.testSchemaManager.uploadSchemaOntology(emptyOntologyID, testInputStream, RDFFormat.RDFXML);
             
             Assert.fail("Did not receive expected exception");
         }
         catch(final EmptyOntologyException e)
         {
             Assert.assertEquals("Message was not as expected", "Loaded ontology is empty", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testUploadSchemaOntologyIDOverrideInvalidRdfXml() throws Exception
     {
         try
         {
             final IRI emptyOntologyIRI = IRI.create("urn:test:empty:ontology:");
             final IRI emptyVersionIRI = IRI.create("urn:test:empty:version:");
             final OWLOntologyID emptyOntologyID = new OWLOntologyID(emptyOntologyIRI, emptyVersionIRI);
             this.owlapiManager.createOntology(emptyOntologyID);
             
             final InputStream testInputStream =
                     this.getClass().getResourceAsStream("/test/ontologies/justatextfile.owl");
             
             this.testSchemaManager.uploadSchemaOntology(emptyOntologyID, testInputStream, RDFFormat.RDFXML);
             
             Assert.fail("Did not receive expected exception");
         }
         catch(final UnparsableOntologyException e)
         {
             Assert.assertTrue("Message was not as expected", e.getMessage().startsWith("Problem parsing "));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testUploadSchemaOntologyIDOverrideInvalidTurtle() throws Exception
     {
         try
         {
             final IRI emptyOntologyIRI = IRI.create("urn:test:empty:ontology:");
             final IRI emptyVersionIRI = IRI.create("urn:test:empty:version:");
             final OWLOntologyID emptyOntologyID = new OWLOntologyID(emptyOntologyIRI, emptyVersionIRI);
             this.owlapiManager.createOntology(emptyOntologyID);
             
             final InputStream testInputStream =
                     this.getClass().getResourceAsStream("/test/ontologies/invalidturtle.ttl");
             
             this.testSchemaManager.uploadSchemaOntology(emptyOntologyID, testInputStream, RDFFormat.TURTLE);
             
             Assert.fail("Did not receive expected exception");
         }
         catch(final UnparsableOntologyException e)
         {
             Assert.assertTrue("Message was not as expected", e.getMessage().startsWith("Problem parsing "));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testUploadSchemaOntologyIDOverrideNoOntologyIRI() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testUploadSchemaOntologyIDOverrideNotConsistent() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testUploadSchemaOntologyIDOverrideNotInProfile() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testUploadSchemaOntologyIDOverrideNullInput() throws Exception
     {
         try
         {
             final IRI emptyOntologyIRI = IRI.create("urn:test:empty:ontology:");
             final IRI emptyVersionIRI = IRI.create("urn:test:empty:version:");
             final OWLOntologyID emptyOntologyID = new OWLOntologyID(emptyOntologyIRI, emptyVersionIRI);
             this.owlapiManager.createOntology(emptyOntologyID);
             
             this.testSchemaManager.uploadSchemaOntology(emptyOntologyID, null, RDFFormat.RDFXML);
             
             Assert.fail("Did not receive expected exception");
         }
         catch(final NullPointerException e)
         {
             Assert.assertEquals("Message was not as expected", "Schema Ontology input stream was null", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testUploadSchemaOntologyIDOverrideOnlyOntologyIRI() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testUploadSchemaOntologyIDOverrideWithOntologyIRIAndVersionIRI() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testUploadSchemaOntologyInvalidRdfXml() throws Exception
     {
         try
         {
             final InputStream testInputStream =
                     this.getClass().getResourceAsStream("/test/ontologies/justatextfile.owl");
             
             this.testSchemaManager.uploadSchemaOntology(testInputStream, RDFFormat.RDFXML);
             
             Assert.fail("Did not receive expected exception");
         }
         catch(final UnparsableOntologyException e)
         {
             Assert.assertTrue("Message was not as expected", e.getMessage().startsWith("Problem parsing "));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testUploadSchemaOntologyInvalidTurtle() throws Exception
     {
         try
         {
             final InputStream testInputStream =
                     this.getClass().getResourceAsStream("/test/ontologies/invalidturtle.ttl");
             
             this.testSchemaManager.uploadSchemaOntology(testInputStream, RDFFormat.RDFXML);
             
             Assert.fail("Did not receive expected exception");
         }
         catch(final UnparsableOntologyException e)
         {
             Assert.assertTrue("Message was not as expected", e.getMessage().startsWith("Problem parsing "));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testUploadSchemaOntologyNoOntologyIRI() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testUploadSchemaOntologyNotConsistent() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testUploadSchemaOntologyNotInProfile() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testUploadSchemaOntologyNullInput() throws Exception
     {
         try
         {
             this.testSchemaManager.uploadSchemaOntology(null, RDFFormat.RDFXML);
             
             Assert.fail("Did not receive expected exception");
         }
         catch(final NullPointerException e)
         {
             Assert.assertEquals("Message was not as expected", "Schema Ontology input stream was null", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Ignore
     @Test
     public final void testUploadSchemaOntologyOnlyOntologyIRI() throws Exception
     {
         Assert.fail("Not yet implemented"); // TODO
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testUploadSchemaOntologyWithOntologyIRIAndVersionIRI() throws Exception
     {
         final String[] resourcePaths =
                 { PoddRdfConstants.PATH_PODD_DCTERMS, PoddRdfConstants.PATH_PODD_FOAF, PoddRdfConstants.PATH_PODD_USER,
                         PoddRdfConstants.PATH_PODD_BASE, };
         
         for(final String path : resourcePaths)
         {
             final InputStream testInputStream = this.getClass().getResourceAsStream(path);
             Assert.assertNotNull("Missing test resource: " + path, testInputStream);
             this.testSchemaManager.uploadSchemaOntology(testInputStream, RDFFormat.RDFXML);
         }
         
         // TODO: verify
     }
     
 }
