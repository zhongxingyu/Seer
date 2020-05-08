 package com.etriacraft.etriawallet;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 
 public class Commands {
 
 	EtriaWallet plugin;
 
 	public Commands(EtriaWallet instance) {
 		this.plugin = instance;
 		init();
 	}
 
 	private void init() {
 		PluginCommand wallet = plugin.getCommand("wallet");
 		CommandExecutor exe;
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (args.length < 1) {
 					s.sendMessage("eEtriaWallet Commands");
 					s.sendMessage("3/wallet create <player>f - Creates a wallet for your account.");
 					s.sendMessage("3/wallet balancef - Shows your balance.");
 					s.sendMessage("3/wallet give <player> <amount>f - Gives a player money.");
 					return true;
 				}
 				else if (args[0].equalsIgnoreCase("balance") && s.hasPermission("etriawallet.balance")) {
 					if (args.length == 1) {
						ResultSet rs2 = DBConnection.query("SELECT balance FROM wallet_players WHERE player = '" + s.getName() + "';", false);
						try {
							while (rs2.next()) {
								s.sendMessage("eYou currently have a balance of a$" + rs2.getDouble("balance") + ".");
							}
						} catch (SQLException e) {
							s.sendMessage("cWas unable to check the balance. Perhaps you don't have a wallet yet.");
							s.sendMessage("eUse 3/wallet create eto create a wallet for yourself.");
							e.printStackTrace();
 						}
 					} else if (args.length == 2 && s.hasPermission("etriawallet.balance.others")) {
 						ResultSet rs2 = DBConnection.query("SELECT balance FROM wallet_players WHERE player = '" + args[1] + "';", false);
 						try {
 							while (rs2.next()) {
 								s.sendMessage("e" + args[1] + " has a balance of a" + rs2.getDouble("balance") + "e.");
 							}
 						} catch (SQLException e) {
 							s.sendMessage("cWas unable to find the balance for e" + args[1] + " cperhaps that player doesn't have a wallet?");
 							e.printStackTrace();
 						}
 					}
 				} else if (args[0].equalsIgnoreCase("create") && s.hasPermission("etriawallet.create")) {
 					if (args.length == 1) {
 						DBConnection.query("INSERT INTO wallet_players(player, balance) VALUES ('" + s.getName() + "', 0.00)", true);
 						s.sendMessage("eWallet Created!");
 					} else if (args.length == 2 && s.hasPermission("etriawallet.create.others")) {
 						DBConnection.query("INSERT INTO wallet_players(player, balance) VALUES ('" + args[1] + "', 0.00)", true);
 						s.sendMessage("eWallet created for " + args[1] + ".");
 					}
 				} else if (args[0].equalsIgnoreCase("give") && args.length == 3 && s.hasPermission("etriawallet.give")) {
 					DBConnection.query("UPDATE wallet_players SET balance = balance + " + args[2] + " WHERE player = '" + args[1] + "';", true);
 					s.sendMessage("eYou have given " + args[1] + " a$" + args[2] + "e.");
 				} else if (args[0].equalsIgnoreCase("take") && args.length == 3 && s.hasPermission("etriawallet.take")) {
 					DBConnection.query("UPDATE wallet_players SET balance = balance - " + args[2] + " WHERE player = '" + args[1] + "';", true);
 					s.sendMessage("eYou have taken " + args[1] + "a$" + args[2] + "e.");
 				} else { 
 					s.sendMessage("cYou don't have permission to do that!");
 				} return true;
 			}
 		}; wallet.setExecutor(exe);
 	}
 
 }
