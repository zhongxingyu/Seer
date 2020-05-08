 package widux.creativetools;
 
 import java.util.List;
 import java.util.Random;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 public class BlockCreative extends BlockContainer
 {
 	
 	Icon[][] textures;
 	
 	public BlockCreative(int ID)
 	{
 		super(ID, Material.iron);
 		this.setCreativeTab(CreativeTabs.tabRedstone);
 	}
 
 	public TileEntity createNewTileEntity(World world)
 	{
 		return null;
 	}
 	
 	public TileEntity createTileEntity(World world, int meta)
 	{
 		switch(meta)
 		{
 		case 0:
 			return new TileEntityItemDuplicator();
 		case 1:
 			return new TileEntityLiquidDuplicator();
 		case 2:
 			return new TileEntityPowerDuplicator();
 		default:
 			return null;
 		}
 	}
 	
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float xOffset, float yOffset, float zOffset)
     {
 		int meta = world.getBlockMetadata(x, y, z);
 		switch(meta)
 		{
 		case 0:
 			player.openGui(CreativeTools.instance, 0, world, x, y, z);
 			return true;
 		case 1:
 			player.openGui(CreativeTools.instance, 1, world, x, y, z);
 			return true;
 		case 2:
 			player.openGui(CreativeTools.instance, 2, world, x, y, z);
 			return true;
 		default:
 			return false;
 		}
     }
 	
 	@SideOnly(Side.CLIENT)
    public void func_94332_a(IconRegister iconRegister)
     {
         this.textures = new Icon[3][6];
 
         for (int meta = 0; meta < 3; meta++)
         {
         	this.textures[meta][0] = iconRegister.registerIcon(ModInfo.INTERNAL_NAME + ":" + getBlockNameFromMeta(meta) + "_bottom");
         	this.textures[meta][1] = iconRegister.registerIcon(ModInfo.INTERNAL_NAME + ":" + getBlockNameFromMeta(meta) + "_top");
         	this.textures[meta][2] = iconRegister.registerIcon(ModInfo.INTERNAL_NAME + ":" + getBlockNameFromMeta(meta) + "_side");
         	this.textures[meta][3] = this.textures[meta][2];
         	this.textures[meta][4] = this.textures[meta][2];
         	this.textures[meta][5] = this.textures[meta][2];
         }
     }
 	
 	private String getBlockNameFromMeta(int meta)
 	{
 		switch(meta)
 		{
 		case 0:
 			return "ItemBlock";
 		case 1:
 			return "LiquidBlock";
 		case 2:
 			return "EnergyBlock";
 		default:
 			return null;
 		}
 	}
 	
     public Icon getBlockTexture(IBlockAccess iba, int x, int y, int z, int blockSide)
     {
     	int meta = iba.getBlockMetadata(x, y, z);
     	return this.textures[meta][blockSide];
     }
 	
     public Icon getBlockTextureFromSideAndMetadata(int side, int meta)
     {
     	return this.textures[meta][side];
     }
     
 	public int idDropped(int meta, Random random, int i)
 	{
 		return 0;
 	}
 	
 	public int quantityDropped(Random random)
 	{
 		return 0;
 	}
 	
     @SuppressWarnings({ "rawtypes", "unchecked" })
 	@SideOnly(Side.CLIENT)
     public void getSubBlocks(int unknown, CreativeTabs tab, List subItems)
     {
     	for (int meta = 0; meta < 3; meta++)
     	{
     		subItems.add(new ItemStack(this, 1, meta));
     	}
     }
 	
 }
