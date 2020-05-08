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
 import org.restlet.ext.html.FormData;
 import org.restlet.ext.html.FormDataSet;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * @author kutila
  * 
  */
 public class AboutResourceImplTest extends AbstractResourceImplTest
 {
     /**
      * Test unauthenticated access to /about
      */
     @Test
     public void testGetAboutWithoutAuthentication() throws Exception
     {
         final ClientResource aboutClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ABOUT));
         
         final Representation results =
                 PoddRestletTestUtils.doTestUnAuthenticatedRequest(aboutClientResource, Method.GET, null,
                         MediaType.TEXT_HTML, Status.SUCCESS_OK);
         
        final String body = results.getText();
        assertFreemarker(body);
        Assert.assertTrue(body.contains("The University of Queensland"));
     }
    
     /**
      * Tests that no error occurs when trying to get the get user resource while authenticated with
      * the admin role.
      * 
      * Login page cannot be tested using this approach as in the unit test environment, the server
      * uses DIGEST authentication.
      * 
      */
     @Ignore
     @Test
     public void testGetUserBasicAuthorised() throws Exception
     {
         final ClientResource creationClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_LOGIN_SUBMIT));
         
         final FormDataSet form = new FormDataSet();
         form.setMultipart(false);
         form.setMediaType(MediaType.APPLICATION_WWW_FORM);
         form.getEntries().add(new FormData("username", "testAdminUser"));
         form.getEntries().add(new FormData("password", "testAdminPassword"));
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.POST, form,
                         MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testNoAdminPrivileges);
         
     }
     
     /**
      * Test authenticated access to /about
      */
     @Ignore
     @Test
     public void testGetAboutWithAuthentication() throws Exception
     {
         final ClientResource aboutClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ABOUT));
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(aboutClientResource, Method.GET, null, MediaType.TEXT_HTML,
                         Status.SUCCESS_OK, this.testNoAdminPrivileges);
         
     }
     
 }
