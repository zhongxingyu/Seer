 package de.hotmail.gurkilein.bankcraft;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.hotmail.gurkilein.bankcraft.banking.ExperienceBankingHandler;
 import de.hotmail.gurkilein.bankcraft.banking.MoneyBankingHandler;
 import de.hotmail.gurkilein.bankcraft.database.AccountDatabaseInterface;
 import de.hotmail.gurkilein.bankcraft.database.DatabaseManagerInterface;
 import de.hotmail.gurkilein.bankcraft.database.SignDatabaseInterface;
 import de.hotmail.gurkilein.bankcraft.database.flatfile.DatabaseManagerFlatFile;
 import de.hotmail.gurkilein.bankcraft.database.flatfile.ExperienceFlatFileInterface;
 import de.hotmail.gurkilein.bankcraft.database.flatfile.MoneyFlatFileInterface;
 import de.hotmail.gurkilein.bankcraft.database.flatfile.SignFlatFileInterface;
 import de.hotmail.gurkilein.bankcraft.database.mysql.DatabaseManagerMysql;
 import de.hotmail.gurkilein.bankcraft.database.mysql.ExperienceMysqlInterface;
 import de.hotmail.gurkilein.bankcraft.database.mysql.MoneyMysqlInterface;
 import de.hotmail.gurkilein.bankcraft.database.mysql.SignMysqlInterface;
 
 public class MinecraftCommandListener implements CommandExecutor{
 
 	private Bankcraft bankcraft;
 	private ConfigurationHandler coHa;
 
 	public MinecraftCommandListener(Bankcraft bankcraft) {
 		this.bankcraft = bankcraft;
 		this.coHa = bankcraft.getConfigurationHandler();
 	}
 
 
 	@SuppressWarnings("unused")
 	private Bankcraft plugin;
 	public Double betrag;
 
 	public void sendHelp(Player p) {
 		p.sendMessage("---Bankcraft-Help---");
 		p.sendMessage("/bank "+coHa.getString("signAndCommand.help")+" Shows the help page.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.balance") || Bankcraft.perms.has(p, "bankcraft.command"))
 			p.sendMessage("/bank "+coHa.getString("signAndCommand.balance")+" PLAYER Shows your banked money.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.balancexp") || Bankcraft.perms.has(p, "bankcraft.command"))
 		p.sendMessage("/bank "+coHa.getString("signAndCommand.balancexp")+" PLAYER Shows your banked XP.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.deposit") || Bankcraft.perms.has(p, "bankcraft.command"))
 		p.sendMessage("/bank "+coHa.getString("signAndCommand.deposit")+" AMOUNT Deposits money to your Account.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.withdraw") || Bankcraft.perms.has(p, "bankcraft.command"))
 		p.sendMessage("/bank "+coHa.getString("signAndCommand.withdraw")+" AMOUNT Withdraws money from your Account.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.depositxp") || Bankcraft.perms.has(p, "bankcraft.command"))
 		p.sendMessage("/bank "+coHa.getString("signAndCommand.depositxp")+" AMOUNT Deposits XP to your Account.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.withdrawxp") || Bankcraft.perms.has(p, "bankcraft.command"))
 		p.sendMessage("/bank "+coHa.getString("signAndCommand.withdrawxp")+" AMOUNT Withdraws XP from your Account.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.transfer") || Bankcraft.perms.has(p, "bankcraft.command"))
 		p.sendMessage("/bank "+coHa.getString("signAndCommand.transfer")+" PLAYER AMOUNT Transfers money to another Account.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.transferxp") || Bankcraft.perms.has(p, "bankcraft.command"))
 		p.sendMessage("/bank "+coHa.getString("signAndCommand.transferxp")+" PLAYER AMOUNT Transfers XP to another Account.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.interesttimer") || Bankcraft.perms.has(p, "bankcraft.command"))
 		p.sendMessage("/bank "+coHa.getString("signAndCommand.interesttimer")+" Shows the remaining time until the next wave of interests.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.exchange") || Bankcraft.perms.has(p, "bankcraft.command"))
 		p.sendMessage("/bank "+coHa.getString("signAndCommand.exchange")+" AMOUNT Exchanges money to XP.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.exchangexp") || Bankcraft.perms.has(p, "bankcraft.command"))
 		p.sendMessage("/bank "+coHa.getString("signAndCommand.exchangexp")+" AMOUNT Exchanges XP to money.");
 	}
 
 	public void sendAdminHelp(Player p) {
 		p.sendMessage("---Bankcraft-AdminHelp---");
 		p.sendMessage("/bankadmin help Shows the help page.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.set") || Bankcraft.perms.has(p, "bankcraft.command.admin"))
 		p.sendMessage("/bankadmin "+coHa.getString("signAndCommand.set")+" PLAYER AMOUNT Sets a players money.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.setxp") || Bankcraft.perms.has(p, "bankcraft.command.admin"))
 		p.sendMessage("/bankadmin "+coHa.getString("signAndCommand.setxp")+" PLAYER AMOUNT Sets a players XP.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.grant") || Bankcraft.perms.has(p, "bankcraft.command.admin"))
 		p.sendMessage("/bankadmin "+coHa.getString("signAndCommand.grant")+" PLAYER AMOUNT Grants a Player money.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.grantxp") || Bankcraft.perms.has(p, "bankcraft.command.admin"))
 		p.sendMessage("/bankadmin "+coHa.getString("signAndCommand.grantxp")+" PLAYER AMOUNT Grants a player XP.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.clear") || Bankcraft.perms.has(p, "bankcraft.command.admin"))
 		p.sendMessage("/bankadmin "+coHa.getString("signAndCommand.clear")+" PLAYER Clears money from a players Account.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.clearxp") || Bankcraft.perms.has(p, "bankcraft.command.admin"))
 		p.sendMessage("/bankadmin "+coHa.getString("signAndCommand.clearxp")+" PLAYER Clears XP from a players Account.");
 		if (Bankcraft.perms.has(p, "bankcraft.command.databaseimport") || Bankcraft.perms.has(p, "bankcraft.command.admin"))
 		p.sendMessage("/bankadmin "+coHa.getString("signAndCommand.databaseimport")+" OLDDATA NEWDATA Moves data from one database type to another");
 	}
 
 
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String cmdlabel, String[] vars) {
 		Player p;
 		if (sender instanceof Player) {
 			p = (Player) sender;
 			if (cmdlabel.equalsIgnoreCase("bank") || cmdlabel.equalsIgnoreCase("bc")) {
 				if (vars.length == 0) {
 					sendHelp(p);
 					return true;
 				}
 				if (vars.length == 1) {
 					if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.help"))) {
 						sendHelp(p);
 						return true;
 					}
 
 					if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.balance")) && (Bankcraft.perms.has(p, "bankcraft.command.balance") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 						return bankcraft.getInteractionHandler().interact(vars[0], "", p, p.getName());
 					}
 					if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.balancexp")) && (Bankcraft.perms.has(p, "bankcraft.command.balancexp") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 						return bankcraft.getInteractionHandler().interact(vars[0], "", p, p.getName());
 					}
 					
 					if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.interesttimer")) && (Bankcraft.perms.has(p, "bankcraft.command.interesttimer") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 						return bankcraft.getInteractionHandler().interact(vars[0], "", p, p.getName());
 					}
 				}
 				if (vars.length == 2) {
 					if (Util.isPositive(vars[1]) || vars[1].equalsIgnoreCase("all")) {
 						if (vars[0].equalsIgnoreCase("add") && (Bankcraft.perms.has(p, "bankcraft.admin"))) {
 							Block signblock = p.getTargetBlock(null, 50);
 							if (signblock.getType() == Material.WALL_SIGN) {
 								Sign sign = (Sign) signblock.getState();
 								if (sign.getLine(0).contains("[Bank]")) {
 									Integer typsign = -1;
 									try {
 										typsign = bankcraft.getSignDatabaseInterface().getType(signblock.getX(), signblock.getY(), signblock.getZ(), signblock.getWorld());
 									} catch (Exception e) {
 										e.printStackTrace();
 									}
									if (typsign == 1 || typsign == 2 || typsign == 3 || typsign == 4 || typsign == 6 || typsign == 7 || typsign == 8 || typsign == 9 || typsign == 12 || typsign == 13 || typsign == 14 || typsign == 15) {
 
 											Integer x = signblock.getX();
 											Integer y = signblock.getY();
 											Integer z = signblock.getZ();
 											World w = signblock.getWorld();
 											
 											Integer newType;
 											Integer currentType = bankcraft.getSignDatabaseInterface().getType(x, y, z, w);
 											
 											if (currentType == 1 || currentType == 2 || currentType == 6 || currentType == 7|| currentType ==  12|| currentType ==  13) {
 												newType = currentType +2;
 												bankcraft.getSignDatabaseInterface().changeType(x, y, z, newType, w);
 											}
 												
 												
 											bankcraft.getSignDatabaseInterface().addAmount(x, y, z, w, vars[1]);
 											coHa.printMessage(p, "message.amountAddedSuccessfullyToSign", vars[1], p.getName());
 											bankcraft.getSignHandler().updateSign(signblock,0);
 											return true;
 											}
 
 										
 									}
 								}
 							}
 						
 
 						
 						else if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.deposit")) && (Bankcraft.perms.has(p, "bankcraft.command.deposit") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 							return bankcraft.getInteractionHandler().interact(vars[0], vars[1], p, p.getName());
 						}
 						else if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.withdraw")) && (Bankcraft.perms.has(p, "bankcraft.command.withdraw") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 							return bankcraft.getInteractionHandler().interact(vars[0], vars[1], p, p.getName());
 						}
 						else if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.depositxp")) && (Bankcraft.perms.has(p, "bankcraft.command.depositxp") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 							return bankcraft.getInteractionHandler().interact(vars[0], vars[1], p, p.getName());
 						}
 						else if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.withdrawxp")) && (Bankcraft.perms.has(p, "bankcraft.command.withdrawxp") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 							return bankcraft.getInteractionHandler().interact(vars[0], vars[1], p, p.getName());
 						}
 						else if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.exchange")) && (Bankcraft.perms.has(p, "bankcraft.command.exchange") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 							return bankcraft.getInteractionHandler().interact(vars[0], vars[1], p, p.getName());
 						}
 						else if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.exchangexp")) && (Bankcraft.perms.has(p, "bankcraft.command.exchangexp") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 							return bankcraft.getInteractionHandler().interact(vars[0], vars[1], p, p.getName());
 						}
 
 						else {
 							p.sendMessage(ChatColor.RED + coHa.getString("chat.prefix") + "Wrong Syntax or missing permissions! Please see /bank help for more information!");
 						}
 
 					} else {
 						
 						if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.balance")) && (Bankcraft.perms.has(p, "bankcraft.command.balance.other") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 							return bankcraft.getInteractionHandler().interact(vars[0], null, p, vars[1]);
 						}
 						else if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.balancexp")) && (Bankcraft.perms.has(p, "bankcraft.command.balancexp.other") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 							return bankcraft.getInteractionHandler().interact(vars[0], null, p, vars[1]);
 						}
 					}
 
 
 				}
 
 				if (vars.length == 3) {
 					if (Util.isPositive(vars[2]) || vars[2].equalsIgnoreCase("all")) {
 						if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.transfer")) && (Bankcraft.perms.has(p, "bankcraft.command.transfer") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 							double amount;
 							if (vars[1].equalsIgnoreCase("all")) {
 								amount = bankcraft.getMoneyDatabaseInterface().getBalance(p.getName());
 							} else {
 								amount = Double.parseDouble(vars[1]);
 							}
 						
 							((MoneyBankingHandler)bankcraft.getBankingHandlers()[0]).transferFromAccountToAccount(p.getName(), vars[1], amount,p);
 							coHa.printMessage(p, "message.transferedSuccessfully", amount+"", vars[1]);
 
 							return true;
 						}
 						} else {
 							
 						if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.transferxp")) && (Bankcraft.perms.has(p, "bankcraft.command.transferxp") || Bankcraft.perms.has(p, "bankcraft.command"))) {
 
 							int amount;
 							if (vars[1].equalsIgnoreCase("all")) {
 								amount = bankcraft.getExperienceDatabaseInterface().getBalance(p.getName());
 							} else {
 								amount = Integer.parseInt(vars[1]);
 							}
 							
 							((ExperienceBankingHandler)bankcraft.getBankingHandlers()[0]).transferFromAccountToAccount(p.getName(), vars[1], amount,p);
 							coHa.printMessage(p, "message.transferedSuccessfullyXp", amount+"", vars[1]);
 
 							return true;
 						}
 						}
 
 						} else {
 							p.sendMessage(ChatColor.RED + coHa.getString("chat.prefix") + "Wrong Syntax or missing permissions! Please see /bank help for more information!");
 							return true;
 						}
 			} else {
 
 				if (cmdlabel.equalsIgnoreCase("bankadmin") || cmdlabel.equalsIgnoreCase("bcadmin")) {
 					if (vars.length == 0) {
 						sendAdminHelp(p);
 						return true;
 					}
 					else if (vars.length == 1) {
 
 						if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.admin.help"))) {
 							sendAdminHelp(p);
 							return true;
 						}
 					}
 					else if (vars.length == 2) {
 						if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.admin.clear")) && (Bankcraft.perms.has(p, "bankcraft.command.clear") || Bankcraft.perms.has(p, "bankcraft.command.admin"))) {
 							bankcraft.getMoneyDatabaseInterface().setBalance(vars[1], 0D);
 							p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Account cleared!");
 							return true;
 						}
 						if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.admin.clearxp")) && (Bankcraft.perms.has(p, "bankcraft.command.clearxp") || Bankcraft.perms.has(p, "bankcraft.command.admin"))) {
 							bankcraft.getExperienceDatabaseInterface().setBalance(vars[1], 0);
 							p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "XP-Account cleared!");
 							return true;
 						}
 					}
 					else if (vars.length == 3) {
 						if (Util.isDouble(vars[2])) {
 							if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.admin.set")) && (Bankcraft.perms.has(p, "bankcraft.command.set") || Bankcraft.perms.has(p, "bankcraft.command.admin"))) {
 								bankcraft.getMoneyDatabaseInterface().setBalance(vars[1], Double.parseDouble(vars[2]));
 								p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Account set!");
 								return true;
 							}
 
 							if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.admin.setxp")) && (Bankcraft.perms.has(p, "bankcraft.command.setxp") || Bankcraft.perms.has(p, "bankcraft.command.admin"))) {
 								bankcraft.getExperienceDatabaseInterface().setBalance(vars[1], Integer.parseInt(vars[2]));
 								p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "XP-Account set!");
 								return true;
 							}
 
 
 							if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.admin.grant")) && (Bankcraft.perms.has(p, "bankcraft.command.grant") || Bankcraft.perms.has(p, "bankcraft.command.admin"))) {
 								bankcraft.getMoneyDatabaseInterface().addToAccount(vars[1], Double.parseDouble(vars[2]));
 								p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Granted "+vars[2]+" Money to "+vars[1]+"!");
 								return true;
 							}
 
 							if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.admin.grantxp")) && (Bankcraft.perms.has(p, "bankcraft.command.grantxp") || Bankcraft.perms.has(p, "bankcraft.command.admin"))) {
 								bankcraft.getExperienceDatabaseInterface().addToAccount(vars[1], Integer.parseInt(vars[2]));
 								p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Granted "+vars[2]+" Experience to "+vars[1]+"!");
 								return true;
 							}
 						} else {
 							if (vars[0].equalsIgnoreCase(coHa.getString("signAndCommand.admin.databaseimport")) && (Bankcraft.perms.has(p, "bankcraft.command.databaseimport") || Bankcraft.perms.has(p, "bankcraft.command.admin"))) {
 								
 								p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Importing...");
 								
 								DatabaseManagerInterface loadDataMan = null;
 								AccountDatabaseInterface <Double> loadDataMoney = null;
 								AccountDatabaseInterface <Integer> loadDataXp = null;
 								SignDatabaseInterface loadDataSign = null;
 								
 								DatabaseManagerInterface saveDataMan = null;
 								AccountDatabaseInterface <Double> saveDataMoney = null;
 								AccountDatabaseInterface <Integer> saveDataXp = null;
 								SignDatabaseInterface saveDataSign = null;
 								
 								
 								if (vars[1].equalsIgnoreCase("flatfile")) {
 									//Load flatFile
 									p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Importing from flatfile...");
 									loadDataMan = new DatabaseManagerFlatFile(bankcraft);
 									loadDataMoney = new MoneyFlatFileInterface(bankcraft);
 									loadDataXp = new ExperienceFlatFileInterface(bankcraft);
 									loadDataSign = new SignFlatFileInterface(bankcraft);
 								}
 								
 								if (vars[1].equalsIgnoreCase("mysql")) {
 									//Load mysql
 									p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Importing from mysql...");
 									loadDataMan = new DatabaseManagerMysql(bankcraft);
 									loadDataMoney = new MoneyMysqlInterface(bankcraft);
 									loadDataXp = new ExperienceMysqlInterface(bankcraft);
 									loadDataSign = new SignMysqlInterface(bankcraft);
 								}
 								
 								if (vars[2].equalsIgnoreCase("flatfile")) {
 									//Load flatFile
 									p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Exporting to flatfile...");
 									saveDataMan = new DatabaseManagerFlatFile(bankcraft);
 									saveDataMoney = new MoneyFlatFileInterface(bankcraft);
 									saveDataXp = new ExperienceFlatFileInterface(bankcraft);
 									saveDataSign = new SignFlatFileInterface(bankcraft);
 								}
 								
 								if (vars[2].equalsIgnoreCase("mysql")) {
 									//Load mysql
 									p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Exporting to mysql...");
 									saveDataMan = new DatabaseManagerMysql(bankcraft);
 									saveDataMoney = new MoneyMysqlInterface(bankcraft);
 									saveDataXp = new ExperienceMysqlInterface(bankcraft);
 									saveDataSign = new SignMysqlInterface(bankcraft);
 								}
 								
 								//get them ready
 								loadDataMan.setupDatabase();
 								saveDataMan.setupDatabase();
 								
 								//move money data
 								for (String accountName: loadDataMoney.getAccounts()) {
 									saveDataMoney.setBalance(accountName, loadDataMoney.getBalance(accountName));
 								}
 								
 								//move xp data
 								for (String accountName: loadDataXp.getAccounts()) {
 									saveDataXp.setBalance(accountName, loadDataXp.getBalance(accountName));
 								}
 								
 								//move sign data
 								String amounts;
 								String[] amountsArray;
 								int type;
 								for (Location location: loadDataSign.getLocations(-1, null)) {
 									//Get amounts
 									amountsArray = loadDataSign.getAmounts((int)location.getX(), (int)location.getY(), (int)location.getZ(), location.getWorld());
 									amounts = amountsArray[0];
 									for (int i = 1; i< amountsArray.length; i++) {
 										amounts+=":"+amountsArray[i];
 									}
 									
 									//Get type
 									type = loadDataSign.getType((int)location.getX(), (int)location.getY(), (int)location.getZ(), location.getWorld());
 									
 									//Create new sign in save database
 									saveDataSign.createNewSign((int)location.getX(), (int)location.getY(), (int)location.getZ(), location.getWorld(), type, amounts);
 								}
 								
 								//close databases
 								loadDataMan.closeDatabase();
 								saveDataMan.closeDatabase();
 								
 								//Send success message
 								p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Moved all data from "+vars[1]+" to "+vars[2]+"!");
 								return true;
 							}
 						
 						}
 						
 					}
 					else {
 						p.sendMessage(ChatColor.RED + coHa.getString("chat.prefix") + "Wrong Syntax or missing permissions! Please see /bank help for more information!");
 					}
 					return true;
 				}
 			}
 		} else {
 			Bankcraft.log.info("[Bankcraft] Please use this ingame!");
 		}
 
 		return false;
 	}
 
 }
