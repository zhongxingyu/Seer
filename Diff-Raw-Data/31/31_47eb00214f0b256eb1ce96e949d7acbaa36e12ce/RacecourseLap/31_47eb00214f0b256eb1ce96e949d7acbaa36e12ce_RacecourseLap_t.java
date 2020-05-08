 package uk.thecodingbadgers.minekart.racecourse;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import uk.thecodingbadgers.minekart.MineKart;
 import uk.thecodingbadgers.minekart.events.jockey.JockeyCheckpointReachedEvent;
 import uk.thecodingbadgers.minekart.jockey.Jockey;
 import uk.thecodingbadgers.minekart.race.Race;
 import uk.thecodingbadgers.minekart.race.RaceState;
 import uk.thecodingbadgers.minekart.util.RaceHelper;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import com.sk89q.worldedit.regions.Region;
 
 /**
  * @author TheCodingBadgers
  * 
  *         A standard race where jockeys must complete a given number of laps
  *         of a course. There are a given number of checkpoints. A jockey must
  *         go through all checkpoints in the correct order, once a jockey has
  *         passed through the last checkpoint the given number of laps they
  *         are the winner
  * 
  */
 public class RacecourseLap extends Racecourse {
 	
 	class CheckpointTimeLapData {
 		public CheckpointTimeLapData(int checkpoint, int lap, long time) {
 			this.checkpoint = checkpoint;
 			this.time = time;
 			this.lap = lap;
 		}
 		
 		public int checkpoint;
 		public int lap;
 		public long time;
 	}
 
 	/** The number of laps in the race */
 	private int noofLaps = 3;
 
 	/** The current lap */
 	protected Map<Jockey, Integer> currentLap = null;
 
 	/** A map of a jockey and their next checkpoint id */
 	protected Map<Jockey, Integer> targetCheckpoints = null;
 
 	/** The checkpoints a jockey must pass through to complete the race */
 	protected List<Region> checkPoints = null;
 	
 	/** A map of a jockey and their checkpoint time lap data */
 	protected Map<Jockey, CheckpointTimeLapData> checkpointTimeLap = null;
 
 	/**
 	 * Class constructor
 	 */
 	public RacecourseLap() {
 		this.type = "lap";
 	}
 
 	/**
 	 * Setup the racecourse.
 	 * 
 	 * @param player The player who is setting up the course
 	 * @return True if the location is within the course bounds, false
 	 *         otherwise.
 	 */
 	@Override
 	public boolean setup(Player player, String name) {
 
 		if (!super.setup(player, name))
 			return false;
 
 		this.checkPoints = new ArrayList<Region>();
 		this.targetCheckpoints = new HashMap<Jockey, Integer>();
 		this.currentLap = new HashMap<Jockey, Integer>();
 		this.checkpointTimeLap = new HashMap<Jockey, CheckpointTimeLapData>();
 
 		save();
 		return true;
 	}
 
 	/**
 	 * Load the racecourse from file.
 	 */
 	@Override
 	public void load(File configfile) {
 
 		super.load(configfile);
 
 		FileConfiguration file = YamlConfiguration.loadConfiguration(configfile);
 
 		// Number of laps
 		this.noofLaps = file.getInt("racecourse.noofLaps");
 
 		// Checkpoints
 		this.checkPoints = new ArrayList<Region>();
 		this.targetCheckpoints = new HashMap<Jockey, Integer>();
 		this.currentLap = new HashMap<Jockey, Integer>();
 		this.checkpointTimeLap = new HashMap<Jockey, CheckpointTimeLapData>();
 
 		int noofCheckpoints = file.getInt("racecourse.checkpoint.count");
 		for (int checkpointIndex = 0; checkpointIndex < noofCheckpoints; ++checkpointIndex) {
 			this.checkPoints.add(loadRegion(file, "racecourse.checkpoint." + checkpointIndex));
 		}
 
 	}
 
 	/**
 	 * Save the racecourse to file.
 	 */
 	@Override
 	public void save() {
 
 		super.save();
 
 		FileConfiguration file = YamlConfiguration.loadConfiguration(this.fileConfiguration);
 
 		// Number of laps
 		file.set("racecourse.noofLaps", this.noofLaps);
 
 		// Checkpoints
 		file.set("racecourse.checkpoint.count", this.checkPoints.size());
 		int checkpointIndex = 0;
 		for (Region checkpoint : this.checkPoints) {
 			saveRegion(file, "racecourse.checkpoint." + checkpointIndex, checkpoint);
 			checkpointIndex++;
 		}
 
 		try {
 			file.save(this.fileConfiguration);
 		} catch (Exception ex) {
 		}
 
 	}
 
 	/**
 	 * Output the remaining requirements to complete this arena
 	 * 
 	 * @param sender The sender to receive the output information
 	 * @return True if all requirements have been met
 	 */
 	@Override
 	public boolean outputRequirements(CommandSender sender) {
 
 		boolean fullySetup = super.outputRequirements(sender);
 
 		if (this.checkPoints.isEmpty()) {
 			MineKart.output(sender, " - Add checkpoints (minimum of 1 required) [/mk addcheckpoint <coursename>]");
 			fullySetup = false;
 		}
 
 		return fullySetup;
 	}
 
 	/**
 	 * Output all information about this racecourse
 	 * 
 	 * @param sender The thing to tell the information
 	 */
 	@Override
 	public void outputInformation(CommandSender sender) {
 
 		super.outputInformation(sender);
 
 		MineKart.output(sender, "Checkpoints:");
 		int checkpointIndex = 0;
 		for (Region point : this.checkPoints) {
 			MineKart.output(sender, "[" + checkpointIndex + "] " + point.toString());
 			checkpointIndex++;
 		}
 		MineKart.output(sender, "-------------");
 
 	}
 
 	/**
 	 * Get the number of laps required to complete this racecourse.
 	 * 
 	 * @return The number of laps in this racecourse
 	 */
 	public int getNumberOfLaps() {
 		return this.noofLaps;
 	}
 
 	/**
 	 * Add a multi-point warp
 	 * 
 	 * @param player The player adding the warp
 	 * @param warpname The name of the warp to add to
 	 */
 	@Override
 	public void addWarp(Player player, String warpname) {
 
 		if (warpname.equalsIgnoreCase("checkpoint")) {
 
 			WorldEditPlugin worldEdit = MineKart.getInstance().getWorldEditPlugin();
 			Selection selection = worldEdit.getSelection(player);
 			if (selection == null) {
 				MineKart.output(player, "Please make a world edit selection of the region you wish to be a checkpoint...");
 				return;
 			}
 
 			try {
 				this.checkPoints.add(selection.getRegionSelector().getRegion().clone());
 			} catch (Exception ex) {
 				MineKart.output(player, "An invalid selection was made using world edit. Please make a complete cuboid selection and try again.");
 				return;
 			}
 
 			MineKart.output(player, "Succesfully add a new checkpoint to the arena!");
 			outputRequirements(player);
 			save();
 			return;
 		}
 
 		// if it's not a checkpoint pass it on
 		super.addWarp(player, warpname);
 		outputRequirements(player);
 		save();
 
 	}
 
 	/**
 	 * Called when a jockey moves
 	 * 
 	 * @param jockey The jockey who moved
 	 * @param race The race the jockeys are in
 	 */
 	@Override
 	public boolean onJockeyMove(Jockey jockey, Race race) {
 
 		if (!super.onJockeyMove(jockey, race))
 			return false;
 
 		if (race.getState() != RaceState.InRace)
 			return false;
 		
 		// player has finished the race
 		if (!this.targetCheckpoints.containsKey(jockey))
 			return false;
 
 		final int targetCheckpointIndex = this.targetCheckpoints.get(jockey);
 		Region targetCheckpoint = this.checkPoints.get(targetCheckpointIndex);
 
 		if (targetCheckpoint.contains(jockey.getWorldEditLocation())) {
 			
 			final Player player = jockey.getPlayer();
 			final Location location = player.getLocation();
 			
 			CheckpointTimeLapData data = this.checkpointTimeLap.get(jockey);
 			data.checkpoint = targetCheckpointIndex;
 			data.time = jockey.getRaceTime();
 			data.lap = this.currentLap.get(jockey);
 			updateRankings(race);
 			
 			// Temp standings output, until we get a scoreboard working
 			race.outputToRace("||== Race Standings == ||");
 			
 			int p = 1;
 			List<Jockey> rankings = race.getRankings();
 			for (Jockey j : rankings) {
 				race.outputToRace(ChatColor.GOLD + RaceHelper.ordinalNo(p) + " - " + j.getPlayer().getName());
 				p++;
 			}
 			
 			race.outputToRace("||================= ||");
 			// End temp code
 			
			jockey.updateRespawnLocation(jockey.getMount().getEntity().getLocation());
 
 			JockeyCheckpointReachedEvent event = new JockeyCheckpointReachedEvent(jockey, race, targetCheckpointIndex);
 			Bukkit.getPluginManager().callEvent(event);
 			
 			if (targetCheckpointIndex < this.checkPoints.size() - 1) {
 				this.targetCheckpoints.put(jockey, targetCheckpointIndex + 1);
 				MineKart.output(player, "Checkpoint [" + (targetCheckpointIndex + 1) + "/" + this.checkPoints.size() + "]      " + ChatColor.GREEN + MineKart.formatTime(jockey.getRaceTime()));
 
 				player.playSound(location, Sound.ORB_PICKUP, 1.0f, 1.0f);
 
 				float lapComplete = 1.0f / ((float) this.checkPoints.size() / (float) (targetCheckpointIndex + 1.0f));
 				player.setExp(lapComplete);
 			} else {
 				int jockeyLap = this.currentLap.get(jockey) + 1;
 				this.currentLap.remove(jockey);
 				this.currentLap.put(jockey, jockeyLap);
 
 				MineKart.output(player, "Lap Complete [" + jockeyLap + "/" + this.noofLaps + "]   " + ChatColor.GREEN + MineKart.formatTime(jockey.getRaceTime()));
 
 				player.setLevel(jockeyLap);
 				player.setExp(0.0f);
 				player.playSound(location, Sound.LEVEL_UP, 1.0f, 1.0f);
 
 				if (jockeyLap >= this.noofLaps) {
 					this.targetCheckpoints.remove(jockey);
 					race.setWinner(jockey);
 				} else {
 					this.targetCheckpoints.put(jockey, 0);
 				}
 			}
 		}
 
 		return true;
 
 	}
 	
 	/**
 	 * Update the rankings of the race, based upon checkpoint/time progress
 	 * @param race The race in progress
 	 */
 	private void updateRankings(Race race) {
 		
 		List<Jockey> ranks = new ArrayList<Jockey>();
 		
 		for (int lapIndex = this.noofLaps - 1; lapIndex >= 0; --lapIndex) {
 			
 			// Get all jockeys at this lap
 			Map<Jockey, CheckpointTimeLapData> lapRanks = new HashMap<Jockey, CheckpointTimeLapData>();
 			for (Jockey jockey : race.getJockeys()) {
 				CheckpointTimeLapData data = this.checkpointTimeLap.get(jockey);
 				if (data.lap == lapIndex) {
 					lapRanks.put(jockey, data);
 				}
 			}
 		
 			for (int checkpointIndex = this.checkPoints.size() - 1; checkpointIndex >= 0; --checkpointIndex) {
 								
 				// Get all jockeys at this lap
 				Map<Jockey, CheckpointTimeLapData> checkpointRanks = new HashMap<Jockey, CheckpointTimeLapData>();
 				for (Entry<Jockey, CheckpointTimeLapData> jockeyData : lapRanks.entrySet()) {
 					if (jockeyData.getValue().checkpoint == checkpointIndex) {
 						checkpointRanks.put(jockeyData.getKey(), jockeyData.getValue());
 					}
 				}
 				
 				// Sort the data by time
 				while (!checkpointRanks.isEmpty()) {
 					
 					Entry<Jockey, CheckpointTimeLapData> fastest = null;
 					for (Entry<Jockey, CheckpointTimeLapData> jockeyData : checkpointRanks.entrySet()) {
 						if (fastest == null) {
 							fastest = jockeyData;
 							continue;
 						}
 						
 						if (jockeyData.getValue().time < fastest.getValue().time) {
 							fastest = jockeyData;
 						}
 					}
 					
 					ranks.add(fastest.getKey());
 					checkpointRanks.remove(fastest.getKey());
 				}
 			}
 			
 		}
 			
 		race.setRankings(ranks);
 		
 	}
 
 
 	/**
 	 * Called on race start
 	 * 
 	 * @param race The race which started
 	 */
 	@Override
 	public void onRaceStart(Race race) {
 		super.onRaceStart(race);
 		this.currentLap.clear();
 		this.targetCheckpoints.clear();
 		for (Jockey jockey : race.getJockeys()) {
 			this.targetCheckpoints.put(jockey, 0);
 			this.currentLap.put(jockey, 0);
 			this.checkpointTimeLap.put(jockey, new CheckpointTimeLapData(-1, -1, -1));
 			jockey.getPlayer().setLevel(0);
 		}
 	}
 
 }
