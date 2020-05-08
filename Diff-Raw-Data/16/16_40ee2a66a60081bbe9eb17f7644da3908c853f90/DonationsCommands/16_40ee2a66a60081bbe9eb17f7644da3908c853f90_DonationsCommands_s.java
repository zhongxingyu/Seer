 package fghjconner.DonationMeter;
 
 
 import java.util.Calendar;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class DonationsCommands implements CommandExecutor
 {
 	private DonationMeter plugin;
 	private boolean advancedPermissions, canNotify, isPlayer;
 	public DonationsCommands(DonationMeter plugin)
 	{
 		this.plugin=plugin;
 	}
 
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		isPlayer = sender instanceof Player;
 		if (isPlayer)
 		{
 			advancedPermissions = plugin.opPermissions ? sender.isOp() : DonationMeter.permissionHandler.has((Player)sender, "DonationMeter.admin");
 			canNotify = plugin.opPermissions || DonationMeter.permissionHandler.has((Player)sender, "DonationMeter.notify");
 		}
 		else
 		{
 			advancedPermissions = true;
 			canNotify = false;
 		}
 
 		switch(args.length)
 		{
 		case 0: return info(sender, command);
 		case 1: return oneArg(sender, command, args[0]);
 		case 2: return twoArgs(sender, command, args);
 		case 3: return threeArgs(sender, command, args);
 		}
 		return false;
 	}
 
 	//basic no arg response
 	public boolean info(CommandSender sender, Command command)
 	{
 		if (DonationMeter.currentDonations<DonationMeter.requiredDonations)
 			sendMessage(sender, ChatColor.BLUE.toString()+"Donation Status: "+DonationMeter.currentDonations+" out of "+DonationMeter.requiredDonations+" "+DonationMeter.currency+String.format(" donated. (%3.1f%%)", (double)DonationMeter.currentDonations/(double)DonationMeter.requiredDonations*100d));
 		else
 			sendMessage(sender, ChatColor.GREEN.toString()+"Donation Status: Complete! All "+DonationMeter.requiredDonations+" "+DonationMeter.currency+" with "+(DonationMeter.currentDonations-DonationMeter.requiredDonations)+" "+DonationMeter.currency+" of Surplus!");
 		printTimeTill(sender);
 		return true;
 	}
 
 	//one arg responses
 	public  boolean oneArg(CommandSender sender, Command command, String arg)
 	{
 		arg = arg.toLowerCase();
 		if (arg.equals("vips") || arg.equals("vip"))
 		{
 			String out=ChatColor.BLUE.toString()+"Current "+DonationMeter.vipName+"s "+ChatColor.GREEN.toString();
 			for (String player:plugin.vips)
 				out+=player+", ";
 			sendMessage(sender, out.substring(0,out.length()-2));
 			return true;
 		}
 		if (arg.equals("help") || arg.equals("?"))
 		{
 			sendMessage(sender, ChatColor.BLUE.toString()+"----------Available Commands----------");
 			sendMessage(sender, ChatColor.BLUE.toString()+"------------------------------------");
 			sendMessage(sender, ChatColor.AQUA.toString()+"/DonationMeter   "+ChatColor.DARK_GREEN.toString()+"*Displays donation info");
 			sendMessage(sender, ChatColor.AQUA.toString()+"/Donations   "+ChatColor.DARK_GREEN.toString()+"*Alias for /DonationMeter");
 			sendMessage(sender, ChatColor.AQUA.toString()+"/DonationMeter VIPs   "+ChatColor.DARK_GREEN.toString()+"*Lists "+DonationMeter.vipName+"s");
 			sendMessage(sender, ChatColor.AQUA.toString()+"/DonationMeter notify <amount>   "+ChatColor.DARK_GREEN.toString()+"*Notifys admin of donation");
 			if (advancedPermissions)
 			{
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter addVIP <player>   "+ChatColor.DARK_GREEN.toString()+"*Adds a "+DonationMeter.vipName);
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter removeVIP <player>   "+ChatColor.DARK_GREEN.toString()+"*Removes a "+DonationMeter.vipName);
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter addDonation <amount>   "+ChatColor.DARK_GREEN.toString()+"*Adds donation to total");
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter setGoal <amount>   "+ChatColor.DARK_GREEN.toString()+"*Sets the monthly goal");
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter setCurrency <name>   "+ChatColor.DARK_GREEN.toString()+"*Sets currency used");
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter woolMeterHelp   "+ChatColor.DARK_GREEN.toString()+"*Displays wool meter help");
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter signMeterHelp   "+ChatColor.DARK_GREEN.toString()+"*Displays sign meter help");
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter pay   "+ChatColor.DARK_GREEN.toString()+"*Pays goal");
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter save   "+ChatColor.DARK_GREEN.toString()+"*Saves data");
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter time [on/off]   "+ChatColor.DARK_GREEN.toString()+"*Toggles time display");
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter notifications   "+ChatColor.DARK_GREEN.toString()+"*Displays notifications");
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter accept <player>  "+ChatColor.DARK_GREEN.toString()+"*Accepts donation notification");
 				sendMessage(sender, ChatColor.DARK_AQUA.toString()+"/DonationMeter update  "+ChatColor.DARK_GREEN.toString()+"*Updates all meters");
 			}
 			if (!DonationMeter.vipName.equals("VIP"))
 				sendMessage(sender, ChatColor.GRAY.toString()+"Note:"+DonationMeter.vipName+" may be substituted for VIP");
 			return true;
 		}
 		
 		if (arg.equals("woolmeterhelp") && advancedPermissions)
 		{
 			sendMessage(sender, ChatColor.BLUE.toString()+"-----------Creating Wool Meters-----------");
 			sendMessage(sender, ChatColor.BLUE.toString()+"-----------------------------------------");
 			sendMessage(sender, "Wool Meters are built of adjacent wool blocks");
 			sendMessage(sender, "Placing a sign starting \"Donations\" onto wool forms a meter");
 			sendMessage(sender, "Meters visually display your donation status by \"filling\"");
 			sendMessage(sender, "The meter starts the \"empty\" color");
 			sendMessage(sender, "The meter then fills with the \"has\" color");
 			sendMessage(sender, "Once full the meter fills again with the \"extra\" color");
 			sendMessage(sender, ChatColor.BLUE.toString()+"------------------------------------");
 			sendMessage(sender, ChatColor.BLUE.toString()+"------------Modifications------------");
 			sendMessage(sender, "Adding -r to the first line reverses the fill direction");
 			sendMessage(sender, "Adding -x, -y, or -z controlls the fill axis");
 			sendMessage(sender, "Placing \"need <color>\" on an empty line sets the empty color");
 			sendMessage(sender, "Placing \"has <color>\" on an empty line sets the filled color");
 			sendMessage(sender, "Placing \"extra <color>\" on an empty line sets the extra color");
 			return true;
 		}
 		
 		if (arg.equals("signmeterhelp") && advancedPermissions)
 		{
 			sendMessage(sender, ChatColor.BLUE.toString()+"-----------Creating Sign Meters-----------");
 			sendMessage(sender, ChatColor.BLUE.toString()+"-----------------------------------------");
 			sendMessage(sender, "Sign Meters are created by placing text on a sign");
 			sendMessage(sender, "[have] is replaced by the current donation value");
 			sendMessage(sender, "[need] is replaced by the remaining donations required");
 			sendMessage(sender, "[extr] is replaced by the donations beyond the goal");
 			sendMessage(sender, "[goal] is replaced by the goal");
 			sendMessage(sender, "[perc] is replaced by the percent of the goal");
 			return true;
 		}
 		
 		if (arg.equals("pay") && advancedPermissions)
 		{
 			DonationMeter.currentDonations -= DonationMeter.requiredDonations;
 			if (DonationMeter.currentDonations<0)
 			{
 				DonationMeter.currentDonations=0;
 				sendMessage(sender, ChatColor.RED.toString()+"You're a bit short! Current total set to zero :'(");
 			}
 			else if (DonationMeter.currentDonations == 0)
 			{
 				sendMessage(sender, ChatColor.BLUE.toString()+"You have exactly enough money! (You didn't do that on purpose did you?)");
 			}
 			else
 			{
 				sendMessage(sender, ChatColor.GREEN.toString()+"You have more than enough money! Theres "+DonationMeter.currentDonations+" "+DonationMeter.currency+" left!");
 			}
 			plugin.updateMeters();
 			return true;
 		}
 		if (arg.equals("save") && advancedPermissions)
 		{
 			plugin.saveAll();
 			sendMessage(sender, ChatColor.BLUE.toString()+"Data Saved!");
 			return true;
 		}
 		if ((arg.equals("showtime") || arg.equals("time")) && advancedPermissions)
 		{
 			String active = plugin.showTime ? ChatColor.RED.toString()+"Off" : ChatColor.GREEN.toString()+"On";
 			sendMessage(sender, "Time till bill toggled. ("+active+")");
 			plugin.showTime = !plugin.showTime;
 			return true;
 		}
 		if (arg.equals("notifications") || arg.equals("notification") || arg.equals("notify"))
 		{
 			if (advancedPermissions)
 			{
 				if (plugin.notificationList.size()==0)
 				{
 					sendMessage(sender, ChatColor.BLUE.toString()+"No notifications!");
 					return true;
 				}
 				sendMessage(sender, ChatColor.GREEN.toString()+"-----Donation Notifications-----");
 				sendMessage(sender, ChatColor.GREEN.toString()+"----------------------------");
 				for (String player: plugin.notificationList.keySet())
 				{
 					sendMessage(sender, player+" claims a donation of "+plugin.notificationList.get(player)+" "+DonationMeter.currency);
 				}
 				return true;
 			}
 			else
 			{
 				sendMessage(sender, ChatColor.RED.toString() + "Insert a donation amount!");
 			}
 		}
 		if (arg.equals("update") && advancedPermissions)
 		{
 			plugin.updateMeters();
 		}
 		return false;
 	}
 
 	//two arg responses
 	public  boolean twoArgs(CommandSender sender, Command command, String[] args)
 	{
 		args[0]=args[0].toLowerCase();
 		if ((args[0].equals("vips") || args[0].equals("vip") || args[0].equals("addvip") || args[0].equals(DonationMeter.vipName)) && advancedPermissions)
 		{
 			if (!plugin.vips.contains(args[1]))
 			{
 				plugin.vips.add(args[1]);
 				sendMessage(sender, ChatColor.GREEN.toString()+args[1]+" added to "+DonationMeter.vipName+" list.");
 			}
 			else
 			{
 				sendMessage(sender, ChatColor.BLUE.toString()+args[1]+" is already on the "+DonationMeter.vipName+" list!");
 			}
 			return true;
 		}
 		if ((args[0].equals("removevip") || args[0].equals("remvip") || args[0].equals("viprem") || args[0].equals("remove"+DonationMeter.vipName) || args[0].equals("remvip"+DonationMeter.vipName) || args[0].equals("viprem"+DonationMeter.vipName)) && advancedPermissions)
 		{
 			if (plugin.vips.remove(args[1]))
 			{
 				sendMessage(sender, ChatColor.BLUE.toString()+args[1]+" removed from "+DonationMeter.vipName+" list.");
 				return true;
 			}
 			sendMessage(sender, ChatColor.RED.toString()+DonationMeter.vipName+" not found. Check your spelling and capitalization!");
 			return false;
 		}
 		if ((args[0].equals("adddonation") || args[0].equals("donation") || args[0].equals("donate") || args[0].equals("add")) && advancedPermissions)
 		{
 			try {
 				Short donation = Short.parseShort(args[1]);
 				DonationMeter.currentDonations+=donation;
 				if (donation<0)
 					sendMessage(sender, ChatColor.BLUE.toString()+"Negative donations? That sucks... "+DonationMeter.currentDonations+" "+DonationMeter.currency+" remaining.");
 				else
 					sendMessage(sender, ChatColor.GREEN.toString()+"Donation added. You are at "+DonationMeter.currentDonations+" "+DonationMeter.currency+".");
 				plugin.updateMeters();
 				return true;
 			} catch (NumberFormatException e)
 			{
 				sendMessage(sender, ChatColor.RED.toString()+"Invalid number");
 				return false;
 			}
 		}
 		if ((args[0].equals("setgoal") || args[0].equals("goal") || args[0].equals("setbill") || args[0].equals("bill")) && advancedPermissions)
 		{
 			short amount;
 			try {
 				amount = Short.parseShort(args[1]);
 			} catch (NumberFormatException e)
 			{
 				sendMessage(sender, ChatColor.RED.toString()+"Invalid number");
 				return false;
 			}
 			if (amount>=0)
 			{
 				DonationMeter.requiredDonations=amount;
 				sendMessage(sender, ChatColor.BLUE.toString()+"Goal set to "+amount);
 				plugin.updateMeters();
 				return true;
 			}
 			else
 			{
 				sendMessage(sender, ChatColor.RED.toString()+"Negative goals indicate self esteem issues.");
 				sendMessage(sender, ChatColor.RED.toString()+"Goal not set");
 				return false;
 			}
 		}
 		if ((args[0].equals("currency") || args[0].equals("setcurrency")) && advancedPermissions)
 		{
 			DonationMeter.currency=args[1];
 			sendMessage(sender, ChatColor.BLUE.toString()+"Now tracking donations in "+args[1]+"!");
 			return true;
 		}
 		if ((args[0].equals("setvipname") || args[0].equals("vipname")) && advancedPermissions)
 		{
 			sendMessage(sender, ChatColor.BLUE.toString()+DonationMeter.vipName+"s are now called "+args[1]+"s!");
 			DonationMeter.vipName=args[1];
 			return true;
 		}
 		if ((args[0].equals("showtime") || args[0].equals("time")) && advancedPermissions)
 		{
 			args[1] = args[1].toLowerCase();
 			if (args[1].equals("on") || args[1].equals("true") || args[1].equals("show"))
 			{
 				sendMessage(sender, "Time till bill is now "+ChatColor.GREEN.toString()+"On.");
 				plugin.showTime = true;
 				return true;
 			}
 			if (args[1].equals("off") || args[1].equals("false") || args[1].equals("hide"))
 			{
 				sendMessage(sender, "Time till bill is now "+ChatColor.RED.toString()+"Off.");
 				plugin.showTime = false;
 				return true;
 			}
 		}
 		if ((args[0].equals("accept") || args[0].equals("acceptnotification")) && advancedPermissions)
 		{
 			if (plugin.notificationList.containsKey(args[1]))
 			{
 				DonationMeter.currentDonations+=plugin.notificationList.get(args[1]);
 				if (!plugin.vips.contains(args[1]))
 					plugin.vips.add(args[1]);
 				sendMessage(sender, ChatColor.GREEN.toString()+"Donation accepted! "+plugin.notificationList.get(args[1])+" "+DonationMeter.currency+" added to total, "+args[1]+" is now a "+DonationMeter.vipName+"!");
 				plugin.notificationList.remove(args[1]);
 				plugin.updateMeters();
 				return true;
 			}
 			sendMessage(sender, "No notifications from that player!");
 		}
 		if (args[0].equals("notify") && canNotify)
 		{
 			if (isPlayer)
 			{
 				try
 				{
 					plugin.addNotification(Short.parseShort(args[1]), ((Player)sender).getName());
 					sendMessage(sender, ChatColor.GREEN.toString()+"Notification of donation sent!");
 					for(Player player: plugin.getServer().getOnlinePlayers())
 					{
 						if (plugin.opPermissions ? sender.isOp() : DonationMeter.permissionHandler.has((Player)sender, "DonationMeter.admin"))
 							player.sendMessage(player.getName() + " has claimed a donation of "+plugin.notificationList.get(player.getName())+" "+DonationMeter.currency);
 					}
 					return true;
 				}
 				catch (NumberFormatException e)
 				{
 					sendMessage(sender, ChatColor.RED.toString()+"Invalid number");
 				}
 			}
 		}
 		return false;
 	}
 
 	//three arg responses
 	public  boolean threeArgs(CommandSender sender, Command command, String[] args)
 	{
 		return false;
 	}
 
 	private void sendMessage(CommandSender sender, String message)
 	{
 		if (isPlayer)
 			sender.sendMessage(message);
 		else
 			sender.sendMessage(ChatColor.stripColor(message));
 	}
 
 	public void printTimeTill(CommandSender sender)
 	{
 		if (!plugin.showTime)
 			return;
 		Calendar date = Calendar.getInstance();
 		int daysTill = getMonthLength(date)-date.get(Calendar.DAY_OF_MONTH);
 		int hoursTill = 24-date.get(Calendar.HOUR_OF_DAY);
 		int minutesTill = 60-date.get(Calendar.MINUTE);
 		int secondsTill = 60-date.get(Calendar.SECOND);
 		if (secondsTill==0)
 			minutesTill++;
 		if (minutesTill==0)
 			hoursTill++;
 		if (hoursTill==0)
 			daysTill++;
 		sendMessage(sender, ChatColor.YELLOW.toString()+daysTill+" Days, "+hoursTill+" Hours, "+minutesTill+" Minutes, and "+secondsTill+" Seconds till server bill.");
 	}
 
 	public int getMonthLength(Calendar date)
 	{
 		switch (date.get(Calendar.MONTH))
 		{
 		case 0:;
 		case 2:;
 		case 4:;
 		case 6:;
 		case 7:;
 		case 9:;
 		case 11: return 31;
 		case 1: return 28;
 		case 3:;
 		case 5:;
 		case 8:;
 		case 10: return 30;
 		}
 		return 0;
 	}
 }
