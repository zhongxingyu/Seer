 /**
  * This class was created by <Vazkii>. It's distributed as
  * part of the ThaumicTinkerer Mod.
  *
  * ThaumicTinkerer is Open Source and distributed under a
  * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
  * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
  *
  * ThaumicTinkerer is a Derivative Work on Thaumcraft 4.
  * Thaumcraft 4 (c) Azanor 2012
  * (http://www.minecraftforum.net/topic/1585216-)
  *
  * File Created @ [Dec 27, 2013, 6:36:19 PM (GMT)]
  */
 package vazkii.tinkerer.common.block;
 
 import java.util.List;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.World;
 import vazkii.tinkerer.common.block.tile.TileCamo;
 
 public class BlockPlatform extends BlockCamo {
 
 	public BlockPlatform(int par1) {
 		super(par1, Material.wood);
 	}
 
 	@Override
 	public void addCollisionBoxesToList(World par1World, int par2, int par3, int par4, AxisAlignedBB par5AxisAlignedBB, List par6List, Entity par7Entity) {
		if(par7Entity.posY > par3 + (par7Entity instanceof EntityPlayer ? 1 : 0) && (!(par7Entity instanceof EntityPlayer) || !par7Entity.isSneaking()))
 			super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
 	}
 
 	@Override
 	public TileCamo createNewTileEntity(World world) {
 		return new TileCamo();
 	}
 
 }
