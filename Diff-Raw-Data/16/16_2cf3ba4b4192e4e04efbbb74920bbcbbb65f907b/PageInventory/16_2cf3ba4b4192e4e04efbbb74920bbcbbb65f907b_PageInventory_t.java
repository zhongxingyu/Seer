 package org.tilegames.mc.magicchest.client.gui;
 
 import net.minecraft.src.InventoryPlayer;
 import net.minecraft.src.OpenGlHelper;
 import net.minecraft.src.Slot;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.GL11;
 
 public class PageInventory extends Page {
     
     public static final int BUTTON_ID = 100;
 
     private Slot hoveredSlot;
     
     public PageInventory (GuiMagicChest gui) {
         super (gui);
     }
     
     @Override
     public String getTitle () {
         return "Magic Chest";
     }
 
     @Override
     public int getButtonId () {
         return BUTTON_ID;
     }
     
 
     @Override
     public void draw (int mouseX, int mouseY) {
         InventoryPlayer playerInventory = gui.getInventoryPlayer ();
         
         /* Draw background. */
        gui.renderHelper.bindAndDrawBackgroundTexture ("/MagicChest/Pages/Inventory.png");
         
         /* Draw slots. */
         hoveredSlot = null;
         
         /* ???? */
         short var6 = 240;
         short var7 = 240;
         OpenGlHelper.setLightmapTextureCoords (OpenGlHelper.lightmapTexUnit, (float) var6 / 1.0F, (float) var7 / 1.0F);
         GL11.glColor4f (1.0F, 1.0F, 1.0F, 1.0F);
         
         /* Draw slots. */
         int size = gui.container.inventorySlots.size ();
         for (int i = 0; i < size; ++i) {
             Slot slot = (Slot) gui.container.inventorySlots.get (i);
             int x = slot.xDisplayPosition;
             int y = slot.yDisplayPosition;
             
             gui.renderHelper.drawSlot (slot);
             
             /* Check hover status. */
             if (gui.renderHelper.pointInRectangle (x, y, 16, 16, mouseX, mouseY)) {
                 hoveredSlot = slot;
                 gui.renderHelper.drawHoverRectangle (x, y, 16, 16, 0x80FFFFFF);
             }
         }
         
         /* Render item taken by player. */
         if (playerInventory.getItemStack () != null) {
             gui.setZLevel (200.0f);
             gui.renderHelper.drawItemStack (playerInventory.getItemStack (), mouseX - gui.offsetX - 8, mouseY - gui.offsetY - 8);
             gui.setZLevel (0.0f);
         }
         /* Draw tooltip. */
         else if (hoveredSlot != null && hoveredSlot.getHasStack ()) { 
             gui.renderHelper.drawTooltip (hoveredSlot.getStack (), mouseX - gui.offsetX, mouseY - gui.offsetY);
         }
     }
 
     @Override
     public void onClick (int x, int y, int button) {
         boolean blockPickPressed = button == gui.getMinecraft ().gameSettings.keyBindPickBlock.keyCode + 100;
 
         if (button == 0 || button == 1 || blockPickPressed) {
             Slot slot = getSlotAtPosition (x, y);
             boolean dropItem = x < gui.offsetX || y < gui.offsetY || x >= gui.offsetX + GuiMagicChest.SIZE_X || y >= gui.offsetY + GuiMagicChest.SIZE_Y;
             int slotId = -1;
 
             if (slot != null) slotId = slot.slotNumber;
             if (dropItem) slotId = -999;
             
             if (slotId != -1) {
                 if (blockPickPressed) {
                     handleMouseClick (slot, slotId, button, 3);
                 }else {
                     boolean transferStack = slotId != -999 && (Keyboard.isKeyDown (42) || Keyboard.isKeyDown (54)); /* Keys: Shift. */
                     handleMouseClick (slot, slotId, button, transferStack ? 1 : 0);
                 }
             }
         }
     }
     
     @Override
     public void onKeyType (char character, int key) {
         if (gui.getInventoryPlayer ().getItemStack () == null && hoveredSlot != null) {
             for (int i = 0; i < 9; ++i) {
                 if (key == 2 + i) {
                     handleMouseClick (hoveredSlot, hoveredSlot.slotNumber, i, 2);
                 }
             }
         }
 
         if (key == gui.getMinecraft ().gameSettings.keyBindPickBlock.keyCode && hoveredSlot != null && hoveredSlot.getHasStack()) {
             handleMouseClick (hoveredSlot, hoveredSlot.slotNumber, key, 3);
         }
         
     }
     
     
     private Slot getSlotAtPosition (int x, int y) {
         int size = gui.container.inventorySlots.size ();
         for (int i = 0; i < size; ++i) {
             Slot slot = (Slot) gui.container.inventorySlots.get (i);
             if (gui.renderHelper.pointInRectangle (slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, x, y)) return slot;
         }
         return null;
     }
     
     private void handleMouseClick (Slot slot, int id, int button, int transferStack) { /* TODO(Marco): Really 'button'? */
         if (slot != null) id = slot.slotNumber;
         gui.getMinecraft ().playerController.windowClick (gui.container.windowId, id, button, transferStack, gui.getMinecraft ().thePlayer);
     }
 
 }
