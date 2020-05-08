 package com.googlecode.prmf.starter;
 
 import com.googlecode.prmf.Game;
 
 public class MafiaListener implements Listener {
 	String channel = "#UFPT"; //TODO why is the channel listed in so many places? and why is it uppercase?
 	private Game game;
 	public void receiveLine(String in, InputThread inputThread) 
 	{
 		String[] msg = in.split(" ",4);
 		String user = channel;
 		
 		if(msg[0].indexOf("!") > 1)
 			user = msg[0].substring(1,msg[0].indexOf("!"));
 
		if(msg.length >= 4 && msg[3].equalsIgnoreCase(":!mafia")) 
 		{
			inputThread.sendMessage(channel, "Mafia game started!");
			if(game == null )
				game = new Game(user, inputThread);
 		}
 		else if(msg.length >= 4 && msg[3].equalsIgnoreCase(":!join") && game != null) 
 		{
 			game.receiveMessage(in);
 		}
 		else if(msg.length >= 4 && msg[3].equalsIgnoreCase(":!end") && game != null) 
 		{
 			game = null;
 		}
 		else if(msg.length >= 4 && msg[3].equalsIgnoreCase(":!quit") && game != null)
 		{
 			game.receiveMessage(in);
 		}
 		else if(msg.length >= 4 && msg[3].equalsIgnoreCase(":!start") && game != null)
 		{
 			game.receiveMessage(in);
 		}
 	}
 }
