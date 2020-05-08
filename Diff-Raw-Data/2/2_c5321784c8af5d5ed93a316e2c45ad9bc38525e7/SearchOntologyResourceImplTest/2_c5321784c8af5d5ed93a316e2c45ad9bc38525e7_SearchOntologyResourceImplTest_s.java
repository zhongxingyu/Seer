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
 package com.github.podd.resources.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.nio.charset.StandardCharsets;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParser;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.api.test.TestConstants;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * @author kutila
  * 
  */
 public class SearchOntologyResourceImplTest extends AbstractResourceImplTest
 {
     
     private Model internalTestSearchRdf(final String searchTerm, final String[] searchTypes,
             final MediaType requestMediaType, final String artifactUri) throws Exception
     {
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SEARCH));
         
         searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCHTERM, searchTerm);
         if(artifactUri != null)
         {
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         }
         
         for(final String searchType : searchTypes)
         {
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCH_TYPES, searchType);
         }
         
         // invoke the search resource
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(searchClientResource, Method.GET, null, requestMediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         // construct a Model out of the result
         final Model resultModel = new LinkedHashModel();
         final RDFParser parser =
                 Rio.createParser(Rio.getWriterFormatForMIMEType(requestMediaType.getName(), RDFFormat.RDFXML));
         parser.setRDFHandler(new StatementCollector(resultModel));
         parser.parse(results.getStream(), "");
         
         return resultModel;
     }
     
     @Test
     public void testErrorSearchRdfWithInvalidArtifactID() throws Exception
     {
         // prepare:
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SEARCH));
         
         searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCHTERM, "Scan");
         searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, "http://no.such.artifact");
         searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCH_TYPES,
                 "http://purl.org/podd/ns/poddScience#Platform");
         
         // there is no need to authenticate or have a test artifact as the artifact ID is checked
         // for first
         try
         {
             searchClientResource.get(MediaType.APPLICATION_RDF_XML);
             Assert.fail("Should have thrown a ResourceException");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
         }
     }
     
     @Test
     public void testErrorSearchRdfWithoutAuthentication() throws Exception
     {
         // prepare:
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SEARCH));
         
         searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCHTERM, "Scan");
         searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact.getOntologyIRI()
                 .toString());
         searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCH_TYPES,
                 "http://purl.org/podd/ns/poddScience#Platform");
         
         // request without authentication
         try
         {
             searchClientResource.get(MediaType.APPLICATION_RDF_XML);
             Assert.fail("Should have thrown a ResourceException");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
     }
     
     @Test
     public void testErrorSearchRdfWithoutSearchTerm() throws Exception
     {
         // prepare:
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SEARCH));
         
         // no search term!
         searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact.getOntologyIRI()
                 .toString());
         searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCH_TYPES,
                 "http://purl.org/podd/ns/poddScience#Platform");
         
         // there is no need to authenticate or have a test artifact as the search term is checked
         // for first
         try
         {
             searchClientResource.get(MediaType.APPLICATION_RDF_XML);
             Assert.fail("Should have thrown a ResourceException");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
         }
     }
     
     @Test
     public void testPostRdfBasic() throws Exception
     {
         // prepare: add an artifact
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SEARCH));
         searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact.getOntologyIRI()
                 .toString());
         
         // prepare: the test input
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
         
         final Model testModel = new LinkedHashModel();
         for(final String s : objectUris)
         {
             testModel.add(PoddRdfConstants.VF.createURI(s), RDFS.LABEL, PoddRdfConstants.VF.createLiteral("?blank"));
         }
         
         final RDFFormat inputFormat = RDFFormat.RDFXML;
         final MediaType inputMediaType = MediaType.valueOf(inputFormat.getDefaultMIMEType());
         
         // build input representation
         final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
         Rio.write(testModel, output, inputFormat);
         final Representation input = new StringRepresentation(output.toString(), inputMediaType);
         
         // invoke service
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(searchClientResource, Method.POST, input, inputMediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         // verify: response
         final String body = results.getText();
         final Model resultModel =
                 this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.RDFXML, 5);
         
         // verify: each URI has the expected label
         for(int i = 0; i < objectUris.length; i++)
         {
             final String objectString =
                     resultModel.filter(PoddRdfConstants.VF.createURI(objectUris[i]), RDFS.LABEL, null).objectString();
             Assert.assertEquals("Not the expected label", expectedLabels[i], objectString);
         }
     }
     
     /**
      * Test successful search for a Platform in JSON format
      */
     @Test
     public void testSearchJson() throws Exception
     {
         final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Platform", OWL.THING.stringValue() };
         final MediaType requestMediaType = MediaType.APPLICATION_JSON;
         
         final Model resultModel = this.internalTestSearchRdf("Scan", searchTypes, requestMediaType, null);
         
         Assert.assertEquals("Not the expected number of results", 5, resultModel.size());
         System.out.println(resultModel.toString());
         Assert.assertEquals("Expected Platform CabScan not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("CabScan")).size());
         Assert.assertEquals("Expected Platform PlantScan not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("PlantScan")).size());
     }
     
     /**
      * Test successful search for a custom Platform in RDF/XML
      */
     @Test
     public void testSearchRdfForCustomPlatforms() throws Exception
     {
         // prepare:
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Platform" };
         final MediaType requestMediaType = MediaType.APPLICATION_RDF_XML;
         
         final Model resultModel =
                 this.internalTestSearchRdf("lat", searchTypes, requestMediaType, testArtifact.getOntologyIRI()
                         .toString());
         
         // verify:
         Assert.assertEquals("Not the expected number of results", 1, resultModel.size());
         Assert.assertEquals("Expected Platform 1 not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Platform 1")).size());
     }
     
     /**
      * Test successful search for a Platform in RDF/XML
      */
     @Test
     public void testSearchRdfForPlatforms() throws Exception
     {
         final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Platform", OWL.THING.stringValue() };
         final MediaType requestMediaType = MediaType.APPLICATION_RDF_XML;
         
         final Model resultModel = this.internalTestSearchRdf("me", searchTypes, requestMediaType, null);
         
         // verify:
         Assert.assertEquals("Not the expected number of results", 9, resultModel.size());
         Assert.assertEquals("Expected Platform SPAD Meter not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("SPAD Meter")).size());
         Assert.assertEquals("Expected Platform Pyrometer not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Pyrometer")).size());
         Assert.assertEquals("Expected Platform SC1 Porometer not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("SC1 Porometer")).size());
     }
     
     /**
      * Test successful search for a PODD Project in RDF/XML
      */
     @Test
     public void testSearchRdfForProjects() throws Exception
     {
         // prepare:
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Project" };
         final MediaType requestMediaType = MediaType.APPLICATION_RDF_XML;
         
         final Model resultModel =
                 this.internalTestSearchRdf("Cot", searchTypes, requestMediaType, testArtifact.getOntologyIRI()
                         .toString());
         
         // verify:
         Assert.assertEquals("Not the expected number of results", 1, resultModel.size());
         Assert.assertTrue("Expected Project not found", resultModel.filter(null, RDFS.LABEL, null).objectString()
                 .contains("Cotton Leaf Morphology"));
     }
     
     /**
      * Test successful search for PoddScience:Sex values in RDF/XML
      */
     @Test
     public void testSearchRdfForSex() throws Exception
     {
         // prepare: add an artifact
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Sex" };
         final MediaType requestMediaType = MediaType.APPLICATION_RDF_XML;
         
         final Model resultModel =
                 this.internalTestSearchRdf("", searchTypes, requestMediaType, testArtifact.getOntologyIRI().toString());
         
         Assert.assertEquals("Not the expected number of results", 5, resultModel.size());
         Assert.assertEquals("Value Hermaphrodite not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Hermaphrodite")).size());
         Assert.assertEquals("Value Male not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Male")).size());
     }
     
     /**
      * Test successful search for a FOR Codes in RDF/XML
      */
     @Test
     public void testSearchRdfForWildtTypeAssertion() throws Exception
     {
         final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#WildTypeAssertion" };
         final MediaType requestMediaType = MediaType.APPLICATION_RDF_XML;
         
         final Model resultModel = this.internalTestSearchRdf("", searchTypes, requestMediaType, null);
         
         // verify:
         Assert.assertEquals("Not the expected number of results", 4, resultModel.size());
         Assert.assertEquals("Expected Assertion 'Yes' not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Yes")).size());
         Assert.assertEquals("Expected Assertion 'Unknown' not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Unknown")).size());
         Assert.assertEquals("Expected Assertion 'Not Applicable' not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Not Applicable")).size());
     }
     
     /**
      * Test a search in RDF/XML, with no "searchTypes" specified.
      */
     @Test
     public void testSearchRdfWithoutSearchTypes() throws Exception
     {
         final String[] searchTypes = {}; // EVERYTHING in the search space is compared
         final MediaType requestMediaType = MediaType.APPLICATION_RDF_XML;
         
         final Model resultModel = this.internalTestSearchRdf("e", searchTypes, requestMediaType, null);
         
         // verify:
        Assert.assertEquals("Not the expected number of results", 203, resultModel.size());
         
         Assert.assertEquals(
                 "dcTerms not found",
                 1,
                 resultModel.filter(null, null,
                         PoddRdfConstants.VF.createLiteral("The PODD Ontology for Dublin Core Terms")).size());
         Assert.assertEquals(
                 "The PODD Ontology for Dublin Core Terms not found",
                 1,
                 resultModel.filter(null, null,
                         PoddRdfConstants.VF.createLiteral("The PODD Ontology for Dublin Core Terms")).size());
     }
     
     /**
      * Test successful search for a Platform in Turtle
      */
     @Test
     public void testSearchTurtle() throws Exception
     {
         final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Platform", OWL.THING.stringValue() };
         final MediaType requestMediaType = MediaType.APPLICATION_RDF_TURTLE;
         
         final Model resultModel = this.internalTestSearchRdf("Scan", searchTypes, requestMediaType, null);
         
         Assert.assertEquals("Not the expected number of results", 5, resultModel.size());
         System.out.println(resultModel.toString());
         Assert.assertEquals("Expected Platform CabScan not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("CabScan")).size());
         Assert.assertEquals("Expected Platform PlantScan not found", 1,
                 resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("PlantScan")).size());
     }
     
 }
