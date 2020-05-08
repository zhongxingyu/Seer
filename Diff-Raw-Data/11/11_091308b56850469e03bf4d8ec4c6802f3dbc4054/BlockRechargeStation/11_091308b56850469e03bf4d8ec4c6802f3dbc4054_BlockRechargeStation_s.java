 package demonmodders.Crymod.Common.Blocks;
 
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 import demonmodders.Crymod.Common.Crymod;
 import demonmodders.Crymod.Common.Gui.GuiType;
 import demonmodders.Crymod.Common.TileEntities.TileEntityRechargeStation;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.IBlockAccess;
 import net.minecraft.src.Material;
 import net.minecraft.src.MathHelper;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 
 public class BlockRechargeStation extends BlockCryMod {
 
 	private static final int TOP_BOTTOM_TEXTURE = 14 * 16 + 13;
 	private static final int FRONT_TEXTURE = 14 * 16 + 15;
 	private static final int SIDES_TEXTURE = 15 * 16 + 15;
 	
 	public BlockRechargeStation(int blockId) {
		super(blockId, 1, Material.iron);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public int getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side) {
 		if (side < 2) {
 			return TOP_BOTTOM_TEXTURE;
 		}
 		int meta = blockAccess.getBlockMetadata(x, y, z);
 		return meta == side ? FRONT_TEXTURE : SIDES_TEXTURE;
 	}
 
 	@Override
 	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving living) {
 		int direction = MathHelper.floor_double((living.rotationYaw * 4 / 360) + 0.5) & 3;
 		int meta = 0;
 		switch (direction) {
 		case 0: // player is facing south
 			meta = 2;
 			break;
 		case 1: // player is facing west
 			meta = 5;
 			break;
 		case 2: // player is facing north
 			meta = 3;
 			break;
 		case 3: // player is facing east
 			meta = 4;
 			break;
 		}
 		world.setBlockMetadataWithNotify(x, y, z, meta);
 	}
 
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9) {
 		if (!player.isSneaking()) {
 			player.openGui(Crymod.instance, GuiType.RECHARGE_STATION.getGuiId(), world, x, y, z);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public boolean hasTileEntity(int metadata) {
 		return true;
 	}
 
 	@Override
 	public TileEntity createTileEntity(World world, int metadata) {
 		return new TileEntityRechargeStation();
 	}
 }
