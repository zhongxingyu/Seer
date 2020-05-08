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
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import me.fogest.mctrade.DatabaseManager;
 import me.fogest.mctrade.MCTrade;
 import me.fogest.mctrade.MessageHandler;
 import me.fogest.mctrade.Msg;
 import me.fogest.mctrade.AcceptTrade;
 
 public class PlayerCommands implements CommandExecutor {
 	private int itemId;
 	private int itemAmount;
 	private int itemDurability;
 	private Material itemMaterial;
 	private MessageHandler m;
 
 	private double tax = MCTrade.tax;
 	private double taxAmount;
 	private String webURL = MCTrade.webAddress;
 	private String longLink = webURL + "registration.php";
 
 	private boolean trade = true;
 	private boolean tradeGo = true;
 
 	private int userId, id, tradeStatus;
 
 	// Command Handlers
 
 	public PlayerCommands(final MCTrade plugin, MessageHandler m) {
 		this.m = m;
 	}
 
 	public boolean onCommand(final CommandSender sender, final Command command, String cmdLabel, String[] args) {
 		Player player = (Player) sender;
 		userId = DatabaseManager.getUserId(player.getName());
 		// Gives usage message if player simply inputs /mct
 		if (args.length <= 0 && checkPerms(player, "mctrade")) {
 			m.tellPlayer(player, Msg.COMMAND_USAGE);
 		}
 		// Checking if the user actually put something after /mct
 		else if (args.length >= 1) {
 			if (args[0].equalsIgnoreCase("help") && checkPerms(player, "help")) {
 				m.tellPlayer(player, Msg.COMMAND_HELP);
 			}
 			// Attempt trade creation command if second var is number
 			if (args[0].matches("[0-9]+") && checkPerms(player, "trade")) {
 				prepareTrade(player);
 				if (userId > 0) {
 					CreateTrade(player, args);
 				} else if (userId < 1) {
 					m.tellPlayer(player, Msg.ACCOUNT_REQUIRED);
 				} else {
 					m.tellPlayer(player, Msg.USERID_GET_ERROR);
 				}
 			}
 			// Creating Online Account
 			else if (args[0].equalsIgnoreCase("create") && checkPerms(player, "create")) {
 				int userId = DatabaseManager.getUserId(player.getName());
 				if (args.length == 2 && userId < 1) {
 					String playerIP = player.getAddress().getAddress().getHostAddress();
 					DatabaseManager.createUser(args[1], player.getName(), playerIP);
 				} else if (userId > 0) {
 					m.tellPlayer(player, "You've already created an account!");
 				} else {
 					m.tellPlayer(player, Msg.CREATE_USAGE);
 				}
 			}
 			// Accepting Trade
 			else if (args[0].equalsIgnoreCase("accept") && checkPerms(player, "accept")) {
 				if (userId > 0) {
 					AcceptTrade(player, args);
 				} else if (userId < 1) {
 					m.tellPlayer(player, Msg.ACCOUNT_REQUIRED);
 				} else {
 					m.tellPlayer(player, Msg.GENERAL_ERROR);
 				}
 
 			}
 		}
 		return false;
 	}
 
 	private void AcceptTrade(Player player, String[] args) {
 		if (args.length == 2 && args[1].matches("[0-9]+")) {
 			id = Integer.parseInt(args[1]);
 			String mcTrader = DatabaseManager.getTradeUsername(id);
 			if (!(mcTrader.equals(player.getName()))) {
 
 				if (MCTrade.checkIP == true) {
 					if (!(player.getAddress().getAddress().getHostAddress().equals(DatabaseManager.getTraderIP(id)))) {
 						tradeGo = false;
 					} else {
 						tradeGo = true;
 					}
 				}
 
 				if (tradeGo == true) {
 					tradeStatus = DatabaseManager.getTradeStatus(id);
 					if (tradeStatus == 1) {
 						double cost = DatabaseManager.getItemCost(id);
 						if (MCTrade.econ.getBalance(player.getName()) >= cost) {
 							AcceptTrade accept = new AcceptTrade(Integer.parseInt(args[1]), player, m);
 							m.tellPlayer(player, "You have sucessfully purchased " + accept.getAmount() + " " + accept.getTradeItem() + "'s");
							m.tellAll(""+ player.getName() + " has purchased " + accept.getAmount() + " " + accept.getTradeItem() + "'s [" + accept.getTradeId() + "]");
 							MCTrade.econ.withdrawPlayer(player.getName(), (cost));
 							MCTrade.econ.depositPlayer(DatabaseManager.getTradeUsername(id), (cost));
 						} else {
 							m.tellPlayer(player, "Sorry, that trade costs: " + cost + " and you only have: " + MCTrade.econ.getBalance(player.getName()));
 						}
 					} else if (tradeStatus == 2) {
 						m.tellPlayer(player, Msg.TRADE_ALREADY_ACCEPTED);
 					} else if (tradeStatus == 3) {
 						m.tellPlayer(player, Msg.TRADE_ALREADY_HIDDEN);
 					}
 				} else {
 					m.tellPlayer(player, Msg.TRADE_CANNOT_ACCEPT_OWN);
 				}
 			} else {
 				m.tellPlayer(player, Msg.TRADE_CANNOT_ACCEPT_OWN);
 			}
 		} else {
 			m.tellPlayer(player, Msg.TRADE_ACCEPT_USAGE);
 		}
 	}
 
 	private void CreateTrade(Player player, String[] args) {
 		if (!(getItemMaterial().toString().equals("AIR"))) {
 			if (args.length == 2 && args[1].matches("[0-9]+")) {
 				int tempItemAmount = Integer.parseInt(args[1]);
 				if (checkItemMax(player) >= tempItemAmount) {
 					setItemAmount(tempItemAmount);
 					trade = true;
 				} else {
 					trade = false;
 				}
 			}
 			int price = Integer.parseInt(args[0]);
 			taxAmount = (price * tax);
 			double balance = (MCTrade.econ.getBalance(player.getName()));
 			int tId = 0;
 			int itemDur = getItemDurability(player.getItemInHand());
 			if (trade == true && balance >= taxAmount) {
 				if (args.length >= 2) {
 					MCTrade.econ.withdrawPlayer(player.getName(), taxAmount);
 					tId = DatabaseManager.createTrade(player.getName(), getItemId(), getItemMaterial().toString(), itemDur, getItemAmount(),
 							DatabaseManager.encodeEnchantments(player, player.getItemInHand()), args[0], player.getAddress().getAddress().getHostAddress());
 					removeItem(player, player.getItemInHand(), Integer.parseInt(args[1]));
 
 				} else if (args.length < 2) {
 					MCTrade.econ.withdrawPlayer(player.getName(), taxAmount);
 					tId = DatabaseManager.createTrade(player.getName(), getItemId(), getItemMaterial().toString(), itemDur, getItemAmount(),
 							DatabaseManager.encodeEnchantments(player, player.getItemInHand()), args[0], player.getAddress().getAddress().getHostAddress());
 					removeItem(player, player.getItemInHand());
 				}
 
 				m.tellAll(player.getName() + " has created a new trade (" + tId + ")");
 				if (itemDur == 101) {
 					m.tellAll("Item: " + ChatColor.GRAY + getItemMaterial() + ChatColor.WHITE + " Amount: " + ChatColor.GRAY + getItemAmount() + ChatColor.WHITE + " Price: " + ChatColor.GRAY + price);
 				} else {
 					m.tellAll("Item: " + ChatColor.GRAY + getItemMaterial() + ChatColor.WHITE + " Amount: " + ChatColor.GRAY + getItemAmount() + ChatColor.WHITE + " Price: " + ChatColor.GRAY + price
 							+ ChatColor.WHITE + "Durability: " + ChatColor.GRAY + itemDur);
 				}
 				m.tellPlayer(player, "You have been charged " + taxAmount + " for the creation of this trade!");
 			} else if (trade == false) {
 				m.tellPlayer(player, Msg.TRADE_NOT_ENOUGH_ITEMS);
 			} else if (balance < taxAmount) {
 				m.tellPlayer(player,
 						"To prevent abuse, tax is charged on your item, on purchase rather then when your trade is accepted. Tax is based on the price you set the trade at and the tax for this one is: "
 								+ taxAmount + "And you only have " + balance);
 			}
 		} else {
 			m.tellPlayer(player, Msg.TRADE_AIR);
 		}
 	}
 
 	// Checks global mctrade permissions
 	private boolean checkPerms(Player player) {
 		if (MCTrade.perms.has(player, "mctrade.*")) {
 			return true;
 		}
 		m.tellPlayer(player, Msg.PERMISSION_DENIED);
 		return false;
 	}
 
 	// Checks specific permissions for sub commands in mctrade and global.
 	private boolean checkPerms(Player player, String p) {
 		if (MCTrade.perms.has(player, "mctrade." + p)) {
 			return true;
 		} else if (MCTrade.perms.has(player, "mctrade.*")) {
 			return true;
 		}
 		m.tellPlayer(player, Msg.PERMISSION_DENIED);
 		return false;
 	}
 
 	public int checkItemMax(Player p) {
 		int amount = 0;
 		for (ItemStack i : p.getInventory().getContents()) {
 			if (i == null) {
 				continue;
 			}
 			if (i.getType().equals(getItemMaterial())) {
 				amount = amount + i.getAmount();
 			}
 		}
 		return amount;
 	}
 
 	public void removeItem(Player player, ItemStack playerStack) {
 		player.getInventory().removeItem(playerStack);
 	}
 
 	public void removeItem(Player player, ItemStack playerStack, int amount) {
 		int typeId = playerStack.getTypeId();
 		ItemStack[] contents = player.getInventory().getContents();
 		int counter = 0;
 		for (ItemStack stack : Arrays.asList(contents)) {
 			if (stack == null) {
 				continue;
 			}
 			if (stack.getTypeId() != typeId) {
 				continue;
 			}
 			if (stack.getAmount() < amount) {
 				contents[counter] = null;
 				amount -= stack.getAmount();
 			}
 			if (stack.getAmount() == amount) {
 				contents[counter] = null;
 				break;
 			}
 			if (stack.getAmount() > amount) {
 				stack.setAmount(stack.getAmount() - amount);
 				contents[counter] = stack;
 				break;
 			}
 			counter++;
 		}
 		player.getInventory().setContents(contents);
 	}
 
 	public boolean onTradeRemoveItem(ItemStack is, Player p) {
 		p.getInventory().removeItem(is);
 		return true;
 	}
 
 	private void prepareTrade(Player player) {
 		setItemId(player.getItemInHand().getTypeId());
 		setItemAmount(player.getItemInHand().getAmount());
 		setItemMaterial(player.getItemInHand().getType());
 		userId = DatabaseManager.getUserId(player.getName());
 	}
 
 	private int getUserId(Player player) {
 		return DatabaseManager.getUserId(player.getName());
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
 
 	public int getItemDurability(ItemStack item) {
 		double itemMax = item.getType().getMaxDurability();
 		double itemDur = item.getDurability();
 		if (itemDur != 0) {
 
 			double percent = (itemDur / itemMax) * 100;
 			m.tellAll("Item max durability is: " + itemMax);
 			m.tellAll("Item current durability is: " + itemDur);
 			m.tellAll("Percent with multiplication is: " + percent);
 
 			itemDurability = (int) Math.round(percent);
 			m.tellAll("Rounded percent with multiplication is: " + itemDurability);
 			return itemDurability;
 		} else if (itemDur == 0) {
 			return 100;
 		}
 		return 0;
 	}
 
 	public void setItemAmount(int itemAmount) {
 		this.itemAmount = itemAmount;
 	}
 }
