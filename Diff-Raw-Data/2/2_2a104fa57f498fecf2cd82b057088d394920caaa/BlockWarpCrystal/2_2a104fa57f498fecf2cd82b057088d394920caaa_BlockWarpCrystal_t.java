 package shadowhax.crystalluscraft.block;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import shadowhax.crystalluscraft.CrystallusCraft;
 import shadowhax.crystalluscraft.block.tile.TileEntityWarpPad;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class BlockWarpCrystal extends BlockContainer
 {
     public BlockWarpCrystal(int par1) {
     	
         super(par1, Material.glass);
         this.setCreativeTab(CrystallusCraft.tab);
         this.setLightOpacity(0);
     }
     
     public static Icon icon;
     
     @Override
     public TileEntity createNewTileEntity(World par1World) {
     	
         return new TileEntityWarpPad();
     }
     
     public boolean isOpaqueCube() {
         return false;
     }
     
     public boolean renderAsNormalBlock() {
         return false;
     }
     
 
 	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
 
 		float var5 = 0.0625F;
 		return AxisAlignedBB.getAABBPool().getAABB((double)((float)par2 + var5), (double)par3, (double)((float)par4 + var5), (double)((float)(par2 + 1) - var5), (double)((float)1 - var5), (double)((float)(par4 + 1) - var5));
 	}
 
 	public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
 
 		float var5 = 0.0625F;
		return AxisAlignedBB.getAABBPool().getAABB((double)((float)par2 + var5), (double)((float)var5), (double)((float)par4 + var5), (double)((float)(par2 + 1) - var5), (double)((float)1-var5), (double)((float)(par4 + 1) - var5));
 	}
 	
 	public int getMobilityFlag() {
 
 		return 3;
 	}
 
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister ir) {	
     	icon = ir.registerIcon("crystalluscraft:blank");
     }
     
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int par1, int par2) {
     	
     	return this.icon;
     }
 }
