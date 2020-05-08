 package MoFoliage2.Blocks;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import MoFoliage2.Core.WorldChecker;
 import MoFoliage2.Refrences.BlockIds;
 import MoFoliage2.Refrences.ItemIds;
 import MoFoliage2.Refrences.Names;
 import MoFoliage2.Refrences.Refrence;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockFlower;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.EnumPlantType;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.IPlantable;
 
 public class BlockPodSeeds extends Block//Flower 
 implements IPlantable
 {
     public String podTrait1 = "";
     public String podTrait2 = "";
     public String podTrait3 = "";
     
     @SideOnly(Side.CLIENT)
     private Icon[] iconArray;
     
     public BlockPodSeeds(int id)
     {
         super(id, Material.plants);
         this.setUnlocalizedName(Names.POD_SEEDS_BLOCK_NAME);
         //this.setCreativeTab(MoFoliage.tab);
         this.setTickRandomly(true);
         float f = 0.2F;
         this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 3.0F, 0.5F + f);
         this.setCreativeTab((CreativeTabs)null);
         this.setHardness(0.0F);
         this.setStepSound(soundGrassFootstep);
         this.disableStats();
     }
     
     /*public TileEntity createNewTileEntity(World world)
     {
         return null;
     }*/
     
     protected final void checkFlowerChange(World par1World, int par2, int par3, int par4)
     {
         if (!this.canBlockStay(par1World, par2, par3, par4))
         {
             this.dropBlockAsItem(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4), 0);
             par1World.setBlock(par2, par3, par4, 0, 0, 2);
         }
     }
     
     public boolean canBlockStay(World par1World, int par2, int par3, int par4)
     {
         Block soil = blocksList[par1World.getBlockId(par2, par3 - 1, par4)];
         return (par1World.getFullBlockLightValue(par2, par3, par4) >= 8 || par1World.canBlockSeeTheSky(par2, par3, par4)) && 
                 (soil != null && (soil == Block.grass || soil == Block.dirt));
     }
     
     public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5)
     {
         super.onNeighborBlockChange(par1World, par2, par3, par4, par5);
         this.checkFlowerChange(par1World, par2, par3, par4);
     }
     
     protected boolean canThisPlantGrowOnThisBlockID(int par1)
     {
         return par1 == Block.tilledField.blockID;
     }
     
     /*public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4)
     {
         return par1World.getBlockId(par2, par3-1, par4) == Block.tilledField.blockID;
     }*/
     
     public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         super.updateTick(par1World, par2, par3, par4, par5Random);
 
         //System.out.println("Ticking");
         /*if (par1World.getBlockLightValue(par2, par3 + 1, par4) >= 9)
         {*/
             int l = par1World.getBlockMetadata(par2, par3, par4);
 
             if (l < 3)
             {
                 float f = this.getGrowthRate(par1World, par2, par3, par4);
 
                if (par5Random.nextInt((int)(/*25.0F / f*/ 5F) + 1) == 0)
                {
                     ++l;
                     par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 2);
                     
                     handlePodTraits(par1World, l, par2, par3, par4);
                }
             }
             else if(l == 3)
             {
                 String ClosestPod = PodTraits.GetClosestPodTrait(podTrait1, podTrait2, podTrait3);
                 System.out.println(ClosestPod);
                 for (int i = 0; i < PodTraits.POD_TRAIT_ARRAY.length; ++i)
                 {
                     if(PodTraits.POD_TRAIT_ARRAY[i] == ClosestPod)
                     {
                         par1World.setBlock(par2, par3, par4, BlockIds.POD_BLOCK_ID, i, 0);
                     }
                 }
                 
             }
         //}
     }
     
     public void handlePodTraits(World par1World, int meta, int x, int y, int z)
     {
         switch(meta)
         {
             case 1:
                if(par1World.getBlockLightValue(x, y, z) < 8)
                 {
                     podTrait1 = "Dark";
                 }
                 else
                 {
                     podTrait1 = "Light";
                 }
                 //System.out.println(podTrait1);
                 break;
                 
             case 2:
                if(WorldChecker.checkForWater(par1World, x, y, z) <= 2)
                 {
                     podTrait2 = "Damp";
                 }
                else if(WorldChecker.checkForWater(par1World, x, y, z) <= 4)
                 {
                     podTrait2 = "Normal";
                 }
                 else
                 {
                     podTrait2 = "Arid";
                 }
                 //System.out.println(podTrait2);
                 break;
             case 3:
                if(WorldChecker.checkForLava(par1World, x, y, z) <= 2)
                 {
                     podTrait3 = "Hot";
                 }
                else if(WorldChecker.checkForLava(par1World, x, y, z) <= 4)
                 {
                     podTrait3 = "Warm";
                 }
                 else
                 {
                     podTrait3 = "Cold";
                 }
                 //System.out.println(podTrait3);
                 break;
         }
     }
     
     private float getGrowthRate(World par1World, int par2, int par3, int par4)
     {
         float f = 1.0F;
         int l = par1World.getBlockId(par2, par3, par4 - 1);
         int i1 = par1World.getBlockId(par2, par3, par4 + 1);
         int j1 = par1World.getBlockId(par2 - 1, par3, par4);
         int k1 = par1World.getBlockId(par2 + 1, par3, par4);
         int l1 = par1World.getBlockId(par2 - 1, par3, par4 - 1);
         int i2 = par1World.getBlockId(par2 + 1, par3, par4 - 1);
         int j2 = par1World.getBlockId(par2 + 1, par3, par4 + 1);
         int k2 = par1World.getBlockId(par2 - 1, par3, par4 + 1);
         boolean flag = j1 == this.blockID || k1 == this.blockID;
         boolean flag1 = l == this.blockID || i1 == this.blockID;
         boolean flag2 = l1 == this.blockID || i2 == this.blockID || j2 == this.blockID || k2 == this.blockID;
 
         for (int l2 = par2 - 1; l2 <= par2 + 1; ++l2)
         {
             for (int i3 = par4 - 1; i3 <= par4 + 1; ++i3)
             {
                 int j3 = par1World.getBlockId(l2, par3 - 1, i3);
                 float f1 = 0.0F;
 
                 if (blocksList[j3] != null && blocksList[j3].canSustainPlant(par1World, l2, par3 - 1, i3, ForgeDirection.UP, this))
                 {
                     f1 = 1.0F;
 
                     if (blocksList[j3].isFertile(par1World, l2, par3 - 1, i3))
                     {
                         f1 = 3.0F;
                     }
                 }
 
                 if (l2 != par2 || i3 != par4)
                 {
                     f1 /= 4.0F;
                 }
 
                 f += f1;
             }
         }
 
         if (flag2 || flag && flag1)
         {
             f /= 2.0F;
         }
 
         return f;
     }
     
     protected int getSeedItem()
     {
         return ItemIds.POD_SEEDS_ID + Refrence.ITEM_ID_OFFSET;
     }
     
     /*protected int getCropItem()
     {
         return PodTraits.GetClosestPodTrait(podTrait1, podTrait2, podTrait3);
     }*/
     
     public void dropBlockAsItemWithChance(World par1World, int par2, int par3, int par4, int par5, float par6, int par7)
     {
         super.dropBlockAsItemWithChance(par1World, par2, par3, par4, par5, par6, 0);
     }
 
     @Override 
     public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
     {
         ArrayList<ItemStack> ret = super.getBlockDropped(world, x, y, z, metadata, fortune);
 
         if (metadata >= 3)
         {
             for (int n = 0; n < 3 + fortune; n++)
             {
                 if (world.rand.nextInt(15) <= metadata)
                 {
                     ret.add(new ItemStack(this.getSeedItem(), 1, 0));
                 }
             }
         }
 
         return ret;
     }
     
     public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
     {
         return null;
     }
     
     public boolean isOpaqueCube()
     {
         return false;
     }
     
     public boolean renderAsNormalBlock()
     {
         return false;
     }
     
     public int getRenderType()
     {
         return 1;
     }
     
     public int idDropped(int par1, Random par2Random, int par3)
     {
         return /*par1 == 3 ? this.getCropItem() :*/ this.getSeedItem();
     }
     
     public int quantityDropped(Random par1Random)
     {
         return 1;
     }
     
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int par1, int par2)
     {
         if (par2 < 0 || par2 > 3)
         {
             par2 = 3;
         }
 
         return this.iconArray[par2];
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
      */
     public int idPicked(World par1World, int par2, int par3, int par4)
     {
         return this.getSeedItem();
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * When this method is called, your block should register all the icons it needs with the given IconRegister. This
      * is the only chance you get to register icons.
      */
     public void registerIcons(IconRegister par1IconRegister)
     {
         this.iconArray = new Icon[4];
 
         for (int i = 0; i < this.iconArray.length; ++i)
         {
             this.iconArray[i] = par1IconRegister.registerIcon(Refrence.MOD_ID + ":" + Names.POD_SEEDS_BLOCK_NAME + "Stage" + i);
         }
     }
     
     public EnumPlantType getPlantType(World world, int x, int y, int z)
     {
         return EnumPlantType.Crop;
     }
     
     public int getPlantID(World world, int x, int y, int z)
     {
         return this.blockID;
     }
     
     public int getPlantMetadata(World world, int x, int y, int z)
     {
         return world.getBlockMetadata(x, y, z);
     }
 }
