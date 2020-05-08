 package mods.themike.modjam.blocks;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import mods.themike.modjam.ModJam;
 import mods.themike.modjam.proxy.ClientProxy;
 import mods.themike.modjam.tile.TileEntityCarvingStone;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 public class BlockCarvingStone extends BlockContainer {
 
 	public BlockCarvingStone(int par1) {
 		super(par1, Material.rock);
 		this.setHardness(1.0F);
 		this.setCreativeTab(ModJam.tab);
 		this.setResistance(2.0F);
 		this.setUnlocalizedName("carvingStone");
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntityCarvingStone();
 	}
 	
 	@Override
 	public int getRenderType() {
 		return ClientProxy.handler.getRenderId();
 	}
 	
 	@Override
 	public boolean isOpaqueCube() {
 		return false;
 	}
 	
 	@Override
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 
 }
