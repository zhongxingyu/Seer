 /*
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version. This program is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details. You should have received a copy of the GNU
  * Lesser General Public License along with this program. If not, see
  * <http://www.gnu.org/licenses/>
  */
 package slimevoid.tmf.client.gui;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.Slot;
 import net.minecraft.util.ResourceLocation;
 
 import org.lwjgl.opengl.GL11;
 
 import slimevoid.tmf.core.lib.GuiLib;
 import slimevoid.tmf.core.lib.ResourceLib;
 import slimevoid.tmf.items.tools.inventory.InventoryMiningToolBelt;
 
 public class GuiMiningToolBelt extends GuiContainer {
 
     InventoryMiningToolBelt data;
 
     public GuiMiningToolBelt(Container container, EntityPlayer entityplayer, InventoryMiningToolBelt toolBelt) {
         super(container);
         this.xSize = 177;
         this.ySize = 221;
         data = toolBelt;
     }
 
     @Override
     public void initGui() {
         super.initGui();
 
         int motionSensorButtonLength = this.fontRenderer.getStringWidth("Settings");
         GuiButton motionSensorSettings = new GuiButton(GuiLib.MOTION_SENSOR_SETTINGS_BUTTONID, (this.width / 2)
                                                                                                - (motionSensorButtonLength / 2), (this.height / 2), motionSensorButtonLength + 6, 20, "Settings");
         this.buttonList.add(motionSensorSettings);
         ((GuiButton) this.buttonList.get(GuiLib.MOTION_SENSOR_SETTINGS_BUTTONID)).enabled = false;
 
     }
 
     @Override
     protected void actionPerformed(GuiButton button) {
 
         switch (button.id) {
         case GuiLib.MOTION_SENSOR_SETTINGS_BUTTONID:
             System.out.println("Settings");
             break;
         default:
             System.out.println("Default");
             break;
         }
 
     }
 
     @Override
     protected void drawGuiContainerForegroundLayer(int x, int y) {
     }
 
     @Override
     protected void drawGuiContainerBackgroundLayer(float renderTicks, int x, int y) {
        int sizeX = (this.width - this.xSize) / 2;
        int sizeY = (this.height - this.ySize) / 2;
        this.drawBackground(sizeX,
                            sizeY);
        this.drawHighlightedSlot(sizeX,
                                 sizeY);
     }
 
    protected void drawBackground(int sizeX, int sizeY) {
         GL11.glColor4f(1.0F,
                        1.0F,
                        1.0F,
                        1.0F);
         this.mc.renderEngine.bindTexture(this.getBackground());
         this.drawTexturedModalRect(sizeX,
                                    sizeY,
                                    0,
                                    0,
                                    this.xSize,
                                    this.ySize);
     }
 
    protected void drawHighlightedSlot(int sizeX, int sizeY) {
         int slot = data.getSelectedSlot();
         Slot selectedSlot = this.inventorySlots.getSlot(slot);
        int x = selectedSlot.xDisplayPosition + sizeX;
        int y = selectedSlot.yDisplayPosition + sizeY;
         GL11.glDisable(GL11.GL_LIGHTING);
         GL11.glDisable(GL11.GL_DEPTH_TEST);
         this.drawGradientRect(x,
                               y,
                               x + 16,
                               y + 16,
                              -2130702222,
                              -2130702222);
         GL11.glEnable(GL11.GL_LIGHTING);
         GL11.glEnable(GL11.GL_DEPTH_TEST);
     }
 
     private void drawSelectedSlotString(int slot, int x, int y) {
         this.drawCenteredString(fontRenderer,
                                 "Slot[" + slot + "] Selected",
                                 50,
                                 100,
                                 0xff00ff);
     }
 
     public ResourceLocation getBackground() {
         return ResourceLib.GUI_TOOLBELT;
     }
 }
