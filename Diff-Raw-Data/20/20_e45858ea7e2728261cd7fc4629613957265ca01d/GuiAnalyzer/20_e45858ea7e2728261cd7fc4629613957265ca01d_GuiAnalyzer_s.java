 package infinitealloys.client.gui;
 
 import infinitealloys.core.InfiniteAlloys;
 import infinitealloys.core.WorldData;
 import infinitealloys.item.Items;
 import infinitealloys.tile.TEEAnalyzer;
 import infinitealloys.util.Consts;
 import infinitealloys.util.Funcs;
 import java.awt.Rectangle;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.ItemStack;
 import org.lwjgl.opengl.GL11;
 
 public class GuiAnalyzer extends GuiElectric {
 
 	private TEEAnalyzer tea;
 
 	public GuiAnalyzer(InventoryPlayer inventoryPlayer, TEEAnalyzer tileEntity) {
		super(216, 166, inventoryPlayer,tileEntity );
 		tea = tileEntity;
 		progressBar.setLocation(54, 57);
 	}
 
 	@Override
 	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
 		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
 		for(int i = 0; i < Consts.METAL_COUNT; i++)
 			itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, new ItemStack(Items.ingot, 1, i), i * 18 + 27, 8);
 		GL11.glDisable(GL11.GL_LIGHTING);
 		GL11.glDisable(GL11.GL_DEPTH_TEST);
 		GL11.glColor4f(1F, 1F, 1F, 1F);
 		bindTexture(extras);
 		if(tea.inventoryStacks[1] != null) {
 			int currentAlloy;
 			currentAlloy = tea.inventoryStacks[1].getTagCompound().getInteger("alloy");
 			WorldData worldData = InfiniteAlloys.instance.worldData;
 			for(int i = 0; i < Consts.METAL_COUNT; i++) {
 				int currentValue = Funcs.intAtPos(currentAlloy, 10, Consts.METAL_COUNT, i);
 				int nextValue = Funcs.intAtPos(worldData.getValidAlloys()[worldData.alloysUnlocked], 10, Consts.METAL_COUNT, i);
 				Rectangle symbol = nextValue > currentValue ? DOWN_ARROW : nextValue < currentValue ? UP_ARROW : CHECK;
 				drawTexturedModalRect((Consts.VALID_ALLOY_COUNT - i) * 18 + 45, 26, symbol.x, symbol.y, symbol.width, symbol.height);
 			}
 		}
 		GL11.glEnable(GL11.GL_LIGHTING);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 	}
 }
