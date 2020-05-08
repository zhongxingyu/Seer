 package f01405.TestMod.Blocks;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockRotatedPillar;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class blockFireLog extends BlockRotatedPillar
 {
     /** The type of tree this log came from. */
     public static final String[] woodType = new String[] {"fire"};
     @SideOnly(Side.CLIENT)
     private Icon[] field_111052_c;
     @SideOnly(Side.CLIENT)
     private Icon[] tree_top;
 
     public blockFireLog(int par1)
     {
         super(par1, Material.wood);
     }
 
     /**
      * Returns the quantity of items to drop on block destruction.
      */
     public int quantityDropped(Random par1Random)
     {
         return 1;
     }
 
     /**
      * Returns the ID of the items to drop on destruction.
      */
     public int idDropped(int par1, Random par2Random, int par3)
     {
         return this.blockID;
     }
 
     /**
      * ejects contained items into the world, and notifies neighbours of an update, as appropriate
      */
     public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
     {
         byte b0 = 4;
         int j1 = b0 + 1;
 
         if (par1World.checkChunksExist(par2 - j1, par3 - j1, par4 - j1, par2 + j1, par3 + j1, par4 + j1))
         {
             for (int k1 = -b0; k1 <= b0; ++k1)
             {
                 for (int l1 = -b0; l1 <= b0; ++l1)
                 {
                     for (int i2 = -b0; i2 <= b0; ++i2)
                     {
                         int j2 = par1World.getBlockId(par2 + k1, par3 + l1, par4 + i2);
 
                         if (Block.blocksList[j2] != null)
                         {
                             Block.blocksList[j2].beginLeavesDecay(par1World, par2 + k1, par3 + l1, par4 + i2);
                         }
                     }
                 }
             }
         }
     }
 
     @SideOnly(Side.CLIENT)
     protected Icon func_111048_c(int par1)
     {
         return this.field_111052_c[par1];
     }
 
     @SideOnly(Side.CLIENT)
     protected Icon func_111049_d(int par1)
     {
         return this.tree_top[par1];
     }
 
     /**
      * returns a number between 0 and 3
      */
     public static int limitToValidMetadata(int par0)
     {
         return par0 & 3;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
      */
     public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
     {
         par3List.add(new ItemStack(par1, 1, 0));
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * When this method is called, your block should register all the icons it needs with the given IconRegister. This
      * is the only chance you get to register icons.
      */
     public void registerIcons(IconRegister par1IconRegister)
     {
         this.field_111052_c = new Icon[woodType.length];
         this.tree_top = new Icon[woodType.length];
 
         for (int i = 0; i < this.field_111052_c.length; ++i)
         {
             this.field_111052_c[i] = par1IconRegister.registerIcon("testmod:log_" + woodType[i]);
             this.tree_top[i] = par1IconRegister.registerIcon("testmod:log_" + woodType[i] + "_top");
         }
     }
 
     @Override
     public boolean canSustainLeaves(World world, int x, int y, int z)
     {
         return true;
     }
 
     @Override
     public boolean isWood(World world, int x, int y, int z)
     {
         return true;
     }
 }
 
