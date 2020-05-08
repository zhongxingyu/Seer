 package me.arno.blocklog.managers;
 
 import java.util.ArrayList;
 
 import org.bukkit.Location;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 import me.arno.blocklog.logs.InteractionType;
 import me.arno.blocklog.logs.LogType;
 import me.arno.blocklog.logs.BlockEdit;
 import me.arno.blocklog.logs.BlockInteraction;
 
 public class QueueManager extends BlockLogManager {
 	private final ArrayList<BlockEdit> blockEdits = new ArrayList<BlockEdit>();
 	private final ArrayList<BlockInteraction> blockInteractions = new ArrayList<BlockInteraction>();
 	
 	/**
 	 * Logs a block edit by the environment.
 	 * This can be either a block that has been created or a block that has been destroyed
 	 * 
 	 * @param block {@link BlockState} of the block that got destroyed
 	 * @param type {@link LogType} of the log
 	 */
 	public void queueBlockEdit(BlockState block, LogType type) {
 		queueBlockEdit(null, block, EntityType.UNKNOWN, type);
 	}
 	
 	/**
 	 * Logs a block edit by a player.
 	 * This can be either a block that has been created or a block that has been destroyed
 	 * 
 	 * @param player The player that triggered the event
 	 * @param block {@link BlockState} of the block that got destroyed
 	 * @param type {@link LogType} of the log
 	 */
 	public void queueBlockEdit(Player player, BlockState block, LogType type) {
 		queueBlockEdit(player, block, EntityType.PLAYER, type);
 	}
 	
 	/**
 	 * Logs a block edit by an entity other than a player.
 	 * This can be either a block that has been created or a block that has been destroyed
 	 * 
 	 * @param block {@link BlockState} of the block that got destroyed
 	 * @param entity {@link EntityType} of the entity that triggered this event
 	 * @param type {@link LogType} of the log
 	 */
 	public void queueBlockEdit(BlockState block, EntityType entity, LogType type) {
 		queueBlockEdit(null, block, entity, type);
 	}
 	
 	/**
 	 * Logs a block edit by an entity that got triggered by a player.
 	 * This can be either a block that has been created or a block that has been destroyed
 	 * 
 	 * @param player The {@link Player} that triggered the event
 	 * @param block {@link BlockState} of the block that got destroyed
 	 * @param entity {@link EntityType} of the entity that triggered this event
 	 * @param type {@link LogType} of the log
 	 */
 	public void queueBlockEdit(Player player, BlockState block, EntityType entity, LogType type) {
 		blockEdits.add(new BlockEdit(player, block, entity, type));
 	}
 	
 	/**
 	 * Logs a block interaction by a player.
 	 * This can be either when a player uses a lever or when he opens a chest
 	 * 
 	 * @param player The {@link Player} that triggered the event
 	 * @param location The {@link Location} where this event happened
 	 * @param type {@link InteractionType} of the log
 	 */
 	public void queueBlockInteraction(Player player, Location location, InteractionType type) {
 		blockInteractions.add(new BlockInteraction(player, location, type));
 	}
 	
 	/**
 	 * Gets a list of unsaved block edits
 	 * 
 	 * @return An {@link ArrayList} containing all the unsaved block edits
 	 */
 	public ArrayList<BlockEdit> getEditQueue() {
 		return blockEdits;
 	}
 	
 	/**
 	 * Gets a list of unsaved block interactions
 	 * 
 	 * @return An {@link ArrayList} containing all the unsaved block interactions
 	 */
 	public ArrayList<BlockInteraction> getInteractionQueue() {
 		return blockInteractions;
 	}
 	
 	/**
 	 * Gets the amount of unsaved block edits
 	 * 
 	 * @return An integer value that represents the amount of unsaved block edits
 	 */
 	public int getEditQueueSize() {
 		return blockEdits.size();
 	}
 	
 	/**
 	 * Gets the amount of unsaved block interactions
 	 * 
 	 * @return An integer value that represents the amount of unsaved block interactions
 	 */
 	public int getInteractionQueueSize() {
 		return blockInteractions.size();
 	}
 	
 	/**
 	 * Gets a queued block edit
 	 * 
 	 * @param index The index of the queued block edit
 	 * @return A {@link BlockEdit} object that represents the queued block edit
 	 */
 	public BlockEdit getQueuedBlockEdit(int index) throws IndexOutOfBoundsException {
 		return getEditQueue().get(index);
 	}
 	
 	/**
 	 * Gets the oldest queued block edit
 	 * 
 	 * @return A {@link BlockEdit} object that represents the queued block edit
 	 */
 	public BlockEdit getOldestQueuedBlockEdit() {
 		return getEditQueue().get(0);
 	}
 	
 	/**
 	 * Gets a queued block interaction
 	 * 
 	 * @param index The index of the queued block interaction
 	 * @return A {@link BlockInteraction} object that represents a queued block interaction
 	 */
 	public BlockInteraction getQueuedInteraction(int index) throws IndexOutOfBoundsException {
 		return getInteractionQueue().get(index);
 	}
 	
 	/**
 	 * Gets the oldest queued block interaction
 	 * 
 	 * @return A {@link BlockInteraction} object that represents a queued block interaction
 	 */
 	public BlockInteraction getOldestQueuedInteraction() {
 		return getInteractionQueue().get(0);
 	}
 	
 	/**
 	 * Saves the oldest queued block edit
 	 */
 	public void saveQueuedEdit() {
 		saveQueuedEdit(0);
 	}
 	
 	/**
 	 * Saves a queued block edit
 	 * 
 	 * @param index The index of the queued block interaction
 	 */
	public void saveQueuedEdit(int index) throws IndexOutOfBoundsException {
 		if(getEditQueueSize() > index) {
			getEditQueue().get(index).save();
			getEditQueue().remove(index);
 		}
 	}
 	
 	/**
	 * Saves the oldest queued block interactions
 	 */
 	public void saveQueuedInteraction() {
 		saveQueuedInteraction(0);
 	}
 	
 	/**
 	 * Saves a queued block interactions
 	 * 
 	 * @param index The index of the queued block interaction
 	 */
	public void saveQueuedInteraction(int index) throws IndexOutOfBoundsException {
 		if(getInteractionQueueSize() > index) {
			getInteractionQueue().get(index).save();
			getInteractionQueue().remove(index);
 		}
 	}
 }
