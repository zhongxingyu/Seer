 package mods.nordwest.client.gui;
 
 import java.awt.Color;
 import java.util.Random;
 
 import mods.nordwest.blocks.BlockAltar;
 import mods.nordwest.client.renders.BlockCandleRenderer;
 import mods.nordwest.common.NordWest;
 import mods.nordwest.utils.EnumFormatting;
 import mods.nordwest.utils.EffectsLibrary;
 import mods.nordwest.tileentity.TileEntityAltar;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.gui.GuiTextField;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.StringTranslate;
 import net.minecraft.world.World;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class GuiAltar extends GuiScreen
 {
 	/*public TileEntityAltar tileEntity;*/
 	private static EntityPlayer player;
 	private static World world;
 	private GuiTextField textfield;
 	private int xSize = 176;
 	private int ySize = 166;
 	
 	String doublePoint = ": ";
 	Random random = new Random();
 	private int variability = random.nextInt(10);
 
 	public GuiAltar(EntityPlayer entityplayer, World currentworld)
 	{
 		player = entityplayer;
 		world = currentworld;
 	}
 	
 	@Override
 	public void initGui()
 	{
 		String DefaultText = LanguageRegistry.instance().getStringLocalization("homing.default");
 		DefaultText = DefaultText.replaceAll("%p", player.getEntityName());
 		
 		buttonList.clear();
 		/* Buttons with their settings! */
         buttonList.add(new GuiButton(0, width / 2 - 82, height / 2 + 45, 80, 20, LanguageRegistry.instance().getStringLocalization("homing.done")));
         buttonList.add(new GuiButton(1, width / 2 + 2, height / 2 + 45, 80, 20, LanguageRegistry.instance().getStringLocalization("homing.back")));
         /* Text field & its settings! */
         textfield = new GuiTextField(fontRenderer, width / 2 - 72, height / 2 - 37, 105, 20);
         textfield.setFocused(false);
         textfield.setCanLoseFocus(true);
         textfield.setText(DefaultText);
         textfield.setMaxStringLength(28);
         textfield.setCursorPosition(0);
         textfield.setEnableBackgroundDrawing(false);
         }
 
 	public void mouseClicked(int i, int j, int k){
         super.mouseClicked(i, j, k);
         textfield.mouseClicked(i, j, k);
         
         if (j >= height / 2 - 41 && j <= height / 2 - 27 && i >= width / 2 + 30 && i <= width / 2 + 46)
             {
     			player.playSound("random.click", 0.5F, 1.2F);
     			//((TileEntityAltar)this.mobInv.ent).setColor(((TileEntityAltar)this.mobInv.ent).getColor() - 1);
     			//if (((TileEntityAltar)this.mobInv.ent).getColor() < -1) ((TileEntityAltar)this.mobInv.ent).setColor(15);	
             } else if (j >= height / 2 - 41 && j <= height / 2 - 27 && i >= width / 2 + 60 && i <= width / 2 + 76)
             {
     			player.playSound("random.click", 0.5F, 1.2F);
     			//((TileEntityAltar)this.mobInv.ent).setColor(((TileEntityAltar)this.mobInv.ent).getColor() + 1);
    			 	//if (((TileEntityAltar)this.mobInv.ent).getColor() > 15) ((TileEntityAltar)this.mobInv.ent).setColor(-1);
             }
         }
 	
 	@Override
 	public void onGuiClosed(){
 		player.playSound("random.fizz", 0.5F, 1.2F);
     }
 	
 	@Override
 	public void drawScreen(int i, int j, float f)
 	{
 		/* Background and Title */
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture("/mods/mekanism/gui/GuiStopwatch.png");
         drawTexturedModalRect(width / 2 - 88, height / 2 - 80, 0, 0, 176, 166);
         fontRenderer.drawString(LanguageRegistry.instance().getStringLocalization("homing.title"), width / 2 - 75, height / 2 - 73, 4210752);
 		textfield.drawTextBox();
 		/* General Content */
 		drawString(fontRenderer, LanguageRegistry.instance().getStringLocalization("homing.subtitle") + doublePoint, width / 2 - 75, height / 2 - 52, 0xF0DC82);
 		drawString(fontRenderer, LanguageRegistry.instance().getStringLocalization("homing.remember") + doublePoint, width / 2 - 75, height / 2 - 22, 0xF0DC82);
 		drawString(fontRenderer, LanguageRegistry.instance().getStringLocalization("homing.desc.s1"), width / 2 - 75, height / 2 - 10, 0xF0DC82);
 		drawString(fontRenderer, LanguageRegistry.instance().getStringLocalization("homing.desc.s2"), width / 2 - 75, height / 2, 0xF0DC82);
 		drawString(fontRenderer, LanguageRegistry.instance().getStringLocalization("homing.desc.s3"), width / 2 - 75, height / 2 + 10, 0xF0DC82);
 		if (StringTranslate.getInstance().getCurrentLanguage().equals("ru_RU")){
 		drawString(fontRenderer, LanguageRegistry.instance().getStringLocalization("homing.desc.s4"), width / 2 - 75, height / 2 + 20, 0xF0DC82);	
 		} else {
 		String randomAlert = "Something's wrong!";
 		if (variability <= 3) {
 			randomAlert = LanguageRegistry.instance().getStringLocalization("homing.alert.1");
 		} else if (variability <=8) {
 			randomAlert = LanguageRegistry.instance().getStringLocalization("homing.alert.2");
 		} else {
 			randomAlert = LanguageRegistry.instance().getStringLocalization("homing.alert.rare");
 		}
 		drawString(fontRenderer, randomAlert, width / 2 - 75, height / 2 + 25, 0xF0DC82);
 		}
 		super.drawScreen(i, j, f);
         GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
         GL11.glEnable(GL11.GL_CULL_FACE);
         GL11.glEnable(GL11.GL_ALPHA_TEST);
         GL11.glEnable(GL11.GL_DEPTH_TEST);
 	}
 	
 	@Override
     public void keyTyped(char c, int i)
     {
         super.keyTyped(c, i);
         textfield.textboxKeyTyped(c, i);
         if (i == 1)
         {
             mc.displayGuiScreen(null);
         }
     }
 	
 	@Override
 	public boolean doesGuiPauseGame()
 	{
 		return false;
 	}
 	
 	@Override
 	public void actionPerformed(GuiButton guibutton)
 	{
 		if(guibutton.id == 0)
 		{
 			//PacketHandler.sendPacketDataInt(EnumPacketType.TIME, 0);
 			mc.displayGuiScreen(null);
 		}
 		if(guibutton.id == 1)
 		{
 			mc.displayGuiScreen(null);
 		}
 	}
 }
