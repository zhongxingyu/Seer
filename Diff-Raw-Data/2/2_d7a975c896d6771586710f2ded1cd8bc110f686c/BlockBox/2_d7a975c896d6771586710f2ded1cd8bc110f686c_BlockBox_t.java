 package ml.boxes.block;
 
 import java.util.ArrayList;
 
 import ml.boxes.Boxes;
 import ml.boxes.TileEntityBox;
 import ml.boxes.data.ItemIBox;
 import ml.boxes.item.ItemBox;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 public class BlockBox extends BlockContainer {
 
 	public BlockBox(int par1) {
		super(par1, Material.rock);
 		setBlockBounds(0.0625F, 0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
 		setRequiresSelfNotify();
 		setCreativeTab(Boxes.BoxTab);
 	}
 
 	@Override
 	public TileEntity createTileEntity(World world, int metadata) {
 		return new TileEntityBox();
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World var1) {
 		return null;
 	}
 
 	@Override
 	public int getRenderType() {
 		return Boxes.boxRendererID;
 	}
 
 	@Override
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 	
 	@Override
 	public boolean isOpaqueCube() {
 		return false;
 	}
 
 	@Override
 	public int getBlockTextureFromSideAndMetadata(int par1, int par2) {
 		return 64;
 	}
 
 	@Override
 	public String getTextureFile() {
 		return "/ml/boxes/res/sprites.png";
 	}
 
 	@Override
 	public boolean onBlockActivated(World par1World, int x, int y,
 			int z, EntityPlayer player, int par6, float par7,
 			float par8, float par9) {
 		
 		TileEntity te = par1World.getBlockTileEntity(x, y, z);
 		if (te == null || !(te instanceof TileEntityBox))
 			return true;
 		
 		if (par1World.isBlockSolidOnSide(x, y+1, z, ForgeDirection.DOWN))
 			return true;
 		
 		if (par1World.isRemote)
 			return true;
 		
 		player.openGui(Boxes.instance, 1, par1World, x, y, z);
 		return true;
 	}
 
 	@Override
 	public void onBlockPlacedBy(World world, int x, int y, int z,
 			EntityLiving entity) {
 	
 		int rot = Math.round((entity.rotationYaw*4F)/360F);
 		TileEntity te = world.getBlockTileEntity(x, y, z);
 		if (te instanceof TileEntityBox){
 			((TileEntityBox) te).facing = 4-rot;
 			world.markBlockForUpdate(x, y, z);
 		}
 	}
 	
 	@Override
 	public void harvestBlock(World par1World, EntityPlayer par2EntityPlayer, int par3, int par4, int par5, int par6) {}
 
 	@Override
 	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y,
 			int z, int metadata, int fortune) {
 		
 		ArrayList<ItemStack> iss = new ArrayList<ItemStack>();
 		ItemStack is = new ItemStack(world.getBlockId(x, y, z), 1, metadata);
 		TileEntity te = world.getBlockTileEntity(x, y, z);
 		
 		if (te instanceof TileEntityBox){
 			ItemBox.saveBoxData(is, ((TileEntityBox)te).getBoxData());
 		}
 		iss.add(is);
 		return iss;		
 	}
 
 	@Override
 	public boolean removeBlockByPlayer(World world, EntityPlayer player, int x,
 			int y, int z) {
 		if (world.isRemote)
 			return true;
 		
 		int meta = world.getBlockMetadata(x, y, z);
 		for (ItemStack is : getBlockDropped(world, x, y, z, meta, 0)){
 			super.dropBlockAsItem_do(world, x, y, z, is);
 		}
 		world.setBlockWithNotify(x, y, z, 0);
 		return true;
 	}
 	
 }
