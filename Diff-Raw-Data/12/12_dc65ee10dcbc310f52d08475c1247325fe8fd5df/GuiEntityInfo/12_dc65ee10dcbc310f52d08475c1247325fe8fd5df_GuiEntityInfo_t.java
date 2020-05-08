 package demonmodders.crymod.client.gui.entityinfo;
 
 import java.util.List;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiTextField;
 import net.minecraft.client.renderer.OpenGlHelper;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.client.renderer.entity.RenderManager;
 import net.minecraft.util.StringTranslate;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import demonmodders.crymod.client.gui.AbstractGuiContainer;
 import demonmodders.crymod.client.gui.GuiButtonImage;
 import demonmodders.crymod.client.render.RenderZombieBase;
 import demonmodders.crymod.common.entities.SummonableBase;
 import demonmodders.crymod.common.gui.ContainerEntityInfo;
 import demonmodders.crymod.common.inventory.InventorySummonable;
 import demonmodders.crymod.common.network.PacketRenameEntity;
 
 public class GuiEntityInfo extends AbstractGuiContainer<ContainerEntityInfo, InventorySummonable> {
 
 	private int mouseX;
 	private int mouseY;
 	private static final int SMALL_BUTTON_WIDTH = 77;
 	private static final int SMALL_BUTTON_HEIGHT = 13;
 	private static final int BAR_WIDTH = 91;
 	private static final int BAR_HEIGHT = 5;
 	private static final int BAR_TEXTURE_Y = 195;
 	private static final int BAR_TEXTURE_X = 0;
 	private static final int ORB_TEXTURE_X = 0;
 	private static final int ORB_TEXTURE_Y = 200;
 	private static final int ORB_WIDTH = 8;
 	private static final int ORB_HEIGHT = 8;
 	
 	private boolean isRenaming = false;
 	
 	private GuiButton buttonConfirm;
 	private GuiButton buttonRename;
 	private GuiButton buttonRenameDone;
 	
 	private GuiTextField textFieldEntityName = null;
 
 	public GuiEntityInfo(ContainerEntityInfo container) {
 		super(container);
 		xSize = 248;
 		ySize = 195;
 	}
 
 	@Override
 	protected String getTextureFile() {
 		return "/demonmodders/crymod/resource/tex/creatureInterface.png";
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
 		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
 			
 		SummonableBase entity = container.getEntity();
 	
 		// Draw the Entity Model
 		int par1 = guiLeft + 195;
 		int par2 = guiTop + 52;
 		int scale = 18;
 		float par4 = (float) (guiLeft + 51) - mouseX;
 		float par5 = (float) (guiTop + 75 - 50) - mouseY;
 
 		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
 		GL11.glPushMatrix();
 		GL11.glTranslatef((float) par1, (float) par2, 50.0F);
 		GL11.glScalef((float) (-scale), (float) scale, (float) scale);
 		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
 		float var6 = entity.renderYawOffset;
 		float var7 = entity.rotationYaw;
 		float var8 = entity.rotationPitch;
 		GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
 		RenderHelper.enableStandardItemLighting();
 		GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
 		GL11.glRotatef(-((float) Math.atan((double) (par5 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
 		entity.renderYawOffset = (float) Math.atan((double) (par4 / 40.0F)) * 20.0F;
 		entity.rotationYaw = (float) Math.atan((double) (par4 / 40.0F)) * 40.0F;
 		entity.rotationPitch = -((float) Math.atan((double) (par5 / 40.0F))) * 20.0F;
 		entity.rotationYawHead = entity.rotationYaw;
 		GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
 		RenderManager.instance.playerViewY = 180.0F;
 		RenderZombieBase.disableLabel();
 		RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
 		RenderZombieBase.enableLabel();
 		entity.renderYawOffset = var6;
 		entity.rotationYaw = var7;
 		entity.rotationPitch = var8;
 		GL11.glPopMatrix();
 		RenderHelper.disableStandardItemLighting();
 		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
 		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
 		GL11.glDisable(GL11.GL_TEXTURE_2D);
 		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
 
 		// draw the health
 		drawBar("crymod.ui.health", 13, 35, entity.getHealth(), entity.getMaxHealth());
 		
 		drawBar("crymod.ui.speed", 13, 66, entity.getSpeed(), SummonableBase.MAX_SPEED);
 		
 		drawBar("crymod.ui.power", 13, 96, entity.getPower(), SummonableBase.MAX_POWER);
 		
 		drawBar("crymod.ui.control", 13, 126, entity.getControl(), SummonableBase.MAX_CONTROL);
 		
 		// Draw the name
 		fontRenderer.drawString(textFieldEntityName.getText() + (textFieldEntityName.getText().equals(entity.getEntityName()) ? "" : "*"), guiLeft + 132, guiTop + 106, 0x000000);
 		
 		// draw the xp cost
 		fontRenderer.drawString("x " + container.calculateCurrentXpCost(), guiLeft + 27, guiTop + 148, 0x000000);
 }
 	private void drawBar(String label, int x, int y, int value, int maxValue) {
 		float factor = (float) BAR_WIDTH / maxValue;
 		int scaledValue = (int) (value * factor);
 		GL11.glColor3f(1, 1, 1);
 		mc.renderEngine.bindTexture(mc.renderEngine.getTexture(getTextureFile()));
 		drawTexturedModalRect(x + guiLeft, y + guiTop, BAR_TEXTURE_X, BAR_TEXTURE_Y, scaledValue, BAR_HEIGHT);
 		String displayString = StringTranslate.getInstance().translateKeyFormat(label, value, maxValue);
 		fontRenderer.drawString(displayString, guiLeft + x + BAR_WIDTH - 2 - fontRenderer.getStringWidth(displayString), guiTop + y - 10, 0x000000);
 	}
 
 	@Override
 	public void drawScreen(int mouseX, int mouseY, float par3) {
 		if (!isRenaming) {
 			super.drawScreen(mouseX, mouseY, par3);
 		} else {
 			drawDefaultBackground();
 			for (GuiButton button : (List<GuiButton>)controlList) {
 				button.drawButton(mc, mouseX, mouseY);
 			}
 			textFieldEntityName.drawTextBox();
 		}
 		this.mouseX = mouseX;
 		this.mouseY = mouseY;
 	}
 
 	@Override
 	public void initGui() {
 		super.initGui();
 		controlList.add((buttonConfirm = new GuiButtonImage(ContainerEntityInfo.BUTTON_CONFIRM, width / 2 - SMALL_BUTTON_WIDTH / 2, guiTop + ySize - 50, SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT, 0, 256 - SMALL_BUTTON_HEIGHT * 2, getTextureFile()).setDisplayString("Confirm")));
 		controlList.add((buttonRename = new GuiButtonEntityInfoRename(ContainerEntityInfo.BUTTON_RENAME, guiLeft + 217, guiTop + 98)));
 		controlList.add((buttonRenameDone = new GuiButton(ContainerEntityInfo.BUTTON_RENAME_DONE, guiLeft + 20, guiTop + 200, "Done")));
 		
 		
 		String textFieldContents = textFieldEntityName == null ? container.getEntity().getEntityName() : textFieldEntityName.getText();
		textFieldEntityName = new GuiTextField(fontRenderer, width / 2 - 100, guiTop + 20, 200, 20);
 		textFieldEntityName.setText(textFieldContents);
 		setRenaming(isRenaming);
 	}
 	
 	private void setRenaming(boolean renaming) {
 		buttonConfirm.drawButton = buttonRename.drawButton = !renaming;
 		isRenaming = buttonRenameDone.drawButton = renaming;
 		textFieldEntityName.setFocused(renaming);
 	}
 
 	@Override
 	protected void actionPerformed(GuiButton button) {
 		if (button.id != ContainerEntityInfo.BUTTON_CONFIRM) {
 			setRenaming(button.id == ContainerEntityInfo.BUTTON_RENAME);
 		} else {
 			new PacketRenameEntity(textFieldEntityName.getText(), container.windowId).sendToServer();
 		}
 		if (button.id == ContainerEntityInfo.BUTTON_RENAME_DONE || button.id == ContainerEntityInfo.BUTTON_CONFIRM) {
 			container.setNewName(textFieldEntityName.getText());
 		}
 		super.actionPerformed(button);
 	}
 
 	@Override
 	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
 		if (!isRenaming) {
 			super.mouseClicked(mouseX, mouseY, mouseButton);
 		} else {
 			for (GuiButton button : (List<GuiButton>)controlList) {
 				if (button.mousePressed(mc, mouseX, mouseY)) {
 					mc.sndManager.playSoundFX("random.click", 1, 1);
                     actionPerformed(button);
 				}
 			}
 			textFieldEntityName.mouseClicked(mouseX, mouseY, mouseButton);
 		}
 	}
 
 	@Override
 	protected void keyTyped(char keyChar, int keyCode) {
 		if (keyCode == Keyboard.KEY_ESCAPE || (keyCode == mc.gameSettings.keyBindInventory.keyCode && !isRenaming)) {
 			if (isRenaming) {
 				setRenaming(false);
 			} else {
				mc.thePlayer.closeScreen();
 			}
 		} else if (isRenaming && textFieldEntityName.isFocused()) {
 			textFieldEntityName.textboxKeyTyped(keyChar, keyCode);
 		}
 	}
 
 	@Override
 	public void updateScreen() {
 		super.updateScreen();
 		textFieldEntityName.updateCursorCounter();
 	}
 }
