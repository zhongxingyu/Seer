 /*
  *  The OpenDiamond Platform for Interactive Search
  *
  *  Copyright (c) 2009-2011 Carnegie Mellon University
  *  All rights reserved.
  *
  *  This software is distributed under the terms of the Eclipse Public
  *  License, Version 1.0 which can be found in the file named LICENSE.
  *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
  *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
  */
 
 package edu.cmu.cs.diamond.opendiamond;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * Factory to create one or more {@link Search} instances. Instances of this
  * class can also be used to generate a <code>Result</code> from an
  * <code>ObjectIdentifier</code>.
  * 
  */
 public class SearchFactory {
     private final ExecutorService executor = new ThreadPoolExecutor(0,
             Integer.MAX_VALUE, 1, TimeUnit.SECONDS,
             new SynchronousQueue<Runnable>());
 
     /**
      * The maximum length of an attribute name.
      */
     public static final int MAX_ATTRIBUTE_NAME = 256;
 
     private final List<Filter> filters;
 
     private final CookieMap cookieMap;
 
     /**
      * Constructs a search factory from a collection of filters and a
      * cookie map.
      * 
      * @param filters
      *            a collection of filters to run during a search
      * @param cookieMap
      *            the cookie map to use to look up servers and authenticate
      *            against
      */
     public SearchFactory(Collection<Filter> filters, CookieMap cookieMap) {
         this.filters = new ArrayList<Filter>(filters);
 
         this.cookieMap = cookieMap;
     }
 
     private static Set<String> copyAndValidateAttributes(Set<String> attributes) {
         Set<String> copyOfAttributes = new HashSet<String>(attributes);
         for (String string : copyOfAttributes) {
             if (string.length() > MAX_ATTRIBUTE_NAME) {
                 throw new IllegalArgumentException("\"" + string
                         + "\" length is greater than MAX_ATTRIBUTE_NAME");
             }
         }
         return copyOfAttributes;
     }
 
     @Override
     public String toString() {
         return filters.toString();
     }
 
     /**
      * Creates a search from the parameters given when constructing the
      * <code>SearchFactory</code>.
      * 
      * @param desiredAttributes
      *            a set of attribute names to specify which attributes to appear
      *            in results. May be <code>null</code>, in which case all
      *            attributes will be included.
      * @return a running <code>Search</code>
      * @throws IOException
      *             if an IO error occurs
      * @throws InterruptedException
      *             if the thread is interrupted
      */
     public Search createSearch(Set<String> desiredAttributes)
             throws IOException, InterruptedException {
         final Set<String> pushAttributes;
         LoggingFramework logging = LoggingFramework
                 .createLoggingFramework("createSearch");
 
         logging.saveSearchFactory(this, desiredAttributes);
 
         if (desiredAttributes == null) {
             // no filtering requested
             pushAttributes = null;
         } else {
             pushAttributes = copyAndValidateAttributes(desiredAttributes);
         }
 
         List<Future<Connection>> futures = new ArrayList<Future<Connection>>();
         CompletionService<Connection> connectService = new ExecutorCompletionService<Connection>(
                 executor);
 
         for (Map.Entry<String, List<Cookie>> e : cookieMap.entrySet()) {
             final String hostname = e.getKey();
             final List<Cookie> cookieList = e.getValue();
 
             futures.add(connectService.submit(new Callable<Connection>() {
                 public Connection call() throws Exception {
                     return Connection.createConnection(hostname, cookieList,
                             filters);
                 }
             }));
         }
 
         // with the connectionCreator, we want to close everything if
         // anything failed: we'll need to catch and cleanup for exceptions
         InterruptedException ie = null;
         IOException ioe = null;
 
         Set<Connection> connections = new HashSet<Connection>();
         try {
             for (int i = 0; i < futures.size(); i++) {
                 Future<Connection> f = connectService.take();
                 // System.out.println(f);
                 connections.add(f.get());
             }
         } catch (ExecutionException e1) {
             Throwable cause = e1.getCause();
             if (cause instanceof IOException) {
                 // System.out.println("*********");
                 IOException e = (IOException) cause;
                 ioe = new IOException();
                 ioe.initCause(e);
             }
         } catch (InterruptedException e2) {
             ie = e2;
         }
 
         if (ie != null) {
             cleanup(futures);
             throw ie;
         }
         if (ioe != null) {
             cleanup(futures);
             throw ioe;
         }
 
         // we're safe
         ConnectionSet cs = new ConnectionSet(executor, connections);
 
         Search search = new Search(cs, pushAttributes, logging);
         search.start();
         return search;
     }
 
     private static void cleanup(List<Future<Connection>> futures)
             throws InterruptedException {
         // System.out.println("cleanup of " + futures);
         InterruptedException ie = null;
         for (Future<Connection> future : futures) {
             try {
                 // System.out.println(" cleanup ");
                 Connection c = future.get();
                 c.close();
             } catch (ExecutionException e) {
                 // e.printStackTrace();
             } catch (InterruptedException e) {
                 ie = e;
             }
         }
         if (ie != null) {
             throw ie;
         }
     }
 
     /**
      * Generates a <code>Result</code> from an object identifier.
      * 
      * @param identifier
      *            the identifier representing the object to evaluate
      * @param desiredAttributes
      *            a set of attribute names to specify which attributes to appear
      * @return a new result
      * @throws IOException
      *             if an IO error occurs
      */
     public Result generateResult(ObjectIdentifier identifier,
             Set<String> desiredAttributes) throws IOException {
         Set<String> attributes = copyAndValidateAttributes(desiredAttributes);
         String host = identifier.getHostname();
         String objID = identifier.getObjectID();
         List<Cookie> c = cookieMap.get(host);
 
         LoggingFramework logging = LoggingFramework
                 .createLoggingFramework("generateResult");
 
         logging.saveSearchFactory(this, desiredAttributes);
 
         if (c == null) {
             throw new IOException("No cookie found for host " + host);
         }
 
         Connection conn = Connection.createConnection(host, c, filters);
         Result newResult = reexecute(conn, objID, attributes);
         conn.close();
 
         return newResult;
     }
 
     /**
      * Generates a <code>Result</code> from object data.
      *
      * @param data
      *            the data to evaluate
      * @param desiredAttributes
      *            a set of attribute names to specify which attributes to appear
      * @return a new result
      * @throws IOException
      *             if an IO error occurs
      */
     public Result generateResult(byte[] data, Set<String> desiredAttributes)
             throws IOException {
         Set<String> attributes = copyAndValidateAttributes(desiredAttributes);
         Signature signature = new Signature(data);
         String objID = signature.asURI().toString();
 
         // pick a host based on the hash code of the signature
         String[] hosts = cookieMap.getHosts();
        String host = hosts[Math.abs(signature.hashCode()) % hosts.length];
         List<Cookie> c = cookieMap.get(host);
 
         LoggingFramework logging = LoggingFramework
                 .createLoggingFramework("generateResult");
 
         logging.saveSearchFactory(this, desiredAttributes);
 
         // prestart
         Connection conn = Connection.createConnection(host, c, filters);
 
         // send eval
         Result newResult;
         try {
             newResult = reexecute(conn, objID, attributes);
         } catch (CacheMissException e) {
             // send blob
             List<byte[]> blobs = new ArrayList<byte[]>();
             blobs.add(data);
             conn.sendBlobs(blobs);
 
             // retry reexecution
             newResult = reexecute(conn, objID, attributes);
         }
 
         // close
         conn.close();
 
         return newResult;
     }
 
     private class CacheMissException extends IOException {}
 
     private Result reexecute(Connection conn, String objID,
             Set<String> attributes) throws IOException {
         byte reexec[] = new XDR_reexecute(objID, attributes).encode();
         // reexecute = 21
         MiniRPCReply reply = new RPC(conn, conn.getHostname(), 21, reexec)
                 .doRPC();
 
         // read reply
         if (reply.getMessage().getStatus() == RPC.DIAMOND_FCACHEMISS) {
             throw new CacheMissException();
         }
         reply.checkStatus();
         Map<String, byte[]> resultAttributes = new XDR_attr_list(reply
                 .getMessage().getData()).createMap();
 
         // create result
         return new Result(resultAttributes, conn.getHostname());
     }
 
     List<Filter> getFilters() {
         return filters;
     }
 
     CookieMap getCookieMap() {
         return cookieMap;
     }
 }
