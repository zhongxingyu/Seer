 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.bigfs.internode.message;
 
 
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.bigfs.internode.configuration.MessagingConfiguration;
 import org.bigfs.internode.events.CloseMessageConnectionEvent;
 import org.bigfs.internode.events.DiscardedMessageEvent;
 import org.bigfs.internode.events.MessageSendingEvent;
 import org.bigfs.internode.serialization.InetAddressSerialization;
 import org.bigfs.internode.service.MessagingService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xerial.snappy.SnappyOutputStream;
 
 public class MessageConnection extends Thread {
 
 	private static final Logger logger = LoggerFactory.getLogger(MessageConnection.class);
 	
 	private static final MessageOut CLOSE_SENTINEL = new MessageOut("CLOSE");
 	private volatile boolean isStopped = false;
 	private final MessageConnectionPool poolInstance; 
 	
     private volatile BlockingQueue<QueuedMessage> backlog = new LinkedBlockingQueue<QueuedMessage>();
     private volatile BlockingQueue<QueuedMessage> active = new LinkedBlockingQueue<QueuedMessage>();
     private volatile long completed = 0;
     private volatile AtomicLong dropped = new AtomicLong(0);
     private Socket socket;
     private DataOutputStream out;
     private int targetVersion;
     private int OPEN_RETRY_DELAY = 1000;
     
    
     
 	public MessageConnection(MessageConnectionPool pool){
 		super("WRITE-" + pool.getRemoteAddress());
 		
		this.poolInstance = pool;	
		
		this.start();
 	}
 	
 	public void run()
     {
         while (true)
         {   
             QueuedMessage qm = active.poll();
             if (qm == null)
             {
                 // exhausted the active queue.  switch to backlog, once there's something to process there
                 try
                 {
                     qm = backlog.take();
                 }
                 catch (InterruptedException e)
                 {
                     throw new AssertionError(e);
                 }
 
                 BlockingQueue<QueuedMessage> tmp = backlog;
                 backlog = active;
                 active = tmp;
             }
 
             MessageOut<?> m = qm.message;
             if (m == CLOSE_SENTINEL)
             {
                 disconnect();  
                 if(isStopped)                    
                     break; 
                 continue;
             }
             if (qm.timestamp < System.currentTimeMillis() - m.getMessageTimeout())
             {
                 MessagingService.instance().getEventHandler().post(new DiscardedMessageEvent(qm));
                 dropped.incrementAndGet();
             }
             else if (socket != null || connect())
            {
                
                 writeConnected(qm);
            }
             else
            {
                 // clear out the queue, else gossip messages back up.
                 active.clear();
            }
         }
     }
 	
 	private boolean connect()
     {
         if (logger.isDebugEnabled())
             logger.debug("attempting to connect to " + poolInstance.getRemoteAddress());
 
         targetVersion = MessagingService.instance().getRemoteMessagingVersion(poolInstance.getRemoteAddress());
 
         long start = System.currentTimeMillis();
         while (System.currentTimeMillis() < start + MessagingConfiguration.getRpcTimeout())
         {
             try
             {
                 socket = poolInstance.getSocket();
                 socket.setKeepAlive(true);
                 /*
                 if (isLocalDC(poolInstance.getRemoteAddress()))
                 {
                     socket.setTcpNoDelay(true);
                 }
                 else
                 {
                     socket.setTcpNoDelay(DatabaseDescriptor.getInterDCTcpNoDelay());
                 }
                 */
                 
                 out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 4096));
                 out.writeInt(MessagingService.PROTOCOL_MAGIC);
                 writeHeader(out, MessagingConfiguration.isCompressionEnable(), false, targetVersion);
                 out.flush();
 
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 int maxTargetVersion = in.readInt();
                 if (targetVersion > maxTargetVersion)
                 {
                     logger.debug("Target max version is {}; will reconnect with that version", maxTargetVersion);
                     MessagingService.instance().setRemoteMessagingVersion(poolInstance.getRemoteAddress(), maxTargetVersion);
                     disconnect();
                     return false;
                 }
 
                 if (targetVersion < maxTargetVersion && targetVersion < MessagingService.CURRENT_VERSION)
                 {
                     logger.trace("Detected higher max version {} (using {}); will reconnect when queued messages are done",
                                  maxTargetVersion, targetVersion);
                     MessagingService.instance().setRemoteMessagingVersion(poolInstance.getRemoteAddress(), Math.min(MessagingService.CURRENT_VERSION, maxTargetVersion));
                     softCloseSocket();
                 }
                 
                 InetAddressSerialization.serialize(MessagingConfiguration.getListenAddress(), out);
                 
                 if (MessagingConfiguration.isCompressionEnable())
                 {
                     out.flush();
                     logger.trace("Upgrading OutputStream to be compressed");
                     out = new DataOutputStream(new SnappyOutputStream(new BufferedOutputStream(socket.getOutputStream())));
                 }
             
                 MessagingService.instance().getEventHandler().post(new MessageSendingEvent(
                         poolInstance.getRemoteAddress(), maxTargetVersion
                 ));
 
                 return true;
             }
             catch (IOException e)
             {
                 socket = null;
                 if (logger.isTraceEnabled())
                     logger.trace("unable to connect to " + poolInstance.getRemoteAddress(), e);
                 try
                 {
                     Thread.sleep(OPEN_RETRY_DELAY);
                 }
                 catch (InterruptedException e1)
                 {
                     throw new AssertionError(e1);
                 }
             }
         }
         return false;
     }
 	
 	void softCloseSocket()
     {
         addToQueue(CLOSE_SENTINEL, null, null);
     }
 	
 	private void disconnect()
     {
         if (socket != null)
         {
             try
             {
                 socket.close();
             }
             catch (IOException e)
             {
                 if (logger.isTraceEnabled())
                     logger.trace("exception closing connection to " + poolInstance.getRemoteAddress(), e);
             }
             out = null;
             socket = null;
         }
     }
     public void addToQueue(MessageOut<?> message, String id, String replyTo)
     {
         
         expireMessages();
         try
         {
             backlog.put(new QueuedMessage(message, id, replyTo, System.currentTimeMillis()));
         }
         catch (InterruptedException e)
         {
             throw new AssertionError(e);
         }
     }
     private void writeConnected(QueuedMessage qm)
     {
         try
         {
             write(qm.message, qm.id, qm.replyTo, qm.timestamp, out, targetVersion);
             completed++;
             if (active.peek() == null)
             {
                 out.flush();
             }
         }
         catch (Exception e)
         {
             // Non IO exceptions is likely a programming error so let's not silence it
             if (!(e instanceof IOException))
                 logger.error("error writing to " + poolInstance.getRemoteAddress(), e);
             else if (logger.isDebugEnabled())
                 logger.debug("error writing to " + poolInstance.getRemoteAddress(), e);
             disconnect();
         }
     }
     
     public static void write(MessageOut message, String id, String replyTo, long timestamp, DataOutputStream out, int version) throws IOException
     {
        
         out.writeUTF(id);
         out.writeUTF(replyTo == null ? "-1": replyTo);
         out.writeInt((int) timestamp);        
         message.serialize(out, version);
     }
     
     private void expireMessages()
     {
         while (true)
         {
             QueuedMessage qm = backlog.peek();
             if (qm == null || qm.timestamp >= System.currentTimeMillis() - qm.message.getMessageTimeout())
                 break;
 
             QueuedMessage qm2 = backlog.poll();
             if (qm2 != qm)
             {
                 // sending thread switched queues.  add this entry (from the "new" backlog)
                 // at the end of the active queue, which keeps it in the same position relative to the other entries
                 // without having to contend with other clients for the head-of-backlog lock.
                 if (qm2 != null)
                     active.add(qm2);
                 break;
             }
         }
     }
     
     private void writeHeader(DataOutputStream out, boolean compressionEnabled, boolean isStream, int guessedRemoteVersion) throws IOException {        
         out.writeInt(MessagingService.getMessageHeader(compressionEnabled, isStream, guessedRemoteVersion));
     } 
     
     public void close(boolean destroyThread)
     {
         active.clear();
         backlog.clear();
         isStopped = destroyThread; // Exit loop to stop the thread
         addToQueue(CLOSE_SENTINEL, null, null);
         MessagingService.instance().getEventHandler().post(new CloseMessageConnectionEvent(
                 poolInstance.getRemoteAddress(), destroyThread
         ));
     }
     
 	public static class QueuedMessage
     {
         final MessageOut<? extends IMessage> message;
         final String id;
         final long timestamp;
         final String replyTo;
         
         public QueuedMessage(MessageOut<?> message, String id, String replyTo, long timestamp)
         {
             this.message = message;
             this.id = id;
             this.timestamp = timestamp;
             this.replyTo = replyTo;
         }
     }
 	
 	public int getPendingMessages()
     {
         return active.size() + backlog.size();
     }
 
    public long getCompletedMessages()
     {
         return completed;
     }
 
     public long getDroppedMessages()
     {
         return dropped.get();
     }
 }
