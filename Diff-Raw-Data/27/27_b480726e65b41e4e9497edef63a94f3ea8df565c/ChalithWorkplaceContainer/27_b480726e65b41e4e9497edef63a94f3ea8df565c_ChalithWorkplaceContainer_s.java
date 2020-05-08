 package tektor.minecraft.chalith.container;
 
 import org.apache.commons.lang3.StringUtils;
 
 import tektor.minecraft.chalith.ChalithBase;
 import tektor.minecraft.chalith.entity.ChalithWorkplaceTileEntity;
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.ContainerRepair;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.InventoryCraftResult;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 
 public class ChalithWorkplaceContainer extends Container {
 	/** Here comes out item you merged and/or renamed. */
 	IInventory outputSlot = new InventoryCraftResult();
 	// true -> rename, false -> copy
 	boolean lastOp = false;
 	boolean worked = false;
 
 	/**
 	 * The 2slots where you put your items in that you want to merge and/or
 	 * rename.
 	 */
 	IInventory inputSlots = new ChalithWorkbenchContainerInner(this,
 			"Rune Workbench", true, 2);
 
 	protected ChalithWorkplaceTileEntity tileEntity;
 	private String repairedItemName;
 
 	public ChalithWorkplaceContainer(InventoryPlayer inventoryPlayer,
 			ChalithWorkplaceTileEntity te) {
 		tileEntity = te;
 		te.container = this;
 
 		// restrict slot 1
 		ItemStack[] slot1 = new ItemStack[2];
 		slot1[0] = new ItemStack(ChalithBase.utilRune, 1, 0);
 		slot1[1] = new ItemStack(ChalithBase.utilRune, 1, 3);
 		addSlotToContainer(new RestrictingSlot(this.inputSlots, 0, 29, 32,
 				slot1, null));
 		// restrict slot 2
 		ItemStack[] slot2 = new ItemStack[2];
 		slot2[0] = new ItemStack(ChalithBase.utilRune, 1, 0);
 		slot2[1] = new ItemStack(ChalithBase.utilRune, 1, 3);
 		addSlotToContainer(new RestrictingSlot(this.inputSlots, 1, 29, 54,
 				slot2, null));
 		// restrict slot 3
 		ItemStack[] slot3 = new ItemStack[0];
 		addSlotToContainer(new RestrictingSlot(this.outputSlot, 2, 119, 40,
 				slot3, this));
 		bindPlayerInventory(inventoryPlayer);
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer player) {
 		return tileEntity.isUseableByPlayer(player);
 	}
 
 	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
 		for (int i = 0; i < 3; i++) {
 			for (int j = 0; j < 9; j++) {
 				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
 						8 + j * 18, 84 + i * 18));
 			}
 		}
 
 		for (int i = 0; i < 9; i++) {
 			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
 		}
 	}
 
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
 		ItemStack stack = null;
 		Slot slotObject = (Slot) inventorySlots.get(slot);
 
 		// null checks
 		if (slotObject != null) {
 			ItemStack stackInSlot = slotObject.getStack();
 			if (stackInSlot != null) {
 				stack = stackInSlot.copy();
 				// merges the item into player inventory since its in the
 				// tileEntity
 				if (slot < 4) {
 					if (!this.mergeItemStack(stackInSlot, 4, 39, true)) {
 						return null;
 					}
 				}
 				// places it into the tileEntity is possible since its in the
 				// player
 				// inventory
 				else if (!this.mergeItemStack(stackInSlot, 0, 4, false)) {
 					return null;
 				}
 				if (stackInSlot.stackSize == 0) {
 					slotObject.putStack(null);
 				} else {
 					slotObject.onSlotChanged();
 				}
 				if (stackInSlot.stackSize == stack.stackSize) {
 					return null;
 				}
 				slotObject.onPickupFromSlot(player, stackInSlot);
 			}
 		}
 
 		return stack;
 	}
 
 	@Override
 	protected boolean mergeItemStack(ItemStack par1ItemStack, int par2,
 			int par3, boolean par4) {
 		boolean success = false;
 		for (int i = par2; i < par3; i++) {
 			if (!this.getSlot(i).isItemValid(par1ItemStack)) {
 				continue;
 			}
 			success = success
 					|| super.mergeItemStack(par1ItemStack, i, i + 1, par4);
 		}
 		return success;
 
 	}
 
 	public void updateItemName(String par1Str) {
 		this.repairedItemName = par1Str;
 
 		if (this.outputSlot.getStackInSlot(0) != null) {
 			ItemStack itemstack = this.outputSlot.getStackInSlot(0);
 
 			if (StringUtils.isBlank(par1Str)) {
 				itemstack.func_135074_t();
 			} else {
 				itemstack.setItemName(this.repairedItemName);
 			}
 		}
 		worked =false;
 		this.updateOutput();
 	}
 
 	public void onCraftMatrixChanged(IInventory par1IInventory) {
 
 		super.onCraftMatrixChanged(par1IInventory);
 		worked = false;
 		if (par1IInventory == this.inputSlots) {
 
 			this.updateOutput();
		} else if (par1IInventory == this.outputSlot) {
			this.updateInput();
 		}
 
 	}
 
 	public void updateInput() {
 		if (this.outputSlot.getStackInSlot(0) == null) {
 			if (lastOp) {
 				this.inputSlots.setInventorySlotContents(0, null);
 			} else {
 				this.inputSlots.setInventorySlotContents(1, null);
 			}
 		}
 
 	}
 
 	private void updateOutput() {
 
		if (this.inputSlots.getStackInSlot(0) == null) {
 			this.outputSlot.setInventorySlotContents(0, (ItemStack) null);
		}
 		if (this.inputSlots.getStackInSlot(0) != null && !worked) {
 			ItemStack itemstack = this.inputSlots.getStackInSlot(0);
 			if (itemstack == null) {
 				this.outputSlot.setInventorySlotContents(0, (ItemStack) null);
 			} else {
 				if (this.outputSlot.getStackInSlot(0) == null) {
 					ItemStack itemstack1 = this.inputSlots.getStackInSlot(0)
 							.copy();
 					ItemStack itemstack2 = this.inputSlots.getStackInSlot(1);
 					if (itemstack2 != null
 							&& itemstack2.stackTagCompound == null) {
 						this.outputSlot.setInventorySlotContents(1, itemstack1);
 						lastOp = false;
 						worked = true;
 					} else if (itemstack2 == null) {
 						this.outputSlot.setInventorySlotContents(0, itemstack1);
 						worked = true;
 						lastOp = true;
 						System.out.println("name in field:" + repairedItemName);
 						if (!StringUtils.isBlank(repairedItemName)) {
 							itemstack1.stackTagCompound.setString("name",
 									repairedItemName);
 
 						}
 					} else {
 						this.outputSlot.setInventorySlotContents(0,
 								(ItemStack) null);
 					}
 				}
				else
				{
					System.out.println("name in field:" + repairedItemName);
					if (!StringUtils.isBlank(repairedItemName)) {
						this.outputSlot.getStackInSlot(0).stackTagCompound.setString("name",
								repairedItemName);
						worked = true;
				}
 				}
 			}
		}
 		this.detectAndSendChanges();
 
 	}
 
 	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
 		super.onContainerClosed(par1EntityPlayer);
 
 		if (!par1EntityPlayer.worldObj.isRemote) {
 			for (int i = 0; i < this.inputSlots.getSizeInventory(); ++i) {
 				ItemStack itemstack = this.inputSlots
 						.getStackInSlotOnClosing(i);
 
 				if (itemstack != null) {
 					par1EntityPlayer.dropPlayerItem(itemstack);
 				}
 			}
 		}
 	}
 
 }
