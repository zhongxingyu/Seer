 /**
  * Copyright 2011 Glenn Maynard
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
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
 
 package org.jivesoftware.smack.filter;
 
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.ReceivedPacket;
 import org.w3c.dom.Element;
 
 /**
  * Filter ReceivedPacket filters for a given localName/namespace tuple.
  *
  * @see org.jivesoftware.smack.packet.ReceivedPacket
  */
 public class ReceivedPacketFilter implements PacketFilter {
     private final String localName;
     private final String namespaceURI;
 
     /**
      * Filter messages with the given localName and namespace.  If localName is
      * null, receive all packets in the given namespace.
      */
     public ReceivedPacketFilter(String localName, String namespaceURI) {
        if(namespaceURI.isEmpty())
             throw new IllegalArgumentException("namespaceURI must not be null");
 
         this.localName = localName;
         this.namespaceURI = namespaceURI;
     }
 
     public boolean accept(Packet packet) {
         if (!(packet instanceof ReceivedPacket)) {
             return false;
         }
         ReceivedPacket receivedPacket = (ReceivedPacket) packet;
         Element root = receivedPacket.getElement();
         if(localName != null && !root.getLocalName().equals(localName))
             return false;
         return root.getNamespaceURI().equals(namespaceURI);
     }
 
 }
