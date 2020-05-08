 package MMC.neocraft.block;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import MMC.neocraft.item.NCitem;
 import MMC.neocraft.lib.Reference;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.IShearable;
 
 public class BlockOrangeLeaves extends NCblock implements IShearable
 {
 	@SideOnly(Side.CLIENT)
     private Icon[] icons;
     int[] adjacentTreeBlocks;
 
 	protected BlockOrangeLeaves(int par1)
 	{
 		super(par1, Material.leaves);
         setTickRandomly(true);
 	}
 	
     @SideOnly(Side.CLIENT)
 
     /**
      * A randomly called display update to be able to add particles or other items for display
      */
     public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         if (par1World.canLightningStrikeAt(par2, par3 + 1, par4) && !par1World.doesBlockHaveSolidTopSurface(par2, par3 - 1, par4) && par5Random.nextInt(15) == 1)
         {
             double d0 = (double)((float)par2 + par5Random.nextFloat());
             double d1 = (double)par3 - 0.05D;
             double d2 = (double)((float)par4 + par5Random.nextFloat());
             par1World.spawnParticle("dripWater", d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
     }
     @Override
     public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
     {
         byte b0 = 1;
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
     @Override
     public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         if (!par1World.isRemote)
         {
             int meta = par1World.getBlockMetadata(par2, par3, par4);
             
             if ((meta & 8) != 0 && (meta & 4) == 0)
             {
                 byte b0 = 4;
                 int i1 = b0 + 1;
                 byte b1 = 32;
                 int j1 = b1 * b1;
                 int k1 = b1 / 2;
 
                 if (this.adjacentTreeBlocks == null)
                 {
                     this.adjacentTreeBlocks = new int[b1 * b1 * b1];
                 }
 
                 int l1;
 
                 if (par1World.checkChunksExist(par2 - i1, par3 - i1, par4 - i1, par2 + i1, par3 + i1, par4 + i1))
                 {
                     int i2;
                     int j2;
                     int k2;
 
                     for (l1 = -b0; l1 <= b0; ++l1)
                     {
                         for (i2 = -b0; i2 <= b0; ++i2)
                         {
                             for (j2 = -b0; j2 <= b0; ++j2)
                             {
                                 k2 = par1World.getBlockId(par2 + l1, par3 + i2, par4 + j2);
 
                                 Block block = Block.blocksList[k2];
 
                                 if (block != null && block.canSustainLeaves(par1World, par2 + l1, par3 + i2, par4 + j2))
                                 {
                                     this.adjacentTreeBlocks[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = 0;
                                 }
                                 else if (block != null && block.isLeaves(par1World, par2 + l1, par3 + i2, par4 + j2))
                                 {
                                     this.adjacentTreeBlocks[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -2;
                                 }
                                 else
                                 {
                                     this.adjacentTreeBlocks[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -1;
                                 }
                             }
                         }
                     }
 
                     for (l1 = 1; l1 <= 4; ++l1)
                     {
                         for (i2 = -b0; i2 <= b0; ++i2)
                         {
                             for (j2 = -b0; j2 <= b0; ++j2)
                             {
                                 for (k2 = -b0; k2 <= b0; ++k2)
                                 {
                                     if (this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1] == l1 - 1)
                                     {
                                         if (this.adjacentTreeBlocks[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                         }
 
                                         if (this.adjacentTreeBlocks[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                         }
 
                                         if (this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] = l1;
                                         }
 
                                         if (this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] = l1;
                                         }
 
                                         if (this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] = l1;
                                         }
 
                                         if (this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] = l1;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
                 
                 l1 = this.adjacentTreeBlocks[k1 * j1 + k1 * b1 + k1];
 
                 if (l1 >= 0)
                 {
                     par1World.setBlockMetadataWithNotify(par2, par3, par4, meta & -9, 4);
                 }
                 else
                 {
                     this.removeLeaves(par1World, par2, par3, par4);
                 }
             }
             else
             {
             	if((meta & 1) == 0)
             	{
             		if(par1World.rand.nextInt(25) == 0) { par1World.setBlockMetadataWithNotify(par2, par3, par4, 1, 3); }
             	}
             	else if((meta & 1) == 1)
             	{
             		if(par1World.rand.nextInt(25) == 0) { par1World.setBlockMetadataWithNotify(par2, par3, par4, 0, 3); }		
             	}
             }
         }
     }
     private void removeLeaves(World par1World, int par2, int par3, int par4)
     {
         this.dropBlockAsItem(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4), 0);
         par1World.setBlockToAir(par2, par3, par4);
     }
 
     /**
      * Returns the quantity of items to drop on block destruction.
      */
     public int quantityDropped(Random rand) 
     { 
     	int r = rand.nextInt(20) - 10;
     	return r > 0 ? (r % 3) + 1 : 0; 
     }
 
     /**
      * Returns the ID of the items to drop on destruction.
      */
     public int idDropped(int par1, Random par2Random, int par3) { return Item.stick.itemID; }
 
     /**
      * Drops the block items with a specified chance of dropping the specified items
      */
     public void dropBlockAsItemWithChance(World par1World, int par2, int par3, int par4, int par5, float par6, int par7)
     {
        if ((par5 & 1) == 1 && par1World.rand.nextInt(3) == 0)
         {
             this.dropBlockAsItem_do(par1World, par2, par3, par4, new ItemStack(NCitem.fruitOrange, 1, 0));
         }
     }
 
     /**
      * Called when the player destroys a block with an item that can harvest it. (i, j, k) are the coordinates of the
      * block and l is the block's subtype/damage.
      */
     public void harvestBlock(World par1World, EntityPlayer par2EntityPlayer, int par3, int par4, int par5, int par6)
     {
         super.harvestBlock(par1World, par2EntityPlayer, par3, par4, par5, par6);
     }
 
     @SideOnly(Side.CLIENT)
     /**
      * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
      */
     public Icon getIcon(int par1, int par2) { return icons[par2 & 1]; }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	@SideOnly(Side.CLIENT)
     /**
      * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
      */
     public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
     {
         par3List.add(new ItemStack(par1, 1, 0));
         par3List.add(new ItemStack(par1, 1, 1));
     }
     @Override protected ItemStack createStackedBlock(int par1) { return new ItemStack(this.blockID, 1, par1 & 1); }
     @SideOnly(Side.CLIENT)
     /**
      * When this method is called, your block should register all the icons it needs with the given IconRegister. This
      * is the only chance you get to register icons.
      */
     public void registerIcons(IconRegister register)
     {
     	icons = new Icon[2];
 		for(int i = 0; i < icons.length; i ++) { icons[i] = register.registerIcon(Reference.MOD_ID + ":" + this.getUnlocalizedName().substring(5) + "-" + i); }
     }
     @Override public boolean isShearable(ItemStack item, World world, int x, int y, int z) { return true; }
     @Override
     public ArrayList<ItemStack> onSheared(ItemStack item, World world, int x, int y, int z, int fortune)
     {
         ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
         ret.add(new ItemStack(Item.stick, 2, 0));
         int i = world.rand.nextInt(5) - 1;
         if((world.getBlockMetadata(x, y, z) & 1) == 1 && i > 0) { ret.add(new ItemStack(NCitem.fruitOrange, i, 0)); }
         return ret;
     }
     @Override public void beginLeavesDecay(World world, int x, int y, int z) { world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) | 8, 4); }
     @Override public boolean isLeaves(World world, int x, int y, int z) { return true; }
 }
