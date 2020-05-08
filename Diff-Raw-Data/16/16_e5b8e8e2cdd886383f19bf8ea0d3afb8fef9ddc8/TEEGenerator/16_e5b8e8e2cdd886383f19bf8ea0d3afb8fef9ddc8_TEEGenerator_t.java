 package infinitealloys.tile;
 
 import infinitealloys.util.MachineHelper;
 import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntityFurnace;
 
 public class TEEGenerator extends TileEntityElectric {
 
 	/** The ratio between how long an item will burn in a generator and how long it will burn in a furnace. Generator is numerator, furnace is demoninator. */
 	private final float GENERATOR_TO_FURNACE_TICK_RATIO = 0.5F;
 	private boolean burning;
 
 	public TEEGenerator(int facing) {
 		this();
 		front = facing;
 	}
 
 	public TEEGenerator() {
 		super(9);
 		inventoryStacks = new ItemStack[11];
 		baseRKPerTick = 72;
 	}
 
 	@Override
 	public int getID() {
 		return MachineHelper.GENERATOR;
 	}
 
 	@Override
 	protected boolean shouldProcess() {
 		if(burning)
 			return true;
 		for(int i = 0; i < inventoryStacks.length - 1; i++)
 			if(inventoryStacks[i] != null)
 				return true;
 		return false;
 	}
 
 	@Override
 	protected void startProcess() {
 		// Take one piece of fuel out of the first slot that has fuel
 		for(int i = 0; i < inventoryStacks.length - 1; i++) {
 			if(inventoryStacks[i] != null) {
 				ticksToProcess = (int)(TileEntityFurnace.getItemBurnTime(inventoryStacks[i]) * GENERATOR_TO_FURNACE_TICK_RATIO);
 				decrStackSize(i, 1);
 				burning = true;
				break;
 			}
 		}
 	}
 
 	@Override
 	protected void finishProcess() {
 		burning = false;
 	}
 
 	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		ticksToProcess = tagCompound.getShort("TicksToProcess");
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("TicksToProcess", ticksToProcess);
	}

	@Override
 	protected void updateUpgrades() {
 		if(hasUpgrade(MachineHelper.SPEED2))
 			processTimeMult = 0.5F;
 		else if(hasUpgrade(MachineHelper.SPEED1))
 			processTimeMult = 0.75F;
 		else
 			processTimeMult = 1.0F;
 
 		if(hasUpgrade(MachineHelper.EFFICIENCY2))
 			rkPerTickMult = 2.0F;
 		else if(hasUpgrade(MachineHelper.EFFICIENCY1))
 			rkPerTickMult = 1.5F;
 		else
 			rkPerTickMult = 1.0F;
 
 		if(hasUpgrade(MachineHelper.CAPACITY2))
 			stackLimit = 64;
 		else if(hasUpgrade(MachineHelper.CAPACITY1))
 			stackLimit = 48;
 		else
 			stackLimit = 32;
 	}
 
 	@Override
 	protected void populateValidUpgrades() {
 		validUpgrades.add(MachineHelper.SPEED1);
 		validUpgrades.add(MachineHelper.SPEED2);
 		validUpgrades.add(MachineHelper.EFFICIENCY1);
 		validUpgrades.add(MachineHelper.EFFICIENCY2);
 		validUpgrades.add(MachineHelper.CAPACITY1);
 		validUpgrades.add(MachineHelper.CAPACITY2);
 		validUpgrades.add(MachineHelper.WIRELESS);
 	}
 }
