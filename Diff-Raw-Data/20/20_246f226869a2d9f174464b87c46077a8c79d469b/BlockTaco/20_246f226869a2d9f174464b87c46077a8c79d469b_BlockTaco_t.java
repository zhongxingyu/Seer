 package assets.tacotek.blocks;
 
 import assets.tacotek.common.tacotek;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.util.Icon;
 
 public class BlockTaco extends Block {
 
 	private static String textureName;
 	
	//Create Definitions for textures
 	@SideOnly(Side.CLIENT)
 	private Icon topTexture;
	@SideOnly(Side.CLIENT)
 	private Icon bottomTexture;
 	
 	public BlockTaco(int id, String textureName) {
 		super(id, Material.rock);
 		this.textureName = textureName;
 	}
 	
	//Register the different textures tot he definitions.
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister reg) {
 		this.blockIcon = reg.registerIcon(tacotek.modID + ":" + textureName);
 		this.topTexture = reg.registerIcon(tacotek.modID + ":" + textureName + "_top");
 		this.bottomTexture = reg.registerIcon(tacotek.modID + ":" + textureName + "_bottom");
 	}
 	
	//Now apply one of the textures, depending on the side.
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int meta) {
		//1 == top
 		if(side == 1) {
 			return this.topTexture;
 		}
		//0 == bottom
 		else if (side == 0) {
 			return this.bottomTexture;
 		}
 		return this.blockIcon;
 	}
 }
