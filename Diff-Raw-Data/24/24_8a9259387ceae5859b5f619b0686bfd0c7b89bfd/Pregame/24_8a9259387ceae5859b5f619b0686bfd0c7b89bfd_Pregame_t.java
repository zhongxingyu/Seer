 package com.googlecode.prmf;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.googlecode.prmf.starter.InputThread;
 
 public class Pregame {
 	
 	private InputThread inputThread;
 	private String startName;	
 	private List<Player> players;
 	private List<Role> townRoles;
 	private List<Role> mafiaRoles;
 	private List<Role> roles; 
 	
 	public Pregame(String startName, InputThread inputThread)
 	{
 		this.startName = startName;
 		players = new ArrayList<Player>();
 		townRoles = new ArrayList<Role>();
 		mafiaRoles = new ArrayList<Role>();
 		roles = new ArrayList<Role>();
 		this.inputThread = inputThread;
 	}
 
 	//TODO change return type to ENUM for day-start, night-start
 	public void receiveMessage(String line)
 	{
 			String[] msg = line.split(" ", 4);
 			String user = "#UFPT";
 
 			//this is kind of a nasty solution...
 
 			if(msg[0].indexOf("!")>1)
 				user = msg[0].substring(1,msg[0].indexOf("!"));
 			
 			Player temp = new Player(user);
 			int index = players.indexOf(temp);
 			
 			String destination = msg[2];
 			String command = msg[3].toLowerCase();
 			if(command.equalsIgnoreCase(":!start"))
 			{
 				if(user.equals(startName))		
 				{
 					inputThread.sendMessage(destination, "The game has begun!");
					startGame();
 				}
 				else
 					inputThread.sendMessage(destination,  "Only " + startName + " can start the game!");
 			}	
 			if(command.equalsIgnoreCase(":!join"))
 			{
 				System.out.println(index);
 				if(index == -1)
 				{
 					players.add(new Player(user));
 					inputThread.sendMessage(destination,  user + " has joined the game!");
 				}
 				else
 					inputThread.sendMessage(destination,  user + " has already joined the game!");
 			}
 			
 			if(command.equalsIgnoreCase(":!quit"))
 			{
 				System.out.println(index);
 				if(index == -1)
 					inputThread.sendMessage(destination, user + " is not part of the game!");
 				else
 				{
 					players.remove(index);
 					inputThread.sendMessage(destination,  user + " has quit the game!");
 				}
 				
 			}	
 			
 			if (command.equalsIgnoreCase(":!end"))
 			{
 				if (user.equals(startName))
 				{
 					inputThread.sendMessage(destination,  user + " has ended the game. Aww :(");
 					return;
 					//does this acceptably end the game? I think so but not positive
 				}
 				else
 					inputThread.sendMessage(destination,  "Only " + startName + " can end the game!");
 			}
	}	
 	
	private void startGame()
	{
 		//assigning roles
 		int numMafia = players.size()/3;
 		//create the Mafia team
 		for(int a = 0; a < numMafia; ++a)
 		{
 			mafiaRoles.add(new Mafia(new MafiaTeam()));
 		}
 		
 		//create the Town team
 		for(int a = 0; a < (players.size() - numMafia); ++a)
 		{
 			townRoles.add(new Citizen(new Town()));
 		}
 		Collections.shuffle(mafiaRoles);
 		Collections.shuffle(townRoles);
 		for(int a = 0; a < numMafia; ++a)
 		{
 			roles.add(mafiaRoles.get(a));
 		}
 		for(int a = 0; a < players.size() - numMafia; ++a)
 		{
 			roles.add(townRoles.get(a));
 		}
 		for(int a = 0; a < players.size(); ++a)
 		{
 			players.get(a).role = roles.get(a);
 			inputThread.sendMessage(players.get(a).name, "your role is " + roles.get(a).name);
 		}
 		
 		//TODO tell game that a day or night needs to start? should this method have a return type?
 		
 	}
 
 	public Player[] getPlayerList()
 	{
 		return players.toArray(new Player[0]);
 	}
 }
