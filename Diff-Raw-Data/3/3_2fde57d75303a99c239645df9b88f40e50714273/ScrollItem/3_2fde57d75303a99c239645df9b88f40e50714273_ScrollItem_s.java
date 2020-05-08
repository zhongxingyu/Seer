 package mods.nordwest.items;
 
 import java.util.List;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import mods.nordwest.common.CustomBlocks;
 import mods.nordwest.common.NordWest;
 import mods.nordwest.utils.EnumColor;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockStone;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 
 public class ScrollItem extends BaseItem {
 
 	public ScrollItem(int par1) {
 		super(par1);
 		setCreativeTab(NordWest.tabNord);
 		this.setMaxStackSize(16);
 		this.setHasSubtypes(true);
 		this.setMaxDamage(0);
 		// TODO Auto-generated constructor stub
 	}
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
 		if (par1ItemStack.hasTagCompound()) {
 			if (par1ItemStack.getItemDamage() == 1) {
 				return;
 			}
 			par3List.add(par1ItemStack.getTagCompound().getString("Lore"));
 			int x, y, z;
 			x = par1ItemStack.getTagCompound().getInteger("X");
 			y = par1ItemStack.getTagCompound().getInteger("Y");
 			z = par1ItemStack.getTagCompound().getInteger("Z");
 			String world = par1ItemStack.getTagCompound().getString("world");
 			par3List.add("X: " + x);
 			par3List.add("Y: " + y);
 			par3List.add("Z: " + z);
 			par3List.add("World: " + world);
 
 		}
 
 	}
 	@Override
 	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
 		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
 		if (movingobjectposition == null) {
 			if (returnTeleport(itemstack, world, player))
 				itemstack.stackSize--;
 
 			return itemstack;
 		} else {
 			int x = movingobjectposition.blockX;
 			int y = movingobjectposition.blockY;
 			int z = movingobjectposition.blockZ;
 			if (!itemstack.hasTagCompound()) {
 				if (itemstack.itemID == this.itemID && (testBlock(world, x, y, z))) {
 					ItemStack item = new ItemStack(this, 1, itemstack.getItemDamage());
 					NBTTagCompound tag = item.getTagCompound();
 					if (tag == null) {
 						tag = new NBTTagCompound();
 						item.setTagCompound(tag);
 					}
 					tag.setString("Lore", player.getEntityName());
 					tag.setInteger("X", x);
 					tag.setInteger("Y", y);
 					tag.setInteger("Z", z);
 					tag.setString("world", world.provider.getDimensionName());
 					itemstack.stackSize--;
 					player.inventory.addItemStackToInventory(item);
 					world.playSound(x, y, z, "random.breath", 0.5f, 1.2f, false);
 					world.spawnParticle("reddust", x + 0.5D, y + 1.0D, z + 0.5D, 0.0D, 0.5D, 0.0D);
 					return itemstack;
 				}
 			} else {
 				if (returnTeleport(itemstack, world, player))
 					itemstack.stackSize--;
 				return itemstack;
 			}
 		}
 		return itemstack;
 	}
 
 	private boolean returnTeleport(ItemStack itemstack, World world, EntityPlayer player) {
 		if (itemstack.hasTagCompound()) {
 			int x, y, z;
 
 			x = itemstack.getTagCompound().getInteger("X");// + 0.5d;
 			y = itemstack.getTagCompound().getInteger("Y");// + 1.2d;
 			z = itemstack.getTagCompound().getInteger("Z");// + 0.5d;
 			String worldName = itemstack.getTagCompound().getString("world");
 			if (worldName.equals(world.provider.getDimensionName()) && (testBlock(world, x, y, z, player, itemstack) || itemstack.getItemDamage() == 1)) {
 				effectDdaw(world, player.posX, player.posY - 1, player.posZ);
 				world.playSound(x + 0.5D, y + 1.0D, z + 0.5D, "random.breath", 0.5f, 2.2f, false);
 				effectDdaw(world, x + 0.5D, y + 1.0D, z + 0.5D);
 				player.setPositionAndUpdate(x + 0.5D, y + 1.0D, z + 0.5D);
 				player.fallDistance = 0.0F;
 				return true;
 			}
 
 		}
 		return false;
 	}
 
 	private void effectDdaw(World world, double x, double y, double z) {
 		double r = 0.5d;
 		int c = 36;
 		for (int i = 1; i < c; i++) {
 			world.spawnParticle("portal", x + r * Math.cos(i), y + (i * 0.025), z + r * Math.sin(i), 0.0D, -0.5D, 0.0D);
 
 		}
 	}
 
 	private boolean testBlock(World world, int x, int y, int z, EntityPlayer player, ItemStack itemstack) {
 		if (itemstack.getItemDamage() == 1) {
 			return true;
 		}
 		int id = world.getBlockId(x, y, z);
 		if (id != CustomBlocks.blockhome.blockID) {
 			if (player.capabilities.isCreativeMode) {
 				if (!world.isRemote)
 					player.sendChatToPlayer(EnumColor.YELLOW + LanguageRegistry.instance().getStringLocalization("scroll.error.creative"));
 				return true;
 			}
 			if (!world.isRemote)
 				player.sendChatToPlayer(EnumColor.DARK_RED + LanguageRegistry.instance().getStringLocalization("scroll.error.notcreative"));
 			return false;
 
 		} else {
 			boolean test = true;
			player.sendChatToPlayer(EnumColor.PURPLE + LanguageRegistry.instance().getStringLocalization("scroll.tp"));
 			return test;
 		}
 	}
 
 	private boolean testBlock(World world, int x, int y, int z) {
 		int id = world.getBlockId(x, y, z);
 		if (id != CustomBlocks.blockhome.blockID) {
 			return false;
 		} else {
 			boolean test = true;
 			/** Altar Feature **/
 			/*
 			 * for (int i = 0; i < 4; i++) { test &= world.getBlockId(x + 2, y + i, z + 2) == Block.obsidian.blockID; test &= world.getBlockId(x - 2, y + i, z + 2) == Block.obsidian.blockID; test &= world.getBlockId(x + 2, y + i, z - 2) == Block.obsidian.blockID; test &= world.getBlockId(x - 2, y + i, z - 2) == Block.obsidian.blockID; }
 			 */
 			return test;
 		}
 	}
 	@Override
 	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
 		for (int j = 0; j < 2; ++j) {
 			par3List.add(new ItemStack(par1, 1, j));
 		}
 	}
 	@Override
 	public String getUnlocalizedName(ItemStack par1ItemStack) {
 		int i = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, 15);
 		return super.getUnlocalizedName() + "." + i;
 	}
 }
