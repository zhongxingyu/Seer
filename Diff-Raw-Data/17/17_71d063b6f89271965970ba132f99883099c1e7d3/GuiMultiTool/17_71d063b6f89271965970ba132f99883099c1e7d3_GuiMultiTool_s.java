 package hsb.client.gui;
 
 import hsb.configuration.Settings;
 import hsb.item.ItemHsbMultiTool;
 import hsb.lib.Strings;
 import hsb.lib.Textures;
 import hsb.network.packet.PacketItemUpdate;
 import net.minecraft.client.entity.EntityClientPlayerMP;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.World;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 public class GuiMultiTool extends GuiScreen{
 
     protected int xSize;
     protected int ySize;
     protected ItemStack itemstack;
     protected NBTTagCompound nbttag;
     protected int port = 0;
     protected String name = StatCollector.translateToLocal(Strings.CONTAINER_MULTI_TOOL);
     int xPos;
     int yPos;
     private EntityPlayer player;
     
     protected static final int MODE_PLACE = 0;
     protected static final int MODE_REMOVE = 1;
     protected static final int MODE_WRENCH = 2;
     
     protected static final int MAX_MODE = 2;
     
     private int mode = 0;
     
 	public GuiMultiTool(ItemStack stack, int x, int y, int z, World world, EntityPlayer player) {
 		//size of the gui
 		xSize=176;
 		ySize=105;
 
 		this.itemstack = stack;
 		
         this.player = player;
         nbttag = itemstack.getTagCompound();
         if(nbttag == null) {
         	((ItemHsbMultiTool)stack.getItem()).createNBTTagCompound(itemstack);	        	
         	
         }
         mode = nbttag.getInteger("mode");
         port = nbttag.getInteger("port");
 
 	}
 	@Override
 	protected void actionPerformed(GuiButton guibutton)
 	    {
 	        switch (guibutton.id)
 	        {
 	            case 0:
 	                this.updatePort(-1);
 	                break;
 
 	            case 1:
 	                this.updatePort(1);
 	                break;
 
 	            case 2:
 	                this.updatePort(-10);
 	                break;
 
 	            case 3:
 	                this.updatePort(10);
 	                break;
 
 	            case 4:
 	                this.mc.displayGuiScreen(null);
 	                break;
 	                
 	            case 5:
 	            	//See Declaration
 	                this.mode++;
 	                if(mode > MAX_MODE)
 	                {
 	                	mode = 0;
 	                }
 	                break;
 	        }
 	    }
 	 protected void drawGuiContainerBackgroundLayer(float f)
 	{
 	    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 	    mc.renderEngine.bindTexture(Textures.GUI_MULTI_TOOL);
 	    int j = (width - xSize) / 2;
 	    int k = (height - ySize) / 2;
 	    drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
 	}
 	 protected void drawGuiContainerForegroundLayer()
 	    {
 	    	String modeStr = StatCollector.translateToLocal(Strings.MULTI_TOOL_ERROR);
 	    	switch(mode) 
 	    	{
 	    	case MODE_PLACE:
 	    		modeStr = StatCollector.translateToLocal(Strings.MULTI_TOOL_PLACE);
 	    		break;
 	    	case MODE_REMOVE:
 	    		modeStr = StatCollector.translateToLocal(Strings.MULTI_TOOL_REMOVE);
 	    		break;
 	    	case MODE_WRENCH:
 	    		modeStr = StatCollector.translateToLocal(Strings.MULTI_TOOL_WRENCH);;
 	    		break;
 	    	default:
 	    		modeStr = StatCollector.translateToLocal(Strings.MULTI_TOOL_ERROR);;
 	    		break;
 	    	}
 	        drawStringBorder(this.xSize / 2 - this.fontRenderer.getStringWidth(name + " " + modeStr) / 2, 6, this.xSize / 2 + this.fontRenderer.getStringWidth(name + " " + modeStr) / 2);
 	        this.fontRenderer.drawString(name + " " + modeStr, this.xSize / 2 - this.fontRenderer.getStringWidth(name + " " + modeStr) / 2, 6, 4210752);
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
 	        drawGuiContainerForegroundLayer();
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
 		    buttonList.add(new GuiButton(4, xPos + xSize / 2 + 40, yPos + 80, 40, 20, StatCollector.translateToLocal(Strings.GUI_DONE)));
 		    
 		    buttonList.add(new GuiButton(5, xPos + xSize / 2 - 10, yPos + 80, 40, 20, StatCollector.translateToLocal(Strings.GUI_MODE)));
 		    super.initGui();
 			
 		}
 	    
 	    @Override
 		public void onGuiClosed()
 	    {
 	    	updateNBTTag();
 	        super.onGuiClosed();
 	    }
 
 	    private void updateNBTTag() {
 	    	itemstack.getTagCompound().setInteger("port", port);
 	    	itemstack.getTagCompound().setInteger("mode", mode);
 	    	//Sending Packet to Server
 	    	//POrt
 	    	PacketItemUpdate packetPort = new PacketItemUpdate(this.itemstack.getItem(), "port", port);
 	    	((EntityClientPlayerMP)player).sendQueue.addToSendQueue(packetPort.getPacket());
 	    	//mode
 	    	PacketItemUpdate packetMode = new PacketItemUpdate(this.itemstack.getItem(), "mode", mode);
 	    	((EntityClientPlayerMP)player).sendQueue.addToSendQueue(packetMode.getPacket());  
 	    }
 
 	    //update port
 	    private void updatePort(int number)
 	    {
 	        this.port += number;
 
 	        if (this.port < 0)
 	        {
 	            this.port = Settings.maxPort + this.port + 1;
 	        }
 
 	        if (this.port > Settings.maxPort)
 	        {
 	            this.port -= Settings.maxPort - 1;
 	        }
 	    }
 	}
 
 
