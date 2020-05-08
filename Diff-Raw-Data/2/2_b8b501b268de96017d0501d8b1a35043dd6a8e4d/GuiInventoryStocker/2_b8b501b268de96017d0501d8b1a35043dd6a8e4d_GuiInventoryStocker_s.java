 package kaijin.InventoryStocker;
 
 import org.lwjgl.opengl.GL11;
 import net.minecraft.src.*;
 import net.minecraft.src.forge.*;
 import kaijin.InventoryStocker.*;
 
 public class GuiInventoryStocker extends GuiContainer
 {
     IInventory playerinventory;
     TileEntityInventoryStocker tile;
     // last button clicked
     private GuiButton selectedButton = null;
     
     //making these class wide
     int XOffset = (this.width - this.xSize) / 2; // X offset = Half the difference between screen width and GUI width
     int YOffset = (this.height - this.ySize) / 2; // Y offset = half the difference between screen height and GUI height
 
     // define button class wide
     private GuiButton button = null;
 
     public GuiInventoryStocker(IInventory playerinventory, TileEntityInventoryStocker tileentityinventorystocker)
     {
         super(new ContainerInventoryStocker(playerinventory, tileentityinventorystocker));
         this.playerinventory = playerinventory;
         this.tile = tileentityinventorystocker;
         xSize = 176;
         ySize = 168;
     }
 
     /**
      * Draw the foreground layer for the GuiContainer (everything in front of the items)
      */
     protected void drawGuiContainerForegroundLayer()
     {
         this.fontRenderer.drawString("Input", 8, 6, 4210752);
         this.fontRenderer.drawString(this.tile.getInvName(), 70, 6, 4210752);
         this.fontRenderer.drawString("Output", 116, 6, 4210752);
         this.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
         
         //Add snapshot text
         this.fontRenderer.drawString("Snapshot:", 65, 25, 4210752);
 
         /*
          * validate snapshot state and display valid or invalid
          * This also needs to be expanded to get the valid/invalid state via custom packet in SMP from the server
          */
         if (this.tile.validSnapshot())
         {
 
             this.fontRenderer.drawString("Valid", 70, 35, 0x007500);
         }
         else
         {
             this.fontRenderer.drawString("Invalid", 70, 35, 0xFF0000);
         }
     }
 
     /**
      * Draw the background layer for the GuiContainer (everything behind the items)
      */
     protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
     {
         int GuiTex = this.mc.renderEngine.getTexture("/kaijin/InventoryStocker/stocker.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.renderEngine.bindTexture(GuiTex);
         int XOffset = (this.width - this.xSize) / 2; // X offset = Half the difference between screen width and GUI width
         int YOffset = (this.height - this.ySize) / 2; // Y offset = half the difference between screen height and GUI height
         this.drawTexturedModalRect(XOffset, YOffset, 0, 0, this.xSize, this.ySize);
 
         //GuiButton(int ID, int XOffset, int YOffset, int Width, int Height, string Text)
         //button definition is the full one with width and height
         //defining button below, setting it look enabled and drawing it
         //If you make changes to the button state, you must call .drawButton(mc, XOffset, YOffset)
         button = new GuiButton(0, (this.width /2)-40, YOffset-20, 80, 20, "Clear Snapshot");
         button.enabled = true;
         button.drawButton(mc, XOffset, YOffset);
     }
 
     //Copied mouseClicked function to get our button to make the "click" noise when clicked
     protected void mouseClicked(int par1, int par2, int par3)
     {
         if (par3 == 0)
         {
             if (button.mousePressed(this.mc, par1, par2))
             {
                 this.selectedButton = button;
                 this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
                 this.actionPerformed(button);
             }
         }
     }
     
     /*
      * This function actually handles what happens when you click on a button, by ID
      * This needs to be expanded to handle the situation where we are in SMP by sending and receiving
      * packets to and from the server to communicate the button being pressed and the return status from
      * the server saying that it successfully invalidated the snapshot
      */
     public void actionPerformed(GuiButton button)
     {
         if (!button.enabled)
         {
             return;
         }
         if (button.id == 0)
         {
             System.out.println("Button Pressed, invalidating snapshot");
             this.tile.clearSnapshot();
         }
     }
 }
