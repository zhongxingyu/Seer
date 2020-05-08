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
import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.rio.RDFFormat;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 
 import com.github.ansell.restletutils.RestletUtilMediaType;
 import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  */
 public class ListDataRepositoriesResourceImplTest extends AbstractResourceImplTest
 {
     
     /**
      * Test requesting data repositories as administrator.
      */
     @Test
     public void testHtmlAuthenticatedAdmin() throws Exception
     {
         final ClientResource dataRepositoriesClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
         
         try
         {
             final Representation result =
                     RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                             MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
             final String body = this.getText(result);
             
             // System.out.println(body);
             
             // verify:
             this.assertFreemarker(body);
             
             Assert.assertFalse(body.contains("No data repositories currently available"));
             
             Assert.assertTrue(body.contains("alias_local_ssh"));
         }
         finally
         {
             this.releaseClient(dataRepositoriesClientResource);
         }
     }
     
     /**
      * Test requesting data repositories as administrator.
      */
     @Test
     public void testHtmlAuthenticatedNonAdmin() throws Exception
     {
         final ClientResource dataRepositoriesClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
         
         try
         {
             RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                     MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testNoAdminPrivileges);
             Assert.fail("Did not find expected exception");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
         finally
         {
             this.releaseClient(dataRepositoriesClientResource);
         }
     }
     
     /**
      * Test requesting data repositories as administrator.
      */
     @Test
     public void testHtmlUnauthenticated() throws Exception
     {
         final ClientResource dataRepositoriesClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
         
         try
         {
             dataRepositoriesClientResource.get(MediaType.TEXT_HTML);
             Assert.fail("Did not find expected exception");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
         finally
         {
             this.releaseClient(dataRepositoriesClientResource);
         }
     }
     
     /**
      * Test requesting data repositories as administrator.
      */
     @Test
     public void testRdfJsonAuthenticatedAdmin() throws Exception
     {
         final ClientResource dataRepositoriesClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
         
         try
         {
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                             RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK, this.testWithAdminPrivileges);
             
             // verify:
            final Model model = this.assertRdf(results, RDFFormat.RDFJSON, 4);
             
            Assert.assertEquals(2, model.filter(null, RDF.TYPE, null).size());
            Assert.assertEquals(1, model.filter(null, PoddRdfConstants.PODD_BASE_HAS_ALIAS, null).size());
            Assert.assertEquals(1, model.filter(null, RDFS.LABEL, null).size());
             // DebugUtils.printContents(model);
         }
         finally
         {
             this.releaseClient(dataRepositoriesClientResource);
         }
     }
     
     /**
      * Test requesting data repositories as administrator.
      */
     @Test
     public void testRdfJsonAuthenticatedNonAdmin() throws Exception
     {
         final ClientResource dataRepositoriesClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
         
         try
         {
             RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                     RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK, this.testNoAdminPrivileges);
             Assert.fail("Did not find expected exception");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
         finally
         {
             this.releaseClient(dataRepositoriesClientResource);
         }
     }
     
     /**
      * Test requesting data repositories as administrator.
      */
     @Test
     public void testRdfJsonUnauthenticated() throws Exception
     {
         final ClientResource dataRepositoriesClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
         
         try
         {
             dataRepositoriesClientResource.get(RestletUtilMediaType.APPLICATION_RDF_JSON);
             Assert.fail("Did not find expected exception");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
         finally
         {
             this.releaseClient(dataRepositoriesClientResource);
         }
     }
     
     /**
      * Test requesting data repositories as administrator.
      */
     @Test
     public void testRdfXmlAuthenticatedAdmin() throws Exception
     {
         final ClientResource dataRepositoriesClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
         
         try
         {
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
             
             // verify:
            final Model model = this.assertRdf(results, RDFFormat.RDFXML, 4);
            
            Assert.assertEquals(2, model.filter(null, RDF.TYPE, null).size());
            Assert.assertEquals(1, model.filter(null, PoddRdfConstants.PODD_BASE_HAS_ALIAS, null).size());
            Assert.assertEquals(1, model.filter(null, RDFS.LABEL, null).size());
             
             // DebugUtils.printContents(model);
         }
         finally
         {
             this.releaseClient(dataRepositoriesClientResource);
         }
     }
     
     /**
      * Test requesting data repositories as administrator.
      */
     @Test
     public void testRdfXmlAuthenticatedNonAdmin() throws Exception
     {
         final ClientResource dataRepositoriesClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
         
         try
         {
             RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                     MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testNoAdminPrivileges);
             Assert.fail("Did not find expected exception");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
         finally
         {
             this.releaseClient(dataRepositoriesClientResource);
         }
     }
     
     /**
      * Test requesting data repositories as administrator.
      */
     @Test
     public void testRdfXmlUnauthenticated() throws Exception
     {
         final ClientResource dataRepositoriesClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
         
         try
         {
             dataRepositoriesClientResource.get(MediaType.APPLICATION_RDF_XML);
             Assert.fail("Did not find expected exception");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
         finally
         {
             this.releaseClient(dataRepositoriesClientResource);
         }
     }
     
 }
