 /*
  * This file is part of the OpenNMS(R) Application.
  *
  * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
  * OpenNMS(R) is a derivative work, containing both original code, included code and modified
  * code that was published under the GNU General Public License. Copyrights for modified
  * and included code are below.
  *
  * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * For more information contact:
  * OpenNMS Licensing       <license@opennms.org>
  *     http://www.opennms.org/
  *     http://www.opennms.com/
  */
 package org.opennms.protocols.icmp6;
 
 import java.nio.ByteBuffer;
 
 
 
 /**
  * ICMPEchoRequest
  *
  * @author brozow
  */
 public class ICMPv6EchoRequest extends ICMPv6EchoPacket {
     
     public static final int PACKET_LENGTH = 64;
 
 
     public ICMPv6EchoRequest() {
         super(64);
         setType(Type.EchoRequest);
         setCode(0);
     }
 

    public ICMPv6EchoRequest(int size) {
        super(size);
        setType(Type.EchoRequest);
        setCode(0);
    }


     public ICMPv6EchoRequest(int id, int seqNum, long threadId) {
         this();
         
         setIdentifier(id);
         setSequenceNumber(seqNum);
         
         // data fields
         setThreadId(threadId);
         setCookie();
         // timestamp is set later
 
         // fill buffer with 'interesting' data
         ByteBuffer buf = getDataBuffer();
         for(int b = DATA_LENGTH; b < buf.limit(); b++) {
             buf.put(b, (byte)b);
         }
 
     }
     
 
    public ICMPv6EchoRequest(int id, int seqNum, long threadId, int size) {
        this(size);
        
        setIdentifier(id);
        setSequenceNumber(seqNum);
        
        // data fields
        setThreadId(threadId);
        setCookie();
        // timestamp is set later

        // fill buffer with 'interesting' data
        ByteBuffer buf = getDataBuffer();
        for(int b = DATA_LENGTH; b < buf.limit(); b++) {
            buf.put(b, (byte)b);
        }

    }
    

 }
