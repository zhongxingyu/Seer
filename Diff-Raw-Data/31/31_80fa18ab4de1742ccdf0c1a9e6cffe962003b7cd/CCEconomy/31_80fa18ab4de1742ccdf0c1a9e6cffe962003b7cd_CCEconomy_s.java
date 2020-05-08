 package com.github.CorporateCraft.cceconomy;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CCEconomy extends JavaPlugin
 {
 	@Override
     public void onEnable()
 	{	
 		getLogger().info("CCEconomy has been enabled, you now have an advanced economy system.");
 		getServer().getPluginManager().registerEvents(new LoginListener(), this);
 		InitiateFiles();
     }
  
 	void InitiateFiles()
 	{
 		File f = new File("plugins/CCEconomy/moneytracker.txt");
 		File d = new File("plugins/CCEconomy");
 		if(!d.exists())
 		{
 			boolean success = d.mkdir();
 			if (!success)
 			{
 				getLogger().info(("something bad happend"));
 			}
 		}
 		if(!f.exists())
 		{
 			try {
 				f.createNewFile();
 			} catch (IOException e) {
 				getLogger().info(("Something bad happend"));
 			}
 		}
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if(cmd.getName().equalsIgnoreCase("bal"))
 		{			
 			if (sender instanceof Player)
 			{
 	           Player player = (Player) sender;
 	           if (args.length > 1)
 	           {
 	        	   return false;
 	           }
 	           if (args.length == 1)
 	           {
 	        	   if(player.hasPermission("CCEconomy.balothers"))
 		           {
 	        		   String playersname;
 	        		   try
 	        		   {
 	        			   Player target = sender.getServer().getPlayer(args[0]);
 	        			   playersname = target.getName();
 	        		   }
 	        		   catch (Exception e)
 	        		   {
 	        			   playersname = args[0];
 	        		   }
 	        		   String balance = Bal(playersname);
 		        	   if(balance == null)
 		        	   {
 		        		   player.sendMessage("That player is not in my records if player is ofline please type in full name.");
 		        		   return true;
 		        	   }
 		        	   player.sendMessage(playersname + "'s balance is: $" + balance + ".");
 		        	   return true;
 		           }
 	           }
 	           if(player.hasPermission("CCEconomy.bal"))
 	           {
 	        	   String balance = Bal(player.getName());
 	        	   if(balance == null)
 	        	   {
 	        		   player.sendMessage("You do not seem to exist let me add you now.");
 	        		   AddPlayerToList(player.getName());
 	        		   return true;
 	        	   }
 	        	   player.sendMessage("Your balance is: $" + balance + ".");
 	        	   return true;
 	           }
 	        } 
 			else
 			{
 				if (args.length == 1)
 		           {
 						String playersname;
 						try
 						{
 							Player target = sender.getServer().getPlayer(args[0]);
 							playersname = target.getName();
 						}
 						catch (Exception e)
 						{
 							playersname = args[0];
 						}
 						String balance = Bal(playersname);
 						if(balance == null)
 						{
 							sender.sendMessage("That player is not in my records if player is ofline please type in full name.");
 							return true;
 						}
 						sender.sendMessage(playersname + "'s balance is: $" + balance + ".");
 						return true;
 		           }
 				else
 				{
 					sender.sendMessage("Log in to use this command");
 				}
 				return false;
 		    }
 		}
 		if(cmd.getName().equalsIgnoreCase("baltop"))
 		{			
 			if (sender instanceof Player)
 			{
 	           Player player = (Player) sender;
 	           if(player.hasPermission("CCEconomy.baltop"))
 		       {
 		           	int page = 0;
 		           	if (args.length == 1)
 		           	{
 		        	   	page = Integer.parseInt(args[0]);
 		           	}
 		           	if (args.length == 0)
 		           	{
 		           		page = 1;
 		           	}
 	        	   	int time = 0;
 	        	   	String bal;
 	        	   	int totalpages = BaltopPages();
 	        	   	if (page>totalpages)
 	        	   	{
	        	   		player.sendMessage("Input a number from 1 to " + Integer.toString(totalpages));
 	        	   	}
 	        	   	player.sendMessage(ChatColor.GOLD + "Balanace Top Page [" + Integer.toString(page) + "/" + Integer.toString(totalpages) + "]");
 	        	   	page = page - 1;
 	        	   	bal = Baltop(page, time);
 	        	   	while(bal != null)
 	        	   	{
 	        	   		bal = Integer.toString(time + 1) + ". " + bal.split(" ")[0] + " has: $" + bal.split(" ")[1];
 	        	   		player.sendMessage(bal);
 	        	   		time++;
 	        	   		bal = Baltop(page, time);
 	        	   	}
 	        	   	return true;
 		       }
 	        } 
 			else
 			{
 				int page = 0;
 	           	if (args.length == 1)
 	           	{
 	        	   	page = Integer.parseInt(args[0]);
 	           	}
 	           	if (args.length == 0)
 	           	{
 	           		page = 1;
 	           	}
         	   	int time = 0;
         	   	String bal;
         	   	int totalpages = BaltopPages();
         	   	if (page>totalpages)
         	   	{
        	   		sender.sendMessage("Input a number from 1 to " + Integer.toString(totalpages));
         	   	}
         	   	sender.sendMessage(ChatColor.GOLD + "Balanace Top Page [" + Integer.toString(page) + "/" + Integer.toString(totalpages) + "]");
         	   	page = page - 1;
         	   	bal = Baltop(page, time);
         	   	while(bal != null)
         	   	{
         	   		bal = Integer.toString(time + 1) + ". " + bal.split(" ")[0] + " has: $" + bal.split(" ")[1];
         	   		sender.sendMessage(bal);
         	   		time++;
         	   		bal = Baltop(page, time);
         	   	}
         	   	return true;
 		    }
 		}
 		if(cmd.getName().equalsIgnoreCase("pay"))
 		{			
 			if (sender instanceof Player)
 			{
 	           Player player = (Player) sender;
 	           if (args.length > 2)
 	           {
 	        	   return false;
 	           }
 	           if(player.hasPermission("CCEconomy.pay"))
 	           {
 	        	   String targetsname;
 	        	   try
 					{
 						Player target = sender.getServer().getPlayer(args[0]);
 						targetsname = target.getName();
 					}
 					catch (Exception e)
 					{
 						targetsname = args[0];
 					}
 					if(!DoesPlayerExist(targetsname))
 					{
 						player.sendMessage("Please enter a valid player to send money to.");
 						return true;
 					}
 					String balance = Bal(player.getName());
 					double intbal = Double.parseDouble(balance);
 					double payamount = Math.abs(Double.parseDouble(args[1]));
 					if (intbal < payamount)
 					{
 						player.sendMessage("You dont have: $" + args[1]);
 						return true;
 					}
 					payamount = roundTwoDecimals(payamount);
 					RemoveMoney(player.getName(), payamount);
 					AddMoney(targetsname, payamount);
 					player.sendMessage("Your payed " + targetsname + " $" + Double.toString(payamount) + ".");
 					return true;
 	           }
 	        } 
 			else
 			{
 				sender.sendMessage("Log in to use this command or use cce");
 				return true;
 		    }
 		}
 		if(cmd.getName().equalsIgnoreCase("cce"))
 		{			
 			if (sender instanceof Player)
 			{
 				Player player = (Player) sender;
 				if (args.length > 3)
 				{
 					return false;
 				}
 				if(player.hasPermission("CCEconomy.editbal"))
 				{
 					String targetsname;
 					try
 					{
 						Player target = sender.getServer().getPlayer(args[1]);
 						targetsname = target.getName();
 					}
 					catch (Exception e)
 					{
 						targetsname = args[1];
 					}
 					if(!DoesPlayerExist(targetsname))
 					{
 						player.sendMessage("Please enter a valid player to change the balance of.");
 						return true;
 					}
 					if (args[0].equalsIgnoreCase("reset"))
 					{
 						SetMoney(targetsname, 0);
 						player.sendMessage("Your successfully reset the balance of " + targetsname + ".");
 						return true;
 					}
 					if (args.length == 3)
 					{
 						double amount = Double.parseDouble(args[2]);
 						String balance = Bal(targetsname);
 						double intbal = Double.parseDouble(balance);
 						amount = roundTwoDecimals(amount);
 						if (args[0].equalsIgnoreCase("give"))
 						{
 							AddMoney(targetsname, amount);
 							player.sendMessage("Your successfully gave "+ " $" + Double.toString(amount) + " to "  + targetsname + ".");
 							return true;
 						}
 						if (args[0].equalsIgnoreCase("take"))
 						{
 							if(intbal-amount>=0)
 							{
 								RemoveMoney(targetsname, amount);
 								player.sendMessage("Your successfully took "+ " $" + Double.toString(amount) + " from "  + targetsname + ".");
 								return true;
 							}
 						}
 						if (args[0].equalsIgnoreCase("set"))
 						{
 							SetMoney(targetsname, amount);
 							player.sendMessage("Your successfully set the balance of " + targetsname + " to $" + Double.toString(amount) + ".");
 							return true;
 						}
 						return false;
 					}
 				}
 			} 	
 			else
 			{
 				if (args.length > 3)
 		        {
 					return false;
 		        }
 				String targetsname;
 				try
 				{
 					Player target = sender.getServer().getPlayer(args[1]);
 					targetsname = target.getName();
 				}
 				catch (Exception e)
 				{
 					targetsname = args[1];
 				}
 				if(!DoesPlayerExist(targetsname))
 				{
 					sender.sendMessage("Please enter a valid player to change the balance of.");
 					return true;
 				}
 				if (args[0].equalsIgnoreCase("reset"))
 				{
 					SetMoney(targetsname, 0);
 					sender.sendMessage("Your successfully reset the balance of " + targetsname + ".");
 					return true;
 				}
 				if (args.length == 3)
 				{
 					double amount = Double.parseDouble(args[2]);
 					String balance = Bal(targetsname);
 					double intbal = Double.parseDouble(balance);
 					amount = roundTwoDecimals(amount);
 					if (args[0].equalsIgnoreCase("give"))
 					{
 						AddMoney(targetsname, amount);
 						sender.sendMessage("Your successfully gave "+ " $" + Double.toString(amount) + " to "  + targetsname + ".");
 						return true;
 					}
 					if (args[0].equalsIgnoreCase("take"))
 					{
 						if(intbal-amount>=0)
 						{
 							RemoveMoney(targetsname, amount);
 							sender.sendMessage("Your successfully took "+ " $" + Double.toString(amount) + " from "  + targetsname + ".");
 							return true;
 						}
 					}
 					if (args[0].equalsIgnoreCase("set"))
 					{
 						SetMoney(targetsname, amount);
 						sender.sendMessage("Your successfully set the balance of " + targetsname + " to $" + Double.toString(amount) + ".");
 						return true;
 					}
 					return false;
 				}
 			}
 		}
 		return false; 
 	}
 	
 	static boolean DoesPlayerExist(String name)
 	{
 		String file;
 		file = "plugins/CCEconomy/moneytracker.txt";
 		try
 		{
 		    FileReader reader = new FileReader(file);
 		    BufferedReader buff = new BufferedReader(reader);
 		    while(true)
 		    {
 		    	String inputText = buff.readLine();
 		        if(inputText == null)
 		        {
 		         	return false;
 		        }
 		        if(inputText.startsWith((name + " ")))
 		        {
 		        	return true;
 		        }
 		    }
 		}
 		catch (IOException ex)
 		{
 		    return false;
 		}
 	}
 	
 	static void AddPlayerToList(String name)
 	{
 		ArrayList<String> list = new ArrayList<String>();
 		String file = "plugins/CCEconomy/moneytracker.txt";
 		try
 		{
 		    FileReader reader = new FileReader(file);
 		    BufferedReader buff = new BufferedReader(reader);
 		    while(true)
 		    {
 		    	String inputText = buff.readLine();
 		        if(inputText == null)
 		        {
 		         	break;
 		        }
 		        list.add(inputText);
 		    }
 		    list.add(name + " 0");
 		}
 		catch (IOException ex)
 		{
 		}
 		Collections.sort(list);
 		try
 		{
 			FileWriter writer = new FileWriter(file);
 			BufferedWriter bw = new BufferedWriter(writer);
 			for (int i = 0; i < list.size(); i++)
 			{
 				bw.write(list.get(i));
 				bw.newLine();
 			}
 			bw.close();
 		}
 		catch (Exception e)
 		{
 		}
 	}
 	
 	public static String Bal(String name)
 	{
 		String file;
 		file = "plugins/CCEconomy/moneytracker.txt";
 		String playersbal= "";
 		try
 		{
 		    FileReader reader = new FileReader(file);
 		    BufferedReader buff = new BufferedReader(reader);
 		    while(true)
 		    {
 		    	String inputText = buff.readLine();
 		        if(inputText == null)
 		        {
 		         	break;
 		        }
 		        if(inputText.startsWith((name + " ")))
 		        {
 		        	playersbal = inputText.replace(name + " ", "");
 		        	return playersbal;
 		        }
 		    }
 		}
 		catch (IOException ex)
 		{
 		    return null;
 		}
 		return null;
 	}
 	
 	static String Baltop(int page, int time)
 	{
 		ArrayList<String> list = new ArrayList<String>();
 		ArrayList<Double> balsort = new ArrayList<Double>();
 		String file = "plugins/CCEconomy/moneytracker.txt";
 		try
 		{
 		    FileReader reader = new FileReader(file);
 		    BufferedReader buff = new BufferedReader(reader);
 		    while(true)
 		    {
 		    	String inputText = buff.readLine();
 		        if(inputText == null)
 		        {
 		         	break;
 		        }
 		        list.add(inputText);
 		        balsort.add(Double.parseDouble(inputText.split(" ")[1]));
 		    }
 		}
 		catch (IOException ex)
 		{
 		}
 		Collections.sort(list);
 		Collections.sort(balsort);
 		Collections.reverse(balsort);
 		page = page * 10;
 		if (list.size() < time + page + 1)
 		{
 			return null;
 		}
 		if (time == 11)
 		{
 			return null;
 		}
 		double temp = roundTwoDecimals(balsort.get(page + time));
 		String StrBal = Double.toString(temp);
		int BalSpot = BaltopCords(StrBal);
 		if (BalSpot == -1)
 		{
 			return list.get(0);
 		}
 		return list.get(BalSpot);
 	}
 	
	static int BaltopCords(String money)
 	{
 		ArrayList<String> list = new ArrayList<String>();
 		String file = "plugins/CCEconomy/moneytracker.txt";
 		try
 		{
 		    FileReader reader = new FileReader(file);
 		    BufferedReader buff = new BufferedReader(reader);
 		    while(true)
 		    {
 		    	String inputText = buff.readLine();
 		        if(inputText == null)
 		        {
 		         	break;
 		        }
 		        list.add(inputText);
 		    }
 		}
 		catch (IOException ex)
 		{
 		}
 		Collections.sort(list);
 		for(int i = 0; i < list.size(); i++)
 		{
 			if(list.get(i).contains(" " + money))
 			{
				return i;
 			}
 		}
 		return 0;
 	}
 	
 	static int BaltopPages()
 	{
 		ArrayList<String> list = new ArrayList<String>();
 		String file = "plugins/CCEconomy/moneytracker.txt";
 		try
 		{
 		    FileReader reader = new FileReader(file);
 		    BufferedReader buff = new BufferedReader(reader);
 		    while(true)
 		    {
 		    	String inputText = buff.readLine();
 		        if(inputText == null)
 		        {
 		         	break;
 		        }
 		        list.add(inputText);
 		    }
 		}
 		catch (IOException ex)
 		{
 		}
 		int rounder = 0;
 		if (list.size()%10 != 0)
 		{
 			rounder = 1;
 		}
 		return (list.size()/10) + rounder;
 	}
 	
 	static double roundTwoDecimals(double d)
 	{
 		DecimalFormat twoDForm = new DecimalFormat("#.##");
         return Double.valueOf(twoDForm.format(d));
 	}
 	
 	public static void SetMoney(String name, double amount)
 	{
 		amount = roundTwoDecimals(amount);
 		ArrayList<String> list = new ArrayList<String>();
 		String file = "plugins/CCEconomy/moneytracker.txt";
 		try
 		{
 		    FileReader reader = new FileReader(file);
 		    BufferedReader buff = new BufferedReader(reader);
 		    while(true)
 		    {
 		    	String inputText = buff.readLine();
 		        if(inputText == null)
 		        {
 		         	break;
 		        }
 		        list.add(inputText);
 		    }
 		}
 		catch (IOException ex)
 		{
 		}
 		int spotinlist = list.indexOf(name + " " + Bal(name));
 		String newbal;
 		newbal = name + " " + Double.toString(amount);
 		list.set(spotinlist, newbal);
 		try
 		{
 			FileWriter writer = new FileWriter(file);
 			BufferedWriter bw = new BufferedWriter(writer);
 			for (int i = 0; i < list.size(); i++)
 			{
 				bw.write(list.get(i));
 				bw.newLine();
 			}
 			bw.close();
 		}
 		catch (Exception e)
 		{
 		}
 	}
 	
 	public static void RemoveMoney(String name, double amount)
 	{
 		String bal = Bal(name);
 		double intbal = Double.parseDouble(bal);
 		double newamount;
 		newamount = intbal - amount;
 		newamount = roundTwoDecimals(newamount);
 		SetMoney(name, newamount);
 	}
 	
 	public static void AddMoney(String name, double amount)
 	{
 		String bal = Bal(name);
 		double intbal = Double.parseDouble(bal);
 		double newamount;
 		newamount = intbal + amount;
 		newamount = roundTwoDecimals(newamount);
 		SetMoney(name, newamount);
 	}
 	
 	
 	public class LoginListener implements Listener
 	{
 		@EventHandler
 		public void onPlayerJoin(PlayerJoinEvent event)
 		{
         	Player player = event.getPlayer();
         	String playername = player.getName();
         	if (!DoesPlayerExist(playername))
         	{
         		AddPlayerToList(playername);
         	}
 		}
 	}
 	
     @Override
     public void onDisable()
     {
     	getLogger().info("CCEconomy has been disabled.");
     }
 }
