 package assets.tacotek.Items;
 
 import ic2.api.item.IMetalArmor;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 import assets.tacotek.common.IDsHelper;
 import assets.tacotek.common.tacotek;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ShieldArmor extends ItemArmor implements IMetalArmor {
 
 	public static boolean isOn = false;
 	private static int powerToUse = 100;
 	
 	public ShieldArmor(int par1, EnumArmorMaterial par2EnumArmorMaterial, int par3, int par4, String unlocalizedName) {
 		super(par1, par2EnumArmorMaterial, par3, par4);
 		this.setCreativeTab(tacotek.tacotekTab);
 		this.setUnlocalizedName(unlocalizedName);
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public String getArmorTexture(ItemStack itemstack, Entity entity, int slot, int layer) {
 		if (itemstack.itemID == IDsHelper.shield_chestID + 256) {
 			return tacotek.modID + ":" + "textures/models/armor/shield_1.png";
 		}
 		else return null;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister reg) {
 		if (this == ItemsHelper.shield_chest) {
 			this.itemIcon = reg.registerIcon(tacotek.modID + ":" + this.getUnlocalizedName());
 		}
 	}
 	
 	public boolean tickArmor(ItemStack stack, Player player) {
 		//Current assumptions: at least 5 seconds (100 ticks) have passed.
 		if (!isOn)
 			return false;
		//if (canTakeDamage(stack, powerToUse)) {
			//damage(stack, powerToUse, player);
 			//apply potion effect for 5.1 seconds (102 ticks)
		//}
 		return false;
 	}
 
 	@Override
 	public boolean isMetalArmor(ItemStack itemstack, EntityPlayer player) {
 		return true;
 	}
 }
