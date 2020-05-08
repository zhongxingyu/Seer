 package com.society;
 
 import java.io.IOException;
 //import java.net.InetSocketAddress;
 //import java.net.UnknownHostException;
 //import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 
 public class Publish extends PacketProcessor {
 	
 	public Publish() {
 		// TODO Auto-generated constructor stub
 		super();
 	}
 
 	@Override
 	public void process(SocietyPacket inPacket, SocketChannel conn) {
 		int sendBufferSize;
 		byte[] sendData;
 		ByteBuffer sendBuffer;
 		ArrayList<SocketChannel> subscribers;
 		
 		subscribers = shared.subscribers.getSubscribers(inPacket.source);
 		if (subscribers != null) {
 			try {
 				for (SocketChannel sub: subscribers) {
 					if (sub.isOpen()) {
 						sendBufferSize = sub.socket().getSendBufferSize();
						sendData = inPacket.serialize();
						sendBuffer = ByteBuffer.wrap(sendData);
 						if (sendBuffer.capacity() > sendBufferSize) {
 							sendBuffer.limit(sendBufferSize);
 						}						
 						while (sendBuffer.position() != sendBuffer.capacity()) {
 //							could send less bytes than buffer size
 							sub.write(sendBuffer);
 							
 							if ((sendBuffer.limit() + sendBufferSize) <= sendBuffer.capacity()) {
 								sendBuffer.limit(sendBuffer.limit() + sendBufferSize);
 							} else {
 								sendBuffer.limit(sendBuffer.capacity());
 							}
 						}
 
 					} else {
 						shared.subscribers.removeSubscriber(sub);
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else {
 			System.out.println("no subscribers");
 		}
 		inPacket = null;
 	}
 }
