 package kaijin.InventoryStocker;
 
 import org.lwjgl.opengl.GL11;
 import net.minecraft.src.*;
 import net.minecraft.src.forge.*;
 import kaijin.InventoryStocker.*;
 
 public class GuiInventoryStocker extends GuiContainer
 {
     IInventory playerinventory;
     TileEntityInventoryStocker tileentityinventorystocker;
 
     public GuiInventoryStocker(IInventory playerinventory, TileEntityInventoryStocker tileentityinventorystocker)
     {
         super(new ContainerInventoryStocker(playerinventory, tileentityinventorystocker));
         this.playerinventory = playerinventory;
         this.tileentityinventorystocker = tileentityinventorystocker;
         xSize = 176;
         ySize = 168;
     }
 
     /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
      */
     protected void drawGuiContainerForegroundLayer()
     {
         this.fontRenderer.drawString("Input", 8, 6, 4210752);
         this.fontRenderer.drawString(this.tileentityinventorystocker.getInvName(), 70, 6, 4210752);
         this.fontRenderer.drawString("Output", 116, 6, 4210752);
 
         this.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
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
 	}
 }
