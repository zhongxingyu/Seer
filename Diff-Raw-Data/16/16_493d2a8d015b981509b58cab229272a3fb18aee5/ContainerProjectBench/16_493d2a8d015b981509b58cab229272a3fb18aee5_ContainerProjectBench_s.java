 package bau5.mods.craftingsuite.common.inventory;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import bau5.mods.craftingsuite.common.handlers.DeepSlotHandler;
 import bau5.mods.craftingsuite.common.handlers.PlanHandler;
 import bau5.mods.craftingsuite.common.handlers.ToolsHandler;
 import bau5.mods.craftingsuite.common.tileentity.IModifiedTileEntityProvider;
 import bau5.mods.craftingsuite.common.tileentity.TileEntityProjectBench;
 
 public class ContainerProjectBench extends ContainerBase{
 	
 	public TileEntityProjectBench tileEntity;
 	private SlotPBCrafting craftingSlot;
 	
 	private int index = 0;
 	
 	public ContainerProjectBench(InventoryPlayer invPlayer, TileEntityProjectBench tpb){
 		super(tpb);
 		tileEntity = tpb;
 		tileEntity.getContainerHandler().containerInit = true;
 		layoutContainer(invPlayer, tileEntity);
 		handleInventoryModifiers();
 		craftingSlot.handler = handler;
 		tileEntity.getContainerHandler().containerInit = false;
 		tileEntity.getInventoryHandler().findRecipe(false);
 	}
 
 	private void layoutContainer(InventoryPlayer invPlayer, TileEntityProjectBench tile) {
 		craftingSlot = new SlotPBCrafting(invPlayer.player, tileEntity, handler, tileEntity.inventoryHandler.resultMatrix(), 0, 124, 35);
 		addSlotToContainer(craftingSlot);
 		craftingSlotIndex = craftingSlot.slotNumber;
 		int row;
 		int col;
 		Slot slot = null;
 		
 		int xShift = 0;
 		if(handler instanceof DeepSlotHandler){
 			xShift = 1;
 		}
 
 		for(row = 0; row < 3; row++)
 		{
 			for(col = 0; col < 3; col++)
 			{
 				slot = new Slot(tileEntity, index++, 30 + col * 18, 17 + row * 18);
 				addSlotToContainer(slot);
 			}
 		}
 		
 		for(row = 0; row < 2; row++)
 		{	
 			for(col = 0; col < 9; col++)
 			{
 				if(row == 1)
 				{
 					slot = new Slot(tileEntity, 18 + col, 8 + col * 18, 
 									(row * 2 - 1) + 77 + row * 18);
 					addSlotToContainer(slot);
 				} else
 				{
 					slot = new Slot(tileEntity, 9 + col, 8 + col * 18,
 							77 + row * 18);
 					addSlotToContainer(slot);
 				}
 			}
 		}		
 		for(int i = 0; i < 3; i++)
 		{
 			for(int j = 0; j < 9; j++)
 			{
 				addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9,
 											8 + j * 18, 82 + i * 18 + 39));
 			}
 		}
 		for(int i = 0; i < 9; i++)
 		{
 			addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + 37));
 		}
 	}
 
 	@Override
 	public ItemStack slotClick(int slot, int clickType, int clickMeta,
 			EntityPlayer player) {
 		ItemStack lastCraftResult = tileEntity.getInventoryHandler().resultMatrix().getStackInSlot(0);
 		lastCraftResult = lastCraftResult != null ? lastCraftResult.copy() : null;
 		ItemStack stack = super.slotClick(slot, clickType, clickMeta, player);
 		if(slot == 0){
 			if(!ItemStack.areItemStacksEqual(lastCraftResult, tileEntity.getInventoryHandler().resultMatrix().getStackInSlot(0)))
 				tileEntity.getInventoryHandler().findRecipe(false);
 		}
 		return stack;
 	}
 	
 	@Override
 	public boolean canInteractWith(EntityPlayer player) {
 		return tileEntity.isUseableByPlayer(player);
 	}
 	
 	@Override
 	public void putStacksInSlots(ItemStack[] par1ArrayOfItemStack) {
 		tileEntity.containerInit = true;
 		tileEntity.containerHandler.containerInit = true;
 		super.putStacksInSlots(par1ArrayOfItemStack);
 		tileEntity.containerInit = false;
 		tileEntity.containerHandler.containerInit = false;
 		tileEntity.onInventoryChanged();
 	}
 	
 	@Override
 	public void putStackInSlot(int slot, ItemStack itemStack) {
 //		if(!tileEntity.worldObj.isRemote)
 			tileEntity.containerHandler.containerInit = true;
 		super.putStackInSlot(slot, itemStack);
 		tileEntity.containerHandler.containerInit = false;
 	}
 	
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer player, int numSlot)
     {
         ItemStack stack = null;
         Slot slot = (Slot)this.inventorySlots.get(numSlot);
        if(handler.handlesThisTransfer(numSlot, slot.getStack())){
         	ItemStack stack2 = handler.handleTransferClick(player, numSlot);
         	return stack2;
         }
         
         if (slot != null && slot.getHasStack())
         {
         	boolean flag = false;
             ItemStack stack2 = slot.getStack();
             stack = stack2.copy();
             //TODO PLANS
             if(!flag){
             	if(tileEntity.getInventoryModifier() == EnumInventoryModifier.TOOLS 
             			&& stack.isItemStackDamageable() && numSlot != 0){
             		if(numSlot < 64){
 	            		if (!this.mergeItemStack(stack2, 64, 67, false)){
 	            			if(!this.mergeItemStack(stack2, 10, 28, false)){
 	            				return null;
 	            			}
 	            		}
             		}else if(numSlot >= 64 && numSlot <= 66){
             			if(!this.mergeItemStack(stack2, 28, 63, false)){
             				return null;
             			}
             		}
             	}else if (numSlot == 0)
 	            {
 	                if (!this.mergeItemStack(stack2, 10, 55, false))
 	                {
 	                    return null;
 	                }
 //	                updateCrafting(true);
 	            }
 	            //Merge crafting matrix item with supply matrix inventory
 	            else if(numSlot > 0 && numSlot <= 9)
 	            {
 	            	if(!this.mergeItemStack(stack2, 10, 28, false))
 	            	{
 	            		if(!this.mergeItemStack(stack2, 28, 63, false))
 	            		{
 	                		return null;
 	            		}
 	            	}
 //	            	updateCrafting(true);
 	            }
 	            //Merge Supply matrix item with player inventory
 	            else if (numSlot >= 10 && numSlot <= 27)
 	            {
 	                if (!this.mergeItemStack(stack2, 28, 63, false))
 	                {
 	                    return null;
 	                }
 	            }
 	            //Merge player inventory item with supply matrix
 	            else if (numSlot >= 28 && numSlot < 64)
 	            {
 	                if (!this.mergeItemStack(stack2, 10, 28, false))
 	                {
 	                    return null;
 	                }
 	            }
             }
 
             if (stack2.stackSize == 0)
             {
                 slot.putStack((ItemStack)null);
             }
             else
             {
                 slot.onSlotChanged();
             }
 
             if (stack2.stackSize == stack.stackSize)
             {
                 return null;
             }
 
             slot.onPickupFromSlot(player, stack2);
         }
 
         return stack;
     }
 
 	@Override
 	public EnumInventoryModifier getInventoryModifier() {
 		return tileEntity.getInventoryModifier();
 	}
 
 	@Override
 	public void handleInventoryModifiers() {
 		//TODO this!@!!!
 		switch(getInventoryModifier()){
		case NONE: break;
 		case TOOLS: 
 			SlotTool[] toolSlots = new SlotTool[3];
 			for(int i = 0; i < 3; i++){
 				toolSlots[i] = new SlotTool(tileEntity, tileEntity.getToolModifierInvIndex() +i, -17, 17 + (16*i +(i*2)));
 				this.addSlotToContainer(toolSlots[i]);
 			}
 			handler = new ToolsHandler(this, toolSlots);
 			break;
 		case PLAN: 
 			SlotPlan planSlot = new SlotPlan(tileEntity, 27, 8, 34);
 			this.addSlotToContainer(planSlot);
 			handler = new PlanHandler(this, planSlot);
 			break;
 		case DEEP:
 			SlotDeep slot = new SlotDeep(tileEntity, 27, -17, 34);
 			this.addSlotToContainer(slot);
 			handler = new DeepSlotHandler(this, slot);
 			break;
 		}
 	}
 	
 	@Override
 	public int getSizeInventoryOfTile() {
 		return tileEntity.getSizeInventory();
 	}
 
 	@Override
 	protected int[] getXYZ() {
 		return new int[]{ tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord };
 	}
 	@Override
 	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
 		// TODO Auto-generated method stub
 		super.onContainerClosed(par1EntityPlayer);
 		tileEntity.closeChest();
 	}
 
 	@Override
 	public IModifiedTileEntityProvider getTileEntity() {
 		return tileEntity;
 	}
 }
