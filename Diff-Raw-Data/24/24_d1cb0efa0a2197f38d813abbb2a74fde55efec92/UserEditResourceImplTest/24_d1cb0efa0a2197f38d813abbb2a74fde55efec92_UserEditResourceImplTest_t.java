 /**
  * 
  */
 package com.github.podd.resources.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.nio.charset.StandardCharsets;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.URI;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 
 import com.github.ansell.restletutils.SesameRealmConstants;
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.restlet.PoddRoles;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddUserStatus;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * Test User Edit Resource at "user/edit/{identifier}"
  * 
  * @author kutila
  * 
  */
 public class UserEditResourceImplTest extends AbstractResourceImplTest
 {
     
     /**
      * Test display of current user Edit page
      */
     @Test
     public void testEditCurrentUserHtml() throws Exception
     {
         // prepare: add a Test User account
         final String testIdentifier = "testuser@podd.com";
         final String testHomePage = "http:///www.john.doe.com";
         final Map<URI, URI> roles = new HashMap<URI, URI>();
         roles.put(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project"));
         final String testFirstName = "John";
         final String testLastName = "Doe";
         final String testOrganization = "CSIRO";
         final String testOrcid = "john-orcid";
         final String testTitle = "Mr";
         final String testPhone = "000333434";
         final String testAddress = "Some Address";
         final String testPosition = "Researcher";
         this.loadTestUser(testIdentifier, "testuserpassword", testFirstName, testLastName, testIdentifier,
                 testHomePage, testOrganization, testOrcid, testTitle, testPhone, testAddress, testPosition,
                 roles, PoddUserStatus.ACTIVE);
         
         
         final ClientResource userEditClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.GET, null,
                         MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         System.out.println(body);
         this.assertFreemarker(body);
         
         Assert.assertTrue("Page missing User identifier", body.contains(testIdentifier));
         Assert.assertTrue("Page missing title", body.contains(testTitle));
         Assert.assertTrue("Page missing first name", body.contains(testFirstName));
         Assert.assertTrue("Page missing last name", body.contains(testLastName));
         Assert.assertTrue("Page missing organization", body.contains(testOrganization));
         Assert.assertTrue("Page missing phone", body.contains(testPhone));
         Assert.assertTrue("Page missing position", body.contains(testPosition));
         Assert.assertTrue("Page missing address", body.contains(testAddress));
         Assert.assertTrue("Page missing home page", body.contains(testHomePage));
         Assert.assertTrue("Page missing orcid", body.contains(testOrcid));
     }
     
     /**
      * Test authenticated edit of current user details
      * 
      * FIXME: investigate why this test fails. 
      */
     @Test
     public void testEditCurrentUserRdf() throws Exception
     {
         final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
         final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
         
         final String testIdentifier = "testAdminUser";
         
         // prepare: retrieve Details of existing User
         final ClientResource userDetailsClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final Model userInfoModel =
                 this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 15);
         // this.log.info("Retrieved [{}] details. ", testIdentifier);
         // DebugUtils.printContents(userInfoModel);
         
         
         // prepare: modify existing User's details
         final String modifiedFirstName = "Totally";
         final String modifiedLastName = "Newman";
         
         final Resource userUri =
                 userInfoModel.filter(null, SesameRealmConstants.OAS_USEREMAIL, null).subjects().iterator().next();
         
         userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, null);
         userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERLASTNAME, null);
         userInfoModel.add(userUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                 PoddRdfConstants.VF.createLiteral(modifiedFirstName));
         userInfoModel.add(userUri, SesameRealmConstants.OAS_USERLASTNAME,
                 PoddRdfConstants.VF.createLiteral(modifiedLastName));
         
         
         // submit modified details to Edit User Service
         final ClientResource userEditClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         Rio.write(userInfoModel, out, format);
         final Representation input = new StringRepresentation(out.toString(), mediaType);
         
         final Representation modifiedResults =
                 RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         
         // verify: response has correct identifier
         final Model model =
                 this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                         RDFFormat.RDFXML, 1);
         Assert.assertEquals("Unexpected user identifier", testIdentifier,
                 model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
         
         
         // verify: details have been correctly updated (by retrieving User details again)
         final ClientResource userDetailsClientResource2 =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
         
         final Representation updatedResults =
                 RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null, mediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final Model resultsModel =
                 this.assertRdf(new ByteArrayInputStream(updatedResults.getText().getBytes(StandardCharsets.UTF_8)),
                         format, 15);
         
         Assert.assertEquals("Unexpected user identifier", testIdentifier,
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
         Assert.assertEquals("Unexpected user URI", userUri.stringValue(),
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects().iterator().next()
                         .stringValue());
         Assert.assertEquals("First name was not modified", modifiedFirstName,
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERFIRSTNAME, null).objectString());
         Assert.assertEquals("Last name was not modified", modifiedLastName,
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERLASTNAME, null).objectString());
         Assert.assertEquals("Role count should not have changed", 2,
                 resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objects().size());
     }
     
     /**
      * Test display of other user Edit page as Administrator
      */
     @Ignore
     @Test
     public void testEditOtherUserHtml() throws Exception
     {
         Assert.fail("Not Implemented");
     }
     
     /**
      * Test authenticated edit of other user details as Administrator
      */
     @Test
     public void testEditOtherUserRdf() throws Exception
     {
         final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
         final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
         
         // prepare: add a Test User account
         final String testIdentifier = "testuser@podd.com";
         final Map<URI, URI> roles = new HashMap<URI, URI>();
         roles.put(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project"));
         this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                 "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                 roles, PoddUserStatus.ACTIVE);
         
         // prepare: retrieve Details of existing User
         final ClientResource userDetailsClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final Model userInfoModel =
                 this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 16);
         // this.log.info("Retrieved [{}] details. ", testIdentifier);
         // DebugUtils.printContents(userInfoModel);
         
         // prepare: modify existing User's details
         final String modifiedFirstName = "Totally";
         final String modifiedLastName = "Newman";
         
         final Resource userUri =
                 userInfoModel.filter(null, SesameRealmConstants.OAS_USEREMAIL, null).subjects().iterator().next();
         
         userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, null);
         userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERLASTNAME, null);
         userInfoModel.remove(userUri, PoddRdfConstants.PODD_USER_STATUS, null);
         userInfoModel.add(userUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                 PoddRdfConstants.VF.createLiteral(modifiedFirstName));
         userInfoModel.add(userUri, SesameRealmConstants.OAS_USERLASTNAME,
                 PoddRdfConstants.VF.createLiteral(modifiedLastName));
        userInfoModel.add(userUri, PoddRdfConstants.PODD_USER_STATUS, PoddUserStatus.INACTIVE.getURI());
         
         // submit modified details to Edit User Service
         final ClientResource userEditClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         Rio.write(userInfoModel, out, format);
         final Representation input = new StringRepresentation(out.toString(), mediaType);
         
         final Representation modifiedResults =
                 RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         // verify: response has correct identifier
         final Model model =
                 this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                         RDFFormat.RDFXML, 1);
         Assert.assertEquals("Unexpected user identifier", testIdentifier,
                 model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
         
         // verify: details have been correctly updated (by retrieving User details again)
         final ClientResource userDetailsClientResource2 =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
         
         final Representation updatedResults =
                 RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null, mediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final Model resultsModel =
                 this.assertRdf(new ByteArrayInputStream(updatedResults.getText().getBytes(StandardCharsets.UTF_8)),
                         format, 16);
         
         Assert.assertEquals("Unexpected user identifier", testIdentifier,
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
         Assert.assertEquals("Unexpected user URI", userUri.stringValue(),
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects().iterator().next()
                         .stringValue());
         Assert.assertEquals("First name was not modified", modifiedFirstName,
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERFIRSTNAME, null).objectString());
         Assert.assertEquals("Last name was not modified", modifiedLastName,
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERLASTNAME, null).objectString());
         Assert.assertEquals("Role count should not have changed", 1,
                 resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objects().size());
         Assert.assertEquals("Status was not modified", PoddUserStatus.INACTIVE.getURI(),
                 resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
     }
     
     /**
      * Test error trying to edit a User that does not exist in the system
      */
     @Test
     public void testErrorEditNonExistentUserRdf() throws Exception
     {
         final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
         
         // submit modified details to Edit User Service
         final ClientResource userEditClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + "noSuchUser"));
         
         final Representation input = new StringRepresentation("Should have user model in JSON", mediaType);
         try
         {
             RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                     Status.CLIENT_ERROR_BAD_REQUEST, this.testWithAdminPrivileges);
             Assert.fail("Should have thrown a ResourceException");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(e.getStatus(), Status.CLIENT_ERROR_BAD_REQUEST);
         }
     }
     
     /**
      * Test error trying to display other user Edit page as non-admin user
      */
     @Ignore
     @Test
     public void testErrorEditOtherUserNonAdminHtml() throws Exception
     {
         Assert.fail("Not Implemented");
     }
     
     /**
      * Test error trying to edit other user details as non-admin user
      */
     @Test
     public void testErrorEditOtherUserNonAdminRdf() throws Exception
     {
         final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
         final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
         
         // prepare: add a Test User account
         final String testIdentifier = "testuser@podd.com";
         final Map<URI, URI> roles = new HashMap<URI, URI>();
         roles.put(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project"));
         this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                 "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                 roles, PoddUserStatus.ACTIVE);
         
         // prepare: retrieve Details of existing User
         final ClientResource userDetailsClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final Model userInfoModel =
                 this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 16);
         // this.log.info("Retrieved [{}] details. ", testIdentifier);
         // DebugUtils.printContents(userInfoModel);
         
         // prepare: modify existing User's details
         final String modifiedFirstName = "Totally";
         final String modifiedLastName = "Newman";
         
         final Resource userUri =
                 userInfoModel.filter(null, SesameRealmConstants.OAS_USEREMAIL, null).subjects().iterator().next();
         
         userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, null);
         userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERLASTNAME, null);
         userInfoModel.add(userUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                 PoddRdfConstants.VF.createLiteral(modifiedFirstName));
         userInfoModel.add(userUri, SesameRealmConstants.OAS_USERLASTNAME,
                 PoddRdfConstants.VF.createLiteral(modifiedLastName));
         
         // try to submit modified details to Edit User Service
         final ClientResource userEditClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         Rio.write(userInfoModel, out, format);
         final Representation input = new StringRepresentation(out.toString(), mediaType);
         
         try
         {
                 RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                         Status.CLIENT_ERROR_UNAUTHORIZED, this.testNoAdminPrivileges);
                 Assert.fail("Should have thrown a ResourceException due to lack of authorization");
         }
         catch (ResourceException e)
         {
             Assert.assertEquals("Should have been Unauthorized",  Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
     }
 }
