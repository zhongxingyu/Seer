 package entropy.jobsManager;/*
  * Copyright (c) Fabien Hermenier
  *
  *        This file is part of Entropy.
  *
  *        Entropy is free software: you can redistribute it and/or modify
  *        it under the terms of the GNU Lesser General Public License as published by
  *        the Free Software Foundation, either version 3 of the License, or
  *        (at your option) any later version.
  *
  *        Entropy is distributed in the hope that it will be useful,
  *        but WITHOUT ANY WARRANTY; without even the implied warranty of
  *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *        GNU Lesser General Public License for more details.
  *
  *        You should have received a copy of the GNU Lesser General Public License
  *        along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import org.eclipse.jetty.client.Address;
 import org.eclipse.jetty.client.ContentExchange;
 import org.eclipse.jetty.client.HttpClient;
 import org.eclipse.jetty.client.HttpExchange;
 import org.eclipse.jetty.io.ByteArrayBuffer;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 /**
  * A job handler is a client of a JobDispatcher.
  * Using HTTP request, it get jobs and commit then once computed.
  *
  * @author Fabien Hermenier
  * @see JobDispatcher
  * @see Job
  */
 public class JobHandler {
 
     /**
      * The HTTP client to get the jobs
      */
     private HttpClient client;
 
     /**
      * The address of the job dispatcher.
      */
     private Address addr;
 
     public static final int DEFAULT_CACHE_SIZE = 200;
 
     private Map<String, byte[]> rcCache;
 
     public JobHandler(String serverName) throws Exception {
         this(serverName, JobDispatcher.DEFAULT_PORT, DEFAULT_CACHE_SIZE);
     }
 
     /**
      * Make a new JobHandler.
      *
      * @param serverName the name of the job dispatcher
      * @param p          the listening port of the job dispatcher
      * @throws Exception if an error occurred
      */
     public JobHandler(String serverName, int p, final int cacheSize) throws Exception {
         client = new HttpClient();
         client.start();
         addr = new Address(serverName, p);
         this.rcCache = new LinkedHashMap<String, byte[]>(cacheSize) {
             @Override
             protected boolean removeEldestEntry(Map.Entry<String, byte[]> entry) {
                 return size() == cacheSize;
             }
         };
     }
 
     public void flushCache() {
         this.rcCache.clear();
     }
 
     public File storeResource(String rc) throws Exception {
         String name = rc.substring(rc.lastIndexOf('/') + 1, rc.length());
         File f = File.createTempFile("vjob", name);
         byte[] content = getResource(rc);
         FileOutputStream out = new FileOutputStream(f);
         out.write(content);
         out.close();
         return f;
     }
 
     public String getResourceAsString(String rc) throws IOException {
         return new String(getResource(rc));
     }
 
     public byte[] getResource(String rc) throws IOException {
 
         if (rcCache.containsKey(rc)) {
             return rcCache.get(rc);
         }
         ContentExchange e = new ContentExchange();
         e.setMethod("GET");
         e.setRequestURI("/" + rc);
        //e.setURI("/" + rc);
         e.setAddress(addr);
         client.send(e);
         try {
             int exchangeState = e.waitForDone();
             if (exchangeState == HttpExchange.STATUS_COMPLETED) {
                 if (e.getResponseStatus() == HttpServletResponse.SC_OK) {
                     byte[] content = e.getResponseContentBytes();
                     rcCache.put(rc, content);
                     return content;
                 } else if (e.getResponseStatus() != HttpServletResponse.SC_GONE) {
                     throw new IOException("Error: server returns status code '" + e.getResponseStatus() + "' instead of '" + HttpServletResponse.SC_OK);
                 }
             } else {
                 throw new IOException("Error: exchange code '" + exchangeState + "' instead of '" + HttpExchange.STATUS_COMPLETED);
             }
         } catch (InterruptedException ex) {
             throw new IOException(ex.getMessage(), ex);
         }
         return null;
     }
 
     /**
      * Dequeue a job.
      *
      * @return the dequeued job, it is null when there is no more jobs to compute
      * @throws java.io.IOException if an error occurred while reading the job
      * @throws JobHandlerException if another error occurred
      */
     public Job dequeue() throws IOException, JobHandlerException {
         Job j = null;
         ContentExchange e = new ContentExchange();
         e.setMethod("GET");
         e.setRequestURI("/?a=dequeue");
         e.setAddress(addr);
         client.send(e);
         try {
 
             int exchangeState = e.waitForDone();
             if (exchangeState == HttpExchange.STATUS_COMPLETED) {
                 if (e.getResponseStatus() == HttpServletResponse.SC_OK) {
                     j = Job.fromJSON(e.getResponseContent());
                 }
             } else {
                 throw new JobHandlerException("Error: exchange code '" + exchangeState + "' instead of '" + HttpExchange.STATUS_COMPLETED);
             }
         } catch (InterruptedException ex) {
             throw new JobHandlerException(ex.getMessage(), ex);
         }
         return j;
     }
 
     /**
      * commit a job using a POST request.
      *
      * @param j the job to commit
      * @throws java.io.IOException if an error occurred while reading the job
      * @throws JobHandlerException if another error occurred
      */
     public void commit(Job j) throws IOException, JobHandlerException {
         ContentExchange e = new ContentExchange();
         e.setAddress(addr);
         e.setMethod("POST");
         e.setRequestURI("/?a=commit&j=" + j.getId());
         e.setRequestContentType("text/json");
         e.setRequestContent(new ByteArrayBuffer(j.toJSON().getBytes("UTF-8")));
         client.send(e);
         try {
             int exchangeState = e.waitForDone();
             if (exchangeState == HttpExchange.STATUS_COMPLETED) {
                 if (e.getResponseStatus() != HttpServletResponse.SC_OK) {
                     throw new JobHandlerException("Error : server status code '" + e.getResponseStatus() + " for request " + e.getURI());
                 }
             } else {
                 throw new JobHandlerException("Error: exchange code '" + exchangeState + "' instead of '" + HttpExchange.STATUS_COMPLETED);
             }
         } catch (InterruptedException ex) {
             throw new JobHandlerException(ex.getMessage(), ex);
         }
     }
 
     public void close() throws Exception {
         this.client.stop();
     }
 }
