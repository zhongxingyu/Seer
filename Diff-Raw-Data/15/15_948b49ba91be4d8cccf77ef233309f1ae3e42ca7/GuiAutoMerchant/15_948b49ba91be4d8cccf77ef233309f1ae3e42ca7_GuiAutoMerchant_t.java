 package mods.alice.villagerblock.client.gui;
 
 //import java.io.ByteArrayOutputStream;
 //import java.io.DataOutputStream;
 import java.util.List;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import mods.alice.villagerblock.inventory.ContainerAutoMerchant;
 import mods.alice.villagerblock.tileentity.TileEntityVillager;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiButtonMerchant;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.util.StatCollector;
 import net.minecraft.village.MerchantRecipe;
 import net.minecraft.village.MerchantRecipeList;
 import net.minecraft.world.World;
 
 public class GuiAutoMerchant extends GuiContainer
 {
 	static ResourceLocation guiTrading;
 	TileEntityVillager villager;
 	GuiButtonMerchant nextRecipeButtonIndex;
 	GuiButtonMerchant previousRecipeButtonIndex;
 	String guiCaption;
 	String inventoryCaption;
 	int currentRecipeIndex;
 
 	static
 	{
 		guiTrading = new ResourceLocation("textures/gui/container/villager.png");
 	}
 
 	public GuiAutoMerchant(InventoryPlayer playerInventory, TileEntityVillager _villager, World world)
 	{
 		super(new ContainerAutoMerchant(playerInventory, _villager, world));
 
 		currentRecipeIndex = 0;
 		villager = _villager;
 		guiCaption = StatCollector.translateToLocal("container.VillagerBlock");
 		inventoryCaption = StatCollector.translateToLocal("container.inventory");
 	}
 
 	@Override
 	protected void actionPerformed(GuiButton button)
 	{
 		//ByteArrayOutputStream byteOut;
 		//DataOutputStream streamOut;
 		//NetClientHandler netClient;
 		//Packet tradeSelectPacket;
 		//boolean flag;
 
 		//if(button == nextRecipeButtonIndex)
 		//{
 		//	currentRecipeIndex++;
 		//	flag = true;
 		//}
 		//else if(button == previousRecipeButtonIndex)
 		//{
 		//	currentRecipeIndex--;
 		//	flag = true;
 		//}
 		//else
 		//{
 		//	flag = false;
 		//}
 
 		//if(flag)
 		//{
 		//	//((ContainerAutoMerchant)inventorySlots).setCurrentRecipeIndex(currentRecipeIndex);
 		//	byteOut = new ByteArrayOutputStream();
 		//	streamOut = new DataOutputStream(byteOut);
 		//	netClient = mc.getNetHandler();
 
 		//	try
 		//	{
 		//		streamOut.writeInt(currentRecipeIndex);
 
 		//		tradeSelectPacket = new Packet250CustomPayload("MC|TrSel", byteOut.toByteArray());
 		//		netClient.addToSendQueue(tradeSelectPacket);
 		//	}
 		//	catch(Exception e)
 		//	{
 		//	}
 		//}
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
 	{
 		MerchantRecipe recipe;
 		MerchantRecipeList recipes;
 		int xCenter, yCenter;
 
 		GL11.glColor4f(1, 1, 1, 1);
		mc.renderEngine.bindTexture(guiTrading);
 
 		xCenter = (width - xSize) / 2;
 		yCenter = (height - ySize) / 2;
 
 		drawTexturedModalRect(xCenter, yCenter, 0, 0, xSize, ySize);
 
 		recipes = villager.getRecipes(mc.thePlayer);
 		if((recipes != null) && !(recipes.isEmpty()))
 		{
 			recipe = (MerchantRecipe)recipes.get(currentRecipeIndex);
 
 			if(recipe.func_82784_g())
 			{
				mc.renderEngine.bindTexture(guiTrading);
 				GL11.glColor4f(1, 1, 1, 1);
 				GL11.glDisable(GL11.GL_LIGHTING);
 				drawTexturedModalRect(guiLeft + 83, guiTop + 21, 212, 0, 28, 21);
 				drawTexturedModalRect(guiLeft + 83, guiTop + 51, 212, 0, 28, 21);
 			}
 		}
 	}
 
 	@Override
 	protected void drawGuiContainerForegroundLayer(int x, int y)
 	{
 		fontRenderer.drawString(guiCaption, (xSize - fontRenderer.getStringWidth(guiCaption)) / 2, 6, 0x404040);
 		fontRenderer.drawString(inventoryCaption, 8, ySize - 94, 0x404040);
 	}
 
 	@Override
 	public void drawScreen(int x, int y, float par3)
 	{
 		ItemStack slot[];
 		MerchantRecipe recipe;
 		MerchantRecipeList recipes;
 		int xCenter, yCenter;
 
 		super.drawScreen(x, y, par3);
 
 		recipes = villager.getRecipes(mc.thePlayer);
 		if((recipes != null) && !(recipes.isEmpty()))
 		{
 			xCenter = (width - xSize) / 2;
 			yCenter = (height - ySize) / 2;
 
 			recipe = (MerchantRecipe)recipes.get(currentRecipeIndex);
 
 			GL11.glPushMatrix();
 
 			slot = new ItemStack[3];
 			slot[0] = recipe.getItemToBuy();
 			slot[1] = recipe.getSecondItemToBuy();
 			slot[2] = recipe.getItemToSell();
 
 			RenderHelper.enableGUIStandardItemLighting();
 
 			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
 			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
 			GL11.glEnable(GL11.GL_LIGHTING);
 
 			itemRenderer.zLevel = 100;
 			itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, slot[0], xCenter + 36, yCenter + 24);
 			itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, slot[0], xCenter + 36, yCenter + 24);
 
 			if(slot[1] != null)
 			{
 				itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, slot[1], xCenter + 62, yCenter + 24);
 				itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, slot[1], xCenter + 62, yCenter + 24);
 			}
 
 			itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, slot[2], xCenter + 120, yCenter + 24);
 			itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, slot[2], xCenter + 120, yCenter + 24);
 
 			itemRenderer.zLevel = 0;
 
 			GL11.glDisable(GL11.GL_LIGHTING);
 
 			if(isPointInRegion(36, 24, 16, 16, x, y))
 			{
 				drawItemStackTooltip(slot[0], x, y);
 			}
 			else if((slot[1] != null) && isPointInRegion(62, 24, 16, 16, x, y))
 			{
 				drawItemStackTooltip(slot[1], x, y);
 			}
 			else if(isPointInRegion(120, 24, 16, 16, x, y))
 			{
 				drawItemStackTooltip(slot[2], x, y);
 			}
 
 			GL11.glPopMatrix();
 			GL11.glEnable(GL11.GL_LIGHTING);
 			GL11.glEnable(GL11.GL_DEPTH_TEST);
 			RenderHelper.enableStandardItemLighting();
 		}
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	List<GuiButton> cast(List l)
 	{
 		return (List<GuiButton>)l;
 	}
 
 	@Override
 	public void initGui()
 	{
 		List<GuiButton> _buttonList;
 		int xCenter, yCenter;
 
 		super.initGui();
 
 		xCenter = (width - xSize) / 2;
 		yCenter = (height - ySize) / 2;
 
 		nextRecipeButtonIndex = new GuiButtonMerchant(1, xCenter + 147, yCenter + 23, true);
 		previousRecipeButtonIndex = new GuiButtonMerchant(2, xCenter + 17, yCenter + 23, false);
 
 		_buttonList = cast(buttonList);
 		_buttonList.add(nextRecipeButtonIndex);
 		_buttonList.add(previousRecipeButtonIndex);
 
 		nextRecipeButtonIndex.enabled = false;
 		previousRecipeButtonIndex.enabled = false;
 	}
 
 	@Override
 	public void updateScreen()
 	{
 		//MerchantRecipeList merchantrecipelist;
 
 		super.updateScreen();
 
 		//merchantrecipelist = villager.getRecipes(mc.thePlayer);
 		//if(merchantrecipelist != null)
 		//{
 		//	nextRecipeButtonIndex.enabled = currentRecipeIndex < (merchantrecipelist.size() - 1);
 		//	previousRecipeButtonIndex.enabled = currentRecipeIndex > 0;
 		//}
 	}
 }
