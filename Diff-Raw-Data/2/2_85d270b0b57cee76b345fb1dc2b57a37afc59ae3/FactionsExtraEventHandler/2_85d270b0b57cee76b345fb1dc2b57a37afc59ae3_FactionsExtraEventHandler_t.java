 package com.mutinycraft.jigsaw.FactionsExtra;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 
 import com.massivecraft.factions.Board;
 import com.massivecraft.factions.FPlayer;
 import com.massivecraft.factions.FPlayers;
 import com.massivecraft.factions.Faction;
 import com.massivecraft.factions.event.FactionCreateEvent;
 import com.massivecraft.factions.event.LandClaimEvent;
 
 public class FactionsExtraEventHandler implements Listener {
 
 	private FactionsExtra plugin;
 
 	private static final String FACTION_WORLD = "BG";
 
 	private static final int POINT_PER_KILL_1_1 = 2;
 	private static final int POINT_PER_KILL_1_2 = 4;
 	private static final int POINT_PER_KILL_2_1 = 1;
 	private static final int POINT_PER_KILL_2_2 = 2;
 
 	private static final int POINT_PER_CLAIM_1_1 = 6;
 	private static final int POINT_PER_CLAIM_1_2 = 12;
 	private static final int POINT_PER_CLAIM_2_1 = 3;
 	private static final int POINT_PER_CLAIM_2_2 = 6;
 
 	public FactionsExtraEventHandler(FactionsExtra pl) {
 		this.plugin = pl;
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 
 	// Events
 
 	@EventHandler(priority = EventPriority.LOW)
 	public void playerDeath(PlayerDeathEvent event) {
 
 		long start = System.currentTimeMillis();
 		if (event.getEntity() instanceof Player) {
 			Player killed = (Player) event.getEntity();
 			if (killed.getKiller() instanceof Player) {
 				Player killer = killed.getKiller();
 				if (killer.getWorld().getName().equalsIgnoreCase(FACTION_WORLD)) {
 					factionKillHandling(killed, killer);
 				}
 			}
 		}
 		long end = System.currentTimeMillis();
 		plugin.log.info("Debug: PlayerDeathEvent took " + (end - start)
 				+ "ms to finish.");
 	}
 
 	@EventHandler
 	public void landClaim(LandClaimEvent event) {
 		Faction claimedFrom = Board.getFactionAt(event.getLocation());
 		FPlayer claimingFP = FPlayers.i.get(event.getPlayer());
 		if (claimingFP.getFaction().getRelationTo(claimedFrom).isEnemy()) {
 			// Record data to file
 			if (getFactionTier(claimingFP.getFactionId()) == 1) {
 				if (getFactionTier(claimedFrom.getId()) == 1) {
 					addScore(claimingFP.getFactionId(), POINT_PER_CLAIM_1_1);
 					messageClaim(claimingFP.getPlayer(), POINT_PER_CLAIM_1_1);
 				} else if (getFactionTier(claimedFrom.getId()) == 2) {
 					addScore(claimingFP.getFactionId(), POINT_PER_CLAIM_1_2);
 					messageClaim(claimingFP.getPlayer(), POINT_PER_CLAIM_1_2);
 				}
 			} else if (getFactionTier(claimingFP.getFactionId()) == 2) {
 				if (getFactionTier(claimedFrom.getId()) == 1) {
 					addScore(claimingFP.getFactionId(), POINT_PER_CLAIM_2_1);
 					messageClaim(claimingFP.getPlayer(), POINT_PER_CLAIM_2_1);
 				} else if (getFactionTier(claimedFrom.getId()) == 2) {
 					addScore(claimingFP.getFactionId(), POINT_PER_CLAIM_2_2);
 					messageClaim(claimingFP.getPlayer(), POINT_PER_CLAIM_2_2);
 				}
 			}
 			recordDataInFile(claimingFP.getNameAndTag() + " claimed land from "
 					+ claimedFrom.getTag());
 		}
 	}
 
 	@EventHandler
 	public void factionCreation(FactionCreateEvent event) {
 		String factionID = event.getFactionId();
 		plugin.addFaction(factionID);
 	}
 
 	private void factionKillHandling(Player killed, Player killer) {
 		FPlayer killedFP = FPlayers.i.get(killed);
 		FPlayer killerFP = FPlayers.i.get(killer);
 		Faction killedF = killedFP.getFaction();
 		Faction killerF = killerFP.getFaction();

 		// Get Ally/Neutral/Enemy relationship
 		if (killedF.getRelationTo(killerF).isEnemy()) {
 			// Record data to file
 			if (getFactionTier(killerF.getId()) == 1) {
 				if (getFactionTier(killedF.getId()) == 1) {
 					addScore(killerF.getId(), POINT_PER_KILL_1_1);
 					messageKill(killerFP.getPlayer(), POINT_PER_KILL_1_1);
 				} else if (getFactionTier(killedF.getId()) == 2) {
 					addScore(killerFP.getId(), POINT_PER_KILL_1_2);
 					messageKill(killerFP.getPlayer(), POINT_PER_KILL_1_2);
 				}
 			} else if (getFactionTier(killerF.getId()) == 2) {
 				if (getFactionTier(killedF.getId()) == 1) {
 					addScore(killerF.getId(), POINT_PER_KILL_2_1);
 					messageKill(killerFP.getPlayer(), POINT_PER_KILL_2_1);
 				} else if (getFactionTier(killedF.getId()) == 2) {
 					addScore(killerF.getId(), POINT_PER_KILL_2_2);
 					messageKill(killerFP.getPlayer(), POINT_PER_KILL_2_2);
 				}
 			}
 			recordDataInFile(killedFP.getNameAndTag() + " was killed by "
 					+ killerFP.getNameAndTag());
 		}
 	}
 
 	private void addScore(String factionID, int score) {
 		List<Integer> data = plugin.getFactionData(factionID);
 		if (data.size() >= 2) {
 			score = data.get(0) + score;
 			data.set(0, score);
 		}
 		plugin.updateFaction(factionID, data);
 	}
 
 	private int getFactionTier(String factionID) {
 		return plugin.getFactionTier(factionID);
 	}
 
 	private void recordDataInFile(String msg) {
 		try {
 			File dataFile = new File(plugin.getDataFolder(), "data.txt");
 			PrintWriter out = new PrintWriter(new BufferedWriter(
 					new FileWriter(dataFile, true)));
 			out.println(msg);
 			out.close();
 		} catch (FileNotFoundException e) {
 			plugin.log.severe("That file does not exist! Please create it.");
 		} catch (IOException e) {
 			plugin.log.severe("Error saving to file!  Data is likely lost.");
 		}
 	}
 
 	private void messageKill(Player player, int points) {
 		player.sendMessage(ChatColor.GREEN
 				+ "You killed a member of an enemy faction and earned "
 				+ points + " points!");
 	}
 
 	private void messageClaim(Player player, int points) {
 		player.sendMessage(ChatColor.GREEN
 				+ "You claimed land from an enemy faction and earned " + points
 				+ " points!");
 	}
 
 }
