 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.gamezgalaxy.GGS.chat;
 
 import java.util.ArrayList;
 
 import com.gamezgalaxy.GGS.iomodel.Player;
 import com.gamezgalaxy.GGS.server.Server;
 
 public class Messages {
 	protected Server server;
 	
 	public Messages(Server server) { this.server = server; }
 	
 	/**
 	 * Send a message to all players on the server regardless of world
 	 * 
 	 * @param message
 	 */
 	public void serverBroadcast(String message)
 	{
 		for (Player p : server.players)
 			p.sendMessage(message);
 	}
 	
 	/**
 	 * Send a message to all players in the specified world
 	 * 
 	 * @param message
 	 * @param world
 	 */
 	public void worldBroadcast(String message, String world)
 	{
 		for (Player p : server.players)
 		{
 			if(p.world == world)
 				p.sendMessage(message);
 		}
 	}
 	
 	/**
 	 * Send a message to a specified username
 	 * 
 	 * @param message
 	 * @param playerName
 	 */
 	public void sendMessage(String message, String playerName)
 	{
 		for (Player p : server.players)
 			if(p.username == playerName)
 				p.sendMessage(message);
 	}
 	
 	public String[] split(String message) {
 		char[] array = message.toCharArray();
 		String toadd = "";
 		ArrayList<String> temp = new ArrayList<String>();
 		for (int i = 0; i < array.length; i++) {
 			toadd += "" + array[i];
			if (i % 64 == 0 && i != 0) {
 				temp.add(toadd);
 				toadd = "";
 			}
 		}
 		if (!toadd.equals(""))
 			temp.add(toadd);
 		return temp.toArray(new String[temp.size()]);
 	}
 }
