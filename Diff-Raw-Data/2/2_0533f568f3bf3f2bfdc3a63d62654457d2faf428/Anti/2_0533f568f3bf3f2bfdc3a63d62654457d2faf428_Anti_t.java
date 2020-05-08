 package me.shock.avatarpvp.commands;
 
 import java.util.ArrayList;
 
 import me.shock.avatarpvp.Main;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class Anti implements CommandExecutor
 {
 
 	
     public Main plugin;
 	
 	public Anti(Main instance)
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
 		 * Anti Bender commands.
 		 * Chi - block your target from using bending powers for 15 seconds.
 		 * Stun - stun opponent for 3 seconds.
 		 */
 		
 		if(cmd.getName().equalsIgnoreCase("anti"))
 		{
 			if(sender.hasPermission("avatarpvp.anti"))
 			{
 				// Check if they forgot to tell us which ability they want.
				if(args.length != 1)
 				{
 					sender.sendMessage(apvp + "Bind different abilities to the item in your hand. Try:");
 					sender.sendMessage(apvp + "chi - block your target from using bending powers for 15 seconds.");
 					sender.sendMessage(apvp + "stun - slow your target for 10 seconds.");
 					return true;
 				}
 				
 				// If they told us then lets give them their ability.
 				else
 				{
 					// Chi Blocker ability.
 					if(args[0].equalsIgnoreCase("chi"))
 					{
 						if(sender.hasPermission("avatarpvp.anti.chi"))
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
 									lore.add(ChatColor.RED + "Chi Blocker");
 									meta.setLore(lore);
 									itemstack.setItemMeta(meta);
 									sender.sendMessage(apvp + "Successfully binded " + ChatColor.RED + "Chi Blocker" + ChatColor.WHITE + " to the item in your hand.");
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
 					
 					// Stun ability.
 					if(args[0].equalsIgnoreCase("stun"))
 					{
 						if(sender.hasPermission("avatarpvp.anti.stun"))
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
 									lore.add(ChatColor.RED + "Stun");
 									meta.setLore(lore);
 									itemstack.setItemMeta(meta);
 									sender.sendMessage(apvp + "Successfully binded " + ChatColor.RED + "Stun" + ChatColor.WHITE + " to the item in your hand.");
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
