 package mveritym.cashflow;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.ListIterator;
 
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SalaryManager {
 
 	protected CashFlow cashFlow;
 	protected Config conf;
 	protected File confFile;
 	List<String> salaries;
 	List<String> paidGroups;
 	List<String> paidPlayers;
 	ListIterator<String> iterator;
 	final Collection<Taxer> salaryTasks = new ArrayList<Taxer>();
 
 	public SalaryManager(CashFlow cashFlow) {
 		this.cashFlow = cashFlow;
 		conf = cashFlow.getPluginConfig();
 		salaries = conf.getStringList("salaries.list");
 	}
 
 	public void createSalary(CommandSender sender, String name,
 			String paycheck, String interval, String employer) {
 		conf.save();
 		String salaryName = name;
 		double salary = Double.parseDouble(paycheck);
 		double salaryInterval = Double.parseDouble(interval);
 		List<String> paidGroups = null;
 
 		salaries = conf.getStringList("salaries.list");
 		iterator = salaries.listIterator();
 
 		if (salary <= 0)
 		{
 			sender.sendMessage(ChatColor.RED
 					+ "Please choose a salary greater than 0.");
 			return;
 		}
 		else if (salaryInterval <= 0)
 		{
 			sender.sendMessage(ChatColor.RED
 					+ "Please choose a salary interval greater than 0.");
 			return;
 		}
 		else if ((this.cashFlow.eco.bankBalance(employer).type) == EconomyResponse.ResponseType.FAILURE
 				&& !(employer.equals("null")))
 		{
 			sender.sendMessage(ChatColor.RED + "Player not found.");
 			return;
 		}
 		else
 		{
 			while (iterator.hasNext())
 			{
 				if (iterator.next().equals(salaryName))
 				{
 					sender.sendMessage(ChatColor.RED
 							+ "A salary with that name has already been created.");
 					return;
 				}
 			}
 		}
 
 		salaries.add(salaryName);
 		conf.setProperty("salaries.list", salaries);
 		conf.setProperty("salaries." + salaryName + ".salary", paycheck);
 		conf.setProperty("salaries." + salaryName + ".salaryInterval",
 				salaryInterval);
 		conf.setProperty("salaries." + salaryName + ".employer", employer);
 		conf.setProperty("salaries." + salaryName + ".paidGroups", paidGroups);
 		conf.setProperty("salaries." + salaryName + ".paidPlayers", paidPlayers);
 		conf.setProperty("salaries." + salaryName + ".lastPaid", null);
 		conf.setProperty("salaries." + salaryName + ".exceptedPlayers", null);
 		conf.setProperty("salaries." + salaryName + ".onlineOnly.isEnabled",
 				false);
 		conf.setProperty("salaries." + salaryName + ".onlineOnly.interval", 0.0);
 		conf.save();
 
 		sender.sendMessage(ChatColor.GREEN + "New salary " + salaryName
 				+ " created successfully.");
 	}
 
 	public void deleteSalary(CommandSender sender, String name) {
 		conf.save();
 		String salaryName = name;
 
 		salaries = conf.getStringList("salaries.list");
 
 		if (salaries.contains(salaryName))
 		{
 			salaries.remove(salaryName);
 
 			for (Taxer task : salaryTasks)
 			{
 				if (task.getName().equals(name))
 				{
 					task.cancel();
 				}
 			}
 
 			conf.setProperty("salaries.list", salaries);
 			conf.removeProperty("salaries." + salaryName);
 			conf.save();
 
 			sender.sendMessage(ChatColor.GREEN + "Salary " + salaryName
 					+ " deleted successfully.");
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "No salary, " + salaryName);
 		}
 
 		return;
 	}
 
 	public void salaryInfo(CommandSender sender, String salaryName) {
 		conf.save();
 		salaries = conf.getStringList("salaries.list");
 
 		if (salaries.contains(salaryName))
 		{
 			sender.sendMessage(ChatColor.BLUE + "Salary: "
 					+ conf.getProperty("salaries." + salaryName + ".salary"));
 			sender.sendMessage(ChatColor.BLUE
 					+ "Interval: "
 					+ conf.getProperty("salaries." + salaryName
 							+ ".salaryInterval") + " hours");
 			sender.sendMessage(ChatColor.BLUE + "Receiving player: "
 					+ conf.getString("salaries." + salaryName + ".employer"));
 			sender.sendMessage(ChatColor.BLUE
 					+ "Paid groups: "
 					+ conf.getStringList("salaries." + salaryName
 							+ ".paidGroups"));
 			sender.sendMessage(ChatColor.BLUE
 					+ "Paid players: "
 					+ conf.getStringList("salaries." + salaryName
 							+ ".paidPlayers"));
 			sender.sendMessage(ChatColor.BLUE
 					+ "Excepted users: "
 					+ conf.getStringList("salaries." + salaryName
 							+ ".exceptedPlayers"));
 			sender.sendMessage(ChatColor.BLUE
 					+ "Online only: "
 					+ conf.getBoolean("salaries." + salaryName
 							+ ".onlineOnly.isEnabled", false)
 					+ ", Online interval: "
 					+ conf.getDouble("salaries." + salaryName
 							+ ".onlineOnly.interval", 0.0) + " hours");
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "No salary, " + salaryName
 					+ ", found.");
 		}
 
 		return;
 	}
 
 	public void listSalaries(CommandSender sender) {
 		conf.save();
 		salaries = conf.getStringList("salaries.list");
 		iterator = salaries.listIterator();
 
 		if (salaries.size() != 0)
 		{
 			while (iterator.hasNext())
 			{
 				sender.sendMessage(ChatColor.BLUE + iterator.next());
 			}
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "No salaries to list.");
 		}
 	}
 
 	public void addGroups(CommandSender sender, String taxName, String groups) {
 		conf.save();
 		String[] groupNames = groups.split(",");
 		for (String name : groupNames)
 		{
 			addGroup(sender, taxName, name);
 		}
 	}
 
 	public void addGroup(CommandSender sender, String salaryName,
 			String groupName) {
 		conf.save();
 		salaries = conf.getStringList("salaries.list");
 		paidGroups = conf.getStringList("salaries." + salaryName
 				+ ".paidGroups");
 
 		if (!(salaries.contains(salaryName)))
 		{
 			sender.sendMessage(ChatColor.RED + "Salary not found.");
 		}
 		else if (!(this.cashFlow.permsManager.isGroup(groupName)))
 		{
 			sender.sendMessage(ChatColor.RED + "Group not found.");
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.GOLD + salaryName + ChatColor.GREEN
 					+ " applied successfully to " + ChatColor.GRAY  + groupName);
 			paidGroups.add(groupName);
 			conf.setProperty("salaries." + salaryName + ".paidGroups",
 					paidGroups);
 			conf.save();
 		}
 
 		return;
 	}
 
 	public void addPlayers(CommandSender sender, String taxName, String players) {
 		conf.save();
 		String[] playerNames = players.split(",");
 		for (String name : playerNames)
 		{
 			addPlayer(sender, taxName, name);
 		}
 	}
 
 	public void addPlayer(CommandSender sender, String salaryName,
 			String playerName) {
 		conf.save();
 		salaries = conf.getStringList("salaries.list");
 		paidPlayers = conf.getStringList("salaries." + salaryName
 				+ ".paidPlayers");
 
 		if (!(salaries.contains(salaryName)))
 		{
 			sender.sendMessage(ChatColor.RED + "Salary not found.");
 		}
 		else if (!(this.cashFlow.permsManager
 				.isPlayer(playerName.toLowerCase())))
 		{
 			sender.sendMessage(ChatColor.RED + "Player not found.");
 		}
 		else if (paidPlayers.contains(playerName.toLowerCase()))
 		{
 			sender.sendMessage(ChatColor.RED + playerName
 					+ " is already paying this tax.");
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.GOLD + salaryName + ChatColor.GREEN
 					+ " applied successfully to " + ChatColor.GRAY + playerName);
 			paidPlayers.add(playerName);
 			conf.setProperty("salaries." + salaryName + ".paidPlayers",
 					paidPlayers);
 			conf.save();
 		}
 
 		return;
 	}
 
 	public void removeGroups(CommandSender sender, String taxName, String groups) {
 		conf.save();
 		String[] groupNames = groups.split(",");
 		for (String name : groupNames)
 		{
 			removeGroup(sender, taxName, name);
 		}
 	}
 
 	public void removeGroup(CommandSender sender, String salaryName,
 			String groupName) {
 		conf.save();
 		salaries = conf.getStringList("salaries.list");
 		paidGroups = conf.getStringList("salaries." + salaryName
 				+ ".paidGroups");
 
 		if (!(salaries.contains(salaryName)))
 		{
 			sender.sendMessage(ChatColor.RED + "Salary not found.");
 		}
 		else if (!(paidGroups.contains(groupName)))
 		{
 			sender.sendMessage(ChatColor.RED + "Group not found.");
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.GOLD + salaryName + ChatColor.GREEN
 					+ " removed successfully from " + ChatColor.GRAY + groupName);
 			paidGroups.remove(groupName);
 			conf.setProperty("salaries." + salaryName + ".paidGroups",
 					paidGroups);
 			conf.save();
 		}
 
 		return;
 	}
 
 	public void removePlayers(CommandSender sender, String salaryName,
 			String players) {
 		conf.save();
 		String[] playerNames = players.split(",");
 		for (String name : playerNames)
 		{
 			removePlayer(sender, salaryName, name);
 		}
 	}
 
 	public void removePlayer(CommandSender sender, String salaryName,
 			String playerName) {
 		conf.save();
 		salaries = conf.getStringList("salaries.list");
 		paidPlayers = conf.getStringList("salaries." + salaryName
 				+ ".paidPlayers");
 
 		if (!(salaries.contains(salaryName)))
 		{
 			sender.sendMessage(ChatColor.RED + "Salary not found.");
 		}
 		else if (!(paidPlayers.contains(playerName)))
 		{
 			sender.sendMessage(ChatColor.RED + "Player not found.");
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.GOLD + salaryName + ChatColor.GREEN
 					+ " removed successfully from " + ChatColor.GRAY + playerName);
 			paidPlayers.remove(playerName);
 			conf.setProperty("salaries." + salaryName + ".paidPlayers",
 					paidPlayers);
 			conf.save();
 		}
 	}
 
 	public void enable() {
 		conf.save();
 		salaries = conf.getStringList("salaries.list");
 
 		for (String salaryName : salaries)
 		{
 
 			enableSalary(salaryName);
 		}
 	}
 
 	public void enableSalary(String salary)
 	{
 		boolean has = false;
 		for(Taxer t : salaryTasks.toArray(new Taxer[0]))
 		{
 			if(t.getName().equals(salary))
 			{
 				t.start();
 				has = true;
 			}
 		}
 		if(!has)
 		{
 			Double hours = conf.getDouble(
 					("salaries." + salary + ".salaryInterval"), 1);
 			this.cashFlow.log.info(this.cashFlow.prefix
 					+ " Enabling " + salary);
 			Taxer taxer = new Taxer(this, salary, hours);
 			salaryTasks.add(taxer);
 		}
 	}
 
 	public void disableSalary(String salary)
 	{
 		for(Taxer t : this.salaryTasks)
 		{
 			if(t.getName().equals(salary))
 			{
 				t.cancel();
 			}
 		}
 	}
 
 	public void disable() {
 		for (Taxer salaryTask : salaryTasks)
 		{
 			salaryTask.cancel();
 		}
 	}
 
 	public void setOnlineOnly(String salaryName, Boolean online, Double interval) {
 		conf.save();
 		conf.setProperty("salaries." + salaryName + ".onlineOnly.isEnabled",
 				online);
 		conf.setProperty("salaries." + salaryName + ".onlineOnly.interval",
 				interval);
 		conf.save();
 		return;
 	}
 
 	public void addException(CommandSender sender, String salaryName,
 			String userName) {
 		conf.save();
 		salaries = conf.getStringList("salaries.list");
 		List<String> exceptedPlayers = conf.getStringList("salaries."
 				+ salaryName + ".exceptedPlayers");
 
 		if (!(salaries.contains(salaryName)))
 		{
 			sender.sendMessage(ChatColor.RED + "Salary not found.");
 		}
 		else if (salaries.contains(userName))
 		{
 			sender.sendMessage(ChatColor.RED + userName
 					+ " is already listed as excepted.");
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.GREEN + userName
 					+ " added as an exception.");
 			exceptedPlayers.add(userName);
 			conf.setProperty("salaries." + salaryName + ".exceptedPlayers",
 					exceptedPlayers);
 			conf.save();
 		}
 
 		return;
 	}
 
 	public void removeException(CommandSender sender, String salaryName,
 			String userName) {
 		salaries = conf.getStringList("salaries.list");
 		List<String> exceptedPlayers = conf.getStringList("salaries."
 				+ salaryName + ".exceptedPlayers");
 
 		if (!(salaries.contains(salaryName)))
 		{
 			sender.sendMessage(ChatColor.RED + "Salary not found.");
 		}
 		else if (!(exceptedPlayers.contains(userName)))
 		{
 			sender.sendMessage(ChatColor.RED + "Player not found.");
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.GREEN + userName
 					+ " removed as an exception.");
 			exceptedPlayers.remove(userName);
 			conf.setProperty("salaries." + salaryName + ".exceptedPlayers",
 					exceptedPlayers);
 			conf.save();
 		}
 	}
 
 	public List<String> checkOnline(List<String> users, Double interval) {
 		List<String> tempPlayerList = new ArrayList<String>();
 		for (String player : users)
 		{
 			if (this.cashFlow.getServer().getPlayer(player) != null)
 			{
 				tempPlayerList.add(player);
 			}
 		}
 		return tempPlayerList;
 	}
 
 	public List<String> getUsers(String salaryName) {
 		List<String> groups = conf.getStringList("salaries." + salaryName
 				+ ".paidGroups");
 		List<String> players = conf.getStringList("salaries." + salaryName
 				+ ".paidPlayers");
 		List<String> exceptedPlayers = conf.getStringList("salaries."
 				+ salaryName + ".exceptedPlayers");
 
 		return this.cashFlow.permsManager.getUsers(groups, players,
 				exceptedPlayers);
 	}
 
 	public void paySalary(String salaryName) {
 		conf.save();
 
 		List<String> users = this.getUsers(salaryName);
 
 		if (conf.getBoolean("salaries." + salaryName + ".onlineOnly.isEnabled",
 				false))
 		{
 			Double onlineInterval = conf.getDouble("salaries." + salaryName
 					+ ".onlineOnly.interval", 0);
 			users = checkOnline(users, onlineInterval);
 		}
 
 		for (String user : users)
 		{
 			Buffer.getInstance().addToBuffer(user, salaryName, false);
 		}
 	}
 
 	public void paySalaryToUser(String user, String salaryName) {
 		Double salary = Double.parseDouble(conf.getString("salaries."
 				+ salaryName + ".salary"));
 		String employer = conf
 				.getString("salaries." + salaryName + ".employer");
 		boolean ico5 = false;
		if(this.cashFlow.eco.getName().equals("iConomy 5") || this.cashFlow.eco.getName().equals("Essentials Economy") || this.cashFlow.eco.getName().equals("BOSEconomy"))
 		{
 			ico5 = true;
 		}
 		EconomyResponse er = new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "EconomyResponse object not initialized.");
 		if(ico5)
 		{
 			er = this.cashFlow.eco.depositPlayer(user, 0);
 		}
 		else
 		{
 			er = this.cashFlow.eco.bankBalance(user);
 		}
 		if (er.type == EconomyResponse.ResponseType.SUCCESS)
 		{
 			final Player p = this.cashFlow.getServer().getPlayer(user);
 			final Player r = this.cashFlow.getServer().getPlayer(employer);
 			// pay salary
 			if (!employer.equals("null"))
 			{
 				double tempSalary = 0;
 				if(ico5)
 				{
					tempSalary = this.cashFlow.eco.getBalance(employer);
 				}
 				else
 				{
 					tempSalary = this.cashFlow.eco.bankBalance(employer).amount;
 				}
 				if (tempSalary != 0)
 				{
 					if (tempSalary > salary)
 					{
 						tempSalary = salary;
 					}
 					if(ico5)
 					{
 						er = this.cashFlow.eco.withdrawPlayer(employer, tempSalary);
 					}
 					else
 					{
 						er = this.cashFlow.eco.bankWithdraw(employer, tempSalary);
 					}
 
 					if (er.type == EconomyResponse.ResponseType.SUCCESS)
 					{
 						if (r != null)
 						{
 							r.sendMessage(ChatColor.BLUE + "You have paid $"
 									+ tempSalary + " in salary to " + user
 									+ ".");
 						}
 						if(ico5)
 						{
 							er = this.cashFlow.eco.depositPlayer(user, tempSalary);
 						}
 						else
 						{
 							er = this.cashFlow.eco.bankDeposit(user, tempSalary);
 						}
 						if (er.type == EconomyResponse.ResponseType.SUCCESS)
 						{
 							if (p != null)
 							{
 								p.sendMessage(ChatColor.BLUE
 										+ "You have received $" + tempSalary
 										+ " for your salary from " + employer
 										+ ".");
 							}
 						}
 						else
 						{
 							if (p != null)
 							{
 								p.sendMessage(ChatColor.RED
 										+ "Could not give salary.");
 							}
 							this.cashFlow.log.warning(this.cashFlow.prefix
 									+ " " + er.errorMessage + ": " + user);
 						}
 					}
 					else
 					{
 						if (p != null)
 						{
 							p.sendMessage(ChatColor.RED
 									+ "Could not get salary.");
 						}
 						if (r != null)
 						{
 							r.sendMessage(ChatColor.RED
 									+ "Could not get salary.");
 						}
 						this.cashFlow.log.warning(this.cashFlow.prefix + " "
 								+ er.errorMessage + ": " + employer);
 					}
 				}
 				else
 				{
 					if (p != null)
 					{
 						p.sendMessage(ChatColor.BLUE
 								+ employer
 								+ " does not have enough money to pay your salary.");
 					}
 				}
 			}
 			else
 			{
 				if(ico5)
 				{
 					er = this.cashFlow.eco.depositPlayer(user, salary);
 				}
 				else
 				{
 					er = this.cashFlow.eco.bankDeposit(user, salary);
 				}
 				if (er.type == EconomyResponse.ResponseType.SUCCESS)
 				{
 					if (p != null)
 					{
 						p.sendMessage(ChatColor.BLUE + "You have received $"
 								+ salary + " for your salary.");
 					}
 				}
 				else
 				{
 					if (p != null)
 					{
 						p.sendMessage(ChatColor.RED + "Could not give salary.");
 					}
 					this.cashFlow.log.warning(this.cashFlow.prefix + " "
 							+ er.errorMessage + ": " + user);
 				}
 			}
 		}
 		else
 		{
 			// Acount does not exist
 			/*
 			 * SalaryManager.cashFlow.log.warning("[" +
 			 * SalaryManager.cashFlow.info.getName() + "] " +
 			 * SalaryManager.cashFlow.eco.bankBalance(user).errorMessage +": " +
 			 * user);
 			 */
 		}
 	}
 
 	public void setRate(CommandSender sender, String salaryName, String salary) {
 		salaries = conf.getStringList("salaries.list");
 		Double rate = Double.parseDouble(salary);
 
 		if (!(salaries.contains(salaryName)))
 		{
 			sender.sendMessage(ChatColor.RED + "Salary not found.");
 		}
 		else if (rate <= 0)
 		{
 			sender.sendMessage(ChatColor.RED
 					+ "Please choose a salary greater than 0.");
 		}
 		else
 		{
 			conf.setProperty("salaries." + salaryName + ".salary", salary);
 			conf.save();
 			sender.sendMessage(ChatColor.GREEN + "Rate of salary " + salaryName
 					+ " is set to " + salary + ".");
 		}
 	}
 }
