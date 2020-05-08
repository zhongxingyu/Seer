 package sammko.quantumCraft.client;
 
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 import java.util.EnumSet;
 
 import sammko.quantumCraft.core.QuantumCraftSettings;
 import sammko.quantumCraft.items.ItemInitializator;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.entity.EntityClientPlayerMP;
 import net.minecraft.client.gui.FontRenderer;
 import net.minecraft.client.gui.GuiIngame;
 import net.minecraft.client.gui.ScaledResolution;
 import net.minecraft.client.settings.GameSettings;
 import net.minecraft.client.settings.KeyBinding;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.entity.player.PlayerCapabilities;
 import net.minecraft.item.ItemStack;
 import net.minecraft.src.ModLoader;
 
 public class ClientTickHandler implements ITickHandler {
 
 	public static Minecraft mc = ModLoader.getMinecraftInstance();
 
 	public EnumSet ticks() {
 		
 		return EnumSet.of(TickType.WORLD, TickType.WORLDLOAD, TickType.CLIENT,
 				TickType.RENDER);
 	}
 	
 	public String getItemDamage(ItemStack item) {
 		if (mc.inGameHasFocus == true && mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().itemID == QuantumCraftSettings.CrystalPickaxeID + 256 ) {
 			final int damage = item.getItemDamageForDisplay();
 			final int damageLeft = 500-damage;
 			String damageString = null;
 			if (damageLeft >= 300){
 				damageString = "a" + damageLeft + "/500";
 			}
 			else if (damageLeft >= 100){
 				damageString = "e" + damageLeft + "/500";
 			}
 			else if (damageLeft >= 50){
 				damageString = "5" + damageLeft + "/500";
 			}
 			return damageString;
 		}else{
			return "error";
 		}
 	}
 
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {
 	
 
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 		if (mc.inGameHasFocus == true) {
 			ItemStack itemstack = mc.thePlayer.inventory.getCurrentItem();
 			mc.fontRenderer.drawString(getItemDamage(itemstack), 1, 1, 1);
 			//mc.fontRenderer.drawString("test", 1, 1, 1);
 		}
 
 	}
 
 	@Override
 	public String getLabel() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
