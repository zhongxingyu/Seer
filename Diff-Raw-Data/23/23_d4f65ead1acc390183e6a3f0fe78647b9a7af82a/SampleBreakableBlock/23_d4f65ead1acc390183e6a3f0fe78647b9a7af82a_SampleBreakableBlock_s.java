 package stargazer.minecraft.samples.blockwithdrops;
 
 import java.util.ArrayList;
 import java.util.Random;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class SampleBreakableBlock extends Block
 {
   public static final int Bottom = 0;
   public static final int Top = 1;    
   public static final int North = 2;
   public static final int South = 3;
   public static final int West = 4;
   public static final int East = 5;
   
   @SideOnly(Side.CLIENT)
   private Icon[] icons;
 
   public SampleBreakableBlock(int blockId, Material material)
   {  
     super(blockId, material);
     this.icons = new Icon[6];
   }
 
   @SideOnly(Side.CLIENT)
   public Icon getIcon(int side, int metadata)
   {
     return this.icons[side];
   }
 
   @SideOnly(Side.CLIENT)
   public void registerIcons(IconRegister iconRegister)
   {
     this.icons[Top] = iconRegister.registerIcon("SampleBreakableBlock:top");
     this.icons[Bottom] = iconRegister.registerIcon("SampleBreakableBlock:bottom");
     this.icons[North] = iconRegister.registerIcon("SampleBreakableBlock:north");
     this.icons[South] = iconRegister.registerIcon("SampleBreakableBlock:south");
     this.icons[East] = iconRegister.registerIcon("SampleBreakableBlock:east");
     this.icons[West] = iconRegister.registerIcon("SampleBreakableBlock:west");
   }
 
   @Override
   public void breakBlock(World world, int x, int y, int z, int par5, int par6)
   {    
     super.breakBlock(world, x, y, z, par5, par6);
   }
   
   /**
    * This returns a complete list of items dropped from this block.
    *
    * @param world The current world
    * @param x X Position
    * @param y Y Position
    * @param z Z Position
    * @param metadata Current metadata
    * @param fortune Breakers fortune level
    * @return A ArrayList containing all items this block drops
    */
   @Override
   public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
   {
     // Get the default drops (which is just the block itself)
     ArrayList<ItemStack> ret = super.getBlockDropped(world, x, y, z, metadata, fortune);
 
     // Drop an additional stack of 0 to 4 items with no metadata.
    ret.add(new ItemStack(SampleItem.SAMPLE_ITEM_ID, world.rand.nextInt(5), 0));
     return ret;
   }  
 }
