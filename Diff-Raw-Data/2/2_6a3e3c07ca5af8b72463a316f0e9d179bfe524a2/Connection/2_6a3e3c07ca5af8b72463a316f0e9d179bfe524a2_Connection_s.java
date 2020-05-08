 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.controller.core.manager.connection;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import org.apache.cassandra.thrift.Cassandra.Client;
 import org.apache.thrift.protocol.TBinaryProtocol;
 import org.apache.thrift.transport.TFramedTransport;
 import org.apache.thrift.transport.TSocket;
 import org.apache.thrift.transport.TTransport;
 import org.apache.thrift.transport.TTransportException;
 
 import ch.qos.logback.classic.Logger;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.utils.MLogger;
 
 /**
  * Class that represents a connection to a Cassandra instance in mymed
  * 
  * @author Milo Casagrande
  * 
  */
 public class Connection implements IConnection {
   // Used for the hash code
   private static final int PRIME = 31;
 
   private static final int DEFAULT_PORT = 4201;
 
   private static final Logger LOGGER = MLogger.getLogger();
 
   // The port and the address associated with this connection
   private String address = null;
   private int port = 0;
 
   private final TSocket socket;
   private final TTransport transport;
   private final Client cassandra;
   private final TBinaryProtocol protocol;
 
   /**
    * Create a new connection using as address the localhost address and the
    * default value for the port (9160).
    */
   public Connection() {
     this(null, 0);
   }
 
   /**
    * Create a new connection with the provided address and port
    * 
    * @param address
    *          the IP address to be associated with this connection
    * @param port
    *          the port to use with this connection
    * @throws Exception
    */
   public Connection(final String address, final int port) {
     if (address == null && (port == 0 || port < 0)) {
       try {
         this.address = InetAddress.getLocalHost().getHostAddress();
         this.port = DEFAULT_PORT;
       } catch (final UnknownHostException ex) {
         LOGGER.debug("Error recovering local host address", ex);
       }
     } else {
       this.address = address;
       this.port = port;
     }
 
     socket = new TSocket(this.address, this.port);
     transport = new TFramedTransport(socket);
     protocol = new TBinaryProtocol(transport);
 
     LOGGER.debug("Connection set to {}:{}", address, port);
     cassandra = new Client(protocol);
   }
 
   /**
    * @return the Cassandra instance to use
    */
   public Client getClient() {
     return cassandra;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mymed.controller.core.manager.connection.IConnection#getAddress()
    */
   @Override
   public String getAddress() {
     return address;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mymed.controller.core.manager.connection.IConnection#getPort()
    */
   @Override
   public int getPort() {
     return port;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mymed.controller.core.manager.connection.IConnection#isOpen()
    */
   @Override
   public boolean isOpen() {
     return transport.isOpen();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mymed.controller.core.manager.connection.IConnection#open()
    */
   @Override
   public void open() throws InternalBackEndException {
     try {
       if (!transport.isOpen()) {
         transport.open();
       }
     } catch (final TTransportException ex) {
       throw new InternalBackEndException("Error opening the connection to " + address + ":" + port); // NOPMD
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mymed.controller.core.manager.connection.IConnection#close()
    */
   @Override
   public void close() {
     if (transport.isOpen()) {
       transport.close();
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mymed.controller.core.manager.connection.IConnection#flush()
    */
   @Override
   public void flush() {
     if (protocol != null) {
       protocol.reset();
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
     int result = 1;
     result = PRIME * result + (address == null ? 0 : address.hashCode());
     result = PRIME * result + port;
     result = PRIME * result + (socket == null ? 0 : socket.hashCode());
     result = PRIME * result + (protocol == null ? 0 : protocol.hashCode());
     return result;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(final Object object) {
 
     boolean equal = false;
 
     if (this == object) {
       equal = true;
     } else if (object instanceof IConnection) {
       final IConnection comparable = (Connection) object;
       equal = true;
 
       if (address == null && comparable.getAddress() != null || address != null && comparable.getAddress() == null) {
         equal &= false;
      } else {
         equal &= address.equals(comparable.getAddress());
       }
 
       equal &= port == comparable.getPort();
     }
 
     return equal;
   }
 }
