 package ip.industrialProcessing.machines.diskFilter;
 
 import org.lwjgl.util.Point;
 import org.lwjgl.util.Rectangle;
 
 import ip.industrialProcessing.machines.containers.gui.GuiContainerFluidMachine;
 import net.minecraft.entity.player.InventoryPlayer;
 
 public class GuiContainerDiskFilter extends GuiContainerFluidMachine {
 
 	private static final Rectangle TANK_SOURCE = new Rectangle(176, 17, 16, 50);
 
 	public GuiContainerDiskFilter(InventoryPlayer inventoryPlayer, TileEntityDiskFilter tileEntity) {
 		super(inventoryPlayer, tileEntity, new ContainerDiskFilter(inventoryPlayer, tileEntity), "Disk Filter", "textures/gui/DiskFilter.png");
 		// setProgressBarLocation(76,34,24,17);
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
 		super.drawGuiContainerBackgroundLayer(par1, par2, par3);
 
 		drawProgressBarTank(TANK_SOURCE, new Point(8, 19), 0);
		drawProgressBarTank(TANK_SOURCE, new Point(44, 19), 1);
		drawProgressBarTank(TANK_SOURCE, new Point(152, 19), 2);
 	}
 }
