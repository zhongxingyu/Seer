 package org.tilegames.mc.magicchest.client.gui;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.FontRenderer;
 import net.minecraft.src.GuiScreen;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.InventoryPlayer;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.RenderEngine;
 import net.minecraft.src.RenderItem;
 import net.minecraft.src.Slot;
 import net.minecraft.src.Tessellator;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 import org.tilegames.mc.magicchest.ContainerMagicChest;
 import org.tilegames.mc.magicchest.TileEntityMagicChest;
 
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 
 /* Note:
  *  To make the GUI more customizable, I unfortunately had to copy (and improve 
  *  of course) the rendering code of GuiContainer. 
  */
 
 @SideOnly(Side.CLIENT)
 public class GuiMagicChest extends GuiScreen {
     
     public static RenderItem itemRenderer = new RenderItem ();
     
     public static final int SIZE_Y = 168;
     public static final int SIZE_X = 176;
     
     public class RenderHelper {
         
         public void bindAndDrawBackgroundTexture (String path) {
             /* Bind texture. */
             bindTexture (path);
             
             /* Draw. */
             GL11.glColor4f (1.0F, 1.0F, 1.0F, 1.0F);
             drawTexturedModalRect (0, 0, 0, 0, SIZE_X, SIZE_Y);
         }
         
         public void bindTexture (String path) {
             getRenderEngine ().bindTexture (getRenderEngine ().getTexture (path));
         }
         
         public void drawHoverRectangle (int x, int y, int width, int height, int color) {
             GL11.glDisable (GL11.GL_LIGHTING);
             GL11.glDisable (GL11.GL_DEPTH_TEST);
             drawGradientRect (x, y, x + width, y + height, color, color);
             GL11.glEnable (GL11.GL_LIGHTING);
             GL11.glEnable (GL11.GL_DEPTH_TEST);
         }
         
         public void drawItemStack (ItemStack stack, int x, int y) {
             RenderEngine renderEngine = getRenderEngine ();
             
             GL11.glEnable (GL11.GL_DEPTH_TEST);
             itemRenderer.func_82406_b (fontRenderer, renderEngine, stack, x, y);
             itemRenderer.renderItemOverlayIntoGUI (fontRenderer, renderEngine, stack, x, y);
         }
         
         public void drawSlot (Slot slot) {
             int x = slot.xDisplayPosition;
             int y = slot.yDisplayPosition;
             ItemStack stack = slot.getStack();
             
             setZLevel (100.0f);
 
             if (stack == null) {
                 int backgroundIconIndex = slot.getBackgroundIconIndex ();
                 if (backgroundIconIndex >= 0) {
                     GL11.glDisable(GL11.GL_LIGHTING);
                     mc.renderEngine.bindTexture (mc.renderEngine.getTexture ("/gui/items.png"));
                     drawTexturedModalRect (x, y, backgroundIconIndex % 16 * 16, backgroundIconIndex / 16 * 16, 16, 16);
                     GL11.glEnable(GL11.GL_LIGHTING);
                 }
             }else { /* Render stack. */
                 drawItemStack (stack, x, y);
             }
 
             setZLevel (0.0f);
         }
         
         public void drawTooltip (ItemStack stack, int x, int y) {
             GL11.glDisable (GL12.GL_RESCALE_NORMAL);
             net.minecraft.src.RenderHelper.disableStandardItemLighting ();
             GL11.glDisable (GL11.GL_LIGHTING);
             GL11.glDisable (GL11.GL_DEPTH_TEST);
             
             @SuppressWarnings("rawtypes")
             List strings = stack.func_82840_a (mc.thePlayer, mc.gameSettings.field_82882_x);
 
             if (!strings.isEmpty ()) {
                 int size = strings.size();
                 int tooltipWidth = 0;
                 @SuppressWarnings("rawtypes")
                 Iterator iterator = strings.iterator();
 
                 /* Get tooltip width by maximum string width. */
                 while (iterator.hasNext ()) {
                     int stringWidth = fontRenderer.getStringWidth ((String) iterator.next ());
                     if (stringWidth > tooltipWidth) tooltipWidth = stringWidth;
                 }
 
                 /* Calculate height. */
                 int height = 8;
                 if (size > 1) height += 2 + (size - 1) * 10;
                 
                 /* Bounds. */
                 int minX = x + 12;
                 int minY = y - 12;
                 int maxX = minX + tooltipWidth;
                 int maxY = minY + height;
 
                 /* Render background.*/
                 setZLevel (300.0f);
                 
                 final int color = 0xF0100010;
                 drawGradientRect (minX - 3, minY - 4, maxX + 3, minY - 3, color, color);
                 drawGradientRect (minX - 3, maxY + 3, maxX + 3, maxY + 4, color, color);
                 drawGradientRect (minX - 3, minY - 3, maxX + 3, maxY + 3, color, color);
                 drawGradientRect (minX - 4, minY - 3, minX - 3, maxY + 3, color, color);
                 drawGradientRect (maxX + 3, minY - 3, maxX + 4, maxY + 3, color, color);
                 
                 final int color1 = 0x505000FF;
                 final int color2 = (color1 & 0xFEFEFE) >> 1 | color1 & 0xFF000000;
                 drawGradientRect (minX - 3, minY - 2, minX - 2, maxY + 2, color1, color2);
                 drawGradientRect (maxX + 2, minY - 2, maxX + 3, maxY + 2, color1, color2);
                 drawGradientRect (minX - 3, minY - 3, maxX + 3, minY - 2, color1, color1);
                 drawGradientRect (minX - 3, maxY + 2, maxX + 3, maxY + 3, color2, color2);
 
                 /* Render strings. */
                 for (int i = 0; i < size; ++i) {
                     String string = (String) strings.get (i);
 
                     if (i == 0) string = "\u00a7" + Integer.toHexString (stack.getRarity ().rarityColor) + string;
                     else string = "\u00a77" + string;
 
                     fontRenderer.drawStringWithShadow (string, minX, minY, -1);
 
                     if (i == 0) minY += 2;
                     minY += 10;
                 }
 
                 setZLevel (0.0f);
             }
         }
         
         public void drawIconButton (int texture, int x, int y, int state) {
             double u = Math.floor (texture / 16.0) * 16.0;
             double v = texture % 16 * 16.0;
             double z = zLevel;
             double uEnd = u + 16.0 / 256.0;
             double vEnd = v + 16.0 / 256.0;
             double xEnd = x + IconButton.WIDTH;
             double yEnd = y + IconButton.HEIGHT;
             
             Tessellator.instance.startDrawingQuads ();
             Tessellator.instance.addVertexWithUV (x, yEnd, z, u, vEnd);
             Tessellator.instance.addVertexWithUV (xEnd, yEnd, z, uEnd, vEnd);
             Tessellator.instance.addVertexWithUV (xEnd, y, z, uEnd, v);
             Tessellator.instance.addVertexWithUV (x, y, z, u, v);
             Tessellator.instance.draw ();
             
             if (state != IconButton.STATE_NORMAL) {
                 int color;
                 if (state == IconButton.STATE_HOVER) { /* HOVER */
                     color = 0x20FFFFFF;
                 }else { /* ACTIVE */
                     color = 0x40FFFFFF;
                 }
                 
                 drawHoverRectangle (x, y, IconButton.WIDTH, IconButton.HEIGHT, color);
             }
         }
         
         
         public boolean pointInRectangle (int x, int y, int width, int height, int px, int py) {
             px -= offsetX;
             py -= offsetY;
             return px >= x - 1 && px < x + width + 1 && py >= y - 1 && py < y + height + 1;
         }
         
     }
     
     
     public ContainerMagicChest container;
     //private TileEntityMagicChest chest;
     
     
     
     public List<IconButton> iconButtons = new ArrayList<IconButton> ();
     
     
     
     /* The page that is currently rendered. */
     private Page page;
     
     
     /**
      * Starting X and Y position for the GUI.
      */
     protected int offsetX;
     protected int offsetY;
     
     public RenderHelper renderHelper;
     
     
     public GuiMagicChest (IInventory playerInventory, TileEntityMagicChest chestInventory) {
         container = new ContainerMagicChest (playerInventory, chestInventory);
         //chest = chestInventory;
     }
     
     
     public void initGui () {
         super.initGui ();
         mc.thePlayer.craftingInventory = container;
         
         offsetX = (width - SIZE_X) / 2;
         offsetY = (height - SIZE_Y) / 2;
         
         renderHelper = new RenderHelper ();
         page = new PageInventory (this);
         
         iconButtons.clear ();
        iconButtons.add (new IconButton (PageOptions.BUTTON_ID, 133, 6, 0));
     }
     
     
     public void drawScreen (int mouseX, int mouseY, float par3) {
         // super.drawScreen (mouseX, mouseY, par3); /* Do NOT draw buttons the standard way! */
         
         /* Draw default background. */
         drawDefaultBackground ();
         
         /* Prepare rendering. */
         net.minecraft.src.RenderHelper.disableStandardItemLighting ();
         GL11.glDisable (GL11.GL_LIGHTING);
         // GL11.glDisable (GL11.GL_DEPTH_TEST);
         
         net.minecraft.src.RenderHelper.enableGUIStandardItemLighting ();
         GL11.glEnable (GL12.GL_RESCALE_NORMAL);
         
         GL11.glPushMatrix ();
         GL11.glTranslatef (offsetX, offsetY, 0.0F);
         GL11.glColor4f (1.0F, 1.0F, 1.0F, 1.0F);
         
         
         /* Draw foreground. */
         GL11.glPushMatrix ();
         GL11.glTranslatef (0.0f, 0.0f, 100.0f);
         
         /* Draw icon buttons. */
         renderHelper.bindTexture ("/MagicChest/Buttons.png");
         
         int size = iconButtons.size ();
         for (int i = 0; i < size; ++i) {
             IconButton button = iconButtons.get (i);
             renderHelper.drawIconButton (button.getTexture (), button.x, button.y, button.getState (page, mouseX - offsetX, mouseY - offsetY));
         }
         
         /* Draw titles. */
         fontRenderer.drawString (page.getTitle (), 8, 6, 0x404040);
         
         GL11.glPopMatrix ();
         
         
         /* Render page. */
         page.draw (mouseX, mouseY);
         
         /* Rendering cleanup. */
         GL11.glPopMatrix();
         GL11.glEnable (GL11.GL_LIGHTING);
         GL11.glEnable (GL11.GL_DEPTH_TEST);
         net.minecraft.src.RenderHelper.enableStandardItemLighting ();
     }
 
     @Override
     protected void mouseClicked (int x, int y, int mouseButton) {
         super.mouseClicked (x, y, mouseButton);
         
         /* Check icon buttons. */
         int size = iconButtons.size ();
         for (int i = 0; i < size; ++i) {
             IconButton button = iconButtons.get (i);
             if (button.inBounds (x - offsetX, y - offsetY)) {
                 onIconButtonClick (button);
                 return;
             }
         }
         
         page.onClick (x, y, mouseButton);
     }
     
     @Override
     protected void keyTyped (char character, int key) {
         if (key == 1 || key == mc.gameSettings.keyBindInventory.keyCode) { /* Escape closes page or UI depending on page. */
             if (page instanceof PageInventory) {
                 mc.thePlayer.closeScreen ();
             }else {
                 page = new PageInventory (this);
             }
         }else {
             page.onKeyType (character, key);
         }
     }
     
     private void onIconButtonClick (IconButton button) {
         if (button.id == page.getButtonId ()) {
             page = new PageInventory (this);
             return;
         }
         
         switch (button.id) {
             case PageOptions.BUTTON_ID:
                 page = new PageOptions (this);
                 break;
         }
     }
     
 
     public void onGuiClosed () {
         if (mc.thePlayer != null) container.onCraftGuiClosed (mc.thePlayer);
     }
 
     public boolean doesGuiPauseGame () {
         return false;
     }
 
     public void updateScreen () {
         super.updateScreen ();
         if (!mc.thePlayer.isEntityAlive () || mc.thePlayer.isDead) mc.thePlayer.closeScreen();
     }
     
     
     public Minecraft getMinecraft () {
         return mc;
     }
     
     public RenderEngine getRenderEngine () {
         return mc.renderEngine;
     }
     
     public InventoryPlayer getInventoryPlayer () {
         return mc.thePlayer.inventory;
     }
     
     public FontRenderer getFontRenderer () {
         return fontRenderer;
     }
     
     public float getZLevel () {
         return zLevel;
     }
     
     public void setZLevel (float zLevel) {
         itemRenderer.zLevel = zLevel;
         this.zLevel = zLevel;
     }
     
 }
