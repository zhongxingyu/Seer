 /**
  * PacketClient.java
  *
  * Copyright 2011 Niolex, Inc.
  *
  * Niolex licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.niolex.network;
 
 import java.nio.ByteBuffer;
 
 import org.apache.niolex.commons.codec.StringUtil;
 
 /**
  * The helper class of Packet, handle reads and writes of Packet.
  * User can use this class directly.
  *
  * @author Xie, Jiyun
  */
 public class PacketData extends Packet implements Cloneable {
     private static final byte[] Z_LEN_STUB = new byte[0];
     // The HEART_BEAT Packet is for test the connectivity between server and client
     private static final PacketData HEART_BEAT = new PacketData(Config.CODE_HEART_BEAT, Z_LEN_STUB);
     private static final int MAX_SIZE = Config.SERVER_MAX_PACKET_SIZE;
 
     /**
      * Get the default heart beat packet.
      *
      * @return the heart beat packet.
      */
     public static final PacketData getHeartBeatPacket() {
         return HEART_BEAT;
     }
 
     /**
      * Default constructor.
      * version is set to 1.
      * All other fields are not set, set them before use the packet.
      */
     public PacketData() {
         super();
         this.version = 1;
     }
 
     /**
      * Create a packet with only packet code.
      * packet data will be set to an array of 0 length.
      *
      * @param code the packet code
      */
     public PacketData(int code) {
     	this(code, Z_LEN_STUB);
     }
 
     /**
      * Create a packet with packet code and String data.
      * String will be encoded as UTF-8.
      *
      * @param code the packet code
      * @param data the packet data in string format
      */
     public PacketData(int code, String data) {
     	this(code, StringUtil.strToUtf8Byte(data));
     }
 
     /**
      * Create packet by packet code and data.
      *
      * @param code the packet code
      * @param data the packet data in byte array
      */
     public PacketData(int code, byte[] data) {
     	this((short)code, data);
     }
 
     /**
      * Create packet by code and data.
      *
      * @param code the packet code
      * @param data the packet data in byte array
      */
     public PacketData(short code, byte[] data) {
         super();
         this.version = 1;
         this.code = code;
         this.data = data;
         this.length = data.length;
     }
 
     /**
      * Make a copy of this packet. All fields will be copied.
      *
      * @return the copy.
      */
     @Override
     public PacketData clone() {
     	PacketData other = new PacketData(this.code, this.data);
     	other.reserved = this.reserved;
     	other.version = this.version;
     	return other;
     }
 
     /**
      * Generate Data from this Packet into the ByteBuffer.
      * Please make sure there are at least 8 bytes left in the buffer.
      *
      * @param bb byte buffer used to put the header
      */
     public void putHeader(ByteBuffer bb) {
         bb.put(version);
         bb.put(reserved);
         bb.putShort(code);
         bb.putInt(length);
     }
 
     /**
      * Parse Packet header from the ByteBuffer.
      * We will create the data array for you to put in the packet content, but
      * user need to put data into the body themselves.
      *
      * @param bb the header byte buffer
     * @throws IllegalStateException if the packet is larger than
     * {@link Config#SERVER_MAX_PACKET_SIZE}
      */
     public void parseHeader(ByteBuffer bb) {
         version = bb.get();
         reserved = bb.get();
         code = bb.getShort();
         length = bb.getInt();
 
         if (length > MAX_SIZE) {
         	throw new IllegalStateException("The packet length is larger than the max size: " + length);
         }
         data = new byte[length];
     }
 
 }
