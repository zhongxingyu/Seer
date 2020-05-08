 package com.shreyaschand.MEDIC.Server;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 public class Client {
 
 	public static void main(String[] args) throws Exception {
 
 		Socket sock = new Socket("chands.dyndns-server.com", 1028);
 		PrintWriter writer = new PrintWriter(sock.getOutputStream(), true);
 		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
 		
 		String in = reader.readLine();
 		while(in != null){
 			writer.println(in);
 			if(in.equals(".exit"))
 				break;
 			in = reader.readLine();
 		}
 		
 		writer.close();
 		reader.close();
 		sock.close();
 		
 
 	}
 
 }
