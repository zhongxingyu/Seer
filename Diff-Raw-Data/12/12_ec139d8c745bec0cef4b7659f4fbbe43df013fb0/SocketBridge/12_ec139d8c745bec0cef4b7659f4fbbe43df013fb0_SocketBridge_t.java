 package com.raphfrk.craftproxy;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 
 public class SocketBridge implements Runnable {
 
 	DataInputStream  in;
 	DataOutputStream out;
 
 	MyBoolean run = new MyBoolean(true);
 	MyBoolean running = new MyBoolean(true);
 
 	boolean server = false;
 	SocketMonitor monitor;
 
 	void kill() {
 
 		synchronized( running ) {
 			synchronized(run) {
 				run.set(false);
 			}
 			while( running.get() ) {
 				try {
 					running.wait();
 				} catch (InterruptedException e) {
 					return;
 				}
 			}
 		}
 	}
 
 
 	SocketBridge( DataInputStream  in, DataOutputStream out, SocketMonitor monitor, boolean server ) {
 
 		this.in = in;
 		this.out = out;
 		this.monitor = monitor;
 		this.server = server;
 
 	}
 
 	PacketIdStore packetIdStore = new PacketIdStore();
 	
 	boolean monitorExit = true;
 
 	public void run() {
 
 		boolean localRun = true;
 
 		Packet currentPacket = new Packet(in, server);
 		
 		monitorExit = monitor.process(currentPacket, out);
		
		int timeoutDuration = 0;
 
 		while( localRun && !currentPacket.eof && monitorExit) {
 
 			synchronized( run ) {
 				localRun = run.get();
 			}
 
 			if( localRun && monitorExit ) {
 
 				currentPacket = new Packet(in, server, monitor.chunkCache, monitor.arrayListByte);
 				
 				monitorExit = monitor.process(currentPacket, out);
 				
 				if( currentPacket.timeout ) {
 					System.out.println("Duration: " + timeoutDuration);
 					if( (timeoutDuration++) > 20) {
 						if(!Globals.isQuiet()) {
 							System.out.println( "Connection timed out");
 						}
 						localRun = false;
 					}
 				} else {
 					timeoutDuration = 0;
 				}
 
 				packetIdStore.add(currentPacket.packetId);
 
 				if( !currentPacket.valid && !currentPacket.eof && !currentPacket.timeout ) {
 					currentPacket.eof = true;
 					System.out.println( "Most recent packets: " + packetIdStore );
 
 				}
 
 			}
 		}
 
 
 		synchronized(running) {
 			running.set(false);
 			running.notifyAll();
 		}
 
 	}
 
 
 
 }
