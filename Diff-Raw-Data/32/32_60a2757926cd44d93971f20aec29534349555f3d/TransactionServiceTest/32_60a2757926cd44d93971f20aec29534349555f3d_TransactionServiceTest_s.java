 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nttdata.masterthesis.javabackend.services.rest;
 
 import javax.ws.rs.core.Cookie;
 import javax.ws.rs.core.MediaType;
import org.junit.After;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.nttdata.masterthesis.javabackend.ressource.ResponseEnvelope;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 /**
  * The TransactionServiceTest checks the access to transactions.
  * @author MATHAF
  */
 public class TransactionServiceTest extends AbstractSecureTest
 {
     private Cookie cookie;
 
     /**
      * Creates a Session Context on Server.
      */
     @Before
     public void setUp()
     {
         Client client = Client.create();
 
         WebResource webResource = client.resource( getLoginURI() );
         ClientResponse response = webResource.accept( "application/json" )
         .type( MediaType.APPLICATION_FORM_URLENCODED )
         .post( ClientResponse.class, getLoginForm() );
 
         cookie = response.getCookies().get( 0 ).toCookie();
     }
 
     /**
      * Removes old cookie value.
      */
     @After
     public void tearDown()
     {
         cookie = null;
     }
 
     /**
      * User want to access a bankaccount id.
      */
     @Test
     public void hasAccessToTransactionTest()
     {
 
         String requestUri = "https://" + getCnName() + ":8181/JavaBackend/rest/secure/bankaccount/1/transactions";
 
         Client client = Client.create();
         WebResource webResource = client.resource( requestUri );
 
         ClientResponse response = webResource.accept( "application/json" )
         .cookie( cookie )
         .get( ClientResponse.class );
 
         ResponseEnvelope env = response.getEntity( ResponseEnvelope.class );
 
         assertEquals( "authorized", 200, response.getStatus() );
         assertEquals( "env-success", true, env.isSuccess() );
         assertNull( "env-status", env.getErrorMsg() );
         assertNull( "env-errorMsg", env.getFieldErrors() );
 
     }
 
     /**
      * Forbidden Test.
      * User wants to request a bankaccount id which is not owned.
      */
     @Test
     public void noAccessToTransactions()
     {
 
         String requestUri = "https://" + getCnName() + ":8181/JavaBackend/rest/secure/bankaccount/2/transactions";
         Client client = Client.create();
 
         WebResource webResource = client.resource( requestUri );
         ClientResponse response = webResource.accept( "application/json" )
         .cookie( cookie )
         .get( ClientResponse.class );
 
         assertEquals( "forbidden", 403, response.getStatus() );
 
     }
 }
