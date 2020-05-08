 /*The MIT License
 
 Copyright (c) 2012 Mark Lohstroh
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
 
 package net.evtr.hungergames;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class EntityListener implements Listener 
 {
 	private HungerGames plugin;
 	
 	public EntityListener(HungerGames instance)
 	{
 		plugin = instance;
 	}
 	
 	@EventHandler
 	public void CreatureSpawn(CreatureSpawnEvent event)
 	{
 		//throttling the creature spawning to only eggs
 		if(event.getSpawnReason() != SpawnReason.SPAWNER_EGG)
 		{
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void OnPlayerInteract(PlayerInteractEvent event)
 	{
 		
 	}
 	
 	public void OnPlayerJoin(PlayerJoinEvent event)
 	{
 		if(plugin.currentGame != null)
 		{
 			plugin.currentGame.removePlayerFromLeaveList(event.getPlayer());
 		}
 	}
 	
 	
 	@EventHandler
 	public void OnPlayerLeave(PlayerQuitEvent event)
 	{
 		if(plugin.currentGame != null)
 		{
 			plugin.currentGame.addPlayerToLeaveList(event.getPlayer());
 		}
 	}
 	@EventHandler
 	public void OnPlayerDeath(PlayerDeathEvent event)
 	{
 		Player player = ((Player)event.getEntity());
 		HungerPlayer killer = null;
 		if(player.getKiller() != null)
 		{
 			if(plugin.currentGame != null)
 			{
 				killer = plugin.currentGame.getPlayer(player);
 			}
 		}
 
 		if(plugin.currentGame != null)
 		{
 			HungerPlayer hPlayer = plugin.currentGame.getPlayer(player);
 			plugin.currentGame.soundCannon();
 			
 			if (hPlayer != null) 
 			{
 				//this means he was sponsored
 				if(plugin.currentGame.getPlayersSponsor(hPlayer) != null)
 				{
 					//so lets tell the sponsor that his player died
 					plugin.currentGame.getPlayersSponsor(hPlayer).SponsoredPlayerDied();
 				}
 				// "kill" him
 				hPlayer.mIsDied = true;
 				//add the player to the sponsor list
 				plugin.currentGame.AddSponsor(new HungerSponsor(hPlayer));
 				player.sendMessage(ChatColor.GOLD + "You may now become a sponsor for someone!");
 				player.sendMessage(ChatColor.GOLD + "Type /hg s <playername> to sponsor the player of your choice!");
 				//TODO: Print the leftover players here
 				player.sendMessage(ChatColor.GOLD + "You may only sponsor one person at a time and your tribute may die!");
 				//give credit to the killer
 				if(killer != null)
 				{
 					killer.KilledPlayer(hPlayer);
 				}
 				
 				java.util.Vector<HungerPlayer> livingPlayers = plugin.currentGame.getAlivePlayers();
 				
				if ( livingPlayers.size() == 1 ) {
 					HungerPlayer hWinner = livingPlayers.get(0);
 					plugin.getServer().broadcastMessage(ChatColor.GOLD + ""
 						+ hWinner.getPlayerName() + " won the Hunger Games with " + hWinner.getKills()
 						+ (hWinner.getKills() == 0 || hWinner.getKills() > 1 ? " kills!" : " kill!"));
 					
 					plugin.currentGame = null; //TODO Add some kind of fancy cleanup to not break everything?
				} else {
 					plugin.getServer().broadcastMessage(ChatColor.GOLD + ""
 						+ hPlayer.getPlayerName() + " died in the Hunger Games with " + hPlayer.getKills()
 						+ (hPlayer.getKills() == 0 || hPlayer.getKills() > 1 ? " kills!" : " kill!"));
 					
 				}
 			}
 			
 			
 			
 			
 		}
 	}
 }
