 package com.lghs.stutor;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Scanner;
 
 import android.app.Activity;
 import android.widget.EditText;
 
 
 public class ServerClient extends Activity implements Runnable{
 	EditText chatLog;
 	Socket socket;
 	ServerSocket server;
 	int connectionCount;
 										
 	
 	public ServerClient()
 	{											
 	}
 	
 	@Override
 	public void run()											//	(IMPLEMENTED FROM THE RUNNABLE INTERFACE)
 	{
 		
 		try {
 			chatLog = (EditText)findViewById(R.id.session_edittext_chatlog); // give variables values
 			final int PORT = 6677;
 			server = new ServerSocket(PORT);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		chatLog.append("\n Waiting for Connections..."); // begin looping to search for clients
 		connectionCount=0;
 		
 		while(connectionCount <2)
 		{
 			try {
 				socket = server.accept();
 				connectionCount ++;
 				chatLog.append("\n" + connectionCount + "people connected");
 				chatLog.append("\n Client connected from " + socket.getLocalAddress().getHostName());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}														// once both are connected begin chat section
 
 		try														//	HAVE TO HAVE THIS FOR THE in AND out VARIABLES
 		{
 			Scanner in = new Scanner(socket.getInputStream());	//	GET THE SOCKETS INPUT STREAM (THE STREAM THAT YOU WILL GET WHAT THEY TYPE FROM)
 			PrintWriter out = new PrintWriter(socket.getOutputStream());//	GET THE SOCKETS OUTPUT STREAM (THE STREAM YOU WILL SEND INFORMATION TO THEM FROM)
 			
 			while (true)										//	WHILE THE PROGRAM IS RUNNING
 			{		
 				if (in.hasNext())
 				{
 					String input = in.nextLine();				//	IF THERE IS INPUT THEN MAKE A NEW VARIABLE input AND READ WHAT THEY TYPED
 					chatLog.append("\n Client Said: " + input);//	PRINT IT OUT TO THE SCREEN
 					out.println("You Said: " + input);			//	RESEND IT TO THE CLIENT
 					out.flush();								//	FLUSH THE STREAM
 				}
 			}
 		} 
 		catch (Exception e)
 		{
 			e.printStackTrace();								//	MOST LIKELY THERE WONT BE AN ERROR BUT ITS GOOD TO CATCH IT
 		}	
 	}
 
 }
