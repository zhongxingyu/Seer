 package com.communication;
 /*
 	Engine Server
 */
 
 import java.io.*;
 
 public class Engine_Server {
 	boolean VERBOSE = false;
 	
     static int POS = 3;		/* number of items per position packet */
     static int SEL = 1;		/* number of items per selection packet */
     static int COM = 20;	/* number of items per command packet */
     
 	private float position[];
 	private int selection[];
 	private String command;
 
 	Server mylink;
 
     public Engine_Server() throws IOException {
     	int port = 5010;
     	int dataport = -1;
 
     	System.out.println("Server, listening on port " + port + ", datagram port " + dataport);
     	mylink = new Server(port, dataport);
 
     	position = new float[POS];
     	selection = new int[SEL];
 
     	System.out.println("Server, waiting for connection...");
     	mylink.Connect();
     }
 
     public void recvData() throws IOException {
     	if (VERBOSE) {
     		System.out.println("Server, receiving floats\n");
     	}
 		mylink.RecvFloats(getPosition(), POS);
 
 		if (VERBOSE) {
 			System.out.println("Server, receiving ints\n");
 		}
 		mylink.RecvInts(getSelection(), SEL);
 
 		if (VERBOSE) {
 			System.out.println("Server, receiving string\n");
 		}
 		command = mylink.RecvString('\n');
     }
 
     public void endServer() throws IOException {
 		System.out.println("Server, closing connection...");
 		mylink.Close();	
 
 		System.out.println("Server, done...");
     }
 
 	public float[] getPosition() {
 		return position;
 	}
 
 	public int[] getSelection() {
 		return selection;
 	}
 	
 	public String getCommand() {
 		return command;
 	}
 }
