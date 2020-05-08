 package bstramke.NetherStuffs.Items;
 
 import java.util.List;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemSword;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.util.DamageSource;
 import bstramke.NetherStuffs.Common.CommonProxy;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class NetherSword extends ItemSword {
 	public static enum Types {
 		hellfire(1), acid(2), death(3), undefined(0);
 		private final int value;
 
 		private Types(int value) {
 			this.value = value;
 		}
 
 		public int getValue() {
 			return value;
 		}
 	}
 
 	@Override
 	public String getTextureFile() {
 		return CommonProxy.ITEMS_PNG;
 	}
 
 	protected Types nType = Types.undefined;
 
 	public NetherSword(int par1, EnumToolMaterial par2EnumToolMaterial) {
 		super(par1, par2EnumToolMaterial);
 		this.nType = Types.undefined;
 	}
 
 	public NetherSword(int par1, EnumToolMaterial par2EnumToolMaterial, Types nType) {
 		super(par1, par2EnumToolMaterial);
 		this.nType = nType;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean hasEffect(ItemStack par1ItemStack) {
 		if (this.nType != Types.undefined)
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public int getDamageVsEntity(Entity par1Entity) {
		if (this.nType == Types.acid) {
			((EntityLiving) par1Entity).addPotionEffect(new PotionEffect(Potion.wither.id, 20 * 5, 4));
		} else if (this.nType == Types.death) {
			((EntityLiving) par1Entity).addPotionEffect(new PotionEffect(Potion.hunger.id, 20 * 60, 8));
			((EntityLiving) par1Entity).addPotionEffect(new PotionEffect(Potion.poison.id, 20 * 5, 8));
		} else if (this.nType == Types.hellfire) {
			((EntityLiving) par1Entity).attackEntityFrom(DamageSource.lava, 2);
			((EntityLiving) par1Entity).setFire(8);
 		}

 		return super.getDamageVsEntity(par1Entity);
 	}
 
 	@Override
 	public boolean hitEntity(ItemStack par1ItemStack, EntityLiving par2EntityLiving, EntityLiving par3EntityLiving) {
 		super.hitEntity(par1ItemStack, par2EntityLiving, par3EntityLiving);
 		// par3EntityLiving == attacking entity
 		// par2EntityLiving == attacked entity
 
 		if (!par3EntityLiving.worldObj.isRemote && this.nType != Types.undefined) {
 			InventoryPlayer inventoryPlayer = ((EntityPlayer) par3EntityLiving).inventory;
 			int nAmountToAdd = (getDamageVsEntity(par2EntityLiving) / 2) * 10;
 			for (int i = 0; i < inventoryPlayer.mainInventory.length && nAmountToAdd > 0; i++) {
 				if (inventoryPlayer.mainInventory[i] != null && inventoryPlayer.mainInventory[i].itemID == new ItemStack(NetherItems.SoulEnergyBottle.itemID, 1, 0).itemID) {
 					nAmountToAdd = SoulEnergyBottle.addSoulEnergy(nAmountToAdd, inventoryPlayer.mainInventory[i]);
 				}
 			}
 		}
 		return true;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
 		if (this.nType == Types.acid) {
 			par3List.add("Acid Blade");
 		} else if (this.nType == Types.death) {
 			par3List.add("Death Blade");
 		} else if (this.nType == Types.hellfire) {
 			par3List.add("Hellfire Blade");
 		}
 	}
 }
