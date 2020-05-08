 package me.shock.avatarpvp.commands;
 
 import java.util.ArrayList;
 
 import me.shock.avatarpvp.Main;
 import me.shock.avatarpvp.commands.help.Help;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class Air implements CommandExecutor
 {
 
 	
     public Main plugin;
     Help helpClass = new Help();
 	
 	public Air(Main instance)
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
 		 * Air Bender command here.
 		 * Fly - have fly ability for 5 seconds.
 		 */
 		
 		if(cmd.getName().equalsIgnoreCase("air"))
 		{
 			if(sender.hasPermission("avatarpvp.air"))
 			{
 				
				if(args.length != 1)
				{
					sender.sendMessage(apvp + "Bind different abilities to the item in your hand. Try:");
					sender.sendMessage(apvp + "fly - have fly ability for 5 seconds.");
					return true;
				}
				
 				// Fly ability.
 				if(args[0].equalsIgnoreCase("fly"))
 				{
 					if(sender.hasPermission("avatarpvp.air.fly"))
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
 								lore.add(ChatColor.AQUA + "Fly");
 								meta.setLore(lore);
 								itemstack.setItemMeta(meta);
 								sender.sendMessage(apvp + "Successfully binded " + ChatColor.AQUA + "Fly" + ChatColor.WHITE + " to the item in your hand.");
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
 				else
 				{
 					helpClass.printHelp(sender);
 				}
 				
 				// Check if they forgot to tell us which ability they want.
				
 				
 				// If they told us then lets give them their ability.
 	
 					
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
