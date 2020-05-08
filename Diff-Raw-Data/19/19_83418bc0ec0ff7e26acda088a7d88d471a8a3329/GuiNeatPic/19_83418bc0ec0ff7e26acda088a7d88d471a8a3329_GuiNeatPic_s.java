 package me.leonblade.neatpics.client;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import me.leonblade.neatpics.CommonProxy;
 import me.leonblade.neatpics.EntityNeatPic;
 import me.leonblade.neatpics.GuiHandler;
 import net.minecraft.client.gui.Gui;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.gui.GuiSmallButton;
 import net.minecraft.client.gui.GuiTextField;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.ThreadDownloadImageData;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.StringTranslate;
 import net.minecraft.world.World;
 
 @SideOnly(Side.CLIENT)
 public class GuiNeatPic extends GuiScreen
 {
 	private GuiTextField urlField;
 	private GuiTextField widthField;
 	private GuiTextField heightField;
 	
 	private boolean heightFieldEnabled;
 	
 	private int xSize = 248;
 	private int ySize = 166;
 	private int xStart;
 	private int yStart;
 	
 	private int xRatio = 1;
 	private int yRatio = 1;
 	
 	private int sizeBlocksX = 1;
 	private int sizeBlocksY = 1;
 	
 	private ThreadDownloadImageData imageData = null;
 	private boolean newImage = false;
 	private boolean imageReady = false;
 	
 	private EntityPlayer player;
 	private World world;
 	private int x;
 	private int y;
 	private int z;
 	private int direction;
 	
 	private float rotate = 0.0f;
 	
 	public GuiNeatPic(EntityPlayer player, World world, int x, int y, int z) 
 	{
 		this.player = player;
 		this.world = world;
 		this.x = x;
 		this.y = y;
 		this.z = z;
 		this.direction = GuiHandler.direction;
 		this.heightFieldEnabled = false;
 	}
 	
 	public void initGui() 
 	{
 		StringTranslate stringTranslate = StringTranslate.getInstance();
 		Keyboard.enableRepeatEvents(true);
 		
 		this.xStart = (this.width / 2) - (this.xSize / 2);
 		this.yStart = (this.height / 2) - (this.ySize / 2);
 		
 		this.controlList.clear();
 		
 		// Done button
 		GuiButton buddon = new GuiButton(0, this.xStart + (this.xSize / 2) - 100, this.yStart + this.ySize - 54, stringTranslate.translateKey("gui.done"));
 		// disabled by default
 		buddon.enabled = false;
 		// add to control list
 		this.controlList.add(buddon);
 		
 		// cancel button
         this.controlList.add(new GuiButton(1, this.xStart + (this.xSize / 2) - 100, this.yStart + this.ySize - 30, stringTranslate.translateKey("gui.cancel")));
         // download image button
         this.controlList.add(new GuiButton(2, this.xStart + (this.xSize / 2) - 100, this.yStart + 46, "Download Image"));
         // x button
         this.controlList.add(new GuiSmallButton(3, this.xStart + (this.xSize / 2) - 8, this.yStart + this.ySize - 78, 16, 20, "x"));
         
         // set url field
 		this.urlField = new GuiTextField(this.fontRenderer, this.xStart + (this.xSize / 2) - 100, this.yStart + 22, 200, 20);
 		this.urlField.setMaxStringLength(32767);
 		this.urlField.setFocused(true);
 		// the width field
 		this.widthField = new GuiTextField(this.fontRenderer, this.xStart + (this.xSize / 2) - 100, this.yStart + this.ySize - 78, 90, 20);
 		this.widthField.setMaxStringLength(4);
 		this.widthField.setText("1");
 		// the height field
 		this.heightField = new GuiTextField(this.fontRenderer, this.xStart + (this.xSize / 2) + 10, this.yStart + this.ySize - 78, 90, 20);
 		this.heightField.setMaxStringLength(4);
 		this.heightField.setText("1");
 		this.heightField.func_82265_c(false);
 		
 		// get initial values
 		EntityNeatPic neatPic = GuiHandler.neatPicReference;
 		if (neatPic != null)
 		{
 			this.widthField.setText(String.valueOf(neatPic.imageWidth));
 			this.heightField.setText(String.valueOf(neatPic.imageHeight));
 			this.urlField.setText(neatPic.textureUrl);
 			this.imageReady = true;
 			GuiButton button = (GuiButton) this.controlList.get(0);
 			button.enabled = true;
 			GuiButton ratioButton = (GuiButton) this.controlList.get(3);
 			this.heightFieldEnabled = true; 
 			ratioButton.displayString = (this.heightFieldEnabled) ? "<>" : "x";
 			this.heightField.func_82265_c(this.heightFieldEnabled);
 			
 			this.sizeBlocksX = neatPic.imageWidth;
 			this.sizeBlocksY = neatPic.imageHeight;
 		}
 	}
 	
 	@Override
 	protected void actionPerformed(GuiButton button) 
 	{
 		if (button.enabled)
 		{
 			// download
 			if (button.id == 2)
 			{
 				try
 				{
 					URL url = new URL(this.urlField.getText());
 					this.imageData = this.mc.renderEngine.obtainImageData(this.urlField.getText(), null);
 					this.newImage = true;
 					this.imageReady = false;
 				}
 				catch (MalformedURLException e) {}
 			}
 			// cancel
 			if (button.id == 1)
 			{
 				this.closeThisScreen(true);
 			}
 			// done button
 			if (button.id == 0)
 			{	            
 	            this.closeThisScreen(false);
 			}
 			// link button
 			if (button.id == 3)
 			{
 				this.heightFieldEnabled = !this.heightFieldEnabled;
 				this.heightField.func_82265_c(this.heightFieldEnabled);
 				button.displayString = (this.heightFieldEnabled) ? "<>" : "x";
 			}
 		}
 	}
 	
 	public void closeThisScreen(boolean cancel)
 	{
 		if (!cancel)
 		{
 			try 
 			{
 				// test for valid url
 				URL url = new URL(this.urlField.getText());
 				// initialize packet
 				Packet250CustomPayload packet = null;
 				
 				// are we referencing an entity
 				if (GuiHandler.neatPicReference == null)
 				{
 					// create the create neat pic packet
 					packet = PacketNeatPicCreate.createPacket(this.x, this.y, this.z, this.direction, this.urlField.getText(), this.sizeBlocksX, this.sizeBlocksY);
 				}
 				else
 				{
 					packet = PacketNeatPicUpdate.createPacket(GuiHandler.neatPicReference.entityId, this.urlField.getText(), this.sizeBlocksX, this.sizeBlocksY);
 				}
 				
 				// queue the packet
 				this.mc.thePlayer.sendQueue.addToSendQueue(packet);
 				
 			}
 			catch (MalformedURLException e) {}
 			
 		}
 		GuiHandler.neatPicReference = null;
 		
 		this.mc.thePlayer.closeScreen();
 	}
 	
 	protected void keyTyped(char par1, int par2) 
 	{
 		// escape closes gui
 		if (par2 == 1)
 		{
 			this.closeThisScreen(true);
 		}
 		
 		super.keyTyped(par1, par2);
 		this.urlField.textboxKeyTyped(par1, par2);
 		this.widthField.textboxKeyTyped(par1, par2);
 		this.heightField.textboxKeyTyped(par1, par2);
 	}
 	
 	protected void mouseClicked(int par1, int par2, int par3) 
 	{
 		super.mouseClicked(par1, par2, par3);
 		this.urlField.mouseClicked(par1, par2, par3);
 		this.widthField.mouseClicked(par1, par2, par3);
 		this.heightField.mouseClicked(par1, par2, par3);
 	}
 	
 	public void updateScreen() 
 	{
 		if (this.mc.renderEngine.getTextureForDownloadableImage(this.urlField.getText(), null) != -1 && this.newImage && !this.imageReady)
 		{
 			// reget the image data
 			this.imageData = this.mc.renderEngine.obtainImageData(this.urlField.getText(), null);
 			
 			// get the dimensions of the image
 			int width = this.imageData.image.getWidth();
 			int height = this.imageData.image.getHeight();
 			
 			// get the ratio
 			int ratio = 1;
 			if (width > height)
 				ratio = (int) Math.ceil((double)width / (double)height);
 			else 
 				ratio = (int) Math.ceil((double)height / (double)width);
 			
 			// store parts
 			this.xRatio = (width < height) ? 1 : ratio;
 			this.yRatio = (width < height) ? ratio : 1;
 			
 			// set new image to false
 			this.newImage = false;
 			// set the image ready to true
 			this.imageReady = true;
 		}
 		
 		GuiButton button = (GuiButton) this.controlList.get(0);
 		if (this.imageReady && this.sizeBlocksX != 0 && this.sizeBlocksY != 0 && !this.widthField.getText().isEmpty())
 		{
 			button.enabled = true;
 		}
 		else
 		{
 			button.enabled = false;
 		}
 		
 		// take the blocks from width and change the height according to the ratio
 		if (!this.heightFieldEnabled)
 		{
			try 
			{
				this.sizeBlocksX = (this.widthField.getText().isEmpty()) ? 1 : Integer.parseInt(this.widthField.getText());
			}
			catch (NumberFormatException e)
			{
				this.sizeBlocksX = 0;
			}
 			this.sizeBlocksY = (int)(((double)this.yRatio / (double)this.xRatio) * this.sizeBlocksX);
 			if (this.sizeBlocksY == 0) this.sizeBlocksY = 1;
 			
 			this.heightField.setText(String.valueOf(this.sizeBlocksY));
 		}
 		else
 		{
 			try
 			{
 				this.sizeBlocksY = Integer.parseInt(this.heightField.getText());
 			}
			catch (NumberFormatException e) {}
 		}
 		
 		this.urlField.updateCursorCounter();
 		this.widthField.updateCursorCounter();
 		this.heightField.updateCursorCounter();
 	}
 	
 	public void onGuiClosed() 
 	{
 		Keyboard.enableRepeatEvents(false);
 	}
 	
 	public void drawScreen(int par1, int par2, float par3) 
 	{
 		this.drawDefaultBackground();
 		
 		int neat_gui = this.mc.renderEngine.getTexture(CommonProxy.GUI_PNG);
 		int woody = this.mc.renderEngine.getTexture(CommonProxy.SPIN_PNG);
 		
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         
         this.mc.renderEngine.bindTexture(neat_gui);
         
         this.drawTexturedModalRect(this.xStart, this.yStart, 0, 0, this.xSize, this.ySize);
         
         if (!this.imageReady && this.newImage)
         {        	
         	GL11.glPushMatrix();
         	GL11.glEnable(GL12.GL_RESCALE_NORMAL);
 			this.mc.renderEngine.bindTexture(woody);
 			GL11.glTranslated(this.xStart + (this.xSize / 2), this.yStart + (this.ySize / 2) - 6, 0);
 			GL11.glRotatef(this.rotate+=3.0f, 0.0f, 0.0f, 1.0f);
 			Tessellator tessellator = Tessellator.instance;
 			tessellator.startDrawing(GL11.GL_QUADS);
 	        tessellator.addVertexWithUV(-8, -8, 0, 1, 1);
 	        tessellator.addVertexWithUV(-8, 8, 0, 1, 0);
 	        tessellator.addVertexWithUV(8, 8, 0, 0, 0);
 	        tessellator.addVertexWithUV(8, -8, 0, 0, 1);
 	        tessellator.draw();
 	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
 			GL11.glPopMatrix();
 		}
 		
 		this.urlField.drawTextBox();
 		this.widthField.drawTextBox();
 		this.heightField.drawTextBox();
 		
 		this.drawString(this.fontRenderer, "URL", this.xStart + 24, this.yStart + 8, 0xFFFFFF);
 		this.drawString(this.fontRenderer, "Size", this.xStart + 24, this.yStart + this.ySize - 92, 0xFFFFFF);
 		
 		super.drawScreen(par1, par2, par3);
 	}
 }
