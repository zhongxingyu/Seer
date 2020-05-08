 package hunternif.mc.dota2items.client.gui;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.core.EntityStats;
 import hunternif.mc.dota2items.core.Mechanics;
 import net.minecraft.client.Minecraft;
 import net.minecraftforge.client.event.RenderGameOverlayEvent;
 import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
 import net.minecraftforge.common.ForgeHooks;
 import net.minecraftforge.event.ForgeSubscribe;
 
 public class GuiOnScreenStats {
 	private static final int HP_BAR_WIDTH = 81;
 	
 	private Minecraft mc;
 	
 	public GuiOnScreenStats(Minecraft mc) {
 		this.mc = mc;
 	}
 	
 	@ForgeSubscribe
 	public void onRenderExperienceBar(RenderGameOverlayEvent event) {
 		// Only interested in Post-ExperienceBar events (the end of overlay rendering)
 		if (event.isCancelable() || event.type != ElementType.EXPERIENCE || mc.thePlayer.capabilities.isCreativeMode) {
 			return;
 		}
 		int width = event.resolution.getScaledWidth();
 		int height = event.resolution.getScaledHeight();
 		
 		//TODO make text positioning configurable; add an option to show Dota-like HP and mana bars.
 		
 		EntityStats stats = Dota2Items.mechanics.getEntityStats(mc.thePlayer);
 		int left = width / 2 - 91;
 		int top = height - 39;
 		String hp = stats.getHealth(mc.thePlayer) + "/" + stats.getMaxHealth();
 		int strlen = mc.fontRenderer.getStringWidth(hp);
 		mc.fontRenderer.drawStringWithShadow(hp, left - strlen - 2, top, 0xFF1313);
 		if (Mechanics.shouldHeal(mc.thePlayer)) {
 			String hpRegen = String.format("+%.2f", stats.getHealthRegen());
 			mc.fontRenderer.drawStringWithShadow(hpRegen, left + HP_BAR_WIDTH + 1, top, 0xFF6C6C);
 		}
 		
 		int curMana = stats.getMana();
 		int maxMana = stats.getMaxMana();
 		if (maxMana > 0) {
 			boolean renderArmor = ForgeHooks.getTotalArmorValue(mc.thePlayer) > 0;
 			top = height - 49 - (renderArmor ? 10 : 0);
 			String mana = curMana + "/" + maxMana;
 			strlen = mc.fontRenderer.getStringWidth(mana);
 			mc.fontRenderer.drawStringWithShadow(mana, left - strlen - 2, top, 0x2162F8);
 			if (curMana < maxMana) {
 				String manaRegen = String.format("+%.2f", stats.getManaRegen());
 				mc.fontRenderer.drawStringWithShadow(manaRegen, left + HP_BAR_WIDTH + 1, top, 0x4893D4);
 			}
 		}
 	}
 }
