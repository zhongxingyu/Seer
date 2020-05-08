 package com.madpc.coffee.block;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockLeaves;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.init.Blocks;
 import net.minecraft.init.Items;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.IIcon;
 import net.minecraft.world.ColorizerFoliage;
 import net.minecraft.world.World;
 import net.minecraftforge.common.IShearable;
 
 import com.madpc.coffee.Coffee;
 import com.madpc.coffee.lib.Strings;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockCoffeeLeaves extends BlockLeaves implements IShearable {
     
     int[] adjacentTreeBlocks;
     private int field_94394_cP;
     private IIcon[][] iconArray = new IIcon[2][];
     
 
     protected BlockCoffeeLeaves() {
         super();
         this.func_149675_a(true);
         this.func_149647_a(Coffee.tabsCoffee);
         this.func_149663_c(Strings.COFFEE_LEAVES_NAME);
     }
     
     @SideOnly(Side.CLIENT)
     public int getBlockColor(){
         double d0 = 0.5D;
         double d1 = 1.0D;
         return ColorizerFoliage.getFoliageColor(d0, d1);
     }
     
     public void func_149749_a(World par1World, int par2, int par3, int par4, int par5, int par6)
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
                         Block j2 = par1World.func_147439_a(par2 + k1, par3 + l1, par4 + i2);
 
                         if (j2.isLeaves(par1World, par2, par3, par4))
                         {
                         	j2.beginLeavesDecay(par1World, par2 + k1, par3 + l1, par4 + i2);
                         }
                     }
                 }
             }
         }
     }
     public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         if (!par1World.isRemote)
         {
             int l = par1World.getBlockMetadata(par2, par3, par4);
 
             if ((l & 8) != 0 && (l & 4) == 0)
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
                                Block block = par1World.func_147439_a(par2 + l1, par3 + i2, par4 + j2);
 
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
                     par1World.setBlockMetadataWithNotify(par2, par3, par4, l & -9, 4);
                 }
                 else
                 {
                     this.removeLeaves(par1World, par2, par3, par4);
                 }
             }
         }
     }
     
     public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         if (par1World.canLightningStrikeAt(par2, par3 + 1, par4) && !par1World.func_147466_a(par1World,par2, par3 - 1, par4) && par5Random.nextInt(15) == 1)
         {
             double d0 = (double)((float)par2 + par5Random.nextFloat());
             double d1 = (double)par3 - 0.05D;
             double d2 = (double)((float)par4 + par5Random.nextFloat());
             par1World.spawnParticle("dripWater", d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
     }
 
     private void removeLeaves(World par1World, int par2, int par3, int par4)
     {
         this.func_149697_b(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4), 0);
         par1World.func_147468_f(par2, par3, par4);
     }
 
    
     public int quantityDropped(Random par1Random)
     {
         return par1Random.nextInt(20) == 0 ? 1 : 0;
     }
 
     
     public Item idDropped(int par1, Random par2Random, int par3)
     {
         return Item.func_150898_a(Blocks.sapling);
     }
 
     
     public void dropBlockAsItemWithChance(World par1World, int par2, int par3, int par4, int par5, float par6, int par7)
     {
         if (!par1World.isRemote)
         {
             int j1 = 20;
 
             if ((par5 & 3) == 3)
             {
                 j1 = 40;
             }
 
             if (par7 > 0)
             {
                 j1 -= 2 << par7;
 
                 if (j1 < 10)
                 {
                     j1 = 10;
                 }
             }
 
             if (par1World.rand.nextInt(j1) == 0)
             {
                 Item k1 = this.func_149650_a(par5, par1World.rand, par7);
                 this.func_149642_a(par1World, par2, par3, par4, new ItemStack(k1, 1, this.damageDropped(par5)));
             }
 
             j1 = 200;
 
             if (par7 > 0)
             {
                 j1 -= 10 << par7;
 
                 if (j1 < 40)
                 {
                     j1 = 40;
                 }
             }
 
             if ((par5 & 3) == 0 && par1World.rand.nextInt(j1) == 0)
             {
                 this.func_149642_a(par1World, par2, par3, par4, new ItemStack(Items.apple, 1, 0));
             }
         }
     }
     public void harvestBlock(World par1World, EntityPlayer par2EntityPlayer, int par3, int par4, int par5, int par6)
     {
         super.func_149636_a(par1World, par2EntityPlayer, par3, par4, par5, par6);
     }
     public int damageDropped(int par1)
     {
         return par1 & 3;
     }
     public boolean isOpaqueCube()
     {
         return !this.field_150121_P;
     }
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon func_149691_a(int par1, int par2)
    {
        return (par2 & 3) == 1 ? this.iconArray[this.field_94394_cP][1] : ((par2 & 3) == 3 ? this.iconArray[this.field_94394_cP][3] : this.iconArray[this.field_94394_cP][0]);
    }
     
     @SideOnly(Side.CLIENT)
     public void setGraphicsLevel(boolean par1)
     {
         this.field_150121_P = par1;
         this.field_150127_b = par1 ? 0 : 1;
     }
 
 
     protected ItemStack createStackedBlock(int par1)
     {
         return new ItemStack(Item.func_150898_a(this), 1, par1 & 3);
     }
     
 
     public boolean isShearable(ItemStack item, World world, int x, int y, int z)
     {
         return true;
     }
 
     
     public ArrayList<ItemStack> onSheared(ItemStack item, World world, int x, int y, int z, int fortune)
     {
         ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
         ret.add(new ItemStack(this, 1, world.getBlockMetadata(x, y, z) & 3));
         return ret;
     }
 
     @Override
     public void beginLeavesDecay(World world, int x, int y, int z)
     {
         world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) | 8, 4);
     }
 
     
     public boolean isLeaves(World world, int x, int y, int z)
     {
         return true;
     }
 
 	@Override
 	public String[] func_150125_e()
 	{
		// TODO Auto-generated method stub
 		return null;
 	}
 }
