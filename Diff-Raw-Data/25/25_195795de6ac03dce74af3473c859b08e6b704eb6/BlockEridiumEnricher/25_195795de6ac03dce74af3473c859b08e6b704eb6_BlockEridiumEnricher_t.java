 package com.isocraft.block.eridiumnet;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IIconRegister;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.IIcon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.util.ForgeDirection;
 
 import com.isocraft.ISOCraft;
 import com.isocraft.api.eridiumnet.ISidedConnectable;
 import com.isocraft.api.eridiumnet.SidedConnectableHelper;
 import com.isocraft.block.ISOCraftBlockMachine;
 import com.isocraft.lib.Reference;
 import com.isocraft.tileentity.eridiumnet.TileEridiumEnricher;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockEridiumEnricher extends ISOCraftBlockMachine{
 
 	public ForgeDirection side = null;
 
 	public BlockEridiumEnricher() {
 		super(Material.iron, false);
 		this.setCreativeTab(ISOCraft.tabsISO);
 		this.setHardness(3.0F);
 		this.setResistance(5.0F);
 		this.setStepSound(Block.soundTypeMetal);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World var1, int meta) {
 		TileEridiumEnricher tile = new TileEridiumEnricher();
 		return tile;
 	}
 
 	@Override
 	public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		boolean flag = false;
		
		if (player.isSneaking()){
			flag = SidedConnectableHelper.tryRemoveConnectableSide(player, (ISidedConnectable)par1World.getTileEntity(x, y, z), par6);
		}
		else{
			flag = SidedConnectableHelper.tryAddConnectableSide(player, (ISidedConnectable)par1World.getTileEntity(x, y, z), par6);	
		}
		
		if (flag){	
			TileEridiumEnricher tile = (TileEridiumEnricher) par1World.getTileEntity(x, y, z);
			tile.updateSide();
			par1World.markBlockForUpdate(x, y, z);
		}
 		return true;	
 	}
 
 	@Override
 	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
 		int l = ISOCraftBlockMachine.determineOrientation(world, x, y, z, player);
 		world.setBlockMetadataWithNotify(x, y, z, l, 2);
 	}
 	
 	@Override
     @SideOnly(Side.CLIENT)
     public IIcon getIcon(IBlockAccess block, int x, int y, int z, int s){
 		ForgeDirection side = ((ISidedConnectable) block.getTileEntity(x, y, z)).getConnectableSides().get(0);
 		if (side != null && s == side.ordinal()) {
 			return this.Textures[1];
 		}
 		else {
 			return this.Textures[0];
 		}
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public IIcon getIcon(int i, int j) {
 		return this.Textures[0];
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerBlockIcons(IIconRegister par1IconRegister) {
 		this.Textures = new IIcon[2];
 		this.Textures[0] = par1IconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":eridiumNet/" + this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1) + "_0");
 		this.Textures[1] = par1IconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":eridiumNet/" + this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1) + "_1");
 	}
 }
