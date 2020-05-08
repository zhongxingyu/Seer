 package sbfp;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public abstract class BlockSB extends Block{
 
 	public BlockSB(int id, Material material, String name){
 		super(id,material);
 		this.setUnlocalizedName(name);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister i){
 		this.blockIcon = i.registerIcon("sbfp:"+this.getUnlocalizedName2());
 	}
}
