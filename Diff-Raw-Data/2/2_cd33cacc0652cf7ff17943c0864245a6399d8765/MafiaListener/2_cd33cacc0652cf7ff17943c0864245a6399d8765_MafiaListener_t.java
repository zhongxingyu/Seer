 package com.googlecode.prmf.starter;
 import java.io.File;
 import java.util.Scanner;
 
 import com.googlecode.prmf.Game;
 import com.googlecode.prmf.Player;
 
 public class MafiaListener implements Listener {
 	private Game game;
 	private String botName;
 	public MafiaListener(String botName) {
 		super();
 		this.botName = botName;
 	}
 	//TODO: need to clean this massive pile of if/else up
 	public void receiveLine(String in, IOThread inputThread) 
 	{
 		String[] msg = in.split(" ",4);
 		String user = "";
 		if(msg[0].indexOf("!") > 1)
 			user = msg[0].substring(1,msg[0].indexOf("!"));
 		
		if(msg.length >=2 && game != null && (msg[1].startsWith("KICK") || msg[1].startsWith("PART") || msg[1].startsWith("QUIT") ))
 		{
 			game.receiveMessage(in);
 			return;
 		}
 		else if(msg.length >=2 && msg[1].startsWith("NICK"))
 		{
 			game.receiveMessage(in);
 			return;
 		}
 		
 		if (msg.length >= 2 && !msg[1].equals("PRIVMSG"))
 			return;
 		if (msg.length >= 4 && msg[2].equals(botName) && msg[3].startsWith(":~"))
 			game.receiveMessage(in);
 		if(msg.length >= 4)
 			msg[3] = msg[3].toLowerCase();
 		if(msg.length >= 4 && msg[3].equalsIgnoreCase(":~mafia") && (game == null || game.getPostgame() != null)) 
 		{
 			game = new Game(user, inputThread);
 			inputThread.sendMessage(inputThread.getChannel(), "Mafia game started by " + user + "!");
 			game.receiveMessage(":" + user + "! PRIVMSG " + inputThread.getChannel() + " :~join"); // TODO H4X is bad
 			
 		}
 		else if(msg.length >= 4 && msg[3].equals(":~stats") && game != null)
 		{
 			game.getState().status();
 		}
 		else if(msg.length >= 4 && msg[3].equals(":~join") && game != null) 
 		{
 			game.receiveMessage(in);
 		}
 		else if(msg.length >= 4 && msg[3].equals(":~end") && game != null) 
 		{
 			if(user.equals(game.getGameStarter()))
 			{
 				inputThread.sendMessage(inputThread.getChannel(), "Mafia game ended!");
 				game.stopTimer();
 				game = null;
 			}
 			else
 				inputThread.sendMessage(inputThread.getChannel(), "The game can only be ended by " + game.getGameStarter());
 		}
 		else if(msg.length >= 4 && msg[3].equals(":~quit") && game != null)
 		{
 			game.receiveMessage(in);
 		}
 		else if(msg.length >= 4 && msg[3].equals(":~start") && game != null)
 		{
 			if(game.getPlayerList().length < 3)
 			{
 				inputThread.sendMessage(inputThread.getChannel(), "Game should have at least 3 people!");
 			}
 			else
 			game.receiveMessage(in);
 		}
 		else if(msg.length >= 4 && msg[3].startsWith(":~lynch") && game != null)
 		{
 			game.receiveMessage(in);
 		}
 		else if(msg.length >= 4 && msg[3].startsWith(":~nolynch") && game != null)
 		{
 			game.receiveMessage(in);
 		}
 		else if(msg.length >= 4 && msg[3].startsWith(":~unvote") && game != null)
 		{
 			game.receiveMessage(in);
 		}
 		else if(msg.length >= 4 && msg[3].startsWith(":~help"))
 		{
 			inputThread.sendMessage(inputThread.getChannel(), "MafiaBot has the following commands: mafia, join, quit, start, lynch, nolynch, unvote, stats, help");
 		}
 		else if(msg.length >= 4 && msg[3].equalsIgnoreCase(":~save"))
 		{
 			inputThread.sendMessage(inputThread.getChannel(), "~save not implemented yet");
 		}
 		else if(msg.length >= 4 && msg[3].startsWith(":~load") && game != null)
 		{
 			Scanner prof = null;
 			try{
 				
 				prof = new Scanner(new File("profiles.txt"));
 				if(msg[3].equalsIgnoreCase(":~load"))
 				{
 					//int profileNum = 1;
 					StringBuilder currProfile = new StringBuilder();
 					currProfile.append("List of saved profiles: ");
 					while(prof.hasNextLine())
 					{
 						String profileLine = prof.nextLine();
 						String profMsg[] = profileLine.split(" ",3);
 						//String roleMsg[] = profMsg[2].split(",");
 						currProfile.append(" "+ profMsg[1]);
 						/**
 						for(int i=0;i<roleMsg.length;++i)
 						{
 							currProfile.append(" "+roleMsg[i].trim());
 						}
 						**/
 					}
 					inputThread.sendMessage(user, currProfile.toString());
 				}
 				else
 				{
 					while(prof.hasNextLine())
 					{
 						String profileLine = prof.nextLine();
 						String profMsg[] = profileLine.split(" ",3);
 						String roleMsg[] = profMsg[2].split(",");
 						for(int i=0;i<roleMsg.length;++i)
 							roleMsg[i] = roleMsg[i].trim();
 						String[] msgLoad = msg[3].split(" ",2);
 						String ProfDesired = msgLoad[1];
 						if(ProfDesired.equalsIgnoreCase(profMsg[1]))
 						{
 							game.getPregame().loadRoleProfile(roleMsg);
 							break;
 						}
 					}
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				inputThread.sendMessage(inputThread.getChannel(), "profiles.txt file not found");
 			}
 
 			
 
 		}
 	}
 	
 	public void timerMessage()
 	{
 		game.stopTimer();
 	}
 }
