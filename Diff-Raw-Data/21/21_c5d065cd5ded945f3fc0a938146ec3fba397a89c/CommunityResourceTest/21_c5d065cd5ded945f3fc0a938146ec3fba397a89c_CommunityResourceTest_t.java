 /**
  * A RESTful web service on top of DSpace.
  * Copyright (C) 2010-2011 National Library of Finland
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 /*
  * @(#)CommunityResourceTest.java
  */
 package fi.helsinki.lib.simplerest;
 
 import fi.helsinki.lib.simplerest.TestServlets.CommunityServlet;
 import java.io.IOException;
import org.dspace.content.Community;
import org.dspace.core.Context;
 import org.eclipse.jetty.testing.HttpTester;
 import org.eclipse.jetty.testing.ServletTester;
 import org.elasticsearch.rest.RestRequest;
 import org.junit.After;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 import org.restlet.Client;
 import org.restlet.Request;
 
 
 import org.restlet.data.MediaType;
 import org.restlet.data.Protocol;
 import org.restlet.representation.StringRepresentation;
 
 
 /**
  * JUnit test.
  * <ul>
  * <li>http://www.junit.org/</li>
  * <li>http://junit.sourceforge.net/doc/faq/faq.htm</li>
  * </ul>
  * Testing the methods of <code>CommunityResource</code> class.
  * @author Markos Mevorah
  * @version %I%, %G%
  * @see fi.helsinki.lib.simplerest.CommunityResource
  */
 public class CommunityResourceTest {
 
     /**
      * @see fi.helsinki.lib.simplerest.CommunityResource
      */
     private CommunityResource communityResource;
     
     private ServletTester tester;
 
     /**
      * JUnit method annotated with {@link org.junit.Before}.
      * Initializing the test resources.
      */
     @Before
     public void setUp() throws Exception {
         this.communityResource = new CommunityResource();
         tester = new ServletTester();
         tester.setContextPath("/");
         tester.addServlet(CommunityServlet.class, "/community/*");
         tester.addServlet("org.mortaby.jetty.servlet.DefaultServlet", "/");
         tester.start();
         
     }
 
     /**
      * JUnit method annotated with {@link org.junit.After}.
      * Releasing the test resources.
      */
     @After
     public void tearDown() {
         this.communityResource = null;
     }
     
     @Test
     public void testGetXml() throws IOException, Exception{
         HttpTester req = new HttpTester();
         HttpTester resp = new HttpTester();
         
         req.setMethod("GET");
         req.setHeader("HOST", "tester");
         req.setURI("/community/");
         resp.parse(tester.getResponses(req.generate()));
         
         System.out.println(resp.getContent());
         assertEquals(200, resp.getStatus());
         String[] attributes = { "short_description", "introductory_text",
                                 "copyright_text", "side_bar_text" };        
         for(String attribute : attributes){
             assertEquals(resp.getContent().contains(attribute), true);
         }
     }
     
     @Test
     public void testGetJson(){
         
     }
 
     /**
      * Test of relativeUrl method, of class CommunityResource.
      */
     @Test
     public void testRelativeUrl() {
         String actualUrl = CommunityResource.relativeUrl(23);
         assertEquals("community/23", actualUrl);
     }
 
     /**
      * Test of doInit method, of class CommunityResource.
      */
     @Test(expected = NullPointerException.class)
     public void testDoInit() throws Exception {
         this.communityResource.doInit();
     }
 
     /**
      * Test of edit method, of class CommunityResource.
      */
     @Test(expected = NullPointerException.class)
     public void testEdit() {
         StringRepresentation representation =
                              (StringRepresentation) this.communityResource.edit(null);
     }
 
     /**
      * Test of delete method, of class CommunityResource.
      */
     @Test
     public void testDelete() {
         StringRepresentation representation =
                              (StringRepresentation) this.communityResource.delete();
         assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
         assertEquals("java.lang.NullPointerException", representation.getText());
     }
 }
