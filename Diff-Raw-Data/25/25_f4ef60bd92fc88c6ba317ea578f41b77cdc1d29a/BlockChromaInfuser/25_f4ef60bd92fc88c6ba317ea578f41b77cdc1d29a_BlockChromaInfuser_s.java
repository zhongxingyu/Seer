 package com.qzx.au.extras;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.ITileEntityProvider;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 import java.util.List;
 import java.util.Random;
 
 import com.qzx.au.core.BlockCoord;
 import com.qzx.au.core.TileEntityAU;
 
 public class BlockChromaInfuser extends Block implements ITileEntityProvider {
 	@SideOnly(Side.CLIENT)
 	private Icon blockIcon_inner;
 	@SideOnly(Side.CLIENT)
 	private Icon blockIcon_top;
 	@SideOnly(Side.CLIENT)
 	private Icon blockIcon_top_item; // non-transparent top icon for item rendering
 	@SideOnly(Side.CLIENT)
 	private Icon blockIcon_bottom;
 	@SideOnly(Side.CLIENT)
 	private Icon blockIcon_water;
 
 	public BlockChromaInfuser(int id, String name, String readableName){
 		super(id, Material.iron);
 		this.setUnlocalizedName(name);
 		GameRegistry.registerBlock(this, name);
 		LanguageRegistry.addName(this, readableName);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister){
 		this.blockIcon_inner = iconRegister.registerIcon("au_extras:chromaInfuser_inner");
 		this.blockIcon_top = iconRegister.registerIcon("au_extras:chromaInfuser_top");
 		this.blockIcon_top_item = iconRegister.registerIcon("au_extras:chromaInfuser_top_item");
 		this.blockIcon_bottom = iconRegister.registerIcon("au_extras:chromaInfuser_bottom");
 		this.blockIcon = iconRegister.registerIcon("au_extras:chromaInfuser_side");
 		this.blockIcon_water = iconRegister.registerIcon("au_extras:chromaInfuser_water");
 	}
 
 	// called be renderer
 	@SideOnly(Side.CLIENT)
 	public static Icon getIconByName(String s){
 		return s == "inner" ? ((BlockChromaInfuser)AUExtras.blockChromaInfuser).blockIcon_inner
 			: (s == "bottom" ? ((BlockChromaInfuser)AUExtras.blockChromaInfuser).blockIcon_bottom : null);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int metadata){
 		return side == 1 ? this.blockIcon_top_item : (side == 0 ? this.blockIcon_bottom : this.blockIcon);
 	}
 
 	@SideOnly(Side.CLIENT)
 	public static Icon getWaterIcon(){
 		return ((BlockChromaInfuser)AUExtras.blockChromaInfuser).blockIcon_water;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getBlockTexture(IBlockAccess access, int x, int y, int z, int side){
 		return side == 1 ? this.blockIcon_top : (side == 0 ? this.blockIcon_bottom : this.blockIcon);
 	}
 
 	//////////
 
 	public TileEntity createNewTileEntity(World world){
 		return null;
 	}
 	@Override
 	public TileEntity createTileEntity(World world, int metadata){
 		return new TileEntityChromaInfuser();
 	}
 
 	//////////
 
 	@Override
 	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisAlignedBB, List list, Entity entity){
 		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 5*BlockCoord.ADD_1_16, 1.0F);
 		super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 		this.setBlockBounds(0.0F, 0.0F, 0.0F, BlockCoord.ADD_1_8, 1.0F, 1.0F);
 		super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, BlockCoord.ADD_1_8);
 		super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 		this.setBlockBounds(BlockCoord.SUB_1_8, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
 		super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 		this.setBlockBounds(0.0F, 0.0F, BlockCoord.SUB_1_8, 1.0F, 1.0F, 1.0F);
 		super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 		this.setBlockBoundsForItemRender();
 	}
 
 	@Override
 	public void setBlockBoundsForItemRender(){
 		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
 	}
 
 	@Override
 	public boolean isOpaqueCube(){
 		return false;
 	}
 
 	@Override
 	public boolean renderAsNormalBlock(){
 		return false;
 	}
 
 	@Override
 	public int getRenderType(){
 		return ClientProxy.infuserRenderType;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public int getRenderBlockPass(){
 		return 1;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean canRenderInPass(int pass){
 		ClientProxy.renderPass = pass;
 		return true;
 	}
 
 	@Override
 	public boolean canBeReplacedByLeaves(World world, int x, int y, int z){
 		return false;
 	}
 
 	//////////
 
 	@Override
 	public boolean onBlockEventReceived(World world, int x, int y, int z, int eventID, int value){
 		TileEntity tileEntity = (TileEntity)world.getBlockTileEntity(x, y, z);
 		if(tileEntity == null) return false;
 		if(!tileEntity.receiveClientEvent(eventID, value)) return false;
 		world.markBlockForUpdate(x, y, z);
 		return true;
 	}
 
 	@Override
 	public void fillWithRain(World world, int x, int y, int z){
 		if(!world.isRemote){
 			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 			if(tileEntity instanceof TileEntityChromaInfuser)
 				if(Cfg.rainResetsChromaInfuser || !((TileEntityChromaInfuser)tileEntity).getWater())
 					((TileEntityChromaInfuser)tileEntity).resetWater();
 		}
 	}
 
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ){
 		if(world.isRemote) return true;
 
 		player.openGui(AUExtras.instance, Guis.TILE_GUI, world, x, y, z);
 		return true;
 	}
 
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int side, int metadata){
 		// don't double break block, only drop contents on server
 		if(!world.isRemote){
 			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 			if(tileEntity != null)
 				((TileEntityAU)tileEntity).dropContents(world, x, y, z);
 		}
 		world.removeBlockTileEntity(x, y, z);
 	}
 }
