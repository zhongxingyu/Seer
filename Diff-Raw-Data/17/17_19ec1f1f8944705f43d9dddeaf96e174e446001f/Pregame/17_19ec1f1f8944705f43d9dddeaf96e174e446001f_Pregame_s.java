 package com.googlecode.prmf;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Scanner;
 
 import com.googlecode.prmf.starter.Communicator;
 import com.googlecode.prmf.starter.Connection;
 
 public class Pregame {
	String startName;
 	
	//TODO default visibility is almost as bad as public
	ArrayList<Player> players;
	ArrayList<Role> townRoles;
	ArrayList<Role> mafiaRoles;
	ArrayList<Role> roles; 
 	
 	public Pregame(String startName)
 	{
 		this.startName = startName;
 	}
 
 	//TODO change return type to ENUM for day-start, night-start
 	public void startGame()
 	{
 		Scanner in = new Scanner(Connection.is);
 		players = new ArrayList<Player>();
 		townRoles = new ArrayList<Role>();
 		mafiaRoles = new ArrayList<Role>();
 		roles = new ArrayList<Role>();
 		
 		
 		//joining the game	
 		
 		while(true) //break when the game starts
 		{
 			String line = in.nextLine();
 			String[] msg = line.split(" ", 4);
 			String user = "#UFPT";
 			if(msg[0].equals("PING"))
 			{
 				//TODO add PONG functionailty
 			}
 			//this is kind of a nasty solution...
 			else
 			{
 				if(msg[0].indexOf("!")>1)
 					user = msg[0].substring(1,msg[0].indexOf("!"));
 				
 				Player temp = new Player(user);
 				int index = players.indexOf(temp);
 				
 				String destination = msg[2];
 				String command = msg[3].toLowerCase();
				if(command.equals(":!start") && user.equals(startName))		
 					break;
 					
 				if(command.equals(":!join"))
 				{
 					System.out.println(index);
 					if(index == -1)
 					{
 						players.add(new Player(user));
 						Communicator.getInstance().sendMessage(destination, user + " has joined the game!");
 					}
 					else
 						Communicator.getInstance().sendMessage(destination, user + " has already joined the game!");
 				}
 				
 				if(command.equals(":!quit"))
 				{
 					System.out.println(index);
 					if(index == -1)
 						Communicator.getInstance().sendMessage(destination, user + " is not part of the game!");
 					else
 					{
 						players.remove(index);
 						Communicator.getInstance().sendMessage(destination, user + " has quit the game!");
 					}
 					
 				}	
 				
 				if (command.equals(":!end"))
 				{
 					if (user.equals(startName))
 					{
 						Communicator.getInstance().sendMessage(destination, user + " has ended the game. Aww :(");
 						return;
 						//does this acceptably end the game? I think so but not positive
 					}
 					else
 						Communicator.getInstance().sendMessage(destination, "Only " + startName + " can end the game!");
 				}
 			}
 		}
 		
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
 		}
 		
 		//TODO tell game that a day or night needs to start? should this method have a return type?
 		
 	}
 
 	public Player[] getPlayerList()
 	{
 		return players.toArray(new Player[0]);
 	}
 }
