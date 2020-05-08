 package btwmods;
 
 import btwmods.events.EventDispatcher;
 import btwmods.events.EventDispatcherFactory;
 import btwmods.events.IAPIListener;
 import btwmods.player.PlayerBlockEvent;
 import btwmods.player.ContainerEvent;
 import btwmods.player.DropEvent;
 import btwmods.player.IPlayerActionListener;
 import btwmods.player.IPlayerBlockListener;
 import btwmods.player.IContainerListener;
 import btwmods.player.IDropListener;
 import btwmods.player.IPlayerInstanceListener;
 import btwmods.player.ISlotListener;
 import btwmods.player.PlayerInstanceEvent;
 import btwmods.player.PlayerInstanceEvent.METADATA;
 import btwmods.player.PlayerEventInvocationWrapper;
 import btwmods.player.PlayerActionEvent;
 import btwmods.player.RespawnPosition;
 import btwmods.player.SlotEvent;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.Block;
 import net.minecraft.src.BlockContainer;
 import net.minecraft.src.Container;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.Slot;
 import net.minecraft.src.World;
 
 public class PlayerAPI {
	private static EventDispatcher listeners = EventDispatcherFactory.create(new Class[] { IPlayerActionListener.class, IPlayerInstanceListener.class, IPlayerBlockListener.class, ISlotListener.class,
 			IContainerListener.class, IDropListener.class }, new PlayerEventInvocationWrapper());
 	
 	private PlayerAPI() {}
 	
 	public static void addListener(IAPIListener listener) {
 		listeners.addListener(listener);
 	}
 
 	public static void removeListener(IAPIListener listener) {
 		listeners.removeListener(listener);
 	}
 
 	public static boolean blockActivationAttempt(int blockId, World world, int x, int y, int z, EntityPlayer player, int direction, float xOffset, float yOffset, float zOffset) {
 		boolean activated = false;
 		
 		if (!listeners.isEmpty(IPlayerBlockListener.class)) {
 			PlayerBlockEvent event = PlayerBlockEvent.ActivationAttempt(player, Block.blocksList[blockId], x, y, z, direction, xOffset, yOffset, zOffset);
 			((IPlayerBlockListener)listeners).onPlayerBlockAction(event);
 			activated = event.isHandled();
 		}
 		
 		if (!activated)
 			activated = Block.blocksList[blockId].onBlockActivated(world, x, y, z, player, direction, xOffset, yOffset, zOffset);
 		
 		if (activated) {
 			blockActivated(player, Block.blocksList[blockId], x, y, z);
 		}
 		
 		return activated;
 	}
 	
 	/**
 	 * Handle a successful block activation.
 	 * 
 	 * @param player The player that activated the block.
 	 * @param block The block that was activated.
 	 * @param x coordinate of the block
 	 * @param y coordinate of the block
 	 * @param z coordinate of the block
 	 * @see net.minecraft.src.ItemInWorldManager#activateBlockOrUseItem(EntityPlayer, World, ItemStack, int, int, int, int, float, float, float)
 	 */
 	public static void blockActivated(EntityPlayer player, Block block, int x, int y, int z) {
 		if (!listeners.isEmpty(IPlayerBlockListener.class)) {
 			PlayerBlockEvent event = PlayerBlockEvent.Activated(player, block, x, y, z);
 			((IPlayerBlockListener)listeners).onPlayerBlockAction(event);
 		}
 
 		if (block instanceof BlockContainer) {
 			containerOpened(player, block, player.craftingInventory, x, y, z);
 		}
 	}
 
 	/**
 	 * Asks all mods if the attempt should be allowed. Mods can only say that it's not allowed.
 	 * 
 	 * @param itemStack The item being placed.
 	 * @param player The player making the attempt.
 	 * @param world The world in which the attempt is taking place.
 	 * @param x The X coordinate where the item is being placed.
 	 * @param y The Y coordinate where the item is being placed.
 	 * @param z The Z coordinate where the item is being placed.
 	 * @param direction The direction the player is facing.
 	 * @param xOffset The X offset the player is from where the item is being placed.
 	 * @param yOffset The Y offset the player is from where the item is being placed.
 	 * @param zOffset The Z offset the player is from where the item is being placed.
 	 * @return true is the attempt should be allowed; false otherwise.
 	 */
 	public static boolean blockPlaceAttempt(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int direction, float xOffset, float yOffset, float zOffset) {
 		if (!listeners.isEmpty(IPlayerBlockListener.class)) {
 			PlayerBlockEvent event = PlayerBlockEvent.PlaceAttempt(player, itemStack, x, y, z, direction, xOffset, yOffset, zOffset);
 			((IPlayerBlockListener)listeners).onPlayerBlockAction(event);
 			
 			if (!event.isAllowed())
 				return false;
 		}
 		
 		return true;
 	}
 	
 	public static void blockRemoved(EntityPlayer player, Block block, int metadata, int x, int y, int z) {
 		if (block instanceof BlockContainer && !listeners.isEmpty(IContainerListener.class)) {
 			ContainerEvent event = ContainerEvent.Removed(player, block, metadata, x, y, z);
 			((IContainerListener)listeners).onContainerAction(event);
 		}
 	}
 
 	/**
 	 * Asks all mods if the attempt should be allowed. Mods can only say that it's not allowed.
 	 * 
 	 * @param player The player making the attempt.
 	 * @param world The world in which the attempt is taking place.
 	 * @param block The block being removed.
 	 * @param metadata The block's metadata.
 	 * @param x The block X coordinate.
 	 * @param y The block Y coordinate.
 	 * @param z The block Z coordinate.
 	 * @return true if the attempt should be allowed; false otherwise.
 	 */
 	public static boolean blockRemoveAttempt(EntityPlayer player, World world, Block block, int metadata, int x, int y, int z) {
 		if (!listeners.isEmpty(IPlayerBlockListener.class)) {
 			PlayerBlockEvent event = PlayerBlockEvent.RemoveAttempt(player, block, metadata, x, y, z);
 			((IPlayerBlockListener)listeners).onPlayerBlockAction(event);
 
 			if (!event.isAllowed())
 				return false;
 		}
 		
 		return true;
 	}
 	
 	@SuppressWarnings("unused")
 	public static void containerPlaced(EntityPlayer player, Container container, World world, int x, int y, int z) {
 		//TODO: this.items.containerPlaced(player, container, world, x, y, z);
 	}
 	
 	public static void containerOpened(EntityPlayer player, Block block, Container container, int x, int y, int z) {
 		if (!listeners.isEmpty(IContainerListener.class)) {
 			ContainerEvent event = ContainerEvent.Open(player, block, container, x, y, z);
 			((IContainerListener)listeners).onContainerAction(event);
 		}
 	}
 	
 	/**
 	 * Player purposefully drops an item.
 	 * 
 	 * @param player The player that dropped the item.
 	 * @param itemStack The items that were dropped.
 	 * @param mouseButton The mouse button that was used: 0 for left, 1 for right.
 	 */
 	public static void itemDropped(EntityPlayer player, ItemStack itemStack, int mouseButton) {
 		if (!listeners.isEmpty(IDropListener.class)) {
 			DropEvent event;
 			
 			if (mouseButton == 1)
 				event = DropEvent.One(player, itemStack);
 			else
 				event = DropEvent.Stack(player, itemStack);
 
         	((IDropListener)listeners).onPlayerItemDrop(event);
 		}
 	}
 	
 	/**
 	 * An item is dropped from a player's inventory for any reason (purposefully drops an item or closed a crafting window).
 	 * 
 	 * @param player The player that ejected the items.
 	 * @param items The items ejected.
 	 */
 	public static void itemEjected(EntityPlayer player, ItemStack items) {
 		if (!listeners.isEmpty(IDropListener.class)) {
 			DropEvent event = DropEvent.Eject(player, items);
         	((IDropListener)listeners).onPlayerItemDrop(event);
 		}
 	}
 	
 	/**
 	 * All player items are dropped.
 	 * 
 	 * @param player The player that dropped the items.
 	 */
 	public static void itemsDroppedAll(EntityPlayer player) {
 		if (!listeners.isEmpty(IDropListener.class)) {
 			DropEvent event = DropEvent.All(player);
         	((IDropListener)listeners).onPlayerItemDrop(event);
 		}
 	}
 	
 	public static void itemTransfered(EntityPlayer player, Container container, int slotId, ItemStack original) {
 		if (!listeners.isEmpty(ISlotListener.class)) {
         	SlotEvent event = SlotEvent.Transfer(player, container, slotId, original);
         	((ISlotListener)listeners).onSlotAction(event);
 		}
 	}
 	
 	public static void itemAddedToSlot(EntityPlayer player, Container container, Slot clickedSlot, int quantity) {
 		if (!listeners.isEmpty(ISlotListener.class)) {
         	SlotEvent event = SlotEvent.Add(player, container, clickedSlot, quantity);
         	((ISlotListener)listeners).onSlotAction(event);
 		}
 	}
 	
 	public static void itemRemovedFromSlot(EntityPlayer player, Container container, Slot clickedSlot, int quantity) {
 		if (!listeners.isEmpty(ISlotListener.class)) {
         	SlotEvent event = SlotEvent.Remove(player, container, clickedSlot, quantity);
         	((ISlotListener)listeners).onSlotAction(event);
 		}
 	}
 	
 	public static void itemSwitchedWithSlot(EntityPlayer player, Container container, Slot clickedSlot) {
 		if (!listeners.isEmpty(ISlotListener.class)) {
         	SlotEvent event = SlotEvent.Switch(player, container, clickedSlot);
         	((ISlotListener)listeners).onSlotAction(event);
 		}
 	}
 	
 	public static void login(EntityPlayer player) {
 		if (!listeners.isEmpty(IPlayerInstanceListener.class)) {
         	PlayerInstanceEvent event = PlayerInstanceEvent.Login(player);
         	((IPlayerInstanceListener)listeners).onPlayerInstanceAction(event);
 		}
 	}
 	
 	public static void logout(EntityPlayer player) {
 		if (!listeners.isEmpty(IPlayerInstanceListener.class)) {
         	PlayerInstanceEvent event = PlayerInstanceEvent.Logout(player);
         	((IPlayerInstanceListener)listeners).onPlayerInstanceAction(event);
 		}
 	}
 
 	/**
 	 * @param oldPlayerInstance The old instance of {@link EntityPlayer} that is being recreated.
 	 * @return The position the player should respawn at, if set by a mod. <code>null</code> otherwise.
 	 */
 	public static RespawnPosition handleRespawn(EntityPlayer oldPlayerInstance) {
 		if (!listeners.isEmpty(IPlayerInstanceListener.class)) {
         	PlayerInstanceEvent event = PlayerInstanceEvent.Respawn(oldPlayerInstance);
         	((IPlayerInstanceListener)listeners).onPlayerInstanceAction(event);
         	return event.getRespawnPosition();
 		}
 		
 		return null;
 	}
 	
 	public static boolean isPvPEnabled(EntityPlayer player) {
 		if (!listeners.isEmpty(IPlayerInstanceListener.class)) {
         	PlayerInstanceEvent event = PlayerInstanceEvent.CheckMetadata(player, METADATA.IS_PVP);
         	((IPlayerInstanceListener)listeners).onPlayerInstanceAction(event);
         	if (event.isInterrupted()) {
         		return event.getMetadataBooleanValue();
         	}
 		}
 		
 		return MinecraftServer.getServer().isPVPEnabled();
 	}
 	
 	public static void readFromNBT(EntityPlayer player, NBTTagCompound nbtTagCompound) {
 		if (!listeners.isEmpty(IPlayerInstanceListener.class)) {
         	PlayerInstanceEvent event = PlayerInstanceEvent.ReadFromNBT(player, nbtTagCompound);
         	((IPlayerInstanceListener)listeners).onPlayerInstanceAction(event);
 		}
 	}
 	
 	public static void writeToNBT(EntityPlayer player, NBTTagCompound nbtTagCompound) {
 		if (!listeners.isEmpty(IPlayerInstanceListener.class)) {
         	PlayerInstanceEvent event = PlayerInstanceEvent.WriteToNBT(player, nbtTagCompound);
         	((IPlayerInstanceListener)listeners).onPlayerInstanceAction(event);
 		}
 	}
 
 	public static void onAttackedByPlayer(Entity attackedEntity, EntityPlayer player) {
 		if (!listeners.isEmpty(IPlayerActionListener.class)) {
 			PlayerActionEvent event = PlayerActionEvent.AttackedByPlayer(attackedEntity, player);
         	((IPlayerActionListener)listeners).onPlayerAction(event);
 		}
 	}
 	
 	public static void onPlayerMetadataChanged(EntityPlayer player, PlayerInstanceEvent.METADATA metadata) {
 		if (!listeners.isEmpty(IPlayerInstanceListener.class)) {
         	PlayerInstanceEvent event = PlayerInstanceEvent.MetadataChanged(player, metadata);
         	((IPlayerInstanceListener)listeners).onPlayerInstanceAction(event);
 		}
 	}
 	
 	public static void onPlayerMetadataChanged(EntityPlayer player, PlayerInstanceEvent.METADATA metadata, Object newValue) {
 		if (!listeners.isEmpty(IPlayerInstanceListener.class)) {
         	PlayerInstanceEvent event = PlayerInstanceEvent.MetadataChanged(player, metadata, newValue);
         	((IPlayerInstanceListener)listeners).onPlayerInstanceAction(event);
 		}
 	}
 }
