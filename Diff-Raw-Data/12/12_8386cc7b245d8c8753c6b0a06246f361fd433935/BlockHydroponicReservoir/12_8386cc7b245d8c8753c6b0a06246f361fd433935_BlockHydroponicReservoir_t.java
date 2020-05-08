 package sct.culinarycraft.block;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.IPlantable;
 import sct.culinarycraft.CulinaryCraft;
 import sct.culinarycraft.tile.TileEntityHydroponicReservoir;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockHydroponicReservoir extends BlockMachine {
 	
 	private Icon[] iconsIdle = new Icon[6];
 	private Icon[] iconsActive = new Icon[6];
 
 	public BlockHydroponicReservoir(int id) {
 		super(id);
 		setTickRandomly(true);
 		setUnlocalizedName("culinary.machine.reservoir");
 	}
 	
 	@Override
 	public void updateTick(World world, int x, int y, int z,
 			Random rand) {
 		super.updateTick(world, x, y, z, rand);
 		
 		int meta = world.getBlockMetadata(x, y, z);
 		if (meta == 1) {
 			TileEntity te = world.getBlockTileEntity(x, y, z);
 			if (te != null && te instanceof TileEntityHydroponicReservoir) {
 				for (int i = 0; i < ((TileEntityHydroponicReservoir) te).getGrowthChances(); i++) {
 					Block plant = Block.blocksList[world.getBlockId(x, y + 1, z)];
 					if (plant instanceof IPlantable) {
 						plant.updateTick(world, x, y + 1, z, rand);
 					}
 				}
 			}
 		}
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister ir) {
		iconsIdle[0] = ir.registerIcon("tile.sct.resevoir.idle.bottom");
 		iconsIdle[1] = ir.registerIcon("tile.sct.resevoir.idle.top");
 		iconsIdle[2] = ir.registerIcon("tile.sct.resevoir.idle.side");
 		iconsIdle[3] = ir.registerIcon("tile.sct.resevoir.idle.side");
 		iconsIdle[4] = ir.registerIcon("tile.sct.resevoir.idle.side");
 		iconsIdle[5] = ir.registerIcon("tile.sct.resevoir.idle.side");
 
		iconsActive[0] = ir.registerIcon("tile.sct.resevoir.active.bottom");
 		iconsActive[1] = ir.registerIcon("tile.sct.resevoir.active.top");
 		iconsActive[2] = ir.registerIcon("tile.sct.resevoir.active.side");
 		iconsActive[3] = ir.registerIcon("tile.sct.resevoir.active.side");
 		iconsActive[4] = ir.registerIcon("tile.sct.resevoir.active.side");
 		iconsActive[5] = ir.registerIcon("tile.sct.resevoir.active.side");
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int par1, int par2) {
 		if (par2 == 1)
 			return iconsActive[par1];
 		return iconsIdle[par1];
 	}
 	
 	@Override
 	public void onBlockAdded(World world, int x, int y, int z) {
 		TileEntity te = world.getBlockTileEntity(x, y, z);
 		if (te != null && te instanceof TileEntityHydroponicReservoir) {
 			((TileEntityHydroponicReservoir) te).findDistributor();
 		}
 	}
 	
 	@Override
 	public void onNeighborBlockChange(World world, int x, int y,
 			int z, int blockid) {
 		if (blockid == CulinaryCraft.reservoir.blockID) {
 			TileEntity te = world.getBlockTileEntity(x, y, z);
 			if (te != null && te instanceof TileEntityHydroponicReservoir) {
 				((TileEntityHydroponicReservoir) te).verifyNetwork();
 			}
 		}
 	}
 	
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z,
 			EntityPlayer player, int par6, float par7, float par8, float par9) {
 		return false;
 	}
 	
 	@Override
 	public boolean canSustainPlant(World world, int x, int y, int z,
 			ForgeDirection direction, IPlantable plant) {
 		return true;
 	}
 	
 	@Override
 	public boolean isFertile(World world, int x, int y, int z) {
 		return world.getBlockMetadata(x, y, z) == 1;
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntityHydroponicReservoir();
 	}
 
 }
