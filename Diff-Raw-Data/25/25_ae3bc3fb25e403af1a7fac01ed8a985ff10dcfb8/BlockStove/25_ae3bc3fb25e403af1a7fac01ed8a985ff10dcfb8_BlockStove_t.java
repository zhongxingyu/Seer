 package adanaran.mods.bfr.blocks;
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import adanaran.mods.bfr.BFR;
 import adanaran.mods.bfr.entities.TileEntityStove;
 
 /**
  * 
  * @author Adanaran
  */
 public class BlockStove extends BlockContainer {
 
 	public BlockStove(int id) {
 		super(id, Material.rock);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntityStove();
 	}
 
 	@Override
 	public void registerIcons(IconRegister par1IconRegister) {
 		// TODO Demitreus versch. Texturen je nach Seite
 		this.blockIcon = par1IconRegister.registerIcon("bfr:stove");
 	}
 
 	@Override
 	public boolean onBlockActivated(World par1World, int par2, int par3,
 			int par4, EntityPlayer par5EntityPlayer, int par6, float par7,
 			float par8, float par9) {
		if (!par1World.isRemote) {
			par5EntityPlayer.openGui(BFR.instance, 1, par1World, par2, par3,
					par4);
		}
 		return true;
 	}
 
 }
