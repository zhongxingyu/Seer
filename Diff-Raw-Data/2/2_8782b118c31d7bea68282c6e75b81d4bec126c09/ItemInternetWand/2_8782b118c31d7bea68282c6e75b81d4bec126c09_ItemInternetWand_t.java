 package infinitealloys.item;
 
 import infinitealloys.core.InfiniteAlloys;
 import infinitealloys.util.Consts;
 import infinitealloys.util.MachineHelper;
 import java.util.List;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemInternetWand extends ItemIA {
 
 	public ItemInternetWand(int id) {
 		super(id, "internetwand");
 	}
 
 	@Override
 	public boolean getShareTag() {
 		return true;
 	}
 
 	@Override
 	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
 		if(itemstack.hasTagCompound())
 			itemstack.getTagCompound().removeTag("CoordsCurrent");
 		player.openGui(InfiniteAlloys.instance, Consts.WAND_GUI, world, (int)player.posX, (int)player.posY, (int)player.posZ);
 		return itemstack;
 	}
 
 	/** Is the machine that is at (x, y, z) OK to be added to this wand?
 	 * 
 	 * @param x the machine's x coord
 	 * @param y the machine's y coord
 	 * @param z the machine's z coord
 	 * @return true if there is space for the machine, the machine is a valid client, and it does not already exist in the wand */
 	public boolean isMachineValid(World world, ItemStack itemstack, int x, int y, int z) {
 		// If the item does not already have a tag compound, create a new one
 		NBTTagCompound tagCompound = itemstack.getTagCompound();
 		if(tagCompound == null)
 			itemstack.setTagCompound(tagCompound = new NBTTagCompound());
 
 		// If the wand is not full and the machine is a valid remote client
 		if(!tagCompound.hasKey("Coords" + (Consts.WAND_SIZE - 1)) && MachineHelper.isClient(world, x, y, z)) {
 			for(int i = 0; i < Consts.WAND_SIZE; i++) { // Iterate over each coord that the wand contains
 				if(tagCompound.hasKey("Coords" + i)) {
 					// If the wand already contains this machine, return false
 					int[] a = tagCompound.getIntArray("Coords" + i);
					if(a[0] == x && a[1] == y && a[2] == z)
 						return false;
 				}
 			}
 			return true; // If the machine is not already in the wand, then it is valid
 		}
 
 		return false;
 	}
 
 	/** Checks if the machine at x, y, z, can be added to the wand at itemstack, then adds it if it can */
 	public void addMachine(World world, ItemStack itemstack, int x, int y, int z) {
 		if(itemstack.getItem() instanceof ItemInternetWand && isMachineValid(world, itemstack, x, y, z)) {
 			NBTTagCompound tagCompound = itemstack.getTagCompound();
 			for(int i = 0; i < Consts.WAND_SIZE; i++) {
 				if(!tagCompound.hasKey("Coords" + i)) {
 					tagCompound.setIntArray("Coords" + i, new int[] { x, y, z });
 					break;
 				}
 			}
 		}
 	}
 
 	/** Removes the machine with the given index from the wand */
 	public void removeMachine(ItemStack itemstack, int index) {
 		if(itemstack.getItem() instanceof ItemInternetWand && itemstack.hasTagCompound()) { // If this is a wand and has stored data
 			NBTTagCompound tagCompound = itemstack.getTagCompound();
 			tagCompound.removeTag("Coords" + index); // Remove the tag at index
 			for(int i = index + 1; i < Consts.WAND_SIZE; i++) { // Iterate over each coord below the one that was removed
 				if(tagCompound.hasKey("Coords" + i)) { // If this key exists
 					tagCompound.setIntArray("Coords" + (i - 1), tagCompound.getIntArray("Coords" + i)); // Move this coord up one spot to fill the hole
 					tagCompound.removeTag("Coords" + i);
 				}
 				else
 					break; // If the tag doesn't exist, break because we have reached the end of the list
 			}
 		}
 	}
 }
