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
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.nio.channels.SocketChannel;
 import java.util.Collection;
 
 import org.bigfs.internode.configuration.MessagingConfiguration;
 import org.bigfs.internode.metrics.ConnectionMetrics;
 import org.cliffc.high_scale_lib.NonBlockingHashMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 
 public class MessageConnectionPool {
 	
 	private static final Logger logger = LoggerFactory.getLogger(MessageConnectionPool.class);
 	
 	private final InetAddress id;
 	
 	private final NonBlockingHashMap<String, MessageConnection> connections = new NonBlockingHashMap<String, MessageConnection>();
 	
 	private ConnectionMetrics metrics;
 	
 	public MessageConnectionPool(InetAddress id) 
 	{
 		this.id = id;	
 		
 		metrics = new ConnectionMetrics(id, this);
 	}
 	
 	public MessageConnection getConnection(MessageOut<?> m)
 	{
 		 String connType = m.getMessageGroup();
		 
		 if(!this.connections.contains(connType))
 		 {
 			 logger.debug("create new connection to {} for {}", this.id, connType);
			 MessageConnection con = new MessageConnection(this);
			 con.start();
			 this.connections.put(connType, con);
 		 }
 		 
		 return this.connections.get(connType);
 	}
 	
 	
 	public MessageConnection getConnection(String groupName)
 	{
 	    return this.connections.get(groupName);
 	}
 	
 	public Collection<MessageConnection> getConnections()
 	{
 	    return this.connections.values();
 	}
 	
 	public Collection<String> getConnectionGroups()
 	{
         return this.connections.keySet();
     }
 	
 	public Socket getSocket() throws IOException
     {       
        return  SocketChannel.open(new InetSocketAddress(id, MessagingConfiguration.getPort())).socket();           
     }
 	
 	public InetAddress getRemoteAddress()
 	{
 		return this.id;
 	}
 	
     public long getRecentTimeouts()
     {
         return metrics.getRecentTimeout();
     }
 	
 	public long getTimeouts()
     {
        return metrics.timeouts.count();
     }
 	
     public void incrementTimeout()
     {
         metrics.timeouts.mark();
     }
 	
 	
 	public int getPendingMessages()
 	{
 	    int count = 0;
 	    for(MessageConnection con: this.connections.values())
 	    {
 	        count += con.getPendingMessages();
 	    }
 	    
 	    return count;
 	}
 	
 	public long getDroppedMessages()
     {
         long count = 0;
         for(MessageConnection con: this.connections.values())
         {
             count += con.getDroppedMessages();
         }
         
         return count;
     }
 	
 	public long getCompletedMesssages()
     {
         long count = 0;
         for(MessageConnection con: this.connections.values())
         {
             count += con.getCompletedMesssages();
         }
         
         return count;
     }
 	
 	public void close()
 	{
 		for(MessageConnection con: connections.values())
 		{
 			con.close(true);
 		}
 		metrics.release();
 	}
 }
