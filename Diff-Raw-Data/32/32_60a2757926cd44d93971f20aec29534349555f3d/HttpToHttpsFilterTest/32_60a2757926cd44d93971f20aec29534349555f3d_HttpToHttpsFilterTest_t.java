 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nttdata.masterthesis.javabackend.filter;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 /**
  * Test checks the http to https redirect.
  * @author MATHAF
  */
 public class HttpToHttpsFilterTest
 {
 
     /**
     * check http to https redirect.
      */
     @Test
     public void redirectionTest()
     {
         Client client = Client.create();
 
         WebResource webResource = client
         .resource( "http://127.0.0.1:8080/JavaBackend/rest/secure/bankaccount/1/transactions" );
 
         ClientResponse response = webResource.accept( "application/json" )
         .get( ClientResponse.class );
 
         assertEquals( "redirection test.", 302, response.getStatus() );
 
     }
 }
