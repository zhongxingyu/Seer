 package modJam;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 public class BlockLightGenerator extends Block{
 
 	public BlockLightGenerator(int par1) {
 		super(par1, Material.circuits);
 		this.setStepSound(soundMetalFootstep);
 		this.setBlockBounds(0.25F, 0.35F, 0.25F, 0.75F, 0.65F, 0.75F);
 	}
 	
     /**
      * Updates the blocks bounds based on its current state. Args: world, x, y, z
      */
     public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
     {
         boolean map[] = getBlockSideMap(par1IBlockAccess, par2, par3, par4);
         float f = 0.25F;
         float f1 = 0.75F;
         float f2 = 0.25F;
         float f3 = 0.75F;
         float f4 = 0.35F;
         float f5 = 0.65F;
 
         if (map[4])
         {
            f2 = 0F;
         }
 
         if (map[5])
         {
            f3 = 1F;
         }
 
         if (map[2])
         {
            f = 0F;
         }
 
         if (map[3])
         {
            f1 = 1F;
         }
 
         if (map[0]){
        	f4 = 0F;
         }
         
         if (map[1]){
        	f5 = 1F;
         }
         if(map[0] && map[1] && map[2] && map[3] && map[4] && map[5]){
         	f = 0.2F;
         	f1 = 0.8F;
         	f2 = 0.2F;
         	f3 = 0.8F;
         	f4 = 0.2F;
         	f5 = 0.8F;
         }
         this.setBlockBounds(f, f4, f2, f1, f5, f3);
     }
     
 	@Override
     public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
     	super.onNeighborBlockChange(par1World, par2, par3, par4, par5);
     	par1World.updateAllLightTypes(par2, par3 - 1, par4);
     	par1World.updateAllLightTypes(par2, par3 + 1, par4);
     	par1World.updateAllLightTypes(par2 - 1, par3, par4);
     	par1World.updateAllLightTypes(par2 + 1, par3, par4);
     	par1World.updateAllLightTypes(par2, par3, par4 - 1);
     	par1World.updateAllLightTypes(par2, par3, par4 + 1);
     }
     
 	@Override
 	public void onBlockAdded(World par1World, int par2, int par3, int par4){
 		super.onBlockAdded(par1World, par2, par3, par4);
     	par1World.updateAllLightTypes(par2, par3 - 1, par4);
     	par1World.updateAllLightTypes(par2, par3 + 1, par4);
     	par1World.updateAllLightTypes(par2 - 1, par3, par4);
     	par1World.updateAllLightTypes(par2 + 1, par3, par4);
     	par1World.updateAllLightTypes(par2, par3, par4 - 1);
     	par1World.updateAllLightTypes(par2, par3, par4 + 1);
 	}
 	
     @Override
     public void onBlockDestroyedByPlayer(World par1World, int par2, int par3, int par4, int par5) {
     	super.onBlockDestroyedByPlayer(par1World, par2, par3, par4, par5);
     	par1World.updateAllLightTypes(par2, par3 - 1, par4);
     	par1World.updateAllLightTypes(par2, par3 + 1, par4);
     	par1World.updateAllLightTypes(par2 - 1, par3, par4);
     	par1World.updateAllLightTypes(par2 + 1, par3, par4);
     	par1World.updateAllLightTypes(par2, par3, par4 - 1);
     	par1World.updateAllLightTypes(par2, par3, par4 + 1);
     }
     
     @Override
     public int getLightValue(IBlockAccess par1IBlockAccess, int par2, int par3, int par4){
     	return par1IBlockAccess.getBlockMetadata(par2, par3, par4);
     }
     
 	@Override
 	public boolean renderAsNormalBlock(){
 		return false;
 	}
 	
 	@Override
 	public boolean isOpaqueCube(){
 		return false;
 	}
 	
 	@Override
     public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
     {
 		return true;
     }
 	
 	public boolean isBlockAir(IBlockAccess par1World, int par2, int par3, int par4){
 		return par1World.getBlockId(par2, par3, par4) == 0;
 	}
 
 	public boolean[] getBlockSideMap(IBlockAccess par1World, int par2, int par3, int par4, int block){
 		boolean[] returnValue = new boolean[6];
 		returnValue[0] = !isBlockAir(par1World, par2, par3 - 1, par4) && par1World.getBlockId(par2, par3 - 1, par4) == block;
 		returnValue[1] = !isBlockAir(par1World, par2, par3 + 1, par4) && par1World.getBlockId(par2, par3 + 1, par4) == block;
 		returnValue[2] = !isBlockAir(par1World, par2 - 1, par3, par4) && par1World.getBlockId(par2 - 1, par3, par4) == block;
 		returnValue[3] = !isBlockAir(par1World, par2 + 1, par3, par4) && par1World.getBlockId(par2 + 1, par3, par4) == block;
 		returnValue[4] = !isBlockAir(par1World, par2, par3, par4 - 1) && par1World.getBlockId(par2, par3, par4 - 1) == block;
 		returnValue[5] = !isBlockAir(par1World, par2, par3, par4 + 1) && par1World.getBlockId(par2, par3, par4 + 1) == block;
 		return returnValue;
 	}
 	
 	public boolean[] getBlockSideMap(IBlockAccess par1World, int par2, int par3, int par4){
 		boolean[] returnValue = new boolean[6];
 		returnValue[0] = !isBlockAir(par1World, par2, par3 - 1, par4);
 		returnValue[1] = !isBlockAir(par1World, par2, par3 + 1, par4);
 		returnValue[2] = !isBlockAir(par1World, par2 - 1, par3, par4);
 		returnValue[3] = !isBlockAir(par1World, par2 + 1, par3, par4);
 		returnValue[4] = !isBlockAir(par1World, par2, par3, par4 - 1);
 		returnValue[5] = !isBlockAir(par1World, par2, par3, par4 + 1);
 		return returnValue;
 	}
 	
 	public static boolean handleRotation(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer){
 		par5EntityPlayer.openGui(ModJam.instance, 0, par1World, par2, par3, par4);
 		return false;
 	}
 	
     public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random) {
     	boolean[] map = getBlockSideMap(par1World, par2, par3, par4);
     	boolean[] selfMap = getBlockSideMap(par1World, par2, par3, par4, this.blockID);
     	double zero = 0.3D;
 	    if(par1World.canBlockSeeTheSky(par2, par3, par4) && par1World.isDaytime() && par1World.getBlockMetadata(par2, par3, par4) > 0){
 	    	par1World.spawnParticle("portal", par2 + 0.5, par3 + 0.653, par4 + 0.5, zero, +5D, zero);
 	    }if(map[0] && par1World.getBlockMetadata(par2, par3, par4) > 0 || selfMap[0]){
 	   		par1World.spawnParticle("portal", par2 + 0.5, par3 + 0.653, par4 + 0.5, zero, -2D, zero);
 	    }if(map[1] && par1World.getBlockMetadata(par2, par3, par4) > 0 || selfMap[1]){
 	   		par1World.spawnParticle("portal", par2 + 0.5, par3 + 0.653, par4 + 0.5, zero, zero, zero);
 	    }if(map[2] && par1World.getBlockMetadata(par2, par3, par4) > 0 || selfMap[2]){
 	    	par1World.spawnParticle("portal", par2 + 0.5, par3 + 0.653, par4 + 0.5, -1D, -1D, zero);
 	    }if(map[3] && par1World.getBlockMetadata(par2, par3, par4) > 0 || selfMap[3]){
 	    	par1World.spawnParticle("portal", par2 + 0.5, par3 + 0.653, par4 + 0.5, +1D, -1D, zero);
 	    }if(map[4] && par1World.getBlockMetadata(par2, par3, par4) > 0 || selfMap[4]){
 	    	par1World.spawnParticle("portal", par2 + 0.5, par3 + 0.653, par4 + 0.5, zero, -1D, -1D);
 	    }if(map[5] && par1World.getBlockMetadata(par2, par3, par4) > 0 || selfMap[5]){
 	    	par1World.spawnParticle("portal", par2 + 0.5, par3 + 0.653, par4 + 0.5, zero, -1D, +1D);
 	    }
     }
 	
 	@Override
 	public int getRenderType(){
 		return ClientProxyModJam.lightGenRenderType;
 	}
 	
 	@Override
 	public void registerIcons(IconRegister par1IconRegister){
 		this.blockIcon = par1IconRegister.registerIcon("AwesomeMod:fuj1n.AwesomeMod.awesomeBlock");
 	}
 
 }
