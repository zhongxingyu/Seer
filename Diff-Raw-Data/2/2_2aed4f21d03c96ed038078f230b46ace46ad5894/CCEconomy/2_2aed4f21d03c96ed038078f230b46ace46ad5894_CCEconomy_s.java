 package com.github.CorporateCraft.cceconomy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CCEconomy extends JavaPlugin
 {
 	@Override
     public void onEnable()
 	{	
		getLogger().info("CCEconomy has been enabled, you now have an advanced economy system.");
 		getServer().getPluginManager().registerEvents(new LoginListener(), this);
 		Initialization.InitiateFiles();
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
 	        		   String balance = BalChecks.Bal(playersname);
 		        	   if(balance == null)
 		        	   {
 		        		   player.sendMessage("That player is not in my records. If the player is offline, please use the full name.");
 		        		   return true;
 		        	   }
 		        	   player.sendMessage(playersname + "'s balance is: $" + balance);
 		        	   return true;
 		           }
 	           }
 	           if(player.hasPermission("CCEconomy.bal"))
 	           {
 	        	   String balance = BalChecks.Bal(player.getName());
 	        	   if(balance == null)
 	        	   {
 	        		   player.sendMessage("You do not seem to exist let me add you now.");
 	        		   PlayerToFile.AddPlayerToList(player.getName());
 	        		   return true;
 	        	   }
 	        	   player.sendMessage("$" + balance);
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
 						String balance = BalChecks.Bal(playersname);
 						if(balance == null)
 						{
 							sender.sendMessage("That player is not in my records. If the player is offline, please use the full name.");
 							return true;
 						}
 						sender.sendMessage(playersname + "'s balance is: $" + balance);
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
 		           		if(!Formatter.isLegal(args[0]))
 						{
 							return false;
 						}
 		        	   	page = Integer.parseInt(args[0]);
 		           	}
 		           	if (args.length == 0)
 		           	{
 		           		page = 1;
 		           	}
 	        	   	int time = 0;
 	        	   	String bal;
 	        	   	int totalpages = BalChecks.BaltopPages();
 	        	   	if (page>totalpages)
 	        	   	{
 	        	   		player.sendMessage(ChatColor.GOLD + "Input a number from 1 to " + Integer.toString(totalpages));
 	        	   		return true;
 	        	   	}
 	        	   	player.sendMessage(ChatColor.GOLD + "Balanace Top Page [" + Integer.toString(page) + "/" + Integer.toString(totalpages) + "]");
 	        	   	page = page - 1;
 	        	   	bal = BalChecks.BalTop(page, time);
 	        	   	while(bal != null)
 	        	   	{
 	        	   		bal = Integer.toString((page*10) + time + 1) + ". " + bal.split(" ")[0] + " has: $" + bal.split(" ")[1];
 	        	   		player.sendMessage(bal);
 	        	   		time++;
 	        	   		bal = BalChecks.BalTop(page, time);
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
         	   	int totalpages = BalChecks.BaltopPages();
         	   	if (page>totalpages)
         	   	{
         	   		sender.sendMessage(ChatColor.GOLD + "Input a number from 1 to " + Integer.toString(totalpages));
         	   		return true;
         	   	}
         	   	sender.sendMessage(ChatColor.GOLD + "Balanace Top Page [" + Integer.toString(page) + "/" + Integer.toString(totalpages) + "]");
         	   	page = page - 1;
         	   	bal = BalChecks.BalTop(page, time);
         	   	while(bal != null)
         	   	{
         	   		bal = Integer.toString((page*10) + time + 1) + ". " + bal.split(" ")[0] + " has: $" + bal.split(" ")[1];
         	   		sender.sendMessage(bal);
         	   		time++;
         	   		bal = BalChecks.BalTop(page, time);
         	   	}
         	   	return true;
 		    }
 		}
 		if(cmd.getName().equalsIgnoreCase("pay"))
 		{			
 			if (sender instanceof Player)
 			{
 	           Player player = (Player) sender;
 	           if (args.length > 2 || args.length == 0)
 	           {
 	        	   return false;
 	           }
 	           if(player.hasPermission("CCEconomy.pay"))
 	           {
 	        	   	if(!Formatter.isLegal(args[1]))
 					{
 						return false;
 					}
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
 					if(!PlayerToFile.DoesPlayerExist(targetsname))
 					{
 						player.sendMessage("Please enter a valid player to send money to.");
 						return true;
 					}
 					String balance = BalChecks.Bal(player.getName());
 					double intbal = Double.parseDouble(balance);
 					double payamount = Math.abs(Double.parseDouble(args[1]));
 					if (intbal < payamount)
 					{
 						player.sendMessage("You dont have: $" + args[1]);
 						return true;
 					}
 					payamount = Double.parseDouble(Formatter.roundTwoDecimals(payamount));
 					EditPlayerMoney.RemoveMoney(player.getName(), payamount);
 					EditPlayerMoney.AddMoney(targetsname, payamount);
 					player.sendMessage("Your payed " + targetsname + " $" + Formatter.roundTwoDecimals(payamount));
 					
 					try
 					{
 						Player target = sender.getServer().getPlayer(args[0]);
 						target.sendMessage("You received $" + Formatter.roundTwoDecimals(payamount) + " from " + player.getName() + ".");
 					}
 					catch (Exception e){}
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
 				if (args.length > 3 || args.length == 0 || args.length == 1)
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
 					if(!PlayerToFile.DoesPlayerExist(targetsname))
 					{
 						player.sendMessage("Please enter a valid player to change the balance of.");
 						return true;
 					}
 					if (args[0].equalsIgnoreCase("reset"))
 					{
 						EditPlayerMoney.SetMoney(targetsname, "0");
 						player.sendMessage("Your successfully reset the balance of " + targetsname + ".");
 						return true;
 					}
 					if (args.length == 3)
 					{
 						if(!Formatter.isLegal(args[2]))
 						{
 							return false;
 						}
 						double amount = Double.parseDouble(args[2]);
 						String balance = BalChecks.Bal(targetsname);
 						double intbal = Double.parseDouble(balance);
 						amount = Double.parseDouble(Formatter.roundTwoDecimals(amount));
 						String setamount = Formatter.roundTwoDecimals(amount);
 						if (args[0].equalsIgnoreCase("give"))
 						{
 							EditPlayerMoney.AddMoney(targetsname, amount);
 							player.sendMessage("Your successfully gave "+ " $" + setamount + " to "  + targetsname + ".");
 							return true;
 						}
 						if (args[0].equalsIgnoreCase("take"))
 						{
 							if(intbal-amount>=0)
 							{
 								EditPlayerMoney.RemoveMoney(targetsname, amount);
 								player.sendMessage("Your successfully took "+ " $" + setamount + " from "  + targetsname + ".");
 								return true;
 							}
 						}
 						if (args[0].equalsIgnoreCase("set"))
 						{
 							EditPlayerMoney.SetMoney(targetsname, setamount);
 							player.sendMessage("Your successfully set the balance of " + targetsname + " to $" + Formatter.roundTwoDecimals(amount));
 							return true;
 						}
 						return false;
 					}
 				}
 				return false;
 			} 	
 			else
 			{
 				if (args.length > 3 || args.length == 0)
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
 				if(!PlayerToFile.DoesPlayerExist(targetsname))
 				{
 					sender.sendMessage("Please enter a valid player to change the balance of.");
 					return true;
 				}
 				if (args[0].equalsIgnoreCase("reset"))
 				{
 					EditPlayerMoney.SetMoney(targetsname, "0");
 					sender.sendMessage("Your successfully reset the balance of " + targetsname + ".");
 					return true;
 				}
 				if (args.length == 3)
 				{
 					if(!Formatter.isLegal(args[2]))
 					{
 						return false;
 					}
 					double amount = Double.parseDouble(args[2]);
 					String balance = BalChecks.Bal(targetsname);
 					double intbal = Double.parseDouble(balance);
 					amount = Double.parseDouble(Formatter.roundTwoDecimals(amount));
 					String setamount = Formatter.roundTwoDecimals(amount);
 					if (args[0].equalsIgnoreCase("give"))
 					{
 						EditPlayerMoney.AddMoney(targetsname, amount);
 						sender.sendMessage("Your successfully gave "+ " $" + setamount + " to "  + targetsname + ".");
 						return true;
 					}
 					if (args[0].equalsIgnoreCase("take"))
 					{
 						if(intbal-amount>=0)
 						{
 							EditPlayerMoney.RemoveMoney(targetsname, amount);
 							sender.sendMessage("Your successfully took "+ " $" + setamount + " from "  + targetsname + ".");
 							return true;
 						}
 					}
 					if (args[0].equalsIgnoreCase("set"))
 					{
 						EditPlayerMoney.SetMoney(targetsname, setamount);
 						sender.sendMessage("Your successfully set the balance of " + targetsname + " to $" + Formatter.roundTwoDecimals(amount));
 						return true;
 					}
 					return false;
 				}
 				return false;
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("price"))
 		{
 			if (sender instanceof Player)
 			{
 				Player player = (Player) sender;
 				if (args.length > 1)
 				{
 					return false;
 				}
 				if(player.hasPermission("CCEconomy.price"))
 				{
 					String ItemName = "";
 					try
 					{
 						ItemName = args[0];
 					}
 					catch (Exception e)
 					{
 						ItemName = Integer.toString(player.getItemInHand().getTypeId());
 					}
 					if(Formatter.isLegal(ItemName))
 					{
 						ItemName = Materials.idToName(Integer.parseInt(ItemName));
 					}
 					ItemName = ItemName.toUpperCase();
 					String cost = Prices.Cost("plugins/CCEconomy/sellprices.txt", ItemName);
 					ItemName = Formatter.CapFirst(ItemName);
 					if(cost == null)
 					{
 						player.sendMessage(ItemName + " cannot be sold to the server");
 						return true;
 					}
 					if(cost.equalsIgnoreCase("null"))
 					{
 						player.sendMessage(ItemName + " cannot be sold to the server");
 						return true;
 					}
 					player.sendMessage(ItemName + " can be sold for $" + cost);
 					return true;
 				}
 			}
 			else
 			{
 				if (args.length != 1)
 				{
 					return false;
 				}
 				String ItemName = args[0];
 				if(Formatter.isLegal(ItemName))
 				{
 					ItemName = Materials.idToName(Integer.parseInt(ItemName));
 				}
 				ItemName = ItemName.toUpperCase();
 				String cost = Prices.Cost("plugins/CCEconomy/sellprices.txt", ItemName);
 				ItemName = Formatter.CapFirst(ItemName);
 				if(cost == null)
 				{
 					sender.sendMessage(ItemName + " cannot be sold to the server");
 					return true;
 				}
 				if(cost.equalsIgnoreCase("null"))
 				{
 					sender.sendMessage(ItemName + " cannot be sold to the server");
 					return true;
 				}
 				sender.sendMessage(ItemName + " can be sold for $" + cost);
 				return true;
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("cost"))
 		{
 			if (sender instanceof Player)
 			{
 				Player player = (Player) sender;
 				if (args.length > 1)
 				{
 					return false;
 				}
 				if(player.hasPermission("CCEconomy.cost"))
 				{
 					String ItemName = "";
 					try
 					{
 						ItemName = args[0];
 					}
 					catch (Exception e)
 					{
 						ItemName = Integer.toString(player.getItemInHand().getTypeId());
 					}
 					if(Formatter.isLegal(ItemName))
 					{
 						ItemName = Materials.idToName(Integer.parseInt(ItemName));
 					}
 					ItemName = ItemName.toUpperCase();
 					String cost = Prices.Cost("plugins/CCEconomy/buyprices.txt", ItemName);
 					ItemName = Formatter.CapFirst(ItemName);
 					if(cost == null)
 					{
 						player.sendMessage(ItemName + " cannot be bought from the server");
 						return true;
 					}
 					if(cost.equalsIgnoreCase("null"))
 					{
 						player.sendMessage(ItemName + " cannot be bought from the server");
 						return true;
 					}
 					player.sendMessage(ItemName + " costs $" + cost);
 					return true;
 				}
 			}
 			else
 			{
 				if (args.length != 1)
 				{
 					return false;
 				}
 				String ItemName = args[0];
 				if(Formatter.isLegal(ItemName))
 				{
 					ItemName = Materials.idToName(Integer.parseInt(ItemName));
 				}
 				ItemName = ItemName.toUpperCase();
 				String cost = Prices.Cost("plugins/CCEconomy/buyprices.txt", ItemName);
 				ItemName = Formatter.CapFirst(ItemName);
 				if(cost == null)
 				{
 					sender.sendMessage(ItemName + " cannot be bought from the server");
 					return true;
 				}
 				if(cost.equalsIgnoreCase("null"))
 				{
 					sender.sendMessage(ItemName + " cannot be bought from the server");
 					return true;
 				}
 				sender.sendMessage(ItemName + " costs $" + cost);
 				return true;
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("setprice"))
 		{
 			if (sender instanceof Player)
 			{
 				Player player = (Player) sender;
 				if (args.length > 2)
 				{
 					return false;
 				}
 				if (args.length == 0)
 				{
 					return false;
 				}
 				if(player.hasPermission("CCEconomy.setprice"))
 				{
 					String ItemName = "";					
 					if(args.length == 2)
 					{
 						ItemName = args[0];
 						if(Formatter.isLegal(ItemName))
 						{
 							ItemName = Materials.idToName(Integer.parseInt(ItemName));
 						}
 						if(!Formatter.isLegal(args[1]) && !args[1].equalsIgnoreCase("null"))
 						{
 							return false;
 						}
 						ItemName = ItemName.toUpperCase();
 						if(args[1].equalsIgnoreCase("null"))
 						{
 							Prices.SetCost("plugins/CCEconomy/sellprices.txt", ItemName, args[1]);
 							ItemName = ItemName.replaceAll("_", " ");
 							ItemName = Formatter.CapFirst(ItemName);
 							player.sendMessage(ItemName + " can no longer be sold");
 							return true;
 						}
 						else
 						{
 							Prices.SetCost("plugins/CCEconomy/sellprices.txt", ItemName, Formatter.roundTwoDecimals(Double.parseDouble(args[1])));
 							ItemName = ItemName.replaceAll("_", " ");
 							ItemName = Formatter.CapFirst(ItemName);
 							player.sendMessage(ItemName + "'s price was set to $" + Formatter.roundTwoDecimals(Double.parseDouble(args[1])));
 							return true;
 						}
 					}
 					else
 					{
 						ItemName = Integer.toString(player.getItemInHand().getTypeId());
 						if(Formatter.isLegal(ItemName))
 						{
 							ItemName = Materials.idToName(Integer.parseInt(ItemName));
 						}
 						if(!Formatter.isLegal(args[0]) && !args[0].equalsIgnoreCase("null"))
 						{
 							return false;
 						}
 						ItemName = ItemName.toUpperCase();
 						if(args[0].equalsIgnoreCase("null"))
 						{
 							Prices.SetCost("plugins/CCEconomy/sellprices.txt", ItemName, args[0]);
 							ItemName = ItemName.replaceAll("_", " ");
 							ItemName = Formatter.CapFirst(ItemName);
 							player.sendMessage(ItemName + " can no longer be sold");
 							return true;
 						}
 						else
 						{
 							Prices.SetCost("plugins/CCEconomy/sellprices.txt", ItemName, Formatter.roundTwoDecimals(Double.parseDouble(args[0])));
 							ItemName = ItemName.replaceAll("_", " ");
 							ItemName = Formatter.CapFirst(ItemName);
 							player.sendMessage(ItemName + "'s price was set to $" + Formatter.roundTwoDecimals(Double.parseDouble(args[0])));
 							return true;
 						}
 					}
 				}
 			}
 			else
 			{
 				if (args.length != 2)
 				{
 					return false;
 				}
 				String ItemName;
 				ItemName = args[0];
 				if(Formatter.isLegal(ItemName))
 				{
 					ItemName = Materials.idToName(Integer.parseInt(ItemName));
 				}
 				if(!Formatter.isLegal(args[1]) && !args[1].equalsIgnoreCase("null"))
 				{
 					return false;
 				}
 				ItemName = ItemName.toUpperCase();
 				if(args[1].equalsIgnoreCase("null"))
 				{
 					Prices.SetCost("plugins/CCEconomy/sellprices.txt", ItemName, args[1]);
 					ItemName = ItemName.replaceAll("_", " ");
 					ItemName = Formatter.CapFirst(ItemName);
 					sender.sendMessage(ItemName + " can no longer be sold");
 					return true;
 				}
 				else
 				{
 					Prices.SetCost("plugins/CCEconomy/sellprices.txt", ItemName, Formatter.roundTwoDecimals(Double.parseDouble(args[1])));
 					ItemName = ItemName.replaceAll("_", " ");
 					ItemName = Formatter.CapFirst(ItemName);
 					sender.sendMessage(ItemName + "'s price was set to $" + Formatter.roundTwoDecimals(Double.parseDouble(args[1])));
 					return true;
 				}
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("setcost"))
 		{
 			if (sender instanceof Player)
 			{
 				Player player = (Player) sender;
 				if (args.length > 2)
 				{
 					return false;
 				}
 				if (args.length == 0)
 				{
 					return false;
 				}
 				if(player.hasPermission("CCEconomy.setcost"))
 				{
 					String ItemName = "";					
 					if(args.length == 2)
 					{
 						ItemName = args[0];
 						if(Formatter.isLegal(ItemName))
 						{
 							ItemName = Materials.idToName(Integer.parseInt(ItemName));
 						}
 						if(!Formatter.isLegal(args[1]) && !args[1].equalsIgnoreCase("null"))
 						{
 							return false;
 						}
 						ItemName = ItemName.toUpperCase();
 						if(args[1].equalsIgnoreCase("null"))
 						{
 							Prices.SetCost("plugins/CCEconomy/buyprices.txt", ItemName, args[1]);
 							ItemName = ItemName.replaceAll("_", " ");
 							ItemName = Formatter.CapFirst(ItemName);
 							player.sendMessage(ItemName + " can no longer be bought");
 							return true;
 						}
 						else
 						{
 							Prices.SetCost("plugins/CCEconomy/buyprices.txt", ItemName, Formatter.roundTwoDecimals(Double.parseDouble(args[1])));
 							ItemName = ItemName.replaceAll("_", " ");
 							ItemName = Formatter.CapFirst(ItemName);
 							player.sendMessage(ItemName + "'s cost was set to $" + Formatter.roundTwoDecimals(Double.parseDouble(args[1])));
 							return true;
 						}
 					}
 					else
 					{
 						ItemName = Integer.toString(player.getItemInHand().getTypeId());
 						if(Formatter.isLegal(ItemName))
 						{
 							ItemName = Materials.idToName(Integer.parseInt(ItemName));
 						}
 						if(!Formatter.isLegal(args[0]) && !args[0].equalsIgnoreCase("null"))
 						{
 							return false;
 						}
 						ItemName = ItemName.toUpperCase();
 						if(args[0].equalsIgnoreCase("null"))
 						{
 							Prices.SetCost("plugins/CCEconomy/buyprices.txt", ItemName, args[0]);
 							ItemName = ItemName.replaceAll("_", " ");
 							ItemName = Formatter.CapFirst(ItemName);
 							player.sendMessage(ItemName + " can no longer be bought");
 							return true;
 						}
 						else
 						{
 							Prices.SetCost("plugins/CCEconomy/buyprices.txt", ItemName, Formatter.roundTwoDecimals(Double.parseDouble(args[0])));
 							ItemName = ItemName.replaceAll("_", " ");
 							ItemName = Formatter.CapFirst(ItemName);
 							player.sendMessage(ItemName + "'s cost was set to $" + Formatter.roundTwoDecimals(Double.parseDouble(args[0])));
 							return true;
 						}
 					}
 				}
 			}
 			else
 			{
 				if (args.length != 2)
 				{
 					return false;
 				}
 				String ItemName;
 				ItemName = args[0];
 				if(Formatter.isLegal(ItemName))
 				{
 					ItemName = Materials.idToName(Integer.parseInt(ItemName));
 				}
 				if(!Formatter.isLegal(args[1]) && !args[1].equalsIgnoreCase("null"))
 				{
 					return false;
 				}
 				ItemName = ItemName.toUpperCase();
 				if(args[1].equalsIgnoreCase("null"))
 				{
 					Prices.SetCost("plugins/CCEconomy/buyprices.txt", ItemName, args[1]);
 					ItemName = ItemName.replaceAll("_", " ");
 					ItemName = Formatter.CapFirst(ItemName);
 					sender.sendMessage(ItemName + " can no longer be bought");
 					return true;
 				}
 				else
 				{
 					Prices.SetCost("plugins/CCEconomy/buyprices.txt", ItemName, Formatter.roundTwoDecimals(Double.parseDouble(args[1])));
 					ItemName = ItemName.replaceAll("_", " ");
 					ItemName = Formatter.CapFirst(ItemName);
 					sender.sendMessage(ItemName + "'s cost was set to $" + Formatter.roundTwoDecimals(Double.parseDouble(args[1])));
 					return true;
 				}
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("buy"))
 		{
 			if (sender instanceof Player)
 			{
 				Player player = (Player) sender;
 				if (args.length > 2)
 				{
 					return false;
 				}
 				if (args.length == 0)
 				{
 					return false;
 				}
 				if(player.hasPermission("CCEconomy.buy"))
 				{
 					PlayerInventory inventory = player.getInventory();
 					String balance = BalChecks.Bal(player.getName());
 					double intbal = Double.parseDouble(balance);
 					int amount = 0;					
 				    String ItemName = "";
 					if(args.length == 2)
 					{
 						ItemName = args[0];
 						if(Formatter.isLegal(ItemName))
 						{
 							ItemName = Materials.idToName(Integer.parseInt(ItemName));
 						}
 						if(!Formatter.isLegal(args[1]))
 						{
 							return false;
 						}
 						amount = Integer.parseInt(args[1]);
 					}
 					else
 					{
 						ItemName = Integer.toString(player.getItemInHand().getTypeId());
 						if(Formatter.isLegal(ItemName))
 						{
 							ItemName = Materials.idToName(Integer.parseInt(ItemName));
 						}
 						if(!Formatter.isLegal(args[0]))
 						{
 							return false;
 						}
 						amount = Integer.parseInt(args[0]);
 					}
 					ItemName = ItemName.toUpperCase();
 					Double Cost = 0.00;
 					Cost = Prices.GetCost("plugins/CCEconomy/buyprices.txt", ItemName, amount);
 					ItemName = Formatter.CapFirst(ItemName);
 					if(Cost == null)
 					{
 						player.sendMessage(ItemName + " cannot be bought from the server.");
 						return true;
 					}
 					else
 					{
 						if (intbal < Cost)
 						{
 							player.sendMessage("You dont have enough money to buy that item.");
 							return true;
 						}
 						EditPlayerMoney.RemoveMoney(player.getName(), Cost);
 						ItemStack itemstack = new ItemStack(Material.matchMaterial(ItemName), amount);
 						inventory.addItem(itemstack);
 						ItemName = ItemName.replaceAll("_", " ");
 						player.sendMessage("You bought " + Integer.toString(amount) + " of " + ItemName + ".");
 						player.sendMessage("$" + Formatter.roundTwoDecimals(Cost) + " was removed from your acount.");
 						return true;
 					}
 				}
 			}
 			else
 			{
 				sender.sendMessage("Log in to use this command");
 				return true;
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("sell"))
 		{
 			if (sender instanceof Player)
 			{
 				Player player = (Player) sender;
 				if (args.length > 2)
 				{
 					return false;
 				}
 				if (args.length == 0)
 				{
 					return false;
 				}
 				if(player.hasPermission("CCEconomy.sell"))
 				{
 					PlayerInventory inventory = player.getInventory();
 					int amount = 0;					
 				    String ItemName = "";
 					if(args.length == 2)
 					{
 						ItemName = args[0];
 						if(Formatter.isLegal(ItemName))
 						{
 							ItemName = Materials.idToName(Integer.parseInt(ItemName));
 						}
 						if(!Formatter.isLegal(args[1]))
 						{
 							return false;
 						}
 						amount = Integer.parseInt(args[1]);
 					}
 					else
 					{
 						ItemName = Integer.toString(player.getItemInHand().getTypeId());
 						if(Formatter.isLegal(ItemName))
 						{
 							ItemName = Materials.idToName(Integer.parseInt(ItemName));
 						}
 						if(!Formatter.isLegal(args[0]))
 						{
 							return false;
 						}
 						amount = Integer.parseInt(args[0]);
 					}
 					ItemName = ItemName.toUpperCase();
 					Double Cost = 0.00;
 					Cost = Prices.GetCost("plugins/CCEconomy/sellprices.txt", ItemName, amount);
 					ItemName = Formatter.CapFirst(ItemName);
 					if(Cost == -1.00)
 					{
 						player.sendMessage(ItemName + " cannot be sold to the server.");
 						return true;
 					}
 					else
 					{
 						EditPlayerMoney.AddMoney(player.getName(), Cost);
 						ItemStack itemstack = new ItemStack(Material.matchMaterial(ItemName), amount);
 						if(inventory.contains(Material.matchMaterial(ItemName), amount))
 						{
 							inventory.removeItem(itemstack);
 							ItemName = ItemName.replaceAll("_", " ");
 							player.sendMessage("You sold " + Integer.toString(amount) + " of " + ItemName + ".");
 							player.sendMessage("$" + Formatter.roundTwoDecimals(Cost) + " was added to your acount.");
 						}
 						else
 						{
 							player.sendMessage("You do not have that many " + ItemName + "s");
 						}
 						return true;
 					}
 				}
 			}
 			else
 			{
 				sender.sendMessage("Log in to use this command");
 				return true;
 			}
 		}
 		return false; 
 	}	
 	
 	public class LoginListener implements Listener
 	{
 		@EventHandler
 		public void onPlayerJoin(PlayerJoinEvent event)
 		{
         	Player player = event.getPlayer();
         	String playername = player.getName();
         	if (!PlayerToFile.DoesPlayerExist(playername))
         	{
         		PlayerToFile.AddPlayerToList(playername);
         	}
 		}
 	}
 	
     @Override
     public void onDisable()
     {
     	getLogger().info("CCEconomy has been disabled.");
     }
 }
