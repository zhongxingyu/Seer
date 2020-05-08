 package siramnot.mods.dmi.gui;
 
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
 import net.minecraft.util.StatCollector;
 
 import org.lwjgl.opengl.GL11;
 
 import siramnot.mods.dmi.DMI;
 import siramnot.mods.dmi.blocks.tileeents.TileEntityLiquidator;
 
 public class GuiLiquidator extends GuiContainer {
 
 	private static final String NAME = "Block/FurnaceGUI.png"; // Make it easier to change path.
 	private static final String PATH = DMI.MOD_ID.toLowerCase() + ":" + NAME;
 
 	private TileEntityLiquidator inv;
	protected static final ResourceLocation gui = new ResourceLocation(PATH); // "DMI has to be lowercases, AFAIK. That doesn't matter in the code, only in the file system. 
	                                                                          //You still have to recompile it and put it inside minecraft for the GUI to show.
 
 	public GuiLiquidator(InventoryPlayer inventory, TileEntityLiquidator liquidator) {
 		super(new ContainerLiquidator(inventory, liquidator));
 		inv = liquidator;
 	}
 
 	/**
 	 * Draw the foreground layer for the GuiContainer (everythin in front of the
 	 * items)
 	 */
 	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
 		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 0xffffff);
 	}
 
 	/**
 	 * Draw the background layer for the GuiContainer (everything behind the
 	 * items)
 	 */
 	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		int j = (width - xSize) / 2;
 		int k = (height - ySize) / 2;
 		this.mc.func_110434_K().func_110577_a(gui);
 		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
 
 		if (inv.isBurning()) {
 			int burn = inv.getBurnTimeRemainingScaled(14);
 			drawTexturedModalRect(j + 73, k + 59, 176, 16, burn, 10);
 		}
 
 		int update = inv.getCookProgressScaled(16);
 		drawTexturedModalRect(j + 89, k + 55, 191, 15, -update, -update);
 	}
 }
