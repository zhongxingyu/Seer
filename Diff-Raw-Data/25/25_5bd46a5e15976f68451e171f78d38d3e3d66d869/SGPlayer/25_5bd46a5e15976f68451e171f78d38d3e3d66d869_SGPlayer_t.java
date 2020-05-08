 package com.xdrapor.safeguard.player;
 
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 
 import com.xdrapor.safeguard.utilities.SGBlockUtil;
 import com.xdrapor.safeguard.utilities.SGCheckTag;
 
 /**
  * Represents a player currently tracked by SafeGuard.
  * 
  * @author IchBinMude
  *
  */
 public class SGPlayer {
 
 	/** Represents the player instance. */
 	private final Player player;
 
 	/** Back pedal diff **/
 	private double backPedalDiff = 0D;
 
 	/** Represents the current violation levels of all SGCheckTags for this player. */
 	private Map<SGCheckTag, Double> violations = new HashMap<SGCheckTag, Double>();
 
 	/** The last time this player hit an entity. */
 	private long lastHitTime = 0L;
 
 	/** The last safe location of the player. */
 	private Location safeLocation = null;
 
 	/** The next expected Y position of the player. */
 	private double nextExpectedY = 0.0D;
 
 	/** The last time this player hit a block. */
 	private long lastBlockHitTime = 0L;
 
 	/** The last time this player broke a block. */
 	private long lastBlockBreakTime = 0L;
 	
 
 	/** The last time the player placed a block */
 	private long lastBlockPlaceTime = 0L;
 
 	/** The amount of packets that a player may send at a single time. */
 	private long lastPacketTime;
 
 	/** A buffer to allow for lag */
 	private int packetBuffer = 50;
 
 	/** Number of blocks broken in a one second timeframe */
 	private int blocksBrokenFreq = 0;
 	
 	/** The last time we updated blocks broken **/
 	private long lastBlocksBrokenFreq = 0;
 	
 	/** The amount of packets sent by a player */
 	private int packets;
 	
 	/** The location the player fell from **/
 	private Location fellFrom = null;
 	
 	/** The location the player fell to **/
 	private Location fellTo = null;
 
 	/** The initial health of the player upon falling */
 	private int initialHealth = 0;
 	
 	/** The final health of the player after falling */
 	private int finalHealth = 0;
 	
 	
 	/** Boolean to let the plugin know that this player was falling **/
 	private boolean falling = false;
 	
 	/** The packet limit for SGMorePacketsCheck */
 	public static final int MOREPACKETS_LIMIT = 22;
 
 	/** Construct a new SGPlayer instance. */
 	public SGPlayer(String playerName) {
 		this.packets = 0;
 		this.lastPacketTime = (long) 0;
 		this.player = Bukkit.getPlayer(playerName);
 
 		for (SGCheckTag tag : SGCheckTag.values()) {
 			this.violations.put(tag, 0.0D);
 		}
 	}
 
 	/** Returns the player instance. */
 	public Player getPlayer() {
 		return (this.player);
 	}
 	
 	/** Sets the initial health upon falling */
 	public void setFallInitialHealth(int health) {
 		this.initialHealth = health;
 	}
 	
 	/** Sets the final health after falling */
 	public void setFallFinalHealth(int health) {
 		this.finalHealth = health;
 	}
 	
 	/** Gets the final health after falling */
 	public int getFallFinalHealth() {
 		return (this.finalHealth);
 	}
 	
 	/** Gets the initial health upon falling */
 	public int getFallInitialHealth() {
 		return (this.initialHealth);
 	}
 
 	/** Sets the player to a state of falling based on the provided flag */
 	public void setFalling(boolean isFalling) {
 		this.falling = isFalling;
 	}
 	
 	/** Is the player falling? */
 	public boolean isFalling() {
 		return (this.falling);
 	}
 	
 	/** Resets all values related to falling */
 	public void resetFallingValues() {
 		this.falling = false;
 	}
 	
 	/** Returns the current safe location of the player. */
 	public Location getSafeLocation() {
 		return (this.safeLocation);
 	}
 
 	/** Sets the location fell from */
 	public void setFellFrom(Location fellFromLocation) {
 		this.fellFrom = fellFromLocation;
 	}
 	
 	/** Sets the location fell to */
 	public void setFellTo(Location fellToLocation) {
 		this.fellTo = fellToLocation;
 	}
 	
 	/** Gets the location fell from */
 	public Location getFellFrom() {
 		if(fellFrom == null)return getPlayer().getLocation();
 		return (this.fellFrom);
 	}
 	
 	/** Gets the location the player fell from */
 	public Location getFellTo() {
 		if(fellTo == null)return getPlayer().getLocation();
 		return (this.fellTo);
 	}
 	
 	public void incrementBlocksFreq() {
 		this.blocksBrokenFreq++;
 	}
 	
 	public int getBlocksFreq() {
 		return (this.blocksBrokenFreq);
 	}
 	public void resetBlocksFreq() {
 		this.blocksBrokenFreq = 0;
 	}
 	
 	public void setLastBlockBrokenFreq(long lastTime) {
 		this.lastBlocksBrokenFreq = lastTime;
 	}
 	
 	public long getLastBlockBrokenFreq() {
 		return (this.lastBlocksBrokenFreq);
 	}
 	
 	/** Sets the current safe location of the player. */
 	public void setSafeLocation(Location safeLocation) {
 		this.safeLocation = safeLocation;
 	}
 
 	/** Resets the current safe location of the player. */
 	public void resetSafeLocation() {
 		this.safeLocation = null;
 	}
 
 	/** Returns the time in milliseconds the player last placed a block*/
 	public long getLastPlaceTime() {
 		return this.lastBlockPlaceTime;
 	}
 
 	/** Sets the time in milliseconds the player last placed a block*/
 	public void setLastPlaceTime(long lastBlockPlaceTime) {
 		this.lastBlockPlaceTime = lastBlockPlaceTime;
 	}
 
 	/** Returns the time in milliseconds the player was last hit by the specified attacker. */
 	public long getLastHitTime() {
 		return this.lastHitTime;
 	}
 
 	/** Sets the time in milliseconds the player was last hit by the specified attacker. */
 	public void setLastHitTime(long lastHitTime) {
 		this.lastHitTime = lastHitTime;
 	}
 
 	/** Returns the last time this player hit a block. */
 	public long getLastBlockHitTime() {
 		return (this.lastBlockHitTime);
 	}
 
 	/** Sets the last time this player hit a block. */
 	public void setLastBlockHitTime(long lastBlockHitTime) {
 		this.lastBlockHitTime = lastBlockHitTime;
 	}
 
 	/** Returns the last time this player broke a block. */
 	public long getLastBlockBreakTime() {
 		return (this.lastBlockBreakTime);
 	}
 
 	/** Sets the last time this player broke a block. */
 	public void setLastBlockBreakTime(long lastBlockBreakTime) {
 		this.lastBlockBreakTime = lastBlockBreakTime;
 	}
 
 	/** Returns the violation level of the player for the specified tag. */
 	public double getVL(SGCheckTag tag) {
 		return (this.violations.get(tag).doubleValue());
 	}
 
 	/** Adds to the violation level of the player for the specified tag. */
 	public void addVL(SGCheckTag tag, double value) {
 		double oldValue = this.getVL(tag);
 		violations.put(tag, oldValue + value);
 	}
 
 	/** Returns the violation level of the player for the specified tag truncated to a max of two decimal places. */
 	public double getVLTruncated(SGCheckTag tag) {
 		return (Double.parseDouble(new DecimalFormat("#.##").format(this.violations.get(tag).doubleValue())));
 	}
 
 
 	/** Resets the specified player violation level. */
 	public void reduceVL(SGCheckTag tag) {
 		double oldValue = this.getVL(tag);
 		this.violations.put(tag, oldValue - (oldValue * 0.3));
 	}
 
 	/** Resets ALL the player violation levels. */
 	public void resetAllVL() {
 
 		this.violations.clear();
 
 		for (SGCheckTag tag : SGCheckTag.values()) {
 			this.violations.put(tag, 0.0D);
 		}		
 	}
 
 
 	/** Returns the Buffer for MorePackets */
 	public int getMorePacketBuffer() {
 		return (this.packetBuffer);
 	}
 
 	/** The current player's packets */
 	public int getPlayerPackets() {
 		return (this.packets);
 	}
 
 	/** The last packet time */
 	public long getLastPacketTime() {
 		return (this.lastPacketTime);
 	}
 
 	/** Sets the last packet time */
 	public void setLastPacketTime(long time) {
 		this.lastPacketTime = time;
 	}
 
 	/** Sets the player's packet amount */
 	public void setPlayerPackets(int packet) {
 		this.packets = packet;
 	}
 
 	/** Sets the morepackets buffer */
 	public void setPacketBuffer(int buffer) {
 		this.packetBuffer = buffer;
 	}
 
 	/** Returns the next expected Y position of the player. */
 	public double getNextExpectedY() {
 		return (this.nextExpectedY);
 	}
 
 	/** Sets the next expected Y position of the player. */
 	public void setNextExpectedY(double nextExpectedY) {
 		this.nextExpectedY = nextExpectedY;
 	}
 	
 	/**
 	 * Checks to see if the player is on a vine, or a ladder.
 	 * @param player
 	 * @return boolean
 	 */
 	public boolean isClimbing() {
 		//Checks the various blockfaces and retrives the relative block to check.
 		final Block block = player.getLocation().getBlock();
 		return (SGBlockUtil.isClimbable(block) || SGBlockUtil.isClimbable(block.getRelative(BlockFace.NORTH)) || SGBlockUtil.isClimbable(block.getRelative(BlockFace.SOUTH)) || SGBlockUtil.isClimbable(block.getRelative(BlockFace.EAST)) || SGBlockUtil.isClimbable(block.getRelative(BlockFace.WEST)) || SGBlockUtil.isClimbable(block.getRelative(BlockFace.SOUTH_WEST)) || SGBlockUtil.isClimbable(block.getRelative(BlockFace.NORTH_WEST))||  SGBlockUtil.isClimbable(block.getRelative(BlockFace.SOUTH_EAST)) || SGBlockUtil.isClimbable(block.getRelative(BlockFace.NORTH_EAST)));
 	}
 	
 
 	/**
 	 * Checks to see if the player is on a vine, or a ladder.
 	 * @param player
 	 * @return boolean
 	 */
 	public boolean isOnLily() {
 		//Checks the various blockfaces and retrives the relative block to check.
 		final Block block = player.getLocation().getBlock();
 		//Checks on jump
 		final Block blockLower = player.getLocation().subtract(0, 0.1, 0).add(0.5, 0, 0).getBlock();
 		final Block blockLowest = player.getLocation().subtract(0, 0.2, 0).add(0.5, 0, 0).getBlock();
 		//Returns if any
 		return (SGBlockUtil.isLily(block) || SGBlockUtil.isLily(blockLower) || SGBlockUtil.isLily(blockLowest) || SGBlockUtil.isLily(block.getRelative(BlockFace.NORTH)) || SGBlockUtil.isLily(block.getRelative(BlockFace.SOUTH)) || SGBlockUtil.isLily(block.getRelative(BlockFace.EAST)) || SGBlockUtil.isLily(block.getRelative(BlockFace.WEST)) || SGBlockUtil.isLily(block.getRelative(BlockFace.SOUTH_WEST)) || SGBlockUtil.isLily(block.getRelative(BlockFace.NORTH_WEST))||  SGBlockUtil.isLily(block.getRelative(BlockFace.SOUTH_EAST)) || SGBlockUtil.isLily(block.getRelative(BlockFace.NORTH_EAST)))
 				|| SGBlockUtil.isLily(blockLower.getRelative(BlockFace.NORTH)) || SGBlockUtil.isLily(blockLower.getRelative(BlockFace.SOUTH)) || SGBlockUtil.isLily(blockLower.getRelative(BlockFace.EAST)) || SGBlockUtil.isLily(blockLower.getRelative(BlockFace.WEST)) || SGBlockUtil.isLily(blockLower.getRelative(BlockFace.SOUTH_WEST)) || SGBlockUtil.isLily(blockLower.getRelative(BlockFace.NORTH_WEST))||  SGBlockUtil.isLily(blockLower.getRelative(BlockFace.SOUTH_EAST)) || SGBlockUtil.isLily(blockLower.getRelative(BlockFace.NORTH_EAST))
 				|| SGBlockUtil.isLily(blockLowest.getRelative(BlockFace.NORTH)) || SGBlockUtil.isLily(blockLowest.getRelative(BlockFace.SOUTH)) || SGBlockUtil.isLily(blockLowest.getRelative(BlockFace.EAST)) || SGBlockUtil.isLily(blockLowest.getRelative(BlockFace.WEST)) || SGBlockUtil.isLily(blockLowest.getRelative(BlockFace.SOUTH_WEST)) || SGBlockUtil.isLily(blockLowest.getRelative(BlockFace.NORTH_WEST))||  SGBlockUtil.isLily(blockLowest.getRelative(BlockFace.SOUTH_EAST)) || SGBlockUtil.isLily(blockLowest.getRelative(BlockFace.NORTH_EAST));
 	}
	
	
	/**
	 * Checks to see if the player is on a vine, or a ladder.
	 * @param player
	 * @return boolean
	 */
	public boolean isOnSnow() {
		//Checks the various blockfaces and retrives the relative block to check.
		final Block block = player.getLocation().getBlock();
		//Checks on jump
		final Block blockLower = player.getLocation().subtract(0, 0.1, 0).add(0.5, 0, 0).getBlock();
		final Block blockLowest = player.getLocation().subtract(0, 0.2, 0).add(0.5, 0, 0).getBlock();
		//Returns if any
		return (SGBlockUtil.isSnow(block) || SGBlockUtil.isSnow(blockLower) || SGBlockUtil.isSnow(blockLowest) || SGBlockUtil.isSnow(block.getRelative(BlockFace.NORTH)) || SGBlockUtil.isSnow(block.getRelative(BlockFace.SOUTH)) || SGBlockUtil.isSnow(block.getRelative(BlockFace.EAST)) || SGBlockUtil.isSnow(block.getRelative(BlockFace.WEST)) || SGBlockUtil.isSnow(block.getRelative(BlockFace.SOUTH_WEST)) || SGBlockUtil.isSnow(block.getRelative(BlockFace.NORTH_WEST))||  SGBlockUtil.isSnow(block.getRelative(BlockFace.SOUTH_EAST)) || SGBlockUtil.isSnow(block.getRelative(BlockFace.NORTH_EAST)))
				|| SGBlockUtil.isSnow(blockLower.getRelative(BlockFace.NORTH)) || SGBlockUtil.isSnow(blockLower.getRelative(BlockFace.SOUTH)) || SGBlockUtil.isSnow(blockLower.getRelative(BlockFace.EAST)) || SGBlockUtil.isSnow(blockLower.getRelative(BlockFace.WEST)) || SGBlockUtil.isSnow(blockLower.getRelative(BlockFace.SOUTH_WEST)) || SGBlockUtil.isSnow(blockLower.getRelative(BlockFace.NORTH_WEST))||  SGBlockUtil.isSnow(blockLower.getRelative(BlockFace.SOUTH_EAST)) || SGBlockUtil.isSnow(blockLower.getRelative(BlockFace.NORTH_EAST))
				|| SGBlockUtil.isSnow(blockLowest.getRelative(BlockFace.NORTH)) || SGBlockUtil.isSnow(blockLowest.getRelative(BlockFace.SOUTH)) || SGBlockUtil.isSnow(blockLowest.getRelative(BlockFace.EAST)) || SGBlockUtil.isSnow(blockLowest.getRelative(BlockFace.WEST)) || SGBlockUtil.isSnow(blockLowest.getRelative(BlockFace.SOUTH_WEST)) || SGBlockUtil.isSnow(blockLowest.getRelative(BlockFace.NORTH_WEST))||  SGBlockUtil.isSnow(blockLowest.getRelative(BlockFace.SOUTH_EAST)) || SGBlockUtil.isSnow(blockLowest.getRelative(BlockFace.NORTH_EAST));
	}
 
 	/** Returns the back pedal diff **/
 	public double getBackPedalDiff() {
 		return backPedalDiff;
 	}
 }
