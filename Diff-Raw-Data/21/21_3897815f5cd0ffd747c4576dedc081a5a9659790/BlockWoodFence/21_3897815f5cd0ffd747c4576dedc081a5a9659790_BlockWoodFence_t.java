 package net.mcft.copy.vanilladj.block;
 
 import java.util.List;
 
 import net.mcft.copy.vanilladj.misc.WoodUtils;
 import net.minecraft.block.BlockFence;
 import net.minecraft.block.BlockWood;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockWoodFence extends BlockFence {
 	
 	private Icon[] icons;
 	
 	public BlockWoodFence(int id) {
 		super(id, null, Material.wood);
 		setHardness(2.0F);
 		setResistance(5.0F);
 		setStepSound(soundWoodFootstep);
 		setUnlocalizedName("fence");
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		icons = WoodUtils.registerIcons(iconRegister);
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int meta) {
 		return icons[Math.min(meta, icons.length - 1)];
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int id, CreativeTabs tabs, List list) {
 		for (int i = 0; i < BlockWood.woodType.length; i++)
 			list.add(new ItemStack(id, 1, i));
 	}
 	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
	                                EntityPlayer player, int side,
	                                float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			AxisAlignedBB aabb = AxisAlignedBB.getAABBPool().getAABB(x - 7, y - 7, z - 7, x + 7, y + 7, z + 7);
			List<EntityLiving> entities = world.getEntitiesWithinAABB(EntityLiving.class, aabb);
			for (EntityLiving entity : entities)
				if (entity.func_110167_bD() && entity.func_110166_bE() == player)
					return true;
			return false;
		} else return super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
	}
	
 }
