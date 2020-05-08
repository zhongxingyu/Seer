 /**
  * Copyright 2012 Markus Geiss
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.github.mgeiss.norn.util;
 
 import java.io.IOException;
 import java.util.Objects;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The
  * <code>NornProperties</code> class represents a persistent set of properties.
  *
  * @author Markus Geiss
  * @version 1.0
  */
 public class NornProperties {
 
     private static final String MULTICAST_ADDRESS_PROPERTY = "com.github.mgeiss.norn.multicast.address";
     private static final String MULTICAST_PORT_PROPERTY = "com.github.mgeiss.norn.multicast.port";
     private static final String SOCKET_TIMEOUT_PROPERTY = "com.github.mgeiss.norn.multicast.timeout";
     private static final String RMI_REGISTRY_PORT_PROPERTY = "com.github.mgeiss.norn.rmi.registry.port";
     private String multicastAddress;
     private int multicastPort = -1;
     private int socketTimeout = -1;
     private int rmiRegistryPort = -1;
 
     private NornProperties() {
         super();
     }
 
     /**
      * Reads the property list (key and element pairs) from
      * <tt>norn.properties</tt>. The file is assumed to use the ISO 8859-1
      * character encoding; that is each byte is one Latin1 character. Characters
      * not in Latin1, and certain special characters, are represented in keys
      * and elements using Unicode escapes.
      *
      * @return a new instance of <code>NornProperties</code> or <tt>null</tt>
      * @see java.util.Properties
      */
     public static NornProperties load() {
         NornProperties nornProperties = null;
 
         Properties properties = new Properties();
         try {
            properties.load(NornProperties.class.getResourceAsStream("norn.properties"));
         } catch (IOException ex) {
             Logger.getLogger(NornProperties.class.getName()).log(Level.SEVERE, "Could not load norn.properties!", ex);
         }
 
         nornProperties = new NornProperties();
         nornProperties.setMulticastAddress(properties.getProperty(NornProperties.MULTICAST_ADDRESS_PROPERTY));
         nornProperties.setMulticastPort(Integer.valueOf(properties.getProperty(NornProperties.MULTICAST_PORT_PROPERTY)));
         nornProperties.setSocketTimeout(Integer.valueOf(properties.getProperty(NornProperties.SOCKET_TIMEOUT_PROPERTY)));
         nornProperties.setRmiRegistryPort(Integer.valueOf(properties.getProperty(NornProperties.RMI_REGISTRY_PORT_PROPERTY)));
 
         return nornProperties;
     }
 
     /**
      * Returns the multicast address.
      * 
      * @return the host name or IP address or <tt>null</tt>
      */
     public String getMulticastAddress() {
         return this.multicastAddress;
     }
 
     private void setMulticastAddress(String multicastAddress) {
         this.multicastAddress = multicastAddress;
     }
 
     /**
      * Returns the multicast port.
      * 
      * @return a valid port number or -1
      */
     public int getMulticastPort() {
         return this.multicastPort;
     }
 
     private void setMulticastPort(int multicastPort) {
         this.multicastPort = multicastPort;
     }
 
     /**
      * Returns the RMI registry port.
      * 
      * @return a valid port number or -1 
      */
     public int getRmiRegistryPort() {
         return this.rmiRegistryPort;
     }
 
     private void setRmiRegistryPort(int rmiRegistryPort) {
         this.rmiRegistryPort = rmiRegistryPort;
     }
 
     /**
      * Returns the socket timeout.
      * 
      * @return a valid timeout or 0 
      */
     public int getSocketTimeout() {
         return this.socketTimeout;
     }
 
     private void setSocketTimeout(int socketTimeout) {
         this.socketTimeout = socketTimeout;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final NornProperties other = (NornProperties) obj;
         if (!Objects.equals(this.multicastAddress, other.multicastAddress)) {
             return false;
         }
         if (this.multicastPort != other.multicastPort) {
             return false;
         }
         if (this.socketTimeout != other.socketTimeout) {
             return false;
         }
         if (this.rmiRegistryPort != other.rmiRegistryPort) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 41 * hash + Objects.hashCode(this.multicastAddress);
         hash = 41 * hash + this.multicastPort;
         hash = 41 * hash + this.socketTimeout;
         hash = 41 * hash + this.rmiRegistryPort;
         return hash;
     }
 }
