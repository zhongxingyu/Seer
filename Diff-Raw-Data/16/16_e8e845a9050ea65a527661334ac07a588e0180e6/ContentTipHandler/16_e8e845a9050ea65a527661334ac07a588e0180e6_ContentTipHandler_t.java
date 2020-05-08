 package ml.boxes.client;
 
 import java.util.EnumSet;
 
 import ml.boxes.Boxes;
 import ml.boxes.client.gui.GuiBox;
 import ml.boxes.data.Box;
 import ml.boxes.data.ItemBoxContainer;
 import ml.boxes.inventory.ContainerBox;
 import ml.boxes.inventory.ContentTip;
 import ml.boxes.item.ItemBox;
 import ml.core.geo.GeoMath;
 import ml.core.geo.Rectangle;
 import ml.core.geo.Vector2;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Slot;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.ObfuscationReflectionHelper;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class ContentTipHandler implements ITickHandler {
 
 	public static ContentTip openTip;
 	
 	private static Slot hoverSlot;
 	private static long tickerTime = 0;
 
 	private static Rectangle gcBounds = new Rectangle(0, 0, 0, 0);
 	
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {
 
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 		if (type.contains(TickType.RENDER) && !Boxes.neiInstalled){ //NEI Provides a better place for doing this. Use it if we can
 			Minecraft mc = FMLClientHandler.instance().getClient();
 			if (mc.currentScreen instanceof GuiContainer){
 				Vector2<Integer> m = GeoMath.getScaledMouse();
 				GL11.glPushMatrix();
 				GL11.glTranslatef(0F, 0F, 200F);
 				renderContentTip(mc, m.X, m.Y, (Float)tickData[0]);
 				GL11.glPopMatrix();
 			}
 		}else if (type.contains(TickType.CLIENT)){
 			updateCurrentTip();
 		}
 	}
 	
 	// Do calculations for the tip
 	private static void updateCurrentTip(){
 		Minecraft mc = FMLClientHandler.instance().getClient();
 		if (mc.currentScreen instanceof GuiContainer){
 			GuiContainer asGuiContainer = (GuiContainer)mc.currentScreen;
 
			int guiXSize = ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, (GuiContainer)mc.currentScreen, "field_" + "74194_b", "xSize", "b");
			int guiYSize = ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, (GuiContainer)mc.currentScreen, "field_" + "74195_c", "ySize", "c");
 
 			Vector2<Integer> m = GeoMath.getScaledMouse();
 
 			gcBounds.width = asGuiContainer.width;
 			gcBounds.height = asGuiContainer.height;
 			
 			gcBounds.xCoord = (asGuiContainer.width - guiXSize) / 2;
 			gcBounds.yCoord = (asGuiContainer.height - guiYSize) / 2;
 			
 			if (openTip == null || !openTip.revalidate(m.X, m.Y)){
 				openTip = null;
 				
 				boolean thoverSlot = false;
 				for (Object objSlt : asGuiContainer.inventorySlots.inventorySlots){
 					Slot slt = (Slot)objSlt;
 					if (GeoMath.pointInRect(m.X, m.Y, gcBounds.xCoord + slt.xDisplayPosition, gcBounds.yCoord + slt.yDisplayPosition, 16, 16)){
 						thoverSlot = true;
 						if (hoverSlot != slt)
 							tickerTime = mc.getSystemTime();
 						hoverSlot = slt;
 					}
 				}
 				if (!thoverSlot)
 					hoverSlot = null;
 			}
 			
 			if (hoverSlot != null &&
 					hoverSlot.getHasStack() &&
 					(hoverSlot.getStack().getItem() instanceof ItemBox) &&
 					(!Boxes.config.shiftForTip || asGuiContainer.isShiftKeyDown()) &&
 					(mc.getSystemTime() - tickerTime > Boxes.config.tipReactionTime || asGuiContainer.isShiftKeyDown()) &&
 					!(asGuiContainer instanceof GuiBox && ((ContainerBox)asGuiContainer.inventorySlots).box instanceof ItemBoxContainer && mc.thePlayer.inventory.currentItem == hoverSlot.getSlotIndex()) && //((ItemIBox)((ContainerBox)asGuiContainer.inventorySlots).box).stack == hoverSlot.getStack()
 					openTip == null
 					)
 			{
 				ItemBoxContainer iib = getItemIBox();
 				Box bd = iib.getBox();
 				
 				if (bd.canOpenContentTip() && (bd.canOpenContentPreview() || canBeInteractive(mc))){
 					openTip = bd.createContentTip(hoverSlot, gcBounds);
 				}
 			}
 			if (openTip != null){
 				openTip.interacting = canBeInteractive(mc);
 				openTip.tick(mc);
 			}
 		}
 	}
 	
 	public static boolean revalidateCurrentTip(int mx, int my){
 		if (openTip != null && openTip.revalidate(mx, my)){
 			return true;
 		}
 		openTip = null;
 		return false;
 	}
 	
 	public static boolean canBeInteractive(Minecraft mc){
 		return (Boxes.neiInstalled && mc.currentScreen.isShiftKeyDown() && hoverSlot.inventory instanceof InventoryPlayer);
 	}
 
 	private static ItemBoxContainer getItemIBox(){
 		return new ItemBoxContainer(hoverSlot.getStack());
 	}
 
 	//Render the tip
 	public static void renderContentTip(Minecraft mc, int mx, int my, float tickTime){
 		if (revalidateCurrentTip(mx, my)){
 			openTip.renderTick(mc, mx, my);
 		}
 	}
 	
 	@Override
 	public EnumSet<TickType> ticks() {
 		return EnumSet.of(TickType.RENDER, TickType.CLIENT);
 	}
 
 	@Override
 	public String getLabel() {
 		return "Boxes";
 	}
 }
