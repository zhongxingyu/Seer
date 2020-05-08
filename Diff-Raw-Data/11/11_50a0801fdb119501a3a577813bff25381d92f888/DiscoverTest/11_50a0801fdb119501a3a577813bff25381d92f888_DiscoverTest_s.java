 package com.raphaelyu.phonemouse;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.Inet4Address;
 import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
 
 public class DiscoverTest {
 
     /**
      * @param args
      * @throws IOException
      */
     public static void main(String[] args) throws IOException {
         byte[] buf = new byte[1];
         InetAddress broadcastIP = Inet4Address.getByName("255.255.255.255");
 
         for (int i = 0; i < 10000; i++) {
             DatagramSocket socket = new DatagramSocket();
             socket.setSoTimeout(5000);
             DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastIP,
                     PhoneMouseServer.PHONE_MOUSE_PORT);
             buf[0] = PhoneMouseServer.PACKET_TYPE_DISCOVER;
             socket.send(packet);
             socket.receive(packet);
             if (packet.getLength() == 1) {
                 byte[] buffer = packet.getData();
                 if (buffer[0] != 0x2) {
                     throw new Error();
                 }
             } else {
                 throw new Error();
             }
             socket.close();
         }
         System.out.println("Test finished.");
     }
 
 }
