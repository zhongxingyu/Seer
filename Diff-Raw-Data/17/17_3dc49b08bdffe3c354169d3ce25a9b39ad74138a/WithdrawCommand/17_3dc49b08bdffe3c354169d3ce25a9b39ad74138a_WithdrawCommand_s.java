 package musician101.itembank.commands.dwcommands;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import musician101.itembank.Config;
 import musician101.itembank.ItemBank;
 import musician101.itembank.exceptions.InvalidAliasException;
 import musician101.itembank.lib.Commands;
 import musician101.itembank.lib.Messages;
 import musician101.itembank.util.IBUtils;
 
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * The code used to run when the Withdraw command is executed.
  * 
  * @author Musician101
  */
 public class WithdrawCommand implements CommandExecutor
 {
 	ItemBank plugin;
 	Config config;
 	
 	/**
 	 * @param plugin References the plugin's main class.
 	 * @param config References the config options.
 	 */
 	public WithdrawCommand(ItemBank plugin, Config config)
 	{
 		this.plugin = plugin;
 		this.config = config;
 	}
 	
 	/**
 	 * @param sender Who sent the command.
 	 * @param command Which command was executed.
 	 * @param label Alias of the command.
 	 * @param args Command parameters.
 	 * @return True if the command was successfully executed.
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		if (command.getName().equalsIgnoreCase(Commands.WITHDRAW_CMD))
 		{
 			if (!sender.hasPermission(Commands.WITHDRAW_PERM))
 			{
 				sender.sendMessage(Messages.NO_PERMISSION);
 				return false;
 			}
 			
 			if (!(sender instanceof Player) && !args[0].equalsIgnoreCase(Commands.ADMIN_CMD))
 			{
 				sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
 				return false;
 			}
 			
 			if (args.length == 0)
 			{
 				sender.sendMessage(Messages.NOT_ENOUGH_ARGUMENTS);
 				return false;
 			}
 			
 			/** Admin Withdraw Check */
 			if (args[0].equalsIgnoreCase(Commands.ADMIN_CMD))
 				return Admin.withdraw(plugin, (Player) sender, args);
 			
 			/** Economy Check Check */
 			if (!IBUtils.checkEconomy(plugin, config, (Player) sender))
 			{
 				sender.sendMessage(Messages.LACK_MONEY);
 				return false;
 			}
 			
 			/** "Custom Item" Check */
 			if (args[0].equalsIgnoreCase(Commands.CUSTOM_ITEM_CMD))
 				return CustomItem.withdraw(plugin, (Player) sender, args);
 			
 			String name = args[0].toLowerCase();
 			int amount = 64;
 			if (args.length == 2)
 			{
 				try
 				{
 					amount = Integer.parseInt(args[1]);
 				}
 				catch (NumberFormatException e)
 				{
 					sender.sendMessage(Messages.NUMBER_FORMAT);
 					return false;
 				}
 			}
 			
 			ItemStack item = null;
 			try
 			{
 				item = IBUtils.getItemFromAlias(plugin, name, amount);
 			}
 			catch (InvalidAliasException e)
 			{
 				item = IBUtils.getItem(name, amount);
 			}
 			catch (NullPointerException e)
 			{
 				sender.sendMessage(Messages.NULL_POINTER);
 				return false;
 			}
 			
 			if (item == null)
 			{
 				sender.sendMessage(Messages.getAliasError(name));
 				return false;
 			}
 			
 			if (item.getType() == Material.AIR)
 			{
 				sender.sendMessage(Messages.AIR_BLOCK);
 				return false;
 			}
 			
 			String itemPath = item.getType().toString().toLowerCase() + "." + item.getDurability();
 			plugin.playerFile = new File(plugin.playerDataDir + "/" + sender.getName().toLowerCase() + ".yml");
 			plugin.playerData = new YamlConfiguration();
 			try
 			{
 				plugin.playerData.load(plugin.playerFile);
 			}
 			catch (FileNotFoundException e)
 			{
 				sender.sendMessage(Messages.FILE_NOT_FOUND);
 				return false;
 			}
 			catch (IOException e)
 			{
 				sender.sendMessage(Messages.IO_EXCEPTION);
 				return false;
 			}
 			catch (InvalidConfigurationException e)
 			{
 				sender.sendMessage(Messages.YAML_EXCEPTION);
 				return false;
 			}
 			
 			int oldAmount = plugin.playerData.getInt(itemPath);
 			if (amount > oldAmount)
 				amount = oldAmount;
 			
 			int freeSpace = 0;
 			for (ItemStack is : ((Player) sender).getInventory())
 			{
 				if (is == null)
 					freeSpace += item.getType().getMaxStackSize();
 				else if (is.getType() == item.getType())
 					freeSpace += is.getType().getMaxStackSize() - is.getAmount();
 			}
 			
 			if (freeSpace == 0)
 			{
 				sender.sendMessage(Messages.FULL_INV);
 				return false;
 			}
 			
 			if (amount > freeSpace)
 				amount = freeSpace;
 			
 			int newAmount = oldAmount - amount;
 			plugin.playerData.set(itemPath, newAmount);
 			try
 			{
 				plugin.playerData.save(plugin.playerFile);
 			}
 			catch (IOException e)
 			{
 				sender.sendMessage(Messages.IO_EXCEPTION);
 				plugin.playerData.set(itemPath, oldAmount);
 				if (plugin.getEconomy().isEnabled() && config.enableVault)
 					plugin.getEconomy().giveMoney(sender.getName(), config.transactionCost);
 				return false;
 			}
 			
 			item.setAmount(amount);
 			((Player) sender).getInventory().addItem(item);
 			sender.sendMessage(Messages.PREFIX + "You have withdrawn " + amount + " " + item.getType().toString() + " and now have a total of " + newAmount + " left.");
 			if (plugin.getEconomy().isEnabled() && config.enableVault)
 				sender.sendMessage(Messages.PREFIX + "A " + config.transactionCost + " transaction fee has been deducted from your account.");
 			
 			return true;
 		}
 		return false;
 	}
 }
