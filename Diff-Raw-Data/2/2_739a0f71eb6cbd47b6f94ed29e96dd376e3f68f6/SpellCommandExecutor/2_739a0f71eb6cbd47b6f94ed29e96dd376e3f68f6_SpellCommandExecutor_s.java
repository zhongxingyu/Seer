 package me.hgilman.Spells.Executors;
 
 import java.util.ArrayList;
 
 import me.hgilman.Spells.Spell;
 import me.hgilman.Spells.Spells;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SpellCommandExecutor implements CommandExecutor {
 	private Spells plugin;
 
 	public SpellCommandExecutor(Spells instance)
 	{
 		plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		Player player;
 		if(sender instanceof Player)
 		{
 			player = (Player) sender;
 			if(player.getItemInHand().getType() == Material.GOLD_HOE)
 			{
 				//SPELLINFO
 				if(command.getName().equalsIgnoreCase("spellinfo"))
 				{
 					if(args.length==0)
 					{
 						Spell currentSpell = plugin.playerBooks.get(player.getName()).getCurrentSpell(); // Default to the current spell.
 						sender.sendMessage("Current spell " + currentSpell.abilityFormat(true) + ": " + currentSpell.getDescription());
 						return true;
 					}
 					else if (args.length==1) // They gave an arg.
 					{
 						if(plugin.playerBooks.get(player.getName()).getSpell(args[0]) != null)
 						{
 							Spell spell = plugin.playerBooks.get(player.getName()).getSpell(args[0]);
 							sender.sendMessage(spell.abilityFormat() + ": " + spell.getDescription());
 							return true;
 						}
 						else
 						{
 							sender.sendMessage(ChatColor.DARK_RED + "Spell " + args[0] + " not found in your spellbook.");
 							return true;
 						}
 					}
 					else
 					{
 						return false;
 					}
 				}
 				
 				// LISTSPELLS
 				else if(command.getName().equalsIgnoreCase("listspells"))
 				{
					sender.sendMessage("Currently available spells (arrow denotes selection:");
 
 					ArrayList<Spell> spellRegistry = plugin.playerBooks.get(player.getName()).getRegistry();
 
 					for (int iii=0;iii<spellRegistry.size();iii++)
 					{
 						if (spellRegistry.get(iii) == plugin.playerBooks.get(player.getName()).getCurrentSpell())
 						{
 							sender.sendMessage("   - " + spellRegistry.get(iii).abilityFormat() + " <--"); // It's the current spell.
 
 						}
 						else
 						{
 							sender.sendMessage("   - " + spellRegistry.get(iii).abilityFormat()); // It's not the current spell.
 						}
 					}
 					sender.sendMessage("Key: " + ChatColor.DARK_GREEN + "(proper resources)" + ChatColor.DARK_RED + " (needs materials)");
 					return true;
 				}
 				else
 				{
 					plugin.log.info(this.toString() + " could not process the commmand: " + command.getName());
 					return false;
 				}
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.DARK_RED + "You must be wielding a Golden Scepter to use Spells.");
 				return true;
 			}
 
 			/*     
 
 	        else if(command.getName().equalsIgnoreCase("setspell"))
 	        {
 	        	//SETSPELL
 	        }*/
 		}
 		else
 		{
 			plugin.log.info(sender.getName() + " tried to use an ingame-only command.");
 			return false; // Only ingame players may use the spell related commands.
 		}
 	}
 
 
 
 }
