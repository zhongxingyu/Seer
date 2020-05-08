 package spacecraft.core.block;
 
 import spacecraft.core.block.tile.TileEntityScreen;
import spacecraft.core.utility.RegistryHelper;
 import spacecraft.core.utility.RenderRegistryHelper;
 import spacecraft.core.world.SpaceManager;
 import spacecraft.core.world.TeleporterInfo;
 import spacecraft.core.world.WorldLinkInfo;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 public class BlockScreen extends BlockContainerBase {
 	private int renderId;
 	
 	public BlockScreen() {
 		super(BlockScreen.class);
 		renderId = RenderRegistryHelper.getRenderId(BlockScreen.class);
 		this.setBlockUnbreakable();
 	}
 	
 	@Override
 	public int getRenderType() {
 		return renderId;
 	}
 	
 	@Override
 	public boolean isBlockNormalCube(World world, int x, int y, int z) {
 		return false;
 	}
 	
 	@Override
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 	
 	@Override
 	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
 		return true;
 	}
 	
 	@Override
 	public boolean isOpaqueCube() {
 		return false;
 	}
 	
 	@Override
 	public boolean isBlockSolid(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
 		return false;
 	}
 	
 	@Override
     public Icon getIcon(int par1, int par2) {
 		return null;
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntityScreen();
 	}
 
 	@Override
 	protected int openGui(World par1World, TileEntity tile, EntityPlayer player) {
 		//on server side
 		TeleporterInfo info = WorldLinkInfo.forWorld(par1World)
 				.getTeleporter(tile.xCoord, tile.yCoord, tile.zCoord);
 		//if loaded
 		World worldAim = SpaceManager.getWorldForServer(info.dimension);
 		if (worldAim.blockExists(info.x, info.y, info.z)) {
 			Block.blocksList[worldAim.getBlockId(info.x, info.y, info.z)]
 					.onBlockActivated(par1World, info.x, info.y, info.z, player,
 					0, 0.0f, 0.0f, 0.0f);
 		}
 		return 0;
 	}
 
 	@Override
 	public boolean onBlockActivated(World par1World, int par2, int par3, int par4,
 			EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
 		if (par1World.isRemote) return true;
 
 		TileEntity tile = (TileEntity)par1World.getBlockTileEntity(par2, par3, par4);
 		TeleporterInfo info = WorldLinkInfo.forWorld(par1World)
 				.getTeleporter(tile.xCoord, tile.yCoord, tile.zCoord);
 		//if loaded
 		World worldAim = SpaceManager.getWorldForServer(info.dimension);
 		if (worldAim.blockExists(info.x, info.y, info.z)) {
 			Block.blocksList[worldAim.getBlockId(info.x, info.y, info.z)]
 					.onBlockActivated(par1World, info.x, info.y, info.z, par5EntityPlayer,
 					par6, par7, par8, par9);
 		}
 		return true;
 	}
 
 	@Override
 	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
 		return null;
 	}
	
	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
		if (par1World.getBlockId(par2, par3, par4) != RegistryHelper.getId(BlockMonitor.class)) {
			WorldLinkInfo.removeFromWorld(par1World, par2, par3, par4, false);
		}
	}
 }
