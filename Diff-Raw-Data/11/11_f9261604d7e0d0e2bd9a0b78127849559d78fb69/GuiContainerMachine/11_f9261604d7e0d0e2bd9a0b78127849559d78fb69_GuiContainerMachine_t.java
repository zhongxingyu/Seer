 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.client;
 
 import monnef.core.block.ContainerMachine;
 import monnef.core.block.TileMachineWithInventory;
 import monnef.core.utils.ColorHelper;
 import monnef.core.utils.LanguageHelper;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.util.StatCollector;
 
 import java.util.List;
 
 public class GuiContainerMachine extends GuiContainerMonnefCore {
     public static final int ENERGY_BAR_X = 146;
     public static final int ENERGY_BAR_Y = 15;
     public static final int ENERGY_BAR_HEIGHT = 50;
     public static final int ENERGY_BAR_INNER_HEIGHT = ENERGY_BAR_HEIGHT - 2;
     public static final int ENERGY_BAR_WIDTH = 18;
     protected static ColorHelper.IntColor topColor = new ColorHelper.IntColor(53, 180, 212);
     protected static ColorHelper.IntColor bottomColor = new ColorHelper.IntColor(25, 78, 90);
     protected final TileMachineWithInventory tile;
 
     public GuiContainerMachine(InventoryPlayer inventoryPlayer, TileMachineWithInventory tileEntity, ContainerMachine container) {
         super(container);
         this.tile = tileEntity;
     }
 
     @Override
     public int getCallerDepthForPackageIdentification() {
         return 1 + super.getCallerDepthForPackageIdentification();
     }
 
     @Override
     protected void drawGuiContainerForegroundLayer(int param1, int param2) {
         fontRendererObj.drawString(getTitle(), 8, 6, 4210752);
         fontRendererObj.drawString(LanguageHelper.localInventory(), 8, ySize - 96 + 2, 4210752);
     }
 
     protected String getTitle() {
         return tile.getMachineTitle();
     }
 
     @Override
     protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
         super.drawGuiContainerBackgroundLayer(par1, par2, par3);
 
         if (tile.isPowerBarRenderingEnabled())
             drawEnergyBar(tile);
     }
 
     protected void drawEnergyBar(TileMachineWithInventory tile) {
         int value = tile.getGuiPowerMax() != 0 ? (tile.getGuiPowerStored() * ENERGY_BAR_INNER_HEIGHT) / tile.getGuiPowerMax() : 0;
         drawBottomUpBar(x + ENERGY_BAR_X, y + ENERGY_BAR_Y, value, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT, topColor, bottomColor);
     }
 
     @Override
     public List<String> fillTooltips(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
         if (isMouseInRect(mousex, mousey, ENERGY_BAR_X, ENERGY_BAR_Y, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT)) {
            currenttip.add("\u00A72Energy:\u00A7r");
            currenttip.add(String.format("\u00A77%d\u00A78 / \u00A77%d\u00A7r", tile.getGuiPowerStored(), tile.getGuiPowerMax()));
         }
 
         return currenttip;
     }
 }
