 package sammko.quantumCraft.client;
 
 import java.util.EnumSet;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.item.ItemStack;
 import net.minecraft.src.ModLoader;
 import sammko.quantumCraft.core.QuantumCraftSettings;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ClientTickHandler implements ITickHandler {
 	String currDamage = "";
 
 	public EnumSet ticks() {
 
		return EnumSet.of(TickType.CLIENT);
 	}
 
 	public String getItemDamage(ItemStack item) {
 		if (FMLCommonHandler.instance().getEffectiveSide().isClient() == true) {
 			Minecraft mc = FMLClientHandler.instance().getClient();
 			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT
 					&& mc.inGameHasFocus == true
 					&& mc.thePlayer.inventory.getCurrentItem() != null
 					&& mc.thePlayer.inventory.getCurrentItem().itemID == QuantumCraftSettings.CrystalPickaxeID + 256) {
 
 				final int damage = item.getItemDamageForDisplay();
 				final int damageLeft = 500 - damage;
 				String damageString = null;
 				if (damageLeft >= 300) {
 					damageString = "§a" + damageLeft + "/500";
 				} else if (damageLeft >= 100) {
 					damageString = "§e" + damageLeft + "/500";
 				} else if (damageLeft >= 50) {
 					damageString = "§5" + damageLeft + "/500";
 				}
 				return damageString;
 			} else {
 				return "";
 			}
 		} else
 			return "";
 
 	}
 
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {
 
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 		if (FMLCommonHandler.instance().getEffectiveSide().isClient() == true) {
 			Minecraft mc = FMLClientHandler.instance().getClient();
 			if (mc.inGameHasFocus == true
 					&& FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
 				ItemStack itemstack = mc.thePlayer.inventory.getCurrentItem();
 				mc.fontRenderer.drawString(currDamage, 1, 1, 1);
 				// mc.fontRenderer.drawString("test", 1, 1, 1);
 
 				currDamage = getItemDamage(itemstack);
 
 			}
 		}
 
 	}
 
 	@Override
 	public String getLabel() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
