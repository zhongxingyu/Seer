 /**
  * PacketDataTest.java
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
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import org.junit.Test;
 
 public class PacketDataTest {
 
     @Test
     public void testMakeCopy() throws Exception {
         byte[] arr = "lex implemented".getBytes();
         PacketData pc = new PacketData((short) 56, arr);
         PacketData qc = pc.makeCopy();
         assertArrayEquals(qc.getData(), pc.getData());
         assertEquals(qc.getCode(), pc.getCode());
     }
 
     @Test
     public void testPutHeader() throws Exception {
         byte[] arr = "lex implemented".getBytes();
         PacketData pc = new PacketData(47, arr);
         ByteBuffer ba = ByteBuffer.allocate(8);
         pc.putHeader(ba);
         ba.flip();
         assertEquals(1, ba.get());
         assertEquals(0, ba.get());
         assertEquals(47, ba.getShort());
         assertEquals(15, ba.getInt());
     }
 
 
     @Test
     public void testGenerateData() throws Exception {
         byte[] arr = "lex implemented".getBytes();
         PacketData pc = new PacketData(47, arr);
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream(bout);
         pc.generateData(out);
         ByteBuffer ba = ByteBuffer.wrap(bout.toByteArray());
         assertEquals(1, ba.get());
         assertEquals(0, ba.get());
         assertEquals(47, ba.getShort());
         assertEquals(15, ba.getInt());
     }
 
     @Test
     public void testparseHeader() {
         PacketData pc = new PacketData();
         ByteBuffer ba = ByteBuffer.allocate(8);
         ba.putInt(12345);
         ba.putInt(10485760);
         ba.flip();
         pc.parseHeader(ba);
         assertEquals(10485760, pc.getLength());
     }
 
     @Test(expected=IllegalStateException.class)
     public void testparseHeaderExceedMax() {
         PacketData pc = new PacketData();
         ByteBuffer ba = ByteBuffer.allocate(8);
         ba.putInt(12345);
         ba.putInt(10485761);
         ba.flip();
         pc.parseHeader(ba);
         assertEquals(10485760, pc.getLength());
     }
 
     @Test
     public void testParsePacket() throws IOException {
         byte[] arr = "lex implemented".getBytes();
         PacketData pc = new PacketData(47, arr);
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream(bout);
         pc.generateData(out);
         DataInputStream in = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()));
         PacketData qc = new PacketData();
         qc.parsePacket(in);
         assertArrayEquals(qc.getData(), pc.getData());
         assertEquals(qc.getCode(), pc.getCode());
         assertEquals(qc.getVersion(), pc.getVersion());
     }
 
     @Test(expected=IOException.class)
     public void testParsePacketEOF() throws IOException {
         byte[] b = new byte[9];
         for (int i = 0; i < 7; ++i) {
             b[i] = 0;
         }
         b[7] = b[8] = 10;
         DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
        PacketData p = PacketData.getHeartBeatPacket().makeCopy();
         p.parsePacket(in);
     }
 
     @Test(expected=IOException.class)
     public void testparsePacketTooLarge() throws IOException {
         PacketData pc = new PacketData();
         ByteBuffer ba = ByteBuffer.allocate(12);
         ba.putInt(12345);
         ba.putInt(10485761);
         ba.putInt(10485761);
 
         pc.parsePacket(new DataInputStream(new ByteArrayInputStream(ba.array())));
         assertEquals(10485760, pc.getLength());
     }
 
 }
