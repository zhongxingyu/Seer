 package hunternif.mc.dota2items.client.gui;
 
 import hunternif.mc.dota2items.Dota2Items;
import hunternif.mc.dota2items.config.Config;
 import hunternif.mc.dota2items.core.EntityStats;
 import hunternif.mc.dota2items.core.Mechanics;
 
 import java.util.Set;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.attributes.AttributeModifier;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.ResourceLocation;
 
 import org.lwjgl.opengl.GL11;
 
 import com.google.common.collect.HashMultimap;
 
 public class GuiDotaStats {
 	private static final ResourceLocation texture = new ResourceLocation(Dota2Items.ID+":textures/gui/stats.png");
 	
 	public static final int WIDTH = 113;
 	public static final int HEIGHT = 60;
 	private static final int COLOR_REGULAR = 0xffffff;
 	private static final int COLOR_BONUS = 0x26B117;
 	
 	public void render() {
 		Minecraft mc = Minecraft.getMinecraft();
 		// Show stats when the inventory is open:
 		if (!(mc.currentScreen instanceof GuiContainer)) {
 			return;
 		}
 		int left = 0;
 		int top = mc.currentScreen.height - HEIGHT;
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		GL11.glDisable(GL11.GL_LIGHTING);
 		mc.renderEngine.func_110577_a(texture);
 		Tessellator tessellator = Tessellator.instance;
 		tessellator.startDrawingQuads();
 		tessellator.addVertexWithUV(left+WIDTH, top+HEIGHT, 0, 1, 1);
 		tessellator.addVertexWithUV(left+WIDTH, top, 0, 1, 0);
 		tessellator.addVertexWithUV(left, top, 0, 0, 0);
 		tessellator.addVertexWithUV(left, top+HEIGHT, 0, 0, 1);
 		tessellator.draw();
 		
 		EntityStats stats = Dota2Items.mechanics.getEntityStats(mc.thePlayer);
 		StringBuilder sb = new StringBuilder();
 		
 		float baseDmg = 1;
 		boolean isMelee = true;
 		ItemStack item = mc.thePlayer.getCurrentEquippedItem();
 		if (item != null) {
 			// Get player damage output. Not accounting for enchantments yet!
 			HashMultimap map = (HashMultimap)item.func_111283_C();
 			Set<AttributeModifier> damageModifierSet = map.get(SharedMonsterAttributes.field_111264_e.func_111108_a());
 			for (AttributeModifier modifier : damageModifierSet) {
 				baseDmg += (float) modifier.func_111164_d();
 			}
 			// The following line works correctly on the "server" side, but always returns 1 on the "client".
 			//baseDmg = (float)mc.thePlayer.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111126_e();
 			if (item.itemID == Item.bow.itemID) {
 				isMelee = false;
 				// Assume the damage of an arrow at average charge: 6
 				if (mc.thePlayer.inventory.hasItem(Item.arrow.itemID)) {
 					baseDmg = 6;
 				}
			} else if (item.itemID == Config.daedalus.getID()) {
				isMelee = false;
				if (mc.thePlayer.inventory.hasItem(Item.arrow.itemID)) {
					baseDmg = 10; // maximum arrow damage
				}
 			}
 		}
 		baseDmg *= Mechanics.DOTA_VS_MINECRAFT_DAMAGE;
 		float improvedDmg = stats.getDamage(baseDmg, isMelee);
 		int baseValue = MathHelper.floor_float(baseDmg);
 		int bonus = MathHelper.floor_float(improvedDmg) - baseValue;
 		formatStat(sb, baseValue, bonus);
 		int strlen = mc.fontRenderer.getStringWidth(sb.toString());
 		mc.fontRenderer.drawString(sb.toString(), left + 32 - strlen/2, top + 8, COLOR_REGULAR);
 		
 		sb = new StringBuilder();
 		baseValue = stats.baseArmor;
 		bonus = stats.getArmor(baseValue);
 		formatStat(sb, baseValue, bonus);
 		strlen = mc.fontRenderer.getStringWidth(sb.toString());
 		mc.fontRenderer.drawString(sb.toString(), left + 18 - strlen/2, top + 44, COLOR_REGULAR);
 		
 		String ms = String.valueOf(stats.getDotaMovementSpeed());
 		strlen = mc.fontRenderer.getStringWidth(ms);
 		mc.fontRenderer.drawString(ms, left + 45 - strlen/2, top + 44, COLOR_REGULAR);
 		
 		sb = new StringBuilder();
 		baseValue = stats.baseStrength;
 		bonus = stats.getStrength();
 		formatStat(sb, baseValue, bonus);
 		strlen = mc.fontRenderer.getStringWidth(sb.toString());
 		mc.fontRenderer.drawString(sb.toString(), left + 93 - strlen/2, top + 11, COLOR_REGULAR);
 		
 		sb = new StringBuilder();
 		baseValue = stats.baseAgility;
 		bonus = stats.getAgility();
 		formatStat(sb, baseValue, bonus);
 		strlen = mc.fontRenderer.getStringWidth(sb.toString());
 		mc.fontRenderer.drawString(sb.toString(), left + 93 - strlen/2, top + 27, COLOR_REGULAR);
 		
 		sb = new StringBuilder();
 		baseValue = stats.baseIntelligence;
 		bonus = stats.getIntelligence();
 		formatStat(sb, baseValue, bonus);
 		strlen = mc.fontRenderer.getStringWidth(sb.toString());
 		mc.fontRenderer.drawString(sb.toString(), left + 93 - strlen/2, top + 44, COLOR_REGULAR);
 	}
 	
 	private static void formatStat(StringBuilder sb, int baseValue, int bonus) {
 		if (baseValue == 0) {
 			if (bonus == 0) {
 				sb.append("0");
 			}
 		} else {
 			sb.append(baseValue);
 		}
 		if (bonus > 0) {
 			sb.append(EnumChatFormatting.GREEN.toString()).append("+").append(bonus);
 		} else if (bonus < 0) {
 			sb.append(EnumChatFormatting.RED.toString()).append(bonus);
 		}
 	}
 }
