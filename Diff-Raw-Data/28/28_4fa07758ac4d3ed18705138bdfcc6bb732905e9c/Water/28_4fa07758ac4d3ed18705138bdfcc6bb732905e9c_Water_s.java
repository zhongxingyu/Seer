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
 
 public class Water implements CommandExecutor
 {
 
 	
     public Main plugin;
 	
 	public Water(Main instance)
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
 		
 		/**
 		 * Water bender commands.
 		 * Heal - Healing 1 potion for 1 30 seconds.
 		 * Make configurable.
 		 */
 		
		if(args[0].equalsIgnoreCase("water"))
 		{
 			if(sender.hasPermission("avatarpvp.water"))
 			{
 				// Check if they forgot to tell us which ability they want.
				if(args.length != 2)
 				{
 					sender.sendMessage(apvp + "Bind different abilities to the item in your hand. Try:");
 					sender.sendMessage(apvp + "ice - Bow shoots ice arrow.");
 					return true;
 				}
 				
 				
 				// If they told us then lets give them their ability.
 				else
 				{
 					// IceBow ability.
					if(args[1].equalsIgnoreCase("ice"))
 					{
 						if(sender.hasPermission("avatarpvp.water.icebow"))
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
 									lore.add(ChatColor.BLUE + "Ice Bow");
 									meta.setLore(lore);
 									itemstack.setItemMeta(meta);
 									sender.sendMessage(apvp + "Successfully binded " + ChatColor.BLUE + "Ice Bow" + ChatColor.WHITE + " to the item in your hand.");
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
