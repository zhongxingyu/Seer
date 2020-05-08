 package fuj1n.modjam2_src.block;
 
 import java.util.Random;
 
 import net.minecraft.block.BlockDoor;
 import net.minecraft.block.ITileEntityProvider;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.IconFlipped;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import fuj1n.modjam2_src.item.SecureModItems;
 import fuj1n.modjam2_src.tileentity.TileEntitySecureBlock;
 
 public class BlockSecureDoor extends BlockDoor implements ITileEntityProvider, ISecure {
 
 	private Icon[] field_111044_a;
 	private Icon[] field_111043_b;
 
 	protected BlockSecureDoor(int par1) {
 		super(par1, Material.tnt);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntitySecureBlock();
 	}
 
 	@Override
 	public boolean canBreak(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
 		TileEntitySecureBlock te = (TileEntitySecureBlock) par1World.getBlockTileEntity(par2, par3, par4);
 		if (par5EntityPlayer.username.equals(te.playerPlaced) || te.playerPlaced == null) {
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
 	public Icon getIcon(int par1, int par2) {
 		return this.field_111043_b[0];
 	}
 
 	@Override
 	public Icon getBlockTexture(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
 		if (par5 != 1 && par5 != 0) {
 			int i1 = this.getFullMetadata(par1IBlockAccess, par2, par3, par4);
 			int j1 = i1 & 3;
 			boolean flag = (i1 & 4) != 0;
 			boolean flag1 = false;
 			boolean flag2 = (i1 & 8) != 0;
 
 			if (flag) {
 				if (j1 == 0 && par5 == 2) {
 					flag1 = !flag1;
 				} else if (j1 == 1 && par5 == 5) {
 					flag1 = !flag1;
 				} else if (j1 == 2 && par5 == 3) {
 					flag1 = !flag1;
 				} else if (j1 == 3 && par5 == 4) {
 					flag1 = !flag1;
 				}
 			} else {
 				if (j1 == 0 && par5 == 5) {
 					flag1 = !flag1;
 				} else if (j1 == 1 && par5 == 3) {
 					flag1 = !flag1;
 				} else if (j1 == 2 && par5 == 4) {
 					flag1 = !flag1;
 				} else if (j1 == 3 && par5 == 2) {
 					flag1 = !flag1;
 				}
 
 				if ((i1 & 16) != 0) {
 					flag1 = !flag1;
 				}
 			}
 
 			return flag2 ? this.field_111044_a[flag1 ? 1 : 0] : this.field_111043_b[flag1 ? 1 : 0];
 		} else {
 			return this.field_111043_b[0];
 		}
 	}
 
 	@Override
 	public void registerIcons(IconRegister par1IconRegister) {
 		this.field_111044_a = new Icon[2];
 		this.field_111043_b = new Icon[2];
 		this.field_111044_a[0] = par1IconRegister.registerIcon("securemod:secure_door_upper");
 		this.field_111043_b[0] = par1IconRegister.registerIcon("securemod:secure_door_lower");
 		this.field_111044_a[1] = new IconFlipped(this.field_111044_a[0], true, false);
 		this.field_111043_b[1] = new IconFlipped(this.field_111043_b[0], true, false);
 	}
 
 	@Override
 	public boolean canEntityDestroy(World world, int x, int y, int z, Entity entity) {
 		return false;
 	}
 
 	@Override
 	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
 		return false;
 	}
 
 	@Override
 	public int idDropped(int par1, Random par2Random, int par3) {
		return SecureModItems.secureDoor.itemID;
 	}
 	
 	@Override
 	public int getMobilityFlag() {
 		return 2;
 	}
 
 }
