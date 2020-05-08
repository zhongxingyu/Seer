 package edu.cmu.cs.diamond.opendiamond;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.*;
 
 class ConnectionSet {
     private static class BlastGetter implements Callable<Object> {
 
         private final BlockingQueue<BlastChannelObject> q;
 
         private final Connection connection;
 
         private final String hostname;
 
         public BlastGetter(Connection connection, String hostname,
                 BlockingQueue<BlastChannelObject> blastQueue) {
             this.connection = connection;
             this.hostname = hostname;
             this.q = blastQueue;
         }
 
         private XDR_object getAndAcknowldgeBlastChannelObject()
                 throws IOException {
             System.out.println(hostname + ": waiting for blast object");
             MiniRPCMessage incoming = connection.receiveBlast();
             XDR_object obj = new XDR_object(incoming.getData());
             System.out.println(hostname + ":   blast object done");
 
             // ack
             System.out.println(hostname + ": sending credit");
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(baos);
             out.writeInt(1); // 1 credit
 
             connection.sendMessageBlast(1, baos.toByteArray());
             System.out.println(hostname + ":   credit done");
 
             return obj;
         }
 
         @Override
         public Object call() throws Exception {
             // block, waiting for blast channel object, then stick into queue
             while (true) {
                 XDR_object obj = getAndAcknowldgeBlastChannelObject();
 
                 // no more objects?
                 if (obj.getAttributes().isEmpty()) {
                     return null;
                 }
 
                 q.put(new BlastChannelObject(obj, hostname, null));
             }
         }
     }
 
     private final Set<Connection> connections;
 
     private final BlockingQueue<BlastChannelObject> blastQueue = new ArrayBlockingQueue<BlastChannelObject>(
             20);
 
     private final ExecutorService executor;
 
     ConnectionSet(ExecutorService executor, Set<Connection> connections) {
         this.executor = executor;
         this.connections = new HashSet<Connection>(connections);
 
         executor.execute(new Runnable() {
             @Override
             public void run() {
                 CompletionService<Object> blastTasks = new ExecutorCompletionService<Object>(
                         ConnectionSet.this.executor);
 
                 int blastCount = 0;
                 for (Connection c : ConnectionSet.this.connections) {
                     // create tasks for getting blast messages
                     blastTasks.submit(new BlastGetter(c, c.getHostname(),
                             blastQueue));
                     blastCount++;
                 }
 
                 // wait for things to finish
                 try {
                     for (int i = 0; i < blastCount; i++) {
                         blastTasks.take().get();
                     }
                 } catch (InterruptedException e) {
                     // ok, we're leaving now
                    e.printStackTrace();
                 } catch (ExecutionException e) {
                     Throwable cause = e.getCause();
                     if (cause instanceof IOException) {
                         IOException e2 = (IOException) cause;
 
                         // inject into blast queue
                        try {
                            blastQueue.put(new BlastChannelObject(null, null,
                                    e2));
                        } catch (InterruptedException e1) {
                            // we're going to exit anyway
                            e1.printStackTrace();
                        }
                     }
                 }
 
                 // all tasks done, inject final object and close
                try {
                    addNoMoreResultsToBlastQueue();
                } catch (InterruptedException e) {
                    // nothing we can do, just close and exit
                    e.printStackTrace();
                }
                 close();
             }
         });
     }
 
     public void close() {
         for (Connection c : connections) {
             c.close();
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
             @Override
             public Callable<MiniRPCReply> createCallable(Connection c) {
                 return new RPC(c, c.getHostname(), cmd, data);
             }
         });
     }
 
     public int size() {
         return connections.size();
     }
 
    public void addNoMoreResultsToBlastQueue() throws InterruptedException {
        blastQueue.put(BlastChannelObject.NO_MORE_RESULTS);
     }
 }
