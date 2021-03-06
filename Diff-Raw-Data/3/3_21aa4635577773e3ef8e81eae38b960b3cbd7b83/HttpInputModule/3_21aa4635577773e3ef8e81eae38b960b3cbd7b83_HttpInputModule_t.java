 /*
  *  Copyright (c) 2012 Malhar, Inc.
  *  All Rights Reserved.
  */
 package com.malhartech.lib.io;
 
import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Future;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.io.IOUtils;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.malhartech.annotation.ModuleAnnotation;
 import com.malhartech.annotation.PortAnnotation;
 import com.malhartech.annotation.PortAnnotation.PortType;
 import com.malhartech.annotation.ShipContainingJars;
 import com.malhartech.dag.AbstractInputModule;
 import com.malhartech.dag.Component;
 import com.malhartech.dag.FailedOperationException;
 import com.malhartech.dag.ModuleConfiguration;
 import com.sun.jersey.api.client.AsyncWebResource;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  *
  * Reads via GET from given URL as chunked transfer encoded input stream<p>
  * <br>
  * Data of type {@link java.util.Map} is converted to JSON. All other types are sent in their {@link Object#toString()} representation.<br>
  * <br>
  *
  */
 @ShipContainingJars(classes={com.sun.jersey.api.client.ClientHandler.class})
 @ModuleAnnotation(
     ports = {
         @PortAnnotation(name = Component.OUTPUT, type = PortType.OUTPUT)
     }
 )
 public class HttpInputModule extends AbstractInputModule
 {
   private static final Logger LOG = LoggerFactory.getLogger(HttpInputModule.class);
 
   /**
    * The URL of the web service resource for the POST request.
    */
   public static final String P_RESOURCE_URL = "resourceUrl";
 
   private transient URI resourceUrl;
   private transient Client wsClient;
   private transient AsyncWebResource resource;
   private transient Thread ioThread;
 
   @Override
   public void setup(ModuleConfiguration config) throws FailedOperationException
   {
     try {
       checkConfiguration(config);
     }
     catch (Exception ex) {
       throw new FailedOperationException(ex);
     }
 
     wsClient = Client.create();
     wsClient.setFollowRedirects(true);
     wsClient.setReadTimeout(30000);
     resource = wsClient.asyncResource(resourceUrl);
     LOG.info("URL: {}", resourceUrl);
 
     // launch IO thread
     Runnable r = new Runnable() {
       @Override
       public void run() {
         HttpInputModule.this.run();
       }
     };
     this.ioThread = new Thread(r, "http-io-"+this.getId());
     this.ioThread.start();
 
   }
 
   @Override
   public void teardown() {
     this.ioThread.interrupt();
     if (wsClient != null) {
       wsClient.destroy();
     }
     super.teardown();
   }
 
   public boolean checkConfiguration(ModuleConfiguration config) {
     String urlStr = config.get(P_RESOURCE_URL);
     if (urlStr == null) {
       throw new MissingResourceException("Key for URL string not set", String.class.getSimpleName(), P_RESOURCE_URL);
     }
     try {
       this.resourceUrl = new URI(urlStr);
     } catch (URISyntaxException e) {
       throw new IllegalArgumentException(String.format("Invalid value '%s' for '%s'.", urlStr, P_RESOURCE_URL));
     }
     return true;
   }
 
   @Override
   public void process(Object payload) {
     Object tuple;
     while ((tuple = tuples.poll()) != null) {
       emit(Component.OUTPUT, tuple);
     }
   }
 
   private final ConcurrentLinkedQueue<Object> tuples = new ConcurrentLinkedQueue<Object>();
 
   //@Override
   public void run() {
     while (true) {
       try {
         Future<ClientResponse> responseFuture = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
         ClientResponse response = responseFuture.get();
         LOG.debug("Opening stream: " + resource);
 
         if (!MediaType.APPLICATION_JSON_TYPE.equals(response.getType())) {
           LOG.error("Unexpected response type " + response.getType());
           response.close();
         } else {
           InputStream is = response.getEntity(java.io.InputStream.class);
           while (true) {
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             byte[] bytes = new byte[255];
             int bytesRead;
             while ((bytesRead = is.read(bytes)) != -1) {
               bos.write(bytes, 0, bytesRead);
               if (is.available() == 0 && bos.size() > 0) {
                 // give chance to process what we have before blocking on read
                 break;
               }
             }
             if (processResponseChunk(bos.toByteArray())) {
               LOG.debug("End of chunked input");
               response.close();
               break;
             }
 
             if (bytesRead == -1) {
               LOG.error("Unexpected end of chunked input stream");
               response.close();
               break;
             }
 
             bos.reset();
           }
         }
       } catch (Exception e) {
           LOG.error("Error reading from " + resource.getURI(), e);
       }
 
       try {
         Thread.sleep(500);
       }
       catch (InterruptedException e) {
         LOG.info("Exiting IO loop {}.", e.toString());
         break;
       }
     }
   }
 
   private boolean processResponseChunk(byte[] bytes) throws IOException, JSONException {
     StringBuilder chunkStr = new StringBuilder();
     // hack: when a line is a number we skip to next object instead of properly reading the chunks
     List<String> lines = IOUtils.readLines(new ByteArrayInputStream(bytes));
     boolean endStream = false;
     for (String line : lines) {
       try {
         int length = Integer.parseInt(line);
         if (length == 0) {
           endStream = true;
         }
         //LOG.debug("chunk length: " + line);
         // end chunk
         // we are expecting a JSON object and converting one level of keys to a map, no further mapping is performed
         if (chunkStr.length() > 0) {
           //LOG.debug("completed chunk: " + chunkStr);
           JSONObject json = new JSONObject(chunkStr.toString());
           chunkStr = new StringBuilder();
           Map<String, Object> tuple = new HashMap<String, Object>();
           Iterator<?> it = json.keys();
           while (it.hasNext()) {
             String key = (String)it.next();
             Object val = json.get(key);
             if (val != null) {
               tuple.put(key, val);
             }
           }
           if (!tuple.isEmpty()) {
             LOG.debug("Got: " + tuple);
             tuples.offer(tuple);
           }
         }
       } catch (NumberFormatException e) {
         // add to chunk
         chunkStr.append(line);
         chunkStr.append("\n");
       }
     }
     return endStream;
   }
 
 }
