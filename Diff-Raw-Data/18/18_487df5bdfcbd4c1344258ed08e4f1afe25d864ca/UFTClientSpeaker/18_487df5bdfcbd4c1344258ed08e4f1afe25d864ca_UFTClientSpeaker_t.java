 package org.DataCom.Client;
 
 import java.util.*;
 import java.util.concurrent.*;
 import java.io.*;
 import java.net.*;
 import org.DataCom.Utility.*;
 
 public class UFTClientSpeaker implements UFTPacketSpeaker {
 
     /*
      * Parent server
      */
     UDPFileTransferClient client;
 
 
 
 
 
 
     // /////////////////////////////////////////////////////////////////
     //   Constructors
     // /////////////////////////////////////////////////////////////////
 
 
     /*
      * UFT Packet Listener
      */
     public UFTClientSpeaker(UDPFileTransferClient client) {
 	this.client = client;
     }
 
 
     // /////////////////////////////////////////////////////////////////
     //   Methods
     // /////////////////////////////////////////////////////////////////
 
 
     /*
      * Is there Data to send?
      */
     public boolean hasData() {
 	return ! client.getSendQueue().isEmpty();
     }
 
 
     /*
      * Try to send a UFTPacket
      */
     public void sendPacket(UFTPacket packet, int port, InetAddress address) throws IOException{
	// ensure checksum
	packet.prepareForSend();
	//create datagram packet
 	byte[] packetData = packet.toBytes();
 	DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length, address, port);
	// send it
	Debug.pln("sending packet: "+packet.getHeader().getChecksum());
 	client.sendSocket.send(sendPacket);
 
     }
 
 
 
     /*
     * Thread run loop
      */
     public void run() {
 	while(this.hasData()) {
 	    UFTPacket next = client.getSendQueue().poll();
 	    try {
 		this.sendPacket(next, client.getSendPort(), client.getServerAddress());
 	    } catch (IOException se) {
 		System.out.println("Failed to send packet, requeueing");
 		this.client.enqueueForSend(next);
 	    }
 	}
 
     }
 }
