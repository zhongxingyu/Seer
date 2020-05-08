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
		HashMap<Ability, Integer> lvls = new HashMap<Ability, Integer>();
 		if((target = iBaby.getSelectedBaby(((Player) sender).getName())) != null) {
 			for(Ability ability : target.abilities.get()) {
				if(!lvls.containsKey(ability)) {
					lvls.put(ability, 1);
 				}else{
					lvls.put(ability, lvls.get(ability) + 1);
 				}
 			}
			for(Map.Entry<Ability, Integer> key : lvls.entrySet()) {
				sender.sendMessage(ChatColor.GRAY + key.getKey().getClass().toString() + ChatColor.BLUE + " " + key.getValue());
 			}
 		}else{
 			sender.sendMessage(ChatColor.YELLOW + "Please select an iBaby by attacking it!");
 		}
 	}
 }
