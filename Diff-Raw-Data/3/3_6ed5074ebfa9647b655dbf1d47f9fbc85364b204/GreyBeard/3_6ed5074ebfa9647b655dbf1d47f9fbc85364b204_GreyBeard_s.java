 /* Deals with shout parsing, cooldowns, permission checking, then lets the
  * shout classes do their thing. */
 
 package com.reilaos.bukkit.TheThuum;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Set;
 
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.ChatColor;
 
 import com.reilaos.bukkit.TheThuum.shouts.Shout;
 import com.reilaos.bukkit.TheThuum.shouts.ShoutType;
 
 
 public class GreyBeard implements Listener{
 	
 	public HashMap<String, Shout> ShoutTable = new HashMap<String, Shout>();
 	
 	// Parses chat to see if it's a shout.  Determines level of the shout.
 	// Does parsing only.  Permissions and the like are handled by GreyBeard.shout()
 	@EventHandler (priority=EventPriority.HIGH)
 	public void onPlayerChat(PlayerChatEvent event) {
 		if (event.isCancelled()) return;
 		
 		String parsed = event.getMessage().toLowerCase().replaceAll("[^A-Za-z\\s]", "");
 		String [] message = parsed.split(" ", 4);
 		int length = message.length;
 		
 		if (length == 4) return;  // There are no 4-word shouts.
 		
 		if (ShoutTable.containsKey(parsed)){
 			int power = message.length;
 			switch(Plugin.thisOne.getConfig().getInt("display.audible chat")){
 			case (2):
 				event.setMessage(ChatColor.valueOf(Plugin.thisOne.getConfig().getString("display.color").toUpperCase()) + event.getMessage());
 				break;
 			case (1):
 				event.getPlayer().sendMessage(ChatColor.valueOf(Plugin.thisOne.getConfig().getString("display.color").toUpperCase()) + event.getMessage());
 			case (0):
 				event.setCancelled(true);
 			break;
 			}
 			shout(event.getPlayer(), ShoutTable.get(parsed), power);
 		}
 	}
 	
 	@EventHandler (priority=EventPriority.HIGH)
 	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
 		if (event.isCancelled()) return;
 		
 		Player dovahkiin = event.getPlayer();
 		String[] message = event.getMessage().split(" ");
 		message[0] = message[0].substring(1);
 		if (message.length > 2) return;
 		if (ShoutTable.containsKey(message[0])){
 			int power = 1;
 			event.setCancelled(true);
 			try {
 				if(message.length == 2) power = Integer.parseInt(message[1]);
 			}
 			catch (NumberFormatException e){
 				dovahkiin.sendMessage("Invalid parameter! Format is: /SHOUTNAME [power]");
 				return;
 			}
 			if (power < 0 || power > 3) {
 				dovahkiin.sendMessage("Invalid power! Must be 1, 2 or 3.");
 				return;
 			}
 			
 			int audible = Plugin.thisOne.getConfig().getInt("display.audible command");
 			if (audible > 0) {
 				StringBuilder say = new StringBuilder(ChatColor.valueOf(Plugin.thisOne.getConfig().getString("display.color").toUpperCase()).toString());
 				for(int i = 0; i < power; i++){
 					say.append(ShoutTable.get(message[0]).words()[i].toUpperCase()).append(" ");
 				}
 				say.insert(say.length()-1, '!');
 				
 				if      (audible == 1) dovahkiin.sendMessage(say.toString());
 				else if (audible == 2) dovahkiin.chat(say.toString());
 			}
 		
 			shout(event.getPlayer(), ShoutTable.get(message[0]), power);
 		}
 	}
 	
 	// Checks permissions, cooldown, performs shout if this checks out.
 	public static void shout(Player dragonBorn, Shout word, int level){
 		if (level > 3 || level < 0) return;
 		String shoutName = word.words()[0]+word.words()[1]+word.words()[2];
 		
 		if (!dragonBorn.hasPermission("thuum.shout." + shoutName + "." + level)) return;
 		
 		if (!dragonBorn.hasPermission("thuum.ignorecooldown." + shoutName + "." + level)) {
 			if (!Plugin.thisOne.arngeir.putOnCooldown(dragonBorn, word, level)) {
 				dragonBorn.sendMessage(Plugin.thisOne.getConfig().getString("cooldown.alert message"));
 				return;
 			}
 		}
 		word.shout(dragonBorn, level);
 	}
 	
 	
 	/**
 	 *  Cooldown handling stuff
 	 */
 	// Good practice would put this as its own player listener, but still.
 	
 	Configuration shoutCooldowns;	
 	Hashtable<String, Set<Shout>> onCooldown = new Hashtable<String, Set<Shout>>();
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event){
 		int persistence = Plugin.thisOne.getConfig().getInt("cooldown.persistence");
 		Plugin.scheduler.scheduleSyncDelayedTask(Plugin.thisOne, new ClearCooldowns(event.getPlayer()), persistence * 20);
 	}
 	
 	// If not already on cooldown, puts this shout on cooldown and returns true
 	// Returns false if the player is already on cooldown.
 	public boolean putOnCooldown(Player dovahkiin, Shout shout, int level){
		int cooldownDuration = (int) (Plugin.thisOne.getConfig().getDoubleList("shouts." + shout.toString().toLowerCase()).get(level - 1) * 20);
 		
 		if (Plugin.thisOne.getConfig().getBoolean("single cooldown", true)) shout = ShoutType.FUSRODAH.shout;
 		
 		if (!onCooldown.containsKey(dovahkiin.getName())) onCooldown.put(dovahkiin.getName(), new HashSet<Shout>());
 		if (onCooldown.get(dovahkiin.getName()).contains(shout)) return false;
 
 		onCooldown.get(dovahkiin.getName()).add(shout);
 		Cooldown task = new Cooldown(dovahkiin, shout);
 		Plugin.scheduler.scheduleSyncDelayedTask(Plugin.thisOne, task,cooldownDuration);
 		return true;
 	}
 		
 	public class Cooldown implements Runnable{
 		Player dovahkiin;
 		Shout shout;
 		
 		public Cooldown(Player player, Shout shout){
 			dovahkiin = player;
 			this.shout = shout;
 		}
 		
 		@Override
 		public void run() {
 			if (!onCooldown.containsKey(dovahkiin.getName())) return;
 			onCooldown.get(dovahkiin.getName()).remove(shout);
 			dovahkiin.sendMessage(Plugin.thisOne.getConfig().getString("cooldown.ready message"));
 		}
 	}
 	
 	public class ClearCooldowns implements Runnable {
 		Player dovahkiin;
 		
 		public ClearCooldowns (Player player){
 			dovahkiin = player;
 		}
 		
 		@Override
 		public void run() {
 			if (!dovahkiin.isOnline()) onCooldown.remove(dovahkiin.getName());
 		}
 	}
 }
