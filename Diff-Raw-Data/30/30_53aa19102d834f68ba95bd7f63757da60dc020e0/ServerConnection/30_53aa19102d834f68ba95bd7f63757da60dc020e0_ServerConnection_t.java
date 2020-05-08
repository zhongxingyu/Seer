 package com.cpcookieman.cookieirc;
 
 import java.io.IOException;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.PircBot;
 
 public class ServerConnection extends PircBot
 {
 	public static String serverip;
 	public int serverid;
 	public int servertab;
 	
 	public ServerConnection(String server, int id, int tab)
 	{
 		serverid = id;
 		servertab = tab;
 		serverip = server;
 		setName(Main.user);
 		try
 		{
 			connect(server);
 		}
 		catch (IOException e)
 		{
 			Main.gui.tabs.get(tab).addMessage((e.getMessage()));
 		}
 		catch (IrcException e)
 		{
 			Main.gui.tabs.get(tab).addMessage((e.getMessage()));
 		}
	}
	
	@Override
	protected void onConnect()
	{
		Main.gui.tabs.get(servertab).addMessage("Connected to " + serverip + ".");
	}
	
	@Override
	protected void onDisconnect()
	{
		Main.gui.tabs.get(servertab).addMessage("Disconnected from " + serverip + ".");
 	}
 	
 	@Override
 	protected void onAction(String sender, String login, String hostname, String target, String action)
 	{
 		super.onAction(sender, login, hostname, target, action);
 		int i = 0;
 		while(true)
 		{
 			Main.debug("Action check: " + serverid + " - " + Main.gui.tabs.get(i).serverid, false); 
 			if(Main.gui.tabs.get(i).serverid == serverid && Main.gui.tabs.get(i).title.equalsIgnoreCase(target))
 			{
 				Main.gui.tabs.get(i).addMessage("<< " + sender + " " + action + " >>");
 				Main.gui.tabs.get(i).onAction();
 				i++;
 			}
 			else
 			{
 				if(i > Main.gui.tabs.size())
 				{
 					break;
 				}
 				i++;
 			}
 		}
 		
 	}
 	
 	@Override
 	protected void onPrivateMessage(String sender, String login, String hostname, String message)
 	{
 		super.onPrivateMessage(sender, login, hostname, message);
 		int targettab = 0;
 		int i = 0;
 		while(true)
 		{
 			try
 			{
 				if(Main.gui.tabs.get(i).title.equalsIgnoreCase(sender) || Main.gui.tabs.get(i).title.substring(1).equalsIgnoreCase(sender))
 				{
 					targettab = i;
 					break;
 				}
 				else
 				{
 					i++;
 				}
 			}
 			catch(Exception e)
 			{
 				break;
 			}
 		}
 		Main.gui.tabs.get(targettab).addMessage(sender, message);
 	}
 	
 	@Override
 	protected void onMessage(String channel, String sender, String login, String hostname, String message)
 	{
 		super.onMessage(channel, sender, login, hostname, message);
 		if(channel == null)
 		{
 			Main.gui.tabs.get(servertab).addMessage(sender, message);
 		}
 		else
 		{
 			int targettab = 0;
 			int i = 0;
 			while(true)
 			{
 				try
 				{
 					if(Main.gui.tabs.get(i).title.equalsIgnoreCase(channel) || Main.gui.tabs.get(i).title.substring(1).equalsIgnoreCase(channel))
 					{
 						targettab = i;
 						break;
 					}
 					else
 					{
 						i++;
 					}
 				}
 				catch(Exception e)
 				{
 					break;
 				}
 			}
 			Main.gui.tabs.get(targettab).addMessage(sender, message);
 		}
 	}
 }
