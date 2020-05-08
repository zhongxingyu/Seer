 package com.qzx.au.extras;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.ItemDye;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 
 import com.qzx.au.core.SidedBlockInfo;
 import com.qzx.au.core.SidedSlotInfo;
 import com.qzx.au.core.TileEntityAU;
 
 public class TileEntityChromaInfuser extends TileEntityAU {
 	public static int SLOT_ITEM_INPUT = 0;
 	public static int SLOT_ITEM_OUTPUT = 1;
 	public static int SLOT_DYE_INPUT = 2;
 	public static int SLOT_WATER_BUCKET = 3;
 
 	private ChromaButton recipeButton;
 	private boolean isLocked;
 	private boolean hasWater;
 	private int dyeColor;
 	private int dyeVolume; // 0-8
 
 	private int outputTick;
 	private ItemStack processingItem;
 	private ChromaRecipe processingRecipe;
 
 	public static final int outputTickMax = 20;
 
 	public TileEntityChromaInfuser(){
 		super();
 		this.nrSlots = 4; // item input(top), item output(bottom), dye inventory(side), water bucket inventory
 		this.slotContents = new ItemStack[this.nrSlots];
 
 		this.recipeButton = ChromaButton.BUTTON_BLANK;
 		this.isLocked = true;
 		this.hasWater = false;
 		this.dyeColor = 0;
 		this.dyeVolume = 0;
 		this.outputTick = 0;
 		this.processingItem = null;
 		this.processingRecipe = null;
 
 		this.sidedBlockInfo = new SidedBlockInfo(TileEntityChromaInfuser.sidedSlotInfo,
 												TileEntityChromaInfuser.sidedSlots,
 												TileEntityChromaInfuser.sidedCanToggle,
 												TileEntityChromaInfuser.sidedIsVisible);
 	}
 
 	private static final SidedSlotInfo[] sidedSlotInfo = {
 		(new SidedSlotInfo("Item Input",		SidedSlotInfo.BLUE_COLOR,	TileEntityChromaInfuser.SLOT_ITEM_INPUT,	1,	true, true){
 				@Override
 				public boolean isItemValid(ItemStack itemstack){
 					return ChromaRegistry.hasRecipe(itemstack);
 				}
 			}),
 		(new SidedSlotInfo("Dye Input",		SidedSlotInfo.PURPLE_COLOR,	TileEntityChromaInfuser.SLOT_DYE_INPUT,		1,	true, true){
 				@Override
 				public boolean isItemValid(ItemStack itemstack){
 					return itemstack.getItem() instanceof ItemDye;
 				}
 			}),
 		new SidedSlotInfo("Item Output",	SidedSlotInfo.ORANGE_COLOR,	TileEntityChromaInfuser.SLOT_ITEM_OUTPUT,	1,	false, true)
 	};
 	private static final int[] sidedSlots = {2, 0, 1, 1, 1, 1}; // bottom:itemOutput, top:itemInput, sides:dyeInput
 	private static final boolean[] sidedCanToggle = {false, false, false, false, false, false};
 	private static final boolean[] sidedIsVisible = {false, false, false, false, false, false};
 
 	//////////
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbt){
 		super.readFromNBT(nbt);
 
 		this.recipeButton = ChromaButton.values()[nbt.getByte("recipeButton")];
 		this.isLocked = nbt.getBoolean("isLocked");
 		this.hasWater = nbt.getBoolean("hasWater");
 		this.dyeColor = nbt.getByte("dyeColor");
 		this.dyeVolume = nbt.getByte("dyeVolume");
 		this.processingItem = this.getInput();
 		this.processingRecipe = ChromaRegistry.getRecipe(this.recipeButton, this.getInput());
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbt){
 		super.writeToNBT(nbt);
 
 		nbt.setByte("recipeButton", (byte)this.recipeButton.ordinal());
 		nbt.setBoolean("isLocked", this.isLocked);
 		nbt.setBoolean("hasWater", this.hasWater);
 		nbt.setByte("dyeColor", (byte)this.dyeColor);
 		nbt.setByte("dyeVolume", (byte)this.dyeVolume);
 	}
 
 	//////////
 
 	@Override
 	public ContainerChromaInfuser getContainer(InventoryPlayer inventory){
 		return new ContainerChromaInfuser(inventory, this);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public GuiChromaInfuser getGuiContainer(InventoryPlayer inventory){
 		return new GuiChromaInfuser(inventory, this);
 	}
 
 	//////////
 
 	@Override
 	public boolean canRotate(){
 		return false;
 	}
 
 	@Override
 	public boolean canCamo(){
 		return false;
 	}
 
 	@Override
 	public boolean canOwn(){
 		return false;
 	}
 
 	//////////
 
 	private static final int CLIENT_EVENT_RECIPE_BUTTON			= 100;
 	private static final int CLIENT_EVENT_LOCKED_BUTTON			= 101;
 	private static final int CLIENT_EVENT_RESET_WATER			= 102;
 	private static final int CLIENT_EVENT_SET_COLOR				= 103;
 	private static final int CLIENT_EVENT_UPDATE_OUTPUT			= 104;
 
 	public ItemStack getInput(){
 		return this.slotContents[TileEntityChromaInfuser.SLOT_ITEM_INPUT];
 	}
 
 	public ChromaButton getRecipeButton(){
 		return this.recipeButton;
 	}
 	public void setRecipeButton(ChromaButton button){
 		this.recipeButton = button;
 		this.updateInput(this.getInput());
 
 		this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType().blockID, TileEntityChromaInfuser.CLIENT_EVENT_RECIPE_BUTTON, button.ordinal());
 		this.markChunkModified();
 	}
 
 	public boolean getLocked(){
 		return this.isLocked;
 	}
 	public void setLocked(boolean locked){
 		this.isLocked = locked;
 
 		if(locked) this.outputTick = 0;
 
 		this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType().blockID, TileEntityChromaInfuser.CLIENT_EVENT_LOCKED_BUTTON, locked ? 1 : 0);
 		this.markChunkModified();
 	}
 	public float getOutputTick(){
 		return (float)this.outputTick / (float)TileEntityChromaInfuser.outputTickMax;
 	}
 
 	public boolean getWater(){
 		return this.hasWater;
 	}
 	public void resetWater(){
 		this.hasWater = true;
 		this.dyeVolume = 0;
 
 		this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType().blockID, TileEntityChromaInfuser.CLIENT_EVENT_RESET_WATER, 0);
 		this.markChunkModified();
 
 		this.consumeDye();
 	}
 
 	public int getDyeColor(){
 		return this.dyeColor;
 	}
 	public int getDyeVolume(){
 		return this.dyeVolume;
 	}
 	public void consumeDye(){
 		this.outputTick = 0;
 
 		if(this.hasWater && this.dyeVolume == 0){
 			ItemStack itemstack = this.slotContents[TileEntityChromaInfuser.SLOT_DYE_INPUT];
 			if(itemstack != null){
 				int color = itemstack.getItemDamage();
 				this.dyeVolume = 8;
 				this.dyeColor = color;
 
 				itemstack.stackSize--;
 				if(itemstack.stackSize == 0) this.slotContents[TileEntityChromaInfuser.SLOT_DYE_INPUT] = null;
 
 				this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType().blockID, TileEntityChromaInfuser.CLIENT_EVENT_SET_COLOR, color);
 				this.markChunkModified();
 			}
 		}
 	}
 
 	//////////
 
 	private void updateInput(ItemStack input){
 		this.processingItem = input;
 		this.processingRecipe = ChromaRegistry.getRecipe(this.recipeButton, input);
 		this.outputTick = 0;
 	}
 
 	private boolean canOutput(ItemStack output){
 		if(output == null) return true;
 		if(output.stackSize == output.getMaxStackSize()) return false;
 		return (this.processingRecipe.output.getItem().itemID == output.getItem().itemID && this.processingRecipe.getOutputColor(this.dyeColor) == output.getItemDamage());
 	}
 
 	private void updateOutput(ItemStack input, ItemStack output, ChromaRecipe recipe){
 		int recipe_itemID = recipe.output.getItem().itemID;
 		if(output != null){
 			// output slot has an item in it, abort if not same item
 			output.stackSize += recipe.output.stackSize;
 		} else {
 			// output slot is empty, create new item
 			this.slotContents[TileEntityChromaInfuser.SLOT_ITEM_OUTPUT] = new ItemStack(recipe_itemID, recipe.output.stackSize, recipe.getOutputColor(this.dyeColor));
 		}
 
 		input.stackSize--;
 		if(input.stackSize == 0) this.slotContents[TileEntityChromaInfuser.SLOT_ITEM_INPUT] = null;
 
 		this.dyeVolume--;
 		this.consumeDye();
 
 		this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType().blockID, TileEntityChromaInfuser.CLIENT_EVENT_UPDATE_OUTPUT, 0);
 		this.markChunkModified();
 	}
 
 	//////////
 
 	@Override
 	public boolean canUpdate(){
 		return true;
 	}
 
 	@Override
 	public void updateEntity(){
 		if(!this.isLocked && this.hasWater && this.dyeVolume > 0){
 			ItemStack input = this.slotContents[TileEntityChromaInfuser.SLOT_ITEM_INPUT];
 			if(input == null){
 				// no input, reset progress
 				if(this.outputTick > 0) this.updateInput(null);
 			} else {
 				if(this.processingItem == null){
 					// new input, reset progress
 					this.updateInput(input);
 				} else if(input.getItem().itemID != this.processingItem.getItem().itemID || input.getItemDamage() != processingItem.getItemDamage()){
 					// input item changed, reset progress
 					this.updateInput(input);
 				}
 
 				if(this.processingRecipe != null){
 					ItemStack output = this.slotContents[TileEntityChromaInfuser.SLOT_ITEM_OUTPUT];
 					if(this.canOutput(output)){
 						if(!this.worldObj.isRemote){
 							// server
 							this.outputTick++;
 							if(this.outputTick < TileEntityChromaInfuser.outputTickMax) return;
 							this.outputTick = 0;
 
 							this.updateOutput(input, output, this.processingRecipe);
 						} else {
 							// client
 							if(this.outputTick < TileEntityChromaInfuser.outputTickMax)
 								this.outputTick++;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean receiveClientEvent(int eventID, int value){
 		if(super.receiveClientEvent(eventID, value)) return true;
 
 		switch(eventID){
 		case TileEntityChromaInfuser.CLIENT_EVENT_RECIPE_BUTTON:
 			// set recipe button
 			this.recipeButton = ChromaButton.values()[value];
 			return true;
 		case TileEntityChromaInfuser.CLIENT_EVENT_LOCKED_BUTTON:
 			// set locked button
 			this.isLocked = (value == 0 ? false : true);
 			this.outputTick = 0;
 			return true;
 		case TileEntityChromaInfuser.CLIENT_EVENT_RESET_WATER:
 			// set water, reset dye
 			this.hasWater = true;
 			this.dyeVolume = 0;
 			this.outputTick = 0;
 			return true;
 		case TileEntityChromaInfuser.CLIENT_EVENT_SET_COLOR:
 			// set dye color
 			this.dyeVolume = 8;
 			this.dyeColor = value;
 			this.outputTick = 0;
 			return true;
 		case TileEntityChromaInfuser.CLIENT_EVENT_UPDATE_OUTPUT:
 			// update output slot, reset tick counter on client
 			this.outputTick = 0;
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	//////////
 
 	@Override
 	public String getInvName(){
 		return "au.tileentity.ChromaInfuser";
 	}
 }
