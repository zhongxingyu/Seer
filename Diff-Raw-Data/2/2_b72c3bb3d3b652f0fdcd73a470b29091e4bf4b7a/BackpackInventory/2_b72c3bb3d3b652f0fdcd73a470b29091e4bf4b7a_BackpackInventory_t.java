 package backpack;
 
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.InventoryBasic;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.NBTTagList;
 
 public class BackpackInventory extends InventoryBasic {
 	// the title of the backpack
 	private String inventoryTitle;
 	
 	// an instance of the player to get the inventory
 	private EntityPlayer playerEntity;
 	// the original ItemStack to compare with the player inventory
 	private ItemStack originalIS;
 	
 	// if class is reading from nbt tag
 	private boolean reading = false;
 
 	/**
 	 * Takes a player and an ItemStack.
 	 * 
 	 * @param player
 	 *            The player which has the backpack.
 	 * @param is
 	 *            The ItemStack which holds the backpack.
 	 */
 	public BackpackInventory(EntityPlayer player, ItemStack is) {
 		// number of slots 3 lines a 9 slots
 		super("", 27);
 		
 		playerEntity = player;
 		originalIS = is;
 
 		// check if inventory exists if not create one
 		if(!hasInventory(is.getTagCompound())) {
 			createInventory();
 		}

		loadInventory();
 	}
 
 	/**
 	 * Is called whenever something is changed in the inventory.
 	 */
 	@Override
 	public void onInventoryChanged() {
 		super.onInventoryChanged();
 		// if reading from nbt don't write
 		if(!reading) {
 			saveInventory();
 		}
 	}
 
 	/**
 	 * This method is called when the chest opens the inventory. It loads the
 	 * content of the inventory and its title.
 	 */
 	@Override
 	public void openChest() {
 		loadInventory();
 	}
 
 	/**
 	 * This method is called when the chest closes the inventory. It then throws
 	 * out every backpack which is inside the backpack and saves the inventory.
 	 */
 	@Override
 	public void closeChest() {
 		dropContainedBackpacks();
 		saveInventory();
 	}
 	
 	/**
 	 * Returns the name of the inventory.
 	 */
 	@Override
 	public String getInvName() {
 		return this.inventoryTitle;
 	}
 
 	// ***** custom methods which are not in IInventory *****
 	/**
 	 * Returns if an Inventory is saved in the NBT.
 	 * 
 	 * @param nbt
 	 *            The NBTTagCompound to check for an inventory.
 	 * @return True when the NBT is not null and the NBT has key "Inventory"
 	 *         otherwise false.
 	 */
 	private boolean hasInventory(NBTTagCompound nbt) {
 		return (nbt != null && (nbt.hasKey("Inventory")));
 	}
 
 	/**
 	 * Creates the Inventory Tag in the NBT with an empty inventory.
 	 */
 	private void createInventory() {
 		NBTTagCompound tag;
 		if(originalIS.hasTagCompound()) {
 			tag = originalIS.getTagCompound();
 		} else {
 			tag = new NBTTagCompound();
 		}
 		// new String so that a new String object is created
 		// so that title == title is false
 		// needed for two new created backpacks
 		setInvName(new String(originalIS.getItemName()));
 		writeToNBT(tag);
 		originalIS.setTagCompound(tag);
 	}
 
 	/**
 	 * Sets the name of the inventory.
 	 * 
 	 * @param name
 	 *            The new name.
 	 */
 	public void setInvName(String name) {
 		this.inventoryTitle = name;
 	}
 
 	/**
 	 * Searches the backpack in players inventory and saves NBT data in it.
 	 */
 	private void setNBT() {
 		// get players inventory
 		ItemStack[] inventory = playerEntity.inventory.mainInventory;
 		ItemStack itemStack;
 		// iterate over all items in player inventory
 		for(int i = 0; i < inventory.length; i++) {
 			// get ItemStack at slot i
 			itemStack = inventory[i];
 			// check if slot is not null and ItemStack is equal to original
 			if(itemStack != null && isItemStackEqual(itemStack)) {
 				// save new data in ItemStack
 				itemStack.setTagCompound(originalIS.getTagCompound());
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Checks if ItemStack is equal to the original ItemStack.
 	 * 
 	 * @param itemStack
 	 *            The ItemStack to check.
 	 * @return true if equal otherwise false.
 	 */
 	private boolean isItemStackEqual(ItemStack itemStack) {
 		// check if ItemStack is a BackpackItem and normal properties are equal
 		if(itemStack.getItem() instanceof BackpackItem && itemStack.isItemEqual(originalIS)) {
 			// never opened backpacks have no NBT so make sure it is there
 			if(itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("Inventory")) {
 				// check if NBT data is equal too
 				return isTagCompoundEqual(itemStack);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if ItemStacks NBT data is equal to original ItemStacks NBT data.
 	 * 
 	 * @param itemStack
 	 *            The ItemStack to check.
 	 * @return true if equal otherwise false.
 	 */
 	private boolean isTagCompoundEqual(ItemStack itemStack) {
 		NBTTagCompound itemStackTag = itemStack.getTagCompound().getCompoundTag("display");
 		NBTTagCompound origItemStackTag = originalIS.getTagCompound().getCompoundTag("display");
 
 		// check if title is unequal
 		if(itemStackTag.getString("Name") == origItemStackTag.getString("Name")) {
 			return true;
 		}
 		
 		// TODO: still there for compatibility
 		itemStackTag = itemStack.getTagCompound().getCompoundTag("Inventory");
 		if(itemStackTag.getString("title") == origItemStackTag.getString("Name")) {
 			return true;
 		}
 		
 		// title is unequal
 		return false;
 	}
 
 	/**
 	 * If there is no inventory create one. Then load the content and title of
 	 * the inventory from the NBT
 	 */
 	public void loadInventory() {
 		readFromNBT(originalIS.getTagCompound());
 	}
 
 	/**
 	 * Saves the actual content of the inventory to the NBT.
 	 */
 	public void saveInventory() {
 		writeToNBT(originalIS.getTagCompound());
 		setNBT();
 	}
 
 	/**
 	 * Drops Backpacks on the ground which are in this backpack
 	 */
 	private void dropContainedBackpacks() {
 		for(int i = 0; i < getSizeInventory(); i++) {
 			ItemStack item = getStackInSlot(i);
 			if(item != null && item.getItem() instanceof BackpackItem) {
 				playerEntity.dropPlayerItem(getStackInSlot(i));
 				setInventorySlotContents(i, null);
 			}
 		}
 	}
 
 	/**
 	 * Writes a NBT Node with inventory.
 	 * 
 	 * @param outerTag
 	 *            The NBT Node to write to.
 	 * @return The written NBT Node.
 	 */
 	private NBTTagCompound writeToNBT(NBTTagCompound outerTag) {
 		if(outerTag == null) {
 			return null;
 		}
 		// save name in display->Name
 		NBTTagCompound name = new NBTTagCompound();
 		name.setString("Name", getInvName());
 		outerTag.setCompoundTag("display", name);
 
 		NBTTagList itemList = new NBTTagList();
 		for(int i = 0; i < getSizeInventory(); i++) {
 			if(getStackInSlot(i) != null) {
 				NBTTagCompound slotEntry = new NBTTagCompound();
 				slotEntry.setByte("Slot", (byte) i);
 				getStackInSlot(i).writeToNBT(slotEntry);
 				itemList.appendTag(slotEntry);
 			}
 		}
 		// save content in Inventory->Items
 		NBTTagCompound inventory = new NBTTagCompound();
 		inventory.setTag("Items", itemList);
 		outerTag.setCompoundTag("Inventory", inventory);
 		return outerTag;
 	}
 
 	/**
 	 * Reads the inventory from a NBT Node.
 	 * 
 	 * @param outerTag
 	 *            The NBT Node to read from.
 	 */
 	private void readFromNBT(NBTTagCompound outerTag) {
 		if(outerTag == null) {
 			return;
 		}
 		
 		reading = true;
 		// TODO for backwards compatibility
 		if(outerTag.getCompoundTag("Inventory").hasKey("title")) {
 			setInvName(outerTag.getCompoundTag("Inventory").getString("title"));
 		} else {
 			setInvName(outerTag.getCompoundTag("display").getString("Name"));
 		}
 
 		NBTTagList itemList = outerTag.getCompoundTag("Inventory").getTagList("Items");
 		for(int i = 0; i < itemList.tagCount(); i++) {
 			NBTTagCompound slotEntry = (NBTTagCompound) itemList.tagAt(i);
 			int j = slotEntry.getByte("Slot") & 0xff;
 
 			if(j >= 0 && j < getSizeInventory()) {
 				setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(slotEntry));
 			}
 		}
 		reading = false;
 	}
 }
