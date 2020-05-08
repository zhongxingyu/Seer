 package com.Cayviel.HardCoreWorlds;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent.Result;
 import org.bukkit.WorldCreator;
 
 public class playerL extends PlayerListener {
 
 	public void onPlayerLogin (PlayerLoginEvent hi){
 		Player player = hi.getPlayer();
 		World inWorld = player.getWorld();
 
 		VirtualHunger.delay(80, player);
 		
 		MiscFunctions.WorldListUpdate(inWorld);
 		if(! BanManager.EnterWorldRequest(player,inWorld)){
 			safetyWcheck();
 			player.teleport(BanManager.Ereturnworld.getSpawnLocation());
 			return;
 		}
 	}
 	
 	public void onPlayerPreLogin (PlayerPreLoginEvent hi){
 		String playerN = hi.getName();
 		if (! BanManager.isServerBanned(playerN)) return;
 		BanManager.updateServerBan(playerN);
 		if (BanManager.isServerBanned(playerN)){
 			int[] times = BanManager.getServerBanTimes(playerN);
 			if(times[1]>times[0]){
 				hi.disallow(Result.KICK_BANNED, "Sorry. You are banned from this server.");
 			}
			hi.disallow(Result.KICK_BANNED, "Sorry. You are banned for about "+(times[0]-BanManager.getHour())+" more hours");
 		}
 	}
 
 	public void onPlayerChangedWorld(PlayerChangedWorldEvent pMo){
 
 		World fromW, toW;
 		Player player = pMo.getPlayer();
 		
 		toW = player.getWorld();
 		fromW = pMo.getFrom();
 		MiscFunctions.WorldListUpdate(toW); //update the world list to include the entered world, if new
 		//if (! Config.getHc(toW.getName())) return; //if not hardcore, return
 		if (! BanManager.EnterWorldRequest(player, toW)){//if player is not granted permission to enter world,  
 			if (BanManager.isBanPerm(player, toW)){
 				player.sendMessage(ChatColor.LIGHT_PURPLE + "You are expelled from world '" + toW.getName() + "' forever because you died!");
 			}else{
 				player.sendMessage(ChatColor.LIGHT_PURPLE + "You are expelled from world '" + toW.getName() + "' for about " + BanManager.getTimeLeft(player, toW)+ " more hours because you died!");	
 			}
 			SafetyCheck(player, toW, fromW); //Return previous world, or to safety world in case of weird scenario
 		}
 		//else just continue on with the world change as normal
 	}
 	
 	public void SafetyCheck(Player player, World toW, World fromW){
 		if (BanManager.isBanned(player, toW)){
 			if (BanManager.isBanned(player, fromW)){
 				safetyWcheck();
 				player.teleport(BanManager.Ereturnworld.getSpawnLocation());
 				return;
 			}else{player.teleport(fromW.getSpawnLocation());
 			return;
 			}
 		}
 	}
 	
 	public static void safetyWcheck(){
 		if (BanManager.Ereturnworld == null){
 			WorldCreator wc = new WorldCreator(BanManager.BannedList.getString("Unbannable World"));
 			wc.environment(Environment.NORMAL);
 			BanManager.Ereturnworld = wc.createWorld();
 		}
 	}
 	
 }
