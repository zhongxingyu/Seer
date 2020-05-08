 package ml.boxes.nei;
 
 import ml.boxes.Boxes;
 import ml.boxes.client.ContentTipHandler;
 import ml.boxes.data.BoxData;
 import ml.boxes.item.ItemBox;
 import ml.boxes.network.packets.PacketTipClick;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import codechicken.nei.MultiItemRange;
 import codechicken.nei.api.API;
 import codechicken.nei.api.IConfigureNEI;
 import codechicken.nei.forge.GuiContainerManager;
 import codechicken.nei.forge.IContainerDrawHandler;
 import codechicken.nei.forge.IContainerInputHandler;
 import codechicken.nei.forge.IContainerObjectHandler;
 import codechicken.nei.recipe.TemplateRecipeHandler;
 import cpw.mods.fml.common.network.Player;
 
 public class NEI_Boxes_Config implements IConfigureNEI {
 
 	@Override
 	public void loadConfig() {
 		Boxes.neiInstalled = true;
 		
 		NEIHandler handler = new NEIHandler();
 		GuiContainerManager.addInputHandler(handler);
 		GuiContainerManager.addObjectHandler(handler);
 		GuiContainerManager.addDrawHandler(handler);
 		
 		TemplateRecipeHandler recipeHandler = new BoxesRecipeHandler();
 		API.registerRecipeHandler(recipeHandler);
 		API.registerUsageHandler(recipeHandler);
 		
 		MultiItemRange range = new MultiItemRange();
 		range.add(Boxes.BlockBox);
 		API.addSetRange("Boxes", range);
 	}
 
 	@Override
 	public String getName() {
 		return "Boxes";
 	}
 
 	@Override
 	public String getVersion() {
 		return "1.0";
 	}
 	
 	private static class NEIHandler implements IContainerInputHandler, IContainerObjectHandler, IContainerDrawHandler{
 
 		@Override
 		public boolean keyTyped(GuiContainer gui, char keyChar, int keyCode) {
 			return false;
 		}
 
 		@Override
 		public void onKeyTyped(GuiContainer gui, char keyChar, int keyID) {
 			
 		}
 
 		@Override
 		public boolean lastKeyTyped(GuiContainer gui, char keyChar, int keyID) {
 			return false;
 		}
 
 		@Override
 		public boolean mouseClicked(GuiContainer gui, int mousex, int mousey,
 				int button) {
 			return ContentTipHandler.handleClick(mousex, mousey, button);
 		}
 
 		@Override
 		public void onMouseClicked(GuiContainer gui, int mousex, int mousey,
 				int button) {
 			
 		}
 
 		@Override
 		public void onMouseUp(GuiContainer gui, int mousex, int mousey,
 				int button) {
 			
 		}
 
 		@Override
 		public boolean mouseScrolled(GuiContainer gui, int mousex, int mousey,
 				int scrolled) {
 			return false;
 		}
 
 		@Override
 		public void onMouseScrolled(GuiContainer gui, int mousex, int mousey,
 				int scrolled) {
 			
 		}
 
 		//IContainerObjectHandler
 		@Override
 		public void guiTick(GuiContainer gui) {}
 
 		@Override
 		public void refresh(GuiContainer gui) {}
 
 		@Override
 		public void load(GuiContainer gui) {}
 
 		@Override
 		public ItemStack getStackUnderMouse(GuiContainer gui, int mousex,
 				int mousey) {
 			if (ContentTipHandler.revalidateTip(mousex, mousey))
 				return ContentTipHandler.getStackAtPosition(mousex, mousey);
 			return null;
 		}
 
 		private boolean hideTips = false;
 		@Override
 		public boolean objectUnderMouse(GuiContainer gui, int mousex, int mousey) {
			hideTips = ContentTipHandler.showingTip;
 			return ContentTipHandler.showingTip && ContentTipHandler.isPointInTip(mousex, mousey);
 		}
 
 		@Override
 		public boolean shouldShowTooltip(GuiContainer gui) {
 			return !hideTips;
 		}
 		
 		//IContainerDrawHandler
 		@Override
 		public void onPreDraw(GuiContainer gui) {}
 
 		@Override
 		public void renderObjects(GuiContainer gui, int mousex, int mousey) {
 			if (ContentTipHandler.showingTip)
 				ContentTipHandler.renderContentTip(gui.mc, mousex, mousey, 0);
 		}
 
 		@Override
 		public void postRenderObjects(GuiContainer gui, int mousex, int mousey) {}
 
 		@Override
 		public void renderSlotUnderlay(GuiContainer gui, Slot slot) {}
 
 		@Override
 		public void renderSlotOverlay(GuiContainer gui, Slot slot) {}
 		
 	}
 }
