 package mods.firstspring.dimchest;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityChest;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemLinkCard extends Item {
 	int posx=0,posy=0,posz=0,dimension=0;
 	boolean hasPosition=false;
 	
 	Icon[] icons;
 
 	public ItemLinkCard(int id) {
 		super(id);
 		this.maxStackSize = 1;
 		this.setHasSubtypes(true);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIconFromDamage(int meta) {
 		if(meta == -1)
 			return icons[16];//レシピガイド用
 		return icons[meta];
 	}
 
 	@Override
 	public boolean onItemUse(ItemStack stack, EntityPlayer player,
 			World world, int x, int y, int z, int side, float hitX, float hitY,
 			float hitZ) {
 		readNBT(stack);
 		TileEntity tile = world.getBlockTileEntity(x, y, z);
 		if(tile instanceof TileEntityDimChest){
 			TileEntityDimChest chest = (TileEntityDimChest)tile;
 			if(this.hasPosition && !chest.hasLinkCard){
 				chest.x = this.posx;
 				chest.y = this.posy;
 				chest.z = this.posz;
 				chest.dim = this.dimension;
 				chest.linkCardColor = stack.getItemDamage();
 				chest.hasLinkCard = true;
 				player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack)null);
 				if(!world.isRemote){
 					world.playSoundEffect((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "tile.piston.in", 0.5F, world.rand.nextFloat() * 0.25F + 0.6F);
 					world.notifyBlocksOfNeighborChange(x, y, z, DimChest.dimChestID);
 				}
 			}
 		}
 		return true;
 	}
 	
 	@Override
 	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player,
 			World world, int x, int y, int z, int side, float hitX, float hitY,
 			float hitZ) {
 		TileEntity tile = world.getBlockTileEntity(x, y, z);
 		if(tile instanceof IInventory){
 			if(tile instanceof TileEntityDimChest && tile.getBlockMetadata() == 0)
 				return false;
 			if(!DimChest.isEnableModInventory && !(tile instanceof TileEntityChest || tile instanceof TileEntityDimChest))
 				return false;
 			readNBT(stack);
 			this.posx=x;
 			this.posy=y;
 			this.posz=z;
 			this.hasPosition=true;
 			this.dimension=player.dimension;
 			writeNBT(stack);
 			if(world.isRemote)
 				return false;//ここでtrueを返すと以降の動作（サーバー含む）がカットされる
 			return true;
 		}
 		return false;
 	}
 	
 	private void readNBT(ItemStack itemstack){
 		NBTTagCompound nbt = itemstack.getTagCompound();
 		if(nbt != null){
 			hasPosition = nbt.getBoolean("flag");
 			posx = nbt.getInteger("posx");
 			posy = nbt.getInteger("posy");
 			posz = nbt.getInteger("posz");
 			dimension = nbt.getInteger("dimension");
 			return;
 		}
 		hasPosition = false;
 	}
 
 	private void writeNBT(ItemStack itemstack){
 		NBTTagCompound nbt = new NBTTagCompound();
 		nbt.setBoolean("flag", hasPosition);
 		nbt.setInteger("posx", posx);
 		nbt.setInteger("posy", posy);
 		nbt.setInteger("posz", posz);
 		nbt.setInteger("dimension", dimension);
 		itemstack.setTagCompound(nbt);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
	public void updateIcons(IconRegister par1IconRegister) {
 		icons = new Icon[16];
 		String[] dyeColorNames = new String[] {"black", "red", "green", "brown", "blue", "purple", "cyan", "silver", "gray", "pink", "lime", "yellow", "lightBlue", "magenta", "orange", "white"};
 		for(int i = 0 ; i < 16 ; i++)
 			icons[i] = par1IconRegister.registerIcon("firstspring/dimchest:linkcard_" + dyeColorNames[i]);
 	}
 }
