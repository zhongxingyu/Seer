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
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.rio.RDFFormat;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.ext.html.FormDataSet;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.api.test.TestConstants;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  */
 public class SparqlResourceImplTest extends AbstractResourceImplTest
 {
     
     @Test
     public void testErrorSparqlWithNoArtifactID() throws Exception
     {
         // prepare:
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SPARQL));
         
         // there is no need to authenticate or have a test artifact as the
         // artifact ID is checked for first
         try
         {
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_SPARQLQUERY,
                     "CONSTRUCT { ?s a ?o } WHERE { ?s a ?o }");
             
             searchClientResource.get(MediaType.APPLICATION_RDF_XML);
             Assert.fail("Should have thrown a ResourceException");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, e.getStatus());
         }
         finally
         {
             this.releaseClient(searchClientResource);
         }
     }
     
     @Test
     public void testErrorSparqlWithInvalidArtifactID() throws Exception
     {
         // prepare:
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SPARQL));
         
         // there is no need to authenticate or have a test artifact as the
         // artifact ID is checked
         // for first
         try
         {
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_SPARQLQUERY,
                     "CONSTRUCT { ?s a ?o } WHERE { ?s a ?o }");
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, "http://no.such.artifact");
             
             searchClientResource.get(MediaType.APPLICATION_RDF_XML);
             Assert.fail("Should have thrown a ResourceException");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
         }
         finally
         {
             this.releaseClient(searchClientResource);
         }
     }
     
     @Test
     public void testErrorNoSparqlQuery() throws Exception
     {
         // prepare:
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SPARQL));
         
         // there is no need to authenticate or have a test artifact as the
         // search term is checked
         // for first
         try
         {
             // no search term!
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact
                     .getOntologyIRI().toString());
             
             searchClientResource.get(MediaType.APPLICATION_RDF_XML);
             Assert.fail("Should have thrown a ResourceException");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
         }
         finally
         {
             this.releaseClient(searchClientResource);
         }
     }
     
     @Test
     public void testSparqlAllContexts() throws Exception
     {
         // prepare: add an artifact
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         // prepare:
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SPARQL));
         
         // there is no need to authenticate or have a test artifact as the
         // artifact ID is checked
         // for first
         try
         {
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_SPARQLQUERY,
                     "CONSTRUCT { ?s a ?o } WHERE { ?s a ?o }");
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact
                     .getOntologyIRI().toString());
             
             // invoke service
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(searchClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // verify: response
            final Model resultModel = this.assertRdf(results, RDFFormat.RDFXML, 870);
             // verify that only type statements have been returned
            Assert.assertEquals(870, resultModel.filter(null, RDF.TYPE, null).size());
            Assert.assertEquals(575, resultModel.filter(null, RDF.TYPE, null).subjects().size());
             Assert.assertEquals(43, resultModel.filter(null, RDF.TYPE, null).objects().size());
         }
         finally
         {
             this.releaseClient(searchClientResource);
         }
     }
     
     @Test
     public void testSparqlAllContextsPost() throws Exception
     {
         // prepare: add an artifact
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         // prepare:
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SPARQL));
         
         // there is no need to authenticate or have a test artifact as the
         // artifact ID is checked
         // for first
         try
         {
             final FormDataSet postQuery = new FormDataSet();
             postQuery.setMediaType(MediaType.APPLICATION_WWW_FORM);
             postQuery.add(PoddWebConstants.KEY_SPARQLQUERY, "CONSTRUCT { ?s a ?o } WHERE { ?s a ?o }");
             postQuery.add(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact.getOntologyIRI().toString());
             
             // invoke service
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(searchClientResource, Method.POST, postQuery,
                             MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // verify: response
            final Model resultModel = this.assertRdf(results, RDFFormat.RDFXML, 870);
             // verify that only type statements have been returned
            Assert.assertEquals(870, resultModel.filter(null, RDF.TYPE, null).size());
            Assert.assertEquals(575, resultModel.filter(null, RDF.TYPE, null).subjects().size());
             Assert.assertEquals(43, resultModel.filter(null, RDF.TYPE, null).objects().size());
         }
         finally
         {
             this.releaseClient(searchClientResource);
         }
     }
     
     @Test
     public void testSparqlNoSchemaContexts() throws Exception
     {
         // prepare: add an artifact
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         // prepare:
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SPARQL));
         
         // there is no need to authenticate or have a test artifact as the
         // artifact ID is checked
         // for first
         try
         {
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_SPARQLQUERY,
                     "CONSTRUCT { ?s a ?o } WHERE { ?s a ?o }");
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact
                     .getOntologyIRI().toString());
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_SCHEMA, Boolean.toString(false));
             
             // invoke service
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(searchClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // verify: response
             final Model resultModel = this.assertRdf(results, RDFFormat.RDFXML, 315);
             // verify that only type statements have been returned
             Assert.assertEquals(315, resultModel.filter(null, RDF.TYPE, null).size());
             Assert.assertEquals(117, resultModel.filter(null, RDF.TYPE, null).subjects().size());
             Assert.assertEquals(35, resultModel.filter(null, RDF.TYPE, null).objects().size());
         }
         finally
         {
             this.releaseClient(searchClientResource);
         }
     }
     
     @Test
     public void testSparqlNoInferredContexts() throws Exception
     {
         // prepare: add an artifact
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         // prepare:
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SPARQL));
         
         // there is no need to authenticate or have a test artifact as the
         // artifact ID is checked
         // for first
         try
         {
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_SPARQLQUERY,
                     "CONSTRUCT { ?s a ?o } WHERE { ?s a ?o }");
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact
                     .getOntologyIRI().toString());
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_INFERRED, Boolean.toString(false));
             
             // invoke service
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(searchClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // verify: response
            final Model resultModel = this.assertRdf(results, RDFFormat.RDFXML, 715);
             // verify that only type statements have been returned
            Assert.assertEquals(715, resultModel.filter(null, RDF.TYPE, null).size());
            Assert.assertEquals(574, resultModel.filter(null, RDF.TYPE, null).subjects().size());
             Assert.assertEquals(35, resultModel.filter(null, RDF.TYPE, null).objects().size());
         }
         finally
         {
             this.releaseClient(searchClientResource);
         }
     }
     
     @Test
     public void testSparqlNoConcreteContexts() throws Exception
     {
         // prepare: add an artifact
         final InferredOWLOntologyID testArtifact =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         // prepare:
         final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SPARQL));
         
         // there is no need to authenticate or have a test artifact as the
         // artifact ID is checked
         // for first
         try
         {
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_SPARQLQUERY,
                     "CONSTRUCT { ?s a ?o } WHERE { ?s a ?o }");
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact
                     .getOntologyIRI().toString());
             searchClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_CONCRETE, Boolean.toString(false));
             
             // invoke service
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(searchClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // verify: response
             // TODO: Deduplicate statements so they don't appear in both the
             // inferred and concrete
            final Model resultModel = this.assertRdf(results, RDFFormat.RDFXML, 833);
             // verify that only type statements have been returned
            Assert.assertEquals(833, resultModel.filter(null, RDF.TYPE, null).size());
            Assert.assertEquals(574, resultModel.filter(null, RDF.TYPE, null).subjects().size());
             Assert.assertEquals(32, resultModel.filter(null, RDF.TYPE, null).objects().size());
         }
         finally
         {
             this.releaseClient(searchClientResource);
         }
     }
     
 }
