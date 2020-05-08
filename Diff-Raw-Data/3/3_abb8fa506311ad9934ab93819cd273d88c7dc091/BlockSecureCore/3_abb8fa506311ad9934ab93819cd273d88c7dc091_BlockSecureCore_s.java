 package fuj1n.modjam2_src.block;
 
 import java.util.Random;
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import fuj1n.modjam2_src.SecureMod;
 import fuj1n.modjam2_src.client.gui.GuiHandler.GuiIdReference;
 import fuj1n.modjam2_src.client.particle.EntityElectricityFX;
 import fuj1n.modjam2_src.item.SecureModItems;
 import fuj1n.modjam2_src.tileentity.TileEntitySecurityCore;
 
 public class BlockSecureCore extends BlockContainer implements ISecure {
 
 	private Icon[] icons = new Icon[3];
 
 	public BlockSecureCore(int par1) {
 		super(par1, Material.tnt);
 		this.setStepSound(this.soundMetalFootstep);
 	}
 
 	@Override
 	public boolean isOpaqueCube() {
 		return true;
 		//return false;
 	}
 
 	@Override
 	public boolean canProvidePower() {
 		return true;
 	}
 
 	@Override
 	public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
 		TileEntitySecurityCore te = (TileEntitySecurityCore)par1IBlockAccess.getBlockTileEntity(par2, par3, par4);
 		return te.redstoneSignalsOut[par5] > 0 ? te.redstoneSignalsOut[par5] : te.redstoneSignalsRet[par5];
 	}
 
 	@Override
 	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
 		return true;
 	}
 
 	@Override
 	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
 		TileEntitySecurityCore te = (TileEntitySecurityCore) par1World.getBlockTileEntity(par2, par3, par4);
 
 		switch (te.inputMode) {
 		case 1:
 			par5EntityPlayer.openGui(SecureMod.instance, GuiIdReference.GUI_SECURECOREPASS, par1World, par2, par3, par4);
 			return true;
 		case 2:
 			if (par5EntityPlayer.getHeldItem() != null && par5EntityPlayer.getHeldItem().itemID == SecureModItems.securityPass.itemID && par5EntityPlayer.getHeldItem().getTagCompound() != null) {
 				if (Integer.toString(par5EntityPlayer.getHeldItem().getTagCompound().getInteger("cardID")).equals(te.passcode)) {
 					te.setOutput();
 				}
				return true;
 			}
 			break;
 		case 3:
 			if (par5EntityPlayer.username.equals(te.playerName)) {
 				te.setOutput();
 				return true;
 			}
 			break;
 		case 4:
 			break;
 		}
 
 		te.setRetaliate(par5EntityPlayer);
 		return false;
 	}
 	
 	@Override
     public void onBlockClicked(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
 		TileEntitySecurityCore te = (TileEntitySecurityCore) par1World.getBlockTileEntity(par2, par3, par4);
 		
 		te.setRetaliate(par5EntityPlayer);
 	}
 	
 	@Override
 	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
 		int l = MathHelper.floor_double((double) (par5EntityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
 
 		if (l == 0) {
 			par1World.setBlockMetadataWithNotify(par2, par3, par4, 2, 2);
 		}
 
 		if (l == 1) {
 			par1World.setBlockMetadataWithNotify(par2, par3, par4, 5, 2);
 		}
 
 		if (l == 2) {
 			par1World.setBlockMetadataWithNotify(par2, par3, par4, 3, 2);
 		}
 
 		if (l == 3) {
 			par1World.setBlockMetadataWithNotify(par2, par3, par4, 4, 2);
 		}
 
 		if (par5EntityLivingBase instanceof EntityPlayer) {
 			EntityPlayer player = (EntityPlayer) par5EntityLivingBase;
 			TileEntitySecurityCore te = (TileEntitySecurityCore)par1World.getBlockTileEntity(par2, par3, par4);
 			te.playerName = player.username;
 			player.openGui(SecureMod.instance, GuiIdReference.GUI_SECURECORE, par1World, par2, par3, par4);
 		}
 	}
 
 	@Override
 	public Icon getBlockTexture(IBlockAccess par1World, int par2, int par3, int par4, int par5) {
 		int meta = par1World.getBlockMetadata(par2, par3, par4);
 		return par5 == 1 ? this.icons[1] : (par5 == 0 ? this.icons[1] : (par5 != meta ? this.icons[0] : this.icons[2]));
 	}
 
 	@Override
 	public Icon getIcon(int par1, int par2) {
 		if (par1 == 0 || par1 == 1) {
 			return icons[1];
 		} else if (par1 == 4) {
 			return icons[2];
 		}
 
 		return icons[0];
 	}
 
 	@Override
     public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
 		TileEntitySecurityCore te = (TileEntitySecurityCore)par1World.getBlockTileEntity(par2, par3, par4);
 		if(te.inputMode == 4 && par1World.isBlockIndirectlyGettingPowered(par2, par3, par4)){
 			te.setOutput();
 		}
 	}
 	
 	@Override
 	public void registerIcons(IconRegister par1IconRegister) {
 		icons[0] = par1IconRegister.registerIcon("securemod:secure_block_base");
 		icons[1] = par1IconRegister.registerIcon("securemod:secure_block_axisY");
 		icons[2] = par1IconRegister.registerIcon("securemod:secure_core_front");
 	}
 	
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntitySecurityCore();
 	}
 
 	@Override
 	public boolean canBreak(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
 		TileEntitySecurityCore te = (TileEntitySecurityCore)par1World.getBlockTileEntity(par2, par3, par4);
 		if(par5EntityPlayer.username.equals(te.playerName) || te.playerName == null){
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean canEntityDestroy(World world, int x, int y, int z, Entity entity) {
 		return false;
 	}
 	
 	@Override
 	public int getMobilityFlag(){
 		return 2;
 	}
 	
 //	@SideOnly(Side.CLIENT)
 //	@Override
 //    public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random){
 //
 //	}
 	
 }
