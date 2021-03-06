 package org.bouncycastle.crypto.tls.test;
 
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.security.SecureRandom;
 
 import org.bouncycastle.crypto.tls.DTLSClientProtocol;
 import org.bouncycastle.crypto.tls.DatagramTransport;
 import org.bouncycastle.crypto.tls.UDPTransport;
 
 /**
  * A simple test designed to conduct a DTLS handshake with an external DTLS server.
  * 
  * Please refer to GnuTLSSetup.txt or OpenSSLSetup.txt, and x509-*.pem files in this package for
  * help configuring an external DTLS server.
  */
 public class DTLSClientTest {
 
     public static void main(String[] args) throws Exception {
 
         SecureRandom secureRandom = new SecureRandom();
 
         DatagramSocket socket = new DatagramSocket();
        // socket.connect(InetAddress.getLocalHost(), 5556);
        socket.connect(InetAddress.getByName("192.168.105.100"), 5556);
 
         int mtu = 1500;
         DatagramTransport transport = new UDPTransport(socket, mtu);
 
         transport = new UnreliableDatagramTransport(transport, secureRandom, 0, 0);
 
         transport = new LoggingDatagramTransport(transport, System.out);
 
         DTLSClientProtocol protocol = new DTLSClientProtocol(secureRandom);
 
         MockDTLSClient client = new MockDTLSClient();
         DatagramTransport dtls = protocol.connect(client, transport);
 
         System.out.println("Receive limit: " + dtls.getReceiveLimit());
         System.out.println("Send limit: " + dtls.getSendLimit());
 
         // Send and hopefully receive a packet back
 
         byte[] request = "Hello World!\n".getBytes("UTF-8");
         dtls.send(request, 0, request.length);
 
         byte[] response = new byte[dtls.getReceiveLimit()];
         int received = dtls.receive(response, 0, response.length, 30000);
         if (received >= 0) {
             System.out.println(new String(response, 0, received, "UTF-8"));
         }
 
         socket.close();
     }
 }
