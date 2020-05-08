 package com.dmillerw.brainFuckBlocks.block;
 
 import java.util.List;
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 import com.dmillerw.brainFuckBlocks.BrainFuckBlocks;
import com.dmillerw.brainFuckBlocks.helper.LogHelper;
 import com.dmillerw.brainFuckBlocks.interfaces.IRotatable;
 import com.dmillerw.brainFuckBlocks.lib.ModInfo;
 import com.dmillerw.brainFuckBlocks.tileentity.TileEntityBrainFuckCode;
 import com.dmillerw.brainFuckBlocks.util.PlayerUtil;
 import com.dmillerw.brainFuckBlocks.util.Position;
 
 public class BlockBrainFuckCode extends BlockContainer {
 
 	public static Icon[][][] textures;
 	public static Icon bottomTexture;
 	public static Icon[] sideTexture;
 	
 	public static String[] blockNames = new String[] {"Increment Pointer", "Decrement Pointer", "Increment Byte", "Decrement Byte", "Output Byte", "Input Byte", "Bracket Open", "Bracket Close"};
 	
 	private static String[] blockFileNames = new String[] {"datainc", "datadec", "byteinc", "bytedec", "byteout", "bytein", "bracketopen", "bracketclose"};
 	private static String[] textureTypes = new String[] {"on", "off"};
 	
 	private static String[] symbolTextureRotations = new String[] {"north", "east", "south", "west"};
 	
 	protected BlockBrainFuckCode(int id) {
 		super(id, Material.rock);
 		setCreativeTab(BrainFuckBlocks.creativeTabBF);
 	}
 
 	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		LogHelper.log("Block Meta: "+world.getBlockMetadata(x, y, z));
		return true;
	}
	
	@Override
 	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving living, ItemStack stack) {
 		super.onBlockPlacedBy(world, x, y, z, living, stack);
 		EntityPlayer player = (EntityPlayer)living;
 		ForgeDirection side = PlayerUtil.get2dOrientation(new Position(player.posX, player.posY, player.posZ), new Position(x,y, z ));
 		IRotatable tile = (IRotatable) world.getBlockTileEntity(x, y, z);
 		tile.setRotation(side);
 	}
 	
 	@Override
 	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side) {
 		int meta = world.getBlockMetadata(x, y, z);
 		ForgeDirection sideForge = ForgeDirection.getOrientation(side);
 		IRotatable blockRotator = (IRotatable) world.getBlockTileEntity(x, y, z);
 
 		if (sideForge == ForgeDirection.DOWN) {
 			return bottomTexture;
 		} else if (sideForge != ForgeDirection.UP) {
 			if (sideForge == blockRotator.getRotation().getRotation(ForgeDirection.UP)) {
 				return sideTexture[1];
 			} else if (sideForge == blockRotator.getRotation().getRotation(ForgeDirection.UP).getOpposite()) {
 				return sideTexture[2];
 			}
 			
 			return sideTexture[0];
 		} else {
 			return textures[meta][1][getTextureIndexFromRotation(blockRotator.getRotation())];
 		}
 	}
 	
 	@Override
 	public Icon getBlockTextureFromSideAndMetadata(int side, int meta) {
 		ForgeDirection sideForge = ForgeDirection.getOrientation(side);
 		
 		if (sideForge == ForgeDirection.DOWN) {
 			return bottomTexture;
 		} else if (sideForge != ForgeDirection.UP) {
 			if (sideForge == ForgeDirection.WEST) {
 				return sideTexture[1];
 			} else if (sideForge == ForgeDirection.EAST) {
 				return sideTexture[2];
 			}
 			
 			return sideTexture[0];
 		} else {
 			return textures[meta][1][2];
 		}
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public void getSubBlocks(int id, CreativeTabs tab, List list) {
 		for (int i=0; i<blockNames.length; i++) {
 			list.add(new ItemStack(id, 1, i));
 		}
 	}
 	
 	@Override
 	public void registerIcons(IconRegister register) {
 		textures = new Icon[16][4][4];
 		
 		bottomTexture = register.registerIcon(ModInfo.MOD_ID.toLowerCase()+":code_bottom");
 		sideTexture = new Icon[3];
 		
 		sideTexture[0] = register.registerIcon(ModInfo.MOD_ID.toLowerCase()+":code_side");
 		sideTexture[1] = register.registerIcon(ModInfo.MOD_ID.toLowerCase()+":code_side_in");
 		sideTexture[2] = register.registerIcon(ModInfo.MOD_ID.toLowerCase()+":code_side_out");
 		
 		for (int i=0; i<blockNames.length; i++) {
 			for (int x=0; x<textureTypes.length; x++) {
 				for (int j=0; j<4; j++) {
 					textures[i][x][j] = register.registerIcon(ModInfo.MOD_ID.toLowerCase()+":code_" + blockFileNames[i] + "_" + symbolTextureRotations[j] + "_" + textureTypes[x]);
 				}
 			}
 		}
 	}
 	
 	private int getTextureIndexFromRotation(ForgeDirection rot) {
 		if (rot == ForgeDirection.NORTH) {
 			return 0;
 		} else if (rot == ForgeDirection.EAST) {
 			return 1;
 		} else if (rot == ForgeDirection.SOUTH) {
 			return 2;
 		} else if (rot == ForgeDirection.WEST) {
 			return 3;
 		}
 		
 		return 0;
 	}
 	
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntityBrainFuckCode();
 	}
 
 }
