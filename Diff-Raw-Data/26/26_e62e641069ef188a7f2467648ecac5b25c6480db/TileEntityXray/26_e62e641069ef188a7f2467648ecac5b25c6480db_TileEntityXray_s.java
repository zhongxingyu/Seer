 package infinitealloys.tile;
 
 import infinitealloys.util.Funcs;
 import infinitealloys.util.Point;
 import java.util.ArrayList;
 import java.util.HashMap;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class TileEntityXray extends TileEntityMachine {
 
 	/** A list of the detected blocks, x and z are relative to the machine, y is absolute */
 	private ArrayList<Point> detectedBlocks = new ArrayList<Point>();
 	public int range;
 	private Point lastSearch;
 
 	/** The selected button for the user, client-side only */
 	@SideOnly(Side.CLIENT)
 	public int selectedButton = -1;
 
 	/** The selected button on the gui for each player */
 	public HashMap<String, Short> selectedButtons = new HashMap<String, Short>();
 
 	/** Should searching continue, or is it complete. Set this to true to begin a search. */
 	public boolean shouldSearch;
 
 	/** Is it searching client-side. Does not necessarily mean the x-ray is running a search, only that the user sees a loading progress bar */
 	public boolean searchingClient;
 
 	public TileEntityXray(int facing) {
 		this();
 		front = facing;
 	}
 
 	public TileEntityXray() {
 		super(1);
 		inventoryStacks = new ItemStack[2];
 		stackLimit = 1;
 		lastSearch = new Point(0, 0, 0);
 	}
 
 	@Override
 	public String getInvName() {
 		return "X-ray";
 	}
 
 	@Override
 	public boolean isStackValidForSlot(int slot, ItemStack itemstack) {
 		return super.isStackValidForSlot(slot, itemstack) || TEHelper.stackValidForSlot(TEHelper.XRAY, slot, itemstack);
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound tagCompound) {
 		super.readFromNBT(tagCompound);
 		// True if there were blocks before to be restored, false if it was empty
 		shouldSearch = tagCompound.getBoolean("ShouldSearch");
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound tagCompound) {
 		super.writeToNBT(tagCompound);
 		// True if there are blocks on the GUI, false if there are no blocks
 		tagCompound.setBoolean("ShouldSearch", getDetectedBlocks().size() > 0);
 	}
 
 	@Override
 	public void updateEntity() {
 		super.updateEntity();
 		if(inventoryStacks[0] == null) {
 			clearDetectedBlocks();
 			selectedButtons.clear();
 			shouldSearch = false;
 			searchingClient = false;
 		}
 		else if(shouldSearch)
 			search();
 
 		// Finished searching client-side
 		if(searchingClient && ++processProgress >= ticksToProcess)
 			searchingClient = false;
 		if(!searchingClient)
 			processProgress = 0;
 	}
 
 	private void search() {
 		if(inventoryStacks[0] == null)
 			return;
 		searchingClient = true;
 		int targetID = inventoryStacks[0].itemID;
 		int targetMetadata = inventoryStacks[0].getItemDamage();
 		int blocksSearched = 0;
 		if(lastSearch.equals(0, 0, 0))
 			clearDetectedBlocks();
 		for(int y = lastSearch.y; y <= yCoord; y++) {
 			for(int x = Math.abs(lastSearch.x); x <= range; x++) {
 				for(int z = Math.abs(lastSearch.z); z <= range; z++) {
 					for(int i = x == 0 ? 1 : 0; i < 2; i++) {
 						for(int j = z == 0 ? 1 : 0; j < 2; j++) {
 							int xRel = i == 0 ? x : -x;
 							int zRel = j == 0 ? z : -z;
 							if(Funcs.blocksEqual(worldObj, targetID, targetMetadata, xCoord + xRel, y, zCoord + zRel))
 								addDetectedBlock(new Point(xRel, y, zRel));
 							if(++blocksSearched >= TEHelper.SEARCH_PER_TICK) {
 								lastSearch.set(xRel, y, zRel);
 								return;
 							}
 						}
 					}
 				}
 				lastSearch.z = 0;
 			}
 			lastSearch.x = 0;
 		}
 		lastSearch.y = 0;
 		shouldSearch = false;
 	}
 
 	public ArrayList<Point> getDetectedBlocks() {
 		return detectedBlocks;
 	}
 
 	public void clearDetectedBlocks() {
 		detectedBlocks.clear();
 	}
 
 	public void addDetectedBlock(Point p) {
 		detectedBlocks.add(p);
 	}
 
 	public void handlePacketDataFromClient(boolean searching, String playerName, short selectedButton) {
 		this.searchingClient = searching;
 		if(selectedButton != -1)
 			selectedButtons.put(playerName, selectedButton);
 		else
 			selectedButtons.remove(playerName);
 	}
 
 	@Override
 	public boolean shouldProcess() {
 		return false;
 	}
 
 	@Override
 	public void finishProcessing() {
 	}
 
 	@Override
 	public int getJoulesUsed() {
 		if(searchingClient && inventoryStacks[0] != null)
 			return joulesUsedPerTick * TEHelper.getDetectableWorth(inventoryStacks[0]);
 		return 0;
 	}
 
 	@Override
 	protected void updateUpgrades() {
 		if(hasUpgrade(TEHelper.SPEED2))
 			ticksToProcess = 12000;
 		else if(hasUpgrade(TEHelper.SPEED1))
 			ticksToProcess = 18000;
 		else
			ticksToProcess = 24; // TODO: Change this back to 24000
 
 		if(hasUpgrade(TEHelper.EFFICIENCY2))
 			joulesUsedPerTick = 1800;
 		else if(hasUpgrade(TEHelper.EFFICIENCY1))
 			joulesUsedPerTick = 2700;
 		else
			joulesUsedPerTick = 0; // TODO: Change this back to 3600
 
 		if(hasUpgrade(TEHelper.RANGE2))
 			range = 10;
 		else if(hasUpgrade(TEHelper.RANGE1))
 			range = 8;
 		else
 			range = 5;
 
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
 		validUpgrades.add(TEHelper.RANGE1);
 		validUpgrades.add(TEHelper.RANGE2);
 		validUpgrades.add(TEHelper.WIRELESS);
 	}
 }
