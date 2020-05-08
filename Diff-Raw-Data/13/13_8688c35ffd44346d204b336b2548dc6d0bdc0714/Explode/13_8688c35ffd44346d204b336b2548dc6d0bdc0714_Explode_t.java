 package me.GudfareN.ExplodingEggs;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerListener;
 import me.GudfareN.ExplodingEggs.Main;
 
 public class Explode extends PlayerListener implements CommandExecutor {
 	
 	
 	
 	public static Main plugin;
 
     public Explode(Main instance) {
         plugin = instance;
     }
     
     public static HashMap<Player, Boolean> ee = new HashMap<Player, Boolean>();
     
     public void togglePluginState(Player player){
      	 
         if(ee.containsKey(player)){
             if(ee.get(player)){
             	ee.put(player, false);
                 player.sendMessage(ChatColor.AQUA + "Egg explosion disabled");
             } else {
             	ee.put(player, true);
                 player.sendMessage(ChatColor.AQUA + "Egg explosion enabled");
                 
             }
         } else {
         	ee.put(player, true); //If you want plugin enabled by default change this value to false.
             player.sendMessage(ChatColor.AQUA + "Egg explosion enabled");
         }
         
     }
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
     	if(cmd.getName().equalsIgnoreCase("ee")){
     		Player player = (Player) sender;
     		togglePluginState(player);
     		return true;
     	} 
     	return false; 
     }
     
     
     //Currently working on this. Not working atm:
     public void onPlayerEggThrow(PlayerEggThrowEvent event) {
     	if (ee.containsKey(event.getPlayer())){
    		if (ee.containsValue(true)){
     		event.getEgg().remove();
     		Egg egg = event.getEgg();
     		event.setHatching(true);
     		event.getEgg().getWorld().createExplosion(egg.getLocation(), 2);
    		}
     	}else{
     		
    		
     	}    	
     }
 }
     	
 
 
    
 
     
 
 
