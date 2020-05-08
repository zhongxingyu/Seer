 package spacecraft.core.block;
 
 import java.util.Random;
 
 import spacecraft.core.block.tile.TileEntityTeleporter;
 import spacecraft.core.utility.RegistryHelper;
 import spacecraft.core.utility.WorldSavedDataSC;
 import spacecraft.core.world.TeleportManager;
 import spacecraft.core.world.TeleporterInfo;
 import spacecraft.core.world.WorldLinkInfo;
 import tutorial.TeleporterTutorial;
 import tutorial.Tutorial;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockPortal;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 public class BlockPortalSC extends BlockPortal {
 
 	public BlockPortalSC() {
 		super(RegistryHelper.getId(BlockPortalSC.class));
 	}
 
 	@Override
 	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
 		//Do nothing
 	}
 	
 	@Override
 	public boolean tryToCreatePortal(World par1World, int par2, int par3, int par4) {
 		return false;
 	}
 	
 	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
 		return true;
 	}
 
 	@Override
 	public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity par5Entity) {
 		if (par5Entity.ridingEntity == null && par5Entity.riddenByEntity == null) {
 			if (par5Entity instanceof EntityPlayerMP) {
 				//TODO count down before teleported
 				TeleportManager.teleport((EntityPlayerMP) par5Entity, par1World, par2, par3, par4);
 			}
 		}
 	}
 	
 	//XTODO test if this method is always called when a portal is destroyed. 
 	//If not, there must be a step to check useless info when loading a world
 	//TODO event when tile entity is removed?
 	@Override
 	public void onBlockDestroyedByPlayer(World par1World, int par2, int par3, int par4, int par5) {
 		super.onBlockDestroyedByPlayer(par1World, par2, par3, par4, par5);
 		WorldLinkInfo.removeFromWorld(par1World, par2, par3, par4, true);
 	}
 }
