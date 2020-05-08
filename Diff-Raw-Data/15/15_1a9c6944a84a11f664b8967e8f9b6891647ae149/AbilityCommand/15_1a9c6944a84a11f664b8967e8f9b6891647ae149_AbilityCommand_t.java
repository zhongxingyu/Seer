 package com.iBaby.commands;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.iBaby.Command;
 import com.iBaby.iBaby;
 import com.iBaby.abilities.Ability;
 import com.iBaby.reflection.EntityIronBaby;
 
 /**
  * The ability list command
  * @author steffengy
  *
  */
 public class AbilityCommand extends Command {
 
 	/**
 	 * Displays a list of all abilities of this entity
 	 */
 	public void handle(CommandSender sender) {
 		EntityIronBaby target = null;
		HashMap<Class<?extends Ability>, Integer> lvls = new HashMap<Class<?extends Ability>, Integer>();
 		if((target = iBaby.getSelectedBaby(((Player) sender).getName())) != null) {
 			for(Ability ability : target.abilities.get()) {
				if(!lvls.containsKey(ability.getClass())) {
					lvls.put(ability.getClass(), 1);
 				}else{
					lvls.put(ability.getClass(), lvls.get(ability.getClass()) + 1);
 				}
 			}
			for(Map.Entry<Class<?extends Ability>, Integer> key : lvls.entrySet()) {
				sender.sendMessage(ChatColor.GRAY + key.getKey().getName().replace("com.iBaby.abilities.", "") + ChatColor.BLUE + " " + key.getValue());
 			}
 		}else{
 			sender.sendMessage(ChatColor.YELLOW + "Please select an iBaby by attacking it!");
 		}
 	}
 }
