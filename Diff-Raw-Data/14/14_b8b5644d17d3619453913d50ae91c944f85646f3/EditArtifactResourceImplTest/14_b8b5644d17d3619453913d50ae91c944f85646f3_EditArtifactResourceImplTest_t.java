 /**
  * 
  */
 package com.github.podd.restlet.test;
 
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.api.test.TestConstants;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * 
  * @author kutila
  */
 public class EditArtifactResourceImplTest extends AbstractResourceImplTest
 {
     
     @Ignore
     @Test
     public void testEditArtifactBasicJson() throws Exception
     {
         Assert.fail("TODO: implement");
     }
     
     @Test
     public void testErrorEditArtifactRdfWithoutArtifactID() throws Exception
     {
         final ClientResource editArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT_MERGE));
         
         // there is no need to authenticate, have a test artifact or send RDF to modify as the
         // artifact is checked for first
         try
         {
             editArtifactClientResource.post(null, MediaType.TEXT_PLAIN);
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
         }
     }
     
     @Test
     public void testErrorEditArtifactRdfWithoutAuthentication() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri = "urn:purl:dummy:artifact:uri:artifact:1";
         
         final ClientResource editArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT_MERGE));
         
         editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         
         final Representation input =
                 this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                         MediaType.APPLICATION_RDF_XML);
         
         // invoke without authentication
         try
         {
             editArtifactClientResource.post(input, MediaType.APPLICATION_RDF_XML);
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
     }
     
     @Test
     public void testEditArtifactBasicRdf() throws Exception
     {
         // prepare: add an artifact
        final String artifactUri =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final ClientResource editArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT_MERGE));
         
         editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
        editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_REPLACE, Boolean.toString(false));
         
         final Representation input =
                 this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                         MediaType.APPLICATION_RDF_XML);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.POST, input,
                         MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify: Inferred Ontology ID is received in RDF format
         Assert.assertTrue("Response not in RDF format", body.contains("<rdf:RDF"));
         Assert.assertTrue("Artifact version has not been updated properly", body.contains("artifact:1:version:2"));
         Assert.assertTrue("Version IRI not in response", body.contains("versionIRI"));
         Assert.assertTrue("Inferred version not in response", body.contains("inferredVersion"));
        
        // verify: publication46 has been added to the artifact
        final String artifactBody = this.getArtifactAsString(artifactUri, MediaType.APPLICATION_RDF_XML);
        Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("rfc2616.html"));
        Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("publication-pdf-a"));
     }
     
     @Test
     public void testEditArtifactBasicTurtle() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final ClientResource editArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT_MERGE));
         
         editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_REPLACE, Boolean.toString(false));
         
         // edit Representation contains statements in Turtle format
         final Representation input =
                 this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_PUBLICATION_OBJECT,
                         MediaType.APPLICATION_RDF_TURTLE);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.POST, input,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String updatedArtifactDetails = results.getText();
         
         // verify: Inferred Ontology ID is NOT in RDF format
         Assert.assertFalse("Response should not be in RDF format", updatedArtifactDetails.contains("<rdf:RDF"));
         Assert.assertTrue("Artifact version has not been updated properly",
                 updatedArtifactDetails.contains("artifact:1:version:2"));
         Assert.assertTrue("Version IRI not in response", updatedArtifactDetails.contains("versionIRI"));
         Assert.assertTrue("Inferred version not in response", updatedArtifactDetails.contains("inferredVersion"));
         
        // verify: publication46 has been added to the artifact
         final String artifactBody = this.getArtifactAsString(artifactUri, MediaType.APPLICATION_RDF_TURTLE);
         Assert.assertTrue("New publication not added to artifact", artifactBody.contains("publication46"));
         Assert.assertTrue("New publication not added to artifact",
                 artifactBody.contains("http://dx.doi.org/10.1109/eScience.2013.44"));
     }
     
     @Test
     public void testEditArtifactTurtleWithDanglingObjects() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final ClientResource editArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT_MERGE));
         
         editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_REPLACE, Boolean.toString(true));
         
         // edit Representation contains statements in Turtle format
         final Representation input =
                 this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFY_DEMO_INVESTIGATION,
                         MediaType.APPLICATION_RDF_TURTLE);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.POST, input,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String updatedArtifactDetails = results.getText();
         
         // verify: Inferred Ontology ID is NOT in RDF format
         Assert.assertFalse("Response should not be in RDF format", updatedArtifactDetails.contains("<rdf:RDF"));
         Assert.assertTrue("Artifact version has not been updated properly",
                 updatedArtifactDetails.contains("artifact:1:version:2"));
         Assert.assertTrue("Version IRI not in response", updatedArtifactDetails.contains("versionIRI"));
         Assert.assertTrue("Inferred version not in response", updatedArtifactDetails.contains("inferredVersion"));
         
         // verify: objects left dangling after edit have been removed from the artifact
         final String artifactBody = this.getArtifactAsString(artifactUri, MediaType.APPLICATION_RDF_TURTLE);
         final String[] danglingObjects =
                 { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_genotype_3",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#Sequence_A", };
         for(final String deletedObject : danglingObjects)
         {
             Assert.assertFalse("Dangling object still exists", artifactBody.contains(deletedObject));
         }
     }
     
     /**
      * Test viewing the edit HTML page for an internal PODD object.
      */
     @Test
     public void testGetEditArtifactInternalObjectHtml() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri = this.loadTestArtifact("/test/artifacts/basic-2.rdf");
         
         final String objectUri = "urn:hardcoded:purl:artifact:1#publication45";
         
         final ClientResource editArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT_MERGE));
         
         editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_IDENTIFIER, objectUri);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.GET, null,
                         MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify:
         System.out.println(body);
         this.assertFreemarker(body);
     }
     
     /**
      * Test viewing the edit HTML page for a PODD top object (i.e. a Project).
      */
     @Ignore
     @Test
     public void testGetEditArtifactTopObjectHtml() throws Exception
     {
         Assert.fail("TODO: implement");
     }
     
     /**
      * Test posting to the edit HTML page modifying an internal PODD object.
      */
     @Ignore
     @Test
     public void testPostEditArtifactInternalObjectHtml() throws Exception
     {
         Assert.fail("TODO: implement");
     }
     
     /**
      * Test posting to the edit HTML page modifying a PODD top object (i.e. a Project).
      */
     @Ignore
     @Test
     public void testPostEditArtifactTopObjectHtml() throws Exception
     {
         Assert.fail("TODO: implement");
     }
     
 }
