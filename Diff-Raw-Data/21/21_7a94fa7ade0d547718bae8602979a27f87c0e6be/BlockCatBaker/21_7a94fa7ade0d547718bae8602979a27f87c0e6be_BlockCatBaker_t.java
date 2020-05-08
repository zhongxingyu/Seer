 package hobmod.block;
  
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import hobmod.lib.BlockIds;
 import hobmod.lib.Refrence;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.audio.SoundManager;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.world.World;
 
 public class BlockCatBaker extends Block
 {
     /** Whether this lamp block is the powered version. */
     private final boolean powered;
 
     public BlockCatBaker(int id, Material par2Material, boolean par2)
     {
         super(id, par2Material);
         
         this.powered = par2;
 
         if (par2)
         {
             this.setLightValue(1.0F);
         }
     }
 
     /**
      * Called whenever the block is added into the world. Args: world, x, y, z
      */
     
     Minecraft mc = Minecraft.getMinecraft();
     
     
     public void onBlockAdded(World par1World, int par2, int par3, int par4)
     {
         if (!par1World.isRemote)
         {
             if (this.powered && !par1World.isBlockIndirectlyGettingPowered(par2, par3, par4))
             {
                par1World.setBlock(par2, par3, par4, BlockIds.CATBAKER, 0, 2);
                 mc.sndManager.stopAllSounds();
             }
             else if (!this.powered && par1World.isBlockIndirectlyGettingPowered(par2, par3, par4))
             {
                 par1World.setBlock(par2, par3, par4, BlockIds.CATBAKERACTIVE, 0, 2);
                 mc.sndManager.playSound("hobmod:BakerCat", par2, par3, par4,1.0F,1.0F);
             }
         }
     }
 
     /**
      * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
      * their own) Args: x, y, z, neighbor blockID
      */
     public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5)
     {
         if (!par1World.isRemote)
         {
             if (this.powered && !par1World.isBlockIndirectlyGettingPowered(par2, par3, par4))
             {
                par1World.setBlock(par2, par3, par4, BlockIds.CATBAKER, 0, 2);
                 mc.sndManager.stopAllSounds();
             }
             else if (!this.powered && par1World.isBlockIndirectlyGettingPowered(par2, par3, par4))
             {
                 par1World.setBlock(par2, par3, par4, BlockIds.CATBAKERACTIVE, 0, 2);
                 mc.sndManager.playSound("hobmod:BakerCat", par2, par3, par4,1.0F,1.0F);
             }
         }
     }
 
     /**
      * Ticks the block if it's been scheduled
      */
     public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         if (!par1World.isRemote && this.powered && !par1World.isBlockIndirectlyGettingPowered(par2, par3, par4))
         {
             par1World.setBlock(par2, par3, par4, BlockIds.CATBAKER, 0, 2);
         }
     }
 
     /**
      * Returns the ID of the items to drop on destruction.
      */
     public int idDropped(int par1, Random par2Random, int par3)
     {
         return BlockIds.CATBAKER;
     }
     
     public void breakBlock(){
     	mc.sndManager.stopAllSounds();
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
      */
     public int idPicked(World par1World, int par2, int par3, int par4)
     {
         return  BlockIds.CATBAKER;
     }
     public void registerIcons(IconRegister par1IconRegister)
     {
     this.blockIcon = par1IconRegister.registerIcon(Refrence.MOD_ID + ":" + this.getUnlocalizedName().substring(5));
     }
 }
