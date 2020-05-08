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
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.rio.RDFFormat;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.api.test.TestConstants;
 import com.github.podd.utils.PODD;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * @author kutila
  * 
  */
 public class GetMetadataResourceImplTest extends AbstractResourceImplTest
 {
     @Test
     public void testErrorGetWithInvalidObjectType() throws Exception
     {
         final String objectType = PODD.PODD_SCIENCE + "NoSuchPoddConcept";
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         try
         {
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
             
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // FIXME: Is there a better way to do this
             // verify: response is empty as no such object exists
             // Assert.assertTrue("Expected NULL for response text",
             // getText(results).isEmpty());
         }
         finally
         {
             this.releaseClient(createObjectClientResource);
         }
     }
     
     @Test
     public void testGetChildrenWithInvestigationRdf() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE)
                         .getOntologyIRI().toString();
         final String objectType = PODD.PODD_SCIENCE + "Investigation";
         
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         try
         {
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES,
                     "false");
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                     PoddWebConstants.METADATA_ONLY_CONTAINS);
             
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // verify:
            final Model model = this.assertRdf(results, RDFFormat.TURTLE, 153);
             
            Assert.assertEquals("GrowthConditions not found", 2,
                     model.filter(null, OWL.ALLVALUESFROM, PODD.VF.createURI(PODD.PODD_PLANT, "GrowthConditions"))
                             .subjects().size());
             
             Assert.assertEquals("No label for FieldConditions", 1,
                     model.filter(PODD.VF.createURI(PODD.PODD_PLANT, "FieldConditions"), RDFS.LABEL, null).objects()
                             .size());
             
            Assert.assertEquals("Unexpected no. of properties", 33,
                     model.filter(PODD.VF.createURI(objectType), null, null).size());
             Assert.assertEquals("Expected no Do-Not-Display properties", 0,
                     model.filter(null, PODD.PODD_BASE_DO_NOT_DISPLAY, null).size());
         }
         finally
         {
             this.releaseClient(createObjectClientResource);
         }
     }
     
     @Test
     public void testGetChildrenWithProjectRdf() throws Exception
     {
         final Object[][] testData = {
         
        { PODD.PODD_SCIENCE + "Project", 107, 22, 0 },
         
        { PODD.PODD_SCIENCE + "Investigation", 153, 33, 0 },
         
         };
         
         for(final Object[] element : testData)
         {
             
             final String objectType = (String)element[0];
             final int expectedModelSize = (int)element[1];
             final int expectedNoOfProperties = (int)element[2];
             final int expectedNoOfDnDProperties = (int)element[3];
             
             final ClientResource createObjectClientResource =
                     new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
             
             try
             {
                 createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
                 createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES,
                         "false");
                 createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                         PoddWebConstants.METADATA_ONLY_CONTAINS);
                 
                 final Representation results =
                         RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                                 MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK,
                                 AbstractResourceImplTest.WITH_ADMIN);
                 
                 // verify:
                 final Model model = this.assertRdf(results, RDFFormat.TURTLE, expectedModelSize);
                 
                 Assert.assertEquals("Unexpected no. of properties", expectedNoOfProperties,
                         model.filter(PODD.VF.createURI(objectType), null, null).size());
                 Assert.assertEquals("Expected no Do-Not-Display properties", expectedNoOfDnDProperties,
                         model.filter(null, PODD.PODD_BASE_DO_NOT_DISPLAY, null).size());
             }
             finally
             {
                 this.releaseClient(createObjectClientResource);
             }
         }
     }
     
     @Test
     public void testGetChildrenWithPublicationRdf() throws Exception
     {
         final String objectType = PODD.PODD_SCIENCE + "Publication";
         
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         try
         {
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES,
                     "false");
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                     PoddWebConstants.METADATA_ONLY_CONTAINS);
             
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // final String body = getText(results);
             // verify:
             // Assert.assertNull("No content since Publication cannot have child objects",
             // body);
             // final Model model =
             // this.assertRdf(new
             // ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
             // RDFFormat.TURTLE, 7);
             //
             // Assert.assertEquals("Unexpected no. of properties", 1,
             // model.filter(PoddRdfConstants.VF.createURI(objectType), null,
             // null).size() - 1);
             // Assert.assertEquals("Expected no Do-Not-Display properties", 0,
             // model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY,
             // null).size());
         }
         finally
         {
             this.releaseClient(createObjectClientResource);
         }
     }
     
     @Test
     public void testGetWithGenotypeRdf() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE)
                         .getOntologyIRI().toString();
         final String objectType = PODD.PODD_SCIENCE + "Genotype";
         
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         try
         {
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
             createObjectClientResource
                     .addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES, "true");
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                     PoddWebConstants.METADATA_ALL);
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
             
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // verify: received contents are in RDF
            final Model model = this.assertRdf(results, RDFFormat.RDFXML, 233);
             
            Assert.assertEquals("Unexpected no. of properties", 35,
                     model.filter(PODD.VF.createURI(objectType), null, null).size() - 1);
             Assert.assertEquals("Expected no Do-Not-Display properties", 3,
                     model.filter(null, PODD.PODD_BASE_DO_NOT_DISPLAY, null).size());
         }
         finally
         {
             this.releaseClient(createObjectClientResource);
         }
     }
     
     @Test
     public void testGetWithInvestigationRdf() throws Exception
     {
         final String objectType = PODD.PODD_SCIENCE + "Investigation";
         
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         try
         {
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
             
             // include-do-not-display-properties defaults to false
             // metadata-policy defaults to exclude sub-properties of
             // poddBase:contains
             
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // verify:
            final Model model = this.assertRdf(results, RDFFormat.TURTLE, 70);
             
            Assert.assertEquals("Unexpected no. of properties", 10,
                     model.filter(PODD.VF.createURI(objectType), null, null).size() - 1);
             Assert.assertEquals("Expected no Do-Not-Display properties", 0,
                     model.filter(null, PODD.PODD_BASE_DO_NOT_DISPLAY, null).size());
             Assert.assertEquals("Missing metadata about poddScience::refersToProcess", 5,
                     model.filter(PODD.VF.createURI(PODD.PODD_SCIENCE, "refersToProcess"), null, null).size());
         }
         finally
         {
             this.releaseClient(createObjectClientResource);
         }
     }
     
     /**
      * tests issue #96 - add child Process has no fields
      */
     @Test
     public void testGetWithProjectPlanRdf() throws Exception
     {
         final String objectType = PODD.PODD_SCIENCE + "Process";
         
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         try
         {
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
             
             // include-do-not-display-properties defaults to false
             // metadata-policy defaults to exclude sub-properties of
             // poddBase:contains
             
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // verify:
             final Model model = this.assertRdf(results, RDFFormat.TURTLE, 18);
             
             Assert.assertEquals("Unexpected no. of properties", 2,
                     model.filter(PODD.VF.createURI(objectType), null, null).size() - 1);
             Assert.assertEquals("Expected no Do-Not-Display properties", 0,
                     model.filter(null, PODD.PODD_BASE_DO_NOT_DISPLAY, null).size());
         }
         finally
         {
             this.releaseClient(createObjectClientResource);
         }
     }
     
     @Test
     public void testGetWithProjectRdf() throws Exception
     {
         final String objectType = PODD.PODD_SCIENCE + "Project";
         
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         try
         {
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
             
             // include-do-not-display-properties defaults to false
             // metadata-policy defaults to exclude sub-properties of
             // poddBase:contains
             
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
             
             // verify:
            final Model model = this.assertRdf(results, RDFFormat.TURTLE, 112);
             
            Assert.assertEquals("Unexpected no. of properties", 15,
                     model.filter(PODD.VF.createURI(objectType), null, null).size() - 1);
             Assert.assertEquals("Expected no Do-Not-Display properties", 0,
                     model.filter(null, PODD.PODD_BASE_DO_NOT_DISPLAY, null).size());
         }
         finally
         {
             this.releaseClient(createObjectClientResource);
         }
     }
     
 }
