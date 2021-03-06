 package electricexpansion.common.blocks;
 
 import java.util.List;
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import universalelectricity.prefab.UETab;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import electricexpansion.common.CommonProxy;
 import electricexpansion.common.ElectricExpansion;
 import electricexpansion.common.tile.TileEntityDistribution;
 import electricexpansion.common.tile.TileEntityInductionReciever;
 import electricexpansion.common.tile.TileEntityInductionSender;
 
 public class BlockWPT extends BlockContainer
 {
 	public BlockWPT(int id, int meta)
 	{
 		super(id, Material.iron);
 		this.setBlockName("WPT");
 		this.setStepSound(soundMetalFootstep);
 		this.setResistance(0.2F);
 		this.setRequiresSelfNotify();
 		this.setHardness(1.5F);
 		this.setResistance(10.0F);
 		this.setCreativeTab(UETab.INSTANCE);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world, int meta)
 	{
 		if (meta >= 0 && meta < 4)
 			return new TileEntityDistribution();
 		else if (meta >= 4 && meta < 8)
 			return new TileEntityInductionSender();
 		else if (meta >= 8 && meta < 12)
 			return new TileEntityInductionReciever();
 		else
 			return null;
 	}
 
 	@Override
 	public boolean isOpaqueCube()
 	{
 		return true;
 	}
 
 	@Override
 	public int damageDropped(int i)
 	{
 		byte dropped = 0;
 		switch (i)
 		{
 			case 0:
 				dropped = 0;
 				break;
 			case 1:
 				dropped = 0;
 				break;
 			case 2:
 				dropped = 0;
 				break;
 			case 3:
 				dropped = 0;
 				break;
 			case 4:
 				dropped = 4;
 				break;
 			case 5:
 				dropped = 4;
 				break;
 			case 6:
 				dropped = 4;
 				break;
 			case 7:
 				dropped = 4;
 				break;
 			case 8:
 				dropped = 8;
 				break;
 			case 9:
 				dropped = 8;
 				break;
 			case 10:
 				dropped = 8;
 				break;
 			case 11:
 				dropped = 8;
 				break;
 		}
 		return (int) dropped;
 	}
 
 	@Override
 	public boolean renderAsNormalBlock()
 	{
 		return true;
 	}
 
 	@Override
 	public int getRenderType()
 	{
 		return 0;
 	}
 
 	public String getTextureFile()
 	{
 		return CommonProxy.ABLOCK;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
 	{
 		par3List.add(new ItemStack(par1, 1, 0));
 		par3List.add(new ItemStack(par1, 1, 4));
 		par3List.add(new ItemStack(par1, 1, 8));
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World var1)
 	{
 		return null;
 	}
 
	/*
 	 * Called when the block is right clicked by the player
	 */
 	@Override
 	public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
 	{
 		int metadata = par1World.getBlockMetadata(x, y, z);
 
 		if (!par1World.isRemote)
 		{
 
 			par5EntityPlayer.openGui(ElectricExpansion.instance, 4, par1World, x, y, z);
 			return true;
 
 		}
 
 		return true;
	}
 }
