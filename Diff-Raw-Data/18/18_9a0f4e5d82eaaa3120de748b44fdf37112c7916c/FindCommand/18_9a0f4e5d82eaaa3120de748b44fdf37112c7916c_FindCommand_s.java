 package au.com.mineauz.PlayerSpy.commands.playback;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import au.com.mineauz.PlayerSpy.PlaybackContext;
 import au.com.mineauz.PlayerSpy.Records.RecordType;
 import au.com.mineauz.PlayerSpy.Utilities.Match;
 import au.com.mineauz.PlayerSpy.Utilities.Util;
 import au.com.mineauz.PlayerSpy.Utilities.Utility;
 
 public class FindCommand extends Command 
 {
 
 	@Override
 	public boolean onCommand(Player sender, PlaybackContext playback, String[] args) 
 	{
 		if(args.length < 2)
 		{
 			return false; // No command takes 1 argument
 		}
 		
 		if(args[0].compareToIgnoreCase("gained") == 0 || args[0].compareToIgnoreCase("lost") == 0)
 		{
 			boolean gained = (args[0].compareToIgnoreCase("gained") == 0);
 			// Find the item specified in arg1
 			//Material mat = Util.parseItem(args[1]);
 			ItemStack item = Utility.matchName(args[1]);
 			if(item == null)
 			{
 				sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a valid item type");
 				return false;
 			}
 			Material mat = item.getType();
 			
 			if(args.length >= 3)
 			{
 				boolean before = false;
 				if(args[2].compareToIgnoreCase("after") == 0)
 				{
 					before = false;
 				}
 				else if(args[2].compareToIgnoreCase("before") == 0)
 				{
 					before = true;
 				}
 				else
 				{
 					sender.sendMessage( ChatColor.RED + " can only specify 'before' or 'after' date");
 					return false;
 				}
 				
 				long date;
 				if(args.length >= 4)
 				{
 					String dateString = args[3];
 					for(int i = 4; i < args.length; i++)
 						dateString += " " + args[i];
 					
 					
 					Match m = Util.parseDate(dateString, playback.getPlaybackDate(), playback.getStartDate(), playback.getEndDate());
 					if(m == null)
 						date = 0;
 					else
 						date = (Long)m.value;
 				}
 				else
 					date = playback.getPlaybackDate();
 				
 				if(date == 0)
 				{
 					sender.sendMessage(ChatColor.RED + " invalid date format");
 					return false;
 				}
 				
 				if(!playback.seekToItem(mat,gained,date,before))
 				{
 					if(before)
 					{
 						sender.sendMessage( ChatColor.RED + " there are no records for mining block " + mat.toString() + " before " + ChatColor.GREEN + Util.dateToString(date));
 					}
 					else
 					{
 						sender.sendMessage( ChatColor.RED + " there are no records for mining block " + mat.toString() + " after " + ChatColor.GREEN + Util.dateToString(date));
 					}
 					return false;
 				}
 			}
 			else
 			{
 				if(!playback.seekToItem(mat, gained, 0, false))
 				{
 					sender.sendMessage( ChatColor.RED + " there are no records for mining block " + mat.toString());
 					return false;
 				}
 			}
 		}
 		else if(args[0].compareToIgnoreCase("mined") == 0 || args[0].compareToIgnoreCase("placed") == 0)
 		{
 			boolean mined = (args[0].compareToIgnoreCase("mined") == 0);
 			// Find the block specified in arg1
 			ItemStack item = Utility.matchName(args[1]);
 			if(item == null)
 			{
 				sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a valid block type");
 				return false;
 			}
 			Material mat = item.getType();
 			if(!mat.isBlock())
 			{
 				sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a valid block type");
 				return false;
 			}
 			
 			if(args.length >= 3)
 			{
 				boolean before = false;
 				if(args[2].compareToIgnoreCase("after") == 0)
 				{
 					before = false;
 				}
 				else if(args[2].compareToIgnoreCase("before") == 0)
 				{
 					before = true;
 				}
 				else
 				{
 					sender.sendMessage( ChatColor.RED + " can only specify 'before' or 'after' date");
 					return false;
 				}
 				
 				long date;
 				if(args.length >= 4)
 				{
 					String dateString = args[3];
 					for(int i = 4; i < args.length; i++)
 						dateString += " " + args[i];
 					
 					
 					Match m = Util.parseDate(dateString, playback.getPlaybackDate(), playback.getStartDate(), playback.getEndDate());
 					if(m == null)
 						date = 0;
 					else
 						date = (Long)m.value;
 				}
 				else
 					date = playback.getPlaybackDate();
 				
 				if(date == 0)
 				{
 					sender.sendMessage( ChatColor.RED + " invalid date format");
 					return false;
 				}
 				
 				if(!playback.seekToBlock(mat,mined,date,before))
 				{
 					if(before)
 					{
 						sender.sendMessage( ChatColor.RED + " there are no records for mining block " + mat.toString() + " before " + ChatColor.GREEN + Util.dateToString(date));
 					}
 					else
 					{
 						sender.sendMessage( ChatColor.RED + " there are no records for mining block " + mat.toString() + " after " + ChatColor.GREEN + Util.dateToString(date));
 					}
 					return false;
 				}
 			}
 			else
 			{
 				if(!playback.seekToBlock(mat, mined, 0, false))
 				{
 					sender.sendMessage( ChatColor.RED + " there are no records for mining block " + mat.toString());
 					return false;
 				}
 			}
 		}
 		else if(args[0].compareToIgnoreCase("attack") == 0 || args[0].compareToIgnoreCase("damage") == 0)
 		{
 			boolean attack = (args[0].compareToIgnoreCase("attack") == 0);
 			EntityType type = Util.parseEntity(args[1]);
 			if(type == null)
 			{
 				sender.sendMessage( ChatColor.RED + "'" + args[1] + "' is not a valid entity type");
 				return false;
 			}
 			
 			String playerName = null;
 			int argStart = 2;
 			
 			if(type == EntityType.PLAYER && args.length >= 3)
 			{
 				// Check if it actually is a player name
 				if(!(args[2].compareToIgnoreCase("after") == 0 || args[2].compareToIgnoreCase("before") == 0))
 				{
 					playerName = args[2];
 					argStart = 3;
 				}
 			}
 			
 			String failMessage = ChatColor.RED + "There are no records for attacking ";
 			
 			if(type == EntityType.PLAYER)
 			{
 				if(playerName != null)
 					failMessage += playerName;
 				else
 					failMessage += "a player";
 			}
 			else
 			{
 				String startLetter = type.getName().substring(0,1).toLowerCase();
 				
 				if(startLetter.equals("a") || startLetter.equals("e") || startLetter.equals("i") || startLetter.equals("o") || startLetter.equals("u"))
 					failMessage += "an";
 				else
 					failMessage += "a";
 				
 				failMessage += " " + type.getName();
 			}
 			
 			// Check for dates
 			if(args.length >= argStart + 1)
 			{
 				boolean before = false;
 				if(args[argStart].compareToIgnoreCase("after") == 0)
 				{
 					before = false;
 				}
 				else if(args[argStart].compareToIgnoreCase("before") == 0)
 				{
 					before = true;
 				}
 				else
 				{
 					sender.sendMessage( ChatColor.RED + " can only specify 'before' or 'after' date");
 					return false;
 				}
 				
 				argStart++;
 				long date;
 				if(args.length >= argStart+1)
 				{
 					String dateString = args[argStart];
 					for(int i = argStart+1; i < args.length; i++)
 						dateString += " " + args[i];
 					
 					Match m = Util.parseDate(dateString, playback.getPlaybackDate(), playback.getStartDate(), playback.getEndDate());
 					if(m == null)
 						date = 0;
 					else
 						date = (Long)m.value;
 				}
 				else
 					date = playback.getPlaybackDate();
 				
 				if(date == 0)
 				{
 					sender.sendMessage( ChatColor.RED + " invalid date format");
 					return false;
 				}
 				
 				if(!playback.seekToDamage(type, attack, playerName,date,before))
 				{
 					if(before)
 					{
 						sender.sendMessage( failMessage + " before " + ChatColor.GREEN + Util.dateToString(date));
 					}
 					else
 					{
 						sender.sendMessage( failMessage + " after " + ChatColor.GREEN + Util.dateToString(date));
 					}
 					return false;
 				}
 			}
 			else
 			{
				if(!playback.seekToDamage(type, attack, playerName, 0, false));
 				{
 					sender.sendMessage( failMessage);
 					return false;
 				}
 			}
 			return true;
 		}
 		else if(args[0].compareToIgnoreCase("event") == 0)
 		{
 			RecordType searchType;
 			// Find the occurance of an event
 			if(args[1].compareToIgnoreCase("login") == 0 || args[1].compareToIgnoreCase("join") == 0)
 			{
 				searchType = RecordType.Login;
 			}
 			else if(args[1].compareToIgnoreCase("logoff") == 0 || args[1].compareToIgnoreCase("quit") == 0)
 			{
 				searchType = RecordType.Logoff;
 			}
 			else if(args[1].compareToIgnoreCase("die") == 0 || args[1].compareToIgnoreCase("death") == 0)
 			{
 				searchType = RecordType.Death;
 			}
 			else if(args[1].compareToIgnoreCase("changeworld") == 0)
 			{
 				searchType = RecordType.WorldChange;
 			}
 			else
 			{
 				sender.sendMessage( ChatColor.RED + " invald event to search for.");
 				return false;
 			}
 			
 			if(args.length >= 3)
 			{
 				boolean before = false;
 				if(args[2].compareToIgnoreCase("after") == 0)
 				{
 					before = false;
 				}
 				else if(args[2].compareToIgnoreCase("before") == 0)
 				{
 					before = true;
 				}
 				else
 				{
 					sender.sendMessage( ChatColor.RED + " can only specify 'before' or 'after' date");
 					return false;
 				}
 				
 				long date;
 				
 				if(args.length >= 4)
 				{
 					String dateString = args[3];
 					for(int i = 4; i < args.length; i++)
 						dateString += " " + args[i];
 					
 					Match m = Util.parseDate(dateString, playback.getPlaybackDate(), playback.getStartDate(), playback.getEndDate());
 					if(m == null)
 						date = 0;
 					else
 						date = (Long)m.value;
 				}
 				else
 					date = playback.getPlaybackDate();
 				if(date == 0)
 				{
 					sender.sendMessage( ChatColor.RED + " invalid date format");
 					return false;
 				}
 				
 				if(!playback.seekToEvent(searchType,date,before))
 				{
 					if(before)
 						sender.sendMessage( ChatColor.RED + " there are no records for that event before " + ChatColor.GREEN + Util.dateToString(date));
 					else
 						sender.sendMessage( ChatColor.RED + " there are no records for that event after " + ChatColor.GREEN + Util.dateToString(date));
 
 					return false;
 				}
 			}
 			else
 			{
 				if(!playback.seekToEvent(searchType, 0, false))
 				{
 					sender.sendMessage( ChatColor.RED + " there are no records for that event");
 					return false;
 				}
 			}
 		}
 		
 		return true;
 	}
 
 	@Override
 	public String getUsage() 
 	{
 		return "find <type> <args>";
 	}
 
 	@Override
 	public String getDescription() 
 	{
 		return "Seeks to an event specified. Valid types are 'attack', 'mined', 'item', 'event'.\n" +
 				"Usage:\n" +
 				"find item\n" +
 				"find mined <blockType> [<before|after> [date]]\n" +
 				"find attack <entityType> [<before|after> [date]]\n" +
 				"find attack player [playerName] [<before|after> [date]]\n" +
 				"find event <eventType> [<before|after> [date]]\n\n" +
 				"Valid eventTypes are: login,logoff,die,death,changeworld";
 	}
 
 }
