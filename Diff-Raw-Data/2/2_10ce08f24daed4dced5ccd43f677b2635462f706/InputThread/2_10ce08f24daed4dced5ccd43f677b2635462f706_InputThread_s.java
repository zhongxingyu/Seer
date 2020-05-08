 package com.googlecode.prmf.starter;
 
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 public class InputThread extends Thread
 {
 	private Socket soc;
 	private InputStream inputstream;
 	private List<Listener> list;
 	private PrintStream printstream;
 	public InputThread(String server, int port)
 	{
 		try {
 			soc = new Socket(server, port);
 			inputstream = soc.getInputStream();
 			printstream = new PrintStream(soc.getOutputStream());
 		} catch (Exception e) {
 			System.out.println("oops");
 		} 
 		list = new ArrayList<Listener>();
 		
 		printstream.println("NICK MAFFAIBOT22");
 		printstream.println("USER MAFFAIBOT22 12 * MAFFAIBOT22");
 		printstream.println("JOIN #UFPT");
 	}
 	
 	public void run()
 	{
 		Scanner in = new Scanner(inputstream);
 		while(in.hasNextLine())
 		{
 			String line = in.nextLine();
 			System.out.println(in.nextLine());
 			for(Listener l : list)
 				l.receiveLine(line, this);
 		}
 	}
 	
 	public void addListener(Listener listener)
 	{
 		list.add(listener);
 	}
 	
 	public void sendMessage(String destination, String message) 
 	{
		printstream.println("PRIVMSG "+destination+" :"+message);
 	}
 	
 }
