 package hsb.block;
 
 import hsb.lib.Strings;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 
 public class BlockHeavyStone extends BlockSimple{
 
 	public BlockHeavyStone(int id) {
 		super(id, Material.rock);
 		this.setHardness(40F);
 		this.setResistance(400F);
 		this.setUnlocalizedName(Strings.BLOCK_HEAVY_STONE);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister reg) {
		// TODO Auto-generated method stub
		
 	}
 
 }
