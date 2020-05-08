 package mveritym.cashflow;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 public class SalaryCommand implements CommandExecutor {
 	private CashFlow cashFlow;
 	private SalaryManager salaryManager;
 	private PermissionsManager permsManager;
 
 	public SalaryCommand(CashFlow plugin, PermissionsManager perm,
 			SalaryManager salary) {
 		cashFlow = plugin;
 		salaryManager = salary;
 		permsManager = perm;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		boolean playerCanDo = false;
 		boolean isConsole = false;
 		if (sender instanceof Player)
 		{
 			Player player = (Player) sender;
 			String node = "cashflow." + command.getName();
 			if (player.isOp() || permsManager.hasPermission(player, node))
 			{
 				playerCanDo = true;
 			}
 		}
 		else if (sender instanceof ConsoleCommandSender)
 		{
 			isConsole = true;
 		}
 		if (!playerCanDo && !isConsole)
 		{
 			sender.sendMessage(ChatColor.RED + cashFlow.prefix
 					+ " Lack permission: cashflow." + command.getName());
 			return true;
 		}
 		if (args.length == 0)
 		{
 			// No arguments given
 			showHelp(sender);
 		}
 		else
 		{
 			try
 			{
 				final CashFlowCommands com = CashFlowCommands.valueOf(args[0]
 						.toLowerCase());
 				switch (com)
 				{
 					case create:
 						String name;
 						String salary;
 						String interval;
 						String employer;
 
 						if (args.length == 5)
 						{
 							name = args[1];
 							salary = args[2];
 							interval = args[3];
 							employer = args[4];
 
 							this.salaryManager.createSalary(sender, name,
 									salary, interval, employer);
 						}
 						else if (args.length == 4)
 						{
 							try
 							{
 								@SuppressWarnings ("unused")
 								double testDouble = Double.parseDouble(args[3]);
 								name = args[1];
 								salary = args[2];
 								interval = args[3];
 								employer = "null";
 
 								this.salaryManager.createSalary(sender, name,
 										salary, interval, employer);
 							}
 							catch (Exception e)
 							{
 								sender.sendMessage(ChatColor.RED
 										+ cashFlow.prefix
 										+ " Incorrect number of arguments.");
 							}
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " Incorrect number of arguments.");
 						}
 						break;
 					case delete:
						if (args.length == 1)
 						{
							this.salaryManager.deleteSalary(sender, args[0]);
 						}
						else if (args.length > 1)
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " Too many arguments.");
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " Not enough arguments.");
 						}
 						break;
 					case apply:
 						if (args.length >= 4)
 						{
 							if (args[1].equals("group"))
 							{
 								if (this.cashFlow.permsManager.pluginDetected())
 								{
 									this.salaryManager.addGroups(sender,
 											args[2], args[3]);
 								}
 								else
 								{
 									sender.sendMessage(ChatColor.RED
 											+ " You must install a permissions plugin to use this command.");
 								}
 							}
 							else if (args[1].equals("player"))
 							{
 								this.salaryManager.addPlayers(sender, args[2],
 										args[3]);
 							}
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED
 									+ " Incorrect number of arguments.");
 						}
 						break;
 					case remove:
 						if (args.length >= 4)
 						{
 							if (args[1].equals("group"))
 							{
 								if (this.cashFlow.permsManager.pluginDetected())
 								{
 									this.salaryManager.removeGroups(sender,
 											args[2], args[3]);
 								}
 								else
 								{
 									sender.sendMessage(ChatColor.RED
 											+ cashFlow.prefix
 											+ " You must install a permissions plugin to use this command.");
 								}
 							}
 							else if (args[1].equals("player"))
 							{
 								this.salaryManager.removePlayers(sender,
 										args[2], args[3]);
 							}
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ "Incorrect argument.");
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " Incorrect number of arguments.");
 						}
 						break;
 					case addexception:
 						if (args.length == 3)
 						{
 							this.salaryManager.addException(sender, args[1],
 									args[2]);
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " Incorrect number of arguments.");
 						}
 						break;
 					case removeexception:
 						if (args.length == 3)
 						{
 							this.salaryManager.removeException(sender, args[1],
 									args[2]);
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " Incorrect number of arguments.");
 						}
 						break;
 					case list:
 						this.salaryManager.listSalaries(sender);
 						break;
 					case info:
						if (args.length == 1)
 						{
							this.salaryManager.salaryInfo(sender, args[0]);
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " Incorrect number of arguments.");
 						}
 						break;
 					case setonlineonly:
 						if (args.length == 3 || args.length == 4)
 						{
 							final String booleanString = args[2].toLowerCase();
 							if (booleanString.equals("true")
 									|| booleanString.equals("false"))
 							{
 								Double onlineInterval = 0.0;
 								if (args.length == 4)
 								{
 									onlineInterval = Math.abs(Double
 											.parseDouble(args[3]));
 								}
 								this.salaryManager.setOnlineOnly(args[1],
 										Boolean.parseBoolean(booleanString),
 										onlineInterval);
 								sender.sendMessage(ChatColor.GREEN
 										+ cashFlow.prefix
 										+ "Online only set to " + ChatColor.GRAY+ args[1]);
 							}
 							else
 							{
 								sender.sendMessage(ChatColor.RED
 										+ cashFlow.prefix
 										+ "Online only can only be set to true or false.");
 							}
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " Incorrect number of arguments.");
 						}
 						break;
 					case setrate:
 						if (args.length == 3)
 						{
 							String salaryName = args[1];
 							String salaryrate = args[2];
 							this.salaryManager.setRate(sender, salaryName,
 									salaryrate);
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " Incorrect number of arguments.");
 						}
 						break;
 					case fire:
 						if (args.length == 2)
 						{
 							String salaryName = args[1];
 							if (cashFlow.getPluginConfig()
 									.getStringList("salaries.list")
 									.contains(salaryName))
 							{
 								this.cashFlow.log.info(this.cashFlow.prefix
 										+ " Paying salary " + salaryName);
 								this.salaryManager.paySalary(salaryName);
 							}
 							else
 							{
 								sender.sendMessage(ChatColor.RED
 										+ cashFlow.prefix
 										+ " Invalid salary name: " + salaryName);
 							}
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " No salary name given");
 						}
 						break;
 					case enable:
 						if (args.length == 2)
 						{
 							String salaryName = args[1];
 							if (cashFlow.getPluginConfig()
 									.getStringList("salaries.list")
 									.contains(salaryName))
 							{
 								sender.sendMessage(ChatColor.GREEN
 										+ cashFlow.prefix
 										+ " Enabling salary - " + ChatColor.GOLD
 										+ salaryName);
 								this.salaryManager.enableSalary(salaryName);
 							}
 							else
 							{
 								sender.sendMessage(ChatColor.RED
 										+ cashFlow.prefix
 										+ " Invalid salary name: " + salaryName);
 							}
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " No tax name given");
 						}
 						break;
 					case disable:
 						if (args.length == 2)
 						{
 							String salaryName = args[1];
 							if (cashFlow.getPluginConfig()
 									.getStringList("salaries.list")
 									.contains(salaryName))
 							{
 
 								sender.sendMessage(ChatColor.YELLOW
 										+ cashFlow.prefix
 										+ " Disabling salary - "
 										+ ChatColor.AQUA + salaryName);
 								this.salaryManager.disableSalary(salaryName);
 							}
 							else
 							{
 								sender.sendMessage(ChatColor.RED
 										+ cashFlow.prefix
 										+ " Invalid salary name: " + salaryName);
 							}
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.RED + cashFlow.prefix
 									+ " No salary name given");
 						}
 						break;
 					default:
 						showHelp(sender);
 						break;
 				}
 			}
 			catch (IllegalArgumentException e)
 			{
 				sender.sendMessage(ChatColor.RED + cashFlow.prefix
						+ " Syntax error. For help, use /tax");
 			}
 		}
 		return true;
 	}
 
 	private void showHelp(CommandSender sender) {
 		sender.sendMessage(ChatColor.GREEN + "=====" + ChatColor.WHITE
 				+ "Salary Help" + ChatColor.GREEN + "=====");
 		sender.sendMessage(ChatColor.AQUA + "/salary " + ChatColor.YELLOW
 				+ "create <salaryname> <salary> <interval> "
 				+ ChatColor.LIGHT_PURPLE + "[employer] " + ChatColor.GREEN
 				+ "- Creates a salary.");
 		sender.sendMessage(ChatColor.AQUA + "/salary " + ChatColor.YELLOW
 				+ "delete <salaryname> " + ChatColor.GREEN
 				+ "- Deletes a salary.");
 		sender.sendMessage(ChatColor.AQUA
 				+ "/salary "
 				+ ChatColor.YELLOW
 				+ "<apply/remove> <group/player> <salaryname> <group/player names> "
 				+ ChatColor.GREEN
 				+ "- Applies a salary to group(s) or player(s). For multiple entries, use commas.");
 		sender.sendMessage(ChatColor.AQUA + "/salary " + ChatColor.YELLOW
 				+ "addexception <salaryname> <playername> " + ChatColor.GREEN
 				+ "- Adds a player as an exception. Case sensitive.");
 		sender.sendMessage(ChatColor.AQUA + "/salary " + ChatColor.YELLOW
 				+ "removeexception <salaryname> <playername>" + ChatColor.GREEN
 				+ "- Removes a player as an exception. Case sensitive.");
 		sender.sendMessage(ChatColor.AQUA + "/salary " + ChatColor.YELLOW
 				+ "list" + ChatColor.GREEN + "- Lists all salaries.");
 		sender.sendMessage(ChatColor.AQUA + "/salary " + ChatColor.YELLOW
 				+ "info <salaryname> " + ChatColor.GREEN
 				+ "- Lists info on given salary.");
 		sender.sendMessage(ChatColor.AQUA
 				+ "/salary "
 				+ ChatColor.YELLOW
 				+ "setonlineonly <salaryname> <true/false> [interval]"
 				+ ChatColor.GREEN
 				+ "- Sets specified salary to be collected only for players online in the given hourly interval.");
 		sender.sendMessage(ChatColor.AQUA + "/salary " + ChatColor.YELLOW
 				+ "setrate <salaryname> <salary>" + ChatColor.GREEN
 				+ "- Sets the salary rate.");
 		sender.sendMessage(ChatColor.AQUA + "/salary " + ChatColor.YELLOW
 				+ "<enable/disable> <salaryname> " + ChatColor.GREEN
 				+ "- Enable or disable a salary.");
 	}
 }
