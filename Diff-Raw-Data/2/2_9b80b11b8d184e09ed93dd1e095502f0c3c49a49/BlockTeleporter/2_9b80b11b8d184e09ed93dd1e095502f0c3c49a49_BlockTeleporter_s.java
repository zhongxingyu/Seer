 package adanaran.mods.ts.blocks;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.item.EntityMinecart;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.ChunkCoordinates;
 import net.minecraft.world.World;
 import adanaran.mods.ts.TeleportStations;
 import adanaran.mods.ts.entities.TileEntityTeleporter;
 
 /**
  * Block for teleportation.
  * <p>
  * Can teleport the player and can also act as target. 
  * 
  * @author Demitreus
  */
 public class BlockTeleporter extends BlockTeleTarget {
 
 	public BlockTeleporter(int par1) {
 		super(par1);
 	}
 
 	@Override
 	protected int update(World world, int i, int j, int k) {
 		if (power(world, i, j, k)) {
 			world.setBlockWithNotify(i, j, k, 3003);
 		} else {
 			world.setBlockWithNotify(i, j, k, 3002);
 		}
 		int meta = super.update(world, i, j, k);
 		return meta;
 	}
 
 	private boolean power(World world, int i, int j, int k) {
 		return (world.isBlockGettingPowered(i, j, k) || world
 				.isBlockIndirectlyGettingPowered(i, j, k));
 	}
 
 	@Override
 	public int getBlockTextureFromSideAndMetadata(int par1, int par2) {
 		return par1 == 1 ? this.blockID == 3002 ? super
 				.getBlockTextureFromSideAndMetadata(par1, par2) : (super
 				.getBlockTextureFromSideAndMetadata(par1, par2) + 16) : 32;
 	}
 
 	@Override
 	public void onNeighborBlockChange(World par1World, int par2, int par3,
 			int par4, int par5) {
 		super.onNeighborBlockChange(par1World, par2, par3, par4, par5);
 		update(par1World, par2, par3, par4);
 	}
 
 	@Override
 	public boolean onBlockActivated(World par1World, int par2, int par3,
 			int par4, EntityPlayer par5EntityPlayer, int par6, float par7,
 			float par8, float par9) {
 		par5EntityPlayer.openGui(TeleportStations.instance, 1, par1World, par2,
 				par3, par4);
 		return true;
 	}
 
 	@Override
 	public void onEntityCollidedWithBlock(World world, int i, int j, int k,
 			Entity entity) {
 		int meta = world.getBlockMetadata(i, j, k);
 		if (power(world, i, j, k)) {
 			if (meta == 0 && entity instanceof EntityPlayer) {
 				teleportPlayer((EntityPlayer) entity, world, i, j, k);
 			} else if (meta > 0 && entity instanceof EntityMinecart) {
 				handleMC(world, (EntityMinecart) entity, i, j, k);
 			}
 		}
 	}
 
 	private void teleportPlayer(EntityPlayer entity, World world, int i, int j,
 			int k) {
 		TileEntity te = world.getBlockTileEntity(i, j + 2, k);
 		if (te != null
 				&& te instanceof TileEntityTeleporter
 				&& (TeleportStations.proxy.isServer() || TeleportStations.proxy
 						.isSinglePlayer())) {
 			TileEntityTeleporter tet = (TileEntityTeleporter) te;
 			if (tet.getTarget() != null) {
 				tet.tp(entity);
 			}
 		}
 	}
 }
