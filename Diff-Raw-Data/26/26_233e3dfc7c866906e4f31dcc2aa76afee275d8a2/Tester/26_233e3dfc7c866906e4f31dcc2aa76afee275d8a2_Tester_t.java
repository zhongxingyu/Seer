 package de.mrpixeldream.beta.textsender.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Scanner;
 
 public class Tester
 {
 	public static void main(String[] args)
 	{
 		System.out.println("Verbindung mit localhost...");
 		try
 		{
 			Socket sock = new Socket("localhost", 22558);
 			PrintWriter writer = new PrintWriter(sock.getOutputStream());
 			
 			System.out.print("Bitte Usernamen eingeben: ");
 			Scanner console = new Scanner(System.in);
 			String user = console.nextLine();
 			
 			System.out.println("Schreibe Login Kommando...");
 			writer.println("LOGIN " + user);
 			writer.flush();
 			Scanner reader = new Scanner(sock.getInputStream());
 			System.out.println(reader.nextLine());
 			
 			System.out.println("Logge aus...");
 			writer.println("LOGOUT");
 			writer.flush();
 			
			System.out.println("Beliebige Taste drcken...");
			console.nextLine();
			
			console.close();
 			reader.close();
 			writer.close();
			sock.close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
