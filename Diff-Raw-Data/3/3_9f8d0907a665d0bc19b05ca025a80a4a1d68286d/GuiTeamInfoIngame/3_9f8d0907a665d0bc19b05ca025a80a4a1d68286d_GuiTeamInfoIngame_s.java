 package kovu.teamstats;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import org.lwjgl.opengl.GL11;
 
 import net.minecraft.client.gui.FontRenderer;
 import net.minecraft.client.gui.Gui;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.src.ModLoader;
 import net.minecraft.util.EnumChatFormatting;
 
 public class GuiTeamInfoIngame extends GuiScreen {
 	
 	//For more, double everything
 	public boolean dragMode = false;
 	public static int dragModeX = 0;
 	public static int dragModeY = 0;
 	public static boolean isModeOpen;
 	private static boolean init = false;
 
 	public int blue = 0x222200ff;
 	public int red = 0x22ff0000;
 	public int green = 0x2200ff00;
 	public static FontRenderer font;
 	
 	
 	public void mouseClicked(int i, int j, int k)
 	{
 		//drawRect(0 + dragModeX, 0 + dragModeY, 120 + dragModeX, 12 + dragModeY, 0x2200ff00);
 		//for a toggle, to stop it from derping out, have the 120 change to something smaller
 		if(0 + dragModeX < i && 120 + dragModeX > i && 0 + dragModeY < j && 12 + dragModeY > j)
 		{
 			dragMode = true;
 		}
 		
 		super.mouseClicked(i, j, k);
 	}
 	
 	
 	public static void clickableGuiMode(FontRenderer fr)
 	{
 		if(isModeOpen)
 		{
 			drawRect(110 + dragModeX, 2 + dragModeY, 118 + dragModeX, 10 + dragModeY, 0x2200ff00);
 		}
 		else
 		{
 			drawRect(0 + dragModeX, 0 + dragModeY, 120 + dragModeX, 12 + dragModeY, 0x2200ff00);
 		}
 		fr.drawStringWithShadow(ModLoader.getMinecraftInstance().thePlayer.username, 5 + dragModeX, 2 + dragModeY, 0xfffffa);
 	}
 	
 	public void drawScreen(int i, int j, float f)
 	{
 		mouseDraggedMode(i, j);
 		clickableGuiMode(fontRenderer);
 		fontRenderer.drawStringWithShadow("TeamStats Dev 0.1.0", 2, 2, 0x00ff00);
 		this.font = fontRenderer;
 		super.drawScreen(i, j, f);
 		GL11.glColor4f(1, 1, 1, 1);
 		getCroppedFacialImage("Rainfur");
		drawTexturedModalRect(9, 9, 0, 0, 250, 300);
 //		GuiUtils.drawBorderedRect(100, 100, 500, 300, 0, 0, 0, 125, (float) 1.4, 0, 0, 0, 255);
 	}
 	
 	public static void drawMain()
 	{
 		if(font == null)
 		{
 			if(init == false)
 			{
 				ModLoader.getMinecraftInstance().thePlayer.addChatMessage(EnumChatFormatting.GREEN+"Please press = to initialize and customize TeamStats"+EnumChatFormatting.RESET);
 				init = true;
 			}
 			return;
 		}
 		font.drawStringWithShadow("TeamStats Dev 0.1.0", 2, 2, 0x00ff00);
 		clickableGuiMode(font);
 	}
 	
 	public void mouseDraggedMode(int i, int j)
 	{
 		if(dragMode)
 		{
 			dragModeX = i - 60;
 			dragModeY = j;
 		}
 	}
 	
 	public boolean doesGuiPauseGame() {
 		
 		return false;
 		
 	}
 	
 	protected void mouseMovedOrUp(int i, int j, int k)
 	{
 		if(k == 0)
 		{
 			dragMode = false;
 		}
 	}
 	
 	public void getCroppedFacialImage(String username)
 	{
 		File skin = new File(mc.getMinecraftDir() + File.separator +
 				"dlskins" + File.separator + username + ".png");
 		try
 		{
 			BufferedImage skinBR = ImageIO.read(skin);
 			BufferedImage skinCropped = skinBR.getSubimage(8, 8, 8, 8);
 			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.allocateAndSetupTexture(skinCropped));
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
