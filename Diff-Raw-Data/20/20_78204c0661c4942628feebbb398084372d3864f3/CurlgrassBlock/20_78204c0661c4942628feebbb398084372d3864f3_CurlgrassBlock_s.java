 package mods.elysium.block;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.stats.StatList;
 import net.minecraft.util.Icon;
 import net.minecraft.world.ColorizerFoliage;
 import net.minecraft.world.ColorizerGrass;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 import net.minecraftforge.common.ForgeHooks;
 import net.minecraftforge.common.IShearable;
 
 public class CurlgrassBlock extends ElysiumFlower implements IShearable
 {
 //    private static final String[] grassTypes = new String[] {"deadbush", "tallgrass", "fern"};
 //    @SideOnly(Side.CLIENT)
 //    private Icon[] iconArray;
 
     public CurlgrassBlock(int par1)
     {
         super(par1, Material.vine);
         float f = 0.4F;
         this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.8F, 0.5F + f);
     }
 
 //    @SideOnly(Side.CLIENT)
 //
 //    /**
 //     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
 //     */
 //    public Icon getIcon(int par1, int par2)
 //    {
 //        if (par2 >= this.iconArray.length)
 //        {
 //            par2 = 0;
 //        }
 //
 //        return this.iconArray[par2];
 //    }
 
     /**
      * Returns the ID of the items to drop on destruction.
      */
     public int idDropped(int par1, Random par2Random, int par3)
     {
         return -1;
     }
 
     /**
      * Returns the usual quantity dropped by the block plus a bonus of 1 to 'i' (inclusive).
      */
     public int quantityDroppedWithBonus(int par1, Random par2Random)
     {
         return 1 + par2Random.nextInt(par1 * 2 + 1);
     }
 
     /**
      * Called when the player destroys a block with an item that can harvest it. (i, j, k) are the coordinates of the
      * block and l is the block's subtype/damage.
      */
     public void harvestBlock(World par1World, EntityPlayer par2EntityPlayer, int par3, int par4, int par5, int par6)
     {
         super.harvestBlock(par1World, par2EntityPlayer, par3, par4, par5, par6);
     }
 
 //    @SideOnly(Side.CLIENT)
 //    public int getBlockColor()
 //    {
 //        double d0 = 0.5D;
 //        double d1 = 1.0D;
 //        return ColorizerGrass.getGrassColor(d0, d1);
 //    }
 
 //    @SideOnly(Side.CLIENT)
 //
 //    /**
 //     * Returns the color this block should be rendered. Used by leaves.
 //     */
 //    public int getRenderColor(int par1)
 //    {
 //        return par1 == 0 ? 16777215 : ColorizerFoliage.getFoliageColorBasic();
 //    }
 
 //    @SideOnly(Side.CLIENT)
 //
 //    /**
 //     * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
 //     * when first determining what to render.
 //     */
 //    public int colorMultiplier(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
 //    {
 //        int l = par1IBlockAccess.getBlockMetadata(par2, par3, par4);
 //        return l == 0 ? 16777215 : par1IBlockAccess.getBiomeGenForCoords(par2, par4).getBiomeGrassColor();
 //    }
 
 //    /**
 //     * Get the block's damage value (for use with pick block).
 //     */
 //    public int getDamageValue(World par1World, int par2, int par3, int par4)
 //    {
 //        return par1World.getBlockMetadata(par2, par3, par4);
 //    }
 
 //    @SideOnly(Side.CLIENT)
 //
 //    /**
 //     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
 //     */
 //    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
 //    {
 //        for (int j = 1; j < 3; ++j)
 //        {
 //            par3List.add(new ItemStack(par1, 1, j));
 //        }
 //    }
 
 //    @SideOnly(Side.CLIENT)
 //
 //    /**
 //     * When this method is called, your block should register all the icons it needs with the given IconRegister. This
 //     * is the only chance you get to register icons.
 //     */
 //    public void registerIcons(IconRegister par1IconRegister)
 //    {
 //        this.iconArray = new Icon[grassTypes.length];
 //
 //        for (int i = 0; i < this.iconArray.length; ++i)
 //        {
 //            this.iconArray[i] = par1IconRegister.registerIcon(grassTypes[i]);
 //        }
 //    }
 
     @Override
     public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int meta, int fortune)
     {
         ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
         if (world.rand.nextInt(8) != 0)
         {
             return ret;
         }
 
        ItemStack item = ForgeHooks.getGrassSeed(world);
         if (item != null)
         {
             ret.add(item);
         }
         return ret;
     }
 
     @Override
     public boolean isShearable(ItemStack item, World world, int x, int y, int z)
     {
         return true;
     }
 
     @Override
     public ArrayList<ItemStack> onSheared(ItemStack item, World world, int x, int y, int z, int fortune)
     {
         ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
         ret.add(new ItemStack(this, 1, world.getBlockMetadata(x, y, z)));
         return ret;
     }
 }
