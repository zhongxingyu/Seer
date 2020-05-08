 package mods.nordwest.blocks;
 
 import mods.nordwest.common.NordWest;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 
 public class BaseBlock extends Block {
 
 	public BaseBlock(int par1, Material par2Material) {
 		super(par1, par2Material);
 		setCreativeTab(NordWest.tabNord);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void registerIcons(IconRegister iconRegister) {
 		blockIcon = iconRegister.registerIcon("nordwest:" + this.getUnlocalizedName2());
 	}
 
	public void onBlockAdded(World world, int x, int y, int z, EntityPlayer player) {		
	}
 }
