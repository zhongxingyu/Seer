 package com.qzx.au.hud;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.ScaledResolution;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 import org.lwjgl.opengl.GL11;
 
 import com.qzx.au.util.UI;
 
 @SideOnly(Side.CLIENT)
 public class ArmorHUD {
 	private UI ui = new UI();
 
 	public ArmorHUD(){}
 
 	//////////
 
 	private void drawItemStack(Minecraft mc, RenderItem itemRenderer, ItemStack itemstack, int x, int y, int quantity, boolean force_quantity){
 		if(itemstack == null) return;
 
 		GL11.glEnable(32826); // GL_RESCALE_NORMAL_EXT + GL_RESCALE_NORMAL_EXT
 		RenderHelper.enableStandardItemLighting();
 		RenderHelper.enableGUIStandardItemLighting();
 
 		itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, x, y);
 
 		this.ui.setCursor(x + ((Cfg.armor_hud_corner&1) == 0 ? 18 : -2), y + 4);
 
 		int durability_style = (Cfg.show_inspector ? Cfg.HUD_DURABILITY_VALUE : Cfg.armor_hud_durability);
 
 		// durability bar/value/percent
 		if(durability_style == Cfg.HUD_DURABILITY_BAR){
 			itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, x, y, (quantity > 1 ? "" : null));
 
 			RenderHelper.disableStandardItemLighting();
 			GL11.glDisable(32826); // GL_RESCALE_NORMAL_EXT + GL_RESCALE_NORMAL_EXT
 		} else {
 			RenderHelper.disableStandardItemLighting();
 			GL11.glDisable(32826); // GL_RESCALE_NORMAL_EXT + GL_RESCALE_NORMAL_EXT
 
 			if(itemstack.isItemStackDamageable()){
 				if(force_quantity || quantity > 1) this.ui.y -= 4;
 
				int max_durability = itemstack.getMaxDamage() + 1;
 				int durability = max_durability - itemstack.getItemDamage();
 				int percent = (int)Math.round(100.0 * (float)durability / (float)max_durability);
 				int color = (percent > 50 ? 0xaaaaaa : (percent < 25 ? 0xff6666 : 0xffff66));
 				if(durability_style == Cfg.HUD_DURABILITY_VALUE){
 					if((Cfg.armor_hud_corner&1) == 0){
 						this.ui.drawString(String.format("/%d", max_durability), 0xaaaaaa);
 						this.ui.drawString(String.format("%d", durability), color);
 					} else {
 						this.ui.drawStringRight(String.format("/%d", max_durability), 0xaaaaaa);
 						this.ui.drawStringRight(String.format("%d", durability), color);
 					}
 				} else if(durability_style == Cfg.HUD_DURABILITY_PERCENT){
 					if((Cfg.armor_hud_corner&1) == 0)
 						this.ui.drawString(String.format("%d%%", percent), color);
 					else
 						this.ui.drawStringRight(String.format("%d%%", percent), color);
 				}
 				this.ui.lineBreak(9);
 			}
 		}
 
 		// quantity below durability value/percent
 		if(force_quantity || quantity > 1){
 			if((Cfg.armor_hud_corner&1) == 0)
 				this.ui.drawString(String.format("%d", quantity), (quantity > 0 ? 0xffffff : 0xff6666));
 			else
 				this.ui.drawStringRight(String.format("%d", quantity), (quantity > 0 ? 0xffffff : 0xff6666));
 		}
 	}
 
 	private int countItemsInInventory(EntityPlayer player, int itemID, int itemDamage){
 		int nr_items = 0;
 		for(int i = 0; i < 36; i++){
 			ItemStack item = player.inventory.mainInventory[i];
 			if(item != null)
 				if(item.itemID == itemID && item.getItemDamage() == itemDamage)
 					nr_items += item.stackSize;
 		}
 		return nr_items;
 	}
 
 	//////////
 
 	public void draw(Minecraft mc, ScaledResolution screen, EntityPlayer player){
 		GL11.glPushMatrix();
 
 		RenderItem itemRenderer = new RenderItem();
 		itemRenderer.zLevel = 200.0F;
 
 		ItemStack helmet = mc.thePlayer.getCurrentArmor(3);
 		ItemStack chest = mc.thePlayer.getCurrentArmor(2);
 		ItemStack pants = mc.thePlayer.getCurrentArmor(1);
 		ItemStack boots = mc.thePlayer.getCurrentArmor(0);
 		ItemStack hand = mc.thePlayer.getHeldItem();
 		ItemStack ammo = null;
 
 		int width = screen.getScaledWidth();
 		int height = screen.getScaledHeight();
 		int x = ((Cfg.armor_hud_corner&1) == 0 ? Cfg.armor_hud_x :  width-16-Cfg.armor_hud_x);
 		int y = ((Cfg.armor_hud_corner&2) == 0 ? Cfg.armor_hud_y : height-80-Cfg.armor_hud_y);
 
 		int nr_hand = 0, nr_ammo = -1;
 		if(hand != null){
 			nr_hand = (hand.getMaxStackSize() > 1 ? countItemsInInventory(player, hand.itemID, hand.getItemDamage()) : 1);
 
 			// bow ammo
 			if(hand.itemID == Item.bow.itemID){
 				nr_ammo = countItemsInInventory(player, Item.arrow.itemID, 0);
 				ammo = new ItemStack(Item.arrow);
 			}
 
 			// move HUD up if at bottom and has ammo
 			if(ammo != null) y -= ((Cfg.armor_hud_corner&2) == 0 ? 0 : 16);
 		}
 
 		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 
 		this.drawItemStack(mc, itemRenderer, helmet, x, y, 0, false);
 		this.drawItemStack(mc, itemRenderer, chest, x, y+16, 0, false);
 		this.drawItemStack(mc, itemRenderer, pants, x, y+32, 0, false);
 		this.drawItemStack(mc, itemRenderer, boots, x, y+48, 0, false);
 		this.drawItemStack(mc, itemRenderer, hand, x, y+64, nr_hand, false);
 		this.drawItemStack(mc, itemRenderer, ammo, x, y+80, nr_ammo, true);
 
 		GL11.glPopMatrix();
 	}
 }
