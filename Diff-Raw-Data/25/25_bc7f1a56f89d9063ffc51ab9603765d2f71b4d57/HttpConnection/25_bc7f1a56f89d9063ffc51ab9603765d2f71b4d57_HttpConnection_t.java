 package net.ropelato.meetpoint.server;
 
 import net.ropelato.meetpoint.data.ChatMessage;
 import net.ropelato.meetpoint.data.Game;
 
 import java.io.*;
 import java.net.Socket;
 
 public class HttpConnection implements Runnable
 {
 	Thread thread = null;
 	Socket socket = null;
 
 	Game game = null;
 	InputStream inStream = null;
 	OutputStream outStream = null;
 
 	public HttpConnection(Socket socket, Game game)
 	{
 		this.socket = socket;
 		this.game = game;
 		thread = new Thread(this);
 		thread.start();
 	}
 
 	public void run()
 	{
 		//System.out.println("request from " + socket.getLocalAddress().getHostAddress());
 
 		try
 		{
 			inStream = socket.getInputStream();
 			outStream = socket.getOutputStream();
 
 			BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
 			String requestLine = reader.readLine();
 
 			String method = requestLine.split(" ")[0];
 			String resource = requestLine.split(" ")[1];
 			String protocol = requestLine.split(" ")[2];
 
 			PrintStream printStream = new PrintStream(outStream, true, "ASCII");
 
 			if(method.equalsIgnoreCase("GET"))
 			{
 				try
 				{
 					if(resource.contains("register"))
 					{
 						sendClientId(printStream, resource);
 					}
 					else if(resource.contains("position"))
 					{
 						updateClientPosition(resource);
 						sendClientPosition(printStream, resource);
 					}
 					else if(resource.contains("chat"))
 					{
 						newChatMessage(printStream, resource);
 					}
 					else
 					{
 						sendBadRequest(printStream, "Unknown request '" + resource + "'.");
 					}
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 					sendBadRequest(printStream, e.toString());
 				}
 			}
 			else
 			{
 				sendBadRequest(printStream, "Only GET method supported.");
 			}
 
 			printStream.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			try
 			{
 				inStream.close();
 				outStream.close();
 			}
 			catch(IOException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void sendClientId(PrintStream printStream, String resource) throws Exception
 	{
 		String[] information = (resource.split("[?]")[1]).split("[&]");
 		int characterId = Integer.parseInt(information[0]);
 
 		String clientId = game.createClient(characterId);
 
 		if(clientId == null)
 		{
 			throw new Exception("Maximum number of players reached.");
 		}
 
 		//header
 		printStream.print("HTTP/1.1 200 OK");
 		printStream.print("\r\n");
 		printStream.print("Content-Type: plain/text");
 		printStream.print("\r\n");
 		printStream.print("Access-Control-Allow-Origin: *");
 		printStream.print("\r\n");
 		printStream.print("\r\n");
 
 		// body
 		printStream.print(clientId);
 	}
 
 	private void updateClientPosition(String resource) throws Exception
 	{
 		String[] information = (resource.split("[?]")[1]).split("[&]");
 
 		String clientId = information[0];
 		double xPosition = Double.parseDouble(information[1]);
 		double zPosition = Double.parseDouble(information[2]);
 		double angle = Double.parseDouble(information[3]);
 
 		if(game.hasClient(clientId))
 		{
 			game.getClient(clientId).setXPosition(xPosition);
 			game.getClient(clientId).setZPosition(zPosition);
 			game.getClient(clientId).setAngle(angle);
 			game.getClient(clientId).setLastAccess(System.currentTimeMillis());
			game.getClient(clientId).setIsPlaying(true);
 		}
 		else
 		{
 			throw new Exception("client id does not exist.");
 		}
 	}
 
 	public void newChatMessage(PrintStream printStream, String resource) throws Exception
 	{
 		String[] information = (resource.split("[?]")[1]).split("[&]");
 
 		String sender = information[0];
 		String message = information[1];
 
 		if(game.hasClient(sender))
 		{
 			game.addNewChatMessage(new ChatMessage(sender, message));
 
 			//header
 			printStream.print("HTTP/1.1 200 OK");
 			printStream.print("\r\n");
 			printStream.print("Content-Type: plain/text");
 			printStream.print("\r\n");
 			printStream.print("Access-Control-Allow-Origin: *");
 			printStream.print("\r\n");
 			printStream.print("\r\n");
 			game.getClient(sender).setLastAccess(System.currentTimeMillis());
 		}
 		else
 		{
 			throw new Exception("client id does not exist.");
 		}
 
 		// (empty) body
 	}
 
 	private void sendClientPosition(PrintStream printStream, String resource) throws Exception
 	{
 		String[] information = (resource.split("[?]")[1]).split("[&]");
 		String clientId = information[0];
 
 		//header
 		printStream.print("HTTP/1.1 200 OK");
 		printStream.print("\r\n");
 		printStream.print("Content-Type: plain/text");
 		printStream.print("\r\n");
 		printStream.print("Access-Control-Allow-Origin: *");
 		printStream.print("\r\n");
 		printStream.print("\r\n");
 
 		// body
 		printStream.print(game.getInformationFromClientsAlive(clientId));
 	}
 
 	private void sendBadRequest(PrintStream printStream, String message)
 	{
 		try
 		{
 			//header
 			printStream.print("HTTP/1.1 400 Bad Request");
 			printStream.print("\r\n");
 			printStream.print("Content-Type: plain/text");
 			printStream.print("\r\n");
 			printStream.print("Access-Control-Allow-Origin: *");
 			printStream.print("\r\n");
 			printStream.print("\r\n");
 
 			// body
 			printStream.print(message);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
 
