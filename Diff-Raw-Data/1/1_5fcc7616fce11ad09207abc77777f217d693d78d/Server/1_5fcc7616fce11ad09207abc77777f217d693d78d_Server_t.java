 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 	
 public class Server
 {
 	static private ArrayList<UserInfo> userList;	
 	static private ArrayList<GameHandler> games;
 
 	static private int userID = 0; 
 	static private Protocol prtcl;	
 
 	public static void main(String[] args) 
 	{
 		ServerSocket server;
 
 
 		userList = new ArrayList<UserInfo>();
 		
 		prtcl = new Protocol();
 		try
 		{
 		    if(args.length == 1)	
 		    {
 		
 		        server = new ServerSocket(Integer.parseInt(args[0]));
 		        waitConnect(server);
 		    } 
 		    else
 		    {
 		        System.out.println("Usage: Specify port");
 		    }
 		}
 		catch (Exception exception)
 		{
 		System.out.println("There was an issue with creating the socket. Abandon ship!");
 		    System.exit(1);
 		}
 	}
 /*	waitConnect
 
 	Like its name, it waits for a connection, then accepts the socket!
 */
 	public static void waitConnect(ServerSocket server) 
 	{
 		Protocol prtcl = new Protocol();
 		Socket connectionSocket;
 		String input;
 		int length;
 		char[] buf;
 		String packet;
 		BufferedReader in;
 		PrintWriter out; 
 		UserInfo curr;
 	
 		// Loop infinitely (keep on waiting for connections)
 		try
 		{
     			while(true)
     			{
     				// Set up our communication streams for this connection.
     				connectionSocket = server.accept(); 
     				in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
     				out = new PrintWriter(connectionSocket.getOutputStream(), true); 
     				
     				// Welcome them; ask for a username. 
     				packet = prtcl.makePacket("Hi there. Please log in with the following syntax: login [username].", "LOGIN");
     				
     				out.println(packet);
     				
     				input = in.readLine();
     				length = prtcl.getLength(input);
     				buf = new char[length];
     				
     				in.read(buf,0,length);
     				input = new String(buf);
    	 
     				System.out.println(input + " has entered the system.");
    	 
     				// This loops if the name is already in the list of active names - not sure if this follows the project
     				while(!uniqueName(input))
     				{
     					packet = prtcl.makePacket("Name taken, please try another one.", "ERROR");
     					out.println(packet); 
     					input = in.readLine();
     				}
 
     		
     				// Generate the "UserInfo" object for the current user.
     				curr = new UserInfo(input, connectionSocket);
     				userList.add(userID++, curr);
     	
 				// The user has been added to the system, and all is good. Time to give them their Very Own Thread™
 
 				listenTo(curr);
     			}
 		}
 		catch (Exception exception)
 		{
 		    System.out.println("Something wacky happened here...");
 		    System.exit(1);
 		}
 	}
 	
 	public static boolean uniqueName(String name)
 	{
 		return true;
 	}
 
 
 
 	public static void listenTo(UserInfo curr)
 	{
 		boolean byeReceived = false;
 		boolean valid_cmd = true;
 		
 		char[] buffer;
 		UserInfo opponent = null;
 		
 		String packet_header = "";
 		String packet_data = "";
 		String packet_type = "";
 		
 		String server_reply = "";
 		
 		while(!byeReceived)	
 		{	
			buffer = new char[prtcl.getLength(packet_header)];
 			// The first line read is the header information. 
 			curr.read(buffer, prtcl.getLength(packet_header));
 	
 			packet_header = curr.getMessage();	
 			packet_data = new String(buffer);
 			packet_type = prtcl.getCommand(packet_header);
 			valid_cmd = true;
 		
 			if(packet_type.equalsIgnoreCase("LOGIN"))
 			{
 				valid_cmd = false;
 				server_reply = "You've already logged in as '" + curr.getName() + "'";
 			} else if(packet_type.equalsIgnoreCase("BYE"))	
 			{
 				server_reply = prtcl.makePacket("", "BYE");
 				byeReceived = true;
 			} else if (packet_type.equalsIgnoreCase("REMOVE"))
 			{
 		
 				if(curr.inGame())
 				{
 					if(!(games.get(curr.getGame())).tryRemove(curr, packet_data))
 					{
 						server_reply = prtcl.makePacket("It is not your turn!", "ERROR");
 					}
 				} else
 				{
 					server_reply = "ERROR: You're not currently in a game."; 
 					valid_cmd = false;
 				} 
 			} else if (packet_type.equalsIgnoreCase("WHO"))
 			{
 					server_reply = prtcl.makePacket(listUsers(), "WHO");
 			} else if (packet_type.equalsIgnoreCase("GAMES"))	
 			{
 				server_reply = (prtcl.makePacket(listGames(), "GAMES"));
 			} else if (packet_type.equalsIgnoreCase("OBSERVE"))
 			{	
 					// Not implemented yet.
 			} else if (packet_type.equalsIgnoreCase("UNOBSERVE"))
 			{		
 					// Not implemented yet.
 			} else if (packet_type.equalsIgnoreCase("PLAY"))
 			{ 
 				opponent = findUser(packet_data);
 				if(opponent == null)
 					valid_cmd = false;
 				else
 				{
 					if(curr.inGame())
 					{
 						valid_cmd = false;
 						server_reply = "ERROR: You're already in a game. Please leave first.";
 					} else if(opponent.inGame())
 					{
 						server_reply = prtcl.makePacket("The opponent is already in a game.", "ERROR");
 					} else if (!addGame(curr, opponent))
 					{
 						server_reply = prtcl.makePacket("This shouldn't happen", "ERROR");
 					}
 				}
 			}
 			
 			if(!valid_cmd)
 			{
 				server_reply += helpMessage();
 				server_reply = prtcl.makePacket(server_reply, "ERROR");
 			} 
 
 			
 			curr.sendMessage(server_reply);							
 						
 					
 			
 			
 		} // while
 
 		// If we're here, the user has said BYE, and we've echoed the sentiment. Time to delete them and move on... :-(
 		userList.remove(curr);
 		curr.delete();	
 	}
 		
 	private static UserInfo findUser(String userName)
 	{
 		UserInfo ret = null;
 		UserInfo curr = null;
 		for(Iterator i = userList.iterator(); ret==null;)
 		{
 			curr = (UserInfo)i.next();
 			if(curr.getName().equalsIgnoreCase(userName))
 			{
 				ret = curr;
 			}
 		}
 
 		return ret;
 	}
 
         private static String helpMessage()
 	{
 		String message;
 		message = "Command Help:\n" +
 		"login [username]\n Logs in the player 'username'\n\n" +
 		"games\n Displays all ongoing games.\n\n" +
 		"who\n Displays all players who are currently available for a game.\n\n" +
 		"play [username]\n Sends a request to play a game with 'username'\n\n" +
 		"observe [gameID]\n Lets you watch game 'gameID' from the list 'games'\n\n" +
 		"unobserve [gameID]\n Lets you stop watching game 'gameID' that you are already observing.\n\n" +
 		"remove [number] [set]\n Removes 'number' objects from set 'set'\n\n" +
 		"bye\n Quit the game.\n\n";
 
 
 		return message;
 	}
 	
 	//lists all users in the system
 	public static String listUsers()
 	{
 		String users = "";//the list of all users
 
 		//goes through the list of users and adds their name and status
 		for(int i = 0; i < userList.size(); i++)
 		{
 			users += userList.get(i).toString() + " ";
 		}
 		
 		return users;
 	}
 	
 	//lists all games currently in progress
 	public static String listGames()
 	{
 		String gameStr = "";//the list of all games
 		
 		//prints each game index and the players associated with it
 		for(int i = 0; i < games.size(); i++)
 		{
 			gameStr += "Game " + i + ": ";
 			
 			//finds all users associated with game i
 			for(int j = 0; j < userList.size(); j++)
 			{
 				if(userList.get(j).getGame() == i)
 					gameStr += userList.get(j).toString() + " ";
 			}
 			
 			gameStr += "\n";
 		}
 		
 		return gameStr;
 	}
 	
 	//creates a new game between user a and user b
 	public static boolean addGame(UserInfo a, UserInfo b)
 	{//add to a game, set statuses to inGame, return false if either is already in a game
 		boolean success = false;//indicates if the game was successfully created
 		GameHandler temp = null;//the game created between the users
 		
 		//attempts to create the game
 		if(a.getStatus() == 0 && b.getStatus() == 0)
 		{//creates a new game if both users are available
 			temp = new GameHandler(a, b);
 			
 			//saves the game index associated with each user, and sets statuses to in-game
 			a.setGame(games.size(), 1);
 			b.setGame(games.size(), 1);
 			
 			//saves the new game
 			games.add(temp);
 	
 			success = true;
 		}
 	
 		return success;
 	}
 
 
 }
