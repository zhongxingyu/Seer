 package com.github.jmpjct.mysql.proto;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 public class HandshakeTest {
     @Test
     public void test1() {
         // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::Handshake
         byte[] packet = new byte[] {
             (byte)0x36, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0a, (byte)0x35, (byte)0x2e, (byte)0x35,
             (byte)0x2e, (byte)0x32, (byte)0x2d, (byte)0x6d, (byte)0x32, (byte)0x00, (byte)0x0b, (byte)0x00,
             (byte)0x00, (byte)0x00, (byte)0x64, (byte)0x76, (byte)0x48, (byte)0x40, (byte)0x49, (byte)0x2d,
             (byte)0x43, (byte)0x4a, (byte)0x00, (byte)0xff, (byte)0xf7, (byte)0x08, (byte)0x02, (byte)0x00,
             (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
             (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x2a, (byte)0x34, (byte)0x64,
             (byte)0x7c, (byte)0x63, (byte)0x5a, (byte)0x77, (byte)0x6b, (byte)0x34, (byte)0x5e, (byte)0x5d,
             (byte)0x3a, (byte)0x00
         };
         
        assertEquals(packet, Handshake.loadFromPacket(packet).toPacket());
     }
     
     
 }
