 package mods.nordwest.items;
 
 import java.util.List;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import mods.nordwest.common.CustomBlocks;
 import mods.nordwest.common.NordWest;
 import mods.nordwest.utils.EnumColors;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockStone;
 import net.minecraft.client.Minecraft;
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
 	}
 	
 	@Override
 	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
 		if (par1ItemStack.hasTagCompound()) {
 			if (par1ItemStack.getItemDamage() == 1) {
 				return;
 			}
 			int x, y, z;
 			int worldID = par1ItemStack.getTagCompound().getInteger("worldID");
 			String by = LanguageRegistry.instance().getStringLocalization("author.by");
 			String doublePoint = ": ";
 			String worldDescription = LanguageRegistry.instance().getStringLocalization("world.desc");
 			String worldName = LanguageRegistry.instance().getStringLocalization("world.undefined");
 			String color = "\u00a77"; //Default Color
 			if (worldID == 0) {
 				worldName = LanguageRegistry.instance().getStringLocalization("world.overworld");
 			} else if (worldID == -1) {
 				worldName = LanguageRegistry.instance().getStringLocalization("world.nether");
 			} else if (worldID != 0 || worldID != 1 || worldID != -1 && par1ItemStack.getTagCompound().getString("worldName") == null){
 				worldName = par1ItemStack.getTagCompound().getString("worldName");
 				color = "\u00a7e"; //Yellow Color
 			} else {
 				worldName = LanguageRegistry.instance().getStringLocalization("world.undefined");
 				color = "\u00a78"; //Gray Color
 			}
 			x = par1ItemStack.getTagCompound().getInteger("X");
 			y = par1ItemStack.getTagCompound().getInteger("Y");
 			z = par1ItemStack.getTagCompound().getInteger("Z");
 			par3List.add(color + worldDescription + doublePoint + worldName);
 			par3List.add(by + doublePoint + par1ItemStack.getTagCompound().getString("Lore"));
 			par3List.add("X" + doublePoint + x);
 			par3List.add("Y" + doublePoint  + y);
 			par3List.add("Z" + doublePoint  + z);
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
				if (itemstack.itemID == this.itemID/** && (testBlock(world, x, y, z))*/) {
 					ItemStack item = new ItemStack(this, 1, itemstack.getItemDamage());
 					NBTTagCompound tag = item.getTagCompound();
 					if (tag == null) {
 						tag = new NBTTagCompound();
 						item.setTagCompound(tag);
 					}
 					tag.setString("Lore", player.getEntityName());
 					tag.setString("worldName", world.provider.getDimensionName());
 					tag.setInteger("X", x);
 					tag.setInteger("Y", y);
 					tag.setInteger("Z", z);
 					tag.setInteger("worldID", world.provider.dimensionId);
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
 
 			x = itemstack.getTagCompound().getInteger("X"); // + 0.5d;
 			y = itemstack.getTagCompound().getInteger("Y"); // + 1.2d;
 			z = itemstack.getTagCompound().getInteger("Z"); // + 0.5d;
 			int worldId = itemstack.getTagCompound().getInteger("worldID");
 			if (/**worldId == (world.provider.dimensionId) && */(testBlock(world, x, y, z, player, itemstack) || itemstack.getItemDamage() == 1)) {
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
 	
 	
 	private void badEffectDraw(World world, double x, double y, double z) {
 		double r = 0.5d;
 		int c = 36;
 		for (int i = 1; i < c; i++) {
 			world.spawnParticle("smoke", x + r * Math.cos(i), y + (i * 0.025), z + r * Math.sin(i), 0.0D, 0.0D, 0.0D);
 		}
 	}
 
 	private boolean testBlock(World world, int x, int y, int z, EntityPlayer player, ItemStack itemstack) {
 		if (itemstack.getItemDamage() == 1) {
 			return true;
 		}
 		int id = world.getBlockId(x, y, z);
 		int worldId = itemstack.getTagCompound().getInteger("worldID");
 		if (id != CustomBlocks.blockhome.blockID && worldId == (world.provider.dimensionId)) {
 			if (player.capabilities.isCreativeMode) {
				spamProtection (world, x, y, z, player, itemstack);
				return true;
 			}
 			spamProtection (world, x, y, z, player, itemstack);
 			return false;
 		} else {
 			if (worldId != (world.provider.dimensionId)) {
 				spamProtection (world, x, y, z, player, itemstack);
 				return false;
 			} else if (worldId != (world.provider.dimensionId)) {
 			return false;
 			} else {
 			boolean test = true;
 			if (!world.isRemote)
 				player.sendChatToPlayer(EnumColors.PURPLE + LanguageRegistry.instance().getStringLocalization("scroll.tp"));
 			return test;
 				}
 			}
 		}
 	
 	private void spamProtection(World world, int x, int y, int z, EntityPlayer player, ItemStack itemstack) {
 		NBTTagCompound tag = itemstack.getTagCompound();
 	     if (tag == null) {
 	      tag = new NBTTagCompound();
 	      itemstack.setTagCompound(tag);
 	     }
 		boolean showError = false;
 		int id = world.getBlockId(x, y, z);
 		int worldId = itemstack.getTagCompound().getInteger("worldID");
 		if (Minecraft.getSystemTime() >= itemstack.getTagCompound().getLong("time") + 1250 || (!tag.hasKey("time"))) {
 				showError = true;
 			} else {
 				showError = false;
 			}
 			if (showError == true) {
				if (worldId == (world.provider.dimensionId)) {
 					if (!world.isRemote)
 						player.sendChatToPlayer(EnumColors.DARK_RED + LanguageRegistry.instance().getStringLocalization("scroll.error.wrongDimension"));
 				} else {
 					if (!world.isRemote)
 						player.sendChatToPlayer(EnumColors.DARK_RED + LanguageRegistry.instance().getStringLocalization("scroll.error.notCreative"));
 				}
 				badEffectDraw(world, player.posX, player.posY - 1, player.posZ);
 				world.playSound(player.posX + 0.5D, player.posY + 1.0D, player.posZ + 0.5D, "random.breath", 0.5f, 2.2f, false);
 				//time = Minecraft.getSystemTime();
 				tag.setLong("time", Minecraft.getSystemTime());
 			}
 		}
 
 	/**private boolean testBlock(World world, int x, int y, int z) {
 		int id = world.getBlockId(x, y, z);
 		if (id != CustomBlocks.blockhome.blockID) {
 			return false;
 		} else {
 			boolean test = true;
 			
 			for (int i = 0; i < 4; i++) { test &= world.getBlockId(x + 2, y + i, z + 2) == Block.obsidian.blockID; test &= world.getBlockId(x - 2, y + i, z + 2) == Block.obsidian.blockID; test &= world.getBlockId(x + 2, y + i, z - 2) == Block.obsidian.blockID; test &= world.getBlockId(x - 2, y + i, z - 2) == Block.obsidian.blockID; }
 
 			return test;
 		}
 	}*/
 	
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
