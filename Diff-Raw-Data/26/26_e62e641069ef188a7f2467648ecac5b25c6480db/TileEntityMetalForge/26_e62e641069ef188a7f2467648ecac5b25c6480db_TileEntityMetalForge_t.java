 package infinitealloys.tile;
 
 import infinitealloys.core.InfiniteAlloys;
 import infinitealloys.item.Items;
 import infinitealloys.util.Consts;
 import java.util.ArrayList;
 import java.util.Arrays;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 
 public class TileEntityMetalForge extends TileEntityMachine {
 
 	/** An array for the "stack sizes" of each ingot in the recipe setting */
 	public byte[] recipeAmts = new byte[Consts.METAL_COUNT];
 	/** recipeAmts from last tick, used to tell if the recipe has changed to reset progress */
 	private byte[] lastRecipeAmts = new byte[Consts.METAL_COUNT];
 	public byte presetSelection = -1;
 
 	public TileEntityMetalForge(int facing) {
 		this();
 		front = facing;
 	}
 
 	public TileEntityMetalForge() {
 		super(1);
 		inventoryStacks = new ItemStack[21];
 		ticksToProcess = 12800;
 	}
 
 	@Override
 	public String getInvName() {
 		return "Metal Forge";
 	}
 
 	@Override
 	public boolean isStackValidForSlot(int slot, ItemStack itemstack) {
 		return super.isStackValidForSlot(slot, itemstack) || TEHelper.stackValidForSlot(TEHelper.METAL_FORGE, slot, itemstack);
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound tagCompound) {
 		super.readFromNBT(tagCompound);
 		recipeAmts = tagCompound.getByteArray("RecipeAmts");
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound tagCompound) {
 		super.writeToNBT(tagCompound);
 		tagCompound.setByteArray("RecipeAmts", recipeAmts);
 	}
 
 	public void handlePacketData(byte[] recipeAmts) {
 		this.recipeAmts = recipeAmts;
 	}
 
 	@Override
 	public void updateEntity() {
 		super.updateEntity();
 		if(!Arrays.equals(lastRecipeAmts, recipeAmts))
 			processProgress = 0;
 		lastRecipeAmts = Arrays.copyOf(recipeAmts, recipeAmts.length);
 	}
 
 	/** Return the resulting ingot for the smelted ingots
 	 * 
 	 * @return The resulting ingot. */
 	private ItemStack getIngotResult() {
 		int alloy = 0;
 		for(int i = 0; i < recipeAmts.length; i++)
 			alloy += Math.pow(Consts.ALLOY_RADIX, i) * recipeAmts[i];
 		ItemStack result = new ItemStack(Items.alloyIngot);
 		NBTTagCompound tagCompound = new NBTTagCompound();
 		tagCompound.setInteger("alloy", alloy);
 		result.setTagCompound(tagCompound);
 		result.setItemDamage(getDamageForAlloy(alloy));
 		return result;
 	}
 
 	public int getDamageForAlloy(int alloy) {
 		int[] validAlloys = InfiniteAlloys.instance.worldData.getValidAlloys();
 		for(int i = 0; i < validAlloys.length; i++)
 			if(alloy == validAlloys[i])
 				return i + 1;
 		return -1;
 	}
 
 	private ArrayList<Integer> getSlotsWithIngot() {
 		ArrayList<Integer> slots = new ArrayList<Integer>();
 		for(int i = 3; i < 21; i++)
 			if(inventoryStacks[i] != null)
 				slots.add(i);
 		return slots;
 	}
 
 	private int[] getIngotAmts() {
 		int[] amts = new int[Consts.METAL_COUNT];
 		for(int slot : getSlotsWithIngot())
 			amts[TEHelper.getIngotNum(inventoryStacks[slot])] += inventoryStacks[slot].stackSize;
 		return amts;
 	}
 
 	private int getIngotsInRecipe() {
 		int ingots = 0;
 		for(int amt : recipeAmts)
 			ingots += amt;
 		return ingots;
 	}
 
 	@Override
 	protected boolean shouldProcess() {
 		int typesInRecipe = 0;
 		ArrayList<Boolean> sufficientIngots = new ArrayList<Boolean>();
 		for(int amt : recipeAmts)
 			if(amt > 0)
 				typesInRecipe++;
 		for(int i = 0; i < getIngotAmts().length; i++)
 			sufficientIngots.add(getIngotAmts()[i] >= recipeAmts[i]);
 		if(sufficientIngots.contains(false))
 			processProgress = 0;
 		return typesInRecipe > 1 && !sufficientIngots.contains(false) && joules >= joulesUsedPerTick
 				&& (inventoryStacks[2] == null || inventoryStacks[2].isItemEqual(getIngotResult()) && getInventoryStackLimit() - inventoryStacks[2].stackSize >= 1);
 	}
 
 	@Override
 	protected void finishProcessing() {
 		byte[] ingotsToRemove = Arrays.copyOf(recipeAmts, recipeAmts.length);
 		for(int slot : getSlotsWithIngot()) {
 			int ingotNum = TEHelper.getIngotNum(inventoryStacks[slot]);
 			int ingots = ingotsToRemove[ingotNum];
 			ingotsToRemove[ingotNum] -= Math.min(ingotsToRemove[ingotNum], inventoryStacks[slot].stackSize);
 			decrStackSize(slot, Math.min(ingots, inventoryStacks[slot].stackSize));
 		}
 		ItemStack result = getIngotResult();
 		if(inventoryStacks[2] == null)
 			inventoryStacks[2] = result;
 		else if(inventoryStacks[2].getTagCompound().getInteger("alloy") == result.getTagCompound().getInteger("alloy"))
 			inventoryStacks[2].stackSize += result.stackSize;
 	}
 
 	@Override
 	public int getJoulesUsed() {
 		if(shouldProcess())
 			return joulesUsedPerTick * getIngotsInRecipe();
 		return 0;
 	}
 
 	@Override
 	protected void updateUpgrades() {
 		if(hasUpgrade(TEHelper.SPEED2))
 			ticksToProcess = 6400;
 		else if(hasUpgrade(TEHelper.SPEED1))
 			ticksToProcess = 9600;
 		else
 			ticksToProcess = 12800;
 
 		if(hasUpgrade(TEHelper.EFFICIENCY2))
 			joulesUsedPerTick = 90;
 		else if(hasUpgrade(TEHelper.EFFICIENCY1))
 			joulesUsedPerTick = 120;
 		else
 			joulesUsedPerTick = 180;
 
 		if(hasUpgrade(TEHelper.CAPACITY2))
 			stackLimit = 64;
		else if(hasUpgrade(TEHelper.CAPACITY1))
			stackLimit = 48;
 		else
 			stackLimit = 32;
 
 		canNetwork = hasUpgrade(TEHelper.WIRELESS);
 
 		if(hasUpgrade(TEHelper.ELECCAPACITY2))
 			maxJoules = 1000000;
 		else if(hasUpgrade(TEHelper.ELECCAPACITY1))
 			maxJoules = 750000;
 		else
 			maxJoules = 500000;
 
 	}
 
 	@Override
 	protected void populateValidUpgrades() {
 		validUpgrades.add(TEHelper.SPEED1);
 		validUpgrades.add(TEHelper.SPEED2);
 		validUpgrades.add(TEHelper.EFFICIENCY1);
 		validUpgrades.add(TEHelper.EFFICIENCY2);
 		validUpgrades.add(TEHelper.CAPACITY1);
 		validUpgrades.add(TEHelper.CAPACITY2);
 		validUpgrades.add(TEHelper.WIRELESS);
 		validUpgrades.add(TEHelper.ELECCAPACITY1);
 		validUpgrades.add(TEHelper.ELECCAPACITY2);
 	}
 }
