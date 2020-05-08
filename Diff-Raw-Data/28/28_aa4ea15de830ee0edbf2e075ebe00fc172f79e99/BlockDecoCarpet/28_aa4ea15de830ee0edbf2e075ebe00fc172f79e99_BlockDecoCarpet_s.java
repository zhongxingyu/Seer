 package ruby.bamboo.block;
 
 import java.util.List;
 
 import ruby.bamboo.BambooCore;
 import ruby.bamboo.BambooInit;
 import ruby.bamboo.BambooUtil;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockCarpet;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class BlockDecoCarpet extends BlockCarpet
 {
     public BlockDecoCarpet(int par1)
     {
         super(par1);
         setTickRandomly(false);
         setStepSound(Block.soundSandFootstep);
     }
     @Override
     public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack)
     {
         int meta = (par1World.getBlockMetadata(par2, par3, par4)) + ((BambooUtil.getPlayerDir(par5EntityLivingBase) & 1) != 0 ? 0 : 1);
         par1World.setBlockMetadataWithNotify(par2, par3, par4, meta, 3);
     }
     @Override
     public int damageDropped(int par1)
     {
         return par1 & 0x0E;
     }
    /*
    @Override
    public void dropBlockAsItemWithChance(World world, int i, int j, int k, int l, float f, int i1)
    {
        if (!world.isRemote){
            dropBlockAsItem_do(world, i, j, k, new ItemStack(this.blockID,1,l&0x0E));
        }
    }*/
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int par1, int par2)
     {
         switch (par2)
         {
             case 0:
                 return Block.blocksList[BambooInit.dSquareBID].getIcon(par1, 0);
 
             case 1:
                 return Block.blocksList[BambooInit.dSquareBID].getIcon(par1, 1);
 
             case 2:
                 return Block.blocksList[BambooInit.dSquareBID].getIcon(par1, 4);
 
             case 3:
                 return Block.blocksList[BambooInit.dSquareBID].getIcon(par1, 5);
 
             case 4:
                 return Block.blocksList[BambooInit.dSquareBID].getIcon(par1, 8);
 
             case 5:
                 return Block.blocksList[BambooInit.dSquareBID].getIcon(par1, 9);
 
             case 6:
                 return Block.blocksList[BambooInit.dSquareBID].getIcon(par1, 10);
 
             case 7:
                 return Block.blocksList[BambooInit.dSquareBID].getIcon(par1, 11);
 
             case 8:
                 return Block.blocksList[BambooInit.dSquareBID].getIcon(par1, 12);
 
             case 9:
                 return Block.blocksList[BambooInit.dSquareBID].getIcon(par1, 13);
 
             default:
                 return Block.blocksList[BambooInit.dSquareBID].getIcon(par1, 0);
         }
     }
     @Override
     @SideOnly(Side.CLIENT)
     public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
     {
         for (int i = 0; i < 10; i += 2)
         {
             par3List.add(new ItemStack(par1, 1, i));
         }
     }
 }
