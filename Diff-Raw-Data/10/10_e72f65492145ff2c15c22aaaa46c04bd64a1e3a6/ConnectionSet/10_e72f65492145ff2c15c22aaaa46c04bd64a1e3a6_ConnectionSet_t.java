 /*
  *  The OpenDiamond Platform for Interactive Search
  *
  *  Copyright (c) 2009 Carnegie Mellon University
  *  All rights reserved.
  *
  *  This software is distributed under the terms of the Eclipse Public
  *  License, Version 1.0 which can be found in the file named LICENSE.
  *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
  *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
  */
 
 package edu.cmu.cs.diamond.opendiamond;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.*;
 
 class ConnectionSet {
     private final Set<Connection> connections;
 
     private final BlastQueue blastQueue = new BlastQueue(20);
 
     private final ExecutorService executor;
 
     private final List<Future<?>> blastFutures = new ArrayList<Future<?>>();
 
     private final Future<?> connectionSetFuture;
 
     private volatile boolean closing;
 
     ConnectionSet(ExecutorService executor, Set<Connection> connections) {
         this.executor = executor;
         this.connections = new HashSet<Connection>(connections);
 
         // create tasks for getting blast messages
         final CompletionService<Object> blastTasks = new ExecutorCompletionService<Object>(
                 executor);
         for (Connection c : connections) {
             blastFutures.add(blastTasks.submit(new BlastGetter(c, c
                     .getHostname(), blastQueue, 10)));
         }
         final int tasksCount = blastFutures.size();
 
         // wait for things to finish
         connectionSetFuture = executor.submit(new Callable<Object>() {
             public Object call() {
                 try {
                     for (int i = 0; i < tasksCount; i++) {
                         blastTasks.take().get();
                     }
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                 } catch (ExecutionException e) {
                     cancelAllBlastTasks();
 
                     Throwable cause = e.getCause();
                    if (!closing) {
                        IOException e2;
                        if (cause instanceof IOException) {
                            e2 = (IOException) cause;
                        } else {
                            e2 = new IOException("couldn't read blast channel",
                                    cause);
                        }
 
                         // inject into blast queue, only if we are not closing
                         try {
                             blastQueue.put(new BlastChannelObject(null, null,
                                     e2));
                         } catch (InterruptedException e1) {
                             Thread.currentThread().interrupt();
                         }
                     }
                 } finally {
                     // all tasks done, shut down queue
                     blastQueue.shutdown();
                 }
 
                 return null;
             }
         });
     }
 
     public void close() throws InterruptedException {
         closing = true;
 
         // cancel all blast tasks
         cancelAllBlastTasks();
 
         // close all connections
         for (Connection c : connections) {
             c.close();
         }
 
         // wait for cancellations and shutdown
         try {
             connectionSetFuture.get();
         } catch (ExecutionException e) {
             // ignore
         }
     }
 
     private void cancelAllBlastTasks() {
         for (Future<?> f : blastFutures) {
             f.cancel(true);
         }
     }
 
     public BlastChannelObject getNextBlastChannelObject()
             throws InterruptedException {
         return blastQueue.take();
     }
 
     public <T> CompletionService<T> runOnAllServers(ConnectionFunction<T> cf) {
         CompletionService<T> cs = new ExecutorCompletionService<T>(executor);
 
         for (Connection c : connections) {
             cs.submit(cf.createCallable(c));
         }
 
         return cs;
     }
 
     public CompletionService<MiniRPCReply> sendToAllControlChannels(
             final int cmd, final byte[] data) {
         return runOnAllServers(new ConnectionFunction<MiniRPCReply>() {
             public Callable<MiniRPCReply> createCallable(Connection c) {
                 return new RPC(c, c.getHostname(), cmd, data);
             }
         });
     }
 
     public int size() {
         return connections.size();
     }
 }
