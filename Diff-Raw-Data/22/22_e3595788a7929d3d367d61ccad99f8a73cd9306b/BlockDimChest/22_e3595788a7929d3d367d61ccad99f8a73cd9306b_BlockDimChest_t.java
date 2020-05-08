 package mods.firstspring.dimchest;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockDimChest extends BlockContainer {
 	
 	private Icon linkChestSprite;
 
 	public BlockDimChest(int id) {
 		super(id, Material.iron);
 		this.setCreativeTab(CreativeTabs.tabDecorations);
 		//当たり判定の設定
 		this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
 	}
 
 	//破壊パーティクル用
 	@Override
 	@SideOnly(Side.CLIENT)
	public Icon getIcon(int par1, int par2) {
 		if(par2 == 0)
 			return linkChestSprite;
		return Block.blockIron.getIcon(0, 0);
 	}
 
 	//ブロックの方角を設定し、名前設定のGUIを開く
 	@Override
 	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLiving par5EntityLiving, ItemStack itemstack)
 	{
 		byte var6 = 0;
 		int var7 = MathHelper.floor_double((double)(par5EntityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
 
 		if (var7 == 0)
 		{
 			var6 = 2;
 		}
 
 		if (var7 == 1)
 		{
 			var6 = 5;
 		}
 
 		if (var7 == 2)
 		{
 			var6 = 3;
 		}
 
 		if (var7 == 3)
 		{
 			var6 = 4;
 		}
 		TileEntity tile = par1World.getBlockTileEntity(par2, par3, par4);
 		if(tile instanceof TileEntityDimChest){
 			((TileEntityDimChest)tile).orient = var6;
 		}
 		int meta = itemstack.getItemDamage();
 		par1World.setBlockMetadataWithNotify(par2, par3, par4, meta, 3);
 		if(meta == 1)
 			CommonProxy.proxy.openGui(par2, par3, par4);
 	}
 
 	@Override
 	public boolean isOpaqueCube()
 	{
 		return false;
 	}
 
 	@Override
 	public boolean renderAsNormalBlock()
 	{
 		return false;
 	}
 
 	@Override
 	public int getRenderType() {
 		return DimChest.dimChestRenderID;
 	}
 
 	//チェストのGUIを流用
 	@Override
 	public boolean onBlockActivated(World world, int x, int y,
 			int z, EntityPlayer entityplayer, int s, float p,
 			float q, float r) {
 		if(world.getBlockMetadata(x, y, z) == 0){
 			TileEntityDimChest tile = (TileEntityDimChest)world.getBlockTileEntity(x, y, z);
 			if(entityplayer.isSneaking()){
 				if(tile.hasLinkCard){
 					if(!world.isRemote){
 						dropCard(world, x, y, z);
 						world.playSoundEffect((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "tile.piston.out", 0.5F, world.rand.nextFloat() * 0.25F + 0.6F);
 					}
 					tile.hasLinkCard = false;
 					world.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
 					return true;
 				}else{
 					removeBlockByPlayer(world, entityplayer, x, y, z);
 					harvestBlock(world, entityplayer, x, y, z, 0);
 					if(!world.isRemote)
 						world.playAuxSFX(1000, x, y, z, 0);
 						world.playAuxSFX(1001, x, y, z, 0);
 				}
 			}
 			if(!world.isRemote){
 				if(tile.hasLinkCard){
 					entityplayer.displayGUIChest((IInventory)tile);
 					return true;
 				}
 				return false;
 			}else{
 				if(tile.hasLinkCard)
 					return true;
 			}
 			return false;
 		}
 		TileEntity tile = world.getBlockTileEntity(x, y, z);
 		if(!world.isRemote)
 			entityplayer.displayGUIChest((IInventory)tile);
 		return true;
 	}
 	
 	@Override
 	public void breakBlock(World world, int i, int j, int k, int par5, int par6){
 		dropCard(world, i, j, k);
 		super.breakBlock(world, i, j, k, par5, par6);
 	}
 	
 	public void dropCard(World world, int x, int y, int z){
 		if(!world.isRemote){
 			TileEntity tile = world.getBlockTileEntity(x, y, z);
 			if(tile instanceof TileEntityDimChest){
 				TileEntityDimChest chest = (TileEntityDimChest)world.getBlockTileEntity(x, y, z);
 				if(chest.hasLinkCard){
 					ItemStack is = new ItemStack(DimChest.linkCard, 1, chest.linkCardColor);
 					NBTTagCompound nbt = new NBTTagCompound();
 					is.setTagCompound(nbt);
 					nbt.setBoolean("flag", true);
 					nbt.setInteger("posx", chest.x);
 					nbt.setInteger("posy", chest.y);
 					nbt.setInteger("posz", chest.z);
 					nbt.setInteger("dimension", chest.dim);
 					EntityItem card = new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, is);
 					Random random = new Random(System.currentTimeMillis());
 					card.motionX = (double)((float)random.nextGaussian() * 0.05F);
 					card.motionY = (double)((float)random.nextGaussian() * 0.05F + 0.2F);
 					card.motionZ = (double)((float)random.nextGaussian() * 0.05F);
 					world.spawnEntityInWorld(card);
 				}
 			}
 		}
 	}
 
 	//設置時やワールドの読み込み時にTileEntityの生成をする
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntityDimChest();
 	}
 
 	public int idDropped(int par1, Random par2Random, int par3)
 	{
 		if(par1 == 0)
 			return this.blockID;
 		if(DimChest.doBreak)
 			if(DimChest.recipeBlock)
				return Block.blockIron.blockID;
 			else
 				return Item.ingotIron.itemID;
 		return this.blockID;
 	}
 	
 	@Override
 	public int damageDropped(int meta)
 	{
 		if(DimChest.doBreak && meta == 1)
 			return 0;
 		return meta;
 	}
 	
 	@Override
 	public int quantityDropped(int meta, int f, Random par1Random)
 	{
 		if(meta == 0)
 			return 1;
 		if(DimChest.doBreak)
 			return 8;
 		return 1;
 	}
 	
 	@Override
 	protected boolean canSilkHarvest()
 	{
 		return true;
 	}
 
 	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
 	{
 		par3List.add(new ItemStack(par1, 1, 0));
 		par3List.add(new ItemStack(par1, 1, 1));
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister) {
 		this.linkChestSprite = par1IconRegister.registerIcon("firstspring/dimchest:particlesprite");
 	}
 }
