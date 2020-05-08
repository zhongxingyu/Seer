  /*******************************************************************************
  * Copyright 2010-2013 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
  *
  * This file is part of SITools2.
  *
  * SITools2 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SITools2 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package fr.cnes.sitools;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 import org.restlet.Client;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.data.ChallengeResponse;
 import org.restlet.data.ChallengeScheme;
 import org.restlet.data.Method;
 import org.restlet.data.Protocol;
 import org.restlet.data.Status;
 
 import fr.cnes.sitools.util.RIAPUtils;
 
 /**
  * Test the {@link NotAuthenticatedFilter}. Try to access a resource as public, as good authentication and as bad
  * authentication. It will fail with bad authentication and return a 403 Forbidden Code
  * 
  * 
  * @author m.gond
  */
 public class NotAuthenticatedFilterTestCase extends SitoolsServerTestCase {
   /** The url to query */
  private static final String URL = getHostUrl() + "/sitools/client-user/siteMap";
   /** good user login */
   private static final String GOOD_USER = "admin";
   /** good user password */
   private static final String GOOD_PWD = "admin";
   /** bad user login */
   private static final String BAD_USER = "admin2";
   /** bad user password */
   private static final String BAD_PWD = "admin";
 
   /**
    * Test
    */
   @Test
   public void test() {
 
     testPublic(URL);
 
     testGoodAuthentication(URL);
 
     testBadAuthentication(URL);
 
   }
 
   /**
    * Test as public => Success OK 200
    * 
    * @param url
    *          the url to query
    */
   private void testPublic(String url) {
 
     final Client client = new Client(Protocol.HTTP);
     Request request = new Request(Method.GET, url);
     Response response = null;
     try {
       response = client.handle(request);
 
       assertNotNull(response);
       assertTrue(response.getStatus().isSuccess());
       assertEquals(Status.SUCCESS_OK, response.getStatus());
 
     }
     finally {
       if (response != null) {
         RIAPUtils.exhaust(response);
       }
     }
 
   }
 
   /**
    * Test with good authentication => Success OK 200
    * 
    * @param url
    *          the url to query
    */
   private void testGoodAuthentication(String url) {
 
     final Client client = new Client(Protocol.HTTP);
     Request request = new Request(Method.GET, url);
     Response response = null;
     try {
       ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, GOOD_USER, GOOD_PWD);
       request.setChallengeResponse(cr);
       response = client.handle(request);
 
       assertNotNull(response);
       assertTrue(response.getStatus().isSuccess());
       assertEquals(Status.SUCCESS_OK, response.getStatus());
 
     }
     finally {
       if (response != null) {
         RIAPUtils.exhaust(response);
       }
     }
 
   }
 
   /**
    * Test with bad authentication => Forbidden NOK 403
    * 
    * @param url
    *          the url to query
    */
   private void testBadAuthentication(String url) {
 
     final Client client = new Client(Protocol.HTTP);
     Request request = new Request(Method.GET, url);
     Response response = null;
     try {
       ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, BAD_USER, BAD_PWD);
       request.setChallengeResponse(cr);
       response = client.handle(request);
 
       assertNotNull(response);
       assertTrue(response.getStatus().isError());
       assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
 
     }
     finally {
       if (response != null) {
         RIAPUtils.exhaust(response);
       }
     }
   }
 
 }
