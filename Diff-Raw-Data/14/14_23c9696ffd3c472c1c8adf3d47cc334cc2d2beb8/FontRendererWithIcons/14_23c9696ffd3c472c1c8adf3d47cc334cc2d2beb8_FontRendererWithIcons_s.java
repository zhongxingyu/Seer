 package hunternif.mc.dota2items.client.gui;
 
 import hunternif.mc.dota2items.Dota2Items;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import net.minecraft.client.gui.FontRenderer;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.texture.TextureManager;
 import net.minecraft.client.settings.GameSettings;
 import net.minecraft.util.ResourceLocation;
 
 import org.lwjgl.opengl.GL11;
 
 public class FontRendererWithIcons extends FontRenderer {
 	private TextureManager textureManager;
 	private List<IconInText> icons = new ArrayList<IconInText>();
 	
 	public void registerIcon(IconInText icon) {
 		for (IconInText curIcon : icons) {
 			if (curIcon.key.equals(icon.key)) {
 				Dota2Items.logger.warning("Tried to register icon with the same key: " + icon.key);
 				return;
 			}
 		}
 		icons.add(icon);
 	}
 
 	public FontRendererWithIcons(GameSettings settings, ResourceLocation resourceLocation,
 			TextureManager textureManager, boolean unicodeFlag) {
 		super(settings, resourceLocation, textureManager, unicodeFlag);
 		this.textureManager = textureManager;
 	}
 	
 	private static class IconInTextPos {
 		public int posX;
 		public IconInText icon;
 	}
 	private static final IconInTextComparator iconInTextComparator = new IconInTextComparator();
 	private static class IconInTextComparator implements Comparator<IconInTextPos> {
 		@Override
 		public int compare(IconInTextPos o1, IconInTextPos o2) {
 			return o1.posX < o2.posX ? -1 : (o1.posX > o2.posX ? 1 : 0);
 		}
 	}
 	@Override
 	public int drawString(String text, int x, int y, int color, boolean dropShadow) {
 		List<IconInTextPos> iconsFound = new ArrayList<IconInTextPos>();
 		for (IconInText icon : icons) {
 			if (text.contains(icon.key)) {
 				for (int i = 0; i < text.length(); i++) {
 					if (text.startsWith(icon.key, i)) {
 						IconInTextPos iconInText = new IconInTextPos();
 						iconInText.icon = icon;
 						iconInText.posX = i;
 						iconsFound.add(iconInText);
 						i += icon.key.length() - 1;
 					}
 				}
 			}
 		}
 		Collections.sort(iconsFound, iconInTextComparator);
 		int posX = 0;
 		for (IconInTextPos iconInText : iconsFound) {
 			String lastTextChunk = text.substring(posX, iconInText.posX);
 			posX = iconInText.posX + iconInText.icon.key.length();
			x = super.drawString(lastTextChunk, x, y, color, dropShadow) + iconInText.icon.kerning;
 			renderIcon(iconInText.icon, x, y);
 			x += iconInText.icon.width + iconInText.icon.kerning;
 		}
 		if (posX < text.length()) {
 			String veryLastTextChunk = text.substring(posX);
 			x = super.drawString(veryLastTextChunk, x, y, color, dropShadow);
 		}
 		return x;
 	}
 	
 	@Override
 	public int getStringWidth(String text) {
 		int totalWidth = super.getStringWidth(text);
 		for (IconInText icon : icons) {
 			if (text.contains(icon.key)) {
 				int keyWidth = super.getStringWidth(icon.key);
 				while (true) {
 					int i = text.indexOf(icon.key);
 					if (i != -1) {
 						String beginning = text.substring(0, i);
 						String end = text.substring(i + icon.key.length());
 						text = beginning + end;
						totalWidth = totalWidth - keyWidth + icon.width + icon.kerning;
 					} else {
 						break;
 					}
 				}
 			}
 		}
 		return totalWidth;
 	}
 	
 	private void renderIcon(IconInText icon, int x, int y) {
 		x += icon.xOffset;
 		y += icon.yOffset;
 		GL11.glColor4f(1, 1, 1, 1);
 		this.textureManager.bindTexture(icon.texture);
 		Tessellator tessellator = Tessellator.instance;
 		tessellator.startDrawingQuads();
 		tessellator.addVertexWithUV(x, y+icon.height, 0, icon.getMinU(), icon.getMaxV());
 		tessellator.addVertexWithUV(x+icon.width, y+icon.height, 0, icon.getMaxU(), icon.getMaxV());
 		tessellator.addVertexWithUV(x+icon.width, y, 0, icon.getMaxU(), icon.getMinV());
 		tessellator.addVertexWithUV(x, y, 0, icon.getMinU(), icon.getMinV());
 		tessellator.draw();
 	}
 }
