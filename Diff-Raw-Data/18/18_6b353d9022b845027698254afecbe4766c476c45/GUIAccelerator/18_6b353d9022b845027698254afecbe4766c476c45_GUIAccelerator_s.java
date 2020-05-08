 package ro.narc.liquiduu;
 
 import net.minecraftforge.client.ForgeHooksClient;
 
 import net.minecraft.src.GuiContainer;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.InventoryPlayer;
 import net.minecraft.src.MathHelper;
 import net.minecraft.src.RenderHelper;
 import net.minecraft.src.Slot;
 import net.minecraft.src.StatCollector;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 public class GUIAccelerator extends GuiContainer {
     public static final int BORDER = 4;
     public int main_width, main_height, left, top, center, middle, right, bottom;
 
     public int mouseX = -1;
     public int mouseY = -1;
 
     TileEntityAccelerator accelerator;
 
     public GUIAccelerator(InventoryPlayer inventory, TileEntityAccelerator accelerator) {
         super(new ContainerAccelerator(inventory, accelerator));
         this.accelerator = accelerator;
 
         // These are such wonderful things to have, I've just stolen them from SeedManager.
         main_width = xSize - BORDER * 2;
         main_height = ySize - BORDER * 2 - 92;
 
         left = BORDER;
         top = BORDER;
         center = left + main_width / 2;
         middle = top + main_height / 2;
         right = left + main_width;
         bottom = top + main_height;
     }
 
     @Override
     protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
         int textureID = mc.renderEngine.getTexture("/liquiduu-gfx/gui-accelerator.png");
         mc.renderEngine.bindTexture(textureID);
 
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 
         // Transfer the coordinate space to within the GUI screen.
         GL11.glPushMatrix();
         GL11.glTranslatef(guiLeft, guiTop, 0.0F);
 
         // Draw the background.
         drawTexturedModalRect(0, 0, 0, 0, xSize, ySize);
 
         Slot machineSlot = inventorySlots.getSlot(3);
         ItemStack machine = machineSlot.getStack();
         if(machine == null) { // "No Machine" icon
             drawTexturedModalRect(machineSlot.xDisplayPosition, machineSlot.yDisplayPosition,
                     176, 0, 16, 16);
         }
 
         int scaledLiquidAmount = MathHelper.ceiling_float_int((float) accelerator.tank.getLiquidAmount() * 47 / 2000.0F);
         drawLiquidTank(152, 29, scaledLiquidAmount);
 
         // Restore previous coordinates.
         GL11.glPopMatrix();
     }
 
     @Override
     protected void drawGuiContainerForegroundLayer(int i, int j) {
         String machineName = "None";
         ItemStack machine = inventorySlots.getSlot(3).getStack();
         if(machine != null) {
             machineName = StatCollector.translateToLocal(machine.getItemName() + ".name");
         }
 
         drawCenteredString("Accelerator",       70, top +  2, 0x404040);
         drawCenteredString("Connected Machine", 70, top + 12, 0x404040);
         drawCenteredString(machineName,         70, top + 38, 0x404040);
 
         int uumAmount = accelerator.tank.getLiquidAmount();
         int operationCost = accelerator.getOperationCost();
         if(operationCost > 0) {
             drawOperationCosts(operationCost);
         }
         fontRenderer.drawString("Inventory", left + 4, bottom + 2, 0x404040);
 
         if((mouseX >= 152) && (mouseX < 168) &&
            (mouseY >= 29) && (mouseY < 76)) {
             String line1 = String.format("%.3f UUM stored",
                     (float) uumAmount / 1000.0F);
             String line2 = "";
 
             if(operationCost > 0) {
                 line2 = String.format("%d operations remaining",
                     MathHelper.floor_float((float)uumAmount / (float)operationCost));
             }
 
             drawTwoLineHoveringText(line1, line2, mouseX, mouseY);
         }
     }
 
     public void drawCenteredString(String s, int x, int y, int color) {
         fontRenderer.drawString(s, x - fontRenderer.getStringWidth(s) / 2, y,
                                 color);
     }
 
     public void drawRightAlignedString(String s, int x, int y, int color) {
         fontRenderer.drawString(s, x - fontRenderer.getStringWidth(s), y,
                                 color);
     }
 
     public void drawOperationCosts(int cost) {
        String[] line1 = String.format("%.3f UUM/operation",  cost / 1000.0F).split("[. ]");
        String[] line2 = String.format("%.3f operations/UUM", 1000.0F / cost).split("[. ]");
 
         int topWidths[] = new int[3];
         int botWidths[] = new int[3];
 
         for (int i=0; i<3; i++) {
             topWidths[i] = fontRenderer.getStringWidth(line1[i]);
             botWidths[i] = fontRenderer.getStringWidth(line2[i]);
         }
 
        int dotWidth = fontRenderer.getStringWidth(".");
         int spaceWidth = fontRenderer.getStringWidth(" ");
 
         int offsets[] = new int[3];
         offsets[2] = Math.max(topWidths[2], botWidths[2]);
         offsets[1] = Math.max(topWidths[1], botWidths[1]) + dotWidth + spaceWidth;
         // Keep offsets[0], just for aligning the arrays.
 
         int rightAlign = right - 24 - offsets[2];
 
         fontRenderer.drawString(line1[2], rightAlign, bottom-17, 0x404040);
         fontRenderer.drawString(line2[2], rightAlign, bottom-8,  0x404040);
 
         rightAlign -= offsets[1];
 
        fontRenderer.drawString("." + line1[1], rightAlign, bottom-17, 0x404040);
        fontRenderer.drawString("." + line2[1], rightAlign, bottom-8,  0x404040);
 
         fontRenderer.drawString(line1[0], rightAlign - topWidths[0], bottom-17, 0x404040);
         fontRenderer.drawString(line2[0], rightAlign - botWidths[0], bottom-8,  0x404040);
     }
 
     public void drawLiquidTank(int x, int y, int scaledValue) {
         ForgeHooksClient.bindTexture(LiquidUU.liquidUU.getItem().getTextureFile(), 0);
         int iconIndex = LiquidUU.liquidUU.getIconIndex();
 
         int imgY = iconIndex / 16;
         int imgX = iconIndex - imgY * 16;
 
         for(int i = scaledValue; i > 0; i -= 16) {
             drawTexturedModalRect(x, y + (47 - i),
                                   imgX * 16, imgY * 16,
                                   16, Math.min(16, i));
         }
 
         int textureID = mc.renderEngine.getTexture("/liquiduu-gfx/gui-accelerator.png");
         mc.renderEngine.bindTexture(textureID);
 
         drawTexturedModalRect(x, y, 176, 16, 16, 47);
     }
 
     // Lovingly stolen from GuiContainer.drawCreativeTabHoveringText();
     // Variable names are mostly guesswork.
     protected void drawTwoLineHoveringText(String line1, String line2, int mouseX, int mouseY)
     {
         GL11.glDisable(GL12.GL_RESCALE_NORMAL);
         RenderHelper.disableStandardItemLighting();
         GL11.glDisable(GL11.GL_LIGHTING);
         GL11.glDisable(GL11.GL_DEPTH_TEST);
         int maxWidth = Math.max(this.fontRenderer.getStringWidth(line1), this.fontRenderer.getStringWidth(line2));
         int targetX = mouseX + 12;
         int targetY = mouseY - 21;
         byte height = 17;
         this.zLevel = 300.0F;
         itemRenderer.zLevel = 300.0F;
         int bgCol = 0xF0100010; // In order: ARGB
         this.drawGradientRect(targetX - 3, targetY - 4, targetX + maxWidth + 3, targetY - 3, bgCol, bgCol);
         this.drawGradientRect(targetX - 3, targetY + height + 3, targetX + maxWidth + 3, targetY + height + 4, bgCol, bgCol);
         this.drawGradientRect(targetX - 3, targetY - 3, targetX + maxWidth + 3, targetY + height + 3, bgCol, bgCol);
         this.drawGradientRect(targetX - 4, targetY - 3, targetX - 3, targetY + height + 3, bgCol, bgCol);
         this.drawGradientRect(targetX + maxWidth + 3, targetY - 3, targetX + maxWidth + 4, targetY + height + 3, bgCol, bgCol);
         int var10 = 0x505000FF;
         int var11 = (var10 & 0xFEFEFE) >> 1 | var10 & 0xFF000000;
         this.drawGradientRect(targetX - 3, targetY - 3 + 1, targetX - 3 + 1, targetY + height + 3 - 1, var10, var11);
         this.drawGradientRect(targetX + maxWidth + 2, targetY - 3 + 1, targetX + maxWidth + 3, targetY + height + 3 - 1, var10, var11);
         this.drawGradientRect(targetX - 3, targetY - 3, targetX + maxWidth + 3, targetY - 3 + 1, var10, var10);
         this.drawGradientRect(targetX - 3, targetY + height + 2, targetX + maxWidth + 3, targetY + height + 3, var11, var11);
         this.fontRenderer.drawStringWithShadow(line1, targetX, targetY, 0xFFFFFFFF);
         this.fontRenderer.drawStringWithShadow(line2, targetX, targetY + 9, 0xFFBBBBBB);
         this.zLevel = 0.0F;
         itemRenderer.zLevel = 0.0F;
         GL11.glEnable(GL11.GL_LIGHTING);
         RenderHelper.enableGUIStandardItemLighting();
     }
 
     // And because we need mouseX and mouseY and nobody seems to be keeping track of them...
     @Override
     protected void mouseMovedOrUp(int x, int y, int button) {
         super.mouseMovedOrUp(x, y, button);
 
         mouseX = x - guiLeft;
         mouseY = y - guiTop;
     }
 }
