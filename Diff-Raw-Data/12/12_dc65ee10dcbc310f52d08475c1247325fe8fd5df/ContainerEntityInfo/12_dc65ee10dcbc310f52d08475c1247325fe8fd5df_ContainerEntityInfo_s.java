 package demonmodders.crymod.common.gui;
 
 import java.util.Iterator;
 
 import cpw.mods.fml.relauncher.Side;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 import demonmodders.crymod.common.entities.SummonableBase;
 import demonmodders.crymod.common.inventory.InventorySummonable;
 import demonmodders.crymod.common.inventory.SlotArmor;
 import demonmodders.crymod.common.inventory.SlotNoPickup;
 import demonmodders.crymod.common.items.ItemCryMod;
 
 public class ContainerEntityInfo extends AbstractContainer<InventorySummonable> {
 
 	public static final int BUTTON_CONFIRM = 0;
 	public static final int BUTTON_RENAME = 1;
 	public static final int BUTTON_RENAME_DONE = 2;
 	
 	private final SummonableBase entity;
 	private final InventoryPlayer playerInventoryCopy;
 	private final EntityPlayer player;
	private boolean applyChanges;
 	private String newName;
 		
 	public ContainerEntityInfo(SummonableBase entity, EntityPlayer player) {
 		super(new InventorySummonable(entity));
 		this.entity = entity;
 		this.player = player;
 		playerInventoryCopy = new InventoryPlayer(player);
 		playerInventoryCopy.copyInventory(player.inventory);
 		
 		addSlotToContainer(new SlotArmor(inventory, 4, 160, 13, 0));
 		addSlotToContainer(new SlotArmor(inventory, 3, 160, 40, 1));
 		addSlotToContainer(new SlotArmor(inventory, 2, 214, 13, 2));
 		addSlotToContainer(new SlotArmor(inventory, 1, 214, 40, 3));
 		addSlotToContainer(new Slot(inventory, 0, 186, 64));
 		
 		for (int k = 0; k < 9; k++) {
 			addSlotToContainer(new Slot(player.inventory, k, 41 + k * 18, 171));
         }
 		
 		for (int i = 0; i < 5; i++) {
 			inventory.setInventorySlotContents(i, entity.getCurrentItemOrArmor(i));
 		}
 		newName = entity.getEntityName();
 	}
 	
 	public SummonableBase getEntity() {
 		return entity;
 	}
 	
 	public void setNewName(String name) {
 		newName = name;
 	}
 
 	@Override
 	public void onCraftGuiClosed(EntityPlayer player) {
 		super.onCraftGuiClosed(player);
 		entity.onPlayerCloseGui();
 		
 		if (!applyChanges) {
 			player.inventory.copyInventory(playerInventoryCopy);
 		}
 	}
 
 	@Override
 	public void buttonClick(int buttonId, Side side, EntityPlayer player) {
 		switch (buttonId) {
 		case BUTTON_CONFIRM:
 			int levelCost = calculateCurrentXpCost();
 			applyChanges = levelCost <= player.experienceLevel;
 			
 			if (side.isServer()) {
 				if (levelCost <= player.experienceLevel) {
 					
 					player.addExperienceLevel(-levelCost);
 					
 					for (int i = 0; i < inventory.getSizeInventory(); i++) {
 						entity.setCurrentItemOrArmor(i, inventory.getStackInSlotOnClosing(i));
 					}
 					
 					if (newName != null) {
 						entity.setName(newName);
 					}
 				}
 				
 				player.closeScreen();
 			}
 			break;
 		}
 	}
 	
 	public int calculateCurrentXpCost() {
 		int xpLevelCost = 0;
 		
 		for (int i = 0; i < inventory.getSizeInventory(); i++) {
 			if (!ItemStack.areItemStacksEqual(inventory.getStackInSlot(i), entity.getCurrentItemOrArmor(i))) {
 				xpLevelCost += i == 0 ? 2 : 1; // 1 level for each armor, 2 for equipped item
 			}
 		}
 		
 		if (!entity.getEntityName().equals(newName)) {
 			xpLevelCost += 1; // +1 for name change
 		}
 		return xpLevelCost;			
 	}
 
 	@Override
 	public boolean handleButtonClick(int buttonId) {
 		return buttonId == BUTTON_CONFIRM;
 	}
 
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer player, int slotId) {
 		Slot slotToTransfer = getSlot(slotId);
 		if (slotToTransfer == null || !slotToTransfer.getHasStack()) {
 			return null;
 		}
 		
 		ItemStack stackToTransfer = slotToTransfer.getStack();
 		ItemStack oldStack = stackToTransfer.copy();
 		
 		if (slotId < inventory.getSizeInventory()) { // transfer from entity to inventory
 			if (!mergeItemStack(stackToTransfer, inventory.getSizeInventory(), inventory.getSizeInventory() + 9, true)) {
 				return null;
 			}
 		} else { // transfer from inventory
 			if (stackToTransfer.getItem() instanceof ItemArmor) { // transfer into armor slots
 				int armorType = ((ItemArmor)stackToTransfer.getItem()).armorType;
 				if (!mergeItemStack(stackToTransfer, armorType, armorType + 1, false)) {
 					return null;
 				}
 			} else if (!mergeItemStack(stackToTransfer, 4, 5, false)) { // transfer into the held item slot
 				return null;
 			}
 		}
 		
 		if (stackToTransfer.stackSize == 0) {
 			slotToTransfer.putStack(null); // completely transferred
 		} else {
 			slotToTransfer.onSlotChanged(); // only partially transferred
 		}
 		
 		if (stackToTransfer.stackSize == oldStack.stackSize) {
 			return null; // we cannot transfer this stack
 		}
 		
 		return oldStack;
 	}
 }
