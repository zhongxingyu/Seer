 /**
  * 
  */
 package com.github.podd.restlet.test;
 
 import java.io.ByteArrayInputStream;
 import java.nio.charset.StandardCharsets;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.rio.RDFFormat;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.api.test.TestConstants;
 import com.github.podd.utils.PoddRdfConstants;
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
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         final String objectType = PoddRdfConstants.PODD_SCIENCE + "NoSuchPoddConcept";
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         // verify: response is empty as no such object exists
         Assert.assertNull("Expected NULL for response text", results.getText());
     }
     
     @Test
     public void testGetWithGenotypeRdf() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE)
                         .getOntologyIRI().toString();
         
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         final String objectType = PoddRdfConstants.PODD_SCIENCE + "Genotype";
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES, "true");
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                         MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify: received contents are in RDF
         Assert.assertTrue("Result does not have RDF", body.contains("<rdf:RDF"));
         Assert.assertTrue("Result does not have RDF", body.endsWith("</rdf:RDF>"));
         
         final Model model =
                this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.RDFXML, 92);
         
        Assert.assertEquals("Unexpected no. of properties", 12,
                 model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size() - 1);
         Assert.assertEquals("Expected no Do-Not-Display properties", 1,
                 model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
     }
     
     @Test
     public void testGetWithProjectRdf() throws Exception
     {
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         final String objectType = PoddRdfConstants.PODD_SCIENCE + "Project";
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify:
         final Model model =
                this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.TURTLE, 160);
         
        Assert.assertEquals("Unexpected no. of properties", 21,
                 model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size() - 1);
         Assert.assertEquals("Expected no Do-Not-Display properties", 0,
                 model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
     }
     
 }
