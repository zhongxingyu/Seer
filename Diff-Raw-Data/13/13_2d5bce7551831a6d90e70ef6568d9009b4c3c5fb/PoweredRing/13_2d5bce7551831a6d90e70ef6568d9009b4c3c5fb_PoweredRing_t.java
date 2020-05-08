 package hunternif.mc.rings.item;
 
 import hunternif.mc.rings.RingsOfPower;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import cpw.mods.fml.common.FMLLog;
 
 public abstract class PoweredRing extends ModItem {
 	private static final String TAG_STORED_FUEL = "RoFStoredFuel";
 	
 	public static final Set<Item> fuelItems = new HashSet<Item>();
 	public static final Set<Block> fuelBlocks = new HashSet<Block>();
 	static {
 		fuelItems.add(Item.coal);
 		fuelItems.add(Item.glowstone);
 		fuelItems.add(Item.redstone);
		fuelBlocks.add(Block.coalBlock);
 		fuelBlocks.add(Block.glowStone);
 		fuelBlocks.add(Block.blockRedstone);
 	}
 	
 	public PoweredRing(int id) {
 		super(id);
 		setMaxStackSize(1);
 	}
 	
 	public boolean hasFuel(ItemStack stack, EntityPlayer player) {
 		return getStoredFuel(stack) > 0 || findItemFuelID(player) != null || findBlockFuelID(player) != null;
 	}
 	private ItemStack findItemFuelID(EntityPlayer player) {
 		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
 			ItemStack stack = player.inventory.getStackInSlot(i);
 			if (stack != null) {
 				Item item = stack.getItem();
 				if (fuelItems.contains(item)) {
 					return stack;
 				}
 			}
 		}
 		return null;
 	}
 	private ItemStack findBlockFuelID(EntityPlayer player) {
 		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
 			ItemStack stack = player.inventory.getStackInSlot(i);
 			if (stack != null && stack.getItem() instanceof ItemBlock) {
 				int blockID = ((ItemBlock) stack.getItem()).getBlockID();
 				Block block = Block.blocksList[blockID];
 				if (block != null && fuelBlocks.contains(block)) {
 					return stack;
 				}
 			}
 		}
 		return null;
 	}
 	
 	
 	public int getStoredFuel(ItemStack stack) {
 		NBTTagCompound tag = stack.getTagCompound();
 		if (tag == null) {
 			return 0;
 		}
 		return tag.getInteger(TAG_STORED_FUEL);
 	}
 	
 	public void consumeFuel(ItemStack stack, EntityPlayer player) {
		if (player.capabilities.isCreativeMode) {
			return;
		}
 		NBTTagCompound tag = stack.getTagCompound();
 		if (tag == null) {
 			tag = new NBTTagCompound();
 			stack.setTagCompound(tag);
 		}
 		// Try using stored fuel:
 		int storedFuel = tag.getInteger(TAG_STORED_FUEL);
 		if (storedFuel > 0) {
 			storedFuel--;
 			tag.setInteger(TAG_STORED_FUEL, storedFuel);
 			return;
 		}
 		// Try using item fuel:
 		ItemStack fuel = findItemFuelID(player);
 		if (fuel != null) {
 			player.inventory.consumeInventoryItem(fuel.itemID);
 			return;
 		}
 		// Try using block fuel:
 		fuel = findBlockFuelID(player);
 		if (fuel != null) {
 			player.inventory.consumeInventoryItem(fuel.itemID);
 			// Blocks provide 9 times the fuel equivalent of items:
 			tag.setInteger(TAG_STORED_FUEL, 8);
 			return;
 		}
 		FMLLog.log(RingsOfPower.ID, Level.WARNING, "Tried to consume fuel, but none was found!");
 	}
 }
