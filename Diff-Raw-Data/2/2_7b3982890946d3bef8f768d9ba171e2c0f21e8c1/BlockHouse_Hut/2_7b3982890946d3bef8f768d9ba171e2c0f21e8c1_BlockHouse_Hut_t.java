 package mrkirby153.MscHouses;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * MscHouses
  * 
  * @author mrkirby153
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 public class BlockHouse_Hut extends Block {
 
 	protected BlockHouse_Hut(int par1) {
 		super(par1, Material.ground);
 		this.setCreativeTab(MscHouses.tabHouse);
 		GameRegistry.registerBlock(this, "BlockHut");
 	}
 
 	@Override
 	public boolean onBlockActivated(World par1World, int par2, int par3,
 			int par4, EntityPlayer par5EntityPlayer, int par6, float par7,
 			float par8, float par9) {
		 MscHouses.h.hut(par2, par3, par4, par1World);
 		return true;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister) {
 		blockIcon = par1IconRegister.registerIcon("MscHouses:BlockHut");
 	}
 }
