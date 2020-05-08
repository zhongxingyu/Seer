 package de.dakror.liturfaliarcest.settings;
 
 import java.util.ArrayList;
 
 import de.dakror.liturfaliarcest.game.Game;
 
 /**
  * @author Dakror
  */
 public class FlagManager
 {
 	public static ArrayList<String> flags = new ArrayList<>();
 	
 	public static void toggleFlag(String name)
 	{
 		if (flags.contains(name.toUpperCase()))
 		{
 			flags.remove(name.toUpperCase());
 			Game.world.dispatchFlagChange(name, false);
 		}
 		else
 		{
 			flags.add(name.toUpperCase());
 			Game.world.dispatchFlagChange(name, true);
 		}
 	}
 	
 	public static void setFlag(String name)
 	{
 		if (!flags.contains(name))
 		{
 			flags.add(name.toUpperCase());
			Game.world.dispatchFlagChange(name, true);
 		}
 	}
 	
 	public static void removeFlag(String name)
 	{
 		flags.remove(name.toUpperCase());
 		Game.world.dispatchFlagChange(name, false);
 	}
 	
 	public static void addFlag(String name)
 	{
 		flags.add(name.toUpperCase());
 	}
 	
 	public static boolean isFlag(String name)
 	{
 		return flags.contains(name.toUpperCase());
 	}
 	
 	public static boolean matchesFlags(String text)
 	{
 		text = text.trim();
 		String[] flags = text.split(" ");
 		for (String flag : flags)
 		{
 			if (flag.contains("|"))
 			{
 				String[] fl = flag.split("\\|");
 				boolean match = false;
 				for (String f : fl)
 				{
 					if ((f.startsWith("!") && !isFlag(f.substring(1))) || (!f.startsWith("!") && isFlag(f)))
 					{
 						match = true;
 						break;
 					}
 				}
 				
 				if (!match) return false;
 			}
 			else if ((flag.startsWith("!") && isFlag(flag.substring(1))) || (!flag.startsWith("!") && !isFlag(flag))) return false;
 		}
 		
 		return true;
 	}
 	
 	public static int countFlags(String name)
 	{
 		int i = 0;
 		for (String n : flags)
 			if (n.toUpperCase().equals(name.toUpperCase())) i++;
 		
 		return i;
 	}
 }
