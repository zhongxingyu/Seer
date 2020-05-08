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
  * The code used to run when the Deposit command is executed.
  * 
  * @author Musician101
  */
 public class DepositCommand implements CommandExecutor
 {
 	ItemBank plugin;
 	Config config;
 	
 	/**
 	 * @param plugin References the plugin's main class.
 	 * @param config References the config options.
 	 */
 	public DepositCommand(ItemBank plugin, Config config)
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
 		if (command.getName().equalsIgnoreCase(Commands.DEPOSIT_CMD))
 		{
 			if (!sender.hasPermission(Commands.DEPOSIT_PERM))
 			{
 				sender.sendMessage(Messages.NO_PERMISSION);
 				return false;
 			}
 			
 			if (!(sender instanceof Player) && !args[0].equalsIgnoreCase(Commands.ADMIN_CMD))
 			{
 				sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
 				return false;
 			}
 			
 			/** Standard Check (w/o arguments) */
 			if (args.length == 0)
 				return execute((Player) sender, ((Player) sender).getItemInHand());
 			
 			/** Admin Deposit Check */
 			if (args[0].equalsIgnoreCase(Commands.ADMIN_CMD))
 				return Admin.deposit(plugin, (Player) sender, args);
 			
 			/** Economy Check */
 			if (!IBUtils.checkEconomy(plugin, config, (Player) sender))
 			{
 				sender.sendMessage(Messages.LACK_MONEY);
 				return false;
 			}
 			
 			/** Custom Item Check */
 			if (args[0].equalsIgnoreCase(Commands.CUSTOM_ITEM_CMD))
 			{
 				ItemStack item = ((Player) sender).getItemInHand();
 				if (args.length < 0)
 				{
 					for (ItemStack itemStack : ((Player) sender).getInventory())
 						if (itemStack.hasItemMeta())
 							if (itemStack.getItemMeta().hasDisplayName())
 								if (itemStack.getItemMeta().getDisplayName() == args[1])
 									item = itemStack;
 					
 					if (item == ((Player) sender).getItemInHand())
 					{
 						sender.sendMessage(Messages.ITEM_NOT_FOUND);
 						return false;
 					}
 				}
 				
 				return CustomItem.deposit(plugin, ((Player) sender).getItemInHand(), (Player) sender);
 			}
 			
 			/** Standard Check */
 			execute((Player) sender, args);
 			
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean execute(Player player, ItemStack item)
 	{
 		return execute(player, new String[]{item.getType().toString(), String.valueOf(item.getAmount())});
 	}
 	
 	public boolean execute(Player player, String[] args)
 	{
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
 				player.sendMessage(Messages.NUMBER_FORMAT);
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
 			player.sendMessage(Messages.NULL_POINTER);
 			return false;
 		}
 		
 		if (item == null)
 		{
 			player.sendMessage(Messages.getAliasError(name));
 			return false;
 		}
 		
 		if (item.getType() == Material.AIR)
 		{
 			player.sendMessage(Messages.AIR_BLOCK);
 			return false;
 		}
 		
 		String itemPath = item.getType().toString().toLowerCase() + "." + item.getDurability();
 		plugin.playerFile = new File(plugin.playerDataDir + "/" + player.getName().toLowerCase() + ".yml");
 		plugin.playerData = new YamlConfiguration();
 		try
 		{
 			plugin.playerData.load(plugin.playerFile);
 		}
 		catch (FileNotFoundException e)
 		{
 			player.sendMessage(Messages.FILE_NOT_FOUND);
 			return false;
 		}
 		catch (IOException e)
 		{
 			player.sendMessage(Messages.IO_EXCEPTION);
 			return false;
 		}
 		catch (InvalidConfigurationException e)
 		{
 			player.sendMessage(Messages.YAML_EXCEPTION);
 			return false;
 		}
 		
 		if (!player.getInventory().containsAtLeast(item, item.getAmount()))
 		{
 			player.sendMessage(Messages.ITEM_NOT_FOUND);
 			return false;
 		}
 		
 		int oldAmount = plugin.playerData.getInt(itemPath);
 		int amountInInv = IBUtils.getAmount(player, item.getType(), item.getDurability());
 		if (amountInInv < amount)
 			amount = amountInInv;
 		
 		int newAmount = oldAmount + amount;
 		if (config.blacklist.isSet(itemPath))
 		{
 			int maxAmount = config.blacklist.getInt(itemPath);
 			if (maxAmount == 0)
 			{
 				player.sendMessage(Messages.NO_DEPOSIT);
 				return false;
 			}
 			else if (maxAmount == oldAmount)
 			{
 				player.sendMessage(Messages.getMaxedDepositMessage(item.getType().toString()));
 				return false;
 			}
 			else if (maxAmount < newAmount)
 			{
 				amount = maxAmount - oldAmount;
 				newAmount = oldAmount + amount;
 				player.sendMessage(Messages.PARTIAL_DEPOSIT);
 			}
 		}
 		
 		plugin.playerData.set(itemPath, newAmount);
 		try
 		{
 			plugin.playerData.save(plugin.playerFile);
 		}
 		catch (IOException e)
 		{
 			player.sendMessage(Messages.IO_EXCEPTION);
 			plugin.playerData.set(itemPath, oldAmount);
 			return false;
 		}
 		
 		item.setAmount(amount);
 		player.getInventory().removeItem(item);
 		player.sendMessage(Messages.getDepositSuccess(item.getType().toString(), item.getAmount()));
 		if (plugin.getEconomy().isEnabled() && config.enableVault)
 		{
 			player.sendMessage(Messages.getTransactionFeeMessage(config.transactionCost));
 			plugin.getEconomy().takeMoney(player.getName(), config.transactionCost);
 		}
 		
 		return true;
 	}
 }
