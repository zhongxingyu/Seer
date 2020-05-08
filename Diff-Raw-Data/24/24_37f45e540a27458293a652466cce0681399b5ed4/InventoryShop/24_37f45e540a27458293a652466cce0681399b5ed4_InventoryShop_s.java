 package hunternif.mc.dota2items.inventory;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.item.Dota2Item;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 
 public class InventoryShop implements IInventory {
 	public static final String TAG_IS_SAMPLE = "D2IisSample";
 	
 	private static final int COLUMNS = 11;
 	private static final int ROWS = 12;
 	
 	private static final ItemStack[][] itemSamples = new ItemStack[COLUMNS][ROWS];
 	
 	public static void populate() {
 		int[] yPosInColumn = new int[COLUMNS];
 		Arrays.fill(yPosInColumn, 0);
 		for (Item item : Dota2Items.itemList) {
 			if (item instanceof Dota2Item) {
 				Dota2Item dota2Item = (Dota2Item) item;
 				int xPos = ((Dota2Item) item).getShopColumn().id;
 				int yPos = yPosInColumn[xPos];
 				ItemStack stack = new ItemStack(dota2Item, dota2Item.getDefaultQuantity());
 				NBTTagCompound tag = stack.getTagCompound();
 				if (tag == null) {
 					tag = new NBTTagCompound();
 					stack.setTagCompound(tag);
 				}
 				tag.setBoolean(TAG_IS_SAMPLE, true);
 				itemSamples[xPos][yPos] = stack;
 				yPosInColumn[xPos] = yPos + 1;
 			}
 		}
 	}
 	
 	public static InventoryShop newFullShop(int rows) {
 		return new InventoryShop(new int[]{
 			Column.CONSUMABLES.id,
 			Column.ATTRIBUTES.id,
 			Column.ARMAMENTS.id,
 			Column.ARCANE.id,
 			Column.COMMON.id,
 			Column.SUPPORT.id,
 			Column.CASTER.id,
 			Column.WEAPONS.id,
 			Column.ARMOR.id,
 			Column.ARTIFACTS.id,
 			Column.SECRET_SHOP.id
 		}, rows);
 	}
 	
 	private int[] columns;
 	private int height;
 	private int filteredHeight;
 	private ItemStack[][] displayStacks;
 	private int scrollRow;
 	private String filterStr;
 	public InventoryShop(int[] columns, int height) {
 		this.columns = columns;
 		this.height = height;
 		this.scrollRow = 0;
 		this.filterStr = "";
 		displayStacks = new ItemStack[getColumns()][getRows()];
 		updateDisplayStacks();
 	}
 	public void setFilterStr(String value) {
 		if (!this.filterStr.equals(value.toLowerCase())) {
 			this.filterStr = value.toLowerCase();
 			updateDisplayStacks();
 		}
 	}
 	public void scrollToRow(int row) {
 		if (this.scrollRow != row) {
 			this.scrollRow = row;
 			updateDisplayStacks();
 		}
 	}
 	public int getScrollRow() {
 		return scrollRow;
 	}
 	public int getRows() {
 		return height;
 	}
 	public int getColumns() {
 		return columns.length;
 	}
 	public int getFilteredRows() {
 		return filteredHeight;
 	}
 	
 	public boolean contains(Dota2Item item) {
 		for (int i = 0; i < columns.length; i++) {
 			if (columns[i] == item.getShopColumn().id) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return columns.length*height;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int i) {
 		int xPos = i % columns.length;
 		int yPos = (i - xPos)/columns.length;
 		return displayStacks[xPos][yPos];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int i, int j) {
 		return null;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int i) {
 		return null;
 	}
 
 	@Override
 	public void setInventorySlotContents(int slotID, ItemStack itemstack) {}
 
 	@Override
 	public String getInvName() {
 		return "Dota 2 Shop Inventory";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return false;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 1;
 	}
 
 	@Override
 	public void onInventoryChanged() {}
 	
 	private void updateDisplayStacks() {
 		int maxFilteredHeight = 0;
 		for (int i = 0; i < getColumns(); i++) {
 			int columnIndex = columns[i];
 			ItemStack[] column = itemSamples[columnIndex];
 			List<ItemStack> filteredColumn = new ArrayList<ItemStack>();
 			for (int j = 0; j < ROWS; j++) {
 				ItemStack stack = column[j];
 				if (stack != null && stack.getDisplayName().toLowerCase().contains(filterStr)) {
 					filteredColumn.add(stack);
 				}
 			}
 			maxFilteredHeight = Math.max(maxFilteredHeight, filteredColumn.size());
 			ItemStack[] displayColumn = new ItemStack[getRows()];
 			for (int j = scrollRow; j < filteredColumn.size() && j-scrollRow < getRows(); j++) {
 				displayColumn[j-scrollRow] = filteredColumn.get(j);
 			}
 			displayStacks[i] = displayColumn;
 		}
 		filteredHeight = maxFilteredHeight;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
 		return false;
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {}
 
 	@Override
 	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 		return false;
 	}
 }
