 /*
  * MCTrade
  * Copyright (C) 2012 Fogest <http://fogest.net16.net> and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.fogest.mctrade.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import me.fogest.mctrade.DatabaseManager;
 import me.fogest.mctrade.MCTrade;
 import me.fogest.mctrade.MessageHandler;
 import me.fogest.mctrade.UrlShortener;
 import me.fogest.mctrade.Verify;
 import me.fogest.mctrade.AcceptTrade;
 
 public class PlayerCommands implements CommandExecutor {
 	private MCTrade plugin;
 	private int itemId;
 	private int itemAmount;
 	private Material itemMaterial;
 	private MessageHandler m;
 
 	private double tax = MCTrade.tax;
 	private double taxAmount;
 	private String webURL = MCTrade.webAddress;
 
 	public PlayerCommands(final MCTrade plugin, MessageHandler m) {
 		this.plugin = plugin;
 		this.m = m;
 	}
 
 	public boolean onCommand(final CommandSender sender, final Command command, String cmdLabel,
 			String[] args) {
 		if (cmdLabel.equalsIgnoreCase("mctrade")) {
 			if (MCTrade.perms.has(sender, "mctrade.mctrade")
 					|| MCTrade.perms.has(sender, "mctrade.*")) {
 
 				if (args.length <= 0) {
 					Player player = (Player) sender;
 					m.sendPlayerMessage(player,
 							"Command Usage : /mctrade <costPerItem> [Amount]. The item in your hand is the item being traded!");
 				} else if (args.length >= 1) {
 					Player player = (Player) sender;
 					setItemId(player.getItemInHand().getTypeId());
 					setItemAmount(player.getItemInHand().getAmount());
 					setItemMaterial(player.getItemInHand().getType());
 					int userId = DatabaseManager.getUserId(sender.getName());
 					if (userId == 0) {
 						String longLink = webURL + "registration.html";
 						m.sendPlayerMessage(player,
 								"You need an account with MCTrade to do this! Visit the following link to register: ");
 						m.sendPlayerMessage(player, UrlShortener.shortenURL(longLink));
 					} else {
 						if (args[0].equalsIgnoreCase("verify")) {
 							int ver = Verify.createUserVerification(sender.getName());
 							m.sendPlayerMessage(player, "Your verification code is: " + ver);
 						} else if (args[0].equalsIgnoreCase("accept")) {
 							if (args.length == 2 && args[1].matches("[0-9]+")) {
 								int id = Integer.parseInt(args[1]);
 								String mcTrader = DatabaseManager.getTradeUsername(id);
 								if (!(mcTrader.equals(sender.getName()))) {
 									int tradeStatus = DatabaseManager.getTradeStatus(id);
 									if (tradeStatus == 1) {
 										double cost = DatabaseManager.getItemCost(id);
 										// int amount =
 										// DatabaseManager.getTradeAmount(id);
 										if (MCTrade.econ.getBalance(sender.getName()) >= cost) {
 											AcceptTrade accept = new AcceptTrade(
 													Integer.parseInt(args[1]), player);
 											m.sendPlayerMessage(
 													player,
 													"You have sucessfully purchased "
 															+ accept.getAmount() + " "
 															+ accept.getTradeItem() + "'s");
 
 											MCTrade.econ.withdrawPlayer(sender.getName(), (cost));
 											MCTrade.econ.depositPlayer(
 													DatabaseManager.getTradeUsername(id), (cost));
 										} else {
 											m.sendPlayerMessage(
 													player,
 													"Sorry, that trade costs: "
 															+ cost
 															+ " and you only have: "
 															+ MCTrade.econ.getBalance(sender
 																	.getName()));
 										}
 									} else if (tradeStatus == 2) {
 										m.sendPlayerMessage(player,
 												"This trade has already been accepted!");
 									} else if (tradeStatus == 3) {
 										m.sendPlayerMessage(player, "This trade is hidden");
 									}
 								} else {
 									m.sendPlayerMessage(player, "You cannot accept your own trades");
 								}
 							} else {
 								m.sendPlayerMessage(player,
 										"Please enter the trade ID using /mctrade accept <id>");
 							}
 							m.sendPlayerMessage(player, "here 4");
 						} else if (args[0].matches("[0-9]+")) {
 							if (!(getItemMaterial().toString().equals("AIR"))) {
 								ItemStack tradeItem = new ItemStack(getItemMaterial(), itemAmount);
 								boolean trade = onTradeRemoveItem(tradeItem, player);
 								int price = Integer.parseInt(args[0]);
 								taxAmount = (price * tax);
 								double balance = (MCTrade.econ.getBalance(sender.getName()));
 								if (trade == true && balance >= taxAmount) {
 									MCTrade.econ.withdrawPlayer(sender.getName(), taxAmount);
 									int tId = DatabaseManager.createTrade(sender.getName(),
 											getItemId(), getItemMaterial().toString(),
 											getItemAmount(), args[0], player.getAddress()
 													.getAddress().getHostAddress());
 									m.serverBroadCast(sender.getName()
 											+ " has created a new trade (" + tId + ")");
 
									m.serverBroadCast("Item: " + ChatColor.GRAY
 											+ getItemMaterial() + ChatColor.WHITE + " Amount: "
 											+ ChatColor.GRAY + getItemAmount() + ChatColor.WHITE
 											+ " Price: " + ChatColor.GRAY + price);
 									m.serverBroadCast("More Info: "
 											+ UrlShortener
 													.shortenURL(webURL+"trades.html?id="
 															+ tId));
 									m.sendPlayerMessage(player,
 											"You have been charged " + taxAmount + " for the creation of this trade!");
 
 									m.sendToConsoleInfo("Player "
 											+ sender.getName()
 											+ " has created a trade with the following info: Price:"
 											+ args[0] + " Item Amount: " + getItemAmount()
 											+ " Item: " + getItemMaterial() + " Item ID: "
 											+ getItemId());
 								} else if (trade == false) {
 									m.sendPlayerMessage(player,
 											"Sorry, you don't have that much of that item!");
 								} else if (balance < taxAmount) {
 									m.sendPlayerMessage(
 											player,
 											"To prevent abuse, tax is charged on your item, on purchase rather then when your trade is accepted. Tax is based on the price you set the trade at and the tax for this one is: "
 													+ taxAmount + "And you only have " + balance);
 								}
 							} else {
 								m.sendPlayerMessage(player,
 										"I know air is cool an all, but I just cannot let you sell that :)");
 							}
 						}
 					}
 				}
 
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean onTradeRemoveItem(ItemStack is, Player p) {
 		p.getInventory().removeItem(is);
 		return true;
 	}
 
 	public int getItemId() {
 		return itemId;
 	}
 
 	public void setItemId(int itemId) {
 		this.itemId = itemId;
 	}
 
 	public Material getItemMaterial() {
 		return itemMaterial;
 	}
 
 	public void setItemMaterial(Material itemMaterial) {
 		this.itemMaterial = itemMaterial;
 	}
 
 	public int getItemAmount() {
 		return itemAmount;
 	}
 
 	public void setItemAmount(int itemAmount) {
 		this.itemAmount = itemAmount;
 	}
 }
