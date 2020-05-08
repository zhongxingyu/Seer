 package musician101.itembank.commands;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Map;
 
 import musician101.itembank.ItemBank;
 import musician101.itembank.exceptions.InvalidAliasException;
 import musician101.itembank.lib.Constants;
 import musician101.itembank.util.IBUtils;
 
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 
 /**
  * The code used to run when the Withdraw command is executed.
  * 
  * @author Musician101
  */
 public class WithdrawCommand implements CommandExecutor
 {
 	ItemBank plugin;
 	/**
 	 * @param plugin References the plugin's 
 	 */
 	public WithdrawCommand(ItemBank plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	/**
 	 * @param sender Who sent the command.
 	 * @param command Which command was executed
 	 * @param label Alias of the command
 	 * @param args Command parameters
 	 * @return True if the command was successfully executed
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		if (command.getName().equalsIgnoreCase(Constants.WITHDRAW_CMD))
 		{
			if (!sender.hasPermission(Constants.WITHDRAW_PERM))
 			{
 				sender.sendMessage(Constants.NO_PERMISSION);
 				return false;
 			}
 			if (!(sender instanceof Player) && !args[0].equalsIgnoreCase(Constants.ADMIN_CMD))
 			{
 				sender.sendMessage(Constants.PLAYER_COMMAND_ONLY);
 				return false;
 			}
 			if (args.length == 0)
 			{
 				sender.sendMessage(Constants.NOT_ENOUGH_ARGUMENTS);
 				return false;
 			}
 			
 			/** Admin Withdraw Start */
 			if (args[0].equalsIgnoreCase(Constants.ADMIN_CMD))
 			{
 				if (args.length < 3)
 				{
 					sender.sendMessage(Constants.NOT_ENOUGH_ARGUMENTS);
 					return false;
 				}
 				
 				String player = args[1].toLowerCase();
 				String name = args[2].toLowerCase();
 				int amount = 64;
 				if (args.length == 4)
 				{
 					try
 					{
 						amount = Integer.parseInt(args[3]);
 					}
 					catch (NumberFormatException e)
 					{
 						sender.sendMessage(Constants.NUMBER_FORMAT);
 						return false;
 					}
 				}
 				
 				ItemStack item = null;
 				try
 				{
 					item = IBUtils.getIdFromAlias(plugin, name, amount);
 				}
 				catch (InvalidAliasException e)
 				{
 					item = IBUtils.getItem(plugin, name, amount);
 				}
 				catch (NullPointerException e)
 				{
 					sender.sendMessage(Constants.NULL_POINTER);
 					return false;
 				}
 				if (item == null)
 				{
 					sender.sendMessage(Constants.getAliasError(name));
 					return false;
 				}
 				if (item.getType() == Material.AIR)
 				{
 					sender.sendMessage(Constants.AIR_BLOCK);
 					return false;
 				}
 				
 				String itemPath = item.getType().toString().toLowerCase() + "." + item.getDurability();
 				plugin.playerFile = new File(plugin.playerDataDir + "/" + player + ".yml");
 				plugin.playerData = new YamlConfiguration();
 				try
 				{
 					plugin.playerData.load(plugin.playerFile);
 				}
 				catch (FileNotFoundException e)
 				{
 					sender.sendMessage(Constants.FILE_NOT_FOUND);
 					return false;
 				}
 				catch (IOException e)
 				{
 					sender.sendMessage(Constants.IO_EXCEPTION);
 					return false;
 				}
 				catch (InvalidConfigurationException e)
 				{
 					sender.sendMessage(Constants.YAML_EXCEPTION);
 					return false;
 				}
 				
 				int oldAmount = plugin.playerData.getInt(itemPath);
 				if (amount > oldAmount)
 					amount = oldAmount;
 				
 				int newAmount = oldAmount - amount;
 				plugin.playerData.set(itemPath, amount);
 				try
 				{
 					plugin.playerData.save(plugin.playerFile);
 				}
 				catch (IOException e)
 				{
 					sender.sendMessage(Constants.IO_EXCEPTION);
 					plugin.playerData.set(itemPath, oldAmount);
 					return false;
 				}
 				
 				sender.sendMessage(Constants.PREFIX + "Removed " + newAmount + " " + item.getType().toString() + " from " + player + "'s account.");
 				return true;
 			}
 			/** Admin Withdraw End */
 			
 			/** "Custom Item" Start */
 			if (args[0].equalsIgnoreCase(Constants.CUSTOM_ITEM))
 			{
 				if (args.length < 2)
 				{
 					sender.sendMessage(Constants.PREFIX + "Error: Item not specified.");
 					return false;
 				}
 				
 				plugin.playerFile = new File(plugin.playerDataDir + "/" + sender.getName().toLowerCase() + ".yml");
 				plugin.playerData = new YamlConfiguration();
 				try
 				{
 					plugin.playerData.load(plugin.playerFile);
 				}
 				catch (FileNotFoundException e)
 				{
 					sender.sendMessage(Constants.FILE_NOT_FOUND);
 					return false;
 				}
 				catch (IOException e)
 				{
 					sender.sendMessage(Constants.IO_EXCEPTION);
 					return false;
 				}
 				catch (InvalidConfigurationException e)
 				{
 					sender.sendMessage(Constants.YAML_EXCEPTION);
 					return false;
 				}
 				
 				ItemStack item = null;
 				String name = args[1];
 				try
 				{
 					item = IBUtils.getIdFromAlias(plugin, name, 1);
 				}
 				catch (InvalidAliasException e)
 				{
 					item = IBUtils.getItem(plugin, name, 1);
 				}
 				catch (NullPointerException e)
 				{
 						sender.sendMessage(Constants.NULL_POINTER);
 						return false;
 				}
 				
 				ItemMeta meta = null;
 				BookMeta bookMeta = null;
 				FireworkMeta fwMeta = null;
 				SkullMeta skullMeta = null;
 				if (item == null)
 				{
 					if (!plugin.playerData.isSet(name))
 					{
 						sender.sendMessage(new String[]{Constants.getAliasError(name), Constants.PREFIX + "Check for capitalization."});
 						return false;
 					}
 					item = new ItemStack(Material.getMaterial(plugin.playerData.getString(name + ".material").toUpperCase()));
 				}
 				
 				try
 				{
 					if (plugin.playerData.isSet(name + ".durability"))
 						item.setDurability(Short.valueOf(plugin.playerData.getString(name + ".durability")));
 				}
 				catch (NumberFormatException e)
 				{
 					sender.sendMessage(Constants.getCustomItemWithdrawError(name));
 					return false;
 				}
 				
 				if (item.getType() == Material.WRITTEN_BOOK)
 				{
 					bookMeta = (BookMeta) item.getItemMeta();
 					bookMeta.setTitle(name.replace("_", " "));
 					bookMeta.setAuthor(plugin.playerData.getString(name + ".author"));
 					if (plugin.playerData.isSet(name + ".pages"))
 					{
 						for (Map.Entry<String, Object> pages : plugin.playerData.getConfigurationSection(name + ".pages").getValues(true).entrySet())
 						{
 							try
 							{
 								bookMeta.addPage(plugin.playerData.getString(name + ".pages." + pages.getKey()));
 							}
 							catch (IllegalArgumentException e)
 							{
 								sender.sendMessage(Constants.getCustomItemWithdrawError(name));
 								return false;
 							}
 						}
 					}
 					item.setItemMeta((ItemMeta) bookMeta);
 				}
 				else if (item.getType() == Material.FIREWORK)
 				{
 					fwMeta = (FireworkMeta) item.getItemMeta();
 					fwMeta.setPower(plugin.playerData.getInt(name + ".power"));
 					int x = 1;
 					while (plugin.playerData.isSet(name + ".effects." + x))
 					{
 						FireworkEffect.Builder effect = FireworkEffect.builder();
 						effect.flicker(plugin.playerData.getBoolean(name + ".effects." + x + ".flicker"));
 						effect.trail(plugin.playerData.getBoolean(name + ".effects." + x + ".trail"));
 						int y = 1;
 						while (plugin.playerData.isSet(name + ".effects." + x + ".colors." + y))
 						{
 							int red = plugin.playerData.getInt(name + ".effects." + x + ".colors." + y + ".red");
 							int green = plugin.playerData.getInt(name + ".effects." + x + ".colors." + y + ".green");
 							int blue = plugin.playerData.getInt(name + ".effects." + x + ".colors." + y + ".blue");
 							effect.withColor(Color.fromRGB(red, green, blue));
 							y++;
 						}
 						
 						y = 1;
 						while (plugin.playerData.isSet(name + ".effects." + x + ".fadeColors." + y))
 						{
 							int red = plugin.playerData.getInt(name + ".effects." + x + ".fadeColors." + y + ".red");
 							int green = plugin.playerData.getInt(name + ".effects." + x + ".fadeColors." + y + ".green");
 							int blue = plugin.playerData.getInt(name + ".effects." + x + ".fadeColors." + y + ".blue");
 							effect.withFade(Color.fromRGB(red, green, blue));
 							y++;
 						}
 						
 						try
 						{
 							effect.with(FireworkEffect.Type.valueOf(plugin.playerData.getString(name + ".effects." + x + ".type").toUpperCase()));
 						}
 						catch (IllegalArgumentException | NullPointerException e)
 						{
 							sender.sendMessage(Constants.PREFIX + "Could not set FireworkEffect.Type.");
 							return false;
 						}
 						
 						fwMeta.addEffect(effect.build());
 						x++;
 					}
 					item.setItemMeta(fwMeta);
 				}
 				else if (item.getType() == Material.SKULL_ITEM)
 				{
 					skullMeta = (SkullMeta) item.getItemMeta();
 					skullMeta.setOwner(name);
 					sender.sendMessage(skullMeta.getOwner());
 					item.setItemMeta(skullMeta);
 				}
 				else
 				{
 					meta = item.getItemMeta();
 					if (!name.equalsIgnoreCase(item.getType().toString()))
 						meta.setDisplayName(name.replace("_", " "));
 					
 					item.setItemMeta(meta);
 					if (plugin.playerData.isSet(name + ".enchantments"))
 					{
 						for (Map.Entry<String, Object> enchant : plugin.playerData.getConfigurationSection(name + ".enchantments").getValues(true).entrySet())
 						{
 							try
 							{						
 								item.addEnchantment(Enchantment.getByName(enchant.getKey().toUpperCase()), Integer.valueOf(enchant.getValue().toString()));
 							}
 							catch (IllegalArgumentException e)
 							{
 								sender.sendMessage(Constants.getCustomItemWithdrawError(name));
 								return false;
 							}
 						}
 					}
 					
 					if (plugin.playerData.isSet(name + ".lore"))
 						meta.setLore(plugin.playerData.getStringList(name + ".lore"));
 				}
 				
 				try
 				{
 					item.setAmount(plugin.playerData.getInt(name + ".amount"));
 				}
 				catch (NumberFormatException e)
 				{
 					sender.sendMessage(Constants.getCustomItemWithdrawError(name));
 					return false;
 				}
 				
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
 					sender.sendMessage(Constants.FULL_INV);
 					return false;
 				}
 				
 				if (item.getAmount() > freeSpace)
 					item.setAmount(freeSpace);
 				
 				int oldAmount = plugin.playerData.getInt(name + ".amount");
 				plugin.playerData.set(name + ".amount", oldAmount - item.getAmount());
 				try
 				{
 					plugin.playerData.save(plugin.playerFile);
 				}
 				catch (IOException e)
 				{
 					sender.sendMessage(Constants.IO_EXCEPTION);
 					plugin.playerData.set(name + ".amount", oldAmount);
 					return false;
 				}
 				
 				((Player) sender).getInventory().addItem(item);
 				sender.sendMessage(Constants.PREFIX + "You have withdrawn " + item.getAmount() + " " + name + " and now have a total of " + plugin.playerData.getInt(name + ".amount") + " left.");
 				return true;
 			}
 			/** "Custom Item" End */
 			
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
 					sender.sendMessage(Constants.NUMBER_FORMAT);
 					return false;
 				}
 			}
 			
 			ItemStack item = null;
 			try
 			{
 				item = IBUtils.getIdFromAlias(plugin, name, amount);
 			}
 			catch (InvalidAliasException e)
 			{
 				item = IBUtils.getItem(plugin, name, amount);
 			}
 			catch (NullPointerException e)
 			{
 				sender.sendMessage(Constants.NULL_POINTER);
 				return false;
 			}
 			if (item == null)
 			{
 				sender.sendMessage(Constants.getAliasError(name));
 				return false;
 			}
 			if (item.getType() == Material.AIR)
 			{
 				sender.sendMessage(Constants.AIR_BLOCK);
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
 				sender.sendMessage(Constants.FILE_NOT_FOUND);
 				return false;
 			}
 			catch (IOException e)
 			{
 				sender.sendMessage(Constants.IO_EXCEPTION);
 				return false;
 			}
 			catch (InvalidConfigurationException e)
 			{
 				sender.sendMessage(Constants.YAML_EXCEPTION);
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
 				sender.sendMessage(Constants.FULL_INV);
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
 				sender.sendMessage(Constants.IO_EXCEPTION);
 				plugin.playerData.set(itemPath, oldAmount);
 				return false;
 			}
 			
 			item.setAmount(amount);
 			((Player) sender).getInventory().addItem(item);
 			sender.sendMessage(Constants.PREFIX + "You have withdrawn " + amount + " " + item.getType().toString() + " and now have a total of " + newAmount + " left.");
 			return true;
 		}
 		return false;
 	}
 }
