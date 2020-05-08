 package me.NerdsWBNerds.ServerGames;
 
 import static org.bukkit.ChatColor.*;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class Countdown extends CurrentState{
 	int i = 0;
 	int time = 15;
 	ServerGames plugin;
 	
 	public Countdown(ServerGames p){
 		plugin = p;
 	}
 	
 	@Override
 	public void run() {
 		if(time == 60 * 4){
 			plugin.server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " 4 Minutes remaining.");
 		}if(time == 60 * 3.5){
 			plugin.server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " 3.5 Minutes remaining.");
 		}if(time == 60 * 3){
 			plugin.server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " 3 Minutes remaining.");
 		}if(time == 60 * 2.5){
 			plugin.server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " 2.5 Minutes remaining.");
 		}if(time == 60 * 2){
 			plugin.server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " 2 Minutes remaining.");
 		}if(time == 60 * 1.5){
 			plugin.server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " 1.5 Minutes remaining.");
 		}if(time == 60 * 1){
 			plugin.server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " 1 Minute remaining.");
 		}if(time == 30){
 			plugin.server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " 30 Seconds remaining.");
 		}if(time == 15){
 			plugin.server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " 15 Seconds remaining.");
 		}if(time == 5){
 			plugin.server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " 5 Seconds remaining.");
 		}if(time == 0){
 			
 			for(Player p : plugin.server.getOnlinePlayers()){
				if(i >= ServerGames.tubes.size())
 					i = 0;
 				
 				Location to = ServerGames.tubes.get(i);
 				p.teleport(plugin.toCenter(to));
 				p.setSprinting(false);
 				p.setSneaking(false);
 				p.setPassenger(null);
 				
 				i++;
 			}
 			
 			
 			plugin.state = State.SET_UP;
 			plugin.cancelTasks();
 			plugin.game = new Game(plugin);
 			plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, plugin.game, 20L, 20L);
 		}
 		
 		time--;
 	}
 
 }
