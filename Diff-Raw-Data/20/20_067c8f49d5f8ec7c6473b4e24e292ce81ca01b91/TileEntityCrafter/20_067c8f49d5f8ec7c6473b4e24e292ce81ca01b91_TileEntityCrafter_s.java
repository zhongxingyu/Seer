 package henje.common;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Vector;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityChest;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import buildcraft.api.gates.IAction;
 import buildcraft.api.gates.IActionReceptor;
 import buildcraft.api.transport.IPipeEntry;
 
public class TileEntityCrafter extends TileEntity implements IInventory, IActionReceptor {
 	private final static int CRAFT_DELAY = 20;
 	private int counter;
 	private ItemStack[] inventory;
 	private boolean activated;
 	private long lastUpdate;
 
 	public TileEntityCrafter() {
 		inventory = new ItemStack[9];
 		activated = true;
 	}
 	
 	public ItemStack craftItem() {
 		ItemStack result = findRecipe();
 		if(result != null) {
 			for(int i = 0; i < 9; i++) {
 				this.decrStackSize(i, 1);
 			}
 		}
 		return result;
 	}
 	
 	public ItemStack craftItemFromChests() {
 		ItemStack result = findRecipe();
 		Vector<IInventory> chests = searchChestsSurrounding(worldObj, xCoord, yCoord, zCoord);
 		if(!chests.isEmpty() && haveChestsEnoughIngredients(chests, getIngredients())) {
 			removeFromChests(chests, getIngredients());
 			return result;
 		} else {
 			return null;
 		}
 	}
 	
 	private boolean haveChestsEnoughIngredients(Vector<IInventory> chests, HashMap<Integer,Integer> ingredients) {
 		Iterator<IInventory> it = chests.iterator();
 		while(it.hasNext()) {
 			IInventory inv = it.next();
 			for(int i = 0; i < inv.getSizeInventory(); i++) {
 				ItemStack stack = inv.getStackInSlot(i);
 				if(stack != null && ingredients.containsKey(stack.itemID)) {
 					int amount = ingredients.get(stack.itemID) - stack.stackSize;
 					ingredients.put(stack.itemID, amount);
 					if(amount < 1) {
 						ingredients.remove(stack.itemID);
 					}
 				}
 			}
 		}
 		return ingredients.isEmpty();
 	}
 	
 	private void removeFromChests(Vector<IInventory> chests,
 			HashMap<Integer, Integer> ingredients) {
 		HashMap<Integer,Integer> copy = (HashMap<Integer, Integer>) ingredients.clone();
 		Iterator<Entry<Integer,Integer>> ingreIt = copy.entrySet().iterator();
 		while(ingreIt.hasNext()) {
 			Entry<Integer,Integer> entry = ingreIt.next();
 			Iterator<IInventory> invIt = chests.iterator();
 			while(invIt.hasNext()) {
 				IInventory inv = invIt.next();
 				for(int i = 0; i < inv.getSizeInventory(); i++) {
 					ItemStack stack = inv.getStackInSlot(i);
 					if(stack != null && ingredients.containsKey(stack.itemID)) {
 						int amount = ingredients.get(stack.itemID) - stack.stackSize;
 						inv.decrStackSize(i, Math.min(ingredients.get(stack.itemID), stack.stackSize));
 						ingredients.put(stack.itemID, amount);
 						if(amount < 1) {
 							ingredients.remove(stack.itemID);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public HashMap<Integer,Integer> getIngredients() {
 		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
 		for(int i = 0; i < 9; i++) {
 			ItemStack now = getStackInSlot(i);
 			if(now != null) {
 				if(map.containsKey(now.itemID)) {
 					int amount = map.get(now.itemID) + 1;
 					map.remove(now.itemID);
 					map.put(now.itemID, amount);
 				} else {
 					map.put(now.itemID, 1);
 				}
 			}
 		}
 		return map;		
 	}
 	
 	private HashMap<Integer,Integer> getContent(IInventory inv) {
 		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
 		for(int i = 0; i < 9; i++) {
 			ItemStack now = inv.getStackInSlot(i);
 			if(now != null) {
 				if(map.containsKey(now.itemID)) {
 					int amount = map.get(now.itemID) + now.stackSize;
 					map.remove(now.itemID);
 					map.put(now.itemID, amount);
 				} else {
 					map.put(now.itemID, now.stackSize);
 				}
 			}
 		}
 		return map;		
 	}
 	
 	private boolean canUseItem(ItemStack is, EntityPlayer player) {
 		ItemStack inv = is, mouse = player.inventory.getItemStack();
 		return (mouse == null) || (inv.getItemName().equals(mouse.getItemName())) && (mouse.getMaxStackSize()-mouse.stackSize >= is.stackSize);
 	}
 	
 	public ItemStack craftItemAsPlayer(EntityPlayer player) {
 		ItemStack result = findRecipe();
 		if(canUseItem(result, player)) {
 			return craftItem();
 		} else {
 			return null;
 		}
 	}
 	
 	public void shiftCraftAsPlayer(EntityPlayer player) {
 		ItemStack result = craftItem();
 		while(result != null && player.inventory.addItemStackToInventory(result)) {
 			result = craftItem();
 		}
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return inventory.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		return inventory[slot];
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack stack) {
 		inventory[slot] = stack;
 		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
 			stack.stackSize = getInventoryStackLimit();
 		}
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int amt) {
 		ItemStack stack = getStackInSlot(slot);
 		if (stack != null) {
 			if (stack.stackSize <= amt) {
 				setInventorySlotContents(slot, null);
 			} else {
 				stack = stack.splitStack(amt);
 				if (stack.stackSize == 0) {
 					setInventorySlotContents(slot, null);
 				}
 			}
 		}
 		return stack;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot) {
 		ItemStack stack = getStackInSlot(slot);
 		if (stack != null) {
 			setInventorySlotContents(slot, null);
 		}
 		return stack;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer player) {
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this &&
 		player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {}
 
 	@Override
 	public void readFromNBT(NBTTagCompound tagCompound) {
 		super.readFromNBT(tagCompound);
 		
 		NBTTagList tagList = tagCompound.getTagList("inventory");
 		for (int i = 0; i < tagList.tagCount(); i++) {
 			NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
 			byte slot = tag.getByte("Slot");
 			if (slot >= 0 && slot < inventory.length) {
 				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
 			}
 		}
 		readFlagsFromNBT(tagCompound.getCompoundTag("flags"));
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound tagCompound) {
 		super.writeToNBT(tagCompound);
 		
 		NBTTagList itemList = new NBTTagList();
 		for (int i = 0; i < inventory.length; i++) {
 			ItemStack stack = inventory[i];
 			if (stack != null) {
 				NBTTagCompound tag = new NBTTagCompound();
 				tag.setByte("Slot", (byte) i);
 				stack.writeToNBT(tag);
 				itemList.appendTag(tag);
 			}
 		}
 		tagCompound.setTag("flags", writeFlagsToNBT());
 		tagCompound.setTag("inventory", itemList);
 	}
 	
 	private void readFlagsFromNBT(NBTTagCompound compound) {
 		activated = compound.getBoolean("activated");
 		lastUpdate = compound.getLong("lastUpdate");
 	}
 	
 	private NBTTagCompound writeFlagsToNBT() {
 		NBTTagCompound nbt = new NBTTagCompound("flags");
 		nbt.setBoolean("activated", activated);
 		nbt.setLong("lastUpdate", lastUpdate);
 		return nbt;
 	}
 
 	@Override
 	public String getInvName() {
 		return "henje.tilentitycrafter";
 	}
 	
 	public ItemStack findRecipe() {
 		InventoryCrafting craftMatrix = new InventoryCrafting(new Container() {
 
 			@Override
 			public boolean canInteractWith(EntityPlayer var1) {
 				return false;
 			}
 			
 		}, 3, 3);
 
 		for (int i = 0; i < getSizeInventory(); ++i) {
 			ItemStack stack = getStackInSlot(i);
 
 			craftMatrix.setInventorySlotContents(i, stack);
 		}
 
 		ItemStack recipe = CraftingManager.getInstance().findMatchingRecipe(craftMatrix, worldObj);
 
 		return recipe;
 	}
 	
 	@Override
 	public void updateEntity() {
 		counter++;
 		if(counter == CRAFT_DELAY) {
 			checkAction();
 			counter = 0;
 			if(activated) {
 				craft();
 			}
 		}
 	}
 	
 	private void craft() {
 		IPipeEntry pipe = searchPipeSurrounding(worldObj, xCoord, yCoord, zCoord);
 		ItemStack result = craftItemFromChests();
 		if(pipe != null && result != null) {
 			pipe.entityEntering(result, computeDirection((TileEntity)pipe));
 		}
 	}
 	
 	private IPipeEntry searchPipeSurrounding(World world, int x, int y, int z) {
 		return (IPipeEntry) searchTileEntitySurrounding(world, x, y, z, IPipeEntry.class);
 	}
 	
 	private Vector<IInventory> searchChestsSurrounding(World world, int x, int y, int z) {
 		Vector<IInventory> sources = new Vector<IInventory>();
 		Vector<TileEntity> chests = searchTileEntitiesSurrounding(world, x, y, z, TileEntityChest.class);
 		Iterator<TileEntity> it = chests.iterator();
 		while(it.hasNext()) {
 			sources.add((IInventory) it.next());
 		}
 		try {
 			Class c = Class.forName("cpw.mods.ironchest.TileEntityIronChest");
 			Vector<TileEntity> ironChests = searchTileEntitiesSurrounding(world, x, y, z, c);
			Iterator<TileEntity> itIron = chests.iterator();
 			while(itIron.hasNext()) {
 				sources.add((IInventory) itIron.next());
 			}
 		} catch (ClassNotFoundException e) {
 		}
 		return sources;
 	}
 	
 	private TileEntity searchTileEntitySurrounding(World world, int x, int y, int z, Class c) {
 		Vector<TileEntity> pipes = searchTileEntitiesSurrounding(world, x, y, z, c);
 		if(pipes.isEmpty()) {
 			return null;
 		} else {
 			return pipes.get(0);
 		}
 	}
 	
 	private Vector<TileEntity> searchTileEntitiesSurrounding(World world, int x, int y, int z, Class c) {
 		Vector<TileEntity> tiles = new Vector<TileEntity>();
 		if(c.isInstance(world.getBlockTileEntity(x+1, y, z))) {
 			tiles.add(world.getBlockTileEntity(x+1, y, z));
 		}
 		if(c.isInstance(world.getBlockTileEntity(x-1, y, z))) {
 			tiles.add(world.getBlockTileEntity(x-1, y, z));
 		}
 		if(c.isInstance(world.getBlockTileEntity(x, y+1, z))) {
 			tiles.add(world.getBlockTileEntity(x, y+1, z));
 		}
 		if(c.isInstance(world.getBlockTileEntity(x, y-1, z))) {
 			tiles.add(world.getBlockTileEntity(x, y-1, z));
 		}
 		if(c.isInstance(world.getBlockTileEntity(x, y, z+1))) {
 			tiles.add(world.getBlockTileEntity(x, y, z+1));
 		}
 		if(c.isInstance(world.getBlockTileEntity(x, y, z-1))) {
 			tiles.add(world.getBlockTileEntity(x, y, z-1));
 		}
 		return tiles;
 	}
 	
 	private ForgeDirection computeDirection(TileEntity tile) {
 		if(xCoord > tile.xCoord) {
 			return ForgeDirection.WEST;
 		} else if(xCoord < tile.xCoord) {
 			return ForgeDirection.EAST;
 		} else if(yCoord > tile.yCoord) {
 			return ForgeDirection.DOWN;
 		} else if(yCoord < tile.yCoord) {
 			return ForgeDirection.UP;
 		} else if(zCoord > tile.zCoord) {
 			return ForgeDirection.NORTH;
 		} else if(zCoord < tile.zCoord) {
 			return ForgeDirection.SOUTH;
 		} else {
 			return ForgeDirection.UNKNOWN;
 		}
 	}
 	
 	public void setActivated(boolean b) {
 		if(FMLCommonHandler.instance().getEffectiveSide().isServer()) {
 			NBTTagCompound nbt = writeFlagsToNBT();
 			PacketDispatcher.sendPacketToAllPlayers(new Packet132TileEntityData(xCoord, yCoord, zCoord, 0, nbt));
 		}
 		activated = b;
 	}
 	
 	public boolean isActivated() {
 		return activated;
 	}
 	
 	private void checkAction() {
 		if(!activated && System.currentTimeMillis() - lastUpdate > 1000) {
 			setActivated(true);
 		}
 	}
 
 	@Override
 	public void actionActivated(IAction action) {
 		lastUpdate = System.currentTimeMillis();
 		if(action.getId() == 1010) {
 			setActivated(false);
 		}
 	}
 	
 	@Override
     public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
 		readFlagsFromNBT(pkt.customParam1);
     }
 }
