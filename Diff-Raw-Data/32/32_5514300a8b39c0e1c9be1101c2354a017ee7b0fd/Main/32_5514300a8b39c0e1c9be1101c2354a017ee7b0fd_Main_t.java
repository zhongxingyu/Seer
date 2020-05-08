 package com.omfgp.sockets;
 
 import java.io.PrintStream;
 import java.util.Scanner;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		//Make an input and output
 		Scanner in = new Scanner(System.in);
 		PrintStream out = System.out;
		
		//Build what will be our chat 'interface'
 		UserChat chat = new UserChat();
		
		//Ready some variables for our client and server
 		SocketServer server;
 		SocketClient client;
 		
		//Store our useful input and output
		String name, address, msg;
 		String cs = "";
 		
		//Dialog with the user
 		out.println("Please enter the following information...");
 		
 		out.print("username: ");
 		name = in.nextLine();
		chat.setUserName(name);
 		
		//Let's make sure we don't get any goofy input
 		while(!cs.equalsIgnoreCase("c") && !cs.equalsIgnoreCase("s")) {
 			out.print("client or server [c/s]: ");
 			cs = in.nextLine();
 			
 			if(cs.equalsIgnoreCase("c")) {
				out.println("Connect to: ");
				address = in.nextLine();
				out.println("Client connecting to " + address);
 			} else if(cs.equalsIgnoreCase("s")) {
 				out.println("Server waiting for connections...");
 			} else {
 				out.println("Please enter either 'c' or 's'");
 			}
 		}
 		
		//Send and receive messages
 		while(true) {
 			msg = in.nextLine();
 			//wait for messages here.
 		}
 
 	}
 
 }
