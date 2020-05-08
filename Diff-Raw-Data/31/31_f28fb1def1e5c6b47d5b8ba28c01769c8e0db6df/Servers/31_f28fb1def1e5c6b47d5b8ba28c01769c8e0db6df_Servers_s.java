 package com.thalesgroup.tcs.tai.gwcalipso;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Set;
 
@Path("servers")
 public class Servers {
 
     @GET
     @Produces(MediaType.TEXT_PLAIN)
     public String getRoot() {
 
         StringBuilder answer = new StringBuilder();

         Set<String> coap_servers = Redis.smembers("coap_servers");
 
         for(String s: coap_servers){
             answer.append(s).append("\n");
         }
         return answer.toString();
     }
 
     @GET @Path("{coap_server}")
     @Produces(MediaType.TEXT_PLAIN)
     public String getWellKnownCore(@PathParam("coap_server") String host) {
         String answer = null;
         // URI construction
         URI uri = null;
         try {
             uri = new URI(String.format("coap://%s/.well-known/core", host));
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
         assert uri != null;
         System.out.println(uri.toString());
         String key_exist = Redis.redis_lookup(uri.toString());
         System.out.println("RESULTS REDIS :" + key_exist);

         if (key_exist.isEmpty()) {
             // Cache invalidation => CoAP request
             CoAPClient coAPClient = new CoAPClient();
             String result = coAPClient.execute(uri, "GET");
             Redis.setex(uri.toString(), 10, result);
             answer = result;
         } else {
             answer = key_exist;
         }
         return answer;
     }
 
     @GET @Path("{coap_server}/{resource}")
     @Produces(MediaType.TEXT_PLAIN)
     public String getCoAPResource(
             @PathParam("coap_server") String host,
             @PathParam("resource") String resource){
         String answer = null;
         // URI construction
         URI uri = null;
         try {
             uri = new URI(String.format("coap://%s/%s", host, resource));
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
         assert uri != null;
         System.out.println(uri.toString());
         String key_exist = Redis.redis_lookup(uri.toString());
         System.out.println("RESULTS REDIS :" + key_exist);

         if (key_exist.isEmpty()) {
             CoAPClient coAPClient = new CoAPClient();
             String result = coAPClient.execute(uri, "GET");
             Redis.setex(uri.toString(), 10, result);
             answer = result;
         }else {
             answer = key_exist;
         }
         return answer;
     }
 
 }
