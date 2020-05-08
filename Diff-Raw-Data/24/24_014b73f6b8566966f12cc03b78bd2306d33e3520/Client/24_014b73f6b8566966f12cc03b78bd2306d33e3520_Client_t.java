 package client;
 
 import java.security.*;
import java.io.*;
import java.net.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
 
 public class Client {
 
 	public static void main(String[] args) {
 		while (true) {
			Socket client = null;
			PrintWriter out = null;
			try {

				client = new Socket("Address to server", 6789);
				out = new PrintWriter(client.getOutputStream(), true);
				if (client.isConnected() != true) {
					return;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
 			System.out.println("Running Client ...");
 		}
 
 	}
 
 }
