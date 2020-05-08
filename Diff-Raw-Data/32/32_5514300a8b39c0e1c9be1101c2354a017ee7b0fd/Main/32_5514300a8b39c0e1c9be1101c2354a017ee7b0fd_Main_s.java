 package com.omfgp.sockets;
 
 import java.io.PrintStream;
 import java.util.Scanner;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Scanner in = new Scanner(System.in);
 		PrintStream out = System.out;
 		UserChat chat = new UserChat();
 		SocketServer server;
 		SocketClient client;
 		
		String name, msg;
 		String cs = "";
 		
 		out.println("Please enter the following information...");
 		
 		out.print("username: ");
 		name = in.nextLine();
 		
 		while(!cs.equalsIgnoreCase("c") && !cs.equalsIgnoreCase("s")) {
 			out.print("client or server [c/s]: ");
 			cs = in.nextLine();
 			
 			if(cs.equalsIgnoreCase("c")) {
				out.println("Client connecting...");
 			} else if(cs.equalsIgnoreCase("s")) {
 				out.println("Server waiting for connections...");
 			} else {
 				out.println("Please enter either 'c' or 's'");
 			}
 		}
 		
 		while(true) {
 			msg = in.nextLine();
 			//wait for messages here.
 		}
 
 	}
 
 }
