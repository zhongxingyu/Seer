 
 
 package mods.storemore.ic2.blocks;
 
 import java.util.List;
 import java.util.Random;
 
 import mods.storemore.ic2.sm_ic2plugin;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 
 
 
 
 public class ic2blocksI extends Block {
 	
 	protected Icon[] icon = new Icon [16];
 	
 	public ic2blocksI(int id){
 		super(id, Material.rock);
 		setHardness(3.0F);
 		setResistance(120.0F);
		setCreativeTab(sm_ic2plugin.StoreMoreTab);
 	    
 	}
 	
 	
 	
 @Override
 public void registerIcons(IconRegister iconRegister)
 {
 icon[0] = iconRegister.registerIcon("storemore:p_copperI");
 icon[1] = iconRegister.registerIcon("storemore:p_copperII");
 icon[2] = iconRegister.registerIcon("storemore:p_copperIII");
 icon[3] = iconRegister.registerIcon("storemore:p_copperIV");
 icon[4] = iconRegister.registerIcon("storemore:p_tinI");
 icon[5] = iconRegister.registerIcon("storemore:p_tinII");
 icon[6] = iconRegister.registerIcon("storemore:p_tinIII");
 icon[7] = iconRegister.registerIcon("storemore:p_tinIV");
 icon[8] = iconRegister.registerIcon("storemore:p_uraniumI");
 icon[9] = iconRegister.registerIcon("storemore:p_uraniumII");
 icon[10] = iconRegister.registerIcon("storemore:p_uraniumIII");
 icon[11] = iconRegister.registerIcon("storemore:p_uraniumIV");
 icon[12] = iconRegister.registerIcon("storemore:p_bronzeI");
 icon[13] = iconRegister.registerIcon("storemore:p_bronzeII");
 icon[14] = iconRegister.registerIcon("storemore:p_bronzeIII");
 icon[15] = iconRegister.registerIcon("storemore:p_bronzeIV");
 }
 
 @Override
 public Icon getBlockTextureFromSideAndMetadata(int side, int meta)
 {
     return icon[meta];
 } 
 
 	
 public int idDropped(int par1, Random par2Random, int par3)
 {
     switch(par1){
     case 1:return sm_ic2plugin.ic2blocksI.blockID;
     case 2:return sm_ic2plugin.ic2blocksI.blockID;
     case 3:return sm_ic2plugin.ic2blocksI.blockID;
     case 4:return sm_ic2plugin.ic2blocksI.blockID;
     case 5:return sm_ic2plugin.ic2blocksI.blockID;
     case 6:return sm_ic2plugin.ic2blocksI.blockID;
     case 7:return sm_ic2plugin.ic2blocksI.blockID;
     case 8:return sm_ic2plugin.ic2blocksI.blockID;
     case 9:return sm_ic2plugin.ic2blocksI.blockID;
     case 10:return sm_ic2plugin.ic2blocksI.blockID;
     case 11:return sm_ic2plugin.ic2blocksI.blockID;
     case 12:return sm_ic2plugin.ic2blocksI.blockID;
     case 13:return sm_ic2plugin.ic2blocksI.blockID;
     case 14:return sm_ic2plugin.ic2blocksI.blockID;
     case 15:return sm_ic2plugin.ic2blocksI.blockID;
     default:return blockID;
     }
 }
 
 @Override
 public int damageDropped(int meta){
 return meta;
 }
 
 public int idPicked(World par1World, int par2, int par3, int par4)
 {
     return this.blockID;
 }
   
 public ItemStack getPickBlock(MovingObjectPosition target,World world, int x, int y, int z)
 {
     return null;
 }
   
 public void getSubBlocks(int i,CreativeTabs tab, List list)
 {
     list.add(new ItemStack(i,1,0));
     list.add(new ItemStack(i,1,1));
     list.add(new ItemStack(i,1,2));
     list.add(new ItemStack(i,1,3));
     list.add(new ItemStack(i,1,4));
     list.add(new ItemStack(i,1,5));
     list.add(new ItemStack(i,1,6));
     list.add(new ItemStack(i,1,7));
     list.add(new ItemStack(i,1,8));
     list.add(new ItemStack(i,1,9));
     list.add(new ItemStack(i,1,10));
     list.add(new ItemStack(i,1,11));
     list.add(new ItemStack(i,1,12));
     list.add(new ItemStack(i,1,13));
     list.add(new ItemStack(i,1,14));
     list.add(new ItemStack(i,1,15));
 }
 
 
 }
