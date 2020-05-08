 package fuj1n.modjam2_src.block;
 
 import java.util.List;
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import fuj1n.modjam2_src.tileentity.TileEntitySecureBlock;
 
 public class BlockSecure extends BlockContainer implements ISecure {
 
 	protected BlockSecure(int par1) {
 		super(par1, Material.tnt);
 		setStepSound(soundMetalFootstep);
 	}
 
 	@Override
 	public boolean canProvidePower() {
 		return false;
 	}
 
 	@Override
 	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
 		return world.getBlockMetadata(x, y, z) != 1;
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntitySecureBlock();
 	}
 
 	@Override
 	public boolean canBreak(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
 		TileEntitySecureBlock te = (TileEntitySecureBlock) par1World.getBlockTileEntity(par2, par3, par4);
		if (par5EntityPlayer.username.equals(te.playerPlaced) || te.playerPlaced == null || te.playerPlaced.equals("")) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
 		super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLivingBase, par6ItemStack);
 		if (par5EntityLivingBase instanceof EntityPlayer) {
 			EntityPlayer player = (EntityPlayer) par5EntityLivingBase;
 			TileEntitySecureBlock te = (TileEntitySecureBlock) par1World.getBlockTileEntity(par2, par3, par4);
 			te.playerPlaced = player.username;
 		}
 	}
 
 	@Override
 	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List) {
 		par3List.add(new ItemStack(par1, 1, 0));
 		par3List.add(new ItemStack(par1, 1, 1));
 	}
 
 	@Override
 	public void registerIcons(IconRegister par1IconRegister) {
 		this.blockIcon = par1IconRegister.registerIcon("securemod:secure_block_standard");
 	}
 
 	@Override
 	public boolean canEntityDestroy(World world, int x, int y, int z, Entity entity) {
 		return false;
 	}
 	
 	@Override
 	public int damageDropped(int par1){
 		return par1;
 	}
 
 	@Override
 	public int getMobilityFlag() {
 		return 2;
 	}
 }
