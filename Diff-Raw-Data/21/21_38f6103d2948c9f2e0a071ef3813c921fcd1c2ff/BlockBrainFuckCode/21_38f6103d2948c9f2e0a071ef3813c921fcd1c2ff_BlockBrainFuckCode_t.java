 package com.dmillerw.brainFuckBlocks.block;
 
 import java.util.List;
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 import com.dmillerw.brainFuckBlocks.BrainFuckBlocks;
 import com.dmillerw.brainFuckBlocks.lib.ModInfo;
 
 public class BlockBrainFuckCode extends BlockContainer {
 
 	public static Icon[][] textures;
 	public static Icon bottomTexture;
 	public static Icon sideTexture;
 	
 	public static String[] blockNames = new String[] {"Increment Pointer", "Decrement Pointer", "Increment Byte", "Decrement Byte", "Output Byte", "Input Byte", "Bracket Open", "Bracket Close"};
 	
 	private static String[] blockFileNames = new String[] {"datainc", "datadec", "byteinc", "bytedec", "byteout", "bytein", "bracketopen", "bracketclose"};
 	private static String[] textureTypes = new String[] {"on", "off"};
 	
 	protected BlockBrainFuckCode(int id) {
 		super(id, Material.rock);
 		setCreativeTab(BrainFuckBlocks.creativeTabBF);
 	}
 
 	@Override
 	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side) {
 		int meta = world.getBlockMetadata(x, y, z);
 		ForgeDirection sideForge = ForgeDirection.getOrientation(side);
 		
 		//TODO Add active state from TE
 		
 		if (sideForge == ForgeDirection.DOWN) {
 			return bottomTexture;
 		} else if (sideForge != ForgeDirection.UP) {
 			return sideTexture;
 		} else {
 			return textures[meta][0];
 		}
 	}
 	
	@Override
	public Icon getBlockTextureFromSideAndMetadata(int side, int meta) {
		ForgeDirection sideForge = ForgeDirection.getOrientation(side);
		
		if (sideForge == ForgeDirection.DOWN) {
			return bottomTexture;
		} else if (sideForge != ForgeDirection.UP) {
			return sideTexture;
		} else {
			return textures[meta][1];
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
 		textures = new Icon[16][4];
 		
		bottomTexture = register.registerIcon(ModInfo.MOD_ID.toLowerCase()+":code_bottom");
		sideTexture = register.registerIcon(ModInfo.MOD_ID.toLowerCase()+":code_side");
 		
 		for (int i=0; i<blockNames.length; i++) {
 			for (int x=0; x<textureTypes.length; x++) {
 				textures[i][x] = register.registerIcon(ModInfo.MOD_ID.toLowerCase()+":code_" + blockFileNames[i] + "_" + textureTypes[x] );
 			}
 		}
 	}
 	
 	@Override
 	public TileEntity createTileEntity(World world, int meta) {
 		return null;
 	}
 	
 	/* IGNORE */
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return null;
 	}
 
 }
