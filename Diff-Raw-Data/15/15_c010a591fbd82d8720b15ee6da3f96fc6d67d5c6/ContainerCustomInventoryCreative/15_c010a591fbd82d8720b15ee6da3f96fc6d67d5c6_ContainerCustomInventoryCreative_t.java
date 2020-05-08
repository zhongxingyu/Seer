 package com.chaosdev.playerinventoryapi.inventory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.chaosdev.playerinventoryapi.api.ICustomPlayerContainer;
 import com.chaosdev.playerinventoryapi.api.ISlotHandler;
 import com.chaosdev.playerinventoryapi.lib.GuiHelper.GuiPos;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.*;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 
 public class ContainerCustomInventoryCreative extends Container implements ICustomPlayerContainer
 {
 	public InventoryCrafting			craftMatrix				= new InventoryCrafting(this, 2, 2);
 	public IInventory					craftResult				= new InventoryCraftResult();
 	
 	public boolean						isLocalWorld			= false;
 	public final EntityPlayer			thePlayer;
 	
 	public static final GuiPos[]		defaultSlotPositions	= getDefaultSlotPositions();
 	public static GuiPos[]				slotPositions			= defaultSlotPositions.clone();
 	public static List<ISlotHandler>	slotHandlers			= new ArrayList<ISlotHandler>();
 	
 	public ContainerCustomInventoryCreative(InventoryPlayer par1InventoryPlayer, boolean par2, EntityPlayer par3EntityPlayer)
 	{
 		this.isLocalWorld = par2;
 		this.thePlayer = par3EntityPlayer;
 		
 		this.thePlayer.inventoryContainer = this;
 		this.thePlayer.openContainer = this;
 		
 		List<Slot> slots = createSlots();
 		int defaultSlots = slots.size();
 		
 		for (ISlotHandler handler : slotHandlers)
 			handler.addSlots(slots, thePlayer, false);
 		
 		for (int i = 0; i < slots.size(); i++)
 		{
 			Slot slot = slots.get(i);
 			addSlotToContainer(slot);
 			
 			if (i >= defaultSlots)
 				slotPositions[slot.slotNumber] = new GuiPos(slot.xDisplayPosition, slot.yDisplayPosition);
 		}
 		
 		this.onCraftMatrixChanged(this.craftMatrix);
 	}
 	
 	@Override
 	public List<Slot> createSlots()
 	{
 		List<Slot> slots = new ArrayList<Slot>(slotPositions.length);
 		GuiPos[] pos = slotPositions;
 		slots.add(new SlotCrafting(this.thePlayer, this.craftMatrix, this.craftResult, 0, pos[0].getX(), pos[0].getY()));
 		
 		int i;
 		int j;
 		
 		for (i = 0; i < 2; ++i)
 		{
 			for (j = 0; j < 2; ++j)
 			{
 				int index = j + i * 2;
 				slots.add(new Slot(this.craftMatrix, index, pos[1 + index].getX(), pos[1 + index].getY()));
 			}
 		}
 		
 		for (i = 0; i < 4; ++i)
 		{
 			slots.add(new SlotCustomArmor(this.thePlayer, this.thePlayer.inventory, this.thePlayer.inventory.getSizeInventory() - 1 - i, pos[8 - i].getX(), pos[8 - i].getY(), i));
 		}
 		
 		for (i = 0; i < 3; ++i)
 		{
 			for (j = 0; j < 9; ++j)
 			{
 				int index = j + (i + 1) * 9;
 				slots.add(new Slot(this.thePlayer.inventory, index, pos[index].getX(), pos[index].getY()));
 			}
 		}
 		
 		for (i = 0; i < 9; ++i)
 		{
 			int index = i + 36;
 			slots.add(new Slot(this.thePlayer.inventory, i, pos[index].getX(), pos[index].getY()));
 		}
 		
 		return slots;
 	}
 	
 	public static GuiPos[] getDefaultSlotPositions()
 	{
 		GuiPos[] pos = new GuiPos[128];
 		int i;
 		int j;
 		
 		// 44 = Crafting output
 		pos[0] = new GuiPos(-2000, -2000);
 		
 		// 1 - 4 = Crafting
 		for (i = 0; i < 2; ++i)
 		{
 			for (j = 0; j < 2; ++j)
 			{
 				pos[1 + j + i * 2] = new GuiPos(-2000, -2000);
 			}
 		}
 		
 		// 5 - 8 = Armor
 		for (i = 0; i < 4; ++i)
 		{
			pos[8 - i] = new GuiPos(45 + i * 18, 6);
 		}
 		
 		// 9 - 35 = Inventory
 		for (i = 0; i < 3; ++i)
 		{
 			for (j = 0; j < 9; ++j)
 			{
 				pos[j + (i + 1) * 9] = new GuiPos(9 + j * 18, 54 + i * 18);
 			}
 		}
 		
 		// 36 - 44 = Hotbar
 		for (i = 0; i < 9; ++i)
 		{
 			pos[36 + i] = new GuiPos(9 + i * 18, 112);
 		}
 		return pos;
 	}
 	
 	public static void setSlotPos(int slotid, int x, int y)
 	{
 		if (slotid < slotPositions.length)
 			slotPositions[slotid] = new GuiPos(x, y);
 		else
 			throw new IllegalArgumentException("Tried to set the slot position of a slot that does not exist - Add that slot first.");
 	}
 	
 	public static void addSlotHandler(ISlotHandler slothandler)
 	{
 		slotHandlers.add(slothandler);
 	}
 	
 	public static void resetSlots()
 	{
		slotPositions = getDefaultSlotPositions();
 	}
 	
 	@Override
 	public EntityPlayer getPlayer()
 	{
 		return thePlayer;
 	}
 	
 	/**
 	 * Callback for when the crafting matrix is changed.
 	 */
 	@Override
 	public void onCraftMatrixChanged(IInventory par1IInventory)
 	{
 		this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.thePlayer.worldObj));
 	}
 	
 	/**
 	 * Callback for when the crafting gui is closed.
 	 */
 	@Override
 	public void onContainerClosed(EntityPlayer par1EntityPlayer)
 	{
 		super.onContainerClosed(par1EntityPlayer);
 		
 		for (int i = 0; i < 4; ++i)
 		{
 			ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);
 			
 			if (itemstack != null)
 			{
 				par1EntityPlayer.dropPlayerItem(itemstack);
 			}
 		}
 		this.craftResult.setInventorySlotContents(0, (ItemStack) null);
 	}
 	
 	@Override
 	public boolean canInteractWith(EntityPlayer par1EntityPlayer)
 	{
 		return true;
 	}
 	
 	/**
 	 * Called when a player shift-clicks on a slot. You must override this or
 	 * you will crash when someone does that.
 	 */
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
 	{
 		ItemStack itemstack = null;
 		Slot slot = (Slot) this.inventorySlots.get(par2);
 		
 		if (slot != null && slot.getHasStack())
 		{
 			ItemStack itemstack1 = slot.getStack();
 			itemstack = itemstack1.copy();
 			
 			if (par2 == 0)
 			{
 				if (!this.mergeItemStack(itemstack1, 9, 45, true))
 				{
 					return null;
 				}
 				
 				slot.onSlotChange(itemstack1, itemstack);
 			}
 			else if (par2 >= 1 && par2 < 5)
 			{
 				if (!this.mergeItemStack(itemstack1, 9, 45, false))
 				{
 					return null;
 				}
 			}
 			else if (par2 >= 5 && par2 < 9)
 			{
 				if (!this.mergeItemStack(itemstack1, 9, 45, false))
 				{
 					return null;
 				}
 			}
 			else if (itemstack.getItem() instanceof ItemArmor && !((Slot) this.inventorySlots.get(5 + ((ItemArmor) itemstack.getItem()).armorType)).getHasStack())
 			{
 				int j = 5 + ((ItemArmor) itemstack.getItem()).armorType;
 				
 				if (!this.mergeItemStack(itemstack1, j, j + 1, false))
 				{
 					return null;
 				}
 			}
 			else if (par2 >= 9 && par2 < 36)
 			{
 				if (!this.mergeItemStack(itemstack1, 36, 45, false))
 				{
 					return null;
 				}
 			}
 			else if (par2 >= 36 && par2 < 45)
 			{
 				if (!this.mergeItemStack(itemstack1, 9, 36, false))
 				{
 					return null;
 				}
 			}
 			else if (!this.mergeItemStack(itemstack1, 9, 45, false))
 			{
 				return null;
 			}
 			
 			if (itemstack1.stackSize == 0)
 			{
 				slot.putStack((ItemStack) null);
 			}
 			else
 			{
 				slot.onSlotChanged();
 			}
 			
 			if (itemstack1.stackSize == itemstack.stackSize)
 			{
 				return null;
 			}
 			
 			slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
 		}
 		
 		return itemstack;
 	}
 	
 	@Override
 	public boolean func_94530_a(ItemStack par1ItemStack, Slot par2Slot)
 	{
 		return par2Slot.inventory != this.craftResult && super.func_94530_a(par1ItemStack, par2Slot);
 	}
 }
