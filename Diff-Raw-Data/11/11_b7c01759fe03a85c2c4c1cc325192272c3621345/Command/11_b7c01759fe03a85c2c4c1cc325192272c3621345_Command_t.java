 package com.textadventure.game;
 
 public class Command {
 
 	public static void recieve()
 	{
 		String string = Listener.fetch().toLowerCase();
		if (string.equals("help"))
 		{
 			ScriptCommand.readPass(0);
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	private static String firstWord(String string)
 	{
 		for(int x=0;x<string.length();x++)
 		{
 			if (x==string.indexOf(' ')&&x!=0)
 			{
 				return string.substring(0,x);
 			}
 		}
 		return null;
 	}
 
 	@SuppressWarnings("unused")
 	private static String secondaryWords(String string)
 	{
 				return string.substring(string.indexOf(' '));
 	}
 }
