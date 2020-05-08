 package gcewing.lighting;
 
 import java.util.ArrayList;

import cpw.mods.fml.common.FMLLog;
 import net.minecraft.block.*;
 import net.minecraft.block.material.*;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.tileentity.*;
 import net.minecraft.util.Icon;
 import net.minecraft.world.*;
 import net.minecraftforge.common.*;
 
 public class BlockFloodlight extends BlockContainer implements BaseIRenderType {
 
 	static int renderType;
 	
 	private String mIconBaseName;
 	
 	private Icon[] mFrontIcons = new Icon[2];
 	private Icon[] mSideIcons  = new Icon[2];
 	private Icon[] mRearIcons = new Icon[2];
 
 	public BlockFloodlight(int id) {
 		this(id, "spotlight", Material.rock);
 	}
 	
 	public BlockFloodlight(int id, String iconBase, Material mat) {
 		super(id, mat);
 		mIconBaseName = iconBase;
 	}
 	
 	@Override
 	public int getRenderType() {
 		return renderType;
 	}
 	
 	@Override
 	public void setRenderType(int id) {
 		renderType = id;
 	}
 
 	@Override
 	public Icon getIcon(int side, int metadata) {
 		int lightActive = 0;
 		
 		if ((metadata & 0x01) != 0)
 			lightActive = 1;
 		
 		int frontSide = 1;
 		int rearSide = 0;
 		
 		if ((metadata & 0x04) != 0)
 		{
 			frontSide = 4;
 			rearSide = 5;
 		} else if ((metadata & 0x08) != 0)
 		{
 			frontSide = 2;
 			rearSide = 3;
 		}
 		
 		if ((metadata & 0x02) != 0)
 		{
 			int tmp = frontSide;
 			frontSide = rearSide;
 			rearSide = tmp;
 		}
 		
 		if (side == frontSide)
 		{
 			return mFrontIcons[lightActive];
 		} else if (side == rearSide)
 		{
 			return mRearIcons[lightActive];
 		} else
 		{
 			return mSideIcons[lightActive];
 		}
 	}
 	
     /**
      * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z, side, hitX, hitY, hitZ, block metadata
      */
 	@Override
     public int onBlockPlaced(World par1World, int par2, int par3, int par4, int par5, float par6, float par7, float par8, int par9)
     {
         switch (par5)
         {
             case 0:
             	return 2;
             case 1:
                 return 0;
             case 2:
             	return 8;
             case 3:
                 return 10;
             case 4:
             	return 4;
             case 5:
                 return 6;
             default:
             	return 2;
         }
     }
 	
 	@Override
 	public void onBlockAdded(World world, int x, int y, int z) {
 		update(world, x, y, z);
 		updateBeam(world, x, y, z);
 	}
 
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int id, int metadata) {
 		ForgeDirection dir = direction(metadata);
 		Floodlight.propagateBeam(world, x, y, z, dir, 0, 1);
 		super.breakBlock(world, x, y, z, id, metadata);
 	}
 
 	@Override
 	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
		try
		{
			update(world, x, y, z);
			updateBeam(world, x, y, z);
		} catch (Exception ex)
		{
			FMLLog.severe("Exception while updating floodlight from neighbor.");
			ex.printStackTrace();
		}
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return null;
 	}
 	
 	public void update(World world, int x, int y, int z) {
 		if (!world.isRemote) {
 			boolean active = isActive(world, x, y, z);
 			setIlluminated(world, x, y, z, active);
 		}
 	}
 
 	public void updateBeam(World world, int x, int y, int z) {
 		if (!world.isRemote) {
 			ForgeDirection dir = getDirection(world, x, y, z);
 			Floodlight.updateBeamInDirection(world,
 				x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir);
 		}
 	}
 	
 	public void setIlluminated(World world, int x, int y, int z, boolean state) {
 		//System.out.printf("BlockFloodlight.setIlluminated: %s\n", state);
 		int oldData = world.getBlockMetadata(x, y, z);
 		int newData = (oldData & 0xe) | (state ? 1 : 0);
 		if (oldData != newData) {
 			world.setBlockMetadataWithNotify(x, y, z, newData, 3);
 			world.markBlockForUpdate(x, y, z);
 		}
 	}
 
 	public boolean isActive(World world, int x, int y, int z) {
 		return world.isBlockIndirectlyGettingPowered(x, y, z);
 	}
 	
 	public boolean isActiveInDirection(World world, int x, int y, int z, ForgeDirection dir) {
 		return getDirection(world, x, y, z) == dir && isActive(world, x, y, z);
 	}
 	
 	public ForgeDirection getDirection(World world, int x, int y, int z) {
 		int data = world.getBlockMetadata(x, y, z);
 		return direction(data);
 	}
 	
 	public ForgeDirection direction(int metadata) {
 		
 		metadata = (metadata & 0x0E);
 		switch (metadata)
 		{
 		case 0:
 			return ForgeDirection.UP;
 		case 2:
 			return ForgeDirection.DOWN;
 		case 4:
 			return ForgeDirection.WEST;
 		case 6:
 			return ForgeDirection.EAST;
 		case 8:
 			return ForgeDirection.NORTH;
 		case 10:
 			return ForgeDirection.SOUTH;
 		default:
 			return ForgeDirection.UP;
 		}
 	}
 	
 	@Override
 	public void registerIcons(IconRegister icon)
 	{
 		mFrontIcons[0] = icon.registerIcon("gregslighting/spotlightOff.front");
 		mFrontIcons[1] = icon.registerIcon("gregslighting/spotlightOn.front");
 		mSideIcons[0] = icon.registerIcon("gregslighting/spotlightOff.side");
 		mSideIcons[1] = icon.registerIcon("gregslighting/spotlightOn.side");
 		mRearIcons[0] = icon.registerIcon("gregslighting/spotlightOff.rear");
 		mRearIcons[1] = icon.registerIcon("gregslighting/spotlightOn.rear");
 	}
 }
