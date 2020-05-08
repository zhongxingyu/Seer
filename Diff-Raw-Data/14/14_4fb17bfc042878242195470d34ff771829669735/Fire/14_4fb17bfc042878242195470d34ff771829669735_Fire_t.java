 package me.shock.avatarpvp.commands;
 
 import java.util.ArrayList;
 
 import me.shock.avatarpvp.Main;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class Fire implements CommandExecutor
 {
 
     public Main plugin;
 	
 	public Fire(Main instance)
 	{
 		this.plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		
 		String apvp = ChatColor.BLUE + "[" + ChatColor.WHITE + "AvatarPvP" + ChatColor.BLUE + "]" + ChatColor.WHITE + ": ";
 		String noperm = apvp + "You don't have permission to use this.";
 		
 		/**
 		 * Quick check if sender is player.
 		 */
 		if (!(sender instanceof Player))
 		{
 			sender.sendMessage("[AvatarPvP] Only players can use commands.");
 			return true;
 		}
 		
 		Player player = (Player) sender;
 		ItemStack itemstack = player.getItemInHand();
 		int amount = itemstack.getAmount();
 		
 		/**
 		 * Fire Bender commands.
 		 * Fireball - shoot 3 fireballs every 3 seconds.
 		 * Lightning - strike lightning where looking during storms.
 		 * Fire Bow - always shoot fire arrows.
 		 */
 		
 		if(cmd.getName().equalsIgnoreCase("fire"))
 		{
 			if(sender.hasPermission("avatarpvp.fire"))
 			{
 				// Check if they forgot to tell us which ability they want.
 				if(args.length != 1)
 				{
 					sender.sendMessage(apvp + "Bind different abilities to the item in your hand. Try:");
 					sender.sendMessage(apvp + "fireball - shoot 3 fireballs every 10 seconds.");
 					sender.sendMessage(apvp + "lightning - strike lightning where you are looking during storms.");
 					sender.sendMessage(apvp + "firebow - bow that always shoots fire arrows.");
 					return true;
 				}
 				
 				// If they told us then lets give them their ability.
 				else
 				{
 					// Fireball ability.
 					if(args[0].equalsIgnoreCase("fireball"))
 					{
 						if(sender.hasPermission("avatarpvp.fire.fireball"))
 						{
 							
 							if(amount != 1)
 							{
 								sender.sendMessage(apvp + "You can only have 1 item at a time.");
 								return true;
 							}
 							else
 							{
 								ItemMeta meta = itemstack.getItemMeta();
 								ArrayList<String> lore = new ArrayList<String>();
 								if(!(lore.isEmpty()))
 								{
 									sender.sendMessage(apvp + "You can't bind more than one ability to an item.");
 									return true;
 								}
 								else
 								{
 									itemstack.setItemMeta(meta);
 									lore.add(ChatColor.DARK_RED + "FireBall");
 									meta.setLore(lore);
 									itemstack.setItemMeta(meta);
 									sender.sendMessage(apvp + "Successfully binded " + ChatColor.DARK_RED + "Fireball" + ChatColor.WHITE + " to the item in your hand.");
 									return true;
 								}
 							}
 						}
 						else
 						{
 							sender.sendMessage(noperm);
 							return true;
 						}
 					}
 					
 					// Lightning ability.
 					if(args[0].equalsIgnoreCase("lightning"))
 					{
 						if(sender.hasPermission("avatarpvp.fire.lightning"))
 						{
 							if(amount != 1)
 							{
 								sender.sendMessage(apvp + "You can only have 1 item at a time");
 								return true;
 							}
 							else
 							{
 								ItemMeta meta = itemstack.getItemMeta();
 								ArrayList<String> lore = new ArrayList<String>();
 								if(!(lore.isEmpty()))
 								{
 									sender.sendMessage(apvp + "You can't bind more than one ability to an item.");
 									return true;
 								}
 								else
 								{
 									itemstack.setItemMeta(meta);
 									lore.add(ChatColor.DARK_RED + "Lightning");
 									meta.setLore(lore);
 									itemstack.setItemMeta(meta);
									sender.sendMessage(apvp + "Successfully binded " + ChatColor.DARK_RED + "Lightning" + ChatColor.WHITE + " to the item in your hand.");
 									return true;
 								}
 							}
 						}
 						// No perms :(
 						else
 						{
 							sender.sendMessage(noperm);
 							return true;
 						}
 					}
 					
 					// Lightning ability.
 					if(args[0].equalsIgnoreCase("firebow"))
 					{
 						if(sender.hasPermission("avatarpvp.fire.firebow"))
 						{
 							if(itemstack.getType() == Material.BOW)
 							{
 								ItemMeta meta = itemstack.getItemMeta();
 								ArrayList<String> lore = new ArrayList<String>();
 								if(!(lore.isEmpty()))
 								{
 									sender.sendMessage(apvp + "You can't bind more than one ability to an item.");
 									return true;
 								}
 								else
 								{
 									itemstack.setItemMeta(meta);
 									lore.add(ChatColor.DARK_RED + "FireBow");
 									meta.setLore(lore);
 									itemstack.setItemMeta(meta);
 									sender.sendMessage(apvp + "Successfully binded " + ChatColor.DARK_RED + "FireBow " + ChatColor.WHITE + "to the item in your hand.");
 									return true;
 								}
 							}
 							else
 							{
 								sender.sendMessage(apvp + "You can only bind that to a bow.");
 								return true;
 							}
 						}
 						// No perms :(
 						else
 						{
 							sender.sendMessage(noperm);
 							return true;
 						}
 					}
 				}
 			}
 			
 			// No perms :(
 			else
 			{
 				sender.sendMessage(noperm);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 }
