 package makmods.levelstorage.block;
 
 import ic2.api.tile.IWrenchable;
 
 import java.util.Random;
 
 import makmods.levelstorage.LevelStorage;
 import makmods.levelstorage.lib.IC2Items;
 import makmods.levelstorage.logic.BlockTextureRegistry;
 import makmods.levelstorage.logic.BlockTextureRegistry.SimpleBlockTexture;
 import makmods.levelstorage.logic.util.Helper;
 import makmods.levelstorage.proxy.ClientProxy;
 import makmods.levelstorage.tileentity.TileEntityBasicMachine;
 import makmods.levelstorage.tileentity.TileEntityMolecularHeater;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.Property;
 import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockMolecularHeater extends BlockContainer {
 
 	public BlockMolecularHeater(int id) {
 		super(id, Material.iron);
 		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
 			this.setCreativeTab(ClientProxy.getCreativeTab("IC2"));
 		}
 		this.setStepSound(Block.soundMetalFootstep);
 		this.setHardness(3.0F);
 	}
	
	private String ICON = "blockMolHeater";
 
 	public Icon facing;
 
 	public static void addCraftingRecipe() {
 		Property p = LevelStorage.configuration.get(
 		        Configuration.CATEGORY_GENERAL,
 		        "enableMolecularHeaterCraftingRecipe", true);
 		p.comment = "Determines whether or not crafting recipe is enabled";
 		if (p.getBoolean(true)) {
 
 		}
 	}
 
 	@Override
 	public int idDropped(int par1, Random par2Random, int par3) {
 		return IC2Items.ADV_MACHINE.itemID;
 	}
 
 	@Override
 	public int damageDropped(int par1) {
 		return IC2Items.ADV_MACHINE.getItemDamage();
 	}
 
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
 		Helper.dropBlockItems(world, x, y, z);
 		super.breakBlock(world, x, y, z, par5, par6);
 	}
 
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z,
 	        EntityPlayer player, int par6, float par7, float par8, float par9) {
 		return Helper.handleMachineRightclick(world, x, y, z, player);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int par2) {
 		return side == ForgeDirection.SOUTH.ordinal() ? facing
 		        : BlockTextureRegistry.instance.getIcon(side,
		                ICON);
 	}
 
 	// TODO: DO NOT FORGET ABOUT DESCRIPTION PACKETS IN BASE SINK.
 	// IF SOMETHING GOES WRONG TURN IT ON.
 
 	public void onBlockPlacedBy(World world, int x, int y, int z,
 	        EntityLivingBase entityliving, ItemStack itemStack) {
 		if (world.isRemote)
 			return;
 
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 
 		if ((tileEntity instanceof IWrenchable)) {
 			IWrenchable te = (IWrenchable) tileEntity;
 
 			if (entityliving == null) {
 				te.setFacing((short) 2);
 			} else {
 				int l = MathHelper
 				        .floor_double(entityliving.rotationYaw * 4.0F / 360.0F + 0.5D) & 0x3;
 
 				switch (l) {
 					case 0:
 						te.setFacing((short) 2);
 						break;
 					case 1:
 						te.setFacing((short) 5);
 						break;
 					case 2:
 						te.setFacing((short) 3);
 						break;
 					case 3:
 						te.setFacing((short) 4);
 				}
 			}
 			entityliving.worldObj.markBlockForUpdate(x, y, z);
 		}
 	}
 
 	public Icon getBlockTexture(IBlockAccess access, int x, int y, int z,
 	        int side) {
 		if (!LevelStorage.isSimulating()) {
 			// Minecraft.getMinecraft().theWorld.markBlockForUpdate(x, y, z);
 			TileEntity te = access.getBlockTileEntity(x, y, z);
 			if (te != null && te instanceof TileEntityBasicMachine) {
 				if (side == ((TileEntityBasicMachine) te).getFacing()) {
 					return facing;
 				}
 			}
 			SimpleBlockTexture txt = BlockTextureRegistry.instance
			        .getTextureFor(ICON);
 			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[side];
 			switch (dir) {
 				case UP:
 					return txt.getUpIcon();
 				case DOWN:
 					return txt.getDownIcon();
 				default:
 					return txt.getSideIcon();
 			}
 		}
 		return null;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		BlockTextureRegistry.instance.registerIcons(iconRegister,
		        ICON);
 		facing = iconRegister.registerIcon(ClientProxy.MOLECULAR_HEATER_FACING);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntityMolecularHeater();
 	}
 
 }
