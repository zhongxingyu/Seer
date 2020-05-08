 package elex.block;
 
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import elex.ElementalExperimentation;
 import elex.lib.BlockIds;
 import elex.lib.Reference;
 
 /**
  * Elemental Experimentation
  * 
  * BlockElExMetalStorage
  * 
  * @author Myo-kun
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 public class BlockElExMetalStorage extends Block {
     
     public int blockListNumber;
     
     @SideOnly(Side.CLIENT)
     private Icon[] icons;
     
     public BlockElExMetalStorage(int par1, int blockList) {
         super(par1, Material.iron);
         this.blockListNumber = blockList;
         setHardness(5.0F);
         setResistance(10.0F);
         setCreativeTab(ElementalExperimentation.elexTab);
         setStepSound(Block.soundMetalFootstep);
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister register) {
         if (blockListNumber == 0) {
             icons = new Icon[BlockIds.BLOCK_ELEX_METAL_STORAGE_REAL_UNLOCALIZED_NAMES.size()];
             for (int i = 0; i < icons.length; i++) {
                 icons[i] = register.registerIcon(Reference.MOD_ID + ":" + BlockIds.BLOCK_ELEX_METAL_STORAGE_REAL_UNLOCALIZED_NAMES.get(i));
             }
         }
         if (blockListNumber == 1) {
             icons = new Icon[BlockIds.BLOCK_ELEX_METAL_STORAGE_2_REAL_UNLOCALIZED_NAMES.size()];
             for (int i = 0; i < icons.length; i++) {
                 icons[i] = register.registerIcon(Reference.MOD_ID + ":" + BlockIds.BLOCK_ELEX_METAL_STORAGE_2_REAL_UNLOCALIZED_NAMES.get(i));
             }
         }
         if (blockListNumber == 2) {
             icons = new Icon[BlockIds.BLOCK_ELEX_METAL_STORAGE_3_REAL_UNLOCALIZED_NAMES.size()];
             for (int i = 0; i < icons.length; i++) {
                 icons[i] = register.registerIcon(Reference.MOD_ID + ":" + BlockIds.BLOCK_ELEX_METAL_STORAGE_3_REAL_UNLOCALIZED_NAMES.get(i));
             }
         }
         if (blockListNumber == 3) {
             icons = new Icon[BlockIds.BLOCK_ELEX_METAL_STORAGE_4_REAL_UNLOCALIZED_NAMES.size()];
             for (int i = 0; i < icons.length; i++) {
                 icons[i] = register.registerIcon(Reference.MOD_ID + ":" + BlockIds.BLOCK_ELEX_METAL_STORAGE_4_REAL_UNLOCALIZED_NAMES.get(i));
             }
         }
         if (blockListNumber == 4) {
             icons = new Icon[BlockIds.BLOCK_ELEX_METAL_STORAGE_5_REAL_UNLOCALIZED_NAMES.size()];
             for (int i = 0; i < icons.length; i++) {
                 icons[i] = register.registerIcon(Reference.MOD_ID + ":" + BlockIds.BLOCK_ELEX_METAL_STORAGE_5_REAL_UNLOCALIZED_NAMES.get(i));
             }
         }
         if (blockListNumber == 5) {
             icons = new Icon[BlockIds.BLOCK_ALLOY_STORAGE_REAL_UNLOCALIZED_NAMES.length];
             for (int i = 0; i < icons.length; i++) {
                 icons[i] = register.registerIcon(Reference.MOD_ID + ":" + BlockIds.BLOCK_ALLOY_STORAGE_REAL_UNLOCALIZED_NAMES[i]);
             }
         }
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int side, int meta) {
         return icons[meta];
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public void getSubBlocks(int id, CreativeTabs tab, List list) {
         if (blockListNumber == 0) {
             for (int i = 0; i < BlockIds.BLOCK_ELEX_METAL_STORAGE_REAL_UNLOCALIZED_NAMES
                     .size(); i++) {
                 list.add(new ItemStack(id, 1, i));
             }
         }
         if (blockListNumber == 1) {
             for (int i = 0; i < BlockIds.BLOCK_ELEX_METAL_STORAGE_2_REAL_UNLOCALIZED_NAMES
                     .size(); i++) {
                 list.add(new ItemStack(id, 1, i));
             }
         }
         if (blockListNumber == 2) {
             for (int i = 0; i < BlockIds.BLOCK_ELEX_METAL_STORAGE_3_REAL_UNLOCALIZED_NAMES
                     .size(); i++) {
                 list.add(new ItemStack(id, 1, i));
             }
         }
         if (blockListNumber == 3) {
             for (int i = 0; i < BlockIds.BLOCK_ELEX_METAL_STORAGE_4_REAL_UNLOCALIZED_NAMES
                     .size(); i++) {
                 list.add(new ItemStack(id, 1, i));
             }
         }
         if (blockListNumber == 4) {
             for (int i = 0; i < BlockIds.BLOCK_ELEX_METAL_STORAGE_5_REAL_UNLOCALIZED_NAMES
                     .size(); i++) {
                 list.add(new ItemStack(id, 1, i));
             }
         }
        if (blockListNumber == 4) {
             for (int i = 0; i < BlockIds.BLOCK_ALLOY_STORAGE_REAL_UNLOCALIZED_NAMES
                     .length; i++) {
                 list.add(new ItemStack(id, 1, i));
             }
         }
     }
     
     @Override
     public int damageDropped(int meta) {
         return meta;
     }
     
     public boolean isBeaconBase(World worldObj, int x, int y, int z, int beaconX, int beaconY, int beaconZ)
     {
         return true;
     }
     
 }
