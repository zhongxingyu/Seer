 package sct.culinarycraft.gui.client;
 
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.util.StatCollector;
 
 import org.lwjgl.opengl.GL11;
 
 import sct.culinarycraft.gui.container.ContainerOven;
 import sct.culinarycraft.tile.TileEntityOven;
 
 public class GuiOven extends GuiMachine {
 
 	TileEntityOven te;
 	
 	public GuiOven(InventoryPlayer invPlayer, TileEntityOven te) {
 		super(new ContainerOven(invPlayer, te));
 		this.te = te;
 	}
 	
 	@Override
 	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
 		fontRenderer.drawString("Oven", 8, 6, 4210752);
 		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 4210752);
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture("/textures/gui/oven.png");
 		int x = (width - xSize) / 2;
 		int y = (height - ySize) / 2;
 		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
 		
 		int cookTime = te.getCookProgressScaled(48);
 		if (cookTime > 0) {
 			drawTexturedModalRect(x + 67, y + 33, 176, 0, 48 - cookTime, 19);
 		}
 		
 		
 		int energyScaled = (te.getEnergyStored() * 42) / te.getEnergyStoredMax();
 		if (energyScaled > 0) {
 			drawTexturedModalRect(x + 154, (y + 7) + 42 - energyScaled, 177, 62 - energyScaled, 14, energyScaled);
 		}
 	}
 	
 	protected void drawTooltips(int mouseX, int mouseY) {
 		if(isPointInRegion(154, 7, 14, 42, mouseX, mouseY)) {
 			drawBarTooltip("Energy", "MJ", te.getEnergyStored(), te.getEnergyStoredMax(), mouseX, mouseY);
 		}
 	}
 
 }
