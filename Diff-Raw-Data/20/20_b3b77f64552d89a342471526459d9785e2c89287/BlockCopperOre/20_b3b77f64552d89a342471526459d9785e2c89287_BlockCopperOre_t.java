 package mrkirby153.MscHouses.block;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
 /**
  * 
  * Msc Houses
  *
  * BlockCopperOre
  *
  * @author mrkirby153
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 public class BlockCopperOre extends Block{
 
 	public BlockCopperOre(int par1) {
 		super(par1, Material.ground);
 		this.setHardness(1F);
 		this.setCreativeTab(CreativeTabs.tabAllSearch);
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		blockIcon = iconRegister.registerIcon("mschouses:CopperOre");
 	}
 
 }
