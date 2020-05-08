 package hsb.client.gui;
 
 import hsb.configuration.Settings;
 import hsb.lib.Textures;
 import hsb.network.NetworkManager;
 import hsb.tileentity.TileEntityHsb;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.entity.player.EntityPlayer;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 /**
  * only displayed to one user (the placer)
  * @author Nuklearwurst
  *
  */
 public class GuiBlockBuilding extends GuiScreen{
 	
 	private int xSize;
 	private int ySize;
 	
 	private int port = 0;
 	private int xPos;
 	private int yPos;
 	
 	TileEntityHsb te;
 	EntityPlayer player;
 	
 	public GuiBlockBuilding(TileEntityHsb te, EntityPlayer player) {
 		xSize = 176;
 		ySize = 105;
 		this.te = te;
 		this.player = player;
 	}
 	@Override
 	protected void actionPerformed(GuiButton guibutton) {
 		switch (guibutton.id)
 		{
 			case 0:
 				updatePort(-1);
 				break;
 			case 1:
 				updatePort(1);
 				break;
 			case 2:
 				updatePort(-10);
 				break;
 			case 3:
 				updatePort(10);
 				break;
 				
 			case 4:
 				this.mc.displayGuiScreen(null);
 				break;
 		}
 	}
 	
 	protected void drawGuiContainerBackgroundLayer (float f)
 	{
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		mc.renderEngine.bindTexture(Textures.GUI_LOCATION+ "GuiBlockPlacer.png");
 		int j = (width - xSize) / 2;
 		int k = (height - ySize) / 2;
 		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
 	}
 	
 	@Override
 	public void drawScreen(int i, int j, float f)
     {
 		drawDefaultBackground();
         int k = (this.width - this.xSize) / 2;
         int l = (this.height - this.ySize) / 2;
         drawGuiContainerBackgroundLayer(f);
         GL11.glPushMatrix();
         GL11.glTranslatef(k, l, 0.0F);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(GL12.GL_RESCALE_NORMAL);
         RenderHelper.disableStandardItemLighting();
         GL11.glDisable(GL11.GL_LIGHTING);
         GL11.glDisable(GL11.GL_DEPTH_TEST);
         GL11.glPopMatrix();
         super.drawScreen(i, j, f);
         GL11.glEnable(GL11.GL_LIGHTING);
         GL11.glEnable(GL11.GL_DEPTH_TEST);
         GL11.glDisable(GL11.GL_LIGHTING);
         drawStringBorder(xPos + xSize / 2 - this.fontRenderer.getStringWidth(port + "") / 2, yPos + ySize / 2 - 4, xPos + xSize / 2 + this.fontRenderer.getStringWidth(port + "") / 2);
         this.fontRenderer.drawString(port + "", xPos + xSize / 2 - this.fontRenderer.getStringWidth(port + "") / 2, yPos + ySize / 2 - 4, 4210752);
         GL11.glEnable(GL11.GL_LIGHTING);
     }
 	
     protected void drawStringBorder(int x1, int y1, int x2)
     {
         drawRect(x1 - 3, y1 - 3, x2 + 3, y1 + 10, -16777216);
         drawRect(x1 - 2, y1 - 2, x2 + 2, y1 + 9, -1);
     }
     
     @SuppressWarnings("unchecked")
 	@Override
 	public void initGui() {
 	    xPos = width / 2 - xSize / 2;
 	    yPos = height / 2 - ySize / 2;
 	    buttonList.clear();
 	    buttonList.add(new GuiButton(0, xPos + xSize / 2 - 30, yPos + ySize / 2 - 10, 20 , 20 , "-1"));
 	    buttonList.add(new GuiButton(1, xPos + xSize / 2 + 10, yPos + ySize / 2 - 10, 20, 20, "+1"));
 	    buttonList.add(new GuiButton(2, xPos + xSize / 2 - 50, yPos + ySize / 2 - 10, 20 , 20 , "-10"));
 	    buttonList.add(new GuiButton(3, xPos + xSize / 2 + 30, yPos + ySize / 2 - 10, 20, 20, "+10"));
 	    buttonList.add(new GuiButton(4, xPos + xSize / 2 + 40, yPos + 80, 40, 20, "Done"));
 	    super.initGui();
 		
 	}
     
     @Override
 	public void onGuiClosed()
     {
 		te.setPort(this.port);
 		NetworkManager.getInstance().initiateClientTileEntityEvent(te, port);
         super.onGuiClosed();
     }
 
     //update port
     public void updatePort(int number)
     {
         this.port += number;
 
         if (this.port < 0)
         {
             this.port = Settings.maxPort + this.port + 1;
         }
 
         if (this.port > Settings.maxPort)
         {
             this.port -= Settings.maxPort;
         }
     }
 	
 	
 
 }
