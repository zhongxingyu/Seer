 //Client.java
 //COS325 Project 4: Battleship Game
 //Spring '11
 //C. Bikle
 
 package battleship;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public class Client
 {
 //----------CLASS VARIABLES----------
 	private int port;
 	
 	private String username;
 	private String remoteHostName;
 	private String localHostName;
 	
 	private Socket clientSocket;
 	
 	private ClientGUI cgui;
 	
 	private BufferedReader inFromUser;
 	private DataOutputStream outToServer;
 	private BufferedReader inFromServer;
 	
 //----------CONSTRUCTOR----------
 	public Client(String[] args)
 	{
 		this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
 		
 		try
 		{
 			if(!setRemoteHostName(args[0]));
 			   throw new Exception();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			
 			infoPrompt(4);
 		}
 		
 		try
 		{
 			if(!setPortNumber(args[1]));
 				throw new Exception();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			
 			infoPrompt(5);
 		}
 		
 		try
 		{
 			if(!setUserName(args[2]))
 			   throw new Exception();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			
 			infoPrompt(6);
 		}
 		
 		try
 		{
 			this.remoteHostName = args[0];
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			System.exit(4);
 		}
 		
 		try
 		{
 			this.localHostName = InetAddress.getLocalHost().getHostName();
 		}
 		catch(UnknownHostException e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 		
 		this.clientSocket = createSocket(this.remoteHostName, this.port);
 		
 		try
 		{
 			this.outToServer = new DataOutputStream(clientSocket.getOutputStream());
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 			System.exit(2);
 		}
 		
 		try
 		{
 			this.inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 			System.exit(3);
 		}
 		
 		System.out.println("ready");
 	}
 
 //----------CREATE SOCKET----------
 	private Socket createSocket(String remoteHostName, int port)
 	{
 		Socket sock = null;
 		
 		try
 		{
 			sock = new Socket(remoteHostName, port);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 		return sock;
 	}
 	
 //----------PARSE ARGS----------
 	private static String[] parseArgs(ArrayList<String> rawArgs)
 	{
 		String[] processedArgs = new String[3];
 		
 		if(rawArgs.isEmpty())
 			return null;
 		
 		if(rawArgs.contains("-h"))
 			processedArgs[0] = rawArgs.get((rawArgs.indexOf("-u")+1));
 		
 		if(rawArgs.contains("-p"))
 			processedArgs[1] = rawArgs.get((rawArgs.indexOf("-p")+1));
 		
 		if(rawArgs.contains("-u"))
 			processedArgs[3] = rawArgs.get((rawArgs.indexOf("-u")+1));
 		
 		return processedArgs;
 	}
 	
 //----------TO ARRAY LIST----------
 	private static ArrayList<String> toArrayList(String[] arhs)
 	{
 		ArrayList<String> als = new ArrayList<String>();
 		
 		for(int i = 0; i < arhs.length; i++)
 		{
 			als.add(arhs[0]);
 		}
 		
 		return als;
 	}
 	
 //----------INFO PROMPT----------
 	private void infoPrompt(int code)
 	{
 		while(code == 4)//hostname
 		{
 			System.out.print("Please enter a valid hostname: ");
 			
 			try
 			{
 				if(setRemoteHostName(inFromUser.readLine()))
 					return;
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 			
 		while(code == 5)//port
 		{
 			System.out.print("Please enter a valid port number: ");
 			
 			try
 			{
 				if(setPortNumber(inFromUser.readLine()))
 					return;
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 			
 		while(code == 6)//username
 		{
 			System.out.print("Please enter a valid username: ");
 			
 			
 			try
 			{
 				if(setUserName(inFromUser.readLine()))
 					return;
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 //----------SET HOST NAME----------
 	private boolean setRemoteHostName(String s)
 	{
 		try
 		{
 			this.remoteHostName = s;
 		}
 		catch(Exception e)
 		{
 			return false;
 		}
 		
 		return true;
 	}
 	
 //----------SET PORT NUMBER----------
 	private boolean setPortNumber(String s)
 	{
 		try
 		{
 			this.port = Integer.parseInt(s);
 		}
 		catch(Exception e)
 		{
 			return false;
 		}
 		
 		return true;
 	}
 	
 //----------SET USER NAME----------
 	private boolean setUserName(String s)
 	{
 		try
 		{
 			this.username = s;
 		}
 		catch(Exception e)
 		{
 			return false;
 		}
 		
 		return true;
 	}
 	
 //----------MAIN----------
 	public static void main(String[] args) throws IOException
 	{
 		String[] parsedArgs = parseArgs(toArrayList(args));
 		
 		Client player = new Client(parsedArgs);
		
		
 /*		if ( args.length > 0 )
 		{
 //			int portNum = 0;
 			
 			try
 			{
 				portNum = Integer.parseInt(args[0]);
 			}
 			catch(NumberFormatException e)
 			{
 				e.printStackTrace();
 			}
 		}*/
 		
 /*		portNum = Integer.parseInt(args[1]);
 		remoteHostName = args[0];
 		
 		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
 		
 		clientSocket = new Socket(remoteHostName, portNum);
 		
 		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
 		
 		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 		
 		System.out.println("ready");
 		
 		modSentence = inFromServer.readLine();
 		
 		System.out.println("FROM SERVER: " + modSentence);
 		
 		while(true)
 		{
 			sentence = inFromUser.readLine();
 			
 			if(sentence.equals("quit"))
 			{
 				break;
 			}
 			
 			outToServer.writeBytes(sentence+"\n");
 			modSentence = inFromServer.readLine();
 			
 			System.out.println("FROM SERVER: " + modSentence);
 		}
 		
 		clientSocket.close();*/
 	}
 }
