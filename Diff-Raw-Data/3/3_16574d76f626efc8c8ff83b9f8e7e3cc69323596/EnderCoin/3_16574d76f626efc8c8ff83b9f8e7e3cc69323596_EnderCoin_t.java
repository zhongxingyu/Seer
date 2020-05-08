 package com.github.soniex2.endermoney.core.item;
 
 import java.math.BigInteger;
 import java.util.List;
 import java.util.Random;
 
 import com.github.soniex2.endermoney.core.EnderMoney;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.Event;
 import net.minecraftforge.event.entity.player.FillBucketEvent;
 import net.minecraftforge.fluids.FluidStack;
 import net.minecraftforge.fluids.IFluidContainerItem;
 
 public class EnderCoin extends Item implements IFluidContainerItem {
 
 	private long capacity = Long.MAX_VALUE;
 
 	private Random rand = new Random();
 
 	public EnderCoin(int id) {
 		super(id);
 		setMaxStackSize(64);
 		setCreativeTab(EnderMoney.tab);
 		setUnlocalizedName("endercoin");
 		this.setHasSubtypes(true);
 		this.func_111206_d("EnderCoin");
 	}
 
 	public ItemStack getItemStack(long value) {
 		ItemStack is = new ItemStack(this, 1, 0);
 		NBTTagCompound tag = new NBTTagCompound("tag");
 		tag.setLong("value", value);
 		is.setTagCompound(tag);
 		return is;
 	}
 
 	@Override
 	public int getColorFromItemStack(ItemStack is, int pass) {
 		NBTTagCompound tag = is.getTagCompound();
 		if (tag == null) {
 			tag = new NBTTagCompound("tag");
 			tag.setLong("value", 0);
 			is.setTagCompound(tag);
 		}
 		long v = tag.getLong("value");
 		rand.setSeed(Long.valueOf(v).hashCode());
 		int r = 255 - rand.nextInt(224) & 255;
 		int g = 255 - rand.nextInt(224) & 255;
 		int b = 255 - rand.nextInt(224) & 255;
 		return r * 0x10000 | g * 0x100 | b;
 	}
 
 	@Override
 	public String getItemDisplayName(ItemStack is) {
 		NBTTagCompound tag = is.getTagCompound();
 		if (tag == null) {
 			tag = new NBTTagCompound("tag");
 			tag.setLong("value", 0);
 			is.setTagCompound(tag);
 		}
 		long v = tag.getLong("value");
 		return "$" + v + " EnderCoin";
 	}
 
 	@Override
 	public void registerIcons(IconRegister ireg) {
 		itemIcon = ireg.registerIcon("endermoneycore:coin");
 	}
 
 	public static long getValueFromItemStack(ItemStack is) {
 		if (!(is.getItem() instanceof EnderCoin))
 			throw new IllegalArgumentException(is.getItem().getItemDisplayName(is)
 					+ " is not a valid item for method EnderCoin.getValueFromItemStack");
 		NBTTagCompound tag = is.getTagCompound();
 		if (tag == null) {
 			tag = new NBTTagCompound("tag");
 			tag.setLong("value", 0);
 			is.setTagCompound(tag);
 		}
 		return tag.getLong("value");
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public void getSubItems(int id, CreativeTabs tab, List list) {
 		for (int x = 0; x <= 63; x++) {
 			if (x != 63) {
 				list.add(getItemStack(BigInteger.valueOf(2).pow(x).longValue()));
 			} else {
 				list.add(getItemStack(Long.MAX_VALUE));
 			}
 		}
 	}
 
 	public ItemStack getItemStack(long value, int amount) {
 		ItemStack is = getItemStack(value);
 		is.stackSize = amount;
 		return is;
 	}
 
 	@Override
 	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) {
 		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world,
 				player, true);
 
 		if (movingobjectposition == null) {
 			return item;
 		} else {
 			FillBucketEvent event = new FillBucketEvent(player, item, world, movingobjectposition);
 			if (MinecraftForge.EVENT_BUS.post(event)) {
 				return item;
 			}
 
 			if (event.getResult() == Event.Result.ALLOW) {
 				if (player.capabilities.isCreativeMode) {
 					return item;
 				}
 
 				if (--item.stackSize <= 0) {
 					return event.result;
 				}
 
 				if (!player.inventory.addItemStackToInventory(event.result)) {
 					player.dropPlayerItem(event.result);
 				}
 
 				return item;
 			}
 
 			if (movingobjectposition.typeOfHit == EnumMovingObjectType.TILE) {
 				int x = movingobjectposition.blockX;
 				int y = movingobjectposition.blockY;
 				int z = movingobjectposition.blockZ;
 
 				if (!world.canMineBlock(player, x, y, z)) {
 					return item;
 				}
 
 				if (movingobjectposition.sideHit == 0) {
 					--y;
 				}
 
 				if (movingobjectposition.sideHit == 1) {
 					++y;
 				}
 
 				if (movingobjectposition.sideHit == 2) {
 					--z;
 				}
 
 				if (movingobjectposition.sideHit == 3) {
 					++z;
 				}
 
 				if (movingobjectposition.sideHit == 4) {
 					--x;
 				}
 
 				if (movingobjectposition.sideHit == 5) {
 					++x;
 				}
 
 				if (!player.canPlayerEdit(x, y, z, movingobjectposition.sideHit, item)) {
 					return item;
 				}
 
 				if (this.tryPlaceContainedLiquid(world, x, y, z)
 						&& !player.capabilities.isCreativeMode) {
 					long value = getValueFromItemStack(item);
 					if (value - 1 > 0) {
 						if (item.stackSize - 1 <= 0) {
 							return getItemStack(value - 1);
 						}
 						ItemStack newItem = getItemStack(value - 1);
 						if (!player.inventory.addItemStackToInventory(newItem)) {
 							player.dropPlayerItem(newItem);
 						}
 					}
 					item.stackSize -= 1;
 					return item;
 				}
 
 			}
 
 			return item;
 		}
 	}
 
 	public boolean tryPlaceContainedLiquid(World par1World, int par2, int par3, int par4) {
 		if (EnderMoney.blockLiqEC.blockID <= 0) {
 			return false;
 		} else {
 			Material material = par1World.getBlockMaterial(par2, par3, par4);
 			boolean flag = !material.isSolid();
 
 			if (!par1World.isAirBlock(par2, par3, par4) && !flag) {
 				return false;
 			} else {
 				if (!par1World.isRemote && flag && !material.isLiquid()) {
 					par1World.destroyBlock(par2, par3, par4, true);
				} else if (!par1World.isRemote && flag && material.isLiquid()
						&& par1World.getBlockMetadata(par2, par3, par4) == 0) {
 					return false;
 				}
 				par1World.setBlock(par2, par3, par4, EnderMoney.blockLiqEC.blockID, 0, 3);
 				return true;
 			}
 		}
 	}
 
 	@Override
 	public FluidStack getFluid(ItemStack container) {
 		long value = getValueFromItemStack(container);
 		return new FluidStack(EnderMoney.fluidEC, value > Integer.MAX_VALUE ? Integer.MAX_VALUE
 				: (int) value);
 	}
 
 	@Override
 	public int getCapacity(ItemStack container) {
 		return Integer.MAX_VALUE;
 	}
 
 	@Override
 	public int fill(ItemStack container, FluidStack resource, boolean doFill) {
 		if (resource == null) return 0;
 		if (!doFill) {
 			if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("value"))
 				return resource.amount;
 			if (resource.getFluid() != EnderMoney.fluidEC) return 0;
 			long amount = getValueFromItemStack(container);
 			return Math.min((capacity - amount > Integer.MAX_VALUE ? Integer.MAX_VALUE
 					: (int) (capacity - amount)), resource.amount);
 		}
 		if (resource.getFluid() != EnderMoney.fluidEC) return 0;
 		if (container.stackTagCompound == null) container.stackTagCompound = new NBTTagCompound();
 		if (!container.stackTagCompound.hasKey("value")) {
 			container.stackTagCompound.setLong("value", resource.amount);
 			return resource.amount;
 		}
 		long amount = getValueFromItemStack(container);
 		if (resource.amount + amount < 0) {
 			container.stackTagCompound.setLong("value", capacity);
 			return (int) ~(resource.amount + amount);
 		}
 		container.stackTagCompound.setLong("value", amount + resource.amount);
 		return resource.amount;
 	}
 
 	@Override
 	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
 		if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("value"))
 			return null;
 		FluidStack stack = getFluid(container);
 		if (stack == null) return null;
 		stack.amount = Math.min(stack.amount, maxDrain);
 		if (doDrain)
 			container.stackTagCompound
 					.setLong("value", getValueFromItemStack(container) - maxDrain);
 		return stack;
 	}
 }
