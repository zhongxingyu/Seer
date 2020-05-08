 package mods.alice.villagerblock.item;
 
 import java.util.Random;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import mods.alice.villagerblock.ItemManager;
 import mods.alice.villagerblock.ModConfig;
 import mods.alice.villagerblock.block.BlockVillager;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.passive.EntityVillager;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.village.MerchantRecipeList;
 import net.minecraft.world.World;
 
 public class ItemVillagerBlockelize extends Item
 {
 	public ItemVillagerBlockelize()
 	{
 		super((int)ModConfig.idItemVillagerBlockelize);
 		setCreativeTab(CreativeTabs.tabTools);
 		setFull3D();
 		setMaxDamage(42);
 		setMaxStackSize(1);
 		setUnlocalizedName("VillagerBlockelize");
 	}
 
 	@Override
 	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
 	{
 		ItemStack playerInventory[];
 		int index;
 
 		if(player.capabilities.isCreativeMode)
 		{
 			return itemStack;
 		}
 
 		playerInventory = player.inventory.mainInventory;
 		for(index = 0; index < playerInventory.length; index++)
 		{
 			if(playerInventory[index] == null)
 			{
 				continue;
 			}
 			if(playerInventory[index].itemID == Item.emerald.itemID)
 			{
 				if(playerInventory[index].stackSize > 0)
 				{
 					playerInventory[index].stackSize--;
 					if(playerInventory[index].stackSize <= 0)
 					{
 						playerInventory[index] = null;
 					}
 					itemStack.setItemDamage(itemStack.getItemDamage() - 21);

					break;
 				}
 			}
 		}
 
 		return itemStack;
 	}
 
 	@Override
 	public boolean onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
 	{
 		Block b;
 		int damage;
 		int id;
 
 		if(world.isRemote)
 		{
 			return false;
 		}
 
 		id = world.getBlockId(x, y, z);
 		b = ItemManager.getBlock(BlockVillager.class);
 		if(id == b.blockID)
 		{
 			if(!player.capabilities.isCreativeMode)
 			{
 				damage = 2 + itemStack.getItemDamage();
 				if(damage > itemStack.getMaxDamage())
 				{
 					return false;
 				}
 			}
 
 			BlockVillager.dropTaggedBlock(world, x, y, z, true);
 			world.destroyBlock(x, y, z, false);
 
 			if(!player.capabilities.isCreativeMode)
 			{
 				itemStack.setItemDamage(itemStack.getItemDamage() + 2);
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean onLeftClickEntity(ItemStack itemStack, EntityPlayer player, Entity entity)
 	{
 		EntityItem itemDrop;
 		ItemStack newItemStack;
 		MerchantRecipeList recipes;
 		NBTTagCompound tagItemStack;
 		NBTTagCompound tagVillager;
 		Random rng;
 		int damage;
 		int prof;
 
 		if(!(entity instanceof EntityVillager))
 		{
 			return false;
 		}
 
 		if(!player.worldObj.isRemote)
 		{
 			if(!player.capabilities.isCreativeMode)
 			{
 				damage = 1 + itemStack.getItemDamage();
 				if(damage > itemStack.getMaxDamage())
 				{
 					return false;
 				}
 			}
 
 			if(((EntityVillager)entity).isChild())
 			{
 				((EntityVillager)entity).setGrowingAge(1);
 				((EntityLiving)entity).onLivingUpdate();
 			}
 
 			newItemStack = new ItemStack(ItemManager.getBlock(BlockVillager.class), 1, 0);
 
 			tagItemStack = new NBTTagCompound();
 
 			recipes = ((EntityVillager)entity).getRecipes(null);
 			tagVillager = recipes.getRecipiesAsTags();
 			tagItemStack.setCompoundTag("Trade", tagVillager);
 
 			prof = ((EntityVillager)entity).getProfession();
 			tagItemStack.setInteger("Profession", prof);
 
 			newItemStack.setTagCompound(tagItemStack);
 
 			itemDrop = new EntityItem(entity.worldObj, entity.posX, entity.posY, entity.posZ, newItemStack);
 			player.worldObj.spawnEntityInWorld(itemDrop);
 		}
 
 		rng = ((EntityVillager)entity).getRNG();
 		for(prof = 0; prof < 5; prof++)
 		{
 			player.worldObj.spawnParticle("happyVillager", entity.posX - 0.5 + rng.nextDouble() * 2, entity.posY - 0.5 + rng.nextDouble() * 2, entity.posZ - 0.5 + rng.nextDouble() * 2, 0, 0, 0);
 		}
 
 		if(!player.worldObj.isRemote)
 		{
 			entity.setDead();
 			if(!player.capabilities.isCreativeMode)
 			{
 				itemStack.setItemDamage(itemStack.getItemDamage() + 1);
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconReg)
 	{
 		itemIcon = iconReg.registerIcon("villagerblock:VillagerBlockelize");
 	}
 
 	@Override
 	public boolean shouldPassSneakingClickToBlock(World world, int x, int y, int z)
 	{
 		Block b;
 		int id;
 
 		b = ItemManager.getBlock(BlockVillager.class);
 		id = world.getBlockId(x, y, z);
 
 		return (id != b.blockID);
 	}
 }
