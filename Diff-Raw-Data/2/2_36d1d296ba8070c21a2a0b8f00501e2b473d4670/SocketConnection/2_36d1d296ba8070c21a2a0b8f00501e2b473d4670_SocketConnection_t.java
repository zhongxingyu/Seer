 package aidancbrady.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 public class SocketConnection extends Thread
 {
 	public Socket connection;
 	public int userID;
 	
 	public SocketConnection(int id, Socket accept)
 	{
 		userID = id;
 		connection = accept;
 	}
 	
 	@Override
 	public void run()
 	{
 		try {
 			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			PrintWriter printWriter = new PrintWriter(connection.getOutputStream(), true);
 			
 			printWriter.println("Please identify yourself with a username.");
 			
 			String readerLine = "";
 			boolean doneReading = false;
 			
 			while((readerLine = bufferedReader.readLine()) != null && !doneReading)
 			{
 				CommandHandler handler = new CommandHandler(printWriter, readerLine.trim().toLowerCase());
 				
 				if(handler.getCommand().equals("msg") && getUser().hasUsername())
 				{
 					try {
 						getUser().messages.add(handler.getText());
 						printWriter.println("Received message.");
 						System.out.println("Received message from user " + userID + ": " + handler.getText());
 						continue;
 					} catch(Exception e) {
 						printWriter.println("Invalid command usage.");
 					}
 				}
 				else if(handler.getCommand().equals("msg") && !getUser().hasUsername())
 				{
 					printWriter.println("Please provide a username before you send a message.");
 					System.out.println("User " + userID + " attempted to send a message without a username.");
 					continue;
 				}
 				else if(handler.getCommand().equals("user") && !getUser().hasUsername())
 				{
 					try {
 						getUser().username = handler.getText();
 						printWriter.println("Username received. You are free to send a message.");
 						System.out.println("User " + userID + " sent username '" + handler.getText() + ".'");
 						continue;
 					} catch(Exception e) {
 						printWriter.println("Invalid command usage.");
 					}
 				}
 				else if(handler.getCommand().equals("user") && getUser().hasUsername())
 				{
 					try {
 						getUser().username = handler.getText();
 						printWriter.println("Successfully changed username.");
 						System.out.println("User " + userID + " changed his username to '" + handler.getText() + ".'");
 						continue;
 					} catch(Exception e) {
 						printWriter.println("Invalid command usage.");
 					}
 				}
 				else if(handler.getCommand().startsWith("info"))
 				{
 					System.out.println("User '" + userID + "' used info command.");
 					try {
 						int id = Integer.parseInt(handler.getText());
 						if(ServerCore.connections.get(id) != null)
 						{
 							printWriter.println("Information on user " + userID + ":");
 							printWriter.println("Username: " + (ServerCore.connections.get(id).hasUsername() ? ServerCore.connections.get(id).username : "unknown"));
 							if(!ServerCore.connections.get(id).messages.isEmpty())
 							{
 								printWriter.println("Logged messages:");
 								for(String message : ServerCore.connections.get(id).messages)
 								{
									printWriter.println(message);
 								}
 							}
 							else {
 								printWriter.println("No messages found for this user.");
 							}
 						}	
 						else {
 							printWriter.println("Unable to find database for user '" + userID + ".'");
 						}
 					} catch(Exception e) {
 						printWriter.println("Invalid command usage.");
 					}
 				}
 				else if(handler.getCommand().equals("done"))
 				{
 					printWriter.println("Received 'done' notification -- closing connection...");
 					System.out.println("User " + userID + " has ended the connection.");
 					doneReading = true;
 					break;
 				}
 				else {
 					printWriter.println("Unknown command.");
 					continue;
 				}
 			}
 			
 			ServerCore.removeConnection(userID);
 
 			printWriter.println("Successfully closed connection!");
 			System.out.println("Closing connection with user '" + userID + ".'");
 			
 			bufferedReader.close();
 			printWriter.close();
 			connection.close();
 			try {
 				finalize();
 			} catch (Throwable e) {
 				System.err.println("Unable to close connection thread! Error: " + e.getMessage());
 			}
 		} catch(IOException e) {
 			if(!e.getMessage().trim().toLowerCase().equals("socket closed"))
 			{
 				System.err.println("Error: " + e.getMessage());
 				e.printStackTrace();
 			}
 			
 			try {
 				connection.close();
 				
 				try {
 					finalize();
 				} catch (Throwable e1) {
 					System.err.println("Unable to close connection thread! Error: " + e1.getMessage());
 				}
 			} catch(IOException e1) {
 				System.err.println("Could not close connection! Error: " + e1.getMessage());
 			}
 		}
 	}
 	
 	public void kick()
 	{
 		try {
 			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			PrintWriter printWriter = new PrintWriter(connection.getOutputStream(), true);
 			
 			printWriter.println("You have been kicked!");
 			System.out.println("Kicked user '" + userID + ".'");
 			
 			ServerCore.removeConnection(userID);
 			bufferedReader.close();
 			printWriter.close();
 			connection.close();
 			
 			try {
 				finalize();
 			} catch (Throwable e) {
 				System.err.println("Unable to close connection thread! Error: " + e.getMessage());
 			}
 		} catch(IOException e) {
 			if(!e.getMessage().trim().toLowerCase().equals("socket closed"))
 			{
 				System.err.println("Error: " + e.getMessage());
 				e.printStackTrace();
 			}
 			
 			try {
 				connection.close();
 				
 				try {
 					finalize();
 				} catch (Throwable e1) {
 					System.err.println("Unable to close connection thread! Error: " + e1.getMessage());
 				}
 			} catch(IOException e1) {
 				System.err.println("Could not close connection! Error: " + e1.getMessage());
 			}
 		}
 	}
 	
 	public ServerConnection getUser()
 	{
 		return ServerCore.connections.get(userID);
 	}
 }
