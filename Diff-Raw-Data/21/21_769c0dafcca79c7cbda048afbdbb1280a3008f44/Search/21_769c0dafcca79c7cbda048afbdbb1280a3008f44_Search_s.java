 /*
  *  The OpenDiamond Platform for Interactive Search
  *  Version 5
  *
  *  Copyright (c) 2007, 2009-2010 Carnegie Mellon University
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
 import java.util.concurrent.Callable;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutionException;
 
 /**
  * Representation of a running or closed Diamond search.
  * <p>
  * The initial state of a search is "running", meaning connections are live to
  * Diamond servers and results are being retrieved. If <code>close()</code> is
  * called, or an <code>IOException</code> is thrown from a call to any of its
  * methods, the search will transition to the "closed" state. In this state,
  * calls to any method (except <code>close()</code>) will result in a
  * <code>SearchClosedException</code> being thrown.
  * <p>
  * When a search object is no longer needed, <code>close()</code> must be
  * called, or internal resources will leak.
  * 
  */
 public class Search {
     private static class SessionVariables {
         final private String hostname;
 
         final private Map<String, Double> map;
 
         public SessionVariables(String hostname, Map<String, Double> vars) {
             this.hostname = hostname;
 
             map = Collections
                     .unmodifiableMap(new HashMap<String, Double>(vars));
         }
 
         public Map<String, Double> getVariables() {
             return map;
         }
 
         @Override
         public String toString() {
             return hostname + ": " + map.toString();
         }
     }
 
     final private ConnectionSet cs;
 
     private final LoggingFramework logging;
 
     private volatile boolean closed;
 
     private Throwable closeCause;
 
     private final Object closeLock = new Object();
 
     private final Object rpcLock = new Object();
 
     /**
      * Closes the Search. After calling this method, all other methods will
      * throw a <code>SearchClosedException</code>. <code>close()</code> must be
      * called at some point, or internal resources will leak.
      * 
      * @throws InterruptedException
      *             if the close is interrupted
      */
     public void close() throws InterruptedException {
         close(null);
     }
 
     void close(Throwable cause) throws InterruptedException {
         synchronized (closeLock) {
             if (!closed) {
                 closed = true;
                 cs.close();
                 closeCause = cause;
             }
         }
         logging.stoppedSearch(cause);
     }
 
     void start() throws InterruptedException, IOException {
         CompletionService<?> replies = cs
                 .runOnAllServers(new ConnectionFunction<Object>() {
                     public Callable<Object> createCallable(final Connection c) {
                         return new Callable<Object>() {
                             public Object call() throws Exception {
                                 c.sendStart();
                                 return null;
                             }
                         };
                     }
                 });
 
         try {
             Util.checkResultsForIOException(cs.size(), replies);
         } catch (InterruptedException e) {
             close(e);
             throw e;
         } catch (IOException e) {
             close(e);
             throw e;
         }
         logging.startedSearch();
     }
 
     private void checkClosed() throws SearchClosedException {
         if (closed) {
             if (closeCause == null) {
                 throw new SearchClosedException();
             } else {
                 throw new SearchClosedException(closeCause);
             }
         }
     }
 
     /**
      * Blocks and returns with the next {@link Result} of this search, or
      * <code>null</code> if there are no more results. The method will block
      * until a result is available or until an exception is thrown. This method
      * is thread safe and can be used simultaneously from multiple threads.
      * 
      * @return the next result, or <code>null</code> if there are no more
      *         results
      * @throws InterruptedException
      *             if the thread is interrupted
      * @throws IOException
      *             if an IO error occurs
      * @throws SearchClosedException
      *             if this <code>Search</code> is closed
      */
     public Result getNextResult() throws InterruptedException, IOException {
         checkClosed();
 
         BlastChannelObject bco = cs.getNextBlastChannelObject();
 
         // done?
         if (bco == BlastChannelObject.NO_MORE_RESULTS) {
             logging.logNoMoreResults();
             return null;
         }
 
         // check for exception
         IOException e = bco.getException();
         if (e != null) {
             close(e);
             IOException e2 = new IOException();
             e2.initCause(e);
             throw e2;
         }
 
         // compose new Result
         XDR_object obj = bco.getObj();
         Map<String, byte[]> attrs = obj.getAttributes();
         byte[] data = obj.getData();
 
         // when push attributes are not set, data is in data, not "" in
         // attributes
         if (data.length != 0) {
             HashMap<String, byte[]> newMap = new HashMap<String, byte[]>(attrs);
             newMap.put("", data);
             attrs = Collections.unmodifiableMap(newMap);
         }
 
         Result result = new Result(attrs, bco.getHostname());
 
         logging.saveGetNewResult(result);
 
         return result;
     }
 
     /**
      * Gets the per-host statistics of a currently running search.
      * 
      * @return a map of hostnames to statistics for each host
      * @throws InterruptedException
      *             if the thread is interrupted
      * @throws IOException
      *             if an IO error occurs
      * @throws SearchClosedException
      *             if this <code>Search</code> is closed
      */
     public Map<String, ServerStatistics> getStatistics() throws IOException,
             InterruptedException {
         checkClosed();
 
         Map<String, ServerStatistics> result = new HashMap<String, ServerStatistics>();
         synchronized (rpcLock) {
             // request_stats = 15
             CompletionService<MiniRPCReply> results = cs
                     .sendToAllControlChannels(15, new byte[0]);
 
             try {
                 for (int i = 0; i < cs.size(); i++) {
                     try {
                         MiniRPCReply reply = results.take().get();
 
                        // don't fail on NOSTATSAVAIL
                        if (reply.getMessage().getStatus() == RPC.DIAMOND_NOSTATSAVAIL) {
                            continue;
                        }

                         reply.checkStatus();
 
                         String host = reply.getHostname();
                         MiniRPCMessage msg = reply.getMessage();
                         XDR_dev_stats stats = new XDR_dev_stats(msg.getData());
 
                         // add
                         result.put(host, new ServerStatistics(stats
                                 .getObjsTotal(), stats.getObjsProcessed(),
                                 stats.getObjsDropped()));
 
                     } catch (ExecutionException e) {
                         Throwable cause = e.getCause();
                         if (cause instanceof IOException) {
                             IOException e2 = (IOException) cause;
                             throw e2;
                         }
                     }
                 }
 
             } catch (IOException e) {
                 close(e);
                 throw e;
             }
         }
         logging.updateStatistics(result);
         return result;
     }
 
     /**
      * Takes a map of named doubles and a <code>DoubleComposer</code> and
      * composes the values with those on each server. This operation is highly
      * dependent on the particulars of the filters running on the server.
      * 
      * @param globalValues
      *            a map of named doubles, representing the master copy of values
      * @param composer
      *            a <code>DoubleComposer</code> that will be used locally to
      *            merge values. The filters in use on the server may use
      *            different composers.
      * @return a new map of updated session variables
      * @throws InterruptedException
      *             if the thread is interrupted
      * @throws IOException
      *             if an IO error occurs
      * @throws SearchClosedException
      *             if this <code>Search</code> is closed
      */
     public Map<String, Double> mergeSessionVariables(
             Map<String, Double> globalValues, DoubleComposer composer)
             throws IOException, InterruptedException {
         checkClosed();
 
         // collect all the session variables
         List<SessionVariables> sv = getSessionVariables();
 
         // build new state
         composeVariables(globalValues, composer, sv);
 
         // set it all back
         setSessionVariables(globalValues);
 
         return globalValues;
     }
 
     private void composeVariables(Map<String, Double> globalValues,
             DoubleComposer composer, List<SessionVariables> sv) {
 
         // System.out.println("INPUT: " + Arrays.toString(sv));
 
         // first, gather all possible keys
         for (SessionVariables v : sv) {
             for (String key : v.getVariables().keySet()) {
                 if (!globalValues.containsKey(key)) {
                     globalValues.put(key, 0.0);
                 }
             }
         }
 
         // now, compose them
         for (Map.Entry<String, Double> e : globalValues.entrySet()) {
             String key = e.getKey();
 
             for (SessionVariables v : sv) {
                 Map<String, Double> localValues = v.getVariables();
 
                 // compose !
                 double global = e.getValue();
                 double local = localValues.containsKey(key) ? localValues
                         .get(key) : 0.0;
                 double composedValue = composer.compose(key, global, local);
                 globalValues.put(key, composedValue);
             }
         }
 
         // System.out.println("OUTPUT: " + globalValues);
     }
 
     private List<SessionVariables> getSessionVariables() throws IOException,
             InterruptedException {
         List<SessionVariables> result = new ArrayList<SessionVariables>();
 
         synchronized (rpcLock) {
             // session_variables_get = 18
             CompletionService<MiniRPCReply> results = cs
                     .sendToAllControlChannels(18, new byte[0]);
             try {
                 for (int i = 0; i < cs.size(); i++) {
                     try {
                         MiniRPCReply reply = results.take().get();
 
                         reply.checkStatus();
 
                         List<XDR_diamond_session_var> vars = new XDR_diamond_session_vars(
                                 reply.getMessage().getData()).getVars();
 
                         // add
                         Map<String, Double> serverVars = new HashMap<String, Double>();
                         for (XDR_diamond_session_var v : vars) {
                             serverVars.put(v.getName(), v.getValue());
                         }
                         result.add(new SessionVariables(reply.getHostname(),
                                 serverVars));
                     } catch (ExecutionException e) {
                         Throwable cause = e.getCause();
                         if (cause instanceof IOException) {
                             IOException e2 = (IOException) cause;
                             throw e2;
                         }
                     }
                 }
             } catch (IOException e) {
                 close(e);
                 throw e;
             }
         }
 
         return result;
 
     }
 
     private void setSessionVariables(Map<String, Double> map)
             throws InterruptedException, IOException {
         // encode
         List<XDR_diamond_session_var> vars = new ArrayList<XDR_diamond_session_var>();
         for (Map.Entry<String, Double> e : map.entrySet()) {
             vars.add(new XDR_diamond_session_var(e.getKey(), e.getValue()));
         }
         byte data[] = new XDR_diamond_session_vars(vars).encode();
 
         synchronized (rpcLock) {
             // session_variables_set = 19
             CompletionService<MiniRPCReply> results = cs
                     .sendToAllControlChannels(19, data);
             try {
                 for (int i = 0; i < cs.size(); i++) {
                     try {
                         results.take().get().checkStatus();
                     } catch (ExecutionException e) {
                         Throwable cause = e.getCause();
                         if (cause instanceof IOException) {
                             IOException e2 = (IOException) cause;
                             throw e2;
                         }
                     }
                 }
             } catch (IOException e) {
                 close(e);
                 throw e;
             }
         }
         logging.saveSessionVariables(map);
     }
 
     Search(ConnectionSet connectionSet, LoggingFramework logging) {
         this.cs = connectionSet;
         this.logging = logging;
     }
 }
