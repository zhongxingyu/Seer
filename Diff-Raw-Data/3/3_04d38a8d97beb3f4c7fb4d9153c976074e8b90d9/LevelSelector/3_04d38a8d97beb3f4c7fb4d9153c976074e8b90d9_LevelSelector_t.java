 /*******************************************************************************
  * Copyright or  or Copr. Quentin Godron (2011)
  * 
  * cafe.en.grain@gmail.com
  * 
  * This software is a computer program whose purpose is to create zombie 
  * survival games on Bukkit's server. 
  * 
  * This software is governed by the CeCILL-C license under French law and
  * abiding by the rules of distribution of free software.  You can  use, 
  * modify and/ or redistribute the software under the terms of the CeCILL-C
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info". 
  * 
  * As a counterpart to the access to the source code and  rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty  and the software's author,  the holder of the
  * economic rights,  and the successive licensors  have only  limited
  * liability. 
  * 
  * In this respect, the user's attention is drawn to the risks associated
  * with loading,  using,  modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean  that it is complicated to manipulate,  and  that  also
  * therefore means  that it is reserved for developers  and  experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or 
  * data to be ensured and,  more generally, to use and operate it in the 
  * same conditions as regards security. 
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C license and that you accept its terms.
  ******************************************************************************/
 package graindcafe.tribu.Level;
 
 import graindcafe.tribu.Tribu;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class LevelSelector implements Runnable {
 	private Tribu plugin;
 	private String randomLevel1;
 	private String randomLevel2;
 	private Random rnd;
 	private int taskID;
 	private HashMap<Player, Integer> votes;
 	private boolean votingEnabled;
 
 	public LevelSelector(Tribu instance) {
 		plugin = instance;
 		taskID = -1;
 		rnd = new Random();
 		votes = new HashMap<Player, Integer>();
 		votingEnabled = false;
 	}
 
 	public void cancelVote() {
 		if (taskID >= 0) {
 			plugin.getServer().getScheduler().cancelTask(taskID);
 		}
 	}
 
 	public void castVote(Player player, int v) {
 		if (votingEnabled) {
 
 			if (v > 2 || v < 1) {
 				Tribu.messagePlayer(player,plugin.getLocale("Message.InvalidVote"));
 				return;
 			}
 
 			votes.put(player, v);
 			Tribu.messagePlayer(player,plugin.getLocale("Message.ThankyouForYourVote"));
 			// if all players have voted
 			if (votes.size() == plugin.getPlayersCount()) {
 				cancelVote();
 				run();
 			}
 		} else {
 			Tribu.messagePlayer(player,plugin.getLocale("Message.YouCannotVoteAtThisTime"));
 		}
 	}
 
 	public void ChangeLevel(String name, Player player) {
 		if (plugin.getLevel() != null) {
 			if (plugin.getLevel().getName().equalsIgnoreCase(name)) {
 				Tribu.messagePlayer(player, String.format(plugin.getLocale("Message.LevelIsAlreadyTheCurrentLevel"), name));
 				return;
 			}
 		}
 
 		cancelVote();
 		boolean restart = false;
 		if (plugin.isRunning()) {
 			restart = true;
			plugin.stopRunning(true);
 		}
 
 
 		TribuLevel temp = plugin.getLevelLoader().loadLevelIgnoreCase(name);
 
 		if (!plugin.getLevelLoader().saveLevel(plugin.getLevel())) {
 			if (player != null) {
 				Tribu.messagePlayer(player,plugin.getLocale("Message.UnableToSaveLevel"));
 			} else {
 				plugin.LogWarning(ChatColor.stripColor(plugin.getLocale("Message.UnableToSaveLevel")));
 			}
 			return;
 		}
 
 		if (temp == null) {
 			if (player != null) {
 				Tribu.messagePlayer(player,plugin.getLocale("Message.UnableToLoadLevel"));
 			} else {
 				plugin.LogWarning(ChatColor.stripColor(plugin.getLocale("Message.UnableToLoadLevel")));
 			}
 			return;
 		} else {
 			if (player != null) {
 				Tribu.messagePlayer(player,plugin.getLocale("Message.LevelLoadedSuccessfully"));
 			} else {
 				plugin.LogInfo(ChatColor.stripColor(plugin.getLocale("Message.LevelLoadedSuccessfully")));
 			}
 		}
 
 		plugin.setLevel(temp);
 		if (restart) {
 			plugin.startRunning();
 		}
 
 	}
 
 	@Override
 	public void run() {
 		taskID = -1;
 		votingEnabled = false;
 		int[] voteCounts = new int[2];
 		Collection<Integer> nums = votes.values();
 		for (int vote : nums) {
 			voteCounts[vote - 1]++;
 		}
 		votes.clear();
 		if (voteCounts[0] >= voteCounts[1]) {
 			ChangeLevel(randomLevel1, null);
 			plugin.messagePlayers(String.format(plugin.getLocale("Broadcast.MapChosen"), randomLevel1));
 		} else {
 			ChangeLevel(randomLevel2, null);
 			plugin.messagePlayers(String.format(plugin.getLocale("Broadcast.MapChosen"), randomLevel2));
 		}
 		plugin.startRunning();
 	}
 
 	public void startVote(int duration) {
 		String[] levels = plugin.getLevelLoader().getLevelList().toArray(new String[0]);
 
 		if (levels.length < 2) { // Skip voting since there's only one option
 			plugin.startRunning();
 			return;
 		}
 		taskID = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, duration);
 		votingEnabled = true;
 
 		do {
 			randomLevel1 = levels[rnd.nextInt(levels.length)];
 		} while (randomLevel1 == plugin.getLevel().getName());
 
 		if (levels.length >= 3) {
 			do {
 				randomLevel2 = levels[rnd.nextInt(levels.length)];
 			} while (randomLevel2 == plugin.getLevel().getName() || randomLevel2 == randomLevel1);
 		} else {
 			randomLevel2 = plugin.getLevel().getName();
 		}
 		plugin.messagePlayers(plugin.getLocale("Broadcast.MapVoteStarting"));
 		plugin.messagePlayers(plugin.getLocale("Broadcast.Type"));
 		plugin.messagePlayers(String.format(plugin.getLocale("Broadcast.SlashVoteForMap"), '1', randomLevel1));
 		plugin.messagePlayers(String.format(plugin.getLocale("Broadcast.SlashVoteForMap"), '2', randomLevel2));
 		plugin.messagePlayers(String.format(plugin.getLocale("Broadcast.VoteClosingInSeconds"), String.valueOf(duration / 20)));
 	}
 
 }
