 package mods.elysium.block;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import java.util.List;
 import java.util.Random;
 
 import mods.elysium.gen.ElysiumGenFostimber;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraft.world.gen.feature.WorldGenBigTree;
 import net.minecraft.world.gen.feature.WorldGenForest;
 import net.minecraft.world.gen.feature.WorldGenHugeTrees;
 import net.minecraft.world.gen.feature.WorldGenTaiga2;
 import net.minecraft.world.gen.feature.WorldGenTrees;
 import net.minecraft.world.gen.feature.WorldGenerator;
 
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.event.terraingen.TerrainGen;
 
 public class FostimberSaplingBlock extends ElysiumFlower
 {
 //    public static final String[] WOOD_TYPES = new String[] {"oak", "spruce", "birch", "jungle"};
 //    private static final String[] field_94370_b = new String[] {"sapling", "sapling_spruce", "sapling_birch", "sapling_jungle"};
 //    @SideOnly(Side.CLIENT)
 //    private Icon[] saplingIcon;
 
     public FostimberSaplingBlock(int par1)
     {
         super(par1);
         float f = 0.4F;
         this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
     }
 
     /**
      * Ticks the block if it's been scheduled
      */
     public void updateTick(World world, int par2, int par3, int par4, Random par5Random)
     {
         if (!world.isRemote)
         {
             super.updateTick(world, par2, par3, par4, par5Random);
 
             if (world.getBlockLightValue(par2, par3 + 1, par4) >= 9 && par5Random.nextInt(7) == 0)
             {
                 this.markOrGrowMarked(world, par2, par3, par4, par5Random);
             }
         }
     }
     
 //    @SideOnly(Side.CLIENT)
 //
 //    /**
 //     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
 //     */
 //    public Icon getIcon(int par1, int par2)
 //    {
 //        par2 &= 3;
 //        return this.saplingIcon[par2];
 //    }
 
     public void markOrGrowMarked(World world, int par2, int par3, int par4, Random par5Random)
     {
         int l = world.getBlockMetadata(par2, par3, par4);
 
         if ((l & 8) == 0)
         {
             world.setBlockMetadataWithNotify(par2, par3, par4, l | 8, 4);
         }
         else
         {
             this.growTree(world, par2, par3, par4, par5Random);
         }
     }
 
     /**
      * Attempts to grow a sapling into a tree
      */
     public void growTree(World world, int i, int j, int k, Random rand)
     {
         if (!TerrainGen.saplingGrowTree(world, rand, i, j, k)) return;
 
        ElyisumGenFostimber fostimber = new ElysiumGenFostimber(true);
 		
 		fostimber.generate(world, rand, i, j, k);
     }
 
     /**
      * Determines if the same sapling is present at the given location.
      */
     public boolean isSameSapling(World world, int par2, int par3, int par4, int par5)
     {
         return world.getBlockId(par2, par3, par4) == this.blockID && (world.getBlockMetadata(par2, par3, par4) & 3) == par5;
     }
 
 //    /**
 //     * Determines the damage on the item the block drops. Used in cloth and wood.
 //     */
 //    public int damageDropped(int par1)
 //    {
 //        return par1 & 3;
 //    }
 
 //    @SideOnly(Side.CLIENT)
 //
 //    /**
 //     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
 //     */
 //    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
 //    {
 //        par3List.add(new ItemStack(par1, 1, 0));
 //        par3List.add(new ItemStack(par1, 1, 1));
 //        par3List.add(new ItemStack(par1, 1, 2));
 //        par3List.add(new ItemStack(par1, 1, 3));
 //    }
 
 //    @SideOnly(Side.CLIENT)
 //
 //    /**
 //     * When this method is called, your block should register all the icons it needs with the given IconRegister. This
 //     * is the only chance you get to register icons.
 //     */
 //    public void registerIcons(IconRegister par1IconRegister)
 //    {
 //        this.saplingIcon = new Icon[field_94370_b.length];
 //
 //        for (int i = 0; i < this.saplingIcon.length; ++i)
 //        {
 //            this.saplingIcon[i] = par1IconRegister.registerIcon(field_94370_b[i]);
 //        }
 //    }
 }
