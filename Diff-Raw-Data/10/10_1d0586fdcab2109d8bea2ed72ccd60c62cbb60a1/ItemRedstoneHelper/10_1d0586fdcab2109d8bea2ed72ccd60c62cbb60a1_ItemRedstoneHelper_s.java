 /**
     Copyright (C) <2013> <coolAlias>
 
     This file is part of coolAlias' Redstone Helper Mod; as such,
     you can redistribute it and/or modify it under the terms of the GNU
     General Public License as published by the Free Software Foundation,
     either version 3 of the License, or (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package coolalias.redstonehelper;
 
 import java.util.List;
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.world.World;
 import coolalias.redstonehelper.lib.LogHelper;
 import coolalias.redstonehelper.lib.ModInfo;
 import coolalias.redstonehelper.utils.LogicGateGenerator;
import coolalias.structuregen.mod.items.ItemStructureSpawnerBase;
 import coolalias.structuregen.api.util.Structure;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemRedstoneHelper extends ItemStructureSpawnerBase
 {
 	public ItemRedstoneHelper(int par1) {
 		super(par1);
 		setCreativeTab(CreativeTabs.tabMisc);
 	}
 
 	@Override
 	public final int nextStructure(ItemStack itemstack) {
 		int index = getData(itemstack, STRUCTURE_INDEX) + 1;
 		if (index >= LogicGateGenerator.structures.size()) index = 0;
 		setData(itemstack, STRUCTURE_INDEX, index);
 		return index;
 	}
 
 	@Override
 	public final int prevStructure(ItemStack itemstack) {
 		int index = getData(itemstack, STRUCTURE_INDEX) - 1;
 		if (index < 0) index = LogicGateGenerator.structures.size() - 1;
 		setData(itemstack, STRUCTURE_INDEX, index);
 		return index;
 	}
 
 	@Override
 	public final String getStructureName(ItemStack itemstack, int index) {
 		return (index < LogicGateGenerator.structures.size() ? LogicGateGenerator.structures.get(index).name : "");
 	}
 
 	@Override
 	public final int getCurrentStructureIndex(ItemStack itemstack) {
 		return getData(itemstack, STRUCTURE_INDEX) >= LogicGateGenerator.structures.size() ? 0 : getData(itemstack, STRUCTURE_INDEX);
 	}
 
 	@Override
 	public final Structure getCurrentStructure(ItemStack itemstack) {
 		return LogicGateGenerator.structures.get(getCurrentStructureIndex(itemstack));
 	}
 	
 	/**
 	 * Toggles 'highlighting' of input / output signal locations
 	 */
 	public static final void toggleHighlightIO(ItemStack itemstack) {
 		if (itemstack.stackTagCompound == null) { init(itemstack); }
 		itemstack.stackTagCompound.setBoolean("HighlightIO", !itemstack.stackTagCompound.getBoolean("HighlightIO"));
 	}
 	
 	/**
 	 * Returns true if input / output signal locations will be marked with colored wool
 	 */
 	public static final boolean highlightIO(ItemStack itemstack) {
 		return itemstack.stackTagCompound.getBoolean("HighlightIO");
 	}
 	
 	/**
 	 * Returns current base block ID
 	 */
 	public static final int getBaseBlockID(ItemStack itemstack) {
 		if (itemstack.stackTagCompound == null) { init(itemstack); }
 		return itemstack.stackTagCompound.getInteger("currentBlockID");
 	}
 	
 	/**
 	 * Sets current base block ID for itemstack
 	 */
 	public static final void setBaseBlockID(ItemStack itemstack, int id) {
 		if (itemstack.stackTagCompound == null) { init(itemstack); }
 		itemstack.stackTagCompound.setInteger("currentBlockID", id);
 	}
 	
 	/**
 	 * Returns current base block metadata
 	 */
 	public static final int getBaseBlockMeta(ItemStack itemstack) {
 		if (itemstack.stackTagCompound == null) { init(itemstack); }
 		return itemstack.stackTagCompound.getInteger("currentBlockMeta");
 	}
 	
 	/**
 	 * Sets current base block metadata for itemstack
 	 */
 	public static final void setBaseBlockMeta(ItemStack itemstack, int meta) {
 		if (itemstack.stackTagCompound == null) { init(itemstack); }
 		itemstack.stackTagCompound.setInteger("currentBlockMeta", meta);
 	}
 	
 	/**
 	 * Sets current base block ID and metadata for itemstack
 	 */
 	public static final void setBaseBlock(ItemStack itemstack, int id, int meta) {
 		if (itemstack.stackTagCompound == null) { init(itemstack); }
 		itemstack.stackTagCompound.setInteger("currentBlockID", id);
 		itemstack.stackTagCompound.setInteger("currentBlockMeta", meta);
 	}
 
 	@Override
 	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
 	{
 		if (itemstack.stackTagCompound == null) { init(itemstack); }
 
 		if (!world.isRemote && LogicGateGenerator.structures.size() > 0)
 		{
 			LogicGateGenerator gen = new LogicGateGenerator(player);
 			Structure structure = getCurrentStructure(itemstack);
 			
 			if (structure == null) {
 				LogHelper.log(Level.WARNING, "Current structure is null.");
 				return false;
 			}
 			
 			if (player.capabilities.isCreativeMode || !RedstoneHelper.requiresMaterials || gen.checkInventory(player, gen.itemsRequired.get(structure.name)))
 			{
 				if (world.getBlockId(x,y,z) == Block.snow.blockID) { --y; }
 
 				gen.setPlayerFacing(player);
 				gen.setRemoveStructure(getRemove(itemstack));
 				gen.setStructureWithRotation(structure, getData(itemstack, ROTATIONS));
 				gen.setDefaultOffset(structure.getOffsetX() + getData(itemstack, OFFSET_X), structure.getOffsetY() + getData(itemstack, OFFSET_Y), structure.getOffsetZ() + getData(itemstack, OFFSET_Z));
 				gen.generate(world, world.rand, x, y, z);
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Creates a new NBTTagCompound for the itemstack if none exists
 	 */
 	private static final void init(ItemStack itemstack)
 	{
 		ItemStructureSpawnerBase.initNBTCompound(itemstack);
 		itemstack.stackTagCompound.setBoolean("HighlightIO", RedstoneHelper.highlightIO);
 		itemstack.stackTagCompound.setInteger("currentBlockID", RedstoneHelper.getBaseBlockID());
 		itemstack.stackTagCompound.setInteger("currentBlockMeta", RedstoneHelper.getBaseBlockMeta());
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		this.itemIcon = iconRegister.registerIcon(ModInfo.MOD_ID + ":" + this.getUnlocalizedName().substring(5).toLowerCase());
 	}
 	
 	/**
 	 * Allows items to add custom lines of information to the mouseover description
 	 */
 	@Override
 	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4)
 	{
 		if (itemstack.stackTagCompound == null) { init(itemstack); }
 		
 		if (getCurrentStructure(itemstack) != null)
 		{
 			ItemStack currentBlock = new ItemStack(getBaseBlockID(itemstack), 1, getBaseBlockMeta(itemstack));
 			List<String> descriptions = LogicGateGenerator.descriptions.get(getCurrentStructure(itemstack).name);
 			
 			list.add(EnumChatFormatting.BOLD + "Base Block: " + EnumChatFormatting.RESET + EnumChatFormatting.ITALIC + currentBlock.getDisplayName());
 			for (String description : descriptions) {
 				list.add(description);
 			}
 		}
 	}
 }
