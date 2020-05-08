 package uk.thecodingbadgers.minekart.race;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 
 import uk.thecodingbadgers.minekart.MineKart;
 import uk.thecodingbadgers.minekart.jockey.Jockey;
 import uk.thecodingbadgers.minekart.lobby.LobbySignManager;
 import uk.thecodingbadgers.minekart.racecourse.Racecourse;
 
 /**
  * @author TheCodingBadgers
  *
  * The interface for races.
  *
  */
 public abstract class Race {
 	
 	/** The course that is used by this race */
 	protected Racecourse course = null;
 	
 	/** The current state of the race */
 	protected RaceState state = RaceState.Unknown;
 	
 	/** The jockeys in this race */
 	protected Map<String, Jockey> jockeys = new HashMap<String, Jockey>();
 	
 	/** All the jockeys that are marked as ready */
	protected Set<Jockey> ready = new HashSet<Jockey>();
 	
 	/**
 	 * Set the course used by this race
 	 * @param course The course to use
 	 */
 	public void setCourse(Racecourse course) {
 		this.course = course;
         setState(RaceState.Waiting);
 	}
 
 	/**
 	 * Add a jockey to a race
 	 * @param player The player who will become the jockey
 	 */
 	public void addJockey(Player player) {
 		
 		if (this.state != RaceState.Waiting) {
 			MineKart.output(player, "You can't currently join this race.");
 			return;
 		}
 		
 		Location loc = this.course.getWarp("lobby");
 		
 		if (loc == null) {
 		    // FIXME For some reason the plugin keeps forgetting about this spawn and then not be able to
 		    // teleport players to the lobby. Protection against error for now, needs a proper fix
             MineKart.output(player, "A internal error has occurred. please inform staff");
             MineKart.getInstance().getLogger().log(Level.INFO, "Lobby spawn is null");
             return;
 		}
 		
 		Location oldLocation = player.getLocation();
 		
 		player.teleport(loc);
 		MineKart.output(player, "You have joined the lobby for the racecourse '" + this.course.getName() + "'.");		
 
         Jockey newJockey = new Jockey(player, this.course.getMountType(), oldLocation, this);
         this.jockeys.put(player.getName(), newJockey);
         
 		List<Location> spawns = this.course.getMultiWarp("spawn");
 		if (spawns.size() == this.jockeys.size()) {
 			teleportToSpawns();
 			return;
 		}
 		
 		LobbySignManager.updateSigns();		
 	}
 
 	/**
 	 * Teleport all jockeys to the starting spawns and put them on their mounts
 	 */
 	public void teleportToSpawns() {
 
         setState(RaceState.Starting);
         
 		List<Location> spawns = this.course.getMultiWarp("spawn");
 		int spawnIndex = spawns.size() - 1;
 		
 		for (Jockey jockey : this.jockeys.values()) {
 			Location spawn = spawns.get(spawnIndex);
 			jockey.teleportToSpawn(spawn);
 			spawnIndex--;
 		}
 		
 		startRace(3);		
 	}
 
 	/**
 	 * Start the race
 	 * @param countdown The amount of time until the race starts
 	 */
 	protected void startRace(final int countdown) {
 		
 		if (countdown <= 0) {
 			playSoundToRace(Sound.LEVEL_UP, 1.0f, 1.0f);
 			onRaceStart();		
 			return;
 		}
 		
 		outputToRace("Race starting in " + countdown);
 		playSoundToRace(Sound.ORB_PICKUP, 1.0f, 1.0f);
 		
 		Bukkit.getScheduler().scheduleSyncDelayedTask(MineKart.getInstance(), new Runnable() {
 
 			@Override
 			public void run() {
 				startRace(countdown - 1);
 			}
 			
 		}, 20L);
 		
 	}
 	
 	/**
 	 * Play a sound to all jockeys in the race
 	 * @param sound The sound to player
 	 * @param volume The volume of the sound
 	 * @param pitch The pitch of the sound
 	 */
 	private void playSoundToRace(Sound sound, float volume, float pitch) {
 		for (Jockey jockey : this.jockeys.values()) {
 			jockey.getPlayer().playSound(jockey.getPlayer().getLocation(), sound, volume, pitch);
 		}
 	}
 
 	/**
 	 * Called when the race starts
 	 */
 	private void onRaceStart() {
 		outputToRace("and they're off!");
 		
 		this.course.onRaceStart(this);
 		
 		for (Jockey jockey : this.jockeys.values()) {
 			jockey.onRaceStart();
 		}
 
         setState(RaceState.InRace);
 	}
 
 	/**
 	 * Output a message to all players in this race
 	 * @param message The message to output
 	 */
 	public void outputToRace(String message) {
 		
 		for (Jockey jockey : this.jockeys.values()) {
 			MineKart.output(jockey.getPlayer(), message);
 		}
 		
 	}
 	
 	/**
 	 * Output a message to all players in this race, from a given player
 	 * @param player The player saying the message
 	 * @param message The message to output
 	 */
 	public void outputToRace(Player player, String message) {
 		
 		for (Jockey jockey : this.jockeys.values()) {
 			MineKart.output(jockey.getPlayer(), player, message);
 		}
 		
 	}
 
 	/**
 	 * Get the jockey which represents a given player
 	 * @param player The player to get the jockey of
 	 * @return The jockey, or null if the given player isn't a jockey in this race
 	 */
 	public Jockey getJockey(Player player) {
 		return this.jockeys.get(player.getName());
 	}
 	
 	/**
 	 * Remove a jockey from the race
 	 * @param jockey The jockey to remove
 	 */
 	public void removeJockey(Jockey jockey) {
 		
 		this.jockeys.remove(jockey.getPlayer().getName());
 		jockey.onRaceEnd();
 
         LobbySignManager.updateSigns(); 
 		
 		if (this.jockeys.isEmpty()) {
 			end();
 		}
 		
 	}
 
 	/**
 	 * End the race
 	 */
 	public void end() {
 		
 		this.course.onRaceEnd(this);
 		
 		Map<String, Jockey> tempJockeys = new HashMap<String, Jockey>(this.jockeys);
 		for (Jockey jockey : tempJockeys.values()) {
 			removeJockey(jockey);
 		}
 		setState(RaceState.Waiting);
 	}
 
 	/**
 	 * Get the current state of the race
 	 * @return The state of the race
 	 */
 	public RaceState getState() {
 		return this.state;
 	}
 	
     /**
      * Change the current state of this race.
      * @param state the new state
      */
     public void setState(RaceState state) {
         this.state = state;
         LobbySignManager.updateSigns();
     }
     
 	/**
 	 * Get the course that this race is using
 	 * @return The course instance
 	 */
 	public Racecourse getCourse() {
 		return this.course;
 	}
 	
 	/**
 	 * Called when a jockey moves
 	 * @param jockey The jockey who moved
 	 */
 	public void onJockeyMove(Jockey jockey) {
 		this.course.onJockeyMove(jockey, this);
 	}
 
 	/**
 	 * Gets all jockeys in this race
 	 * @return A collection of jockeys
 	 */
 	public Collection<Jockey> getJockeys() {
 		return this.jockeys.values();
 	}
 
 	/**
 	 * Set the winner of the race
 	 * @param jockey The jockey who is the winner
 	 */
 	public void setWinner(Jockey jockey) {
 		
 		if (this.state != RaceState.InRace)
 			return;
 		
         setState(RaceState.Waiting);
 		
 		this.outputToRace(
 				ChatColor.YELLOW + jockey.getPlayer().getName() + 
 				ChatColor.WHITE + " and their mount " + 
 				ChatColor.YELLOW + jockey.getMount().getName() +
 				ChatColor.WHITE +" are the Winners!"
 			);
 		
 		end();
 	}
 
 	/**
 	 * Mark a player as ready to start the race
 	 * 
 	 * @param jockey the jockey to mark as ready
 	 * @return true if successful, false otherwise (eg. already ready)
 	 */
     public boolean readyUp(Jockey jockey) {
        if (this.ready.contains(jockey)) {
             return false;
         }
         
        this.ready.add(jockey);
         this.outputToRace(jockey.getPlayer().getName() + " is now ready! (" + this.ready.size() + "/" + this.jockeys.size() + ")");
         
        if (this.ready.size() == this.jockeys.size() && this.jockeys.size() >= this.course.getMinimumPlayers()) {
             this.ready.clear();
             this.teleportToSpawns();
         }
         
         return true;
     }
 }
