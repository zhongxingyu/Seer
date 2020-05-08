 package btwmods.api.player.events;
 
 import java.util.EventObject;
 
 import btwmods.api.player.PlayerAPI;
 import btwmods.api.player.events.DropEvent.TYPE;
 import net.minecraft.src.Container;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Slot;
 
 public class SlotEvent extends EventObject {
 	
 	public enum TYPE { ADD, REMOVE, SWITCH, TRANSFER };
 
 	private TYPE type;
 	private PlayerAPI api;
 	private Container container;
 	private int slotId = -1;
 	private Slot slot = null;
 	private ItemStack slotItems = null;
 	private ItemStack heldItems = null;
 	private ItemStack originalItems = null;
 	private ItemStack remainingItems = null;
 	private int quantity = -1;
 	
 	public TYPE getType() {
 		return type;
 	}
 	
 	public PlayerAPI getApi() {
 		return api;
 	}
 	
 	public Container getContainer() {
 		return container;
 	}
 	
 	public int getSlotId() {
 		if (slotId == -1)
 			slotId = slot.slotNumber;
 		
 		return slotId;
 	}
 	
 	public Slot getSlot() {
 		if (slot == null)
 			slot = container.getSlot(slotId);
 		
 		return slot;
 	}
 	
 	public ItemStack getSlotItems() {
 		if (slotItems == null && type != TYPE.TRANSFER)
 			slotItems = getSlot().getStack();
 		
 		return slotItems;
 	}
 	
 	public ItemStack getHeldItems() {
 		if (heldItems == null && type != TYPE.TRANSFER)
 			heldItems = api.player.inventory.getItemStack();
 		
 		return heldItems;
 	}
 	
 	public ItemStack getOriginalItems() {
 		return originalItems;
 	}
 	
 	public ItemStack getRemainingItems() {
 		if (remainingItems == null && type == TYPE.TRANSFER)
 			remainingItems = getSlot().getStack();
 		
 		return remainingItems;
 	}
 	
 	public int getQuantity() {
 		if (quantity == -1 && type == TYPE.TRANSFER)
			quantity = getOriginalItems().stackSize - (getRemainingItems() == null ? 0 : getRemainingItems().stackSize);
 		
 		return quantity;
 	}
 	
 	public static SlotEvent Add(PlayerAPI api, Container container, Slot slot, int quantity) {
 		SlotEvent event = new SlotEvent(TYPE.ADD, api, container, slot.slotNumber, slot);
 		event.quantity = quantity;
 		return event;
 	}
 	
 	public static SlotEvent Remove(PlayerAPI api, Container container, Slot slot, int quantity) {
 		SlotEvent event = new SlotEvent(TYPE.REMOVE, api, container, slot.slotNumber, slot);
 		event.quantity = quantity;
 		return event;
 	}
 	
 	public static SlotEvent Switch(PlayerAPI api, Container container, Slot slot) {
 		SlotEvent event = new SlotEvent(TYPE.SWITCH, api, container, slot.slotNumber, slot);
 		return event;
 	}
 	
 	public static SlotEvent Transfer(PlayerAPI api, Container container, int slotId, ItemStack originalItems) {
 		SlotEvent event = new SlotEvent(TYPE.TRANSFER, api, container, slotId, null);
 		event.originalItems = originalItems;
 		return event;
 	}
 	
 	private SlotEvent(TYPE type, PlayerAPI api, Container container, int slotId, Slot slot) {
 		super(api);
 		this.type = type;
 		this.api = api;
 		this.container = container;
 		this.slotId = slotId;
 		this.slot = slot;
 	}
 }
