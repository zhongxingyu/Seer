 package com.qzx.au.core;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.item.ItemStack;
 #ifndef MC147
 import net.minecraft.util.Icon;
 #endif
 #if !defined MC147 && !defined MC152
 import net.minecraft.util.ResourceLocation;
 #endif
 
 import org.lwjgl.opengl.GL11;
 
 public class UI {
 	private Minecraft mc = Minecraft.getMinecraft();
 	private float scale = 1.0F;
 	private int line_height = 10;
 	private int base_x = 0;
 	private int x = 0;
 	private int y = 0;
 
 	public UI(){}
 
 	//////////
 
 	public void scale(float scale){
 		this.scale = scale;
 		GL11.glScalef(scale, scale, 1.0F);
 	}
 	public void unscale(){
 		// GL11.glPopMatrix() also takes care of this
 		GL11.glScalef(1.0F / this.scale, 1.0F / this.scale, 1.0F);
 		this.scale = 1.0F;
 	}
 
 	public int scaleValue(float value){
 		return Math.round(value / this.scale);
 	}
 	public int unscaleValue(float value){
 		return Math.round(value * this.scale);
 	}
 
 	//////////
 
 	public void setCursor(int x, int y){
 		this.base_x = Math.round((float)x / this.scale);
 		this.x = Math.round((float)x / this.scale);
 		this.y = Math.round((float)y / this.scale);
 	}
 	public void resetX(){
 		this.x = this.base_x;
 	}
 	public int getBaseX(){
 		return Math.round((float)this.base_x * this.scale); // unscale
 	}
 	public void setBaseX(int x){
 		this.base_x = Math.round((float)x / this.scale);
 	}
 	public int getScaledX(){
 		return this.x;
 	}
 	public int getX(){
 		return Math.round((float)this.x * this.scale); // unscale
 	}
 	public void setX(int x){
 		this.x = Math.round((float)x / this.scale);
 	}
 	public int getScaledY(){
 		return this.y;
 	}
 	public int getY(){
 		return Math.round((float)this.y * this.scale); // unscale
 	}
 	public void setY(int y){
 		this.y = Math.round((float)y / this.scale);
 	}
 
 	public void setLineHeight(int height){
 		this.line_height = height;
 	}
 
 	public void lineBreak(){
 		this.x = this.base_x;
 		this.y += this.line_height;
 	}
 	public void lineBreak(int height){
 		this.x = this.base_x;
 		this.y += height;
 	}
 
 	public void drawSpace(int width){
 		this.x += width;
 	}
 	public void drawVSpace(int height){
 		this.y += height;
 	}
 
 	public static int ALIGN_LEFT = 0;
 	public static int ALIGN_RIGHT = 1;
 	public static int ALIGN_CENTER = 2;
 
 	//////////
 
 	public int drawString(int align, String s, int color, int box_width){
 		if(s == null) return this.getX(); // unscale
 
 		if(align == ALIGN_LEFT){
 			// cursor moved to right side of string
 			this.mc.fontRenderer.drawStringWithShadow(s, this.x, this.y, color);
 			this.x += this.mc.fontRenderer.getStringWidth(s);
 			return this.getX(); // right side of string - unscale
 		}
 		if(align == ALIGN_RIGHT){
 			// cursor moved to left side of string
 			this.x -= (this.mc.fontRenderer.getStringWidth(s) + box_width);
 			this.mc.fontRenderer.drawStringWithShadow(s, this.x, this.y, color);
 			return this.getX(); // left side of string - unscale
 		}
 		if(align == ALIGN_CENTER){
 			// cursor moved to right side of box_width
 			int x = this.x + (box_width/2 - this.mc.fontRenderer.getStringWidth(s)/2);
 			this.mc.fontRenderer.drawStringWithShadow(s, x, this.y, color);
 			this.x += box_width;
 			return Math.round((float)x * this.scale); // left side of string - unscale
 		}
 
 		return this.getX(); // unscale
 	}
 
 	public int drawString(String s, int color){
 		return this.drawString(UI.ALIGN_LEFT, s, color, 0);
 	}
 
 	// draw s2 if s1 is null or empty
 	public int drawString(int align, String s1, String s2, int color, int box_width){
 		if(s1 == null)
 			return this.drawString(align, s2, color, box_width);
 		if(s1.equals(""))
 			return this.drawString(align, s2, color, box_width);
 		return this.drawString(align, s1, color, box_width);
 	}
 
 	// draw s2 if s1 is null or empty
 	public int drawString(String s1, String s2, int color){
 		return this.drawString(UI.ALIGN_LEFT, s1, s2, color, 0);
 	}
 
 	//////////
 
 	public Button newButton(int align, int id, String s, int width, int height, int box_width){
 		Button b;
 
 		if(align == ALIGN_LEFT){
 			// cursor moved to right side of button
 			b = new Button(id, this.x, this.y, width, height, s);
 			this.x += width;
 		} else if(align == ALIGN_RIGHT){
 			// cursor moved to left side of button
 			this.x = this.base_x + (box_width - width);
 			b = new Button(id, this.x, this.y, width, height, s);
 		} else { // ALIGN_CENTER
 			// cursor moved to right side of button
 			this.x = this.base_x + (box_width/2 - width/2);
 			b = new Button(id, this.x, this.y, width, height, s);
 			this.x += width;
 		}
 
 		return b;
 	}
 
 	//////////
 
 	#if !defined MC147 && !defined MC152
 	public TextField newTextField(String s, int max_length, int width, int style){
 		TextField t = new TextField(this.mc.fontRenderer, this.x, this.y, width, style);
 		t.setMaxStringLength(max_length);
		t.setText(s);
 		this.x += width;
 		return t;
 	}
 	#endif
 
 	//////////
 
 	public static void drawBorder(int x, int y, int width, int height, int colorTL, int colorBR, int colorCorner){
 		UI.drawPixel(x+width-1, y, colorCorner); // UR corner
 		UI.drawPixel(x, y+height-1, colorCorner); // BL corner
 		UI.drawLineH(x+1, y, width-2, colorTL); // T border
 		UI.drawLineV(x, y, height-1, colorTL); // L border
 		UI.drawLineH(x+1, y+height-1, width-2, colorBR); // B border
 		UI.drawLineV(x+width-1, y+1, height-1, colorBR); // R border
 	}
 
 	public static void drawPixel(int x, int y, int color){
 		UI.drawRect(x, y, 1, 1, color);
 	}
 
 	public static void drawLineH(int x, int y, int width, int color){
 		UI.drawRect(x, y, width, 1, color);
 	}
 
 	public static void drawLineV(int x, int y, int height, int color){
 		UI.drawRect(x, y, 1, height, color);
 	}
 
 	public static void drawRect(int x, int y, int width, int height, int color){
 		Tessellator tessellator = Tessellator.instance;
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 		GL11.glEnable(GL11.GL_BLEND);
 		GL11.glDisable(GL11.GL_TEXTURE_2D);
 		UI.setColor(color);
 		tessellator.startDrawingQuads();
 		tessellator.addVertex(x,		y+height, 0.0D);
 		tessellator.addVertex(x+width,	y+height, 0.0D);
 		tessellator.addVertex(x+width,	y, 0.0D);
 		tessellator.addVertex(x,		y, 0.0D);
 		tessellator.draw();
 		GL11.glEnable(GL11.GL_TEXTURE_2D);
 		GL11.glDisable(GL11.GL_BLEND);
 	}
 
 	public static void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height, float zLevel){
 		float f = 0.00390625F;
 		float f1 = 0.00390625F;
 		Tessellator tessellator = Tessellator.instance;
 		tessellator.startDrawingQuads();
 		tessellator.addVertexWithUV(x + 0,		y + height,	zLevel, f * (textureX + 0),		f1 * (textureY + height));
 		tessellator.addVertexWithUV(x + width,	y + height,	zLevel, f * (textureX + width), f1 * (textureY + height));
 		tessellator.addVertexWithUV(x + width,	y + 0,		zLevel, f * (textureX + width), f1 * (textureY + 0));
 		tessellator.addVertexWithUV(x + 0,		y + 0,		zLevel, f * (textureX + 0),		f1 * (textureY + 0));
 		tessellator.draw();
 	}
 
 	public static void bindTexture(Minecraft mc, String mod, String filename){
 		#ifdef MC147
 		mc.renderEngine.bindTexture(mc.renderEngine.getTexture(filename));
 		#elif defined MC152
 		mc.renderEngine.bindTexture(filename);
 		#else
 		mc.getTextureManager().bindTexture(mod == null ? new ResourceLocation(filename) : new ResourceLocation(mod, filename));
 		#endif
 	}
 	public static void bindTexture(Minecraft mc, String filename){
 		UI.bindTexture(mc, null, filename);
 	}
 
 	// no support for 147
 	#ifndef MC147
 	public static void drawTexturedRect(int x, int y, Icon icon, int width, int height, float zLevel){
 		Tessellator tessellator = Tessellator.instance;
 		tessellator.startDrawingQuads();
 		tessellator.addVertexWithUV(x + 0,		y + height,	zLevel, icon.getMinU(), icon.getMaxV());
 		tessellator.addVertexWithUV(x + width,	y + height,	zLevel, icon.getMaxU(), icon.getMaxV());
 		tessellator.addVertexWithUV(x + width,	y + 0,		zLevel, icon.getMaxU(), icon.getMinV());
 		tessellator.addVertexWithUV(x + 0,		y + 0,		zLevel, icon.getMinU(), icon.getMinV());
 		tessellator.draw();
 	}
 	#endif
 	// no support for 147
 
 	public static void drawItemStack(Minecraft mc, RenderItem itemRenderer, ItemStack itemstack, int x, int y, boolean durability_bar) throws Exception {
 		if(itemstack != null){
 			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 			GL11.glEnable(32826); // GL_RESCALE_NORMAL_EXT + GL_RESCALE_NORMAL_EXT
 			RenderHelper.enableStandardItemLighting();
 			RenderHelper.enableGUIStandardItemLighting();
 			try {
 				itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, x, y);
 
 				if(durability_bar){
 					#ifdef MC147
 					itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, x, y);
 					#else
 					itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, x, y, null);
 					#endif
 				}
 			} catch(Exception e){
 				RenderHelper.disableStandardItemLighting();
 				GL11.glDisable(32826); // GL_RESCALE_NORMAL_EXT + GL_RESCALE_NORMAL_EXT
 				throw e;
 			}
 			RenderHelper.disableStandardItemLighting();
 			GL11.glDisable(32826); // GL_RESCALE_NORMAL_EXT + GL_RESCALE_NORMAL_EXT
 		}
 	}
 
 	//////////
 
 	public static void setColor(int color){
 		GL11.glColor4f((float)(color >> 16 & 255) / 255.0F, (float)(color >> 8 & 255) / 255.0F, (float)(color & 255) / 255.0F, (float)(color >> 24 & 255) / 255.0F);
 	}
 	public static void setColor(float r, float g, float b, float alpha){
 		GL11.glColor4f(r, g, b, alpha);
 	}
 
 /*
 	public static void setLighting(boolean enable){
 		if(enable)
 			GL11.glEnable(GL11.GL_LIGHTING);
 		else
 			GL11.glDisable(GL11.GL_LIGHTING);
 	}
 */
 }
